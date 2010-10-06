/*
 * Copyright (C) 2005-2010 ManyDesigns srl.  All rights reserved.
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


import com.manydesigns.elements.logging.LogUtil;
import com.manydesigns.portofino.database.ConnectionProvider;
import com.manydesigns.portofino.database.DbUtil;
import com.manydesigns.portofino.database.JdbcConnectionProvider;
import com.manydesigns.portofino.database.Type;
import com.manydesigns.portofino.model.datamodel.ForeignKey;
import com.manydesigns.portofino.model.datamodel.*;
import org.hibernate.FetchMode;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Mappings;
import org.hibernate.id.PersistentIdentifierGenerator;
import org.hibernate.mapping.*;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.PrimaryKey;
import org.hibernate.mapping.Table;

import java.util.*;
import java.util.List;
import java.util.logging.Logger;
import java.text.MessageFormat;


/**
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Angelo    Lupo       - angelo.lupo@manydesigns.com
 * @author Paolo     Predonzani - paolo.predonzani@manydesigns.com
 */
public class HibernateConfig {

    protected final ConnectionProvider connectionProvider;
    private static final String SHOW_SQL = "true";
    public static final Logger logger =
            LogUtil.getLogger(HibernateConfig.class);


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
                    jdbcConnectionProvider.getConnectionURL())
                    .setProperty("hibernate.connection.driver_class",
                            jdbcConnectionProvider.getDriverClass())
                    .setProperty("hibernate.connection.username",
                            jdbcConnectionProvider.getUsername())
                    .setProperty("hibernate.connection.password",
                            jdbcConnectionProvider.getPassword())
                    .setProperty("hibernate.current_session_context_class",
                            "org.hibernate.context.ThreadLocalSessionContext");
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
                LogUtil.finestMF(logger, MessageFormat.format("Class - {0} ",
                                aTable.getQualifiedName()));
                RootClass clazz = createTableMapping(
                        mappings, aTable);
                mappings.addClass(clazz);
                mappings.addImport(clazz.getEntityName(),
                        aTable.getTableName());
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
                    if (aTable.isM2m()) {
                        LogUtil.finestMF(logger, MessageFormat.format("Many to one - {0} {1}",
                                aTable.getQualifiedName(), rel.getFkName()));
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
                     LogUtil.finestMF(logger, MessageFormat.format("One to many - {0} {1}",
                                aTable.getQualifiedName(), rel.getFkName()));
                    createO2M(database, configuration, mappings, rel);
                }
            }
        }
    }



    protected RootClass createTableMapping(Mappings mappings,
                                           com.manydesigns.portofino.model.datamodel.Table aTable) {


        Table tab = mappings.addTable(aTable.getSchemaName(), null,
                aTable.getTableName(), null, false);
        tab.setName(aTable.getTableName());
        tab.setSchema(aTable.getSchemaName());
        mappings.addTableBinding(aTable.getSchemaName(), null,
                aTable.getTableName(), aTable.getTableName(), null);

        RootClass clazz = new RootClass();
        clazz.setEntityName(aTable.getQualifiedName());
        if (aTable.getJavaClassName() != null) {
            clazz.setClassName(aTable.getJavaClassName());
            clazz.setProxyInterfaceName(aTable.getJavaClassName());
        }
        clazz.setLazy(true);
        clazz.setTable(tab);
        clazz.setNodeName(aTable.getTableName());


        List<com.manydesigns.portofino.model.datamodel.Column> columnList =
                new ArrayList<com.manydesigns.portofino.model.datamodel.Column>();
        columnList.addAll(aTable.getColumns());

        //Primary keys
        List<com.manydesigns.portofino.model.datamodel.Column> columnPKList
                = aTable.getPrimaryKey().getColumns();

        if (columnPKList.size() > 1) {
            createPKComposite(mappings, aTable, aTable.getPrimaryKey().getPkName(),
                    clazz, tab, columnPKList);
        } else {
            createPKSingle(mappings, aTable, aTable.getPrimaryKey().getPkName(),
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
        col.setName(column.getColumnName());
        col.setLength(column.getLength());
        col.setPrecision(column.getLength());
        col.setScale(column.getScale());
        col.setNullable(column.isNullable());
        String columnType = column.getColumnType();
        Type type = connectionProvider.getTypeByName(columnType);
        if (type==null) {
            LogUtil.severeMF(logger, "Cannot find JDBC type for table {0}," +
                    " column {1}, type {2}", tab.getName(), column.getColumnName(), columnType);
        }

        col.setSqlTypeCode(type.getJdbcType());
        tab.addColumn(col);

        Property prop = new Property();
        if (column.getPropertyName() != null) {
            prop.setName(column.getPropertyName());
            prop.setNodeName(column.getPropertyName());
        } else {
            prop.setName(column.getColumnName());
            prop.setNodeName(column.getColumnName());
        }
        SimpleValue value = new SimpleValue();
        value.setTable(tab);
        org.hibernate.type.Type hibernateType =
                DbUtil.getHibernateType(type.getJdbcType());
        value.setTypeName(hibernateType.getName());
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


        final PrimaryKey primaryKey = new PrimaryKey();
        primaryKey.setName(pkName);
        primaryKey.setTable(tab);
        tab.setPrimaryKey(primaryKey);

        clazz.setEmbeddedIdentifier(true);
        Component component = new Component(clazz);
        component.setDynamic(mdTable.getJavaClass()==null);
        String name;
        name = mdTable.getQualifiedName();

        component.setRoleName(name + ".id");
        component.setEmbedded(true);
        component.setNodeName("id");
        component.setKey(true);
        component.setNullValue("undefined");

        if (!component.isDynamic()){
            component.setComponentClassName
                    (mdTable.getJavaClassName());
        }

        for (com.manydesigns.portofino.model.datamodel.Column
                column : columnPKList) {
            Column col = new Column();
            col.setName(column.getColumnName());
            String columnType = column.getColumnType();

            Type type = connectionProvider.getTypeByName(columnType);
            col.setSqlTypeCode(type.getJdbcType());
            primaryKey.addColumn(col);
            SimpleValue value = new SimpleValue();
            value.setTable(tab);
            value.setTypeName(DbUtil.getHibernateType(type.getJdbcType())
                    .getName());
            value.addColumn(col);
            tab.getPrimaryKey().addColumn(col);
            tab.addColumn(col);
            Property prop = new Property();
            prop.setName(column.getColumnName());
            prop.setValue(value);
            prop.setCascade("none");
            prop.setNodeName(column.getColumnName());
            prop.setPropertyAccessorName("property");
            component.addProperty(prop);
            mappings.addColumnBinding(column.getColumnName(),
                    col, tab);
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
        com.manydesigns.portofino.model.datamodel.Column
                column = columnPKList.get(0);
        SimpleValue id = new SimpleValue(tab);
        final PrimaryKey primaryKey = new PrimaryKey();
        primaryKey.setName(pkName);
        primaryKey.setTable(tab);
        tab.setPrimaryKey(primaryKey);

        id.setTypeName(column.getColumnType());
        Column col = new Column();
        col.setName(column.getColumnName());
        String columnType = column.getColumnType();
        col.setValue(id);
        col.setLength(column.getLength());
        col.setPrecision(column.getLength());
        col.setScale(column.getScale());
        col.setNullable(column.isNullable());
        Type type = connectionProvider.getTypeByName(columnType);
        col.setSqlTypeCode(type.getJdbcType());
        org.hibernate.type.Type hibernateType =
                DbUtil.getHibernateType(type.getJdbcType());
        id.setTypeName(hibernateType.getName());

        mappings.addColumnBinding(column.getColumnName(),
                col, tab);
        tab.addColumn(col);
        tab.getPrimaryKey().addColumn(col);
        id.addColumn(col);

        Property prop = new Property();
        if (column.getPropertyName() != null) {
            prop.setName(column.getPropertyName());
            prop.setNodeName(column.getPropertyName());
        } else {
            prop.setName(column.getColumnName());
            prop.setNodeName(column.getColumnName());
        }
        prop.setValue(id);
        prop.setPropertyAccessorName(mappings.getDefaultAccess());
        PropertyGeneration generation = PropertyGeneration.parse(null);
        prop.setGeneration(generation);

        prop.setInsertable(false);
        prop.setUpdateable(false);
        clazz.addProperty(prop);


        if (type.isAutoincrement()) {
            manageAutoIncrementTypes(mappings, tab, id);
        }

        tab.setIdentifierValue(id);
        clazz.setIdentifier(id);
        clazz.setIdentifierProperty(prop);
        clazz.setDiscriminatorValue(mdTable.getQualifiedName());

    }

    private void manageAutoIncrementTypes(Mappings mappings, Table tab,
                                          SimpleValue id) {
        id.setIdentifierGeneratorStrategy("identity");
        Properties params = new Properties();
        params.put(PersistentIdentifierGenerator.IDENTIFIER_NORMALIZER,
                    mappings.getObjectNameNormalizer());

        params.setProperty(
                    PersistentIdentifierGenerator.SCHEMA,
                    tab.getSchema());
        id.setIdentifierGeneratorProperties(params);
        id.setNullValue(null);
    }


    protected void createO2M(
            Database database,
            Configuration config,
            Mappings mappings,
            ForeignKey relationship) {
        com.manydesigns.portofino.model.datamodel.Table manyMDTable =
                relationship.getFromTable();
        com.manydesigns.portofino.model.datamodel.Table oneMDTable =
                relationship.getToTable();
        String manyMDQualifiedTableName = manyMDTable.getQualifiedName();
        String oneMDQualifiedTableName = oneMDTable.getQualifiedName();

        PersistentClass clazzOne =
                config.getClassMapping(oneMDQualifiedTableName);
        PersistentClass clazzMany =
                config.getClassMapping(manyMDQualifiedTableName);

        //Uso i Bag perché i set non funzionano con i componenti dinamici
        Bag set = new Bag(clazzOne);
        // Mettere Lazy in debug a false per ottenere subito eventuali errori
        // nelle relazioni
        set.setLazy(true);

        if (relationship.getManyPropertyName() == null) {
            set.setRole(manyMDQualifiedTableName + "."
                    + relationship.getFkName());
            set.setNodeName(relationship.getFkName());
        } else {
            set.setRole(relationship.getManyPropertyName());
            set.setNodeName(relationship.getManyPropertyName());
        }
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

        tableMany.createForeignKey(relationship.getFkName(),
                manyColumns,
                oneMDQualifiedTableName,
                oneColumns);
        set.setKey(dv);
        mappings.addCollection(set);

        Property prop = new Property();
        if (relationship.getManyPropertyName() == null) {
            prop.setName(manyMDQualifiedTableName + "."
                    + relationship.getFkName());
            prop.setNodeName(relationship.getFkName());
        } else {
            prop.setName(relationship.getManyPropertyName());
            prop.setNodeName(relationship.getManyPropertyName());
        }
        prop.setValue(set);
        clazzOne.addProperty(prop);
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
        component.setDynamic(manyMDTable.getJavaClass()==null);
        component.setEmbedded(true);
        dv = new DependantValue(clazzMany.getTable(), component);
        dv.setNullable(true);
        dv.setUpdateable(true);


        for (Reference ref : relationship.getReferences()) {
            String colToName = ref.getToColumnName();
            String colFromName = ref.getFromColumnName();
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

        String colFromName = refs.get(0).getFromColumnName();
        String colToName = refs.get(0).getToColumnName();
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
        String manyMDQualifiedTableName = manyMDTable.getQualifiedName();
        String oneMDQualifiedTableName = oneMDTable.getQualifiedName();

        RootClass clazz =
                (RootClass) mappings.getClass(manyMDQualifiedTableName);
        Table tab = clazz.getTable();
        List<String> columnNames = new ArrayList<String>();

        for (Reference ref : relationship.getReferences()) {
            columnNames.add(ref.getFromColumnName());
        }

        ManyToOne m2o = new ManyToOne(tab);
        m2o.setLazy(true);
        final HashMap<String, PersistentClass> persistentClasses =
                new HashMap<String, PersistentClass>();
        persistentClasses.put(oneMDQualifiedTableName,
                config.getClassMapping(oneMDQualifiedTableName));
        m2o.setReferencedEntityName(oneMDQualifiedTableName);
        m2o.createPropertyRefConstraints(persistentClasses);
        for (String columnName : columnNames) {
            Column col = new Column();
            col.setName(columnName);
            m2o.addColumn(col);
        }

        Property prop = new Property();
        prop.setName(relationship.getOnePropertyName());
        prop.setNodeName(relationship.getOnePropertyName());
        prop.setValue(m2o);
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
}