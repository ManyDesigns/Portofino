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

package com.manydesigns.elements;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public final class ElementsProperties {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

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

    public static final String BLOBS_DIR =
            "blobs.dir";
    public static final String BLOBS_META_FILENAME_PATTERN =
            "blobs.meta.filename.pattern";
    public static final String BLOBS_DATA_FILENAME_PATTERN =
            "blobs.data.filename.pattern";

    public static final String WEB_FRAMEWORK =
            "web.framework";


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
            configuration.addConfiguration(
                    new PropertiesConfiguration(resource));
        } catch (Throwable e) {
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
