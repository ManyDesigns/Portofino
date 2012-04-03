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

import com.manydesigns.portofino.model.database.ForeignKey;
import com.manydesigns.portofino.model.database.ModelSelectionProvider;
import com.manydesigns.portofino.pageactions.m2m.configuration.ManyToManyConfiguration;
import com.manydesigns.portofino.pageactions.m2m.configuration.SelectionProviderReference;
import org.apache.commons.lang.StringUtils;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class ConfigurationForm extends ManyToManyConfiguration {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    public String oneSpName;

    public String manySpName;

    public ConfigurationForm(ManyToManyConfiguration m2mConfiguration) {
        setViewType(m2mConfiguration.getViewType());
        setDatabase(m2mConfiguration.getDatabase());
        setQuery(m2mConfiguration.getQuery());
        setOneExpression(m2mConfiguration.getOneExpression());
        setOnePropertyName(m2mConfiguration.getOnePropertyName());
        if(m2mConfiguration.getOneSelectionProvider() != null) {
            oneSpName = m2mConfiguration.getOneSelectionProvider().getActualSelectionProvider().getName();
        }
        if(m2mConfiguration.getManySelectionProvider() != null) {
            manySpName = m2mConfiguration.getManySelectionProvider().getActualSelectionProvider().getName();
        }
    }

    public void writeTo(ManyToManyConfiguration m2mConfiguration) {
        m2mConfiguration.setViewType(viewType);
        m2mConfiguration.setDatabase(database);
        m2mConfiguration.setQuery(query);
        m2mConfiguration.setOneExpression(oneExpression);
        m2mConfiguration.setOnePropertyName(onePropertyName);
        if(StringUtils.isEmpty(oneSpName)) {
            m2mConfiguration.setOneSelectionProvider(null);
        } else {
            boolean found = false;
            for(ForeignKey fk : m2mConfiguration.getActualRelationTable().getForeignKeys()) {
                if(fk.getName().equals(oneSpName)) {
                    SelectionProviderReference ref = new SelectionProviderReference();
                    ref.setForeignKeyName(oneSpName);
                    m2mConfiguration.setOneSelectionProvider(ref);
                    found = true;
                    break;
                }
            }
            if(!found) {
                for(ModelSelectionProvider sp : m2mConfiguration.getActualRelationTable().getSelectionProviders()) {
                    if(sp.getName().equals(oneSpName)) {
                        SelectionProviderReference ref = new SelectionProviderReference();
                        ref.setSelectionProviderName(oneSpName);
                        m2mConfiguration.setOneSelectionProvider(ref);
                        break;
                    }
                }
            }
        }

        if(StringUtils.isEmpty(manySpName) || m2mConfiguration.getActualRelationTable() == null) {
            m2mConfiguration.setManySelectionProvider(null);
        } else {
            boolean found = false;
            for(ForeignKey fk : m2mConfiguration.getActualRelationTable().getForeignKeys()) {
                if(fk.getName().equals(manySpName)) {
                    SelectionProviderReference ref = new SelectionProviderReference();
                    ref.setForeignKeyName(manySpName);
                    m2mConfiguration.setManySelectionProvider(ref);
                    found = true;
                    break;
                }
            }
            if(!found) {
                for(ModelSelectionProvider sp : m2mConfiguration.getActualRelationTable().getSelectionProviders()) {
                    if(sp.getName().equals(manySpName)) {
                        SelectionProviderReference ref = new SelectionProviderReference();
                        ref.setSelectionProviderName(manySpName);
                        m2mConfiguration.setManySelectionProvider(ref);
                        break;
                    }
                }
            }
        }
    }
}
