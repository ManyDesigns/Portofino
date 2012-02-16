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

package com.manydesigns.portofino.pageactions.timesheet.configuration;

import com.manydesigns.elements.annotations.LabelI18N;
import com.manydesigns.elements.annotations.Required;
import com.manydesigns.portofino.application.Application;
import com.manydesigns.portofino.dispatcher.PageActionConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@XmlRootElement(name = "configuration")
@XmlAccessorType(XmlAccessType.NONE)
public class TimesheetConfiguration implements PageActionConfiguration {
    public static final String copyright =
            "Copyright (c) 2005-2011, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected String level1Label = "Level 1";
    protected String level2Label = "Level 2";
    protected String level3Label = "Level 3";

    //**************************************************************************
    // Fields for wire-up
    //**************************************************************************

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(TimesheetConfiguration.class);

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public TimesheetConfiguration() {
        super();
    }

    //**************************************************************************
    // Configuration implementation
    //**************************************************************************

    public void init(Application application) {}

    //**************************************************************************
    // Getters/setters
    //**************************************************************************


    @XmlAttribute
    @Required
    @LabelI18N("timesheet.level1.label")
    public String getLevel1Label() {
        return level1Label;
    }

    public void setLevel1Label(String level1Label) {
        this.level1Label = level1Label;
    }

    @XmlAttribute
    @Required
    @LabelI18N("timesheet.level2.label")
    public String getLevel2Label() {
        return level2Label;
    }

    public void setLevel2Label(String level2Label) {
        this.level2Label = level2Label;
    }

    @XmlAttribute
    @Required
    @LabelI18N("timesheet.level3.label")
    public String getLevel3Label() {
        return level3Label;
    }

    public void setLevel3Label(String level3Label) {
        this.level3Label = level3Label;
    }
}
