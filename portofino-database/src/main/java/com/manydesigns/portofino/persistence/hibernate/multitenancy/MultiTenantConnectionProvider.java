package com.manydesigns.portofino.persistence.hibernate.multitenancy;

import org.hibernate.engine.jdbc.connections.spi.AbstractMultiTenantConnectionProvider;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.service.spi.Configurable;
import org.hibernate.service.spi.ServiceRegistryAwareService;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MultiTenantConnectionProvider extends AbstractMultiTenantConnectionProvider
        implements ServiceRegistryAwareService, Configurable {

    public static final String CONNECTION_PROVIDER_CLASS = "portofino.persistence.hibernate.multitenancy.connectionProviderClass";
    private Class<ConnectionProvider> connectionProviderClass;
    private ServiceRegistryImplementor serviceRegistry;
    private Map configurationValues;
    private ConcurrentMap<Object, ConnectionProvider> connectionProviders = new ConcurrentHashMap<>();
    private static final Object ANY = new Object();

    public MultiTenantConnectionProvider() {}

    @Override
    protected ConnectionProvider getAnyConnectionProvider() {
        return getConnectionProvider(ANY, configurationValues);
    }

    @Override
    protected ConnectionProvider selectConnectionProvider(String tenantIdentifier) {
        Map settings = new HashMap(configurationValues);
        return getConnectionProvider(tenantIdentifier, settings);
    }

    @NotNull
    protected ConnectionProvider getConnectionProvider(Object key, Map configuration) {
        return connectionProviders.computeIfAbsent(key, o -> {
            ConnectionProvider connectionProvider;
            try {
                connectionProvider = connectionProviderClass.getConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Could not instantiate connection provider " + connectionProviderClass, e);
            }
            if(connectionProvider instanceof ServiceRegistryAwareService) {
                ((ServiceRegistryAwareService) connectionProvider).injectServices(serviceRegistry);
            }
            if(connectionProvider instanceof Configurable) {
                ((Configurable) connectionProvider).configure(configuration);
            }
            return connectionProvider;
        });
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
}
