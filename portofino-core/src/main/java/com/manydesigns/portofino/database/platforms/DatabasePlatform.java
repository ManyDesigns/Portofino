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

package com.manydesigns.portofino.database.platforms;

import com.manydesigns.elements.annotations.Status;
import com.manydesigns.portofino.model.database.ConnectionProvider;
import org.hibernate.dialect.Dialect;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public interface DatabasePlatform {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    final static String STATUS_CREATED = "created";
    final static String STATUS_OK = "ok";
    final static String STATUS_DRIVER_NOT_FOUND = "driver not found";

    String getDescription();
    String getStandardDriverClassName();
    Dialect getHibernateDialect();

    @Status(red={}, amber={STATUS_CREATED, STATUS_DRIVER_NOT_FOUND}, green={STATUS_OK})
    String getStatus();

    void test();
    boolean isApplicable(ConnectionProvider connectionProvider);
    void shutdown(ConnectionProvider connectionProvider);

    List<String> getSchemaNames(DatabaseMetaData databaseMetaData) throws SQLException;

}
