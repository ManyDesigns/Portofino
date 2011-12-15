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

package com.manydesigns.portofino.liquibase.sqlgenerators;

import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.typeconversion.TypeConverter;
import liquibase.database.typeconversion.TypeConverterFactory;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.statement.NotNullConstraint;
import liquibase.statement.core.CreateDatabaseChangeLogTableStatement;
import liquibase.statement.core.CreateTableStatement;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class PortofinoPostgresCreateDatabaseChangeLogTableGenerator extends AbstractSqlGenerator<CreateDatabaseChangeLogTableStatement> {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(CreateDatabaseChangeLogTableStatement statement, Database database) {
        return database instanceof PostgresDatabase;
    }


    public ValidationErrors validate(CreateDatabaseChangeLogTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    public Sql[] generateSql(CreateDatabaseChangeLogTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        TypeConverter typeConverter = TypeConverterFactory.getInstance().findTypeConverter(database);
        CreateTableStatement createTableStatement = new CreateTableStatement(database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName())
                .addPrimaryKeyColumn("id", typeConverter.getDataType("VARCHAR(63)", false), null, null, null,new NotNullConstraint())
                .addPrimaryKeyColumn("author", typeConverter.getDataType("VARCHAR(63)", false), null, null, null,new NotNullConstraint())
                .addPrimaryKeyColumn("filename", typeConverter.getDataType("VARCHAR(200)", false), null, null, null,new NotNullConstraint())
                .addColumn("dateexecuted", typeConverter.getDateTimeType(), null, new NotNullConstraint())
                .addColumn("orderexecuted", typeConverter.getDataType("INT", false), new NotNullConstraint())
                .addColumn("exectype", typeConverter.getDataType("VARCHAR(10)", false), new NotNullConstraint())
                .addColumn("md5sum", typeConverter.getDataType("VARCHAR(35)", false))
                .addColumn("description", typeConverter.getDataType("VARCHAR(255)", false))
                .addColumn("comments", typeConverter.getDataType("VARCHAR(255)", false))
                .addColumn("tag", typeConverter.getDataType("VARCHAR(255)", false))
                .addColumn("liquibase", typeConverter.getDataType("VARCHAR(20)", false));

        return SqlGeneratorFactory.getInstance().generateSql(createTableStatement, database);
    }

}
