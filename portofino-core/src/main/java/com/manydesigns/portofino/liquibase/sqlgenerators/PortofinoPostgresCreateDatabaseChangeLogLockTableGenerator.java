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
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.statement.NotNullConstraint;
import liquibase.statement.core.CreateDatabaseChangeLogLockTableStatement;
import liquibase.statement.core.CreateTableStatement;
import liquibase.statement.core.InsertStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class PortofinoPostgresCreateDatabaseChangeLogLockTableGenerator
        extends AbstractSqlGenerator<CreateDatabaseChangeLogLockTableStatement> {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(CreateDatabaseChangeLogLockTableStatement statement, Database database) {
        return database instanceof PostgresDatabase;
    }

    public ValidationErrors validate(CreateDatabaseChangeLogLockTableStatement
        createDatabaseChangeLogLockTableStatement, Database
        database, SqlGeneratorChain
        sqlGeneratorChain) {
        return new ValidationErrors();
    }

    public Sql[] generateSql(CreateDatabaseChangeLogLockTableStatement statement,
                             Database database,
                             SqlGeneratorChain sqlGeneratorChain) {
        CreateTableStatement createTableStatement = new CreateTableStatement(database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName())
                .addPrimaryKeyColumn("id", TypeConverterFactory.getInstance().findTypeConverter(database).getDataType("INT", false), null, null, null, new NotNullConstraint())
                .addColumn("locked", TypeConverterFactory.getInstance().findTypeConverter(database).getDataType("BOOLEAN", false), null, new NotNullConstraint())
                .addColumn("lockgranted", TypeConverterFactory.getInstance().findTypeConverter(database).getDataType("DATETIME", false))
                .addColumn("lockedby", TypeConverterFactory.getInstance().findTypeConverter(database).getDataType("VARCHAR(255)", false));

        InsertStatement insertStatement = new InsertStatement(database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName())
                .addColumnValue("id", 1)
                .addColumnValue("locked", Boolean.FALSE);

        List<Sql> sql = new ArrayList<Sql>();

        sql.addAll(Arrays.asList(SqlGeneratorFactory.getInstance().generateSql(createTableStatement, database)));
        sql.addAll(Arrays.asList(SqlGeneratorFactory.getInstance().generateSql(insertStatement, database)));

        return sql.toArray(new Sql[sql.size()]);
    }
}
