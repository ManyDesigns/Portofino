import com.manydesigns.elements.ElementsThreadLocals
import com.manydesigns.portofino.di.Inject
import com.manydesigns.portofino.modules.DatabaseModule
import com.manydesigns.portofino.pageactions.custom.CustomAction
import com.manydesigns.portofino.persistence.Persistence
import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions
import net.sourceforge.stripes.action.DefaultHandler
import net.sourceforge.stripes.action.ForwardResolution
import net.sourceforge.stripes.action.Resolution
import org.apache.shiro.SecurityUtils
import org.apache.shiro.subject.Subject
import org.hibernate.Session
import org.hibernate.criterion.Order
import org.hibernate.criterion.Restrictions

@RequiresPermissions(level = AccessLevel.VIEW)
class ProjectsActiveTicketsAssignedToMeAction extends CustomAction {

    @Inject(DatabaseModule.PERSISTENCE)
    private Persistence persistence;

    List tickets;

    @DefaultHandler
    public Resolution execute() {
        Object component = ElementsThreadLocals.getOgnlContext().get("component");
        Session session = persistence.getSession("tt");
        Subject subject = SecurityUtils.getSubject();
        Object principal = subject.getPrincipal();
        if (principal == null) {
            tickets = Collections.EMPTY_LIST;
        } else {
            tickets = session.createCriteria("tickets")
                    .add(Restrictions.eq("assignee", principal.id))
                    .add(Restrictions.ne("state", 4L))
                    .addOrder(Order.asc("n"))
                    .createCriteria("fk_affected_component_ticket")
                    .add(Restrictions.eq("component", component.id))
                    .list();
        }

        return new ForwardResolution("/jsp/common/active-tickets-assigned-to-me.jsp");
    }

}