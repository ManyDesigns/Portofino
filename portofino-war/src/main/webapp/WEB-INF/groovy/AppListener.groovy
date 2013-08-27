import com.manydesigns.portofino.di.Inject
import com.manydesigns.portofino.modules.ApplicationListener
import org.apache.commons.configuration.Configuration
import com.manydesigns.portofino.menu.*
import com.manydesigns.portofino.modules.BaseModule

public class AppListener implements ApplicationListener {

    @Inject(BaseModule.USER_MENU)
    public MenuBuilder userMenu;

    @Inject(BaseModule.PORTOFINO_CONFIGURATION)
    public Configuration configuration;

    UserMenuAppender userMenuAppender;

    boolean applicationStarting() {
        userMenuAppender = new UserMenuAppender(configuration);
        userMenu.menuAppenders.add(userMenuAppender);
        return true;
    }

    void applicationDestroying() {}

}