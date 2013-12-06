import com.manydesigns.portofino.tt.TtUtils

import com.manydesigns.elements.ElementsThreadLocals
import com.manydesigns.elements.messages.SessionMessages
import com.manydesigns.portofino.di.Inject
import com.manydesigns.portofino.modules.DatabaseModule
import com.manydesigns.portofino.pageactions.custom.CustomAction
import com.manydesigns.portofino.persistence.Persistence
import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions
import org.apache.shiro.SecurityUtils
import org.hibernate.Session
import org.hibernate.criterion.Order
import org.hibernate.criterion.Restrictions
import net.sourceforge.stripes.action.*

@RequiresPermissions(level = AccessLevel.VIEW)
class TicketsActivityAction extends CustomAction {

    Serializable ticket;

    String comment

    @Before
    public void prepareProject() {
        ticket = ElementsThreadLocals.getOgnlContext().get("ticket");
    }

    @Inject(DatabaseModule.PERSISTENCE)
    private Persistence persistence;

    List activityItems;

    @DefaultHandler
    public Resolution execute() {
        Session session = persistence.getSession("tt");
        activityItems = session.createCriteria("activity")
                .add(Restrictions.eq("project", ticket.project))
                .add(Restrictions.eq("n", ticket.n))
                .addOrder(Order.asc("id"))
                .list();

        return new ForwardResolution("/jsp/projects/tickets/activity.jsp");
    }

    public Resolution addComment() {
        Session session = persistence.getSession("tt");
        Object principal = SecurityUtils.subject.principal;
        Date now = new Date();
        TtUtils.addActivity(session, ticket, principal.id, now, TtUtils.ACTIVITY_TYPE_COMMENT_CREATED, comment);
        session.getTransaction().commit();
        SessionMessages.addInfoMessage("Comment added successfully");
        return new RedirectResolution("/projects/$ticket.project/tickets/$ticket.project/$ticket.n")
    }

}