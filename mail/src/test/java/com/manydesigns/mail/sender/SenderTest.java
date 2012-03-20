/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.manydesigns.mail.sender;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;
import com.manydesigns.elements.util.ElementsFileUtils;
import com.manydesigns.mail.queue.FileSystemMailQueue;
import com.manydesigns.mail.queue.LockingMailQueue;
import com.manydesigns.mail.queue.MailQueue;
import com.manydesigns.mail.queue.QueueException;
import com.manydesigns.mail.queue.model.Email;
import com.manydesigns.mail.queue.model.Recipient;
import junit.framework.TestCase;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
    FileSystemMailQueue fsQueue;
    protected Thread senderThread;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        server = SimpleSmtpServer.start(SMTP_PORT);
        File file = File.createTempFile("mail", ".queue");
        file.deleteOnExit();
        file = new File(file.getAbsolutePath() + ".d");
        fsQueue = new FileSystemMailQueue(file);
        queue = new LockingMailQueue(fsQueue);
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
        senderThread.join();
    }

    public void testSimple() throws QueueException {
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

    public void testSimpleMultiRecipient() throws QueueException {
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

    public void testHtml() throws QueueException {
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

    public void testServerDown() throws QueueException {
        sender.setPort(SMTP_PORT + 1);
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
        assertFalse(queue.getEnqueuedEmailIds().isEmpty());

        assertEquals(0, server.getReceivedEmailSize());

        sender.setPort(SMTP_PORT);
        try {
            Thread.sleep(sender.getPollInterval() * 3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(queue.getEnqueuedEmailIds().isEmpty());

        assertEquals(1, server.getReceivedEmailSize());
    }

    public void testFailedAuth() throws QueueException {
        sender.setPort(465);
        sender.setServer("smtp.gmail.com");
        sender.setSsl(true);
        Email myEmail = new Email();
        myEmail.setFrom("ginopino@example.com");
        myEmail.getRecipients().add(new Recipient(Recipient.Type.TO, "pulcinella@example.com"));
        myEmail.setSubject("subj");
        myEmail.setTextBody("body");
        queue.enqueue(myEmail);
        try {
            Thread.sleep(sender.getPollInterval() * 2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertFalse(queue.getEnqueuedEmailIds().isEmpty());

        sender.setLogin("fake");
        sender.setPassword("login");
        try {
            Thread.sleep(sender.getPollInterval() * 2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertFalse(queue.getEnqueuedEmailIds().isEmpty());
    }

    public void testWrongAddress() throws QueueException {
        Email myEmail = new Email();
        myEmail.setFrom("ginopino");
        myEmail.getRecipients().add(new Recipient(Recipient.Type.TO, "pulcinella"));
        myEmail.setSubject("subj");
        myEmail.setTextBody("body");
        queue.enqueue(myEmail);
        try {
            Thread.sleep(sender.getPollInterval() * 2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(queue.getEnqueuedEmailIds().isEmpty());
        assertEquals(0, server.getReceivedEmailSize());
    }

    public void testMalformedMail() throws IOException, QueueException {
        File malformedFile = new File(fsQueue.getQueuedDirectory(), "email-wrong.xml");
        FileWriter fw = new FileWriter(malformedFile);
        fw.write("malformed");
        fw.close();
        assertFalse(queue.getEnqueuedEmailIds().isEmpty());
        try {
            Thread.sleep(sender.getPollInterval() * 2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(queue.getEnqueuedEmailIds().isEmpty());
        assertEquals(0, server.getReceivedEmailSize());
    }

    public void testMarkSentFailed() throws QueueException {
        Email myEmail = new Email();
        myEmail.setFrom("granatella@gmail.com");
        myEmail.getRecipients().add(new Recipient(Recipient.Type.TO, "giampiero.granatella@manydesigns.com"));
        myEmail.setSubject("subj");
        myEmail.setTextBody("body");
        if(!ElementsFileUtils.setWritable(fsQueue.getSentDirectory(), false)) {
            fail("Couldn't make sent directory not writable");
        }

        queue.enqueue(myEmail);
        try {
            Thread.sleep(sender.getPollInterval() * 2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertFalse(queue.getEnqueuedEmailIds().isEmpty());
        assertEquals(1, server.getReceivedEmailSize());

        try {
            Thread.sleep(sender.getPollInterval() * 2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertFalse(queue.getEnqueuedEmailIds().isEmpty());
        assertEquals(1, server.getReceivedEmailSize());

        if(!ElementsFileUtils.setWritable(fsQueue.getSentDirectory(), true)) {
            fail("Couldn't make sent directory writable");
        }

        try {
            Thread.sleep(sender.getPollInterval() * 2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(queue.getEnqueuedEmailIds().isEmpty());
        assertEquals(1, server.getReceivedEmailSize());
    }



    public void testBrutalTermination() throws InterruptedException {
        senderThread.interrupt();
        senderThread.join();
        assertFalse(sender.isAlive());
    }

}
