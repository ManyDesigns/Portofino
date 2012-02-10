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
package com.manydesigns.portofino.application.hibernate;


import com.manydesigns.elements.reflection.JavaClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.database.DbUtil;
import com.manydesigns.portofino.database.StringBooleanType;
import com.manydesigns.portofino.model.database.*;
import com.manydesigns.portofino.model.database.ForeignKey;
import liquibase.database.structure.ForeignKeyConstraintType;
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
import org.hibernate.type.*;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;

/**
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Angelo    Lupo       - angelo.lupo@manydesigns.com
 * @author Paolo     Predonzani - paolo.predonzani@manydesigns.com
 */
public class HibernateConfig {

    protected final ConnectionProvider connectionProvider;
    protected final org.apache.commons.configuration.Configuration portofinoConfiguration;
    private static final boolean LAZY = true;
    public static final Logger logger =
            LoggerFactory.getLogger(HibernateConfig.class);

    //String values for the mapping of boolean values to CHAR/VARCHAR columns
    private static String trueString = "T";
    private static String falseString = "F";

    public HibernateConfig(ConnectionProvider connectionProvider,
                           org.apache.commons.configuration.Configuration portofinoConfiguration) {
        this.connectionProvider = connectionProvider;
        this.portofinoConfiguration = portofinoConfiguration;
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
            configuration.setProperty("hibernate.show_sql",
                    portofinoConfiguration.getString(
                            PortofinoProperties.HIBERNATE_SHOW_SQL));
            configuration.setProperty("hibernate.connection.provider_class",
                    portofinoConfiguration.getString(
                            PortofinoProperties.HIBERNATE_CONNECTION_PROVIDER_CLASS));
            configuration.setProperty("hibernate.c3p0.min_size",
                    portofinoConfiguration.getString(
                            PortofinoProperties.HIBERNATE_C3P0_MIN_SIZE));
            configuration.setProperty("hibernate.c3p0.max_size",
                    portofinoConfiguration.getString(
                            PortofinoProperties.HIBERNATE_C3P0_MAX_SIZE));
            configuration.setProperty("hibernate.c3p0.timeout",
                    portofinoConfiguration.getString(
                            PortofinoProperties.HIBERNATE_C3P0_TIMEOUT));
            configuration.setProperty("hibernate.c3p0.idle_test_period",
                    portofinoConfiguration.getString(
                            PortofinoProperties.HIBERNATE_C3P0_IDLE_TEST_PERIOD));

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
            for (com.manydesigns.portofino.model.database.Table aTable :
                    schema.getTables()) {
                logger.debug("Class - {}", aTable.getQualifiedName());
                com.manydesigns.portofino.model.database.PrimaryKey primaryKey =
                        aTable.getPrimaryKey();
                if (primaryKey == null) {
                    logger.debug("Skipping table without primary key: {}",
                            aTable.getQualifiedName());
                    continue;
                }
                if (!primaryKey.isValid()) {
                    logger.debug("Skipping table with invalid primary key: {}",
                            aTable.getQualifiedName());
                    continue;
                }
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
            for (com.manydesigns.portofino.model.database.Table aTable :
                    schema.getTables()) {
                for (ForeignKey rel : aTable.getForeignKeys()) {
                    if (BooleanUtils.isTrue(aTable.getManyToMany())) {
                        logger.debug(MessageFormat.format("Many to one - {0} {1}",
                                aTable.getQualifiedName(), rel.getName()));
                        createM2O(configuration, mappings, rel);
                    }
                }
            }
        }
    }

    private void o2mMapping(Database database, Configuration configuration, Mappings mappings) {
        for (Schema schema : database.getSchemas()) {
            for (com.manydesigns.portofino.model.database.Table aTable :
                    schema.getTables()) {
                for (ForeignKey rel : aTable.getOneToManyRelationships()) {
                     logger.debug(MessageFormat.format("One to many - {0} {1}",
                                aTable.getQualifiedName(), rel.getName()));
                    createO2M(configuration, mappings, rel);
                }
            }
        }
    }

    protected RootClass createTableMapping(Mappings mappings,
                                           com.manydesigns.portofino.model.database.Table aTable) {


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


        List<com.manydesigns.portofino.model.database.Column> columnList =
                new ArrayList<com.manydesigns.portofino.model.database.Column>();

        for(com.manydesigns.portofino.model.database.Column modelColumn : aTable.getColumns()) {
            int jdbcType = modelColumn.getJdbcType();
            Class javaType = modelColumn.getActualJavaType();
            
            //First param = null ==> doesn't really set anything, just check
            boolean hibernateTypeOk =
                    HibernateConfig.setHibernateType(null, modelColumn, javaType, jdbcType);
            if (hibernateTypeOk) {
                columnList.add(modelColumn);
            } else {
                logger.error("Cannot find Hibernate type for table: {}, column: {}, jdbc type: {}, type name: {}. Skipping column.",
                        new Object[]{
                                aTable.getTableName(),
                                modelColumn.getColumnName(),
                                jdbcType,
                                javaType.getName()
                        });
            }
        }

        //Primary keys
        List<com.manydesigns.portofino.model.database.Column> columnPKList
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

        for (com.manydesigns.portofino.model.database.Column column
                : columnList) {
            createColumn(mappings, clazz, tab, column);

        }

        return clazz;
    }

    protected void createColumn(Mappings mappings, RootClass clazz,
                                Table tab,
                                com.manydesigns.portofino.model.database.Column column) {
        Column col = new Column();
        col.setName(escapeName(column.getColumnName()));
        col.setLength(column.getLength());
        col.setPrecision(column.getLength());
        col.setScale(column.getScale());
        col.setNullable(column.isNullable());
        String columnType = column.getColumnType();
        int jdbcType = column.getJdbcType();

        col.setSqlTypeCode(jdbcType);
        col.setSqlType(columnType);
        tab.addColumn(col);

        Property prop = new Property();
        prop.setName(column.getActualPropertyName());
        prop.setNodeName(column.getActualPropertyName());

        SimpleValue value = new SimpleValue(mappings, tab);

        if (!setHibernateType(value, column, column.getActualJavaType(), jdbcType)) {
            logger.error("Skipping column");
            return;
        }

        value.addColumn(col);
        clazz.addProperty(prop);
        prop.setValue(value);

        mappings.addColumnBinding(column.getColumnName(),
                col, tab);
    }

    protected void createPKComposite(Mappings mappings,
                                     com.manydesigns.portofino.model.database.Table mdTable,
                                     String pkName, RootClass clazz,
                                     Table tab,
                                     List<com.manydesigns.portofino.model.database.Column> columnPKList) {


        PrimaryKey primaryKey = new PrimaryKey();
        primaryKey.setName(pkName);
        primaryKey.setTable(tab);

        clazz.setEmbeddedIdentifier(true);
        Component component = new Component(mappings, clazz);
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

        boolean hasErrors = false;
        for (com.manydesigns.portofino.model.database.Column
                column : columnPKList) {
            if (column == null ) {
                throw new InternalError("Null column");
            }

            Column col = new Column();
            col.setName(escapeName(column.getColumnName()));
            String columnType = column.getColumnType();
            int jdbcType = column.getJdbcType();

            col.setSqlTypeCode(jdbcType);
            col.setSqlType(columnType);
            primaryKey.addColumn(col);
            SimpleValue value = new SimpleValue(mappings, tab);

            hasErrors = !setHibernateType(value, column, column.getActualJavaType(), jdbcType) || hasErrors;

            value.addColumn(col);
            primaryKey.addColumn(col);
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
        if (hasErrors) {
            // TODO PAOLO: se la PK non e' buona, tutta la tabella dovrebbe saltare
            logger.error("Skipping foreign key");
            return;
        }

        tab.setIdentifierValue(component);
        clazz.setIdentifier(component);
        clazz.setDiscriminatorValue(name);

        tab.setPrimaryKey(primaryKey);
    }


    protected void createPKSingle(Mappings mappings,
                                  com.manydesigns.portofino.model.database.Table mdTable,
                                  String pkName, RootClass clazz,
                                  Table tab,
                                  List<com.manydesigns.portofino.model.database.Column> columnPKList) {
        PrimaryKeyColumn pkcol =mdTable.getPrimaryKey().getPrimaryKeyColumns().get(0);
        com.manydesigns.portofino.model.database.Column
                column = columnPKList.get(0);
        SimpleValue id = new SimpleValue(mappings, tab);
        final PrimaryKey primaryKey = new PrimaryKey();
        primaryKey.setName(pkName);
        primaryKey.setTable(tab);
        tab.setPrimaryKey(primaryKey);

        //Make the defaults explicit. See section 5.1.4.5. Assigned identifiers in the Hibernate reference
        //(http://docs.jboss.org/hibernate/core/3.3/reference/en/html/mapping.html)
        id.setIdentifierGeneratorStrategy("assigned");
        id.setNullValue("undefined");

        id.setTypeName(column.getColumnType()); //TODO alessio serve? viene sovrascritto sotto
        Column col = new Column();
        col.setName(escapeName(column.getColumnName()));
        String columnType = column.getColumnType();
        col.setValue(id);
        col.setLength(column.getLength());
        col.setPrecision(column.getLength());
        col.setScale(column.getScale());
        col.setNullable(column.isNullable());
        int jdbcType = column.getJdbcType();

        col.setSqlTypeCode(jdbcType);
        col.setSqlType(columnType);
        if (!setHibernateType(id, column, column.getActualJavaType(), jdbcType)) {
            // TODO PAOLO: se la PK non e' buona, tutta la tabella dovrebbe saltare
            logger.error("Skipping foreign key");
            return;
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
                com.manydesigns.portofino.model.database.TableGenerator) {
            manageTableGenerator(mappings, tab, id,
                    (com.manydesigns.portofino.model.database.TableGenerator) generator);
        }

        if (column.isAutoincrement()) {
            manageIdentityGenerator(mappings, tab, id);
        }

        if (null!=generator && generator instanceof
                com.manydesigns.portofino.model.database.IncrementGenerator){
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
                                          com.manydesigns.portofino.model.database.TableGenerator generator) {
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

        com.manydesigns.portofino.model.database.Table manyMDTable =
                relationship.getFromTable();
        com.manydesigns.portofino.model.database.Table oneMDTable =
                relationship.getToTable();

        //Se la classe One non e' dinamica e
        // non ha la proprieta' non inserisco la relazione
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
                //se non c'e' non inserisco la relazione
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
        if (clazzOne == null) {
            logger.error("Cannot find table '{}' as 'one' side of foreign key '{}'. Skipping relationship.",
                    oneMDQualifiedTableName, relationship.getName());
            return;
        }

        PersistentClass clazzMany =
                config.getClassMapping(manyMDQualifiedTableName);
        if (clazzMany == null) {
            logger.error("Cannot find table '{}' as 'many' side of foreign key '{}'. Skipping relationship.",
                    manyMDQualifiedTableName, relationship.getName());
            return;
        }

        //Uso i Bag perche' i set non funzionano con i componenti dinamici
        Bag set = new Bag(mappings, clazzOne);
        // Mettere Lazy in debug a false per ottenere subito eventuali errori
        // nelle relazioni
        set.setLazy(LAZY);

        set.setRole(relationship.getToTable()+"."+relationship.getActualManyPropertyName());
        set.setNodeName(relationship.getActualManyPropertyName());
        set.setCollectionTable(clazzMany.getTable());
        OneToMany oneToMany = new OneToMany(mappings, set.getOwner());
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
            dv = createFKComposite(mappings, relationship, manyMDTable, clazzOne,
                    clazzMany, set, tableMany, tableOne, oneColumns, manyColumns);
        } else {  //chiave straniera singola
            dv = createFKSingle(mappings, clazzOne, clazzMany, tableOne,
                    oneColumns, manyColumns, refs);
        }

        tableMany.createForeignKey(relationship.getName(),
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
        if (ForeignKeyConstraintType.importedKeyCascade.name()
                .equalsIgnoreCase(relationship.getOnDelete())){
            prop.setCascade("delete");
        } else {
            prop.setCascade("none");
        }
        clazzOne.addProperty(prop);

        //if(!StringUtils.)
    }

    private DependantValue createFKComposite(
            Mappings mappings,
            com.manydesigns.portofino.model.database.ForeignKey relationship,
            com.manydesigns.portofino.model.database.Table manyMDTable,
            PersistentClass clazzOne,
            PersistentClass clazzMany, Bag set,
            Table tableMany, Table tableOne,
            List<Column> oneColumns,
            List<Column> manyColumns) {
        DependantValue dv;
        Component component = new Component(mappings, set);
        component.setDynamic(manyMDTable.getActualJavaClass()==null);
        component.setEmbedded(true);
        dv = new DependantValue(mappings, clazzMany.getTable(), component);
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

    private DependantValue createFKSingle(
            Mappings mappings, PersistentClass clazzOne,
            PersistentClass clazzMany, Table tableOne, List<Column> oneColumns,
            List<Column> manyColumns, List<Reference> refs) {
        DependantValue dv;
        Property refProp;

        String colFromName = refs.get(0).getFromColumn();
        String colToName = refs.get(0).getToColumn();
        refProp = getRefProperty(clazzOne, colToName);
        dv = new DependantValue(mappings, clazzMany.getTable(),
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
        com.manydesigns.portofino.model.database.Table manyMDTable =
                relationship.getFromTable();
        com.manydesigns.portofino.model.database.Table oneMDTable =
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

        ManyToOne m2o = new ManyToOne(mappings, tab);
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
        // Portofino handles all tables in a case-sensitive way
        return "`"+name+"`";
    }

    public static boolean setHibernateType(@Nullable SimpleValue value,
                                 com.manydesigns.portofino.model.database.Column column,
                                 Class javaType,
                                 final int jdbcType) {
        String typeName;
        Properties typeParams = null;
        if(javaType == null) {
            return false;
        }
        if (javaType == Long.class) {
            typeName = LongType.INSTANCE.getName();
        } else if (javaType == Short.class) {
            typeName = ShortType.INSTANCE.getName();
        } else if (javaType == Integer.class) {
            typeName = IntegerType.INSTANCE.getName();
        } else if (javaType == Byte.class) {
            typeName = ByteType.INSTANCE.getName();
        } else if (javaType == Float.class) {
            typeName = FloatType.INSTANCE.getName();
        } else if (javaType == Double.class) {
            typeName = DoubleType.INSTANCE.getName();
        } else if (javaType == Character.class) {
            typeName = CharacterType.INSTANCE.getName();
        } else if (javaType == String.class) {
            typeName = StringType.INSTANCE.getName();
        } else if (java.util.Date.class.isAssignableFrom(javaType)) {
            switch (jdbcType) {
                case Types.DATE:
                    typeName = DateType.INSTANCE.getName();
                    break;
                case Types.TIME:
                    typeName = TimeType.INSTANCE.getName();
                    break;
                case Types.TIMESTAMP:
                    typeName = TimestampType.INSTANCE.getName();
                    break;
                default:
                    typeName = null;
            }
        } else if (javaType == Boolean.class) {
            if(jdbcType == Types.BIT || jdbcType == Types.BOOLEAN) {
                typeName = BooleanType.INSTANCE.getName();
            } else if(jdbcType == Types.NUMERIC || jdbcType == Types.DECIMAL) {
                typeName = DbUtil.NUMERIC_BOOLEAN.getName();
            } else if(jdbcType == Types.CHAR || jdbcType == Types.VARCHAR) {
                typeName = StringBooleanType.class.getName();
                typeParams = new Properties();
                typeParams.setProperty("true", trueString != null ? trueString : StringBooleanType.NULL);
                typeParams.setProperty("false", falseString != null ? falseString : StringBooleanType.NULL);
                typeParams.setProperty("sqlType", String.valueOf(jdbcType));
            } else {
                typeName = null;
            }
        } else if (javaType == BigDecimal.class) {
            typeName = BigDecimalType.INSTANCE.getName();
        } else if (javaType == BigInteger.class) {
            typeName = BigIntegerType.INSTANCE.getName();
        } else if (javaType == byte[].class) {
            typeName = BlobType.INSTANCE.getName();
        } else {
            typeName = null;
        }

        if (typeName == null) {
            logger.error("Unsupported type (java type: {}, jdbc type: {}) " +
                    "for column '{}'.",
                    new Object[] {
                            javaType,
                            jdbcType,
                            column.getColumnName()
                    });
            return false;
        }

        if (value != null) {
            value.setTypeName(typeName);
            if(typeParams != null) {
                value.setTypeParameters(typeParams);
            }
        }
        return true;
    }

    public String getTrueString() {
        return trueString;
    }

    public void setTrueString(String trueString) {
        this.trueString = trueString;
    }

    public String getFalseString() {
        return falseString;
    }

    public void setFalseString(String falseString) {
        this.falseString = falseString;
    }
}