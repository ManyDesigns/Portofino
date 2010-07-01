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
package com.manydesigns.portofino.base.model;

import org.apache.commons.lang.StringUtils;
import javax.xml.stream.*;
import javax.xml.stream.XMLInputFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.text.MessageFormat;

/**
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Angelo    Lupo       - angelo.lupo@manydesigns.com
 * @author Paolo     Predonzani - paolo.predonzani@manydesigns.com
 */
public class DBParser {
    private static final String DATAMODEL = "datamodel";
    private static final String DATABASE = "database";
    private static final String CONNECTION = "connection";
    private static final String SCHEMA = "schema";
    private static final String SCHEMAS = SCHEMA + "s";
    private static final String TABLE = "table";
    private static final String TABLES = TABLE + "s";
    private static final String COLUMN = "column";
    private static final String COLUMNS = COLUMN + "s";
    private static final String PRIMARY_KEY = "primaryKey";
    private static final String RELATIONSHIP = "relationship";
    private static final String RELATIONSHIPS = RELATIONSHIP + "s";
    private static final String REFERENCE = "reference";


    public DataModel parse(String fileName) throws Exception {
        DataModel dataModel = new DataModel();
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        ClassLoader cl = this.getClass().getClassLoader();
        InputStream input = cl.getResourceAsStream(fileName);
        XMLStreamReader xmlStreamReader = inputFactory.createXMLStreamReader(input);
        doStartDocument(xmlStreamReader, dataModel);
        return dataModel;
    }

    private void doStartDocument(XMLStreamReader xmlStreamReader,
                                 DataModel dataModel) throws Exception {
        int event = -1;
        while (xmlStreamReader.hasNext()) {
            if (event == XMLStreamConstants.END_ELEMENT
                    && xmlStreamReader.getLocalName().equals(DATAMODEL)) {
                break;
            }
            event = next(xmlStreamReader);

            if (event == XMLStreamConstants.START_ELEMENT
                    && xmlStreamReader.getLocalName().equals(DATAMODEL)) {
                doDataModel(xmlStreamReader, dataModel);
            }
        }
    }

    private void doDataModel(XMLStreamReader xmlStreamReader, DataModel dataModel) throws Exception {
         doDataBase(xmlStreamReader, dataModel);
    }

    private void doDataBase(XMLStreamReader xmlStreamReader, DataModel dataModel) throws Exception {
        int event=-1;
        String lName=null;
        int dbopen = 0;
        boolean dbpresent = false;


        while (xmlStreamReader.hasNext()) {
            if (event == XMLStreamConstants.END_ELEMENT && DATABASE.equals(lName)) {
                break;
            }
            event = next(xmlStreamReader);
            lName = xmlStreamReader.getLocalName();
            if (event == XMLStreamConstants.START_ELEMENT && lName.equals(DATABASE)) {
                dbpresent = true;
                dbopen++;
                Database db = new Database();
                String attName;
                String attValue;

                attName = xmlStreamReader.getAttributeLocalName(0);
                attValue = xmlStreamReader.getAttributeValue(0);

                if (attName.equals("name")) {
                    db.setName(attValue);
                } else {
                    throw new Exception("TAG " + DATABASE + ", attr name non presente");
                }
                dataModel.getDatabases().add(db);
                doConnection(xmlStreamReader, db);
                doSchemas(xmlStreamReader, dataModel, db);
            }

            if (event == XMLStreamConstants.END_ELEMENT && lName.equals(DATABASE)) {
                dbopen--;
            }
        }
        if (!dbpresent) {
            throw new Exception("TAG " + DATABASE + " non presente");
        }
        if (dbopen != 0) {
            throw new Exception("TAG " + DATABASE + " non chiuso");
        }

    }

    private void doConnection(XMLStreamReader xmlStreamReader, Database db) throws Exception {
        Connection conn = new Connection();
        int event = next(xmlStreamReader);
        String lName = xmlStreamReader.getLocalName();
        if (event == XMLStreamConstants.START_ELEMENT && lName.equals(CONNECTION)) {
            String attName;
            //String attValue;
            List<String> expectedValList = new ArrayList<String>();
            expectedValList.add("type");
            expectedValList.add("driver");
            expectedValList.add("url");
            expectedValList.add("username");
            expectedValList.add("password");
            for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++) {
                attName = xmlStreamReader.getAttributeLocalName(i);
                //attValue = xmlStreamReader.getAttributeValue(i);

                if (attName.equals("type")) {
                    expectedValList.remove(attName);
                    //conn.setType(attValue);
                    continue;
                }
                if (attName.equals("driver")) {
                    expectedValList.remove(attName);
                    //conn.setDriver(attValue);
                    continue;
                }
                if (attName.equals("url")) {
                    expectedValList.remove(attName);
                    //conn.setUrl(attValue);
                    continue;
                }
                if (attName.equals("username")) {
                    expectedValList.remove(attName);
                    //conn.setUsername(attValue);
                    continue;
                }
                if (attName.equals("password")) {
                    expectedValList.remove(attName);
                    //conn.setPassword(attValue);

                }
            }

            if (expectedValList.size() != 0) {

                throw new Exception(MessageFormat.format("Non sono presenti gli " +
                        "attributi {0} di Connection", StringUtils.join(expectedValList, ", ")));
            }

            db.setConnection(conn);
            if (xmlStreamReader.hasNext()) {
                event = next(xmlStreamReader);
                lName = xmlStreamReader.getLocalName();
                if (event == XMLStreamConstants.END_ELEMENT && lName.equals(CONNECTION)) {
                } else {
                    throw new Exception("TAG " + CONNECTION + " non chiuso");
                }
            }
        } else {
            throw new Exception("TAG Connection non presente");
        }
    }

    private void doSchemas(XMLStreamReader xmlStreamReader, DataModel dm, Database db) throws Exception {
        int event=-1;
        String lName=null;
        int schemaOpen = 0;
        boolean schemaPresent = false;
        if (xmlStreamReader.hasNext()) {
            event = next(xmlStreamReader);
            lName = xmlStreamReader.getLocalName();
            if (event == XMLStreamConstants.START_ELEMENT && lName.equals(SCHEMAS)) {
            } else {
                throw new Exception("TAG " + SCHEMA + "s non presente");
            }
        }
        while (xmlStreamReader.hasNext()) {
            if (event == XMLStreamConstants.END_ELEMENT && SCHEMA.equals(lName)) {
                schemaOpen--;
                break;
            }
            event = next(xmlStreamReader);
            lName = xmlStreamReader.getLocalName();
            if (event == XMLStreamConstants.START_ELEMENT && lName.equals(SCHEMA)) {
                schemaPresent = true;
                schemaOpen++;
                Schema schema = new Schema();
                String attName = xmlStreamReader.getAttributeLocalName(0);
                String attValue = xmlStreamReader.getAttributeValue(0);

                if (attName.equals("name")) {
                    schema.setSchemaName(attValue);
                } else {
                    throw new Exception("TAG " + SCHEMA + ", ATTR Name non presente");
                }
                db.getSchemas().add(schema);
                doTables(xmlStreamReader, dm, schema);
            }


        }
        if (!schemaPresent) {
            throw new Exception("TAG " + SCHEMA + " non presente");
        }
        if (schemaOpen != 0) {
            throw new Exception("TAG " + SCHEMA + " non chiuso");
        }

        if (xmlStreamReader.hasNext()) {
            event = next(xmlStreamReader);
            lName = xmlStreamReader.getLocalName();
            if (event == XMLStreamConstants.END_ELEMENT && lName.equals(SCHEMAS)) {
            } else {
                throw new Exception("TAG " + SCHEMA + "s non chiuso");
            }
        }
    }

    private void doTables(XMLStreamReader xmlStreamReader, DataModel dm, Schema schema) throws Exception {
        int event;
        String lName;
        int tableOpen = 0;
        boolean tablePresent = false;
        if (xmlStreamReader.hasNext()) {
            event = next(xmlStreamReader);
            lName = xmlStreamReader.getLocalName();
            if (event == XMLStreamConstants.START_ELEMENT && lName.equals(TABLES)) {
            } else {
                throw new Exception("TAG " + TABLES + " non presente");
            }
        }
        while (xmlStreamReader.hasNext()) {
            event = next(xmlStreamReader);
            lName = xmlStreamReader.getLocalName();
            if (event == XMLStreamConstants.END_ELEMENT && lName.equals(TABLES)) {

                return;
            }
            if (event == XMLStreamConstants.END_ELEMENT && lName.equals(TABLE)) {
                tableOpen--;
                next(xmlStreamReader);
                xmlStreamReader.getLocalName();
                break;
            }
            if (event == XMLStreamConstants.START_ELEMENT && lName.equals(TABLE)) {
                tablePresent = true;
                tableOpen++;
                Table table = new Table();
                table.setSchemaName(schema.getSchemaName());
                String attName = xmlStreamReader.getAttributeLocalName(0);
                String attValue = xmlStreamReader.getAttributeValue(0);

                if (attName.equals("name")) {
                    table.setTableName(attValue);
                } else {
                    throw new Exception("TAG " + SCHEMA + ", ATTR Name non presente");
                }
                schema.getTables().add(table);
                doColumns(xmlStreamReader, table);
                doPrimaryKey(xmlStreamReader, table);
                doRelationship(xmlStreamReader, dm, table);
            }
        }
    }

    private void doColumns(XMLStreamReader xmlStreamReader, Table table) throws Exception {
        int event;
        String lName;
        int columnOpen = 0;

        if (xmlStreamReader.hasNext()) {
            event = next(xmlStreamReader);
            lName = xmlStreamReader.getLocalName();
            if (event == XMLStreamConstants.START_ELEMENT && lName.equals(COLUMNS)) {
            } else {
                throw new Exception("TAG " + COLUMNS + " non presente");
            }
        }
        while (xmlStreamReader.hasNext()) {
            event = next(xmlStreamReader);
            lName = xmlStreamReader.getLocalName();
            if (event == XMLStreamConstants.END_ELEMENT && lName.equals(COLUMNS)) {
                return;
            }
            if (event == XMLStreamConstants.START_ELEMENT && lName.equals(COLUMN)) {
                columnOpen++;
                Column col = new Column();
                col.setTableName(table.getTableName());
                col.setSchemaName(table.getSchemaName());
                List<String> expectedValList = new ArrayList<String>();
                expectedValList.add("name");
                expectedValList.add(COLUMN + "Type");
                for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++) {
                    String attName = xmlStreamReader.getAttributeLocalName(i);
                    String attValue = xmlStreamReader.getAttributeValue(i);

                    if (attName.equals("name")) {
                        col.setColumnName(attValue);
                        expectedValList.remove(attName);
                        continue;
                    }
                    if (attName.equals(COLUMN + "Type")) {
                        col.setColumnType(attValue);
                        expectedValList.remove(attName);
                        continue;
                    }
                    if (attName.equals("length")) {
                        col.setLength(Integer.parseInt(attValue));
                        continue;
                    }
                    if (attName.equals("nullable")) {
                        col.setNullable(Boolean.parseBoolean(attValue));
                        continue;
                    }
                   
                    if (attName.equals("scale")) {
                        col.setScale(Integer.parseInt(attValue));

                    }
                }

                if (expectedValList.size() != 0) {

                    throw new Exception(MessageFormat.format("Non sono presenti gli " +
                            "attributi {0} di Column", StringUtils.join(expectedValList, ", ")));
                }
                table.getColumns().add(col);


            }

            if (event == XMLStreamConstants.END_ELEMENT && lName.equals(COLUMN)) {
                columnOpen--;
            }
        }

        if (columnOpen != 0) {
            throw new Exception("TAG " + COLUMN + " non chiuso");
        }

        if (xmlStreamReader.hasNext()) {
            event = xmlStreamReader.next();
            lName = xmlStreamReader.getLocalName();
            if (event == XMLStreamConstants.END_ELEMENT && lName.equals(SCHEMAS)) {
            } else {
                throw new Exception("TAG " + COLUMNS + " non chiuso");
            }
        }
    }

    private void doPrimaryKey(XMLStreamReader xmlStreamReader, Table table) throws Exception {
        PrimaryKey pk = new PrimaryKey();
        int event = next(xmlStreamReader);
        String lName = xmlStreamReader.getLocalName();
        if (event == XMLStreamConstants.START_ELEMENT && lName.equals(PRIMARY_KEY)) {
            String attName;
            String attValue;
            attName = xmlStreamReader.getAttributeLocalName(0);
            attValue = xmlStreamReader.getAttributeValue(0);
            if (attName.equals("name")) {
                pk.setName(attValue);
            } else {
                throw new Exception("TAG Primary Key, ATTR " + PRIMARY_KEY + " non presente");
            }
            do {
                event = next(xmlStreamReader);
                xmlStreamReader.getLocalName();
                attValue = xmlStreamReader.getAttributeValue(0);
                pk.getColumns().add(getColumn(table, attValue));
                event = next(xmlStreamReader);
                lName = xmlStreamReader.getLocalName();
                if (event != XMLStreamConstants.END_ELEMENT && !lName.equals(COLUMN)) {
                    throw new Exception("TAG " + COLUMN + " in Primary Key non chiuso");
                }

            }
            while (lName.equals(COLUMN) && event == XMLStreamConstants.START_ELEMENT);
            table.setPrimaryKey(pk);
            event = next(xmlStreamReader);
            lName = xmlStreamReader.getLocalName();
            if (event != XMLStreamConstants.END_ELEMENT && !lName.equals(PRIMARY_KEY)) {
                throw new Exception("TAG Primary Key non chiuso");
            }
        }
    }

    private void doRelationship(XMLStreamReader xmlStreamReader, DataModel dm, Table table) throws Exception {
        int event;
        String lName;
        if (xmlStreamReader.hasNext()) {
            event = next(xmlStreamReader);
            lName = xmlStreamReader.getLocalName();
            if (event == XMLStreamConstants.START_ELEMENT && lName.equals(RELATIONSHIPS)) {
            } else {
                return;
            }
        }


        Relationship rel = new Relationship();
        if (xmlStreamReader.hasNext()) {
            event = next(xmlStreamReader);
            lName = xmlStreamReader.getLocalName();
            if (event == XMLStreamConstants.START_ELEMENT && lName.equals(RELATIONSHIP)) {
            } else {
                throw new Exception("Tag " + RELATIONSHIP + " missing");
            }
        }
        String schemaName=null, tableName=null;
        for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++) {
            String attName = xmlStreamReader.getAttributeLocalName(i);
            String attValue = xmlStreamReader.getAttributeValue(i);

            if (attName.equals("name")) {
                rel.setName(attValue);
                continue;
            }
            if (attName.equals("toSchema")) {
                schemaName = attValue;
                continue;
            }
            if (attName.equals("toTable")) {
                tableName = attValue;
                continue;
            }
            if (attName.equals("onUpdate")) {
                rel.setOnUpdate(attValue);
                continue;
            }
            if (attName.equals("onDelete")) {
                rel.setOnDelete(attValue);

            }

        }
        rel.setTable(getTable(dm, schemaName, tableName));
        if (xmlStreamReader.hasNext()) {
            event = next(xmlStreamReader);
            lName = xmlStreamReader.getLocalName();
            if (event == XMLStreamConstants.START_ELEMENT && lName.equals(REFERENCE)) {
                String fromCol = null, toCol = null;
                for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++) {
                    String attName = xmlStreamReader.getAttributeLocalName(i);
                    String attValue = xmlStreamReader.getAttributeValue(i);
                    if (attName.equals("fromColumn")) {
                        fromCol = attValue;
                        continue;
                    }
                    if (attName.equals("toColumn")) {
                        toCol = attValue;

                    }
                }

                Reference ref = new Reference(getColumn(table, fromCol), getColumn(rel.getTable(), toCol));
                rel.getReferences().add(ref);
                if (xmlStreamReader.hasNext()) {
                    event = next(xmlStreamReader);
                    lName = xmlStreamReader.getLocalName();
                    if (event == XMLStreamConstants.END_ELEMENT && lName.equals(REFERENCE)) {
                    } else {
                        throw new Exception("TAG Reference non chiuso");
                    }
                }
            }
        }

        if (xmlStreamReader.hasNext()) {
            event = next(xmlStreamReader);
            lName = xmlStreamReader.getLocalName();
            if (event == XMLStreamConstants.END_ELEMENT && lName.equals(RELATIONSHIP)) {
            } else {
                throw new Exception("TAG Relationship non chiuso");
            }
        }

        if (xmlStreamReader.hasNext()) {
            event = next(xmlStreamReader);
            lName = xmlStreamReader.getLocalName();

            if (event == XMLStreamConstants.END_ELEMENT && lName.equals(RELATIONSHIPS)) {
            } else {
                throw new Exception("TAG Relationships non chiuso");
            }
        }

    }

    private Table getTable(DataModel dm, String schemaName, String tableName) throws Exception {
        for (Database db : dm.getDatabases()) {
            for (Schema schema : db.getSchemas()) {
                if (schemaName.equals(schema.getSchemaName())){
                    for (Table tb : schema.getTables()) {
                        if(tableName.equals(tb.getTableName()))
                            return tb;
                    }
                }
            }

        }
        throw new Exception("Tabella non presente");

    }

    private Column getColumn(Table table, String attValue) throws Exception {
        for (Column col : table.getColumns()) {
            if (col.getColumnName().equals(attValue)) {
                return col;
            }
        }
        throw new Exception("Colonna non presente");
    }

    private int next(XMLStreamReader xmlStreamReader) throws Exception {
        if (xmlStreamReader.hasNext()) {
            int event = xmlStreamReader.next();
            if (event == 1 || event == 2) {
                return event;
            } else {
                return next(xmlStreamReader);
            }
        } else {
            throw new Exception("Fine inattesa");
        }
    }


    public static void main(String[] argv) {

        try {
            new DBParser().parse("jpetstore-postgres.xml");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
