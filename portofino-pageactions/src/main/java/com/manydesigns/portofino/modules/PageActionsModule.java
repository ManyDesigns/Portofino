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
import com.manydesigns.elements.util.ElementsFileUtils;
import com.manydesigns.elements.util.Util;
import com.manydesigns.elements.xml.XhtmlBuffer;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.RequestAttributes;
import com.manydesigns.portofino.actions.admin.SettingsAction;
import com.manydesigns.portofino.actions.admin.page.PageAdminAction;
import com.manydesigns.portofino.actions.admin.page.RootChildrenAction;
import com.manydesigns.portofino.actions.admin.page.RootPermissionsAction;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.di.Injections;
import com.manydesigns.portofino.dispatcher.Dispatch;
import com.manydesigns.portofino.dispatcher.DispatcherLogic;
import com.manydesigns.portofino.dispatcher.DispatcherUtil;
import com.manydesigns.portofino.dispatcher.PageAction;
import com.manydesigns.portofino.files.TempFileService;
import com.manydesigns.portofino.head.HtmlHead;
import com.manydesigns.portofino.head.HtmlHeadAppender;
import com.manydesigns.portofino.head.HtmlHeadBuilder;
import com.manydesigns.portofino.logic.SecurityLogic;
import com.manydesigns.portofino.menu.*;
import com.manydesigns.portofino.pageactions.calendar.CalendarAction;
import com.manydesigns.portofino.pageactions.custom.CustomAction;
import com.manydesigns.portofino.pageactions.login.DefaultLoginAction;
import com.manydesigns.portofino.pageactions.login.OpenIdLoginAction;
import com.manydesigns.portofino.pageactions.registry.PageActionRegistry;
import com.manydesigns.portofino.pageactions.text.TextAction;
import com.manydesigns.portofino.scripting.ScriptingUtil;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.shiro.SecurityGroovyRealm;
import groovy.util.GroovyScriptEngine;
import net.sf.ehcache.CacheManager;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.util.UrlBuilder;
import ognl.OgnlRuntime;
import org.apache.commons.configuration.Configuration;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.util.LifecycleUtils;
import org.apache.shiro.web.env.EnvironmentLoader;
import org.apache.shiro.web.env.WebEnvironment;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class PageActionsModule implements Module {
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
    // Constants
    //**************************************************************************

    public static final String GROOVY_SCRIPT_ENGINE = "GROOVY_SCRIPT_ENGINE";
    public static final String GROOVY_CLASS_PATH = "GROOVY_CLASS_PATH";
    public static final String PAGES_DIRECTORY = "PAGES_DIRECTORY";
    public static final String EHCACHE_MANAGER = "portofino.ehcache.manager";
    public static final String PAGE_ACTIONS_REGISTRY =
            "com.manydesigns.portofino.pageactions.registry.PageActionRegistry";

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(PageActionsModule.class);

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
        return "portofino-pageactions";
    }

    @Override
    public String getName() {
        return "Portofino PageActions";
    }

    @Override
    public int install() {
        return 1;
    }

    @Override
    public void init() {
        logger.debug("Initializing dispatcher");
        DispatcherLogic.init(configuration);

        logger.debug("Setting up temporary file service");
        String tempFileServiceClass = configuration.getString(PortofinoProperties.TEMP_FILE_SERVICE_CLASS);
        try {
            TempFileService.setInstance((TempFileService) Class.forName(tempFileServiceClass).newInstance());
        } catch (Exception e) {
            logger.error("Could not set up temp file service", e);
            throw new Error(e);
        }

        //Disabilitazione security manager per funzionare su GAE. Il security manager permette di valutare
        //in sicurezza espressioni OGNL provenienti da fonti non sicure, configurando i necessari permessi
        //(invoke.<declaring-class>.<method-name>). In Portofino non permettiamo agli utenti finali di valutare
        //espressioni OGNL arbitrarie, pertanto il security manager può essere disabilitato in sicurezza.
        logger.info("Disabling OGNL security manager");
        OgnlRuntime.setSecurityManager(null);

        logger.info("Initializing ehcache service");
        cacheManager = CacheManager.newInstance();
        servletContext.setAttribute(EHCACHE_MANAGER, cacheManager);

        logger.info("Initializing Shiro environment");
        WebEnvironment environment = environmentLoader.initEnvironment(servletContext);
        logger.debug("Publishing the Application Realm in the servlet context");
        RealmSecurityManager rsm = (RealmSecurityManager) environment.getWebSecurityManager();

        logger.info("Initializing Groovy script engine");
        File groovyClasspath = new File(applicationDirectory, "groovy");
        ElementsFileUtils.ensureDirectoryExistsAndWarnIfNotWritable(groovyClasspath);

        servletContext.setAttribute(GROOVY_CLASS_PATH, groovyClasspath);
        GroovyScriptEngine groovyScriptEngine = createScriptEngine(groovyClasspath);
        servletContext.setAttribute(GROOVY_SCRIPT_ENGINE, groovyScriptEngine);

        File appListenerFile = new File(groovyClasspath, "AppListener.groovy");
        try {
            ElementsThreadLocals.setServletContext(servletContext); //Necessary for getGroovyObject
            Object listener = ScriptingUtil.getGroovyObject(appListenerFile);
            if(listener != null) {
                if(listener instanceof ApplicationListener) {
                    ApplicationListener applicationListener = (ApplicationListener) listener;
                    logger.info("Groovy application listener found at {}", appListenerFile.getAbsolutePath());
                    Injections.inject(applicationListener, servletContext, null);
                    applicationListeners.add((ApplicationListener) listener);
                } else {
                    logger.error(
                            "Candidate app listener " + listener + " found at " + appListenerFile.getAbsolutePath() +
                            " is not an instance of " + ApplicationListener.class);
                }
            } else {
                logger.debug("No Groovy app listener present");
            }
        } catch (Throwable e) {
            logger.error("Could not invoke app listener", e);
        }

        File pagesDirectory = new File(applicationDirectory, "pages");
        ElementsFileUtils.ensureDirectoryExistsAndWarnIfNotWritable(pagesDirectory);
        servletContext.setAttribute(PAGES_DIRECTORY, pagesDirectory);

        ClassLoader classLoader = groovyScriptEngine.getGroovyClassLoader();
        servletContext.setAttribute(BaseModule.CLASS_LOADER, classLoader);

        logger.debug("Creating pageactions registry");
        PageActionRegistry pageActionRegistry = new PageActionRegistry();
        pageActionRegistry.register(CalendarAction.class);
        pageActionRegistry.register(CustomAction.class);
        pageActionRegistry.register(TextAction.class);
        pageActionRegistry.register(DefaultLoginAction.class);
        pageActionRegistry.register(OpenIdLoginAction.class);
        servletContext.setAttribute(PAGE_ACTIONS_REGISTRY, pageActionRegistry);

        File scriptFile = new File(groovyClasspath, "Security.groovy");
        SecurityGroovyRealm realm = new SecurityGroovyRealm(groovyScriptEngine, scriptFile.toURI().toString());
        LifecycleUtils.init(realm);
        rsm.setRealm(realm);

        appendToAdminMenu();
        appendToAppMenu();

        setupHead();

        status = ModuleStatus.ACTIVE;
    }

    protected GroovyScriptEngine createScriptEngine(File classpathFile) {
        CompilerConfiguration cc = new CompilerConfiguration(CompilerConfiguration.DEFAULT);
        String classpath = classpathFile.getAbsolutePath();
        logger.info("Groovy classpath: " + classpath);
        cc.setClasspath(classpath);
        cc.setRecompileGroovySource(true);
        GroovyScriptEngine scriptEngine;
        try {
            scriptEngine =
                    new GroovyScriptEngine(new URL[] { classpathFile.toURI().toURL() },
                                           originalClassLoader);
        } catch (IOException e) {
            throw new Error(e);
        }
        scriptEngine.setConfig(cc);
        return scriptEngine;
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
                    MenuGroup pageGroup = new MenuGroup("page", "icon-file icon-white", "Page", 1.0);
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
                        "icon-file icon-white",
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
                        "icon-folder-open icon-white",
                        "Page children",
                        request.getContextPath() + urlBuilder.toString(),
                        2.0);
                pageMenu.menuLinks.add(link);

                urlBuilder = new UrlBuilder(Locale.getDefault(), PageAdminAction.class, false);
                urlBuilder.addParameter("originalPath", pageAction.getDispatch().getOriginalPath());
                urlBuilder.setEvent("newPage");
                link = new MenuLink(
                        "newPage",
                        "icon-plus icon-white",
                        "Add new page",
                        request.getContextPath() + urlBuilder.toString(),
                        3.0);
                pageMenu.menuLinks.add(link);

                String jsArgs = "('" +
                        pageAction.getDispatch().getOriginalPath() + "', '" +
                        request.getContextPath() + "');";

                link = new MenuLink(
                        "deletePage",
                        "icon-minus icon-white",
                        "Delete page",
                        "javascript:portofino.confirmDeletePage" + jsArgs,
                        4.0);
                pageMenu.menuLinks.add(link);

                link = new MenuLink(
                        "copyPage",
                        "icon-file icon-white",
                        "Copy page",
                        "javascript:portofino.showCopyPageDialog" + jsArgs,
                        5.0);
                pageMenu.menuLinks.add(link);

                link = new MenuLink(
                        "movePage",
                        "icon-share icon-white",
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
                            "icon-user icon-white",
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
                "configuration", "settings", null, "Settings", SettingsAction.URL_BINDING, 0.5);
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

    protected void setupHead() {
        headBuilder.appenders.add(new HtmlHeadAppender() {
            @Override
            public void append(HtmlHead head) {
                XhtmlBuffer xb = new XhtmlBuffer();
                HttpServletRequest request = ElementsThreadLocals.getHttpServletRequest();

                xb.writeLink("stylesheet", "text/css",
                             Util.getAbsoluteUrl("/jquery-ui/css/no-theme/jquery-ui-1.10.3.custom.min.css"));
                String skin = (String) request.getAttribute(RequestAttributes.SKIN);
                if(skin != null) {
                    xb.writeLink("stylesheet", "text/css",
                                 Util.getAbsoluteUrl("/skins/" + skin + "/portofino.css"));
                }

                xb.openElement("script");
                xb.addAttribute("src", Util.getAbsoluteUrl("/portofino.js.jsp"));
                xb.closeElement("script");

                //Setup base href - uniform handling of .../resource and .../resource/
                ActionBean actionBean = (ActionBean) request.getAttribute("actionBean");
                Dispatch dispatch = DispatcherUtil.getDispatch(request, actionBean);
                if(dispatch != null) {
                    String baseHref = dispatch.getAbsoluteOriginalPath();
                    //Remove all trailing slashes
                    while (baseHref.length() > 1 && baseHref.endsWith("/")) {
                        baseHref = baseHref.substring(0, baseHref.length() - 1);
                    }
                    //Add a single trailing slash so all relative URLs use this page as the root
                    baseHref += "/";
                    //Try to make the base HREF absolute
                    try {
                        URL url = new URL(request.getRequestURL().toString());
                        String port = url.getPort() > 0 ? ":" + url.getPort() : "";
                        baseHref = url.getProtocol() + "://" + url.getHost() + port + baseHref;
                    } catch (MalformedURLException e) {
                        //Ignore
                    }

                    xb.openElement("base");
                    xb.addAttribute("href", baseHref);
                    xb.closeElement("base");
                }

                head.fragments.add(xb);
            }
        });
    }

    @Override
    public void destroy() {
        logger.info("Destroying Shiro environment...");
        environmentLoader.destroyEnvironment(servletContext);
        logger.info("Shutting down cache...");
        cacheManager.shutdown();
        logger.info("Removing Groovy classloader...");
        servletContext.removeAttribute(GROOVY_SCRIPT_ENGINE);
        servletContext.removeAttribute(GROOVY_CLASS_PATH);
        servletContext.setAttribute(BaseModule.CLASS_LOADER, originalClassLoader);
        status = ModuleStatus.DESTROYED;
    }

    @Override
    public ModuleStatus getStatus() {
        return status;
    }
}
