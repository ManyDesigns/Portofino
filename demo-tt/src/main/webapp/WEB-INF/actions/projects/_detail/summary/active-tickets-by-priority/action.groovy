import com.manydesigns.elements.ElementsThreadLocals
import com.manydesigns.portofino.resourceactions.custom.CustomAction
import com.manydesigns.portofino.persistence.Persistence
import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions
import com.manydesigns.portofino.tt.TicketGroup
import net.sourceforge.stripes.action.DefaultHandler
import net.sourceforge.stripes.action.ForwardResolution
import net.sourceforge.stripes.action.Resolution
import org.hibernate.SQLQuery
import org.hibernate.Session
import org.hibernate.transform.ResultTransformer
import org.springframework.beans.factory.annotation.Autowired

@RequiresPermissions(level = AccessLevel.VIEW)
class ActiveTicketsByPriorityAction extends CustomAction {

    public final static String SQL = """
    select p.id, p.priority, count(t.n)
    from tt.ticket_priorities p
    left join tt.tickets t on (p.id = t.priority and t.project = :project and t.state <> 4)
    group by p.id, p.priority
    order by p.id desc
    """;

    @Autowired
    private Persistence persistence;

    List groups;

    @DefaultHandler
    public Resolution execute() {
        Object project = ElementsThreadLocals.getOgnlContext().get("project");

        Session session = persistence.getSession("tt");
        SQLQuery query = session.createSQLQuery(SQL);
        query.setResultTransformer(new ResultTransformer() {
            Object transformTuple(Object[] tuple, String[] aliases) {
                int groupId = tuple[0];
                String groupName = tuple[1];
                String url = "/projects/$project.id/tickets?search_state=1&search_state=2&search_state=3&search_priority=$groupId";
                int groupCount = (int)tuple[2];
                return new TicketGroup(groupName, url, groupCount);
            }
            List transformList(List collection) {
                return collection;
            }
        });
        query.setParameter("project", project.id);
        groups = query.list();

        return new ForwardResolution("/jsp/common/ticket-groups.jsp");
    }

}
