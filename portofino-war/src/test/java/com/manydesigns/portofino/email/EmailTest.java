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

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;
import com.manydesigns.portofino.AbstractPortofinoTest;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.system.model.email.EmailBean;

import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class EmailTest extends AbstractPortofinoTest {
    private static final int SMTP_PORT = 2026;
    private SimpleSmtpServer server;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        server = SimpleSmtpServer.start(SMTP_PORT);
        PortofinoProperties.loadProperties("portofino_test.properties");
        application.openSession();
    }

    public void testSimple() {
        EmailBean myEmail = new EmailBean();
        myEmail.setState(EmailUtils.TOBESENT);
        myEmail.setFrom("granatella@gmail.com");
        myEmail.setTo("giampiero.granatella@manydesigns.com");
        myEmail.setSubject("subj");
        myEmail.setBody("body");
        myEmail.setCreateDate(new Date());
        EmailSender sender = new EmailSender(null, myEmail, "127.0.0.1", SMTP_PORT,
                false, null, null);
        //lancio il run del sender
        sender.run();
        assertEquals(1, server.getReceivedEmailSize());
        Iterator it = server.getReceivedEmail();
        while (it.hasNext()) {
            SmtpMessage msg = (SmtpMessage) it.next();
            assertEquals("body", msg.getBody());
        }

    }

    public void testServerDown() {
        EmailBean myEmail = new EmailBean();
        myEmail.setState(EmailUtils.TOBESENT);
        myEmail.setFrom("granatella@gmail.com");
        myEmail.setTo("giampiero.granatella@manydesigns.com");
        myEmail.setSubject("subj");
        myEmail.setBody("body");
        myEmail.setCreateDate(new Date());
        EmailSender sender = new EmailSender(null, myEmail, "127.0.0.1", SMTP_PORT - 1,
                false, null, null);

        //lancio il run del sender
        sender.run();

        assertEquals(0, server.getReceivedEmailSize());
    }

    public void testEmailAttachment() {
        EmailBean myEmail = new EmailBean();
        myEmail.setState(EmailUtils.TOBESENT);
        myEmail.setFrom("granatella@gmail.com");
        myEmail.setTo("giampiero.granatella@manydesigns.com");
        myEmail.setSubject("subj");
        myEmail.setBody("body");
        myEmail.setCreateDate(new Date());
        //final String url = "../../test/java/com/manydesigns/portofino/email/";
        final String url = "src/test/java/com/manydesigns/portofino/email/";
        File attachment = new File(url +
                "feather.gif");
        myEmail.setAttachmentPath(attachment.getAbsolutePath());
        myEmail.setAttachmentName("piuma");
        myEmail.setAttachmentDescription("piuma dell'apache");

        EmailSender sender = new EmailSender(null, myEmail, "127.0.0.1", SMTP_PORT,
                false, null, null);
        //lancio il run del sender
        sender.run();
        assertEquals(1, server.getReceivedEmailSize());
        Iterator it = server.getReceivedEmail();
        if (it.hasNext()) {
            SmtpMessage msg = (SmtpMessage) it.next();
            assertEquals("multipart/mixed;", msg.getHeaderValue("Content-Type"));
            assertTrue(msg.getBody().contains(
                    "Content-Type: image/gif; name=piumaContent-Transfer-Encoding:" +
                            " base64Content-Disposition: attachment; " +
                            "filename=piumaContent-Description: piuma dell'apache"));
        }
    }

    public void testEmailTask() {
        Timer scheduler = new Timer(true);
        
        for (int i = 1; i <= 10; i++) {
            EmailBean bean = new EmailBean ("subj:" + i,
                    "body:" + i, "granatella@gmail.com",
                    "spammer@spam.it");
            application.saveObject(EmailUtils.EMAILQUEUE_TABLE, bean);
            application.commit("portofino");
        }
        try {
            scheduler.schedule(new EmailTask(application), 0, 10);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }
        scheduler.cancel();
        EmailTask.stop();
        
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }
        assertEquals(10, server.getReceivedEmailSize());
        List<Object> emailList =
                application.getAllObjects("portofino.public.emailqueue");
        assertEquals(0, emailList.size());
    }

    public void testEmailTaskErrSmtp() {
        PortofinoProperties.loadProperties
                ("portofino_smtpsbagliato.properties");
        Timer scheduler = new Timer(true);
        for (int i = 1; i <= 10; i++) {
            EmailUtils.addEmail(application, "subj:" + i,
                    "body:" + i, "granatella@gmail.com",
                    "spammer@spam.it");
            application.commit("portofino");
        }
        try {
            scheduler.schedule(new EmailTask(application), 0, 10);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }

        scheduler.cancel();
        EmailTask.stop();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }
        assertEquals(0, server.getReceivedEmailSize());
        List<Object> emailList =
                application.getAllObjects("portofino.public.emailqueue");
        assertEquals(10, emailList.size());
    }

    public void tearDown() {
        server.stop();
        application.closeSession();
        Runtime r = Runtime.getRuntime();
        r.gc();
        r.runFinalization();
    }
}
