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

package com.manydesigns.portofino.actions.user;

import com.manydesigns.elements.Mode;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.SessionAttributes;
import com.manydesigns.portofino.actions.AbstractActionBean;
import com.manydesigns.portofino.actions.PortofinoAction;
import com.manydesigns.portofino.annotations.InjectApplication;
import com.manydesigns.portofino.annotations.InjectHttpRequest;
import com.manydesigns.portofino.annotations.InjectHttpSession;
import com.manydesigns.portofino.annotations.InjectPortofinoProperties;
import com.manydesigns.portofino.context.Application;
import com.manydesigns.portofino.system.model.users.Group;
import com.manydesigns.portofino.system.model.users.User;
import com.manydesigns.portofino.system.model.users.UserUtils;
import com.manydesigns.portofino.system.model.users.UsersGroups;
import net.sourceforge.stripes.action.Before;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class ProfileAction extends AbstractActionBean {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    public static final String CHANGE_PWD = "changePwd";
    public static final String UPDATE_PWD = "updatePwd";

    //**************************************************************************
    // Injections
    //**************************************************************************

    @InjectApplication
    public Application application;

    @InjectHttpRequest
    public HttpServletRequest req;

    @InjectHttpSession
    public HttpSession session;

    @InjectPortofinoProperties
    public Configuration portofinoConfiguration;

    public List<Group> groups;

    private Boolean enc;
    private Long userId;

    @Before
    public void prepare() {
        groups = new ArrayList<Group>();
        enc = portofinoConfiguration.getBoolean(
                PortofinoProperties.PWD_ENCRYPTED, false);
    }

    //**************************************************************************
    // User
    //**************************************************************************
    public Form form;

    public static final Logger logger =
            LoggerFactory.getLogger(ProfileAction.class);

    public String execute() {
        userId = (Long) session.getAttribute(SessionAttributes.USER_ID);
        return read();
    }

    private String read() {
        User thisUser =
            (User) application.getObjectByPk(UserUtils.USERTABLE, new User(userId));
        ClassAccessor accessor = application.getTableAccessor(UserUtils.USERTABLE);
        FormBuilder formBuilder = new FormBuilder(accessor);
        formBuilder.configFields("email", "userName", "firstName",
                "middleName", "lastName", "creationDate");
        for (UsersGroups ug : thisUser.getGroups()){
            Group grp = ug.getGroup();

            if(ug.getDeletionDate()==null){
                groups.add(grp);
            }
        }
        form = formBuilder
                .configMode(Mode.VIEW)
                .build();
        form.readFromObject(thisUser);
        return PortofinoAction.READ;
    }

    public String edit() {
        userId = (Long) session.getAttribute(SessionAttributes.USER_ID);
        User thisUser =
            (User) application.getObjectByPk(UserUtils.USERTABLE, new User(userId));

        ClassAccessor accessor = application.getTableAccessor(UserUtils.USERTABLE);
        FormBuilder formBuilder = new FormBuilder(accessor);
        form = formBuilder
                .configFields("email", "userName", "firstName",
                        "middleName", "lastName")
                .configMode(Mode.EDIT)
                .build();
        form.readFromObject(thisUser);
        return PortofinoAction.EDIT;
    }

    public String update() {
        userId = (Long) session.getAttribute(SessionAttributes.USER_ID);
        User thisUser =
            (User) application.getObjectByPk(UserUtils.USERTABLE, new User(userId));
        ClassAccessor accessor = application.getTableAccessor(UserUtils.USERTABLE);
        FormBuilder formBuilder = new FormBuilder(accessor);
        form = formBuilder
                .configFields("email", "userName", "firstName",
                        "middleName", "lastName")
                .configMode(Mode.EDIT)
                .build();
        form.readFromObject(thisUser);
        form.readFromRequest(req);
        
        if(form.validate()){
            form.writeToObject(thisUser);
            application.updateObject(UserUtils.USERTABLE, thisUser);
            application.commit("portofino");
            logger.debug("User {} updated", thisUser.getEmail());
            SessionMessages.addInfoMessage("Utente aggiornato correttamente");
            return PortofinoAction.UPDATE;
        } else {
            return PortofinoAction.EDIT;
        }
    }

    public String changePwd() {
        userId = (Long) session.getAttribute(SessionAttributes.USER_ID);
        form = new FormBuilder(ChangePasswordFormBean.class).configFields("oldPwd", "pwd")
                .configMode(Mode.EDIT)
                .build();
        return CHANGE_PWD;
    }

    public String updatePwd() {
        userId = (Long) session.getAttribute(SessionAttributes.USER_ID);
        User thisUser =
            (User) application.getObjectByPk(UserUtils.USERTABLE, new User(userId));

        form = new FormBuilder(ChangePasswordFormBean.class).configFields("oldPwd", "pwd")
                .configMode(Mode.EDIT)
                .build();
        form.readFromRequest(req);

        if(form.validate()) {
            ChangePasswordFormBean bean = new ChangePasswordFormBean();
            form.writeToObject(bean);

            String encOldPwd;
            if (portofinoConfiguration.getBoolean(PortofinoProperties.PWD_ENCRYPTED, false)){
                try {
                    MessageDigest md = MessageDigest.getInstance("SHA-1");
                    md.update(bean.oldPwd.getBytes("UTF-8"));
                    byte raw[] = md.digest();
                    encOldPwd = (new BASE64Encoder()).encode(raw);
                } catch (Exception e) {
                    throw new Error(e);
                }
            } else {
                encOldPwd = bean.oldPwd;
            }

            if(encOldPwd.equals(thisUser.getPwd())) {
                thisUser.setPwd(bean.pwd);
                if (enc) {
                    thisUser.setPwd(UserUtils.encryptPassword(bean.pwd));
                } else {
                    thisUser.setPwd(bean.pwd);
                }
                thisUser.setPwdModDate(new Timestamp(new Date().getTime()));
                application.updateObject(UserUtils.USERTABLE, thisUser);
                application.commit("portofino");

                logger.debug("User {} updated", thisUser.getEmail());
                SessionMessages.addInfoMessage("Password correctely updated");

                return UPDATE_PWD;
            } else {
                SessionMessages.addErrorMessage
                        ("La password non corrisponde a quella in uso");
                return CHANGE_PWD;
            }
        } else {
            return CHANGE_PWD;
        }
    }
}
