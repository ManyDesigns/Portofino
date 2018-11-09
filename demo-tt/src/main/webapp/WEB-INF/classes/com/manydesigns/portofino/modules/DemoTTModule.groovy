/*
* Copyright (C) 2005-2017 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.modules

import com.manydesigns.portofino.persistence.Persistence
import com.manydesigns.portofino.tt.NotificationsJob
import com.manydesigns.portofino.tt.TtUtils
import org.hibernate.Session
import org.quartz.*
import org.quartz.DateBuilder.IntervalUnit
import org.quartz.impl.StdSchedulerFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class DemoTTModule implements Module {
    public static final String copyright =
            "Copyright (C) 2005-2017 ManyDesigns srl";


    public final static Logger logger =
            LoggerFactory.getLogger(DemoTTModule.class);

    public final static String TT_VERSION = "0.9"

    protected ModuleStatus status = ModuleStatus.CREATED;

    @Autowired
    public Persistence persistence;

    public boolean installationMode = false;

    String getModuleVersion() {
        return TT_VERSION;
    }

    int getMigrationVersion() {
        return 1;
    }

    double getPriority() {
        return 100;
    }

    String getId() {
        return "tt";
    }

    String getName() {
        return "Portofino demo-tt";
    }

    int install() {
        installationMode = true;
        return 1;
    }

    void init() {
        status = ModuleStatus.ACTIVE;
    }

    void start() {
        if (installationMode) {
            Session session = persistence.getSession("tt");
            Date now = new Date();
            TtUtils.addActivity(session,
                    null,
                    now,
                    TtUtils.ACTIVITY_TYPE_SYSTEM_INSTALLED_SUCCESSFULLY,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
            session.getTransaction().commit();
        }

        status = ModuleStatus.STARTED;
    }

    void stop() {
        status = ModuleStatus.STOPPED;
    }

    void destroy() {
        status = ModuleStatus.DESTROYED;
    }

    ModuleStatus getStatus() {
        return status;
    }

}