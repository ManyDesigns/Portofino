package com.manydesigns.portofino.actions.admin.info;

import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.ForwardResolution;

/**
 * @author Emanuele Poggi        - emanuele.poggi@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */

@UrlBinding(InfoAction.URL_BINDING)
public class InfoAction {
    public static final String copyright = "Copyright (c) 2005-2015, ManyDesigns srl";

    public static final String URL_BINDING = "/actions/admin/info";

    @DefaultHandler
    public Resolution execute() {
        return new ForwardResolution("/m/admin/info/info.jsp");
    }
}
