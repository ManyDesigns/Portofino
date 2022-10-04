package com.manydesigns.portofino.microservices.launcher;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class WarFileLauncher {

    public static void main (String[] args) throws URISyntaxException, IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        URI uri = WarFileLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        String pathFromUri;
        if(uri.getScheme().equals("jar")) {
            pathFromUri = new URI(uri.getSchemeSpecificPart()).getSchemeSpecificPart();
            int index = pathFromUri.indexOf("!");
            if(index >= 0) {
                pathFromUri = pathFromUri.substring(0, index);
            }
        } else {
            pathFromUri = uri.toString();
        }
        File warFile = new File(pathFromUri);
        String warPath = warFile.getAbsolutePath();
        if(!warFile.exists()) {
            System.err.println("Could not determine the path of the running war file: " + warPath);
            System.exit(1);
        }
        int port = 8080;
        File tempDir = createTempDir(port);
        try(JarFile jarFile = new JarFile(warFile)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            List<URL> tomcatLibs = new ArrayList<>();
            File tomcatLibDir = new File(tempDir, "tomcat-libs");
            tomcatLibDir.mkdir();
            while(entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if(entry.getName().startsWith("WEB-INF/lib-provided") && entry.getName().endsWith(".jar")) {
                    File outFile = new File(tomcatLibDir, entry.getName().substring("WEB-INF/lib-provided/".length()));
                    if (!outFile.toPath().normalize().startsWith(tomcatLibDir.toPath())) {
                        throw new IOException("Bad zip entry: " + entry.getName());
                    }
                    try(InputStream in = jarFile.getInputStream(entry);
                        OutputStream out = new FileOutputStream(outFile)) {
                        IOUtils.copy(in, out);
                    }
                    tomcatLibs.add(outFile.toURI().toURL());
                }
            }
            File pkgFile = tomcatLibDir;
            String packageName = TomcatLauncher.class.getPackage().getName();
            for(String pkg : packageName.split("\\.")) {
                pkgFile = new File(pkgFile, pkg);
            }
            pkgFile.mkdirs();
            File outFile = new File(pkgFile, "TomcatLauncher.class");
            try(InputStream in = TomcatLauncher.class.getResourceAsStream("TomcatLauncher.class");
                OutputStream out = new FileOutputStream(outFile)) {
                IOUtils.copy(in, out);
            }
            tomcatLibs.add(tomcatLibDir.toURI().toURL());
            URLClassLoader tomcatClassLoader = new URLClassLoader(tomcatLibs.toArray(new URL[0]));

            Class<?> launcherClass = tomcatClassLoader.loadClass(TomcatLauncher.class.getName());
            Method launcherMethod = launcherClass.getMethod("launch", File.class, String.class, Integer.TYPE);
            launcherMethod.invoke(null, tempDir, warPath, port);
        }
    }

    /**
     * Returns the absolute path to the temp directory.
     */
    public static File createTempDir(int port) throws IOException {
        File tempDir = Files.createTempDirectory("portofino.tomcat." + "." + port).toFile();
        tempDir.deleteOnExit();
        return tempDir;
    }

}
