/*
 * Copyright (C) 2005-2010 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.context;

import com.manydesigns.elements.logging.LogUtil;
import com.manydesigns.portofino.database.HibernateConfig;
import com.manydesigns.portofino.model.*;
import com.manydesigns.portofino.search.HibernateCriteriaAdapter;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class MDContextHibernateImpl implements MDContext {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    protected DataModel dataModel;
    protected Map<String, SessionFactory> sessionFactories;
    protected final ThreadLocal<Map<String, Session>> threadSessions;

    public static final Logger logger =
            LogUtil.getLogger(MDContextHibernateImpl.class);

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    public MDContextHibernateImpl() {
        threadSessions = new ThreadLocal<Map<String, Session>>();
    }

    //--------------------------------------------------------------------------
    // Model loading
    //--------------------------------------------------------------------------

    public void loadXmlModelAsResource(String resource) {
        LogUtil.entering(logger, "loadXmlModelAsResource", resource);

        DBParser parser = new DBParser();
        try {
            dataModel = parser.parse(resource);
            HibernateConfig builder = new HibernateConfig();
            sessionFactories = builder.build(dataModel);
        } catch (Exception e) {
            LogUtil.severeMF(logger, "Cannot load/parse model: {0}", e,
                    resource);
        }

        LogUtil.exiting(logger, "loadXmlModelAsResource");
    }

    //--------------------------------------------------------------------------
    // Modell access
    //--------------------------------------------------------------------------

    public DataModel getDataModel() {
        return dataModel;
    }

    //--------------------------------------------------------------------------
    // Persistance
    //--------------------------------------------------------------------------

    public Map<String, Object> getObjectByPk(String qualifiedTableName,
                                             Object... pk) {
        throw new UnsupportedOperationException();
    }
    
    public Map<String, Object> getObjectByPk(String qualifiedTableName,
                                             HashMap<String, Object> pk) {
        Session session = getSession(qualifiedTableName);

        //noinspection unchecked
        return (Map<String, Object>)session.load(qualifiedTableName, pk);
    }


    public List<Map<String, Object>> getAllObjects(String qualifiedTableName) {
        Session session = getSession(qualifiedTableName);

        Criteria hibernateCriteria = session.createCriteria(qualifiedTableName);
        //noinspection unchecked
        return hibernateCriteria.list();
    }

    protected Session getSession(String qualifiedTableName) {
        Table table = dataModel.findTableByQualifiedName(qualifiedTableName);
        String databaseName = table.getDatabaseName();
        return threadSessions.get().get(databaseName);
    }

    public com.manydesigns.elements.fields.search.Criteria
    createCriteria(String qualifiedTableName) {
        Session session = getSession(qualifiedTableName);
        Criteria hibernateCriteria = session.createCriteria(qualifiedTableName);
        return new HibernateCriteriaAdapter(hibernateCriteria);
    }

    public List<Map<String, Object>> getObjects(
            com.manydesigns.elements.fields.search.Criteria criteria) {
        HibernateCriteriaAdapter hibernateCriteriaAdapter =
                (HibernateCriteriaAdapter)criteria;
        Criteria hibernateCriteria =
                hibernateCriteriaAdapter.getHibernateCriteria();
        //noinspection unchecked
        return hibernateCriteria.list();
    }

    public void saveOrUpdateObject(Map<String, Object> obj) {
        Session session = getSession((String) obj.get("$type$"));
        session.beginTransaction();
        session.saveOrUpdate((String) obj.get("$type$"), obj);
        session.getTransaction().commit();
    }

    public void saveObject(Map<String, Object> obj) {
        Session session = getSession((String) obj.get("$type$"));
        session.beginTransaction();
        session.save((String) obj.get("$type$"), obj);
        session.getTransaction().commit();
    }

    public void updateObject(Map<String, Object> obj) {
        Session session = getSession((String) obj.get("$type$"));
        session.beginTransaction();
        session.update((String) obj.get("$type$"), obj);
        session.getTransaction().commit();
    }

    public void deleteObject(Map<String, Object> obj) {
        Session session = getSession((String) obj.get("$type$"));
        session.beginTransaction();
        session.delete((String) obj.get("$type$"), obj);
        session.getTransaction().commit();

    }

    public void openSession() {
        Map<String, Session> sessions = new HashMap<String, Session>();

        for (Map.Entry<String, SessionFactory> current : sessionFactories.entrySet()) {
            String databaseName = current.getKey();
            SessionFactory sessionFactory = current.getValue();
            Session session = sessionFactory.openSession();
            sessions.put(databaseName, session);
        }
        threadSessions.set(sessions);
    }


    public void closeSession() {
        Map<String, Session> sessions = threadSessions.get();

        for (Session current : sessions.values()) {
            current.close();
        }

        threadSessions.set(null);
    }

    @SuppressWarnings({"unchecked"})
    public List<Map<String, Object>> getRelatedObjects(
            Map<String, Object> obj, String oneToManyRelationshipName) {
        if (obj.get(oneToManyRelationshipName) instanceof List){
            return (List<Map<String, Object>>)
                    obj.get(oneToManyRelationshipName);
        }
        String qualifiedTableName = (String)obj.get("$type$");
        Relationship relationship =
                dataModel.findOneToManyRelationship(
                        qualifiedTableName, oneToManyRelationshipName);
        Table fromTable = relationship.getFromTable();

        Session session =
                threadSessions.get().get(fromTable.getDatabaseName());
        Criteria criteria =
                session.createCriteria(fromTable.getQualifiedName());
        for (Reference reference : relationship.getReferences()) {
            Column fromColumn = reference.getFromColumn();
            Column toColumn = reference.getToColumn();
            criteria.add(Restrictions.eq(fromColumn.getColumnName(),
                    obj.get(toColumn.getColumnName())));
        }
        return (List<Map<String, Object>>)criteria.list();
    }
}
