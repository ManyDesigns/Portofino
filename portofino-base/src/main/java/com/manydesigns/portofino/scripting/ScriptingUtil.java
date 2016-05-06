/*
 * Copyright (C) 2005-2015 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.scripting;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.util.RandomUtil;
import com.manydesigns.portofino.modules.BaseModule;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
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
            "Copyright (c) 2005-2015, ManyDesigns srl";

    public static final Logger logger =
            LoggerFactory.getLogger(ScriptingUtil.class);

    public static final String GROOVY_FILE_NAME_PATTERN = "{0}.groovy";

    public static File getGroovyScriptFile(File storageDir, String pageId) {
        return RandomUtil.getCodeFile(storageDir, GROOVY_FILE_NAME_PATTERN, pageId);
    }

    public static Class<?> getGroovyClass(File scriptFile) throws IOException, ScriptException, ResourceException {
        if(!scriptFile.exists()) {
            return null;
        }
        GroovyScriptEngine scriptEngine =
                (GroovyScriptEngine) ElementsThreadLocals.getServletContext().getAttribute(
                        BaseModule.GROOVY_SCRIPT_ENGINE);
        return scriptEngine.loadScriptByName(scriptFile.toURI().toString());
    }

    public static GroovyScriptEngine createScriptEngine(File classpathFile, ClassLoader parent) {
        CompilerConfiguration cc = new CompilerConfiguration(CompilerConfiguration.DEFAULT);
        String classpath = classpathFile.getAbsolutePath();
        cc.setClasspath(classpath);
        cc.setRecompileGroovySource(true);
        GroovyScriptEngine scriptEngine;
        try {
            scriptEngine =
                    new GroovyScriptEngine(new URL[] { classpathFile.toURI().toURL() }, parent);
        } catch (IOException e) {
            throw new Error(e);
        }
        scriptEngine.setConfig(cc);
        scriptEngine.getGroovyClassLoader().setShouldRecompile(true);
        return scriptEngine;
    }
}
