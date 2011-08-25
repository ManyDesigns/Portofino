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

import com.manydesigns.portofino.context.Application;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.pages.*;
import net.sourceforge.stripes.controller.StripesConstants;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

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
        String originalPath = (String) request.getAttribute(
                StripesConstants.REQ_ATTR_INCLUDE_PATH);
        if (originalPath == null) {
            originalPath = request.getServletPath();
        }

        List<PageInstance> path = new ArrayList<PageInstance>();
        List<PageInstance> tree = new ArrayList<PageInstance>();

        Model model = application.getModel();

        if (model == null) {
            logger.error("Model is null");
            throw new Error("Model is null");
        }

        Page rootPage = model.getRootPage();
        List<Page> pageList = rootPage.getChildPages();
        String[] fragments = StringUtils.split(originalPath, '/');

        List<String> fragmentsAsList = Arrays.asList(fragments);
        ListIterator<String> fragmentsIterator = fragmentsAsList.listIterator();

        visitPagesInPath(path, tree, pageList, fragmentsIterator);

        if (path.isEmpty()) {
            return null;
        }

        if (fragmentsIterator.hasNext()) {
            logger.debug("Not all fragments matched");
            return null;
        }

        PageInstance pageInstance =
                path.get(path.size() - 1);
        Page page = pageInstance.getPage();
        String rewrittenPath = page.getUrl();
        if (rewrittenPath == null) {
            if (page instanceof TextPage) {
                rewrittenPath = "/text.action";
            } else if (page instanceof ChartPage) {
                rewrittenPath = "/chart.action";
            } else if (page instanceof FolderPage) {
                rewrittenPath = "/index.action";
            } else if (page instanceof CrudPage) {
                rewrittenPath = "/crud.action";
            } else if (page instanceof JspPage) {
                rewrittenPath = "/jsp.action";
            } else {
                throw new Error("Unrecognized page type");
            }
        }

        PageInstance[] pageArray =
                new PageInstance[path.size()];
        path.toArray(pageArray);

        return new Dispatch(request, originalPath, rewrittenPath, pageArray, tree);
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
        } else {
            pageInstance =
                    new PageInstance(application, page, null);
        }

        // add to path
        path.add(pageInstance);

        // visit recursively
        visitPagesInPath(path, pageInstance.getChildPageInstances(),
                pageInstance.getChildPages(), fragmentsIterator);

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
