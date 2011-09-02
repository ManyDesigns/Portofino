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
package com.manydesigns.portofino.email;

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
            "Copyright (c) 2005-2011, ManyDesigns srl";


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
