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
package com.manydesigns.portofino.email;

import com.manydesigns.elements.fields.search.Criteria;
import com.manydesigns.elements.logging.LogUtil;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.JavaClassAccessor;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.context.Context;
import com.manydesigns.portofino.systemModel.users.User;

import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimerTask;
import java.util.logging.Logger;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class PasswordGenerator extends TimerTask {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";
    private final Context context;
    private final boolean ssl;
    private final String login;
    private final String password;
    private final String server;
    private final String sender;
    private final int port;
    public static final Logger logger =
            LogUtil.getLogger(PasswordGenerator.class);


    public PasswordGenerator(Context context) {
        this.context = context;
        this.server = (String) PortofinoProperties.getProperties()
                    .getProperty("mail.smtp.host");
        this.port = Integer.parseInt(PortofinoProperties.getProperties()
                    .getProperty("mail.smtp.port"));
        this.ssl = Boolean.parseBoolean(PortofinoProperties.getProperties()
                    .getProperty("mail.smtp.ssl.enabled"));
        this.login = (String) PortofinoProperties.getProperties()
                    .getProperty("mail.smtp.login");
        this.password = (String) PortofinoProperties.getProperties()
                    .getProperty("mail.smtp.password");
        this.sender = (String) PortofinoProperties.getProperties()
                    .getProperty("mail.sender");
    }

    public void run() {
        try {
            ClassAccessor accessor = JavaClassAccessor
                    .getClassAccessor(User.class);
            Criteria criteria = new Criteria(accessor);
            List<Object> users = context.getObjects(
                    criteria.isNull(accessor.getProperty("password")));
            for (Object obj : users) {
                 mailGenerator(context, (User) obj);
            }
        } catch (Exception e) {
            LogUtil.warning(logger, "Cannot generate  password", e);
        }
    }

    private synchronized void mailGenerator(Context context,
                                            User user) {
        try {
            Properties props = PortofinoProperties.getProperties();
            String pwd = user.passwordGenerator(
                            Integer.parseInt(props.getProperty
                                    ("users.pwd.minlength", "6")));
            //Aggiorno password
            user.setPwdEncrypted(pwd);
            //salvo la mail per l'utente
            String msg =
             "user "+ user.getEmail()+" "+ user.getPwd();
            EmailTask em = new EmailTask(context);
            EmailHandler.addEmail(context, "subject", msg,
                user.getEmail(), sender, new Date(), EmailHandler.TOBESENT);
            context.saveObject("portofino.user_", user);
            context.commit("portofino");
        } catch (Throwable e) {
            logger.warning("error sending email");
        }
    }
}
