package com.manydesigns.portofino.tt

import com.manydesigns.elements.fields.Field
import com.manydesigns.elements.forms.FieldSet
import com.manydesigns.elements.forms.Form
import com.manydesigns.elements.reflection.PropertyAccessor
import com.manydesigns.elements.stripes.ElementsActionBeanContext
import com.manydesigns.elements.xml.XhtmlBuffer
import com.manydesigns.portofino.pageactions.activitystream.ActivityItem
import com.manydesigns.portofino.pageactions.activitystream.ActivityItem.Arg
import com.sksamuel.diffpatch.DiffMatchPatch
import com.sksamuel.diffpatch.DiffMatchPatch.Diff
import net.sourceforge.stripes.util.UrlBuilder
import org.apache.commons.lang.ObjectUtils
import org.apache.commons.lang.StringUtils
import org.apache.shiro.SecurityUtils
import org.apache.shiro.subject.Subject
import org.hibernate.Session
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by IntelliJ IDEA.
 * User: predo
 * Date: 11/7/13
 * Time: 3:07 PM
 * To change this template use File | Settings | File Templates.
 */
class TtUtils {

    public static final Logger logger = LoggerFactory.getLogger(TtUtils.class);

    static final public long TICKET_STATE_OPEN = 1L;
    static final public long TICKET_STATE_WORK_IN_PROGRESS = 2L;
    static final public long TICKET_STATE_RESOLVED = 3L;
    static final public long TICKET_STATE_CLOSED = 4L;

    static final public long ACTIVITY_TYPE_TICKET_CREATED = 1L;
    static final public long ACTIVITY_TYPE_TICKET_UPDATED = 2L;
    static final public long ACTIVITY_TYPE_TICKET_DELETED = 3L;
    static final public long ACTIVITY_TYPE_COMMENT_CREATED = 11L;
    static final public long ACTIVITY_TYPE_COMMENT_UPDATED = 12L;
    static final public long ACTIVITY_TYPE_COMMENT_DELETED = 13L;
    static final public long ACTIVITY_TYPE_ATTACHMENT_CREATED = 21L;
    static final public long ACTIVITY_TYPE_ATTACHMENT_UPDATED = 22L;
    static final public long ACTIVITY_TYPE_ATTACHMENT_DELETED = 23L;
    static final public long ACTIVITY_TYPE_AFFECTED_COMPONENT_CREATED = 31L;
    static final public long ACTIVITY_TYPE_AFFECTED_COMPONENT_UPDATED = 32L;
    static final public long ACTIVITY_TYPE_AFFECTED_COMPONENT_DELETED = 33L;

    static final public long ROLE_VIEWER = 1L;
    static final public long ROLE_CONTRIBUTOR = 2L;
    static final public long ROLE_EDITOR = 3L;
    static final public long ROLE_MANAGER = 4L;

    public static String ACTIVITY_SQL = """
select
    act.id,
    act.timestamp_,
    act.type as type_id,
    at.type,
    act.user_,
    u.first_name,
    u.last_name,
    act.notifications_sent,
    act.message,
    act.user2,
    u2.first_name as first_name2,
    u2.last_name as last_name2,
    act.project,
    p.title as project_title,
    act.n,
    t.title as ticket_title,
    act.attachment,
    a.title as attachment_title,
    act.version,
    v.title as version_title,
    act.component,
    c.title as component_title,
    act.role,
    r.role as role_title,
    act.ticket_resolution,
    tr.resolution,
    act.ticket_priority,
    tp.priority,
    act.ticket_type as ticket_type_id,
    tt.type as ticket_type,
    act.ticket_state as ticket_state_id,
    ts.state as ticket_state,
    act.version_state as version_state_id,
    vs.state as version_state
from activity act
join activity_types at on at.id = act.type
left join users u on u.id = act.user_
left join users u2 on u2.id = act.user2
left join projects p on p.id = act.project
left join tickets t on t.project = act.project and t.n = act.n
left join attachments a on a.id = act.attachment
left join versions v on v.id = act.version
left join components c on c.id = act.component
left join roles r on r.id = act.role
left join ticket_resolutions tr on tr.id = act.ticket_resolution
left join ticket_priorities tp on tp.id = act.ticket_priority
left join ticket_types tt on tt.id = act.ticket_type
left join ticket_states ts on ts.id = act.ticket_state
left join version_states vs on vs.id = act.version_state
    """

    static public void addActivity(Session session, Object ticket,
                                   Long userId, Date date, long type,
                                   String message) {
        Map<String, Object> newActivity = new HashMap<String, Object>();
        newActivity.project = ticket.project;
        newActivity.n = ticket.n;
        newActivity.message = message;
        newActivity.type = type;
        newActivity.timestamp_ = date;
        newActivity.user_ = userId;
        session.save("activity", (Object) newActivity);
    }

    static public String createDiffMessage(Form from, Form to) {
        XhtmlBuffer xb = new XhtmlBuffer();
        int diffCount = 0;
        xb.openElement("dl");
        for (FieldSet fieldSet: from) {
            for (Field oldField: fieldSet) {
                PropertyAccessor propertyAccessor = oldField.propertyAccessor
                Field newField = to.findFieldByPropertyName(propertyAccessor.name);
                if (ObjectUtils.equals(oldField.value, newField.value)) {
                    continue;
                }
                diffCount++;
                xb.openElement("dt")
                xb.write(oldField.label);
                xb.closeElement("dt")
                xb.openElement("dd")
                if (propertyAccessor.type == String.class) {
                    DiffMatchPatch diffMatchPatch = new DiffMatchPatch();
                    LinkedList<DiffMatchPatch.Diff> diffs = diffMatchPatch.diff_main(oldField.value, newField.value);
                    diffMatchPatch.diff_cleanupSemantic(diffs);
                    diff_prettyHtml(xb, diffs);
                } else {
                    String oldStringValue = StringUtils.defaultString(oldField.stringValue);
                    String newStringValue = StringUtils.defaultString(newField.stringValue);
                    String diff = "$oldStringValue -> $newStringValue";
                    xb.write(diff);
                }
                xb.closeElement("dd")
            }
        }
        xb.closeElement("dl");
        String result = xb.toString();
        if (diffCount == 0) {
            return null;
        } else {
            return result;
        }
    }

    public static void diff_prettyHtml(XhtmlBuffer xb, LinkedList<Diff> diffs) {
        for (Diff aDiff: diffs) {
            String text = aDiff.text;
            switch (aDiff.operation) {
                case DiffMatchPatch.Operation.INSERT:
                    xb.openElement("ins")
                    xb.addAttribute("style", "background:#e6ffe6;");
                    writeTextWithParagraphs(xb, text);
                    xb.closeElement("ins")
                    break;
                case DiffMatchPatch.Operation.DELETE:
                    xb.openElement("del")
                    xb.addAttribute("style", "background:#ffe6e6;");
                    writeTextWithParagraphs(xb, text);
                    xb.closeElement("del")
                    break;
                case DiffMatchPatch.Operation.EQUAL:
                    xb.openElement("span")
                    writeTextWithParagraphs(xb, text);
                    xb.closeElement("span")
                    break;
            }
        }
    }

    private static void writeTextWithParagraphs(XhtmlBuffer xb, String text) {
        StringTokenizer tokenizer = new StringTokenizer(text, "\n", true)
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if ("\n".equals(token)) {
                xb.writeNoHtmlEscape("&para;");
                xb.writeBr();
            } else {
                xb.write(token);
            }
        }
    }


    static public boolean principalHasProjectRole(Object project, long minimumRole) {
        logger.debug("principalHasProjectRole() - project id: {} - minimum role : {}", project.id, minimumRole)
        Subject subject = SecurityUtils.subject
        if (!subject.isAuthenticated()) {
            logger.debug("Subject not authenticated")
            return false;
        }
        long userId = subject.principal.id;
        for (Object member: project.fk_member_project) {
            def memberUser = member.user_
            def memberRole = member.role
            logger.debug("Member user: {} - member role: {}", memberUser, memberRole)
            if (memberUser == userId && memberRole >= minimumRole) {
                logger.debug("Member found")
                return true;
            }
        }
        logger.debug("Member not found")
        return false;
    }

    public static populateActivityItems(List items, List<ActivityItem> activityItems,
                                        String keyPrefix, Locale locale, ElementsActionBeanContext context) {
        for (Object[] item: items) {
            String userName = "${item[5]} ${item[6]}"

            String ticketCode = "${item[12]}-${item[14]}"
            String ticketHref = "/projects/${item[12]}/tickets/${item[12]}/${item[14]}"
            String ticketTitle = "${item[15]}";

            Date timestamp = (Date) item[1];
            String imageSrc =
            new UrlBuilder(Locale.getDefault(), context.actionPath, false).
                    setEvent("userImage").
                    addParameter("userId", item[4]).
                    toString();
            String imageHref = null;
            String imageAlt = userName;
            String message = item[8];
            String key = item[3];
            if (keyPrefix != null) {
                key = keyPrefix + key;
            }
            ActivityItem activityItem = new ActivityItem(
                    locale,
                    timestamp,
                    imageSrc,
                    imageHref,
                    imageAlt,
                    message,
                    key,
                    new Arg(userName, null),
                    new Arg(ticketCode, ticketHref),
                    new Arg(ticketTitle, null),
            );
            activityItems.add(activityItem)
        }
    }

}
