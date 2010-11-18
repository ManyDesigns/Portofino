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
import com.manydesigns.portofino.model.selectionproviders.ModelSelectionProvider;
import com.manydesigns.portofino.model.selectionproviders.SelectionProperty;
import com.manydesigns.portofino.model.site.*;
import com.manydesigns.portofino.model.site.usecases.Button;
import com.manydesigns.portofino.model.site.usecases.UseCase;
import com.manydesigns.portofino.model.site.usecases.UseCaseProperty;
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
    protected XMLStreamWriter w = null;

    public static final Logger logger =
            LogUtil.getLogger(ModelWriter.class);


    public String write(Model model, File file) throws IOException {
        XMLOutputFactory f = XMLOutputFactory.newInstance();

        try {
            //Istanzio il Writer a partire da un FileWiter
            XMLStreamWriter xmlStreamWriter =
                    f.createXMLStreamWriter(new FileWriter(file));
            w = new IndentingXMLStreamWriter(xmlStreamWriter);
            //Inizio il documento XML
            w.writeStartDocument();

            writeModel(model);
    
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
        for (PropertyAccessor propertyAccessor
                : classAccessor.getProperties()) {
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

    //--------------------------------------------------------------------------
    // Scrittura modello
    //--------------------------------------------------------------------------

    private void writeModel(Model model) throws XMLStreamException {
        w.writeStartElement(ModelParser.MODEL);

        // databases
        w.writeStartElement(ModelParser.DATABASES);
        for (Database database : model.getDatabases()){
            writeDatabase(database);
        }
        w.writeEndElement(); // databases

        SiteNode rootNode = model.getRoot();
        if (rootNode != null) {
            writeSiteNode(rootNode);
        }

        w.writeEndElement(); // model
    }

    //--------------------------------------------------------------------------
    // Databases/schemas/tables/columns/...
    //--------------------------------------------------------------------------

    private void writeDatabase(Database database) throws XMLStreamException {
        w.writeStartElement(ModelParser.DATABASE);
        writeAttributes(database);
        w.writeStartElement(ModelParser.SCHEMAS);
        for (Schema schema : database.getSchemas()){
            writeSchema(schema);
        }
        w.writeEndElement();//schemas
        w.writeEndElement();//database
    }

    private void writeSchema(Schema schema) throws XMLStreamException {
        w.writeStartElement(ModelParser.SCHEMA);
        writeAttributes(schema);
        w.writeStartElement(ModelParser.TABLES);
        for (Table table : schema.getTables()){
            writeTable(table);
        }
        w.writeEndElement();
        w.writeEndElement();//schema
    }

    private void writeTable(Table table) throws XMLStreamException {
        w.writeStartElement(ModelParser.TABLE);
        writeAttributes(table);
        w.writeStartElement(ModelParser.COLUMNS);
        for (Column column : table.getColumns()){
            writeColumn(column);
        }
        w.writeEndElement();

        // PrimaryKey
        writePrimaryKey(table.getPrimaryKey());

        // Foreign keys
        if (!table.getForeignKeys().isEmpty()) {
            w.writeStartElement(ModelParser.FOREIGNKEYS);

            for(ForeignKey rel : table.getForeignKeys()) {
                writeForeignKey(rel);
            }

            w.writeEndElement(); // foreign keys
        }

        // Annotations
        writeModelAnnotations(table.getModelAnnotations());

        w.writeEndElement(); //table
    }

    private void writeColumn(Column column) throws XMLStreamException {
        List<ModelAnnotation> modelAnnotations = column.getModelAnnotations();
        if (modelAnnotations.isEmpty()) {
            w.writeEmptyElement(ModelParser.COLUMN);
            writeAttributes(column);
        } else {
            w.writeStartElement(ModelParser.COLUMN);
            writeAttributes(column);
            writeModelAnnotations(modelAnnotations);
            w.writeEndElement(); //column
        }
    }

    private void writeModelAnnotations(List<ModelAnnotation> modelAnnotations)
            throws XMLStreamException {
        if (!modelAnnotations.isEmpty()) {
            w.writeStartElement(ModelParser.ANNOTATIONS);
            for (ModelAnnotation modelAnnotation : modelAnnotations){
                writeModelAnnotation(modelAnnotation);
            }
            w.writeEndElement(); // annotations
        }
    }

    private void writeModelAnnotation(ModelAnnotation modelAnnotation)
            throws XMLStreamException {
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


    private void writePrimaryKey(PrimaryKey primaryKey)
            throws XMLStreamException {
        if (primaryKey == null) {
            return;
        }
        w.writeStartElement(ModelParser.PRIMARYKEY);
        writeAttributes(primaryKey);

        for (PrimaryKeyColumn primaryKeyColumn
                : primaryKey.getPrimaryKeyColumns()) {
            writePrimaryKeyColumn(primaryKeyColumn);
        }
        w.writeEndElement();//primaryKey
    }

    private void writePrimaryKeyColumn(PrimaryKeyColumn primaryKeyColumn)
            throws XMLStreamException {
        w.writeEmptyElement(ModelParser.COLUMN);
        writeAttributes(primaryKeyColumn);
    }


    private void writeForeignKey(ForeignKey foreignKey)
            throws XMLStreamException {
        w.writeStartElement(ModelParser.FOREIGNKEY);
        writeAttributes(foreignKey);
        w.writeStartElement(ModelParser.REFERENCES);
        for (Reference reference : foreignKey.getReferences()) {
            writeReference(reference);
        }
        w.writeEndElement(); // references

        // Annotations
        writeModelAnnotations(foreignKey.getModelAnnotations());
        w.writeEndElement(); // foreign key
    }

    private void writeReference(Reference reference)
            throws XMLStreamException {
        w.writeEmptyElement(ModelParser.REFERENCE);
        writeAttributes(reference);
    }

    //--------------------------------------------------------------------------
    // RootNode/Folder/DocumentNode/CustomNode/CustomFolder/UseCaseNode/PortletNode
    //--------------------------------------------------------------------------

    private void writeSiteNode(SiteNode node) throws XMLStreamException {
        if (node instanceof RootNode) {
            writeRootNode((RootNode)node);
        } else if (node instanceof FolderNode) {
            writeFolderNode((FolderNode)node);
        } else if (node instanceof DocumentNode) {
            writeDocumentNode((DocumentNode)node);
        } else if (node instanceof CustomNode) {
            writeCustomNode((CustomNode)node);
        } else if (node instanceof CustomFolderNode) {
            writeCustomFolderNode((CustomFolderNode)node);
        } else if (node instanceof UseCaseNode) {
            writeUseCaseNode((UseCaseNode)node);
        } else if (node instanceof PortletNode) {
            writePortletNode((PortletNode)node);
        } else {
            throw new Error("Unknown node type: " + node.getClass());
        }
    }

    private void writeRootNode(RootNode node) throws XMLStreamException {
        simpleWriteNode(node, ModelParser.ROOTNODE);
    }

    private void writeFolderNode(FolderNode node) throws XMLStreamException {
        simpleWriteNode(node, ModelParser.FOLDERNODE);
    }

    private void writeDocumentNode(DocumentNode node) throws XMLStreamException {
        simpleWriteNode(node, ModelParser.DOCUMENTNODE);
    }

    private void writeCustomNode(CustomNode node) throws XMLStreamException {
        simpleWriteNode(node, ModelParser.CUSTOMNODE);
    }

    private void writeCustomFolderNode(CustomFolderNode node) throws XMLStreamException {
        simpleWriteNode(node, ModelParser.CUSTOMFOLDERNODE);
    }

    private void writeUseCaseNode(UseCaseNode node) throws XMLStreamException {
        w.writeStartElement(ModelParser.USECASENODE);
        writeAttributes(node);
        writeUseCase(node.getUseCase());
        writeChildNodes(node);
        writePermissions(node);
        w.writeEndElement(); // useCaseNode
    }

    private void writeUseCase(UseCase useCase) throws XMLStreamException {
        w.writeStartElement(ModelParser.USECASE);
        writeAttributes(useCase);

        if (!useCase.getProperties().isEmpty()) {
            w.writeStartElement(ModelParser.PROPERTIES);
            for (UseCaseProperty property : useCase.getProperties()) {
                writeUseCaseProperty(property);
            }
            w.writeEndElement(); // properties
        }

        if (!useCase.getModelSelectionProviders().isEmpty()) {
            w.writeStartElement(ModelParser.SELECTIONPROVIDERS);
            for (ModelSelectionProvider modelSelectionProvider
                    : useCase.getModelSelectionProviders()) {
                writeModelSelectionProvider(modelSelectionProvider);
            }
            w.writeEndElement(); // selectionProviders
        }

        writeModelAnnotations(useCase.getModelAnnotations());

        if (!useCase.getButtons().isEmpty()) {
            w.writeStartElement(ModelParser.BUTTONS);
            for (Button button : useCase.getButtons()) {
                writeButton(button);
            }
            w.writeEndElement(); // buttons
        }

        if (!useCase.getSubUseCases().isEmpty()) {
            w.writeStartElement(ModelParser.SUBUSECASES);
            for (UseCase subUseCase : useCase.getSubUseCases()) {
                writeUseCase(subUseCase);
            }
            w.writeEndElement(); // subUseCases
        }

        w.writeEndElement(); // useCase
    }

    private void writeButton(Button button) throws XMLStreamException {
        w.writeEmptyElement(ModelParser.BUTTON);
        writeAttributes(button);
    }

    private void writeModelSelectionProvider(ModelSelectionProvider modelSelectionProvider)
            throws XMLStreamException {
        w.writeStartElement(ModelParser.SELECTIONPROVIDER);
        writeAttributes(modelSelectionProvider);
        if (!modelSelectionProvider.getSelectionProperties().isEmpty()) {
            w.writeStartElement(ModelParser.SELECTIONPROPERTIES);
            for (SelectionProperty selectionProperty
                    : modelSelectionProvider.getSelectionProperties()) {
                writeSelectionProperty(selectionProperty);
            }
            w.writeEndElement(); // selectionProperties
        }
        w.writeEndElement(); // selectionProvider
    }

    private void writeSelectionProperty(SelectionProperty selectionProperty)
            throws XMLStreamException {
        w.writeEmptyElement(ModelParser.SELECTIONPROPERTY);
        writeAttributes(selectionProperty);
    }

    private void writeUseCaseProperty(UseCaseProperty property)
            throws XMLStreamException {
        if (property.getAnnotations().isEmpty()) {
            w.writeEmptyElement(ModelParser.PROPERTY);
            writeAttributes(property);
        } else {
            w.writeStartElement(ModelParser.PROPERTY);
            writeAttributes(property);

            writeModelAnnotations(property.getAnnotations());

            w.writeEndElement();
        }
    }

    private void writePortletNode(PortletNode node) throws XMLStreamException {
        simpleWriteNode(node, ModelParser.PORTLETNODE);
    }

    private void simpleWriteNode(SiteNode node, String elementType)
            throws XMLStreamException {
        if (node.getChildNodes().isEmpty()
                && node.getAllowGroups().isEmpty()
                && node.getDenyGroups().isEmpty()) {
            w.writeEmptyElement(elementType);
            writeAttributes(node);
        } else {
            w.writeStartElement(elementType);
            writeAttributes(node);
            writeChildNodes(node);
            writePermissions(node);
            w.writeEndElement();
        }
    }

    private void writeChildNodes(SiteNode node) throws XMLStreamException {
        if (!node.getChildNodes().isEmpty()) {
            w.writeStartElement(ModelParser.CHILDNODES);
            for (SiteNode childNode : node.getChildNodes()) {
                writeSiteNode(childNode);
            }
            w.writeEndElement(); // childNodes
        }
    }

    private void writePermissions(SiteNode node) throws XMLStreamException {
        if (!node.getAllowGroups().isEmpty()
                || !node.getDenyGroups().isEmpty()) {
            w.writeStartElement(ModelParser.PERMISSIONS);
            if (!node.getAllowGroups().isEmpty()) {
                w.writeStartElement(ModelParser.ALLOW);
                for (String group : node.getAllowGroups()) {
                    w.writeEmptyElement(ModelParser.GROUP);
                    w.writeAttribute(ModelParser.NAME, group);
                }
                w.writeEndElement(); // allow
            }
            if (!node.getDenyGroups().isEmpty()) {
                w.writeStartElement(ModelParser.DENY);
                for (String group : node.getDenyGroups()) {
                    w.writeEmptyElement(ModelParser.GROUP);
                    w.writeAttribute(ModelParser.NAME, group);
                }
                w.writeEndElement(); // DENY
            }
            w.writeEndElement(); // permissions
        }
    }



}
