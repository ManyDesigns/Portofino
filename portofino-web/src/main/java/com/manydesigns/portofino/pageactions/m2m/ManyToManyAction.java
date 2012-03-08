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

package com.manydesigns.portofino.pageactions.m2m;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.Mode;
import com.manydesigns.elements.fields.SelectField;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.elements.ognl.OgnlUtils;
import com.manydesigns.elements.options.DefaultSelectionProvider;
import com.manydesigns.elements.options.DisplayMode;
import com.manydesigns.elements.options.SelectionProvider;
import com.manydesigns.elements.reflection.JavaClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.text.QueryStringWithParameters;
import com.manydesigns.portofino.application.QueryUtils;
import com.manydesigns.portofino.buttons.GuardType;
import com.manydesigns.portofino.buttons.annotations.Button;
import com.manydesigns.portofino.buttons.annotations.Guard;
import com.manydesigns.portofino.database.TableCriteria;
import com.manydesigns.portofino.dispatcher.PageInstance;
import com.manydesigns.portofino.logic.SelectionProviderLogic;
import com.manydesigns.portofino.model.database.Database;
import com.manydesigns.portofino.model.database.Table;
import com.manydesigns.portofino.pageactions.AbstractPageAction;
import com.manydesigns.portofino.pageactions.PageActionName;
import com.manydesigns.portofino.pageactions.annotations.ConfigurationClass;
import com.manydesigns.portofino.pageactions.annotations.ScriptTemplate;
import com.manydesigns.portofino.pageactions.m2m.configuration.ManyToManyConfiguration;
import com.manydesigns.portofino.pageactions.m2m.configuration.ViewType;
import com.manydesigns.portofino.reflection.TableAccessor;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.security.RequiresPermissions;
import com.manydesigns.portofino.util.PkHelper;
import net.sourceforge.stripes.action.*;
import ognl.OgnlContext;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;

import java.io.Serializable;
import java.util.*;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@UrlBinding("/actions/m2m")
@RequiresPermissions(level = AccessLevel.VIEW)
@ScriptTemplate("script_template.txt")
@ConfigurationClass(ManyToManyConfiguration.class)
@PageActionName("Many-to-Many")
public class ManyToManyAction extends AbstractPageAction {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    protected ManyToManyConfiguration m2mConfiguration;

    protected Serializable onePk;

    protected List existingAssociations;
    protected List availableAssociations;
    protected List potentiallyAvailableAssociations;

    protected TableAccessor relationTableAccessor;
    protected TableAccessor manyTableAccessor;

    protected SelectField oneSelectField;

    //Checkboxes view
    protected Map<Object, Boolean> booleanRelation;
    protected List<String> selectedPrimaryKeys = new ArrayList<String>();

    //Configuration
    protected Form configurationForm;

    public Resolution prepare(PageInstance pageInstance, ActionBeanContext context) {
        this.pageInstance = pageInstance;
        if(!pageInstance.getParameters().isEmpty()) {
            return new ErrorResolution(404);
        }
        m2mConfiguration = (ManyToManyConfiguration) pageInstance.getConfiguration();
        return null;
    }

    @Before
    public void prepare() throws NoSuchFieldException {
        Table table = m2mConfiguration.getActualRelationTable();
        relationTableAccessor = new TableAccessor(table);
        manyTableAccessor = new TableAccessor(m2mConfiguration.getActualManyTable());
        String expression = m2mConfiguration.getOneExpression();
        if(!StringUtils.isBlank(expression)) {
            //Set primary key
            OgnlContext ognlContext = ElementsThreadLocals.getOgnlContext();
            onePk = (Serializable) OgnlUtils.getValueQuietly(expression, ognlContext, this); //TODO handle exception
        } else {
            //Setup "one" selection
            if(!StringUtils.isBlank(m2mConfiguration.getOneQuery())) {
                JavaClassAccessor myselfAccessor = JavaClassAccessor.getClassAccessor(getClass());
                String databaseName = m2mConfiguration.getActualOneDatabase().getDatabaseName();
                SelectionProvider selectionProvider =
                        SelectionProviderLogic.createSelectionProviderFromHql
                                ("onePk", application, databaseName, m2mConfiguration.getOneQuery(),
                                 DisplayMode.DROPDOWN);
                oneSelectField =
                        new SelectField(myselfAccessor.getProperty("onePk"), selectionProvider, Mode.EDIT, "__");
                oneSelectField.readFromObject(this);
                oneSelectField.readFromObject(this);
                oneSelectField.readFromRequest(context.getRequest());
                oneSelectField.writeToObject(this);
            }
        }
    }

    @DefaultHandler
    public Resolution execute() throws NoSuchFieldException { //TODO
        if(onePk != null) {
            loadAssociations();
            if(potentiallyAvailableAssociations == null && onePk != null) {
                return forwardToPortletNotConfigured(); //TODO
            }
        }
        return view();
    }

    protected Resolution view() {
        switch (m2mConfiguration.getActualViewType()) {
            case CHECKBOXES:
                booleanRelation = new LinkedHashMap<Object, Boolean>();
                if(potentiallyAvailableAssociations != null) {
                    for(Object o : potentiallyAvailableAssociations) {
                        booleanRelation.put(o, !availableAssociations.contains(o));
                    }
                }
                return forwardTo("/layouts/m2m/checkboxes.jsp");
            default:
                return forwardToPortletNotConfigured(); //TODO
        }
    }

    protected void loadAssociations() throws NoSuchFieldException {
        Table table = m2mConfiguration.getActualRelationTable();
        TableCriteria criteria = new TableCriteria(table);
        PropertyAccessor onePropertyAccessor =
                relationTableAccessor.getProperty(m2mConfiguration.getOnePropertyName());
        PropertyAccessor manyPropertyAccessor =
                relationTableAccessor.getProperty(m2mConfiguration.getManyPropertyName());
        criteria = criteria.eq(onePropertyAccessor, onePk);
        QueryStringWithParameters queryString =
                QueryUtils.mergeQuery(m2mConfiguration.getRelationQuery(), criteria, this);
        Session session = application.getSession(m2mConfiguration.getRelationDatabase());
        existingAssociations =
                QueryUtils.runHqlQuery(session, queryString.getQueryString(), queryString.getParameters());
        availableAssociations = new ArrayList<Object>();
        potentiallyAvailableAssociations =
                QueryUtils.runHqlQuery(session, m2mConfiguration.getManyQuery(), null);
        PropertyAccessor[] manyKeyProperties = manyTableAccessor.getKeyProperties();
        //TODO handle manyKeyProperties.length > 1
        PropertyAccessor manyPkAccessor = manyTableAccessor.getProperty(manyKeyProperties[0].getName());
        for(Object o : potentiallyAvailableAssociations) {
            Object oPk = manyPkAccessor.get(o);
            boolean existing = isExistingAssociation(manyPropertyAccessor, oPk);
            if(!existing) {
                availableAssociations.add(o);
            }
        }
    }

    private boolean isExistingAssociation(PropertyAccessor manyPropertyAccessor, Object oPk) {
        boolean existing = false;
        for(Object a : existingAssociations) {
            if(oPk.equals(manyPropertyAccessor.get(a))) {
                existing = true;
                break;
            }
        }
        return existing;
    }

    @Button(list = "m2m-checkboxes-edit", key = "commons.update")
    @Guard(test = "onePk != null", type = GuardType.VISIBLE)
    public Resolution saveCheckboxes() throws Exception {
        loadAssociations();
        PkHelper pkHelper = new PkHelper(manyTableAccessor);
        PropertyAccessor onePropertyAccessor =
                relationTableAccessor.getProperty(m2mConfiguration.getOnePropertyName());
        PropertyAccessor manyPropertyAccessor =
                relationTableAccessor.getProperty(m2mConfiguration.getManyPropertyName());
        Session session = application.getSession(m2mConfiguration.getActualRelationDatabase().getDatabaseName());
        PropertyAccessor[] manyKeyProperties = manyTableAccessor.getKeyProperties();
        //TODO handle manyKeyProperties.length > 1
        PropertyAccessor manyPkAccessor = manyTableAccessor.getProperty(manyKeyProperties[0].getName());
        for(String pkString : selectedPrimaryKeys) {
            Serializable pkObject = pkHelper.getPrimaryKey(pkString.split("/"));
            Object pk = manyPkAccessor.get(pkObject);
            if(!isExistingAssociation(manyPropertyAccessor, pk)) {
                Object newRelation = relationTableAccessor.newInstance();
                onePropertyAccessor.set(newRelation, onePk);
                manyPropertyAccessor.set(newRelation, pk);
                prepareSave(newRelation);
                session.save(m2mConfiguration.getActualRelationTable().getActualEntityName(), newRelation);
                existingAssociations.add(newRelation);
            }
        }
        Iterator it = existingAssociations.iterator();
        while(it.hasNext()) {
            Object o = it.next();
            //TODO handle manyKeyProperties.length > 1
            Object pkObject = manyPropertyAccessor.get(o);
            String pkString =
                    (String) OgnlUtils.convertValue(pkObject, String.class);
            //String pkString = StringUtils.join(pkHelper.generatePkStringArray(o), "/");
            if(!selectedPrimaryKeys.contains(pkString)) {
                session.delete(m2mConfiguration.getActualRelationTable().getActualEntityName(), o);
                it.remove();
            }
        }
        session.getTransaction().commit();
        SessionMessages.addInfoMessage(getMessage("commons.update.successful"));
        if(oneSelectField != null) {
            session.beginTransaction();
            loadAssociations();
            return view();
        } else {
            return cancel();
        }
    }

    //Configuration

    @Button(list = "portletHeaderButtons", key = "commons.configure", order = 1, icon = "ui-icon-wrench")
    @RequiresPermissions(level = AccessLevel.DEVELOP)
    public Resolution configure() {
        prepareConfigurationForms();
        return new ForwardResolution("/layouts/m2m/configure.jsp");
    }

    @Button(list = "configuration", key = "commons.updateConfiguration")
    @RequiresPermissions(level = AccessLevel.DEVELOP)
    public Resolution updateConfiguration() {
        prepareConfigurationForms();
        readPageConfigurationFromRequest();
        configurationForm.readFromRequest(context.getRequest());
        boolean valid = validatePageConfiguration();
        valid = configurationForm.validate() && valid;
        if(valid) {
            updatePageConfiguration();
            configurationForm.writeToObject(m2mConfiguration);
            saveConfiguration(m2mConfiguration);
            SessionMessages.addInfoMessage(getMessage("commons.configuration.updated"));
        }
        return cancel();
    }

    @Override
    protected void prepareConfigurationForms() {
        super.prepareConfigurationForms();
        FormBuilder formBuilder = new FormBuilder(ManyToManyConfiguration.class);
        String general = "General";
        String oneSide = "One- side of the relation";
        String manySide = "-Many side of the relation";
        formBuilder.configFieldSetNames(general, oneSide, manySide);
        formBuilder.configFields(
                new String[] { "viewType", "relationDatabase", "relationQuery" },
                new String[] { "onePropertyName", "oneExpression", "oneDatabase", "oneQuery" },
                new String[] { "manyPropertyName", "manyDatabase", "manyQuery" });

        DefaultSelectionProvider viewTypeSelectionProvider = new DefaultSelectionProvider("viewType");
        String label = getMessage("com.manydesigns.portofino.pageactions.m2m.configuration.ViewType.CHECKBOXES");
        viewTypeSelectionProvider.appendRow(ViewType.CHECKBOXES.name(), label, true);
        label = getMessage("com.manydesigns.portofino.pageactions.m2m.configuration.ViewType.LISTS");
        viewTypeSelectionProvider.appendRow(ViewType.LISTS.name(), label, true);
        formBuilder.configSelectionProvider(viewTypeSelectionProvider, "viewType");

        addDatabaseSelectionProvider(formBuilder, "oneDatabase");
        addDatabaseSelectionProvider(formBuilder, "manyDatabase");
        addDatabaseSelectionProvider(formBuilder, "relationDatabase");
        configurationForm = formBuilder.build();
        configurationForm.readFromObject(m2mConfiguration);
    }

    protected void addDatabaseSelectionProvider(FormBuilder formBuilder, String propertyName) {
        SelectionProvider databaseSelectionProvider =
                SelectionProviderLogic.createSelectionProvider(
                        propertyName,
                        model.getDatabases(),
                        Database.class,
                        null,
                        new String[]{ "databaseName" });
        formBuilder.configSelectionProvider(databaseSelectionProvider, propertyName);
    }

    //Extension hooks

    protected void prepareSave(Object newRelation) {}

    //Getters/Setters

    public Serializable getOnePk() {
        return onePk;
    }

    public void setOnePk(Serializable onePk) {
        this.onePk = onePk;
    }

    public ManyToManyConfiguration getConfiguration() {
        return m2mConfiguration;
    }

    public List<?> getExistingAssociations() {
        return existingAssociations;
    }

    public List<?> getAvailableAssociations() {
        return availableAssociations;
    }

    public TableAccessor getRelationTableAccessor() {
        return relationTableAccessor;
    }

    public TableAccessor getManyTableAccessor() {
        return manyTableAccessor;
    }

    public Map<Object, Boolean> getBooleanRelation() {
        return booleanRelation;
    }

    public List<String> getSelectedPrimaryKeys() {
        return selectedPrimaryKeys;
    }

    public void setSelectedPrimaryKeys(List<String> selectedPrimaryKeys) {
        this.selectedPrimaryKeys = selectedPrimaryKeys;
    }

    public Form getConfigurationForm() {
        return configurationForm;
    }

    public SelectField getOneSelectField() {
        return oneSelectField;
    }
}
