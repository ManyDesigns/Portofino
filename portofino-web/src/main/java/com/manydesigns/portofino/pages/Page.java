/*
 * Copyright (C) 2005-2011 ManyDesigns srl.  All rights reserved.
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
            "Copyright (c) 2005-2011, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected String id;
    protected String title;
    protected String description;
    protected Layout layout;
    protected Layout detailLayout;
    protected Permissions permissions;
    protected boolean subtreeRoot = false;

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

    @XmlAttribute
    public boolean isSubtreeRoot() {
        return subtreeRoot;
    }

    public void setSubtreeRoot(boolean subtreeRoot) {
        this.subtreeRoot = subtreeRoot;
    }
}
