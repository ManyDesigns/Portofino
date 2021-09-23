/*
* Copyright (C) 2005-2020 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.modules;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * A Module is a component in a Portofino application that offers capabilities to user code
 * (actions and shared classes). Portofino discovers modules automatically at startup and registers
 * them as Spring beans.
 *
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public interface Module {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    //Utilities
    static String getPortofinoVersion() {
        try {
            return IOUtils.toString(Module.class.getResourceAsStream("/portofino.version"), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Each module has a version. Currently, its only use is to inform the user about installed modules.
     * @return the version of this module.
     */
    String getModuleVersion();

    /**
     * Each module has a name. Currently, its only use is to inform the user about installed modules.
     * @return the name of this module.
     */
    String getName();

    /**
     * Each module has a dynamic state representing the phase of its lifecycle that it's currently in.
     * @return this module's status.
     */
    ModuleStatus getStatus();

}
