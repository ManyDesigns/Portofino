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
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.pages.ChildPage;
import com.manydesigns.portofino.pages.Page;
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

        String[] fragments = StringUtils.split(path, '/');

        List<String> fragmentsAsList = Arrays.asList(fragments);
        ListIterator<String> fragmentsIterator = fragmentsAsList.listIterator();

        File rootDir = application.getPagesDir();
        Page rootPage;
        try {
            rootPage = DispatcherLogic.getPage(rootDir);
        } catch (Exception e) {
            logger.error("Cannot load root page", e);
            return null;
        }

        PageInstance rootPageInstance = new PageInstance(null, rootDir, application, rootPage);
        pagePath.add(rootPageInstance);
        try {
            makePageInstancePath(pagePath, fragmentsIterator, rootPageInstance);
        } catch (PageNotActiveException e) {
            logger.debug("Page not active, not creating dispatch");
            return null;
        } catch (Exception e) {
            logger.error("Couldn't create dispatch", e);
            return null;
        }

        if (fragmentsIterator.hasNext()) {
            logger.debug("Not all fragments matched");
            return null;
        }

        // check path contains root page and some child page at least
        if (pagePath.size() <= 1) {
            return null;
        }

        PageInstance[] pageArray =
                new PageInstance[pagePath.size()];
        pagePath.toArray(pageArray);

        Dispatch dispatch = new Dispatch(contextPath, path, pageArray);
        return dispatch;
        //return checkDispatch(dispatch);
    }

    protected void makePageInstancePath
            (List<PageInstance> pagePath, ListIterator<String> fragmentsIterator, PageInstance parentPageInstance)
            throws Exception {
        File currentDirectory = parentPageInstance.getDirectory();
        boolean params = false;
        while(fragmentsIterator.hasNext()) {
            String nextFragment = fragmentsIterator.next();
            File childDirectory = new File(currentDirectory, nextFragment);
            if(childDirectory.isDirectory() && !PageInstance.DETAIL.equals(childDirectory.getName())) {
                ChildPage childPage = null;
                for(ChildPage candidate : parentPageInstance.getLayout().getChildPages()) {
                    if(candidate.getName().equals(childDirectory.getName())) {
                        childPage = candidate;
                        break;
                    }
                }
                if(childPage == null) {
                    throw new PageNotActiveException();
                }

                Page page = DispatcherLogic.getPage(childDirectory);
                PageInstance pageInstance = new PageInstance(parentPageInstance, childDirectory, application, page);
                pagePath.add(pageInstance);
                makePageInstancePath(pagePath, fragmentsIterator, pageInstance);
                return;
            } else {
                if(!params) {
                    currentDirectory = new File(currentDirectory, PageInstance.DETAIL);
                    params = true;
                }
                parentPageInstance.getParameters().add(nextFragment);
            }
        }
    }

    public static boolean isValidActionClass(Class<?> actionClass) {
        if(actionClass == null) {
            return false;
        }
        if(!PageAction.class.isAssignableFrom(actionClass)) {
            logger.error("Action class must implement PortofinoAction: " + actionClass);
            return false;
        }
        return true;
    }

    protected Dispatch checkDispatch(Dispatch dispatch) {
        String pathUrl = dispatch.getLastPageInstance().getPath();
        assert pathUrl.equals(normalizePath(dispatch.getOriginalPath()));
        return dispatch;
    }

    protected static String normalizePath(String originalPath) {
        int trimPosition = originalPath.length() - 1;
        while(trimPosition >= 0 && originalPath.charAt(trimPosition) == '/') {
            trimPosition--;
        }
        String withoutTrailingSlashes = originalPath.substring(0, trimPosition + 1);
        while (withoutTrailingSlashes.contains("//")) {
            withoutTrailingSlashes = withoutTrailingSlashes.replace("//", "/");
        }
        return withoutTrailingSlashes;
    }

    protected static class PageNotActiveException extends Exception {}
}
