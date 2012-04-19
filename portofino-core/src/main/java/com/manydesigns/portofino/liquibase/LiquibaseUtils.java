/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.manydesigns.portofino.liquibase;

import com.manydesigns.portofino.liquibase.databases.*;
import com.manydesigns.portofino.liquibase.sqlgenerators.*;
import liquibase.database.DatabaseFactory;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class LiquibaseUtils {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

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

        logger.debug("Registering PortofinoPostgresMarkChangeSetRanGenerator");
        sqlGeneratorFactory.register(
                new PortofinoPostgresMarkChangeSetRanGenerator());

        logger.debug("Registering PortofinoPostgresUnlockDatabaseChangeLogGenerator");
        sqlGeneratorFactory.register(
                new PortofinoPostgresUnlockDatabaseChangeLogGenerator());
    }
}
