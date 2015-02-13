package com.manydesigns.portofino.persistence.hibernate;

import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.IdentifierGeneratorHelper;
import org.hibernate.id.IdentityGenerator;
import org.hibernate.id.PostInsertIdentityPersister;
import org.hibernate.id.insert.InsertGeneratedIdentifierDelegate;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PortofinoIdentityGenerator extends IdentityGenerator {

    public InsertGeneratedIdentifierDelegate getInsertGeneratedIdentifierDelegate(
            PostInsertIdentityPersister persister,
            Dialect dialect,
            boolean isGetGeneratedKeysEnabled) throws HibernateException {
        if (isGetGeneratedKeysEnabled) {
            return new PortofinoGetGeneratedKeysDelegate(persister, dialect);
        } else if (dialect.supportsInsertSelectIdentity()) {
            return new InsertSelectDelegate(persister, dialect);
        } else {
            return new BasicDelegate(persister, dialect);
        }
    }

    /**
     * Delegate for dealing with IDENTITY columns using JDBC3 getGeneratedKeys.
     */
    public static class PortofinoGetGeneratedKeysDelegate extends GetGeneratedKeysDelegate implements InsertGeneratedIdentifierDelegate {
        private final PostInsertIdentityPersister persister;
        private final Dialect dialect;

        public PortofinoGetGeneratedKeysDelegate(PostInsertIdentityPersister persister, Dialect dialect) {
            super(persister, dialect);
            this.persister = persister;
            this.dialect = dialect;
        }

        /**
         * Override in order to unquote the primary key column name, else it breaks (at least on Postgres)
         */
        @Override
        public Serializable executeAndExtract(PreparedStatement insert, SessionImplementor session) throws SQLException {
            session.getTransactionCoordinator().getJdbcCoordinator().getResultSetReturn().executeUpdate(insert);
            ResultSet rs = null;
            try {
                rs = insert.getGeneratedKeys();
                return IdentifierGeneratorHelper.getGeneratedIdentity(
                        rs,
                        unquotedIdentifier(persister.getRootTableKeyColumnNames()[0]),
                        persister.getIdentifierType()
                );
            } finally {
                if (rs != null) {
                    session.getTransactionCoordinator().getJdbcCoordinator().release(rs, insert);
                }
            }
        }

        protected String unquotedIdentifier(String identifier) { //This is a hack.
            if(identifier.startsWith(dialect.openQuote() + "")) {
                return identifier.substring(1, identifier.length() - 1);
            } else {
                return identifier;
            }
        }
    }

}
