/*
 * Copyright (C) 2005-2019 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.resourceactions;

import com.manydesigns.portofino.resourceactions.annotations.ConfigurationClass;
import com.manydesigns.portofino.resourceactions.annotations.ScriptTemplate;
import com.manydesigns.portofino.resourceactions.annotations.SupportsDetail;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class ResourceActionLogic {
    public static final String copyright = "Copyright (C) 2005-2019 ManyDesigns srl";

    public static final Logger logger = LoggerFactory.getLogger(ResourceActionLogic.class);

    public static boolean supportsDetail(Class<?> actionClass) {
        if(!ResourceAction.class.isAssignableFrom(actionClass)) {
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
        if(!ResourceAction.class.isAssignableFrom(actionClass)) {
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
        if(!ResourceAction.class.isAssignableFrom(actionClass)) {
            return null;
        }
        ScriptTemplate scriptTemplate = actionClass.getAnnotation(ScriptTemplate.class);
        if(scriptTemplate != null) {
            String templateLocation = scriptTemplate.value();
            try {
                return IOUtils.toString(actionClass.getResourceAsStream(templateLocation), StandardCharsets.UTF_8);
            } catch (Exception e) {
                logger.error("Can't load script template: " + templateLocation + " for class: " + actionClass.getName(), e);
            }
        } else {
            String template = getScriptTemplate(actionClass.getSuperclass());
            if(template != null) {
                return template;
            }
        }
        logger.debug("Falling back to default template for {}", actionClass);
        try {
            InputStream stream =
                    ResourceActionLogic.class.getResourceAsStream
                            ("/com/manydesigns/portofino/resourceactions/default_script_template.txt");
            return IOUtils.toString(stream, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new Error("Can't load script template", e);
        }
    }

    public static String getDescriptionKey(Class<?> actionClass) {
        ResourceActionName annotation = actionClass.getAnnotation(ResourceActionName.class);
        if(annotation != null) {
            return annotation.value();
        } else {
            return actionClass.getName();
        }
    }
}
