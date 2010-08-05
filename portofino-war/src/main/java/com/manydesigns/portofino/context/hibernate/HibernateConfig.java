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


import com.manydesigns.portofino.database.DatabaseAbstraction;
import com.manydesigns.portofino.database.DbUtil;
import com.manydesigns.portofino.database.JdbcConnectionProvider;
import com.manydesigns.portofino.database.Type;
import com.manydesigns.portofino.model.Database;
import com.manydesigns.portofino.model.Reference;
import com.manydesigns.portofino.model.Relationship;
import com.manydesigns.portofino.model.Schema;
import org.hibernate.FetchMode;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Mappings;
import org.hibernate.mapping.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Angelo    Lupo       - angelo.lupo@manydesigns.com
 * @author Paolo     Predonzani - paolo.predonzani@manydesigns.com
 */
public class HibernateConfig {

    protected final DatabaseAbstraction databaseAbstraction;

    public HibernateConfig(DatabaseAbstraction databaseAbstraction) {
        this.databaseAbstraction = databaseAbstraction;
    }

    public Configuration buildSessionFactory(Database database) {
        try {
            Configuration configuration = new Configuration()
                    .setProperty("default_entity_mode", "dynamic-map");

            JdbcConnectionProvider connectionProvider = 
                    (JdbcConnectionProvider)databaseAbstraction.getConnectionProvider();
            configuration.setProperty("hibernate.connection.url",
                    connectionProvider.getJdbcConnectionURL())
                    .setProperty("hibernate.connection.driver_class",
                            connectionProvider.getJdbcDriverClass())
                    .setProperty("hibernate.connection.username",
                            connectionProvider.getJdbcUsername())
                    .setProperty("hibernate.connection.password",
                            connectionProvider.getJdbcPassword())
                    .setProperty("hibernate.current_session_context_class",
                            "org.hibernate.context.ThreadLocalSessionContext")
                    .setProperty("hibernate.show_sql", "true");

            Mappings mappings = configuration.createMappings();
            for (Schema schema : database.getSchemas()) {
                for (com.manydesigns.portofino.model.Table aTable :
                        schema.getTables()) {
                    RootClass clazz = createTableMapping(configuration,
                            mappings, aTable);
                    mappings.addClass(clazz);
                    mappings.addImport(clazz.getEntityName(),
                            aTable.getTableName());
                    mappings.addImport(clazz.getEntityName(),
                            clazz.getEntityName());

                }
            }

           for (Schema schema : database.getSchemas()) {
                for (com.manydesigns.portofino.model.Table aTable :
                        schema.getTables()) {
                    for (Relationship rel: aTable.getOneToManyRelationships()) {
                        createO2M(configuration, mappings, rel);
                    }
                }
            }
            return configuration;
        }
        catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed." + ex);
            ex.printStackTrace();
            throw new ExceptionInInitializerError(ex);
        }
    }

    protected RootClass createTableMapping(Configuration conf, Mappings mappings,
                         com.manydesigns.portofino.model.Table aTable) {


        RootClass clazz = new RootClass();
        clazz.setEntityName(aTable.getQualifiedName());
        clazz.setLazy(true);
        Table tab = mappings.addTable(aTable.getSchemaName(), null,
                aTable.getTableName() ,null, false);
        tab.setName(aTable.getTableName());
        tab.setSchema(aTable.getSchemaName());
        mappings.addTableBinding(aTable.getSchemaName(), null,
                aTable.getTableName(), aTable.getTableName(), null );
        clazz.setTable(tab);
        clazz.setNodeName(aTable.getTableName());

        final List<com.manydesigns.portofino.model.Column> columnList =
                new ArrayList<com.manydesigns.portofino.model.Column>();
        columnList.addAll(aTable.getColumns());

        //Primary keys
        List<com.manydesigns.portofino.model.Column> columnPKList
                = aTable.getPrimaryKey().getColumns();

        createPKColumn(mappings, aTable, aTable.getPrimaryKey().getPkName(),
                clazz, tab, columnPKList);
        //Other columns
        columnList.removeAll(columnPKList);

        for (com.manydesigns.portofino.model.Column column
                : columnList) {
            createColumn(mappings, clazz,  tab, column);

        }

        return clazz;
    }

    protected void createColumn(Mappings mappings, RootClass clazz,
                        Table tab,
                        com.manydesigns.portofino.model.Column column) {
        Column col = new Column();
        col.setName(column.getColumnName());

        String columnType = column.getColumnType();
        System.out.println("Column type: " + columnType);

        Type type = databaseAbstraction.getTypeByName(columnType);
        System.out.println("Portofino type: " + type);
        col.setSqlTypeCode(type.getDataType());

        Property prop = new Property();
        prop.setName(column.getColumnName());
        prop.setNodeName(column.getColumnName());
        SimpleValue value = new SimpleValue();
        value.setTable(tab);
        org.hibernate.type.Type hibernateType = DbUtil.getHibernateType(columnType);
        System.out.println("Hibernate type: " + hibernateType);
        value.setTypeName(hibernateType.getName());
        value.addColumn(col);
        tab.addColumn(col);
        prop.setValue(value);
        clazz.addProperty(prop);
        mappings.addColumnBinding(column.getColumnName(),
                    col, tab);
    }

    protected void createPKColumn(Mappings mappings,
              com.manydesigns.portofino.model.Table mdTable,
              String pkName, RootClass clazz,
              Table tab,
              List<com.manydesigns.portofino.model.Column> columnPKList) {
        clazz.setEmbeddedIdentifier(true);
        Component component = new Component(clazz);
        component.setDynamic(true);
        component.setRoleName(mdTable.getQualifiedName()+".id");
        component.setEmbedded(true);
        component.setNodeName(mdTable.getTableName());
        component.setKey(true);
        component.setNullValue("undefined");
        final PrimaryKey primaryKey = new PrimaryKey();
        primaryKey.setName(pkName);
        primaryKey.setTable(tab);
        tab.setPrimaryKey(primaryKey);
        for (com.manydesigns.portofino.model.Column
                column : columnPKList) {

            Column col = new Column();
            col.setName(column.getColumnName());
            primaryKey.addColumn(col);

            SimpleValue value = new SimpleValue();
            value.setTable(tab);
            value.setTypeName(DbUtil.getHibernateType(column.getColumnType())
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

            //clazz.addProperty(prop);
            component.addProperty(prop);
            mappings.addColumnBinding(column.getColumnName(),
                    col, tab);

        }
        tab.setIdentifierValue(component);
        clazz.setIdentifier(component);
        //clazz.setIdentifierMapper(component);
        clazz.setDiscriminatorValue(mdTable.getQualifiedName());
    }



    protected void createO2M(
            Configuration config,
            Mappings mappings,
            Relationship relationship) {
        com.manydesigns.portofino.model.Table manyTable
                = relationship.getFromTable();
        com.manydesigns.portofino.model.Table oneTable
                = relationship.getToTable();


        PersistentClass clazzOne = config.getClassMapping
                (oneTable.getQualifiedName());
        PersistentClass clazzMany = config.getClassMapping
                (manyTable.getQualifiedName());

        Bag set = new Bag(clazzOne);
        set.setLazy(true);
        set.setRole(manyTable.getQualifiedName()+"."
                +relationship.getRelationshipName());
        set.setNodeName(relationship.getRelationshipName());
        set.setCollectionTable(clazzMany.getTable());
        OneToMany oneToMany = new OneToMany( set.getOwner() );
        set.setElement( oneToMany );
        oneToMany.setReferencedEntityName(manyTable.getQualifiedName() );
        oneToMany.setAssociatedClass(clazzMany);
        oneToMany.setEmbedded(true);
        set.setSorted(false);
        set.setFetchMode(FetchMode.DEFAULT);
        //Riferimenti alle colonne
        Component component = new Component(set);
        DependantValue dv = new DependantValue(clazzMany.getTable(), component);
        dv.setNullable(true);
        dv.setUpdateable(true);

        component.setDynamic(true);
        component.setEmbedded(true);

        for (Reference ref:  relationship.getReferences() ){
            String colToName = ref.getToColumn().getColumnName();
            String colFromName = ref.getFromColumn().getColumnName();
            Iterator it = clazzMany.getTable().getColumnIterator();
            while (it.hasNext())
            {
                Column col = (Column) it.next();
                if (col.getName().equals(colFromName)){
                    dv.addColumn(col);
                    break;
                }
            }

            Property refProp;
            try{
                refProp = clazzOne.getProperty(colToName);
            } catch (Exception e) {
                refProp = ((Component) clazzOne.getIdentifier())
                        .getProperty(colToName);
            }
            component.addProperty(refProp);
        }

        set.setKey(dv);

        mappings.addCollection(set);

        Property prop = new Property();
        prop.setName(relationship.getRelationshipName());
        prop.setNodeName(relationship.getRelationshipName());
        prop.setValue(set);
        clazzOne.addProperty(prop);
   }

   protected void createFKReference(Configuration config, RootClass clazz,
                         Table tab,
                         Relationship relationship,
                         List<com.manydesigns.portofino.model.Column> cols) {


        ManyToOne m2o = new ManyToOne(tab);
        m2o.createForeignKey();
        final HashMap<String, PersistentClass> persistentClasses =
                new HashMap<String, PersistentClass>();
        persistentClasses.put(relationship.getToTable().getQualifiedName(),
                config.getClassMapping(relationship.getToTable().getQualifiedName()));
        m2o.setReferencedEntityName(relationship.getToTable().getQualifiedName());
        m2o.createPropertyRefConstraints(persistentClasses);
        for (com.manydesigns.portofino.model.Column column : cols) {
            Column col = new Column();
            col.setName(column.getColumnName());
            m2o.addColumn(col);
        }

        Property prop = new Property();
        prop.setName(relationship.getRelationshipName());
        prop.setValue(m2o);
        clazz.addProperty(prop);

    }

}
