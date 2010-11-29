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
import com.manydesigns.portofino.model.annotations.Annotation;
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

    //--------------------------------------------------------------------------
    // Constants
    //--------------------------------------------------------------------------

    public static final String MODEL = "model";

    public static final String DATABASES = "databases";
    public static final String DATABASE = "database";
    public static final String SCHEMAS = "schemas";
    public static final String SCHEMA = "schema";
    public static final String TABLES = "tables";
    public static final String TABLE = "table";
    public static final String COLUMNS = "columns";
    public static final String COLUMN = "column";
    public static final String PRIMARYKEY = "primaryKey";
    public static final String PRIMARYKEYCOLUMN = COLUMN;
    public static final String FOREIGNKEYS = "foreignKeys";
    public static final String FOREIGNKEY = "foreignKey";
    public static final String REFERENCES = "references";
    public static final String REFERENCE = "reference";
    public static final String CHILDNODES = "childNodes";
    public static final String PORTLETS = "portlets";
    public static final String PORTLET = "portlet";
    public static final String USECASES = "useCases";
    public static final String USECASE = "useCase";
    public static final String BUTTONS = "buttons";
    public static final String BUTTON = "button";
    public static final String SUBUSECASES = "subUseCases";
    public static final String PROPERTIES = "properties";
    public static final String PROPERTY = "property";
    public static final String ANNOTATIONS = "annotations";
    public static final String ANNOTATION = "annotation";
    public static final String VALUE = "value";
    public static final String ROOTNODE = "rootNode";
    public static final String DOCUMENTNODE = "documentNode";
    public static final String FOLDERNODE = "folderNode";
    public static final String CUSTOMNODE = "customNode";
    public static final String CUSTOMFOLDERNODE = "customFolderNode";
    public static final String USECASENODE = "useCaseNode";
    public static final String PORTLETNODE = "portletNode";
    public static final String PERMISSIONS = "permissions";
    public static final String ALLOW = "allow";
    public static final String DENY = "deny";
    public static final String GROUP = "group";
    public static final String NAME = "name";
    public static final String SELECTIONPROVIDERS = "selectionProviders";
    public static final String SELECTIONPROVIDER = "selectionProvider";
    public static final String SELECTIONPROPERTIES = "selectionProperties";
    public static final String SELECTIONPROPERTY = "selectionProperty";
    
    protected XMLStreamWriter w = null;

    public static final Logger logger =
            LogUtil.getLogger(ModelWriter.class);


    public void write(Model model, File file) throws IOException {
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

            Object value = propertyAccessor.get(object);

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
        w.writeStartElement(MODEL);

        // databases
        w.writeStartElement(DATABASES);
        for (Database database : model.getDatabases()){
            writeDatabase(database);
        }
        w.writeEndElement(); // databases

        SiteNode rootNode = model.getRootNode();
        if (rootNode != null) {
            writeSiteNode(rootNode);
        }

        w.writeEndElement(); // model
    }

    //--------------------------------------------------------------------------
    // Databases/schemas/tables/columns/...
    //--------------------------------------------------------------------------

    private void writeDatabase(Database database) throws XMLStreamException {
        w.writeStartElement(DATABASE);
        writeAttributes(database);
        w.writeStartElement(SCHEMAS);
        for (Schema schema : database.getSchemas()){
            writeSchema(schema);
        }
        w.writeEndElement();//schemas
        w.writeEndElement();//database
    }

    private void writeSchema(Schema schema) throws XMLStreamException {
        w.writeStartElement(SCHEMA);
        writeAttributes(schema);
        w.writeStartElement(TABLES);
        for (Table table : schema.getTables()){
            writeTable(table);
        }
        w.writeEndElement();
        w.writeEndElement();//schema
    }

    private void writeTable(Table table) throws XMLStreamException {
        w.writeStartElement(TABLE);
        writeAttributes(table);
        w.writeStartElement(COLUMNS);
        for (Column column : table.getColumns()){
            writeColumn(column);
        }
        w.writeEndElement();

        // PrimaryKey
        writePrimaryKey(table.getPrimaryKey());

        // Foreign keys
        if (!table.getForeignKeys().isEmpty()) {
            w.writeStartElement(FOREIGNKEYS);

            for(ForeignKey rel : table.getForeignKeys()) {
                writeForeignKey(rel);
            }

            w.writeEndElement(); // foreign keys
        }

        // Annotations
        writeModelAnnotations(table.getAnnotations());

        w.writeEndElement(); //table
    }

    private void writeColumn(Column column) throws XMLStreamException {
        List<Annotation> annotations = column.getAnnotations();
        if (annotations.isEmpty()) {
            w.writeEmptyElement(COLUMN);
            writeAttributes(column);
        } else {
            w.writeStartElement(COLUMN);
            writeAttributes(column);
            writeModelAnnotations(annotations);
            w.writeEndElement(); //column
        }
    }

    private void writeModelAnnotations(List<Annotation> annotations)
            throws XMLStreamException {
        if (!annotations.isEmpty()) {
            w.writeStartElement(ANNOTATIONS);
            for (Annotation annotation : annotations){
                writeModelAnnotation(annotation);
            }
            w.writeEndElement(); // annotations
        }
    }

    private void writeModelAnnotation(Annotation annotation)
            throws XMLStreamException {
        if (annotation.isEmpty()) {
            w.writeEmptyElement(ANNOTATION);
            writeAttributes(annotation);
        } else {
            w.writeStartElement(ANNOTATION);
            writeAttributes(annotation);
            for (String value : (List<String>) annotation) {
                if (value == null) {
                    w.writeEmptyElement(VALUE);                    
                } else {
                    w.writeStartElement(VALUE);
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
        w.writeStartElement(PRIMARYKEY);
        writeAttributes(primaryKey);

        for (PrimaryKeyColumn primaryKeyColumn : primaryKey) {
            writePrimaryKeyColumn(primaryKeyColumn);
        }
        w.writeEndElement();//primaryKey
    }

    private void writePrimaryKeyColumn(PrimaryKeyColumn primaryKeyColumn)
            throws XMLStreamException {
        w.writeEmptyElement(COLUMN);
        writeAttributes(primaryKeyColumn);
    }


    private void writeForeignKey(ForeignKey foreignKey)
            throws XMLStreamException {
        w.writeStartElement(FOREIGNKEY);
        writeAttributes(foreignKey);
        w.writeStartElement(REFERENCES);
        for (Reference reference : foreignKey.getReferences()) {
            writeReference(reference);
        }
        w.writeEndElement(); // references

        // Annotations
        writeModelAnnotations(foreignKey.getAnnotations());
        w.writeEndElement(); // foreign key
    }

    private void writeReference(Reference reference)
            throws XMLStreamException {
        w.writeEmptyElement(REFERENCE);
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
        simpleWriteNode(node, ROOTNODE);
    }

    private void writeFolderNode(FolderNode node) throws XMLStreamException {
        simpleWriteNode(node, FOLDERNODE);
    }

    private void writeDocumentNode(DocumentNode node) throws XMLStreamException {
        simpleWriteNode(node, DOCUMENTNODE);
    }

    private void writeCustomNode(CustomNode node) throws XMLStreamException {
        simpleWriteNode(node, CUSTOMNODE);
    }

    private void writeCustomFolderNode(CustomFolderNode node) throws XMLStreamException {
        simpleWriteNode(node, CUSTOMFOLDERNODE);
    }

    private void writeUseCaseNode(UseCaseNode node) throws XMLStreamException {
        w.writeStartElement(USECASENODE);
        writeAttributes(node);
        writeUseCase(node.getUseCase());
        writeChildNodes(node);
        writePermissions(node);
        w.writeEndElement(); // useCaseNode
    }

    private void writeUseCase(UseCase useCase) throws XMLStreamException {
        w.writeStartElement(USECASE);
        writeAttributes(useCase);

        if (!useCase.getProperties().isEmpty()) {
            w.writeStartElement(PROPERTIES);
            for (UseCaseProperty property : useCase.getProperties()) {
                writeUseCaseProperty(property);
            }
            w.writeEndElement(); // properties
        }

        if (!useCase.getSelectionProviders().isEmpty()) {
            w.writeStartElement(SELECTIONPROVIDERS);
            for (ModelSelectionProvider modelSelectionProvider
                    : useCase.getSelectionProviders()) {
                writeModelSelectionProvider(modelSelectionProvider);
            }
            w.writeEndElement(); // selectionProviders
        }

        writeModelAnnotations(useCase.getModelAnnotations());

        if (!useCase.getButtons().isEmpty()) {
            w.writeStartElement(BUTTONS);
            for (Button button : useCase.getButtons()) {
                writeButton(button);
            }
            w.writeEndElement(); // buttons
        }

        if (!useCase.getSubUseCases().isEmpty()) {
            w.writeStartElement(SUBUSECASES);
            for (UseCase subUseCase : useCase.getSubUseCases()) {
                writeUseCase(subUseCase);
            }
            w.writeEndElement(); // subUseCases
        }

        w.writeEndElement(); // useCase
    }

    private void writeButton(Button button) throws XMLStreamException {
        w.writeEmptyElement(BUTTON);
        writeAttributes(button);
    }

    private void writeModelSelectionProvider(ModelSelectionProvider modelSelectionProvider)
            throws XMLStreamException {
        w.writeStartElement(SELECTIONPROVIDER);
        writeAttributes(modelSelectionProvider);
        if (!modelSelectionProvider.getSelectionProperties().isEmpty()) {
            w.writeStartElement(SELECTIONPROPERTIES);
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
        w.writeEmptyElement(SELECTIONPROPERTY);
        writeAttributes(selectionProperty);
    }

    private void writeUseCaseProperty(UseCaseProperty property)
            throws XMLStreamException {
        if (property.getAnnotations().isEmpty()) {
            w.writeEmptyElement(PROPERTY);
            writeAttributes(property);
        } else {
            w.writeStartElement(PROPERTY);
            writeAttributes(property);

            writeModelAnnotations(property.getAnnotations());

            w.writeEndElement();
        }
    }

    private void writePortletNode(PortletNode node) throws XMLStreamException {
        simpleWriteNode(node, PORTLETNODE);
    }

    private void simpleWriteNode(SiteNode node, String elementType)
            throws XMLStreamException {
        if (node.getChildNodes().isEmpty() && (node.getPermissions() == null || (
                node.getPermissions().getAllow().isEmpty()
                && node.getPermissions().getDeny().isEmpty()))) {
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
            w.writeStartElement(CHILDNODES);
            for (SiteNode childNode : node.getChildNodes()) {
                writeSiteNode(childNode);
            }
            w.writeEndElement(); // childNodes
        }
    }

    private void writePermissions(SiteNode node) throws XMLStreamException {
        Permissions permissions = node.getPermissions();
        if (permissions == null) {
            return;
        }
        if (!permissions.getAllow().isEmpty()
                || !permissions.getDeny().isEmpty()) {
            w.writeStartElement(PERMISSIONS);
            if (!permissions.getAllow().isEmpty()) {
                w.writeStartElement(ALLOW);
                for (String group : permissions.getAllow()) {
                    w.writeEmptyElement(GROUP);
                    w.writeAttribute(NAME, group);
                }
                w.writeEndElement(); // allow
            }
            if (!permissions.getDeny().isEmpty()) {
                w.writeStartElement(DENY);
                for (String group : permissions.getDeny()) {
                    w.writeEmptyElement(GROUP);
                    w.writeAttribute(NAME, group);
                }
                w.writeEndElement(); // DENY
            }
            w.writeEndElement(); // permissions
        }
    }



}
