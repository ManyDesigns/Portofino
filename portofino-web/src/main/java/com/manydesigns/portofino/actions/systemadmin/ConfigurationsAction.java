/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.manydesigns.portofino.actions.systemadmin;

import com.manydesigns.elements.ElementsProperties;
import com.manydesigns.elements.Mode;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.CommonsConfigurationAccessor;
import com.manydesigns.portofino.ApplicationAttributes;
import com.manydesigns.portofino.dispatcher.AbstractActionBean;
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
            "Copyright (c) 2005-2012, ManyDesigns srl";

    @Inject(ApplicationAttributes.PORTOFINO_CONFIGURATION)
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
