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

package com.manydesigns.portofino.actions.model;

import com.manydesigns.elements.Mode;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.forms.TableForm;
import com.manydesigns.elements.forms.TableFormBuilder;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.text.OgnlTextFormat;
import com.manydesigns.portofino.actions.PortofinoAction;
import com.manydesigns.portofino.database.ConnectionProvider;
import com.manydesigns.portofino.database.JdbcConnectionProvider;
import com.manydesigns.portofino.database.Type;
import com.manydesigns.portofino.database.platforms.DatabasePlatform;
import com.manydesigns.portofino.database.platforms.DatabasePlatformsManager;

import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class ConnectionProvidersAction extends PortofinoAction {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public List<ConnectionProvider> connectionProviders;
    public ConnectionProvider connectionProvider;
    public DatabasePlatform[] databasePlatforms;
    public DatabasePlatform databasePlatform;
    public Type[] types;

    public TableForm tableForm;
    public Form form;
    public Form detectedValuesForm;
    public TableForm typesTableForm;
    public TableForm databasePlatformsTableForm;

    public String databaseName;

    public String execute() {
        if (databaseName == null) {
            return search();
        } else {
            return read();
        }
    }

    public String search() {
        connectionProviders = context.getConnectionProviders();

        OgnlTextFormat hrefFormat =
                OgnlTextFormat.create(
                        "/model/ConnectionProviders.action?" +
                                "databaseName=%{databaseName}");
        hrefFormat.setUrl(true);

        tableForm = new TableFormBuilder(ConnectionProvider.class)
                .configFields("databaseName", "description", "status")
                .configNRows(connectionProviders.size())
                .configHyperlinkGenerators("databaseName", hrefFormat, null)
                .configMode(Mode.VIEW)
                .build();
        tableForm.readFromObject(connectionProviders);

        // database platforms
        DatabasePlatformsManager manager = DatabasePlatformsManager.getManager();
        databasePlatforms = manager.getDatabasePlatforms();
        databasePlatformsTableForm =
                new TableFormBuilder(DatabasePlatform.class)
                        .configFields("description",
                                "standardDriverClassName",
                                "status")
                        .configNRows(databasePlatforms.length)
                        .configMode(Mode.VIEW)
                        .build();
        databasePlatformsTableForm.readFromObject(databasePlatforms);

        return LIST;
    }

    public String read() {
        connectionProvider = context.getConnectionProvider(databaseName);
        databasePlatform = connectionProvider.getDatabasePlatform();
        types = connectionProvider.getTypes();

        if (connectionProvider instanceof JdbcConnectionProvider) {
            form = new FormBuilder(JdbcConnectionProvider.class)
                    .configFields("databaseName", "driverClass",
                            "connectionURL", "username", "password",
                            "status", "errorMessage", "lastTested")
                    .build();
        } else {
            form = new FormBuilder(connectionProvider.getClass())
                    .configMode(Mode.VIEW)
                    .build();
        }
        form.readFromObject(connectionProvider);

        if (ConnectionProvider.STATUS_CONNECTED
                .equals(connectionProvider.getStatus())) {
            configureDetectedAndTypes();
        }

        return READ;
    }

    protected void configureDetectedAndTypes() {
        detectedValuesForm = new FormBuilder(JdbcConnectionProvider.class)
                .configFields(
                        "databaseProductName",
                        "databaseProductVersion",
                        "databaseMajorMinorVersion",
                        "driverName",
                        "driverVersion",
                        "driverMajorMinorVersion",
                        "JDBCMajorMinorVersion"
                        )
                .configMode(Mode.VIEW)
                .build();
        detectedValuesForm.readFromObject(connectionProvider);

        typesTableForm = new TableFormBuilder(Type.class)
                .configFields(
                        "typeName",
                        "jdbcType",
                        "autoincrement",
//                        "maximumPrecision",
//                        "literalPrefix",
//                        "literalSuffix",
                        "nullable",
                        "caseSensitive",
                        "searchable"
//                        "minimumScale",
//                        "maximumScale"
                )
                .configNRows(types.length)
                .configMode(Mode.VIEW)
                .build();
        typesTableForm.readFromObject(types);
    }

    public String returnToSearch() {
        return RETURN_TO_LIST;
    }

    public String test() {
        connectionProvider = context.getConnectionProvider(databaseName);
        connectionProvider.test();
        String status = connectionProvider.getStatus();
        if (ConnectionProvider.STATUS_CONNECTED.equals(status)) {
            SessionMessages.addInfoMessage("Connection tested successfully");
        } else {
            SessionMessages.addErrorMessage(
                    String.format("Connection failed. Status: %s. Error message: %s",
                            status, connectionProvider.getErrorMessage()));
        }
        return RETURN_TO_READ;
    }

}
