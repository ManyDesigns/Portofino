/*
 * Copyright (C) 2005-2015 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.pageactions.chart.chartjs.configuration;

import com.manydesigns.elements.annotations.*;
import com.manydesigns.elements.util.BootstrapSizes;
import com.manydesigns.portofino.di.Inject;
import com.manydesigns.portofino.dispatcher.PageActionConfiguration;
import com.manydesigns.portofino.model.database.Database;
import com.manydesigns.portofino.model.database.DatabaseLogic;
import com.manydesigns.portofino.modules.DatabaseModule;
import com.manydesigns.portofino.persistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.*;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@XmlRootElement(name = "configuration")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {"name","type","database", "query"})
public class ChartJsConfiguration implements PageActionConfiguration {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    protected String name;
    protected String type;
    protected String database;
    protected String query;

    protected Type actualType;
    protected Database actualDatabase;

    @Inject(DatabaseModule.PERSISTENCE)
    public Persistence persistence;

    public static final Logger logger =
            LoggerFactory.getLogger(ChartJsConfiguration.class);


    public ChartJsConfiguration() {
        super();
    }

    public static enum Type {

        LINE("Line", 2), BAR("Bar", 2), RADAR("Radar", 2), PIE("Pie", 1), POLAR("PolarArea", 1), DOUGHNUT("Doughnut", 1);

        public final String jsName;
        public final int kind;

        Type(String jsName, int kind) {
            this.jsName = jsName;
            this.kind = kind;
        }

        public String getJsName() {
            return jsName;
        }

        public int getKind() {
            return kind;
        }
    }

    //**************************************************************************
    // Configuration implementation
    //**************************************************************************

    public void init() {
        assert name != null;
        assert type != null;
        assert database != null;
        assert query != null;
        actualDatabase = DatabaseLogic.findDatabaseByName(persistence.getModel(), database);
        actualType = Type.valueOf(type);
    }

    //**************************************************************************
    // Getters/setters
    //**************************************************************************
    @Required
    @XmlAttribute(required = true)
    @LabelI18N("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlAttribute(name = "type", required = true)
    @Required
    @LabelI18N("type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Required
    @XmlAttribute(required = true)
    @LabelI18N("database")
    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    @Required
    @LabelI18N("sql.query")
    @Multiline
    @XmlAttribute(required = true)
    @CssClass(BootstrapSizes.FILL_ROW)
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Database getActualDatabase() {
        return actualDatabase;
    }

    public Type getActualType() {
        return actualType;
    }
}
