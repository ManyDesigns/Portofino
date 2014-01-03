import com.manydesigns.elements.ElementsThreadLocals
import com.manydesigns.elements.messages.SessionMessages
import com.manydesigns.portofino.buttons.GuardType
import com.manydesigns.portofino.buttons.annotations.Button
import com.manydesigns.portofino.buttons.annotations.Guard
import com.manydesigns.portofino.di.Inject
import com.manydesigns.portofino.modules.DatabaseModule
import com.manydesigns.portofino.pageactions.activitystream.ActivityStreamAction
import com.manydesigns.portofino.persistence.Persistence
import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions
import com.manydesigns.portofino.tt.TtUtils
import net.sourceforge.stripes.action.Before
import net.sourceforge.stripes.action.ForwardResolution
import net.sourceforge.stripes.action.RedirectResolution
import net.sourceforge.stripes.action.Resolution
import org.apache.commons.lang.StringEscapeUtils
import org.apache.commons.lang.exception.ExceptionUtils
import org.apache.shiro.SecurityUtils
import org.hibernate.Session

@RequiresPermissions(level = AccessLevel.VIEW)
class TicketActivityAction extends ActivityStreamAction {

    public static String TICKET_ACTIVTY_SQL = TtUtils.ACTIVITY_SQL +
            "WHERE act.project = :project_id AND act.n = :ticket_n ORDER BY act.id";


    Serializable project;
    Serializable ticket;

    String comment

    @Before
    public void prepareProject() {
        project = ElementsThreadLocals.getOgnlContext().get("project");
        ticket = ElementsThreadLocals.getOgnlContext().get("ticket");
    }

    @Inject(DatabaseModule.PERSISTENCE)
    private Persistence persistence;

    //**************************************************************************
    // Role checking
    //**************************************************************************

    public boolean isContributor() {
        return TtUtils.principalHasProjectRole(project, TtUtils.ROLE_CONTRIBUTOR);
    }

    //**************************************************************************
    // View
    //**************************************************************************
    @Override
    void populateActivityItems() {
        Locale locale = context.request.locale;
        Session session = persistence.getSession("tt");
        List items = session.createSQLQuery(TICKET_ACTIVTY_SQL).setString("project_id", project.id).setLong("ticket_n", ticket.n).setMaxResults(30).list();

        String keyPrefix = "ticket.";

        String memberImageFormat = String.format("/projects/%s/members?userImage=&userId=%%s&code=%%s", project.id);

        TtUtils.populateActivityItems(items, activityItems, keyPrefix, locale, memberImageFormat);
    }

    @Override
    protected Resolution getViewResolution() {
        return new ForwardResolution("/jsp/projects/tickets/activity.jsp");
    }


    @Button(list = "activity", key = "post.comment")
    @Guard(test = "isContributor()", type = GuardType.VISIBLE)
    public Resolution postComment() {
        try {
            Session session = persistence.getSession("tt");
            Object principal = SecurityUtils.subject.principal;
            Date now = new Date();
            String message = StringEscapeUtils.escapeHtml(comment);
            TtUtils.addActivity(session,
                    principal,
                    now,
                    TtUtils.ACTIVITY_TYPE_COMMENT_CREATED,
                    message,
                    null,
                    null,
                    ticket,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            session.getTransaction().commit();
            SessionMessages.addInfoMessage("Comment posted successfully");
        } catch (Throwable e) {
            String message = "Your comment could not be posted: " + ExceptionUtils.getRootCauseMessage(e);
            SessionMessages.addErrorMessage(message);
        }
        return new RedirectResolution("/projects/$ticket.project/tickets/$ticket.project/$ticket.n")
    }

}