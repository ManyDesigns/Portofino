/*
 * Copyright (C) 2005-2017 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.pageactions.crud;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.annotations.ShortName;
import com.manydesigns.elements.options.*;
import com.manydesigns.elements.text.OgnlSqlFormat;
import com.manydesigns.elements.text.OgnlTextFormat;
import com.manydesigns.elements.text.QueryStringWithParameters;
import com.manydesigns.elements.text.TextFormat;
import com.manydesigns.portofino.pageactions.crud.configuration.database.SelectionProviderReference;
import com.manydesigns.portofino.persistence.Persistence;
import com.manydesigns.portofino.persistence.QueryUtils;
import com.manydesigns.portofino.logic.SelectionProviderLogic;
import com.manydesigns.portofino.model.database.*;
import com.manydesigns.portofino.pageactions.crud.configuration.CrudProperty;
import com.manydesigns.portofino.reflection.TableAccessor;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Support object for standard (model-based, i.e. HQL or SQL) selection providers.
 *
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class ModelSelectionProviderSupport implements SelectionProviderSupport {
    public static final String copyright =
            "Copyright (C) 2005-2017 ManyDesigns srl";

    public static final Logger logger =
            LoggerFactory.getLogger(ModelSelectionProviderSupport.class);

    protected final CrudAction crudAction;
    protected final Persistence persistence;
    protected List<CrudSelectionProvider> crudSelectionProviders;
    protected final Multimap<List<String>, ModelSelectionProvider> availableSelectionProviders;

    public ModelSelectionProviderSupport(CrudAction crudAction, Persistence persistence) {
        this.crudAction = crudAction;
        this.persistence = persistence;
        availableSelectionProviders = HashMultimap.create();
    }

    public void setup() {
        crudSelectionProviders = new ArrayList<CrudSelectionProvider>();
        Set<String> configuredSPs = new HashSet<String>();
        for(SelectionProviderReference ref : crudAction.getCrudConfiguration().getSelectionProviders()) {
            boolean added;
            if(ref.getForeignKey() != null) {
                added = setupSelectionProvider(ref, ref.getForeignKey(), configuredSPs);
            } else if(ref.getSelectionProvider() instanceof DatabaseSelectionProvider) {
                DatabaseSelectionProvider dsp = (DatabaseSelectionProvider) ref.getSelectionProvider();
                added = setupSelectionProvider(ref, dsp, configuredSPs);
            } else {
                logger.error("Unsupported selection provider: " + ref.getSelectionProvider());
                continue;
            }
            if(ref.isEnabled() && !added) {
                logger.warn("Selection provider {} not added; check whether the fields on which it is configured " +
                        "overlap with some other selection provider", ref);
            }
        }

        //Remove disabled selection providers and mark them as configured to avoid re-adding them
        Iterator<CrudSelectionProvider> it = crudSelectionProviders.iterator();
        while (it.hasNext()) {
            CrudSelectionProvider sp = it.next();
            if(sp.getSelectionProvider() == null) {
                it.remove();
                Collections.addAll(configuredSPs, sp.getFieldNames());
            }
        }

        Table table = crudAction.getCrudConfiguration().getActualTable();
        if(table != null) {
            for(ForeignKey fk : table.getForeignKeys()) {
                setupSelectionProvider(null, fk, configuredSPs);
            }
            for(ModelSelectionProvider dsp : table.getSelectionProviders()) {
                if(dsp instanceof DatabaseSelectionProvider) {
                    setupSelectionProvider(null, (DatabaseSelectionProvider) dsp, configuredSPs);
                } else {
                    logger.error("Unsupported selection provider: " + dsp);
                }
            }
        }
    }

    protected boolean setupSelectionProvider(
            @Nullable SelectionProviderReference ref,
            DatabaseSelectionProvider current,
            Set<String> configuredSPs) {
        List<Reference> references = current.getReferences();

        String[] fieldNames = new String[references.size()];
        Class[] fieldTypes = new Class[references.size()];

        int i = 0;
        for (Reference reference : references) {
            Column column = reference.getActualFromColumn();
            fieldNames[i] = column.getActualPropertyName();
            fieldTypes[i] = column.getActualJavaType();
            i++;
        }

        availableSelectionProviders.put(Arrays.asList(fieldNames), current);
        for(String fieldName : fieldNames) {
            //If another SP is configured for the same field, stop
            if(configuredSPs.contains(fieldName)) {
                return false;
            }
        }

        if(ref == null || ref.isEnabled()) {
            DisplayMode dm = ref != null ? ref.getDisplayMode() : DisplayMode.DROPDOWN;
            SearchDisplayMode sdm = ref != null ? ref.getSearchDisplayMode() : SearchDisplayMode.DROPDOWN;
            String newHref = ref != null ? ref.getCreateNewValueHref() : null;
            String newText = ref != null ? ref.getCreateNewValueText() : null;
            SelectionProvider selectionProvider = createSelectionProvider
                    (current, fieldNames, fieldTypes, dm, sdm, newHref, newText);

            CrudSelectionProvider crudSelectionProvider =
                new CrudSelectionProvider(selectionProvider, fieldNames);
            crudSelectionProviders.add(crudSelectionProvider);
            Collections.addAll(configuredSPs, fieldNames);
            return true;
        } else {
            //To avoid automatically adding a FK later
            CrudSelectionProvider crudSelectionProvider =
                new CrudSelectionProvider(null, fieldNames);
            crudSelectionProviders.add(crudSelectionProvider);
            return false;
        }
    }

    protected SelectionProvider createSelectionProvider
            (DatabaseSelectionProvider current, String[] fieldNames,
             Class[] fieldTypes, DisplayMode dm, SearchDisplayMode sdm,
             String newHref, String newText) {
        DefaultSelectionProvider selectionProvider;

        boolean anyActiveProperty = false;
        for(String propertyName : fieldNames) {
            CrudProperty crudProperty =
                    findProperty(propertyName, crudAction.getCrudConfiguration().getProperties());
            if(crudProperty != null && crudProperty.isEnabled()) {
                anyActiveProperty = true;
                break;
            }
        }
        if(!anyActiveProperty) {
            //Dummy
            selectionProvider = SelectionProviderLogic.createSelectionProvider(
                    current.getName(), new Class[0], Collections.emptyList());
        } else {
            selectionProvider = createSelectionProvider(current, fieldNames, fieldTypes, dm, sdm);
        }
        if(selectionProvider != null) {
            if(newHref != null) {
                OgnlTextFormat tf = new OgnlTextFormat(newHref);
                newHref = tf.format(crudAction);
                String contextPath = ElementsThreadLocals.getHttpServletRequest().getContextPath();
                if(newHref.startsWith("/") && !newHref.startsWith(contextPath)) {
                    newHref = contextPath + newHref;
                }

                tf = new OgnlTextFormat(newText);
                newText = tf.format(crudAction);
            }
            selectionProvider.setCreateNewValueHref(newHref);
            selectionProvider.setCreateNewValueText(newText);
        }
        return selectionProvider;
    }

    public List<CrudSelectionProvider> getCrudSelectionProviders() {
        return crudSelectionProviders;
    }

    public void disableSelectionProvider(List<String> key) {
        //TODO this is a shortcut: takes the first available selection provider and disables it
        Collection<ModelSelectionProvider> selectionProviders = availableSelectionProviders.get(key);
        ModelSelectionProvider dsp = selectionProviders.iterator().next();
        SelectionProviderReference sel = makeSelectionProviderReference(dsp);
        sel.setEnabled(false);
    }

    public void configureSelectionProvider
            (List<String> key, String name, DisplayMode displayMode, SearchDisplayMode searchDisplayMode,
             String createNewHref, String createNewText) {
        Collection<ModelSelectionProvider> selectionProviders = availableSelectionProviders.get(key);
        for(ModelSelectionProvider dsp : selectionProviders) {
            if(name.equals(dsp.getName())) {
                SelectionProviderReference sel = makeSelectionProviderReference(dsp);
                sel.setDisplayMode(displayMode);
                sel.setSearchDisplayMode(searchDisplayMode);
                sel.setCreateNewValueHref(createNewHref);
                sel.setCreateNewValueText(createNewText);
                break;
            }
        }
    }

    @Override
    public void clearSelectionProviders() {
        crudAction.getCrudConfiguration().getSelectionProviders().clear();
    }

    protected SelectionProviderReference makeSelectionProviderReference(ModelSelectionProvider dsp) {
        SelectionProviderReference sel = new SelectionProviderReference();
        if(dsp instanceof ForeignKey) {
            sel.setForeignKeyName(dsp.getName());
        } else {
            sel.setSelectionProviderName(dsp.getName());
        }
        crudAction.getCrudConfiguration().getSelectionProviders().add(sel);
        return sel;
    }

    public Map<List<String>, Collection<String>> getAvailableSelectionProviderNames() {
        Multimap<List<String>, String> namesMap = HashMultimap.create();
        for(Map.Entry<List<String>, ModelSelectionProvider> e : availableSelectionProviders.entries()) {
            namesMap.put(e.getKey(), e.getValue().getName());
        }
        return namesMap.asMap();
    }

    protected CrudProperty findProperty(String name, List<CrudProperty> properties) {
        for(CrudProperty p : properties) {
            if(p.getName().equals(name)) {
                return p;
            }
        }
        return null;
    }

    protected DefaultSelectionProvider createSelectionProvider(
            DatabaseSelectionProvider sp, String[] fieldNames, Class[] fieldTypes,
            DisplayMode dm, SearchDisplayMode sdm) {
        OptionProvider optionProvider;
        String name = sp.getName();
        String databaseName = sp.getToDatabase();
        String sql = sp.getSql();
        String hql = sp.getHql();
        if (!StringUtils.isEmpty(sql)) {
            optionProvider = createSQLOptionProvider(sp, fieldTypes, name, databaseName, sql);
        } else if (!StringUtils.isEmpty(hql)) {
            optionProvider = createHQLOptionProvider(sp, name, databaseName, hql);
        } else {
            logger.error("ModelSelection provider '{}': both 'hql' and 'sql' are null", name);
            return null;
        }
        if(optionProvider == null) {
            logger.debug("Could not create optionProvider");
            return null;
        }
        DefaultSelectionProvider selectionProvider =
                new DefaultSelectionProvider(name, fieldNames.length, optionProvider);
        selectionProvider.setDisplayMode(dm);
        selectionProvider.setSearchDisplayMode(sdm);
        return selectionProvider;
    }

    protected OptionProvider createHQLOptionProvider(
            DatabaseSelectionProvider selectionProvider, String name, String databaseName, String hql) {
        Database database =
                DatabaseLogic.findDatabaseByName(persistence.getModel(), databaseName);
        Table table = QueryUtils.getTableFromQueryString(database, hql);
        if(table == null) {
            logger.error("Selection provider {} has a HQL query that " +
                    "refers to an entity that does not exist ({})", name, hql);
            return null;
        }
        OptionProvider optionProvider;
        optionProvider = new OptionProvider() {
            @Override
            public List<Option> getOptions() {
                String entityName = table.getActualEntityName();
                Session session = persistence.getSession(databaseName);
                QueryStringWithParameters queryWithParameters = QueryUtils.mergeQuery(hql, null, this);

                Collection<Object> objects = getFromQueryCache(selectionProvider, queryWithParameters);
                if(objects == null) {
                    String queryString = queryWithParameters.getQueryString();
                    Object[] parameters = queryWithParameters.getParameters();
                    logger.debug("Query not in cache: {}", queryString);
                    try {
                        objects = QueryUtils.runHqlQuery(session, queryString, parameters);
                    } catch (Exception e) {
                        logger.error("Exception in populating selection provider " + name, e);
                        return null;
                    }
                    putInQueryCache(selectionProvider, queryWithParameters, objects);
                }

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
                final TextFormat[] actualTextFormats = textFormats;
                Stream<Option> optionStream =
                        objects.stream().map(o -> SelectionProviderLogic.getOption(name, tableAccessor.getKeyProperties(), actualTextFormats, o));
                if(selectionProvider instanceof ForeignKey) {
                    optionStream = optionStream.sorted(DefaultSelectionProvider.OPTION_COMPARATOR_BY_LABEL);
                }
                return optionStream.collect(Collectors.toList());
            }
        };
        return optionProvider;
    }

    @NotNull
    protected OptionProvider createSQLOptionProvider(
            DatabaseSelectionProvider selectionProvider, Class[] fieldTypes, String name, String databaseName, String sql) {
        OptionProvider optionProvider;
        optionProvider = new OptionProvider() {
            @Override
            public List<Option> getOptions() {
                Session session = persistence.getSession(databaseName);
                OgnlSqlFormat sqlFormat = OgnlSqlFormat.create(sql);
                String formatString = sqlFormat.getFormatString();
                Object[] parameters = sqlFormat.evaluateOgnlExpressions(this);
                QueryStringWithParameters cacheKey = new QueryStringWithParameters(formatString, parameters);
                Collection<Object[]> objects = getFromQueryCache(selectionProvider, cacheKey);
                if(objects == null) {
                    logger.debug("Query not in cache: {}", formatString);
                    try {
                        objects = QueryUtils.runSql(session, formatString, parameters);
                    } catch (Exception e) {
                        logger.error("Exception in populating selection provider " + name, e);
                        return null;
                    }
                    putInQueryCache(selectionProvider, cacheKey, objects);
                }
                return objects.stream().map(o -> SelectionProviderLogic.getOption(fieldTypes, o)).collect(Collectors.toList());
            }
        };
        return optionProvider;
    }

    protected void putInQueryCache(
            DatabaseSelectionProvider sp, QueryStringWithParameters queryWithParameters, Collection objects) {}

    protected Collection getFromQueryCache(
            DatabaseSelectionProvider sp, QueryStringWithParameters queryWithParameters) {
        return null;
    }
}
