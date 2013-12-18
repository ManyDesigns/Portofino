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
import org.hibernate.Session
import org.hibernate.criterion.Order
import org.hibernate.criterion.Restrictions

@RequiresPermissions(level = AccessLevel.VIEW)
class ProjectsRecentlyUpdatedTicketsAction extends CustomAction {

    @Inject(DatabaseModule.PERSISTENCE)
    private Persistence persistence;

    List tickets;

    @DefaultHandler
    public Resolution execute() {
        Object project = ElementsThreadLocals.getOgnlContext().get("project");

        Session session = persistence.getSession("tt");
        tickets = session.createCriteria("tickets")
                .add(Restrictions.eq("project", project.id))
                .addOrder(Order.desc("last_updated"))
                .setMaxResults(10)
                .list();

        return new ForwardResolution("/jsp/common/recently-updated-tickets.jsp");
    }
}