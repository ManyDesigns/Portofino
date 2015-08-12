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

package com.manydesigns.portofino.pageactions.map.configuration;

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
@XmlType(propOrder = {"latitude","longitude","zoom","geolocation"})
public class MapConfiguration implements PageActionConfiguration {
    public static final String copyright = "Copyright (c) 2005-2015, ManyDesigns srl";

    //**************************************************************************
    // Fields
    //**************************************************************************

    protected BigDecimal latitude = new BigDecimal( 44.0 );
    protected BigDecimal longitude = new BigDecimal( 9.0 );
    protected Boolean geolocation = false ;
    protected Integer zoom = 8 ;

    //**************************************************************************
    // Fields for wire-up
    //**************************************************************************

    //**************************************************************************
    // Logging
    //**************************************************************************

    public static final Logger logger = LoggerFactory.getLogger(MapConfiguration.class);

    //**************************************************************************
    // Constructors
    //**************************************************************************

    public MapConfiguration() {
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
    @FieldSet("position")
    @LabelI18N("latitude")
    @MinDecimalValue(-85.0)
    @MaxDecimalValue(85.0)
    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    @XmlAttribute
    @Required
    @FieldSet("position")
    @LabelI18N("longitude")
    @MinDecimalValue(-180.0)
    @MaxDecimalValue(180.0)
    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    @XmlAttribute
    @Required
    @FieldSet("position")
    @LabelI18N("geolocation")
    public Boolean getGeolocation() {
        return geolocation;
    }

    public void setGeolocation(Boolean geolocation) {
        this.geolocation = geolocation;
    }

    @XmlAttribute
    @Required
    @FieldSet("position")
    @LabelI18N("zoom")
    public Integer getZoom() {
        return zoom;
    }

    public void setZoom(Integer zoom) {
        this.zoom = zoom;
    }
}
