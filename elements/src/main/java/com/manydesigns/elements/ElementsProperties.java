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

package com.manydesigns.elements;

import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.BuilderParameters;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public final class ElementsProperties {
    public static final String copyright =
            "Copyright (C) 2005-2019 ManyDesigns srl";

    //**************************************************************************
    // Default and custom properties location
    //**************************************************************************

    public final static String PROPERTIES_RESOURCE =
            "elements.properties";
    public final static String CUSTOM_PROPERTIES_RESOURCE =
            "elements-custom.properties";


    //**************************************************************************
    // Property names
    //**************************************************************************

    public static final String FIELDS_MANAGER =
            "fields.manager";
    public static final String FIELDS_LIST =
            "fields.list";

    public static final String ANNOTATIONS_MANAGER =
            "annotations.manager";
    public static final String ANNOTATIONS_IMPLEMENTATION_LIST =
            "annotations.implementation.list";

    public static final String FIELDS_LABEL_CAPITALIZE =
            "fields.label.capitalize";
    public static final String FIELDS_DATE_FORMAT =
            "fields.date.format";
    public static final String FIELDS_DECIMAL_FORMAT =
            "elements.fields.format.decimal";

    //**************************************************************************
    // Static fields, singleton initialization and retrieval
    //**************************************************************************

    private static final CompositeConfiguration configuration;

    public static final Logger logger =
            LoggerFactory.getLogger(ElementsProperties.class);

    static {
        configuration = new CompositeConfiguration();
        addConfiguration(CUSTOM_PROPERTIES_RESOURCE);
        addConfiguration(PROPERTIES_RESOURCE);
    }

    public static void addConfiguration(String resource) {
        try {
            Configurations configurations = new Configurations();
            BuilderParameters parameters =
                    new Parameters().properties().setListDelimiterHandler(new DefaultListDelimiterHandler(',')).setFileName(resource);
            configuration.addConfiguration(configurations.propertiesBuilder(resource).configure(parameters).getConfiguration());
        } catch (Exception e) {
            logger.warn(String.format(
                    "Error loading properties from: %s", resource), e);
        }
    }

    public static Configuration getConfiguration() {
        return configuration;
    }

    //**************************************************************************
    // Dummy constructor
    //**************************************************************************

    private ElementsProperties() {}

}
