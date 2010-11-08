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
package com.manydesigns.portofino.actions.user.admin;

import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.portofino.actions.model.TableDataAction;
import com.manydesigns.portofino.system.model.users.Group;

import java.sql.Timestamp;
import java.text.MessageFormat;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class GroupAction extends TableDataAction {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";



    //**************************************************************************
    // Delete
    //**************************************************************************

    public String delete() {
        setupMetadata();
        Group pkGrp = new Group(new Long(pk));
        Group aGroup = (Group) context.getObjectByPk(qualifiedName, pkGrp);
        aGroup.setDeletionDate(new Timestamp(System.currentTimeMillis()));
        context.saveObject(qualifiedName, aGroup);
        String databaseName = model.findTableByQualifiedName(qualifiedName)
                .getDatabaseName();
        context.commit(databaseName);
        SessionMessages.addInfoMessage("DELETE avvenuto con successo");
        return DELETE;
    }

    public String bulkDelete() {
        setupMetadata();
        if (selection == null) {
            SessionMessages.addWarningMessage(
                    "DELETE non avvenuto: nessun oggetto selezionato");
            return CANCEL;
        }
        for (String current : selection) {Group pkGrp = new Group(new Long(current));

            Group aGroup = (Group) context.getObjectByPk(qualifiedName, pkGrp);
            aGroup.setDeletionDate(new Timestamp(System.currentTimeMillis()));
            context.saveObject(qualifiedName, aGroup);
            String databaseName = model.findTableByQualifiedName(qualifiedName)
                    .getDatabaseName();
            context.commit(databaseName);
            SessionMessages.addInfoMessage("DELETE avvenuto con successo");
        }
        String databaseName = model.findTableByQualifiedName(qualifiedName)
                .getDatabaseName();
        context.commit(databaseName);
        SessionMessages.addInfoMessage(MessageFormat.format(
                "DELETE di {0} oggetti avvenuto con successo", selection.length));
        return DELETE;
    }


}
