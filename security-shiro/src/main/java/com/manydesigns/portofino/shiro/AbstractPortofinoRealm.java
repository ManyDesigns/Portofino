/*
 * Copyright (C) 2005-2022 ManyDesigns srl.  All rights reserved.
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

import com.manydesigns.elements.ognl.OgnlUtils;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.JavaClassAccessor;
import com.manydesigns.portofino.code.CodeBase;
import com.manydesigns.portofino.config.ConfigurationSource;
import com.manydesigns.portofino.security.SecurityLogic;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang3.StringUtils;
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
import org.apache.shiro.crypto.hash.DefaultHashService;
import org.apache.shiro.crypto.hash.HashService;
import org.apache.shiro.crypto.hash.format.Base64Format;
import org.apache.shiro.crypto.hash.format.HashFormat;
import org.apache.shiro.crypto.hash.format.HexFormat;
import org.apache.shiro.crypto.hash.format.Shiro1CryptFormat;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
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
    public static final String copyright = "Copyright (C) 2005-2022 ManyDesigns srl";

    public static final String JWT_EXPIRATION_PROPERTY = "jwt.expiration";
    public static final String JWT_SECRET_PROPERTY = "jwt.secret";
    public static final String HASH_ALGORITHM = "auth.hash.algorithm";
    public static final String HASH_FORMAT = "auth.hash.format";
    public static final String HASH_ITERATIONS = "auth.hash.iterations";
    public static final String PLAINTEXT = "plaintext";

    @Autowired
    protected ConfigurationSource configuration;

    @Autowired
    protected CodeBase codeBase;

    protected PasswordService passwordService;

    protected boolean legacyHashing = false;

    private static final Logger logger = LoggerFactory.getLogger(AbstractPortofinoRealm.class);

    @PostConstruct
    public void setup() {
        Configuration conf = configuration.getProperties();
        String hashAlgorithm = conf.getString(HASH_ALGORITHM, null);
        if (hashAlgorithm != null) {
            if (!PLAINTEXT.equalsIgnoreCase(hashAlgorithm)) {
                DefaultHashService hashService = new DefaultHashService();
                hashService.setHashIterations(conf.getInt(HASH_ITERATIONS, 1));
                hashService.setHashAlgorithmName(hashAlgorithm);
                boolean generatePublicSalt = false; //TODO read from configuration
                HashFormat hashFormat;
                hashService.setGeneratePublicSalt(generatePublicSalt); //to enable salting, set this to true and/or call setPrivateSalt
                if (generatePublicSalt) {
                    //Otherwise different realm instances will fail to match credentials stored in the database
                    hashFormat = new Shiro1CryptFormat();
                } else {
                    hashFormat = getHashFormatFromConfiguration();
                }
                setup(hashService, hashFormat);
            }
        } else {
            //Legacy - let the actual implementation handle hashing
            setup(new PlaintextHashService(), new PlaintextHashFormat());
            legacyHashing = true;
        }
    }

    protected HashFormat getHashFormatFromConfiguration() {
        String formatSpec = configuration.getProperties().getString(HASH_FORMAT, null);
        if (StringUtils.isBlank(formatSpec)) {
            return null;
        } else {
            formatSpec = formatSpec.toLowerCase();
        }
        switch (formatSpec) {
            case "plaintext":
                return null;
            case "hex":
                return new HexFormat();
            case "base64":
                return new Base64Format();
            default:
                try {
                    Class<?> formatClass = Class.forName(formatSpec);
                    if (HashFormat.class.isAssignableFrom(formatClass)) {
                        return (HashFormat) formatClass.getConstructor().newInstance();
                    } else {
                        throw new IllegalArgumentException("Not a hash format class: " + formatSpec);
                    }
                } catch (Exception e) {
                    throw new IllegalArgumentException("Unsupported hash format: " + formatSpec, e);
                }
        }
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
            jwt = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token.getPrincipal());
        } catch (JwtException e) {
            throw new AuthenticationException(e);
        }
        String credentials = legacyHashing ? token.getCredentials() : encryptPassword(token.getCredentials());
        String userId = extractUserIdFromWebToken(jwt);
        return new SimpleAuthenticationInfo(getPrincipal(userId), credentials, getName());
    }

    protected Object getPrincipal(String userId) {
        return userId;
    }

    protected String extractUserIdFromWebToken(Jws<Claims> jwt) {
        Map<String, Object> body = jwt.getBody();
        return (String) body.get("userId");
    }

    public String generateWebToken(Object principal) {
        Key key = getJWTKey();
        Map<String, Object> claims = new HashMap<>();
        fillClaims(principal, claims);
        int expireAfterMinutes = configuration.getProperties().getInt(JWT_EXPIRATION_PROPERTY, 30);
        return Jwts.builder().
                setClaims(claims).
                setExpiration(new DateTime().plusMinutes(expireAfterMinutes).toDate()).
                signWith(key, SignatureAlgorithm.HS512).
                compact();
    }

    protected void fillClaims(Object principal, Map<String, Object> claims) {
        claims.put("userId", OgnlUtils.convertValueToString(getUserId(principal)));
    }

    @NotNull
    protected Key getJWTKey() {
        String secret = configuration.getProperties().getString(JWT_SECRET_PROPERTY);
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
        if(groups.contains(SecurityLogic.getAdministratorsGroup(configuration.getProperties()))) {
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
        groups.add(SecurityLogic.getAllGroup(configuration.getProperties()));
        if (principal == null) {
            groups.add(SecurityLogic.getAnonymousGroup(configuration.getProperties()));
        } else if (principal instanceof Serializable) {
            groups.add(SecurityLogic.getRegisteredGroup(configuration.getProperties()));
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
     * @return the built-in groups.
     */
    public Set<String> getGroups() {
        Set<String> groups = new LinkedHashSet<>();
        groups.add(SecurityLogic.getAllGroup(configuration.getProperties()));
        groups.add(SecurityLogic.getAnonymousGroup(configuration.getProperties()));
        groups.add(SecurityLogic.getRegisteredGroup(configuration.getProperties()));
        groups.add(SecurityLogic.getAdministratorsGroup(configuration.getProperties()));
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
        return JavaClassAccessor.getClassAccessor(UserRegistration.class);
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
