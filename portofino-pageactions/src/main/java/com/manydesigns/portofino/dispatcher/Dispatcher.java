/*
 * Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
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

import com.manydesigns.portofino.pages.ChildPage;
import com.manydesigns.portofino.pages.Page;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            "Copyright (c) 2005-2013, ManyDesigns srl";

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
        if(path.endsWith(".jsp")) {
            logger.debug("Path is a JSP page ({}), not dispatching.", path);
            return null;
        }

        path = normalizePath(path);

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
        if(dispatch == null) {
            List<PageInstance> pagePath = new ArrayList<PageInstance>();
            String[] fragments = StringUtils.split(path, '/');

            List<String> fragmentsAsList = Arrays.asList(fragments);
            ListIterator<String> fragmentsIterator = fragmentsAsList.listIterator();

            File rootDir = pagesDirectory;
            Page rootPage;
            try {
                rootPage = DispatcherLogic.getPage(rootDir);
            } catch (Exception e) {
                logger.error("Cannot load root page", e);
                return null;
            }

            PageInstance rootPageInstance = new PageInstance(null, rootDir, rootPage, null);
            pagePath.add(rootPageInstance);

            dispatch = getDispatch(pagePath, fragmentsIterator);
            if(dispatch != null) {
                cache.put(path, dispatch);
            }
            return dispatch;
        } else {
            List<PageInstance> pagePath =
                    new ArrayList<PageInstance>(Arrays.asList(dispatch.getPageInstancePath()));
            String[] fragments = StringUtils.split(path.substring(subPath.length()), '/');

            List<String> fragmentsAsList = Arrays.asList(fragments);
            ListIterator<String> fragmentsIterator = fragmentsAsList.listIterator();

            dispatch = getDispatch(pagePath, fragmentsIterator);
            if(dispatch != null) {
                cache.put(path, dispatch);
            }
            return dispatch;
        }
    }

    protected Dispatch getDispatch(
            List<PageInstance> initialPath,
            ListIterator<String> fragmentsIterator) {
        try {
            makePageInstancePath(initialPath, fragmentsIterator);
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
        if (initialPath.size() <= 1) {
            return null;
        }

        PageInstance[] pageArray =
                new PageInstance[initialPath.size()];
        initialPath.toArray(pageArray);

        Dispatch dispatch = new Dispatch(pageArray);
        return dispatch;
        //return checkDispatch(dispatch);
    }

    protected void makePageInstancePath
            (List<PageInstance> pagePath, ListIterator<String> fragmentsIterator)
            throws Exception {
        PageInstance parentPageInstance = pagePath.get(pagePath.size() - 1);
        File currentDirectory = parentPageInstance.getChildrenDirectory();
        boolean params = !parentPageInstance.getParameters().isEmpty();
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
                Class<? extends PageAction> actionClass =
                        DispatcherLogic.getActionClass(configuration, childDirectory);
                PageInstance pageInstance =
                        new PageInstance(parentPageInstance, childDirectory, page, actionClass);
                pagePath.add(pageInstance);
                makePageInstancePath(pagePath, fragmentsIterator);
                return;
            } else {
                if(!params) {
                    currentDirectory = new File(currentDirectory, PageInstance.DETAIL);
                    params = true;
                }
                parentPageInstance = parentPageInstance.copy();
                parentPageInstance.getParameters().add(nextFragment);
                pagePath.set(pagePath.size() - 1, parentPageInstance);
            }
        }
    }

    protected static String normalizePath(String originalPath) {
        String path = removePathParameters(originalPath);
        path = removeRedundantTrailingSlashes(path);
        return path;
    }

    /**
     * See <a href="http://tomcat.10.n6.nabble.com/Path-parameters-and-getRequestURI-td4377159.html">this</a>
     * and <a href="http://tomcat.markmail.org/thread/ykx72wcuzcmiyujz">this</a>.
     */
    protected static String removePathParameters(String originalPath) {
        String[] tokens = originalPath.split("/");
        for(int i = 0; i < tokens.length; i++) {
            int index = tokens[i].indexOf(";");
            if(index >= 0) {
                tokens[i] = tokens[i].substring(0, index);
            }
        }
        return StringUtils.join(tokens, "/");
    }

    protected static String removeRedundantTrailingSlashes(String path) {
        int trimPosition = path.length() - 1;
        while(trimPosition >= 0 && path.charAt(trimPosition) == '/') {
            trimPosition--;
        }
        String withoutTrailingSlashes = path.substring(0, trimPosition + 1);
        while (withoutTrailingSlashes.contains("//")) {
            withoutTrailingSlashes = withoutTrailingSlashes.replace("//", "/");
        }
        return withoutTrailingSlashes;
    }

}
