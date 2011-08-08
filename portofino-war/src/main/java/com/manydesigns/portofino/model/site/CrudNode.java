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
package com.manydesigns.portofino.model.site;

import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.site.crud.Crud;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@XmlAccessorType(XmlAccessType.NONE)
public class CrudNode extends SiteNode {
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
    protected final ArrayList<SiteNode> detailChildNodes;

    protected String detailLayoutContainer;
    protected String detailLayoutOrder;

    protected int actualDetailLayoutOrder;

    public CrudNode() {
        super();
        detailChildNodes = new ArrayList<SiteNode>();
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

        for (SiteNode current : detailChildNodes) {
            current.reset();
        }

        crud.reset();
    }

    @Override
    public void init(Model model) {
        super.init(model);

        if(detailLayoutOrder != null) {
            actualDetailLayoutOrder = Integer.parseInt(detailLayoutOrder);
        }

        for (SiteNode current : detailChildNodes) {
            current.init(model);
        }

        crud.init(model);
    }

    @XmlElementWrapper(name="detailChildNodes")
    @XmlElements({
          @XmlElement(name="documentNode",type=DocumentNode.class),
          @XmlElement(name="folderNode",type=FolderNode.class),
          @XmlElement(name="customNode",type=CustomNode.class),
          @XmlElement(name="customFolderNode",type=CustomFolderNode.class),
          @XmlElement(name="crudNode",type=CrudNode.class),
          @XmlElement(name="portletNode",type=ChartNode.class)
    })
    public ArrayList<SiteNode> getDetailChildNodes() {
        return detailChildNodes;
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
}
