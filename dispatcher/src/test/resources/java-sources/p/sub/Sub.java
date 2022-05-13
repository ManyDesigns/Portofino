import com.manydesigns.portofino.dispatcher.AbstractResource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

public class Sub extends AbstractResource {

    @Path("3")
    @GET
    public String get3() {
        return "3";
    }

}
