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

import com.manydesigns.portofino.PortofinoProperties;
import org.apache.commons.mail.*;

import java.util.Map;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class EmailSender implements Runnable{

    private final boolean ssl;
    private final String login;
    private final String password;
    private final String server;
    private final int port;
    private final Map emailMap;


    //Costruttore con proprietà da inserire
    public EmailSender(String server, Map emailMap, int port, boolean ssl,
                 String login, String password) {
        this.emailMap = emailMap;
        this.server = server;
        this.port = port;
        this.ssl = ssl;
        this.login = login;
        this.password = password;
    }

    //Costruttore che prende le proprietà dal portofino.properties
    public EmailSender(Map emailMap) {

        this.emailMap = emailMap;
        this.server = (String) PortofinoProperties.getProperties()
                    .get("mail.smtp.host");
        this.port = (Integer) PortofinoProperties.getProperties()
                    .get("mail.smtp.port");
        this.ssl = (Boolean) PortofinoProperties.getProperties()
                    .get("mail.smtp.ssl.enabled");
        this.login = (String) PortofinoProperties.getProperties()
                    .get("mail.smtp.login");
        this.password = (String) PortofinoProperties.getProperties()
                    .get("mail.smtp.password");
    }

    public String getServer() {
        return server;
    }

    public int getPort() {
        return port;
    }


    public synchronized void run() {

        try {

            String attachmentPath = (String)emailMap.get("attachmentPath");
            String attachmentDescription = (String)emailMap.get("attachmentDescription");
            String attachmentName = (String)emailMap.get("attachmentName");
            if(null == attachmentPath) {
                Email email = new SimpleEmail();
                email.setSmtpPort(port);
                if (null!=login && null!=password ) {
                    email.setAuthenticator(new DefaultAuthenticator(login, password));
                }
                email.setHostName(server);
                email.setFrom((String) emailMap.get("from"));
                email.setSubject((String) emailMap.get("subject"));
                email.setMsg((String) emailMap.get("body"));
                email.addTo((String) emailMap.get("to"));
                email.setTLS(true);


                email.send();
            } else {
                MultiPartEmail email =  new MultiPartEmail();
                EmailAttachment attachment = new EmailAttachment();
                attachment.setPath(attachmentPath);
                attachment.setDisposition(EmailAttachment.ATTACHMENT);
                attachment.setDescription(attachmentDescription);
                attachment.setName(attachmentName);
                email.attach(attachment);
                email.send();
            }


        } catch (Throwable e) {
            e.printStackTrace();
            EmailTask.rejectedQueue.add(this);

        }
    }

    
}