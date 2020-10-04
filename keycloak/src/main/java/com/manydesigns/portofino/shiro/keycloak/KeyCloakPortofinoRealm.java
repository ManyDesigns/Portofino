package com.manydesigns.portofino.shiro.keycloak;

import com.manydesigns.portofino.shiro.AbstractPortofinoRealm;
import com.manydesigns.portofino.shiro.JSONWebToken;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.keycloak.TokenVerifier;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;

import java.io.Serializable;

public abstract class KeyCloakPortofinoRealm extends AbstractPortofinoRealm {

    public AuthenticationInfo loadAuthenticationInfo(JSONWebToken token) {
        AccessToken jwt;
        TokenVerifier<AccessToken> verifier =
                TokenVerifier.create(token.getPrincipal(), AccessToken.class).withDefaultChecks();
        try {
            verifier.verify();
            jwt = verifier.getToken();
        } catch (VerificationException e) {
            throw new AuthenticationException(e);
        }
        String credentials = legacyHashing ? token.getCredentials() : encryptPassword(token.getCredentials());
        Serializable principal = getPrincipalFromWebToken(jwt);
        return new SimpleAuthenticationInfo(principal, credentials, getName());
    }

    protected abstract Serializable getPrincipalFromWebToken(AccessToken jwt);

}
