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

package com.manydesigns.portofino.actions.admin.modules;

import com.manydesigns.elements.Mode;
import com.manydesigns.elements.annotations.Status;
import com.manydesigns.elements.forms.TableForm;
import com.manydesigns.elements.forms.TableFormBuilder;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.modules.BaseModule;
import com.manydesigns.portofino.modules.Module;
import com.manydesigns.portofino.modules.ModuleRegistry;
import com.manydesigns.portofino.security.RequiresAdministrator;
import com.manydesigns.portofino.stripes.AbstractActionBean;
import net.sourceforge.stripes.action.*;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@RequiresAuthentication
@RequiresAdministrator
@UrlBinding(ModulesAction.URL_BINDING)
public class ModulesAction extends AbstractActionBean {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    public static final String URL_BINDING = "/actions/admin/modules";

    @Inject(BaseModule.MODULE_REGISTRY)
    ModuleRegistry moduleRegistry;

    TableForm form;

    //--------------------------------------------------------------------------
    // Logging
    //--------------------------------------------------------------------------

    public final static Logger logger =
            LoggerFactory.getLogger(ModulesAction.class);

    //--------------------------------------------------------------------------
    // Action events
    //--------------------------------------------------------------------------

    @DefaultHandler
    public Resolution execute() {
        setupForm();
        return new ForwardResolution("/m/admin/modules/list.jsp");
    }

    protected void setupForm() {
        TableFormBuilder builder = new TableFormBuilder(ModuleView.class);
        builder.configNRows(moduleRegistry.getModules().size());
        builder.configMode(Mode.VIEW);
        form = builder.build();
        List<ModuleView> modules = new ArrayList<ModuleView>();
        for(Module module : moduleRegistry.getModules()) {
            ModuleView view = new ModuleView();
            view.id = module.getId();
            view.name = module.getName();
            view.status = module.getStatus().name();
            view.version = module.getModuleVersion();
            modules.add(view);
        }
        form.readFromObject(modules);
        form.setCondensed(true);
    }

    @Button(list = "modules", key = "return.to.pages", order = 2  , icon = Button.ICON_HOME)
    public Resolution returnToPages() {
        return new RedirectResolution("/");
    }

    public TableForm getForm() {
        return form;
    }

    public static final class ModuleView {

        public String id;
        public String name;
        public String version;
        @Status(red = { "FAILED", "DESTROYED" }, amber = { "CREATED", "STOPPED" }, green = { "ACTIVE", "STARTED" })
        public String status;

    }
}
