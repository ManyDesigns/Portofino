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
import org.apache.commons.lang.ArrayUtils;

import javax.xml.stream.*;
import java.io.InputStream;
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

    public void expectDocument(DocumentCallback callback) throws XMLStreamException {
        callback.doDocument();
        if (event != XMLStreamConstants.END_DOCUMENT) {
            throw new Error(MessageFormat.format(
                    "Document end expected but not found. {0}",
                    getLocationString()));
        }
    }

    public void expectElement(String elementName, int min, Integer max,
                               ElementCallback callback) throws XMLStreamException {
        int counter = 0;
        while (true) {
            skipSpacesAndComments();
            if (event == XMLStreamConstants.START_ELEMENT
                    && localName.equals(elementName)) {
                Map<String, String> callbackAttributes = attributes;
                next();
                callback.doElement(callbackAttributes);
                skipSpacesAndComments();
                if (event == XMLStreamConstants.END_ELEMENT
                        && localName.equals(elementName)) {
                    next();
                } else {
                    throw new Error(MessageFormat.format(
                            "Closing tag ''{0}'' not found. Found ''{1}'' instead. {2}",
                            elementName, localName, getLocationString()));
                }
            } else {
                break;
            }
            counter = counter + 1;
        }
        if (counter < min) {
            throw new Error(MessageFormat.format(
                    "Element ''{0}'' expected min: {1}  actual: {2}. {3}",
                    elementName, min, counter, getLocationString()));
        }
        if (max != null && counter > max) {
            throw new Error(MessageFormat.format(
                    "Element ''{0}'' expected max: {1}  actual: {2}. {3}",
                    elementName, max, counter, getLocationString()));
        }
    }


    public void expectElement(String[] elementNameArray, int min, Integer max,
                               ElementCallback[] callbackArray) throws XMLStreamException {
        int counter = 0;
        while (true) {
            skipSpacesAndComments();
            int index = ArrayUtils.indexOf(elementNameArray, localName);
            if (event == XMLStreamConstants.START_ELEMENT
                    && index >= 0) {
                Map<String, String> callbackAttributes = attributes;
                next();
                callbackArray[index].doElement(callbackAttributes);
                skipSpacesAndComments();
                String expectedElementName = elementNameArray[index];
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
            counter = counter + 1;
        }
        if (counter < min) {
            throw new Error(MessageFormat.format(
                    "Element ''{0}'' expected min: {1}  actual: {2}. {3}",
                    ArrayUtils.toString(elementNameArray), min, counter, getLocationString()));
        }
        if (max != null && counter > max) {
            throw new Error(MessageFormat.format(
                    "Element ''{0}'' expected max: {1}  actual: {2}. {3}",
                    ArrayUtils.toString(elementNameArray), max, counter, getLocationString()));
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

    public void expectCharacters(CharactersCallback callback)
            throws XMLStreamException {
        if (event == XMLStreamConstants.CHARACTERS) {
            String callbackText = text;
            next();
            callback.doCharacters(callbackText);
        } else {
            callback.doCharacters(null);
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
}
