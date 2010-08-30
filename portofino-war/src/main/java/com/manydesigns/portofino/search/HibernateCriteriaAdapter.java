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

package com.manydesigns.portofino.search;

import com.manydesigns.elements.fields.search.Criteria;
import com.manydesigns.elements.fields.search.TextMatchMode;
import com.manydesigns.elements.reflection.PropertyAccessor;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;


/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class HibernateCriteriaAdapter implements Criteria {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    protected final Session hibernateSession;
    protected final org.hibernate.Criteria hibernateCriteria;

    public HibernateCriteriaAdapter(Session hibernateSession,
                                    org.hibernate.Criteria hibernateCriteria) {
        this.hibernateSession = hibernateSession;
        this.hibernateCriteria = hibernateCriteria;
    }

    public void eq(PropertyAccessor accessor, Object value) {
        hibernateCriteria.add(Restrictions.eq(accessor.getName(), value));
    }

    public void ne(PropertyAccessor accessor, Object value) {
        hibernateCriteria.add(Restrictions.ne(accessor.getName(), value));
    }

    public void between(PropertyAccessor accessor, Object min, Object max) {
        hibernateCriteria.add(Restrictions.between(accessor.getName(), min, max));
    }

    public void gt(PropertyAccessor accessor, Object value) {
        hibernateCriteria.add(Restrictions.gt(accessor.getName(), value));
    }

    public void ge(PropertyAccessor accessor, Object value) {
        hibernateCriteria.add(Restrictions.ge(accessor.getName(), value));
    }

    public void lt(PropertyAccessor accessor, Object value) {
        hibernateCriteria.add(Restrictions.lt(accessor.getName(), value));
    }

    public void le(PropertyAccessor accessor, Object value) {
        hibernateCriteria.add(Restrictions.le(accessor.getName(), value));
    }

    public void like(PropertyAccessor accessor, String value) {
        hibernateCriteria.add(Restrictions.like(accessor.getName(), value));
    }

    public void like(PropertyAccessor accessor, String value,
                     TextMatchMode textMatchMode) {
        MatchMode hibernateMatchMode = convertTextMatchMode(textMatchMode);
        hibernateCriteria.add(Restrictions.like(
                accessor.getName(), value, hibernateMatchMode));
    }

    public void ilike(PropertyAccessor accessor, String value) {
        hibernateCriteria.add(Restrictions.ilike(accessor.getName(), value));
    }

    public void ilike(PropertyAccessor accessor, String value,
                     TextMatchMode textMatchMode) {
        MatchMode hibernateMatchMode = convertTextMatchMode(textMatchMode);
        hibernateCriteria.add(Restrictions.ilike(
                accessor.getName(), value, hibernateMatchMode));
    }

    protected MatchMode convertTextMatchMode(TextMatchMode textMatchMode) {
        MatchMode hibernateMatchMode;
        switch (textMatchMode) {
            case CONTAINS:
                hibernateMatchMode = MatchMode.ANYWHERE;
                break;
            case ENDS_WITH:
                hibernateMatchMode = MatchMode.END;
                break;
            case EQUALS:
                hibernateMatchMode = MatchMode.EXACT;
                break;
            case STARTS_WITH:
                hibernateMatchMode = MatchMode.START;
                break;
            default:
                throw new IllegalArgumentException(textMatchMode.getLabel());
        }
        return hibernateMatchMode;
    }

    public void sqlRestriction(String sql) {
        hibernateCriteria.add(Restrictions.sqlRestriction(sql));
    }

    public Session getHibernateSession() {
        return hibernateSession;
    }

    public org.hibernate.Criteria getHibernateCriteria() {
        return hibernateCriteria;
    }
}
