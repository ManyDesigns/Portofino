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

import com.manydesigns.elements.logging.LogUtil;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.JavaClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.util.Util;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.annotations.ModelAnnotation;
import com.manydesigns.portofino.model.datamodel.*;
import com.manydesigns.portofino.xml.XmlAttribute;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.logging.Logger;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/

public class ModelWriter {
    public final Model model;
    protected XMLStreamWriter w = null;

    public static final Logger logger =
            LogUtil.getLogger(ModelWriter.class);


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

            // databases
            w.writeStartElement(ModelParser.DATABASES);
            for (Database database :model.getDatabases()){
                visit (database);
            }
            w.writeEndElement(); // databases

            logger.info("Model writer: TODO: site nodes");
            logger.info("Model writer: TODO: portlets");
            logger.info("Model writer: TODO: use cases");

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

    public void writeAttributes(Object object) throws XMLStreamException {
        Class javaClass = object.getClass();
        ClassAccessor classAccessor =
                JavaClassAccessor.getClassAccessor(javaClass);
        for (PropertyAccessor propertyAccessor : classAccessor.getProperties()) {
            XmlAttribute xmlAttribute =
                    propertyAccessor.getAnnotation(XmlAttribute.class);
            if (xmlAttribute == null) {
                continue;
            }

            String name = propertyAccessor.getName();

            Object value = null;
            try {
                value = propertyAccessor.get(object);
            } catch (Throwable e) {
                LogUtil.warningMF(logger,
                        "Cannot get attribute/property ''{0}''", name);
            }

            String stringValue = Util.convertValueToString(value);

            if (value == null) {
                if (xmlAttribute.required()) {
                    throw new Error(MessageFormat.format(
                            "Attribute ''{0}'' required", name));
                }
            } else {
                w.writeAttribute(name, stringValue);
            }
        }

    }

    private void visit(Database database) throws XMLStreamException {
        w.writeStartElement(ModelParser.DATABASE);
        writeAttributes(database);
        w.writeStartElement(ModelParser.SCHEMAS);
        for (Schema schema : database.getSchemas()){
            visit (schema);
        }
        w.writeEndElement();//schemas
        w.writeEndElement();//database
    }

    private void visit(Schema schema) throws XMLStreamException {
        w.writeStartElement(ModelParser.SCHEMA);
        writeAttributes(schema);
        w.writeStartElement(ModelParser.TABLES);
        for (Table table : schema.getTables()){
            visit (table);
        }
        w.writeEndElement();
        w.writeEndElement();//schema
    }

    private void visit(Table table) throws XMLStreamException {
        w.writeStartElement(ModelParser.TABLE);
        writeAttributes(table);
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

    private void visit(Column column) throws XMLStreamException {
        List<ModelAnnotation> modelAnnotations = column.getModelAnnotations();
        if (modelAnnotations.isEmpty()) {
            w.writeEmptyElement(ModelParser.COLUMN);
            writeAttributes(column);
        } else {
            w.writeStartElement(ModelParser.COLUMN);
            writeAttributes(column);
            visitAnnotations(modelAnnotations);
            w.writeEndElement(); //column
        }
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
            writeAttributes(modelAnnotation);
        } else {
            w.writeStartElement(ModelParser.ANNOTATION);
            writeAttributes(modelAnnotation);
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
        writeAttributes(primaryKey);

        for (PrimaryKeyColumn column : primaryKey.getPrimaryKeyColumns()){
            w.writeEmptyElement(ModelParser.COLUMN);
            writeAttributes(column);
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

    private void visit(ForeignKey foreignKey) throws XMLStreamException {
        w.writeStartElement(ModelParser.FOREIGNKEY);
        writeAttributes(foreignKey);
        w.writeStartElement(ModelParser.REFERENCES);
        for (Reference reference : foreignKey.getReferences()){
            w.writeEmptyElement(ModelParser.REFERENCE);
            writeAttributes(reference);
        }
        w.writeEndElement(); // references
        // Annotations
        visitAnnotations(foreignKey.getModelAnnotations());
        w.writeEndElement(); // foreign key
    }
}
