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
import org.apache.commons.io.FileUtils;

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

    public void testExistingDirectory() throws IOException {
        File file = File.createTempFile("mail", ".queue");
        file.deleteOnExit();
        file = new File(file.getAbsolutePath() + ".d");
        if(!file.mkdir()) {
            fail("Couldn't create directory " + file.getAbsolutePath());
        }
        file.deleteOnExit();

        basicTest(file);
    }

    public void testNonExistingDirectory() throws IOException {
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
        } catch (QueueError e) {}
    }

    public void testInaccessibleQueueAfterCreation() throws IOException {
        File file = File.createTempFile("mail", ".queue");
        file.deleteOnExit();
        file = new File(file.getAbsolutePath() + ".d");
        file.deleteOnExit();

        FileSystemMailQueue fsmq = new FileSystemMailQueue(file);
        MailQueue mq = new LockingMailQueue(fsmq);

        assertTrue(mq.getEnqueuedEmailIds().isEmpty());
        assertEquals(null, mq.loadEmail("aaa"));

        if(fsmq.getQueuedDirectory().setWritable(false)) {
            Email email = new Email();
            try {
                mq.enqueue(email);
                fail();
            } catch (QueueError e) {}
        } else {
            fail("Couldn't make queue directory not writable");
        }

        if(fsmq.getQueuedDirectory().setWritable(true)) {
            Email email = new Email();
            mq.enqueue(email);
        } else {
            fail("Couldn't make queue directory writable");
        }

        FileUtils.deleteDirectory(fsmq.getQueuedDirectory());
        Email email = new Email();
        mq.enqueue(email);
    }

    public void testInaccessibleFileInQueue() throws IOException {
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
                if(created && file.setWritable(false)) {
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
        } catch (QueueError e) {}
    }

    public void testLoadMailError() throws IOException {
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

        if(emailFile.setReadable(false)) {
            try {
                mq.loadEmail(id);
                fail();
            } catch (QueueError e) {}
        } else {
            fail("Could not make file unreadable");
        }

        if(emailFile.setReadable(true)) {
            assertNotNull(mq.loadEmail(id));
        } else {
            fail("Could not make file unreadable");
        }
    }

    public void testMarkSent() throws IOException {
        testMarkSent(false);
        testMarkSent(true);
    }

    private void testMarkSent(boolean keepSent) throws IOException {
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
        String id = mq.enqueue(email);

        mq.markSent(id);
        assertEquals(null, mq.loadEmail(id));

        email = new Email();
        id = mq.enqueue(email);

        if(fsmq.getSentDirectory().setWritable(false)) {
            try {
                mq.markSent(id);
                fail();
            } catch (QueueError e) {}
        } else {
            fail("Couldn't make sent directory not writable");
        }

        if(fsmq.getSentDirectory().setWritable(true)) {
            mq.markSent(id);
            assertEquals(null, mq.loadEmail(id));
        } else {
            fail("Couldn't make sent directory writable");
        }

        FileUtils.deleteDirectory(fsmq.getSentDirectory());
        email = new Email();
        id = mq.enqueue(email);
        mq.markSent(id);
        assertEquals(null, mq.loadEmail(id));
        mq.markSent(id);
    }

    public void testMarkFailed() throws IOException {
        File file = File.createTempFile("mail", ".queue");
        file.deleteOnExit();
        file = new File(file.getAbsolutePath() + ".d");
        file.deleteOnExit();

        FileSystemMailQueue fsmq = new FileSystemMailQueue(file);
        MailQueue mq = new LockingMailQueue(fsmq);

        assertTrue(mq.getEnqueuedEmailIds().isEmpty());
        assertEquals(null, mq.loadEmail("aaa"));

        Email email = new Email();
        String id = mq.enqueue(email);

        mq.markFailed(id);
        assertEquals(null, mq.loadEmail(id));

        email = new Email();
        id = mq.enqueue(email);

        if(fsmq.getFailedDirectory().setWritable(false)) {
            try {
                mq.markFailed(id);
                fail();
            } catch (QueueError e) {}
        } else {
            fail("Couldn't make failed directory not writable");
        }

        if(fsmq.getFailedDirectory().setWritable(true)) {
            mq.markFailed(id);
            assertEquals(null, mq.loadEmail(id));
        } else {
            fail("Couldn't make failed directory writable");
        }

        FileUtils.deleteDirectory(fsmq.getFailedDirectory());
        email = new Email();
        id = mq.enqueue(email);
        mq.markFailed(id);
        assertEquals(null, mq.loadEmail(id));
        mq.markFailed(id);
    }

    public void basicTest(File file) {
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
