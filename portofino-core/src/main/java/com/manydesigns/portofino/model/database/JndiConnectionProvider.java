/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.manydesigns.portofino.model.database;

import com.manydesigns.elements.annotations.Required;
import org.apache.commons.dbutils.DbUtils;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import java.sql.Connection;
import java.text.MessageFormat;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@XmlAccessorType(XmlAccessType.NONE)
public class JndiConnectionProvider extends ConnectionProvider {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    private String jndiResource;


    //**************************************************************************
    // Constructors
    //**************************************************************************

    public JndiConnectionProvider() {
        super();
    }

    //**************************************************************************
    // Implementation of ConnectionProvider
    //**************************************************************************

    public String getDescription() {
        return MessageFormat.format("JNDI data source: {0}", jndiResource);
    }

    public Connection acquireConnection() throws Exception {
        InitialContext ic = new InitialContext();
        DataSource ds = (DataSource) ic.lookup(jndiResource);
        return ds.getConnection();
    }

    public void releaseConnection(Connection conn) {
        DbUtils.closeQuietly(conn);
    }

    //**************************************************************************
    // Getters/setters
    //**************************************************************************

    @XmlAttribute(required = true)
    @Required
    public String getJndiResource() {
        return jndiResource;
    }

    public void setJndiResource(String jndiResource) {
        this.jndiResource = jndiResource;
    }
}
