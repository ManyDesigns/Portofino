/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.manydesigns.portofino.scripting;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.ognl.OgnlUtils;
import com.manydesigns.elements.util.RandomUtil;
import com.manydesigns.portofino.application.Application;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyShell;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import ognl.OgnlContext;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class ScriptingUtil {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    private static final Logger logger =
            LoggerFactory.getLogger(ScriptingUtil.class);

    public static final String GROOVY_FILE_NAME_PATTERN = "{0}.groovy";

    public static Object runScript(String script,
                                   String scriptLanguage,
                                   Object root)
            throws Exception {
        OgnlContext ognlContext = ElementsThreadLocals.getOgnlContext();
        if ("ognl".equals(scriptLanguage)) {
            return OgnlUtils.getValueQuietly(script, ognlContext, root);
        } else if ("groovy".equals(scriptLanguage)) {
            ognlContext.put("root", root);
            Binding binding = new Binding(ognlContext);
            GroovyShell shell = new GroovyShell(binding);
            return shell.evaluate(script);
        } else {
            String msg = String.format(
                    "Unrecognised script language: %s", scriptLanguage);
            throw new IllegalArgumentException(msg);
        }
    }

    public static GroovyObject getGroovyObject(File storageDir, String pageId)
            throws IOException, ScriptException, ResourceException {
        File file = getGroovyScriptFile(storageDir, pageId);
        return getGroovyObject(file);
    }

    public static GroovyObject getGroovyObject(File file) throws IOException, ScriptException, ResourceException {
        if(!file.exists()) {
            return null;
        }

        Class groovyClass = getGroovyClass(file);

        try {
            return (GroovyObject) groovyClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static File getGroovyScriptFile(File storageDir, String pageId) {
        return RandomUtil.getCodeFile(storageDir, GROOVY_FILE_NAME_PATTERN, pageId);
    }

    public static GroovyScriptEngine GROOVY_SCRIPT_ENGINE =
            new GroovyScriptEngine(new URL[0], ScriptingUtil.class.getClassLoader());
            //makeFallbackClassLoader();

    public static Class<?> getGroovyClass(File scriptFile) throws IOException, ScriptException, ResourceException {
        if(!scriptFile.exists()) {
            return null;
        }
        return GROOVY_SCRIPT_ENGINE.loadScriptByName(scriptFile.toURI().toString());
    }

    /*public static Class<?> getGroovyClass(String script, File scriptFile) {
        //String path = scriptFile.getAbsolutePath();
        return GROOVY_SCRIPT_ENGINE.loadScriptByName(scriptFile.toURI().toString());
    }*/

    //TODO mappa application -> classloader (multi-app)
    public static void initBaseClassLoader(Application application) {
        CompilerConfiguration cc = new CompilerConfiguration(CompilerConfiguration.DEFAULT);
        File classpathFile = new File(application.getAppScriptsDir(), "common");
        String classpath = classpathFile.getAbsolutePath();
        logger.info("Base classpath for application " + application.getAppId() + " is " + classpath);
        cc.setClasspath(classpath);
        cc.setRecompileGroovySource(true);
        GroovyClassLoader gcl = new GroovyClassLoader(ScriptingUtil.class.getClassLoader(), cc);
        GroovyClassLoader defaultClassLoader = new GroovyClassLoader(gcl);
        defaultClassLoader.setShouldRecompile(true);
        try {
            GROOVY_SCRIPT_ENGINE =
                    new GroovyScriptEngine(new URL[] { classpathFile.toURI().toURL() },
                                           ScriptingUtil.class.getClassLoader());
        } catch (IOException e) {
            throw new Error(e);
        }
        GROOVY_SCRIPT_ENGINE.setConfig(cc);
    }

    public static void removeBaseClassLoader(Application application) {
        GROOVY_SCRIPT_ENGINE = new GroovyScriptEngine(new URL[0], ScriptingUtil.class.getClassLoader());
    }
}
