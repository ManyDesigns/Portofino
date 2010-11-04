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

package com.manydesigns.portofino.model.diff;

import com.manydesigns.elements.logging.LogUtil;
import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.JavaClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.portofino.model.annotations.ModelAnnotation;
import com.manydesigns.portofino.model.datamodel.*;
import com.manydesigns.portofino.xml.XmlAttribute;

import java.text.MessageFormat;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class MergeDiffer extends AbstractDiffer {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    //--------------------------------------------------------------------------
    // Constructor
    //--------------------------------------------------------------------------

    public MergeDiffer() {
        super();
    }

    //--------------------------------------------------------------------------
    // Databases
    //--------------------------------------------------------------------------

    public void diffDatabaseSourceNull(DatabaseDiff databaseDiff) {
        diffDatabaseChildren(databaseDiff);
    }

    public void diffDatabaseTargetNull(DatabaseDiff databaseDiff) {
        targetDatabase = new Database();
        diffDatabaseSourceTarget(databaseDiff);
    }

    public void diffDatabaseSourceTarget(DatabaseDiff databaseDiff) {
        mergeProperties(sourceDatabase, targetDatabase);
        diffDatabaseChildren(databaseDiff);
    }


    //--------------------------------------------------------------------------
    // Schemas
    //--------------------------------------------------------------------------

    public void diffSchemaSourceNull(SchemaDiff schemaDiff) {
        diffSchemaChildren(schemaDiff);
    }

    public void diffSchemaTargetNull(SchemaDiff schemaDiff) {
        targetSchema = new Schema(targetDatabase);
        targetDatabase.getSchemas().add(targetSchema);
        diffSchemaSourceTarget(schemaDiff);
    }

    public void diffSchemaSourceTarget(SchemaDiff schemaDiff) {
        mergeProperties(sourceSchema, targetSchema);
        diffSchemaChildren(schemaDiff);
    }

    //--------------------------------------------------------------------------
    // Tables
    //--------------------------------------------------------------------------

    public void diffTableSourceNull(TableDiff tableDiff) {
        diffTableChildren(tableDiff);
    }

    public void diffTableTargetNull(TableDiff tableDiff) {
        targetTable = new Table(targetSchema);
        targetSchema.getTables().add(targetTable);
        diffTableSourceTarget(tableDiff);
    }

    public void diffTableSourceTarget(TableDiff tableDiff) {
        mergeProperties(sourceTable, targetTable);
        diffTableChildren(tableDiff);
    }

    //--------------------------------------------------------------------------
    // Table annotations
    //--------------------------------------------------------------------------

    public void diffTableAnnotationSourceNull(ModelAnnotationDiff modelAnnotationDiff) {
        diffTableAnnotationChildren(modelAnnotationDiff);
    }

    public void diffTableAnnotationTargetNull(ModelAnnotationDiff modelAnnotationDiff) {
        targetModelAnnotation = new ModelAnnotation();
        targetTable.getModelAnnotations().add(targetModelAnnotation);
        diffTableAnnotationSourceTarget(modelAnnotationDiff);
    }

    public void diffTableAnnotationSourceTarget(ModelAnnotationDiff modelAnnotationDiff) {
        mergeProperties(sourceModelAnnotation, targetModelAnnotation);
        diffTableAnnotationChildren(modelAnnotationDiff);
    }

    //--------------------------------------------------------------------------
    // Columns
    //--------------------------------------------------------------------------

    public void diffColumnSourceNull(ColumnDiff columnDiff) {
        diffColumnChildren(columnDiff);
    }

    public void diffColumnTargetNull(ColumnDiff columnDiff) {
        targetColumn = new Column(targetTable);
        targetTable.getColumns().add(targetColumn);
        diffColumnSourceTarget(columnDiff);
    }

    public void diffColumnSourceTarget(ColumnDiff columnDiff) {
        mergeProperties(sourceColumn, targetColumn);
        diffColumnChildren(columnDiff);
    }

    //--------------------------------------------------------------------------
    // Column annotations
    //--------------------------------------------------------------------------

    public void diffColumnAnnotationSourceNull(ModelAnnotationDiff modelAnnotationDiff) {
        diffColumnAnnotationChildren(modelAnnotationDiff);
    }

    public void diffColumnAnnotationTargetNull(ModelAnnotationDiff modelAnnotationDiff) {
        targetModelAnnotation = new ModelAnnotation();
        targetColumn.getModelAnnotations().add(targetModelAnnotation);
        diffColumnAnnotationSourceTarget(modelAnnotationDiff);
    }

    public void diffColumnAnnotationSourceTarget(ModelAnnotationDiff modelAnnotationDiff) {
        mergeProperties(sourceModelAnnotation, targetModelAnnotation);
        diffColumnAnnotationChildren(modelAnnotationDiff);
    }

    //--------------------------------------------------------------------------
    // Primary keys
    //--------------------------------------------------------------------------

    public void diffPrimaryKeySourceNull(PrimaryKeyDiff primaryKeyDiff) {
        targetTable.setPrimaryKey(null);
//        diffPrimaryKeyChildren(primaryKeyDiff);
    }

    public void diffPrimaryKeyTargetNull(PrimaryKeyDiff primaryKeyDiff) {
        targetPrimaryKey = new PrimaryKey(targetTable);
        targetTable.setPrimaryKey(targetPrimaryKey);
        diffPrimaryKeySourceTarget(primaryKeyDiff);
    }

    public void diffPrimaryKeySourceTarget(PrimaryKeyDiff primaryKeyDiff) {
        mergeProperties(sourcePrimaryKey, targetPrimaryKey);
        diffPrimaryKeyChildren(primaryKeyDiff);
    }

    //--------------------------------------------------------------------------
    // Primary key columns
    // Il merge delle colonne delle chiavi primarie prevede che alla fine
    // source e target siano esattamente allineati.
    // Il diff deve prevedere: aggiunta, modifica, rimozione.
    //--------------------------------------------------------------------------

    public void diffPrimaryKeyColumnSourceNull(PrimaryKeyColumnDiff primaryKeyColumnDiff) {
        targetPrimaryKey.getPrimaryKeyColumns().remove(targetPrimaryKeyColumn);
        diffPrimaryKeyColumnChildren(primaryKeyColumnDiff);
    }

    public void diffPrimaryKeyColumnTargetNull(PrimaryKeyColumnDiff primaryKeyColumnDiff) {
        targetPrimaryKeyColumn = new PrimaryKeyColumn(targetPrimaryKey);
        targetPrimaryKey.getPrimaryKeyColumns().add(targetPrimaryKeyColumn);
        diffPrimaryKeyColumnSourceTarget(primaryKeyColumnDiff);
    }

    public void diffPrimaryKeyColumnSourceTarget(PrimaryKeyColumnDiff primaryKeyColumnDiff) {
        mergeProperties(sourcePrimaryKeyColumn, targetPrimaryKeyColumn);
        diffPrimaryKeyColumnChildren(primaryKeyColumnDiff);
    }

    //--------------------------------------------------------------------------
    // Foreign keys
    //--------------------------------------------------------------------------

    public void diffForeignKeySourceNull(ForeignKeyDiff foreignKeyDiff) {
        targetTable.getForeignKeys().remove(targetForeignKey);
//        diffForeignKeyChildren(foreignKeyDiff);
    }

    public void diffForeignKeyTargetNull(ForeignKeyDiff foreignKeyDiff) {
        targetForeignKey = new ForeignKey(targetTable);
        targetTable.getForeignKeys().add(targetForeignKey);
        diffForeignKeySourceTarget(foreignKeyDiff);
    }

    public void diffForeignKeySourceTarget(ForeignKeyDiff foreignKeyDiff) {
        mergeProperties(sourceForeignKey, targetForeignKey);
        diffForeignKeyChildren(foreignKeyDiff);
    }

    //--------------------------------------------------------------------------
    // References
    // Il merge dei reference prevede che alla fine source e target
    // siano esattamente allineati.
    // Il diff deve prevedere: aggiunta, modifica, rimozione.
    //--------------------------------------------------------------------------

    public void diffReferenceSourceNull(ReferenceDiff referenceDiff) {
        targetForeignKey.getReferences().remove(targetReference);
        diffReferenceChildren(referenceDiff);
    }

    public void diffReferenceTargetNull(ReferenceDiff referenceDiff) {
        targetReference = new Reference(targetForeignKey);
        targetForeignKey.getReferences().add(targetReference);
        diffReferenceSourceTarget(referenceDiff);
    }

    public void diffReferenceSourceTarget(ReferenceDiff referenceDiff) {
        mergeProperties(sourceReference, targetReference);
        diffReferenceChildren(referenceDiff);
    }

    //--------------------------------------------------------------------------
    // Foreign key annotations
    //--------------------------------------------------------------------------

    public void diffForeignKeyAnnotationSourceNull(ModelAnnotationDiff modelAnnotationDiff) {
        diffForeignKeyAnnotationChildren(modelAnnotationDiff);
    }

    public void diffForeignKeyAnnotationTargetNull(ModelAnnotationDiff modelAnnotationDiff) {
        diffForeignKeyAnnotationChildren(modelAnnotationDiff);
    }

    public void diffForeignKeyAnnotationSourceTarget(ModelAnnotationDiff modelAnnotationDiff) {
        diffForeignKeyAnnotationChildren(modelAnnotationDiff);
    }



    //--------------------------------------------------------------------------
    // Merge properties by reflection
    //--------------------------------------------------------------------------

    public void mergeProperties(Object source, Object target) {
        Class javaClass = source.getClass();
        ClassAccessor classAccessor =
                JavaClassAccessor.getClassAccessor(javaClass);

        for (PropertyAccessor propertyAccessor : classAccessor.getProperties()) {
            XmlAttribute xmlAttribute =
                    propertyAccessor.getAnnotation(XmlAttribute.class);
            if (xmlAttribute == null) {
                continue;
            }

            String name = propertyAccessor.getName();

            Object value = null;
            try {
                value = propertyAccessor.get(source);
            } catch (Throwable e) {
                LogUtil.warningMF(logger,
                        "Cannot get source attribute/property ''{0}''", e, name);
            }

            if (value == null) {
                if (xmlAttribute.required()) {
                    throw new Error(MessageFormat.format(
                            "Attribute ''{0}'' required. {1}", name));
                }
            } else {
                try {
                    propertyAccessor.set(target, value);
                } catch (Throwable e) {
                    LogUtil.warningMF(logger,
                            "Cannot set target attribute/property ''{0}''", e, name);
                }
            }
        }
    }

}
