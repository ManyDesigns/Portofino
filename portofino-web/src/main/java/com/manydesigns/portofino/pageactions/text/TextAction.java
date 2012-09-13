/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
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
package com.manydesigns.portofino.pageactions.text;

import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.util.RandomUtil;
import com.manydesigns.elements.util.Util;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.buttons.annotations.Buttons;
import com.manydesigns.portofino.logic.SecurityLogic;
import com.manydesigns.portofino.pageactions.AbstractPageAction;
import com.manydesigns.portofino.pageactions.PageActionName;
import com.manydesigns.portofino.pageactions.annotations.ConfigurationClass;
import com.manydesigns.portofino.pageactions.annotations.ScriptTemplate;
import com.manydesigns.portofino.pageactions.text.configuration.Attachment;
import com.manydesigns.portofino.pageactions.text.configuration.TextConfiguration;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.security.RequiresPermissions;
import net.sourceforge.stripes.action.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class TextAction extends AbstractPageAction {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";
    public static final String CONTENT_ENCODING = "UTF-8";
    public static final String EMPTY_STRING = "";
    public static final String TEXT_FILE_NAME_PATTERN = "{0}.html";
    public static final String ATTACHMENT_FILE_NAME_PATTERN = "{0}.data";

    public String content;
    public String[] selection;
    public String[] downloadable;

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
        Resolution resolution = super.preparePage();
        if(resolution != null) {
            return resolution;
        }
        textConfiguration = (TextConfiguration) pageInstance.getConfiguration();
        if(!pageInstance.getParameters().isEmpty()) {
            if(pageInstance.getParameters().size() == 1 &&
               !isEmbedded() &&
               SecurityLogic.hasPermissions(pageInstance, SecurityUtils.getSubject(), AccessLevel.EDIT)) {
                return new ForwardResolution("/layouts/text/create-page.jsp");
            } else {
                return portletPageNotFound();
            }
        }
        return null;
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
        textFile = RandomUtil.getCodeFile(pageInstance.getDirectory(), TEXT_FILE_NAME_PATTERN, "text");
        try {
            content = FileUtils.readFileToString(textFile, CONTENT_ENCODING);
            content = processContentBeforeView(content);
        } catch (FileNotFoundException e) {
            content = EMPTY_STRING;
            logger.debug("Content file not found. Content set to empty.", e);
        }
    }

    protected void saveContent() throws IOException {
        if (content == null) {
            content = EMPTY_STRING;
        }
        content = processContentBeforeSave(content);
        byte[] contentByteArray = content.getBytes(CONTENT_ENCODING);
        File dataFile =
                textFile = RandomUtil.getCodeFile(pageInstance.getDirectory(), TEXT_FILE_NAME_PATTERN, "text");

        // copy the data
        FileOutputStream fileOutputStream = new FileOutputStream(dataFile);
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
        hosts.addAll(application.getPortofinoProperties().getList(PortofinoProperties.HOSTNAMES));
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
        @Button(list = "configuration", key = "commons.cancel", order = 99),
        @Button(list = "edit-content",  key = "commons.cancel", order = 99)})
    public Resolution cancel() {
        return super.cancel();
    }

    /*protected String convertPathToInternalLink(String path) {
        Dispatcher dispatcher = new Dispatcher(application);
        Dispatch pathDispatch = dispatcher.createDispatch(context.getRequest().getContextPath(), path);
        PageInstance[] pageInstancePath = pathDispatch.getPageInstancePath();
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (PageInstance current : pageInstancePath) {
            String pageId = current.getPage().getId();
            String fragment = current.formatUrlFragment(pageId);
            if (first) {
                first = false;
                // ignore fragment of root node
            } else {
                sb.append("/");
                sb.append(fragment);
            }
        }
        return sb.toString();
    }*/

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
            String hrefAttribute = matcher.groupCount() > 3 ? matcher.group(3) : "src";

            sb.append(content.substring(lastEnd, matcher.start()))
              .append(hrefAttribute).append("=\"")
              .append(generateViewAttachmentUrl(attachmentId))
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

    /*protected String convertInternalLinkToPath(String link) {
        Dispatcher dispatcher = new Dispatcher(application) {
            @Override
            protected String getFragmentToMatch(Page page) {
                return page.getId();
            }
            @Override
            protected Dispatch checkDispatch(Dispatch dispatch) {
                return dispatch;
            }
        };
        Dispatch pathDispatch = dispatcher.createDispatch(context.getRequest().getContextPath(), link);
        return pathDispatch.getPathUrl();
    }*/

    @Button(list = "portletHeaderButtons", key = "commons.configure", order = 1, icon = "ui-icon-wrench")
    @RequiresPermissions(level = AccessLevel.EDIT)
    public Resolution configure() {
        prepareConfigurationForms();
        try {
            loadContent();
            logger.info("Edit content: {}", textFile.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Could not load content", e);
            SessionMessages.addErrorMessage("Could not load content: " + e);
        }
        return new ForwardResolution("/layouts/text/configure.jsp");
    }

    @Button(list = "configuration", key = "commons.updateConfiguration")
    @RequiresPermissions(level = AccessLevel.EDIT)
    public Resolution updateConfiguration() throws IOException {
        prepareConfigurationForms();
        readPageConfigurationFromRequest();
        boolean valid = validatePageConfiguration();
        if (valid) {
            updatePageConfiguration();
            saveContent();
            SessionMessages.addInfoMessage(getMessage("commons.configuration.updated"));
            return cancel();
        } else {
            return new ForwardResolution("/layouts/text/configure.jsp");
        }
    }

    @Override
    protected void updateScript() {
        Subject subject = SecurityUtils.getSubject();
        if(SecurityLogic.hasPermissions(getPageInstance(), subject, AccessLevel.DEVELOP)) {
            super.updateScript();
        }
    }

    @RequiresPermissions(level = AccessLevel.EDIT)
    @Button(list = "manage-attachments-upload", key = "text.attachment.upload", order = 1)
    public Resolution uploadAttachment() {
        if (upload == null) {
            SessionMessages.addWarningMessage(getMessage("text.attachment.noFileSelected"));
        } else {
            try {
                commonUploadAttachment();
                SessionMessages.addInfoMessage(getMessage("text.attachment.uploadSuccessful"));
            } catch (IOException e) {
                logger.error("Upload failed", e);
                SessionMessages.addErrorMessage(getMessage("text.attachment.uploadFailed"));
            }
        }
        return new RedirectResolution(getDispatch().getOriginalPath())
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
        logger.info("Uploading attachment");
        InputStream attachmentStream = upload.getInputStream();
        String attachmentId = RandomUtil.createRandomId();
        File dataFile = RandomUtil.getCodeFile(
                application.getAppStorageDir(), ATTACHMENT_FILE_NAME_PATTERN, attachmentId);

        // copy the data
        IOUtils.copyLarge(attachmentStream, new FileOutputStream(dataFile));
        if(textConfiguration == null) {
            textConfiguration = new TextConfiguration();
        }
        TextLogic.createAttachment(
                textConfiguration, attachmentId,
                upload.getFileName(), upload.getContentType(),
                upload.getSize());
        viewAttachmentUrl =
                generateViewAttachmentUrl(attachmentId);
        saveConfiguration(textConfiguration);
    }

    protected String generateViewAttachmentUrl(String attachmentId) {
        return String.format("%s?viewAttachment=&id=%s",
                getDispatch().getAbsoluteOriginalPath(),
                attachmentId);
    }

    @RequiresPermissions(level = AccessLevel.VIEW)
    public Resolution viewAttachment() {
        // find the attachment
        Attachment attachment =
                TextLogic.findAttachmentById(textConfiguration, id);

        if (attachment == null) {
            return new ErrorResolution(404, "Attachment not found");
        }

        try {
            String attachmentId = attachment.getId();
            final File file = RandomUtil.getCodeFile(
                    application.getAppStorageDir(), ATTACHMENT_FILE_NAME_PATTERN, attachmentId);
            /* TODO cache
            //Suggerisce al browser di usare la risorsa che ha in cache invece di riscaricarla
            HttpServletRequest request = context.getRequest();
            if(request.getHeader("If-Modified-Since") != null) {
                long ifModifiedSince = request.getDateHeader("If-Modified-Since");
                if(ifModifiedSince >= file.lastModified()) {
                    return new ErrorResolution(304); //Not modified
                }
            }*/

            InputStream is = new FileInputStream(file);
            Resolution resolution =
                    new StreamingResolution(attachment.getContentType(), is)
                            .setLength(attachment.getSize())
                            .setAttachment(false)
                            .setLastModified(file.lastModified());
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
                TextLogic.findAttachmentById(textConfiguration, id);

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

    @RequiresPermissions(level = AccessLevel.DEVELOP)
    public Resolution browse() {
        logger.info("Browse");
        return new ForwardResolution("/layouts/text/browse.jsp");
    }

    @RequiresPermissions(level = AccessLevel.DEVELOP)
    public Resolution browsePages() {
        logger.info("Browse Pages");
        return new ForwardResolution("/layouts/text/browsePages.jsp");
    }

    @Button(list = "portletHeaderButtons", key = "layouts.text.manage-attachments.manage_attachments_for_page", order = 3, icon = "ui-icon-link")
    @RequiresPermissions(level = AccessLevel.EDIT)
    public Resolution manageAttachments() {
        logger.info("Manage attachments");
        return new ForwardResolution("/layouts/text/manage-attachments.jsp");
    }

    @Button(list = "edit-content", key = "commons.update")
    @RequiresPermissions(level = AccessLevel.EDIT)
    public Resolution updateContent() {
        try {
            saveContent();
            SessionMessages.addInfoMessage(getMessage("commons.update.successful"));
        } catch (IOException e) {
            SessionMessages.addInfoMessage(getMessage("commons.update.failed"));
        }
        return cancel();
    }

    @RequiresPermissions(level = AccessLevel.EDIT)
    @Button(list = "manage-attachments-delete", key = "commons.delete", order = 1)
    public Resolution deleteAttachments() {
        if (selection == null || selection.length == 0) {
            SessionMessages.addWarningMessage(getMessage("text.attachment.noAttachmentSelected"));
        } else {
            int counter = 0;
            for (String code : selection) {
                Attachment attachment =
                        TextLogic.deleteAttachmentByCode(textConfiguration, code);
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
            saveConfiguration(textConfiguration);
            if (counter == 1) {
                SessionMessages.addInfoMessage(getMessage("text.attachment.oneDeleted"));
            } else if (counter > 1) {
                SessionMessages.addInfoMessage(
                                getMessage("text.attachment.nDeleted", counter));
            }
        }
        return new RedirectResolution(getDispatch().getOriginalPath())
                .addParameter("manageAttachments")
                .addParameter("cancelReturnUrl", cancelReturnUrl);
    }

    @Button(list = "manage-attachments", key = "commons.ok", order = 1)
    @RequiresPermissions(level = AccessLevel.EDIT)
    public Resolution save() {
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
