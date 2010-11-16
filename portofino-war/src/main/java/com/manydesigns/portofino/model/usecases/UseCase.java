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

package com.manydesigns.portofino.model.usecases;

import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.ModelObject;
import com.manydesigns.portofino.model.annotations.ModelAnnotation;
import com.manydesigns.portofino.model.datamodel.Table;
import com.manydesigns.portofino.model.selectionproviders.ModelSelectionProvider;
import com.manydesigns.portofino.xml.XmlAttribute;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class UseCase implements ModelObject {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";
    

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected final UseCase parentUseCase;
    protected final List<UseCaseProperty> properties;
    protected final List<ModelSelectionProvider> modelSelectionProviders;
    protected final List<ModelAnnotation> modelAnnotations;
    protected final List<Button> buttons;
    protected final List<UseCase> subUseCases;

    protected String name;
    protected String table;
    protected String query;
    protected String searchTitle;
    protected String createTitle;
    protected String readTitle;
    protected String editTitle;

    //**************************************************************************
    // Fields for wire-up
    //**************************************************************************

    protected Table actualTable;

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public UseCase(UseCase parentUseCase) {
        this.parentUseCase = parentUseCase;
        properties = new ArrayList<UseCaseProperty>();
        modelSelectionProviders = new ArrayList<ModelSelectionProvider>();
        modelAnnotations = new ArrayList<ModelAnnotation>();
        buttons = new ArrayList<Button>();
        subUseCases = new ArrayList<UseCase>();
    }

    public UseCase(UseCase parentUseCase,
                   String name, String table, String query,
                   String searchTitle, String createTitle,
                   String readTitle, String editTitle) {
        this(parentUseCase);
        this.name = name;
        this.table = table;
        this.query = query;
        this.searchTitle = searchTitle;
        this.createTitle = createTitle;
        this.readTitle = readTitle;
        this.editTitle = editTitle;
    }

    //**************************************************************************
    // ModelObject implementation
    //**************************************************************************

    public void reset() {
        actualTable = null;

        for (UseCaseProperty property : properties) {
            property.reset();
        }

        for (ModelSelectionProvider modelSelectionProvider : modelSelectionProviders) {
            modelSelectionProvider.reset();
        }

        for (ModelAnnotation modelAnnotation : modelAnnotations) {
            modelAnnotation.reset();
        }

        for (Button button : buttons) {
            button.reset();
        }

        for (UseCase subUseCase : subUseCases) {
            subUseCase.reset();
        }
    }

    public void init(Model model) {
        actualTable = model.findTableByQualifiedName(table);

        for (UseCaseProperty property : properties) {
            property.init(model);
        }

        for (ModelSelectionProvider modelSelectionProvider : modelSelectionProviders) {
            modelSelectionProvider.init(model);
        }

        for (ModelAnnotation modelAnnotation : modelAnnotations) {
            modelAnnotation.init(model);
        }

        for (Button button : buttons) {
            button.init(model);
        }

        for (UseCase subUseCase : subUseCases) {
            subUseCase.init(model);
        }
    }

    public String getQualifiedName() {
        if (parentUseCase == null) {
            return name;
        } else {
            return MessageFormat.format("{0}.{1}",
                    parentUseCase.getQualifiedName(), name);
        }
    }


    //**************************************************************************
    // Getters/setters
    //**************************************************************************

    public UseCase getParentUseCase() {
        return parentUseCase;
    }

    public List<UseCaseProperty> getProperties() {
        return properties;
    }

    public List<ModelSelectionProvider> getModelSelectionProviders() {
        return modelSelectionProviders;
    }

    public List<Button> getButtons() {
        return buttons;
    }

    public List<ModelAnnotation> getModelAnnotations() {
        return modelAnnotations;
    }

    public List<UseCase> getSubUseCases() {
        return subUseCases;
    }


    @XmlAttribute(required = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlAttribute(required = true)
    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public Table getActualTable() {
        return actualTable;
    }

    public void setActualTable(Table actualTable) {
        this.actualTable = actualTable;
    }

    @XmlAttribute(required = true)
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    @XmlAttribute(required = false)
    public String getSearchTitle() {
        return searchTitle;
    }

    public void setSearchTitle(String searchTitle) {
        this.searchTitle = searchTitle;
    }

    @XmlAttribute(required = false)
    public String getCreateTitle() {
        return createTitle;
    }

    public void setCreateTitle(String createTitle) {
        this.createTitle = createTitle;
    }

    @XmlAttribute(required = false)
    public String getReadTitle() {
        return readTitle;
    }

    public void setReadTitle(String readTitle) {
        this.readTitle = readTitle;
    }

    @XmlAttribute(required = false)
    public String getEditTitle() {
        return editTitle;
    }

    public void setEditTitle(String editTitle) {
        this.editTitle = editTitle;
    }
}
