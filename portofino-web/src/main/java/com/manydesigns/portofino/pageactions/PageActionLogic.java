/*
* Copyright (C) 2005-2011 ManyDesigns srl.  All rights reserved.
* http://www.manydesigns.com/
*
* Unless you have purchased a commercial license agreement from ManyDesigns srl,
* the following license terms apply:
*
* This program is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License version 3 as published by
* the Free Software Foundation.
*
* There are special exceptions to the terms and conditions of the GPL
* as it is applied to this software. View the full text of the
* exception in file OPEN-SOURCE-LICENSE.txt in the directory of this
* software distribution.
*
* This program is distributed WITHOUT ANY WARRANTY; and without the
* implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see http://www.gnu.org/licenses/gpl.txt
* or write to:
* Free Software Foundation, Inc.,
* 59 Temple Place - Suite 330,
* Boston, MA  02111-1307  USA
*
*/

package com.manydesigns.portofino.pageactions;

import com.manydesigns.portofino.dispatcher.PageAction;
import com.manydesigns.portofino.pageactions.annotations.ConfigurationClass;
import com.manydesigns.portofino.pageactions.annotations.ScriptTemplate;
import com.manydesigns.portofino.pageactions.annotations.SupportsDetail;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class PageActionLogic {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    public static boolean supportsDetail(Class<?> actionClass) {
        if(!PageAction.class.isAssignableFrom(actionClass)) {
            return false;
        }
        SupportsDetail supportsDetail = actionClass.getAnnotation(SupportsDetail.class);
        if(supportsDetail != null) {
            return supportsDetail.value();
        } else {
            return supportsDetail(actionClass.getSuperclass());
        }
    }

    public static Class<?> getConfigurationClass(Class<?> actionClass) {
        if(!PageAction.class.isAssignableFrom(actionClass)) {
            return null;
        }
        ConfigurationClass configurationClass = actionClass.getAnnotation(ConfigurationClass.class);
        if(configurationClass != null) {
            return configurationClass.value();
        } else {
            return getConfigurationClass(actionClass.getSuperclass());
        }
    }

    public static String getScriptTemplate(Class<?> actionClass) {
        if(!PageAction.class.isAssignableFrom(actionClass)) {
            return null;
        }
        ScriptTemplate scriptTemplate = actionClass.getAnnotation(ScriptTemplate.class);
        if(scriptTemplate != null) {
            String templateLocation = scriptTemplate.value();
            try {
                return IOUtils.toString(actionClass.getResourceAsStream(templateLocation));
            } catch (Exception e) {
                throw new Error("Can't load script template", e);
            }
        } else {
            String template = getScriptTemplate(actionClass.getSuperclass());
            if(template != null) {
                return template;
            } else {
                try {
                    InputStream stream =
                            PageActionLogic.class.getResourceAsStream
                                    ("/com/manydesigns/portofino/pageactions/default_script_template.txt");
                    return IOUtils.toString(stream);
                } catch (Exception e) {
                    throw new Error("Can't load script template", e);
                }
            }
        }
    }

    public static String getDescriptionKey(Class<?> actionClass) {
        PageActionName annotation = actionClass.getAnnotation(PageActionName.class);
        if(annotation != null) {
            return annotation.value();
        } else {
            return actionClass.getName();
        }
    }
}
