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

package com.manydesigns.portofino.servlets;

import com.manydesigns.elements.annotations.Label;
import com.manydesigns.elements.annotations.Memory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.lang.reflect.Method;
import java.text.MessageFormat;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class ServerInfo {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(ServerInfo.class);

    protected final ServletContext servletContext;

    protected final String realPath;
    protected final String contextPath;
    protected final String servletContextName;
    protected final String serverInfo;
    protected final int servletApiMajor;
    protected final int servletApiMinor;
    protected final String servletApiVersion;

    protected final Runtime runTime;

    public ServerInfo(ServletContext servletContext) {
        this.servletContext = servletContext;

        realPath = servletContext.getRealPath("/");
        logger.debug("Real path: {}", realPath);

        servletContextName = servletContext.getServletContextName();
        logger.debug("Servlet context name: {}", servletContextName);

        serverInfo = servletContext.getServerInfo();
        logger.debug("Server info: {}", serverInfo);

        servletApiMajor = servletContext.getMajorVersion();
        servletApiMinor = servletContext.getMinorVersion();
        servletApiVersion = MessageFormat.format("{0}.{1}",
                        servletApiMajor, servletApiMinor);
        logger.debug("Servlet API version: {}", servletApiVersion);

        String tmp = null;
        try {
            Method method =
                    servletContext.getClass().getMethod("getContextPath");
            tmp = (String)method.invoke(servletContext);
        } catch (NoSuchMethodException e) {
            logger.debug("Cannot invoke getContextPath(). Required Servlet API >= 2.5");
        } catch (Exception e) {
            logger.debug("Uncaught exception", e);
        }
        contextPath = tmp;
        logger.debug("Context path: {}", contextPath);

        runTime = Runtime.getRuntime();
    }


    public ServletContext getServletContext() {
        return servletContext;
    }

    public String getRealPath() {
        return realPath;
    }

    public String getContextPath() {
        return contextPath;
    }

    public String getServletContextName() {
        return servletContextName;
    }

    public String getServerInfo() {
        return serverInfo;
    }

    @Label("servlet API major")
    public int getServletApiMajor() {
        return servletApiMajor;
    }

    @Label("servlet API minor")
    public int getServletApiMinor() {
        return servletApiMinor;
    }

    @Label("servlet API version")
    public String getServletApiVersion() {
        return servletApiVersion;
    }

    @Memory
    public long getFreeMemory() {
        return runTime.freeMemory();
    }

    @Memory
    public long getUsedMemory() {
        return getTotalMemory() - getFreeMemory();
    }

    @Memory
    public long getTotalMemory() {
        return runTime.totalMemory();
    }

    @Memory
    public long getMaxMemory() {
        return runTime.maxMemory();
    }

    public int getAvailableProcessors() {
        return runTime.availableProcessors();
    }

}
