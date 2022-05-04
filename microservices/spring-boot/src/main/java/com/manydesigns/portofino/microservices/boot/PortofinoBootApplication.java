package com.manydesigns.portofino.microservices.boot;

import org.apache.commons.vfs2.*;
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
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.Properties;

@SpringBootApplication(exclude = { ErrorMvcAutoConfiguration.class, GroovyTemplateAutoConfiguration.class })
public class PortofinoBootApplication {
	private static final Logger logger = LoggerFactory.getLogger(PortofinoBootApplication.class);

	public static void main(String[] args) throws Exception {
		run(PortofinoBootApplication.class, args);
	}

	public static ConfigurableApplicationContext run(Class<?> applicationClass, String... args) throws FileSystemException {
		installCommonsVfsBootSupport();
		SpringApplication application = new SpringApplication(applicationClass);
		ApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
		FileObject appDir;
		try {
			appDir = getApplicationDirectory(applicationArguments);
		} catch (IOException e) {
			logger.error("Could not create application", e);
			System.exit(1);
			return null;
		}
		if (applicationArguments.containsOption("dump-application-to")) {
			List<String> values = applicationArguments.getOptionValues("dump-application-to");
			FileSystemManager manager = VFS.getManager();
			values.forEach(v -> {
				try {
					dumpBundledApplication(manager.resolveFile(v));
				} catch (FileSystemException e) {
					logger.error("Could not dump the bundled application to " + v, e);
				}
			});
		}
		application.setApplicationContextFactory(webApplicationType -> {
			switch (webApplicationType) {
				case SERVLET:
					return new PortofinoAnnotationConfigServletWebServerApplicationContext(appDir);
				case REACTIVE:
					return new AnnotationConfigServletWebServerApplicationContext();
				default:
					return ApplicationContextFactory.DEFAULT.create(webApplicationType);
			}
		});
		Properties defaultProperties = new Properties();
		defaultProperties.put("spring.jersey.application-path", "/api/");
		defaultProperties.put("spring.resteasy.application-path", "/api/");
		application.setDefaultProperties(defaultProperties);
		return application.run(args);
	}

	public static void installCommonsVfsBootSupport() throws FileSystemException {
		((DefaultFileSystemManager) VFS.getManager()).removeProvider("res");
		((DefaultFileSystemManager) VFS.getManager()).addProvider("res", new SpringBootResourceFileProvider());
	}

	public static FileObject getApplicationDirectory(ApplicationArguments applicationArguments) throws IOException {
		((DefaultFileSystemManager) VFS.getManager()).setBaseFile(new File(""));
		if(applicationArguments.containsOption("app-dir")) {
			FileObject applicationDirectory = VFS.getManager().resolveFile(applicationArguments.getOptionValues("app-dir").get(0));
				initAppDirectory(applicationDirectory);
			return applicationDirectory;
		}
		if(applicationArguments.containsOption("dev")) {
			FileObject main = VFS.getManager().resolveFile("src").resolveFile("main");
			if (main.isFolder()) {
				FileObject javaDir = main.resolveFile("java").resolveFile("portofino").resolveFile("actions");
				if(!javaDir.isFolder()) {
					// This method does not work with Java actions since the compiled version is in the build directory
					FileObject appDir = main.resolveFile("resources").resolveFile("portofino");
					if (appDir.isFolder()) {
						return appDir;
					}
					appDir = main.resolveFile("webapp").resolveFile("WEB-INF");
					if (appDir.isFolder()) {
						return appDir;
					}
				}
			}
		}
		if(new File("portofino.properties").isFile()) {
			return VFS.getManager().toFileObject(new File(""));
		}
		if(new File("portofino-application").isDirectory()) {
			return VFS.getManager().toFileObject(new File("portofino-application"));
		}
		FileObject bundledApplication = VFS.getManager().resolveFile("res:portofino");
		if (bundledApplication.isFolder() && bundledApplication.getChildren().length > 0) {
			return bundledApplication;
		}
		FileObject applicationDirectory = VFS.getManager().resolveFile("portofino-application");
		initAppDirectory(applicationDirectory);
		return applicationDirectory;
	}

	protected static void initAppDirectory(FileObject applicationDirectory) throws IOException {
		if(!applicationDirectory.exists()) {
			applicationDirectory.createFolder();
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

	private static void dumpBundledApplication(FileObject applicationDirectory) throws FileSystemException {
		if(PortofinoBootApplication.class.getClassLoader().getResource("portofino") != null) try {
			FileObject bundledApplication = VFS.getManager().resolveFile("res:portofino");
			if(bundledApplication.isFolder()) {
				logger.info("Dumping bundled application to " + applicationDirectory.getName().getPath());
				applicationDirectory.copyFrom(bundledApplication, new AllFileSelector());
			} else {
				logger.warn("No bundled application to dump");
			}
		} catch (IOException e) {
			applicationDirectory.deleteAll();
			throw e;
		}
	}

}
