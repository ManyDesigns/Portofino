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

package com.manydesigns.portofino.actions.admin;

import com.manydesigns.elements.fields.SelectField;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.options.DefaultSelectionProvider;
import com.manydesigns.elements.util.RandomUtil;
import com.manydesigns.elements.util.ReflectionUtil;
import com.manydesigns.portofino.actions.AbstractActionBean;
import com.manydesigns.portofino.actions.PortletAction;
import com.manydesigns.portofino.actions.RequestAttributes;
import com.manydesigns.portofino.actions.forms.NewPage;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.buttons.annotations.Buttons;
import com.manydesigns.portofino.database.QueryUtils;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.dispatcher.Dispatch;
import com.manydesigns.portofino.dispatcher.Dispatcher;
import com.manydesigns.portofino.dispatcher.PageInstance;
import com.manydesigns.portofino.logic.PageLogic;
import com.manydesigns.portofino.logic.SecurityLogic;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.ModelObject;
import com.manydesigns.portofino.model.ModelVisitor;
import com.manydesigns.portofino.model.pages.*;
import com.manydesigns.portofino.system.model.users.User;
import com.manydesigns.portofino.system.model.users.annotations.RequiresAdministrator;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.controller.LifecycleStage;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.converters.ClassConverter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
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
            {"pageClassName", "fragment", "title", "description", "insertPositionName"}};
    protected Form newPageForm;
    protected String destinationPageId;
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
        @Button(list = "page-permissions-edit", key = "commons.cancel", order = 99),
        @Button(list = "page-create", key = "commons.cancel", order = 99)
    })
    public Resolution cancel() {
        return new RedirectResolution(dispatch.getOriginalPath());
    }

    @RequiresAdministrator
    public Resolution updateLayout() {
        synchronized (application) {
            HttpServletRequest request = context.getRequest();
            Enumeration parameters = request.getParameterNames();
            while(parameters.hasMoreElements()) {
                String parameter = (String) parameters.nextElement();
                if(parameter.startsWith("portletWrapper_")) {
                    String layoutContainer = parameter.substring("portletWrapper_".length());
                    String[] portletWrapperIds = request.getParameterValues(parameter);
                    updateLayout(layoutContainer, portletWrapperIds);
                }
            }
            saveModel();
        }
        return new RedirectResolution(dispatch.getOriginalPath());
    }

    protected void updateLayout(String layoutContainer, String[] portletWrapperIds) {
        PageInstance myself = dispatch.getLastPageInstance();
        for(int i = 0; i < portletWrapperIds.length; i++) {
            String current = portletWrapperIds[i];
            if("p".equals(current)) {
                myself.setLayoutContainer(layoutContainer);
                myself.setLayoutOrder(i);
            } else if (current.startsWith("c")) {
                String pageId = current.substring(1); //current = c...
                PageInstance childPageInstance = myself.findChildPageByFragment(pageId);
                Page childPage = childPageInstance.getPage();
                childPage.setLayoutContainerInParent(layoutContainer);
                childPage.setLayoutOrderInParent(i + "");
            } else {
                logger.debug("Ignoring: {}", current);
            }
        }
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
            String pageClassName = newPage.getPageClassName();
            Page page = (Page) ReflectionUtil.newInstance(pageClassName);
            copyModelObject(newPage, page);
            String pageId = RandomUtil.createRandomId();
            page.setId(pageId);
            page.setLayoutContainer(PortletAction.DEFAULT_LAYOUT_CONTAINER);
            page.setLayoutOrder("0");
            String configurePath;
            synchronized (application) {
                switch (insertPosition) {
                    case TOP:
                        model.getRootPage().addChild(page);
                        configurePath = "";
                        break;
                    case CHILD:
                        dispatch.getLastPageInstance().addChild(page);
                        configurePath = dispatch.getOriginalPath();
                        break;
                    case SIBLING:
                        dispatch.getPageInstance(-2).addChild(page);
                        configurePath = dispatch.getParentPathUrl();
                        break;
                    default:
                        throw new IllegalStateException("Don't know how to add page " + page + " at position " + insertPosition);
                }
                saveModel();
            }
            SessionMessages.addInfoMessage(getMessage("page.create.successful"));
            return new RedirectResolution(configurePath + "/" + page.getFragment()).addParameter("configure");
        } else {
            return new ForwardResolution("/layouts/page-crud/new-page.jsp");
        }
    }

    protected void saveModel() {
        model.init();
        application.saveXmlModel();
    }

    protected void copyModelObject(ModelObject src, ModelObject dest) throws IllegalAccessException, InvocationTargetException {
        //To handle actualActionClass = null, ClassConverter must have a null return value
        BeanUtilsBean.getInstance().getConvertUtils().register(new ClassConverter(null), Class.class);
        BeanUtils.copyProperties(dest, src);
    }

    @RequiresAdministrator
    public Resolution deletePage() {
        PageInstance parentPageInstance = dispatch.getParentPageInstance();
        Page page = getPage();
        synchronized (application) {
            if(page.getParent() == null) {
                SessionMessages.addErrorMessage(getMessage("page.delete.forbidden.root"));
            }  else if(PageLogic.isLandingPage(model.getRootPage(), page)) {
                SessionMessages.addErrorMessage(getMessage("page.delete.forbidden.landing"));
            } else {
                parentPageInstance.removeChild(page);
                saveModel();
                return new RedirectResolution(dispatch.getParentPathUrl());
            }
        }
        return new RedirectResolution(dispatch.getOriginalPath());
    }

    public Page getPage() {
        return dispatch.getLastPageInstance().getPage();
    }

    @RequiresAdministrator
    public Resolution movePage() {
        if(destinationPageId == null) {
            SessionMessages.addErrorMessage(getMessage("page.move.noDestination"));
            return new RedirectResolution(dispatch.getOriginalPath());
        }
        Page page = getPage();
        synchronized (application) {
            if(page.getParent() == null) {
                SessionMessages.addErrorMessage(getMessage("page.move.forbidden.root"));
            } else {
                boolean detail = destinationPageId.endsWith("-detail");
                if(detail) {
                    destinationPageId = destinationPageId.substring(0, destinationPageId.length() - 7);
                }
                Page newParent = model.getRootPage().findDescendantPageById(destinationPageId);
                if(!SecurityLogic.isAdministrator(context.getRequest())) {
                    List<String> groups =
                            (List<String>) context.getRequest().getAttribute(RequestAttributes.GROUPS);
                    if(!SecurityLogic.hasPermissions(newParent.getPermissions(), groups, AccessLevel.EDIT)) {
                        SessionMessages.addErrorMessage(getMessage("page.move.forbidden.accessLevel"));
                        return new RedirectResolution(dispatch.getOriginalPath());
                    }
                }
                if(newParent != null) {
                    page.getParent().removeChild(page);
                    if(detail) {
                        ((CrudPage) newParent).addDetailChild(page);
                    } else {
                        newParent.addChild(page);
                    }
                    saveModel();
                    return new RedirectResolution(""); //PageLogic.getPagePath(page)); TODO
                } else {
                    String msg = MessageFormat.format(getMessage("page.move.invalidDestination"), destinationPageId);
                    SessionMessages.addErrorMessage(msg);
                }
            }
        }
        return new RedirectResolution(dispatch.getOriginalPath());
    }

    @RequiresAdministrator
    public Resolution copyPage() {
        if(destinationPageId == null) {
            SessionMessages.addErrorMessage(getMessage("page.copy.noDestination"));
            return new RedirectResolution(dispatch.getOriginalPath());
        }
        if(fragment == null) {
            SessionMessages.addErrorMessage(getMessage("page.copy.noFragment"));
            return new RedirectResolution(dispatch.getOriginalPath());
        }
        Page page = getPage();
        synchronized (application) {
            if(page.getParent() == null) {
                SessionMessages.addErrorMessage(getMessage("page.copy.forbidden.root"));
            } else {
                boolean detail = destinationPageId.endsWith("-detail");
                if(detail) {
                    int length = "-detail".length();
                    destinationPageId = destinationPageId.substring(0, destinationPageId.length() - length);
                }
                final Page newParent = model.getRootPage().findDescendantPageById(destinationPageId);
                if(!SecurityLogic.isAdministrator(context.getRequest())) {
                    List<String> groups = (List<String>) context.getRequest().getAttribute(RequestAttributes.GROUPS);
                    if(!SecurityLogic.hasPermissions(newParent.getPermissions(), groups, AccessLevel.EDIT)) {
                        SessionMessages.addErrorMessage(getMessage("page.copy.forbidden.accessLevel"));
                        return new RedirectResolution(dispatch.getOriginalPath());
                    }
                }
                if(newParent != null) {
                    Page newPage;
                    Model tmpModel = new Model();
                    tmpModel.setRootPage(new RootPage());
                    tmpModel.getRootPage().getChildPages().add(page);
                    try {
                        JAXBContext jc = JAXBContext.newInstance(Model.JAXB_MODEL_PACKAGES);
                        Marshaller marshaller = jc.createMarshaller();
                        StringWriter sw = new StringWriter();
                        marshaller.marshal(tmpModel, sw);
                        Unmarshaller unmarshaller = jc.createUnmarshaller();
                        tmpModel = (Model) unmarshaller.unmarshal(new StringReader(sw.toString()));
                    } catch (JAXBException e) {
                        SessionMessages.addErrorMessage(getMessage("page.copy.error"));
                        logger.error("Error copying page", e);
                        return new RedirectResolution(dispatch.getOriginalPath());
                    }
                    newPage = tmpModel.getRootPage().getChildPages().get(0);
                    newPage.setFragment(fragment);
                    new CopyVisitor().visit(newPage);
                    if(detail) {
                        ((CrudPage) newParent).addDetailChild(newPage);
                    } else {
                        newParent.addChild(newPage);
                    }
                    saveModel();
                    return new RedirectResolution(""); //PageLogic.getPagePath(page)); TODO
                } else {
                    String msg = MessageFormat.format(getMessage("page.copy.invalidDestination"), destinationPageId);
                    SessionMessages.addErrorMessage(msg);
                }
            }
        }
        return new RedirectResolution(dispatch.getOriginalPath());
    }

    protected class CopyVisitor extends ModelVisitor {

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
        DefaultSelectionProvider classSelectionProvider = new DefaultSelectionProvider("pageClassName");
        classSelectionProvider.appendRow(CrudPage.class.getName(), "Crud", true);
        classSelectionProvider.appendRow(ChartPage.class.getName(), "Chart", true);
        classSelectionProvider.appendRow(TextPage.class.getName(), "Text", true);
        classSelectionProvider.appendRow(JspPage.class.getName(), "JSP", true);
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
                .configSelectionProvider(classSelectionProvider, "pageClassName")
                .configSelectionProvider(insertPositionSelectionProvider, "insertPositionName")
                .build();
        ((SelectField) newPageForm.findFieldByPropertyName("insertPositionName")).setValue(InsertPosition.CHILD.name());
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
        Page page = getPage();

        setupGroups(page);

        Session session = application.getSystemSession();
        users = (List) QueryUtils.runHqlQuery(session, "from users", null);

        return forwardToPagePermissions();
    }

    @Button(list = "testUserPermissions", key = "user.permissions.test")
    public Resolution testUserPermissions() {
        if(StringUtils.isBlank(testUserId)) {
            return new RedirectResolution(dispatch.getOriginalPath());
        }
        List<String> groups = SecurityLogic.manageGroups(application, testUserId);
        Page page = getPage();
        Permissions permissions = page.getPermissions();
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

    public void setupGroups(Page page) {
        Session session = application.getSystemSession();
        Criteria criteria = session.createCriteria(SecurityLogic.GROUP_ENTITY_NAME).addOrder(Order.asc("name"));
        groups = new ArrayList(criteria.list());
    }

    @RequiresAdministrator
    @Button(list = "page-permissions-edit", key = "commons.update", order = 1)
    public Resolution updatePagePermissions() {
        Page page = getPage();
        synchronized (application) {
            updatePagePermissions(page);
            saveModel();

            SessionMessages.addInfoMessage(getMessage("permissions.page.updated"));
        }

        return new RedirectResolution(dispatch.getOriginalPath())
                .addParameter("pagePermissions");
    }

    public void updatePagePermissions(Page page) {
        Permissions pagePermissions = page.getPermissions();

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

    public String getDestinationPageId() {
        return destinationPageId;
    }

    public void setDestinationPageId(String destinationPageId) {
        this.destinationPageId = destinationPageId;
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
