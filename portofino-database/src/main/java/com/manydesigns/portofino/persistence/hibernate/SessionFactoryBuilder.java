package com.manydesigns.portofino.persistence.hibernate;

import com.manydesigns.elements.annotations.Updatable;
import com.manydesigns.portofino.code.CodeBase;
import com.manydesigns.portofino.code.JavaCodeBase;
import com.manydesigns.portofino.model.database.Column;
import com.manydesigns.portofino.model.database.ForeignKey;
import com.manydesigns.portofino.model.database.SequenceGenerator;
import com.manydesigns.portofino.model.database.Table;
import com.manydesigns.portofino.model.database.TableGenerator;
import com.manydesigns.portofino.model.database.*;
import com.manydesigns.portofino.model.database.platforms.DatabasePlatform;
import javassist.*;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.hibernate.EntityMode;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.BootstrapServiceRegistry;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.registry.classloading.internal.ClassLoaderServiceImpl;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.service.ServiceRegistry;
import org.jadira.usertype.dateandtime.joda.PersistentDateTime;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
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
    protected final ClassPool classPool = new ClassPool(ClassPool.getDefault());
    protected EntityMode entityMode = EntityMode.MAP;

    public SessionFactoryBuilder(Database database) {
        this.database = database;
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

        try {

            CtClass baseClass = generateBaseClass();
            FileObject baseClassFile = root.resolveFile(
                    database.getDatabaseName() + FileName.SEPARATOR_CHAR + "BaseEntity.class");
            try(OutputStream outputStream = baseClassFile.getContent().getOutputStream()) {
                outputStream.write(baseClass.toBytecode());
            }

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
        return buildSessionFactory(new JavaCodeBase(root), mappableTables, externallyMappedTables);
    }

    protected boolean checkInvalidPrimaryKey(Table table) {
        return checkInvalidPrimaryKey(table, true);
    }

    protected boolean checkInvalidPrimaryKey(Table table, boolean warn) {
        if(table.getPrimaryKey() == null || table.getPrimaryKey().getPrimaryKeyColumns().isEmpty()) {
            if(warn) {
                logger.warn("Skipping table without primary key: {}", table.getQualifiedName());
            }
            return true;
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

    public SessionFactoryAndCodeBase buildSessionFactory(CodeBase codeBase, List<Table> tablesToMap, List<Table> externallyMappedTables) {
        BootstrapServiceRegistryBuilder bootstrapRegistryBuilder = new BootstrapServiceRegistryBuilder();
        DynamicClassLoaderService classLoaderService = new DynamicClassLoaderService();
        bootstrapRegistryBuilder.applyClassLoaderService(classLoaderService);
        BootstrapServiceRegistry bootstrapServiceRegistry = bootstrapRegistryBuilder.build();
        Map<String, Object> settings = new HashMap<>();
        setupConnection(settings);
        ServiceRegistry standardRegistry =
                new StandardServiceRegistryBuilder(bootstrapServiceRegistry).applySettings(settings).build();
        MetadataSources sources = new MetadataSources(standardRegistry);
        List<String> externallyMappedClasses = new ArrayList<>();
        try {
            for (Table table : tablesToMap) {
                Class persistentClass = getPersistentClass(table, codeBase);
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
                        component.setComponentClassName(null);
                        component.setDynamic(true);
                    }
                }
            });
        }
        org.hibernate.boot.SessionFactoryBuilder sessionFactoryBuilder = metadata.getSessionFactoryBuilder();
        return new SessionFactoryAndCodeBase(sessionFactoryBuilder.build(), codeBase);
    }

    protected void setupConnection(Map<String, Object> settings) {
        ConnectionProvider connectionProvider = database.getConnectionProvider();
        if(!connectionProvider.isHibernateDialectAutodetected()) {
            settings.put(
                    "hibernate.dialect",
                    connectionProvider.getActualHibernateDialectName());
        }
        if(connectionProvider instanceof JdbcConnectionProvider) {
            JdbcConnectionProvider jdbcConnectionProvider =
                    (JdbcConnectionProvider) connectionProvider;
            settings.put("hibernate.connection.url", jdbcConnectionProvider.getActualUrl());
            settings.put("hibernate.connection.driver_class", jdbcConnectionProvider.getDriver());
            if(jdbcConnectionProvider.getActualUsername() != null) {
                settings.put("hibernate.connection.username", jdbcConnectionProvider.getActualUsername());
            }
            if(jdbcConnectionProvider.getActualPassword() != null) {
                settings.put("hibernate.connection.password", jdbcConnectionProvider.getActualPassword());
            }
        } else if(connectionProvider instanceof JndiConnectionProvider) {
            JndiConnectionProvider jndiConnectionProvider =
                    (JndiConnectionProvider) connectionProvider;
            settings.put("hibernate.connection.datasource", jndiConnectionProvider.getJndiResource());
        } else {
            throw new Error("Unsupported connection provider: " + connectionProvider);
        }
        settings.put("hibernate.ejb.metamodel.population", "enabled");
        //TODO evaluate if they're still applicable:
        //  .setProperty("hibernate.current_session_context_class", "org.hibernate.context.internal.ThreadLocalSessionContext")
        //  .setProperty("org.hibernate.hql.ast.AST", "true")
        //  .setProperty("hibernate.globally_quoted_identifiers", "false");
    }

    protected FileObject getEntityLocation(FileObject root, Table table) throws FileSystemException {
        return root.resolveFile(entityNameToFileName(table));
    }

    @NotNull
    protected String entityNameToFileName(Table table) {
        return getMappedClassName(table).replace('.', FileName.SEPARATOR_CHAR) + ".class";
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
        String className = table.getActualEntityName();
        if(entityMode == EntityMode.POJO) {
            className = Arrays.stream(StringUtils.split(className, "_- "))
                    .map(StringUtils::capitalize)
                    .collect(Collectors.joining());
        } else {
            className = className.replaceAll("-|\\h", "");
        }
        if(Character.isDigit(className.charAt(0))) {
            className = "_" + className;
        }
        String fullName = packageName + "." + className;
        for(Table other : table.getSchema().getDatabase().getAllTables()) {
            if(other != table && other.getActualJavaClass() != null && other.getActualJavaClass().getName().equals(fullName)) {
                fullName += "_1";
            }
        }
        return fullName;
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

    public CtClass generateBaseClass() throws NotFoundException {
        CtClass cc = classPool.makeClass(getBaseEntityName());
        cc.addInterface(classPool.get(Serializable.class.getName()));
        ClassFile ccFile = cc.getClassFile();
        ConstPool constPool = ccFile.getConstPool();
        AnnotationsAttribute classAnnotations = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
        ccFile.addAttribute(classAnnotations);
        javassist.bytecode.annotation.Annotation annotation;

        annotation = new javassist.bytecode.annotation.Annotation(MappedSuperclass.class.getName(), constPool);
        classAnnotations.addAnnotation(annotation);

        Annotation stringBooleanType = new Annotation(TypeDef.class.getName(), constPool);
        stringBooleanType.addMemberValue("name", new StringMemberValue(StringBooleanType.class.getName(), constPool));
        stringBooleanType.addMemberValue("typeClass", new ClassMemberValue(StringBooleanType.class.getName(), constPool));
        ArrayMemberValue parameters = new ArrayMemberValue(new AnnotationMemberValue(constPool), constPool);
        parameters.setValue(new AnnotationMemberValue[] {
                new AnnotationMemberValue(makeParameterAnnotation("trueString", trueString, constPool), constPool),
                new AnnotationMemberValue(makeParameterAnnotation("falseString", falseString, constPool), constPool)
        });
        stringBooleanType.addMemberValue("parameters", parameters);

        annotation = new javassist.bytecode.annotation.Annotation(TypeDefs.class.getName(), constPool);
        ArrayMemberValue typeDefs = new ArrayMemberValue(new AnnotationMemberValue(constPool), constPool);
        typeDefs.setValue(new AnnotationMemberValue[] {
                new AnnotationMemberValue(stringBooleanType, constPool)
        });
        annotation.addMemberValue("value", typeDefs);
        classAnnotations.addAnnotation(annotation);
        return cc;
    }

    protected Annotation makeParameterAnnotation(String name, String value, ConstPool constPool) {
        Annotation annotation = new Annotation(org.hibernate.annotations.Parameter.class.getName(), constPool);
        annotation.addMemberValue("name", new StringMemberValue(name, constPool));
        annotation.addMemberValue("value", new StringMemberValue(value, constPool));
        return annotation;
    }

    @NotNull
    public String getBaseEntityName() {
        return database.getDatabaseName() + ".BaseEntity";
    }

    public CtClass generateClass(Table table) throws CannotCompileException, NotFoundException {
        CtClass cc = classPool.makeClass(getMappedClassName(table));
        cc.setSuperclass(classPool.get(getBaseEntityName()));
        ClassFile ccFile = cc.getClassFile();
        ConstPool constPool = ccFile.getConstPool();
        AnnotationsAttribute classAnnotations = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
        javassist.bytecode.annotation.Annotation annotation;

        String schemaName = table.getSchema().getActualSchemaName();
        annotation = new javassist.bytecode.annotation.Annotation(javax.persistence.Table.class.getName(), constPool);
        annotation.addMemberValue("name", new StringMemberValue(jpaEscape(table.getTableName()), constPool));
        annotation.addMemberValue("schema", new StringMemberValue(jpaEscape(schemaName), constPool));
        classAnnotations.addAnnotation(annotation);

        annotation = new javassist.bytecode.annotation.Annotation(javax.persistence.Entity.class.getName(), constPool);
        annotation.addMemberValue("name", new StringMemberValue(table.getActualEntityName(), constPool));
        classAnnotations.addAnnotation(annotation);

        table.getAnnotations().forEach(ann -> {
            Class annotationClass = ann.getJavaAnnotationClass();
            if(javax.persistence.Table.class.equals(annotationClass) || Entity.class.equals(annotationClass)) {
                logger.warn("@Table or @Entity specified on table {}, skipping annotation {}", table.getQualifiedName(), annotationClass);
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
        ccFile.addAttribute(classAnnotations);
        setupColumns(table, cc, constPool);
        return cc;
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
            CtField field = new CtField(classPool.get(column.getActualJavaType().getName()), propertyName, cc);
            AnnotationsAttribute fieldAnnotations = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
            annotation = new Annotation(javax.persistence.Column.class.getName(), constPool);
            annotation.addMemberValue("name", new StringMemberValue(jpaEscape(column.getColumnName()), constPool));
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
                if(javax.persistence.Column.class.equals(annotationClass) ||
                   Id.class.equals(annotationClass) ||
                   org.hibernate.annotations.Type.class.equals(annotationClass)) {
                    logger.warn("@Column or @Id or @Type specified on column {}, skipping annotation {}", column.getQualifiedName(), annotationClass);
                    return;
                }
                Annotation fieldAnn = convertAnnotation(constPool, ann);
                if (fieldAnn != null) {
                    fieldAnnotations.addAnnotation(fieldAnn);
                }
            });


            field.getFieldInfo().addAttribute(fieldAnnotations);
            cc.addField(field);
            String accessorName = propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
            cc.addMethod(CtNewMethod.getter("get" + accessorName, field));
            cc.addMethod(CtNewMethod.setter("set" + accessorName, field));
        }
    }

    /**
     * https://vladmihalcea.com/escape-sql-reserved-keywords-jpa-hibernate/
     */
    public String jpaEscape(String columnName) {
        return "\"" + columnName + "\"";
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
            Annotation annotation = new Annotation(javax.persistence.SequenceGenerator.class.getName(), constPool);
            annotation.addMemberValue("name", new StringMemberValue(generatorName, constPool));
            annotation.addMemberValue("sequenceName", new StringMemberValue(((SequenceGenerator) generator).getName(), constPool));
            fieldAnnotations.addAnnotation(annotation);
        } else if (generator instanceof TableGenerator) {
            TableGenerator tableGenerator = (TableGenerator) generator;
            addGeneratedValueAnnotation(GenerationType.TABLE, generatorName, fieldAnnotations, constPool);
            Annotation annotation = new Annotation(javax.persistence.TableGenerator.class.getName(), constPool);
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
                annotation = new Annotation(org.hibernate.annotations.Type.class.getName(), constPool);
                annotation.addMemberValue("type", new StringMemberValue(StringBooleanType.class.getName(), constPool));
                fieldAnnotations.addAnnotation(annotation);
            }
        } else if(DateTime.class.isAssignableFrom(column.getActualJavaType())) {
            annotation = new Annotation(org.hibernate.annotations.Type.class.getName(), constPool);
            annotation.addMemberValue("type", new StringMemberValue(PersistentDateTime.class.getName(), constPool));
            ArrayMemberValue parameters = new ArrayMemberValue(new AnnotationMemberValue(constPool), constPool);
            parameters.setValue(new AnnotationMemberValue[] {
                    new AnnotationMemberValue(makeParameterAnnotation("databaseZone", "jvm", constPool), constPool)
            });
            annotation.addMemberValue("parameters", parameters);
            fieldAnnotations.addAnnotation(annotation);
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
            annotation = new Annotation(JoinColumn.class.getName(), constPool);
            annotation.addMemberValue("insertable", new BooleanMemberValue(false, constPool));
            annotation.addMemberValue("updatable", new BooleanMemberValue(false, constPool));
            annotation.addMemberValue("name", new StringMemberValue(jpaEscape(reference.getFromColumn()), constPool));
            annotation.addMemberValue("referencedColumnName", new StringMemberValue(jpaEscape(reference.getToColumn()), constPool));
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
        CtClass cc = getMappedClass(foreignKey.getToTable());
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

        String accessorName = field.getName().toUpperCase() + field.getName().substring(1);
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
