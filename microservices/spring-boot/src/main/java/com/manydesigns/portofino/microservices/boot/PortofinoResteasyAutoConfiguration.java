package com.manydesigns.portofino.microservices.boot;

import com.manydesigns.elements.servlet.ElementsFilter;
import com.manydesigns.portofino.resteasy.PortofinoApplication;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.servlet.DispatcherType;

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(value = PortofinoApplication.class)
@AutoConfigureAfter(PortofinoDispatcherAutoConfiguration.class)
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
