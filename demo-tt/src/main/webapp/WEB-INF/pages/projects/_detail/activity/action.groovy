import com.manydesigns.elements.ElementsThreadLocals
import com.manydesigns.portofino.di.Inject
import com.manydesigns.portofino.modules.DatabaseModule
import com.manydesigns.portofino.pageactions.custom.CustomAction
import com.manydesigns.portofino.persistence.Persistence
import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions
import net.sourceforge.stripes.action.Before
import net.sourceforge.stripes.action.DefaultHandler
import net.sourceforge.stripes.action.ForwardResolution
import net.sourceforge.stripes.action.Resolution
import org.hibernate.Session
import org.hibernate.criterion.Order
import org.hibernate.criterion.Restrictions

@RequiresPermissions(level = AccessLevel.VIEW)
class MyCustomAction extends CustomAction {

    Serializable project;

    @Before
    public void prepareProject() {
        project = ElementsThreadLocals.getOgnlContext().get("project");
    }

    @Inject(DatabaseModule.PERSISTENCE)
    private Persistence persistence;

    List activityItems;



    @DefaultHandler
    public Resolution execute() {
        Session session = persistence.getSession("tt");
        activityItems = session.createCriteria("activity")
                .add(Restrictions.eq("project", project.id))
                .addOrder(Order.desc("id"))
                .list();

        return new ForwardResolution("/jsp/projects/activity.jsp");
    }

}