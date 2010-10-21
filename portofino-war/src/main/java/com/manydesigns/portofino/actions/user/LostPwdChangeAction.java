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

import com.manydesigns.elements.Mode;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.logging.LogUtil;
import com.manydesigns.portofino.actions.PortofinoAction;
import com.manydesigns.portofino.system.model.users.User;
import org.apache.struts2.interceptor.ServletRequestAware;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.logging.Logger;
import java.sql.Timestamp;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class LostPwdChangeAction extends PortofinoAction 
        implements ServletRequestAware, LoginUnAware {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // ServletRequestAware implementation
    //**************************************************************************
    public HttpServletRequest req;
    public void setServletRequest(HttpServletRequest req) {
        this.req = req;
    }

    public Form form;
    public String token;

    public static final Logger logger =
            LogUtil.getLogger(LostPwdChangeAction.class);



    public String execute() {
        form =  new FormBuilder(LostPwdChangeFormBean.class)
                    .configMode(Mode.EDIT)
                    .build();
        return INPUT;
    }

    public String updatePwd() {
            LostPwdChangeFormBean pwd = new LostPwdChangeFormBean();
            form =  new FormBuilder(LostPwdChangeFormBean.class)
                    .configMode(Mode.EDIT)
                    .build();
            form.readFromRequest(req);
            form.writeToObject(pwd);

            if(form.validate()){
                User user = context.findUserByToken(token);
                user.setPwd(pwd.pwd);
                user.setPwdModDate(new Timestamp(new Date().getTime()));
                context.updateObject("portofino.public.user_", user);
                context.commit("portofino");
                LogUtil.finestMF(logger, "User {0} updated", user.getEmail());
                return SUCCESS;
            } else {
                return INPUT;
            }
    }
}