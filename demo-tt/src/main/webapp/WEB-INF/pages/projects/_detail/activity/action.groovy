import com.manydesigns.elements.ElementsThreadLocals
import com.manydesigns.portofino.di.Inject
import com.manydesigns.portofino.modules.DatabaseModule
import com.manydesigns.portofino.persistence.Persistence
import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions
import com.manydesigns.portofino.tt.ActivityStreamWithUserImageAction
import com.manydesigns.portofino.tt.TtUtils
import net.sourceforge.stripes.action.Before
import org.hibernate.Session

@RequiresPermissions(level = AccessLevel.VIEW)
class ProjectActivityAction extends ActivityStreamWithUserImageAction {

    public static String PROJECT_ACTIVTY_SQL = TtUtils.ACTIVITY_SQL +
            "WHERE act.project = :project_id ORDER BY act.id DESC";

    Serializable project;

    @Before
    public void prepareProject() {
        project = ElementsThreadLocals.getOgnlContext().get("project");
    }

    @Inject(DatabaseModule.PERSISTENCE)
    private Persistence persistence;

    @Override
    public void populateActivityItems() {
        Locale locale = context.request.locale;
        Session session = persistence.getSession("tt");
        List items = session.createSQLQuery(PROJECT_ACTIVTY_SQL).setString("project_id", project.id).setMaxResults(30).list();

        String keyPrefix = "project.";

        TtUtils.populateActivityItems(items, activityItems, keyPrefix, locale, context);
    }

}