/*
 * Copyright (C) 2005-2020 ManyDesigns srl.  All rights reserved.
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
import com.manydesigns.portofino.code.CodeBase;
import com.manydesigns.portofino.security.SecurityLogic;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import org.apache.commons.configuration2.Configuration;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.Key;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Default implementation of PortofinoRealm. Provides convenient implementations of the interface methods.
 *
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public abstract class AbstractPortofinoRealm extends AuthorizingRealm implements PortofinoRealm {
    public static final String copyright = "Copyright (C) 2005-2020 ManyDesigns srl";

    public static final String JWT_EXPIRATION_PROPERTY = "jwt.expiration";
    public static final String JWT_SECRET_PROPERTY = "jwt.secret";

    @Autowired
    protected Configuration portofinoConfiguration;

    @Autowired
    protected CodeBase codeBase;

    protected PasswordService passwordService;

    protected boolean legacyHashing = false;

    private static final Logger logger = LoggerFactory.getLogger(AbstractPortofinoRealm.class);

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
        Jws<Claims> jwt;
        try {
            jwt = Jwts.parser().setSigningKey(key).parseClaimsJws(token.getPrincipal());
        } catch (JwtException e) {
            throw new AuthenticationException(e);
        }
        String credentials = legacyHashing ? token.getCredentials() : encryptPassword(token.getCredentials());
        Object principal = extractPrincipalFromWebToken(jwt);
        return new SimpleAuthenticationInfo(principal, credentials, getName());
    }

    protected Object extractPrincipalFromWebToken(Jws<Claims> jwt) {
        Map<String, Object> body = jwt.getBody();
        String base64Principal = (String) body.get("serialized-principal");
        byte[] serializedPrincipal = Base64.decode(base64Principal);
        Object principal;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(codeBase.asClassLoader()); //In case the serialized principal is a POJO entity
            ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(serializedPrincipal)) {
                @Override
                protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
                    return codeBase.loadClass(desc.getName());
                }
            };
            principal = objectInputStream.readObject();
            objectInputStream.close();
        } catch (Exception e) {
            throw new AuthenticationException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(loader);
        }
        return principal;
    }

    public String generateWebToken(Object principal) {
        Key key = getJWTKey();
        Map<String, Object> claims = new HashMap<>();
        claims.put("principal", getPrincipalForWebToken(principal));
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
        int expireAfterMinutes = portofinoConfiguration.getInt(JWT_EXPIRATION_PROPERTY, 30);
        return Jwts.builder().
                setClaims(claims).
                setExpiration(new DateTime().plusMinutes(expireAfterMinutes).toDate()).
                signWith(key, SignatureAlgorithm.HS512).
                compact();
    }

    protected Object getPrincipalForWebToken(Object principal) {
        return cleanUserPrincipal(principal);
    }

    /**
     * Clean the user principal making it suitable for JSON serialization. For example, if it is a map, remove
     * circular references.
     * @param principal the principal.
     * @return
     */
    protected Object cleanUserPrincipal(Object principal) {
        if(principal instanceof Map) {
            Map cleanUser = new HashMap();
            AtomicBoolean skipped = new AtomicBoolean(false);
            ((Map<?, ?>) principal).forEach((k, v) -> {
                if (v instanceof List || v instanceof Map) {
                    logger.debug("Skipping {}", k);
                    skipped.set(true);
                } else {
                    cleanUser.put(k, v);
                }
            });
            if(skipped.get()) {
                logger.debug("The user entity has potential self-references that make it unusable as a principal, because it must be serializable to JSON. Returning a non-persistent map with no references.");
                return cleanUser;
            } else {
                return principal;
            }
        }
        return principal;
    }

    @NotNull
    protected Key getJWTKey() {
        String secret = portofinoConfiguration.getString(JWT_SECRET_PROPERTY);
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
    public boolean supportsSelfRegistration() {
        return false;
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
    public String[] saveSelfRegisteredUser(Object user) {
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
