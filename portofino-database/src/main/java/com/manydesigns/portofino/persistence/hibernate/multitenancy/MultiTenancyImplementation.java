package com.manydesigns.portofino.persistence.hibernate.multitenancy;

import com.manydesigns.portofino.shiro.PortofinoRealm;
import com.manydesigns.portofino.shiro.ShiroUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.hibernate.MultiTenancyStrategy;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.jdbc.connections.spi.AbstractMultiTenantConnectionProvider;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.service.spi.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * This class has two purposes:
 * <ol>
 *     <li>act as an AbstractMultiTenantConnectionProvider for Hibernate, selecting a different connection provider
 *     for each tenant;</li>
 *     <li>act as a strategy for Portofino to compute the current tenant.</li>
 * </ol>
 * Given that the two purposes have overlapping needs, and we can't use inner classes (because this will be instantiated
 * reflectively both by Portofino and by Hibernate), we've settled on a single class even if it's not properly designed
 * OO.
 * Implementors should keep in mind that two instances will be created to perform the two different tasks,
 * and they will not share state.
 */
public class MultiTenancyImplementation extends AbstractMultiTenantConnectionProvider
        implements ServiceRegistryAwareService, Configurable, Stoppable {

    public static final String CONNECTION_PROVIDER_CLASS = "portofino.persistence.hibernate.multitenancy.connectionProviderClass";
    private static final Logger logger = LoggerFactory.getLogger(MultiTenancyImplementation.class);

    private Class<ConnectionProvider> connectionProviderClass;

    private ServiceRegistryImplementor serviceRegistry;
    private Map configurationValues;
    private final ConcurrentMap<Object, ConnectionProvider> connectionProviders = new ConcurrentHashMap<>();

    public MultiTenancyImplementation() {}

    public MultiTenancyStrategy getStrategy() {
        return MultiTenancyStrategy.DATABASE;
    }

    @Override
    protected ConnectionProvider getAnyConnectionProvider() {
        return getConnectionProvider(getDefaultTenant(), configurationValues);
    }

    public String getDefaultTenant() {
        return "portofino";
    }

    public String getTenant() {
        SecurityManager securityManager = ThreadContext.getSecurityManager();
        if(securityManager == null) {
            return getDefaultTenant(); //Out of Shiro context
        }
        Subject subject = SecurityUtils.getSubject();
        if(!subject.isAuthenticated()) {
            return getDefaultTenant();
        }
        PortofinoRealm portofinoRealm = ShiroUtils.getPortofinoRealm();
        return portofinoRealm.getUsername((Serializable) subject.getPrincipal());
    }

    @Override
    protected ConnectionProvider selectConnectionProvider(String tenantIdentifier) {
        Map settings = new HashMap(configurationValues);
        return getConnectionProvider(tenantIdentifier, settings);
    }

    @NotNull
    protected ConnectionProvider getConnectionProvider(String tenant, Map configuration) {
        return connectionProviders.computeIfAbsent(tenant, o -> createConnectionProvider(tenant, configuration));
    }

    @NotNull
    protected ConnectionProvider createConnectionProvider(String tenant, Map configuration) {
        ConnectionProvider connectionProvider;
        try {
            connectionProvider = connectionProviderClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Could not instantiate connection provider " + connectionProviderClass, e);
        }
        String url = getConnectionURL(tenant);
        if(url != null) {
            configuration.put(AvailableSettings.URL, url);
        }
        String username = getUsername(tenant);
        if(username != null) {
            configuration.put(AvailableSettings.USER, username);
        }
        String password = getPassword(tenant);
        if(password != null) {
            configuration.put(AvailableSettings.PASS, username);
        }
        if(connectionProvider instanceof ServiceRegistryAwareService) {
            ((ServiceRegistryAwareService) connectionProvider).injectServices(serviceRegistry);
        }
        if(connectionProvider instanceof Configurable) {
            ((Configurable) connectionProvider).configure(configuration);
        }
        if(connectionProvider instanceof Startable) {
            ((Startable) connectionProvider).start();
        }
        return connectionProvider;
    }

    public String getConnectionURL(String tenant) {
        return null;
    }

    public String getUsername(String tenant) {
        return null;
    }

    public String getPassword(String tenant) {
        return null;
    }

    @Override
    public void injectServices(ServiceRegistryImplementor serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public void configure(Map configurationValues) {
        this.configurationValues = configurationValues;
        connectionProviderClass = (Class<ConnectionProvider>) configurationValues.get(CONNECTION_PROVIDER_CLASS);
        if(!ConnectionProvider.class.isAssignableFrom(connectionProviderClass)) {
            throw new RuntimeException("Class " + connectionProviderClass.getName() + " does not implement ConnectionProvider");
        }
    }

    @Override
    public void stop() {
        connectionProviders.values().forEach(cp -> {
            if(cp instanceof Stoppable) {
                try {
                    ((Stoppable) cp).stop();
                } catch (Exception e) {
                    logger.warn("Could not stop connection provider " + cp, e);
                }
            }
        });
        connectionProviders.clear();
    }
}
