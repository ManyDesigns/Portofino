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

import com.manydesigns.elements.blobs.Blob;
import com.manydesigns.elements.blobs.BlobManager;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.logic.TextLogic;
import com.manydesigns.portofino.model.pages.Attachment;
import com.manydesigns.portofino.model.pages.TextPage;
import net.sourceforge.stripes.action.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@UrlBinding("/text.action")
public class TextAction extends PortletAction {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";
    public static final String CONTENT_ENCODING = "UTF-8";
    public static final String EMPTY_STRING = "";

    public String title;
    public String content;
    public List<Blob> blobs;
    public String[] selection;

    //**************************************************************************
    // File upload with CKEditor
    //**************************************************************************

    public FileBean upload;
    public String CKEditor;
    public Integer CKEditorFuncNum;
    public String langCode;
    public String code;
    public String viewAttachmentUrl;
    public String message;

    //**************************************************************************
    // Injections
    //**************************************************************************

    public TextPage textPage;
    public BlobManager textManager;
    public BlobManager attachmentManager;
    public Blob textBlob;

    public static final Logger logger =
            LoggerFactory.getLogger(TextAction.class);

    //**************************************************************************
    // Setup
    //**************************************************************************

    @Before
    public void prepare() {
        textPage = (TextPage) pageInstance.getPage();
        String storageDirectory =
                portofinoConfiguration.getString(
                        PortofinoProperties.STORAGE_DIRECTORY);
        textManager = new BlobManager(
                storageDirectory, "text-{0}.properties", "text-{0}.data");
        attachmentManager = new BlobManager(
                storageDirectory, "attachment-{0}.properties", "attachment-{0}.data");
    }

    //**************************************************************************
    // Handlers
    //**************************************************************************


    @DefaultHandler
    public Resolution execute() throws IOException {
        loadContent();
        if (StringUtils.isEmpty(content)) {
            content = "<em>Empty content. To add content, configure this page.</em>";
        }
        setupBlobs();
        return forwardToPortletPage("/layouts/text/read.jsp");
    }

    public void setupBlobs() {
        blobs = new ArrayList<Blob>();
        for (Attachment attachment : textPage.getAttachments()) {
            Blob blob;
            try {
                blob = attachmentManager.loadBlob(attachment.getCode());
                blobs.add(blob);
            } catch (IOException e) {
                logger.warn("Cannot load attachment", e);
            }
        }
    }

    protected void loadContent() throws IOException {
        String textCode = textPage.getCode();
        if(textCode != null) {
            textBlob = textManager.loadBlob(textCode);
            File file = textBlob.getDataFile();
            String characterEncoding = textBlob.getCharacterEncoding();
            content = FileUtils.readFileToString(file, characterEncoding);
        } else {
            content = EMPTY_STRING;
        }
    }

    protected void saveContent() throws IOException {
        if (content == null) {
            content = EMPTY_STRING;
        }
        byte[] contentByteArray = content.getBytes(CONTENT_ENCODING);
        String textCode = textPage.getCode();
        if(textCode != null) {
            textBlob = textManager.updateBlob(textCode, contentByteArray, CONTENT_ENCODING);
        } else {
            textBlob = textManager.saveBlob(contentByteArray, null, "text/html", CONTENT_ENCODING);
        }
    }

    public Resolution configure() throws IOException {
        title = textPage.getTitle();
        loadContent();
        return new ForwardResolution("/layouts/text/configure.jsp");
    }

    public Resolution updateConfiguration() throws IOException {
        synchronized (application) {
            title = StringUtils.trimToNull(title);
            boolean valid = true;
            if (title == null) {
                SessionMessages.addErrorMessage("Title cannot be empty");
                valid = false;
            }
            if (valid) {
                textPage.setTitle(title);
                saveContent();
                textPage.setCode(textBlob.getCode());
                saveModel();
                SessionMessages.addInfoMessage("Configuration updated successfully");
                return cancel();
            } else {
                return new ForwardResolution("/layouts/text/configure.jsp");
            }
        }

    }

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
            Blob blob = attachmentManager.saveBlob(
                    upload.getInputStream(),
                    upload.getFileName(),
                    upload.getContentType(),
                    null);
            TextLogic.createAttachment(textPage, blob.getCode());
            viewAttachmentUrl =
                    String.format("%s?downloadAttachment=&code=%s",
                            dispatch.getAbsoluteOriginalPath(),
                            blob.getCode());
            saveModel();
        }
    }

    public Resolution viewAttachment() {
        // find the attachment
        Attachment attachment =
                TextLogic.findAttachmentByCode(textPage, code);

        if (attachment == null) {
            return new ErrorResolution(404, "Attachment not found");
        }

        try {
            Blob blob = attachmentManager.loadBlob(code);
            File file = blob.getDataFile();
            InputStream is = new FileInputStream(file);
            Resolution resolution =
                    new StreamingResolution(blob.getContentType(), is)
                    .setLength(blob.getSize())
                    .setAttachment(false);
            return resolution;
        } catch (IOException e) {
            logger.error("Download failed", e);
            return new ErrorResolution(500, "Attachment error");
        }
    }

    public Resolution downloadAttachment() {
        // find the attachment
        Attachment attachment =
                TextLogic.findAttachmentByCode(textPage, code);

        if (attachment == null) {
            return new ErrorResolution(404, "Attachment not found");
        }

        try {
            Blob blob = attachmentManager.loadBlob(code);
            File file = blob.getDataFile();
            InputStream is = new FileInputStream(file);
            Resolution resolution =
                    new StreamingResolution(blob.getContentType(), is)
                    .setLength(blob.getSize())
                    .setFilename(blob.getFilename())
                    .setAttachment(true);
            return resolution;
        } catch (IOException e) {
            logger.error("Download failed", e);
            return new ErrorResolution(500, "Attachment error");
        }
    }

    public Resolution browse() throws IOException {
        logger.info("Browse");
        setupBlobs();
        return new ForwardResolution("/layouts/text/browse.jsp");
    }

    public Resolution manageAttachments() {
        logger.info("Manage attachments");
        setupBlobs();
        return new ForwardResolution("/layouts/text/manage-attachments.jsp");
    }

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
                    attachmentManager.deleteBlob(code);

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


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public List<Blob> getBlobs() {
        return blobs;
    }

    public void setBlobs(List<Blob> blobs) {
        this.blobs = blobs;
    }

    public Blob getTextBlob() {
        return textBlob;
    }

    public void setTextBlob(Blob textBlob) {
        this.textBlob = textBlob;
    }
}
