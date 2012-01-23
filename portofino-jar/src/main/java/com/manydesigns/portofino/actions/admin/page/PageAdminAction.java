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

import com.manydesigns.elements.fields.SelectField;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.forms.TableForm;
import com.manydesigns.elements.forms.TableFormBuilder;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.options.DefaultSelectionProvider;
import com.manydesigns.elements.util.RandomUtil;
import com.manydesigns.elements.util.ReflectionUtil;
import com.manydesigns.portofino.dispatcher.AbstractActionBean;
import com.manydesigns.portofino.pageactions.AbstractPageAction;
import com.manydesigns.portofino.application.QueryUtils;
import com.manydesigns.portofino.dispatcher.PageAction;
import com.manydesigns.portofino.dispatcher.RequestAttributes;
import com.manydesigns.portofino.pageactions.chart.ChartAction;
import com.manydesigns.portofino.pageactions.crud.CrudAction;
import com.manydesigns.portofino.actions.forms.NewPage;
import com.manydesigns.portofino.pageactions.jsp.JspAction;
import com.manydesigns.portofino.pageactions.text.TextAction;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.buttons.annotations.Buttons;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.dispatcher.*;
import com.manydesigns.portofino.logic.SecurityLogic;
import com.manydesigns.portofino.model.datamodel.Model;
import com.manydesigns.portofino.model.datamodel.ModelObject;
import com.manydesigns.portofino.model.datamodel.ModelObjectVisitor;
import com.manydesigns.portofino.pages.*;
import com.manydesigns.portofino.scripting.ScriptingUtil;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.system.model.users.User;
import com.manydesigns.portofino.system.model.users.annotations.RequiresAdministrator;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.controller.LifecycleStage;
import net.sourceforge.stripes.util.HttpUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.converters.ClassConverter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
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

    @After(stages = LifecycleStage.BindingAndValidation) //Così può influenzare SecurityInterceptor (dispatch)
    public void prepare() {
        Application application =
                //l'injection interceptor non è ancora stato chiamato
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
    }

    protected String getMessage(String key) {
        Locale locale = context.getLocale();
        ResourceBundle resourceBundle = application.getBundle(locale);
        return resourceBundle.getString(key);
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
        //TODO verificare
        PageInstance myparent = getPageInstance().getParent();
        Layout parentLayout = myparent.getLayout();
        if(parentLayout == null) {
            parentLayout = new Layout();
            myparent.setLayout(parentLayout);
        }
        for(int i = 0; i < portletWrapperIds.length; i++) {
            String current = portletWrapperIds[i];
            if("p".equals(current)) {
                parentLayout.getSelf().setContainer(layoutContainer);
                parentLayout.getSelf().setOrder(i + "");
            } else if (current.startsWith("c")) {
                String pageFragment = current.substring(1); //current = c...
                for(ChildPage p : parentLayout.getChildPages()) {
                    if(pageFragment.equals(p.getName())) {
                        p.setContainer(layoutContainer);
                        p.setOrder(i + "");
                    }
                }
            } else {
                logger.debug("Ignoring: {}", current);
            }
        }
        DispatcherLogic.savePage(myparent);
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
            String msg = getMessage("page.create.failed");
            msg = MessageFormat.format(msg, e.getMessage());
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

    private Resolution doCreateNewPage() throws IllegalAccessException, InvocationTargetException {
        prepareNewPageForm();
        newPageForm.readFromRequest(context.getRequest());
        if(newPageForm.validate()) {
            NewPage newPage = new NewPage();
            newPageForm.writeToObject(newPage);
            InsertPosition insertPosition =
                    InsertPosition.valueOf(newPage.getInsertPositionName());
            String pageClassName = newPage.getActionClassName();
            PageAction action = (PageAction) ReflectionUtil.newInstance(pageClassName);
            String pageId = RandomUtil.createRandomId();

            String script;
            //TODO duplicato in PortletAction
            String template = action.getScriptTemplate();
            String className = pageId;
            if(Character.isDigit(className.charAt(0))) {
                className = "_" + className;
            }
            script = template.replace("__CLASS_NAME__", className);
            //end TODO

            Page page = new Page();
            BeanUtils.copyProperties(newPage, page);
            page.setId(pageId);

            Object configuration = ReflectionUtil.newInstance(action.getConfigurationClass());
            Model model = application.getModel();
            if(configuration instanceof ModelObject) {
                model.init((ModelObject) configuration);
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
                    configurePath = dispatch.getParentPathUrl();
                    break;
                default:
                    throw new IllegalStateException("Don't know how to add page " + page + " at position " + insertPosition);
            }

            if(directory.exists()) {
                logger.error("Can't create page - directory {} exists", directory.getAbsolutePath());
                SessionMessages.addErrorMessage(getMessage("page.create.failed.directoryExists"));
                return new ForwardResolution("/layouts/page-crud/new-page.jsp");
            }
            if(directory.mkdir()) {
                try {
                    logger.debug("Creating the new child page in directory: {}", directory);
                    DispatcherLogic.savePage(directory, page);
                    DispatcherLogic.saveConfiguration(directory, configuration);
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
            SessionMessages.addInfoMessage(getMessage("page.create.successful"));
            return new RedirectResolution(configurePath + "/" + fragment).addParameter("configure");
        } else {
            return new ForwardResolution("/layouts/page-crud/new-page.jsp");
        }
    }

    protected void copyModelObject(ModelObject src, ModelObject dest) throws IllegalAccessException, InvocationTargetException {
        //To handle actualActionClass = null, ClassConverter must have a null return value
        BeanUtilsBean.getInstance().getConvertUtils().register(new ClassConverter(null), Class.class);
        BeanUtils.copyProperties(dest, src);
    }

    @RequiresAdministrator
    public Resolution deletePage() {
        PageInstance parentPageInstance = dispatch.getParentPageInstance();
        if(parentPageInstance.getParent() == null) {
            SessionMessages.addErrorMessage(getMessage("page.delete.forbidden.root"));
        /*} else if(PageLogic.isLandingPage(application, page)) { //TODO ripristinare
            SessionMessages.addErrorMessage(getMessage("page.delete.forbidden.landing"));*/
        } else {
            try {
                PageInstance pageInstance = getPageInstance();
                String pageName = pageInstance.getName();
                File childPageDirectory = parentPageInstance.getChildPageDirectory(pageName);
                Layout parentLayout = parentPageInstance.getLayout();
                Iterator<ChildPage> it = parentLayout.getChildPages().iterator();
                while(it.hasNext()) {
                    if(pageName.equals(it.next().getName())) {
                        it.remove();
                        DispatcherLogic.savePage(pageInstance.getDirectory(), pageInstance.getPage());
                        break;
                    }
                }
                FileUtils.deleteDirectory(childPageDirectory);
            } catch (Exception e) {
                logger.error("Error deleting page directory", e);
            }
            return new RedirectResolution(dispatch.getParentPathUrl());
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
        if(destinationPagePath == null) {
            SessionMessages.addErrorMessage(getMessage("page.copyOrMove.noDestination"));
            return new RedirectResolution(dispatch.getOriginalPath());
        }
        PageInstance pageInstance = getPageInstance();
        PageInstance oldParent = pageInstance.getParent();
        if(oldParent == null) {
            SessionMessages.addErrorMessage(getMessage("page.copyOrMove.forbidden.root"));
            return new RedirectResolution(dispatch.getOriginalPath());
        }
        Dispatcher dispatcher = new Dispatcher(application);
        Dispatch destinationDispatch =
                dispatcher.createDispatch(context.getRequest().getContextPath(), destinationPagePath);
        //TODO gestione eccezioni
        PageInstance newParent = destinationDispatch.getLastPageInstance();
        if(!SecurityLogic.isAdministrator(context.getRequest())) {
            List<String> groups =
                    (List<String>) context.getRequest().getAttribute(RequestAttributes.GROUPS);
            if(!SecurityLogic.hasPermissions(newParent, groups, AccessLevel.EDIT)) {
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
                    String msg = MessageFormat.format(getMessage("page.copyOrMove.failed"), destinationPagePath);
                    SessionMessages.addErrorMessage(msg);
                }
            } else {
                String msg = MessageFormat.format
                        (getMessage("page.copyOrMove.destinationExists"), newDirectory.getAbsolutePath());
                SessionMessages.addErrorMessage(msg);
            }
            if(deleteOriginal) {
                return new RedirectResolution("");
            } else {
                return new RedirectResolution(dispatch.getOriginalPath());
            }
        } else {
            String msg = MessageFormat.format(getMessage("page.copyOrMove.invalidDestination"), destinationPagePath);
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
        DefaultSelectionProvider classSelectionProvider = new DefaultSelectionProvider("actionClassName");
        classSelectionProvider.appendRow(CrudAction.class.getName(), "Crud", true);
        classSelectionProvider.appendRow(ChartAction.class.getName(), "Chart", true);
        classSelectionProvider.appendRow(TextAction.class.getName(), "Text", true);
        classSelectionProvider.appendRow(JspAction.class.getName(), "JSP", true);
        /*PageReference.class.getName(), "Reference to another page"*/
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
        if(getPageInstance().getActionBean().supportsParameters()) {
            File detailDirectory = new File(directory, PageInstance.DETAIL);
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
            for(ChildPage cp : layout.getChildPages()) {
                if(cp.getName().equals(dir.getName())) {
                    childPage = new EditChildPage();
                    childPage.active = true;
                    childPage.name = cp.getName();
                    childPage.showInNavigation = cp.isShowInNavigation();
                    childPage.title = DispatcherLogic.getPage(dir).getTitle();
                    childPage.embedded = cp.getContainer() != null;
                    break;
                }
            }
            if(childPage == null) {
                childPage = new EditChildPage();
                childPage.active = false;
                childPage.name = dir.getName();
                childPage.title = DispatcherLogic.getPage(dir).getTitle();
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
                .configFields("active", "name", "title", "showInNavigation", "embedded")
                .configPrefix(prefix)
                .build();
        childPagesForm.readFromObject(childPages);
        return childPagesForm;
    }

    @RequiresAdministrator
    @Button(list = "page-children-edit", key = "commons.update", order = 1)
    public Resolution updatePageChildren() {
        setupChildPages();
        String[] order = context.getRequest().getParameterValues("childrenTable_0");
        boolean success = updatePageChildren(childPagesForm, childPages, getPage().getLayout(), order);
        childPages.clear();
        if(success && detailChildPagesForm != null) {
            order = context.getRequest().getParameterValues("childrenTable_1");
            updatePageChildren(detailChildPagesForm, detailChildPages, getPage().getDetailLayout(), order);
            detailChildPages.clear();
        }
        setupChildPages(); //Re-read sorted values
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
                String msg = getMessage("page.warnNotShowInNavigationNotEmbedded");
                msg = MessageFormat.format(msg, editChildPage.name);
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
            String msg = getMessage("page.update.failed");
            msg = MessageFormat.format(msg, e.getMessage());
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

    protected List<com.manydesigns.portofino.system.model.users.Group> groups;

    //<group, level>
    protected Map<String, String> accessLevels = new HashMap<String, String>();
    //<group, permissions>
    protected Map<String, List<String>> permissions = new HashMap<String, List<String>>();

    protected String testUserId;
    protected List<User> users;
    protected AccessLevel testedAccessLevel;
    protected Set<String> testedPermissions;

    @RequiresAdministrator
    public Resolution pagePermissions() {
        setupGroups();

        Session session = application.getSystemSession();
        users = (List) QueryUtils.runHqlQuery(session, "from users", null);
        User anonymous = new User();
        anonymous.setUserName("(anonymous)");
        users.add(0, anonymous);

        return forwardToPagePermissions();
    }

    @Button(list = "testUserPermissions", key = "user.permissions.test")
    @RequiresAdministrator
    public Resolution testUserPermissions() {
        testUserId = StringUtils.defaultIfEmpty(testUserId, null);
        List<String> groups = SecurityLogic.manageGroups(application, testUserId);
        Permissions permissions = SecurityLogic.calculateActualPermissions(getPageInstance());
        testedAccessLevel = AccessLevel.NONE;
        testedPermissions = new HashSet<String>();
        for(String group : groups) {
            AccessLevel accessLevel = permissions.getActualLevels().get(group);
            if(accessLevel != null &&
               accessLevel.isGreaterThanOrEqual(testedAccessLevel)) {
                testedAccessLevel = accessLevel;
            }
            Set<String> perms = permissions.getActualPermissions().get(group);
            if(perms != null) {
                testedPermissions.addAll(perms);
            }
        }

        return pagePermissions();
    }

    protected Resolution forwardToPagePermissions() {
        return new ForwardResolution("/layouts/page/permissions.jsp");
    }

    protected void setupGroups() {
        Session session = application.getSystemSession();
        Criteria criteria =
                session.createCriteria(SecurityLogic.GROUP_ENTITY_NAME)
                       .addOrder(Order.asc("name"));
        groups = new ArrayList(criteria.list());
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

    public List<com.manydesigns.portofino.system.model.users.Group> getGroups() {
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

    public List<User> getUsers() {
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
