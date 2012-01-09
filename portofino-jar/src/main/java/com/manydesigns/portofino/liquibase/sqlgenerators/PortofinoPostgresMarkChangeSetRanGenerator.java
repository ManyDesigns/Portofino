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

import liquibase.change.Change;
import liquibase.change.core.TagDatabaseChange;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.exception.LiquibaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.InsertStatement;
import liquibase.statement.core.MarkChangeSetRanStatement;
import liquibase.statement.core.UpdateStatement;
import liquibase.util.LiquibaseUtil;
import liquibase.util.StringUtils;

import java.util.List;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class PortofinoPostgresMarkChangeSetRanGenerator extends AbstractSqlGenerator<MarkChangeSetRanStatement> {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(MarkChangeSetRanStatement statement, Database database) {
        return database instanceof PostgresDatabase;
    }

    public ValidationErrors validate(MarkChangeSetRanStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("changeSet", statement.getChangeSet());

        return validationErrors;
    }

    public Sql[] generateSql(MarkChangeSetRanStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        String dateValue = database.getCurrentDateTimeFunction();

        ChangeSet changeSet = statement.getChangeSet();

        SqlStatement runStatement;
        try {
            if (statement.getExecType().equals(ChangeSet.ExecType.FAILED) || statement.getExecType().equals(ChangeSet.ExecType.SKIPPED)) {
                return new Sql[0]; //don't mark
            } else  if (statement.getExecType().ranBefore) {
                runStatement = new UpdateStatement(database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName())
                        .addNewColumnValue("dateexecuted", new DatabaseFunction(dateValue))
                        .addNewColumnValue("md5sum", changeSet.generateCheckSum().toString())
                        .addNewColumnValue("exectype", statement.getExecType().value)
                        .setWhereClause("id=? AND author=? AND filename=?")
                        .addWhereParameters(changeSet.getId(), changeSet.getAuthor(), changeSet.getFilePath());
            } else {
                runStatement = new InsertStatement(database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName())
                        .addColumnValue("id", changeSet.getId())
                        .addColumnValue("author", changeSet.getAuthor())
                        .addColumnValue("filename", changeSet.getFilePath())
                        .addColumnValue("dateexecuted", new DatabaseFunction(dateValue))
                        .addColumnValue("orderexecuted", database.getNextChangeSetSequenceValue())
                        .addColumnValue("md5sum", changeSet.generateCheckSum().toString())
                        .addColumnValue("description", limitSize(changeSet.getDescription()))
                        .addColumnValue("comments", limitSize(StringUtils.trimToEmpty(changeSet.getComments())))
                        .addColumnValue("exectype", statement.getExecType().value)
                        .addColumnValue("liquibase", LiquibaseUtil.getBuildVersion().replaceAll("SNAPSHOT", "SNP"));

                String tag = null;
                List<Change> changes = changeSet.getChanges();
                if (changes != null && changes.size() == 1) {
                    Change change = changes.get(0);
                    if (change instanceof TagDatabaseChange) {
                        TagDatabaseChange tagChange = (TagDatabaseChange) change;
                        tag = tagChange.getTag();
                    }
                }
                if (tag != null) {
                    ((InsertStatement) runStatement).addColumnValue("TAG", tag);
                }
            }
        } catch (LiquibaseException e) {
            throw new UnexpectedLiquibaseException(e);
        }

        return SqlGeneratorFactory.getInstance().generateSql(runStatement, database);
    }

    private String limitSize(String string) {
        int maxLength = 255;
        if (string.length() > maxLength) {
            return string.substring(0, maxLength - 3) + "...";
        }
        return string;
    }
}
