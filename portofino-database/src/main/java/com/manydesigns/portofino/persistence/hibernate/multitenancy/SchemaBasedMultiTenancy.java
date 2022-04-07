package com.manydesigns.portofino.persistence.hibernate.multitenancy;

import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public abstract class SchemaBasedMultiTenancy extends MultiTenancyImplementation {

    @Override
    public MultiTenancyStrategy getStrategy() {
        return MultiTenancyStrategy.SEPARATE_SCHEMA;
    }

    @NotNull
    @Override
    protected ConnectionProvider createConnectionProvider(String tenant, Map configuration) {
        ConnectionProvider delegate = super.createConnectionProvider(tenant, configuration);
        return new ConnectionProviderWithSchemaPerTenant(delegate, tenant);
    }

    protected abstract void setSchema(Connection connection, String tenant) throws SQLException;

    public class ConnectionProviderWithSchemaPerTenant implements ConnectionProvider {
        private final ConnectionProvider delegate;
        private final String tenant;

        public ConnectionProviderWithSchemaPerTenant(ConnectionProvider delegate, String tenant) {
            this.delegate = delegate;
            this.tenant = tenant;
        }

        @Override
        public Connection getConnection() throws SQLException {
            Connection connection = delegate.getConnection();
            setSchema(connection, tenant);
            return connection;
        }

        @Override
        public void closeConnection(Connection conn) throws SQLException {
            delegate.closeConnection(conn);
        }

        @Override
        public boolean supportsAggressiveRelease() {
            return delegate.supportsAggressiveRelease();
        }

        @Override
        public boolean isUnwrappableAs(Class unwrapType) {
            return delegate.isUnwrappableAs(unwrapType);
        }

        @Override
        public <T> T unwrap(Class<T> unwrapType) {
            return delegate.unwrap(unwrapType);
        }
    }
}
