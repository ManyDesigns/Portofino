package com.manydesigns.portofino.tt

import com.manydesigns.portofino.di.Inject
import com.manydesigns.portofino.modules.DatabaseModule
import com.manydesigns.portofino.pageactions.activitystream.ActivityStreamAction
import com.manydesigns.portofino.persistence.Persistence
import net.sourceforge.stripes.action.RedirectResolution
import net.sourceforge.stripes.action.Resolution
import com.manydesigns.elements.blobs.BlobManager
import com.manydesigns.elements.ElementsThreadLocals
import net.sourceforge.stripes.action.StreamingResolution

class ActivityStreamWithUserImageAction extends ActivityStreamAction {

    @Inject(DatabaseModule.PERSISTENCE)
    public Persistence persistence;

    private Long userId;

    public Resolution userImage() {
        if(userId == null) {
            return new RedirectResolution("/images/user-placeholder-40x40.png");
        }
        Map user = (Map) persistence.getSession("tt").get("users", userId);
        if(user.avatar == null) {
            return new RedirectResolution("/images/user-placeholder-40x40.png");
        } else {
            BlobManager mgr = ElementsThreadLocals.blobManager;
            def blob = mgr.loadBlob(user.avatar);
            return new StreamingResolution(blob.contentType, new FileInputStream(blob.dataFile));
        }
    }

    Long getUserId() {
        return userId
    }

    void setUserId(Long userId) {
        this.userId = userId
    }
}
