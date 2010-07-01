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

/**
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Angelo    Lupo       - angelo.lupo@manydesigns.com
 * @author Paolo     Predonzani - paolo.predonzani@manydesigns.com
 */

import org.apache.commons.lang.StringUtils;

import javax.xml.stream.*;
import javax.xml.stream.XMLInputFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.text.MessageFormat;


public class DBParser {


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
        String lName = null;
        while (xmlStreamReader.hasNext()) {
            if (event == XMLStreamConstants.END_ELEMENT
                    && xmlStreamReader.getLocalName().equals("datamodel")) {
                break;
            }
            event = next(xmlStreamReader);

            if (event == XMLStreamConstants.START_ELEMENT
                    && xmlStreamReader.getLocalName().equals("datamodel")) {
                doDataModel(xmlStreamReader, dataModel);
            }
        }
        int i=0;
    }

    private void doDataModel(XMLStreamReader xmlStreamReader, DataModel dataModel) throws Exception {
        String lName = xmlStreamReader.getLocalName();
        doDataBase(xmlStreamReader, dataModel);
    }

    private void doDataBase(XMLStreamReader xmlStreamReader, DataModel dataModel) throws Exception {
        int event=-1;
        String lName=null;
        int dbopen = 0;
        boolean dbpresent = false;


        while (xmlStreamReader.hasNext()) {
            if (event == XMLStreamConstants.END_ELEMENT && "database".equals(lName)) {
                break;
            }
            event = next(xmlStreamReader);
            lName = xmlStreamReader.getLocalName();
            if (event == XMLStreamConstants.START_ELEMENT && lName.equals("database")) {
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
                    throw new Exception("TAG database, attr name non presente");
                }
                dataModel.getDatabases().add(db);
                doConnection(xmlStreamReader, db);
                doSchemas(xmlStreamReader, db);
            }

            if (event == XMLStreamConstants.END_ELEMENT && lName.equals("database")) {
                dbopen--;
            }
        }
        if (!dbpresent) {
            throw new Exception("TAG database non presente");
        }
        if (dbopen != 0) {
            throw new Exception("TAG database non chiuso");
        }

    }

    private void doConnection(XMLStreamReader xmlStreamReader, Database db) throws Exception {
        Connection conn = new Connection();
        int event = next(xmlStreamReader);
        String lName = xmlStreamReader.getLocalName();
        if (event == XMLStreamConstants.START_ELEMENT && lName.equals("connection")) {
            String attName;
            String attValue;
            List<String> expectedValList = new ArrayList<String>();
            expectedValList.add("type");
            expectedValList.add("driver");
            expectedValList.add("url");
            expectedValList.add("username");
            expectedValList.add("password");
            for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++) {
                attName = xmlStreamReader.getAttributeLocalName(i);
                attValue = xmlStreamReader.getAttributeValue(i);

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
                    continue;
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
                if (event == XMLStreamConstants.END_ELEMENT && lName.equals("connection")) {
                } else {
                    throw new Exception("TAG connection non chiuso");
                }
            }
        } else {
            throw new Exception("TAG Connection non presente");
        }
    }

    private void doSchemas(XMLStreamReader xmlStreamReader, Database db) throws Exception {
        int event=-1;
        String lName=null;
        int schemaOpen = 0;
        boolean schemaPresent = false;
        if (xmlStreamReader.hasNext()) {
            event = next(xmlStreamReader);
            lName = xmlStreamReader.getLocalName();
            if (event == XMLStreamConstants.START_ELEMENT && lName.equals("schemas")) {
            } else {
                throw new Exception("TAG schemas non presente");
            }
        }
        while (xmlStreamReader.hasNext()) {
            if (event == XMLStreamConstants.END_ELEMENT && "schema".equals(lName)) {
                schemaOpen--;
                break;
            }
            event = next(xmlStreamReader);
            lName = xmlStreamReader.getLocalName();
            if (event == XMLStreamConstants.START_ELEMENT && lName.equals("schema")) {
                schemaPresent = true;
                schemaOpen++;
                Schema schema = new Schema();
                String attName = xmlStreamReader.getAttributeLocalName(0);
                String attValue = xmlStreamReader.getAttributeValue(0);

                if (attName.equals("name")) {
                    schema.setSchemaName(attValue);
                } else {
                    throw new Exception("TAG schema, ATTR Name non presente");
                }
                db.getSchemas().add(schema);
                doTables(xmlStreamReader, schema);
            }


        }
        if (!schemaPresent) {
            throw new Exception("TAG schema non presente");
        }
        if (schemaOpen != 0) {
            throw new Exception("TAG schema non chiuso");
        }

        if (xmlStreamReader.hasNext()) {
            event = next(xmlStreamReader);
            lName = xmlStreamReader.getLocalName();
            if (event == XMLStreamConstants.END_ELEMENT && lName.equals("schemas")) {
            } else {
                throw new Exception("TAG schemas non chiuso");
            }
        }
    }

    private void doTables(XMLStreamReader xmlStreamReader, Schema schema) throws Exception {
        int event;
        String lName;
        int tableOpen = 0;
        boolean tablePresent = false;
        if (xmlStreamReader.hasNext()) {
            event = next(xmlStreamReader);
            lName = xmlStreamReader.getLocalName();
            if (event == XMLStreamConstants.START_ELEMENT && lName.equals("tables")) {
            } else {
                throw new Exception("TAG tables non presente");
            }
        }
        while (xmlStreamReader.hasNext()) {
            event = next(xmlStreamReader);
            lName = xmlStreamReader.getLocalName();
            if (event == XMLStreamConstants.END_ELEMENT && lName.equals("tables")) {
                //todo controllo su table open
                return;
            }
            if (event == XMLStreamConstants.END_ELEMENT && lName.equals("table")) {
                tableOpen--;
                event = next(xmlStreamReader);
                lName = xmlStreamReader.getLocalName();
                break;
            }
            if (event == XMLStreamConstants.START_ELEMENT && lName.equals("table")) {
                tablePresent = true;
                tableOpen++;
                Table table = new Table();
                table.setSchemaName(schema.getSchemaName());
                String attName = xmlStreamReader.getAttributeLocalName(0);
                String attValue = xmlStreamReader.getAttributeValue(0);

                if (attName.equals("name")) {
                    table.setTableName(attValue);
                } else {
                    throw new Exception("TAG schema, ATTR Name non presente");
                }
                schema.getTables().add(table);
                doColumns(xmlStreamReader, table);
                doPrimaryKey(xmlStreamReader, table);
                doRelationship(xmlStreamReader, table);
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
            if (event == XMLStreamConstants.START_ELEMENT && lName.equals("columns")) {
            } else {
                throw new Exception("TAG columns non presente");
            }
        }
        while (xmlStreamReader.hasNext()) {
            event = next(xmlStreamReader);
            lName = xmlStreamReader.getLocalName();
            if (event == XMLStreamConstants.END_ELEMENT && lName.equals("columns")) {
                return;
            }
            if (event == XMLStreamConstants.START_ELEMENT && lName.equals("column")) {
                columnOpen++;
                Column col = new Column();
                col.setTableName(table.getTableName());
                col.setSchemaName(table.getSchemaName());
                List<String> expectedValList = new ArrayList<String>();
                expectedValList.add("name");
                expectedValList.add("columnType");
                for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++) {
                    String attName = xmlStreamReader.getAttributeLocalName(i);
                    String attValue = xmlStreamReader.getAttributeValue(i);

                    if (attName.equals("name")) {
                        col.setColumnName(attValue);
                        expectedValList.remove(attName);
                        continue;
                    }
                    if (attName.equals("columnType")) {
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
                    if (attName.equals("precision")) {
                        col.setPrecision(Integer.parseInt(attValue));
                        continue;
                    }
                    if (attName.equals("scale")) {
                        col.setScale(Integer.parseInt(attValue));
                        continue;
                    }
                }

                if (expectedValList.size() != 0) {

                    throw new Exception(MessageFormat.format("Non sono presenti gli " +
                            "attributi {0} di Column", StringUtils.join(expectedValList, ", ")));
                }
                table.getColumns().add(col);


            }

            if (event == XMLStreamConstants.END_ELEMENT && lName.equals("column")) {
                columnOpen--;
            }
        }

        if (columnOpen != 0) {
            throw new Exception("TAG column non chiuso");
        }

        if (xmlStreamReader.hasNext()) {
            event = xmlStreamReader.next();
            lName = xmlStreamReader.getLocalName();
            if (event == XMLStreamConstants.END_ELEMENT && lName.equals("schemas")) {
            } else {
                throw new Exception("TAG columns non chiuso");
            }
        }
    }

    private void doPrimaryKey(XMLStreamReader xmlStreamReader, Table table) throws Exception {
        PrimaryKey pk = new PrimaryKey();
        int event = next(xmlStreamReader);
        String lName = xmlStreamReader.getLocalName();
        if (event == XMLStreamConstants.START_ELEMENT && lName.equals("primaryKey")) {
            String attName;
            String attValue;
            attName = xmlStreamReader.getAttributeLocalName(0);
            attValue = xmlStreamReader.getAttributeValue(0);
            if (attName.equals("name")) {
                pk.setName(attValue);
            } else {
                throw new Exception("TAG Primary Key, ATTR primaryKey non presente");
            }
            do {
                event = next(xmlStreamReader);
                lName = xmlStreamReader.getLocalName();
                attName = xmlStreamReader.getAttributeLocalName(0);
                attValue = xmlStreamReader.getAttributeValue(0);
                pk.getColumns().add(getColumn(table, attValue));
                event = next(xmlStreamReader);
                lName = xmlStreamReader.getLocalName();
                if (event != XMLStreamConstants.END_ELEMENT && !lName.equals("column")) {
                    throw new Exception("TAG column in Primary Key non chiuso");
                }

            }
            while (lName.equals("column") && event == XMLStreamConstants.START_ELEMENT);
            table.setPrimaryKey(pk);
            event = next(xmlStreamReader);
            lName = xmlStreamReader.getLocalName();
            if (event != XMLStreamConstants.END_ELEMENT && !lName.equals("primaryKey")) {
                throw new Exception("TAG Primary Key non chiuso");
            }
        }
    }

    private void doRelationship(XMLStreamReader xmlStreamReader, Table table) throws Exception {
        PrimaryKey pk = new PrimaryKey();
        int event;
        String lName;
        if (xmlStreamReader.hasNext()) {
            event = next(xmlStreamReader);
            lName = xmlStreamReader.getLocalName();
            if (event == XMLStreamConstants.START_ELEMENT && lName.equals("relationships")) {
            } else {
                return;
            }
        }


        Relationship rel = new Relationship();
        if (xmlStreamReader.hasNext()) {
            event = next(xmlStreamReader);
            lName = xmlStreamReader.getLocalName();
            if (event == XMLStreamConstants.START_ELEMENT && lName.equals("relationship")) {
            } else {
                throw new Exception("Tag relationship missing");
            }
        }
        for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++) {
            String attName = xmlStreamReader.getAttributeLocalName(i);
            String attValue = xmlStreamReader.getAttributeValue(i);
            if (attName.equals("name")) {
                rel.setTableName(attValue);
                continue;
            }
            if (attName.equals("toSchema")) {
                rel.setSchemaName(attValue);
                continue;
            }
            if (attName.equals("toTable")) {
                rel.setTableName(attValue);
                continue;
            }
            if (attName.equals("onUpdate")) {
                rel.setOnUpdate(attValue);
                continue;
            }
            if (attName.equals("onDelete")) {
                rel.setOnDelete(attValue);
                continue;
            }

        }

        if (xmlStreamReader.hasNext()) {
            event = next(xmlStreamReader);
            lName = xmlStreamReader.getLocalName();
            if (event == XMLStreamConstants.START_ELEMENT && lName.equals("reference")) {
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
                        continue;
                    }
                }

                Reference ref = new Reference(getColumn(table, fromCol), getColumn(table, toCol));
                rel.getReferences().add(ref);
                if (xmlStreamReader.hasNext()) {
                    event = next(xmlStreamReader);
                    lName = xmlStreamReader.getLocalName();
                    if (event == XMLStreamConstants.END_ELEMENT && lName.equals("reference")) {
                    } else {
                        throw new Exception("TAG Reference non chiuso");
                    }
                }
            }
        }

        if (xmlStreamReader.hasNext()) {
            event = next(xmlStreamReader);
            lName = xmlStreamReader.getLocalName();
            if (event == XMLStreamConstants.END_ELEMENT && lName.equals("relationship")) {
            } else {
                throw new Exception("TAG Relationship non chiuso");
            }
        }

        if (xmlStreamReader.hasNext()) {
            event = next(xmlStreamReader);
            lName = xmlStreamReader.getLocalName();

            if (event == XMLStreamConstants.END_ELEMENT && lName.equals("relationships")) {
            } else {
                throw new Exception("TAG Relationships non chiuso");
            }
        }

    }

    private Column getColumn(Table table, String attValue) throws Exception {
        for (Column col : table.getColumns()) {
            if (col.getColumnName().equals(attValue)) ;
            return col;
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
