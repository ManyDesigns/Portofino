/*
 * Copyright (C) 2005-2019 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.manydesigns.portofino.resourceactions.m2m;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.Mode;
import com.manydesigns.elements.annotations.ShortName;
import com.manydesigns.elements.fields.SelectField;
import com.manydesigns.elements.forms.Form;
import com.manydesigns.elements.forms.FormBuilder;
import com.manydesigns.elements.messages.RequestMessages;
import com.manydesigns.elements.ognl.OgnlUtils;
import com.manydesigns.elements.options.*;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.JavaClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.text.OgnlTextFormat;
import com.manydesigns.elements.text.QueryStringWithParameters;
import com.manydesigns.elements.text.TextFormat;
import com.manydesigns.elements.util.MimeTypes;
import com.manydesigns.portofino.database.TableCriteria;
import com.manydesigns.portofino.logic.SelectionProviderLogic;
import com.manydesigns.portofino.model.database.*;
import com.manydesigns.portofino.resourceactions.AbstractResourceAction;
import com.manydesigns.portofino.resourceactions.ResourceActionName;
import com.manydesigns.portofino.resourceactions.annotations.ConfigurationClass;
import com.manydesigns.portofino.resourceactions.annotations.ScriptTemplate;
import com.manydesigns.portofino.resourceactions.m2m.configuration.ManyToManyConfiguration;
import com.manydesigns.portofino.resourceactions.m2m.configuration.SelectionProviderReference;
import com.manydesigns.portofino.resourceactions.m2m.configuration.ViewType;
import com.manydesigns.portofino.persistence.Persistence;
import com.manydesigns.portofino.persistence.QueryUtils;
import com.manydesigns.portofino.reflection.TableAccessor;
import com.manydesigns.portofino.security.AccessLevel;
import com.manydesigns.portofino.security.RequiresPermissions;
import com.manydesigns.portofino.security.SecurityLogic;
import com.manydesigns.portofino.security.SupportsPermissions;
import com.manydesigns.portofino.util.PkHelper;
import com.manydesigns.portofino.util.ShortNameUtils;
import ognl.OgnlContext;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.hibernate.Session;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.*;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@RequiresPermissions(level = AccessLevel.VIEW)
@ScriptTemplate("script_template.groovy")
@ConfigurationClass(ManyToManyConfiguration.class)
@ResourceActionName("Many-to-Many")
@SupportsPermissions(ManyToManyAction.PERMISSION_UPDATE)
public class ManyToManyAction extends AbstractResourceAction {
    public static final String copyright =
            "Copyright (C) 2005-2019 ManyDesigns srl";

    public static final String PERMISSION_UPDATE = "m2m-update";

    protected ManyToManyConfiguration m2mConfiguration;

    protected Object onePk;

    protected List existingAssociations;
    protected List availableAssociations;
    protected List potentiallyAvailableAssociations;

    protected TableAccessor relationTableAccessor;
    protected TableAccessor manyTableAccessor;

    protected SelectField oneSelectField;

    protected boolean correctlyConfigured;

    protected Session session;

    //Checkboxes view
    protected Map<Object, Boolean> booleanRelation;
    protected List<String> selectedPrimaryKeys = new ArrayList<String>();

    //Configuration
    protected Form configurationForm;

    //Logging
    private  static final Logger logger = LoggerFactory.getLogger(ManyToManyAction.class);

    @Autowired
    public Persistence persistence;

    public Response preparePage() {
        if(!parameters.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        m2mConfiguration = (ManyToManyConfiguration) actionInstance.getConfiguration();
        if(m2mConfiguration != null && m2mConfiguration.getActualRelationDatabase() != null) {
            String databaseName = m2mConfiguration.getActualRelationDatabase().getDatabaseName();
            session = persistence.getSession(databaseName);
        }
        return null;
    }

    @Override
    public Object init() {

        preparePage();

        if(m2mConfiguration == null || m2mConfiguration.getActualRelationTable() == null ||
           m2mConfiguration.getActualManyTable() == null) {
            logger.error("Configuration is null or relation/many table not found (check previous log messages)");
            return this; //TODO WebApplicationException instead?
        }
        Table table = m2mConfiguration.getActualRelationTable();
        relationTableAccessor = new TableAccessor(table);
        manyTableAccessor = new TableAccessor(m2mConfiguration.getActualManyTable());
        if(StringUtils.isBlank(m2mConfiguration.getActualOnePropertyName())) {
            logger.error("One property name not set");
            return this;
        }

        String expression = m2mConfiguration.getOneExpression();
        if(!StringUtils.isBlank(expression)) {
            //Set primary key
            OgnlContext ognlContext = ElementsThreadLocals.getOgnlContext();
            onePk = OgnlUtils.getValueQuietly(expression, ognlContext, this); //TODO handle exception
            correctlyConfigured = true;
        } else {
            assert !StringUtils.isBlank(m2mConfiguration.getActualOnePropertyName());
            //Setup "one" selection
            SelectionProviderReference oneSelectionProvider = m2mConfiguration.getOneSelectionProvider();
            if(oneSelectionProvider != null) {
                ModelSelectionProvider actualSelectionProvider = oneSelectionProvider.getActualSelectionProvider();
                if(!(actualSelectionProvider instanceof DatabaseSelectionProvider)) {
                    logger.warn("Selection provider {} not supported", actualSelectionProvider);
                    return this;
                }

                TableAccessor tableAccessor = new TableAccessor(m2mConfiguration.getActualRelationTable());
                PropertyAccessor onePkAccessor = null;
                try {
                    onePkAccessor = tableAccessor.getProperty(m2mConfiguration.getActualOnePropertyName());
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }

                if(onePkAccessor == null) {
                    logger.warn("Not a property: {}", m2mConfiguration.getActualOnePropertyName());
                    return this;
                }

                String databaseName = m2mConfiguration.getActualOneDatabase().getDatabaseName();

                DatabaseSelectionProvider sp =
                        (DatabaseSelectionProvider) actualSelectionProvider;

                DefaultSelectionProvider selectionProvider;
                String name = sp.getName();
                String hql = sp.getHql();

                if (StringUtils.isNotEmpty(hql)) {
                    selectionProvider =
                            createSelectionProviderFromHql
                                    (name, databaseName, hql, DisplayMode.DROPDOWN, SearchDisplayMode.DROPDOWN);

                    if(sp instanceof ForeignKey) {
                        selectionProvider.sortByLabel();
                    }
                } else {
                    logger.warn("ModelSelection provider '{}': unsupported query", name);
                    return this;
                }

                Object myInstance = tableAccessor.newInstance();
                oneSelectField =
                        new SelectField(onePkAccessor, selectionProvider, Mode.EDIT, "__");
                oneSelectField.setRequired(false);
                oneSelectField.readFromRequest(context.getRequest());
                oneSelectField.writeToObject(myInstance);
                onePk = onePkAccessor.get(myInstance);
                correctlyConfigured = true;
            }
        }
        return this;
    }

    public DefaultSelectionProvider createSelectionProviderFromHql
            (String name, String databaseName,
             String hql, DisplayMode dm, SearchDisplayMode sdm) {
        Database database = DatabaseLogic.findDatabaseByName(persistence.getModel(), databaseName);
        Table table = QueryUtils.getTableFromQueryString(database, hql);
        String entityName = table.getActualEntityName();
        Session session = persistence.getSession(databaseName);
        Collection<Object> objects = QueryUtils.getObjects(session, hql, null, null);
        TableAccessor tableAccessor =
                persistence.getTableAccessor(databaseName, entityName);
        ShortName shortNameAnnotation =
                tableAccessor.getAnnotation(ShortName.class);
        TextFormat[] textFormats = null;
        //L'ordinamento e' usato solo in caso di chiave singola
        if (shortNameAnnotation != null && tableAccessor.getKeyProperties().length == 1) {
            textFormats = new TextFormat[] {
                OgnlTextFormat.create(shortNameAnnotation.value())
            };
        }

        DefaultSelectionProvider selectionProvider = SelectionProviderLogic.createSelectionProvider
                (name, objects, tableAccessor.getKeyProperties(), textFormats);
        selectionProvider.setDisplayMode(dm);
        selectionProvider.setSearchDisplayMode(sdm);
        return selectionProvider;
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
        QueryStringWithParameters queryString;
        try {
            queryString = QueryUtils.mergeQuery(m2mConfiguration.getQuery(), criteria, this);
        } catch (RuntimeException e) {
            RequestMessages.addErrorMessage("Invalid query");
            throw e;
        }
        existingAssociations =
                QueryUtils.runHqlQuery(session, queryString.getQueryString(), queryString.getParameters());
        availableAssociations = new ArrayList<Object>();
        String manyQueryString = ((DatabaseSelectionProvider) manySelectionProvider.getActualSelectionProvider()).getHql();
        if(manyQueryString == null) {
            throw new RuntimeException("Couldn't determine many query");
        }
        QueryStringWithParameters manyQuery =
                QueryUtils.mergeQuery(manyQueryString, null, this);
        potentiallyAvailableAssociations =
                QueryUtils.runHqlQuery(session, manyQuery.getQueryString(), manyQuery.getParameters());
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
        for(Object a : existingAssociations) {
            if(oPk.equals(manyPropertyAccessor.get(a))) {
                return true;
            }
        }
        return false;
    }

    protected void deleteRelation(Object rel) {
        session.delete(m2mConfiguration.getActualRelationTable().getActualEntityName(), rel);
    }

    protected Object saveNewRelation(Object pk, PropertyAccessor onePropertyAccessor, PropertyAccessor manyPropertyAccessor) {
        Object newRelation = relationTableAccessor.newInstance();
        onePropertyAccessor.set(newRelation, onePk);
        manyPropertyAccessor.set(newRelation, pk);
        prepareSave(newRelation);
        session.save(m2mConfiguration.getActualRelationTable().getActualEntityName(), newRelation);
        return newRelation;
    }

    //Configuration

    /*@Button(list = "pageHeaderButtons", titleKey = "configure", order = 1, icon = Button.ICON_WRENCH)
    @RequiresPermissions(level = AccessLevel.DEVELOP)
    public Resolution configure() {
        prepareConfigurationForms();
        return new ForwardResolution("/m/crud/many2many/configure.jsp");
    }

    @Button(list = "configuration", key = "update.configuration", order = 1, type = Button.TYPE_PRIMARY)
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
            RequestMessages.addInfoMessage(ElementsThreadLocals.getText("configuration.updated.successfully"));
            return cancel();
        } else {
            RequestMessages.addErrorMessage(ElementsThreadLocals.getText("the.configuration.could.not.be.saved"));
            return new ForwardResolution("/m/crud/many2many/configure.jsp");
        }
    }*/

    protected void prepareConfigurationForms() {
        FormBuilder formBuilder = new FormBuilder(ConfigurationForm.class);
        if(m2mConfiguration != null && m2mConfiguration.getActualRelationTable() != null) {
            formBuilder.configFields(
                    "viewType", "database", "query", "oneExpression", "onePropertyName", "oneSpName", "manySpName");
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
        } else {
            formBuilder.configFields(
                    "viewType", "database", "query", "oneExpression", "onePropertyName");
        }
        formBuilder.configFieldSetNames("Many to many");

        DefaultSelectionProvider viewTypeSelectionProvider = new DefaultSelectionProvider("viewType");
        String label = ElementsThreadLocals.getText("check.boxes.horizontal");
        viewTypeSelectionProvider.appendRow(ViewType.CHECKBOXES.name(), label, true);
        label = ElementsThreadLocals.getText("check.boxes.vertical");
        viewTypeSelectionProvider.appendRow(ViewType.CHECKBOXES_VERTICAL.name(), label, true);
        //label = getMessage("lists");
        //viewTypeSelectionProvider.appendRow(ViewType.LISTS.name(), label, true);
        formBuilder.configSelectionProvider(viewTypeSelectionProvider, "viewType");

        SelectionProvider databaseSelectionProvider =
                SelectionProviderLogic.createSelectionProvider(
                        "database",
                        persistence.getModel().getDatabases(),
                        Database.class,
                        null,
                        new String[]{ "databaseName" });
        formBuilder.configSelectionProvider(databaseSelectionProvider, "database");

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

    public Object getOnePk() {
        return onePk;
    }

    public void setOnePk(Object onePk) {
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

        //--------------------------------------------------------------------------
    // REST
    //--------------------------------------------------------------------------

    /**
     * Handles available keys via REST.
     * @since 4.2.1
     * @return key set results (key, label) as JSON.
     */
    @GET
    @Produces(MimeTypes.APPLICATION_JSON_UTF8)
    public Response getAsJson() {
        return jsonKeys();
    }

    /**
     * Handles available associations for given key
     * @param key the key string
     * @since 4.2.1
     * @return available associations set results as JSON.
     */
    @GET
    @Path(":availableAssociations/{key}")
    @Produces(MimeTypes.APPLICATION_JSON_UTF8)
    public Response getAssociations(@PathParam("key") String key) {
        try {
            loadOnePk(key);
        }catch (Exception e){
            logger.error("Cannot get key " + key ,e);
        }
        return jsonAssociations();
    }

    protected void loadOnePk(Object key) throws Exception{
        TableAccessor tableAccessor = new TableAccessor(m2mConfiguration.getActualRelationTable());
        PropertyAccessor onePkAccessor = tableAccessor.getProperty(m2mConfiguration.getActualOnePropertyName());

        if(onePkAccessor == null) {
            logger.error("Not a property: {}", m2mConfiguration.getActualOnePropertyName());
            return;
        }

        Class type = onePkAccessor.getType();
        onePk = OgnlUtils.convertValue(key, type);
    }

    /**
     * Handles object creation via REST.
     * @param jsonObject the object (in serialized JSON form)
     * @since 4.2.1
     * @return the created object as JSON (in a JAX-RS Response).
     * @throws Exception only to make the compiler happy. Nothing should be thrown in normal operation. If this method throws, it is probably a bug.
     */
    @POST
    @RequiresPermissions(permissions = ManyToManyAction.PERMISSION_UPDATE)
    @Produces(MimeTypes.APPLICATION_JSON_UTF8)
    @Consumes(MimeTypes.APPLICATION_JSON_UTF8)
    public Response httpPostJson(String jsonObject) throws Exception {
        JSONObject obj = new JSONObject(jsonObject);
        logger.debug(jsonObject);

        if(!correctlyConfigured) {
            return Response.serverError().entity(configurationForm).build();
        }

        for( Object key : obj.keySet() ){
            try{
                loadOnePk(key);
            }catch (Exception e){
                logger.error("Cannot get key "+key ,e);
            }

            JSONArray selectedKeysJson = obj.getJSONArray(onePk.toString());

            for( int i=0; i<selectedKeysJson.length();i++ ){
                selectedPrimaryKeys.add(selectedKeysJson.get(i).toString());
            }

            try {
                loadAssociations();
            } catch (Exception e) {
                logger.error("Could not load associations", e);
                return Response.serverError().entity(e).build();
            }

            PkHelper pkHelper = new PkHelper(manyTableAccessor);
            //TODO chiave multipla
            String onePropertyName = m2mConfiguration.getActualOnePropertyName();
            PropertyAccessor onePropertyAccessor = relationTableAccessor.getProperty(onePropertyName);
            //TODO chiave multipla
            String manyPropertyName = m2mConfiguration.getManySelectionProvider().getActualSelectionProvider().getReferences().get(0).getActualFromColumn().getActualPropertyName();
            PropertyAccessor manyPropertyAccessor = relationTableAccessor.getProperty(manyPropertyName);
            PropertyAccessor[] manyKeyProperties = manyTableAccessor.getKeyProperties();
            //TODO handle manyKeyProperties.length > 1
            PropertyAccessor manyPkAccessor = manyTableAccessor.getProperty(manyKeyProperties[0].getName());
            for(String pkString : selectedPrimaryKeys) {
                Serializable pkObject = pkHelper.getPrimaryKey(pkString.split("/"));
                Object pk = manyPkAccessor.get(pkObject);
                if(!isExistingAssociation(manyPropertyAccessor, pk)) {
                    Object newRelation = saveNewRelation(pk, onePropertyAccessor, manyPropertyAccessor);
                    existingAssociations.add(newRelation);
                }
            }
            Iterator it = existingAssociations.iterator();
            while(it.hasNext()) {
                Object o = it.next();
                //TODO handle manyKeyProperties.length > 1
                Object pkObject = manyPropertyAccessor.get(o);
                String pkString = (String) OgnlUtils.convertValue(pkObject, String.class);
                if(!selectedPrimaryKeys.contains(pkString)) {
                    deleteRelation(o);
                    it.remove();
                }
            }
        }
        session.getTransaction().commit();
        return objectCreated();
    }

    protected Response objectCreated() throws URISyntaxException {
        return Response.status(Response.Status.CREATED). build();
    }

    public Response jsonKeys() throws JSONException {
        if(!correctlyConfigured) {
            return resourceActionNotConfigured();
        }

        JSONArray keys = new JSONArray();
        if( onePk==null ){
            Map<Object,SelectionModel.Option> map = oneSelectField.getOptions();
            for(Object key : map.keySet()) {
                //logger.info( map.get(key).label);
                onePk=map.get(key).value;
                if(onePk != null) {
                    JSONObject jsonKey = new JSONObject();
                    jsonKey.put("key",onePk.toString());
                    jsonKey.put("label",map.get(key).label);
                    keys.put(jsonKey);
                }
            }
        }else{
            JSONObject jsonKey = new JSONObject();
            jsonKey.put("key",onePk.toString());
            jsonKey.put("label","");
            keys.put(jsonKey);
        }

        JSONObject response = new JSONObject();
        response.put("keys",keys);
        String jsonText = response.toString(2);
        return Response.ok(jsonText).type(MediaType.APPLICATION_JSON_TYPE).encoding("UTF-8").build();
    }

    public Response jsonAssociations() throws JSONException {
        JSONObject response = new JSONObject();
        JSONArray enumList = new JSONArray();
        JSONObject model = new JSONObject();
        JSONObject titleMap = new JSONObject();
        JSONArray trueRelations = new JSONArray();
        JSONArray form = new JSONArray();
        JSONObject schema = new JSONObject();

        if(onePk != null) {
            try {
                loadAssociations();
                if(potentiallyAvailableAssociations == null) {
                    return resourceActionNotConfigured();
                }
            } catch (NoSuchFieldException e) {
                return resourceActionNotConfigured();
            }

            booleanRelation = new LinkedHashMap<>();
            ClassAccessor ca = getManyTableAccessor();
            PkHelper pkHelper = new PkHelper(ca);

            for(Object obj : potentiallyAvailableAssociations) {
                String pk = StringUtils.join(pkHelper.generatePkStringArray(obj), "/");
                enumList.put(pk);
                titleMap.put(pk, ShortNameUtils.getName(ca, obj));

                if(!availableAssociations.contains(obj)) {
                    trueRelations.put(pk);
                }
            }

            model.put(onePk.toString(), trueRelations);

            JSONObject items = new JSONObject();
            items.put("type", "string");
            items.put("enum", enumList);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", "array");
            jsonObject.put("title", "");
            jsonObject.put("items", items);

            JSONObject properties = new JSONObject();
            properties.put(onePk.toString(),jsonObject);

            schema.put("type", "object");
            schema.put("title", "Many to many");
            schema.put("properties", properties);

            JSONObject checkboxes = new JSONObject();
            checkboxes.put("key", onePk.toString());
            checkboxes.put("titleMap", titleMap);
            checkboxes.put("notitle", true);
            form.put(checkboxes);
        }

        if(!SecurityLogic.hasPermissions(
                getPortofinoConfiguration(),
                getActionInstance(), SecurityUtils.getSubject(),
                AccessLevel.VIEW, ManyToManyAction.PERMISSION_UPDATE)) {
            schema.put("readonly",true);
        }

        response.put("model",model);
        response.put("schema",schema);
        response.put("form",form);

        String jsonText = response.toString(2);
        return Response.ok(jsonText).type(MediaType.APPLICATION_JSON_TYPE).encoding("UTF-8").build();
    }
}
