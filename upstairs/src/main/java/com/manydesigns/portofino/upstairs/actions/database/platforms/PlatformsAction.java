package com.manydesigns.portofino.upstairs.actions.database.platforms;

import com.manydesigns.portofino.model.database.platforms.DatabasePlatform;
import com.manydesigns.portofino.model.database.platforms.DatabasePlatformsRegistry;
import com.manydesigns.portofino.pageactions.AbstractPageAction;
import com.manydesigns.portofino.persistence.Persistence;
import com.manydesigns.portofino.security.RequiresAdministrator;
import org.apache.commons.configuration.Configuration;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.GET;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Alessio Stalla - alessiostalla@gmail.com
 */
@RequiresAuthentication
@RequiresAdministrator
public class PlatformsAction extends AbstractPageAction {

    private static final Logger logger = LoggerFactory.getLogger(PlatformsAction.class);

    @Autowired
    protected Persistence persistence;

    @GET
    public Map<String, DatabasePlatform> listDatabasePlatforms() {
        DatabasePlatformsRegistry manager = persistence.getDatabasePlatformsRegistry();
        DatabasePlatform[] platforms = manager.getDatabasePlatforms();
        Map<String, DatabasePlatform> platformMap = new HashMap<>();
        for (DatabasePlatform platform : platforms) {
            platformMap.put(platform.getClass().getName(), platform);
        }
        return platformMap;
    }

}
