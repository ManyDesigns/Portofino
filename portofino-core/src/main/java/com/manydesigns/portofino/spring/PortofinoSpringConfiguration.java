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

package com.manydesigns.portofino.spring;

import com.manydesigns.elements.blobs.BlobManager;
import com.manydesigns.elements.blobs.HierarchicalBlobManager;
import com.manydesigns.elements.blobs.SimpleBlobManager;
import com.manydesigns.elements.crypto.KeyManager;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.cache.CacheResetListenerRegistry;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.vfs2.FileObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

import java.io.File;

@org.springframework.context.annotation.Configuration
public class PortofinoSpringConfiguration implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(PortofinoSpringConfiguration.class);
    public static final String APPLICATION_DIRECTORY = "com.manydesigns.portofino.application.directory";
    public static final String DEFAULT_BLOB_MANAGER = "defaultBlobManager";
    public final static String PORTOFINO_CONFIGURATION = "com.manydesigns.portofino.portofinoConfiguration";
    public final static String PORTOFINO_CONFIGURATION_FILE = "com.manydesigns.portofino.portofinoConfigurationFile";

    @Autowired
    @Qualifier(PORTOFINO_CONFIGURATION)
    Configuration configuration;
    @Autowired
    @Qualifier(APPLICATION_DIRECTORY)
    FileObject applicationDirectory;

    @Bean(name = DEFAULT_BLOB_MANAGER)
    public BlobManager getDefaultBlobManager() {
        File appBlobsDir;
        if(configuration.containsKey(PortofinoProperties.BLOBS_DIR_PATH)) {
            appBlobsDir = new File(configuration.getString(PortofinoProperties.BLOBS_DIR_PATH));
        } else {
            appBlobsDir = new File(applicationDirectory.getName().getPath(), "blobs");
        }
        logger.info("Blobs directory: " + appBlobsDir.getAbsolutePath());

        String metaFilenamePattern = "blob-{0}.properties";
        String dataFilenamePattern = "blob-{0}.data";
        File[] blobs = appBlobsDir.listFiles((dir, name) -> name.startsWith("blob-") && name.endsWith(".properties"));
        if(blobs == null || blobs.length == 0) { //Null if the directory does not exist yet
            logger.info("Using hierarchical blob manager");
            return new HierarchicalBlobManager(appBlobsDir, metaFilenamePattern, dataFilenamePattern);
        } else {
            logger.warn("Blobs found directly under the blobs directory: using old style (pre-4.1.1) flat file blob manager");
            return new SimpleBlobManager(appBlobsDir, metaFilenamePattern, dataFilenamePattern);
        }
    }

    @Bean
    public CacheResetListenerRegistry getCacheResetListenerRegistry() {
        return new CacheResetListenerRegistry();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if(!KeyManager.isActive()) {
            try {
                logger.info("Initializing KeyManager ");
                KeyManager.init(configuration);
            } catch (Exception e) {
                logger.error("Could not initialize KeyManager", e);
            }
        }
    }
}
