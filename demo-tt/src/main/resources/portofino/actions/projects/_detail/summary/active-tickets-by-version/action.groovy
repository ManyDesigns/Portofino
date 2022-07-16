import com.manydesigns.elements.ElementsThreadLocals
import com.manydesigns.elements.fields.search.SelectSearchField
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
import org.hibernate.query.NativeQuery
import org.hibernate.query.TupleTransformer
import org.hibernate.transform.ResultTransformer
import org.springframework.beans.factory.annotation.Autowired

@RequiresPermissions(level = AccessLevel.VIEW)
class ActiveTicketsByVersionAction extends CustomAction {

    public final static String SQL = """
    select v.id, v.title, count(t.n)
    from tt.tickets t
    left join tt.versions v on v.id = t.fix_version
    where t.project = :project
    and t.state <> 4
    group by v.id, v.title
    order by v.id desc
    """;

    @Autowired
    private Persistence persistence;

    List groups;

    @DefaultHandler
    public Resolution execute() {
        Object project = ElementsThreadLocals.getOgnlContext().get("project");

        Session session = persistence.getSession("tt");
        NativeQuery query = session.createNativeQuery(SQL)
        query.setTupleTransformer(new TupleTransformer<Object>() {
            Object transformTuple(Object[] tuple, String[] aliases) {
                Integer groupId = tuple[0];
                String groupName = tuple[1];
                String groupCode;
                if (groupId == null) {
                    groupCode = SelectSearchField.VALUE_NOT_SET;
                    groupName = "Unassigned"
                } else {
                    groupCode = groupId.toString();
                }
                String url = "/projects/$project.id/tickets?search_state=1&search_state=2&search_state=3&search_fix_version=$groupCode";
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
