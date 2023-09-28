/*
* Copyright (C) 2005-2022 ManyDesigns srl.  All rights reserved.
* http://www.manydesigns.com/
*
* Unless you have purchased a commercial license agreement from ManyDesigns srl,
* the following license terms apply:
*
* This program is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License version 3 as published by
* the Free Software Foundation.
*
* There are special exceptions to the terms and conditions of the GPL
* as it is applied to this software. View the full text of the
* exception in file OPEN-SOURCE-LICENSE.txt in the directory of this
* software distribution.
*
* This program is distributed WITHOUT ANY WARRANTY; and without the
* implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see http://www.gnu.org/licenses/gpl.txt
* or write to:
* Free Software Foundation, Inc.,
* 59 Temple Place - Suite 330,
* Boston, MA  02111-1307  USA
*
*/

import com.manydesigns.elements.ElementsThreadLocals
import com.manydesigns.elements.reflection.ClassAccessor
import com.manydesigns.elements.util.RandomUtil
import com.manydesigns.portofino.persistence.Persistence
import com.manydesigns.portofino.persistence.QueryUtils
import com.manydesigns.portofino.security.SecurityLogic
import com.manydesigns.portofino.shiro.*
import com.manydesigns.portofino.tt.Refresh
import org.apache.shiro.authc.*
import org.apache.shiro.crypto.hash.Sha1Hash
import org.hibernate.Session
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

import jakarta.annotation.PostConstruct
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Root
import java.sql.Timestamp

class Security extends AbstractPortofinoRealm {

    public static final String ADMIN_GROUP_NAME = "admin";
    public static final String PROJECT_MANAGER_GROUP_NAME = "project-manager";

    private static final Logger logger = LoggerFactory.getLogger(Security.class);

    @Autowired
    Persistence persistence

    //The following is to verify that user-defined beans are accessible in Security.groovy
    // TODO This stopped working with Groovy 4
    //@Autowired
    Refresh refresh

    @PostConstruct
    void test() {
        logger.info("Refresh: " + refresh)
    }

    //--------------------------------------------------------------------------
    // Authentication
    //--------------------------------------------------------------------------

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) {
        loadAuthenticationInfo(token)
    }

    AuthenticationInfo loadAuthenticationInfo(UsernamePasswordToken usernamePasswordToken) {
        String login = usernamePasswordToken.username;
        String plainTextPassword;
        if (usernamePasswordToken.password == null) {
            plainTextPassword = "";
        } else {
            plainTextPassword = new String(usernamePasswordToken.password);
        }

        String encryptedPassword = encryptPassword(plainTextPassword);
        Session session = persistence.getSession("tt");

        def cdef = QueryUtils.createCriteria(session, 'users')
        def criteria = cdef.query
        def cb = cdef.builder
        def from = cdef.root
        criteria.where(cb.equal(cb.lower(from.get("email")), login?.toLowerCase()))

        Serializable principal = (Serializable) session.createQuery(criteria).uniqueResult()

        if (principal == null) {
            throw new UnknownAccountException("Unknown user");
        } else if (!encryptedPassword.equals(principal.password)) {
            throw new IncorrectCredentialsException("Wrong password");
        } else if (principal.validated == null) {
            throw new DisabledAccountException("User not validated");
        } else {
            logger.debug("Updating access fields.");
            updateAccess(principal, new Date());
            session.update("users", (Object)principal);
            session.getTransaction().commit();
        }

        SimpleAuthenticationInfo info =
                new SimpleAuthenticationInfo(
                        cleanUserPrincipal(principal), plainTextPassword.toCharArray(), getName());
        return info;
    }

    public AuthenticationInfo loadAuthenticationInfo(
            PasswordResetToken passwordResetToken) {
        String token = passwordResetToken.token;
        String newPassword = passwordResetToken.newPassword;

        String encryptedPassword = encryptPassword(newPassword);
        Session session = persistence.getSession("tt");
        def (CriteriaQuery criteria, CriteriaBuilder builder, Root root) =
            QueryUtils.createCriteria(session, "users")
        criteria.where(builder.equal(root.get('token'), token))

        Serializable principal = (Serializable) session.createQuery(criteria).uniqueResult()

        if (principal == null) {
            throw new IncorrectCredentialsException();
        } else {
            logger.debug("Updating fields.");
            updateAccess(principal, new Date());
            principal.token = null;
            if (principal.validated == null) {
                principal.validated = new Date();
            }
            principal.password = encryptedPassword;
            session.update("users", (Object)principal);
            session.getTransaction().commit();
        }

        SimpleAuthenticationInfo info =
                new SimpleAuthenticationInfo(
                        cleanUserPrincipal(principal), token, getName());
        return info;
    }

    public AuthenticationInfo loadAuthenticationInfo(
            SignUpToken signUpToken) {
        String token = signUpToken.token;

        Session session = persistence.getSession("tt")
        def (CriteriaQuery criteria, CriteriaBuilder builder, Root root) =
            QueryUtils.createCriteria(session, "users")
        criteria.where(builder.equal(root.get('token'), token))

        Serializable principal = (Serializable) session.createQuery(criteria).uniqueResult()

        if (principal == null) {
            throw new IncorrectCredentialsException();
        } else {
            logger.debug("Updating fields.");
            Date now = new Date();
            updateAccess(principal, now);
            principal.token = null;
            principal.validated = now;
            session.update("users", (Object)principal);
            session.getTransaction().commit();
        }

        SimpleAuthenticationInfo info =
                new SimpleAuthenticationInfo(
                        cleanUserPrincipal(principal), token, getName());
        return info;
    }

    private void updateAccess(Object principal, Date now) {
        def request = ElementsThreadLocals.getHttpServletRequest()
        principal.last_access = new Timestamp(now.time)
        principal.last_access_ip = request.getRemoteAddr()
    }

    @Override
    protected Object cleanUserPrincipal(principal) {
        Map cleanUser = new HashMap()
        //user.properties.each { k, v -> //use this for POJO persistence
        principal.each { k, v ->
            if (v instanceof List || v instanceof Map) {
                logger.debug("Skipping {}", k)
            } else {
                cleanUser.put(k, v)
            }
        }
        cleanUser
    }

    String encryptPassword(String plainText) {
        Sha1Hash sha1Hash = new Sha1Hash(plainText)
        return sha1Hash.toHex()
    }

    @Override
    boolean supports(AuthenticationToken token) {
        token instanceof PasswordResetToken || token instanceof SignUpToken || super.supports(token)
    }

    //--------------------------------------------------------------------------
    // Authorization
    //--------------------------------------------------------------------------

    @Override
    protected Collection<String> loadAuthorizationInfo(Serializable principal) {
        List<String> result = new ArrayList<String>()
        if (principal.admin) {
            result.add(ADMIN_GROUP_NAME);
            result.add(SecurityLogic.getAdministratorsGroup(configuration.properties))
        }
        if (principal.project_manager) {
            result.add(PROJECT_MANAGER_GROUP_NAME);
        }
        return result;
    }

    //--------------------------------------------------------------------------
    // Users crud
    //--------------------------------------------------------------------------

    @Override
    Map<Serializable, String> getUsers() {
        Session session = persistence.getSession("tt");
        def (criteria, cb, root) = QueryUtils.createCriteria(session, "users")
        List users = session.createQuery(criteria).list()

        Map result = new HashMap();
        for (Serializable user : users) {
            long id = user.id;
            String prettyName = getUserPrettyName(user);
            result.put(id, prettyName);
        }

        return result;
    }

    @Override
    Set<String> getGroups() {
        Set<String> result = super.getGroups();
        result.add(ADMIN_GROUP_NAME);
        result.add(PROJECT_MANAGER_GROUP_NAME);
        return result;
    }

    @Override
    Serializable getUserById(String encodedUserId) {
        Session session = persistence.getSession("tt");
        return (Serializable) session.get("users", Long.parseLong(encodedUserId));
    }

    @Override
    Serializable getUserByEmail(String email) {
        Session session = persistence.getSession("tt");
        def (CriteriaQuery criteria, CriteriaBuilder builder, Root root) =
            QueryUtils.createCriteria(session, "users")
        criteria.where(builder.equal(builder.upper(root.<String>get('email')), email?.toUpperCase()))

        (Serializable) session.createQuery(criteria).uniqueResult()
    }

    @Override
    String getUserPrettyName(Serializable user) {
        "${user.first_name} ${user.last_name}"
    }

    @Override
    Serializable getUserId(Serializable user) {
        user.id
    }

    @Override
    String getUsername(Serializable user) {
        user.email
    }

    @Override
    String getEmail(Serializable user) {
        user.email
    }

    @Override
    boolean supportsSelfRegistration() {
        true
    }

    @Override
    ClassAccessor getSelfRegisteredUserClassAccessor() {
        persistence.getTableAccessor("tt", "users")
    }

    @Override
    String[] saveSelfRegisteredUser(Object principal) {
        Session session = persistence.getSession("tt")
        logger.debug("Check if user already registered. Email: {}", principal.email)
        Object user2 = getUserByEmail(principal.email)
        if (user2 != null) {
            throw new ExistingUserException(principal.email)
        }

        logger.debug("Marking registration ip and date")
        String token = RandomUtil.createRandomId();
        def now = new Date()
        principal.password = encryptPassword(principal.password);
        principal.token = token;
        principal.registration = now;
        def request = ElementsThreadLocals.getHttpServletRequest();
        principal.registration_ip = request.getRemoteAddr();
        principal.admin = false;
        principal.project_manager = false;

        session.persist("users", (Object)principal);
        session.getTransaction().commit();

        [token, principal.email]
    }

    @Override
    void changePassword(Serializable user, String oldPassword, String newPassword) {
        String encryptedOldPassword = encryptPassword(oldPassword);
        String encryptedNewPassword = encryptPassword(newPassword);
        def session = persistence.getSession("tt");
        def q = session.createQuery(
                "update users set password = :newPassword where id = :id and password = :oldPassword", Object)
        q.setParameter("newPassword", encryptedNewPassword);
        q.setParameter("oldPassword", encryptedOldPassword);
        q.setParameter("id", user.id);
        if(q.executeUpdate() != 1) {
            throw new IncorrectCredentialsException("Password not changed");
        }
        session.getTransaction().commit();
    }

    @Override
    String generateOneTimeToken(Serializable principal) {
        Session session = persistence.getSession("tt");
        String token = RandomUtil.createRandomId();
        principal.token = token;
        session.persist("users", (Object)principal);
        session.getTransaction().commit();

        return token;
    }


}
