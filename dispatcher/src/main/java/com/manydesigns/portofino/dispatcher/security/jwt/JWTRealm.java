package com.manydesigns.portofino.dispatcher.security.jwt;

import com.manydesigns.portofino.dispatcher.security.RolesPermission;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import org.apache.commons.configuration2.Configuration;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import javax.crypto.spec.SecretKeySpec;
import java.io.Serializable;
import java.security.Key;
import java.util.*;

/**
 * Created by alessio on 02/08/16.
 */
public class JWTRealm extends AuthorizingRealm {
    
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        Set<String> roles = getRoles(principals);
        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo(roles);
        simpleAuthorizationInfo.addObjectPermission(new RolesPermission(roles));
        return simpleAuthorizationInfo;
    }

    protected Set<String> getRoles(PrincipalCollection principals) {
        return getRoles(principals.getPrimaryPrincipal());
    }
    
    protected Set<String> getRoles(Object principal) {
        HashSet<String> roles = new HashSet<>();
        if(principal instanceof Map) {
            Object rolesList = ((Map) principal).get("roles");
            if(rolesList instanceof Collection) {
                roles.addAll((Collection<? extends String>) rolesList);
            }
        }
        return roles;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        String secret = getSecret();
        Key key = new SecretKeySpec(Decoders.BASE64.decode(secret), getSignatureAlgorithm().getJcaName());
        
        Jwt jwt = Jwts.parser().
                setSigningKey(key).
                parse((String) token.getPrincipal());
        Map<String, Serializable> principal = getPrincipal(jwt);
        return new SimpleAuthenticationInfo(principal, ((String) token.getCredentials()).toCharArray(), getName());
    }

    protected SignatureAlgorithm getSignatureAlgorithm() {
        return SignatureAlgorithm.HS512;
    }

    protected Map<String, Serializable> getPrincipal(Jwt jwt) {
        Map<String, Serializable> principal = new HashMap<>();
        principal.put("jwt", (Serializable) jwt.getBody());
        return principal;
    }
    
    protected Configuration getConfiguration() {
        return null; //TODO
    }

    protected String getSecret() {
        return getConfiguration().getString("jwt.secret");
    }

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JSONWebToken;
    }
}
