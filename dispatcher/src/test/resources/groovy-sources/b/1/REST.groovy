import com.manydesigns.portofino.dispatcher.Node;

import javax.ws.rs.GET;
import javax.ws.rs.POST;

public class REST extends Node {
    @GET
    public String get() {
        return "GET";
    }

    @POST
    public String post() {
        return "POST";
    }
}