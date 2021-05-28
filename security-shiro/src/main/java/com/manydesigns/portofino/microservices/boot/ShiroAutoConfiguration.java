package com.manydesigns.portofino.microservices.boot;

import com.manydesigns.portofino.shiro.SelfRegisteringShiroFilter;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.DispatcherType;

@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@AutoConfigureAfter(PortofinoDispatcherAutoConfiguration.class)
public class ShiroAutoConfiguration {

    @Bean
    public FilterRegistrationBean<SelfRegisteringShiroFilter> getShiroFilter() {
        FilterRegistrationBean<SelfRegisteringShiroFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new SelfRegisteringShiroFilter());
        bean.addUrlPatterns("/*");
        bean.setDispatcherTypes(DispatcherType.REQUEST);
        return bean;
    }
}
