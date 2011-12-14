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

package com.manydesigns.portofino.dispatcher;

import com.manydesigns.elements.servlet.ServletUtils;
import com.manydesigns.portofino.actions.CrudAction;
import com.manydesigns.portofino.actions.JspAction;
import com.manydesigns.portofino.actions.PageReferenceAction;
import com.manydesigns.portofino.actions.TextAction;
import com.manydesigns.portofino.actions.chart.ChartAction;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.pages.*;
import com.manydesigns.portofino.scripting.ScriptingUtil;
import net.sourceforge.stripes.action.ActionBean;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class Dispatcher {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    public static final Logger logger =
            LoggerFactory.getLogger(Dispatcher.class);

    protected final Application application;

    public Dispatcher(Application application) {
        this.application = application;
    }

    public Dispatch createDispatch(HttpServletRequest request) {
        String originalPath = ServletUtils.getOriginalPath(request);

        return createDispatch(request.getContextPath(), originalPath);
    }

    public Dispatch createDispatch(String contextPath, String path) {
        if(path.endsWith(".jsp")) {
            logger.debug("Path is a JSP page ({}), not dispatching.", path);
            return null;
        }

        List<PageInstance> pagePath = new ArrayList<PageInstance>();

        Model model = application.getModel();

        if (model == null) {
            logger.error("Model is null");
            throw new Error("Model is null");
        }

        String[] fragments = StringUtils.split(path, '/');

        List<String> fragmentsAsList = Arrays.asList(fragments);
        ListIterator<String> fragmentsIterator = fragmentsAsList.listIterator();

        Page rootPage = model.getRootPage();
        visitPageInPath(pagePath, fragmentsIterator, rootPage);

        if (fragmentsIterator.hasNext()) {
            logger.debug("Not all fragments matched");
            return null;
        }

        // check path contains root page and some child page at least
        if (pagePath.size() <= 1) {
            return null;
        }

        PageInstance pageInstance = pagePath.get(pagePath.size() - 1);
        Page page = pageInstance.getPage();
        Class<? extends ActionBean> actionBeanClass = null;
        try {
            actionBeanClass = getActionBeanClass(application, page);
        } catch (ClassNotFoundException e) {
            logger.error("Couldn't get action bean class for " + page, e);
        }

        PageInstance[] pageArray =
                new PageInstance[pagePath.size()];
        pagePath.toArray(pageArray);

        return new Dispatch(contextPath, path, actionBeanClass, pageArray);
    }

    public static Class<? extends ActionBean> getActionBeanClass(Application application, Page page)
            throws ClassNotFoundException {
        if(page == null) {
            return null;
        }
        Class<?> actionClass = page.getActualActionClass();
        if (!isValidActionClass(actionClass)) {
            actionClass = getScriptActionClass(application, page);
            page.setActualActionClass(actionClass);
        }
        if (isValidActionClass(actionClass)) {
            return (Class<? extends ActionBean>) actionClass;
        } else {
            return getDefaultActionClass(page);
        }
    }

    protected static boolean isValidActionClass(Class<?> actionClass) {
        if(actionClass == null) {
            return false;
        }
        if(!ActionBean.class.isAssignableFrom(actionClass)) {
            logger.error("Action class must implement ActionBean: " + actionClass);
            return false;
        }
        return true;
    }

    public static Class<?> getScriptActionClass(Application application, Page page) {
        try {
            File storageDirFile = application.getAppStorageDir();
            String id = page.getId();
            return ScriptingUtil.getGroovyClass(storageDirFile, id);
        } catch (Exception e) {
            logger.error("Couldn't load script for " + page, e);
            return null;
        }
    }

    protected static Class<? extends ActionBean> getDefaultActionClass(Page page) {
        if (page instanceof TextPage) {
            return TextAction.class;
        } else if (page instanceof ChartPage) {
            return ChartAction.class;
        }/* else if (page instanceof FolderPage) {
            className = "/actions/index";
        }*/ else if (page instanceof CrudPage) {
            return CrudAction.class;
        } else if (page instanceof JspPage) {
            return JspAction.class;
        } else if (page instanceof PageReference) {
            return PageReferenceAction.class;
        } else if (page instanceof RootPage) {
            return null;
        } else {
            throw new Error("Unrecognized page type: " + page.getClass().getName());
        }
    }

    private void visitPagesInPath(List<PageInstance> path,
                                  List<PageInstance> tree,
                                  List<Page> pages,
                                  ListIterator<String> fragmentsIterator) {
        if (!fragmentsIterator.hasNext()) {
            logger.debug("Beyond available fragments. Switching to visitPagesOutsidePath().");
            visitPagesOutsidePath(tree, pages);
            return;
        }

        String fragment = fragmentsIterator.next();

        boolean visitedInPath = false;
        for (Page page : pages) {
            // Wrap Page in PageInstance
            PageInstance pageInstance;
            if (fragment.equals(page.getFragment())) {
                pageInstance = visitPageInPath(path, fragmentsIterator, page);
                visitedInPath = true;
            } else {
                pageInstance = visitPageOutsidePath(page);
            }
            tree.add(pageInstance);
        }
        if (!visitedInPath) {
            fragmentsIterator.previous();
        }
    }

    private PageInstance visitPageInPath(List<PageInstance> path,
                                 ListIterator<String> fragmentsIterator,
                                 Page page) {
        PageInstance pageInstance = makePageInstance(page, fragmentsIterator);

        // add to path
        path.add(pageInstance);

        // visit recursively
        visitPagesInPath(path, pageInstance.getChildPageInstances(),
                pageInstance.getChildPages(), fragmentsIterator);

        return pageInstance;
    }

    protected PageInstance makePageInstance(Page page, ListIterator<String> fragmentsIterator) {
        PageInstance pageInstance;
        if (page instanceof CrudPage) {
            CrudPage crudPage = (CrudPage) page;
            String mode;
            String param;
            if (fragmentsIterator.hasNext()) {
                String peek = fragmentsIterator.next();
                if (CrudPage.MODE_NEW.equals(peek)) {
                    mode = CrudPage.MODE_NEW;
                    param = null;
                } else if (matchSearchChildren(page, peek)) {
                    mode = CrudPage.MODE_SEARCH;
                    param = null;
                    fragmentsIterator.previous();
                } else {
                    mode = CrudPage.MODE_DETAIL;
                    param = peek;
                }
            } else {
                mode = CrudPage.MODE_SEARCH;
                param = null;
            }
            pageInstance = new CrudPageInstance(
                    application, crudPage, mode, param);
        } else if(page instanceof PageReference) {
            Page toPage = ((PageReference) page).getToPage();
            if(toPage != null) {
                PageInstance wrappedPageInstance = makePageInstance(toPage, fragmentsIterator);
                pageInstance = new PageReferenceInstance(application, page, null, wrappedPageInstance);
            } else {
                pageInstance =
                    new PageInstance(application, page, null);
            }
        } else {
            pageInstance =
                    new PageInstance(application, page, null);
        }
        return pageInstance;
    }

    private boolean matchSearchChildren(Page page, String peek) {
        for (Page current : page.getChildPages()) {
            if (peek.equals(current.getFragment())) {
                return true;
            }
        }
        return false;
    }


    private PageInstance visitPageOutsidePath(Page page) {
        PageInstance pageInstance;
        if (page instanceof CrudPage) {
            CrudPage crudPage = (CrudPage) page;
            pageInstance = new CrudPageInstance(
                    application, crudPage, CrudPage.MODE_SEARCH, null);
        } else {
            pageInstance =
                    new PageInstance(application, page, null);
        }

        // visit recursively
        visitPagesOutsidePath(pageInstance.getChildPageInstances(),
                pageInstance.getChildPages());

        return pageInstance;
    }

    private void visitPagesOutsidePath(List<PageInstance> tree,
                                       List<Page> pages) {
        for (Page page : pages) {
            // Wrap Page in PageInstance
            PageInstance pageInstance = visitPageOutsidePath(page);
            tree.add(pageInstance);
        }
    }

}
