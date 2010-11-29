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
import java.util.Collection;
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
    protected XMLStreamWriter w = null;
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

    public void write(File file, Object object, String rootName) {
        try {
            //Istanzio il Writer a partire da un FileWiter
            w = f.createXMLStreamWriter(new FileWriter(file));
            if (indentationEnabled) {
                w = new IndentingXMLStreamWriter(w);
            }
            //Inizio il documento XML
            w.writeStartDocument();

            writeElement(object, rootName);

            // Chiudo il documento
            w.writeEndDocument();
            w.flush();
        } catch (Exception e) {
            LogUtil.warningMF(logger,
                    "Exception caught while writing file: {0}",
                    e, file.getAbsolutePath());
        } finally {
            closeQuietly();
        }
    }

    public void writeElement(Object object, String elementName)
            throws XMLStreamException {
        // opening tag
        w.writeStartElement(elementName);

        // element's attributes
        writeAttributes(object);

        Class javaClass = object.getClass();
        ClassAccessor classAccessor =
                JavaClassAccessor.getClassAccessor(javaClass);

        doOwnCollection(object, classAccessor);

        for (PropertyAccessor propertyAccessor
                : classAccessor.getProperties()) {
            String propertyName = propertyAccessor.getName();
            XmlElement xmlElementAnnotation =
                    propertyAccessor.getAnnotation(XmlElement.class);

            if (xmlElementAnnotation != null) {
                Object item = propertyAccessor.get(object);
            }
        }

        // closing tag
        w.writeEndElement();

    }

    private void doOwnCollection(Object object, ClassAccessor classAccessor)
            throws XMLStreamException {
        XmlCollection xmlCollectionAnnotation =
                classAccessor.getAnnotation(XmlCollection.class);
        if (xmlCollectionAnnotation != null) {
            Class[] itemClasses = xmlCollectionAnnotation.itemClasses();
            String[] itemNames = xmlCollectionAnnotation.itemNames();
            Collection collection = (Collection)object;
            for (Object item : collection) {
                Class itemClass = item.getClass();
                int index = ArrayUtils.indexOf(itemClasses, itemClass);
                if (index >= 0) {
                    String itemName = itemNames[index];
                    writeElement(item, itemName);
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

}
