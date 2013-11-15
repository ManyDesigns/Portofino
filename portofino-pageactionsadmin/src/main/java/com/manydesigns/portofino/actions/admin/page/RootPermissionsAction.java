/*
 * Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.actions.admin.page;

import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.security.RequiresAdministrator;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@UrlBinding(RootPermissionsAction.URL_BINDING)
public class RootPermissionsAction extends RootConfigurationAction {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    public static final String URL_BINDING = "/actions/admin/root-page/permissions";

    @Override
    @DefaultHandler
    @RequiresAdministrator
    public Resolution pagePermissions() {
        return super.pagePermissions();
    }

    @Override
    protected Resolution forwardToPagePermissions() {
        return new ForwardResolution("/m/pageactionsadmin/actions/admin/page/rootPermissions.jsp");
    }

    @Button(list = "root-permissions", key = "update", order = 1, type = Button.TYPE_PRIMARY)
    public Resolution updatePagePermissions() {
        return super.updatePagePermissions();
    }

}
