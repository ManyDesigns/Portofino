package com.manydesigns.portofino.jersey;

import com.manydesigns.mail.rest.SendMailAction;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("/")
public class PortofinoApplication extends ResourceConfig {

    public PortofinoApplication() {
        packages("com.manydesigns.portofino.rest"); //TODO configure user packages
        register(OpenApiResource.class);
        register(JacksonFeature.class);
        try {
            register(new MailInit().getSendMailAction());
        } catch (NoClassDefFoundError e) {
            //Mail not available
        }
    }

    private static class MailInit {
        private Class<?> getSendMailAction() {
            return SendMailAction.class;
        }
    }

}
