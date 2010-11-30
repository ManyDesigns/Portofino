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

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;
import com.manydesigns.portofino.AbstractPortofinoTest;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.system.model.email.EmailBean;

import java.util.*;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class EmailTest extends AbstractPortofinoTest {
   private static SimpleSmtpServer server;
    private static final int SMTP_PORT = 2026;



    @Override
    public void setUp() throws Exception {
        super.setUp();
        PortofinoProperties.loadProperties("database/portofino_test.properties");
        server = SimpleSmtpServer.start(SMTP_PORT);
        context.openSession();}

    public void testSimple() {
        EmailBean myEmail = new EmailBean();
        myEmail.setState(EmailUtils.TOBESENT);
        myEmail.setFrom("granatella@gmail.com");
        myEmail.setTo("giampiero.granatella@manydesigns.com");
        myEmail.setSubject("subj");
        myEmail.setBody("body");
        myEmail.setCreateDate(new Date());
        EmailSender sender = new EmailSender(myEmail, "127.0.0.1", SMTP_PORT,
                false, null, null);

        //lancio il run del sender
        sender.run();

        assertEquals(1, server.getReceivedEmailSize());
        Iterator it = server.getReceivedEmail();
        while(it.hasNext()) {
            SmtpMessage msg = (SmtpMessage) it.next();
            assertEquals("body",msg.getBody());
        }
        assertTrue(EmailTask.successQueue.contains(sender)); 
    }

    public void testServerDown() {

        EmailBean myEmail = new EmailBean();
        myEmail.setState(EmailUtils.TOBESENT);
        myEmail.setFrom("granatella@gmail.com");
        myEmail.setTo("giampiero.granatella@manydesigns.com");
        myEmail.setSubject("subj");
        myEmail.setBody("body");
        myEmail.setCreateDate(new Date());
        EmailSender sender = new EmailSender(myEmail, "127.0.0.1", SMTP_PORT-1,
                false, null, null);

        //lancio il run del sender
        sender.run();

        assertEquals(0, server.getReceivedEmailSize());
        assertTrue(EmailTask.rejectedQueue.contains(sender));
    }

    public void testEmailAttachment() {
        EmailBean myEmail = new EmailBean();
        myEmail.setState(EmailUtils.TOBESENT);
        myEmail.setFrom("granatella@gmail.com");
        myEmail.setTo("giampiero.granatella@manydesigns.com");
        myEmail.setSubject("subj");
        myEmail.setBody("body");
        myEmail.setCreateDate(new Date());
        myEmail.setAttachmentPath("/Users/giampi/feather.gif");
        myEmail.setAttachmentName("piuma");
        myEmail.setAttachmentDescription("piuma dell'apache");
        EmailSender sender = new EmailSender(myEmail, "127.0.0.1", SMTP_PORT,
                false, null, null);
        //lancio il run del sender
        sender.run();

        assertEquals(1, server.getReceivedEmailSize());
        Iterator it = server.getReceivedEmail();
        if(it.hasNext()) {
            SmtpMessage msg = (SmtpMessage) it.next();
            assertEquals("multipart/mixed;",msg.getHeaderValue("Content-Type"));
            assertTrue(msg.getBody().contains(
            "Content-Type: image/gif; name=piumaContent-Transfer-Encoding:" +
                    " base64Content-Disposition: attachment; " +
                    "filename=piumaContent-Description: piuma dell'apache"));
        }
        assertTrue(EmailTask.successQueue.contains(sender)); 
    }

  /*  public void testEmailTask(){

        Timer scheduler  = new java.util.Timer(true);
        for (int i=1; i <= 10; i++) {
           EmailUtils.addEmail(context, "subj:"+i, "body:"+i, "granatella@gmail.com",
            "spammer@spam.it");
            context.commit("portofino");
        }
        Properties p = PortofinoProperties.getProperties();
        
        try {
            scheduler.schedule(new EmailTask(context), 0, 10);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }

        if (scheduler!=null) {
            scheduler.cancel();
            EmailTask.stop();
        } else {
            fail();
        }
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }

        assertEquals(10, server.getReceivedEmailSize());

        assertTrue(EmailTask.successQueue.size()==0);
        assertTrue(EmailTask.rejectedQueue.size()==0);

        List<Object> emailList =
                context.getAllObjects("portofino.public.emailqueue");
        assertEquals(0, emailList.size());
        
    }

    public void testEmailTaskErrSmtp(){
        PortofinoProperties.loadProperties("database/portofino_smtpsbagliato.properties");
        Timer scheduler  = new java.util.Timer(true);
        for (int i=1; i <= 10; i++) {
           EmailUtils.addEmail(context, "subj:"+i, "body:"+i, "granatella@gmail.com",
            "spammer@spam.it");
            context.commit("portofino");
        }
        Properties p = PortofinoProperties.getProperties();

        try {
            scheduler.schedule(new EmailTask(context), 0, 10);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }

        if (scheduler!=null) {
            scheduler.cancel();
            EmailTask.stop();
        } else {
            fail();
        }
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }

        assertEquals(0, server.getReceivedEmailSize());

        assertTrue(EmailTask.successQueue.size()==0);
        assertTrue(EmailTask.rejectedQueue.size()==0);

        List<Object> emailList =
                context.getAllObjects("portofino.public.emailqueue");
        assertEquals(10, emailList.size());

    }*/

    public void tearDown() {
        server.stop();
        context.closeSession();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }
    }
}
