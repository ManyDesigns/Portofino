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

import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.JavaClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.util.ReflectionUtil;
import com.manydesigns.elements.util.Util;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.text.MessageFormat;
import java.util.*;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class XmlParser {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    protected XMLStreamReader xmlStreamReader;
    protected int event;
    protected String localName;
    protected String text;
    protected Map<String, String> attributes;
    protected Stack<String> elementStack = new Stack<String>();

    public static final Logger logger =
            LoggerFactory.getLogger(XmlParser.class);

    public void initParser(InputStream inputStream) throws XMLStreamException {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        xmlStreamReader = inputFactory.createXMLStreamReader(inputStream);
        next();
    }

    public void checkAndSetAttributes(Object object,
                                      Map<String, String> attributes) {
        Class javaClass = object.getClass();
        ClassAccessor classAccessor =
                JavaClassAccessor.getClassAccessor(javaClass);
        Set<String> usedAttributeSet = new HashSet<String>();
        for (PropertyAccessor propertyAccessor : classAccessor.getProperties()) {
            XmlAttribute xmlAttribute =
                    propertyAccessor.getAnnotation(XmlAttribute.class);
            if (xmlAttribute == null) {
                continue;
            }

            String name = propertyAccessor.getName();
            usedAttributeSet.add(name);

            String value = attributes.get(name);
            if (xmlAttribute.required() && value == null) {
                throw new Error(MessageFormat.format(
                        "Attribute ''{0}'' required. {1}",
                        name,
                        getLocationString()));
            }

            Class type = propertyAccessor.getType();
            Object castValue;
            if (value == null) {
                castValue = null;
            } else if (type == String.class) {
                castValue = value;
            } else if (type == Boolean.class || type == Boolean.TYPE) {
                castValue = Boolean.valueOf(value);
            } else {
                castValue = Util.convertValue(value, type);
            }

            propertyAccessor.set(object, castValue);
        }

        // look for any unused attributes
        for (String attribute : attributes.keySet()) {
            if (!usedAttributeSet.contains(attribute)) {
                logger.warn("Unknown attribute '{}'. {}",
                        attribute, getLocationString());
            }
        }
    }

    public void checkRequiredAttributes(Map<String, String> attributes,
                                         String... attrNames) {
        for (String current : attrNames) {
            if (attributes.get(current) == null) {
                throw new Error(MessageFormat.format(
                        "Attribute ''{0}'' required. {1}",
                        current,
                        getLocationString()));
            }
        }
    }

    public String getLocationString() {
        StringBuilder sb = new StringBuilder();
        Location location = xmlStreamReader.getLocation();
        sb.append("Line ");
        sb.append(location.getLineNumber() - 1);
        sb.append(". Column ");
        sb.append(location.getColumnNumber());
        sb.append(". Element stack: ");
        for (String current : elementStack) {
            sb.append("/");
            sb.append(current);
        }
        return sb.toString();
    }

    public void next() throws XMLStreamException {
        event = xmlStreamReader.next();
        switch (event) {
            case XMLStreamConstants.START_ELEMENT:
                loadLocalName();
                logger.debug("START_ELEMENT: {}", localName);
                loadAttributes();
                elementStack.push(localName);
                break;
            case XMLStreamConstants.END_ELEMENT:
                loadLocalName();
                logger.debug("END_ELEMENT: {}", localName);
                String matchingElementName = elementStack.pop();
                if (!matchingElementName.equals(localName)) {
                    throw new Error(MessageFormat.format(
                            "Open/close tags don''t match: {0}/{1}. {1}",
                            matchingElementName,
                            localName,
                            getLocationString()));
                }
                break;
            case XMLStreamConstants.PROCESSING_INSTRUCTION:
                logger.debug("PROCESSING_INSTRUCTION");
                break;
            case XMLStreamConstants.CHARACTERS:
                loadText();
                logger.debug("CHARACTERS: {}", text);
                break;
            case XMLStreamConstants.COMMENT:
                logger.debug("COMMENT");
                break;
            case XMLStreamConstants.SPACE:
                logger.debug("SPACE");
                break;
            case XMLStreamConstants.START_DOCUMENT:
                logger.debug("START_DOCUMENT");
                break;
            case XMLStreamConstants.END_DOCUMENT:
                logger.debug("END_DOCUMENT");
                break;
            case XMLStreamConstants.ENTITY_REFERENCE:
                logger.debug("ENTITY_REFERENCE");
                break;
            case XMLStreamConstants.ATTRIBUTE:
                logger.debug("ATTRIBUTE");
                break;
            case XMLStreamConstants.DTD:
                logger.debug("DTD");
                break;
            case XMLStreamConstants.CDATA:
                loadText();
                logger.debug("CDATA: {}", text);
                break;
            case XMLStreamConstants.NAMESPACE:
                logger.debug("NAMESPACE");
                break;
            case XMLStreamConstants.NOTATION_DECLARATION:
                logger.debug("NOTATION_DECLARATION");
                break;
            case XMLStreamConstants.ENTITY_DECLARATION:
                logger.debug("ENTITY_DECLARATION");
                break;
        }
    }

    public void expectDocumentEnd() throws Exception {
        if (event != XMLStreamConstants.END_DOCUMENT) {
            throw new Error(MessageFormat.format(
                    "Document end expected but not found. {0}",
                    getLocationString()));
        }
    }

    public void expectElement(List<Callback> callbacks) throws Exception {
        Callback[] callbackArray = new Callback[callbacks.size()];
        callbacks.toArray(callbackArray);
        expectElement(callbackArray);
    }

    public void expectElement(Callback... callbackArray) throws Exception {
        int length = callbackArray.length;
        String[] elementNameArray = new String[length];
        int[] counter = new int[length];

        for (int i = 0; i < length; i++) {
            Callback callback = callbackArray[i];
            elementNameArray[i] = callback.getElementName();
        }

        while (true) {
            skipSpacesAndComments();
            int index = ArrayUtils.indexOf(elementNameArray, localName);
            if (event == XMLStreamConstants.START_ELEMENT
                    && index >= 0) {
                Callback callback = callbackArray[index];
                Map<String, String> callbackAttributes = attributes;
                next();
                callback.doElement(callbackAttributes);
                skipSpacesAndComments();
                String expectedElementName = callback.getElementName();
                if (event == XMLStreamConstants.END_ELEMENT
                        && localName.equals(expectedElementName)) {
                    next();
                } else {
                    throw new Error(MessageFormat.format(
                            "Closing tag ''{0}'' not found. Found ''{1}'' instead. {2}",
                            expectedElementName, localName, getLocationString()));
                }
            } else {
                break;
            }
            counter[index] = counter[index] + 1;
        }

        for (int i = 0; i < length; i++) {
            Callback callback = callbackArray[i];
            String name = callback.getElementName();
            int min = callback.getMin();
            int max = callback.getMax();
            if (counter[i] < min) {
                throw new Error(MessageFormat.format(
                        "Element ''{0}'' expected min: {1}  actual: {2}. {3}",
                        name, min, counter[i], getLocationString()));
            }
            if (max >=0 && counter[i] > max) {
                throw new Error(MessageFormat.format(
                        "Element ''{0}'' expected max: {1}  actual: {2}. {3}",
                        name, max, counter[i], getLocationString()));
            }
        }
    }

    protected void skipSpacesAndComments() throws XMLStreamException {
        while (true) {
            if (event == XMLStreamConstants.CHARACTERS
                    && text.trim().length() == 0) {
                next();
                continue;
            }
            if (event == XMLStreamConstants.SPACE) {
                next();
                continue;
            }
            if (event == XMLStreamConstants.COMMENT) {
                next();
                continue;
            }
            break;
        }
    }

    public String expectCharacters()
            throws XMLStreamException {
        if (event == XMLStreamConstants.CHARACTERS) {
            String callbackText = text;
            next();
            return callbackText;
        } else {
            return "";
        }
    }

    public void loadText() {
        text = xmlStreamReader.getText();
    }

    public void loadLocalName() {
        localName = xmlStreamReader.getLocalName();
    }

    public void loadAttributes() {
        attributes = new HashMap<String, String>();
        for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++) {
            String attrName = xmlStreamReader.getAttributeLocalName(i);
            String attrvalue = xmlStreamReader.getAttributeValue(i);
            attributes.put(attrName, attrvalue);
            logger.debug("Attribute {} = {}", attrName, attrvalue);
        }
    }

    public abstract class Callback {
        public static final String copyright =
                "Copyright (c) 2005-2010, ManyDesigns srl";

        protected final String elementName;
        protected final int min;
        protected final int max;

        protected Callback(String elementName, int min, int max) {
            this.elementName = elementName;
            this.min = min;
            this.max = max;
        }

        public abstract void doElement(Map<String, String> attributes) throws Exception;

        public String getElementName() {
            return elementName;
        }

        public int getMin() {
            return min;
        }

        public int getMax() {
            return max;
        }
    }

    public class ElementCallback extends Callback {
        public static final String copyright =
                "Copyright (c) 2005-2010, ManyDesigns srl";

        protected final Object parent;
        protected final Collection parentCollection;
        protected final PropertyAccessor parentProperty;
        protected final Class clazz;
        protected Object obj;

        protected ElementCallback(Object parent, Collection parentCollection,
                                  PropertyAccessor parentProperty,
                                  Class clazz, String elementName,
                                  int min, int max) {
            super(elementName, min, max);
            this.parent = parent;
            this.parentCollection = parentCollection;
            this.parentProperty = parentProperty;
            this.clazz = clazz;
        }

        protected ElementCallback(Class clazz, String elementName,
                                  int min, int max) {
            this(null, null, null, clazz, elementName, min, max);
        }

        public void doElement(Map<String, String> attributes)
                throws Exception {
            // handle strings in a special way
            if (clazz == String.class) {
                obj = expectCharacters();
                if (parentCollection != null) {
                    parentCollection.add(obj);
                } else if (parentProperty != null) {
                    parentProperty.set(parent, obj);
                }
                return;
            }

            // instanciate the class
            if (parent == null) { // root object
                obj = ReflectionUtil.newInstance(clazz);
            } else {
                Class parentClass = parent.getClass();
                Constructor constructor =
                        ReflectionUtil.getBestMatchConstructor(clazz, parentClass);
                if (constructor == null) {
                    obj = ReflectionUtil.newInstance(clazz);
                } else {
                    obj = ReflectionUtil.newInstance(constructor, parent);
                }
                if (parentCollection != null) {
                    parentCollection.add(obj);
                } else if (parentProperty != null) {
                    parentProperty.set(parent, obj);
                }
            }
            if (obj == null) {
                throw new Exception("Could not instanciate: " + clazz);
            }
            checkAndSetAttributes(obj, attributes);

            List<Callback> childCallbacks = new ArrayList<Callback>();

            ClassAccessor classAccessor =
                    JavaClassAccessor.getClassAccessor(clazz);

            // see if the class is also a collection
            XmlCollection xmlClassCollectionAnnotation =
                    classAccessor.getAnnotation(XmlCollection.class);
            if (xmlClassCollectionAnnotation != null) {
                Class[] itemClass = xmlClassCollectionAnnotation.itemClasses();
                String[] itemName = xmlClassCollectionAnnotation.itemNames();
                int itemMin = xmlClassCollectionAnnotation.itemMin();
                int itemMax = xmlClassCollectionAnnotation.itemMax();

                for (int i = 0; i < itemClass.length; i++) {
                    ElementCallback elementCallback =
                            new ElementCallback(obj, (Collection) obj, null,
                                    itemClass[i], itemName[i], itemMin, itemMax);
                    childCallbacks.add(elementCallback);
                }
            }

            // scan the properties looking for annotations
            for (PropertyAccessor propertyAccessor
                    : classAccessor.getProperties()) {
                XmlElement xmlElementAnnotation =
                        propertyAccessor.getAnnotation(XmlElement.class);
                XmlCollection xmlCollectionAnnotation =
                        propertyAccessor.getAnnotation(XmlCollection.class);

                if (xmlElementAnnotation != null) {
                    boolean required = xmlElementAnnotation.required();

                    Class[] itemClasses = xmlElementAnnotation.itemClasses();
                    if (itemClasses.length == 0) {
                        itemClasses = new Class[] {propertyAccessor.getType()};
                    }

                    String[] itemNames = xmlElementAnnotation.itemNames();
                    if (itemNames.length == 0) {
                        itemNames = new String[] {propertyAccessor.getName()};
                    }

                    for (int index = 0; index < itemClasses.length; index++) {
                        Class itemClass = itemClasses[index];
                        String itemName = itemNames[index];
                        ElementCallback callback =
                                new ElementCallback(obj, null, propertyAccessor,
                                        itemClass, itemName, 0, 1);
                        childCallbacks.add(callback);
                    }
                } else if (xmlCollectionAnnotation != null) {
                    boolean required = xmlCollectionAnnotation.required();
                    String collectionName = propertyAccessor.getName();

                    Class[] itemClass = xmlCollectionAnnotation.itemClasses();
                    String[] itemName = xmlCollectionAnnotation.itemNames();
                    int itemMin = xmlCollectionAnnotation.itemMin();
                    int itemMax = xmlCollectionAnnotation.itemMax();

                    Collection collection =
                            (Collection) propertyAccessor.get(obj);
                    CollectionCallback collectionCallBack =
                            new CollectionCallback(obj,
                                    collection, collectionName, required,
                                    itemClass, itemName, itemMin, itemMax);
                    childCallbacks.add(collectionCallBack);
                }
            }
            expectElement(childCallbacks);
        }
    }

    public class CollectionCallback extends Callback {
        protected final Object parent;
        protected final Collection parentCollection;
        protected final boolean required;
        protected final Class[] itemClass;
        protected final String[] itemName;
        protected final int itemMin;
        protected final int itemMax;

        protected CollectionCallback(Object parent, Collection parentCollection,
                                     String elementName, boolean required,
                                     Class[] itemClass, String[] itemName,
                                     int itemMin, int itemMax) {
            super(elementName, (required ? 1 : 0), 1);
            this.parent = parent;
            this.parentCollection = parentCollection;
            this.itemClass = itemClass;
            this.itemName = itemName;
            this.required = required;
            this.itemMin = itemMin;
            this.itemMax = itemMax;
        }

        public void doElement(Map<String, String> attributes)
                throws Exception {
            List<Callback> callbacks = new ArrayList<Callback>();
            for (int i = 0; i < itemClass.length; i++) {
                ElementCallback callback = new ElementCallback(parent, parentCollection, null,
                        itemClass[i], itemName[i], itemMin, itemMax);
                callbacks.add(callback);
            }
            expectElement(callbacks);
        }
    }


    public Object parse(String resourceName, Class rootClass,
                        String rootName) throws Exception {
        InputStream inputStream = ReflectionUtil.getResourceAsStream(resourceName);
        return parse(inputStream, rootClass, rootName);
    }

    public Object parse(File file, Class rootClass,
                        String rootName) throws Exception {
        logger.info("Parsing file: {}", file.getAbsolutePath());
        InputStream input = new FileInputStream(file);
        return parse(input, rootClass, rootName);
    }

    private Object parse(InputStream inputStream, Class rootClass,
                         String rootName) throws Exception {
        initParser(inputStream);
        ElementCallback elementCallback =
                new ElementCallback(rootClass, rootName, 1, 1);
        expectElement(elementCallback);
        expectDocumentEnd();
        return elementCallback.obj;
    }
}
