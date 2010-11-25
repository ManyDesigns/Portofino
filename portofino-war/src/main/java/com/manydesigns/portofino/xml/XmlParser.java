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
import com.manydesigns.elements.util.ReflectionUtil;
import com.manydesigns.elements.util.Util;
import org.apache.commons.lang.ArrayUtils;

import javax.xml.stream.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Logger;

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
            LogUtil.getLogger(XmlParser.class);

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

            try {
                propertyAccessor.set(object, castValue);
            } catch (Throwable e) {
                LogUtil.warningMF(logger,
                        "Cannot set attribute/property ''{0}''. {1}",
                        e, name, getLocationString());
            }
        }

        // look for any unused attributes
        for (String attribute : attributes.keySet()) {
            if (!usedAttributeSet.contains(attribute)) {
                LogUtil.warningMF(logger,
                        "Unknown attribute ''{0}''. {1}",
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
                LogUtil.fineMF(logger, "START_ELEMENT: {0}", localName);
                loadAttributes();
                elementStack.push(localName);
                break;
            case XMLStreamConstants.END_ELEMENT:
                loadLocalName();
                LogUtil.fineMF(logger, "END_ELEMENT: {0}", localName);
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
                LogUtil.fineMF(logger, "PROCESSING_INSTRUCTION");
                break;
            case XMLStreamConstants.CHARACTERS:
                loadText();
                LogUtil.fineMF(logger, "CHARACTERS: {0}", text);
                break;
            case XMLStreamConstants.COMMENT:
                LogUtil.fineMF(logger, "COMMENT");
                break;
            case XMLStreamConstants.SPACE:
                LogUtil.fineMF(logger, "SPACE");
                break;
            case XMLStreamConstants.START_DOCUMENT:
                LogUtil.fineMF(logger, "START_DOCUMENT");
                break;
            case XMLStreamConstants.END_DOCUMENT:
                LogUtil.fineMF(logger, "END_DOCUMENT");
                break;
            case XMLStreamConstants.ENTITY_REFERENCE:
                LogUtil.fineMF(logger, "ENTITY_REFERENCE");
                break;
            case XMLStreamConstants.ATTRIBUTE:
                LogUtil.fineMF(logger, "ATTRIBUTE");
                break;
            case XMLStreamConstants.DTD:
                LogUtil.fineMF(logger, "DTD");
                break;
            case XMLStreamConstants.CDATA:
                loadText();
                LogUtil.fineMF(logger, "CDATA: {0}", text);
                break;
            case XMLStreamConstants.NAMESPACE:
                LogUtil.fineMF(logger, "NAMESPACE");
                break;
            case XMLStreamConstants.NOTATION_DECLARATION:
                LogUtil.fineMF(logger, "NOTATION_DECLARATION");
                break;
            case XMLStreamConstants.ENTITY_DECLARATION:
                LogUtil.fineMF(logger, "ENTITY_DECLARATION");
                break;
        }
    }

    public void expectDocument(DocumentCallback callback) throws Exception {
        callback.doDocument();
        if (event != XMLStreamConstants.END_DOCUMENT) {
            throw new Error(MessageFormat.format(
                    "Document end expected but not found. {0}",
                    getLocationString()));
        }
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
            LogUtil.fineMF(logger, "Attribute {0} = {1}", attrName, attrvalue);
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
                        ReflectionUtil.getConstructor(clazz, parentClass);
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
            checkAndSetAttributes(obj, attributes);

            List<Callback> childCallbacks = new ArrayList<Callback>();

            ClassAccessor classAccessor =
                    JavaClassAccessor.getClassAccessor(clazz);

            // see if the class is also a collection
            XmlCollection xmlClassCollectionAnnotation =
                    classAccessor.getAnnotation(XmlCollection.class);
            if (xmlClassCollectionAnnotation != null) {
                    Class itemClass = xmlClassCollectionAnnotation.itemClass();
                    String itemName = xmlClassCollectionAnnotation.itemName();
                    int itemMin = xmlClassCollectionAnnotation.itemMin();
                    int itemMax = xmlClassCollectionAnnotation.itemMax();

                    ElementCallback elementCallback =
                            new ElementCallback(obj, (Collection) obj, null,
                                    itemClass, itemName, itemMin, itemMax);
                    childCallbacks.add(elementCallback);
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
                    Class itemClass = propertyAccessor.getType();
                    String itemName = propertyAccessor.getName();
                    ElementCallback callback =
                            new ElementCallback(obj, null, propertyAccessor,
                                    itemClass, itemName, (required ? 1 : 0), 1);
                    childCallbacks.add(callback);
                } else if (xmlCollectionAnnotation != null) {
                    boolean required = xmlCollectionAnnotation.required();
                    String collectionName = propertyAccessor.getName();

                    Class itemClass = xmlCollectionAnnotation.itemClass();
                    String itemName = xmlCollectionAnnotation.itemName();
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
            Callback[] callbackArray = new Callback[childCallbacks.size()];
            childCallbacks.toArray(callbackArray);
            expectElement(callbackArray);
        }
    }

    public class CollectionCallback extends Callback {
        protected final Object parent;
        protected final Collection parentCollection;
        protected final boolean required;
        protected final Class itemClass;
        protected final String itemName;
        protected final int itemMin;
        protected final int itemMax;

        protected CollectionCallback(Object parent, Collection parentCollection,
                                     String elementName, boolean required,
                                     Class itemClass, String itemName,
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
            expectElement(new ElementCallback(parent, parentCollection, null,
                    itemClass, itemName, itemMin, itemMax));
        }
    }


    public Object parse(String resourceName, Class rootClass) throws Exception {
        InputStream inputStream = ReflectionUtil.getResourceAsStream(resourceName);
        return parse(inputStream, rootClass);
    }

    public Object parse(File file, Class rootClass) throws Exception {
        LogUtil.infoMF(logger, "Parsing file: {0}", file.getAbsolutePath());
        InputStream input = new FileInputStream(file);
        return parse(input, rootClass);
    }

    private Object parse(InputStream inputStream, Class rootClass) throws Exception {
        initParser(inputStream);
        this.rootClass = rootClass;
        expectDocument(new ModelDocumentCallback());
        return model;
    }

    private Class rootClass;
    private Object model;

    private class ModelDocumentCallback implements DocumentCallback {
        public void doDocument() throws Exception {
            ElementCallback elementCallback =
                    new ElementCallback(rootClass, "model", 1, 1);
            expectElement(elementCallback);
            model = elementCallback.obj;
        }
    }

}
