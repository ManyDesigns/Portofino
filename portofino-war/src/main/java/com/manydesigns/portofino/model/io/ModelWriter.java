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
import com.manydesigns.portofino.model.annotations.ModelAnnotation;
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
    protected XMLStreamWriter w = null;

    public ModelWriter(Model model) {
        this.model = model;
    }

    public String write(File file) throws IOException {
        XMLOutputFactory f = XMLOutputFactory.newInstance();

        try {
            //Istanzio il Writer a partire da un FileWiter
            w = new IndentingXMLStreamWriter(f.createXMLStreamWriter(new FileWriter(file)));
            //Inizio il documento XML
            w.writeStartDocument();
    
            w.writeStartElement(ModelParser.MODEL);
            w.writeStartElement(ModelParser.DATABASES);
            for (Database database :model.getDatabases()){
                visit (database);
            }
            w.writeEndElement(); // databases
            w.writeEndElement(); // model
    
            // Chiudo il documento
            w.writeEndDocument();
            w.flush();
        }catch (Exception e) {
            
            e.printStackTrace();
        } finally {

            if (w != null) {
                //Concludo la scrittura
                try {
                    w.close();
                } catch (XMLStreamException e) {
                    //do nothing
                }
            }
        }

        return file.getAbsolutePath();
    }

    private void visit(Database database) throws XMLStreamException {
        w.writeStartElement(ModelParser.DATABASE);
        w.writeAttribute(ModelParser.DATABASE_DATABASENAME, database.getDatabaseName());
        w.writeStartElement(ModelParser.SCHEMAS);
        for (Schema schema : database.getSchemas()){
            visit (schema);
        }
        w.writeEndElement();//schemas
        w.writeEndElement();//database
    }

    private void visit(Schema schema) throws XMLStreamException {
        w.writeStartElement(ModelParser.SCHEMA);
        w.writeAttribute(ModelParser.SCHEMA_SCHEMANAME, schema.getSchemaName());
        w.writeStartElement(ModelParser.TABLES);
        for (Table table : schema.getTables()){
            visit (table);
        }
        w.writeEndElement();
        w.writeEndElement();//schema
    }

    private void visit(Table table) throws XMLStreamException {
        w.writeStartElement(ModelParser.TABLE);
        w.writeAttribute(ModelParser.TABLE_TABLENAME, table.getTableName());
        writeOptionalAttribute(ModelParser.TABLE_JAVACLASS, table.getJavaClassName());
        w.writeAttribute(ModelParser.TABLE_MANYTOMANY, Boolean.toString(table.isM2m()));
        w.writeStartElement(ModelParser.COLUMNS);
        for (Column column : table.getColumns()){
            visit (column);
        }
        w.writeEndElement();

        //PrimaryKey
        visit (table.getPrimaryKey());

        //Relationships
        visit (table.getForeignKeys());

        // Annotations
        visitAnnotations(table.getModelAnnotations());

        w.writeEndElement(); //table
    }

    private void writeOptionalAttribute(String localName, String optionalValue)
            throws XMLStreamException {
        if (optionalValue != null) {
            w.writeAttribute(localName, optionalValue);
        }
    }

    private void visit(Column column) throws XMLStreamException {
        List<ModelAnnotation> modelAnnotations = column.getModelAnnotations();
        if (modelAnnotations.isEmpty()) {
            w.writeEmptyElement(ModelParser.COLUMN);
            writeColumnAttributes(column);
        } else {
            w.writeStartElement(ModelParser.COLUMN);
            writeColumnAttributes(column);
            visitAnnotations(modelAnnotations);
            w.writeEndElement(); //column
        }
    }

    private void writeColumnAttributes(Column column) throws XMLStreamException {
        w.writeAttribute(ModelParser.COLUMN_COLUMNNAME, column.getColumnName());
        w.writeAttribute(ModelParser.COLUMN_COLUMNTYPE, column.getColumnType());
        w.writeAttribute(ModelParser.COLUMN_LENGTH, Integer.toString(column.getLength()));
        w.writeAttribute(ModelParser.COLUMN_SCALE, Integer.toString(column.getScale()));
        w.writeAttribute(ModelParser.COLUMN_NULLABLE, Boolean.toString(column.isNullable()));
        w.writeAttribute(ModelParser.COLUMN_SEARCHABLE, Boolean.toString(column.isSearchable()));
        w.writeAttribute(ModelParser.COLUMN_AUTOINCREMENT, Boolean.toString(column.isAutoincrement()));
        writeOptionalAttribute(ModelParser.COLUMN_PROPERTYNAME, column.getPropertyName());
        writeOptionalAttribute(ModelParser.COLUMN_JAVATYPE, column.getJavaTypeName());
    }

    private void visitAnnotations(List<ModelAnnotation> modelAnnotations)
            throws XMLStreamException {
        if (!modelAnnotations.isEmpty()) {
            w.writeStartElement(ModelParser.ANNOTATIONS);
            for (ModelAnnotation modelAnnotation : modelAnnotations){
                visit (modelAnnotation);
            }
            w.writeEndElement(); // annotations
        }
    }

    private void visit(ModelAnnotation modelAnnotation) throws XMLStreamException {
        List<String> values= modelAnnotation.getValues();
        if (values.isEmpty()) {
            w.writeEmptyElement(ModelParser.ANNOTATION);
            w.writeAttribute(ModelParser.ANNOTATION_TYPE, modelAnnotation.getType());
        } else {
            w.writeStartElement(ModelParser.ANNOTATION);
            w.writeAttribute(ModelParser.ANNOTATION_TYPE, modelAnnotation.getType());
            for (String value : values) {
                if (value == null) {
                    w.writeEmptyElement(ModelParser.VALUE);                    
                } else {
                    w.writeStartElement(ModelParser.VALUE);
                    w.writeCharacters(value);
                    w.writeEndElement(); // value
                }
            }
            w.writeEndElement(); // annotation
        }
    }


    private void visit(PrimaryKey primaryKey)
            throws XMLStreamException {
        if (primaryKey == null) {
            return;
        }
        w.writeStartElement(ModelParser.PRIMARYKEY);
        w.writeAttribute(ModelParser.PRIMARYKEY_PRIMARYKEYNAME,
                primaryKey.getPrimaryKeyName());
        writeOptionalAttribute(ModelParser.PRIMARYKEY_CLASSNAME,
                primaryKey.getClassName());

        for (PrimaryKeyColumn column : primaryKey.getPrimaryKeyColumns()){
            w.writeEmptyElement(ModelParser.COLUMN);
            w.writeAttribute(ModelParser.COLUMN_COLUMNNAME, column.getColumnName());
        }
        w.writeEndElement();//primaryKey
    }


    private void visit(List<ForeignKey> foreignKeys)
            throws XMLStreamException {
        if (foreignKeys.isEmpty())
            return;

        w.writeStartElement(ModelParser.FOREIGNKEYS);

        for(ForeignKey rel : foreignKeys) {
            visit(rel);
        }


        w.writeEndElement(); // foreign keys
    }

    private void visit(ForeignKey rel) throws XMLStreamException {
        w.writeStartElement(ModelParser.FOREIGNKEY);
        w.writeAttribute(ModelParser.FOREIGNKEY_FOREIGNKEYNAME, rel.getForeignKeyName());
        w.writeAttribute(ModelParser.FOREIGNKEY_TODATABASE, rel.getToDatabaseName());
        w.writeAttribute(ModelParser.FOREIGNKEY_TOSCHEMA, rel.getToSchemaName());
        w.writeAttribute(ModelParser.FOREIGNKEY_TOTABLE, rel.getToTableName());
        w.writeAttribute(ModelParser.FOREIGNKEY_ONDELETE, rel.getOnDelete());
        w.writeAttribute(ModelParser.FOREIGNKEY_ONUPDATE, rel.getOnUpdate());
        w.writeStartElement(ModelParser.REFERENCES);
        for (Reference ref : rel.getReferences()){
            w.writeEmptyElement(ModelParser.REFERENCE);
            w.writeAttribute(ModelParser.REFERENCE_FROMCOLUMN, ref.getFromColumnName());
            w.writeAttribute(ModelParser.REFERENCE_TOCOLUMN, ref.getToColumnName());
        }
        w.writeEndElement(); // references
        // Annotations
        visitAnnotations(rel.getModelAnnotations());
        w.writeEndElement(); // foreign key
    }
}
