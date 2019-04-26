/*
 * Copyright (C) 2005-2019 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.manydesigns.portofino.shiro;

import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.JavaClassAccessor;
import com.manydesigns.portofino.security.SecurityLogic;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import org.apache.commons.configuration.Configuration;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.credential.PasswordMatcher;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.crypto.hash.HashService;
import org.apache.shiro.crypto.hash.format.HashFormat;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.Key;
import java.util.*;

/**
 * Default implementation of PortofinoRealm. Provides convenient implementations of the interface methods.
 *
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public abstract class AbstractPortofinoRealm extends AuthorizingRealm implements PortofinoRealm {
    public static final String copyright =
            "Copyright (C) 2005-2019 ManyDesigns srl";

    @Autowired
    protected Configuration portofinoConfiguration;

    protected PasswordService passwordService;

    protected boolean legacyHashing = false;

    protected AbstractPortofinoRealm() {
        //Legacy - let the actual implementation handle hashing
        setup(new PlaintextHashService(), new PlaintextHashFormat());
        legacyHashing = true;
    }

    //--------------------------------------------------------------------------
    // Authentication
    //--------------------------------------------------------------------------

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JSONWebToken || super.supports(token);
    }

    public AuthenticationInfo loadAuthenticationInfo(JSONWebToken token) {
        Key key = getJWTKey();
        Jwt jwt;
        try {
            jwt = Jwts.parser().setSigningKey(key).parse(token.getPrincipal());
        } catch (JwtException e) {
            throw new AuthenticationException(e);
        }
        Map body = (Map) jwt.getBody();
        String credentials = legacyHashing ? token.getCredentials() : encryptPassword(token.getCredentials());
        String base64Principal = (String) body.get("serialized-principal");
        byte[] serializedPrincipal = Base64.decode(base64Principal);
        Object principal;
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(serializedPrincipal));
            principal = objectInputStream.readObject();
            objectInputStream.close();
        } catch (Exception e) {
            throw new AuthenticationException(e);
        }
        return new SimpleAuthenticationInfo(principal, credentials, getName());
    }

    public String generateWebToken(Object principal) {
        Key key = getJWTKey();
        Map<String, Object> claims = new HashMap<>();
        claims.put("principal", principal);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream;
        try {
            objectOutputStream = new ObjectOutputStream(bytes);
            objectOutputStream.writeObject(principal);
            objectOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        claims.put("serialized-principal", bytes.toByteArray());
        return Jwts.builder().
                setClaims(claims).
                setExpiration(new DateTime().plusDays(1).toDate()).
                signWith(key, SignatureAlgorithm.HS512).
                compact();
    }

    @NotNull
    protected Key getJWTKey() {
        String secret = portofinoConfiguration.getString("jwt.secret");
        return new SecretKeySpec(Decoders.BASE64.decode(secret), SignatureAlgorithm.HS512.getJcaName());
    }

    // --------------------------------------------------------------------------
    // Authorization
    //--------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     * <p>This default implementation handles built-in groups (all, anonymous, registered, etc.), delegating
     * to loadAuthorizationInfo method the actual loading of application-specific groups.</p>
     *
     * @return
     */
    public AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        Object principal = principals.getPrimaryPrincipal();
        Set<String> groups = getGroups(principal);

        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo(groups);
        if(groups.contains(SecurityLogic.getAdministratorsGroup(portofinoConfiguration))) {
            info.addStringPermission("*");
        }
        Permission permission = new GroupPermission(groups);
        info.setObjectPermissions(Collections.singleton(permission));
        return info;
    }

    @Override
    @NotNull
    public Set<String> getGroups(Object principal) {
        Set<String> groups = new HashSet<>();
        groups.add(SecurityLogic.getAllGroup(portofinoConfiguration));
        if (principal == null) {
            groups.add(SecurityLogic.getAnonymousGroup(portofinoConfiguration));
        } else if (principal instanceof Serializable) {
            groups.add(SecurityLogic.getRegisteredGroup(portofinoConfiguration));
            groups.addAll(loadAuthorizationInfo((Serializable) principal));
        } else {
            throw new AuthorizationException("Invalid principal: " + principal);
        }
        return groups;
    }

    /**
     * Loads the groups associated to a given user.
     * @param principal the user object.
     * @return the groups as a collection of strings.
     */
    protected Collection<String> loadAuthorizationInfo(Serializable principal) {
        return Collections.emptySet();
    }

    //--------------------------------------------------------------------------
    // Groups CRUD
    //--------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     * <p>This default implementation returns the built-in groups
     * (all, anonymous, registered, administrators).
     * You can override it to add custom groups for your application.</p>
     * @return
     */
    public Set<String> getGroups() {
        Set<String> groups = new LinkedHashSet<String>();
        groups.add(SecurityLogic.getAllGroup(portofinoConfiguration));
        groups.add(SecurityLogic.getAnonymousGroup(portofinoConfiguration));
        groups.add(SecurityLogic.getRegisteredGroup(portofinoConfiguration));
        groups.add(SecurityLogic.getAdministratorsGroup(portofinoConfiguration));
        return groups;
    }

    //--------------------------------------------------------------------------
    // Users CRUD
    //--------------------------------------------------------------------------

    @Override
    public Serializable getUserById(String encodedUserId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Serializable getUserByEmail(String email) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClassAccessor getSelfRegisteredUserClassAccessor() {
        return JavaClassAccessor.getClassAccessor(User.class);
    }

    @Override
    public String getUserPrettyName(Serializable user) {
        return user.toString();
    }

    //--------------------------------------------------------------------------
    // User workflow
    //--------------------------------------------------------------------------

    @Override
    public void verifyUser(Serializable user) {
        throw new UnsupportedOperationException();
    }

    //--------------------------------------------------------------------------
    // User password management
    //--------------------------------------------------------------------------

    @Override
    public void changePassword(Serializable user, String oldPassword, String newPassword) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String generateOneTimeToken(Serializable user) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String saveSelfRegisteredUser(Object user) {
        throw new UnsupportedOperationException();
    }

    protected void setup(HashService hashService, HashFormat hashFormat) {
        PortofinoPasswordService passwordService = new PortofinoPasswordService();
        passwordService.setHashService(hashService);
        passwordService.setHashFormat(hashFormat);
        PasswordMatcher passwordMatcher = new PasswordMatcher();
        passwordMatcher.setPasswordService(passwordService);
        setCredentialsMatcher(passwordMatcher);
        this.passwordService = passwordService;
        this.legacyHashing = false;
    }
}
