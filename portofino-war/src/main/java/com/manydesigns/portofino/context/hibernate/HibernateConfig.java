/*
 * Copyright (C) 2005-2011 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * There are special exceptions to the terms and conditions of the GPL
 * as it is applied to this software. View the full text of the
 * exception in file OPEN-SOURCE-LICENSE.txt in the directory of this
 * software distribution.
 *
 * This program is distributed WITHOUT ANY WARRANTY; and without the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see http://www.gnu.org/licenses/gpl.txt
 * or write to:
 * Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307  USA
 *
 */
package com.manydesigns.portofino.context.hibernate;


import com.manydesigns.elements.reflection.JavaClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.portofino.connections.ConnectionProvider;
import com.manydesigns.portofino.connections.JdbcConnectionProvider;
import com.manydesigns.portofino.database.Type;
import com.manydesigns.portofino.model.datamodel.*;
import com.manydesigns.portofino.model.datamodel.ForeignKey;
import org.apache.commons.lang.BooleanUtils;
import org.hibernate.FetchMode;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Mappings;
import org.hibernate.id.IncrementGenerator;
import org.hibernate.id.PersistentIdentifierGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.hibernate.id.enhanced.TableGenerator;
import org.hibernate.mapping.*;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.PrimaryKey;
import org.hibernate.mapping.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.*;
import java.util.List;

import static com.manydesigns.portofino.database.DbUtil.getHibernateType;
import static com.manydesigns.portofino.database.DbUtil.getHibernateTypeName;

/**
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Angelo    Lupo       - angelo.lupo@manydesigns.com
 * @author Paolo     Predonzani - paolo.predonzani@manydesigns.com
 */
public class HibernateConfig {

    protected final ConnectionProvider connectionProvider;
    private static final String SHOW_SQL = "true";
    private static final boolean LAZY = true;
    public static final Logger logger =
            LoggerFactory.getLogger(HibernateConfig.class);


    public HibernateConfig(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    public Configuration buildSessionFactory(Database database) {
        try {
            Configuration configuration = new Configuration()
                    .setProperty("default_entity_mode", "dynamic-map");

            JdbcConnectionProvider jdbcConnectionProvider =
                    (JdbcConnectionProvider) connectionProvider;
            configuration.setProperty("hibernate.connection.url",
                    jdbcConnectionProvider.getActualUrl())
                    .setProperty("hibernate.connection.driver_class",
                            jdbcConnectionProvider.getDriver())
                    .setProperty("hibernate.connection.username",
                            jdbcConnectionProvider.getUsername())
                    .setProperty("hibernate.connection.password",
                            jdbcConnectionProvider.getPassword())
                    .setProperty("hibernate.current_session_context_class",
                            "org.hibernate.context.ThreadLocalSessionContext")
                    .setProperty("org.hibernate.hql.ast.AST", "true")
                    .setProperty("hibernate.globally_quoted_identifiers", "false");
            configuration.setProperty("hibernate.show_sql", SHOW_SQL);
            Mappings mappings = configuration.createMappings();

            //Class Mapping
            classMapping(database, mappings);
            //One2Many Mapping
            o2mMapping(database, configuration, mappings);
            //Many2One Mapping solo per molti a molti
            m2oMapping(database, configuration, mappings);

            return configuration;
        }
        catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed." + ex);
            ex.printStackTrace();
            throw new ExceptionInInitializerError(ex);
        }
    }

     private Mappings classMapping(Database database, Mappings mappings) {

        for (Schema schema : database.getSchemas()) {
            for (com.manydesigns.portofino.model.datamodel.Table aTable :
                    schema.getTables()) {
                logger.debug(MessageFormat.format("Class - {0} ",
                                aTable.getQualifiedName()));
                RootClass clazz = createTableMapping(
                        mappings, aTable);
                mappings.addClass(clazz);
                mappings.addImport(clazz.getEntityName(),
                        clazz.getEntityName());
            }
        }
        return mappings;
    }
    private void m2oMapping(Database database, Configuration configuration, Mappings mappings) {
        for (Schema schema : database.getSchemas()) {
            for (com.manydesigns.portofino.model.datamodel.Table aTable :
                    schema.getTables()) {
                for (ForeignKey rel : aTable.getForeignKeys()) {
                    if (BooleanUtils.isTrue(aTable.getManyToMany())) {
                        logger.debug(MessageFormat.format("Many to one - {0} {1}",
                                aTable.getQualifiedName(), rel.getForeignKeyName()));
                        createM2O(configuration, mappings, rel);
                    }
                }
            }
        }
    }

    private void o2mMapping(Database database, Configuration configuration, Mappings mappings) {
        for (Schema schema : database.getSchemas()) {
            for (com.manydesigns.portofino.model.datamodel.Table aTable :
                    schema.getTables()) {
                for (ForeignKey rel : aTable.getOneToManyRelationships()) {
                     logger.debug(MessageFormat.format("One to many - {0} {1}",
                                aTable.getQualifiedName(), rel.getForeignKeyName()));
                    createO2M(configuration, mappings, rel);
                }
            }
        }
    }



    protected RootClass createTableMapping(Mappings mappings,
                                           com.manydesigns.portofino.model.datamodel.Table aTable) {


        Table tab = mappings.addTable(aTable.getSchemaName(), null,
                aTable.getTableName(), null, false);
        tab.setName(escapeName(aTable.getTableName()));
        tab.setSchema(escapeName(aTable.getSchemaName()));
        mappings.addTableBinding(aTable.getSchemaName(), null,
                aTable.getTableName(), aTable.getTableName(), null);

        RootClass clazz = new RootClass();
        clazz.setEntityName(aTable.getActualEntityName());
        if (aTable.getJavaClass() != null) {
            clazz.setClassName(aTable.getJavaClass());
            clazz.setProxyInterfaceName(aTable.getJavaClass());
        }
        clazz.setLazy(LAZY);
        clazz.setTable(tab);
        clazz.setNodeName(aTable.getTableName());


        List<com.manydesigns.portofino.model.datamodel.Column> columnList =
                new ArrayList<com.manydesigns.portofino.model.datamodel.Column>();
        columnList.addAll(aTable.getColumns());

        //Primary keys
        List<com.manydesigns.portofino.model.datamodel.Column> columnPKList
                = aTable.getPrimaryKey().getColumns();

        if (columnPKList.size() > 1) {
            createPKComposite(mappings, aTable, aTable.getPrimaryKey().getPrimaryKeyName(),
                    clazz, tab, columnPKList);
        } else {
            createPKSingle(mappings, aTable, aTable.getPrimaryKey().getPrimaryKeyName(),
                    clazz, tab, columnPKList);
        }

        //Other columns
        columnList.removeAll(columnPKList);

        for (com.manydesigns.portofino.model.datamodel.Column column
                : columnList) {
            createColumn(mappings, clazz, tab, column);

        }

        return clazz;
    }

    protected void createColumn(Mappings mappings, RootClass clazz,
                                Table tab,
                                com.manydesigns.portofino.model.datamodel.Column column) {
        Column col = new Column();
        col.setName(escapeName(column.getColumnName()));
        col.setLength(column.getLength());
        col.setPrecision(column.getLength());
        col.setScale(column.getScale());
        col.setNullable(column.isNullable());
        String columnType = column.getColumnType();
        Type type = connectionProvider.getTypeByName(columnType);
        if (type==null) {
            logger.error("Cannot find JDBC type for table {}," +
                    " column {}, type {}",
                    new String[] {tab.getName(), column.getColumnName(), columnType});
        }

        if (type != null) {
            col.setSqlTypeCode(type.getJdbcType());
        }
        col.setSqlType(columnType);
        tab.addColumn(col);

        Property prop = new Property();
        prop.setName(column.getActualPropertyName());
        prop.setNodeName(column.getActualPropertyName());

        SimpleValue value = new SimpleValue();
        value.setTable(tab);
        String hibernateTypeName = null;
        if (type != null) {
//            hibernateType = getHibernateType(type.getJdbcType());
            hibernateTypeName = getHibernateTypeName(
                    column.getActualJavaType(), type.getJdbcType());
        }
        if (hibernateTypeName != null) {
            value.setTypeName(hibernateTypeName);
        }
        value.addColumn(col);
        clazz.addProperty(prop);
        prop.setValue(value);

        mappings.addColumnBinding(column.getColumnName(),
                col, tab);
    }

    protected void createPKComposite(Mappings mappings,
                                     com.manydesigns.portofino.model.datamodel.Table mdTable,
                                     String pkName, RootClass clazz,
                                     Table tab,
                                     List<com.manydesigns.portofino.model.datamodel.Column> columnPKList) {


        PrimaryKey primaryKey = new PrimaryKey();
        primaryKey.setName(pkName);
        primaryKey.setTable(tab);
        tab.setPrimaryKey(primaryKey);

        clazz.setEmbeddedIdentifier(true);
        Component component = new Component(clazz);
        component.setDynamic(mdTable.getActualJavaClass()==null);
        String name;
        name = mdTable.getQualifiedName();

        component.setRoleName(name + ".id");
        component.setEmbedded(true);
        component.setNodeName("id");
        component.setKey(true);
        component.setNullValue("undefined");

        if (!component.isDynamic()){
            component.setComponentClassName
                    (mdTable.getJavaClass());
        }

        for (com.manydesigns.portofino.model.datamodel.Column
                column : columnPKList) {
            Column col = new Column();
            col.setName(escapeName(column.getColumnName()));
            String columnType = column.getColumnType();

            Type type = connectionProvider.getTypeByName(columnType);
            if (type==null) {
            logger.error("Cannot find JDBC type for table {}," +
                    " column {}, type {}",
                    new String[] {tab.getName(), column.getColumnName(), columnType});
            }
            if (type != null) {
                col.setSqlTypeCode(type.getJdbcType());
            }
            col.setSqlType(columnType);
            primaryKey.addColumn(col);
            SimpleValue value = new SimpleValue();
            value.setTable(tab);
            if (type != null) {
                org.hibernate.type.Type hibernateType =
                        getHibernateType(column.getActualJavaType(), type.getJdbcType());
                value.setTypeName(hibernateType.getName());
            }
            value.addColumn(col);
            tab.getPrimaryKey().addColumn(col);
            tab.addColumn(col);
            Property prop = new Property();
            prop.setName(column.getActualPropertyName());
            prop.setValue(value);
            prop.setCascade("none");
            prop.setNodeName(column.getActualPropertyName());
            prop.setPropertyAccessorName("property");
            component.addProperty(prop);
            mappings.addColumnBinding(column.getColumnName(), col, tab);
        }
        tab.setIdentifierValue(component);
        clazz.setIdentifier(component);
        clazz.setDiscriminatorValue(name);
    }


    protected void createPKSingle(Mappings mappings,
                                  com.manydesigns.portofino.model.datamodel.Table mdTable,
                                  String pkName, RootClass clazz,
                                  Table tab,
                                  List<com.manydesigns.portofino.model.datamodel.Column> columnPKList) {
        PrimaryKeyColumn pkcol =mdTable.getPrimaryKey().getPrimaryKeyColumns().get(0);
        com.manydesigns.portofino.model.datamodel.Column
                column = columnPKList.get(0);
        SimpleValue id = new SimpleValue(tab);
        final PrimaryKey primaryKey = new PrimaryKey();
        primaryKey.setName(pkName);
        primaryKey.setTable(tab);
        tab.setPrimaryKey(primaryKey);

        id.setTypeName(column.getColumnType());
        Column col = new Column();
        col.setName(escapeName(column.getColumnName()));
        String columnType = column.getColumnType();
        col.setValue(id);
        col.setLength(column.getLength());
        col.setPrecision(column.getLength());
        col.setScale(column.getScale());
        col.setNullable(column.isNullable());
        Type type = connectionProvider.getTypeByName(columnType);
        if (type==null) {
            logger.error("Cannot find JDBC type for table {}," +
                    " column {}, type {}",
                    new String[] {tab.getName(), column.getColumnName(), columnType});
        }
        if (type != null) {
            col.setSqlTypeCode(type.getJdbcType());
        }
        col.setSqlType(columnType);
        if (type != null) {
            org.hibernate.type.Type hibernateType =
                    getHibernateType(column.getActualJavaType(), type.getJdbcType());
            id.setTypeName(hibernateType.getName());
        }


        mappings.addColumnBinding(column.getColumnName(),
                col, tab);
        tab.addColumn(col);
        tab.getPrimaryKey().addColumn(col);
        id.addColumn(col);
        

        Property prop = new Property();
        prop.setName(column.getActualPropertyName());
        prop.setNodeName(column.getActualPropertyName());

        prop.setValue(id);
        prop.setPropertyAccessorName(mappings.getDefaultAccess());
        PropertyGeneration generation = PropertyGeneration.parse(null);
        prop.setGeneration(generation);

        prop.setInsertable(false);
        prop.setUpdateable(false);
        clazz.addProperty(prop);

        Generator generator = pkcol.getGenerator();

        if (null!=generator && generator instanceof SequenceGenerator) {
            manageSequenceGenerator(mappings, tab, id, (SequenceGenerator) generator);
        }

        if (null!=generator && generator instanceof
                com.manydesigns.portofino.model.datamodel.TableGenerator) {
            manageTableGenerator(mappings, tab, id,
                    (com.manydesigns.portofino.model.datamodel.TableGenerator) generator);
        }

        if (column.isAutoincrement()) {
            manageIdentityGenerator(mappings, tab, id);
        }

        if (null!=generator && generator instanceof
                com.manydesigns.portofino.model.datamodel.IncrementGenerator){
            manageAutoIncrementType(mappings, id, clazz.getEntityName());
        }

        tab.setIdentifierValue(id);
        clazz.setIdentifier(id);
        clazz.setIdentifierProperty(prop);
        clazz.setDiscriminatorValue(mdTable.getQualifiedName());

    }

    private void manageIdentityGenerator(Mappings mappings, Table tab,
                                          SimpleValue id) {
        id.setIdentifierGeneratorStrategy("identity");
        Properties params = new Properties();
        params.put(PersistentIdentifierGenerator.IDENTIFIER_NORMALIZER,
                    mappings.getObjectNameNormalizer());

        params.setProperty(
                    PersistentIdentifierGenerator.SCHEMA,
                    escapeName(tab.getSchema()));
        id.setIdentifierGeneratorProperties(params);
        id.setNullValue(null);
    }

    private void manageSequenceGenerator(Mappings mappings, Table tab,
                                          SimpleValue id, SequenceGenerator generator) {
        id.setIdentifierGeneratorStrategy
                ("enhanced-sequence");
        Properties params = new Properties();
        params.put(PersistentIdentifierGenerator.IDENTIFIER_NORMALIZER,
                    mappings.getObjectNameNormalizer());
        params.put(SequenceStyleGenerator.SEQUENCE_PARAM,
                    escapeName(generator.getName()));
        params.setProperty(
                    SequenceStyleGenerator.SCHEMA,
                    escapeName(tab.getSchema()));
        id.setIdentifierGeneratorProperties(params);
        id.setNullValue(null);
    }

    private void manageTableGenerator(Mappings mappings, Table tab, SimpleValue id,
                                          com.manydesigns.portofino.model.datamodel.TableGenerator generator) {
        id.setIdentifierGeneratorStrategy("enhanced-table");
        Properties params = new Properties();
        params.put(TableGenerator.TABLE,
                    tab);
        params.put(TableGenerator.TABLE_PARAM,
                    escapeName(generator.getTable()));
        params.put(PersistentIdentifierGenerator.IDENTIFIER_NORMALIZER,
                    mappings.getObjectNameNormalizer());
        params.put(TableGenerator.SEGMENT_COLUMN_PARAM, escapeName(generator.getKeyColumn()));
        params.put(TableGenerator.SEGMENT_VALUE_PARAM, generator.getKeyValue());
        params.put(TableGenerator.VALUE_COLUMN_PARAM,escapeName(generator.getValueColumn()));
        params.setProperty(
                    TableGenerator.SCHEMA,escapeName(tab.getSchema()));
        id.setIdentifierGeneratorProperties(params);
        id.setNullValue(null);
    }

    private void manageAutoIncrementType(Mappings mappings, SimpleValue id, String entityName) {
        id.setIdentifierGeneratorStrategy("increment");
        Properties params = new Properties();
        params.put(PersistentIdentifierGenerator.IDENTIFIER_NORMALIZER,
                    mappings.getObjectNameNormalizer());
        params.put(IncrementGenerator.ENTITY_NAME,
                    entityName);
        id.setIdentifierGeneratorProperties(params);
        id.setNullValue(null);
    }

    protected void createO2M(
            Configuration config,
            Mappings mappings,
            ForeignKey relationship) {

        com.manydesigns.portofino.model.datamodel.Table manyMDTable =
                relationship.getFromTable();
        com.manydesigns.portofino.model.datamodel.Table oneMDTable =
                relationship.getToTable();

        //Se la classe One non è dinamica e
        // non ha la proprietà non inserisco la relazione
        if (oneMDTable.getJavaClass()!=null){
            try {
                Class oneClass = oneMDTable.getActualJavaClass();
                JavaClassAccessor accessor = JavaClassAccessor
                        .getClassAccessor(oneClass);
                PropertyAccessor[] propertyAccessors = accessor.getProperties();
                boolean found = false;
                for (PropertyAccessor propertyAccessor : propertyAccessors){
                    if (propertyAccessor.getName()
                            .equals(relationship.getActualManyPropertyName())) {
                        found=true;
                    }
                }
                if(!found){
                    return;
                }
            } catch (Exception e) {
                //se non c'è non inserisco la relazione
                return;
            }
        }
        //relazione virtuali fra Database differenti
        if(!manyMDTable.getDatabaseName().equalsIgnoreCase(oneMDTable.getDatabaseName())){
            return;
        }

        String manyMDQualifiedTableName = manyMDTable.getActualEntityName();
        String oneMDQualifiedTableName = oneMDTable.getActualEntityName();

        PersistentClass clazzOne =
                config.getClassMapping(oneMDQualifiedTableName);
        PersistentClass clazzMany =
                config.getClassMapping(manyMDQualifiedTableName);

        //Uso i Bag perché i set non funzionano con i componenti dinamici
        Bag set = new Bag(clazzOne);
        // Mettere Lazy in debug a false per ottenere subito eventuali errori
        // nelle relazioni
        set.setLazy(LAZY);

        set.setRole(relationship.getToTable()+"."+relationship.getActualManyPropertyName());
        set.setNodeName(relationship.getActualManyPropertyName());
        set.setCollectionTable(clazzMany.getTable());
        OneToMany oneToMany = new OneToMany(set.getOwner());
        set.setElement(oneToMany);


        oneToMany.setReferencedEntityName(manyMDQualifiedTableName);

        oneToMany.setAssociatedClass(clazzMany);
        oneToMany.setEmbedded(true);
        
        set.setSorted(false);
        set.setFetchMode(FetchMode.DEFAULT);
        //Riferimenti alle colonne

        DependantValue dv;
        Table tableMany = clazzMany.getTable();
        Table tableOne = clazzOne.getTable();
        List<Column> oneColumns = new ArrayList<Column>();
        List<Column> manyColumns = new ArrayList<Column>();
        //Chiave multipla
        final List<Reference> refs = relationship.getReferences();
        if (refs.size() > 1) {
            dv = createFKComposite(relationship, manyMDTable, clazzOne,
                    clazzMany, set, tableMany, tableOne, oneColumns, manyColumns);
        } else {  //chiave straniera singola
            dv = createFKSingle(clazzOne, clazzMany, tableOne,
                    oneColumns, manyColumns, refs);
        }

        tableMany.createForeignKey(relationship.getForeignKeyName(),
                manyColumns,
                oneMDQualifiedTableName,
                oneColumns);

        dv.setNullable(false);
        set.setKey(dv);
        mappings.addCollection(set);

        Property prop = new Property();
        prop.setName(relationship.getActualManyPropertyName());
        prop.setNodeName(relationship.getActualManyPropertyName());
        prop.setValue(set);
        if ("cascade".equalsIgnoreCase(relationship.getOnDelete())){
            prop.setCascade("delete");
        } else {
            prop.setCascade("none");
        }
        clazzOne.addProperty(prop);

        //if(!StringUtils.)
    }

    private DependantValue createFKComposite(com.manydesigns.portofino.model.datamodel.ForeignKey relationship,
                                             com.manydesigns.portofino.model.datamodel.Table manyMDTable,
                                             PersistentClass clazzOne,
                                             PersistentClass clazzMany, Bag set,
                                             Table tableMany, Table tableOne,
                                             List<Column> oneColumns,
                                             List<Column> manyColumns) {
        DependantValue dv;
        Component component = new Component(set);
        component.setDynamic(manyMDTable.getActualJavaClass()==null);
        component.setEmbedded(true);
        dv = new DependantValue(clazzMany.getTable(), component);
        dv.setNullable(true);
        dv.setUpdateable(true);


        for (Reference ref : relationship.getReferences()) {
            String colToName = ref.getToColumn();
            String colFromName = ref.getFromColumn();
            Iterator it = tableMany.getColumnIterator();
            while (it.hasNext()) {
                Column col = (Column) it.next();
                if (col.getName().equals(colFromName)) {
                    dv.addColumn(col);
                    manyColumns.add(col);
                    break;
                }
            }

            Iterator it2 = tableOne.getColumnIterator();
            while (it2.hasNext()) {
                Column col = (Column) it2.next();
                if (col.getName().equals(colToName)) {
                    oneColumns.add(col);
                    break;
                }
            }
            Property refProp;
            refProp = getRefProperty(clazzOne, colToName);
            component.addProperty(refProp);
        }
        return dv;
    }

    private DependantValue createFKSingle(PersistentClass clazzOne,
                                          PersistentClass clazzMany,
                                          Table tableOne, List<Column> oneColumns,
                                          List<Column> manyColumns,
                                          List<Reference> refs) {
        DependantValue dv;
        Property refProp;

        String colFromName = refs.get(0).getFromColumn();
        String colToName = refs.get(0).getToColumn();
        refProp = getRefProperty(clazzOne, colToName);
        dv = new DependantValue(clazzMany.getTable(),
                refProp.getPersistentClass().getKey());
        dv.setNullable(true);
        dv.setUpdateable(true);

        Iterator it = clazzMany.getTable().getColumnIterator();
        while (it.hasNext()) {
            Column col = (Column) it.next();
            if (col.getName().equals(colFromName)) {
                dv.addColumn(col);
                manyColumns.add(col);
                break;
            }
        }

        Iterator it2 = tableOne.getColumnIterator();
        while (it2.hasNext()) {
            Column col = (Column) it2.next();
            if (col.getName().equals(colToName)) {
                oneColumns.add(col);
                break;
            }
        }
        return dv;
    }


    protected void createM2O(Configuration config, Mappings mappings,
                             ForeignKey relationship) {
        com.manydesigns.portofino.model.datamodel.Table manyMDTable =
                relationship.getFromTable();
        com.manydesigns.portofino.model.datamodel.Table oneMDTable =
                relationship.getToTable();
        String manyMDQualifiedTableName = manyMDTable.getActualEntityName();
        String oneMDQualifiedTableName = oneMDTable.getActualEntityName();

        RootClass clazz =
                (RootClass) mappings.getClass(manyMDQualifiedTableName);
        Table tab = clazz.getTable();
        List<String> columnNames = new ArrayList<String>();

        for (Reference ref : relationship.getReferences()) {
            columnNames.add(ref.getFromColumn());
        }

        ManyToOne m2o = new ManyToOne(tab);
        m2o.setLazy(LAZY);
        final HashMap<String, PersistentClass> persistentClasses =
                new HashMap<String, PersistentClass>();
        persistentClasses.put(oneMDQualifiedTableName,
                config.getClassMapping(oneMDQualifiedTableName));
        m2o.setReferencedEntityName(oneMDQualifiedTableName);
        m2o.createPropertyRefConstraints(persistentClasses);
        for (String columnName : columnNames) {
            Column col = new Column();
            col.setName(escapeName(columnName));
            m2o.addColumn(col);
        }

        
        Property prop = new Property();
        prop.setName(relationship.getActualOnePropertyName());
        prop.setNodeName(relationship.getActualOnePropertyName());
        prop.setValue(m2o);
        prop.setCascade("all");
        prop.setInsertable(false);
        prop.setUpdateable(false);
        clazz.addProperty(prop);
    }

    private Property getRefProperty(PersistentClass clazzOne, String colToName) {
        Property refProp;
        if (null != clazzOne.getIdentifierProperty()) {
            refProp = clazzOne.getIdentifierProperty();
        } else if (null != clazzOne.getIdentifier()) {
            refProp = ((Component) clazzOne.getIdentifier())
                    .getProperty(colToName);
        } else {
            refProp = clazzOne.getProperty(colToName);
        }
        return refProp;
    }

    private String escapeName(String name) {
        return name;
//        return "`"+name+"`";
    }
}