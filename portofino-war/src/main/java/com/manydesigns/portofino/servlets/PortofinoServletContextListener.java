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

package com.manydesigns.portofino.servlets;

import com.manydesigns.elements.ElementsProperties;
import com.manydesigns.elements.logging.LogUtil;
import com.manydesigns.elements.util.InstanceBuilder;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.context.MDContext;
import com.manydesigns.portofino.context.MDContextHibernateImpl;
import com.manydesigns.portofino.context.ServerInfo;
import org.apache.commons.lang.time.StopWatch;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Properties;
import java.util.logging.Logger;


/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class PortofinoServletContextListener implements ServletContextListener {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public static final String SEPARATOR =
            "----------------------------------------" +
            "----------------------------------------";

    public static final String PORTOFINO_VERSION = "4.0.0-SNAPSHOT";

    public static final String PORTOFINO_VERSION_ATTRIBUTE =
            "portofinoVersion";
    public static final String ELEMENTS_PROPERTIES_ATTRIBUTE =
            "elementsProperties";
    public static final String PORTOFINO_PROPERTIES_ATTRIBUTE =
            "portofinoProperties";
    public static final String SERVER_INFO_ATTRIBUTE =
            "serverInfo";
    public static final String MDCONTEXT_ATTRIBUTE =
            "mdContext";

    protected Properties elementsProperties;
    protected Properties portofinoProperties;
    protected ServletContext servletContext;
    protected ServerInfo serverInfo;
    protected MDContext mdContext;

    protected final Logger logger =
            LogUtil.getLogger(PortofinoServletContextListener.class);

    /**
     * Creates a new instance of PortofinoServletContextListener
     */
    public PortofinoServletContextListener() {

    }

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        servletContext = servletContextEvent.getServletContext();
        serverInfo = new ServerInfo(servletContext);
        
        LogUtil.infoMF(logger, "\n" + SEPARATOR +
                "\n--- ManyDesigns Portofino {0} starting..." +
                "\n--- Context path: {1}" +
                "\n--- Real path: {2}" +
                "\n" + SEPARATOR,
                PORTOFINO_VERSION,
                serverInfo.getContextPath(),
                serverInfo.getRealPath()
        );

        servletContext.setAttribute(PORTOFINO_VERSION_ATTRIBUTE,
                PORTOFINO_VERSION);
        servletContext.setAttribute(SERVER_INFO_ATTRIBUTE,
                serverInfo);

        elementsProperties = ElementsProperties.getProperties();
        portofinoProperties = PortofinoProperties.getProperties();

        servletContext.setAttribute(ELEMENTS_PROPERTIES_ATTRIBUTE,
                elementsProperties);
        servletContext.setAttribute(PORTOFINO_PROPERTIES_ATTRIBUTE,
                portofinoProperties);

        boolean success = true;

        // check servlet API version
        if (serverInfo.getServletApiMajor() < 2 ||
                (serverInfo.getServletApiMajor() == 2 &&
                        serverInfo.getServletApiMinor() < 3)) {
            LogUtil.severeMF(logger,
                    "Servlet API version must be >= 2.3. Found: {0}.",
                    serverInfo.getServletApiVersion());
            success = false;
        }

        if (success) {
            createMDContext();
        }

        stopWatch.stop();
        if (success) {
            LogUtil.infoMF(logger,
                    "ManyDesigns Portofino successfully started in {0} ms.",
                    stopWatch.getTime());
        } else {
            logger.severe("Failed to start ManyDesigns Portofino.");
        }
    }

    private void createMDContext() {
        logger.info("Creating MDContext and " +
                "registering on servlet context...");
        // create and register the container first, without exceptions

        String managerClassName =
                portofinoProperties.getProperty(
                        PortofinoProperties.CONTEXT_CLASS_PROPERTY);
        InstanceBuilder<MDContext> builder =
                new InstanceBuilder<MDContext>(
                        MDContext.class,
                        MDContextHibernateImpl.class,
                        logger);
        mdContext = builder.createInstance(managerClassName);

        mdContext.loadXmlModelAsResource(
                    portofinoProperties.getProperty(
                            PortofinoProperties.MODEL_LOCATION_PROPERTY));
        servletContext.setAttribute(MDCONTEXT_ATTRIBUTE, mdContext);
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        logger.info("ManyDesigns Portofino stopping...");

        logger.info("Unregistering MDContext from servlet context...");
        servletContext.removeAttribute(MDCONTEXT_ATTRIBUTE);

        servletContext.removeAttribute(SERVER_INFO_ATTRIBUTE);
        servletContext.removeAttribute(PORTOFINO_VERSION_ATTRIBUTE);
        servletContext.removeAttribute(PORTOFINO_PROPERTIES_ATTRIBUTE);
        servletContext.removeAttribute(ELEMENTS_PROPERTIES_ATTRIBUTE);

        logger.info("ManyDesigns Portofino stopped.");
    }

}
