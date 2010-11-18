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

import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.portofino.actions.PortofinoAction;
import com.manydesigns.portofino.database.ConnectionProvider;
import com.manydesigns.portofino.model.datamodel.Database;
import com.manydesigns.portofino.model.diff.DatabaseDiff;
import com.manydesigns.portofino.model.diff.DiffUtil;
import com.manydesigns.portofino.model.io.ModelWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.MessageFormat;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class SelfTestAction extends PortofinoAction {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public TreeTableDiffer treeTableDiffer;
    // result parameters
    public InputStream inputStream;
    public String contentType;
    public String contentDisposition;

    public String execute() throws SQLException {
        treeTableDiffer = new TreeTableDiffer();
        model = context.getModel();
        for (ConnectionProvider current : context.getConnectionProviders()) {
            Database sourceDatabase = current.readModel();

            Database targetDatabase =
                    model.findDatabaseByName(current.getDatabaseName());

            DatabaseDiff diff =
                    DiffUtil.diff(sourceDatabase, targetDatabase);

            treeTableDiffer.diffDatabase(diff);
        }
        return SUCCESS;
    }

    public String sync() throws SQLException {
        context.syncDataModel();
        SessionMessages.addInfoMessage("In-memory model synchronized to database model");
        return "sync";
    }


    public String export() throws IOException {
        contentType= "text/xml";
        contentDisposition= MessageFormat.format("inline; filename={0}.xml",
                    "datamodel");
        ModelWriter writer = new ModelWriter();
        File tempFile = File.createTempFile("portofino", ".xml");
        writer.write(model, tempFile);
        inputStream = new FileInputStream(tempFile);

        return "export";
    }

}
