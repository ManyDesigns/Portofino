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
import com.manydesigns.elements.fields.Field;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.forms.TableForm;
import com.manydesigns.elements.forms.TableFormBuilder;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.JavaClassAccessor;
import com.manydesigns.elements.text.OgnlTextFormat;
import com.manydesigns.portofino.actions.PortofinoAction;
import com.manydesigns.portofino.model.connections.ConnectionProvider;
import com.manydesigns.portofino.model.connections.JdbcConnectionProvider;
import com.manydesigns.portofino.database.Type;
import com.manydesigns.portofino.database.platforms.DatabasePlatform;
import com.manydesigns.portofino.database.platforms.DatabasePlatformsManager;
import com.manydesigns.portofino.model.connections.JndiConnectionProvider;
import org.apache.struts2.interceptor.ServletRequestAware;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class ConnectionProvidersAction extends PortofinoAction implements ServletRequestAware{
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public List<ConnectionProvider> connectionProviders;
    public ConnectionProvider connectionProvider;
    public DatabasePlatform[] databasePlatforms;
    public DatabasePlatform databasePlatform;
    public Type[] types;

    public TableForm tableForm;
    public Form form;
    public Form jdbcForm;
    public Form jndiForm;
    public Form detectedValuesForm;
    public TableForm typesTableForm;
    public TableForm databasePlatformsTableForm;

    public String databaseName;
    public String connectionType;

    public String[] selection;

    //**************************************************************************
    // Request aware
    //**************************************************************************
    private HttpServletRequest req;
    public void setServletRequest(HttpServletRequest req) {
        this.req = req;
    }

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
                .configHrefTextFormat("databaseName", hrefFormat)
                .configMode(Mode.VIEW)
                .build();
        tableForm.setSelectable(true);
        tableForm.setKeyGenerator(OgnlTextFormat.create("%{databaseName}"));

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
                    .configFields("databaseName", "driver",
                            "url", "username", "password",
                            "includeSchemas", "excludeSchemas",
                            "status", "errorMessage", "lastTested")
                    .configMode(Mode.VIEW)
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
        connectionProvider.init();
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

    public String create() {
        setupForm(Mode.CREATE);
        return CREATE;
    }

    public String save() {
        ClassAccessor accessor;
        FormBuilder formBuilder;
        Form form;

        if ("jdbc".equals(connectionType)){
            accessor = JavaClassAccessor.getClassAccessor(JdbcConnectionProvider.class);
            formBuilder = new FormBuilder(accessor);
            formBuilder.configPrefix("jdbc_");
            form = formBuilder
                .configFields("databaseName", "driver", "url", "username", "password",
                        "includeSchemas", "excludeSchemas")
                .configPrefix("jdbc_")
                .configMode(Mode.CREATE)
                .build();
        } else if ("jndi".equals(connectionType)) {
            accessor = JavaClassAccessor.getClassAccessor(JndiConnectionProvider.class);
            formBuilder = new FormBuilder(accessor);
            formBuilder.configPrefix("jndi_");
            form = formBuilder
                .configFields("databaseName", "jndiResource",
                        "includeSchemas", "excludeSchemas")
                .configPrefix("jndi_")
                .configMode(Mode.CREATE)
                .build();
        } else {
            return PortofinoAction.CREATE;
        }
        
        form.readFromRequest(req);
        if (form.validate()) {
            ConnectionProvider object = (ConnectionProvider) accessor.newInstance();
            form.writeToObject(object);
            context.addConnectionProvider(object);
            SessionMessages.addInfoMessage("Connection provider created");
            return PortofinoAction.SAVE;
        } else {
            return PortofinoAction.CREATE;
        }
    }

    public String edit() {
        ClassAccessor accessor;
        FormBuilder formBuilder;
        ConnectionProvider connectionProvider =
                context.getConnectionProvider(databaseName);

        if (connectionProvider instanceof JdbcConnectionProvider){
            accessor = JavaClassAccessor.getClassAccessor(JdbcConnectionProvider.class);
            formBuilder = new FormBuilder(accessor);
            form = formBuilder
                .configFields("databaseName", "driver", "url", "username", "password",
                        "includeSchemas", "excludeSchemas")
                .configMode(Mode.EDIT)
                .build();
        } else {
            accessor = JavaClassAccessor.getClassAccessor(JndiConnectionProvider.class);
            formBuilder = new FormBuilder(accessor);
            form = formBuilder
                .configFields("databaseName", "jndiResource",
                        "includeSchemas", "excludeSchemas")
                .configMode(Mode.EDIT)
                .build();
        }

        form.readFromObject(connectionProvider);
        return EDIT;
    }

    public String update() {
        ClassAccessor accessor;
        FormBuilder formBuilder;
        Form form;
        ConnectionProvider connectionProvider =
                context.getConnectionProvider(databaseName);
        ConnectionProvider object;


        if (connectionProvider instanceof JdbcConnectionProvider){
            accessor = JavaClassAccessor.getClassAccessor(JdbcConnectionProvider.class);
            formBuilder = new FormBuilder(accessor);
            form = formBuilder
                .configFields("databaseName", "driver", "url", "username", "password",
                        "includeSchemas", "excludeSchemas")
                .configMode(Mode.CREATE)
                .build();
            object = (ConnectionProvider) accessor.newInstance();
        } else {
            accessor = JavaClassAccessor.getClassAccessor(JndiConnectionProvider.class);
            formBuilder = new FormBuilder(accessor);

            form = formBuilder
                .configFields("databaseName", "jndiResource",
                        "includeSchemas", "excludeSchemas")
                 .configMode(Mode.CREATE)
                .build();
            object = (ConnectionProvider) accessor.newInstance();
        }

        form.readFromRequest(req);
        if (form.validate()) {            
            form.writeToObject(object);
            context.updateConnectionProvider(object);
            SessionMessages.addInfoMessage("Connection provider updated");
            return PortofinoAction.RETURN_TO_LIST;
        } else {
            return PortofinoAction.EDIT;
        }
    }

    public String delete(){
        if(null!=databaseName){
            context.deleteConnectionProvider(databaseName);
            SessionMessages.addInfoMessage("Connection providers deleted");
        } 
        return RETURN_TO_LIST;
    }

    public String bulkDelete(){

        if(null!=selection && 0!=selection.length){
            context.deleteConnectionProvider(selection);
            SessionMessages.addInfoMessage("Connection providers deleted");
        } else {
            SessionMessages.addInfoMessage("No Connection providers selected");
        }
        return RETURN_TO_LIST;
    }

    public String cancel() {
        return RETURN_TO_LIST;        
    }

    private void setupForm(Mode mode) {
        ClassAccessor jdbcClassAccessor =
                JavaClassAccessor.getClassAccessor(JdbcConnectionProvider.class);
        ClassAccessor jndiClassAccessor =
                JavaClassAccessor.getClassAccessor(JndiConnectionProvider.class);
        FormBuilder formBuilder = new FormBuilder(jdbcClassAccessor);

        jdbcForm = formBuilder
                .configFields("databaseName", "driver", "url", "username", "password",
                        "includeSchemas", "excludeSchemas")
                .configPrefix("jdbc_")
                .configMode(mode)
                .build();

        for (Field field : jdbcForm.get(0))
        {
            field.setRequired(true);
        }

        formBuilder = new FormBuilder(jndiClassAccessor);
        jndiForm = formBuilder
                .configFields("databaseName", "jndiResource",
                        "includeSchemas", "excludeSchemas")
                .configPrefix("jndi_")
                .configMode(mode)
                .build();

        for (Field field : jndiForm.get(0))
        {
            field.setRequired(true);
        }
    }
}
