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


import org.hibernate.SessionFactory;
import org.hibernate.Hibernate;
import org.hibernate.mapping.*;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.PrimaryKey;
import org.hibernate.cfg.Configuration;

import java.util.*;
import java.util.Map;
import java.util.List;
import java.sql.Types;

import com.manydesigns.portofino.base.model.*;

/**
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Angelo    Lupo       - angelo.lupo@manydesigns.com
 * @author Paolo     Predonzani - paolo.predonzani@manydesigns.com
 */
public class HibernateConfig {

    private static final Map<String, SessionFactory> sessionFactories = new HashMap<String, SessionFactory>();

    private static void buildSessionFactory(DataModel model) {
        try {


            for (Database database : model.getDatabases()) {
                Configuration result = new Configuration().setProperty("default_entity_mode", "dynamic-map");

                Connection connection = database.getConnection();
                result.setProperty("hibernate.connection.url", connection.getConnectionUrl());
                result.setProperty("hibernate.connection.driver_class", connection.getDriverClass());
                result.setProperty("hibernate.connection.username", connection.getUsername());
                result.setProperty("hibernate.connection.password", connection.getPassword());
                result.setProperty("hibernate.current_session_context_class",
                        "org.hibernate.context.ThreadLocalSessionContext");
                result.setProperty("hibernate.show_sql", "true");
                result.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
               


                for (Schema schema : database.getSchemas()) {
                    for (com.manydesigns.portofino.base.model.Table aTable : schema.getTables()) {
                        RootClass clazz = createTableMapping(aTable);
                        result.createMappings().addClass(clazz);
                    }
                }


                sessionFactories.put(database.getDatabaseName(), result.buildSessionFactory());
            }


        }
        catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed." + ex);
            ex.printStackTrace();
            throw new ExceptionInInitializerError(ex);
        }
    }

    private static RootClass createTableMapping(
            com.manydesigns.portofino.base.model.Table aTable) {


        RootClass clazz = new RootClass();
        clazz.setEntityName(aTable.getTableName());

        Table tab = new Table();
        tab.setName(aTable.getTableName());
        tab.setSchema(aTable.getSchemaName());
        clazz.setTable(tab);

        final List<com.manydesigns.portofino.base.model.Column> columnList =
                new ArrayList<com.manydesigns.portofino.base.model.Column>();
        columnList.addAll(aTable.getColumns());
        final List<com.manydesigns.portofino.base.model.Column> columnPKList
                = aTable.getPrimaryKey().getColumns();

        columnList.removeAll(columnPKList);

        for (com.manydesigns.portofino.base.model.Column column
                : columnList) {
            createColumn(clazz, tab, column);
        }


        createPKColumn(aTable.getPrimaryKey().getName(), clazz, tab, columnPKList);

        /*
        tab.addColumn(col);
        Column col2 = new Column();
        col2.setName("codistat");
        col2.setSqlTypeCode(Types.VARCHAR);
        tab.addColumn(col2);
        Property propId = new Property();
        propId.setName("codistat");
        SimpleValue valueId = new SimpleValue();
        valueId.setTypeName(Hibernate.STRING.getName());
        valueId.addColumn(col2);
        propId.setValue(valueId);
        clazz.setIdentifierProperty(propId);
        */
        return clazz;
    }

    private static void createColumn(RootClass clazz,
                                     Table tab, com.manydesigns.portofino.base.model.Column column) {
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

    private static void createPKColumn(String pkName, RootClass clazz,
                                       Table tab, List<com.manydesigns.portofino.base.model.Column> columnPKList) {
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
            component.addProperty(prop);

        }
        clazz.setIdentifier(component);
    }

    public static Map<String, SessionFactory> getSessionFactory(DataModel model) {
        buildSessionFactory(model);
        return sessionFactories;
    }
}
