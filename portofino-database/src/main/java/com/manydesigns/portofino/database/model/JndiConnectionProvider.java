/*
 * Copyright (C) 2005-2020 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.manydesigns.portofino.database.model;

import com.manydesigns.elements.annotations.Required;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
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
            "Copyright (C) 2005-2020 ManyDesigns srl";

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
        DataSource ds = getDataSource();
        return ds.getConnection();
    }

    public DataSource getDataSource() throws Exception {
        InitialContext ic = new InitialContext();
        return (DataSource) ic.lookup(jndiResource);
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
