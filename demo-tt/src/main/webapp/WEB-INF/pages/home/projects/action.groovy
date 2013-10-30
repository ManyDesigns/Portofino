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

@RequiresPermissions(level = AccessLevel.VIEW)
class HomeProjectsAction extends CustomAction {

    public final static String ANONYMOUS_SQL = """
    select p.id, p.title, p.description, count(t.n) as c
    from projects p
    left join tickets t on (t.project_id = p.id and t.state_id <>4)
    where p.public
    group by p.id, p.title, p.description
    order by p.id
    """;

    public final static String LOGGED_SQL = """
    select p.id, p.title, p.description, count(t.n) as c
    from projects p
    left join members m on m.project_id = p.id
    left join tickets t on (t.project_id = p.id and t.state_id <>4)
    where p.public = true
    or m.user_id = :user_id
    group by p.id, p.title, p.description
    order by id
    """;

    public final static String LOGGED_SQL2 = """
    select p.id, p.title, p.description, count(t.n) as c
    from projects p
    left join tickets t on (t.project_id = p.id and t.state_id <>4)
    where p.public = true
    union select p.id, p.title, p.description, count(t.n) as c
    from projects p
    join members m on m.project_id = p.id
    left join tickets t on (t.project_id = p.id and t.state_id <>4)
    where p.public = false
    and m.user_id = :user_id
    group by p.id, p.title, p.description
    order by id
    """;

    @Inject(DatabaseModule.PERSISTENCE)
    private Persistence persistence;

    List projects;

    @DefaultHandler
    public Resolution execute() {
        Session session = persistence.getSession("tt");
        Subject subject = SecurityUtils.getSubject();
        if (subject.isAuthenticated()) {
            projects = session.createSQLQuery(LOGGED_SQL)
                    .setParameter("user_id", subject.getPrincipal().id)
                    .list();
        } else {
            projects = session.createSQLQuery(ANONYMOUS_SQL).list();
        }

        return new ForwardResolution("/jsp/home/projects.jsp");
    }

}