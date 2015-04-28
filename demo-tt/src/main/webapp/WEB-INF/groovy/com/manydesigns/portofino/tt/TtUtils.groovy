/*
* Copyright (C) 2005-2015 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.tt

import com.manydesigns.elements.fields.Field
import com.manydesigns.elements.forms.FieldSet
import com.manydesigns.elements.forms.Form
import com.manydesigns.elements.reflection.PropertyAccessor
import com.manydesigns.elements.xml.XhtmlBuffer
import com.manydesigns.portofino.pageactions.activitystream.ActivityItem
import com.sksamuel.diffpatch.DiffMatchPatch
import com.sksamuel.diffpatch.DiffMatchPatch.Diff
import org.apache.commons.lang.ObjectUtils
import org.apache.commons.lang.StringUtils
import org.apache.shiro.SecurityUtils
import org.apache.shiro.subject.Subject
import org.hibernate.Session
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
class TtUtils {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";


    public static final Logger logger = LoggerFactory.getLogger(TtUtils.class);

    static final public long TICKET_STATE_OPEN = 1L;
    static final public long TICKET_STATE_WORK_IN_PROGRESS = 2L;
    static final public long TICKET_STATE_RESOLVED = 3L;
    static final public long TICKET_STATE_CLOSED = 4L;

    //--------------------------------------------------------------------------
    // activity_types hardwired values
    //--------------------------------------------------------------------------

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

    static final public long ACTIVITY_TYPE_PROJECT_CREATED = 41L;
    static final public long ACTIVITY_TYPE_PROJECT_UPDATED = 42L;
    static final public long ACTIVITY_TYPE_PROJECT_DELETED = 43L;

    static final public long ACTIVITY_TYPE_USER_CREATED = 51L;
    static final public long ACTIVITY_TYPE_USER_UPDATED = 52L;
    static final public long ACTIVITY_TYPE_USER_DELETED = 53L;

    static final public long ACTIVITY_TYPE_VERSION_CREATED = 61L;
    static final public long ACTIVITY_TYPE_VERSION_UPDATED = 62L;
    static final public long ACTIVITY_TYPE_VERSION_DELETED = 63L;

    static final public long ACTIVITY_TYPE_COMPONENT_CREATED = 71L;
    static final public long ACTIVITY_TYPE_COMPONENT_UPDATED = 72L;
    static final public long ACTIVITY_TYPE_COMPONENT_DELETED = 73L;

    static final public long ACTIVITY_TYPE_ROLE_CREATED = 81L;
    static final public long ACTIVITY_TYPE_ROLE_UPDATED = 82L;
    static final public long ACTIVITY_TYPE_ROLE_DELETED = 83L;

    static final public long ACTIVITY_TYPE_TICKET_RESOLUTION_CREATED = 91L;
    static final public long ACTIVITY_TYPE_TICKET_RESOLUTION_UPDATED = 92L;
    static final public long ACTIVITY_TYPE_TICKET_RESOLUTION_DELETED = 93L;

    static final public long ACTIVITY_TYPE_TICKET_PRIORITY_CREATED = 101L;
    static final public long ACTIVITY_TYPE_TICKET_PRIORITY_UPDATED = 102L;
    static final public long ACTIVITY_TYPE_TICKET_PRIORITY_DELETED = 103L;

    static final public long ACTIVITY_TYPE_TICKET_TYPE_CREATED = 111L;
    static final public long ACTIVITY_TYPE_TICKET_TYPE_UPDATED = 112L;
    static final public long ACTIVITY_TYPE_TICKET_TYPE_DELETED = 113L;

    static final public long ACTIVITY_TYPE_TICKET_STATE_CREATED = 121L;
    static final public long ACTIVITY_TYPE_TICKET_STATE_UPDATED = 122L;
    static final public long ACTIVITY_TYPE_TICKET_STATE_DELETED = 123L;

    static final public long ACTIVITY_TYPE_VERSION_STATE_CREATED = 131L;
    static final public long ACTIVITY_TYPE_VERSION_STATE_UPDATED = 132L;
    static final public long ACTIVITY_TYPE_VERSION_STATE_DELETED = 133L;

    static final public long ACTIVITY_TYPE_MEMBER_CREATED = 141L;
    static final public long ACTIVITY_TYPE_MEMBER_UPDATED = 142L;
    static final public long ACTIVITY_TYPE_MEMBER_DELETED = 143L;

    static final public long ACTIVITY_TYPE_SYSTEM_INSTALLED_SUCCESSFULLY = 151L;

    //--------------------------------------------------------------------------
    // roles hardwired values
    //--------------------------------------------------------------------------

    static final public long ROLE_VIEWER = 1L;
    static final public long ROLE_CONTRIBUTOR = 2L;
    static final public long ROLE_EDITOR = 3L;
    static final public long ROLE_MANAGER = 4L;

    //--------------------------------------------------------------------------
    // Base url properties
    //--------------------------------------------------------------------------

    static final public String BASE_URL_SCHEME_PROPERTY = "base.url.scheme";
    static final public String BASE_URL_SERVER_NAME_PROPERTY = "base.url.server.name";
    static final public String BASE_URL_SERVER_PORT_PROPERTY = "base.url.server.port";
    static final public String BASE_URL_CONTEXT_PATH_PROPERTY = "base.url.context.path";

    //--------------------------------------------------------------------------
    // SQL
    //--------------------------------------------------------------------------


    public static String ACTIVITY_SQL = """
select
    act.id,
    act.timestamp_,
    act.type as type_id,
    at.type,
    act.user_,
    u.first_name,
    u.last_name,
    u.avatar,
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
    a.file as attachment_file,
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

    public final static int ACTIVITY_SQL_ACTIVITY_ID = 0;
    public final static int ACTIVITY_SQL_ACTIVITY_TIMESTAMP = 1;
    public final static int ACTIVITY_SQL_ACTIVITY_TYPE_ID = 2;
    public final static int ACTIVITY_SQL_ACTIVITY_TYPE_TYPE = 3;
    public final static int ACTIVITY_SQL_USER_ID = 4;
    public final static int ACTIVITY_SQL_USER_FIRST_NAME = 5;
    public final static int ACTIVITY_SQL_USER_LAST_NAME = 6;
    public final static int ACTIVITY_SQL_USER_AVATAR = 7;
    public final static int ACTIVITY_SQL_ACTIVITY_NOTIFICATIONS_SENT = 8;
    public final static int ACTIVITY_SQL_ACTIVITY_MESSAGE = 9;
    public final static int ACTIVITY_SQL_USER2_ID = 10;
    public final static int ACTIVITY_SQL_USER2_FIRST_NAME = 11;
    public final static int ACTIVITY_SQL_USER2_LAST_NAME = 12;
    public final static int ACTIVITY_SQL_PROJECT_ID = 13;
    public final static int ACTIVITY_SQL_PROJECT_TITLE = 14;
    public final static int ACTIVITY_SQL_TICKET_N = 15;
    public final static int ACTIVITY_SQL_TICKET_TITLE = 16;
    public final static int ACTIVITY_SQL_ATTACHMENT_ID = 17;
    public final static int ACTIVITY_SQL_ATTACHMENT_TITLE = 18;
    public final static int ACTIVITY_SQL_ATTACHMENT_FILE = 19;
    public final static int ACTIVITY_SQL_VERSION_ID = 20;
    public final static int ACTIVITY_SQL_VERSION_TITLE = 21;
    public final static int ACTIVITY_SQL_COMPONENT_ID = 22;
    public final static int ACTIVITY_SQL_COMPONENT_TITLE = 23;
    public final static int ACTIVITY_SQL_ROLE_ID = 24;
    public final static int ACTIVITY_SQL_ROLE_TITLE = 25;
    public final static int ACTIVITY_SQL_RESOLUTION_ID = 26;
    public final static int ACTIVITY_SQL_RESOLUTION_TITLE = 27;
    public final static int ACTIVITY_SQL_TICKET_PRIORITY_ID = 28;
    public final static int ACTIVITY_SQL_TICKET_PRIORITY_TITLE = 29;
    public final static int ACTIVITY_SQL_TICKET_TYPE_ID = 30;
    public final static int ACTIVITY_SQL_TICKET_TYPE_TITLE = 31;
    public final static int ACTIVITY_SQL_TICKET_STATE_ID = 32;
    public final static int ACTIVITY_SQL_TICKET_STATE_TITLE = 33;
    public final static int ACTIVITY_SQL_VERSION_STATE_ID = 34;
    public final static int ACTIVITY_SQL_VERSION_STATE_TITLE = 35;


    static public void addActivity(Session session,
                                   Object user,
                                   Date date,
                                   long type,
                                   String message,
                                   Object user2,
                                   Object project,
                                   Object ticket,
                                   Object attachment,
                                   Object version,
                                   Object component,
                                   Object role,
                                   Object ticketResolution,
                                   Object ticketPriority,
                                   Object ticketType,
                                   Object ticketState,
                                   Object versionState
    ) {
        Map<String, Object> newActivity = new HashMap<String, Object>();
        newActivity.message = message;
        newActivity.type = type;
        newActivity.timestamp_ = date;
        if (user != null) {
            newActivity.user_ = user.id;
        }
        if (user2 != null) {
            newActivity.user2 = user2.id;
        }
        if (project != null) {
            newActivity.project = project.id;
        }
        if (ticket != null) {
            newActivity.project = ticket.project;
            newActivity.n = ticket.n;
        }
        if (attachment != null) {
            newActivity.attachment = attachment.id;
        }
        if (version != null) {
            newActivity.version = version.id;
        }
        if (component != null) {
            newActivity.component = component.id;
        }
        if (role != null) {
            newActivity.role = role.id;
        }
        if (ticketResolution != null) {
            newActivity.ticket_resolution = ticketResolution.id;
        }
        if (ticketPriority != null) {
            newActivity.ticket_priority = ticketPriority.id;
        }
        if (ticketType != null) {
            newActivity.ticket_type = ticketType.id;
        }
        if (ticketState != null) {
            newActivity.ticket_state = ticketState.id;
        }
        if (versionState != null) {
            newActivity.version_state = versionState.id;
        }
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
                    writeTextWithParagraphs(xb, text);
                    xb.closeElement("ins")
                    break;
                case DiffMatchPatch.Operation.DELETE:
                    xb.openElement("del")
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
        Object principal = subject.getPrincipal();
        if (principal == null) {
            logger.debug("No principal")
            return false;
        }
        long userId = principal.id;
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
                                        String keyPrefix, Locale locale, String imageSrcFormat) {
        for (Object[] item: items) {
            Long userId = item[ACTIVITY_SQL_USER_ID];
            String userName = "${item[ACTIVITY_SQL_USER_FIRST_NAME]} ${item[ACTIVITY_SQL_USER_LAST_NAME]}"
            if (item[ACTIVITY_SQL_USER_FIRST_NAME] == null) {
                userName = "<deleted id ${userId}>";
            }

            Long userId2 = item[ACTIVITY_SQL_USER2_ID];
            String userName2 = "${item[ACTIVITY_SQL_USER2_FIRST_NAME]} ${item[ACTIVITY_SQL_USER2_LAST_NAME]}"
            if (item[ACTIVITY_SQL_USER2_FIRST_NAME] == null) {
                userName2 = "<deleted id ${userId2}>";
            }

            String projectId = item[ACTIVITY_SQL_PROJECT_ID];
            String projectTitle = item[ACTIVITY_SQL_PROJECT_TITLE]
            String projectName = "${projectId} ${projectTitle}"
            String projectHref = "/projects/${projectId}"
            if (projectTitle == null) {
                projectName = "<deleted id ${projectId}>";
                projectHref = null;
            }

            String ticketCode = "${projectId}-${item[ACTIVITY_SQL_TICKET_N]}"
            String ticketHref = "/projects/${projectId}/tickets/${projectId}/${item[ACTIVITY_SQL_TICKET_N]}"
            String ticketTitle = item[ACTIVITY_SQL_TICKET_TITLE];
            if (ticketTitle == null) {
                ticketCode = "<deleted id ${ticketCode}>";
                ticketHref = null;
                ticketTitle = null;
            }

            Long attachmentId = item[ACTIVITY_SQL_ATTACHMENT_ID];
            String attachmentTitle = item[ACTIVITY_SQL_ATTACHMENT_TITLE];
            String attachmentCode = item[ACTIVITY_SQL_ATTACHMENT_FILE];
            String attachmentHref = "${ticketHref}/attachments/${attachmentId}?downloadBlob=&propertyName=file&code=${attachmentCode}";
            if (attachmentTitle == null) {
                attachmentTitle = "<deleted id ${attachmentId}>";
                attachmentHref = null;
            }

            Long versionId = item[ACTIVITY_SQL_VERSION_ID];
            String versionTitle = item[ACTIVITY_SQL_VERSION_TITLE];
            String versionHref = "/projects/${projectId}/versions/${versionId}"
            if (versionTitle == null) {
                versionTitle = "<deleted id ${versionId}>";
                versionHref = null;
            }

            Long componentId = item[ACTIVITY_SQL_COMPONENT_ID];
            String componentTitle = item[ACTIVITY_SQL_COMPONENT_TITLE];
            String componentHref = "/projects/${projectId}/components/${componentId}";
            if (componentTitle == null) {
                componentTitle = "<deleted id ${componentId}>";
                componentHref = null;
            }

            Long roleId = item[ACTIVITY_SQL_ROLE_ID];
            String roleTitle = item[ACTIVITY_SQL_ROLE_TITLE];
            if (roleTitle == null) {
                roleTitle = "<deleted id ${roleId}>";
            }

            Long ticketResolutionId = item[ACTIVITY_SQL_RESOLUTION_ID];
            String ticketResolutionTitle = item[ACTIVITY_SQL_RESOLUTION_TITLE];
            if (ticketResolutionTitle == null) {
                ticketResolutionTitle = "<deleted id ${ticketResolutionId}>";
            }

            Long ticketPriorityId = item[ACTIVITY_SQL_TICKET_PRIORITY_ID];
            String ticketPriorityTitle = item[ACTIVITY_SQL_TICKET_PRIORITY_TITLE];
            if (ticketPriorityTitle == null) {
                ticketPriorityTitle = "<deleted id ${ticketPriorityId}>";
            }

            Long ticketTypeId = item[ACTIVITY_SQL_TICKET_TYPE_ID];
            String ticketTypeTitle = item[ACTIVITY_SQL_TICKET_TYPE_TITLE];
            if (ticketTypeTitle == null) {
                ticketTypeTitle = "<deleted id ${ticketTypeId}>";
            }

            Long ticketStateId = item[ACTIVITY_SQL_TICKET_STATE_ID];
            String ticketStateTitle = item[ACTIVITY_SQL_TICKET_STATE_TITLE];
            if (ticketStateTitle == null) {
                ticketStateTitle = "<deleted id ${ticketStateId}>";
            }

            Long versionStateId = item[ACTIVITY_SQL_VERSION_STATE_ID];
            String versionStateTitle = item[ACTIVITY_SQL_VERSION_STATE_TITLE];
            if (versionStateTitle == null) {
                versionStateTitle = "<deleted id ${versionStateId}>";
            }

            Date timestamp = (Date) item[ACTIVITY_SQL_ACTIVITY_TIMESTAMP];
            String imageSrc = null;
            if (imageSrcFormat != null) {
                imageSrc = String.format(imageSrcFormat, item[ACTIVITY_SQL_USER_ID], item[ACTIVITY_SQL_USER_AVATAR]);
            }
            String imageHref = null;
            String imageAlt = userName;
            String message = item[ACTIVITY_SQL_ACTIVITY_MESSAGE];
            String key = item[ACTIVITY_SQL_ACTIVITY_TYPE_TYPE];
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
            );
            activityItem.addArg(userName, null);
            activityItem.addArg(userName2, null);
            activityItem.addArg(projectName, projectHref);
            activityItem.addArg(ticketCode, ticketHref);
            activityItem.addArg(ticketTitle, null);
            activityItem.addArg(attachmentTitle, attachmentHref);
            activityItem.addArg(versionTitle, versionHref);
            activityItem.addArg(componentTitle, componentHref);
            activityItem.addArg(roleTitle, null);
            activityItem.addArg(ticketResolutionTitle, null);
            activityItem.addArg(ticketPriorityTitle, null);
            activityItem.addArg(ticketTypeTitle, null);
            activityItem.addArg(ticketStateTitle, null);
            activityItem.addArg(versionStateTitle, null);

            activityItems.add(activityItem)
        }
    }

}
