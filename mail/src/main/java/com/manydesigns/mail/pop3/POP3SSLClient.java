/*
 * Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.manydesigns.mail.pop3;

import com.sun.mail.pop3.POP3SSLStore;

import javax.mail.*;
import java.util.Set;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class POP3SSLClient extends POP3Client {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";


    public POP3SSLClient(String host, String protocol, int port,
                         String username, String password) {
        super(host, protocol, port, username, password);
    }


    public Set<String> read() {
        emails.clear();
        URLName url = new URLName(protocol, host, port, "",
                username, password);
        Folder inbox = null;
        Store store = null;
        try {
            Session session = Session.getInstance(pop3Props, null);
            store = new POP3SSLStore(session, url);
            store.connect();

            inbox = store.getFolder("INBOX");
            if (inbox == null) {
                logger.warn("No INBOX");
                return null;
            }
            inbox.open(Folder.READ_ONLY);

            Message[] messages = inbox.getMessages();
            for (Message message : messages) {
                if (message.getSubject().toLowerCase().contains(DELIVERY_STATUS_NOTIFICATION))
                {
                    extractEmail(message);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inbox != null) {
                try {
                    inbox.close(false);
                } catch (MessagingException e) {
                     logger.warn("cannot close INBOX",e);
                }
            }
            if (store != null) {
                try {
                    store.close();
                } catch (MessagingException e) {
                    logger.warn("cannot close Store",e);
                }
            }
        }
        return emails;
    }
}
