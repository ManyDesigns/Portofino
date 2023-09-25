package portofino.actions.admin.activity

import com.manydesigns.elements.blobs.Blob
import com.manydesigns.elements.blobs.BlobManager
import com.manydesigns.portofino.persistence.Persistence
import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions
import com.manydesigns.portofino.spring.PortofinoSpringConfiguration
import com.manydesigns.portofino.tt.TtUtils
import net.sourceforge.stripes.action.RedirectResolution
import net.sourceforge.stripes.action.Resolution
import net.sourceforge.stripes.action.StreamingResolution
import org.apache.shiro.authz.annotation.RequiresAuthentication
import org.hibernate.Session
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

@RequiresAuthentication
@RequiresPermissions(level = AccessLevel.VIEW)
class MyActivityStreamAction extends ActivityStreamAction {

    public static String SYSTEM_ACTIVTY_SQL = TtUtils.ACTIVITY_SQL +
            "WHERE act.project IS NULL ORDER BY act.id DESC";

    @Autowired
    protected Persistence persistence;

    @Autowired
    @Qualifier(PortofinoSpringConfiguration.DEFAULT_BLOB_MANAGER)
    protected BlobManager blobManager;

    @Override
    public void populateActivityItems() {
        Locale locale = context.request.locale;
        Session session = persistence.getSession("tt");
        List items = session.createNativeQuery(SYSTEM_ACTIVTY_SQL).setMaxResults(30).list();

        String keyPrefix = "system.";

        String userImageFormat = "/admin/activity?userImage=&userId=%s&code=%s";

        TtUtils.populateActivityItems(items, activityItems, keyPrefix, locale, userImageFormat);
    }

    //**************************************************************************
    // member image
    //**************************************************************************

    private Long userId;

    public Resolution userImage() {
        if(userId == null) {
            return new RedirectResolution("/images/user-placeholder-40x40.png");
        }
        Map user = (Map) persistence.getSession("tt").get("users", userId);
        if(user.avatar == null) {
            return new RedirectResolution("/images/user-placeholder-40x40.png");
        } else {
            Blob blob = new Blob(user.avatar);
            blobManager.loadMetadata(blob);
            long contentLength = blob.size;
            String contentType = blob.contentType;
            InputStream inputStream = blobManager.openStream(blob);

            return new StreamingResolution(contentType, inputStream)
                    .setLength(contentLength)
                    .setLastModified(blob.getCreateTimestamp().getMillis());
        }
    }

    Long getUserId() {
        return userId
    }

    void setUserId(Long userId) {
        this.userId = userId
    }

}
