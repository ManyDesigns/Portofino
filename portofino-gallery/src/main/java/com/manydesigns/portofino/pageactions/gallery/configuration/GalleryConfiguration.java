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

package com.manydesigns.portofino.pageactions.gallery.configuration;

import com.manydesigns.elements.annotations.*;
import com.manydesigns.portofino.dispatcher.PageActionConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.*;
import java.math.BigDecimal;
import java.math.BigInteger;

/*
* @author Emanuele Poggi    - emanuele.poggi@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
@XmlRootElement(name = "configuration")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = {"autoplay","autoplayInterval","height","width"})
public class GalleryConfiguration implements PageActionConfiguration {
    public static final String copyright = "Copyright (c) 2005-2015, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected Boolean autoplay = true ;
    protected Integer autoplayInterval = 4000 ;
    protected Integer width = 720 ;
    protected Integer height = 480 ;

    //**************************************************************************
    // Fields for wire-up
    //**************************************************************************

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger = LoggerFactory.getLogger(GalleryConfiguration.class);

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public GalleryConfiguration() {
        super();
    }

    //**************************************************************************
    // Configuration implementation
    //**************************************************************************

    public void init() {}

    //**************************************************************************
    // Getters/setters
    //**************************************************************************

    @XmlAttribute
    @Required
    @FieldSet("Autoplay")
    @LabelI18N("autoplay")
    public Boolean getAutoplay() {
        return autoplay;
    }

    public void setAutoplay(Boolean autoplay) {
        this.autoplay = autoplay;
    }

    @XmlAttribute
    @Required
    @FieldSet("Autoplay")
    @LabelI18N("interval")
    public Integer getAutoplayInterval() {
        return autoplayInterval;
    }

    public void setAutoplayInterval (Integer autoplayInterval) {
        this.autoplayInterval = autoplayInterval;
    }

    @XmlAttribute
    @Required
    @FieldSet("Size")
    @LabelI18N("width")
    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    @XmlAttribute
    @Required
    @FieldSet("Size")
    @LabelI18N("height")
    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }
}
