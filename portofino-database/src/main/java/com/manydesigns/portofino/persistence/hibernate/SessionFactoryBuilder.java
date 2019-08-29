package com.manydesigns.portofino.persistence.hibernate;

import com.google.common.reflect.TypeParameter;
import com.manydesigns.portofino.code.CodeBase;
import com.manydesigns.portofino.code.JavaCodeBase;
import com.manydesigns.portofino.model.database.*;
import com.manydesigns.portofino.model.database.Column;
import com.manydesigns.portofino.model.database.ForeignKey;
import com.manydesigns.portofino.model.database.Table;
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
import org.hibernate.EntityMode;
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
import org.hibernate.type.Type;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLClassLoader;
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
        try {
            //Use a new classloader as scratch space for Javassist
            URLClassLoader scratchClassLoader = new URLClassLoader(new URL[0]);
            Thread.currentThread().setContextClassLoader(scratchClassLoader);
            for (Table table : database.getAllTables()) {
                generateClass(table);
            }
            for (Table table : database.getAllTables()) {
                mapRelationships(table);
            }
            for (Table table : database.getAllTables()) {
                byte[] classFile = getClassFile(table);
                FileObject location = getEntityLocation(root, table);
                try(OutputStream outputStream = location.getContent().getOutputStream()) {
                    outputStream.write(classFile);
                }
            }
            try {
                Class<?> aClass = scratchClassLoader.loadClass("tt.tt.components");
                logger.info("foo " + aClass);
            } catch (Exception e) {}
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
        return buildSessionFactory(codeBase);
    }

    public SessionFactoryAndCodeBase buildSessionFactory(CodeBase codeBase) {
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
            for (Table table : database.getAllTables()) {
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
        //TODO necessary?
        settings.put("hibernate.default_entity_mode", EntityMode.MAP.getExternalName());
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

    public CtClass generateClass(Table table) throws CannotCompileException, NotFoundException {
        CtClass cc = classPool.makeClass(getMappedClassName(table));
        cc.addInterface(classPool.get(Serializable.class.getName()));
        ClassFile ccFile = cc.getClassFile();
        ConstPool constPool = ccFile.getConstPool();
        AnnotationsAttribute classAnnotations = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
        ccFile.addAttribute(classAnnotations);
        javassist.bytecode.annotation.Annotation annotation;

        annotation = new javassist.bytecode.annotation.Annotation(javax.persistence.Table.class.getName(), constPool);
        annotation.addMemberValue("name", new StringMemberValue(table.getTableName(), ccFile.getConstPool()));
        annotation.addMemberValue("schema", new StringMemberValue(table.getSchema().getActualSchemaName(), ccFile.getConstPool()));
        classAnnotations.addAnnotation(annotation);

        annotation = new javassist.bytecode.annotation.Annotation(javax.persistence.Entity.class.getName(), constPool);
        annotation.addMemberValue("name", new StringMemberValue(table.getActualEntityName(), ccFile.getConstPool()));
        classAnnotations.addAnnotation(annotation);


        //Primary keys
        List<Column> columnPKList = table.getPrimaryKey().getColumns();

        if(!table.getColumns().containsAll(columnPKList)) {
            logger.error("Primary key refers to some invalid columns, skipping table {}", table.getQualifiedName());
            return null;
        }

        for(Column column : table.getColumns()) {
            String propertyName = column.getActualPropertyName();
            CtField field = new CtField(classPool.get(column.getActualJavaType().getName()), propertyName, cc);
            AnnotationsAttribute fieldAnnotations = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
            annotation = new javassist.bytecode.annotation.Annotation(javax.persistence.Column.class.getName(), constPool);
            annotation.addMemberValue("name", new StringMemberValue(column.getColumnName(), ccFile.getConstPool()));
            fieldAnnotations.addAnnotation(annotation);
            if(columnPKList.contains(column)) {
                annotation.addMemberValue("updatable", new BooleanMemberValue(false, ccFile.getConstPool()));
                annotation = new javassist.bytecode.annotation.Annotation(Id.class.getName(), constPool);
                fieldAnnotations.addAnnotation(annotation);
                if(column.isAutoincrement()) {
                    annotation = new javassist.bytecode.annotation.Annotation(GeneratedValue.class.getName(), constPool);
                    EnumMemberValue value = new EnumMemberValue(ccFile.getConstPool());
                    value.setType(GenerationType.class.getName());
                    value.setValue(GenerationType.IDENTITY.name());
                    annotation.addMemberValue("strategy", value);
                    fieldAnnotations.addAnnotation(annotation);
                }
            }
            field.getFieldInfo().addAttribute(fieldAnnotations);
            cc.addField(field);
            String accessorName = propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
            cc.addMethod(CtNewMethod.getter("get" + accessorName, field));
            cc.addMethod(CtNewMethod.setter("set" + accessorName, field));
        }
        return cc;
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
