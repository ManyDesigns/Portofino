package com.manydesigns.portofino.upstairs.actions.database;

import com.manydesigns.portofino.resourceactions.AbstractResourceAction;
import com.manydesigns.portofino.security.RequiresAdministrator;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Alessio Stalla - alessiostalla@gmail.com
 */
@RequiresAuthentication
@RequiresAdministrator
public class DatabaseAction extends AbstractResourceAction {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseAction.class);

}
