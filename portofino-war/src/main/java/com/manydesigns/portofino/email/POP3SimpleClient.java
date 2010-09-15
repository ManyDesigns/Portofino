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

import javax.mail.*;
import java.util.Set;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class POP3SimpleClient extends POP3Client {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public POP3SimpleClient(String host, String provider, int port, String username, String password) {
        super(host, provider, port, username, password);
    }

    public Set<String> read() {
        emails.clear();
        Folder inbox = null;
        Store store = null;
        try {
            Session session = Session.getDefaultInstance(pop3Props, null);
            store = session.getStore(provider);
            store.connect(host, username, password);

            inbox = store.getFolder("INBOX");
            if (inbox == null) {
                log.error("No INBOX");
                return null;
            }
            inbox.open(Folder.READ_ONLY);

            Message[] messages = inbox.getMessages();
            for (Message message : messages) {
                extractEmail(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inbox != null) {
                try {
                    inbox.close(false);
                } catch (MessagingException e) {
                    log.error(e);
                }
            }
            if (store != null) {
                try {

                    store.close();

                } catch (MessagingException e) {
                    log.error(e);
                }
            }
        }
        return emails;
    }
}
