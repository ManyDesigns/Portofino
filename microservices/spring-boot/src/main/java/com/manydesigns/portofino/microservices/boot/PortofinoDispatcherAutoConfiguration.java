package com.manydesigns.portofino.microservices.boot;

import com.manydesigns.elements.servlet.ElementsFilter;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import javax.servlet.DispatcherType;

@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@AutoConfigureAfter(ServletWebServerFactoryAutoConfiguration.class)
public class PortofinoDispatcherAutoConfiguration {

    @Bean
    public FilterRegistrationBean<ElementsFilter> getElementsFilter() {
        FilterRegistrationBean<ElementsFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new ElementsFilter());
        bean.addUrlPatterns("/*");
        bean.setDispatcherTypes(DispatcherType.REQUEST);
        return bean;
    }

}
