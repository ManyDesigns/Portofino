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
package com.manydesigns.mail.sender;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;
import com.manydesigns.elements.util.ElementsFileUtils;
import com.manydesigns.elements.util.RandomUtil;
import com.manydesigns.mail.queue.FileSystemMailQueue;
import com.manydesigns.mail.queue.LockingMailQueue;
import com.manydesigns.mail.queue.MailQueue;
import com.manydesigns.mail.queue.QueueException;
import com.manydesigns.mail.queue.model.Attachment;
import com.manydesigns.mail.queue.model.Email;
import com.manydesigns.mail.queue.model.Recipient;
import junit.framework.TestCase;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
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
    MailSenderRunnable runnable;
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
        runnable = new MailSenderRunnable(sender);
        senderThread = new Thread(runnable);
        senderThread.start();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        runnable.stop();
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
            Thread.sleep(runnable.getPollInterval() * 2);
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

    public void testNullFrom() throws QueueException {
        Email myEmail = new Email();
        myEmail.setFrom(null);
        myEmail.getRecipients().add(new Recipient(Recipient.Type.TO, "alessio.stalla@manydesigns.com"));
        myEmail.setSubject("subj");
        myEmail.setTextBody("body");
        String id = queue.enqueue(myEmail);
        try {
            Thread.sleep(runnable.getPollInterval() * 2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(queue.getEnqueuedEmailIds().isEmpty());
        File failedEmailFile =
                RandomUtil.getCodeFile(fsQueue.getFailedDirectory(), "email-{0}.xml", id);
        assertTrue(failedEmailFile.exists());

        assertEquals(0, server.getReceivedEmailSize());
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
            Thread.sleep(runnable.getPollInterval() * 2);
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
        //with html body only
        Email myEmail = new Email();
        myEmail.setFrom("granatella@gmail.com");
        myEmail.getRecipients().add(new Recipient(Recipient.Type.TO, "giampiero.granatella@manydesigns.com"));
        myEmail.setSubject("subj");
        myEmail.setHtmlBody("<body>body1</body>");
        queue.enqueue(myEmail);
        try {
            Thread.sleep(runnable.getPollInterval() * 2);
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

        //with html body and text body
        myEmail = new Email();
        myEmail.setFrom("granatella@gmail.com");
        myEmail.getRecipients().add(new Recipient(Recipient.Type.TO, "giampiero.granatella@manydesigns.com"));
        myEmail.setSubject("subj");
        myEmail.setTextBody("textBody");
        myEmail.setHtmlBody("<body>body2</body>");
        queue.enqueue(myEmail);
        try {
            Thread.sleep(runnable.getPollInterval() * 2);
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

    public void testAttachments() throws QueueException, IOException {
        //with text body and attachments
        Email myEmail = new Email();
        myEmail.setFrom("alessiostalla@gmail.com");
        myEmail.getRecipients().add(new Recipient(Recipient.Type.TO, "alessiostalla@gmail.com"));
        myEmail.setSubject("subj");
        String htmlBody = "<body>body<img src=\"cid:attach2\" /></body>";
        myEmail.setHtmlBody(htmlBody);

        Attachment attachment = new Attachment();
        attachment.setName("attachName1");
        attachment.setDescription("attachDescr1");
        attachment.setInputStream(new ByteArrayInputStream("attachContent1".getBytes()));
        myEmail.getAttachments().add(attachment);

        attachment = new Attachment();
        attachment.setName("feather.gif");
        attachment.setDescription("attachDescr2");
        attachment.setInputStream(getClass().getResourceAsStream("feather.gif"));
        attachment.setEmbedded(true);
        attachment.setContentId("attach2");
        myEmail.getAttachments().add(attachment);
        queue.enqueue(myEmail);
        try {
            Thread.sleep(runnable.getPollInterval() * 2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(queue.getEnqueuedEmailIds().isEmpty());

        assertEquals(1, server.getReceivedEmailSize());
        Iterator it = server.getReceivedEmail();
        while (it.hasNext()) {
            SmtpMessage msg = (SmtpMessage) it.next();
            assertTrue(msg.getBody().contains(htmlBody));

            assertTrue(msg.getBody().contains(
                    "Content-Type: application/octet-stream; name=attachName1" +
                    "Content-Transfer-Encoding: 7bit" +
                    "Content-Disposition: attachment; filename=attachName1" +
                    "Content-Description: attachDescr1\n" +
                    "attachContent1"));

            String encodedAttachment =
                    new String(
                            Base64.encodeBase64(
                                    IOUtils.toByteArray(getClass().getResourceAsStream("feather.gif"))));
            assertTrue(msg.getBody().contains(
                    "Content-Type: application/octet-stream; name=feather.gif" +
                    "Content-Transfer-Encoding: base64" +
                    "Content-Disposition: inline; filename=feather.gif" +
                    "Content-ID: <attach2>\n" +
                    encodedAttachment));
            it.remove();
        }

        //with html body and attachments
        myEmail = new Email();
        myEmail.setFrom("alessiostalla@gmail.com");
        myEmail.getRecipients().add(new Recipient(Recipient.Type.TO, "alessiostalla@gmail.com"));
        myEmail.setSubject("subj");
        String body = "body";
        myEmail.setTextBody(body);

        attachment = new Attachment();
        attachment.setName("attachName1");
        attachment.setDescription("attachDescr1");
        attachment.setInputStream(new ByteArrayInputStream("attachContent1".getBytes()));
        myEmail.getAttachments().add(attachment);

        attachment = new Attachment();
        attachment.setName("feather.gif");
        attachment.setDescription("attachDescr2");
        attachment.setInputStream(getClass().getResourceAsStream("feather.gif"));
        attachment.setEmbedded(true);
        attachment.setContentId("attach2");
        myEmail.getAttachments().add(attachment);
        queue.enqueue(myEmail);
        try {
            Thread.sleep(runnable.getPollInterval() * 2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(queue.getEnqueuedEmailIds().isEmpty());

        assertEquals(1, server.getReceivedEmailSize());
        it = server.getReceivedEmail();
        while (it.hasNext()) {
            SmtpMessage msg = (SmtpMessage) it.next();
            assertTrue(msg.getBody().contains(body));

            assertTrue(msg.getBody().contains(
                    "Content-Type: application/octet-stream; name=attachName1" +
                    "Content-Transfer-Encoding: 7bit" +
                    "Content-Disposition: attachment; filename=attachName1" +
                    "Content-Description: attachDescr1\n" +
                    "attachContent1"));

            String encodedAttachment =
                    new String(
                            Base64.encodeBase64(
                                    IOUtils.toByteArray(getClass().getResourceAsStream("feather.gif"))));
            assertTrue(msg.getBody().contains(
                    "Content-Type: application/octet-stream; name=feather.gif" +
                    "Content-Transfer-Encoding: base64" +
                    "Content-Disposition: attachment; filename=feather.gif" +
                    "Content-Description: attachDescr2\n" +
                    encodedAttachment));
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
            Thread.sleep(runnable.getPollInterval() * 2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertFalse(queue.getEnqueuedEmailIds().isEmpty());

        assertEquals(0, server.getReceivedEmailSize());

        sender.setPort(SMTP_PORT);
        try {
            Thread.sleep(runnable.getPollInterval() * 3);
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
            Thread.sleep(runnable.getPollInterval() * 2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertFalse(queue.getEnqueuedEmailIds().isEmpty());

        sender.setLogin("fake");
        sender.setPassword("login");
        try {
            Thread.sleep(runnable.getPollInterval() * 2);
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
            Thread.sleep(runnable.getPollInterval() * 2);
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
            Thread.sleep(runnable.getPollInterval() * 2);
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
            Thread.sleep(runnable.getPollInterval() * 2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertFalse(queue.getEnqueuedEmailIds().isEmpty());
        assertEquals(1, server.getReceivedEmailSize());

        try {
            Thread.sleep(runnable.getPollInterval() * 2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertFalse(queue.getEnqueuedEmailIds().isEmpty());
        assertEquals(1, server.getReceivedEmailSize());

        if(!ElementsFileUtils.setWritable(fsQueue.getSentDirectory(), true)) {
            fail("Couldn't make sent directory writable");
        }

        try {
            Thread.sleep(runnable.getPollInterval() * 2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(queue.getEnqueuedEmailIds().isEmpty());
        assertEquals(1, server.getReceivedEmailSize());
    }



    public void testBrutalTermination() throws InterruptedException {
        senderThread.interrupt();
        senderThread.join();
        assertFalse(runnable.isAlive());
    }

}
