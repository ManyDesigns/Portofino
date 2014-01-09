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
    select p.id, p.title, p.description, count(t.n) as c, true
    from projects p
    left join tickets t on (t.project = p.id and t.state <>4)
    where p.public_
    group by p.id, p.title, p.description
    order by p.id
    """;

    public final static String LOGGED_SQL = """
    select p.id, p.title, p.description, count(t.n) as c, p.public_
    from projects p
    left join members m on m.project = p.id
    left join tickets t on (t.project = p.id and t.state <>4)
    where p.public_ = true
    or m.user_ = :user
    group by p.id, p.title, p.description, p.public_
    order by id
    """;

    public final static String LOGGED_SQL2 = """
    select p.id, p.title, p.description, count(t.n) as c, true
    from projects p
    left join tickets t on (t.project = p.id and t.state <>4)
    where p.public_ = true
    union select p.id, p.title, p.description, count(t.n) as c, false
    from projects p
    join members m on m.project = p.id
    left join tickets t on (t.project = p.id and t.state <>4)
    where p.public_ = false
    and m.user_ = :user
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
        Object principal = subject.getPrincipal();
        if (principal == null) {
            projects = session.createSQLQuery(ANONYMOUS_SQL).list();
        } else {
            projects = session.createSQLQuery(LOGGED_SQL)
                    .setParameter("user", principal.id)
                    .list();
        }

        return new ForwardResolution("/jsp/home/projects.jsp");
    }

}