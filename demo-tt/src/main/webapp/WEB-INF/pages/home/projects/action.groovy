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

@RequiresPermissions(level = AccessLevel.VIEW)
class HomeProjectsAction extends CustomAction {

    public final static String sql = """
    select p.id, p.title, p.description, count(t.n) as c
    from projects p
    left join tickets t on (t.project_id = p.id and t.state_id <>4)
    group by p.id, p.title, p.description
    order by p.id
    """;

    @Inject(DatabaseModule.PERSISTENCE)
    private Persistence persistence;

    List projects;

    @DefaultHandler
    public Resolution execute() {
        Session session = persistence.getSession("tt");
        projects = session.createSQLQuery(sql).list();

        return new ForwardResolution("/jsp/home/projects.jsp");
    }

}