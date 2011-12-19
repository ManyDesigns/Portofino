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
package com.manydesigns.portofino.actions;

import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.util.RandomUtil;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.logic.TextLogic;
import com.manydesigns.portofino.model.pages.AccessLevel;
import com.manydesigns.portofino.model.pages.Attachment;
import com.manydesigns.portofino.model.pages.TextPage;
import com.manydesigns.portofino.system.model.users.annotations.RequiresPermissions;
import net.sourceforge.stripes.action.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@UrlBinding("/actions/text")
@RequiresPermissions(level = AccessLevel.VIEW)
public class TextAction extends PortletAction {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";
    public static final String CONTENT_ENCODING = "UTF-8";
    public static final String EMPTY_STRING = "";
    public static final String TEXT_FILE_NAME_PATTERN = "{0}.html";
    public static final String ATTACHMENT_FILE_NAME_PATTERN = "{0}.data";

    public String content;
    public String[] selection;

    //**************************************************************************
    // File upload with CKEditor
    //**************************************************************************

    public FileBean upload;
    public String CKEditor;
    public Integer CKEditorFuncNum;
    public String langCode;
    public String id;
    public String viewAttachmentUrl;
    public String message;

    //**************************************************************************
    // Injections
    //**************************************************************************

    public TextPage textPage;
    public File textFile;

    public static final Logger logger =
            LoggerFactory.getLogger(TextAction.class);

    //**************************************************************************
    // Setup
    //**************************************************************************

    @Before
    @Override
    public void prepare() {
        super.prepare();
        textPage = (TextPage) pageInstance.getPage();
    }

    //**************************************************************************
    // Handlers
    //**************************************************************************


    @DefaultHandler
    @RequiresPermissions(level = AccessLevel.VIEW)
    public Resolution execute() throws IOException {
        loadContent();
        if (StringUtils.isEmpty(content)) {
            content = "<em>Empty content. To add content, configure this page.</em>";
        }
        if (isEmbedded()) {
            return new ForwardResolution("/layouts/text/read.jsp");
        } else {
            return forwardToPortletPage("/layouts/text/read.jsp");
        }
    }

    protected void loadContent() throws IOException {
        String textCode = textPage.getId();
        textFile = RandomUtil.getCodeFile(application.getAppTextDir(), TEXT_FILE_NAME_PATTERN, textCode);
        try {
            content = FileUtils.readFileToString(textFile, CONTENT_ENCODING);
        } catch (FileNotFoundException e) {
            content = EMPTY_STRING;
            logger.debug("Content file not found. Content set to empty.", e);
        }
    }

    protected void saveContent() throws IOException {
        if (content == null) {
            content = EMPTY_STRING;
        }
        byte[] contentByteArray = content.getBytes(CONTENT_ENCODING);
        String textCode = textPage.getId();
        File dataFile =
                RandomUtil.getCodeFile(application.getAppTextDir(), TEXT_FILE_NAME_PATTERN, textCode);

        // copy the data
        long size = IOUtils.copyLarge(
                new ByteArrayInputStream(contentByteArray), new FileOutputStream(dataFile));
    }

    @Button(list = "portletHeaderButtons", key = "commons.configure", order = 1)
    @RequiresPermissions(level = AccessLevel.EDIT)
    public Resolution configure() throws IOException {
        prepareConfigurationForms();
        loadContent();
        return new ForwardResolution("/layouts/text/configure.jsp");
    }

    @Button(list = "configuration", key = "commons.updateConfiguration")
    @RequiresPermissions(level = AccessLevel.EDIT)
    public Resolution updateConfiguration() throws IOException {
        synchronized (application) {
            prepareConfigurationForms();
            readPageConfigurationFromRequest();
            boolean valid = validatePageConfiguration();
            if (valid) {
                updatePageConfiguration();
                saveContent();
                saveModel();
                SessionMessages.addInfoMessage("Configuration updated successfully");
                return cancel();
            } else {
                return new ForwardResolution("/layouts/text/configure.jsp");
            }
        }

    }

    @RequiresPermissions(level = AccessLevel.EDIT)
    public Resolution uploadAttachment() {
        if (upload == null) {
            SessionMessages.addWarningMessage("No file selected for upload");
        } else {
            try {
                commonUploadAttachment();
                SessionMessages.addInfoMessage("File uploaded successfully");
            } catch (IOException e) {
                logger.error("Upload failed", e);
                SessionMessages.addErrorMessage("Upload failed!");
            }
        }
        return new RedirectResolution(dispatch.getOriginalPath())
                .addParameter("manageAttachments")
                .addParameter("cancelReturnUrl", cancelReturnUrl);
    }

    @RequiresPermissions(level = AccessLevel.EDIT)
    public Resolution uploadAttachmentFromCKEditor() {
        try {
            commonUploadAttachment();
            message = null;
        } catch (IOException e) {
            message = "File upload failed!";
            logger.error("Upload failed", e);
        }
        return new ForwardResolution(
                "/layouts/text/upload-attachment.jsp");
    }

    protected void commonUploadAttachment() throws IOException {
        viewAttachmentUrl = null;
        synchronized (application) {
            logger.info("Uploading attachment");
            InputStream attachmentStream = upload.getInputStream();
            String attachmentId = RandomUtil.createRandomId();
            File dataFile = RandomUtil.getCodeFile(
                    application.getAppStorageDir(), ATTACHMENT_FILE_NAME_PATTERN, attachmentId);

            // copy the data
            IOUtils.copyLarge(attachmentStream, new FileOutputStream(dataFile));
            TextLogic.createAttachment(
                    textPage, attachmentId,
                    upload.getFileName(), upload.getContentType(),
                    upload.getSize());
            viewAttachmentUrl =
                    String.format("%s?viewAttachment=&id=%s",
                            dispatch.getAbsoluteOriginalPath(),
                            attachmentId);
            saveModel();
        }
    }

    @RequiresPermissions(level = AccessLevel.VIEW)
    public Resolution viewAttachment() {
        // find the attachment
        Attachment attachment =
                TextLogic.findAttachmentById(textPage, id);

        if (attachment == null) {
            return new ErrorResolution(404, "Attachment not found");
        }

        try {
            String attachmentId = attachment.getId();
            File file = RandomUtil.getCodeFile(
                    application.getAppStorageDir(), ATTACHMENT_FILE_NAME_PATTERN, attachmentId);
            InputStream is = new FileInputStream(file);
            Resolution resolution =
                    new StreamingResolution(attachment.getContentType(), is)
                    .setLength(attachment.getSize())
                    .setAttachment(false);
            return resolution;
        } catch (IOException e) {
            logger.error("Download failed", e);
            return new ErrorResolution(500, "Attachment error");
        }
    }

    @RequiresPermissions(level = AccessLevel.VIEW)
    public Resolution downloadAttachment() {
        // find the attachment
        Attachment attachment =
                TextLogic.findAttachmentById(textPage, id);

        if (attachment == null) {
            return new ErrorResolution(404, "Attachment not found");
        }

        try {
            String attachmentId = attachment.getId();
            File file = RandomUtil.getCodeFile(
                    application.getAppStorageDir(), ATTACHMENT_FILE_NAME_PATTERN, attachmentId);
            InputStream is = new FileInputStream(file);
            Resolution resolution =
                    new StreamingResolution(attachment.getContentType(), is)
                    .setLength(attachment.getSize())
                    .setFilename(attachment.getFilename())
                    .setAttachment(true);
            return resolution;
        } catch (IOException e) {
            logger.error("Download failed", e);
            return new ErrorResolution(500, "Attachment error");
        }
    }

    @RequiresPermissions(level = AccessLevel.EDIT)
    public Resolution browse() throws IOException {
        logger.info("Browse");
        return new ForwardResolution("/layouts/text/browse.jsp");
    }

    @Button(list = "portletHeaderButtons", key = "layouts.text.manage-attachments.manage_attachments_for_page", order = 2)
    @RequiresPermissions(level = AccessLevel.EDIT)
    public Resolution manageAttachments() {
        logger.info("Manage attachments");
        return new ForwardResolution("/layouts/text/manage-attachments.jsp");
    }

    @RequiresPermissions(level = AccessLevel.EDIT)
    public Resolution deleteAttachments() {
        if (selection == null || selection.length == 0) {
            SessionMessages.addWarningMessage("No attachments selected");
        } else {
            synchronized (application) {
                int counter = 0;
                for (String code : selection) {
                    Attachment attachment =
                            TextLogic.deleteAttachmentByCode(textPage, code);
                    if (attachment == null) {
                        logger.warn("Ignoring non-existing attachment with code: {}", code);
                        continue;
                    }
                    File file = RandomUtil.getCodeFile(application.getAppStorageDir(), ATTACHMENT_FILE_NAME_PATTERN, attachment.getId());
                    if(!FileUtils.deleteQuietly(file)) {
                        logger.warn("File wasn't deleted: {}", file.getAbsolutePath());
                    }

                    counter++;
                }
                saveModel();
                if (counter == 1) {
                    SessionMessages.addInfoMessage("1 attachment deleted successfully");
                } else if (counter > 1) {
                    SessionMessages.addInfoMessage(
                            String.format(
                                    "%d attachments deleted successfully",
                                    counter));
                }
            }
        }
        return new RedirectResolution(dispatch.getOriginalPath())
                .addParameter("manageAttachments")
                .addParameter("cancelReturnUrl", cancelReturnUrl);
    }

    //**************************************************************************
    // Getters/setters
    //**************************************************************************


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public TextPage getTextPage() {
        return textPage;
    }

    public FileBean getUpload() {
        return upload;
    }

    public void setUpload(FileBean upload) {
        this.upload = upload;
    }

    public String getCKEditor() {
        return CKEditor;
    }

    public void setCKEditor(String CKEditor) {
        this.CKEditor = CKEditor;
    }

    public Integer getCKEditorFuncNum() {
        return CKEditorFuncNum;
    }

    public void setCKEditorFuncNum(Integer CKEditorFuncNum) {
        this.CKEditorFuncNum = CKEditorFuncNum;
    }

    public String getLangCode() {
        return langCode;
    }

    public void setLangCode(String langCode) {
        this.langCode = langCode;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getViewAttachmentUrl() {
        return viewAttachmentUrl;
    }

    public void setViewAttachmentUrl(String viewAttachmentUrl) {
        this.viewAttachmentUrl = viewAttachmentUrl;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public File getTextFile() {
        return textFile;
    }

    public void setTextFile(File textFile) {
        this.textFile = textFile;
    }
}
