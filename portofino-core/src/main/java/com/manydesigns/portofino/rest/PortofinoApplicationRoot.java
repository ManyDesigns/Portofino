package com.manydesigns.portofino.rest;

import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.util.MimeTypes;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.dispatcher.DispatcherUtil;
import com.manydesigns.portofino.dispatcher.web.ApplicationRoot;
import com.manydesigns.portofino.modules.BaseModule;
import com.manydesigns.portofino.navigation.Navigation;
import com.manydesigns.portofino.navigation.NavigationItem;
import com.manydesigns.portofino.pages.Page;
import org.apache.commons.configuration.Configuration;
import org.apache.shiro.SecurityUtils;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;

@Path("/")
public class PortofinoApplicationRoot extends ApplicationRoot {

    @Context
    protected ServletContext servletContext;

    @Context
    protected HttpServletResponse response;

    @Context
    protected UriInfo uriInfo;

    @Path(":pages")
    @GET
    @Produces(MimeTypes.APPLICATION_JSON_UTF8)
    public NavigationItem getPages() {
        Configuration configuration = (Configuration) servletContext.getAttribute(BaseModule.PORTOFINO_CONFIGURATION);
        String landingPage = configuration.getString(PortofinoProperties.LANDING_PAGE);
        if(landingPage != null) {
            HttpServletRequest request = ElementsThreadLocals.getHttpServletRequest();
            return new Navigation(
                    configuration, DispatcherUtil.get(request).getDispatch(landingPage), SecurityUtils.getSubject(), false).
                    getRootNavigationItem();
        } else {
            return null;
        }
    }

    /**
     * Returns a fixed description simulating AbstractPageAction#getPageDescription.
     * @since 4.2.2
     * @return the description as JSON.
     */
    @Path(":page")
    @GET
    @Produces(MimeTypes.APPLICATION_JSON_UTF8)
    public Map<String, Object> getPageDescription() {
        Map<String, Object> description = new HashMap<String, Object>();
        description.put("superclass", PortofinoRoot.class.getName());
        description.put("class", null);
        description.put("page", new Page());
        return description;
    }

}
