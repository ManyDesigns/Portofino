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

package com.manydesigns.portofino.actions.admin;

import com.manydesigns.elements.Mode;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.forms.TableForm;
import com.manydesigns.elements.forms.TableFormBuilder;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.text.OgnlTextFormat;
import com.manydesigns.portofino.actions.AbstractActionBean;
import com.manydesigns.portofino.actions.RequestAttributes;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.connections.ConnectionProvider;
import com.manydesigns.portofino.connections.JdbcConnectionProvider;
import com.manydesigns.portofino.connections.JndiConnectionProvider;
import com.manydesigns.portofino.database.platforms.DatabasePlatform;
import com.manydesigns.portofino.database.platforms.DatabasePlatformsManager;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.system.model.users.annotations.RequiresAdministrator;
import net.sourceforge.stripes.action.*;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@RequiresAdministrator
@UrlBinding("/admin/connection-providers.action")
public class ConnectionProvidersAction extends AbstractActionBean {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    public List<ConnectionProvider> connectionProviders;
    public ConnectionProvider connectionProvider;
    public DatabasePlatform[] databasePlatforms;
    public DatabasePlatform databasePlatform;

    public TableForm tableForm;
    public Form form;
    public Form jdbcForm;
    public Form jndiForm;
    public Form detectedValuesForm;
    public TableForm databasePlatformsTableForm;

    public String databaseName;
    public String connectionType;

    public String[] selection;

    //**************************************************************************
    // Injections
    //**************************************************************************

    @Inject(RequestAttributes.APPLICATION)
    public Application application;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(ConnectionProvidersAction.class);

    //--------------------------------------------------------------------------
    // Action events
    //--------------------------------------------------------------------------

    @DefaultHandler
    public Resolution execute() {
        if (databaseName == null) {
            return search();
        } else {
            return read();
        }
    }

    public Resolution search() {
        connectionProviders = application.getConnectionProviders();

        OgnlTextFormat hrefFormat =
                OgnlTextFormat.create(
                        "/admin/connection-providers.action?" +
                                "databaseName=%{databaseName}");
        hrefFormat.setUrl(true);

        tableForm = new TableFormBuilder(ConnectionProvider.class)
                .configFields("databaseName", "description", "status")
                .configNRows(connectionProviders.size())
                .configHrefTextFormat("databaseName", hrefFormat)
                .configMode(Mode.VIEW)
                .build();
        tableForm.setSelectable(true);
        tableForm.setKeyGenerator(OgnlTextFormat.create("%{databaseName}"));

        tableForm.readFromObject(connectionProviders);

        // database platforms
        DatabasePlatformsManager manager =
                application.getDatabasePlatformsManager();
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

        return new ForwardResolution("/layouts/admin/connectionProviders/list.jsp");
    }

    public Resolution read() {
        connectionProvider = application.getConnectionProvider(databaseName);
        databasePlatform = connectionProvider.getDatabasePlatform();

        buildConnectionProviderForm(Mode.VIEW);
        form.readFromObject(connectionProvider);

        if (ConnectionProvider.STATUS_CONNECTED
                .equals(connectionProvider.getStatus())) {
            configureDetected();
        }

        return new ForwardResolution("/layouts/admin/connectionProviders/read.jsp");
    }

    public final static String[] jdbcViewFields = {"databaseName", "driver",
                            "url", "username", "password",
                            "includeSchemas", "excludeSchemas",
                            "status", "errorMessage", "lastTested"};

    public final static String[] jdbcEditFields = {"databaseName", "driver",
                            "url", "username", "password",
                            "includeSchemas", "excludeSchemas"};

    public final static String[] jndiViewFields = {"databaseName", "jndiResource",
                            "includeSchemas", "excludeSchemas",
                            "status", "errorMessage", "lastTested"};

    public final static String[] jndiEditFields = {"databaseName", "jndiResource",
                            "includeSchemas", "excludeSchemas"};

    private void buildConnectionProviderForm(Mode mode) {
        String [] fields;
        if (connectionProvider instanceof JdbcConnectionProvider) {
            fields = (mode == Mode.VIEW)
                    ? jdbcViewFields
                    : jdbcEditFields;
        } else if (connectionProvider instanceof JndiConnectionProvider) {
            fields = (mode == Mode.VIEW)
                    ? jndiViewFields
                    : jndiEditFields;
        } else {
            throw new InternalError("Unknown connection provider type: " +
                    connectionProvider.getClass().getName());
        }
        form = new FormBuilder(connectionProvider.getClass())
                    .configFields(fields)
                    .configMode(mode)
                    .build();
    }

    protected void configureDetected() {
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
    }

    public Resolution test() {
        connectionProvider = application.getConnectionProvider(databaseName);
        connectionProvider.init(application.getDatabasePlatformsManager());
        String status = connectionProvider.getStatus();
        if (ConnectionProvider.STATUS_CONNECTED.equals(status)) {
            SessionMessages.addInfoMessage("Connection tested successfully");
        } else {
            SessionMessages.addErrorMessage(
                    String.format("Connection failed. Status: %s. Error message: %s",
                            status, connectionProvider.getErrorMessage()));
        }
        return new RedirectResolution(this.getClass())
                .addParameter("databaseName", databaseName);
    }

    public Resolution create() {
        if (!createConnectionProvider()) {
            return new ForwardResolution("/layouts/admin/connectionProviders/createSelectType.jsp");
        }
        buildConnectionProviderForm(Mode.CREATE);
        return new ForwardResolution("/layouts/admin/connectionProviders/create.jsp");
    }

    protected boolean createConnectionProvider() {
        if("JDBC".equals(connectionType)) {
            connectionProvider = new JdbcConnectionProvider();
        } else if("JNDI".equals(connectionType)) {
            connectionProvider = new JndiConnectionProvider();
        } else {
            return false;
        }
        return true;
    }

    public Resolution save() {
        if (!createConnectionProvider()) {
            return new ForwardResolution("/layouts/admin/connectionProviders/createSelectType.jsp");
        }
        buildConnectionProviderForm(Mode.CREATE);
        
        form.readFromRequest(context.getRequest());
        if (form.validate()) {
            form.writeToObject(connectionProvider);
            //TODO check for duplicate database names
            application.addConnectionProvider(connectionProvider);
            connectionProvider.init(application.getDatabasePlatformsManager());
            SessionMessages.addInfoMessage("Connection provider created successfully");
            return new RedirectResolution(this.getClass());
        } else {
            return new ForwardResolution("/layouts/admin/connectionProviders/create.jsp");
        }
    }

    public Resolution edit() {
        connectionProvider = application.getConnectionProvider(databaseName);
        databasePlatform = connectionProvider.getDatabasePlatform();

        buildConnectionProviderForm(Mode.EDIT);
        form.readFromObject(connectionProvider);

        return new ForwardResolution("/layouts/admin/connectionProviders/edit.jsp");
    }

    public Resolution update() {
        connectionProvider = application.getConnectionProvider(databaseName);
        databasePlatform = connectionProvider.getDatabasePlatform();

        buildConnectionProviderForm(Mode.EDIT);
        form.readFromObject(connectionProvider);

        form.readFromRequest(context.getRequest());
        if (form.validate()) {            
            form.writeToObject(connectionProvider);
            application.updateConnectionProvider(connectionProvider);
            connectionProvider.init(application.getDatabasePlatformsManager());
            SessionMessages.addInfoMessage("Connection provider updated successfully");
        }
        return new RedirectResolution(this.getClass())
                .addParameter("databaseName", databaseName);
    }

    public Resolution delete(){
        if(null!=databaseName){
            application.deleteConnectionProvider(databaseName);
            SessionMessages.addInfoMessage("Connection providers deleted successfully");
        }
        return new RedirectResolution(this.getClass());
    }

    public Resolution bulkDelete() {

        if(null!=selection && 0!=selection.length){
            application.deleteConnectionProvider(selection);
            SessionMessages.addInfoMessage("Connection providers deleted successfully");
        } else {
            SessionMessages.addInfoMessage("No Connection providers selected");
        }
        return new RedirectResolution(this.getClass());
    }

    public Resolution sync() {
        try {
            application.syncDataModel(databaseName);
        } catch (Exception e) {
            logger.error("Errore in sincronizzazione", e);
            SessionMessages.addErrorMessage("Errore in sincronizzazione: " + ExceptionUtils.getRootCauseMessage(e));
        }
        return new RedirectResolution(getClass()).addParameter("databaseName", databaseName);
    }

    public Resolution returnToList() {
        return new RedirectResolution(ConnectionProvidersAction.class);
    }

    public Resolution returnToPages() {
        return new RedirectResolution("/");
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public Form getDetectedValuesForm() {
        return detectedValuesForm;
    }

    public String getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }
}
