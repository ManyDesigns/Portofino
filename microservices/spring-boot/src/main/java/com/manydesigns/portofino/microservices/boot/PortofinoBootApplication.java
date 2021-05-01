package com.manydesigns.portofino.microservices.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.ApplicationContextFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;

@SpringBootApplication(exclude = { DispatcherServletAutoConfiguration.class, ErrorMvcAutoConfiguration.class})
public class PortofinoBootApplication implements ApplicationContextAware, ServletContextAware {
	public static final Logger logger = LoggerFactory.getLogger(PortofinoBootApplication.class);

	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(PortofinoBootApplication.class);
		application.setApplicationContextFactory(webApplicationType -> {
			switch (webApplicationType) {
				case SERVLET:
					return new PortofinoAnnotationConfigServletWebServerApplicationContext();
				case REACTIVE:
					return new AnnotationConfigServletWebServerApplicationContext();
				default:
					return ApplicationContextFactory.DEFAULT.create(webApplicationType);
			}
		});
		application.run(args);
	}

	@Bean
	public String foo() {
		System.out.println("Foo!");
		return "foo";
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		System.out.println(applicationContext);
	}

	@Override
	public void setServletContext(ServletContext servletContext) {
		System.out.println(servletContext);
	}

}
