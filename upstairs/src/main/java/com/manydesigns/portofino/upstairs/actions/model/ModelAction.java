package com.manydesigns.portofino.upstairs.actions.model;

import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.service.ModelService;
import com.manydesigns.portofino.resourceactions.AbstractResourceAction;
import com.manydesigns.portofino.persistence.Persistence;
import com.manydesigns.portofino.security.RequiresAdministrator;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * @author Alessio Stalla - alessiostalla@gmail.com
 */
@RequiresAuthentication
@RequiresAdministrator
public class ModelAction extends AbstractResourceAction {

    private static final Logger logger = LoggerFactory.getLogger(ModelAction.class);

    @Autowired
    protected ModelService modelService;

    @GET
    public Model getModel() {
        return modelService.getModel();
    }

    @POST
    @Path(":reload")
    public void reloadModel() {
        try {
            modelService.loadModel();
        } catch (IOException e) {
            throw new WebApplicationException("Could not load model", e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

}
