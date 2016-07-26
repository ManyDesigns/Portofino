/*
 * Copyright (C) 2005-2016 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.dispatcher;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.servlet.ServletUtils;
import com.manydesigns.portofino.di.Injections;
import com.manydesigns.portofino.pages.Page;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory object that can produce {@link Dispatch} instances given the requested path.
 *
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class Dispatcher {
    public static final String copyright =
            "Copyright (C) 2005-2016, ManyDesigns srl";

    public static final Logger logger =
            LoggerFactory.getLogger(Dispatcher.class);

    protected final Configuration configuration;
    protected final File pagesDirectory;
    protected final Map<String, Dispatch> cache = new ConcurrentHashMap<String, Dispatch>();

    public Dispatcher(Configuration configuration, File pagesDirectory) {
        this.configuration = configuration;
        this.pagesDirectory = pagesDirectory;
    }

    /**
     * Returns a dispatch for the provided path.
     * @param path the path to resolve, not including the context path.
     * @return the dispatch. If no dispatch can be constructed, this method returns null.
     */
    public Dispatch getDispatch(String path) {
        if(!isPotentialPagePath(path)) {
            logger.debug("Path is certainly not a page ({}), not dispatching to save time.", path);
            return null;
        }

        path = normalizePath(path);

        if(path.isEmpty() || path.equals("/")) {
            return null;
        }

        Dispatch dispatch = cache.get(path);
        if(dispatch != null) {
            return dispatch;
        }

        int index;
        String subPath = path;
        while((index = subPath.lastIndexOf('/')) != -1) {
            subPath = path.substring(0, index);
            dispatch = cache.get(subPath);
            if(dispatch != null) {
                break;
            }
        }

        Iterator<String> fragmentsIterator;
        DispatchElement currentElement;
        PageInstance currentPageInstance;
        List<PageInstance> pagePath = new ArrayList<PageInstance>();
        if(dispatch == null) {
            String[] fragments = StringUtils.split(path, '/');
            List<String> fragmentsAsList = Arrays.asList(fragments);
            fragmentsIterator = fragmentsAsList.iterator();
            try {
                currentElement = new Root();
                currentPageInstance = currentElement.getPageInstance();
                pagePath.add(currentPageInstance);
            } catch (Exception e) {
                logger.error("Cannot load root page", e);
                return null;
            }
        } else {
            pagePath = new ArrayList<PageInstance>(Arrays.asList(dispatch.getPageInstancePath()));
            String[] fragments = StringUtils.split(path.substring(subPath.length()), '/');

            List<String> fragmentsAsList = Arrays.asList(fragments);
            fragmentsIterator = fragmentsAsList.listIterator();
            currentElement = dispatch.getLastPageInstance().getActionBean();
        }

        while(fragmentsIterator.hasNext()) {
            try {
                currentElement = currentElement.consumePathFragment(fragmentsIterator.next());
                if(currentElement == null) {
                    logger.debug("Couldn't create dispatch");
                    return null;
                }
                currentPageInstance = currentElement.getPageInstance();
                if(!pagePath.contains(currentPageInstance)) {
                    pagePath.add(currentPageInstance);
                }
            } catch (Exception e) {
                logger.debug("Couldn't create dispatch", e);
                return null;
            }
        }
        dispatch = new Dispatch(pagePath.toArray(new PageInstance[pagePath.size()]));
        cache.put(path, dispatch);
        return dispatch;
    }

    protected boolean isPotentialPagePath(String path) {
        return !(
                ((path.startsWith("/webjars/") || path.startsWith("/theme/") || path.startsWith("/m/")) &&
                 (path.endsWith(".js") || path.endsWith(".css") || path.endsWith(".html"))) ||
                path.endsWith(".jsp"));
    }

    protected static String normalizePath(String originalPath) {
        String path = ServletUtils.removePathParameters(originalPath);
        path = ServletUtils.removeRedundantTrailingSlashes(path);
        return path;
    }

    public class Root implements DispatchElement {

        private PageInstance pageInstance;

        public Root() {
            Page rootPage = DispatcherLogic.getPage(pagesDirectory);
            pageInstance = new PageInstance(null, pagesDirectory, rootPage, null);
        }

        @Override
        public DispatchElement consumePathFragment(String pathFragment) {
            PageAction subpage = DispatcherLogic.getSubpage(configuration, pageInstance, pathFragment);
            HttpServletRequest request = ElementsThreadLocals.getHttpServletRequest();
            Injections.inject(subpage, request.getServletContext(), request);
            return subpage;
        }

        public PageInstance getPageInstance() {
            return pageInstance;
        }

        public void setPageInstance(PageInstance pageInstance) {
            this.pageInstance = pageInstance;
        }
    }

}
