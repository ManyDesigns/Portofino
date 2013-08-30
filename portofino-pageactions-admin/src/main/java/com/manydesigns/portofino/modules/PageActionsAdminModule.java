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

package com.manydesigns.portofino.modules;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.actions.admin.page.PageAdminAction;
import com.manydesigns.portofino.actions.admin.page.RootChildrenAction;
import com.manydesigns.portofino.actions.admin.page.RootPermissionsAction;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.dispatcher.PageAction;
import com.manydesigns.portofino.head.HtmlHeadBuilder;
import com.manydesigns.portofino.logic.SecurityLogic;
import com.manydesigns.portofino.menu.*;
import com.manydesigns.portofino.security.AccessLevel;
import net.sf.ehcache.CacheManager;
import net.sourceforge.stripes.util.UrlBuilder;
import org.apache.commons.configuration.Configuration;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.web.env.EnvironmentLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.List;
import java.util.Locale;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class PageActionsAdminModule implements Module {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    @Inject(BaseModule.SERVLET_CONTEXT)
    public ServletContext servletContext;

    @Inject(BaseModule.PORTOFINO_CONFIGURATION)
    public Configuration configuration;

    @Inject(BaseModule.APPLICATION_DIRECTORY)
    public File applicationDirectory;

    @Inject(BaseModule.ADMIN_MENU)
    public MenuBuilder adminMenu;

    @Inject(BaseModule.APP_MENU)
    public MenuBuilder appMenu;

    @Inject(BaseModule.HTML_HEAD_BUILDER)
    public HtmlHeadBuilder headBuilder;

    @Inject(BaseModule.CLASS_LOADER)
    public ClassLoader originalClassLoader;

    @Inject(BaseModule.APP_LISTENERS)
    public List<ApplicationListener> applicationListeners;

    protected EnvironmentLoader environmentLoader = new EnvironmentLoader();

    protected CacheManager cacheManager;

    protected ModuleStatus status = ModuleStatus.CREATED;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(PageActionsAdminModule.class);

    @Override
    public String getModuleVersion() {
        return configuration.getString(PortofinoProperties.PORTOFINO_VERSION);
    }

    @Override
    public int getMigrationVersion() {
        return 1;
    }

    @Override
    public double getPriority() {
        return 1;
    }

    @Override
    public String getId() {
        return "portofino-pageactions-admin";
    }

    @Override
    public String getName() {
        return "Portofino PageActions Admin";
    }

    @Override
    public int install() {
        return 1;
    }

    @Override
    public void init() {
        appendToAdminMenu();
        appendToAppMenu();

        status = ModuleStatus.ACTIVE;
    }

    protected void appendToAppMenu() {
        appMenu.menuAppenders.add(new MenuAppender() {
            @Override
            public void append(Menu menu) {
                HttpServletRequest request = ElementsThreadLocals.getHttpServletRequest();
                if(!(request.getAttribute("actionBean") instanceof PageAction)) {
                    return;
                }
                PageAction pageAction = (PageAction) request.getAttribute("actionBean");
                if(pageAction.getDispatch() != null &&
                   SecurityLogic.hasPermissions(
                           configuration, pageAction.getPageInstance(),
                           SecurityUtils.getSubject(), AccessLevel.EDIT)) {
                    MenuGroup pageGroup = new MenuGroup("page", "icon-file", "Page", 1.0);
                    menu.items.add(pageGroup);
                }
            }
        });

        appMenu.menuAppenders.add(new PageMenuAppender() {
            @Override
            public void append(MenuGroup pageMenu, PageAction pageAction) {
                HttpServletRequest request = ElementsThreadLocals.getHttpServletRequest();

                MenuLink link = new MenuLink(
                        "editLayout",
                        "icon-file",
                        "Edit layout",
                        "javascript:portofino.enablePortletDragAndDrop($(this), '" +
                                pageAction.getDispatch().getOriginalPath() +
                                "');",
                        1.0);
                pageMenu.menuLinks.add(link);

                UrlBuilder urlBuilder = new UrlBuilder(Locale.getDefault(), PageAdminAction.class, false);
                urlBuilder.addParameter("originalPath", pageAction.getDispatch().getOriginalPath());
                urlBuilder.setEvent("pageChildren");
                link = new MenuLink(
                        "pageChildren",
                        "icon-folder-open",
                        "Page children",
                        request.getContextPath() + urlBuilder.toString(),
                        2.0);
                pageMenu.menuLinks.add(link);

                urlBuilder = new UrlBuilder(Locale.getDefault(), PageAdminAction.class, false);
                urlBuilder.addParameter("originalPath", pageAction.getDispatch().getOriginalPath());
                urlBuilder.setEvent("newPage");
                link = new MenuLink(
                        "newPage",
                        "icon-plus",
                        "Add new page",
                        request.getContextPath() + urlBuilder.toString(),
                        3.0);
                pageMenu.menuLinks.add(link);

                String jsArgs = "('" +
                        pageAction.getDispatch().getOriginalPath() + "', '" +
                        request.getContextPath() + "');";

                link = new MenuLink(
                        "deletePage",
                        "icon-minus",
                        "Delete page",
                        "javascript:portofino.confirmDeletePage" + jsArgs,
                        4.0);
                pageMenu.menuLinks.add(link);

                link = new MenuLink(
                        "copyPage",
                        "icon-file",
                        "Copy page",
                        "javascript:portofino.showCopyPageDialog" + jsArgs,
                        5.0);
                pageMenu.menuLinks.add(link);

                link = new MenuLink(
                        "movePage",
                        "icon-share",
                        "Move page",
                        "javascript:portofino.showMovePageDialog" + jsArgs,
                        6.0);
                pageMenu.menuLinks.add(link);

                if(SecurityLogic.hasPermissions(
                        configuration, pageAction.getPageInstance(),
                        SecurityUtils.getSubject(), AccessLevel.DEVELOP)) {
                    urlBuilder = new UrlBuilder(Locale.getDefault(), PageAdminAction.class, false);
                    urlBuilder.addParameter("originalPath", pageAction.getDispatch().getOriginalPath());
                    urlBuilder.setEvent("pagePermissions");
                    link = new MenuLink(
                            "pagePermissions",
                            "icon-user",
                            "Page permissions",
                            request.getContextPath() + urlBuilder.toString(),
                        7.0);
                    pageMenu.menuLinks.add(link);
                }
            }
        });

    }

    protected void appendToAdminMenu() {
        SimpleMenuAppender group;
        SimpleMenuAppender link;

        group = SimpleMenuAppender.group("security", null, "Security", 2.0);
        adminMenu.menuAppenders.add(group);

        link = SimpleMenuAppender.link(
                "security", "rootPermissions", null, "Root permissions", RootPermissionsAction.URL_BINDING, 1.0);
        adminMenu.menuAppenders.add(link);

        link = SimpleMenuAppender.link(
                "configuration", "topLevelPages", null, "Top-level pages", RootChildrenAction.URL_BINDING, 3.0);
        adminMenu.menuAppenders.add(link);
    }

    public abstract class PageMenuAppender implements MenuAppender {
        @Override
        public void append(Menu menu) {
            for(MenuItem item : menu.items) {
                if("page".equals(item.id) && item instanceof MenuGroup) {
                    HttpServletRequest request = ElementsThreadLocals.getHttpServletRequest();
                    if(!(request.getAttribute("actionBean") instanceof PageAction)) {
                        return;
                    }
                    PageAction pageAction = (PageAction) request.getAttribute("actionBean");
                    if(pageAction.getDispatch() != null &&
                       SecurityLogic.hasPermissions(
                               configuration, pageAction.getPageInstance(),
                               SecurityUtils.getSubject(), AccessLevel.EDIT)) {
                        append((MenuGroup) item, pageAction);
                    }
                    break;
                }
            }
        }

        protected abstract void append(MenuGroup pageMenu, PageAction pageAction);
    }

    @Override
    public void destroy() {
        status = ModuleStatus.DESTROYED;
    }

    @Override
    public ModuleStatus getStatus() {
        return status;
    }
}
