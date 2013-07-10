import com.manydesigns.portofino.shiro.User
import com.manydesigns.elements.annotations.Required

public class DemoUser extends User {

    @Required
    public String firstname;

    @Required
    public String lastname;

}