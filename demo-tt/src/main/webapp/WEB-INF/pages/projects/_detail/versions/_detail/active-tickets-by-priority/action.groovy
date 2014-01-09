import com.manydesigns.elements.ElementsThreadLocals
import com.manydesigns.portofino.di.Inject
import com.manydesigns.portofino.modules.DatabaseModule
import com.manydesigns.portofino.pageactions.custom.CustomAction
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

@RequiresPermissions(level = AccessLevel.VIEW)
class ActiveTicketsByPriorityAction extends CustomAction {

    public final static String SQL = """
    select p.id, p.priority, count(t.n)
    from ticket_priorities p
    left join tickets t on (p.id = t.priority and t.fix_version = :version and t.state <> 4)
    group by p.id, p.priority
    order by p.id desc
    """;

    @Inject(DatabaseModule.PERSISTENCE)
        private Persistence persistence;

        List groups;

        @DefaultHandler
        public Resolution execute() {
            Object version = ElementsThreadLocals.getOgnlContext().get("version");

            Session session = persistence.getSession("tt");
            SQLQuery query = session.createSQLQuery(SQL);
            query.setResultTransformer(new ResultTransformer() {
                Object transformTuple(Object[] tuple, String[] aliases) {
                    int groupId = tuple[0];
                    String groupName = tuple[1];
                    String url = "/projects/$version.project/tickets?search_state=1&search_state=2&search_state=3&search_priority=${groupId}&search_fix_version=${version.id}";
                    int groupCount = (int)tuple[2];
                    return new TicketGroup(groupName, url, groupCount);
                }
                List transformList(List collection) {
                    return collection;
                }
            });
            query.setParameter("version", version.id);
            groups = query.list();

            return new ForwardResolution("/jsp/common/ticket-groups.jsp");
        }

}