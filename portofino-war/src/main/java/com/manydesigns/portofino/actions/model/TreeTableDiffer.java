/*
 * Copyright (C) 2005-2010 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.actions.model;

import com.manydesigns.elements.xml.XhtmlBuffer;
import com.manydesigns.elements.xml.XhtmlFragment;
import com.manydesigns.portofino.xml.XmlDiffer;

import java.text.MessageFormat;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class TreeTableDiffer implements XhtmlFragment {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    public final static String STATUS_OK = "Ok";
    public final static String STATUS_ONLY_ON_DB = "Only on db";
    public final static String STATUS_NOT_ON_DB = "Not on db";

    protected final XhtmlBuffer xb;
    protected int nodeCounter;

    public TreeTableDiffer() {
        super();
        xb = new XhtmlBuffer();
        nodeCounter = 1;
    }

    public void run(XmlDiffer.Differ rootDiffer) {
        run(rootDiffer, null);
    }

    public void run(XmlDiffer.Differ differ, String htmlClass) {
        String id = generateId();
        xb.openElement("tr");
        xb.addAttribute("id", id);
        if (htmlClass != null) {
            xb.addAttribute("class", htmlClass);
        }

        xb.openElement("td");
        xb.write(differ.getName());
        xb.closeElement("td");

        writeTypeAndStatus(differ.getType(), differ.getStatus());

        xb.closeElement("tr");

        String childOfId = generateChildOfId(id);
        // scan child differs
        for (XmlDiffer.Differ childDiffer : differ.getChildDiffers()) {
            run(childDiffer, childOfId);
        }
    }

    private String generateId() {
        return MessageFormat.format("id-{0}", nodeCounter++);
    }

    private String generateChildOfId(String parentId) {
        return MessageFormat.format("child-of-{0}", parentId);
    }

    private void writeTypeAndStatus(String type, XmlDiffer.Status status) {
        xb.openElement("td");
        xb.write(type);
        xb.closeElement("td");

        xb.openElement("td");
        switch(status) {
            case EQUAL:
                xb.addAttribute("class", "status_green");
                xb.write("Ok");
                break;
            case BOTH_NULL:
                xb.addAttribute("class", "status_red");
                xb.write("Ok");
                break;
            case SOURCE_NULL:
                xb.addAttribute("class", "status_red");
                xb.write("Not on db");
                break;
            case TARGET_NULL:
                xb.addAttribute("class", "status_red");
                xb.write("Only on db");
                break;
            case DIFFERENT:
                xb.addAttribute("class", "status_red");
                xb.write("Differences");
                break;
            default:
                throw new Error("Unknown case");
        }
        xb.closeElement("td");
    }

    //--------------------------------------------------------------------------
    // XhtmlFragment implementation
    //--------------------------------------------------------------------------
    public void toXhtml(XhtmlBuffer toBuffer) {
        xb.toXhtml(toBuffer);
    }

    /*
    //--------------------------------------------------------------------------
    // Databases
    //--------------------------------------------------------------------------

    public void diffDatabaseSourceNull(DatabaseDiff databaseDiff) {
        String databaseDisplayName = targetDatabase.getDatabaseName();
        writeDatabase(databaseDisplayName, STATUS_NOT_ON_DB);
        diffDatabaseChildren(databaseDiff);
    }

    public void diffDatabaseTargetNull(DatabaseDiff databaseDiff) {
        String databaseDisplayName = sourceDatabase.getDatabaseName();
        writeDatabase(databaseDisplayName, STATUS_ONLY_ON_DB);
        diffDatabaseChildren(databaseDiff);
    }

    public void diffDatabaseSourceTarget(DatabaseDiff databaseDiff) {
        String databaseDisplayName = sourceDatabase.getDatabaseName();
        writeDatabase(databaseDisplayName, STATUS_OK);
        diffDatabaseChildren(databaseDiff);
    }


    public void writeDatabase(String databaseDisplayName, String status) {
        databaseId = generateId();
        xb.openElement("tr");
        xb.addAttribute("id", databaseId);

        xb.openElement("td");
        xb.write(databaseDisplayName);
        xb.closeElement("td");

        writeTypeAndStatus("database", status);

        xb.closeElement("tr");
    }

    private void writeTypeAndStatus(String type, String status) {
        xb.openElement("td");
        xb.write(type);
        xb.closeElement("td");

        xb.openElement("td");
        if (STATUS_OK.equals(status)) {
            xb.addAttribute("class", "status_green");
        } else if (STATUS_ONLY_ON_DB.equals(status)) {
            xb.addAttribute("class", "status_red");
        } else if (STATUS_NOT_ON_DB.equals(status)) {
            xb.addAttribute("class", "status_red");
        }
        xb.write(status);
        xb.closeElement("td");
    }

    private String generateId() {
        return MessageFormat.format("id-{0}", nodeCounter++);
    }

    //--------------------------------------------------------------------------
    // Schemas
    //--------------------------------------------------------------------------

    public void diffSchemaSourceNull(SchemaDiff schemaDiff) {
        String schemaDisplayName = targetSchema.getSchemaName();
        writeSchema(schemaDisplayName, STATUS_NOT_ON_DB);
        diffSchemaChildren(schemaDiff);
    }

    public void diffSchemaTargetNull(SchemaDiff schemaDiff) {
        String schemaDisplayName = sourceSchema.getSchemaName();
        writeSchema(schemaDisplayName, STATUS_ONLY_ON_DB);
        diffSchemaChildren(schemaDiff);
    }

    public void diffSchemaSourceTarget(SchemaDiff schemaDiff) {
        String schemaDisplayName = sourceSchema.getSchemaName();
        writeSchema(schemaDisplayName, STATUS_OK);
        diffSchemaChildren(schemaDiff);
    }

    public void writeSchema(String schemaDisplayName, String status) {
        schemaId = generateId();
        xb.openElement("tr");
        xb.addAttribute("id", schemaId);
        xb.addAttribute("class", generateChildOfId(databaseId));

        xb.openElement("td");
        xb.write(schemaDisplayName);
        xb.closeElement("td");

        writeTypeAndStatus("schema", status);

        xb.closeElement("tr");
    }

    //--------------------------------------------------------------------------
    // Tables
    //--------------------------------------------------------------------------

    public void diffTableSourceNull(TableDiff tableDiff) {
        String tableDisplayName = targetTable.getTableName();
        writeTable(tableDisplayName, STATUS_NOT_ON_DB);
        diffTableChildren(tableDiff);
    }

    public void diffTableTargetNull(TableDiff tableDiff) {
        String tableDisplayName = sourceTable.getTableName();
        writeTable(tableDisplayName, STATUS_ONLY_ON_DB);
        diffTableChildren(tableDiff);
    }

    public void diffTableSourceTarget(TableDiff tableDiff) {
        String tableDisplayName = sourceTable.getTableName();
        writeTable(tableDisplayName, STATUS_OK);
        diffTableChildren(tableDiff);
    }

    public void writeTable(String tableDisplayName, String status) {
        tableId = generateId();
        xb.openElement("tr");
        xb.addAttribute("id", tableId);
        xb.addAttribute("class", generateChildOfId(schemaId));

        xb.openElement("td");
        xb.write(tableDisplayName);
        xb.closeElement("td");

        writeTypeAndStatus("table", status);

        xb.closeElement("tr");
    }

    //--------------------------------------------------------------------------
    // Table annotations
    //--------------------------------------------------------------------------

    public void diffTableAnnotationSourceNull(ModelAnnotationDiff modelAnnotationDiff) {
    }

    public void diffTableAnnotationTargetNull(ModelAnnotationDiff modelAnnotationDiff) {
    }

    public void diffTableAnnotationSourceTarget(ModelAnnotationDiff modelAnnotationDiff) {
    }

    //--------------------------------------------------------------------------
    // Columns
    //--------------------------------------------------------------------------

    public void diffColumnSourceNull(ColumnDiff columnDiff) {
        String columnDisplayName = targetColumn.getColumnName();
        writeColumn(columnDisplayName, STATUS_NOT_ON_DB);
        diffColumnChildren(columnDiff);
    }

    public void diffColumnTargetNull(ColumnDiff columnDiff) {
        String columnDisplayName = sourceColumn.getColumnName();
        writeColumn(columnDisplayName, STATUS_ONLY_ON_DB);
        diffColumnChildren(columnDiff);
    }

    public void diffColumnSourceTarget(ColumnDiff columnDiff) {
        String columnDisplayName = sourceColumn.getColumnName();
        writeColumn(columnDisplayName, STATUS_OK);
        diffColumnChildren(columnDiff);
    }

    public void writeColumn(String columnDisplayName, String status) {
        columnId = generateId();
        xb.openElement("tr");
        xb.addAttribute("id", columnId);
        xb.addAttribute("class", generateChildOfId(tableId));

        xb.openElement("td");
        xb.write(columnDisplayName);
        xb.closeElement("td");

        writeTypeAndStatus("column", status);

        xb.closeElement("tr");
    }


    //--------------------------------------------------------------------------
    // Column annotations
    //--------------------------------------------------------------------------

    public void diffColumnAnnotationSourceNull(ModelAnnotationDiff modelAnnotationDiff) {
    }

    public void diffColumnAnnotationTargetNull(ModelAnnotationDiff modelAnnotationDiff) {
    }

    public void diffColumnAnnotationSourceTarget(ModelAnnotationDiff modelAnnotationDiff) {
    }

    //--------------------------------------------------------------------------
    // Primary keys
    //--------------------------------------------------------------------------

    public void diffPrimaryKeySourceNull(PrimaryKeyDiff primaryKeyDiff) {
        String primaryKeyDisplayName = targetPrimaryKey.getPrimaryKeyName();
        writePrimaryKey(primaryKeyDisplayName, STATUS_NOT_ON_DB);
        diffPrimaryKeyChildren(primaryKeyDiff);
    }

    public void diffPrimaryKeyTargetNull(PrimaryKeyDiff primaryKeyDiff) {
        String primaryKeyDisplayName = sourcePrimaryKey.getPrimaryKeyName();
        writePrimaryKey(primaryKeyDisplayName, STATUS_ONLY_ON_DB);
        diffPrimaryKeyChildren(primaryKeyDiff);
    }

    public void diffPrimaryKeySourceTarget(PrimaryKeyDiff primaryKeyDiff) {
        String primaryKeyDisplayName = sourcePrimaryKey.getPrimaryKeyName();
        writePrimaryKey(primaryKeyDisplayName, STATUS_OK);
        diffPrimaryKeyChildren(primaryKeyDiff);
    }

    public void writePrimaryKey(String primaryKeyDisplayName, String status) {
        primaryKeyId = generateId();
        xb.openElement("tr");
        xb.addAttribute("id", primaryKeyId);
        xb.addAttribute("class", generateChildOfId(tableId));

        xb.openElement("td");
        xb.write(primaryKeyDisplayName);
        xb.closeElement("td");

        writeTypeAndStatus("primary key", status);

        xb.closeElement("tr");
    }

    //--------------------------------------------------------------------------
    // Primary key columns
    //--------------------------------------------------------------------------

    public void diffPrimaryKeyColumnSourceNull(PrimaryKeyColumnDiff primaryKeyColumnDiff) {
    }

    public void diffPrimaryKeyColumnTargetNull(PrimaryKeyColumnDiff primaryKeyColumnDiff) {
    }

    public void diffPrimaryKeyColumnSourceTarget(PrimaryKeyColumnDiff primaryKeyColumnDiff) {
    }


    //--------------------------------------------------------------------------
    // Foreign keys
    //--------------------------------------------------------------------------

    public void diffForeignKeySourceNull(ForeignKeyDiff foreignKeyDiff) {
        String foreignKeyDisplayName = targetForeignKey.getForeignKeyName();
        writeForeignKey(foreignKeyDisplayName, STATUS_NOT_ON_DB);
        diffForeignKeyChildren(foreignKeyDiff);
    }

    public void diffForeignKeyTargetNull(ForeignKeyDiff foreignKeyDiff) {
        String foreignKeyDisplayName = sourceForeignKey.getForeignKeyName();
        writeForeignKey(foreignKeyDisplayName, STATUS_ONLY_ON_DB);
        diffForeignKeyChildren(foreignKeyDiff);
    }

    public void diffForeignKeySourceTarget(ForeignKeyDiff foreignKeyDiff) {
        String foreignKeyDisplayName = sourceForeignKey.getForeignKeyName();
        writeForeignKey(foreignKeyDisplayName, STATUS_OK);
        diffForeignKeyChildren(foreignKeyDiff);
    }

    public void writeForeignKey(String foreignKeyDisplayName, String status) {
        foreignKeyId = generateId();
        xb.openElement("tr");
        xb.addAttribute("id", foreignKeyId);
        xb.addAttribute("class", generateChildOfId(tableId));

        xb.openElement("td");
        xb.write(foreignKeyDisplayName);
        xb.closeElement("td");

        writeTypeAndStatus("foreign key", status);

        xb.closeElement("tr");
    }


    //--------------------------------------------------------------------------
    // References
    //--------------------------------------------------------------------------

    public void diffReferenceSourceNull(ReferenceDiff referenceDiff) {
    }

    public void diffReferenceTargetNull(ReferenceDiff referenceDiff) {
    }

    public void diffReferenceSourceTarget(ReferenceDiff referenceDiff) {
    }


    //--------------------------------------------------------------------------
    // Foreign key annotations
    //--------------------------------------------------------------------------

    public void diffForeignKeyAnnotationSourceNull(ModelAnnotationDiff modelAnnotationDiff) {
    }

    public void diffForeignKeyAnnotationTargetNull(ModelAnnotationDiff modelAnnotationDiff) {
    }

    public void diffForeignKeyAnnotationSourceTarget(ModelAnnotationDiff modelAnnotationDiff) {
    }

    //--------------------------------------------------------------------------
    // Getter/setter
    //--------------------------------------------------------------------------
*/
}
