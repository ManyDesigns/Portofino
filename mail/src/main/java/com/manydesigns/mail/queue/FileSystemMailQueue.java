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
import com.manydesigns.elements.util.RandomUtil;
import com.manydesigns.mail.queue.model.Email;
import com.manydesigns.mail.queue.model.Recipient;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class FileSystemMailQueue implements MailQueue {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    protected final File queuedDirectory, sentDirectory, failedDirectory;
    protected final JAXBContext jaxbContext;
    protected boolean keepSent;

    protected static final Logger logger = LoggerFactory.getLogger(FileSystemMailQueue.class);

    public FileSystemMailQueue(File directory) {
        this.queuedDirectory = new File(directory, "queue");
        this.sentDirectory = new File(directory, "sent");
        this.failedDirectory = new File(directory, "failed");

        if(!ElementsFileUtils.ensureDirectoryExistsAndWritable(this.queuedDirectory)) {
            logger.warn("Directory does not exist or is not writable: {}", this.queuedDirectory);
        }
        if(!ElementsFileUtils.ensureDirectoryExistsAndWritable(this.sentDirectory)) {
            logger.warn("Directory does not exist or is not writable: {}", this.sentDirectory);
        }
        if(!ElementsFileUtils.ensureDirectoryExistsAndWritable(this.failedDirectory)) {
            logger.warn("Directory does not exist or is not writable: {}", this.failedDirectory);
        }
        try {
            jaxbContext = JAXBContext.newInstance(Email.class, Recipient.class);
        } catch (JAXBException e) {
            throw new Error("Couldn't create jaxb context", e);
        }
    }

    protected void checkDirectory(File file) throws QueueException {
        if(!ElementsFileUtils.ensureDirectoryExistsAndWritable(file)) {
            throw new QueueException("Invalid directory " + file.getAbsolutePath());
        }
    }

    protected void checkDirectories() throws QueueException {
        checkDirectory(queuedDirectory);
        checkDirectory(sentDirectory);
        checkDirectory(failedDirectory);
    }

    public String enqueue(Email email) throws QueueException {
        try {
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            String emailId = RandomUtil.createRandomId(20);
            File destinationFile = getEmailFile(emailId);
            checkDirectory(queuedDirectory);
            marshaller.marshal(email, destinationFile);
            return emailId;
        } catch (Exception e) {
            throw new QueueException("Couldn't enqueue mail", e);
        }
    }

    protected File getEmailFile(String emailId) {
        return RandomUtil.getCodeFile(queuedDirectory, "email-{0}.xml", emailId);
    }

    public List<String> getEnqueuedEmailIds() throws QueueException {
        checkDirectory(queuedDirectory);
        List<String> ids = new ArrayList<String>();
        Pattern pattern = Pattern.compile("^email-(.*)\\.xml$");
        for(String filename : queuedDirectory.list()) {
            Matcher matcher = pattern.matcher(filename);
            if(matcher.matches()) {
                logger.debug("Path matched: {}", filename);
                ids.add(matcher.group(1));
            }
        }
        return ids;
    }

    public Email loadEmail(String id) throws QueueException {
        try {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            File emailFile = getEmailFile(id);
            if(emailFile.exists()) {
                logger.debug("Found email with id: {}", id);
                Email email = (Email) unmarshaller.unmarshal(emailFile);
                return email;
            } else {
                logger.debug("Email with id {} not found", id);
                return null;
            }
        } catch (JAXBException e) {
            throw new MailParseException("Couldn't parse email", e);
        } catch (Exception e) {
            throw new QueueException("Couldn't load email", e);
        }
    }

    public void markSent(String id) throws QueueException {
        checkDirectories();
        try {
            File emailFile = getEmailFile(id);
            if(emailFile.exists()) {
                if(keepSent) {
                    logger.info("Moving email with id {} to sent directory", id);
                    FileUtils.moveToDirectory(emailFile, sentDirectory, false);
                } else {
                    logger.info("Deleting sent email with id {}", id);
                    if(!emailFile.delete()) {
                        throw new QueueException("Couldn't mark mail as sent");
                    }
                }
            } else {
                logger.debug("Not marking email with id {} as sent", id);
            }
        } catch (IOException e) {
            throw new Error("Couldn't mark mail as sent", e);
        }
    }

    public void markFailed(String id) throws QueueException {
        checkDirectories();
        try {
            File emailFile = getEmailFile(id);
            if(emailFile.exists()) {
                logger.info("Marking email with id {} as failed", id);
                FileUtils.moveToDirectory(emailFile, failedDirectory, false);
            } else {
                logger.debug("Not marking email with id {} as sent", id);
            }
        } catch (IOException e) {
            throw new QueueException("Couldn't mark mail as sent", e);
        }
    }

    public File getQueuedDirectory() {
        return queuedDirectory;
    }

    public File getSentDirectory() {
        return sentDirectory;
    }

    public File getFailedDirectory() {
        return failedDirectory;
    }

    public boolean isKeepSent() {
        return keepSent;
    }

    public void setKeepSent(boolean keepSent) {
        this.keepSent = keepSent;
    }
}
