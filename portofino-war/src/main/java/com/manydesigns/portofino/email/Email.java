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

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Map;
import java.util.Properties;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class Email implements Runnable{

    private final boolean ssl;
    private final String login;
    private final String password;
    private final String server;
    private final int port;
    private final Map email;



    //Costruttore con proprietà da inserire
    public Email(String server, Map email, int port, boolean ssl,
                 String login, String password) {
        this.email = email;
        this.server = server;
        this.port = port;
        this.ssl = ssl;
        this.login = login;
        this.password = password;
    }

    //Costruttore che prende le proprietà dal portofino.properties
    public Email(Map email) {

        this.email = email;
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
            Session s = getSession();
            InternetAddress from = new InternetAddress((String) email.get("sender"));
            InternetAddress to = new InternetAddress((String) email.get("addresse"));
            MimeMessage message = new MimeMessage(s);
            message.setFrom(from);
            message.addRecipient(Message.RecipientType.TO, to);
            message.setReplyTo(new Address[]{to});
            message.setSubject((String) email.get("subject"), "UTF-8");
            message.setText((String) email.get("body"), "UTF-8");
            Transport.send(message);
            EmailTask.successQueue.add(this);

        } catch (Throwable e) {
            e.printStackTrace();
            EmailTask.rejectedQueue.add(this);

        }
    }

    private Session getSession() {
		Properties properties = new Properties();
        properties.setProperty("mail.smtp.host", this.getServer());
		properties.setProperty("mail.smtp.port", ""+this.getPort());
        Authenticator authenticator = null;
        if (login!=null && password!=null)
        {
            authenticator = new Authenticator(login, password);
            properties.setProperty("mail.smtp.submitter", authenticator.getPasswordAuthentication().getUserName());
		    properties.setProperty("mail.smtp.auth", "true");
        }

        if (ssl)
        {
            properties.put("mail.smtp.socketFactory.port", port);
            properties.put("mail.smtp.socketFactory.class",
                    "javax.net.ssl.SSLSocketFactory");
            properties.put("mail.smtp.socketFactory.fallback", "false");
            properties.setProperty("mail.smtp.quitwait", "false");

        }
        return Session.getInstance(properties, authenticator);
	}

	private class Authenticator extends javax.mail.Authenticator {

		private PasswordAuthentication authentication;

		public Authenticator(String login, String password) {

			authentication = new PasswordAuthentication(login, password);
		}

		protected PasswordAuthentication getPasswordAuthentication() {
			return authentication;
		}
	}
}