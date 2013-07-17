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

package com.manydesigns.portofino.actions.systemadmin;

import com.manydesigns.elements.ElementsProperties;
import com.manydesigns.elements.Mode;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.CommonsConfigurationAccessor;
import com.manydesigns.portofino.modules.BaseModule;
import com.manydesigns.portofino.stripes.AbstractActionBean;
import com.manydesigns.portofino.di.Inject;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.SystemConfiguration;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class ConfigurationsAction extends AbstractActionBean {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    @Inject(BaseModule.PORTOFINO_CONFIGURATION)
    public Configuration portofinoConfiguration;

    public Form form;

    public String execute() {
        return portofinoConfiguration();
    }

    public String portofinoConfiguration() {
        form = configureForm(portofinoConfiguration);

        return "portofinoConfiguration";
    }

    public String elementsConfiguration() {
        form = configureForm(ElementsProperties.getConfiguration());

        return "elementsConfiguration";
    }

    public String systemConfiguration() {
        form = configureForm(new SystemConfiguration());

        return "systemConfiguration";
    }

    private Form configureForm(Configuration configuration) {
        ClassAccessor accessor = new CommonsConfigurationAccessor(configuration);
        Form form = new FormBuilder(accessor).configMode(Mode.VIEW).build();
        form.readFromObject(configuration);
        return form;
    }

}
