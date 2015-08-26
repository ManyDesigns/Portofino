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

package com.manydesigns.portofino.actions.admin.servletcontext;

import com.manydesigns.elements.Mode;
import com.manydesigns.elements.forms.TableForm;
import com.manydesigns.elements.forms.TableFormBuilder;
import com.manydesigns.elements.ognl.OgnlUtils;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.security.RequiresAdministrator;
import com.manydesigns.portofino.stripes.AbstractActionBean;
import net.sourceforge.stripes.action.*;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@RequiresAuthentication
@RequiresAdministrator
@UrlBinding(ServletContextAction.URL_BINDING)
public class ServletContextAction extends AbstractActionBean {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    public static final String URL_BINDING = "/actions/admin/servlet-context";

    TableForm form;

    //--------------------------------------------------------------------------
    // Logging
    //--------------------------------------------------------------------------

    public final static Logger logger =
            LoggerFactory.getLogger(ServletContextAction.class);

    //--------------------------------------------------------------------------
    // Action events
    //--------------------------------------------------------------------------

    @DefaultHandler
    @RequiresPermissions("com.manydesigns.portofino.servletcontext:list")
    public Resolution execute() {
        setupForm();
        return new ForwardResolution("/m/admin/servletcontext/list.jsp");
    }

    protected void setupForm() {
        ServletContext servletContext = context.getServletContext();
        Enumeration<String> attributeNames = servletContext.getAttributeNames();
        List<KeyValue> attributes = new ArrayList<KeyValue>();
        while (attributeNames.hasMoreElements()) {
            String key = attributeNames.nextElement();
            String value = StringUtils.abbreviate(OgnlUtils.convertValueToString(servletContext.getAttribute(key)), 300);
            attributes.add(new KeyValue(key, value));
        }
        TableFormBuilder builder = new TableFormBuilder(KeyValue.class);
        builder.configNRows(attributes.size());
        builder.configMode(Mode.VIEW);
        form = builder.build();
        form.readFromObject(attributes);
        form.setCondensed(true);
    }

    @Button(list = "modules", key = "return.to.pages", order = 2  , icon = Button.ICON_HOME)
    public Resolution returnToPages() {
        return new RedirectResolution("/");
    }

    public TableForm getForm() {
        return form;
    }

    public static class KeyValue {

        public KeyValue(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String key;
        public String value;
    }
}
