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

package com.manydesigns.portofino.xml;

import com.manydesigns.elements.logging.LogUtil;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.JavaClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.util.Util;
import com.manydesigns.portofino.model.io.IndentingXMLStreamWriter;
import org.apache.commons.lang.ArrayUtils;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Logger;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class XmlWriter {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";


    //--------------------------------------------------------------------------
    // Constants
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    // Variables
    //--------------------------------------------------------------------------

    protected final XMLOutputFactory f;
    protected XMLStreamWriter w;
    protected boolean indentationEnabled = true;

    //--------------------------------------------------------------------------
    // Logging
    //--------------------------------------------------------------------------

    public static final Logger logger =
            LogUtil.getLogger(XmlWriter.class);

    //--------------------------------------------------------------------------
    // Constructor
    //--------------------------------------------------------------------------

    public XmlWriter() {
        f = XMLOutputFactory.newInstance();        
    }

    //--------------------------------------------------------------------------
    // Methods
    //--------------------------------------------------------------------------

    public void write(File file, Object object, String rootName) throws Exception {
        try {
            //Istanzio il Writer a partire da un FileWiter
            w = f.createXMLStreamWriter(new FileWriter(file));
            if (indentationEnabled) {
                w = new IndentingXMLStreamWriter(w);
            }
            //Inizio il documento XML
            w.writeStartDocument();

            ElementWriter elementWriter = new ElementWriter(object, rootName);
            elementWriter.write();

            // Chiudo il documento
            w.writeEndDocument();
            w.flush();
        } catch (Exception e) {
            LogUtil.warningMF(logger,
                    "Exception caught while writing file: {0}",
                    e, file.getAbsolutePath());
            throw e;
        } finally {
            closeQuietly();
        }
    }



    public void writeAttributes(Object object) throws XMLStreamException {
        Class javaClass = object.getClass();
        ClassAccessor classAccessor =
                JavaClassAccessor.getClassAccessor(javaClass);

        List<AttributeWriter> writers = new ArrayList<AttributeWriter>();
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
                    LogUtil.warningMF(logger,
                            "Attribute ''{0}'' required", name);
                }
            } else {
                int order = xmlAttribute.order();
                AttributeWriter writer =
                        new AttributeWriter(name, stringValue, order);
                writers.add(writer);
            }
        }
        Collections.sort(writers, new AttributeWriterComparator());
        for (Writer current : writers) {
            current.write();
        }
    }

    public void closeQuietly() {
        if (w != null) {
            //Concludo la scrittura
            try {
                w.close();
            } catch (Throwable e) {
                LogUtil.warning(logger,
                        "Exception caught while closing stream", e);
            }
        }
    }

    //--------------------------------------------------------------------------
    // Writer interface
    //--------------------------------------------------------------------------

    interface Writer {
        void write() throws XMLStreamException;
    }

    //--------------------------------------------------------------------------
    // AttributeWriter
    //--------------------------------------------------------------------------

    class AttributeWriter implements Writer {
        final String name;
        final String value;
        final int order;

        AttributeWriter(String name, String value, int order) {
            this.name = name;
            this.value = value;
            this.order = order;
        }

        public void write() throws XMLStreamException {
            w.writeAttribute(name, value);
        }
    }

    static class AttributeWriterComparator
            implements Comparator<AttributeWriter> {
        public int compare(AttributeWriter o1,
                           AttributeWriter o2) {
            int order1 = o1.order;
            int order2 = o2.order;
            if(order1 > order2) {
                return 1;
            } else if(order1 < order2) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    //--------------------------------------------------------------------------
    // ElementWriter
    //--------------------------------------------------------------------------

    class ElementWriter implements Writer {
        final Object object;
        final String elementName;

        ElementWriter(Object object, String elementName) {
            this.object = object;
            this.elementName = elementName;
        }

        public void write()
                throws XMLStreamException {
            Class javaClass = object.getClass();

            if (javaClass == String.class) {
                writeStringElement();
            } else {
                writeObjectElement();
            }

        }

        private void writeObjectElement() throws XMLStreamException {
            Class javaClass = object.getClass();
            ClassAccessor classAccessor =
                    JavaClassAccessor.getClassAccessor(javaClass);

            List<Writer> writers = new ArrayList<Writer>();

            XmlCollection ownCollectionAnnotation =
                    classAccessor.getAnnotation(XmlCollection.class);
            if (ownCollectionAnnotation != null) {
                CollectionContentWriter collectionContentWriter = 
                        new CollectionContentWriter(
                                (Collection)object, ownCollectionAnnotation);
                writers.add(collectionContentWriter);
            }

            for (PropertyAccessor propertyAccessor
                    : classAccessor.getProperties()) {
                String propertyName = propertyAccessor.getName();

                XmlElement xmlElementAnnotation =
                        propertyAccessor.getAnnotation(XmlElement.class);
                if (xmlElementAnnotation != null) {
                    Object item = propertyAccessor.get(object);
                    if (item == null) {
                        if (xmlElementAnnotation.required()) {
                            LogUtil.warningMF(logger,
                                    "Element ''{0}'' required", propertyName);
                        }
                    } else {
                        ElementWriter elementWriter =
                                new ElementWriter(item, propertyName);
                        writers.add(elementWriter);
                    }
                }

                XmlCollection collectionAnnotation =
                        propertyAccessor.getAnnotation(XmlCollection.class);
                if (collectionAnnotation != null) {
                    Collection collection =
                            (Collection) propertyAccessor.get(object);
                    if (!collection.isEmpty() || collectionAnnotation.required()) {
                        CollectionWriter collectionWriter =
                                new CollectionWriter(collection,
                                        collectionAnnotation, propertyName);
                        writers.add(collectionWriter);
                    }
                }
            }

            // do all the writing
            if (writers.isEmpty()) {
                w.writeEmptyElement(elementName);
                writeAttributes(object);
            } else {
                w.writeStartElement(elementName);
                writeAttributes(object);
                for (Writer current : writers) {
                    current.write();
                }
                w.writeEndElement();
            }
        }

        protected void writeStringElement() throws XMLStreamException {
            w.writeStartElement(elementName);
            w.writeCharacters((String) object);
            w.writeEndElement();
        }
    }

    //--------------------------------------------------------------------------
    // CollectionContentWriter
    //--------------------------------------------------------------------------

    class CollectionContentWriter implements Writer {
        final Collection collection;
        final XmlCollection annotation;

        CollectionContentWriter(Collection collection,
                                XmlCollection annotation) {
            this.collection = collection;
            this.annotation = annotation;
        }

        public void write() throws XMLStreamException {
            Class[] itemClasses = annotation.itemClasses();
            String[] itemNames = annotation.itemNames();
            for (Object item : collection) {
                Class itemClass = item.getClass();
                int index = ArrayUtils.indexOf(itemClasses, itemClass);
                if (index >= 0) {
                    String itemName = itemNames[index];
                    ElementWriter elementWriter =
                            new ElementWriter(item, itemName);
                    elementWriter.write();
                } else {
                    String message = MessageFormat.format(
                            "Item class ''{0}'' not in expected set ''{1}''",
                            itemClass.getName(),
                            ArrayUtils.toString(itemClasses));
                    throw new Error(message);
                }
            }
        }
    }

    //--------------------------------------------------------------------------
    // CollectionWriter
    //--------------------------------------------------------------------------

    class CollectionWriter extends CollectionContentWriter {
        final String elementName;

        CollectionWriter(Collection collection,
                         XmlCollection annotation,
                         String elementName) {
            super(collection, annotation);
            this.elementName = elementName;
        }

        @Override
        public void write() throws XMLStreamException {
            if (collection.isEmpty()) {
                w.writeEmptyElement(elementName);
            } else {
                w.writeStartElement(elementName);
                super.write();
                w.writeEndElement();
            }
        }
    }
}
