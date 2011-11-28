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
import com.manydesigns.portofino.ApplicationAttributes;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.SessionAttributes;
import com.manydesigns.portofino.actions.AbstractActionBean;
import com.manydesigns.portofino.actions.RequestAttributes;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.database.QueryUtils;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.dispatcher.Dispatch;
import com.manydesigns.portofino.logic.SecurityLogic;
import com.manydesigns.portofino.system.model.users.Group;
import com.manydesigns.portofino.system.model.users.User;
import com.manydesigns.portofino.system.model.users.UsersGroups;
import net.sourceforge.stripes.action.*;
import org.apache.commons.configuration.Configuration;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Encoder;

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
@UrlBinding("/actions/profile")
public class ProfileAction extends AbstractActionBean {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    //**************************************************************************
    // Injections
    //**************************************************************************

    @Inject(RequestAttributes.APPLICATION)
    public Application application;

    @Inject(ApplicationAttributes.PORTOFINO_CONFIGURATION)
    public Configuration portofinoConfiguration;

    @Inject(RequestAttributes.DISPATCH)
    public Dispatch dispatch;

    public List<Group> groups;

    private Boolean enc;
    private String userId;

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

    @DefaultHandler
    public Resolution execute() {
        HttpSession session = getSession();
        userId = (String) session.getAttribute(SessionAttributes.USER_ID);
        return read();
    }

    private Resolution read() {
        User thisUser =
            (User) QueryUtils.getObjectByPk
                    (application, "portofino", SecurityLogic.USER_ENTITY_NAME, new User(userId));
        ClassAccessor accessor = application.getTableAccessor(SecurityLogic.USERTABLE);
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
        return new ForwardResolution("/layouts/user/profile/read.jsp");
    }

    public Resolution edit() {
        userId = (String) getSession().getAttribute(SessionAttributes.USER_ID);
        User thisUser =
            (User) QueryUtils.getObjectByPk
                (application, "portofino", SecurityLogic.USER_ENTITY_NAME, new User(userId));

        ClassAccessor accessor = application.getTableAccessor(SecurityLogic.USERTABLE);
        FormBuilder formBuilder = new FormBuilder(accessor);
        form = formBuilder
                .configFields("email", "userName", "firstName",
                        "middleName", "lastName")
                .configMode(Mode.EDIT)
                .build();
        form.readFromObject(thisUser);
        return new ForwardResolution("/layouts/user/profile/edit.jsp");
    }

    public Resolution update() {
        userId = (String) getSession().getAttribute(SessionAttributes.USER_ID);
        User thisUser =
            (User) QueryUtils.getObjectByPk
                (application, "portofino", SecurityLogic.USER_ENTITY_NAME, new User(userId));
        ClassAccessor accessor = application.getTableAccessor(SecurityLogic.USERTABLE);
        FormBuilder formBuilder = new FormBuilder(accessor);
        form = formBuilder
                .configFields("email", "userName", "firstName",
                        "middleName", "lastName")
                .configMode(Mode.EDIT)
                .build();
        form.readFromObject(thisUser);
        form.readFromRequest(context.getRequest());
        
        if(form.validate()){
            form.writeToObject(thisUser);
            Session session = application.getSession("portofino");
            session.update(SecurityLogic.USER_ENTITY_NAME, thisUser);
            session.getTransaction().commit();
            logger.debug("User {} updated", thisUser.getEmail());
            SessionMessages.addInfoMessage("Utente aggiornato correttamente");
            return new RedirectResolution(dispatch.getOriginalPath());
        } else {
            return new ForwardResolution("/layouts/user/profile/edit.jsp");
        }
    }

    public Resolution changePwd() {
        userId = (String) getSession().getAttribute(SessionAttributes.USER_ID);
        form = new FormBuilder(ChangePasswordFormBean.class).configFields("oldPwd", "pwd")
                .configMode(Mode.EDIT)
                .build();
        return new ForwardResolution("/layouts/user/profile/changePwd.jsp");
    }

    public Resolution updatePwd() {
        userId = (String) getSession().getAttribute(SessionAttributes.USER_ID);
        User thisUser =
            (User) QueryUtils.getObjectByPk
                (application, "portofino", SecurityLogic.USER_ENTITY_NAME, new User(userId));

        form = new FormBuilder(ChangePasswordFormBean.class).configFields("oldPwd", "pwd")
                .configMode(Mode.EDIT)
                .build();
        form.readFromRequest(context.getRequest());

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
                    thisUser.setPwd(SecurityLogic.encryptPassword(bean.pwd));
                } else {
                    thisUser.setPwd(bean.pwd);
                }
                thisUser.setPwdModDate(new Timestamp(new Date().getTime()));

                Session session = application.getSession("portofino");
                session.update(SecurityLogic.USER_ENTITY_NAME, thisUser);
                session.getTransaction().commit();
                logger.debug("User {} updated", thisUser.getEmail());
                SessionMessages.addInfoMessage("Password correctely updated");

                return new RedirectResolution(dispatch.getOriginalPath());
            } else {
                SessionMessages.addErrorMessage
                        ("La password non corrisponde a quella in uso");
                return new ForwardResolution("/layouts/user/profile/changePwd.jsp");
            }
        } else {
            return new ForwardResolution("/layouts/user/profile/changePwd.jsp");
        }
    }

    // do not expose this method publicly
    protected HttpSession getSession() {
        return context.getRequest().getSession(false);
    }
}
