/*
* Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.oauth;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.CredentialStore;
import com.manydesigns.elements.ognl.OgnlUtils;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.model.database.Column;
import com.manydesigns.portofino.model.database.Database;
import com.manydesigns.portofino.model.database.DatabaseLogic;
import com.manydesigns.portofino.model.database.Table;
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
public class ApplicationCredentialStore implements CredentialStore {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    protected final Application application;
    protected final Table table;
    protected final String idProperty;
    protected final String accessTokenProperty;
    protected final String refreshTokenProperty;
    protected final String expirationProperty;

    public ApplicationCredentialStore(
            Application application, String databaseName, String entityName,
            String idProperty, String accessTokenProperty, String refreshTokenProperty, String expirationProperty) {
        this.application = application;
        this.idProperty = idProperty;
        this.accessTokenProperty = accessTokenProperty;
        this.refreshTokenProperty = refreshTokenProperty;
        this.expirationProperty = expirationProperty;
        Database database = DatabaseLogic.findDatabaseByName(application.getModel(), databaseName);
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
        Session session = application.getSession(table.getDatabaseName());
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
        Session session = application.getSession(table.getDatabaseName());
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
        Session session = application.getSession(table.getDatabaseName());
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
