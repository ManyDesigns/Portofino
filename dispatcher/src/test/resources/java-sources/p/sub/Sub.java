import com.manydesigns.portofino.dispatcher.Node;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

public class Sub extends Node {

    @Path("3")
    @GET
    public String get3() {
        return "3";
    }

}
