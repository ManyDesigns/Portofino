/*
 * Copyright (C) 2005-2020 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.upstairs.actions.database.platforms;

import com.manydesigns.portofino.database.model.platforms.DatabasePlatform;
import com.manydesigns.portofino.database.model.platforms.DatabasePlatformsRegistry;
import com.manydesigns.portofino.resourceactions.AbstractResourceAction;
import com.manydesigns.portofino.persistence.Persistence;
import com.manydesigns.portofino.security.RequiresAdministrator;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.ws.rs.GET;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Alessio Stalla - alessiostalla@gmail.com
 */
@RequiresAuthentication
@RequiresAdministrator
public class PlatformsAction extends AbstractResourceAction {

    private static final Logger logger = LoggerFactory.getLogger(PlatformsAction.class);

    @Autowired
    protected Persistence persistence;

    @GET
    public Map<String, Map<String, String>> listDatabasePlatforms() {
        DatabasePlatformsRegistry manager = persistence.getDatabasePlatformsRegistry();
        DatabasePlatform[] platforms = manager.getDatabasePlatforms();
        Map<String, Map<String, String>> platformMap = new HashMap<>();
        for (DatabasePlatform platform : platforms) {
            Map<String, String> desc = new HashMap<>();
            desc.put("description", platform.getDescription());
            desc.put("standardDriverClassName", platform.getStandardDriverClassName());
            desc.put("status", platform.getStatus());
            desc.put("connectionStringTemplate", platform.getConnectionStringTemplate());
            platformMap.put(platform.getClass().getName(), desc);
        }
        return platformMap;
    }

}
