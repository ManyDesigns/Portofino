/*
 * Copyright (C) 2005-2011 ManyDesigns srl.  All rights reserved.
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
package com.manydesigns.portofino.application;

import com.manydesigns.portofino.AbstractPortofinoTest;
import com.manydesigns.portofino.model.database.Database;
import com.manydesigns.portofino.model.database.JdbcConnectionProvider;
import org.apache.commons.io.IOUtils;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class ConnectionProvidersTest extends AbstractPortofinoTest {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    @Override
    public void setUp() throws Exception {
        super.setUp();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();    //To change body of overridden methods use File | Settings | File Templates.
    }

    public void testConnectionProvider() throws IOException, JAXBException {
        List<Database> databases = model.getDatabases();
        assertEquals(3, databases.size());

        JdbcConnectionProvider conn = new JdbcConnectionProvider();
        conn.setDriver("org.h2.Driver");
        conn.setUrl("jdbc:h2:mem:test");
        conn.setUsername("manydesigns");
        conn.setPassword("manydesigns");
        Database database = new Database();
        database.setDatabaseName("test");
        database.setConnectionProvider(conn);
        conn.setDatabase(database);

        databases.add(database);
        application.saveXmlModel();

//        File connectionsFile = application.getAppConnectionsFile();
        File connectionsFile = null;

        assertEquals(4, databases.size());
        InputStream is = null;
        try {
            is = new FileInputStream(connectionsFile);
            String file = convertStreamToString(is);
            assertTrue(file.contains("<jdbcConnection driver=\"org.h2.Driver\" password=\"manydesigns\" url=\"jdbc:h2:mem:test\" username=\"manydesigns\" databaseName=\"test\"/>"));
        } finally {
            IOUtils.closeQuietly(is);
        }

        conn = new JdbcConnectionProvider();
        conn.setDriver("org.h2.Driver");
        conn.setUrl("jdbc:h2:mem:test2");
        conn.setUsername("manydesigns2");
        conn.setPassword("manydesigns2");

        databases.remove(database);
        application.saveXmlModel();
        assertEquals(3, databases.size());

        database = new Database();
        database.setDatabaseName("test");
        database.setConnectionProvider(conn);
        conn.setDatabase(database);

        databases.add(database);
        application.saveXmlModel();
        assertEquals(4, databases.size());
        is = null;
        try {
            is = new FileInputStream(connectionsFile);
            String file = convertStreamToString(is);
            assertTrue(file.contains("<jdbcConnection driver=\"org.h2.Driver\" password=\"manydesigns2\" url=\"jdbc:h2:mem:test2\" username=\"manydesigns2\" databaseName=\"test\"/>"));
        } finally {
            IOUtils.closeQuietly(is);
        }

        databases.remove(database);
        application.saveXmlModel();
        assertEquals(3, databases.size());
        is = null;
        try {
            is = new FileInputStream(connectionsFile);
            String file = convertStreamToString(is);
            assertFalse(file.contains("<jdbcConnection driver=\"org.h2.Driver\" password=\"manydesigns\" url=\"jdbc:h2:mem:test\" username=\"manydesigns\" databaseName=\"test\"/>"));
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    private String convertStreamToString(InputStream is)
            throws IOException {
        if (is != null) {
            Writer writer = new StringWriter();

            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(
                        new InputStreamReader(is, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                is.close();
            }
            return writer.toString();
        } else {
            return "";
        }
    }
}
