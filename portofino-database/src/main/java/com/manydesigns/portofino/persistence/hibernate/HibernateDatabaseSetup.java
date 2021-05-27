/*
 * Copyright (C) 2005-2020 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.persistence.hibernate;

import com.manydesigns.portofino.code.CodeBase;
import com.manydesigns.portofino.model.database.Database;
import com.manydesigns.portofino.persistence.Persistence;
import com.manydesigns.portofino.persistence.hibernate.multitenancy.MultiTenancyImplementation;
import org.apache.commons.configuration2.Configuration;
import org.hibernate.EntityMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class HibernateDatabaseSetup {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    protected final Database database;
    protected final SessionFactory sessionFactory;
    protected final CodeBase codeBase;
    protected final ThreadLocal<Session> threadSessions;
    protected final EntityMode entityMode;
    protected final Configuration configuration;
    protected final Map<String, String> jpaEntityNameToClassNameMap = new HashMap<>();
    protected final MultiTenancyImplementation multiTenancyImplementation;
    protected final Persistence persistence;

        public static final Logger logger =
            LoggerFactory.getLogger(HibernateDatabaseSetup.class);

    public HibernateDatabaseSetup(
            Database database, SessionFactory sessionFactory, CodeBase codeBase, EntityMode entityMode,
            Configuration configuration, MultiTenancyImplementation multiTenancyImplementation, Persistence persistence) {
        this.database = database;
        this.sessionFactory = sessionFactory;
        this.codeBase = codeBase;
        this.entityMode = entityMode;
        this.configuration = configuration;
        this.multiTenancyImplementation = multiTenancyImplementation;
        this.persistence = persistence;
        threadSessions = new ThreadLocal<>();
        database.getAllTables().forEach(t -> {
            jpaEntityNameToClassNameMap.put(t.getActualEntityName(), SessionFactoryBuilder.getMappedClassName(t, entityMode));
        });
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public ThreadLocal<Session> getThreadSessions() {
        return threadSessions;
    }

    public Session getThreadSession() {
        return getThreadSession(true);
    }

    public Session getThreadSession(boolean create) {
        Session session = threadSessions.get();
        if(session == null && create) {
            if(logger.isDebugEnabled()) {
                logger.debug("Creating thread-local session for {}", Thread.currentThread());
            }
            session = createSession();
            session.beginTransaction();
            threadSessions.set(session);
        }
        return session;
    }

    public Session createSession() {
        Session session;
        if(multiTenancyImplementation != null) {
            session = sessionFactory.withOptions().tenantIdentifier(multiTenancyImplementation.getTenant()).openSession();
        } else {
            session = sessionFactory.openSession();
        }
        return new SessionDelegator(this, session);
    }

    public String translateEntityNameFromJpaToHibernate(String entityName) {
        String hibernateEntityName = jpaEntityNameToClassNameMap.get(entityName);
        return hibernateEntityName != null ? hibernateEntityName : entityName;
    }

    public void dispose() {
        //TODO It is the responsibility of the application to ensure that there are no open Sessions before calling close().
        //http://ajava.org/online/hibernate3api/org/hibernate/SessionFactory.html#close%28%29
        getSessionFactory().close();
    }

    public void setThreadSession(Session session) {
        threadSessions.set(session);
    }

    public void removeThreadSession() {
        threadSessions.remove();
    }

    public Database getDatabase() {
        return database;
    }

    public CodeBase getCodeBase() {
        return codeBase;
    }

    public EntityMode getEntityMode() {
        return entityMode;
    }

    public MultiTenancyImplementation getMultiTenancyImplementation() {
        return multiTenancyImplementation;
    }
}
