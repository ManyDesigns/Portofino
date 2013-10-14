/*
* Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.pageactions.login;

import com.manydesigns.mail.queue.MailQueue;
import com.manydesigns.mail.queue.QueueException;
import com.manydesigns.mail.queue.model.Email;
import com.manydesigns.mail.queue.model.Recipient;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.actions.user.LoginAction;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.dispatcher.Dispatch;
import com.manydesigns.portofino.dispatcher.PageAction;
import com.manydesigns.portofino.dispatcher.PageInstance;
import com.manydesigns.portofino.modules.MailModule;
import com.manydesigns.portofino.pageactions.PageActionName;
import com.manydesigns.portofino.pageactions.annotations.ScriptTemplate;
import net.sourceforge.stripes.action.Resolution;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@ScriptTemplate("script_template.groovy")
@PageActionName("Login")
public class DefaultLoginAction extends LoginAction implements PageAction {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    //--------------------------------------------------------------------------
    // Properties
    //--------------------------------------------------------------------------

    /**
     * The dispatch property. Injected.
     */
    public Dispatch dispatch;

    /**
     * The PageInstance property. Injected.
     */
    public PageInstance pageInstance;

    @Inject(MailModule.MAIL_QUEUE)
    public MailQueue mailQueue;

    //--------------------------------------------------------------------------
    // PageAction implementation
    //--------------------------------------------------------------------------

    @Override
    protected void sendForgotPasswordEmail(String from, String to, String subject, String body) {
        sendMail(from, to, subject, body);
    }

    @Override
    protected void sendSignupConfirmationEmail(String from, String to, String subject, String body) {
        sendMail(from, to, subject, body);
    }

    protected void sendMail(String from, String to, String subject, String body) {
        if(mailQueue == null) {
            throw new UnsupportedOperationException("Mail queue is not enabled");
        }

        Email email = new Email();
        email.getRecipients().add(new Recipient(Recipient.Type.TO, to));
        email.setFrom(from);
        email.setSubject(subject);
        email.setHtmlBody(body);
        try {
            mailQueue.enqueue(email);
        } catch (QueueException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getApplicationName() {
        return portofinoConfiguration.getString(PortofinoProperties.APP_NAME);
    }

    @Override
    public Resolution preparePage() {
        return null;
    }

    @Override
    public PageInstance getPageInstance() {
        return pageInstance;
    }

    @Override
    public void setPageInstance(PageInstance pageInstance) {
        this.pageInstance = pageInstance;
    }

}
