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

package com.manydesigns.mail.queue;

import com.manydesigns.mail.queue.model.Email;
import com.manydesigns.mail.queue.model.Recipient;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class FileSystemMailQueueTest extends TestCase {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    public void testFSMQ() throws IOException {
        File file = File.createTempFile("mail", ".queue");
        file.deleteOnExit();
        file = new File(file.getAbsolutePath() + ".d");
        if(!file.mkdir()) {
            fail("Couldn't create directory " + file.getAbsolutePath());
        }
        file.deleteOnExit();

        MailQueue mq = new LockingMailQueue(new FileSystemMailQueue(file));

        assertTrue(mq.getEnqueuedEmailIds().isEmpty());
        assertEquals(null, mq.loadEmail("aaa"));

        Email email = new Email();
        email.setSubject("pippo");
        email.getRecipients().add(new Recipient(Recipient.Type.TO, "mario"));
        String id = mq.enqueue(email);

        assertEquals(1, mq.getEnqueuedEmailIds().size());

        email = mq.loadEmail(id);
        assertEquals("pippo", email.getSubject());
        assertEquals(1, email.getRecipients().size());
        assertEquals(Recipient.Type.TO, email.getRecipients().get(0).getType());
        assertEquals("mario", email.getRecipients().get(0).getAddress());
    }

}
