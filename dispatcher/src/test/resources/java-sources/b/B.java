import com.manydesigns.portofino.dispatcher.Node;
import org.apache.shiro.authz.annotation.RequiresPermissions;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

public class B extends Node {
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
