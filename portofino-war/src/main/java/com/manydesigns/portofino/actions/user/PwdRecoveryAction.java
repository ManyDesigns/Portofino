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
package com.manydesigns.portofino.actions.user;

import com.manydesigns.elements.logging.LogUtil;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.portofino.actions.PortofinoAction;
import com.manydesigns.portofino.systemModel.users.User;
import com.manydesigns.portofino.email.EmailHandler;

import java.util.logging.Logger;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class PwdRecoveryAction extends PortofinoAction implements LoginUnAware{
    public static final Logger logger =
        LogUtil.getLogger(PwdRecoveryAction.class);

    public String email;

    public String execute(){
        return INPUT;
    }

    public String send(){
        try {
            String url = "";
            User user = context.findUserByEmail(email);
            if (user==null){
                SessionMessages.addErrorMessage("email non esistente");
                return INPUT;
            }
            user.tokenGenerator();
        } catch (Exception e) {
            final String errore = "Errore nella verifica della email. " +
                    "L'email non è stata inviata";
            SessionMessages.addErrorMessage(
                    errore);
            LogUtil.warning(logger, errore, e);
            return INPUT;
        }
        EmailHandler.addEmail(context,"subject", "body",
                "giampiero.granatella@manydesigns.com", "giampiero.granatella@manydesigns.com");
        return SUCCESS;
    }
}
