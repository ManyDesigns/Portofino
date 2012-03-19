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

package com.manydesigns.portofino.actions.admin.page;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.fields.SelectField;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.forms.TableForm;
import com.manydesigns.elements.forms.TableFormBuilder;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.options.DefaultSelectionProvider;
import com.manydesigns.elements.text.OgnlTextFormat;
import com.manydesigns.elements.util.RandomUtil;
import com.manydesigns.elements.util.ReflectionUtil;
import com.manydesigns.portofino.ApplicationAttributes;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.RequestAttributes;
import com.manydesigns.portofino.actions.forms.NewPage;
import com.manydesigns.portofino.application.AppProperties;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.buttons.annotations.Buttons;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.dispatcher.*;
import com.manydesigns.portofino.logic.SecurityLogic;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.ModelObject;
import com.manydesigns.portofino.model.ModelObjectVisitor;
import com.manydesigns.portofino.pageactions.AbstractPageAction;
import com.manydesigns.portofino.pageactions.PageActionLogic;
import com.manydesigns.portofino.pageactions.registry.PageActionInfo;
import com.manydesigns.portofino.pageactions.registry.PageActionRegistry;
import com.manydesigns.portofino.pages.*;
import com.manydesigns.portofino.scripting.ScriptingUtil;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.security.RequiresAdministrator;
import com.manydesigns.portofino.security.SupportsPermissions;
import com.manydesigns.portofino.shiro.UsersGroupsDAO;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.controller.LifecycleStage;
import net.sourceforge.stripes.util.HttpUtil;
import ognl.OgnlContext;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.text.MessageFormat;
import java.util.*;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@UrlBinding("/actions/admin/page")
public class PageAdminAction extends AbstractActionBean {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    protected String originalPath;
    public Dispatch dispatch;

    @Inject(RequestAttributes.APPLICATION)
    public Application application;

    @Inject(RequestAttributes.MODEL)
    public Model model;

    private static final Logger logger = LoggerFactory.getLogger(PageAdminAction.class);

    //--------------------------------------------------------------------------
    // Page crud fields
    //--------------------------------------------------------------------------

    protected static final String[][] NEW_PAGE_SETUP_FIELDS = {
            {"actionClassName", "fragment", "title", "description", "insertPositionName"}};
    protected Form newPageForm;
    protected String destinationPagePath;
    protected String fragment;
    protected String title;

    protected final PageActionRegistry registry = new PageActionRegistry();

    @After(stages = LifecycleStage.BindingAndValidation) //Cosi' puo' influenzare SecurityInterceptor (dispatch)
    public void prepare() {
        Application application =
                //l'injection interceptor non e' ancora stato chiamato
                (Application) context.getRequest().getAttribute(RequestAttributes.APPLICATION);
        Dispatcher dispatcher = new Dispatcher(application);
        String contextPath = context.getRequest().getContextPath();
        dispatch = dispatcher.createDispatch(contextPath, originalPath);
        try {
            PageInstance pageInstance = dispatch.getLastPageInstance();
            PageAction actionBean = pageInstance.getActionClass().newInstance();
            pageInstance.setActionBean(actionBean);
        } catch (Exception e) {
            throw new Error("Couldn't instantiate action", e);
        }
        context.getRequest().setAttribute(RequestAttributes.DISPATCH, dispatch);

        List<String> knownPageActions = application.getPortofinoProperties().getList("pageactions");
        for(String pageAction : knownPageActions) {
            tryToRegisterPageAction(pageAction);
        }
    }

    protected void tryToRegisterPageAction(String className) {
        try {
            registry.register(Class.forName(className));
        } catch (Exception e) {
            logger.warn("{} class not found, page not available", className);
        }
    }

    protected String getMessage(String key, Object... args) {
        Locale locale = context.getLocale();
        ResourceBundle resourceBundle = application.getBundle(locale);
        String msg = resourceBundle.getString(key);
        return MessageFormat.format(msg, args);
    }

    //--------------------------------------------------------------------------
    // Page crud
    //--------------------------------------------------------------------------

    @Buttons({
        @Button(list = "page-children-edit", key = "commons.cancel", order = 99),
        @Button(list = "page-permissions-edit", key = "commons.cancel", order = 99),
        @Button(list = "page-create", key = "commons.cancel", order = 99)
    })
    public Resolution cancel() {
        return new RedirectResolution(dispatch.getOriginalPath());
    }

    @RequiresAdministrator
    public Resolution updateLayout() {
        HttpServletRequest request = context.getRequest();
        Enumeration parameters = request.getParameterNames();
        while(parameters.hasMoreElements()) {
            String parameter = (String) parameters.nextElement();
            if(parameter.startsWith("portletWrapper_")) {
                String layoutContainer = parameter.substring("portletWrapper_".length());
                String[] portletWrapperIds = request.getParameterValues(parameter);
                try {
                    updateLayout(layoutContainer, portletWrapperIds);
                } catch (Exception e) {
                    logger.error("Error updating layout", e);
                    SessionMessages.addErrorMessage(getMessage("layout.update.failed"));
                }
            }
        }
        return new RedirectResolution(dispatch.getOriginalPath());
    }

    protected void updateLayout(String layoutContainer, String[] portletWrapperIds) throws Exception {
        PageInstance instance = getPageInstance();
        Layout layout = instance.getLayout();
        if(layout == null) {
            layout = new Layout();
            instance.setLayout(layout);
        }
        for(int i = 0; i < portletWrapperIds.length; i++) {
            String current = portletWrapperIds[i];
            if("p".equals(current)) {
                layout.getSelf().setContainer(layoutContainer);
                layout.getSelf().setOrder(i + "");
            } else if (current.startsWith("c")) {
                String pageFragment = current.substring(1); //current = c...
                for(ChildPage p : layout.getChildPages()) {
                    if(pageFragment.equals(p.getName())) {
                        p.setContainer(layoutContainer);
                        p.setOrder(i + "");
                    }
                }
            } else {
                logger.debug("Ignoring: {}", current);
            }
        }
        DispatcherLogic.savePage(instance);
    }

    @RequiresAdministrator
    public Resolution reloadModel() {
        synchronized (application) {
            application.loadXmlModel();
            return new RedirectResolution(dispatch.getOriginalPath());
        }
    }

    @RequiresAdministrator
    public Resolution newPage() {
        prepareNewPageForm();
        return new ForwardResolution("/layouts/page-crud/new-page.jsp");
    }

    @RequiresAdministrator
    @Button(list = "page-create", key = "commons.create", order = 1)
    public Resolution createPage() {
        try {
            return doCreateNewPage();
        } catch (Exception e) {
            logger.error("Error creating page", e);
            String msg = getMessage("page.create.failed", e.getMessage());
            SessionMessages.addErrorMessage(msg);
            return new ForwardResolution("/layouts/page-crud/new-page.jsp");
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public static enum InsertPosition {
        TOP, CHILD, SIBLING
    }

    private Resolution doCreateNewPage() throws Exception {
        prepareNewPageForm();
        newPageForm.readFromRequest(context.getRequest());
        if(newPageForm.validate()) {
            NewPage newPage = new NewPage();
            newPageForm.writeToObject(newPage);
            InsertPosition insertPosition =
                    InsertPosition.valueOf(newPage.getInsertPositionName());
            String pageClassName = newPage.getActionClassName();
            Class actionClass = Class.forName(pageClassName);
            PageActionInfo info = registry.getInfo(actionClass);
            String pageId = RandomUtil.createRandomId();

            String className = pageId;
            if(Character.isDigit(className.charAt(0))) {
                className = "_" + className;
            }
            OgnlContext ognlContext = ElementsThreadLocals.getOgnlContext();
            ognlContext.put("generatedClassName", className);
            ognlContext.put("pageClassName", pageClassName);
            String script = OgnlTextFormat.format(info.scriptTemplate, this);

            Page page = new Page();
            BeanUtils.copyProperties(page, newPage);
            page.setId(pageId);

            Object configuration = null;
            if(info.configurationClass != null) {
                configuration = ReflectionUtil.newInstance(info.configurationClass);
            }
            page.init();

            String fragment = newPage.getFragment();
            String configurePath;
            File directory;
            File parentDirectory;
            Page parentPage;
            Layout parentLayout;
            switch (insertPosition) {
                case TOP:
                    parentDirectory = application.getPagesDir();
                    directory = new File(parentDirectory, fragment);
                    parentPage = DispatcherLogic.getPage(parentDirectory);
                    parentLayout = parentPage.getLayout();
                    configurePath = "";
                    break;
                case CHILD:
                    PageInstance lastPageInstance = getPageInstance();
                    parentPage = lastPageInstance.getPage();
                    parentLayout = lastPageInstance.getLayout();
                    parentDirectory = lastPageInstance.getDirectory();
                    directory = lastPageInstance.getChildPageDirectory(fragment);
                    configurePath = dispatch.getOriginalPath();
                    break;
                case SIBLING:
                    PageInstance parentPageInstance = dispatch.getPageInstance(-2);
                    parentPage = parentPageInstance.getPage();
                    parentLayout = parentPageInstance.getLayout();
                    parentDirectory = parentPageInstance.getDirectory();
                    directory = parentPageInstance.getChildPageDirectory(fragment);
                    configurePath = parentPageInstance.getPath();
                    break;
                default:
                    throw new IllegalStateException("Don't know how to add page " + page + " at position " + insertPosition);
            }

            if(directory.exists()) {
                logger.error("Can't create page - directory {} exists", directory.getAbsolutePath());
                SessionMessages.addErrorMessage(getMessage("page.create.failed.directoryExists"));
                return new ForwardResolution("/layouts/page-crud/new-page.jsp");
            }
            if(directory.mkdirs()) {
                try {
                    logger.debug("Creating the new child page in directory: {}", directory);
                    DispatcherLogic.savePage(directory, page);
                    if(configuration != null) {
                        DispatcherLogic.saveConfiguration(directory, configuration);
                    }
                    File groovyScriptFile =
                        ScriptingUtil.getGroovyScriptFile(directory, "action");
                    FileWriter fw = null;
                    try {
                        fw = new FileWriter(groovyScriptFile);
                        fw.write(script);
                    } finally {
                        IOUtils.closeQuietly(fw);
                    }
                    logger.debug("Registering the new child page in parent page (directory: {})", parentDirectory);
                    ChildPage childPage = new ChildPage();
                    childPage.setName(directory.getName());
                    childPage.setShowInNavigation(true);
                    parentLayout.getChildPages().add(childPage);

                    if(info.supportsDetail) {
                        File detailDir = new File(directory, PageInstance.DETAIL);
                        logger.debug("Creating _detail directory: {}", detailDir);
                        if(!detailDir.mkdir()) {
                            logger.warn("Couldn't create detail directory {}", detailDir);
                        }
                    }

                    DispatcherLogic.savePage(parentDirectory, parentPage);
                } catch (Exception e) {
                    logger.error("Exception saving page configuration");
                    SessionMessages.addErrorMessage(getMessage("page.create.failed"));
                    return new ForwardResolution("/layouts/page-crud/new-page.jsp");
                }
            } else {
                logger.error("Can't create directory {}", directory.getAbsolutePath());
                SessionMessages.addErrorMessage(getMessage("page.create.failed.cantCreateDir"));
                return new ForwardResolution("/layouts/page-crud/new-page.jsp");
            }
            logger.info("Page " + pageId + " created. Path: " + directory.getAbsolutePath());
            SessionMessages.addInfoMessage(getMessage("page.create.successful"));
            return new RedirectResolution(configurePath + "/" + fragment).addParameter("configure");
        } else {
            return new ForwardResolution("/layouts/page-crud/new-page.jsp");
        }
    }

    @RequiresAdministrator
    public Resolution deletePage() {
        PageInstance pageInstance = getPageInstance();
        PageInstance parentPageInstance = pageInstance.getParent();
        if(parentPageInstance == null) {
            SessionMessages.addErrorMessage(getMessage("page.delete.forbidden.root"));
        } else {
            Dispatcher dispatcher = new Dispatcher(application);
            String contextPath = context.getRequest().getContextPath();
            String landingPagePath = application.getAppConfiguration().getString(AppProperties.LANDING_PAGE);
            Dispatch landingPageDispatch = dispatcher.createDispatch(contextPath, landingPagePath);
            if(landingPageDispatch != null &&
               landingPageDispatch.getLastPageInstance().getDirectory().equals(pageInstance.getDirectory())) {
                SessionMessages.addErrorMessage(getMessage("page.delete.forbidden.landing"));
                return new RedirectResolution(dispatch.getOriginalPath());
            }
            try {
                String pageName = pageInstance.getName();
                File childPageDirectory = parentPageInstance.getChildPageDirectory(pageName);
                Layout parentLayout = parentPageInstance.getLayout();
                Iterator<ChildPage> it = parentLayout.getChildPages().iterator();
                while(it.hasNext()) {
                    if(pageName.equals(it.next().getName())) {
                        it.remove();
                        DispatcherLogic.savePage(parentPageInstance.getDirectory(), parentPageInstance.getPage());
                        break;
                    }
                }
                FileUtils.deleteDirectory(childPageDirectory);
            } catch (Exception e) {
                logger.error("Error deleting page", e);
            }
            return new RedirectResolution(parentPageInstance.getPath());
        }
        return new RedirectResolution(dispatch.getOriginalPath());
    }

    public Page getPage() {
        return getPageInstance().getPage();
    }

    public PageInstance getPageInstance() {
        return dispatch.getLastPageInstance();
    }

    @RequiresAdministrator
    public Resolution movePage() {
        return copyPage(null, true);
    }

    @RequiresAdministrator
    public Resolution copyPage() {
        return copyPage(fragment, false);
    }

    protected Resolution copyPage(String newName, boolean deleteOriginal) {
        if(StringUtils.isEmpty(destinationPagePath)) {
            SessionMessages.addErrorMessage(getMessage("page.copyOrMove.noDestination"));
            return new RedirectResolution(dispatch.getOriginalPath());
        }
        PageInstance pageInstance = getPageInstance();
        PageInstance oldParent = pageInstance.getParent();
        if(oldParent == null) {
            SessionMessages.addErrorMessage(getMessage("page.copyOrMove.forbidden.root"));
            return new RedirectResolution(dispatch.getOriginalPath());
        }
        PageInstance newParent;
        if("/".equals(destinationPagePath)) {
            File dir = application.getPagesDir();
            try {
                newParent = new PageInstance(null, dir, application, DispatcherLogic.getPage(dir));
            } catch (Exception e) {
                throw new Error("Couldn't load root page", e);
            }
        } else {
            Dispatcher dispatcher = new Dispatcher(application);
            Dispatch destinationDispatch =
                    dispatcher.createDispatch(context.getRequest().getContextPath(), destinationPagePath);
            //TODO gestione eccezioni
            newParent = destinationDispatch.getLastPageInstance();
        }
        if(newParent.getDirectory().equals(oldParent.getDirectory())) {
            List<String> params = newParent.getParameters();
            newParent = new PageInstance(newParent.getParent(), newParent.getDirectory(), application, oldParent.getPage());
            newParent.getParameters().addAll(params);
        }
        if(!SecurityLogic.isAdministrator(context.getRequest())) {
            Subject subject = SecurityUtils.getSubject();
            if(!SecurityLogic.hasPermissions(newParent, subject, AccessLevel.DEVELOP)) {
                SessionMessages.addErrorMessage(getMessage("page.copyOrMove.forbidden.accessLevel"));
                return new RedirectResolution(dispatch.getOriginalPath());
            }
        }
        if(newParent != null) { //TODO vedi sopra
            newName = StringUtils.isEmpty(newName) ? pageInstance.getName() : newName;
            File newDirectory = newParent.getChildPageDirectory(newName);
            File newParentDirectory = newDirectory.getParentFile();
            logger.debug("Ensuring that new parent directory {} exists", newParentDirectory);
            newParentDirectory.mkdirs();
            if(!newDirectory.exists()) {
                try {
                    Iterator<ChildPage> it = oldParent.getLayout().getChildPages().iterator();
                    ChildPage oldChildPage = null;
                    String oldName = pageInstance.getName();
                    while (it.hasNext()) {
                        oldChildPage = it.next();
                        if(oldChildPage.getName().equals(oldName)) {
                            if(deleteOriginal) {
                                it.remove();
                            }
                            break;
                        }
                    }
                    if(deleteOriginal) {
                        logger.debug("Removing from old parent");
                        DispatcherLogic.savePage(oldParent.getDirectory(), oldParent.getPage());
                        logger.debug("Moving directory");
                        FileUtils.moveDirectory(pageInstance.getDirectory(), newDirectory);
                    } else {
                        logger.debug("Copying directory");
                        FileUtils.copyDirectory(pageInstance.getDirectory(), newDirectory);
                        logger.debug("Generating a new Id for the new page");
                        Page newPage = DispatcherLogic.getPage(newDirectory);
                        String pageId = RandomUtil.createRandomId();
                        newPage.setId(pageId);
                        DispatcherLogic.savePage(newDirectory, newPage);
                    }
                    logger.debug("Registering the new child page in parent page (directory: {})",
                            newDirectory);

                    ChildPage newChildPage = new ChildPage();
                    newChildPage.setName(newName);
                    if(oldChildPage != null) {
                        newChildPage.setShowInNavigation(oldChildPage.isShowInNavigation());
                    } else {
                        newChildPage.setShowInNavigation(true);
                    }
                    newParent.getLayout().getChildPages().add(newChildPage);
                    DispatcherLogic.savePage(newParent.getDirectory(), newParent.getPage());
                } catch (Exception e) {
                    logger.error("Couldn't copy/move page", e);
                    String msg = getMessage("page.copyOrMove.failed", destinationPagePath);
                    SessionMessages.addErrorMessage(msg);
                }
            } else {
                String msg = getMessage("page.copyOrMove.destinationExists", newDirectory.getAbsolutePath());
                SessionMessages.addErrorMessage(msg);
            }
            if(deleteOriginal) {
                return new RedirectResolution("");
            } else {
                return new RedirectResolution(dispatch.getOriginalPath());
            }
        } else {
            String msg = getMessage("page.copyOrMove.invalidDestination", destinationPagePath);
            SessionMessages.addErrorMessage(msg);
        }
        return new RedirectResolution(dispatch.getOriginalPath());
    }

    protected class CopyVisitor extends ModelObjectVisitor {

        @Override
        public void visitNodeBeforeChildren(ModelObject node) {
            if(node instanceof Page) {
                Page page = (Page) node;
                final String oldPageId = page.getId();
                final String pageId = RandomUtil.createRandomId();
                page.setId(pageId);
                File storageDirFile = application.getAppStorageDir();
                copyPageFiles(oldPageId, pageId, storageDirFile);
                File scriptsFile = application.getAppScriptsDir();
                copyPageFiles(oldPageId, pageId, scriptsFile);
            }
        }

        private void copyPageFiles(final String oldPageId, String pageId, File dirFile) {
            File[] resources = dirFile.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.startsWith(oldPageId + ".");
                }
            });
            for(File res : resources) {
                File dest = new File(dirFile, pageId + res.getName().substring(oldPageId.length()));
                try {
                    FileUtils.copyFile(res, dest);
                } catch (IOException e) {
                    logger.error("Couldn't copy resource file " + res + " to " + dest, e);
                }
            }
        }

    }

    private void prepareNewPageForm() {
        application.getPortofinoProperties().getProperties("");
        DefaultSelectionProvider classSelectionProvider = new DefaultSelectionProvider("actionClassName");
        for(PageActionInfo info : registry) {
            classSelectionProvider.appendRow(info.actionClass.getName(), info.description, true);
        }
        //root + at least 1 child
        boolean includeSiblingOption = dispatch.getPageInstancePath().length > 2;
        int fieldCount = includeSiblingOption ? 3 : 2;
        String[] insertPositions = new String[fieldCount];
        String[] labels =  new String[fieldCount];
        insertPositions[0] =  InsertPosition.TOP.name();
        labels[0] = "at the top level";
        insertPositions[1] = InsertPosition.CHILD.name();
        labels[1] = "as a child of " + getPage().getTitle();
        if(includeSiblingOption) {
            insertPositions[2] = InsertPosition.SIBLING.name();
            labels[2] = "as a sibling of " + getPage().getTitle();
        }
        DefaultSelectionProvider insertPositionSelectionProvider = new DefaultSelectionProvider("insertPositionName");
        for(int i = 0; i < insertPositions.length; i++) {
            insertPositionSelectionProvider.appendRow(insertPositions[i], labels[i], true);
        }
        newPageForm = new FormBuilder(NewPage.class)
                .configFields(NEW_PAGE_SETUP_FIELDS)
                .configFieldSetNames("Page setup")
                .configSelectionProvider(classSelectionProvider, "actionClassName")
                .configSelectionProvider(insertPositionSelectionProvider, "insertPositionName")
                .build();
        ((SelectField) newPageForm.findFieldByPropertyName("insertPositionName")).setValue(InsertPosition.CHILD.name());
    }

    //--------------------------------------------------------------------------
    // Page children
    //--------------------------------------------------------------------------

    protected List<EditChildPage> childPages = new ArrayList<EditChildPage>();
    protected List<EditChildPage> detailChildPages = new ArrayList<EditChildPage>();
    protected TableForm childPagesForm;
    protected TableForm detailChildPagesForm;

    @RequiresAdministrator
    public Resolution pageChildren() {
        setupChildPages();
        return forwardToPageChildren();
    }

    protected void setupChildPages() {
        File directory = getPageInstance().getDirectory();
        childPagesForm = setupChildPagesForm(childPages, directory, getPage().getLayout(), "");
        if(PageActionLogic.supportsDetail(getPageInstance().getActionClass())) {
            File detailDirectory = new File(directory, PageInstance.DETAIL);
            if(!detailDirectory.isDirectory() && !detailDirectory.mkdir()) {
                logger.error("Could not create detail directory{}", detailDirectory.getAbsolutePath());
                SessionMessages.addErrorMessage("Could not create detail directory");
                return;
            }
            detailChildPagesForm =
                    setupChildPagesForm(detailChildPages, detailDirectory, getPage().getDetailLayout(), "detail");
        }
    }

    protected TableForm setupChildPagesForm
            (List<EditChildPage> childPages, File childrenDirectory, Layout layout, String prefix) {
        TableForm childPagesForm;
        FileFilter filter = new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        };
        List<EditChildPage> unorderedChildPages = new ArrayList<EditChildPage>();
        for (File dir : childrenDirectory.listFiles(filter)) {
            if(PageInstance.DETAIL.equals(dir.getName())) {
                continue;
            }
            EditChildPage childPage = null;
            String title;
            try {
                Page page = DispatcherLogic.getPage(dir);
                title = page.getTitle();
            } catch (Exception e) {
                logger.error("Couldn't load page for " + dir, e);
                title = null;
            }
            for(ChildPage cp : layout.getChildPages()) {
                if(cp.getName().equals(dir.getName())) {
                    childPage = new EditChildPage();
                    childPage.active = true;
                    childPage.name = cp.getName();
                    childPage.showInNavigation = cp.isShowInNavigation();
                    childPage.title = title;
                    childPage.embedded = cp.getContainer() != null;
                    break;
                }
            }
            if(childPage == null) {
                childPage = new EditChildPage();
                childPage.active = false;
                childPage.name = dir.getName();
                childPage.title = title;
            }
            unorderedChildPages.add(childPage);
        }

        logger.debug("Adding known pages in order");
        for(ChildPage cp : layout.getChildPages()) {
            for(EditChildPage ecp : unorderedChildPages) {
                if(cp.getName().equals(ecp.name)) {
                    childPages.add(ecp);
                    break;
                }
            }
        }
        logger.debug("Adding unknown pages");
        for(EditChildPage ecp : unorderedChildPages) {
            if(!ecp.active) {
                childPages.add(ecp);
            }
        }

        childPagesForm = new TableFormBuilder(EditChildPage.class)
                .configNRows(childPages.size())
                .configFields(getChildPagesFormFields())
                .configPrefix(prefix)
                .build();
        childPagesForm.readFromObject(childPages);
        return childPagesForm;
    }

    protected String[] getChildPagesFormFields() {
        return new String[] { "active", "name", "title", "showInNavigation", "embedded" };
    }

    @RequiresAdministrator
    @Button(list = "page-children-edit", key = "commons.update", order = 1)
    public Resolution updatePageChildren() {
        setupChildPages();
        String[] order = context.getRequest().getParameterValues("childrenTable_0");
        if(order == null) {
            order = new String[0];
        }
        boolean success = updatePageChildren(childPagesForm, childPages, getPage().getLayout(), order);
        childPages.clear();
        if(success && detailChildPagesForm != null) {
            order = context.getRequest().getParameterValues("childrenTable_1");
            if(order == null) {
                order = new String[0];
            }
            updatePageChildren(detailChildPagesForm, detailChildPages, getPage().getDetailLayout(), order);
            detailChildPages.clear();
        }
        setupChildPages(); //Re-read sorted values
        SessionMessages.addInfoMessage(getMessage("commons.update.successful"));
        return forwardToPageChildren();
    }

    protected boolean updatePageChildren
            (TableForm childPagesForm, List<EditChildPage> childPages, Layout layout, String[] order) {
        childPagesForm.readFromRequest(context.getRequest());
        if(!childPagesForm.validate()) {
            return false;
        }
        childPagesForm.writeToObject(childPages);
        List<ChildPage> newChildren = new ArrayList<ChildPage>();
        for(EditChildPage editChildPage : childPages) {
            if(!editChildPage.active) {
                continue;
            }
            ChildPage childPage = null;
            for(ChildPage cp : layout.getChildPages()) {
                if(cp.getName().equals(editChildPage.name)) {
                    childPage = cp;
                    break;
                }
            }
            if(childPage == null) {
                childPage = new ChildPage();
                childPage.setName(editChildPage.name);
            }
            childPage.setShowInNavigation(editChildPage.showInNavigation);
            if(editChildPage.embedded) {
                if(childPage.getContainer() == null) {
                    childPage.setContainer(AbstractPageAction.DEFAULT_LAYOUT_CONTAINER);
                    childPage.setOrder("0");
                }
            } else {
                childPage.setContainer(null);
                childPage.setOrder(null);
            }
            newChildren.add(childPage);
            if(!editChildPage.showInNavigation && !editChildPage.embedded) {
                String msg = getMessage("page.warnNotShowInNavigationNotEmbedded", editChildPage.name);
                SessionMessages.addWarningMessage(msg);
            }
        }
        List<ChildPage> sortedChildren = new ArrayList<ChildPage>();
        for(String name : order) {
            for(ChildPage p : newChildren) {
                if(name.equals(p.getName())) {
                    sortedChildren.add(p);
                    break;
                }
            }
        }
        layout.getChildPages().clear();
        layout.getChildPages().addAll(sortedChildren);
        try {
            DispatcherLogic.savePage(getPageInstance());
        } catch (Exception e) {
            logger.error("Couldn't save page", e);
            String msg = getMessage("page.update.failed", e.getMessage());
            SessionMessages.addErrorMessage(msg);
            return false;
        }
        return true;
    }

    public List<EditChildPage> getChildPages() {
        return childPages;
    }

    public TableForm getChildPagesForm() {
        return childPagesForm;
    }

    public List<EditChildPage> getDetailChildPages() {
        return detailChildPages;
    }

    public TableForm getDetailChildPagesForm() {
        return detailChildPagesForm;
    }

    protected Resolution forwardToPageChildren() {
        return new ForwardResolution("/layouts/page/children.jsp");
    }

    //--------------------------------------------------------------------------
    // Page permisssions
    //--------------------------------------------------------------------------

    protected Set<String> groups;

    //<group, level>
    protected Map<String, String> accessLevels = new HashMap<String, String>();
    //<group, permissions>
    protected Map<String, List<String>> permissions = new HashMap<String, List<String>>();

    protected String testUserId;
    protected Set<String> users;
    protected AccessLevel testedAccessLevel;
    protected Set<String> testedPermissions;

    @RequiresAdministrator
    public Resolution pagePermissions() {
        setupGroups();

        UsersGroupsDAO dao =
                (UsersGroupsDAO) context.getServletContext().getAttribute(ApplicationAttributes.USERS_GROUPS_DAO);

        if(dao != null) {
            users = new LinkedHashSet<String>();
            users.add(null);
            users.addAll(dao.getUsers());
        }

        return forwardToPagePermissions();
    }

    @Button(list = "testUserPermissions", key = "user.permissions.test")
    @RequiresAdministrator
    public Resolution testUserPermissions() {
        testUserId = StringUtils.defaultIfEmpty(testUserId, null);
        PrincipalCollection principalCollection;
        if(!StringUtils.isEmpty(testUserId)) {
            principalCollection = new SimplePrincipalCollection(testUserId, "realm");
        } else {
            principalCollection = null;
        }
        Permissions permissions = SecurityLogic.calculateActualPermissions(getPageInstance());
        testedAccessLevel = AccessLevel.NONE;
        testedPermissions = new HashSet<String>();

        SecurityManager securityManager = SecurityUtils.getSecurityManager();

        for(AccessLevel level : AccessLevel.values()) {
            if(level.isGreaterThanOrEqual(testedAccessLevel) &&
                SecurityLogic.hasPermissions(application, permissions, securityManager, principalCollection, level)) {
                testedAccessLevel = level;
            }
        }
        String[] supportedPermissions = getSupportedPermissions();
        if(supportedPermissions != null) {
            for(String permission : supportedPermissions) {
                boolean permitted =
                        SecurityLogic.hasPermissions
                                (application, permissions, securityManager,
                                 principalCollection, testedAccessLevel, permission);
                if(permitted) {
                    testedPermissions.add(permission);
                }
            }
        }

        return pagePermissions();
    }

    public String[] getSupportedPermissions() {
        Class<?> actualActionClass = getPageInstance().getActionClass();
        SupportsPermissions supportsPermissions =
                actualActionClass.getAnnotation(SupportsPermissions.class);
        if(supportsPermissions != null && supportsPermissions.value().length > 0) {
            return supportsPermissions.value();
        } else {
            return null;
        }
    }

    protected Resolution forwardToPagePermissions() {
        return new ForwardResolution("/layouts/page/permissions.jsp");
    }

    protected void setupGroups() {
        UsersGroupsDAO dao =
                (UsersGroupsDAO) context.getServletContext().getAttribute(ApplicationAttributes.USERS_GROUPS_DAO);
        if(dao != null) {
            groups = dao.getGroups();
        } else {
            groups = new LinkedHashSet<String>();
            Configuration conf = application.getPortofinoProperties();
            groups.add(conf.getString(PortofinoProperties.GROUP_ALL));
            groups.add(conf.getString(PortofinoProperties.GROUP_ANONYMOUS));
            groups.add(conf.getString(PortofinoProperties.GROUP_REGISTERED));
            groups.add(conf.getString(PortofinoProperties.GROUP_ADMINISTRATORS));
            Permissions permissions = SecurityLogic.calculateActualPermissions(getPageInstance());
            for(String group : permissions.getActualLevels().keySet()) {
                groups.add(group);
            }
            for(String group : permissions.getActualPermissions().keySet()) {
                groups.add(group);
            }
        }
    }

    @RequiresAdministrator
    @Button(list = "page-permissions-edit", key = "commons.update", order = 1)
    public Resolution updatePagePermissions() {
        try {
            updatePagePermissions(getPageInstance());
            SessionMessages.addInfoMessage(getMessage("permissions.page.updated"));
            return new RedirectResolution(HttpUtil.getRequestedPath(context.getRequest()))
                    .addParameter("pagePermissions").addParameter("originalPath", dispatch.getOriginalPath());
        } catch (Exception e) {
            logger.error("Couldn't update page permissions", e);
            SessionMessages.addInfoMessage(getMessage("permissions.page.notUpdated"));
            return new RedirectResolution(HttpUtil.getRequestedPath(context.getRequest()))
                    .addParameter("pagePermissions").addParameter("originalPath", dispatch.getOriginalPath());
        }
    }

    protected void updatePagePermissions(PageInstance page) throws Exception {
        Permissions pagePermissions = page.getPage().getPermissions();

        pagePermissions.getGroups().clear();

        Map<String, Group> groups = new HashMap<String, Group>();

        for(Map.Entry<String, String> entry : accessLevels.entrySet()) {
            Group group = new Group();
            group.setName(entry.getKey());
            group.setAccessLevel(entry.getValue());
            groups.put(group.getName(), group);
        }

        for(Map.Entry<String, List<String>> custPerm : permissions.entrySet()) {
            String groupId = custPerm.getKey();
            Group group = groups.get(groupId);
            if(group == null) {
                group = new Group();
                group.setName(groupId);
                groups.put(groupId, group);
            }
            group.getPermissions().addAll(custPerm.getValue());
        }

        for(Group group : groups.values()) {
            pagePermissions.getGroups().add(group);
        }

        DispatcherLogic.savePage(page);
    }

    public AccessLevel getLocalAccessLevel(Page currentPage, String groupId) {
        Permissions currentPagePermissions = currentPage.getPermissions();
        for(Group group : currentPagePermissions.getGroups()) {
            if(groupId.equals(group.getName())) {
                return group.getActualAccessLevel();
            }
        }
        return null;
    }

    //--------------------------------------------------------------------------
    // Getters/Setters
    //--------------------------------------------------------------------------

    public Set<String> getGroups() {
        return groups;
    }

    public Map<String, String> getAccessLevels() {
        return accessLevels;
    }

    public void setAccessLevels(Map<String, String> accessLevels) {
        this.accessLevels = accessLevels;
    }

    public Map<String, List<String>> getPermissions() {
        return permissions;
    }

    public void setPermissions(Map<String, List<String>> permissions) {
        this.permissions = permissions;
    }

    public String getTestUserId() {
        return testUserId;
    }

    public void setTestUserId(String testUserId) {
        this.testUserId = testUserId;
    }

    public Set<String> getUsers() {
        return users;
    }

    public AccessLevel getTestedAccessLevel() {
        return testedAccessLevel;
    }

    public Set<String> getTestedPermissions() {
        return testedPermissions;
    }

    public Form getNewPageForm() {
        return newPageForm;
    }

    public String getDestinationPagePath() {
        return destinationPagePath;
    }

    public void setDestinationPagePath(String destinationPagePath) {
        this.destinationPagePath = destinationPagePath;
    }

    public String getFragment() {
        return fragment;
    }

    public void setFragment(String fragment) {
        this.fragment = fragment;
    }

    public String getOriginalPath() {
        return originalPath;
    }

    public void setOriginalPath(String originalPath) {
        this.originalPath = originalPath;
    }

    public Dispatch getDispatch() {
        return dispatch;
    }

    public Application getApplication() {
        return application;
    }

    public Model getModel() {
        return model;
    }

    public String getCancelReturnUrl() {
        return dispatch.getAbsoluteOriginalPath();
    }
}
