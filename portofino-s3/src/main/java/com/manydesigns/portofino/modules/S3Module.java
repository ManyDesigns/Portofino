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

package com.manydesigns.portofino.modules;

import com.manydesigns.elements.blobs.BlobManager;
import com.manydesigns.elements.blobs.S3BlobManager;
import com.manydesigns.portofino.di.Inject;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.ServletContext;
/*
* @author Emanuele Poggi        - emanuele.poggi@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class S3Module implements Module {
    public static final String copyright =
            "Copyright (C) 2005-2021 ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    @Inject(BaseModule.PORTOFINO_CONFIGURATION)
    public Configuration configuration;

    @Inject(BaseModule.SERVLET_CONTEXT)
    public ServletContext servletContext;

    //BLOB Manager
    public static final String BLOB_MANAGER_TYPE = "blobmanager.type";
    public static final String AWS_REGION = "aws.region";
    public static final String AWS_S3_BUCKET = "aws.s3.bucket";
    public static final String AWS_S3_LOCATION = "aws.s3.location";

    protected ModuleStatus status = ModuleStatus.CREATED;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger = LoggerFactory.getLogger(S3Module.class);

    @Override
    public String getModuleVersion() {
        return ModuleRegistry.getPortofinoVersion();
    }

    @Override
    public int getMigrationVersion() {
        return 1;
    }

    @Override
    public double getPriority() {
        return 20;
    }

    @Override
    public String getId() {
        return "s3";
    }

    @Override
    public String getName() {
        return "S3";
    }

    @Override
    public int install() {
        return 1;
    }

    @Override
    public void init() {
        BlobManager blobManager = getBlobManager();

        if( blobManager!=null)
            servletContext.setAttribute(BaseModule.DEFAULT_BLOB_MANAGER, blobManager);

        status = ModuleStatus.ACTIVE;
    }

    @Override
    public void start() {
        status = ModuleStatus.STARTED;
    }

    @Override
    public void stop() {
        status = ModuleStatus.STOPPED;
    }

    @Override
    public void destroy() {
        status = ModuleStatus.DESTROYED;
    }

    @Override
    public ModuleStatus getStatus() {
        return status;
    }

    private BlobManager getBlobManager(){
        if(configuration.getString(BLOB_MANAGER_TYPE, "standard").equalsIgnoreCase( "s3" )){
            String region = configuration.getString(AWS_REGION);
            String bucketName = configuration.getString(AWS_S3_BUCKET);
            String location =   configuration.getString(AWS_S3_LOCATION);
            logger.info("Using S3 blob manager");
            if(StringUtils.trimToNull( region )==null){
                logger.error(AWS_REGION+ " property not found" );
            }
            if(StringUtils.trimToNull( bucketName )==null){
                logger.error( AWS_S3_BUCKET + " property not found" );
            }
            if(StringUtils.trimToNull(location) == null)
                return new S3BlobManager(region, bucketName);
            else
                return new S3BlobManager(region, bucketName, location);
        }
        return null;
    }
}
