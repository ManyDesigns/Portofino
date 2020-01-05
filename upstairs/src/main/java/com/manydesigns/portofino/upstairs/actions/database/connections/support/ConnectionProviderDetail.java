package com.manydesigns.portofino.upstairs.actions.database.connections.support;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.manydesigns.elements.annotations.*;
import com.manydesigns.portofino.model.database.ConnectionProvider;
import com.manydesigns.portofino.model.database.Database;
import com.manydesigns.portofino.model.database.JdbcConnectionProvider;
import com.manydesigns.portofino.model.database.JndiConnectionProvider;
import com.manydesigns.portofino.model.database.platforms.DatabasePlatform;
import org.apache.commons.lang.StringUtils;

import java.util.Date;

@JsonClassDescription
public class ConnectionProviderDetail {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";

    @JsonIgnore
    protected Database database;
    protected String databasePlatform;
    @JsonIgnore
    protected JdbcConnectionProvider jdbcConnectionProvider;
    @JsonIgnore
    protected JndiConnectionProvider jndiConnectionProvider;

    public ConnectionProviderDetail(ConnectionProvider connectionProvider) {
        this.database = connectionProvider.getDatabase();
        DatabasePlatform databasePlatform = connectionProvider.getDatabasePlatform();
        this.databasePlatform = databasePlatform != null ? databasePlatform.getClass().getName() : null;
        if(connectionProvider instanceof JdbcConnectionProvider) {
            jdbcConnectionProvider = (JdbcConnectionProvider) connectionProvider;
        } else if(connectionProvider instanceof JndiConnectionProvider) {
            jndiConnectionProvider = (JndiConnectionProvider) connectionProvider;
        } else {
            throw new IllegalArgumentException("Invalid connection provider type: " + connectionProvider);
        }
    }

    public void setDatabaseName(String databaseName) {
        database.setDatabaseName(databaseName);
    }

    @Updatable(false)
    @Required(true)
    public String getDatabaseName() {
        return database.getDatabaseName();
    }

    public void setTrueString(String trueString) {
        database.setTrueString(StringUtils.defaultIfEmpty(trueString, null));
    }

    public String getTrueString() {
        return database.getTrueString();
    }

    public void setFalseString(String falseString) {
        database.setFalseString(StringUtils.defaultIfEmpty(falseString, null));
    }

    public String getFalseString() {
        return database.getFalseString();
    }

    @FieldSize(50)
    @Required
    public String getDriver() {
        if(jdbcConnectionProvider == null) {
            return null;
        }
        return jdbcConnectionProvider.getDriver();
    }

    public void setDriver(String driver) {
        jdbcConnectionProvider.setDriver(driver);
    }

    @FieldSize(100)
    @Required
    @Label("connection URL")
    public String getUrl() {
        if(jdbcConnectionProvider == null) {
            return null;
        }
        return jdbcConnectionProvider.getActualUrl();
    }

    public void setUrl(String url) {
        jdbcConnectionProvider.setActualUrl(url);
    }

    public String getUsername() {
        if(jdbcConnectionProvider == null) {
            return null;
        }
        return jdbcConnectionProvider.getActualUsername();
    }

    public void setUsername(String username) {
        jdbcConnectionProvider.setActualUsername(username);
    }

    @Password
    public String getPassword() {
        if(jdbcConnectionProvider == null) {
            return null;
        }
        return jdbcConnectionProvider.getActualPassword();
    }

    public void setPassword(String password) {
        jdbcConnectionProvider.setActualPassword(password);
    }

    @Required
    public String getJndiResource() {
        if(jndiConnectionProvider == null) {
            return null;
        }
        return jndiConnectionProvider.getJndiResource();
    }

    public void setJndiResource(String jndiResource) {
        jndiConnectionProvider.setJndiResource(jndiResource);
    }

    @Updatable(false)
    public String getDescription() {
        if(jdbcConnectionProvider != null) {
            return jdbcConnectionProvider.getDescription();
        } else if(jndiConnectionProvider != null) {
            return jndiConnectionProvider.getDescription();
        } else {
            return null;
        }
    }

    @Label("Hibernate dialect (leave empty to use default)")
    public String getHibernateDialect() {
        if(jdbcConnectionProvider != null) {
            return jdbcConnectionProvider.getHibernateDialect();
        } else if(jndiConnectionProvider != null) {
            return jndiConnectionProvider.getHibernateDialect();
        } else {
            return null;
        }
    }

    public void setHibernateDialect(String dialect) {
        if(jdbcConnectionProvider != null) {
            jdbcConnectionProvider.setHibernateDialect(dialect);
        } else if(jndiConnectionProvider != null) {
            jndiConnectionProvider.setHibernateDialect(dialect);
        } else {
            throw new Error("Misconfigured");
        }
    }

    @Updatable(false)
    public String getErrorMessage() {
        if(jdbcConnectionProvider != null) {
            return jdbcConnectionProvider.getErrorMessage();
        } else if(jndiConnectionProvider != null) {
            return jndiConnectionProvider.getErrorMessage();
        } else {
            return null;
        }
    }

    @Updatable(false)
    public String getStatus() {
        if(jdbcConnectionProvider != null) {
            return jdbcConnectionProvider.getStatus();
        } else if(jndiConnectionProvider != null) {
            return jndiConnectionProvider.getStatus();
        } else {
            return null;
        }
    }

    @Updatable(false)
    public Date getLastTested() {
        if(jdbcConnectionProvider != null) {
            return jdbcConnectionProvider.getLastTested();
        } else if(jndiConnectionProvider != null) {
            return jndiConnectionProvider.getLastTested();
        } else {
            return null;
        }
    }

}
