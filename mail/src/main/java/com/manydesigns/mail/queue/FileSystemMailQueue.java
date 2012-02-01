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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.util.List;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class FileSystemMailQueue implements MailQueue {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    protected final File queued, sent, failed;
    protected final JAXBContext jaxbContext;

    public FileSystemMailQueue(File directory) {
        this.queued = new File(directory, "queue");
        this.sent = new File(directory, "sent");
        this.failed = new File(directory, "failed");
        try {
            jaxbContext = JAXBContext.newInstance(Email.class, Recipient.class);
        } catch (JAXBException e) {
            throw new Error("Couldn't create jaxb context", e);
        }
    }

    public String enqueue(Email email) {
        try {
            javax.xml.bind.Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            //String
            marshaller.marshal(email, new java.io.FileOutputStream(""));
            return null; //TODO
        } catch (Exception e) {
            throw new Error("Couldn't create jaxb context", e);
        }
    }

    public List<String> getEnqueuedEmailIds() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Email loadEmail(String id) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void markSent(String id) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void markFailed(String id) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public File getQueued() {
        return queued;
    }

    public File getSent() {
        return sent;
    }

    public File getFailed() {
        return failed;
    }
}
