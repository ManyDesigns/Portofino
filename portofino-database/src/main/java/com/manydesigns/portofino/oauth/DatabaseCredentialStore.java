/*
 * Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.oauth;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.CredentialStore;
import com.manydesigns.elements.ognl.OgnlUtils;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.portofino.model.database.Column;
import com.manydesigns.portofino.model.database.Database;
import com.manydesigns.portofino.model.database.DatabaseLogic;
import com.manydesigns.portofino.model.database.Table;
import com.manydesigns.portofino.persistence.Persistence;
import com.manydesigns.portofino.reflection.TableAccessor;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import java.io.IOException;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class DatabaseCredentialStore implements CredentialStore {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    protected final Persistence persistence;
    protected final Table table;
    protected final String idProperty;
    protected final String accessTokenProperty;
    protected final String refreshTokenProperty;
    protected final String expirationProperty;

    public DatabaseCredentialStore(
            Persistence persistence, String databaseName, String entityName,
            String idProperty, String accessTokenProperty, String refreshTokenProperty, String expirationProperty) {
        this.persistence = persistence;
        this.idProperty = idProperty;
        this.accessTokenProperty = accessTokenProperty;
        this.refreshTokenProperty = refreshTokenProperty;
        this.expirationProperty = expirationProperty;
        Database database = DatabaseLogic.findDatabaseByName(persistence.getModel(), databaseName);
        table = DatabaseLogic.findTableByEntityName(database, entityName);
        if(table == null) {
            throw new IllegalArgumentException("Table not found: " + entityName);
        }
        try {
            TableAccessor tableAccessor = new TableAccessor(table);
            tableAccessor.getProperty(idProperty);
            tableAccessor.getProperty(accessTokenProperty);
            tableAccessor.getProperty(refreshTokenProperty);
            tableAccessor.getProperty(expirationProperty);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("Invalid field name", e);
        }
    }

    public boolean load(String userId, Credential credential) throws IOException {
        Session session = persistence.getSession(table.getDatabaseName());
        Column idColumn = DatabaseLogic.findColumnByPropertyName(table, idProperty);
        Object idValue = OgnlUtils.convertValue(userId, idColumn.getActualJavaType());
        Criteria criteria =
                session.createCriteria(table.getActualEntityName()).add(Restrictions.eq(idProperty, idValue));
        TableAccessor tableAccessor = new TableAccessor(table);
        Object record = criteria.uniqueResult();
        if(record == null) {
            return false;
        }
        try {
            Object value = tableAccessor.getProperty(accessTokenProperty).get(record);
            credential.setAccessToken((String) value);

            value = tableAccessor.getProperty(refreshTokenProperty).get(record);
            credential.setRefreshToken((String) value);

            value = tableAccessor.getProperty(expirationProperty).get(record);
            credential.setExpirationTimeMilliseconds((Long) OgnlUtils.convertValue(value, Long.class));
            return true;
        } catch (NoSuchFieldException e) {
            throw new Error(e);
        }
    }

    public void store(String userId, Credential credential) throws IOException {
        Session session = persistence.getSession(table.getDatabaseName());
        Column idColumn = DatabaseLogic.findColumnByPropertyName(table, idProperty);
        Object idValue = OgnlUtils.convertValue(userId, idColumn.getActualJavaType());
        Criteria criteria =
                session.createCriteria(table.getActualEntityName()).add(Restrictions.eq(idProperty, idValue));
        TableAccessor tableAccessor = new TableAccessor(table);
        Object record = criteria.uniqueResult();
        boolean isNew = record == null;
        if(isNew) {
            record = create(tableAccessor, idValue);
        }
        try {
            tableAccessor.getProperty(accessTokenProperty).set(record, credential.getAccessToken());

            tableAccessor.getProperty(refreshTokenProperty).set(record, credential.getRefreshToken());

            PropertyAccessor exp = tableAccessor.getProperty(expirationProperty);
            exp.set(record, OgnlUtils.convertValue(credential.getExpirationTimeMilliseconds(), exp.getType()));
        } catch (NoSuchFieldException e) {
            throw new Error(e);
        }
        if(isNew) {
            session.save(table.getActualEntityName(), record);
        } else {
            session.update(table.getActualEntityName(), record);
        }
        session.getTransaction().commit();
        session.beginTransaction();
    }

    protected Object create(TableAccessor tableAccessor, Object idValue) {
        Object object = tableAccessor.newInstance();
        try {
            tableAccessor.getProperty(idProperty).set(object, idValue);
        } catch (NoSuchFieldException e) {
            throw new Error(e);
        }
        return object;
    }

    public void delete(String userId, Credential credential) throws IOException {
        Session session = persistence.getSession(table.getDatabaseName());
        Column idColumn = DatabaseLogic.findColumnByPropertyName(table, idProperty);
        Object idValue = OgnlUtils.convertValue(userId, idColumn.getActualJavaType());
        Criteria criteria =
                session.createCriteria(table.getActualEntityName()).add(Restrictions.eq(idProperty, idValue));
        TableAccessor tableAccessor = new TableAccessor(table);
        Object record = criteria.uniqueResult();
        if(record == null) {
            throw new IllegalArgumentException("No record exists with id: " + userId);
        }
        delete(session, tableAccessor, record);
    }

    protected void delete(Session session, TableAccessor tableAccessor, Object record) {
        try {
            tableAccessor.getProperty(accessTokenProperty).set(record, null);
            tableAccessor.getProperty(refreshTokenProperty).set(record, null);
            tableAccessor.getProperty(expirationProperty).set(record, null);
            session.update(table.getActualEntityName(), record);
        } catch (NoSuchFieldException e) {
            throw new Error(e);
        }
    }
}
