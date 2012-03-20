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

package com.manydesigns.portofino.liquibase.sqlgenerators;

import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.typeconversion.TypeConverterFactory;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.statement.core.LockDatabaseChangeLogStatement;
import liquibase.statement.core.UpdateStatement;
import liquibase.util.NetUtil;

import java.net.InetAddress;
import java.sql.Timestamp;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class PortofinoPostgresLockDatabaseChangeLogGenerator extends AbstractSqlGenerator<LockDatabaseChangeLogStatement> {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(LockDatabaseChangeLogStatement statement, Database database) {
        return database instanceof PostgresDatabase;
    }

    public ValidationErrors validate(LockDatabaseChangeLogStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    public Sql[] generateSql(LockDatabaseChangeLogStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
    	String liquibaseSchema = null;
    		liquibaseSchema = database.getLiquibaseSchemaName();

        InetAddress localHost;
    	try {
            localHost = NetUtil.getLocalHost();
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }

        UpdateStatement updateStatement = new UpdateStatement(liquibaseSchema, database.getDatabaseChangeLogLockTableName());
        updateStatement.addNewColumnValue("locked", true);
        updateStatement.addNewColumnValue("lockgranted", new Timestamp(new java.util.Date().getTime()));
        updateStatement.addNewColumnValue("lockedby", localHost.getHostName() + " (" + localHost.getHostAddress() + ")");
        updateStatement.setWhereClause("id = 1 AND locked = "+ TypeConverterFactory.getInstance().findTypeConverter(database).getBooleanType().getFalseBooleanValue());

        return SqlGeneratorFactory.getInstance().generateSql(updateStatement, database);

    }
}