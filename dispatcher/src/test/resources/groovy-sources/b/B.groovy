import com.manydesigns.portofino.dispatcher.Node;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

public class B extends Node {
    public String string = new Inner().string;

    @Path("2")
    @GET
    public String get2() {
        return "2";
    }

    public class Inner {
        public String string = "class B";
    }
}
