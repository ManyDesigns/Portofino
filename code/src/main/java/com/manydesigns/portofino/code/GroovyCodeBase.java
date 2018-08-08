package com.manydesigns.portofino.code;

import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

import static com.manydesigns.portofino.code.JavaCodeBase.classNameToPath;

/**
 * Created by alessio on 28/03/17.
 */
public class GroovyCodeBase implements CodeBase {
    
    protected GroovyScriptEngine groovyScriptEngine;
    protected FileObject root;
    protected CodeBase parent;
    protected ClassLoader classLoader;
    protected static final Logger logger = LoggerFactory.getLogger(GroovyCodeBase.class);
    
    public GroovyCodeBase(FileObject root) throws IOException {
        this(root, null, null);
    }

    public GroovyCodeBase(FileObject root, CodeBase parent, ClassLoader classLoader) throws IOException {
        this.root = root;
        this.parent = parent;
        this.classLoader = classLoader;
        CompilerConfiguration cc = new CompilerConfiguration(CompilerConfiguration.DEFAULT);
        try {
            String classpath = Paths.get(root.getURL().toURI()).toFile().getAbsolutePath();
            cc.setClasspath(classpath);
        } catch (Exception e) {
            logger.debug("Could not set classpath", e);
        }
        cc.setRecompileGroovySource(true);
        groovyScriptEngine = new GroovyScriptEngine(new URL[] { root.getURL() }, getClassLoader());
        groovyScriptEngine.setConfig(cc);
        groovyScriptEngine.getGroovyClassLoader().setShouldRecompile(true);
    }

    public GroovyCodeBase(FileObject root, CodeBase parent) throws IOException {
        this(root, parent, parent != null ? parent.getClassLoader() : null);
    }
    
    @Override
    public Class loadClass(String className) throws IOException, ClassNotFoundException {
        String resourceName = classNameToPath(className);
        FileObject fileObject = root.resolveFile(resourceName + ".groovy");
        if(fileObject.exists()) {
            try {
                return loadGroovyFile(fileObject);
            } catch (IOException e) {
                throw e;
            } catch (Exception e) {
                throw new ClassNotFoundException(className, e);
            }
        }
        if(parent != null) {
            return parent.loadClass(className);
        }
        throw new ClassNotFoundException(className);
    }

    public Class loadGroovyFile(FileObject fileObject) throws FileSystemException, ResourceException, ScriptException {
        return groovyScriptEngine.loadScriptByName(fileObject.getURL().toString());
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader != null ? classLoader : Thread.currentThread().getContextClassLoader();
    }

    @Override
    public void close() {
        
    }
}
