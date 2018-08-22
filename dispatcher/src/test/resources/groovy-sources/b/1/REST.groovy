import com.manydesigns.portofino.dispatcher.AbstractResource;

import javax.ws.rs.GET;
import javax.ws.rs.POST;

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