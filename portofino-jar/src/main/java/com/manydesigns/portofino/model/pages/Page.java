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

package com.manydesigns.portofino.model.pages;

import com.manydesigns.elements.annotations.FieldSize;
import com.manydesigns.elements.annotations.Required;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.ModelObject;
import com.manydesigns.portofino.model.ModelVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@XmlRootElement
@XmlAccessorType(value = XmlAccessType.NONE)
public class Page implements ModelObject {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected String id;
    protected String title;
    protected String description;
    protected final Layout layout;
    protected final Layout detailLayout;
    protected final Permissions permissions;
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
    // ModelObject implementation
    //**************************************************************************

    public void afterUnmarshal(Unmarshaller u, Object parent) {
    }

    public void reset() {
    }

    public void init(Model model) {
        assert title != null;
        assert description != null;
    }

    public void link(Model model) {}

    public void visitChildren(ModelVisitor visitor) {
        visitor.visit(layout);
        visitor.visit(detailLayout);
        visitor.visit(permissions);
    }

    public String getQualifiedName() {
        return null;
    }

    //**************************************************************************
    // Utility Methods
    //**************************************************************************

    /*public Page findDescendantPageById(String pageId) {
        if(pageId.equals(getId())) {
            return this;
        }
        for(Page page : getChildPages()) {
            Page descendant = page.findDescendantPageById(pageId);
            if(descendant != null) {
                return descendant;
            }
        }
        return null;
    }*/

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

//    @XmlAttribute(required = true)
//    @Required
//    @RegExp(value = "[a-zA-Z0-9_\\-]+", errorMessage = "page.invalid.fragment")
//    public String getFragment() {
//        return fragment;
//    }
//
//    public void setFragment(String fragment) {
//        this.fragment = fragment;
//    }

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

    @XmlElement()
    public Layout getDetailLayout() {
        return detailLayout;
    }

    @XmlElement()
    public Permissions getPermissions() {
        return permissions;
    }

    /*protected void addChild(Page page, List<Page> children) {
        for(Page child : children) {
            if(child.getFragment().equals(page.getFragment())) {
                throw new IllegalArgumentException(
                        String.format("Page %s already has a child page with fragment %s and title %s",
                                      this.getTitle(), page.getFragment(), child.getTitle()));
            }
        }
        page.setParent(this);
        children.add(page);
    }

    public boolean removeChild(Page page) {
        List<Page> children = getChildPages();
        return removeChild(page, children);
    }

    protected boolean removeChild(Page page, List<Page> children) {
        if(page.getParent() == this) {
            page.setParent(null);
            children.remove(page);
            return true;
        } else {
            return false;
        }
    }*/

    @XmlAttribute
    public boolean isSubtreeRoot() {
        return subtreeRoot;
    }

    public void setSubtreeRoot(boolean subtreeRoot) {
        this.subtreeRoot = subtreeRoot;
    }
}
