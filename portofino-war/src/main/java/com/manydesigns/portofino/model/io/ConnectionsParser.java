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
import com.manydesigns.elements.util.ReflectionUtil;
import com.manydesigns.portofino.database.ConnectionProvider;
import com.manydesigns.portofino.database.JdbcConnectionProvider;
import com.manydesigns.portofino.xml.DocumentCallback;
import com.manydesigns.portofino.xml.ElementCallback;
import com.manydesigns.portofino.xml.XmlParser;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class ConnectionsParser extends XmlParser {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public final static String CONNECTION = "connection";
    public final static String CONNECTIONS = "connections";

    List<ConnectionProvider> connections;

    public static final Logger logger =
            LogUtil.getLogger(ConnectionsParser.class);

    public List<ConnectionProvider> parse(String resourceName) throws Exception {
        InputStream inputStream = ReflectionUtil.getResourceAsStream(resourceName);
        return parse(inputStream);
    }

    public List<ConnectionProvider> parse(File file) throws Exception {
        LogUtil.infoMF(logger, "Parsing file: {0}", file.getAbsolutePath());
        InputStream input = new FileInputStream(file);
        return parse(input);
    }


    public List<ConnectionProvider> parse(InputStream inputStream)
            throws XMLStreamException {
        connections = new ArrayList<ConnectionProvider>();
        initParser(inputStream);
        expectDocument(new ConnectionsDocumentCallback());
        return connections;
    }

    private class ConnectionsDocumentCallback implements DocumentCallback {
        public void doDocument() throws XMLStreamException {
            expectElement(CONNECTIONS, 1, 1, new ConnectionsCallback());
        }
    }

    private class ConnectionsCallback implements ElementCallback {
        public void doElement(Map<String, String> attributes)
                throws XMLStreamException {
            expectElement(CONNECTION, 1, null, new ConnectionCallback());
        }
    }

    private class ConnectionCallback implements ElementCallback {
        public void doElement(Map<String, String> attributes) {
            checkRequiredAttributes(attributes,
                    "databaseName", "url", "driver", "username", "password");

            ConnectionProvider connection = new JdbcConnectionProvider(
                    attributes.get("databaseName"),
                    attributes.get("driver"),
                    attributes.get("url"),
                    attributes.get("username"),
                    attributes.get("password")
            );
            connections.add(connection);

        }
    }

}
