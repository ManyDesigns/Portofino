import com.manydesigns.elements.ElementsThreadLocals
import com.manydesigns.portofino.persistence.QueryUtils
import com.manydesigns.portofino.resourceactions.custom.CustomAction
import com.manydesigns.portofino.persistence.Persistence
import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions
import org.apache.shiro.SecurityUtils
import org.apache.shiro.subject.Subject
import org.hibernate.Session
import org.springframework.beans.factory.annotation.Autowired

import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.Root
import javax.ws.rs.GET

@RequiresPermissions(level = AccessLevel.VIEW)
class ProjectsActiveTicketsAssignedToMeAction extends CustomAction {

    @Autowired
    private Persistence persistence

    @GET
    public List getTickets() {
        Object project = ElementsThreadLocals.getOgnlContext().get("project")

        Session session = persistence.getSession("tt")
        Subject subject = SecurityUtils.getSubject()
        Object principal = subject.getPrincipal()
        if (principal == null) {
            Collections.EMPTY_LIST
        } else {
            def (CriteriaQuery<Object> criteria, CriteriaBuilder cb, Root from) =
                QueryUtils.createCriteria(session, 'tickets')
            criteria.where(
                    cb.equal(from.get("project"), project.id),
                    cb.equal(from.get("assignee"), principal.id),
                    cb.equal(from.get("state"), 4L))
            criteria.orderBy(cb.asc(from.get("n")))
            def tickets = session.createQuery(criteria).list()
            tickets.collect {
                [ n: it.n, title: it.title, last_updated: it.last_updated?.time ]
            }
        }
    }

}
