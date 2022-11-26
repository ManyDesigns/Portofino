package com.manydesigns.portofino.persistence.hibernate;

import com.manydesigns.elements.annotations.Updatable;
import com.manydesigns.portofino.code.CodeBase;
import com.manydesigns.portofino.code.JavaCodeBase;
import com.manydesigns.portofino.database.model.Column;
import com.manydesigns.portofino.database.model.ForeignKey;
import com.manydesigns.portofino.database.model.SequenceGenerator;
import com.manydesigns.portofino.database.model.Table;
import com.manydesigns.portofino.database.model.TableGenerator;
import com.manydesigns.portofino.database.model.*;
import com.manydesigns.portofino.database.model.platforms.DatabasePlatform;
import com.manydesigns.portofino.persistence.hibernate.multitenancy.MultiTenancyImplementation;
import com.manydesigns.portofino.persistence.hibernate.multitenancy.MultiTenancyStrategy;
import javassist.*;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.*;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.hibernate.MappingException;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Immutable;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.BootstrapServiceRegistry;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.registry.classloading.internal.ClassLoaderServiceImpl;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.config.spi.ConfigurationService;
import org.hibernate.engine.config.spi.StandardConverters;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.service.ServiceRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Types;
import java.util.*;
import java.util.stream.Collectors;

public class SessionFactoryBuilder {

    private static final Logger logger = LoggerFactory.getLogger(SessionFactoryBuilder.class);
    //String values for the mapping of boolean values to CHAR/VARCHAR columns
    protected String trueString = "T";
    protected String falseString = "F";
    protected final Database database;
    protected final Events events;
    protected final ClassPool classPool = new ClassPool(ClassPool.getDefault());
    protected final Configuration configuration;
    protected final MultiTenancyImplementation multiTenancyImplementation;
    protected EntityMode entityMode = EntityMode.MAP;

    protected static final Set<String> JAVA_KEYWORDS = new HashSet<>();
    private ConfigurationService configurationService;

    static {
        JAVA_KEYWORDS.add("private");
        JAVA_KEYWORDS.add("protected");
        JAVA_KEYWORDS.add("public");
    }

    public SessionFactoryBuilder(
            Database database, Configuration configuration, Events events,
            MultiTenancyImplementation multiTenancyImplementation) {
        this.database = database;
        this.configuration = configuration;
        this.events = events;
        this.multiTenancyImplementation = multiTenancyImplementation;
        String trueString = database.getTrueString();
        if (trueString != null) {
            this.trueString = "null".equalsIgnoreCase(trueString) ? null : trueString;
        }
        String falseString = database.getFalseString();
        if (falseString != null) {
            this.falseString = "null".equalsIgnoreCase(falseString) ? null : falseString;
        }
        String entityModeName = database.getEntityMode();
        if(!StringUtils.isEmpty(entityModeName)) {
            entityMode = EntityMode.parse(entityModeName);
        }
    }

    public SessionFactoryAndCodeBase buildSessionFactory() {
        try (FileObject root = VFS.getManager().resolveFile("ram://portofino/model/")) {
            return buildSessionFactory(root);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public SessionFactoryAndCodeBase buildSessionFactory(FileObject root) throws Exception {
        List<Table> mappableTables = database.getAllTables();
        mappableTables.removeIf(this::checkInvalidPrimaryKey);
        List<Table> externallyMappedTables = mappableTables.stream().filter(t -> {
            boolean externallyMapped = t.getActualJavaClass() != null;
            if (externallyMapped) {
                logger.debug("Skipping table explicitly mapped with {}", t.getActualJavaClass());
            }
            return externallyMapped;
        }).collect(Collectors.toList());
        mappableTables.removeAll(externallyMappedTables);

        //Use a new classloader as scratch space for Javassist
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        URLClassLoader scratchClassLoader = new URLClassLoader(new URL[0], contextClassLoader);
        Thread.currentThread().setContextClassLoader(scratchClassLoader);

        BootstrapServiceRegistryBuilder bootstrapRegistryBuilder = new BootstrapServiceRegistryBuilder();
        bootstrapRegistryBuilder.applyIntegrator(new EventsIntegrator(events));
        DynamicClassLoaderService classLoaderService = new DynamicClassLoaderService();
        bootstrapRegistryBuilder.applyClassLoaderService(classLoaderService);
        BootstrapServiceRegistry bootstrapServiceRegistry = bootstrapRegistryBuilder.build();
        Map<String, Object> settings = setupConnection();
        ServiceRegistry standardRegistry =
                new StandardServiceRegistryBuilder(bootstrapServiceRegistry).applySettings(settings).build();
        configurationService = standardRegistry.getService(ConfigurationService.class);

        try {
            FileObject databaseDir = root.resolveFile(database.getDatabaseName());
            databaseDir.deleteAll();
            databaseDir.createFolder();
            for (Table table : mappableTables) {
                generateClass(table);
            }
            for (Table table : mappableTables) {
                mapRelationships(table);
            }
            for (Table table : mappableTables) {
                byte[] classFile = getClassFile(table);
                FileObject location = getEntityLocation(root, table);
                try(OutputStream outputStream = location.getContent().getOutputStream()) {
                    outputStream.write(classFile);
                }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
        return buildSessionFactory(
                new JavaCodeBase(root), mappableTables, externallyMappedTables, standardRegistry, classLoaderService);
    }

    protected boolean checkInvalidPrimaryKey(Table table) {
        return checkInvalidPrimaryKey(table, true);
    }

    protected boolean checkInvalidPrimaryKey(Table table, boolean warn) {
        if(table.getPrimaryKey() == null || table.getPrimaryKey().getPrimaryKeyColumns().isEmpty()) {
            if(!ensurePrimaryKey(table)) {
                if (warn) {
                    logger.warn("Skipping table without primary key: {}", table.getQualifiedName());
                }
                return true;
            }
        }
        List<Column> columnPKList = table.getPrimaryKey().getColumns();
        if(!table.getColumns().containsAll(columnPKList)) {
            if(warn) {
                logger.error("Skipping table with primary key that refers to invalid columns: {}", table.getQualifiedName());
            }
            return true;
        }
        return false;
    }

    protected boolean ensurePrimaryKey(Table table) {
        List<Column> idColumns = new ArrayList<>();
        for(Column column : table.getColumns()) {
            column.getAnnotations().forEach(ann -> {
                Class<?> annotationClass = ann.getJavaAnnotationClass();
                if(Id.class.equals(annotationClass)) {
                    idColumns.add(column);
                }
            });
        }
        if(idColumns.isEmpty()) {
            return false;
        } else {
            logger.info("Creating primary key on table {} according to @Id annotations", table.getQualifiedName());
            PrimaryKey pk = new PrimaryKey(table);
            pk.setPrimaryKeyName("synthetic_pk_" + table.getQualifiedName().replace('.', '_'));
            for(Column column : idColumns) {
                pk.add(column);
            }
            table.setPrimaryKey(pk);
            return true;
        }
    }

    public SessionFactoryAndCodeBase buildSessionFactory(
            CodeBase codeBase, List<Table> tablesToMap, List<Table> externallyMappedTables,
            ServiceRegistry standardRegistry, DynamicClassLoaderService classLoaderService) {
        MetadataSources sources = new MetadataSources(standardRegistry);
        List<String> externallyMappedClasses = new ArrayList<>();
        try {
            for (Table table : tablesToMap) {
                Class<?> persistentClass = getPersistentClass(table, codeBase);
                sources.addAnnotatedClass(persistentClass);
                classLoaderService.classes.put(persistentClass.getName(), persistentClass);
                if(entityMode == EntityMode.POJO) {
                    table.setActualJavaClass(persistentClass);
                }
            }
            for(Table table : externallyMappedTables) {
                sources.addAnnotatedClass(table.getActualJavaClass());
                externallyMappedClasses.add(table.getActualJavaClass().getName());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        MetadataBuilder metadataBuilder = sources.getMetadataBuilder();
        Metadata metadata = metadataBuilder.build();
        if (entityMode == EntityMode.MAP) {
            metadata.getEntityBindings().forEach((PersistentClass c) -> {
                if(!externallyMappedClasses.contains(c.getClassName())) {
                    c.setClassName(null);
                    if (c.getIdentifier() instanceof Component) {
                        Component component = (Component) c.getIdentifier();
                        component.setDynamic(true);
                        component.setKey(true);
                        component.setRoleName(component.getComponentClassName() + ".<id>");
                    }
                }
            });
        }
        org.hibernate.boot.SessionFactoryBuilder sessionFactoryBuilder = metadata.getSessionFactoryBuilder();
        return new SessionFactoryAndCodeBase(sessionFactoryBuilder.build(), codeBase);
    }

    protected Map<String, Object> setupConnection() {
        Map<String, Object> settings = new HashMap<>();
        ConnectionProvider connectionProvider = database.getConnectionProvider();
        if(!connectionProvider.isHibernateDialectAutodetected()) {
            settings.put(
                    AvailableSettings.DIALECT,
                    connectionProvider.getActualHibernateDialectName());
        }
        settings.put(AvailableSettings.JPA_METAMODEL_POPULATION, "enabled");
        if(multiTenancyImplementation != null) {
            MultiTenancyStrategy strategy = multiTenancyImplementation.getStrategy();
            if (strategy.requiresMultiTenantConnectionProvider()) {
                setupMultiTenantConnection(connectionProvider, settings);
            } else {
                setupSingleTenantConnection(connectionProvider, settings);
            }
        } else {
            setupSingleTenantConnection(connectionProvider, settings);
        }
        if(database.getSettings() != null) {
            settings.putAll((Map) database.getSettings());
        }
        return settings;
    }

    protected void setupMultiTenantConnection(ConnectionProvider connectionProvider, Map<String, Object> settings) {
        if(connectionProvider instanceof JndiConnectionProvider) {
            logger.debug("JNDI connection provider configured. Using default Hibernate strategy based on JNDI (org.hibernate.engine.jdbc.connections.spi.DataSourceBasedMultiTenantConnectionProviderImpl).");
            return;
        }

        setupSingleTenantConnection(connectionProvider, settings);

        BootstrapServiceRegistryBuilder bootstrapRegistryBuilder = new BootstrapServiceRegistryBuilder();
        BootstrapServiceRegistry bootstrapServiceRegistry = bootstrapRegistryBuilder.build();
        Class<?> connectionProviderClass;
        try(StandardServiceRegistry standardRegistry =
                new StandardServiceRegistryBuilder(bootstrapServiceRegistry).applySettings(settings).build()) {
            Object service = standardRegistry.getService(org.hibernate.engine.jdbc.connections.spi.ConnectionProvider.class);
            connectionProviderClass = service.getClass();
        }

        settings.put(MultiTenancyImplementation.CONNECTION_PROVIDER_CLASS, connectionProviderClass);
        settings.put(AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, multiTenancyImplementation.getClass());
    }

    protected void setupSingleTenantConnection(ConnectionProvider connectionProvider, Map<String, Object> settings) {
        if(connectionProvider instanceof JdbcConnectionProvider) {
            JdbcConnectionProvider jdbcConnectionProvider =
                    (JdbcConnectionProvider) connectionProvider;
            settings.put(AvailableSettings.URL, jdbcConnectionProvider.getActualUrl());
            String driver = jdbcConnectionProvider.getDriver();
            if(driver != null) {
                settings.put(AvailableSettings.DRIVER, driver);
            }
            if(jdbcConnectionProvider.getActualUsername() != null) {
                settings.put(AvailableSettings.USER, jdbcConnectionProvider.getActualUsername());
            }
            if(jdbcConnectionProvider.getActualPassword() != null) {
                settings.put(AvailableSettings.PASS, jdbcConnectionProvider.getActualPassword());
            }
        } else if(connectionProvider instanceof JndiConnectionProvider) {
            JndiConnectionProvider jndiConnectionProvider =
                    (JndiConnectionProvider) connectionProvider;
            settings.put(AvailableSettings.DATASOURCE, jndiConnectionProvider.getJndiResource());
        } else {
            throw new Error("Unsupported connection provider: " + connectionProvider);
        }
    }

    protected FileObject getEntityLocation(FileObject root, Table table) throws FileSystemException {
        return root.resolveFile(entityNameToFileName(getMappedClassName(table)));
    }

    @NotNull
    protected String entityNameToFileName(String entityName) {
        return entityName.replace('.', FileName.SEPARATOR_CHAR) + ".class";
    }

    @NotNull
    public String getMappedClassName(Table table) {
        return getMappedClassName(table, entityMode);
    }

    @NotNull
    public static String getMappedClassName(Table table, EntityMode entityMode) {
        return table.getActualJavaClass() == null ?
                deriveMappedClassName(table, entityMode) :
                table.getActualJavaClass().getName();
    }

    @NotNull
    public static String deriveMappedClassName(Table table, EntityMode entityMode) {
        String packageName = table.getSchema().getQualifiedName().toLowerCase();
        String className = toJavaLikeName(table.getActualEntityName());
        if(Character.isDigit(className.charAt(0))) {
            className = "_" + className;
        }
        String fullName = ensureValidJavaName(packageName + "." + className);
        for(Table other : table.getSchema().getDatabase().getAllTables()) {
            if(other != table && other.getActualJavaClass() != null && other.getActualJavaClass().getName().equals(fullName)) {
                fullName += "_1";
            }
        }
        return fullName;
    }

    public static String ensureValidJavaName(String fullName) {
        String[] tokens = fullName.split("\\.");
        for(int i = 0; i < tokens.length; i++) {
            if(JAVA_KEYWORDS.contains(tokens[i])) {
                tokens[i] = tokens[i] + "_";
            }
        }
        return StringUtils.join(tokens, ".");
    }

    @NotNull
    protected static String toJavaLikeName(String name) {
        return Arrays.stream(StringUtils.split(name.toLowerCase(), "_- "))
                .map(StringUtils::capitalize)
                .collect(Collectors.joining());
    }

    public Class<?> getPersistentClass(Table table, CodeBase codeBase) throws IOException, ClassNotFoundException {
        Class<?> javaClass = table.getActualJavaClass();
        if(javaClass != null) {
            return javaClass;
        } else {
            return codeBase.loadClass(getMappedClassName(table));
        }
    }

    public byte[] getClassFile(Table table) throws NotFoundException, IOException, CannotCompileException {
        return getMappedClass(table).toBytecode();
    }

    protected Annotation makeParameterAnnotation(String name, String value, ConstPool constPool) {
        Annotation annotation = new Annotation(org.hibernate.annotations.Parameter.class.getName(), constPool);
        annotation.addMemberValue("name", new StringMemberValue(name, constPool));
        annotation.addMemberValue("value", new StringMemberValue(value, constPool));
        return annotation;
    }

    public CtClass generateClass(Table table) throws CannotCompileException, NotFoundException {
        CtClass cc = classPool.makeClass(getMappedClassName(table));
        ClassFile ccFile = cc.getClassFile();
        ConstPool constPool = ccFile.getConstPool();
        AnnotationsAttribute classAnnotations = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
        configureAnnotations(table, constPool, classAnnotations);
        ccFile.addAttribute(classAnnotations);
        setupColumns(table, cc, constPool);

        if(entityMode == EntityMode.POJO) {
            defineEqualsAndHashCode(table, cc);
        }
        return cc;
    }

    protected void defineEqualsAndHashCode(Table table, CtClass cc) throws CannotCompileException {
        List<Column> columnPKList = table.getPrimaryKey().getColumns();
        String equalsMethod =
                "public boolean equals(Object other) {" +
                        "    if(!(other instanceof " + cc.getName() + ")) {" +
                        "        return false;" +
                        "    }" +
                        cc.getName() + " castOther = (" + cc.getName() + ") other;";
        String hashCodeMethod =
                "public int hashCode() {" +
                        "    return java.util.Objects.hash(new java.lang.Object[] {";

        boolean first = true;
        for (Column c : columnPKList) {
            equalsMethod +=
                    "    if(!java.util.Objects.equals(this." + c.getActualPropertyName() + ", castOther." + c.getActualPropertyName() + ")) {" +
                            "        return false;" +
                            "    }";
            if (first) {
                first = false;
            } else {
                hashCodeMethod += ", ";
            }
            hashCodeMethod += c.getActualPropertyName();
        }

        equalsMethod += "return true; }";
        hashCodeMethod += "});}";

        cc.addMethod(CtNewMethod.make(equalsMethod, cc));
        cc.addMethod(CtNewMethod.make(hashCodeMethod, cc));
    }

    protected void configureAnnotations(Table table, ConstPool constPool, AnnotationsAttribute classAnnotations) throws NotFoundException, CannotCompileException {
        Annotation annotation;

        annotation = new Annotation(jakarta.persistence.Table.class.getName(), constPool);
        annotation.addMemberValue("name",
                new StringMemberValue(jpaEscape(table.getTableName(), false), constPool));
        if(multiTenancyImplementation == null ||
                multiTenancyImplementation.getStrategy() != MultiTenancyStrategy.SEPARATE_SCHEMA) {
            //Don't configure the schema name if we're using schema-based multitenancy
            String schemaName = table.getSchema().getActualSchemaName();
            annotation.addMemberValue("schema",
                    new StringMemberValue(jpaEscape(schemaName, false), constPool));
        }
        classAnnotations.addAnnotation(annotation);

        annotation = new Annotation(Entity.class.getName(), constPool);
        annotation.addMemberValue("name", new StringMemberValue(table.getActualEntityName(), constPool));
        classAnnotations.addAnnotation(annotation);

        table.getAnnotations().forEach(ann -> {
            Class<?> annotationClass = ann.getJavaAnnotationClass();
            if(jakarta.persistence.Table.class.equals(annotationClass) || Entity.class.equals(annotationClass)) {
                logger.debug("@Table or @Entity specified on table {}, skipping annotation {}",
                        table.getQualifiedName(), annotationClass);
                return;
            }
            Annotation classAnn = convertAnnotation(constPool, ann);
            if (classAnn != null) {
                classAnnotations.addAnnotation(classAnn);
            }
            if(annotationClass == Updatable.class && !((Updatable) ann.getJavaAnnotation()).value()) {
                classAnnotations.addAnnotation(new Annotation(Immutable.class.getName(), constPool));
            }
        });
    }

    @Nullable
    protected Annotation convertAnnotation(
            ConstPool constPool, com.manydesigns.portofino.model.Annotation portofinoAnnotation) {
        java.lang.annotation.Annotation javaAnnotation = portofinoAnnotation.getJavaAnnotation();
        if(javaAnnotation == null) {
            return null;
        }
        Class<?> annotationType = javaAnnotation.annotationType();
        Annotation annotation = new Annotation(annotationType.getName(), constPool);
        for(Method method : annotationType.getMethods()) {
            if(Modifier.isStatic(method.getModifiers()) ||
               method.getDeclaringClass() == Object.class ||
               method.getDeclaringClass() == java.lang.annotation.Annotation.class ||
               method.getParameterCount() > 0) {
                logger.debug("Skipping " + method);
                continue;
            }
            try {
                Object result = method.invoke(javaAnnotation);
                if(result == null) {
                    continue;
                }
                Class<?> returnType = method.getReturnType();
                if(returnType == String.class) {
                    annotation.addMemberValue(method.getName(), new StringMemberValue((String) result, constPool));
                } else if(returnType == Integer.class || returnType == Integer.TYPE) {
                    annotation.addMemberValue(method.getName(), new IntegerMemberValue(constPool, (Integer) result));
                } else if(returnType == Long.class || returnType == Long.TYPE) {
                    annotation.addMemberValue(method.getName(), new LongMemberValue((Long) result, constPool));
                } else if(returnType == Float.class || returnType == Float.TYPE) {
                    annotation.addMemberValue(method.getName(), new DoubleMemberValue((Double) result, constPool));
                } else if(returnType == Double.class || returnType == Double.TYPE) {
                    annotation.addMemberValue(method.getName(), new DoubleMemberValue((Double) result, constPool));
                } else if(returnType == Character.class || returnType == Character.TYPE) {
                    annotation.addMemberValue(method.getName(), new CharMemberValue((Character) result, constPool));
                } else if(returnType == Boolean.class || returnType == Boolean.TYPE) {
                    annotation.addMemberValue(method.getName(), new BooleanMemberValue((Boolean) result, constPool));
                } else if(returnType == Class.class) {
                    annotation.addMemberValue(method.getName(), new ClassMemberValue(((Class) result).getName(), constPool));
                } else if(returnType.isEnum()) {
                    EnumMemberValue value = new EnumMemberValue(constPool);
                    value.setType(returnType.getName());
                    value.setValue(((Enum<?>) result).name());
                    annotation.addMemberValue(method.getName(), value);
                }
            } catch (Exception e) {
                logger.warn("Skipping " + method + " as it errored", e);
            }
        }
        return annotation;
    }

    protected void setupColumns(Table table, CtClass cc, ConstPool constPool) throws CannotCompileException, NotFoundException {
        List<Column> columnPKList = table.getPrimaryKey().getColumns();
        Annotation annotation;
        for(Column column : table.getColumns()) {
            String propertyName = column.getActualPropertyName();
            Class<?> javaType = column.getActualJavaType();
            if(javaType == null) {
                logger.error("Cannot determine default Java type for table: {}, column: {}, jdbc type: {}, type name: {}. Skipping column.",
                        table.getTableName(),
                        column.getColumnName(),
                        column.getJdbcType(),
                        column.getJavaType());
                continue;
            }
            CtField field = new CtField(classPool.get(javaType.getName()), propertyName, cc);
            AnnotationsAttribute fieldAnnotations = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
            annotation = new Annotation(jakarta.persistence.Column.class.getName(), constPool);
            annotation.addMemberValue("name",
                    new StringMemberValue(jpaEscape(column.getColumnName(), true), constPool));
            annotation.addMemberValue("nullable", new BooleanMemberValue(column.isNullable(), constPool));
            if(column.getLength() != null) {
                annotation.addMemberValue("precision", new IntegerMemberValue(constPool, column.getLength()));
                annotation.addMemberValue("length", new IntegerMemberValue(constPool, column.getLength()));
            }
            if(column.getScale() != null) {
                annotation.addMemberValue("scale", new IntegerMemberValue(constPool, column.getScale()));
            }
            fieldAnnotations.addAnnotation(annotation);

            if(columnPKList.contains(column)) {
                annotation.addMemberValue("updatable", new BooleanMemberValue(false, constPool));
                annotation = new Annotation(Id.class.getName(), constPool);
                fieldAnnotations.addAnnotation(annotation);
                if(column.isAutoincrement()) {
                    setupIdentityGenerator(fieldAnnotations, constPool);
                } else {
                    PrimaryKeyColumn pkColumn = table.getPrimaryKey().findPrimaryKeyColumnByName(column.getColumnName());
                    Generator generator = pkColumn.getGenerator();
                    if(generator != null) {
                        setupNonIdentityGenerator(table, fieldAnnotations, generator, constPool);
                    }
                }
            }

            setupColumnType(column, fieldAnnotations, constPool);

            column.getAnnotations().forEach(ann -> {
                Class<?> annotationClass = ann.getJavaAnnotationClass();
                if(jakarta.persistence.Column.class.equals(annotationClass) ||
                   Id.class.equals(annotationClass) ||
                   org.hibernate.annotations.Type.class.equals(annotationClass)) {
                    logger.debug("@Column or @Id or @Type specified on column {}, ignoring annotation {}",
                            column.getQualifiedName(), annotationClass);
                    return;
                }
                Annotation fieldAnn = convertAnnotation(constPool, ann);
                if (fieldAnn != null) {
                    fieldAnnotations.addAnnotation(fieldAnn);
                }
            });

            field.getFieldInfo().addAttribute(fieldAnnotations);
            field.setModifiers(javassist.Modifier.PROTECTED);
            cc.addField(field);
            String accessorName = propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
            cc.addMethod(CtNewMethod.getter("get" + accessorName, field));
            cc.addMethod(CtNewMethod.setter("set" + accessorName, field));
        }
    }

    /**
     * See <a href="https://vladmihalcea.com/escape-sql-reserved-keywords-jpa-hibernate/">https://vladmihalcea.com/escape-sql-reserved-keywords-jpa-hibernate/</a>
     */
    public String jpaEscape(String name, boolean column) {
        boolean needsQuoting = !configurationService.getSetting(
                AvailableSettings.GLOBALLY_QUOTED_IDENTIFIERS, StandardConverters.BOOLEAN, false);
        if (column && !needsQuoting) {
            boolean skipColumns = configurationService.getSetting(
                    AvailableSettings.GLOBALLY_QUOTED_IDENTIFIERS_SKIP_COLUMN_DEFINITIONS, StandardConverters.BOOLEAN, false);
            if (skipColumns) {
                needsQuoting = true;
            }
        }
        if (needsQuoting) {
            return "\"" + name + "\"";
        } else {
            return name;
        }
    }

    protected void setupIdentityGenerator(AnnotationsAttribute fieldAnnotations, ConstPool constPool) {
        Annotation annotation = makeGeneratedValueAnnotation(GenerationType.IDENTITY, constPool);
        fieldAnnotations.addAnnotation(annotation);
    }

    protected void setupNonIdentityGenerator(Table table, AnnotationsAttribute fieldAnnotations, Generator generator, ConstPool constPool) {
        String generatorName = table.getQualifiedName() + "_generator";
        if (generator instanceof IncrementGenerator) {
            addGeneratedValueAnnotation(GenerationType.AUTO, generatorName, fieldAnnotations, constPool);
            Annotation annotation = new Annotation(GenericGenerator.class.getName(), constPool);
            annotation.addMemberValue("name", new StringMemberValue(generatorName, constPool));
            annotation.addMemberValue("strategy", new StringMemberValue("increment", constPool));
            fieldAnnotations.addAnnotation(annotation);
        } else if (generator instanceof SequenceGenerator) {
            addGeneratedValueAnnotation(GenerationType.SEQUENCE, generatorName, fieldAnnotations, constPool);
            Annotation annotation = new Annotation(jakarta.persistence.SequenceGenerator.class.getName(), constPool);
            annotation.addMemberValue("name", new StringMemberValue(generatorName, constPool));
            annotation.addMemberValue("sequenceName", new StringMemberValue(((SequenceGenerator) generator).getName(), constPool));
            fieldAnnotations.addAnnotation(annotation);
        } else if (generator instanceof TableGenerator) {
            TableGenerator tableGenerator = (TableGenerator) generator;
            addGeneratedValueAnnotation(GenerationType.TABLE, generatorName, fieldAnnotations, constPool);
            Annotation annotation = new Annotation(jakarta.persistence.TableGenerator.class.getName(), constPool);
            annotation.addMemberValue("name", new StringMemberValue(generatorName, constPool));
            annotation.addMemberValue("schema", new StringMemberValue(table.getSchema().getActualSchemaName(), constPool));
            annotation.addMemberValue("table", new StringMemberValue(tableGenerator.getTable(), constPool));
            annotation.addMemberValue("pkColumnName", new StringMemberValue(tableGenerator.getKeyColumn(), constPool));
            annotation.addMemberValue("pkColumnValue", new StringMemberValue(tableGenerator.getKeyValue(), constPool));
            annotation.addMemberValue("valueColumnName", new StringMemberValue(tableGenerator.getValueColumn(), constPool));
            //TODO support additional parameters for the generator?
            fieldAnnotations.addAnnotation(annotation);
        } else {
            throw new IllegalArgumentException("Unsupported generator: " + generator);
        }
    }

    protected void addGeneratedValueAnnotation(GenerationType generationType, String generatorName, AnnotationsAttribute fieldAnnotations, ConstPool constPool) {
        Annotation annotation = makeGeneratedValueAnnotation(generationType, constPool);
        annotation.addMemberValue("generator", new StringMemberValue(generatorName, constPool));
        fieldAnnotations.addAnnotation(annotation);
    }

    @NotNull
    protected Annotation makeGeneratedValueAnnotation(GenerationType identity, ConstPool constPool) {
        Annotation annotation = new Annotation(GeneratedValue.class.getName(), constPool);
        EnumMemberValue value = new EnumMemberValue(constPool);
        value.setType(GenerationType.class.getName());
        value.setValue(identity.name());
        annotation.addMemberValue("strategy", value);
        return annotation;
    }

    protected void setupColumnType(Column column, AnnotationsAttribute fieldAnnotations, ConstPool constPool) {
        Annotation annotation;
        if(Boolean.class.equals(column.getActualJavaType())) {
            if(column.getJdbcType() == Types.CHAR || column.getJdbcType() == Types.VARCHAR) {
                Annotation stringBooleanType = new Annotation(org.hibernate.annotations.Type.class.getName(), constPool);
                stringBooleanType.addMemberValue("value", new ClassMemberValue(StringBooleanType.class.getName(), constPool));
                ArrayMemberValue parameters = new ArrayMemberValue(new AnnotationMemberValue(constPool), constPool);
                parameters.setValue(new AnnotationMemberValue[] {
                        new AnnotationMemberValue(makeParameterAnnotation("trueString", trueString, constPool), constPool),
                        new AnnotationMemberValue(makeParameterAnnotation("falseString", falseString, constPool), constPool)
                });
                stringBooleanType.addMemberValue("parameters", parameters);
                fieldAnnotations.addAnnotation(stringBooleanType);
            }
        } else {
            DatabasePlatform.TypeDescriptor databaseSpecificType =
                    database.getConnectionProvider().getDatabasePlatform().getDatabaseSpecificType(column);
            if(databaseSpecificType != null) {
                annotation = new Annotation(org.hibernate.annotations.Type.class.getName(), constPool);
                annotation.addMemberValue("type", new StringMemberValue(databaseSpecificType.name, constPool));
                ArrayMemberValue parameters = new ArrayMemberValue(new AnnotationMemberValue(constPool), constPool);
                List<AnnotationMemberValue> typeParams = new ArrayList<>();
                databaseSpecificType.parameters.forEach((key, value) -> {
                    Annotation typeParam = makeParameterAnnotation(
                            key.toString(), String.valueOf(value), constPool);
                    typeParams.add(new AnnotationMemberValue(typeParam, constPool));
                });
                parameters.setValue(typeParams.toArray(new AnnotationMemberValue[0]));
                annotation.addMemberValue("parameters", parameters);
                fieldAnnotations.addAnnotation(annotation);
            }
        }
    }

    public void mapRelationships(Table table) throws NotFoundException, CannotCompileException {
        for(ForeignKey foreignKey : table.getForeignKeys()) {
            if(checkValidFk(foreignKey)) {
                mapManyToOne(foreignKey);
                mapOneToMany(foreignKey);
            }
        }
    }

    protected void mapManyToOne(ForeignKey foreignKey) throws CannotCompileException, NotFoundException {
        CtClass cc = getMappedClass(foreignKey.getFromTable());
        ClassFile ccFile = cc.getClassFile();
        ConstPool constPool = ccFile.getConstPool();
        Table toTable = foreignKey.getToTable();
        CtField field = new CtField(getMappedClass(toTable), foreignKey.getActualOnePropertyName(), cc);
        cc.addField(field);

        AnnotationsAttribute fieldAnnotations = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
        Annotation annotation;
        annotation = new Annotation(ManyToOne.class.getName(), constPool);
        fieldAnnotations.addAnnotation(annotation);
        List<MemberValue> joinColumnsValue = new ArrayList<>();
        for(Reference reference : foreignKey.getReferences()) {
            String fromColumn = reference.getActualFromColumn().getColumnName();
            String toColumn = reference.getActualToColumn().getColumnName();
            annotation = new Annotation(JoinColumn.class.getName(), constPool);
            annotation.addMemberValue("insertable", new BooleanMemberValue(false, constPool));
            annotation.addMemberValue("updatable", new BooleanMemberValue(false, constPool));
            annotation.addMemberValue("name", new StringMemberValue(jpaEscape(fromColumn, true), constPool));
            annotation.addMemberValue("referencedColumnName", new StringMemberValue(jpaEscape(toColumn, true), constPool));
            joinColumnsValue.add(new AnnotationMemberValue(annotation, constPool));
        }
        annotation = new Annotation(JoinColumns.class.getName(), constPool);
        ArrayMemberValue joinColumns = new ArrayMemberValue(new AnnotationMemberValue(constPool), constPool);
        joinColumns.setValue(joinColumnsValue.toArray(new MemberValue[0]));
        annotation.addMemberValue("value", joinColumns);

        finalizeRelationshipProperty(cc, field, annotation, fieldAnnotations);
    }

    protected boolean checkValidFk(ForeignKey foreignKey) {
        Table toTable = foreignKey.getToTable();
        if(toTable == null) {
            logger.error("The foreign key " + foreignKey.getQualifiedName() + " does not refer to any table.");
            return false;
        }
        if(checkInvalidPrimaryKey(toTable, false)) {
            logger.error("The foreign key " + foreignKey.getQualifiedName() + " refers to a table with absent or invalid primary key.");
            return false;
        }
        //Check that referenced columns coincide with the primary key
        Set<Column> fkColumns = new HashSet<>();
        Set<Column> pkColumns = new HashSet<>(toTable.getPrimaryKey().getColumns());
        for(Reference reference : foreignKey.getReferences()) {
            fkColumns.add(reference.getActualToColumn());
        }
        if(!fkColumns.equals(pkColumns)) {
            logger.error(
                    "The foreign key " + foreignKey.getQualifiedName() + " does not refer to " +
                    "the exact primary key of table " + toTable.getQualifiedName() +
                    ", this is not supported.");
            return false;
        }
        try {
            getMappedClass(toTable);
        } catch (NotFoundException e) {
            logger.error(
                    "The foreign key " + foreignKey.getQualifiedName() + " refers to unmapped table " +
                    toTable.getQualifiedName() + ", skipping.");
            return false;
        }
        return true;
    }

    protected void mapOneToMany(ForeignKey foreignKey) throws NotFoundException, CannotCompileException {
        Table toTable = foreignKey.getToTable();
        if (toTable.getActualJavaClass() != null) {
            return;
        }
        CtClass cc = getMappedClass(toTable);
        ClassFile ccFile = cc.getClassFile();
        ConstPool constPool = ccFile.getConstPool();
        Table fromTable = foreignKey.getFromTable();
        CtField field = new CtField(classPool.get(List.class.getName()), foreignKey.getActualManyPropertyName(), cc);
        String referencedClassName = getMappedClassName(fromTable);
        field.setGenericSignature("Ljava/util/List<L" + referencedClassName.replace('.', '/') + ";>;");
        cc.addField(field);

        AnnotationsAttribute fieldAnnotations = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
        Annotation annotation;
        annotation = new Annotation(OneToMany.class.getName(), constPool);
        annotation.addMemberValue("targetEntity", new ClassMemberValue(referencedClassName, constPool));
        annotation.addMemberValue("mappedBy", new StringMemberValue(foreignKey.getActualOnePropertyName(), constPool));
        //TODO cascade?

        finalizeRelationshipProperty(cc, field, annotation, fieldAnnotations);
    }

    protected void finalizeRelationshipProperty(
            CtClass cc, CtField field, Annotation annotation, AnnotationsAttribute fieldAnnotations)
            throws CannotCompileException {
        fieldAnnotations.addAnnotation(annotation);
        field.getFieldInfo().addAttribute(fieldAnnotations);

        String accessorName = toJavaLikeName(field.getName());
        cc.addMethod(CtNewMethod.getter("get" + accessorName, field));
        cc.addMethod(CtNewMethod.setter("set" + accessorName, field));
    }

    protected CtClass getMappedClass(Table table) throws NotFoundException {
        return classPool.get(getMappedClassName(table));
    }

    public static class DynamicClassLoaderService extends ClassLoaderServiceImpl {

        public final Map<String, Class> classes = new HashMap<>();

        @Override
        public <T> Class<T> classForName(String className) {
            Class theClass = classes.get(className);
            if(theClass != null) {
                return theClass;
            } else {
                return super.classForName(className);
            }
        }
    }

    public EntityMode getEntityMode() {
        return entityMode;
    }
}
