package com.manydesigns.portofino.shiro;

import com.manydesigns.elements.util.RandomUtil;
import com.manydesigns.portofino.model.database.Column;
import com.manydesigns.portofino.model.database.DatabaseLogic;
import com.manydesigns.portofino.model.database.Table;
import com.manydesigns.portofino.persistence.Persistence;
import com.manydesigns.portofino.persistence.QueryUtils;
import com.manydesigns.portofino.reflection.TableAccessor;
import com.manydesigns.portofino.util.PkHelper;
import groovy.lang.Tuple3;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authc.*;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.query.Query;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public abstract class ModelBasedRealm extends AbstractPortofinoRealm {

    protected Table usersTable;
    protected Table groupsTable;
    protected Table usersGroupsTable;

    protected String userIdProperty;
    protected String userNameProperty;
    protected String userEmailProperty;
    protected String userPasswordProperty;
    protected String userTokenProperty;

    protected String groupIdProperty;
    protected String groupNameProperty;

    protected String groupLinkProperty;
    protected String userLinkProperty;

    protected static final Logger logger = LoggerFactory.getLogger(ModelBasedRealm.class);

    @Autowired
    Persistence persistence;

    @PostConstruct
    public void configure() {
        if(persistence.status.getValue() != Persistence.Status.STARTED) {
            throw new IllegalStateException("Persistence is not yet started");
        }
        persistence.getModel().getDatabases().forEach(d -> {
            d.getAllTables().forEach(t -> {
                t.getColumns().forEach(c -> {
                    if(c.getAnnotation(Username.class).isPresent()) {
                        setupUserTable(t);
                        userNameProperty = c.getActualPropertyName();
                    }
                    if(c.getAnnotation(Password.class).isPresent()) {
                        setupUserTable(t);
                        userPasswordProperty = c.getActualPropertyName();
                    }
                    if(c.getAnnotation(EmailAddress.class).isPresent()) {
                        setupUserTable(t);
                        userEmailProperty = c.getActualPropertyName();
                    }
                    if(c.getAnnotation(EmailToken.class).isPresent()) {
                        setupUserTable(t);
                        userTokenProperty = c.getActualPropertyName();
                    }
                    if(c.getAnnotation(GroupName.class).isPresent()) {
                        if(groupsTable != null) {
                            throw new IllegalStateException("Multiple tables with an @GroupName annotation: " + groupsTable.getQualifiedName() + " and " + t.getQualifiedName());
                        }
                        groupsTable = t;
                        groupNameProperty = c.getActualPropertyName();
                        List<Column> pkcols = t.getPrimaryKey().getColumns();
                        if(pkcols.size() > 1) {
                            throw new IllegalStateException("Group table has composite id, not supported: " + groupsTable.getQualifiedName());
                        }
                        groupIdProperty = pkcols.get(0).getActualPropertyName();
                    }
                    if(c.getAnnotation(GroupLink.class).isPresent()) {
                        if(usersGroupsTable != null && usersGroupsTable != t) {
                            throw new IllegalStateException("Multiple user-group tables: " + usersGroupsTable.getQualifiedName() + " and " + t.getQualifiedName());
                        }
                        usersGroupsTable = t;
                        groupLinkProperty = c.getActualPropertyName();
                    }
                    if(c.getAnnotation(UserLink.class).isPresent()) {
                        if(usersGroupsTable != null && usersGroupsTable != t) {
                            throw new IllegalStateException("Multiple user-group tables: " + usersGroupsTable.getQualifiedName() + " and " + t.getQualifiedName());
                        }
                        usersGroupsTable = t;
                        userLinkProperty = c.getActualPropertyName();
                    }
                });
            });
        });
        if(usersTable == null) {
            throw new IllegalStateException("No users table found");
        }
        if(userLinkProperty == null || groupLinkProperty == null) {
            usersGroupsTable = null;
        }
    }

    protected void setupUserTable(Table t) {
        if(usersTable != null && usersTable != t) {
            throw new IllegalStateException("Multiple users tables : " + usersTable.getQualifiedName() + " and " + t.getQualifiedName());
        }
        usersTable = t;
        List<Column> pkcols = t.getPrimaryKey().getColumns();
        if(pkcols.size() > 1) {
            throw new IllegalStateException("Users table has composite id, not supported: " + groupsTable.getQualifiedName());
        }
        userIdProperty = pkcols.get(0).getActualPropertyName();
    }

    @Override
    protected Collection<String> loadAuthorizationInfo(Serializable principal) {
        List<String> groups = new ArrayList<>();
        if(groupsTable == null) {
            return groups;
        }
        Session session = persistence.getSession(usersTable.getDatabaseName());
        String queryString = getUserGroupsQuery();
        Query<String> query = session.createQuery(queryString);
        Object userId = getUserProperty(principal, userIdProperty);
        if(userId != null) {
            //Load groups from the database
            query.setParameter("userId", userId);
            groups.addAll(query.list());
        }
        return groups;
    }

    @NotNull
    protected String getUserGroupsQuery() {
        String queryString =
                "select distinct g." + groupNameProperty + "\n" +
                "from " + groupsTable.getActualEntityName() + " g, " +
                usersGroupsTable.getActualEntityName() + " ug, " + usersTable.getActualEntityName() + " u\n" +
                "where g." + groupIdProperty + " = ug." + groupLinkProperty + "\n" +
                "and ug." + userLinkProperty + " = u." + userIdProperty + "\n" +
                "and u." + userIdProperty + " = :userId";
        return queryString;
    }

    protected Object getUserProperty(Object principal, String property) {
        if(principal instanceof Map) {
            return ((Map<?, ?>) principal).get(property);
        } else {
            try {
                return BeanUtils.getProperty(principal, property);
            } catch (Exception e) {
                logger.debug("Not a valid POJO principal: " + principal, e);
                return null;
            }
        }
    }

    protected void setUserProperty(Object principal, String property, Object value) {
        if(principal instanceof Map) {
            ((Map) principal).put(property, value);
        } else {
            try {
                BeanUtils.setProperty(principal, property, value);
            } catch (Exception e) {
                throw new IllegalStateException("Not a valid POJO principal: " + principal, e);
            }
        }
    }

    protected AuthenticationInfo loadAuthenticationInfo(UsernamePasswordToken usernamePasswordToken) {
        String userName = usernamePasswordToken.getUsername();
        Session session = persistence.getSession(usersTable.getDatabaseName());
        Tuple3<CriteriaQuery<Object>, CriteriaBuilder, Root> criteriaTuple =
                QueryUtils.createCriteria(session, usersTable.getActualEntityName());
        CriteriaQuery<Object> criteria = criteriaTuple.getV1();
        CriteriaBuilder cb = criteriaTuple.getV2();
        Root<?> from = criteriaTuple.getV3();
        criteria.where(cb.equal(from.get(userNameProperty), userName));

        List<?> result = session.createQuery(criteria).list();

        if (result.size() == 1) {
            Object user = cleanUserPrincipal(result.get(0));
            return new SimpleAuthenticationInfo(user, getUserProperty(user, userPasswordProperty), getName());
        } else {
            throw new IncorrectCredentialsException("Login failed");
        }
    }

    protected AuthenticationInfo loadAuthenticationInfo(PasswordResetToken token) {
        if(StringUtils.isEmpty(userTokenProperty)) {
            throw new AuthenticationException("User token property is not configured; password reset is not supported by this application.");
        }

        Session session = persistence.getSession(usersTable.getDatabaseName());
        Tuple3<CriteriaQuery<Object>, CriteriaBuilder, Root> criteriaTuple =
                QueryUtils.createCriteria(session, usersTable.getActualEntityName());
        CriteriaQuery<Object> criteria = criteriaTuple.getV1();
        CriteriaBuilder cb = criteriaTuple.getV2();
        Root<?> from = criteriaTuple.getV3();
        criteria.where(cb.equal(from.get(userTokenProperty), token.getPrincipal()));

        List result = session.createQuery(criteria).list();

        if (result.size() == 1) {
            String hashedPassword = encryptPassword(token.newPassword);
            Object user = result.get(0);
            setUserProperty(user, userTokenProperty, null); //Consume token
            setUserProperty(user, userPasswordProperty, hashedPassword);
            session.update(usersTable.getActualEntityName(), user);
            session.getTransaction().commit();
            SimpleAuthenticationInfo info =
                    new SimpleAuthenticationInfo(cleanUserPrincipal(user), hashedPassword, getName());
            return info;
        } else {
            throw new IncorrectCredentialsException("Invalid token");
        }
    }

    protected AuthenticationInfo loadAuthenticationInfo(SignUpToken token) {
        if(StringUtils.isEmpty(userTokenProperty)) {
            throw new AuthenticationException(
                    "User token property is not configured; self registration is not supported by this application.");
        }

        Session session = persistence.getSession(usersTable.getDatabaseName());
        Tuple3<CriteriaQuery<Object>, CriteriaBuilder, Root> criteriaTuple =
                QueryUtils.createCriteria(session, usersTable.getActualEntityName());
        CriteriaQuery<Object> criteria = criteriaTuple.getV1();
        CriteriaBuilder cb = criteriaTuple.getV2();
        Root<?> from = criteriaTuple.getV3();
        criteria.where(cb.equal(from.get(userTokenProperty), token.getPrincipal()));

        List result = session.createQuery(criteria).list();

        if (result.size() == 1) {
            Object user = result.get(0);
            setUserProperty(user, userTokenProperty, null); //Consume token
            session.update(usersTable.getActualEntityName(), user);
            session.getTransaction().commit();
            SimpleAuthenticationInfo info =
                    new SimpleAuthenticationInfo(cleanUserPrincipal(user), encryptPassword(token.getCredentials()), getName());
            return info;
        } else {
            throw new IncorrectCredentialsException("Invalid token");
        }
    }

    @Override
    protected Object cleanUserPrincipal(Object principal) {
        if(principal instanceof Map) {
            return super.cleanUserPrincipal(principal);
        }
        Object clean = persistence.getTableAccessor(usersTable).newInstance();
        PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(principal);
        for(PropertyDescriptor pd : propertyDescriptors) {
            if(DatabaseLogic.findColumnByName(usersTable, pd.getName()) != null) {
                setUserProperty(clean, pd.getName(), getUserProperty(principal, pd.getName()));
            }
        }
        return clean;
    }

    @Override
    public boolean supports(AuthenticationToken token) {
        if(token instanceof PasswordResetToken || token instanceof SignUpToken) {
            return !StringUtils.isEmpty(userTokenProperty);
        }
        return super.supports(token);
    }

    @Override
    public void changePassword(Serializable user, String oldPassword, String newPassword) {
        Session session = persistence.getSession(usersTable.getDatabaseName());
        Object savedUser = session.load(usersTable.getActualEntityName(), (Serializable) getUserProperty(user, userIdProperty));
        if(savedUser == null) {
            throw new UnknownAccountException("User has been deleted");
        } else if(!passwordService.passwordsMatch(oldPassword, (String) getUserProperty(savedUser, userPasswordProperty))) {
            throw new IncorrectCredentialsException("Wrong password");
        } else {
            setUserProperty(savedUser, userPasswordProperty, encryptPassword(newPassword));
            session.getTransaction().commit();
        }
    }

    public String encryptPassword(String password) {
        return passwordService.encryptPassword(password);
    }

    public Map<Serializable, String> getUsers() {
        Map users = new LinkedHashMap();
        Session session = persistence.getSession(usersTable.getDatabaseName());
        Query<Object[]> query = session.createQuery(
                "select " + userIdProperty + ",  " + userNameProperty + " from " + usersTable.getActualEntityName() +
                    " order by " + userNameProperty);
        for(Object[] user : query.list()) {
            users.put(user[0], user[1]);
        }
        return users;
    }

    public Serializable getUserById(String encodedId) {
        TableAccessor accessor = persistence.getTableAccessor(usersTable);
        PkHelper pkHelper = new PkHelper(accessor);
        Serializable id = pkHelper.getPrimaryKey(encodedId);
        Session session = persistence.getSession(usersTable.getActualEntityName());
        return (Serializable) QueryUtils.getObjectByPk(session, accessor, id);
    }

    public Serializable getUserByEmail(String email) {
        if(StringUtils.isEmpty(userEmailProperty)) {
            throw new UnsupportedOperationException("Email property not configured.");
        }
        Session session = persistence.getSession(usersTable.getDatabaseName());
        Tuple3<CriteriaQuery<Object>, CriteriaBuilder, Root> criteriaTuple =
                QueryUtils.createCriteria(session, usersTable.getActualEntityName());
        CriteriaQuery<Object> criteria = criteriaTuple.getV1();
        CriteriaBuilder cb = criteriaTuple.getV2();
        Root<?> from = criteriaTuple.getV3();
        criteria.where(cb.equal(from.get(userEmailProperty), email));
        return (Serializable) session.createQuery(criteria).uniqueResult();
    }

    @Override
    public String getUserPrettyName(Serializable user) {
        if(StringUtils.isEmpty(userNameProperty)) {
            return user.toString();
        }
        return (String) getUserProperty(user, userNameProperty);
    }

    @Override
    public Serializable getUserId(Serializable user) {
        if(StringUtils.isEmpty(userIdProperty)) {
            return user.toString();
        }
        return (Serializable) getUserProperty(user, userIdProperty);
    }

    @Override
    public String getUsername(Serializable user) {
        if(StringUtils.isEmpty(userNameProperty)) {
            return user.toString();
        }
        return (String) getUserProperty(user, userNameProperty);
    }

    @Override
    public String getEmail(Serializable user) {
        if(StringUtils.isEmpty(userEmailProperty)) {
            throw new UnsupportedOperationException("Email property not configured.");
        }
        return (String) getUserProperty(user, userEmailProperty);
    }

    @Override
    public String generateOneTimeToken(Serializable user) {
        if(StringUtils.isEmpty(userTokenProperty)) {
            throw new UnsupportedOperationException("Token property not configured.");
        }
        Session session = persistence.getSession(usersTable.getDatabaseName());
        user = (Serializable) session.get(usersTable.getActualEntityName(), getUserId(user));
        String token = RandomUtil.createRandomId(20);
        setUserProperty(user, userTokenProperty, token);
        session.update(usersTable.getActualEntityName(), user);
        session.getTransaction().commit();
        return token;
    }

    @Override
    public boolean supportsSelfRegistration() {
        return true;
    }

    public String[] saveSelfRegisteredUser(Object user) throws RegistrationException {
        if(StringUtils.isEmpty(userTokenProperty)) {
            throw new UnsupportedOperationException("Token property not configured.");
        }
        User theUser = (User) user;
        Session session = persistence.getSession(usersTable.getDatabaseName());
        TableAccessor accessor = persistence.getTableAccessor(usersTable);
        Object persistentUser = accessor.newInstance();
        setUserProperty(persistentUser, userNameProperty, theUser.username);
        setUserProperty(persistentUser, userPasswordProperty, encryptPassword(theUser.password));
        if(!StringUtils.isEmpty(userEmailProperty)) {
            setUserProperty(persistentUser, userEmailProperty, theUser.email);
        }

        String token = RandomUtil.createRandomId(20);
        setUserProperty(persistentUser, userTokenProperty, token);

        try {
            session.save(usersTable.getActualEntityName(), persistentUser);
            session.flush();
        } catch (ConstraintViolationException e) {
            throw new ExistingUserException(e);
        }
        session.getTransaction().commit();
        return new String[] { token, theUser.email };
    }

    public Set<String> getGroups() {
        Set<String> groups = super.getGroups();
        if(groupsTable != null) {
            Session session = persistence.getSession(groupsTable.getDatabaseName());
            Tuple3<CriteriaQuery<Object>, CriteriaBuilder, Root> criteriaTuple =
                    QueryUtils.createCriteria(session, groupsTable.getActualEntityName());
            CriteriaQuery<Object> criteria = criteriaTuple.getV1();
            CriteriaBuilder cb = criteriaTuple.getV2();
            Root<?> from = criteriaTuple.getV3();

            Path<Object> groupNameExpression = from.get(groupNameProperty);
            criteria.select(groupNameExpression).orderBy(cb.asc(groupNameExpression));
            groups.addAll(session.createQuery(criteria).list().stream().map(String::valueOf).collect(Collectors.toList()));
        }
        return groups;
    }

    public Table getUsersTable() {
        return usersTable;
    }

    public Table getGroupsTable() {
        return groupsTable;
    }

    public Table getUsersGroupsTable() {
        return usersGroupsTable;
    }
}
