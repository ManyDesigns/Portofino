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
class MyCustomAction extends CustomAction {

    //Automatically generated on Mon Oct 28 13:29:47 CET 2013 by ManyDesigns Portofino
    //Write your code here

    @Inject(DatabaseModule.PERSISTENCE)
    private Persistence persistence;

    List projects;

    @DefaultHandler
    public Resolution execute() {
        Session session = persistence.getSession("tt");
        String hql = "from projects order by id"
        projects = session.createQuery(hql).list();

        return new ForwardResolution("/jsp/home/projects.jsp");
    }

}