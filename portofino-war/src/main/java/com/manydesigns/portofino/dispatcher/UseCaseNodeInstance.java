/*
 * Copyright (C) 2005-2010 ManyDesigns srl.  All rights reserved.
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

import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.portofino.context.Application;
import com.manydesigns.portofino.model.datamodel.Table;
import com.manydesigns.portofino.model.site.UseCaseNode;
import com.manydesigns.portofino.model.site.usecases.UseCase;
import com.manydesigns.portofino.util.PkHelper;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla - alessio.stalla@manydesigns.com
*/
public class UseCaseNodeInstance extends SiteNodeInstance {

    protected final String pk;
    protected final UseCase useCase;

    protected final ClassAccessor classAccessor;
    protected final Table baseTable;
    protected final PkHelper pkHelper;

    protected Object object;

    public UseCaseNodeInstance(Application application, UseCaseNode siteNode, String mode, String param) {
        super(application, siteNode, mode);
        this.pk = param;
        this.useCase = siteNode.getUseCase();
        classAccessor = application.getUseCaseAccessor(useCase);
        baseTable = useCase.getActualTable();
        pkHelper = new PkHelper(classAccessor);
    }

    public Map<String, Object> realize(Object rootObject) {
        if(UseCaseNode.MODE_DETAIL.equals(mode)) {
            loadObject(pk, rootObject);
            if(object != null) {
                Map<String, Object> newValue = new HashMap<String, Object>();
                newValue.put(useCase.getActualVariable(), object);
                return newValue;
            } else {
                throw new RuntimeException("Not in use case: " + useCase.getName());
            }
        }
        return null;
    }

    private void loadObject(String pk, Object rootObject) {
        Serializable pkObject = pkHelper.parsePkString(pk);
        object = application.getObjectByPk(baseTable.getQualifiedName(), pkObject, useCase.getQuery(), rootObject);
    }

    // Getter/setter

    public String getPk() {
        return pk;
    }

    @Override
    public UseCaseNode getSiteNode() {
        return (UseCaseNode) super.getSiteNode();
    }

    public UseCase getUseCase() {
        return useCase;
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
}
