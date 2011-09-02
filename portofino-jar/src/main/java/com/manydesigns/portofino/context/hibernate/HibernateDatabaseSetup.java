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

package com.manydesigns.portofino.context.hibernate;

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
            "Copyright (c) 2005-2011, ManyDesigns srl";

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
            threadSessions.set(session);
        }
        return session;
    }

    public void setThreadSession(Session session) {
        threadSessions.set(session);
    }
}
