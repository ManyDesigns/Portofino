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
import com.manydesigns.portofino.model.Connection;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Logger;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class ConnectionsParser {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public final static String CONNECTION = "connection";
    public final static String CONNECTIONS = "connections";

    List<Connection> connections;
    XMLStreamReader xmlStreamReader;
    int event;
    String localName;
    String text;
    Map<String, String> attributes;
    Stack<String> elementStack = new Stack<String>();

    public static final Logger logger =
            LogUtil.getLogger(ConnectionsParser.class);

    public List<Connection> parse(String resourceName) throws Exception {
        connections = new ArrayList<Connection>();
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        ClassLoader cl = this.getClass().getClassLoader();
        InputStream input = cl.getResourceAsStream(resourceName);
        xmlStreamReader = inputFactory.createXMLStreamReader(input);
        next();
        doStartDocument();
        return connections;
    }

    private void doStartDocument()
            throws XMLStreamException {
        for (;;) {
            switch (event) {
                case XMLStreamConstants.END_DOCUMENT:
                    return;
                case XMLStreamConstants.START_ELEMENT:
                    if (CONNECTIONS.equals(localName)) {
                        next();
                        doConnections();
                    } else {
                        throw new Error("Unrecognized tag");
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    next();
                    return;
                case XMLStreamConstants.CHARACTERS:
                    next();
                    break;
                default:
                    throw new Error("Invalid XML");
            }
        }
    }

    private void doConnections() throws XMLStreamException {
        for (;;) {
            switch (event) {
                case XMLStreamConstants.START_ELEMENT:
                    if (CONNECTION.equals(localName)) {
                        doConnection();
                        next();
                    } else {
                        throw new Error("Unrecognized tag");
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    next();
                    return;
                case XMLStreamConstants.CHARACTERS:
                    next();
                    break;
                default:
                    throw new Error("Invalid XML");
            }
        }
    }

    private void doConnection() {
        checkRequiredAttributes("databaseName", "url", "driver", "username", "password");

        Connection connection = new Connection(
                attributes.get("databaseName"),
                attributes.get("type"),
                attributes.get("url"),
                attributes.get("driver"),
                attributes.get("username"),
                attributes.get("password")
        );
        connections.add(connection);
    }

    private void checkRequiredAttributes(String... attrNames) {
        for (String current : attrNames) {
            if (attributes.get(current) == null) {
                throw new Error("Attribute " + current + " required");
            }
        }
    }

    private void next() throws XMLStreamException {
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
                    throw new Error("Open/close tags don't match: " +
                            matchingElementName + "/" + localName);
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

    private void loadText() {
        text = xmlStreamReader.getText();
    }

    private void loadLocalName() {
        localName = xmlStreamReader.getLocalName();
    }

    private void loadAttributes() {
        attributes = new HashMap<String, String>();
        for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++) {
            String attrName = xmlStreamReader.getAttributeLocalName(i);
            String attrvalue = xmlStreamReader.getAttributeValue(i);
            attributes.put(attrName, attrvalue);
            LogUtil.fineMF(logger, "Attribute {0} = {1}", attrName, attrvalue);
        }
    }

}
