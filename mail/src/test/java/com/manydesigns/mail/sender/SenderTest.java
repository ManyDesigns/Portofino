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
package com.manydesigns.mail.sender;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;
import com.manydesigns.mail.queue.FileSystemMailQueue;
import com.manydesigns.mail.queue.LockingMailQueue;
import com.manydesigns.mail.queue.MailQueue;
import com.manydesigns.mail.queue.model.Email;
import com.manydesigns.mail.queue.model.Recipient;
import junit.framework.TestCase;

import java.io.File;
import java.util.Iterator;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class SenderTest extends TestCase {
    private static final int SMTP_PORT = 2026;
    private SimpleSmtpServer server;
    MailSender sender;
    MailQueue queue;
    protected Thread senderThread;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        server = SimpleSmtpServer.start(SMTP_PORT);
        File file = File.createTempFile("mail", ".queue");
        file.deleteOnExit();
        file = new File(file.getAbsolutePath() + ".d");
        queue = new LockingMailQueue(new FileSystemMailQueue(file));
        sender = new DefaultMailSender(queue);
        sender.setPort(SMTP_PORT);
        senderThread = new Thread(sender);
        senderThread.start();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        sender.stop();
        server.stop();
    }

    public void testSimple() {
        Email myEmail = new Email();
        myEmail.setFrom("granatella@gmail.com");
        myEmail.getRecipients().add(new Recipient(Recipient.Type.TO, "giampiero.granatella@manydesigns.com"));
        myEmail.setSubject("subj");
        myEmail.setTextBody("body");
        queue.enqueue(myEmail);
        try {
            Thread.sleep(sender.getPollInterval() * 2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(queue.getEnqueuedEmailIds().isEmpty());

        assertEquals(1, server.getReceivedEmailSize());
        Iterator it = server.getReceivedEmail();
        while (it.hasNext()) {
            SmtpMessage msg = (SmtpMessage) it.next();
            assertEquals("body", msg.getBody());
            String[] to = msg.getHeaderValues("To");
            assertEquals(1, to.length);
        }
    }

    public void testSimpleMultiRecipient() {
        Email myEmail = new Email();
        myEmail.setFrom("granatella@gmail.com");
        myEmail.getRecipients().add(new Recipient(Recipient.Type.TO, "giampiero.granatella@manydesigns.com"));
        myEmail.getRecipients().add(new Recipient(Recipient.Type.TO, "alessio.stalla@manydesigns.com"));
        myEmail.getRecipients().add(new Recipient(Recipient.Type.CC, "paolo.predonzani@manydesigns.com"));
        myEmail.getRecipients().add(new Recipient(Recipient.Type.CC, "angelo.lupo@manydesigns.com"));
        myEmail.setSubject("subj");
        myEmail.setTextBody("body");
        queue.enqueue(myEmail);
        try {
            Thread.sleep(sender.getPollInterval() * 2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(queue.getEnqueuedEmailIds().isEmpty());

        assertEquals(1, server.getReceivedEmailSize());
        Iterator it = server.getReceivedEmail();
        while (it.hasNext()) {
            SmtpMessage msg = (SmtpMessage) it.next();
            assertEquals("body", msg.getBody());
        }
    }

    public void testHtml() {
        Email myEmail = new Email();
        myEmail.setFrom("granatella@gmail.com");
        myEmail.getRecipients().add(new Recipient(Recipient.Type.TO, "giampiero.granatella@manydesigns.com"));
        myEmail.setSubject("subj");
        myEmail.setHtmlBody("<body>body1</body>");
        queue.enqueue(myEmail);
        try {
            Thread.sleep(sender.getPollInterval() * 2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(queue.getEnqueuedEmailIds().isEmpty());

        assertEquals(1, server.getReceivedEmailSize());
        Iterator it = server.getReceivedEmail();
        while (it.hasNext()) {
            SmtpMessage msg = (SmtpMessage) it.next();
            assertTrue(msg.getBody().contains("<body>body1</body>"));
            it.remove();
        }

        myEmail = new Email();
        myEmail.setFrom("granatella@gmail.com");
        myEmail.getRecipients().add(new Recipient(Recipient.Type.TO, "giampiero.granatella@manydesigns.com"));
        myEmail.setSubject("subj");
        myEmail.setTextBody("textBody");
        myEmail.setHtmlBody("<body>body2</body>");
        queue.enqueue(myEmail);
        try {
            Thread.sleep(sender.getPollInterval() * 2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(queue.getEnqueuedEmailIds().isEmpty());

        assertEquals(1, server.getReceivedEmailSize());
        it = server.getReceivedEmail();
        while (it.hasNext()) {
            SmtpMessage msg = (SmtpMessage) it.next();
            assertTrue(msg.getBody().contains("<body>body2</body>"));
            assertTrue(msg.getBody().contains("textBody"));
            it.remove();
        }
    }

    public void testBrutalTermination() throws InterruptedException {
        senderThread.interrupt();
        senderThread.join();
        assertFalse(sender.isAlive());
    }

}
