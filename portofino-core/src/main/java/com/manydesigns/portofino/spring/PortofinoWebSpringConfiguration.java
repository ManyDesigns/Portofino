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

package com.manydesigns.portofino.spring;

import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.config.ConfigurationSource;
import com.manydesigns.portofino.i18n.I18nUtils;
import com.manydesigns.portofino.modules.Module;
import com.manydesigns.portofino.servlets.ServerInfo;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.vfs2.FileObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

import javax.servlet.ServletContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@org.springframework.context.annotation.Configuration
public class PortofinoWebSpringConfiguration implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(PortofinoWebSpringConfiguration.class);

    protected ServletContext servletContext;
    protected FileObject applicationDirectory;
    protected Configuration configuration;
    protected static final String SEPARATOR =
            "----------------------------------------" +
            "----------------------------------------";


    @Bean
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Autowired
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Autowired
    @Qualifier(PortofinoSpringConfiguration.APPLICATION_DIRECTORY)
    public void setApplicationDirectory(FileObject applicationDirectory) {
        this.applicationDirectory = applicationDirectory;
    }

    @Autowired
    public void setConfiguration(ConfigurationSource configuration) {
        this.configuration = configuration.getProperties();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        I18nUtils.setupResourceBundleManager(applicationDirectory, servletContext);
        String encoding = configuration.getString(
                PortofinoProperties.URL_ENCODING, PortofinoProperties.URL_ENCODING_DEFAULT);
        logger.info("URL character encoding is set to " + encoding + ". Make sure the web server uses the same encoding to parse URLs.");
        if(!Charset.isSupported(encoding)) {
            logger.error("The encoding is not supported by the JVM!");
        }

        String versionCheckUrl = configuration.getString(
                "portofino.version.check.url",
                "https://portofino.manydesigns.com/version-check.jsp");
        if(!"off".equalsIgnoreCase(versionCheckUrl)) {
            String portofinoVersion = Module.getPortofinoVersion();
            try {
                checkForNewVersion(portofinoVersion, versionCheckUrl);
            } catch (Throwable t) {
                logger.warn("Version check failed unexpectedly", t);
            }
        }

        String portofinoVersion = Module.getPortofinoVersion();
        String lineSeparator = System.getProperty("line.separator", "\n");
        ServerInfo serverInfo = new ServerInfo(servletContext);
        logger.info(lineSeparator + SEPARATOR +
                        lineSeparator + "--- ManyDesigns Portofino " + portofinoVersion + " started successfully" +
                        lineSeparator + "--- Context path: {}" +
                        lineSeparator + "--- Real path: {}" +
                        lineSeparator + "--- Visit https://portofino.manydesigns.com for news, documentation, issue tracker, community forums, commercial support!" +
                        lineSeparator + SEPARATOR,
                serverInfo.getContextPath(), serverInfo.getRealPath());
    }

    protected void checkForNewVersion(String portofinoVersion, String versionCheckUrl) {
        String SEPARATOR = "--------------------------------------------------------------------------------";
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(versionCheckUrl).queryParam("version", portofinoVersion);
        Future<Response> responseFuture = target.request().async().get();
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                Response response = responseFuture.get();
                if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
                    String latestVersion = response.readEntity(String.class).trim();
                    if (Objects.equals(portofinoVersion, latestVersion)) {
                        logger.info("Your installation of Portofino is up-to-date");
                    } else {
                        String lineSeparator = System.getProperty("line.separator", "\n");
                        logger.info(lineSeparator + SEPARATOR + lineSeparator +
                                "A new version of Portofino is available: " + latestVersion +
                                lineSeparator + SEPARATOR);
                    }
                } else {
                    logger.info("Version check failed: " + response.getStatus());
                }
                String message = response.getHeaderString("X-Message");
                if (message != null) {
                    logger.info(message);
                }
            } catch (Exception e) {
                logger.info("Could not check for new version: " + e.getMessage());
                logger.debug("Additional information", e);
            }
        });
    }
}
