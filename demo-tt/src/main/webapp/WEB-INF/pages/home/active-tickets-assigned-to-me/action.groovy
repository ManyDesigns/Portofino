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
class MyCustomAction extends CustomAction {

    @Inject(DatabaseModule.PERSISTENCE)
    private Persistence persistence;

    List tickets;

    @DefaultHandler
    public Resolution execute() {
        Session session = persistence.getSession("tt");
        Subject subject = SecurityUtils.getSubject();
        if (subject.isAuthenticated()) {
            tickets = session.createCriteria("tickets")
                    .add(Restrictions.eq("assignee", subject.getPrincipal().id))
                    .add(Restrictions.ne("state_id", 4L))
                    .addOrder(Order.asc("project_id"))
                    .addOrder(Order.asc("n"))
                    .list();
        } else {
            tickets = Collections.EMPTY_LIST;
        }

        return new ForwardResolution("/jsp/home/active-tickets-assigned-to-me.jsp");
    }



}