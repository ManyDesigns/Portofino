import com.manydesigns.elements.ElementsThreadLocals
import com.manydesigns.portofino.ApplicationAttributes
import com.manydesigns.portofino.actions.admin.AdminAction
import com.manydesigns.portofino.application.Application
import com.manydesigns.portofino.di.Inject
import com.manydesigns.portofino.logic.SecurityLogic
import com.manydesigns.portofino.shiro.ShiroUtils
import com.manydesigns.portofino.starter.ApplicationListener
import com.manydesigns.portofino.stripes.AbstractActionBean
import javax.servlet.ServletContext
import javax.servlet.http.HttpServletRequest
import net.sourceforge.stripes.util.UrlBuilder
import org.apache.commons.configuration.Configuration
import org.apache.shiro.SecurityUtils
import org.apache.shiro.subject.Subject
import com.manydesigns.portofino.menu.*

public class AppListener implements ApplicationListener {

    @Inject(ApplicationAttributes.USER_MENU)
    public MenuBuilder userMenu;

    @Inject(ApplicationAttributes.PORTOFINO_CONFIGURATION)
    public Configuration configuration;


    boolean applicationStarting(Application application, ServletContext servletContext) {
        appendToUserMenu();
        return true;
    }

    void applicationDestroying(Application application, ServletContext servletContext) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    protected void appendToUserMenu() {
        userMenu.menuAppenders.add(new MenuAppender() {
            @Override
            public void append(Menu menu) {
                Subject subject = SecurityUtils.getSubject();
                if(!subject.isAuthenticated()) {
                    HttpServletRequest request = ElementsThreadLocals.getHttpServletRequest();
                    String originalPath = "/";
                    if(request.getAttribute("actionBean") instanceof AbstractActionBean) {
                        AbstractActionBean actionBean = (AbstractActionBean) request.getAttribute("actionBean");
                        originalPath = actionBean.getOriginalPath();
                    }
                    String loginLinkHref = ShiroUtils.getLoginLink(
                            configuration, request.getContextPath(), originalPath, originalPath);
                    MenuLink loginLink =
                            new MenuLink("login", null,
                                         ElementsThreadLocals.getText("skins.default.header.log_in"),
                                         loginLinkHref);
                    menu.items.add(loginLink);
                }

            }
        });

        userMenu.menuAppenders.add(new MenuAppender() {
            @Override
            public void append(Menu menu) {
                HttpServletRequest request = ElementsThreadLocals.getHttpServletRequest();
                if(SecurityLogic.isAdministrator(request)) {
                    int index = 0;
                    for(MenuItem item : menu.items) {
                        if("logout".equals(item.id)) {
                            break;
                        }
                        index++;
                    }

                    UrlBuilder urlBuilder = new UrlBuilder(request.getLocale(), AdminAction.class, false);
                    MenuLink adminLink =
                            new MenuLink("admin", null,
                                         ElementsThreadLocals.getText("skins.default.header.administration"),
                                         request.getContextPath() + urlBuilder.toString());
                    menu.items.add(index, adminLink);
                }

            }
        });
    }


}