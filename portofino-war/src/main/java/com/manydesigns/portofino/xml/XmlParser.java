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

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
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
    protected int _event;
    protected String _localName;
    protected String _text;
    protected Map<String, String> _attributes;
    protected Stack<String> _elementStack = new Stack<String>();

    public static final Logger logger =
            LogUtil.getLogger(XmlParser.class);

    public void initParser(XMLStreamReader xmlStreamReader)
            throws XMLStreamException {
        this.xmlStreamReader = xmlStreamReader;
        next();
    }

    public void checkRequiredAttributes(Map<String, String> attributes,
                                         String... attrNames) {
        for (String current : attrNames) {
            if (attributes.get(current) == null) {
                throw new Error("Attribute " + current + " required");
            }
        }
    }

    public void next() throws XMLStreamException {
        _event = xmlStreamReader.next();
        switch (_event) {
            case XMLStreamConstants.START_ELEMENT:
                loadLocalName();
                LogUtil.fineMF(logger, "START_ELEMENT: {0}", _localName);
                loadAttributes();
                _elementStack.push(_localName);
                break;
            case XMLStreamConstants.END_ELEMENT:
                loadLocalName();
                LogUtil.fineMF(logger, "END_ELEMENT: {0}", _localName);
                String matchingElementName = _elementStack.pop();
                if (!matchingElementName.equals(_localName)) {
                    throw new Error("Open/close tags don't match: " +
                            matchingElementName + "/" + _localName);
                }
                break;
            case XMLStreamConstants.PROCESSING_INSTRUCTION:
                LogUtil.fineMF(logger, "PROCESSING_INSTRUCTION");
                break;
            case XMLStreamConstants.CHARACTERS:
                loadText();
                LogUtil.fineMF(logger, "CHARACTERS: {0}", _text);
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
                LogUtil.fineMF(logger, "CDATA: {0}", _text);
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
        if (_event != XMLStreamConstants.END_DOCUMENT) {
            throw new Error("Document end expected but not found");
        }
    }

    public void expectElement(String elementName, int min, Integer max,
                               ElementCallback callback) throws XMLStreamException {
        int counter = 0;
        while (true) {
            skipSpacesAndComments();
            if (_event == XMLStreamConstants.START_ELEMENT
                    && _localName.equals(elementName)) {
                Map<String, String> callbackAttributes = _attributes;
                next();
                callback.doElement(callbackAttributes);
                skipSpacesAndComments();
                if (_event == XMLStreamConstants.END_ELEMENT
                        && _localName.equals(elementName)) {
                    next();
                } else {
                    throw new Error("Closing tag not found for: " + elementName);
                }
            } else {
                break;
            }
            counter = counter + 1;
        }
        if (counter < min) {
            throw new Error(MessageFormat.format(
                    "Element {0} expected min: {1}  actual: {2}",
                    elementName, min, counter));
        }
        if (max != null && counter > max) {
            throw new Error(MessageFormat.format(
                    "Element {0} expected max: {1}  actual: {2}",
                    elementName, max, counter));
        }
    }

    protected void skipSpacesAndComments() throws XMLStreamException {
        while (true) {
            if (_event == XMLStreamConstants.CHARACTERS
                    && _text.trim().length() == 0) {
                next();
                continue;
            }
            if (_event == XMLStreamConstants.SPACE) {
                next();
                continue;
            }
            if (_event == XMLStreamConstants.COMMENT) {
                next();
                continue;
            }
            break;
        }
    }

    public void expectCharacters(CharactersCallback callback)
            throws XMLStreamException {
        if (_event == XMLStreamConstants.CHARACTERS) {
            String callbackText = _text;
            next();
            callback.doCharacters(callbackText);
        } else {
            throw new Error("Characters expected but not found");
        }
    }

    public void loadText() {
        _text = xmlStreamReader.getText();
    }

    public void loadLocalName() {
        _localName = xmlStreamReader.getLocalName();
    }

    public void loadAttributes() {
        _attributes = new HashMap<String, String>();
        for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++) {
            String attrName = xmlStreamReader.getAttributeLocalName(i);
            String attrvalue = xmlStreamReader.getAttributeValue(i);
            _attributes.put(attrName, attrvalue);
            LogUtil.fineMF(logger, "Attribute {0} = {1}", attrName, attrvalue);
        }
    }

}
