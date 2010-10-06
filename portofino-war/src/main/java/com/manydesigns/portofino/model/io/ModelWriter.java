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

package com.manydesigns.portofino.model.io;

import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.datamodel.*;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/

public class ModelWriter {
    public final Model model;

    public ModelWriter(Model model) {
        this.model = model;
    }

    public String write(File file) throws IOException {
        if(file==null) {
            file = File.createTempFile("portofino-model", ".xml");
        }
        XMLOutputFactory f = XMLOutputFactory.newInstance();
        XMLStreamWriter w = null;

        try {

            //Istanzio il Writer a partire da un FileWiter
            w = f.createXMLStreamWriter(new FileWriter(file));
            //Inizio il documento XML
            w.writeStartDocument();
    
            //Ritorni a capo nel documento finale
            w.writeCharacters("\n");
           
            w.writeStartElement( "model");
            w.writeCharacters("\n");
            w.writeStartElement( "databases");
            for (Database database :model.getDatabases()){
                visit (w, database);
            }
            w.writeEndElement(); // databases
            w.writeEndElement(); // model
    
            // Chiudo il documento
            w.writeEndDocument();
        }catch (Exception e) {
            
            e.printStackTrace();
        } finally {
    
            //Concludo la scrittura
            try {
                w.close();
            } catch (XMLStreamException e) {
                //do nothing
            }
        }

        return file.getAbsolutePath();
    }

    private void visit(XMLStreamWriter w, Database database) throws XMLStreamException {
        w.writeStartElement( "database");
        w.writeAttribute("name", database.getDatabaseName());
        w.writeCharacters("\n");
        w.writeStartElement( "schemas");
        w.writeCharacters("\n");
        for (Schema schema : database.getSchemas()){
            visit (w, schema);
        }
        w.writeCharacters("\n");
        w.writeEndElement();//schemas
        w.writeCharacters("\n");
        w.writeEndElement();//database
    }

    private void visit(XMLStreamWriter w, Schema schema) throws XMLStreamException {
        w.writeStartElement( "schema");
        w.writeAttribute("name", schema.getSchemaName());
        w.writeCharacters("\n");
        w.writeStartElement( "tables");
        w.writeCharacters("\n");
        for (Table table : schema.getTables()){
            visit (w, table);
        }
        w.writeCharacters("\n");//tables
        w.writeEndElement();
        w.writeCharacters("\n");
        w.writeEndElement();//schema
    }

    private void visit(XMLStreamWriter w, Table table) throws XMLStreamException {
        w.writeStartElement( "table");
        w.writeAttribute("name", table.getTableName());
        w.writeCharacters("\n");
        w.writeStartElement( "columns");
        w.writeCharacters("\n");
        for (Column column : table.getColumns()){
            visit (w, column);
        }
        w.writeCharacters("\n");//columns
        w.writeEndElement();

        //PrimaryKey
        visit (w, table.getPrimaryKey());

        //Relationships
        visit (w, table.getForeignKeys());

        w.writeCharacters("\n");
        w.writeEndElement();//table
    }

    private void visit(XMLStreamWriter w, Column column) throws XMLStreamException {
        w.writeStartElement( "column");
        w.writeAttribute("name", column.getColumnName());
        w.writeAttribute("columnType", column.getColumnType());

        if (column.getJavaType()!=null) {
            w.writeAttribute("javaType", column.getJavaType().getCanonicalName());
        }
        w.writeAttribute("length", Integer.toString(column.getLength()));
        w.writeAttribute("scale", Integer.toString(column.getScale()));
        w.writeAttribute("nullable", Boolean.toString(column.isNullable()));
        w.writeEndElement();//column
        w.writeCharacters("\n");
    }



    private void visit(XMLStreamWriter w, PrimaryKey primaryKey)
            throws XMLStreamException {
        w.writeStartElement( "primarykey");
        w.writeAttribute("name", primaryKey.getPkName());
        w.writeCharacters("\n");

        for (PrimaryKeyColumn column : primaryKey.getPrimaryKeyColumns()){
            w.writeStartElement( "column");
            w.writeAttribute("name", column.getColumnName());
            w.writeEndElement();
            w.writeCharacters("\n");

        }
        w.writeEndElement();//primaryKey
        w.writeCharacters("\n");
    }


    private void visit(XMLStreamWriter w, List<ForeignKey> manyToOneRelationships)
            throws XMLStreamException {
        if (manyToOneRelationships.isEmpty())
            return;


        w.writeStartElement( "relationships");
        w.writeCharacters("\n");

        for(ForeignKey rel : manyToOneRelationships) {
            w.writeStartElement( "relationship");
            w.writeAttribute("name", rel.getFkName());
            w.writeAttribute("toTable", rel.getToTableName());
            w.writeAttribute("toSchema", rel.getToSchemaName());
            w.writeAttribute("onDelete", rel.getOnDelete());
            w.writeAttribute("onUpdate", rel.getOnUpdate());
            w.writeCharacters("\n");
            for (Reference ref : rel.getReferences()){
                w.writeStartElement( "reference");
                w.writeAttribute("fromColumn", ref.getFromColumnName());
                w.writeAttribute("toColumn", ref.getToColumnName());
                w.writeEndElement();//reference
                w.writeCharacters("\n");
            }
            w.writeEndElement();//relationship

        }


        w.writeEndElement();//relationships
        w.writeCharacters("\n");
    }


    public static void main(String[] args){
        try {
            ModelParser parser = new ModelParser();
             Model model = parser.parse(
                    "databases/jpetstore/postgresql/jpetstore-postgres.xml");
            ModelWriter writer = new ModelWriter(model);
            System.out.println(writer.write(null));
        } catch (Throwable e) {

        }
    }
}
