import com.manydesigns.portofino.dispatcher.AbstractResource;
import org.apache.shiro.authz.annotation.RequiresPermissions;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

public class REST extends AbstractResource {
    @GET
    public String get() {
        return "GET";
    }

    @POST
    public String post() {
        return "POST";
    }

    @Path("secure1")
    @GET
    @RequiresPermissions("secure1")
    public String secure1() {
        return "secure";
    }
}