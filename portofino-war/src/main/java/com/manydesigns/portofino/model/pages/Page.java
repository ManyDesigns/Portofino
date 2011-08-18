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

import com.manydesigns.elements.annotations.Required;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.ModelObject;
import com.manydesigns.portofino.xml.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@XmlAccessorType(value = XmlAccessType.NONE)
public abstract class Page implements ModelObject {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected String id;
    protected Page parent;
    protected final ArrayList<Page> childPages;

    protected Permissions permissions;
    protected String fragment;
    protected String title;
    protected String description;
    protected String url;
    protected String layoutContainerInParent;
    protected String layoutOrderInParent;
    protected String layoutContainer;
    protected String layoutOrder;

    //**************************************************************************
    // Actual fields
    //**************************************************************************

    protected Integer actualLayoutOrderInParent;
    protected int actualLayoutOrder;

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger = LoggerFactory.getLogger(Page.class);

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public Page() {
        childPages = new ArrayList<Page>();
        permissions = new Permissions();
    }

    //**************************************************************************
    // ModelObject implementation
    //**************************************************************************

    public void afterUnmarshal(Unmarshaller u, Object parent) {
        this.parent = (Page) parent;
    }

    public void reset() {
        for (Page childPage : childPages) {
            childPage.reset();
        }
        actualLayoutOrderInParent = null;
        actualLayoutOrder = 0;
    }

    public void init(Model model) {
        assert fragment != null;
        assert title != null;
        assert description != null;

        for (Page childPage : childPages) {
            childPage.init(model);
        }
        if (layoutOrderInParent != null) {
            //TODO controllare che sia non-null anche layoutContainerInParent
            actualLayoutOrderInParent = Integer.parseInt(layoutOrderInParent);
        }
        if(layoutOrder != null) {
            actualLayoutOrder = Integer.parseInt(layoutOrder);
        }
    }

    public String getQualifiedName() {
        return null;
    }

    //**************************************************************************
    // Utility Methods
    //**************************************************************************

    public Page findChildPageByFragment(String fragment) {
        for(Page page : getChildPages()) {
            if(fragment.equals(page.getFragment())) {
                return page;
            }
        }
        logger.debug("Child page not found: {}", fragment);
        return null;
    }

    public Page findDescendantPageById(String pageId) {
        for(Page page : getChildPages()) {
            if(pageId.equals(page.getId())) {
                return page;
            }
            Page descendant = page.findDescendantPageById(pageId);
            if(descendant != null) {
                return descendant;
            }
        }
        return null;
    }

    //**************************************************************************
    // Getters/Setters
    //**************************************************************************

    @Identifier
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
    public String getFragment() {
        return fragment;
    }

    public void setFragment(String fragment) {
        this.fragment = fragment;
    }

    @XmlAttribute(required = true)
    @Required
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
    public Permissions getPermissions() {
        return permissions;
    }

    public void setPermissions(Permissions permissions) {
        this.permissions = permissions;
    }

    @XmlElementWrapper(name="childPages")
    @XmlElements({
          @XmlElement(name="textPage",type=TextPage.class),
          @XmlElement(name="folderPage",type=FolderPage.class),
          @XmlElement(name="customPage",type=CustomPage.class),
          @XmlElement(name="customFolderPage",type=CustomFolderPage.class),
          @XmlElement(name="crudPage",type=CrudPage.class),
          @XmlElement(name="portletPage",type=ChartPage.class)
    })
    public List<Page> getChildPages() {
        return childPages;
    }

    public Page getParent() {
        return parent;
    }

    public void setParent(Page parent) {
        this.parent = parent;
    }

    @XmlAttribute()
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @XmlAttribute(required = false)
    public String getLayoutContainerInParent() {
        return layoutContainerInParent;
    }

    public void setLayoutContainerInParent(String layoutContainerInParent) {
        this.layoutContainerInParent = layoutContainerInParent;
    }

    @XmlAttribute(required = false)
    public String getLayoutOrderInParent() {
        return layoutOrderInParent;
    }

    public void setLayoutOrderInParent(String layoutOrderInParent) {
        this.layoutOrderInParent = layoutOrderInParent;
    }

    @XmlAttribute(required = true)
    public String getLayoutContainer() {
        return layoutContainer;
    }

    public void setLayoutContainer(String layoutContainer) {
        this.layoutContainer = layoutContainer;
    }

    @XmlAttribute(required = true)
    public String getLayoutOrder() {
        return layoutOrder;
    }

    public void setLayoutOrder(String layoutOrder) {
        this.layoutOrder = layoutOrder;
    }

    /* TODO: spostare quaesto metodo nella classe che gestisce la logica
    *  dei permessi. Lasciare le classi in in model il più possibile passive
    **/
    public boolean isAllowed(List<String> groups) {
        boolean parentAllowed= true;
        if (parent != null){
            parentAllowed= parent.isAllowed(groups);
        }
        if (!parentAllowed) {
            return false;
        }

        boolean result = true;
        if (permissions != null) {
            result = permissions.isAllowed(groups);
        }
        return result;
    }

    public Integer getActualLayoutOrderInParent() {
        return actualLayoutOrderInParent;
    }

    public int getActualLayoutOrder() {
        return actualLayoutOrder;
    }

    public void addChildPage(Page page) {
        for(Page child : getChildPages()) {
            if(child.getFragment().equals(page.getFragment())) {
                throw new IllegalArgumentException(
                        String.format("Page %s already has a child page with fragment %s and title %s",
                                      this.getTitle(), page.getFragment(), child.getTitle()));
            }
        }
        page.setParent(this);
        getChildPages().add(page);
    }

    public boolean removeChild(Page page) {
        if(page.getParent() == this) {
            page.setParent(null);
            getChildPages().remove(page);
            return true;
        } else {
            return false;
        }
    }
}
