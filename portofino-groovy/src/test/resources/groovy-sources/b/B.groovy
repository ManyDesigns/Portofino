import com.manydesigns.portofino.dispatcher.AbstractResource;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

public class B extends AbstractResource {
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
