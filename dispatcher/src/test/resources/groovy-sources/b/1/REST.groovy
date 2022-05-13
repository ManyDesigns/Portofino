import com.manydesigns.portofino.dispatcher.AbstractResource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;

public class REST extends AbstractResource {
    @GET
    public String get() {
        return "GET";
    }

    @POST
    public String post() {
        return "POST";
    }
}