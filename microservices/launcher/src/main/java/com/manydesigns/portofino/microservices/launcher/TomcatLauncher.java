package com.manydesigns.portofino.microservices.launcher;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

import java.io.File;

public class TomcatLauncher {

    public static void launch(File tempDir, String warPath, int port) throws LifecycleException {
        Tomcat tomcat = new Tomcat();
        //Make sure we use this class' classloader, as it won't include the libraries of the application
        //packaged in the war file.
        tomcat.getServer().setParentClassLoader(TomcatLauncher.class.getClassLoader());
        tomcat.setBaseDir(tempDir.getAbsolutePath());
        File appBase = new File(tempDir, "appBase");
        appBase.mkdir();
        tomcat.getHost().setAppBase(appBase.getAbsolutePath());
        tomcat.setPort(port);
        tomcat.addWebapp("", warPath);
        //"The default connector will only be created if getConnector is called."
        tomcat.getConnector();
        tomcat.start();
        tomcat.getServer().await();
    }

}
