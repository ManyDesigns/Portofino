/*
 * Copyright (C) 2005-2010 ManyDesigns srl.  All rights reserved.
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
import com.manydesigns.elements.logging.LogUtil;

import javax.servlet.ServletContext;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.logging.Logger;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class ServerInfo {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    public static final Logger logger =
            LogUtil.getLogger(ServerInfo.class);

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
        LogUtil.finerMF(logger, "Real path: {0}", realPath);

        servletContextName = servletContext.getServletContextName();
        LogUtil.finerMF(logger, "Servlet context name: {0}", servletContextName);

        serverInfo = servletContext.getServerInfo();
        LogUtil.finerMF(logger, "Server info: {0}", serverInfo);

        servletApiMajor = servletContext.getMajorVersion();
        servletApiMinor = servletContext.getMinorVersion();
        servletApiVersion = MessageFormat.format("{0}.{1}",
                        servletApiMajor, servletApiMinor);
        LogUtil.finerMF(logger, "Servlet API version: {0}", servletApiVersion);

        String tmp = null;
        if (servletApiMajor >= 2 && servletApiMinor >= 5) {
            try {
                Method method =
                        servletContext.getClass().getMethod("getContextPath");
                tmp = (String)method.invoke(servletContext);
            } catch (Throwable e) {
                LogUtil.severe(logger, "Uncaught exception", e);
            }
        }
        contextPath = tmp;
        LogUtil.finerMF(logger, "Context path: {0}", contextPath);

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
