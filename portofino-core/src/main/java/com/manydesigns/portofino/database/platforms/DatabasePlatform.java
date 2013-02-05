/*
 * Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
            "Copyright (c) 2005-2013, ManyDesigns srl";

    final static String STATUS_CREATED = "created";
    final static String STATUS_OK = "ok";
    final static String STATUS_DRIVER_NOT_FOUND = "driver not found";
    final static String STATUS_DRIVER_ERROR = "driver error";

    String getDescription();
    String getStandardDriverClassName();
    Dialect getHibernateDialect();
    /**
     * Is Hibernate able to automatically the dialect from a JDBC connection for this database platform?
     * @return false if and only if the hibernate.dialect property must be explicitly set in order to connect to
     * an instance of this database platform.
     */
    boolean isDialectAutodetected();
    String getConnectionStringTemplate();

    @Status(red={STATUS_DRIVER_ERROR}, amber={STATUS_CREATED, STATUS_DRIVER_NOT_FOUND}, green={STATUS_OK})
    String getStatus();

    void test();
    boolean isApplicable(ConnectionProvider connectionProvider);
    void shutdown(ConnectionProvider connectionProvider);

    List<String> getSchemaNames(DatabaseMetaData databaseMetaData) throws SQLException;

}
