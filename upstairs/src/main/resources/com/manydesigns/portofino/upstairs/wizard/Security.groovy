package com.manydesigns.portofino.upstairs.appwizard

import com.manydesigns.elements.messages.RequestMessages
import com.manydesigns.portofino.persistence.Persistence
import com.manydesigns.portofino.security.SecurityLogic
import com.manydesigns.portofino.shiro.ModelBasedRealm
import org.apache.commons.lang.StringUtils
import org.apache.shiro.authc.*
import org.apache.shiro.crypto.hash.DefaultHashService
import org.apache.shiro.crypto.hash.HashService
import org.apache.shiro.crypto.hash.format.HashFormat
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

import javax.annotation.PostConstruct

class Security extends ModelBasedRealm {

    public static final Logger logger = LoggerFactory.getLogger(Security.class);

    protected String adminGroupName = "$adminGroupName";

    ////Password Hashing
    protected int hashIterations = $hashIterations;
    protected String hashAlgorithm = $hashAlgorithm;
    protected HashFormat hashFormat = $hashFormat;

    @Autowired
    Persistence persistence;

    public Security() {
        if(!"plaintext".equals(hashAlgorithm)) {
            HashService hashService = new DefaultHashService()
            hashService.setHashIterations(hashIterations);
            hashService.setHashAlgorithmName(hashAlgorithm);
            hashService.setGeneratePublicSalt(false); //to enable salting, set this to true and/or call setPrivateSalt
            //Also, if using a public salt, you should uncomment the following, otherwise different Security.groovy
            //instances will fail to match credentials stored in the database
            //hashFormat = new Shiro1CryptFormat()
            setup(hashService, hashFormat);
        }
    }

    @Override
    @PostConstruct
    void configure() {
        try {
            super.configure()
        } catch(IllegalStateException e) {
            logger.warn(e.message, e)
        }
    }

    @Override
    protected Collection<String> loadAuthorizationInfo(Serializable principal) {
        if(!usersGroupsTable || !groupsTable) {
            //Groups were not configured with the wizard. Please place here your own authorization logic.

            /////////////////////////////////////////////////////////////////
            //NB admin is hardcoded for the wizard to work - remove it in production!
            /////////////////////////////////////////////////////////////////
            if("admin" == principal ||
               (principal instanceof Map && "admin" == principal[userNameProperty])) {
                logger.warn("Generated Security.groovy is using the hardcoded 'admin' user; " +
                            "remember to disable it in production!")
                return [SecurityLogic.getAdministratorsGroup(portofinoConfiguration)]
            } else {
                return []
            }
            /////////////////////////////////////////////////////////////////
        } else {
            def groups = super.loadAuthorizationInfo(principal)
            if(!StringUtils.isEmpty(adminGroupName) && groups.contains(adminGroupName)) {
                groups.add(SecurityLogic.getAdministratorsGroup(portofinoConfiguration))
            }
            return groups
        }
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) {
        return loadAuthenticationInfo(token)
    }

    AuthenticationInfo loadAuthenticationInfo(UsernamePasswordToken usernamePasswordToken) {
        if(!usersTable) {
            //Users were not configured with the wizard. Please place here your own authentication logic.
            String userName = usernamePasswordToken.username;
            String password = new String(usernamePasswordToken.password)

            /////////////////////////////////////////////////////////////////
            //NB admin is hardcoded for the wizard to work - remove it in production!
            /////////////////////////////////////////////////////////////////
            if("admin" == userName && "admin" == password) {
                logger.warn("Generated Security.groovy is using the hardcoded 'admin' user; " +
                            "remember to disable it in production!")
                RequestMessages.addWarningMessage("Generated Security.groovy is using the hardcoded 'admin' user; " +
                                                  "remember to disable it in production!")
                SimpleAuthenticationInfo info =
                        new SimpleAuthenticationInfo(userName, encryptPassword(password), getName());
                return info
            } else {
                throw new UnknownAccountException()
            }
            /////////////////////////////////////////////////////////////////
        } else {
            return super.loadAuthenticationInfo(usernamePasswordToken)
        }
    }

    @Override
    void changePassword(Serializable user, String oldPassword, String newPassword) {
        if(!usersTable) {
            throw new UnsupportedOperationException("Users table is not configured");
        }
        super.changePassword(user, oldPassword, newPassword)
    }

    @Override
    Map<Serializable, String> getUsers() {
        if(!usersTable) {
            //Users were not configured with the wizard.
            return Collections.emptyMap();
        }
        super.getUsers()
    }

    Serializable getUserById(String encodedId) {
        if(!usersTable) {
            //Users were not configured with the wizard.
            return encodedId;
        }
        super.getUserById(encodedId)
    }

}

