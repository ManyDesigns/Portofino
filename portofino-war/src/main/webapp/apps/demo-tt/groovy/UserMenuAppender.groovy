import com.manydesigns.elements.ElementsThreadLocals
import com.manydesigns.elements.util.Util
import com.manydesigns.portofino.PortofinoProperties
import com.manydesigns.portofino.actions.admin.AdminAction
import com.manydesigns.portofino.logic.SecurityLogic
import com.manydesigns.portofino.menu.Menu
import com.manydesigns.portofino.menu.MenuAppender
import com.manydesigns.portofino.menu.MenuGroup
import com.manydesigns.portofino.menu.MenuLink
import com.manydesigns.portofino.stripes.AbstractActionBean
import javax.servlet.http.HttpServletRequest
import net.sourceforge.stripes.util.UrlBuilder
import org.apache.commons.configuration.Configuration
import org.apache.shiro.SecurityUtils
import org.apache.shiro.subject.Subject

public class UserMenuAppender implements MenuAppender {

    public final Configuration configuration;

    UserMenuAppender(Configuration configuration) {
        this.configuration = configuration
    }

    void append(Menu menu) {
        HttpServletRequest request = ElementsThreadLocals.getHttpServletRequest();
        String originalPath = "/";
        if(request.getAttribute("actionBean") instanceof AbstractActionBean) {
            AbstractActionBean actionBean = (AbstractActionBean) request.getAttribute("actionBean");
            originalPath = actionBean.getOriginalPath();
        }
        String loginPage = configuration.getString(PortofinoProperties.LOGIN_PAGE);

        Subject subject = SecurityUtils.getSubject();
        if(subject.isAuthenticated()) {
            Object principal = subject.getPrincipal();
            String prettyName = "$principal.firstname $principal.lastname";
            MenuGroup userGroup =
                    new MenuGroup("user", "icon-user icon-white",
                                 prettyName, 10.0);
            menu.items.add(userGroup);


            UrlBuilder profileUrlBuilder =
                    new UrlBuilder(Locale.getDefault(), "/", false);
            String profileUrl = Util.getAbsoluteUrl(profileUrlBuilder.toString());
            MenuLink profileLink =
                    new MenuLink("profile", null,
                                 ElementsThreadLocals.getText("skins.default.header.profile"),
                                 profileUrl, 1.0);
            userGroup.menuLinks.add(profileLink);

            UrlBuilder changePasswordUrlBuilder =
                    new UrlBuilder(Locale.getDefault(), loginPage, false);
            changePasswordUrlBuilder.addParameter("returnUrl", originalPath);
            changePasswordUrlBuilder.addParameter("cancelReturnUrl", originalPath);
            changePasswordUrlBuilder.addParameter("changePassword");
            String changePasswordUrl = Util.getAbsoluteUrl(changePasswordUrlBuilder.toString());
            MenuLink changePasswordLink =
                    new MenuLink("change-password", null,
                                 ElementsThreadLocals.getText("skins.default.header.change.password"),
                                 changePasswordUrl, 2.0);
            userGroup.menuLinks.add(changePasswordLink);

            UrlBuilder logoutUrlBuilder =
                    new UrlBuilder(Locale.getDefault(), loginPage, false);
            logoutUrlBuilder.addParameter("returnUrl", originalPath);
            logoutUrlBuilder.addParameter("cancelReturnUrl", originalPath);
            logoutUrlBuilder.addParameter("logout");
            String logoutUrl = Util.getAbsoluteUrl(logoutUrlBuilder.toString());
            MenuLink logoutLink =
                    new MenuLink("logout", null,
                                 ElementsThreadLocals.getText("skins.default.header.log_out"),
                                 logoutUrl, 3.0);
            userGroup.menuLinks.add(logoutLink);

            if(SecurityLogic.isAdministrator(request)) {
                UrlBuilder urlBuilder = new UrlBuilder(request.getLocale(), AdminAction.class, false);
                MenuLink adminLink =
                        new MenuLink("admin", null,
                                     ElementsThreadLocals.getText("skins.default.header.administration"),
                                     request.getContextPath() + urlBuilder.toString(), 1.0);
                menu.items.add(adminLink);
            }
        } else {
            UrlBuilder loginUrlBuilder =
                    new UrlBuilder(Locale.getDefault(), loginPage, false);
            loginUrlBuilder.addParameter("returnUrl", originalPath);
            loginUrlBuilder.addParameter("cancelReturnUrl", originalPath);
            String loginUrl = Util.getAbsoluteUrl(loginUrlBuilder.toString());
            MenuLink loginLink =
                    new MenuLink("login", null,
                                 ElementsThreadLocals.getText("skins.default.header.log_in"),
                                 loginUrl, 1.0);
            menu.items.add(loginLink);

        }

    }

}