import com.manydesigns.portofino.dispatcher.AbstractResourceWithParameters;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

public class Params extends AbstractResourceWithParameters {
    
    public Params() {
        minParameters = 1;
        maxParameters = 2;
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getParameters() {
        parametersAcquired();
        return parameters;
    }
    
}