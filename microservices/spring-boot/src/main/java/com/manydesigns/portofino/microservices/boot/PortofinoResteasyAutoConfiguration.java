/*
 * Copyright (C) 2005-2022 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.microservices.boot;

import com.manydesigns.portofino.resteasy.PortofinoApplication;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(value = PortofinoApplication.class)
@ConditionalOnProperty(name = ConfigProperties.DISPATCHER_ENABLED)
@AutoConfigureAfter(ElementsAutoConfiguration.class)
public class PortofinoResteasyAutoConfiguration {

    private String basePath;
    private static final Logger logger = LoggerFactory.getLogger(PortofinoJerseyAutoConfiguration.class);

    @Bean
    public ServletRegistrationBean<?> getResteasyApplication() {
        ServletRegistrationBean<HttpServletDispatcher> bean = new ServletRegistrationBean<>();
        bean.setServlet(new HttpServletDispatcher());
        bean.addUrlMappings(basePath + "*");
        bean.addInitParameter("javax.ws.rs.Application", PortofinoApplication.class.getName());
        bean.setLoadOnStartup(1);
        return bean;
    }

    @Value("${spring.resteasy.application-path:/api/}")
    public void setBasePath(String basePath) {
        if (!basePath.startsWith("/")) {
            basePath = "/" + basePath;
        }
        while (basePath.endsWith("/")) {
            basePath = basePath.substring(0, basePath.length() - 1);
        }
        basePath = basePath + "/";
        this.basePath = basePath;
    }

    @PostConstruct
    public void announceApiPath() {
        logger.info("API path: " + basePath);
    }

}
