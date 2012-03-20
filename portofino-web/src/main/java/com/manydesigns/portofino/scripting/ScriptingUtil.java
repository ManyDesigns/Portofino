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
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyShell;
import ognl.OgnlContext;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class ScriptingUtil {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

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

    public static GroovyObject getGroovyObject(File storageDir, String pageId) throws IOException {
        File file = getGroovyScriptFile(storageDir, pageId);
        return getGroovyObject(file);
    }

    public static GroovyObject getGroovyObject(File file) throws IOException {
        ClassLoader parent = ScriptingUtil.class.getClassLoader();
        GroovyClassLoader loader = new GroovyClassLoader(parent);
        if(!file.exists()) {
            return null;
        }

        Class groovyClass = loader.parseClass(file);

        try {
            return (GroovyObject) groovyClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static GroovyObject getGroovyObject(String text, String fileName) {
        ClassLoader parent = ScriptingUtil.class.getClassLoader();
        GroovyClassLoader loader = new GroovyClassLoader(parent);

        Class groovyClass = loader.parseClass(text, fileName);

        try {
            return (GroovyObject) groovyClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static File getGroovyScriptFile(File storageDir, String pageId) {
        return RandomUtil.getCodeFile(storageDir, GROOVY_FILE_NAME_PATTERN, pageId);
    }

    public static final GroovyClassLoader GROOVY_CLASS_LOADER =
            new GroovyClassLoader(ScriptingUtil.class.getClassLoader());

    public static Class<?> getGroovyClass(File storageDirFile, String id) throws IOException {
        File scriptFile = getGroovyScriptFile(storageDirFile, id);
        if(!scriptFile.exists()) {
            return null;
        }
        FileReader fr = new FileReader(scriptFile);
        String script = IOUtils.toString(fr);
        IOUtils.closeQuietly(fr);
        String path = scriptFile.getAbsolutePath();
        Class groovyClass = GROOVY_CLASS_LOADER.parseClass(script, path);
        return groovyClass;
    }
}
