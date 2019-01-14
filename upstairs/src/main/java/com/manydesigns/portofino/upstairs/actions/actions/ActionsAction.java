package com.manydesigns.portofino.upstairs.actions.actions;

import com.manydesigns.portofino.dispatcher.Resource;
import com.manydesigns.portofino.dispatcher.WithParameters;
import com.manydesigns.portofino.pageactions.AbstractPageAction;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Map;

public class ActionsAction extends AbstractPageAction {

    @GET
    @Path("{actionPath:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> getAction(@PathParam("actionPath") String actionPath) {
        Resource resource = getRoot();
        String[] pathSegments = actionPath.split("/");
        for(String segment : pathSegments) {
            if(resource instanceof WithParameters) {
                WithParameters withParameters = (WithParameters) resource;
                if(withParameters.getParameters().size() < withParameters.getMinParameters()) {
                    withParameters.consumeParameter(segment);
                    continue;
                }
            }
            Object subResource;
            try {
                subResource = resource.getSubResource(segment);
            } catch (Exception e) {
                throw new WebApplicationException(e);
            }
            if(subResource instanceof Resource) {
                resource = (Resource) subResource;
            } else {
                logger.error("Not a Resource: " + subResource);
                throw new WebApplicationException();
            }
        }
        if(resource instanceof AbstractPageAction) {
            return ((AbstractPageAction) resource).describe();
        } else {
            logger.error("Not a PageAction: " + resource);
            throw new WebApplicationException();
        }
    }

}
