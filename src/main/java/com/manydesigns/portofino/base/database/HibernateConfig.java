/*
 * Copyright (C) 2005-2009 ManyDesigns srl.  All rights reserved.
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
package com.manydesigns.portofino.base.database;


import com.manydesigns.portofino.base.model.*;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.mapping.*;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.PrimaryKey;
import org.hibernate.mapping.Table;

import java.util.*;
import java.util.List;
import java.util.Map;

/**
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Angelo    Lupo       - angelo.lupo@manydesigns.com
 * @author Paolo     Predonzani - paolo.predonzani@manydesigns.com
 */
public class HibernateConfig {

    protected final Map<String, SessionFactory> sessionFactories =
            new HashMap<String, SessionFactory>();

    protected void buildSessionFactory(DataModel model) {
        try {
            for (Database database : model.getDatabases()) {
                Configuration result = new Configuration()
                        .setProperty("default_entity_mode", "dynamic-map");

                Connection connection = database.getConnection();
                result.setProperty("hibernate.connection.url",
                        connection.getConnectionUrl())
                        .setProperty("hibernate.connection.driver_class",
                                connection.getDriverClass())
                        .setProperty("hibernate.connection.username",
                                connection.getUsername())
                        .setProperty("hibernate.connection.password",
                                connection.getPassword())
                        .setProperty("hibernate.current_session_context_class",
                                "org.hibernate.context.ThreadLocalSessionContext")
                        .setProperty("hibernate.show_sql", "true");


                for (Schema schema : database.getSchemas()) {
                    for (com.manydesigns.portofino.base.model.Table aTable :
                            schema.getTables()) {
                        RootClass clazz = createTableMapping(result, aTable);
                        result.createMappings().addClass(clazz);
                    }
                }
                sessionFactories.put(database.getDatabaseName(),
                        result.buildSessionFactory());
            }
        }
        catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed." + ex);
            ex.printStackTrace();
            throw new ExceptionInInitializerError(ex);
        }
    }

    protected RootClass createTableMapping(Configuration conf,
                         com.manydesigns.portofino.base.model.Table aTable) {


        RootClass clazz = new RootClass();
        clazz.setEntityName(aTable.getQualifiedName());

        Table tab = new Table();
        tab.setName(aTable.getTableName());
        tab.setSchema(aTable.getSchemaName());
        clazz.setTable(tab);

        final List<com.manydesigns.portofino.base.model.Column> columnList =
                new ArrayList<com.manydesigns.portofino.base.model.Column>();
        columnList.addAll(aTable.getColumns());

        //Primary keys
        List<com.manydesigns.portofino.base.model.Column> columnPKList
                = aTable.getPrimaryKey().getColumns();

        createPKColumn(aTable.getPrimaryKey().getName(), clazz, tab,
                columnPKList);

        //Foreign keys
        for (Relationship rel : aTable.getRelationships()) {
            List<com.manydesigns.portofino.base.model.Column> columnsinFKList
                    = new ArrayList<com.manydesigns.portofino.base.model.Column>();
            for (Reference ref : rel.getReferences()) {
                columnsinFKList.add(ref.getFromColumn());
            }
            columnList.removeAll(columnsinFKList);
            createFKReference(conf, clazz, tab, rel, columnsinFKList);
        }

        //Other columns
        columnList.removeAll(columnPKList);


        for (com.manydesigns.portofino.base.model.Column column
                : columnList) {
            createColumn(clazz, tab, column);
        }

        return clazz;
    }

    protected void createColumn(RootClass clazz,
                                Table tab,
                                com.manydesigns.portofino.base.model.Column column) {
        Column col = new Column();
        col.setName(column.getColumnName());
        col.setSqlTypeCode(DbUtil.getSQLType(column.getColumnType()));

        Property prop = new Property();
        prop.setName(column.getColumnName());
        SimpleValue value = new SimpleValue();
        value.setTable(tab);
        value.setTypeName(DbUtil.getHibernateType(column.getColumnType())
                .getName());
        value.addColumn(col);

        tab.addColumn(col);
        prop.setValue(value);
        clazz.addProperty(prop);
    }

    protected void createPKColumn(String pkName, RootClass clazz,
                                  Table tab,
                                  List<com.manydesigns.portofino.base.model.Column> columnPKList) {
        Component component = new Component(clazz);
        component.setDynamic(true);
        final PrimaryKey primaryKey = new PrimaryKey();
        primaryKey.setName(pkName);
        tab.setPrimaryKey(primaryKey);
        for (com.manydesigns.portofino.base.model.Column
                column : columnPKList) {
            Property prop = new Property();
            Column col = new Column();
            col.setName(column.getColumnName());
            prop.setName(column.getColumnName());
            SimpleValue value = new SimpleValue();
            value.setTable(tab);
            value.setTypeName(DbUtil.getHibernateType(column.getColumnType())
                    .getName());
            value.addColumn(col);
            tab.getPrimaryKey().addColumn(col);
            tab.addColumn(col);
            prop.setValue(value);
            clazz.addProperty(prop);
            component.addProperty(prop);
        }
        clazz.setIdentifier(component);
        clazz.setIdentifierMapper(component);
    }

    protected void createFKReference(Configuration config, RootClass clazz,
                                     Table tab,
                                     Relationship relationship,
                                     List<com.manydesigns.portofino.base.model.Column> cols) {
        Property prop = new Property();
        prop.setName(relationship.getName());


        ManyToOne m2o = new ManyToOne(tab);
        m2o.createForeignKey();


        final HashMap<String, PersistentClass> persistentClasses =
                new HashMap<String, PersistentClass>();
        persistentClasses.put(relationship.getTable().getQualifiedName(),
                config.getClassMapping(relationship.getTable().getQualifiedName()));
        m2o.setReferencedEntityName(relationship.getTable().getQualifiedName());
        m2o.createPropertyRefConstraints(persistentClasses);
        for (com.manydesigns.portofino.base.model.Column column : cols) {
            Column col = new Column();
            col.setName(column.getColumnName());
            m2o.addColumn(col);
        }
        prop.setValue(m2o);
        clazz.addProperty(prop);
    }

    public Map<String, SessionFactory> build(DataModel model) {
        buildSessionFactory(model);
        return sessionFactories;
    }
}
