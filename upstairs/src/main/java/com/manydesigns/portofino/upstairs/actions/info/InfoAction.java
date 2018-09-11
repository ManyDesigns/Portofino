package com.manydesigns.portofino.upstairs.actions.info;

import com.manydesigns.portofino.pageactions.AbstractPageAction;
import com.manydesigns.portofino.security.RequiresAdministrator;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@RequiresAuthentication
@RequiresAdministrator
public class InfoAction extends AbstractPageAction {
    public static final String copyright = "Copyright (C) 2005-2017 ManyDesigns srl";

    public final static Logger logger = LoggerFactory.getLogger(InfoAction.class);

}
