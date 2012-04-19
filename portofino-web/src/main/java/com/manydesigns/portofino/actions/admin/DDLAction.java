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

package com.manydesigns.portofino.actions.admin;

import com.manydesigns.portofino.ApplicationAttributes;
import com.manydesigns.portofino.RequestAttributes;
import com.manydesigns.portofino.dispatcher.AbstractActionBean;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.servlets.ServerInfo;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.model.database.DatabaseLogic;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.database.*;
import com.manydesigns.portofino.security.RequiresAdministrator;
import net.sourceforge.stripes.action.*;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@RequiresAdministrator
@UrlBinding("/actions/admin/model-definition")
public class DDLAction extends AbstractActionBean {

    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";
    //**************************************************************************
    // Injections
    //**************************************************************************

    @Inject(RequestAttributes.APPLICATION)
    Application application;

    @Inject(ApplicationAttributes.SERVER_INFO)
    ServerInfo serverInfo;

    @Inject(RequestAttributes.MODEL)
    Model model;

    //**************************************************************************
    // Parameters
    //**************************************************************************
    List<String[]> _tbls = new ArrayList<String[]>();
    String tableName;

    //**************************************************************************
    // Constant
    //**************************************************************************
    public static int tbsSize = 7;
    //**************************************************************************
    // Action default execute method
    //**************************************************************************

    @DefaultHandler
    public Resolution execute() {
        List<Database> databases = model.getDatabases();
        int line = 0;
        for (Database db : databases){
            String[] dbLine = new String[tbsSize];
            //id
            dbLine[0] = Integer.toString(line);
            //ancestor
            dbLine[1] = null;
            //Db Name
            dbLine[2] = db.getDatabaseName();
            //Db QualifiedName
            dbLine[3] = db.getQualifiedName();
            //Schema Name
            dbLine[4] = "";
            //Table Name
            dbLine[5] = "";
            //Table Qualified Name
            dbLine[6] = "";
            _tbls.add(dbLine);
            int anc = line;
            line++;
            List<Table> tables = db.getAllTables();

            for (Table tbl : tables){
                dbLine = new String[tbsSize];
                dbLine[0] = Integer.toString(line);
                dbLine[1] = Integer.toString(anc);
                dbLine[2] = db.getDatabaseName();
                dbLine[3] = db.getQualifiedName();
                dbLine[4] = tbl.getSchema().getSchemaName();
                dbLine[5] = tbl.getTableName();
                dbLine[6] = tbl.getQualifiedName();
                _tbls.add(dbLine);
                line ++;
            }
        }

        return new ForwardResolution("/layouts/admin/ddl/model-definition.jsp");
    }


    //**************************************************************************
    // Json Actions
    //**************************************************************************
    public Resolution getJsonColumns()
    {
        Table table = DatabaseLogic.findTableByQualifiedName(model, tableName);
        return new StreamingResolution("text", new StringReader(DDLJsonUtils
                .getColumns(table)));
    }

    public Resolution getJsonPk()
    {
        Table table = DatabaseLogic.findTableByQualifiedName(model, tableName);
        return new StreamingResolution("text", new StringReader(DDLJsonUtils
                .getPk(table)));
    }

    public Resolution getJsonFk()
    {
        Table table = DatabaseLogic.findTableByQualifiedName(model, tableName);
        return new StreamingResolution("text", new StringReader(DDLJsonUtils
                .getForeignKey(table)));
    }

    public Resolution getJsonAnnotations()
    {
        Table table = DatabaseLogic.findTableByQualifiedName(model, tableName);
        return new StreamingResolution("text", new StringReader(DDLJsonUtils
                .getAnnotations(table)));
    }

    //**************************************************************************
    // Getter and Setter
    //**************************************************************************

    public List<String[]> get_tbls() {
        return _tbls;
    }

    public void set_tbls(List<String[]> _tbls) {
        this._tbls = _tbls;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}
