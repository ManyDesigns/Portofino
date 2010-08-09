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

package com.manydesigns.portofino.actions.upstairs;

import com.manydesigns.elements.Mode;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.forms.TableForm;
import com.manydesigns.elements.forms.TableFormBuilder;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.text.Generator;
import com.manydesigns.elements.text.HrefExpressionGenerator;
import com.manydesigns.portofino.actions.ActionResults;
import com.manydesigns.portofino.context.MDContext;
import com.manydesigns.portofino.database.ConnectionProvider;
import com.manydesigns.portofino.database.DatabaseAbstraction;
import com.manydesigns.portofino.database.JdbcConnectionProvider;
import com.manydesigns.portofino.database.Type;
import com.manydesigns.portofino.interceptors.MDContextAware;
import com.opensymphony.xwork2.ActionSupport;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class ConnectionProvidersAction
        extends ActionSupport implements MDContextAware {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public MDContext context;
    public List<ConnectionProvider> connectionProviders;
    public ConnectionProvider connectionProvider;
    public DatabaseAbstraction databaseAbstraction;
    public Type[] types;

    public TableForm tableForm;
    public Form form;
    public Form databaseAbstractionForm;
    public TableForm typesTableForm;

    public String databaseName;

    public String skin = "default";

    public void setContext(MDContext context) {
        this.context = context;
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

        Generator hrefGenerator =
                new HrefExpressionGenerator(ConnectionProvider.class,
                        "/upstairs/ConnectionProviders.action?" +
                                "databaseName={databaseName}");

        tableForm = new TableFormBuilder(ConnectionProvider.class)
                .configFields("databaseName", "description")
                .configNRows(connectionProviders.size())
                .configHyperlinkGenerators("databaseName", hrefGenerator, null)
                .build();
        tableForm.setMode(Mode.VIEW);
        tableForm.readFromObject(connectionProviders);

        return ActionResults.LIST;
    }

    public String read() {
        connectionProvider = context.getConnectionProvider(databaseName);
        databaseAbstraction = connectionProvider.getDatabaseAbstraction();
        types = databaseAbstraction.getTypes();
        Arrays.sort(types, new Comparator<Type>() {

            public int compare(Type o1, Type o2) {
                return o1.getTypeName().compareToIgnoreCase(o2.getTypeName());
            }
        });

        if (connectionProvider instanceof JdbcConnectionProvider) {
            form = new FormBuilder(JdbcConnectionProvider.class)
                    .configFields("databaseName", "driverClass",
                            "connectionURL", "username", "password")
                    .build();
        } else {
            form = new FormBuilder(connectionProvider.getClass())
                    .build();
        }
        form.setMode(Mode.VIEW);
        form.readFromObject(connectionProvider);

        databaseAbstractionForm = new FormBuilder(DatabaseAbstraction.class)
                .configFields(
                        "databaseProductName",
                        "databaseProductVersion",
                        "databaseMajorMinorVersion",
                        "driverClassName",
                        "driverName",
                        "driverVersion",
                        "driverMajorMinorVersion",
                        "JDBCMajorMinorVersion"
                        )
                .build();
        databaseAbstractionForm.setMode(Mode.VIEW);
        databaseAbstractionForm.readFromObject(databaseAbstraction);

        typesTableForm = new TableFormBuilder(Type.class)
                .configFields(
                        "typeName",
                        "jdbcType",
                        "autoincrement",
                        "maximumPrecision",
                        "literalPrefix",
                        "literalSuffix",
                        "nullable",
                        "caseSensitive",
                        "searchable",
                        "minimumScale",
                        "maximumScale"
                )
                .configNRows(types.length)
                .build();
        typesTableForm.setMode(Mode.VIEW);
        typesTableForm.readFromObject(types);

        return ActionResults.READ;
    }

    public String returnToSearch() {
        return ActionResults.RETURN_TO_LIST;
    }

    public String test() {
        SessionMessages.addInfoMessage("Connection tested successfully");
        return ActionResults.RETURN_TO_READ;
    }

}
