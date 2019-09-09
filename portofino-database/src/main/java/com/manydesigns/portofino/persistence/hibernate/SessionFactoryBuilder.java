package com.manydesigns.portofino.persistence.hibernate;

import com.manydesigns.portofino.code.CodeBase;
import com.manydesigns.portofino.code.JavaCodeBase;
import com.manydesigns.portofino.model.database.*;
import com.manydesigns.portofino.model.database.Column;
import com.manydesigns.portofino.model.database.ForeignKey;
import com.manydesigns.portofino.model.database.SequenceGenerator;
import com.manydesigns.portofino.model.database.Table;
import com.manydesigns.portofino.model.database.TableGenerator;
import com.manydesigns.portofino.model.database.platforms.DatabasePlatform;
import javassist.*;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.*;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.hibernate.annotations.GenericGenerator;
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
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SessionFactoryBuilder {

    private static final Logger logger = LoggerFactory.getLogger(SessionFactoryBuilder.class);
    //String values for the mapping of boolean values to CHAR/VARCHAR columns
    protected String trueString = "T";
    protected String falseString = "F";
    protected final Database database;
    protected final ClassPool classPool = new ClassPool(ClassPool.getDefault());

    public SessionFactoryBuilder(Database database, Configuration configuration) {
        this.database = database;
        String trueString = database.getTrueString();
        if (trueString != null) {
            this.trueString = "null".equalsIgnoreCase(trueString) ? null : trueString;
        }
        String falseString = database.getFalseString();
        if (falseString != null) {
            this.falseString = "null".equalsIgnoreCase(falseString) ? null : falseString;
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
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        CodeBase codeBase = new JavaCodeBase(root);
        List<Table> tablesWithPK = database.getAllTables();
        tablesWithPK.removeIf(this::checkTableWithValidPrimaryKey);
        try {
            //Use a new classloader as scratch space for Javassist
            URLClassLoader scratchClassLoader = new URLClassLoader(new URL[0]);
            Thread.currentThread().setContextClassLoader(scratchClassLoader);

            CtClass baseClass = generateBaseClass();
            try(OutputStream outputStream = root.resolveFile(database.getDatabaseName() + FileName.SEPARATOR_CHAR + "BaseEntity.class").getContent().getOutputStream()) {
                outputStream.write(baseClass.toBytecode());
            }

            for (Table table : tablesWithPK) {
                generateClass(table);
            }
            for (Table table : tablesWithPK) {
                mapRelationships(table);
            }
            for (Table table : tablesWithPK) {
                byte[] classFile = getClassFile(table);
                FileObject location = getEntityLocation(root, table);
                try(OutputStream outputStream = location.getContent().getOutputStream()) {
                    outputStream.write(classFile);
                }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
        return buildSessionFactory(codeBase, tablesWithPK);
    }

    protected boolean checkTableWithValidPrimaryKey(Table table) {
        if(table.getPrimaryKey() == null) {
            logger.warn("Skipping table without primary key: {}", table.getQualifiedName());
            return true;
        }
        List<Column> columnPKList = table.getPrimaryKey().getColumns();
        if(!table.getColumns().containsAll(columnPKList)) {
            logger.error("Skipping table with primary key that refers to invalid columns: {}", table.getQualifiedName());
            return true;
        }
        return false;
    }

    public SessionFactoryAndCodeBase buildSessionFactory(CodeBase codeBase, List<Table> tablesWithPK) {
        BootstrapServiceRegistryBuilder bootstrapRegistryBuilder = new BootstrapServiceRegistryBuilder();
        DynamicClassLoaderService classLoaderService = new DynamicClassLoaderService();
        bootstrapRegistryBuilder.applyClassLoaderService(classLoaderService);
        BootstrapServiceRegistry bootstrapServiceRegistry = bootstrapRegistryBuilder.build();
        Map settings = new HashMap();
        setupConnection(settings);
        ServiceRegistry standardRegistry =
                new StandardServiceRegistryBuilder(bootstrapServiceRegistry).applySettings(settings).build();
        MetadataSources sources = new MetadataSources(standardRegistry);
        try {
            for (Table table : tablesWithPK) {
                Class persistentClass = getPersistentClass(table, codeBase);
                sources.addAnnotatedClass(persistentClass);
                classLoaderService.classes.put(persistentClass.getName(), persistentClass);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        MetadataBuilder metadataBuilder = sources.getMetadataBuilder();
        Metadata metadata = metadataBuilder.build();
        //Map using hashmaps
        //TODO handle tables with javaClass=...
        metadata.getEntityBindings().forEach((PersistentClass c) -> {
            c.setClassName(null);
            if(c.getIdentifier() instanceof Component) {
                Component component = (Component) c.getIdentifier();
                component.setComponentClassName(null);
                component.setDynamic(true);
            }
        });
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
        //Setting this disallows mapping entities to Java classes
        //settings.put("hibernate.default_entity_mode", EntityMode.MAP.getExternalName());
    }

    protected FileObject getEntityLocation(FileObject root, Table table) throws FileSystemException {
        return root.resolveFile(entityNameToFileName(table));
    }

    @NotNull
    protected String entityNameToFileName(Table table) {
        return getMappedClassName(table).replace('.', FileName.SEPARATOR_CHAR) + ".class";
    }

    @NotNull
    private String getMappedClassName(Table table) {
        return table.getSchema().getQualifiedName() + "." + table.getActualEntityName();
    }

    public Class getPersistentClass(Table table, CodeBase codeBase) throws IOException, ClassNotFoundException {
        Class javaClass = table.getActualJavaClass();
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
        ccFile.addAttribute(classAnnotations);
        javassist.bytecode.annotation.Annotation annotation;

        annotation = new javassist.bytecode.annotation.Annotation(javax.persistence.Table.class.getName(), constPool);
        annotation.addMemberValue("name", new StringMemberValue(table.getTableName(), constPool));
        annotation.addMemberValue("schema", new StringMemberValue(table.getSchema().getActualSchemaName(), constPool));
        classAnnotations.addAnnotation(annotation);

        annotation = new javassist.bytecode.annotation.Annotation(javax.persistence.Entity.class.getName(), constPool);
        annotation.addMemberValue("name", new StringMemberValue(table.getActualEntityName(), constPool));
        classAnnotations.addAnnotation(annotation);

        setupColumns(table, cc, constPool);
        return cc;
    }

    protected void setupColumns(Table table, CtClass cc, ConstPool constPool) throws CannotCompileException, NotFoundException {
        List<Column> columnPKList = table.getPrimaryKey().getColumns();
        Annotation annotation;
        for(Column column : table.getColumns()) {
            String propertyName = column.getActualPropertyName();
            CtField field = new CtField(classPool.get(column.getActualJavaType().getName()), propertyName, cc);
            AnnotationsAttribute fieldAnnotations = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
            annotation = new Annotation(javax.persistence.Column.class.getName(), constPool);
            annotation.addMemberValue("name", new StringMemberValue(column.getColumnName(), constPool));
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

            field.getFieldInfo().addAttribute(fieldAnnotations);
            cc.addField(field);
            String accessorName = propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
            cc.addMethod(CtNewMethod.getter("get" + accessorName, field));
            cc.addMethod(CtNewMethod.setter("set" + accessorName, field));
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
            Annotation annotation = new Annotation(javax.persistence.SequenceGenerator.class.getName(), constPool);
            annotation.addMemberValue("name", new StringMemberValue(generatorName, constPool));
            annotation.addMemberValue("sequenceName", new StringMemberValue(((SequenceGenerator) generator).getName(), constPool));
            fieldAnnotations.addAnnotation(annotation);
        } else if (generator instanceof TableGenerator) {
            addGeneratedValueAnnotation(GenerationType.TABLE, generatorName, fieldAnnotations, constPool);
            Annotation annotation = new Annotation(javax.persistence.TableGenerator.class.getName(), constPool);
            annotation.addMemberValue("name", new StringMemberValue(generatorName, constPool));
            annotation.addMemberValue("schema", new StringMemberValue(table.getSchema().getActualSchemaName(), constPool));
            annotation.addMemberValue("table", new StringMemberValue(((TableGenerator) generator).getTable(), constPool));
            annotation.addMemberValue("pkColumnName", new StringMemberValue(((TableGenerator) generator).getKeyColumn(), constPool));
            annotation.addMemberValue("pkColumnValue", new StringMemberValue(((TableGenerator) generator).getKeyValue(), constPool));
            annotation.addMemberValue("valueColumnName", new StringMemberValue(((TableGenerator) generator).getValueColumn(), constPool));
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
                for(Map.Entry param : databaseSpecificType.parameters.entrySet()) {
                    typeParams.add(new AnnotationMemberValue(makeParameterAnnotation(param.getKey().toString(), String.valueOf(param.getValue()), constPool), constPool));
                }
                parameters.setValue(typeParams.toArray(new AnnotationMemberValue[0]));
                annotation.addMemberValue("parameters", parameters);
                fieldAnnotations.addAnnotation(annotation);
            }
        }
    }

    public void mapRelationships(Table table) throws NotFoundException, CannotCompileException {
        for(ForeignKey foreignKey : table.getForeignKeys()) {
            mapManyToOne(foreignKey);
            mapOneToMany(foreignKey);
        }
    }

    protected void mapManyToOne(ForeignKey foreignKey) throws CannotCompileException, NotFoundException {
        CtClass cc = getMappedClass(foreignKey.getFromTable());
        ClassFile ccFile = cc.getClassFile();
        ConstPool constPool = ccFile.getConstPool();
        Table toTable = foreignKey.getToTable();
        String propertyName = foreignKey.getActualOnePropertyName();
        CtField field = new CtField(getMappedClass(toTable), propertyName, cc);
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
            annotation.addMemberValue("name", new StringMemberValue(reference.getFromColumn(), constPool));
            annotation.addMemberValue("referencedColumnName", new StringMemberValue(reference.getToColumn(), constPool));
            joinColumnsValue.add(new AnnotationMemberValue(annotation, constPool));
        }
        annotation = new Annotation(JoinColumns.class.getName(), constPool);
        ArrayMemberValue joinColumns = new ArrayMemberValue(new AnnotationMemberValue(constPool), constPool);
        joinColumns.setValue(joinColumnsValue.toArray(new MemberValue[0]));
        annotation.addMemberValue("value", joinColumns);
        fieldAnnotations.addAnnotation(annotation);
        field.getFieldInfo().addAttribute(fieldAnnotations);

        String accessorName = propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
        cc.addMethod(CtNewMethod.getter("get" + accessorName, field));
        cc.addMethod(CtNewMethod.setter("set" + accessorName, field));
    }

    protected void mapOneToMany(ForeignKey foreignKey) throws NotFoundException, CannotCompileException {
        CtClass cc = getMappedClass(foreignKey.getToTable());
        ClassFile ccFile = cc.getClassFile();
        ConstPool constPool = ccFile.getConstPool();
        Table fromTable = foreignKey.getFromTable();
        String propertyName = foreignKey.getActualManyPropertyName();
        CtField field = new CtField(classPool.get(List.class.getName()), propertyName, cc);
        String referencedClassName = getMappedClassName(fromTable);
        field.setGenericSignature("Ljava/util/List<L" + referencedClassName.replace('.', '/') + ";>;");
        cc.addField(field);

        AnnotationsAttribute fieldAnnotations = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
        Annotation annotation;
        annotation = new Annotation(OneToMany.class.getName(), constPool);
        annotation.addMemberValue("targetEntity", new ClassMemberValue(referencedClassName, constPool));
        annotation.addMemberValue("mappedBy", new StringMemberValue(foreignKey.getActualOnePropertyName(), constPool));
        //TODO cascade
        fieldAnnotations.addAnnotation(annotation);
        field.getFieldInfo().addAttribute(fieldAnnotations);

        String accessorName = propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
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

}
