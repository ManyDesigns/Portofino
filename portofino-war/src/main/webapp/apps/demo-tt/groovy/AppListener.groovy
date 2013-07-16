import com.manydesigns.portofino.ApplicationAttributes
import com.manydesigns.portofino.application.Application
import com.manydesigns.portofino.di.Inject
import com.manydesigns.portofino.menu.MenuBuilder
import com.manydesigns.portofino.modules.BaseModule
import com.manydesigns.portofino.starter.ApplicationListener
import javax.servlet.ServletContext
import org.apache.commons.configuration.Configuration

public class AppListener implements ApplicationListener {

    @Inject(ApplicationAttributes.USER_MENU)
    public MenuBuilder userMenu;

    @Inject(BaseModule.PORTOFINO_CONFIGURATION)
    public Configuration configuration;

    UserMenuAppender userMenuAppender;

    boolean applicationStarting(Application application, ServletContext servletContext) {
        userMenuAppender = new UserMenuAppender(configuration);
        userMenu.menuAppenders.add(userMenuAppender);
        return true;
    }

    void applicationDestroying(Application application, ServletContext servletContext) {}

}