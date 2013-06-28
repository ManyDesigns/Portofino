/*
* Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
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

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.portofino.ApplicationAttributes;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.actions.admin.modules.ModulesAction;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.menu.*;
import com.manydesigns.portofino.shiro.ShiroUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class BaseModule implements Module {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    protected ModuleStatus status = ModuleStatus.CREATED;

    @Inject(ApplicationAttributes.PORTOFINO_CONFIGURATION)
    public Configuration configuration;

    @Inject(ApplicationAttributes.ADMIN_MENU)
    public MenuBuilder adminMenu;

    @Inject(ApplicationAttributes.USER_MENU)
    public MenuBuilder userMenu;

    @Override
    public String getModuleVersion() {
        return configuration.getString(PortofinoProperties.PORTOFINO_VERSION);
    }

    @Override
    public int getMigrationVersion() {
        return 1;
    }

    @Override
    public double getPriority() {
        return 0;
    }

    @Override
    public String getId() {
        return "base";
    }

    @Override
    public String getName() {
        return "Portofino base";
    }

    @Override
    public int install() {
        return getMigrationVersion();
    }

    @Override
    public void init() {
        SimpleMenuAppender group = SimpleMenuAppender.group("configuration", null, "Configuration");
        adminMenu.menuAppenders.add(group);

        SimpleMenuAppender link = SimpleMenuAppender.link(
                "configuration", "modules", null, "Modules", ModulesAction.URL_BINDING);
        adminMenu.menuAppenders.add(link);

        userMenu.menuAppenders.add(new MenuAppender() {
            @Override
            public void append(Menu menu) {
                Subject subject = SecurityUtils.getSubject();
                if(subject.isAuthenticated()) {
                    MenuLink userLink =
                            new MenuLink("user", "icon-user icon-white",
                                         ShiroUtils.getPrimaryPrincipal(subject).toString(),
                                         null);
                    menu.items.add(userLink);
                }

            }
        });

        userMenu.menuAppenders.add(new MenuAppender() {
            @Override
            public void append(Menu menu) {
                Subject subject = SecurityUtils.getSubject();
                HttpServletRequest request = ElementsThreadLocals.getHttpServletRequest();
                if(subject.isAuthenticated()) {
                    MenuLink userLink =
                            new MenuLink("logout", null,
                                         ElementsThreadLocals.getText("skins.default.header.log_out"),
                                         ShiroUtils.getLogoutLink(configuration, request.getContextPath()));
                    menu.items.add(userLink);
                }

            }
        });

        status = ModuleStatus.ACTIVE;
    }

    @Override
    public void destroy() {
        status = ModuleStatus.DESTROYED;
    }

    @Override
    public ModuleStatus getStatus() {
        return status;
    }
}
