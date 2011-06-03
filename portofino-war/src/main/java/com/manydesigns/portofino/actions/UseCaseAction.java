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

import com.manydesigns.elements.annotations.ShortName;
import com.manydesigns.elements.options.DefaultSelectionProvider;
import com.manydesigns.elements.options.SelectionProvider;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.text.OgnlTextFormat;
import com.manydesigns.elements.text.TextFormat;
import com.manydesigns.portofino.annotations.InjectContext;
import com.manydesigns.portofino.annotations.InjectModel;
import com.manydesigns.portofino.annotations.InjectNavigation;
import com.manydesigns.portofino.context.Context;
import com.manydesigns.portofino.context.ModelObjectNotFoundError;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.datamodel.Table;
import com.manydesigns.portofino.model.selectionproviders.ModelSelectionProvider;
import com.manydesigns.portofino.model.selectionproviders.SelectionProperty;
import com.manydesigns.portofino.model.site.UseCaseNode;
import com.manydesigns.portofino.model.site.usecases.Button;
import com.manydesigns.portofino.model.site.usecases.UseCase;
import com.manydesigns.portofino.navigation.Navigation;
import com.manydesigns.portofino.reflection.TableAccessor;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class UseCaseAction extends AbstractCrudAction {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    @InjectContext
    public Context context;

    @InjectModel
    public Model model;

    @InjectNavigation
    public Navigation navigation;

    //**************************************************************************
    // Setup
    //**************************************************************************

    public void prepare() {
        if (qualifiedName == null) {
            return;
        }
        UseCase rootUseCase = ((UseCaseNode) navigation
                .getSelectedNavigationNode().getActualSiteNode()).getUseCase();
        if (rootUseCase == null) {
            throw new ModelObjectNotFoundError(qualifiedName);
        }
        rootCrudUnit = setupUseCaseInstance(rootUseCase, null, true);
    }

    private CrudUnit setupUseCaseInstance(UseCase useCase,
                                          String prefix, boolean first) {
        ClassAccessor classAccessor =
                    context.getUseCaseAccessor(useCase);
        Table baseTable = useCase.getActualTable();
        String query = useCase.getQuery();
        CrudUnit result = new CrudUnit(classAccessor, baseTable, query,
                useCase.getSearchTitle(), useCase.getCreateTitle(),
                useCase.getReadTitle(), useCase.getEditTitle(),
                useCase.getName(), prefix, first);
        for (Button button : useCase.getButtons()) {
            CrudButton crudButton = new CrudButton(button);
            result.crudButtons.add(crudButton);
        }

        // inject values
        result.context = context;
        result.model = model;
        result.req = req;

        // set up selection providers

        setupSelectionProviders(useCase, result);

        // expand recursively
        int index = 0;
        for (UseCase subUseCase : useCase.getSubUseCases()) {
            String tmp = MessageFormat.format("subCrudUnits[{0}].", index);
            String subPrefix;
            if (prefix == null) {
                subPrefix = tmp;
            } else {
                subPrefix = prefix + "." + tmp;
            }
            CrudUnit subCrudUnit = setupUseCaseInstance(subUseCase, subPrefix,
                    false);
            result.subCrudUnits.add(subCrudUnit);
            index++;
        }
        return result;
    }

    private void setupSelectionProviders(UseCase useCase, CrudUnit result) {
        for (ModelSelectionProvider current : useCase.getSelectionProviders()) {
            String name = current.getName();
            String database = current.getDatabase();
            String sql = current.getSql();
            String hql = current.getHql();
            List<SelectionProperty> selectionProperties =
                    current.getSelectionProperties();

            String[] fieldNames = new String[selectionProperties.size()];
            Class[] fieldTypes = new Class[selectionProperties.size()];

            int i = 0;
            for (SelectionProperty selectionProperty : selectionProperties) {
                try {
                    fieldNames[i] = selectionProperty.getName();
                    PropertyAccessor propertyAccessor =
                            result.classAccessor.getProperty(fieldNames[i]);
                    fieldTypes[i] = propertyAccessor.getType();
                    i++;
                } catch (NoSuchFieldException e) {
                    throw new Error(e);
                }
            }


            SelectionProvider selectionProvider;
            if (sql != null) {
                Collection<Object[]> objects = context.runSql(database, sql);
                selectionProvider = DefaultSelectionProvider.create(
                        name, fieldNames.length, fieldTypes, objects);
            } else if (hql != null) {
                Collection<Object> objects = context.getObjects(hql);
                String qualifiedTableName = 
                        context.getQualifiedTableNameFromQueryString(hql);
                TableAccessor tableAccessor =
                        context.getTableAccessor(qualifiedTableName);
                ShortName shortNameAnnotation =
                        tableAccessor.getAnnotation(ShortName.class);
                TextFormat[] textFormats = null;
                if (shortNameAnnotation != null) {
                    textFormats = new TextFormat[] {
                        OgnlTextFormat.create(shortNameAnnotation.value())
                    };
                }

                selectionProvider = DefaultSelectionProvider.create(
                        name, objects, tableAccessor, textFormats);
            } else {
                logger.warn("ModelSelection provider '{}':" +
                        " both 'hql' and 'sql' are null", name);
                break;
            }

            CrudSelectionProvider crudSelectionProvider =
                    new CrudSelectionProvider(selectionProvider, fieldNames);
            result.crudSelectionProviders.add(crudSelectionProvider);
        }
    }


    //**************************************************************************
    // Redirect to first use case -
    //**************************************************************************

    //TODO da eliminare
    @Override
    public String redirectToFirst() {
        return PortofinoAction.REDIRECT_TO_FIRST;
    }
}
