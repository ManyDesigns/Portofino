import com.manydesigns.elements.ElementsThreadLocals
import com.manydesigns.portofino.demott.TicketGroup
import com.manydesigns.portofino.di.Inject
import com.manydesigns.portofino.modules.DatabaseModule
import com.manydesigns.portofino.pageactions.custom.CustomAction
import com.manydesigns.portofino.persistence.Persistence
import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions
import net.sourceforge.stripes.action.DefaultHandler
import net.sourceforge.stripes.action.ForwardResolution
import net.sourceforge.stripes.action.Resolution
import org.hibernate.SQLQuery
import org.hibernate.Session
import org.hibernate.transform.ResultTransformer

@RequiresPermissions(level = AccessLevel.VIEW)
class ActiveTicketsByVersionAction extends CustomAction {

    public final static String SQL = """
    select v.id, v.name, count(t.n)
    from tickets t
    left join versions v on v.id = t.fix_version
    where t.project_id = :project_id
    and t.state_id <> 4
    group by v.id, v.name
    order by v.id desc
    """;

    @Inject(DatabaseModule.PERSISTENCE)
        private Persistence persistence;

        List groups;

        @DefaultHandler
        public Resolution execute() {
            Object project = ElementsThreadLocals.getOgnlContext().get("project");

            Session session = persistence.getSession("tt");
            SQLQuery query = session.createSQLQuery(SQL);
            query.setResultTransformer(new ResultTransformer() {
                Object transformTuple(Object[] tuple, String[] aliases) {
                    String groupId = tuple[0];
                    String groupName = tuple[1];
                    if (groupName == null) {
                        groupName = "Unassigned"
                    }
                    String url = "/projects/$project.id/tickets?search_state_id=1&search_state_id=2&search_state_id=3&search_fix_version=$groupId";
                    int groupCount = (int)tuple[2];
                    return new TicketGroup(groupName, url, groupCount);
                }
                List transformList(List collection) {
                    return collection;
                }
            });
            query.setParameter("project_id", project.id);
            groups = query.list();

            return new ForwardResolution("/jsp/common/ticket-groups.jsp");
        }

}