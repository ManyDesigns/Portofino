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

package com.manydesigns.portofino.model.portlets;

import com.manydesigns.elements.annotations.Label;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class Portlet {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";

    protected String name;
    protected String type;
    protected String title;
    protected String legend;
    protected String database;
    protected String sql;
    protected String urlExpression;

    public Portlet(String name, String type, String title, String legend,
                   String database, String sql, String urlExpression) {
        this.name = name;
        this.type = type;
        this.title = title;
        this.legend = legend;
        this.database = database;
        this.sql = sql;
        this.urlExpression = urlExpression;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLegend() {
        return legend;
    }

    public void setLegend(String legend) {
        this.legend = legend;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    @Label("SQL")
    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    @Label("URL expression")
    public String getUrlExpression() {
        return urlExpression;
    }

    public void setUrlExpression(String urlExpression) {
        this.urlExpression = urlExpression;
    }
    
}
