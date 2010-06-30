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
package com.manydesigns.portofino.base.model; /**
 * Created by IntelliJ IDEA.
 * User: giampi
 * Date: Jun 30, 2010
 * Time: 2:01:24 PM
 * To change this template use File | Settings | File Templates.
 */

/**
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Angelo    Lupo       - angelo.lupo@manydesigns.com
 * @author Paolo     Predonzani - paolo.predonzani@manydesigns.com
 */

import javax.xml.stream.*;
import javax.xml.stream.XMLInputFactory;
import java.io.*;


public class DBParser {
    public DataModel parse(String fileName) {
        DataModel dataModel = new DataModel();
        try {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            InputStream input = DBParser.class.getResourceAsStream(fileName);
            XMLStreamReader xmlStreamReader = inputFactory.createXMLStreamReader(input);

            Database currentDataBase = null;
            Schema currentSchema = null;
            Table currentTable = null;
            Column currentColumn;

            while (xmlStreamReader.hasNext()) {
                int event = xmlStreamReader.next();

                if (event == XMLStreamConstants.START_DOCUMENT) {
                    System.out.println("Event Type:START_DOCUMENT");
                    System.out.println("Document Encoding:" + xmlStreamReader.getEncoding());
                    System.out.println("XML Version:" + xmlStreamReader.getVersion());
                }
                if (event == XMLStreamConstants.START_ELEMENT) {
                    final String lName = xmlStreamReader.getLocalName();

                    System.out.println("-Event Type: START_ELEMENT");
                    System.out.println("-Element Prefix:" + xmlStreamReader.getPrefix());
                    System.out.println("-Element Local Name:" + lName);
                    System.out.println("-Namespace URI:" + xmlStreamReader.getNamespaceURI());

                    if (lName.equalsIgnoreCase("database")) {
                        currentDataBase = manageDb(dataModel, xmlStreamReader);
                        continue;
                    }

                    if (lName.equalsIgnoreCase("connection")) {
                        currentDataBase.setConnection(manageConnection(xmlStreamReader));
                        continue;
                    }

                    if (lName.equalsIgnoreCase("schema")) {
                        currentSchema = manageSchema(currentDataBase, xmlStreamReader);
                        continue;
                    }

                    if (lName.equalsIgnoreCase("table")) {
                        currentTable = manageTable(currentSchema, xmlStreamReader);
                        continue;
                    }

                    if (lName.equalsIgnoreCase("column")) {
                        currentColumn = manageColumn(currentTable, xmlStreamReader);
                        continue;
                    }
                }

                if (event == XMLStreamConstants.END_ELEMENT) {
                    System.out.println("-Event Type: END_ELEMENT");
                }


            }
            int i=0;
        } catch (FactoryConfigurationError

                e) {
            System.out.println("FactoryConfigurationError" + e.getMessage());
        }
        catch (XMLStreamException e) {
            System.out.println("XMLStreamException" + e.getMessage());
        }

        return dataModel;

    }

    private Connection manageConnection(XMLStreamReader xmlStreamReader) {
        Connection conn = new Connection();
        String attName;
        String attValue;
        for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++) {
            attName = xmlStreamReader.getAttributeLocalName(i);
            attValue = xmlStreamReader.getAttributeValue(i);

            if (attName.equalsIgnoreCase("type")) {
                //conn.setType(attValue);
                continue;
            }
            if (attName.equalsIgnoreCase("driver")) {
                //conn.setDriver(attValue);
                continue;
            }
            if (attName.equalsIgnoreCase("url")) {
                //conn.setUrl(attValue);
                continue;
            }
            if (attName.equalsIgnoreCase("username")) {
                //conn.setUsername(attValue);
                continue;
            }
            if (attName.equals("password")) {
                //conn.setPassword(attValue);
                continue;
            }
        }

        return conn;
    }

    private Table manageTable(Schema schema, XMLStreamReader xmlStreamReader) {
        String attName;
        String attValue;
        Table table = new Table();
        table.setSchemaName(schema.getSchemaName());
        for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++) {
            attName = xmlStreamReader.getAttributeLocalName(i);
            attValue = xmlStreamReader.getAttributeValue(i);

            if (attName.equals("name")) {
                table.setTableName(attValue);
                continue;
            }

        }

        schema.getTables().add(table);
        return table;
    }

    private Column manageColumn(Table table, XMLStreamReader xmlStreamReader) {
        Column col = new Column();
        col.setTableName(table.getTableName());
        col.setSchemaName(table.getSchemaName());

        String attName;
        String attValue;

        for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++) {
            attName = xmlStreamReader.getAttributeLocalName(i);
            attValue = xmlStreamReader.getAttributeValue(i);

            if (attName.equals("name")) {
                col.setTableName(attValue);
                continue;
            }
            if (attName.equals("columnType")) {
                col.setColumnType(attValue);
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

        table.getColumns().add(col);
        return col;
    }

    private Schema manageSchema(Database db, XMLStreamReader xmlStreamReader) {
        Schema schema = new Schema();
        String attName;
        String attValue;
        for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++) {
            attName = xmlStreamReader.getAttributeLocalName(i);
            attValue = xmlStreamReader.getAttributeValue(i);

            if (attName.equals("name")) {
                schema.setSchemaName(attValue);
            }
        }
        db.getSchemas().add(schema);
        return schema;
    }

    private Database manageDb(DataModel dataModel, XMLStreamReader xmlStreamReader) {
        Database db = new Database();
        String attName;
        String attValue;
        for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++) {
            attName = xmlStreamReader.getAttributeLocalName(i);
            attValue = xmlStreamReader.getAttributeValue(i);

            if (attName.equals("name")) {
                db.setName(attValue);
            }
        }
        dataModel.getDatabases().add(db);
        return db;
    }

    public static void main(String[] argv) {

        new DBParser().parse("jpetstore-postgres.xml");

    }
}
