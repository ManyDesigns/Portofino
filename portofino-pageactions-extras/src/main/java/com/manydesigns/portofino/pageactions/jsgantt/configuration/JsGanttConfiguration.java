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
package com.manydesigns.portofino.pageactions.jsgantt.configuration;

import com.manydesigns.elements.annotations.*;
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
public class JsGanttConfiguration implements PageActionConfiguration {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************
    boolean showResource = true;
    boolean showDuration = true;
    boolean showComplete = true;
    String captionType = "None";
    boolean showStartDate = true;
    boolean showEndDate = true;
    String dateDisplayFormat = "mm/dd/yyyy";

    //**************************************************************************
    // Fields for wire-up
    //**************************************************************************

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(JsGanttConfiguration.class);

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public JsGanttConfiguration() {
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
    @LabelI18N("jsgantt.configuration.captionType")
    @Select(values = {"None","Caption","Resource","Duration","Complete"}, labels = {"None","Caption","Resource","Duration","Complete"})
    public String getCaptionType() {
        return captionType;
    }

    public void setCaptionType(String captionType) {
        this.captionType = captionType;
    }

    @XmlAttribute
    @Required
    @LabelI18N("jsgantt.configuration.dateDisplayFormat")
    public String getDateDisplayFormat() {
        return dateDisplayFormat;
    }

    public void setDateDisplayFormat(String dateDisplayFormat) {
        this.dateDisplayFormat = dateDisplayFormat;
    }

    @XmlAttribute
    @Required
    @LabelI18N("jsgantt.configuration.showComplete")
    public boolean isShowComplete() {
        return showComplete;
    }

    public void setShowComplete(boolean showComplete) {
        this.showComplete = showComplete;
    }

    @XmlAttribute
    @Required
    @LabelI18N("jsgantt.configuration.showDuration")
    public boolean isShowDuration() {
        return showDuration;
    }

    public void setShowDuration(boolean showDuration) {
        this.showDuration = showDuration;
    }

    @XmlAttribute
    @Required
    @LabelI18N("jsgantt.configuration.showStartDate")
    public boolean isShowStartDate() {
        return showStartDate;
    }

    public void setShowStartDate(boolean showStartDate) {
        this.showStartDate = showStartDate;
    }

    @XmlAttribute
    @Required
    @LabelI18N("jsgantt.configuration.showEndDate")
    public boolean isShowEndDate() {
        return showEndDate;
    }

    public void setShowEndDate(boolean showEndDate) {
        this.showEndDate = showEndDate;
    }

    @XmlAttribute
    @Required
    @LabelI18N("jsgantt.configuration.showResources")
    public boolean isShowResource() {
        return showResource;
    }

    public void setShowResource(boolean showResource) {
        this.showResource = showResource;
    }


}
