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

package com.manydesigns.portofino.context;

import com.manydesigns.elements.annotations.Label;
import com.manydesigns.elements.annotations.Memory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.io.File;
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
            "Copyright (c) 2005-2011, ManyDesigns srl";

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

    public File getWebAppFile(String filename) {
        File modelFile = new File(filename);
        if(modelFile.isAbsolute()) {
            return modelFile;
        } else if (realPath == null) {
            return modelFile;
        } else {
            return new File (realPath, filename);
        }
    }
}
