package com.manydesigns.portofino.microservices.boot;

import com.manydesigns.elements.servlet.ElementsFilter;
import com.manydesigns.portofino.jersey.PortofinoApplication;
import org.glassfish.jersey.servlet.ServletContainer;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.DispatcherType;

@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(DispatcherServlet.class)
@AutoConfigureAfter(ServletWebServerFactoryAutoConfiguration.class)
public class PortofinoDispatcherAutoConfiguration {

    //TODO conditionally use Jersey or RestEasy
    @Bean
    public ServletRegistrationBean<?> getJerseyApplication() {
        ServletRegistrationBean<ServletContainer> bean = new ServletRegistrationBean<>();
        bean.setServlet(new ServletContainer(new PortofinoApplication()));
        bean.addUrlMappings("/*");
        bean.setLoadOnStartup(1);
        return bean;
    }

    @Bean
    public FilterRegistrationBean<ElementsFilter> getElementsFilter() {
        FilterRegistrationBean<ElementsFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new ElementsFilter());
        bean.addUrlPatterns("/*");
        bean.setDispatcherTypes(DispatcherType.REQUEST);
        return bean;
    }
}
