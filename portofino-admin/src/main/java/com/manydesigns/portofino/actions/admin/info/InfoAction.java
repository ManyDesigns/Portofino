package com.manydesigns.portofino.actions.admin.info;

import com.manydesigns.portofino.security.RequiresAdministrator;
import com.manydesigns.portofino.stripes.AbstractActionBean;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.ForwardResolution;
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
@UrlBinding(InfoAction.URL_BINDING)
public class InfoAction extends AbstractActionBean {
    public static final String copyright = "Copyright (c) 2005-2015, ManyDesigns srl";

    public static final String URL_BINDING = "/actions/admin/info";

    //--------------------------------------------------------------------------
    // Logging
    //--------------------------------------------------------------------------

    public final static Logger logger = LoggerFactory.getLogger(InfoAction.class);

    @DefaultHandler
    public Resolution execute() {
        return new ForwardResolution("/m/admin/info/info.jsp");
    }
}
