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

package com.manydesigns.mail.queue;

import com.manydesigns.elements.util.ElementsFileUtils;
import com.manydesigns.mail.queue.model.Attachment;
import com.manydesigns.mail.queue.model.Email;
import com.manydesigns.mail.queue.model.Recipient;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;

import java.io.ByteArrayInputStream;
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
            "Copyright (c) 2005-2012, ManyDesigns srl";

    public void testExistingDirectory() throws IOException, QueueException {
        File file = File.createTempFile("mail", ".queue");
        file.deleteOnExit();
        file = new File(file.getAbsolutePath() + ".d");
        if(!file.mkdir()) {
            fail("Couldn't create directory " + file.getAbsolutePath());
        }
        file.deleteOnExit();

        basicTest(file);
    }

    public void testNonExistingDirectory() throws IOException, QueueException {
        File file = File.createTempFile("mail", ".queue");
        file.deleteOnExit();
        file = new File(file.getAbsolutePath() + ".d");

        basicTest(file);
    }

    public void testNonDirectory() throws IOException {
        File file = File.createTempFile("mail", ".queue");
        file.deleteOnExit();

        try {
            basicTest(file);
            fail();
        } catch (QueueException e) {}
    }

    public void testInaccessibleQueueAfterCreation() throws IOException, QueueException {
        File file = File.createTempFile("mail", ".queue");
        file.deleteOnExit();
        file = new File(file.getAbsolutePath() + ".d");
        file.deleteOnExit();

        FileSystemMailQueue fsmq = new FileSystemMailQueue(file);
        MailQueue mq = new LockingMailQueue(fsmq);

        assertTrue(mq.getEnqueuedEmailIds().isEmpty());
        assertEquals(null, mq.loadEmail("aaa"));

        if(ElementsFileUtils.setWritable(fsmq.getQueuedDirectory(), false)) {
            Email email = new Email();
            try {
                mq.enqueue(email);
                fail();
            } catch (QueueException e) {}
        } else {
            fail("Couldn't make queue directory not writable");
        }

        if(ElementsFileUtils.setWritable(fsmq.getQueuedDirectory(), true)) {
            Email email = new Email();
            mq.enqueue(email);
        } else {
            fail("Couldn't make queue directory writable");
        }

        FileUtils.deleteDirectory(fsmq.getQueuedDirectory());
        Email email = new Email();
        mq.enqueue(email);
    }

    public void testInaccessibleFileInQueue() throws IOException, QueueException {
        File file = File.createTempFile("mail", ".queue");
        file.deleteOnExit();
        file = new File(file.getAbsolutePath() + ".d");
        file.deleteOnExit();

        FileSystemMailQueue fsmq = new FileSystemMailQueue(file) {
            @Override
            protected File getEmailFile(String emailId) {
                File file = super.getEmailFile(emailId);
                boolean created = false;
                try {
                    created = file.createNewFile();
                } catch (IOException e) {
                    fail("Couldn't create file");
                }
                if(created && ElementsFileUtils.setWritable(file, false)) {
                    return file;
                } else {
                    fail("Couldn't make file not writable");
                    return null; //not reached
                }
            }
        };
        MailQueue mq = new LockingMailQueue(fsmq);

        assertTrue(mq.getEnqueuedEmailIds().isEmpty());

        Email email = new Email();
        try {
            mq.enqueue(email);
            fail();
        } catch (QueueException e) {}
    }

    public void testLoadMailError() throws IOException, QueueException {
        File file = File.createTempFile("mail", ".queue");
        file.deleteOnExit();
        file = new File(file.getAbsolutePath() + ".d");
        file.deleteOnExit();

        FileSystemMailQueue fsmq = new FileSystemMailQueue(file);
        MailQueue mq = new LockingMailQueue(fsmq);

        assertTrue(mq.getEnqueuedEmailIds().isEmpty());
        Email email = new Email();
        email.setSubject("pippo");
        email.getRecipients().add(new Recipient(Recipient.Type.TO, "mario"));
        String id = mq.enqueue(email);

        File emailFile = fsmq.getEmailFile(id);

        if(ElementsFileUtils.setReadable(emailFile, false)) {
            try {
                mq.loadEmail(id);
                fail();
            } catch (QueueException e) {}
        } else {
            fail("Could not make file unreadable");
        }

        if(ElementsFileUtils.setReadable(emailFile, true)) {
            assertNotNull(mq.loadEmail(id));
        } else {
            fail("Could not make file unreadable");
        }
    }

    public void testMarkSent() throws Exception {
        testMarkSent(false);
        testMarkSent(true);
    }

    private void testMarkSent(boolean keepSent) throws IOException, QueueException {
        File file = File.createTempFile("mail", ".queue");
        file.deleteOnExit();
        file = new File(file.getAbsolutePath() + ".d");
        file.deleteOnExit();

        FileSystemMailQueue fsmq = new FileSystemMailQueue(file);
        fsmq.setKeepSent(keepSent);
        MailQueue mq = new LockingMailQueue(fsmq);

        assertTrue(mq.getEnqueuedEmailIds().isEmpty());
        assertEquals(null, mq.loadEmail("aaa"));

        Email email = new Email();
        Attachment attachment = new Attachment();
        attachment.setInputStream(new ByteArrayInputStream("foo".getBytes()));
        email.getAttachments().add(attachment);
        String id = mq.enqueue(email);
        assertTrue(new File(attachment.getFilePath()).exists());

        mq.markSent(id);
        assertEquals(null, mq.loadEmail(id));
        assertFalse(new File(attachment.getFilePath()).exists());


        email = new Email();
        attachment = new Attachment();
        attachment.setInputStream(new ByteArrayInputStream("foo".getBytes()));
        email.getAttachments().add(attachment);
        id = mq.enqueue(email);
        assertTrue(new File(attachment.getFilePath()).exists());

        if(ElementsFileUtils.setWritable(fsmq.getSentDirectory(), false)) {
            try {
                mq.markSent(id);
                fail();
            } catch (QueueException e) {}
        } else {
            fail("Couldn't make sent directory not writable");
        }

        if(ElementsFileUtils.setWritable(fsmq.getSentDirectory(), true)) {
            mq.markSent(id);
            assertEquals(null, mq.loadEmail(id));
            assertFalse(new File(attachment.getFilePath()).exists());
        } else {
            fail("Couldn't make sent directory writable");
        }


        FileUtils.deleteDirectory(fsmq.getSentDirectory());
        email = new Email();
        attachment = new Attachment();
        attachment.setInputStream(new ByteArrayInputStream("foo".getBytes()));
        email.getAttachments().add(attachment);
        id = mq.enqueue(email);
        assertTrue(new File(attachment.getFilePath()).exists());
        mq.markSent(id);
        assertEquals(null, mq.loadEmail(id));
        assertFalse(new File(attachment.getFilePath()).exists());
        mq.markSent(id);
    }

    public void testMarkFailed() throws IOException, QueueException {
        File file = File.createTempFile("mail", ".queue");
        file.deleteOnExit();
        file = new File(file.getAbsolutePath() + ".d");
        file.deleteOnExit();

        FileSystemMailQueue fsmq = new FileSystemMailQueue(file);
        MailQueue mq = new LockingMailQueue(fsmq);

        assertTrue(mq.getEnqueuedEmailIds().isEmpty());
        assertEquals(null, mq.loadEmail("aaa"));

        Email email = new Email();
        Attachment attachment = new Attachment();
        attachment.setInputStream(new ByteArrayInputStream("foo".getBytes()));
        email.getAttachments().add(attachment);
        String id = mq.enqueue(email);
        assertTrue(new File(attachment.getFilePath()).exists());

        mq.markFailed(id);
        assertEquals(null, mq.loadEmail(id));
        assertFalse(new File(attachment.getFilePath()).exists());

        email = new Email();
        attachment = new Attachment();
        attachment.setInputStream(new ByteArrayInputStream("foo".getBytes()));
        email.getAttachments().add(attachment);
        id = mq.enqueue(email);
        assertTrue(new File(attachment.getFilePath()).exists());

        if(ElementsFileUtils.setWritable(fsmq.getFailedDirectory(), false)) {
            try {
                mq.markFailed(id);
                fail();
            } catch (QueueException e) {}
        } else {
            fail("Couldn't make failed directory not writable");
        }

        if(ElementsFileUtils.setWritable(fsmq.getFailedDirectory(), true)) {
            mq.markFailed(id);
            assertEquals(null, mq.loadEmail(id));
            assertFalse(new File(attachment.getFilePath()).exists());
        } else {
            fail("Couldn't make failed directory writable");
        }

        FileUtils.deleteDirectory(fsmq.getFailedDirectory());
        email = new Email();
        attachment = new Attachment();
        attachment.setInputStream(new ByteArrayInputStream("foo".getBytes()));
        email.getAttachments().add(attachment);
        id = mq.enqueue(email);
        assertTrue(new File(attachment.getFilePath()).exists());
        mq.markFailed(id);
        assertEquals(null, mq.loadEmail(id));
        assertFalse(new File(attachment.getFilePath()).exists());
        mq.markFailed(id);
    }

    public void basicTest(File file) throws QueueException {
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

    public void testAttachments() throws IOException, QueueException {
        File file = File.createTempFile("mail", ".queue");
        file.deleteOnExit();
        file = new File(file.getAbsolutePath() + ".d");

        MailQueue mq = new LockingMailQueue(new FileSystemMailQueue(file));

        assertTrue(mq.getEnqueuedEmailIds().isEmpty());
        assertEquals(null, mq.loadEmail("aaa"));

        Email email = new Email();
        email.setSubject("pippo");
        email.getRecipients().add(new Recipient(Recipient.Type.TO, "mario"));
        Attachment attachment = new Attachment();
        attachment.setName("foo");
        attachment.setDescription("bar");
        attachment.setInputStream(new ByteArrayInputStream("quux".getBytes()));
        email.getAttachments().add(attachment);
        String id = mq.enqueue(email);

        assertEquals(1, mq.getEnqueuedEmailIds().size());

        email = mq.loadEmail(id);
        assertEquals("pippo", email.getSubject());
        assertEquals(1, email.getRecipients().size());
        assertEquals(Recipient.Type.TO, email.getRecipients().get(0).getType());
        assertEquals("mario", email.getRecipients().get(0).getAddress());
        assertEquals(1, email.getAttachments().size());
        File attachmentFile = new File(email.getAttachments().get(0).getFilePath());
        attachmentFile.deleteOnExit();
        assertTrue(attachmentFile.exists());
    }

}
