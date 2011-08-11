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
import com.manydesigns.portofino.ApplicationAttributes;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.context.ServerInfo;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.logic.DocumentLogic;
import com.manydesigns.portofino.model.site.Attachment;
import com.manydesigns.portofino.model.site.DocumentNode;
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
@UrlBinding("/document.action")
public class DocumentAction extends PortletAction {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";
    public static final String CONTENT_ENCODING = "UTF-8";

    public String title;
    public String content;
    public List<Blob> blobs;

    //**************************************************************************
    // File upload with CKEditor
    //**************************************************************************

    public FileBean upload;
    public String CKEditor;
    public Integer CKEditorFuncNum;
    public String langCode;
    public String code;
    public String downloadAttachmentUrl;
    public String message;

    //**************************************************************************
    // Injections
    //**************************************************************************

    @Inject(ApplicationAttributes.SERVER_INFO)
    public ServerInfo serverInfo;

    public DocumentNode documentNode;
    public BlobManager documentManager;
    public BlobManager attachmentManager;
    public Blob documentBlob;

    public static final Logger logger =
            LoggerFactory.getLogger(DocumentAction.class);

    //**************************************************************************
    // Setup
    //**************************************************************************

    @Before
    public void prepare() {
        documentNode = (DocumentNode) siteNodeInstance.getSiteNode();
        String storageDirectory =
                portofinoConfiguration.getString(
                        PortofinoProperties.STORAGE_DIRECTORY);
        documentManager = new BlobManager(
                storageDirectory, "document-{0}.properties", "document-{0}.data");
        attachmentManager = new BlobManager(
                storageDirectory, "attachment-{0}.properties", "attachment-{0}.data");
    }

    //**************************************************************************
    // Handlers
    //**************************************************************************


    @DefaultHandler
    public Resolution execute() throws IOException {
        loadContent();
        setupBlobs();
        return forwardToPortletPage("/layouts/document/read.jsp");
    }

    public void setupBlobs() {
        blobs = new ArrayList<Blob>();
        for (Attachment attachment : documentNode.getAttachments()) {
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
        String documentCode = documentNode.getFileName();
        documentBlob = documentManager.loadBlob(documentCode);
        File file = documentBlob.getDataFile();
        String characterEncoding = documentBlob.getCharacterEncoding();
        content = FileUtils.readFileToString(file, characterEncoding);
    }

    protected void saveContent() throws IOException {
        String documentCode = documentNode.getFileName();
        byte[] contentByteArray = content.getBytes(CONTENT_ENCODING);
        documentManager.updateBlob(documentCode, contentByteArray, CONTENT_ENCODING);
    }

    public Resolution configure() throws IOException {
        title = documentNode.getTitle();
        loadContent();
        return new ForwardResolution("/layouts/document/configure.jsp");
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
                documentNode.setTitle(title);
                saveContent();
                saveModel();
                SessionMessages.addInfoMessage("Configuration updated successfully");
                return cancel();
            } else {
                return new ForwardResolution("/layouts/document/configure.jsp");
            }
        }

    }

    public Resolution uploadAttachment() {
        synchronized (application) {
            logger.info("Uploading attachment");
            try {
                Blob blob = attachmentManager.saveBlob(
                        upload.getInputStream(),
                        upload.getFileName(),
                        upload.getContentType(),
                        null);
                DocumentLogic.createAttachment(documentNode, blob.getCode());
                downloadAttachmentUrl =
                        String.format("%s?downloadAttachment=&code=%s",
                                dispatch.getAbsoluteOriginalPath(),
                                blob.getCode());
                message = null;
                saveModel();
            } catch (IOException e) {
                downloadAttachmentUrl = null;
                message = "File upload failed!";
                logger.error("Upload failed", e);
            }
            return new ForwardResolution(
                    "/layouts/document/upload-attachment.jsp");
        }
    }

    public Resolution downloadAttachment() {
        // find the attachment
        Attachment attachment =
                DocumentLogic.findAttachmentByCode(documentNode, code);

        if (attachment == null) {
            return new ErrorResolution(404, "Attachment not found");
        }

        try {
            Blob blob = attachmentManager.loadBlob(code);
            File file = blob.getDataFile();
            InputStream is = new FileInputStream(file);
            Resolution resolution =
                    new StreamingResolution(blob.getContentType(), is)
                    .setLength(blob.getSize());
            return resolution;
        } catch (IOException e) {
            logger.error("Download failed", e);
            return new ErrorResolution(500, "Attachment error");
        }
    }

    public Resolution browse() throws IOException {
        logger.info("Browse");
        setupBlobs();
        return new ForwardResolution("/layouts/document/browse.jsp");
    }

    public Resolution manageAttachments() {
        logger.info("Manage attachments");
        setupBlobs();
        return new ForwardResolution("/layouts/document/manage-attachments.jsp");
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

    public DocumentNode getDocumentNode() {
        return documentNode;
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

    public String getDownloadAttachmentUrl() {
        return downloadAttachmentUrl;
    }

    public void setDownloadAttachmentUrl(String downloadAttachmentUrl) {
        this.downloadAttachmentUrl = downloadAttachmentUrl;
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

    public Blob getDocumentBlob() {
        return documentBlob;
    }

    public void setDocumentBlob(Blob documentBlob) {
        this.documentBlob = documentBlob;
    }
}
