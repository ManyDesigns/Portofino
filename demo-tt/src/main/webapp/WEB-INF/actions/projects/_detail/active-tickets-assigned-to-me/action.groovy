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

import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Root
import jakarta.ws.rs.GET

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
            def criteria = QueryUtils.createCriteria(session, 'tickets')
            criteria.query.where(
                    criteria.builder.equal(criteria.root.get("project"), project.id),
                    criteria.builder.equal(criteria.root.get("assignee"), principal.id),
                    criteria.builder.equal(criteria.root.get("state"), 4L))
            criteria.query.orderBy(criteria.builder.asc(criteria.root.get("n")))
            def tickets = session.createQuery(criteria.query).list()
            tickets.collect {
                [ n: it.n, title: it.title, last_updated: it.last_updated?.time ]
            }
        }
    }

}
