package com.manydesigns.portofino.microservices.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationContextFactory;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.groovy.template.GroovyTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;

@SpringBootApplication(exclude = {
		DispatcherServletAutoConfiguration.class, ErrorMvcAutoConfiguration.class,
		GroovyTemplateAutoConfiguration.class })
public class PortofinoBootApplication {
	public static final Logger logger = LoggerFactory.getLogger(PortofinoBootApplication.class);

	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(PortofinoBootApplication.class);
		ApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
		application.setApplicationContextFactory(webApplicationType -> {
			switch (webApplicationType) {
				case SERVLET:
					return new PortofinoAnnotationConfigServletWebServerApplicationContext(getApplicationDirectoryPath(applicationArguments));
				case REACTIVE:
					return new AnnotationConfigServletWebServerApplicationContext();
				default:
					return ApplicationContextFactory.DEFAULT.create(webApplicationType);
			}
		});
		application.run(args);
	}

	public static String getApplicationDirectoryPath(ApplicationArguments applicationArguments) {
		if(applicationArguments.containsOption("app-dir")) {
			return applicationArguments.getOptionValues("app-dir").get(0);
		}
		File main = new File(new File("src"), "main");
		if(main.isDirectory()) {
			File appDir = new File(main, "portofino");
			if(appDir.isDirectory()) {
				return appDir.getAbsolutePath();
			}
			appDir = new File(new File(main, "webapp"), "WEB-INF");
			if(appDir.isDirectory()) {
				return appDir.getAbsolutePath();
			}
		}
		if(new File("portofino.properties").isFile()) {
			return new File("").getAbsolutePath();
		}
		try {
			File tempDirectory = Files.createTempDirectory("portofino").toFile();
			File actions = new File(tempDirectory, "actions");
			actions.mkdirs();
			try(FileWriter fw = new FileWriter(new File(actions, "action.xml"))) {
				fw.write("<action><permissions /></action>");
			}
			new File(tempDirectory, "portofino.properties").createNewFile();
			return tempDirectory.getAbsolutePath();
		} catch (IOException e) {
			logger.error("Could not create application directory", e);
			System.exit(1);
			return null;
		}
	}

}
