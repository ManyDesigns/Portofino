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

import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.pages.crud.Crud;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@XmlAccessorType(XmlAccessType.NONE)
public class CrudPage extends Page {
    //**************************************************************************
    // Constants
    //**************************************************************************

    public static final String MODE_SEARCH = "search";
    public static final String MODE_NEW = "new";
    public static final String MODE_DETAIL = "detail";
//    public static final String MODE_EMBEDDED_SEARCH = "embeddedSearch";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected Crud crud;
    protected final ArrayList<Page> detailChildPages;

    protected String detailLayoutContainer;
    protected String detailLayoutOrder;

    protected int actualDetailLayoutOrder;

    public CrudPage() {
        super();
        detailChildPages = new ArrayList<Page>();
    }

    @XmlElement()
    public Crud getCrud() {
        return crud;
    }

    public void setCrud(Crud crud) {
        this.crud = crud;
    }

    @Override
    public void reset() {
        super.reset();

        for (Page current : detailChildPages) {
            current.reset();
        }

        if(crud != null) {
            crud.reset();
        }
    }

    @Override
    public void init(Model model) {
        super.init(model);

        if(detailLayoutOrder != null) {
            actualDetailLayoutOrder = Integer.parseInt(detailLayoutOrder);
        }

        for (Page current : detailChildPages) {
            current.init(model);
        }

        if(crud != null) {
            crud.init(model);
        }
    }

    @XmlElementWrapper(name="detailChildPages")
    @XmlElements({
          @XmlElement(name="textPage",type=TextPage.class),
          @XmlElement(name="folderPage",type=FolderPage.class),
          @XmlElement(name="customPage",type=CustomPage.class),
          @XmlElement(name="customFolderPage",type=CustomFolderPage.class),
          @XmlElement(name="crudPage",type=CrudPage.class),
          @XmlElement(name="chartPage",type=ChartPage.class),
          @XmlElement(name="jspPage",type=JspPage.class),
          @XmlElement(name="pageReference",type=PageReference.class)
    })
    public ArrayList<Page> getDetailChildPages() {
        return detailChildPages;
    }

    @XmlAttribute
    public String getDetailLayoutContainer() {
        return detailLayoutContainer;
    }

    public void setDetailLayoutContainer(String detailLayoutContainer) {
        this.detailLayoutContainer = detailLayoutContainer;
    }

    @XmlAttribute
    public String getDetailLayoutOrder() {
        return detailLayoutOrder;
    }

    public void setDetailLayoutOrder(String detailLayoutOrder) {
        this.detailLayoutOrder = detailLayoutOrder;
    }

    public int getActualDetailLayoutOrder() {
        return actualDetailLayoutOrder;
    }

    public boolean removeDetailChild(Page page) {
        return removeChild(page, detailChildPages);
    }

    public void addDetailChild(Page page) {
        addChild(page, detailChildPages);
    }

    @Override
    public Page findDescendantPageById(String pageId) {
        if(pageId.equals(getId())) {
            return this;
        }
        for(Page page : getChildPages()) {
            Page descendant = page.findDescendantPageById(pageId);
            if(descendant != null) {
                return descendant;
            }
        }
        for(Page page : getDetailChildPages()) {
            Page descendant = page.findDescendantPageById(pageId);
            if(descendant != null) {
                return descendant;
            }
        }
        return null;
    }



}
