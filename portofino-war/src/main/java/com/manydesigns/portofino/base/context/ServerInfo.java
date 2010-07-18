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

package com.manydesigns.portofino.base.context;

import com.manydesigns.portofino.logging.LogUtil;

import javax.servlet.ServletContext;
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

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    protected final Logger logger;

    protected final ServletContext servletContext;

    protected final String realPath;
    protected final String servletContextName;
    protected final String serverInfo;
    protected final int servletApiMajor;
    protected final int servletApiMinor;
    protected final String servletApiVersion;

    protected final String javaRuntimeName;
    protected final String javaRuntimeVersion;
    protected final String javaVmName;
    protected final String javaVmVersion;
    protected final String javaVmVendor;
    protected final String osName;
    protected final String userLanguage;
    protected final String userRegion;

    protected final Runtime runTime;

    public ServerInfo(ServletContext servletContext) {
        logger = Logger.getLogger(getClass().getName());

        this.servletContext = servletContext;

        realPath = servletContext.getRealPath("/");
        LogUtil.info(logger, "Real path: {0}", realPath);

        servletContextName = servletContext.getServletContextName();
        LogUtil.info(logger, "Servlet context name: {0}", servletContextName);

        serverInfo = servletContext.getServerInfo();
        LogUtil.info(logger, "Server info: {0}", serverInfo);

        servletApiMajor = servletContext.getMajorVersion();
        servletApiMinor = servletContext.getMinorVersion();
        servletApiVersion = MessageFormat.format("{0}.{1}",
                        servletApiMajor, servletApiMinor);
        LogUtil.info(logger, "Servlet API version: {0}", servletApiVersion);

        javaRuntimeName = System.getProperty("java.runtime.name");
        LogUtil.info(logger, "java.runtime.name: {0}", javaRuntimeName);

        javaRuntimeVersion = System.getProperty("java.runtime.version");
        LogUtil.info(logger, "java.runtime.version: {0}", javaRuntimeVersion);

        javaVmName = System.getProperty("java.vm.name");
        LogUtil.info(logger, "java.vm.name: {0}", javaVmName);

        javaVmVersion = System.getProperty("java.vm.version");
        LogUtil.info(logger, "java.vm.version: {0}", javaVmVersion);

        javaVmVendor = System.getProperty("java.vm.vendor");
        LogUtil.info(logger, "java.vm.vendor: {0}", javaVmVendor);

        osName = System.getProperty("os.name");
        LogUtil.info(logger, "os.name: {0}", osName);

        userLanguage = System.getProperty("user.language");
        LogUtil.info(logger, "user.language: {0}", userLanguage);

        userRegion = System.getProperty("user.region");
        LogUtil.info(logger, "user.region: {0}", userRegion);

        runTime = Runtime.getRuntime();        
    }


    public ServletContext getServletContext() {
        return servletContext;
    }

    public String getRealPath() {
        return realPath;
    }

    public String getServletContextName() {
        return servletContextName;
    }

    public String getServerInfo() {
        return serverInfo;
    }

    public int getServletApiMajor() {
        return servletApiMajor;
    }

    public int getServletApiMinor() {
        return servletApiMinor;
    }

    public String getServletApiVersion() {
        return servletApiVersion;
    }

    public String getJavaRuntimeName() {
        return javaRuntimeName;
    }

    public String getJavaRuntimeVersion() {
        return javaRuntimeVersion;
    }

    public String getJavaVmName() {
        return javaVmName;
    }

    public String getJavaVmVersion() {
        return javaVmVersion;
    }

    public String getJavaVmVendor() {
        return javaVmVendor;
    }

    public String getOsName() {
        return osName;
    }

    public String getUserLanguage() {
        return userLanguage;
    }

    public String getUserRegion() {
        return userRegion;
    }

    public long getFreeMemory() {
        return runTime.freeMemory();
    }

    public long getUsedMemory() {
        return getTotalMemory() - getFreeMemory();
    }

    public long getTotalMemory() {
        return runTime.totalMemory();
    }

    public long getMaxMemory() {
        return runTime.maxMemory();
    }

    public int getAvailableProcessors() {
        return runTime.availableProcessors();
    }

}
