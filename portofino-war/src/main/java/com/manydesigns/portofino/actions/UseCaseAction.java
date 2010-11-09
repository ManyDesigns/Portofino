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

import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.portofino.context.ModelObjectNotFoundError;
import com.manydesigns.portofino.model.datamodel.Table;
import com.manydesigns.portofino.model.usecases.UseCase;

import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class UseCaseAction extends AbstractCrudAction {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //**************************************************************************
    // Setup
    //**************************************************************************

    @Override
    public void setupMetadata() {
        if (qualifiedName == null) {
            return;
        }
        UseCase rootUseCase = model.findUseCaseByQualifiedName(qualifiedName);
        if (rootUseCase == null) {
            throw new ModelObjectNotFoundError(qualifiedName);
        }
        rootCrudUnit = setupUseCaseInstance(rootUseCase);
    }

    private CrudUnit setupUseCaseInstance(UseCase useCase) {
        ClassAccessor classAccessor =
                    context.getUseCaseAccessor(useCase.getQualifiedName());
        Table baseTable = useCase.getActualTable();
        String query = useCase.getQuery();
        CrudUnit result = new CrudUnit(classAccessor, baseTable, query,
                useCase.getSearchTitle(), useCase.getCreateTitle(),
                useCase.getReadTitle(), useCase.getEditTitle());
        result.buttons.addAll(useCase.getButtons());

        // inject values
        result.context = context;
        result.model = model;
        result.req = req;

        // expand recursively
        for (UseCase subUseCase : useCase.getSubUseCases()) {
            CrudUnit subCrudUnit = setupUseCaseInstance(subUseCase);
            result.subCrudUnits.add(subCrudUnit);
        }
        return result;
    }


    //**************************************************************************
    // Redirect to first use case
    //**************************************************************************

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


}
