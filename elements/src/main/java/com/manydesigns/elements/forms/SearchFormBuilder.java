/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.elements.forms;

import com.manydesigns.elements.annotations.Searchable;
import com.manydesigns.elements.fields.helpers.FieldsManager;
import com.manydesigns.elements.fields.search.SearchField;
import com.manydesigns.elements.fields.search.SelectSearchField;
import com.manydesigns.elements.options.SelectionModel;
import com.manydesigns.elements.options.SelectionProvider;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.JavaClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import org.apache.commons.lang.ArrayUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class SearchFormBuilder extends AbstractFormBuilder {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected List<PropertyAccessor> propertyAccessors;

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public SearchFormBuilder(Class aClass) {
        this(JavaClassAccessor.getClassAccessor(aClass));
    }

    public SearchFormBuilder(ClassAccessor classAccessor) {
        super(classAccessor);
    }

    //**************************************************************************
    // Builder configuration
    //**************************************************************************

    public SearchFormBuilder configFields(String... fieldNames) {
        propertyAccessors = new ArrayList<PropertyAccessor>();
        for (String current : fieldNames) {
            try {
                PropertyAccessor accessor =
                        classAccessor.getProperty(current);
                propertyAccessors.add(accessor);
            } catch (NoSuchFieldException e) {
                logger.warn("Field not found: " + current, e);
            }
        }

        return this;
    }

    public SearchFormBuilder configPrefix(String prefix) {
        logger.debug("prefix = {}", prefix);

        this.prefix = prefix;
        return this;
    }

    public SearchFormBuilder configSelectionProvider(SelectionProvider selectionProvider,
                                            String... fieldNames) {
        selectionProviders.put(fieldNames, selectionProvider);
        return this;
    }


    public SearchFormBuilder configReflectiveFields() {
        logger.debug("configReflectiveFields");

        propertyAccessors = new ArrayList<PropertyAccessor>();

        for (PropertyAccessor current : classAccessor.getProperties()) {
            if (skippableProperty(current)) {
                continue;
            }

            if(!isPropertyEnabled(current)) {
                continue;
            }

            // check if field is searchable
            Searchable searchableAnnotation = current.getAnnotation(Searchable.class);
            if (searchableAnnotation != null && !searchableAnnotation.value()) {
                logger.debug("Skipping non-searchable field: {}",
                        current.getName());
                continue;
            }

            propertyAccessors.add(current);
        }

        return this;
    }

    //**************************************************************************
    // Building
    //**************************************************************************

    public SearchForm build() {
        logger.debug("build");

        SearchForm searchForm = new SearchForm();
        FieldsManager manager = FieldsManager.getManager();

        if (propertyAccessors == null) {
            configReflectiveFields();
        }


        Map<String, SearchField> fieldMap = new HashMap<String,SearchField>();
        for (PropertyAccessor propertyAccessor : propertyAccessors) {
            SearchField field = null;
            String fieldName = propertyAccessor.getName();
            
            for (Map.Entry<String[], SelectionProvider> current
                : selectionProviders.entrySet()) {

                String[] fieldNames = current.getKey();
                int index = ArrayUtils.indexOf(fieldNames, fieldName);
                if (index >= 0) {
                    field = new SelectSearchField(propertyAccessor, prefix);
                    
                    break;
                }
            }

            if (field == null) {
                field = manager.tryToInstantiateSearchField(
                    classAccessor, propertyAccessor, prefix);
            }

            if (field == null) {
                logger.warn("Cannot instanciate field for property {}",
                        propertyAccessor);
                continue;
            }
            fieldMap.put(fieldName, field);
            searchForm.add(field);
        }

         // handle cascaded select fields
        for (Map.Entry<String[], SelectionProvider> current :
                selectionProviders.entrySet()) {
            String[] fieldNames = current.getKey();
            SelectionProvider selectionProvider = current.getValue();
            SelectionModel selectionModel =
                    selectionProvider.createSelectionModel();

            SelectSearchField previousField = null;
            for (int i = 0; i < fieldNames.length; i++) {
                SelectSearchField selectSearchField =
                        (SelectSearchField)fieldMap.get(fieldNames[i]);
                if(selectSearchField == null) {
                    previousField = null;
                    continue;
                }
                selectSearchField.setSelectionModel(selectionModel);
                selectSearchField.setSelectionModelIndex(i);
                if (previousField != null) {
                    selectSearchField.setPreviousSelectField(previousField);
                    previousField.setNextSelectField(selectSearchField);
                }
                previousField = selectSearchField;
            }
        }

        return searchForm;
    }
}
