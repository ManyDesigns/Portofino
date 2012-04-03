/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
import com.manydesigns.portofino.model.database.*;
import com.manydesigns.portofino.pageactions.AbstractPageAction;
import com.manydesigns.portofino.pageactions.PageActionName;
import com.manydesigns.portofino.pageactions.annotations.ConfigurationClass;
import com.manydesigns.portofino.pageactions.annotations.ScriptTemplate;
import com.manydesigns.portofino.pageactions.m2m.configuration.ManyToManyConfiguration;
import com.manydesigns.portofino.pageactions.m2m.configuration.SelectionProviderReference;
import com.manydesigns.portofino.pageactions.m2m.configuration.ViewType;
import com.manydesigns.portofino.reflection.TableAccessor;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.security.RequiresPermissions;
import com.manydesigns.portofino.util.PkHelper;
import net.sourceforge.stripes.action.*;
import ognl.OgnlContext;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
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

    protected boolean correctlyConfigured;

    //Checkboxes view
    protected Map<Object, Boolean> booleanRelation;
    protected List<String> selectedPrimaryKeys = new ArrayList<String>();

    //Configuration
    protected Form configurationForm;

    //Logging
    private  static final Logger logger = LoggerFactory.getLogger(ManyToManyAction.class);

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
        if(m2mConfiguration == null || m2mConfiguration.getActualRelationTable() == null ||
           m2mConfiguration.getActualManyTable() == null) {
            logger.error("Configuration is null or relation/many table not found (check previous log messages)");
            return;
        }
        Table table = m2mConfiguration.getActualRelationTable();
        relationTableAccessor = new TableAccessor(table);
        manyTableAccessor = new TableAccessor(m2mConfiguration.getActualManyTable());
        if(StringUtils.isBlank(m2mConfiguration.getActualOnePropertyName())) {
            logger.error("One property name not set");
            return;
        }

        String expression = m2mConfiguration.getOneExpression();
        if(!StringUtils.isBlank(expression)) {
            //Set primary key
            OgnlContext ognlContext = ElementsThreadLocals.getOgnlContext();
            onePk = (Serializable) OgnlUtils.getValueQuietly(expression, ognlContext, this); //TODO handle exception
            correctlyConfigured = true;
        } else {
            //Setup "one" selection
            SelectionProviderReference oneSelectionProvider = m2mConfiguration.getOneSelectionProvider();
            if(oneSelectionProvider != null) {
                ModelSelectionProvider actualSelectionProvider = oneSelectionProvider.getActualSelectionProvider();
                if(!(actualSelectionProvider instanceof DatabaseSelectionProvider)) {
                    logger.warn("Selection provider {} not supported", actualSelectionProvider);
                    return;
                }
                JavaClassAccessor myselfAccessor = JavaClassAccessor.getClassAccessor(getClass());
                String databaseName = m2mConfiguration.getActualOneDatabase().getDatabaseName();

                DatabaseSelectionProvider sp =
                        (DatabaseSelectionProvider) actualSelectionProvider;

                DefaultSelectionProvider selectionProvider;
                String name = sp.getName();
                String hql = sp.getHql();

                if (hql != null) {
                    selectionProvider =
                            SelectionProviderLogic.createSelectionProviderFromHql
                                    (name, application, databaseName, hql, DisplayMode.DROPDOWN);

                    if(sp instanceof ForeignKey) {
                        selectionProvider.sortByLabel();
                    }
                } else {
                    logger.warn("ModelSelection provider '{}': unsupported query", name);
                    return;
                }
                oneSelectField =
                        new SelectField(myselfAccessor.getProperty("onePk"), selectionProvider, Mode.EDIT, "__");
                oneSelectField.readFromObject(this);
                oneSelectField.readFromObject(this);
                oneSelectField.readFromRequest(context.getRequest());
                oneSelectField.writeToObject(this);
                correctlyConfigured = true;
            }
        }
    }

    @DefaultHandler
    public Resolution execute() {
        if(!correctlyConfigured) {
            return forwardToPortletNotConfigured();
        }
        if(onePk != null) {
            try {
                loadAssociations();
                if(potentiallyAvailableAssociations == null && onePk != null) {
                    return forwardToPortletNotConfigured(); //TODO
                }
            } catch (NoSuchFieldException e) {
                return forwardToPortletNotConfigured();
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
        //TODO chiave multipla
        String onePropertyName = m2mConfiguration.getActualOnePropertyName();
        PropertyAccessor onePropertyAccessor =
                relationTableAccessor.getProperty(onePropertyName);
        //TODO chiave multipla
        SelectionProviderReference manySelectionProvider = m2mConfiguration.getManySelectionProvider();
        String manyPropertyName = manySelectionProvider.getActualSelectionProvider().getReferences().get(0).getActualFromColumn().getActualPropertyName();
        PropertyAccessor manyPropertyAccessor =
                relationTableAccessor.getProperty(manyPropertyName);
        criteria = criteria.eq(onePropertyAccessor, onePk);
        QueryStringWithParameters queryString =
                QueryUtils.mergeQuery(m2mConfiguration.getQuery(), criteria, this);
        Session session = application.getSession(m2mConfiguration.getDatabase());
        existingAssociations =
                QueryUtils.runHqlQuery(session, queryString.getQueryString(), queryString.getParameters());
        availableAssociations = new ArrayList<Object>();
        String manyQuery = ((DatabaseSelectionProvider) manySelectionProvider.getActualSelectionProvider()).getHql();
        if(manyQuery == null) {
            logger.error("Couldn't determine many query");
            return;
        }
        potentiallyAvailableAssociations =
                QueryUtils.runHqlQuery(session, manyQuery, null);
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
        if(!correctlyConfigured) {
            return forwardToPortletNotConfigured();
        }
        loadAssociations();
        PkHelper pkHelper = new PkHelper(manyTableAccessor);
        //TODO chiave multipla
        String onePropertyName = m2mConfiguration.getOneSelectionProvider().getActualSelectionProvider().getReferences().get(0).getActualFromColumn().getActualPropertyName();
        PropertyAccessor onePropertyAccessor =
                relationTableAccessor.getProperty(onePropertyName);
        //TODO chiave multipla
        String manyPropertyName = m2mConfiguration.getManySelectionProvider().getActualSelectionProvider().getReferences().get(0).getActualFromColumn().getActualPropertyName();
        PropertyAccessor manyPropertyAccessor =
                relationTableAccessor.getProperty(manyPropertyName);
        Session session = application.getSession(m2mConfiguration.getActualRelationDatabase().getDatabaseName());
        PropertyAccessor[] manyKeyProperties = manyTableAccessor.getKeyProperties();
        //TODO handle manyKeyProperties.length > 1
        PropertyAccessor manyPkAccessor = manyTableAccessor.getProperty(manyKeyProperties[0].getName());
        for(String pkString : selectedPrimaryKeys) {
            Serializable pkObject = pkHelper.getPrimaryKey(pkString.split("/"));
            Object pk = manyPkAccessor.get(pkObject);
            if(!isExistingAssociation(manyPropertyAccessor, pk)) {
                Object newRelation = saveNewRelation(session, pk, onePropertyAccessor, manyPropertyAccessor);
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
            if(!selectedPrimaryKeys.contains(pkString)) {
                deleteRelation(session, o);
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

    protected void deleteRelation(Session session, Object rel) {
        session.delete(m2mConfiguration.getActualRelationTable().getActualEntityName(), rel);
    }

    protected Object saveNewRelation(Session session, Object pk, PropertyAccessor onePropertyAccessor, PropertyAccessor manyPropertyAccessor) {
        Object newRelation = relationTableAccessor.newInstance();
        onePropertyAccessor.set(newRelation, onePk);
        manyPropertyAccessor.set(newRelation, pk);
        prepareSave(newRelation);
        session.save(m2mConfiguration.getActualRelationTable().getActualEntityName(), newRelation);
        return newRelation;
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
        ConfigurationForm conf = new ConfigurationForm(m2mConfiguration);
        configurationForm.readFromObject(conf);
        readPageConfigurationFromRequest();
        configurationForm.readFromRequest(context.getRequest());
        boolean valid = validatePageConfiguration();
        valid = configurationForm.validate() && valid;
        if(valid) {
            updatePageConfiguration();
            configurationForm.writeToObject(conf);
            conf.writeTo(m2mConfiguration);
            saveConfiguration(m2mConfiguration);
            SessionMessages.addInfoMessage(getMessage("commons.configuration.updated"));
            return cancel();
        } else {
            SessionMessages.addErrorMessage(getMessage("commons.configuration.notUpdated"));
            return new ForwardResolution("/layouts/m2m/configure.jsp");
        }
    }

    @Override
    protected void prepareConfigurationForms() {
        super.prepareConfigurationForms();
        FormBuilder formBuilder = new FormBuilder(ConfigurationForm.class);
        formBuilder
            .configFields("viewType", "database", "query", "oneExpression", "onePropertyName", "oneSpName", "manySpName")
            .configFieldSetNames("Many to many");

        DefaultSelectionProvider viewTypeSelectionProvider = new DefaultSelectionProvider("viewType");
        String label = getMessage("com.manydesigns.portofino.pageactions.m2m.configuration.ViewType.CHECKBOXES");
        viewTypeSelectionProvider.appendRow(ViewType.CHECKBOXES.name(), label, true);
        label = getMessage("com.manydesigns.portofino.pageactions.m2m.configuration.ViewType.LISTS");
        viewTypeSelectionProvider.appendRow(ViewType.LISTS.name(), label, true);
        formBuilder.configSelectionProvider(viewTypeSelectionProvider, "viewType");

        SelectionProvider databaseSelectionProvider =
                SelectionProviderLogic.createSelectionProvider(
                        "database",
                        model.getDatabases(),
                        Database.class,
                        null,
                        new String[]{ "databaseName" });
        formBuilder.configSelectionProvider(databaseSelectionProvider, "database");

        if(m2mConfiguration != null && m2mConfiguration.getActualRelationTable() != null) {
            List<ModelSelectionProvider> sps = new ArrayList<ModelSelectionProvider>();
            sps.addAll(m2mConfiguration.getActualRelationTable().getForeignKeys());
            sps.addAll(m2mConfiguration.getActualRelationTable().getSelectionProviders());

            SelectionProvider oneSp =
                    SelectionProviderLogic.createSelectionProvider(
                            "oneSpName",
                            sps,
                            ModelSelectionProvider.class,
                            null,
                            new String[]{ "name" });
            formBuilder.configSelectionProvider(oneSp, "oneSpName");
            SelectionProvider manySp =
                    SelectionProviderLogic.createSelectionProvider(
                            "manySpName",
                            sps,
                            ModelSelectionProvider.class,
                            null,
                            new String[]{ "name" });
            formBuilder.configSelectionProvider(manySp, "manySpName");
        }

        configurationForm = formBuilder.build();
        configurationForm.readFromObject(new ConfigurationForm(m2mConfiguration));
    }

    protected void addSpRefSelectionProvider
            (final List<ModelSelectionProvider> sps, Form form, String fieldName)
            throws NoSuchFieldException {
        SelectionProvider sp =
                SelectionProviderLogic.createSelectionProvider(
                        fieldName,
                        sps,
                        ModelSelectionProvider.class,
                        null,
                        new String[]{ "name" });
        JavaClassAccessor acc = JavaClassAccessor.getClassAccessor(ManyToManyConfiguration.class);
        form.get(0).add(new SelectField(acc.getProperty(fieldName), sp, Mode.EDIT, "") {
            @Override
            public void readFromRequest(HttpServletRequest req) {
                String stringValue = req.getParameter(inputName);
                if(!StringUtils.isEmpty(stringValue)) {
                    for(ModelSelectionProvider msp : sps) {
                        if(msp.getName().equals(stringValue)) {
                            SelectionProviderReference ref = new SelectionProviderReference();
                            if(msp instanceof ForeignKey) {
                                ref.setForeignKeyName(msp.getName());
                            } else {
                                ref.setSelectionProviderName(msp.getName());
                            }
                            selectionModel.setValue(selectionModelIndex, ref);
                        }
                    }
                } else {
                    selectionModel.setValue(selectionModelIndex, null);
                }
            }
        });
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
