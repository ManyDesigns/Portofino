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

package com.manydesigns.portofino.modules;

import com.manydesigns.portofino.database.platforms.H2DatabasePlatform;
import com.manydesigns.portofino.model.database.platforms.DatabasePlatformsRegistry;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class H2Module implements Module {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    @Autowired
    DatabasePlatformsRegistry databasePlatformsRegistry;

    protected ModuleStatus status = ModuleStatus.CREATED;

    @Override
    public String getModuleVersion() {
        return Module.getPortofinoVersion();
    }

    @Override
    public String getName() {
        return "H2";
    }

    @PostConstruct
    public void init() {
        databasePlatformsRegistry.addDatabasePlatform(new H2DatabasePlatform());
        status = ModuleStatus.STARTED;
    }

    @PreDestroy
    public void destroy() {
        status = ModuleStatus.DESTROYED;
    }

    @Override
    public ModuleStatus getStatus() {
        return status;
    }
}
