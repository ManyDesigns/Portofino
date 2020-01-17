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
public class GroovyCodeBase extends AbstractCodeBase {
    
    protected GroovyScriptEngine groovyScriptEngine;
    private static final Logger logger = LoggerFactory.getLogger(GroovyCodeBase.class);
    
    public GroovyCodeBase(FileObject root) throws IOException {
        this(root, null, null);
    }

    public GroovyCodeBase(FileObject root, CodeBase parent, ClassLoader classLoader) throws IOException {
        super(root, parent, classLoader);
        this.root = root;
        this.classLoader = classLoader;
        resetGroovyScriptEngine();
    }

    public void resetGroovyScriptEngine() throws FileSystemException {
        CompilerConfiguration cc = new CompilerConfiguration(CompilerConfiguration.DEFAULT);
        try {
            String classpath = this.root.getName().getPath();
            cc.setClasspath(classpath);
        } catch (Exception e) {
            logger.debug("Could not set classpath", e);
        }
        cc.setRecompileGroovySource(true);
        groovyScriptEngine = new GroovyScriptEngine(new URL[] { this.root.getURL() }, parent != null ? parent.asClassLoader() : getClassLoader());
        groovyScriptEngine.setConfig(cc);
        groovyScriptEngine.getGroovyClassLoader().setShouldRecompile(Boolean.TRUE);
    }

    public GroovyCodeBase(FileObject root, CodeBase parent) throws IOException {
        this(root, parent, parent != null ? parent.getClassLoader() : null);
    }

    @Override
    protected Class loadLocalClass(String className) throws IOException, ClassNotFoundException {
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
        return null;
    }

    public Class loadGroovyFile(FileObject fileObject) throws FileSystemException, ResourceException, ScriptException {
        return groovyScriptEngine.loadScriptByName(fileObject.getURL().toString());
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader != null ? classLoader : Thread.currentThread().getContextClassLoader();
    }

    @Override
    public void clear(boolean recursively) throws Exception {
        super.clear(recursively);
        resetGroovyScriptEngine();
    }
}
