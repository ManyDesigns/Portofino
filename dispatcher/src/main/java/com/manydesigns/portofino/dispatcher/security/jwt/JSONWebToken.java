package com.manydesigns.portofino.dispatcher.security.jwt;

import org.apache.shiro.authc.AuthenticationToken;

/**
 * Created by alessio on 7/6/16.
 */
public class JSONWebToken implements AuthenticationToken {

    protected final String token;

    public JSONWebToken(String token) {
        this.token = token;
    }

    @Override
    public Object getPrincipal() {
        return token;
    }

    @Override
    public Object getCredentials() {
        return token;
    }
}
