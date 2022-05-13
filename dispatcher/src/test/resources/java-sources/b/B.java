import com.manydesigns.portofino.dispatcher.AbstractResource;
import org.apache.shiro.authz.annotation.RequiresPermissions;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

public class B extends AbstractResource {
    public String string = new Inner().string;

    public B() {}

    @Path("2")
    @GET
    public String get2() {
        return "2";
    }

    @Path("secure1")
    @GET
    @RequiresPermissions("secure1")
    public String secure1() {
        return "secure";
    }

    public class Inner {
        public String string = "class B";

        @GET
        public String getString() {
            return string;
        }

        @Path("2")
        @GET
        public String get2() {
            return string + "2";
        }
    }
}
