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

package com.manydesigns.portofino.persistence.hibernate;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class HibernateDatabaseSetup {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    protected final Configuration configuration;
    protected final SessionFactory sessionFactory;
    protected final ThreadLocal<Session> threadSessions;

        public static final Logger logger =
            LoggerFactory.getLogger(HibernateDatabaseSetup.class);

    public HibernateDatabaseSetup(Configuration configuration,
                                  SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.configuration = configuration;
        threadSessions = new ThreadLocal<Session>();
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public Configuration getConfiguration() {
        return configuration;
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
            session = sessionFactory.openSession();
            session.beginTransaction();
            threadSessions.set(session);
        }
        return session;
    }

    public void setThreadSession(Session session) {
        threadSessions.set(session);
    }

    public void removeThreadSession() {
        threadSessions.remove();
    }
}
