package com.manydesigns.portofino.resourceactions.login.keycloak;

import com.manydesigns.portofino.resourceactions.login.DefaultLoginAction;
import com.manydesigns.portofino.shiro.JSONWebToken;
import com.manydesigns.portofino.shiro.JWTFilter;
import com.manydesigns.portofino.shiro.PortofinoRealm;
import com.manydesigns.portofino.shiro.ShiroUtils;
import org.apache.commons.configuration2.Configuration;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.Response;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KeyCloakLoginAction extends DefaultLoginAction {

    protected final Map<Serializable, String> refreshTokens = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(KeyCloakLoginAction.class);

    @Override
    protected String doLogin(String username, String password) {
        Invocation.Builder request = createTokenRequest();
        Form form = prepareForm();
        form.param("grant_type", "password");
        form.param("username", username);
        form.param("password", password);
        PortofinoRealm portofinoRealm = ShiroUtils.getPortofinoRealm();
        Subject subject = SecurityUtils.getSubject();
        String jwt = authenticate(request, form, subject);
        return userInfo(subject, portofinoRealm, jwt);
    }

    @Override
    public void logout() {
        Subject subject = SecurityUtils.getSubject();
        if(subject.isAuthenticated()) {
            forgetRefreshToken((Serializable) subject.getPrincipal());
        }
        super.logout();
    }

    protected String authenticate(Invocation.Builder request, Form form, Subject subject) {
        Response response = request.post(Entity.form(form));
        String jwt;
        if(response.getStatus() == Response.Status.OK.getStatusCode()) {
            Map entity = response.readEntity(Map.class);
            jwt = (String) entity.get("access_token");
            subject.login(new JSONWebToken(jwt));
            String refreshToken = (String) entity.get("refresh_token");
            saveRefreshToken((Serializable) subject.getPrincipal(), refreshToken);
        } else {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        return jwt;
    }

    protected Invocation.Builder createTokenRequest() {
        Client client = ClientBuilder.newClient();
        String kRealmUrl = portofinoConfiguration.getProperties().getString("keycloak.realm.url");
        if(!kRealmUrl.endsWith("/")) {
            kRealmUrl = kRealmUrl + "/";
        }
        WebTarget target = client.target(kRealmUrl + "protocol/openid-connect/token");
        Invocation.Builder request = target.request();
        return request;
    }

    protected void saveRefreshToken(Serializable principal, String refreshToken) {
        Serializable userId = ShiroUtils.getPortofinoRealm().getUserId(principal);
        refreshTokens.put(userId, refreshToken);
    }

    protected String getRefreshToken(Serializable principal) {
        Serializable userId = ShiroUtils.getPortofinoRealm().getUserId(principal);
        return refreshTokens.get(userId);
    }

    protected void forgetRefreshToken(Serializable principal) {
        Serializable userId = ShiroUtils.getPortofinoRealm().getUserId(principal);
        refreshTokens.remove(userId);
    }

    @Override
    public String refreshToken() {
        Subject subject = SecurityUtils.getSubject();
        if(subject.isAuthenticated()) {
            Object principal = subject.getPrincipal();
            String refreshToken = getRefreshToken((Serializable) principal);
            if(refreshToken == null) {
                return JWTFilter.getJSONWebToken(context.getRequest());
            }
            subject.logout();

            Invocation.Builder request = createTokenRequest();
            Form form = prepareForm();
            form.param("grant_type", "refresh_token");
            form.param("refresh_token", refreshToken);
            return authenticate(request, form, subject);
        } else {
            logger.warn("Token refresh request for unauthenticated user");
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
    }

    @NotNull
    protected Form prepareForm() {
        Configuration conf = portofinoConfiguration.getProperties();
        String clientId = conf.getString("keycloak.client.id", "portofino");
        Form form = new Form();
        form.param("client_id", clientId);
        form.param("client_secret", conf.getString("keycloak.client.secret"));
        form.param("scope", "openid");
        return form;
    }


}
