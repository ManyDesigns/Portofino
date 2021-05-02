package com.manydesigns.portofino.microservices.boot;

import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
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

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;

@SpringBootApplication(exclude = {
		DispatcherServletAutoConfiguration.class, ErrorMvcAutoConfiguration.class,
		GroovyTemplateAutoConfiguration.class })
public class PortofinoBootApplication {
	public static final Logger logger = LoggerFactory.getLogger(PortofinoBootApplication.class);

	public static void main(String[] args) throws Exception {
		installCommonsVfsBootSupport();

		SpringApplication application = new SpringApplication(PortofinoBootApplication.class);
		ApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
		application.setApplicationContextFactory(webApplicationType -> {
			switch (webApplicationType) {
				case SERVLET:
					try {
						return new PortofinoAnnotationConfigServletWebServerApplicationContext(getApplicationDirectoryPath(applicationArguments));
					} catch (IOException e) {
						logger.error("Could not create application", e);
						System.exit(1);
						return null;
					}
				case REACTIVE:
					return new AnnotationConfigServletWebServerApplicationContext();
				default:
					return ApplicationContextFactory.DEFAULT.create(webApplicationType);
			}
		});
		application.run(args);
	}

	public static void installCommonsVfsBootSupport() throws FileSystemException {
		((DefaultFileSystemManager) VFS.getManager()).removeProvider("res");
		((DefaultFileSystemManager) VFS.getManager()).addProvider("res", new SpringBootResourceFileProvider());
	}

	public static String getApplicationDirectoryPath(ApplicationArguments applicationArguments) throws IOException {
		((DefaultFileSystemManager) VFS.getManager()).setBaseFile(new File(""));
		if(applicationArguments.containsOption("app-dir")) {
			FileObject applicationDirectory = VFS.getManager().resolveFile(applicationArguments.getOptionValues("app-dir").get(0));
				initAppDirectory(applicationDirectory);
			return applicationDirectory.getName().getURI();
		}
		FileObject main = VFS.getManager().resolveFile("src").resolveFile("main");
		if(main.isFolder()) {
			FileObject appDir = main.resolveFile( "resources").resolveFile("portofino-application");
			if(appDir.isFolder()) {
				return appDir.getName().getURI();
			}
			appDir = main.resolveFile("webapp").resolveFile("WEB-INF");
			if(appDir.isFolder()) {
				return appDir.getName().getURI();
			}
		}
		if(new File("portofino.properties").isFile()) {
			return new File("").getAbsolutePath();
		}
		if(new File("portofino-application").isDirectory()) {
			return new File("portofino-application").getAbsolutePath();
		}
		FileObject applicationDirectory = VFS.getManager().resolveFile("portofino-application");
		initAppDirectory(applicationDirectory);
		return applicationDirectory.getName().getURI();
	}

	protected static void initAppDirectory(FileObject applicationDirectory) throws IOException {
		if(!applicationDirectory.exists()) {
			applicationDirectory.createFolder();
			if(PortofinoBootApplication.class.getClassLoader().getResource("portofino-application") != null) {
				FileObject bundledApplication = VFS.getManager().resolveFile("res:portofino-application");
				applicationDirectory.copyFrom(bundledApplication, new AllFileSelector());
			}
		}

		FileObject actions = applicationDirectory.resolveFile("actions");
		actions.createFolder();

		FileObject actionXml = actions.resolveFile("action.xml");
		if(!actionXml.exists()) {
			actionXml.createFile();
			try (Writer w = new OutputStreamWriter(actionXml.getContent().getOutputStream())) {
				w.write("<action><permissions /></action>");
			}
		}
		applicationDirectory.resolveFile("portofino.properties").createFile();
	}

}
