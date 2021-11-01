/*
 * Copyright (C) 2005-2020 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.portofino.resourceactions.crud.configuration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.manydesigns.portofino.model.Annotated;
import com.manydesigns.portofino.model.Annotation;
import com.manydesigns.portofino.model.Model;
import org.apache.commons.configuration2.Configuration;
import org.eclipse.emf.ecore.EAnnotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/

@XmlAccessorType(value = XmlAccessType.NONE)
@XmlType(propOrder = {"enabled","name", "label","insertable","updatable","inSummary","searchable","annotations"})
@JsonIgnoreProperties(ignoreUnknown = true) //Deserialize VirtualProperties as normal properties as we're only interested in common fields for now
public class CrudProperty implements Annotated {
    public static final String copyright =
            "Copyright (C) 2005-2020 ManyDesigns srl";


    //**************************************************************************
    // Fields
    //**************************************************************************

    protected String name;
    protected String label;
    protected boolean searchable;
    protected boolean inSummary;
    protected boolean enabled;
    protected boolean insertable;
    protected boolean updatable;

    protected final List<Annotation> annotations = new ArrayList<>();

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger =
            LoggerFactory.getLogger(CrudProperty.class);

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public CrudProperty() {}

    //**************************************************************************
    // Configuration implementation
    //**************************************************************************

    public void init(Model model, Configuration configuration) {
        assert name != null;
        for(Annotation annotation : annotations) {
            annotation.reset();
            annotation.init(model, configuration);
            annotation.link(model, configuration);
        }
    }

    //**************************************************************************
    // Getters/setters
    //**************************************************************************


    @XmlAttribute(required = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlAttribute(required = false)
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @XmlAttribute(required = true)
    public boolean isSearchable() {
        return searchable;
    }

    public void setSearchable(boolean searchable) {
        this.searchable = searchable;
    }

    @XmlAttribute(required = true)
    public boolean isInSummary() {
        return inSummary;
    }

    public void setInSummary(boolean inSummary) {
        this.inSummary = inSummary;
    }

    @XmlAttribute(required = true)
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @XmlAttribute(required = true)
    public boolean isInsertable() {
        return insertable;
    }

    public void setInsertable(boolean insertable) {
        this.insertable = insertable;
    }

    @XmlAttribute(required = true)
    public boolean isUpdatable() {
        return updatable;
    }

    public void setUpdatable(boolean updatable) {
        this.updatable = updatable;
    }

    @JsonProperty("annotations")
    @XmlElementWrapper(name = "annotations")
    @XmlElement(name = "annotation", type = Annotation.class)
    public List<Annotation> getAnnotations() {
        return annotations;
    }

    //Needed for Jackson
    public void setAnnotations(List<Annotation> annotations) {
        this.annotations.clear();
        this.annotations.addAll(annotations);
    }

    @Override
    public void addAnnotation(EAnnotation eAnnotation) {
        //Do nothing as we're not interested in EAnnotations here
    }
}
