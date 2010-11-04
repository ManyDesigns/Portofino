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

package com.manydesigns.portofino.actions;

import com.manydesigns.elements.fields.search.Criteria;
import com.manydesigns.portofino.context.ModelObjectNotFoundError;
import com.manydesigns.portofino.model.datamodel.ForeignKey;
import com.manydesigns.portofino.model.usecases.UseCase;
import com.manydesigns.portofino.reflection.TableAccessor;
import com.manydesigns.portofino.util.PkHelper;
import org.apache.struts2.interceptor.ServletRequestAware;

import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class UseCaseAction extends AbstractCrudAction
        implements ServletRequestAware {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Model metadata
    //**************************************************************************

    public UseCase useCase;

    @Override
    public String redirectToFirst() {
        List<UseCase> useCases = model.getUseCases();
        if (useCases.isEmpty()) {
            return NO_CLASSES;
        } else {
            qualifiedName = useCases.get(0).getName();
            return REDIRECT_TO_FIRST;
        }
    }

    //**************************************************************************
    // Common methods
    //**************************************************************************

    public void setupMetadata() {
        useCase = model.findUseCaseByName(qualifiedName);
        baseTable = useCase.getActualTable();
        classAccessor = context.getUseCaseAccessor(qualifiedName);
        pkHelper = new PkHelper(classAccessor);
        if (useCase == null || classAccessor == null) {
            throw new ModelObjectNotFoundError(qualifiedName);
        }
    }

    @Override
    public void setupCriteria() {
        TableAccessor tableAccessor =
                context.getTableAccessor(useCase.getTable());
        Criteria criteria = new Criteria(tableAccessor);
        searchForm.configureCriteria(criteria);
        objects = context.getObjects(useCase.getFilter(), criteria);
    }


    protected void setupRelatedTableForm(ForeignKey relationship) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

}
