/*
 * Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.liquibase;

import com.manydesigns.portofino.liquibase.databases.*;
import com.manydesigns.portofino.liquibase.databasesnapshotgenerators.PortofinoOracleDatabaseSnapshotGenerator;
import com.manydesigns.portofino.liquibase.databasesnapshotgenerators.PortofinoPostgresDatabaseSnapshotGenerator;
import com.manydesigns.portofino.liquibase.sqlgenerators.*;
import liquibase.database.DatabaseFactory;
import liquibase.snapshot.DatabaseSnapshotGenerator;
import liquibase.snapshot.DatabaseSnapshotGeneratorFactory;
import liquibase.snapshot.jvm.*;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class LiquibaseUtils {
    public static final String copyright =
            "Copyright (c) 2005-2013, ManyDesigns srl";

    //--------------------------------------------------------------------------
    // Logging
    //--------------------------------------------------------------------------

    public static final Logger logger =
            LoggerFactory.getLogger(LiquibaseUtils.class);

    //--------------------------------------------------------------------------
    // Static methods
    //--------------------------------------------------------------------------

    public static String escapeDatabaseObject(
            String objectName, String quoteString) {
        return escapeDatabaseObject(objectName, quoteString, quoteString);
    }

    public static String escapeDatabaseObject(
            String objectName,
            String openingQuoteString,
            String closingQuoteString
    ) {
        if (objectName == null) {
            return null;
        }
        if (openingQuoteString == null) {
            return objectName;
        }
        return String.format("%s%s%s",
                openingQuoteString, objectName, closingQuoteString);
    }

    public static void setup() {
        setupDatabaseFactory();
        setupDatabaseSnapshotGeneratorFactory();
        setupSqlGeneratorFactory();
    }

    public static void setupDatabaseFactory() {
        DatabaseFactory databaseFactory = DatabaseFactory.getInstance();

        logger.debug("Clearing Liquibase database factory registry");
        databaseFactory.clearRegistry();

        logger.debug("Registering DB2");
        databaseFactory.register(new PortofinoDB2Database());

        logger.debug("Registering Derby");
        databaseFactory.register(new PortofinoDerbyDatabase());

        logger.debug("Registering H2");
        databaseFactory.register(new PortofinoH2Database());

        logger.debug("Registering MSSQL");
        databaseFactory.register(new PortofinoMSSQLDatabase());

        logger.debug("Registering MySQL");
        databaseFactory.register(new PortofinoMySQLDatabase());

        logger.debug("Registering Oracle");
        databaseFactory.register(new PortofinoOracleDatabase());

        logger.debug("Registering Postgres");
        databaseFactory.register(new PortofinoPostgresDatabase());

        logger.debug("Registering Google Cloud SQL");
        databaseFactory.register(new GoogleCloudSQLDatabase());
    }

    public static void setupDatabaseSnapshotGeneratorFactory() {
        DatabaseSnapshotGeneratorFactory dsgf =
                DatabaseSnapshotGeneratorFactory.getInstance();

        List<DatabaseSnapshotGenerator> registry = dsgf.getRegistry();

        logger.debug("Clearing Liquibase database snapshot generator factory registry");
        registry.clear();

        logger.debug("Registering DB2");
        dsgf.register(new DB2DatabaseSnapshotGenerator());

        logger.debug("Registering Derby");
        dsgf.register(new DerbyDatabaseSnapshotGenerator());

        logger.debug("Registering H2");
        dsgf.register(new H2DatabaseSnapshotGenerator());

        logger.debug("Registering MSSQL");
        dsgf.register(new MSSQLDatabaseSnapshotGenerator());

        logger.debug("Registering MySQL");
        dsgf.register(new MySQLDatabaseSnapshotGenerator());

        logger.debug("Registering Oracle");
        dsgf.register(new PortofinoOracleDatabaseSnapshotGenerator());

        logger.debug("Registering Postgres");
        dsgf.register(new PortofinoPostgresDatabaseSnapshotGenerator());
    }

    public static void setupSqlGeneratorFactory() {
        logger.debug("Setting up Liquibase sql generator factory");

        SqlGeneratorFactory sqlGeneratorFactory =
                SqlGeneratorFactory.getInstance();

        logger.debug("Registering PortofinoPostgresCreateDatabaseChangeLogTableGenerator");
        sqlGeneratorFactory.register(
                new PortofinoPostgresCreateDatabaseChangeLogTableGenerator());

        logger.debug("Registering PortofinoPostgresCreateDatabaseChangeLogLockTableGenerator");
        sqlGeneratorFactory.register(
                new PortofinoPostgresCreateDatabaseChangeLogLockTableGenerator());

        logger.debug("Registering PortofinoSelectFromDatabaseChangeLogLockGenerator");
        sqlGeneratorFactory.register(
                new PortofinoSelectFromDatabaseChangeLogLockGenerator());

        logger.debug("Registering PortofinoPostgresLockDatabaseChangeLogGenerator");
        sqlGeneratorFactory.register(
                new PortofinoPostgresLockDatabaseChangeLogGenerator());

        logger.debug("Registering GoogleCloudSQLLockDatabaseChangeLogGenerator");
        sqlGeneratorFactory.register(
                new GoogleCloudSQLLockDatabaseChangeLogGenerator());

        logger.debug("Registering PortofinoPostgresMarkChangeSetRanGenerator");
        sqlGeneratorFactory.register(
                new PortofinoPostgresMarkChangeSetRanGenerator());

        logger.debug("Registering PortofinoPostgresUnlockDatabaseChangeLogGenerator");
        sqlGeneratorFactory.register(
                new PortofinoPostgresUnlockDatabaseChangeLogGenerator());
    }
}
