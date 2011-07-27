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

package com.manydesigns.portofino.dispatcher;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.portofino.context.Application;
import com.manydesigns.portofino.model.datamodel.Table;
import com.manydesigns.portofino.model.site.CrudNode;
import com.manydesigns.portofino.model.site.SiteNode;
import com.manydesigns.portofino.model.site.crud.Crud;
import com.manydesigns.portofino.util.PkHelper;
import ognl.OgnlContext;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla - alessio.stalla@manydesigns.com
*/
public class CrudNodeInstance extends SiteNodeInstance {

    protected final String pk;
    protected final Crud crud;

    protected final ClassAccessor classAccessor;
    protected final Table baseTable;
    protected final PkHelper pkHelper;

    protected Object object;

    public CrudNodeInstance(Application application, CrudNode siteNode, String mode, String param) {
        super(application, siteNode, mode);
        this.pk = param;
        this.crud = siteNode.getCrud();
        classAccessor = application.getCrudAccessor(crud);
        baseTable = crud.getActualTable();
        pkHelper = new PkHelper(classAccessor);
    }

    public void realize() {
        if(CrudNode.MODE_DETAIL.equals(mode)) {
            OgnlContext ognlContext = ElementsThreadLocals.getOgnlContext();
            loadObject(pk);
            if(object != null) {
                ognlContext.put(crud.getActualVariable(), object);
            } else {
                throw new RuntimeException("Not in use case: " + crud.getName());
            }
        }
    }

    private void loadObject(String pk) {
        Serializable pkObject = pkHelper.parsePkString(pk);
        object = application.getObjectByPk(
                baseTable.getQualifiedName(), pkObject,
                crud.getQuery(), null);
    }

    // Getter/setter

    public String getPk() {
        return pk;
    }

    @Override
    public CrudNode getSiteNode() {
        return (CrudNode) super.getSiteNode();
    }

    @Override
    public String getUrlFragment() {
        if (pk == null) {
            return super.getUrlFragment();
        } else {
            return String.format("%s/%s", super.getUrlFragment(), pk);
        }
    }

    public Crud getCrud() {
        return crud;
    }

    public ClassAccessor getClassAccessor() {
        return classAccessor;
    }

    public Table getBaseTable() {
        return baseTable;
    }

    public PkHelper getPkHelper() {
        return pkHelper;
    }

    public Object getObject() {
        return object;
    }

    @Override
    public List<SiteNode> getChildNodes() {
        if (CrudNode.MODE_SEARCH.equals(mode)) {
            return siteNode.getChildNodes();
        } else if (CrudNode.MODE_DETAIL.equals(mode)) {
            return getSiteNode().getDetailChildNodes();
        } else {
            return Collections.EMPTY_LIST;
        }
    }
}
