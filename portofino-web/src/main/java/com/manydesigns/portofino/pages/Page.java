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

package com.manydesigns.portofino.pages;

import com.manydesigns.elements.annotations.FieldSize;
import com.manydesigns.elements.annotations.Required;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.*;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@XmlRootElement
@XmlAccessorType(value = XmlAccessType.NONE)
public class Page {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected String id;
    protected String title;
    protected String description;
    protected String template;
    protected Layout layout;
    protected Layout detailLayout;
    protected Permissions permissions;
    protected String navigationRoot;

    //**************************************************************************
    // Actual fields
    //**************************************************************************

    protected NavigationRoot actualNavigationRoot;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger = LoggerFactory.getLogger(Page.class);

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public Page() {
        layout = new Layout();
        detailLayout = new Layout();
        permissions = new Permissions();
    }

    //**************************************************************************
    // Reset / init
    //**************************************************************************

    public void init() {
        assert title != null;
        assert description != null;

        if(navigationRoot == null) {
            actualNavigationRoot = NavigationRoot.INHERIT;
            navigationRoot = actualNavigationRoot.name();
        } else {
            actualNavigationRoot = NavigationRoot.valueOf(navigationRoot);
        }

        if(layout != null) {
            layout.init();
        }
        if(detailLayout != null) {
            detailLayout.init();
        }
        if(permissions != null) {
            permissions.init();
        }
    }

    //**************************************************************************
    // Getters/Setters
    //**************************************************************************

    @XmlAttribute(required = true)
    @Required
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @XmlAttribute(required = true)
    @Required
    @FieldSize(50)
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @XmlAttribute(required = true)
    @Required
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Page template
     */
    @XmlAttribute(required = true)
    @Required
    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    @XmlElement()
    public Layout getLayout() {
        return layout;
    }

    public void setLayout(Layout layout) {
        this.layout = layout;
    }

    @XmlElement()
    public Layout getDetailLayout() {
        return detailLayout;
    }

    public void setDetailLayout(Layout detailLayout) {
        this.detailLayout = detailLayout;
    }

    @XmlElement()
    public Permissions getPermissions() {
        return permissions;
    }

    public void setPermissions(Permissions permissions) {
        this.permissions = permissions;
    }

    @XmlAttribute(required = true)
    public String getNavigationRoot() {
        return navigationRoot;
    }

    public void setNavigationRoot(String navigationRoot) {
        this.navigationRoot = navigationRoot;
    }

    public NavigationRoot getActualNavigationRoot() {
        return actualNavigationRoot;
    }
}
