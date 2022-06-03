/*
 * Copyright (C) 2005-2022 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.config;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationSource {

    protected final Configuration configuration;
    protected final FileBasedConfigurationBuilder<PropertiesConfiguration> writableConfiguration;

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationSource.class);

    public ConfigurationSource(
            Configuration configuration, FileBasedConfigurationBuilder<PropertiesConfiguration> writableConfiguration) {
        this.configuration = configuration;
        this.writableConfiguration = writableConfiguration;
    }

    public Configuration getProperties() {
        return configuration;
    }

    public boolean isWritable() {
        return writableConfiguration != null;
    }

    public void save() throws ConfigurationException {
        if (!isWritable()) {
            throw new ConfigurationException("Not writable");
        }
        writableConfiguration.save();
        logger.info("Saved configuration file {}", writableConfiguration.getFileHandler().getFile().getAbsolutePath());
    }
}
