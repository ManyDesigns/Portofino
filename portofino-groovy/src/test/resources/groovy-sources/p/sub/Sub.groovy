import com.manydesigns.portofino.dispatcher.AbstractResource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

class Sub extends AbstractResource {

    @Path("3")
    @GET
    public String get3() {
        return "3";
    }

}
