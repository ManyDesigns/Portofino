package com.manydesigns.portofino.shiro;

import com.manydesigns.portofino.config.ConfigurationSource;
import org.apache.commons.configuration2.Configuration;
import org.apache.shiro.authc.*;
import org.apache.shiro.authc.pam.UnsupportedTokenException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.subject.PrincipalCollection;

import java.io.Serializable;
import java.util.*;

public class ConfigurationBasedRealm extends AbstractPortofinoRealm {

    protected final ConfigurationSource configuration;

    public ConfigurationBasedRealm(ConfigurationSource configuration) {
        this.configuration = configuration;
    }

    @Override
    public void setup() {
        setup(new PlaintextHashService(), new PlaintextHashFormat());
        legacyHashing = true;
    }

    @Override
    public String encryptPassword(String password) {
        return password;
    }

    @Override
    public Map<Serializable, String> getUsers() {
        Configuration properties = configuration.getProperties();
        List<String> users = properties.getList(String.class, "auth.users", Collections.emptyList());
        Map<Serializable, String> map = new HashMap<>();
        users.forEach(u -> map.put(u, u));
        return map;
    }

    @Override
    public Serializable getUserId(Object user) {
        return (String) user;
    }

    @Override
    public String getUsername(Serializable user) {
        return (String) user;
    }

    @Override
    public String getUserPrettyName(Serializable user) {
        return getUsername(user);
    }

    @Override
    public Serializable getUserById(String encodedUserId) {
        return encodedUserId;
    }

    @Override
    public String getEmail(Serializable user) {
        throw new UnsupportedOperationException("User has no email property.");
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        if (token instanceof UsernamePasswordToken) {
            String username = ((UsernamePasswordToken) token).getUsername();
            String key = "auth.users." + username + ".password";
            String password = new String(((UsernamePasswordToken) token).getPassword());
            if (configuration.getProperties().containsKey(key) &&
                    configuration.getProperties().getString(key).equals(password)) {
                return new SimpleAuthenticationInfo(username, password, getName());
            } else {
                throw new CredentialsException("User not known or password doesn't match");
            }
        } else {
            throw new UnsupportedTokenException(String.valueOf(token));
        }
    }

    @Override
    protected Collection<String> loadAuthorizationInfo(Serializable principal) {
        String key = "auth.users." + principal + ".groups";
        return configuration.getProperties().getList(String.class, key, Collections.emptyList());
    }
}
