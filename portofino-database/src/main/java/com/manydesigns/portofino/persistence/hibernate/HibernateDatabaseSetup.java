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

package com.manydesigns.portofino.persistence.hibernate;

import com.manydesigns.portofino.model.database.Database;
import org.hibernate.*;
import org.hibernate.engine.spi.SessionDelegatorBaseImpl;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.persister.entity.EntityPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
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
            "Copyright (C) 2005-2019 ManyDesigns srl";

    protected final Database database;
    protected final SessionFactory sessionFactory;
    protected final ThreadLocal<Session> threadSessions;
    protected final Map<String, String> jpaEntityNameToClassNameMap = new HashMap<>();

        public static final Logger logger =
            LoggerFactory.getLogger(HibernateDatabaseSetup.class);

    public HibernateDatabaseSetup(Database database, SessionFactory sessionFactory) {
        this.database = database;
        this.sessionFactory = sessionFactory;
        threadSessions = new ThreadLocal<>();
        database.getAllTables().forEach(t -> {
            jpaEntityNameToClassNameMap.put(t.getActualEntityName(), t.getSchema().getQualifiedName() + "." + t.getActualEntityName());
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
        Session session = sessionFactory.openSession();
        return new SessionDelegatorBaseImpl((SessionImplementor) session) {
            @Override
            public EntityPersister getEntityPersister(String entityName, Object object) throws HibernateException {
                return super.getEntityPersister(translateEntityNameFromJpaToHibernate(entityName), object);
            }

            @Override
            public Object get(String entityName, Serializable id) {
                return super.get(translateEntityNameFromJpaToHibernate(entityName), id);
            }

            @Override
            public Object get(String entityName, Serializable id, LockMode lockMode) {
                return super.get(translateEntityNameFromJpaToHibernate(entityName), id, lockMode);
            }

            @Override
            public Object get(String entityName, Serializable id, LockOptions lockOptions) {
                return super.get(translateEntityNameFromJpaToHibernate(entityName), id, lockOptions);
            }

            @Override
            public Object load(String entityName, Serializable id, LockOptions lockOptions) {
                return super.load(translateEntityNameFromJpaToHibernate(entityName), id, lockOptions);
            }

            @Override
            public Object load(String entityName, Serializable id) {
                return super.load(translateEntityNameFromJpaToHibernate(entityName), id);
            }

            @Override
            public void replicate(String entityName, Object object, ReplicationMode replicationMode) {
                super.replicate(translateEntityNameFromJpaToHibernate(entityName), object, replicationMode);
            }

            @Override
            public Serializable save(String entityName, Object object) {
                return super.save(translateEntityNameFromJpaToHibernate(entityName), object);
            }

            @Override
            public void saveOrUpdate(String entityName, Object object) {
                super.saveOrUpdate(translateEntityNameFromJpaToHibernate(entityName), object);
            }

            @Override
            public void update(String entityName, Object object) {
                super.update(translateEntityNameFromJpaToHibernate(entityName), object);
            }

            @Override
            public Object merge(String entityName, Object object) {
                return super.merge(translateEntityNameFromJpaToHibernate(entityName), object);
            }

            @Override
            public void persist(String entityName, Object object) {
                super.persist(translateEntityNameFromJpaToHibernate(entityName), object);
            }

            @Override
            public void delete(String entityName, Object object) {
                super.delete(translateEntityNameFromJpaToHibernate(entityName), object);
            }

            @Override
            public void lock(String entityName, Object object, LockMode lockMode) {
                super.lock(translateEntityNameFromJpaToHibernate(entityName), object, lockMode);
            }

            @Override
            public void refresh(String entityName, Object object) {
                super.refresh(translateEntityNameFromJpaToHibernate(entityName), object);
            }

            @Override
            public void refresh(String entityName, Object object, LockOptions lockOptions) {
                super.refresh(translateEntityNameFromJpaToHibernate(entityName), object, lockOptions);
            }
        };
    }

    public String translateEntityNameFromJpaToHibernate(String entityName) {
        String hibernateEntityName = jpaEntityNameToClassNameMap.get(entityName);
        return hibernateEntityName != null ? hibernateEntityName : entityName;
    }

    public void setThreadSession(Session session) {
        threadSessions.set(session);
    }

    public void removeThreadSession() {
        threadSessions.remove();
    }
}
