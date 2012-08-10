/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
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

        logger.debug("Registering PortofinoPostgresMarkChangeSetRanGenerator");
        sqlGeneratorFactory.register(
                new PortofinoPostgresMarkChangeSetRanGenerator());

        logger.debug("Registering PortofinoPostgresUnlockDatabaseChangeLogGenerator");
        sqlGeneratorFactory.register(
                new PortofinoPostgresUnlockDatabaseChangeLogGenerator());
    }
}
