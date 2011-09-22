/*
 * Copyright (C) 2005-2011 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.sync;

import com.manydesigns.portofino.connections.ConnectionProvider;
import com.manydesigns.portofino.database.DbUtil;
import com.manydesigns.portofino.database.platforms.DatabasePlatform;
import com.manydesigns.portofino.logic.DataModelLogic;
import com.manydesigns.portofino.model.Model;
import com.manydesigns.portofino.model.datamodel.Database;
import com.manydesigns.portofino.model.datamodel.Schema;
import com.manydesigns.portofino.model.datamodel.Table;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.DatabaseSnapshotGeneratorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class DatabaseSyncer {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    public static final String TABLE_SCHEM = "TABLE_SCHEM";

    public static final Logger logger =
            LoggerFactory.getLogger(DatabaseSyncer.class);


    public Database syncDatabase(ConnectionProvider connectionProvider,
                                 Model sourceModel) throws Exception {
        Database targetDatabase = new Database();

        Connection conn = null;
        try {
            logger.debug("Acquiring connection");
            conn = connectionProvider.acquireConnection();

            logger.debug("Reading database metadata");
            DatabaseMetaData metadata = conn.getMetaData();

            logger.debug("Creating Liquibase connection");
            DatabaseConnection liquibaseConnection =
                    new JdbcConnection(conn);

            logger.debug("Retrieving source database");
            String databaseName = connectionProvider.getDatabaseName();
            Database sourceDatabase =
                    DataModelLogic.findDatabaseByName(sourceModel, databaseName);
            if (sourceDatabase == null) {
                logger.debug("Source database not found. Creating an empty one.");
                sourceDatabase = new Database();
            }

            logger.debug("Reading schema names from metadata");
            List<String> schemaNames =
                    readSchemaNames(conn, connectionProvider, metadata);
            DatabaseSnapshotGeneratorFactory dsgf =
                    DatabaseSnapshotGeneratorFactory.getInstance();
            for (String schemaName : schemaNames) {
                logger.debug("Processing schema: {}", schemaName);
                Schema sourceSchema =
                        DataModelLogic.findSchemaByName(
                                sourceDatabase, schemaName);
                if (sourceSchema == null) {
                    logger.debug("Source schema not found. Creating an empty one.");
                    sourceSchema = new Schema();
                    sourceSchema.setSchemaName(schemaName);
                }
                logger.debug("Creating Liquibase database");
                DatabasePlatform databasePlatform =
                        connectionProvider.getDatabasePlatform();

                liquibase.database.Database liquibaseDatabase =
                        databasePlatform.createLiquibaseDatabase();
                liquibaseDatabase.setConnection(liquibaseConnection);
                logger.debug("Creating Liquibase database snapshot");
                DatabaseSnapshot snapshot = dsgf.createSnapshot(liquibaseDatabase, "public", null);

                logger.debug("Synchronizing schema");
                Schema targetSchema = syncSchema(snapshot, sourceSchema);
                targetSchema.setDatabase(targetDatabase);
                targetDatabase.getSchemas().add(targetSchema);
        }
        } finally {
            connectionProvider.releaseConnection(conn);
        }
        return targetDatabase;
    }

    public Schema syncSchema(DatabaseSnapshot databaseSnapshot, Schema sourceSchema) {
        logger.debug("Synchronizing schema: {}", sourceSchema.getSchemaName());
        Schema targetSchema = new Schema();
        targetSchema.setSchemaName(sourceSchema.getSchemaName());
        for (liquibase.database.structure.Table liquibaseTable
                : databaseSnapshot.getTables()) {
            logger.debug("Processing table: {}", liquibaseTable.getName());
            Table targetTable = new Table(targetSchema);
            targetSchema.getTables().add(targetTable);

            targetTable.setTableName(liquibaseTable.getName());

            // merge table attributes and annotations
        }
        return targetSchema;
    }

    protected List<String> readSchemaNames(Connection conn,
                                           ConnectionProvider connectionProvider,
                                           DatabaseMetaData metadata)
            throws SQLException {
        logger.debug("Searching for schemas in: {}",
                connectionProvider.getDatabaseName());

        List<String> result = new ArrayList<String>();

        ResultSet rs = null;
        Pattern includePattern = connectionProvider.getIncludeSchemasPattern();
        Pattern excludePattern = connectionProvider.getExcludeSchemasPattern();
        try {
            rs = metadata.getSchemas();
            while(rs.next()) {
                String schemaName = rs.getString(TABLE_SCHEM);
                if (includePattern != null) {
                    Matcher includeMatcher = includePattern.matcher(schemaName);
                    if (!includeMatcher.matches()) {
                        logger.info("Schema '{}' does not match include pattern '{}'. Skipping this schema.",
                                schemaName,
                                connectionProvider.getIncludeSchemas());
                        continue;
                    }
                }
                if (excludePattern != null) {
                    Matcher excludeMatcher = excludePattern.matcher(schemaName);
                    if (excludeMatcher.matches()) {
                        logger.info("Schema '{}' matches exclude pattern '{}'. Skipping this schema.",
                                schemaName,
                                connectionProvider.getExcludeSchemas());
                        continue;
                    }
                }
                logger.info("Found schema: {}", schemaName);
                result.add(schemaName);
            }
        } finally {
            DbUtil.closeResultSetAndStatement(rs);
        }
        return result;
    }




}
