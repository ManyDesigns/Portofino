package com.manydesigns.portofino.demott

import com.manydesigns.elements.reflection.ClassAccessor
import com.manydesigns.elements.reflection.PropertyAccessor
import org.apache.commons.lang.ObjectUtils
import org.hibernate.Session

/**
 * Created by IntelliJ IDEA.
 * User: predo
 * Date: 11/7/13
 * Time: 3:07 PM
 * To change this template use File | Settings | File Templates.
 */
class TtUtils {

    static final public long TICKET_STATE_OPEN = 1L;
    static final public long TICKET_STATE_WORK_IN_PROGRESS = 2L;
    static final public long TICKET_STATE_RESOLVED = 3L;
    static final public long TICKET_STATE_CLOSED = 4L;

    static final public long ACTIVITY_TYPE_CREATED_TICKET = 1L;
    static final public long ACTIVITY_TYPE_UPDATED_TICKET = 2L;
    static final public long ACTIVITY_TYPE_COMMENTED = 10L;
    static final public long ACTIVITY_TYPE_ADDED_ATTACHMENT = 20L;
    static final public long ACTIVITY_TYPE_DELETED_ATTACHMENT = 21L;
    static final public long ACTIVITY_TYPE_ADDED_AFFECTED_COMPONENT = 30L;
    static final public long ACTIVITY_TYPE_DELETED_AFFECTED_COMPONENT = 31L;
    static final public long ACTIVITY_TYPE_STARTED_WORK = 40L;

    static public void addActivity(Session session, Object ticket,
                                   Long userId, Date date, long type,
                                   String message) {
        Map<String, Object> newActivity = new HashMap<String, Object>();
        newActivity.project = ticket.project;
        newActivity.n = ticket.n;
        newActivity.message = message;
        newActivity.type = type;
        newActivity.date = date;
        newActivity.user = userId;
        session.save("activity", (Object)newActivity);
    }

    static public String createDiffMessage(ClassAccessor accessor, Object from, Object to) {
        StringBuilder sb = new StringBuilder();
        for (PropertyAccessor current : accessor.getProperties()) {
            Object fromValue = current.get(from);
            Object toValue = current.get(to);
            if (!ObjectUtils.equals(fromValue, toValue)) {
                String diff = "$current.name: $fromValue -> $toValue";
                sb.append(diff);
            }
        }
        if (sb.length() == 0) {
            return null;
        } else {
            return sb.toString();
        }
    }
}
