/*
 * Copyright (C) 2005-2015 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.manydesigns.portofino.pageactions.text;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.servlet.ServletUtils;
import com.manydesigns.elements.util.RandomUtil;
import com.manydesigns.elements.util.Util;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.buttons.annotations.Buttons;
import com.manydesigns.portofino.dispatcher.DispatcherLogic;
import com.manydesigns.portofino.logic.SecurityLogic;
import com.manydesigns.portofino.pageactions.AbstractPageAction;
import com.manydesigns.portofino.pageactions.PageActionName;
import com.manydesigns.portofino.pageactions.annotations.ConfigurationClass;
import com.manydesigns.portofino.pageactions.annotations.ScriptTemplate;
import com.manydesigns.portofino.pageactions.text.configuration.Attachment;
import com.manydesigns.portofino.pageactions.text.configuration.TextConfiguration;
import com.manydesigns.portofino.pages.Page;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.security.RequiresPermissions;
import com.manydesigns.portofino.security.SupportsPermissions;
import net.sourceforge.stripes.action.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@RequiresPermissions(level = AccessLevel.VIEW)
@ScriptTemplate("script_template.groovy")
@ConfigurationClass(TextConfiguration.class)
@PageActionName("Text")
@SupportsPermissions({ TextAction.PERMISSION_EDIT })
public class TextAction extends AbstractPageAction {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";
    public static final String CONTENT_ENCODING = "UTF-8";
    public static final String EMPTY_STRING = "";
    public static final String TEXT_FILE_NAME_PATTERN = "{0}.html";
    public static final String ATTACHMENT_FILE_NAME_PATTERN = "{0}.data";

    public static final String PERMISSION_EDIT = "permission.text.edit";

    public String title;
    public String description;
    public String content;
    public String[] selection;
    public String[] downloadable;
    public boolean uploadDownloadable = true;

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

    public TextConfiguration textConfiguration;
    public File textFile;

    public static final Logger logger =
            LoggerFactory.getLogger(TextAction.class);

    //**************************************************************************
    // Setup
    //**************************************************************************

    public Resolution preparePage() {
        textConfiguration = (TextConfiguration) pageInstance.getConfiguration();
        if(!pageInstance.getParameters().isEmpty()) {
            if(pageInstance.getParameters().size() == 1 &&
               SecurityLogic.hasPermissions(
                       portofinoConfiguration, pageInstance, SecurityUtils.getSubject(), AccessLevel.EDIT)) {
                return new ForwardResolution("/m/pageactions/text/create-page.jsp");
            } else {
                return new ErrorResolution(404);
            }
        }
        return null;
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
        return new ForwardResolution("/m/pageactions/text/read.jsp");
    }

    /**
     * Loads the content of the page from a file (see {@link TextAction#locateTextFile()}).
     * Assigns the {@link TextAction#content} field.
     * @throws IOException if there's a problem loading the content.
     */
    protected void loadContent() throws IOException {
        textFile = locateTextFile();
        try {
            content = FileUtils.readFileToString(textFile, CONTENT_ENCODING);
            content = processContentBeforeView(content);
        } catch (FileNotFoundException e) {
            content = EMPTY_STRING;
            logger.debug("Content file not found. Content set to empty.", e);
        }
    }

    /**
     * Computes the File object used to store this page's content. The default implementation
     * looks for a file with a fixed name in this page's directory, computed by calling
     * {@link TextAction#computeTextFileName()}.
     * @see TextAction#computeTextFileName()
     * @return the document File.
     */
    protected File locateTextFile() {
        return new File(pageInstance.getDirectory(), computeTextFileName());
    }

    /**
     * Computes the name of the file used to store this page's content. The default implementation
     * returns "text.html".
     * @see com.manydesigns.portofino.pageactions.text.TextAction#locateTextFile()
     * @return the name of the file.
     */
    protected String computeTextFileName() {
        return RandomUtil.getCodeFileName(TEXT_FILE_NAME_PATTERN, "text");
    }

    /**
     * Saves the content of the page to a file (see {@link TextAction#locateTextFile()}).
     * @throws IOException if there's a problem saving the content.
     */
    protected void saveContent() throws IOException {
        if (content == null) {
            content = EMPTY_STRING;
        }
        content = processContentBeforeSave(content);
        byte[] contentByteArray = content.getBytes(CONTENT_ENCODING);
        textFile = locateTextFile();

        // copy the data
        FileOutputStream fileOutputStream = new FileOutputStream(textFile);
        try {
            long size = IOUtils.copyLarge(
                    new ByteArrayInputStream(contentByteArray), fileOutputStream);
        } catch (IOException e) {
            logger.error("Could not save content", e);
            throw e;
        } finally {
            fileOutputStream.close();
        }
        logger.info("Content saved to: {}", textFile.getAbsolutePath());
    }

    /**
     * Processes the content before saving it, in order to encode internal links and attachments, and
     * removing potentially problematic characters.
     * @param content the original content.
     * @return the processed content.
     */
    protected String processContentBeforeSave(String content) {
        content = processAttachmentUrls(content);
        content = processLocalUrls(content);
        content = Util.replaceBadUnicodeCharactersWithHtmlEntities(content);
        return content;
    }

    protected String processAttachmentUrls(String content) {
        String baseUrl = StringEscapeUtils.escapeHtml(generateViewAttachmentUrl("").replace("?", "\\?"));
        String patternString = "([^\\s\"]+)\\s*=\\s*\"\\s*" + baseUrl + "([^\"]+)\"";
        Pattern pattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);
        int lastEnd = 0;
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String hrefAttribute = matcher.group(1);
            String attachmentId = matcher.group(2);
            sb.append(content.substring(lastEnd, matcher.start()));
            sb.append("portofino:attachment=\"").append(attachmentId).append("\" ");
            sb.append("portofino:hrefAttribute=\"").append(hrefAttribute).append("\"");
            lastEnd = matcher.end();
        }
        sb.append(content.substring(lastEnd));
        return sb.toString();
    }

    protected static final String BASE_USER_URL_PATTERN =
            "(href|src)\\s*=\\s*\"\\s*((http(s)?://)?((HOSTS)(:\\d+)?)?)?((/[^/?\"]*)+)(\\?[^\"]*)?\\s*\"";

    protected String processLocalUrls(String content) {
        List<String> hosts = new ArrayList<String>();
        hosts.add(context.getRequest().getLocalAddr());
        hosts.add(context.getRequest().getLocalName());
        hosts.addAll((List)portofinoConfiguration.getList(PortofinoProperties.HOSTNAMES));
        String patternString = BASE_USER_URL_PATTERN.replace("HOSTS", "(" + StringUtils.join(hosts, ")|(") + ")");
        Pattern pattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);
        int lastEnd = 0;
        StringBuilder sb = new StringBuilder();
        String contextPath = context.getRequest().getContextPath();
        while (matcher.find()) {
            String attribute = matcher.group(1);
            String path = matcher.group(8 + hosts.size());
            assert path.startsWith("/");
            String queryString = matcher.group(10 + hosts.size());
            String hostAndPort = matcher.group(5);
            if(!StringUtils.isBlank(hostAndPort) && !path.startsWith(contextPath)) {
                logger.debug("Path refers to another web application on the same host, skipping: {}", path);
                continue;
            }

            sb.append(content.substring(lastEnd, matcher.start()));
            sb.append("portofino:hrefAttribute=\"").append(attribute).append("\"");

            if(path.startsWith(contextPath)) {
                path = path.substring(contextPath.length());
            }

            //path = convertPathToInternalLink(path);
            sb.append(" portofino:link=\"").append(path).append("\"");
            if(!StringUtils.isBlank(queryString)) {
                sb.append(" portofino:queryString=\"").append(queryString).append("\"");
            }

            lastEnd = matcher.end();
        }

        sb.append(content.substring(lastEnd));

        return sb.toString();
    }

    @Override
    @Buttons({
        @Button(list = "configuration", key = "cancel", order = 99),
        @Button(list = "edit-content",  key = "cancel", order = 99)})
    public Resolution cancel() {
        return super.cancel();
    }

    protected String processContentBeforeView(String content) {
        content = restoreAttachmentUrls(content);
        content = restoreLocalUrls(content);
        return content;
    }

    protected static final String PORTOFINO_ATTACHMENT_PATTERN =
            "portofino:attachment=\"([^\"]+)\"( portofino:hrefAttribute=\"([^\"]+)\")?";

    protected String restoreAttachmentUrls(String content) {
        Pattern pattern = Pattern.compile(PORTOFINO_ATTACHMENT_PATTERN);
        Matcher matcher = pattern.matcher(content);
        int lastEnd = 0;
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String attachmentId = matcher.group(1);
            //Default to src for old texts
            String hrefAttribute =
                    (matcher.groupCount() >= 3 && matcher.group(3) != null) ? matcher.group(3) : "src";

            sb.append(content.substring(lastEnd, matcher.start()))
              .append(hrefAttribute).append("=\"")
              .append(StringEscapeUtils.escapeHtml(generateViewAttachmentUrl(attachmentId)))
              .append("\"");

            lastEnd = matcher.end();
        }
        sb.append(content.substring(lastEnd));
        return sb.toString();
    }

    protected static final String PORTOFINO_HREF_PATTERN =
            "portofino:hrefAttribute=\"([^\"]+)\" " +
            "portofino:link=\"([^\"]+)\"" +
            "( portofino:queryString=\"([^\"]+)\")?";

    protected String restoreLocalUrls(String content) {
        Pattern pattern = Pattern.compile(PORTOFINO_HREF_PATTERN);
        Matcher matcher = pattern.matcher(content);
        int lastEnd = 0;
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String attribute = matcher.group(1);
            String link = matcher.group(2);
            String queryString = matcher.group(4);

            sb.append(content.substring(lastEnd, matcher.start()));
            sb.append(attribute).append("=\"");

            sb.append(context.getRequest().getContextPath());
            //link = convertInternalLinkToPath(link);
            sb.append(link);
            if(!StringUtils.isBlank(queryString)) {
                sb.append(queryString);
            }
            sb.append("\"");

            lastEnd = matcher.end();
        }

        sb.append(content.substring(lastEnd));

        return sb.toString();
    }

    @Button(list = "pageHeaderButtons", titleKey = "edit", order = 2, icon = Button.ICON_EDIT,
            group = "pageHeaderButtons")
    @RequiresPermissions(level = AccessLevel.VIEW, permissions = { PERMISSION_EDIT })
    @RequiresAuthentication
    public Resolution configure() {
        title = pageInstance.getPage().getTitle();
        description = pageInstance.getPage().getDescription();
        try {
            loadContent();
            logger.debug("Edit content: {}", textFile.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Could not load content", e);
            SessionMessages.addErrorMessage("Could not load content: " + e);
        }
        return new ForwardResolution("/m/pageactions/text/edit-content.jsp");
    }

    @Button(list = "pageHeaderButtons", titleKey = "configure", order = 1, icon = Button.ICON_WRENCH,
            group = "pageHeaderButtons")
    @RequiresPermissions(level = AccessLevel.EDIT)
    @RequiresAuthentication
    public Resolution configurePage() {
        prepareConfigurationForms();
        return new ForwardResolution("/m/pageactions/text/configure.jsp");
    }

    @Button(list = "configuration", key = "update.configuration", order = 1, type = Button.TYPE_PRIMARY)
    @RequiresPermissions(level = AccessLevel.DEVELOP)
    @RequiresAuthentication
    public Resolution updateConfiguration() throws IOException {
        prepareConfigurationForms();
        readPageConfigurationFromRequest();
        boolean valid = validatePageConfiguration();
        if (valid) {
            updatePageConfiguration();
            SessionMessages.addInfoMessage(ElementsThreadLocals.getText("configuration.updated.successfully"));
            return cancel();
        } else {
            return new ForwardResolution("/m/pageactions/text/configure.jsp");
        }
    }

    @Button(list = "manage-attachments-upload", key = "text.attachment.upload", order = 1 , icon=Button.ICON_UPLOAD )
    @RequiresPermissions(level = AccessLevel.VIEW, permissions = { PERMISSION_EDIT })
    @RequiresAuthentication
    public Resolution uploadAttachment() {
        if (upload == null) {
            SessionMessages.addWarningMessage(ElementsThreadLocals.getText("text.attachment.noFileSelected"));
        } else {
            try {
                commonUploadAttachment();
                SessionMessages.addInfoMessage(ElementsThreadLocals.getText("text.attachment.uploadSuccessful"));
            } catch (IOException e) {
                logger.error("Upload failed", e);
                SessionMessages.addErrorMessage(ElementsThreadLocals.getText("text.attachment.uploadFailed"));
            }
        }
        return new RedirectResolution(context.getActionPath())
                .addParameter("manageAttachments")
                .addParameter("returnUrl", returnUrl);
    }

    @RequiresPermissions(level = AccessLevel.VIEW, permissions = { PERMISSION_EDIT })
    @RequiresAuthentication
    public Resolution uploadAttachmentFromCKEditor() {
        try {
            uploadDownloadable = false;
            commonUploadAttachment();
            message = null;
        } catch (IOException e) {
            message = "File upload failed!";
            logger.error("Upload failed", e);
        }
        return new ForwardResolution(
                "/m/pageactions/text/upload-attachment.jsp");
    }

    protected void commonUploadAttachment() throws IOException {
        logger.debug("Uploading attachment");
        viewAttachmentUrl = null;
        InputStream attachmentStream = upload.getInputStream();
        String attachmentId = RandomUtil.createRandomId();
        File dataFile = RandomUtil.getCodeFile(
                pageInstance.getDirectory(), ATTACHMENT_FILE_NAME_PATTERN, attachmentId);

        // copy the data
        FileOutputStream fileOutputStream = new FileOutputStream(dataFile);
        IOUtils.copyLarge(attachmentStream, fileOutputStream);
        if(textConfiguration == null) {
            textConfiguration = new TextConfiguration();
        }
        Attachment attachment = TextLogic.createAttachment(
                textConfiguration, attachmentId,
                upload.getFileName(), upload.getContentType(),
                upload.getSize());
        attachment.setDownloadable(uploadDownloadable);
        viewAttachmentUrl =
                generateViewAttachmentUrl(attachmentId);
        saveConfiguration(textConfiguration);
        logger.info("Attachment uploaded: " + upload.getFileName() + " (" + attachmentId + ")");
        IOUtils.closeQuietly(attachmentStream);
        IOUtils.closeQuietly(fileOutputStream);
        upload.delete();
        logger.debug("Upload resources cleaned");
    }

    protected String generateViewAttachmentUrl(String attachmentId) {
        return String.format("%s?viewAttachment=&id=%s",
                Util.getAbsoluteUrl(context.getActionPath()),
                attachmentId);
    }

    public Resolution viewAttachment() {
        return streamAttachment(false);
    }

    public Resolution downloadAttachment() {
        return streamAttachment(true);
    }

    protected Resolution streamAttachment(boolean isAttachment) {
        // find the attachment
        Attachment attachment =
                TextLogic.findAttachmentById(textConfiguration, id);

        if (attachment == null) {
            return new ErrorResolution(404, "Attachment not found");
        }

        try {
            String attachmentId = attachment.getId();
            final File file = RandomUtil.getCodeFile(
                    pageInstance.getDirectory(), ATTACHMENT_FILE_NAME_PATTERN, attachmentId);

            //Cache
            HttpServletResponse response = context.getResponse();
            ServletUtils.markCacheableForever(response);

            //Suggerisce al browser di usare la risorsa che ha in cache invece di riscaricarla - serve ancora?
            HttpServletRequest request = context.getRequest();
            if(request.getHeader("If-Modified-Since") != null) {
                long ifModifiedSince = request.getDateHeader("If-Modified-Since");
                if(ifModifiedSince >= file.lastModified()) {
                    return new ErrorResolution(304); //Not modified
                }
            }

            InputStream is = new FileInputStream(file);
            StreamingResolution resolution =
                    new StreamingResolution(attachment.getContentType(), is)
                            .setLength(attachment.getSize())
                            .setFilename(attachment.getFilename())
                            .setAttachment(isAttachment)
                            .setLastModified(file.lastModified());
            return resolution;
        } catch (IOException e) {
            logger.error("Download failed", e);
            return new ErrorResolution(500, "Attachment error");
        }
    }

    @RequiresPermissions(level = AccessLevel.VIEW, permissions = { PERMISSION_EDIT })
    public Resolution browse() {
        logger.debug("Browse");
        return new ForwardResolution("/m/pageactions/text/browse.jsp");
    }

    @RequiresPermissions(level = AccessLevel.VIEW, permissions = { PERMISSION_EDIT })
    public Resolution browsePages() {
        logger.debug("Browse Pages");
        return new ForwardResolution("/m/pageactions/text/browsePages.jsp");
    }

    @Button(list = "pageHeaderButtons", titleKey = "manage.attachments", order = 3,
            icon = Button.ICON_PICTURE, group = "pageHeaderButtons")
    @RequiresPermissions(level = AccessLevel.VIEW, permissions = { PERMISSION_EDIT })
    public Resolution manageAttachments() {
        logger.debug("Manage attachments");
        return new ForwardResolution("/m/pageactions/text/manage-attachments.jsp");
    }

    @Button(list = "edit-content", key = "update", order = 1, type = Button.TYPE_PRIMARY)
    @RequiresPermissions(level = AccessLevel.VIEW, permissions = { PERMISSION_EDIT })
    public Resolution updateContent() {
        title = context.getRequest().getParameter("title");
        title = StringUtils.trimToNull(title);
        if (title == null) {
            SessionMessages.addErrorMessage(ElementsThreadLocals.getText("title.cannot.be.empty"));
            return new ForwardResolution("/m/pageactions/text/edit-content.jsp");
        }

        description = context.getRequest().getParameter("description");
        description = StringUtils.trimToNull(description);

        Page page = pageInstance.getPage();
        page.setTitle(title);
        page.setDescription(description);
        try {
            DispatcherLogic.savePage(pageInstance.getDirectory(), page);
            saveContent();
            SessionMessages.addInfoMessage(ElementsThreadLocals.getText("object.updated.successfully"));
        } catch (Exception e) {
            logger.error("Could not save content for page " + pageInstance.getPath(), e);
            SessionMessages.addInfoMessage(ElementsThreadLocals.getText("update.failed"));
        }
        return cancel();
    }

    @Button(list = "manage-attachments-delete", key = "delete", order = 1 , icon = Button.ICON_TRASH )
    @RequiresPermissions(level = AccessLevel.VIEW, permissions = { PERMISSION_EDIT })
    @RequiresAuthentication
    public Resolution deleteAttachments() {
        if (selection == null || selection.length == 0) {
            SessionMessages.addWarningMessage(ElementsThreadLocals.getText("text.attachment.noAttachmentSelected"));
        } else {
            int counter = 0;
            for (String code : selection) {
                Attachment attachment =
                        TextLogic.deleteAttachmentByCode(textConfiguration, code);
                if (attachment == null) {
                    logger.warn("Ignoring non-existing attachment with code: {}", code);
                    continue;
                }
                File file = RandomUtil.getCodeFile(
                        pageInstance.getDirectory(), ATTACHMENT_FILE_NAME_PATTERN, attachment.getId());
                if(!FileUtils.deleteQuietly(file)) {
                    logger.warn("File wasn't deleted: {}", file.getAbsolutePath());
                }
                counter++;
            }
            saveConfiguration(textConfiguration);
            if (counter == 1) {
                SessionMessages.addInfoMessage(ElementsThreadLocals.getText("text.attachment.oneDeleted"));
            } else if (counter > 1) {
                SessionMessages.addInfoMessage(
                                ElementsThreadLocals.getText("text.attachment.nDeleted", counter));
            }
        }
        return new RedirectResolution(context.getActionPath())
                .addParameter("manageAttachments")
                .addParameter("returnUrl", returnUrl);
    }

    @Button(list = "manage-attachments", key = "ok", order = 1, type = Button.TYPE_PRIMARY)
    @RequiresPermissions(level = AccessLevel.VIEW, permissions = { PERMISSION_EDIT })
    public Resolution saveAttachments() {
        if(downloadable == null) {
            downloadable = new String[0];
        }
        if(textConfiguration == null) {
            textConfiguration = new TextConfiguration();
        }
        for(Attachment attachment : textConfiguration.getAttachments()) {
            boolean contained = ArrayUtils.contains(downloadable, attachment.getId());
            attachment.setDownloadable(contained);
        }
        saveConfiguration(textConfiguration);
        return cancel();
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

    public String getDescription() {
        return title;
    }

    public void setDescription(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public TextConfiguration getTextConfiguration() {
        return textConfiguration;
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

    public List<Attachment> getDownloadableAttachments() {
        List<Attachment> downloadableAttachments = new ArrayList<Attachment>();
        for(Attachment attachment : getTextConfiguration().getAttachments()) {
            if(attachment.isDownloadable()) {
                downloadableAttachments.add(attachment);
            }
        }
        return downloadableAttachments;
    }
}
