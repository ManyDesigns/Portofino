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
import com.manydesigns.portofino.actions.UseCaseAction;
import com.manydesigns.portofino.system.model.users.Group;
import com.manydesigns.portofino.system.model.users.UserUtils;

import java.sql.Timestamp;
import java.text.MessageFormat;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class GroupAction extends UseCaseAction {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";




    //**************************************************************************
    // Delete
    //**************************************************************************

    public String delete() {
        Group pkGrp = new Group(Long.parseLong(rootCrudUnit.pk));
        Group aGroup = (Group) context.getObjectByPk(UserUtils.GROUPTABLE, pkGrp);
        aGroup.setDeletionDate(new Timestamp(System.currentTimeMillis()));
        context.saveObject(UserUtils.GROUPTABLE, aGroup);
        String databaseName = model.findTableByQualifiedName(UserUtils.GROUPTABLE)
                .getDatabaseName();
        context.commit(databaseName);
        SessionMessages.addInfoMessage("DELETE avvenuto con successo");
        return RETURN_TO_READ;
    }

    public String bulkDelete() {        
        if (rootCrudUnit.selection == null) {
            SessionMessages.addWarningMessage(
                    "DELETE non avvenuto: nessun oggetto selezionato");
            return CANCEL;
        }
        for (String current : rootCrudUnit.selection) {
            Group pkGrp = new Group(new Long(current));
            Group aGroup = (Group) context
                    .getObjectByPk(UserUtils.GROUPTABLE, pkGrp);
            aGroup.setDeletionDate(new Timestamp(System.currentTimeMillis()));
            context.saveObject(UserUtils.GROUPTABLE, aGroup);
            String databaseName = model
                    .findTableByQualifiedName(UserUtils.GROUPTABLE)
                    .getDatabaseName();
            SessionMessages.addInfoMessage("DELETE avvenuto con successo");
        }
        String databaseName = model
                .findTableByQualifiedName(UserUtils.GROUPTABLE)
                .getDatabaseName();
        context.commit(databaseName);
        SessionMessages.addInfoMessage(MessageFormat.format(
                "DELETE di {0} oggetti avvenuto con successo",
                rootCrudUnit.selection.length));
        return RETURN_TO_SEARCH;
    }


}
