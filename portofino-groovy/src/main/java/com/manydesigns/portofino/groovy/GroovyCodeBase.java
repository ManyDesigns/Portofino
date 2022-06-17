/*
 * Copyright (C) 2005-2021 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.manydesigns.portofino.groovy;

import com.manydesigns.portofino.code.AbstractCodeBase;
import com.manydesigns.portofino.code.CodeBase;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

import static com.manydesigns.portofino.code.JavaCodeBase.classNameToPath;

/**
 * CodeBase that knows how to load Groovy classes.
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
