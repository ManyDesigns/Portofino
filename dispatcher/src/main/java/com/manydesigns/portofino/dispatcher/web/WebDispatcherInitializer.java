/*
 * Copyright (C) 2005-2021 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.dispatcher.web;

import com.manydesigns.portofino.dispatcher.DispatcherInitializer;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import java.io.File;

/**
 * Created by alessio on 28/07/16.
 */
public class WebDispatcherInitializer extends DispatcherInitializer implements ServletContextListener {

    public static final String PORTOFINO_APPLICATION_DIRECTORY_PARAMETER = "portofino.application.directory";
    public static final String CODE_BASE_ATTRIBUTE = "portofino.codebase";

    private static final Logger logger = LoggerFactory.getLogger(WebDispatcherInitializer.class);

    protected ServletContext servletContext;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        initWithServletContext(sce.getServletContext());
    }

    public void initWithServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
        initialize();
        servletContext.setAttribute(CODE_BASE_ATTRIBUTE, codeBase);
        servletContext.setAttribute(ApplicationRoot.PORTOFINO_CONFIGURATION_ATTRIBUTE, configuration);
    }

    @Override
    protected FileObject getDefaultApplicationRoot() throws FileSystemException {
        return VFS.getManager().resolveFile(servletContext.getRealPath("")).resolveFile("WEB-INF");
    }

    @Override
    protected String getApplicationDirectoryPath() {
        return servletContext.getInitParameter(PORTOFINO_APPLICATION_DIRECTORY_PARAMETER);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Application destroyed.");
    }

}
