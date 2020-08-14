import com.manydesigns.elements.ElementsThreadLocals
import com.manydesigns.portofino.resourceactions.custom.CustomAction
import com.manydesigns.portofino.persistence.Persistence
import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions
import org.apache.shiro.SecurityUtils
import org.apache.shiro.subject.Subject
import org.hibernate.Session
import org.hibernate.criterion.Order
import org.hibernate.criterion.Restrictions
import org.springframework.beans.factory.annotation.Autowired

import javax.ws.rs.GET

@RequiresPermissions(level = AccessLevel.VIEW)
class ProjectsActiveTicketsAssignedToMeAction extends CustomAction {

    @Autowired
    private Persistence persistence;

    @GET
    public List getTickets() {
        Object project = ElementsThreadLocals.getOgnlContext().get("project");

        Session session = persistence.getSession("tt");
        Subject subject = SecurityUtils.getSubject();
        Object principal = subject.getPrincipal();
        if (principal == null) {
            Collections.EMPTY_LIST;
        } else {
            def tickets = session.createCriteria("tickets")
                    .add(Restrictions.eq("project", project.id))
                    .add(Restrictions.eq("assignee", principal.id))
                    .add(Restrictions.ne("state", 4L))
                    .addOrder(Order.asc("n"))
                    .list()
            tickets.collect {
                [ n: it.n, title: it.title, last_updated: it.last_updated?.time ]
            }
        }
    }

}
