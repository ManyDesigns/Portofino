package com.manydesigns.portofino.pageactions.google;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manydesigns.elements.ElementsThreadLocals;
import com.manydesigns.elements.messages.SessionMessages;
import com.manydesigns.portofino.pageactions.PageActionName;
import com.manydesigns.portofino.pageactions.annotations.ScriptTemplate;
import com.manydesigns.portofino.pageactions.login.DefaultLoginAction;
import com.manydesigns.portofino.shiro.google.GoogleToken;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.Subject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.MessageFormat;
import java.util.Map;

@ScriptTemplate("script_template_google.groovy")
@PageActionName("Google Login")
public class GoogleLoginAction extends DefaultLoginAction {

    protected static final String GOOGLE_API_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

    @Override
    protected String getLoginPage() {
        return "/m/google/pageactions/google/googleLogin.jsp";
    }

    public Resolution googleCallback() {
        return new RedirectResolution("/m/google/pageactions/google/googleCallback.jsp");
    }

    public Resolution googleCallbackJS() {
        String errorMessage = ElementsThreadLocals.getText("google.error.message");

        HttpServletRequest servletContext = context.getRequest();
        String error = servletContext.getParameter("error");
        String accessToken = servletContext.getParameter("access_token");
        if (error != null || accessToken == null) {
            SessionMessages.addErrorMessage(errorMessage);
            logger.error(errorMessage);
            return new RedirectResolution(pageInstance.getPath());
        } else {
            Client client = ClientBuilder.newClient();
            WebTarget webTarget = client.target(GOOGLE_API_URL);
            Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON_TYPE)
                    .header("Authorization", "Bearer " + accessToken);
            Response response = invocationBuilder.get();
            if (response == null || response.getStatus() != 200) {
                SessionMessages.addErrorMessage(errorMessage);
                logger.error(errorMessage);
            } else {
                String l = response.readEntity(String.class);
                logger.debug("Google response: {}", l);
                ObjectMapper mapper = new ObjectMapper();
                Map m = null;
                try {
                    m = mapper.readValue(l, Map.class);
                    Subject subject = SecurityUtils.getSubject();
                    if (!subject.isAuthenticated()) {
                        GoogleToken gt = GoogleToken.fromMap(m);
                        subject.login(gt);
                    }
                    logger.info("Google data retrieved successfully for User with email '{}'", m.get("email"));
                } catch (AuthenticationException e) {
                    String errMsg = MessageFormat.format(
                            ElementsThreadLocals.getText("login.failed.for.user._"), (m != null ? m.get("email"): null));
                    logger.warn(errMsg);
                    SessionMessages.addErrorMessage(errMsg);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
        }
        return new RedirectResolution(pageInstance.getPath());
    }
}
