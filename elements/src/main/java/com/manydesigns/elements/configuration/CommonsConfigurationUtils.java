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

package com.manydesigns.elements.configuration;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.FileConfiguration;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class CommonsConfigurationUtils {

    public static void save(Configuration configuration) throws ConfigurationException {
        FileConfiguration fileConfiguration =
                getWritableFileConfiguration(configuration);
        if (fileConfiguration == null) {
            throw new ConfigurationException("Cannot save configuration");
        } else {
            fileConfiguration.save();
        }
    }

    public static FileConfiguration getWritableFileConfiguration(Configuration configuration) {
        if (configuration instanceof FileConfiguration) {
            return (FileConfiguration)configuration;
        } else if (configuration instanceof CompositeConfiguration) {
            CompositeConfiguration compositeConfiguration =
                    (CompositeConfiguration)configuration;
            Configuration inMemoryConfigutation =
                    compositeConfiguration.getInMemoryConfiguration();
            return getWritableFileConfiguration(inMemoryConfigutation);
        } else {
            return null;
        }
    }
}
