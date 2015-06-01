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

package com.manydesigns.portofino.pageactions.map;

import com.manydesigns.elements.gfx.ColorUtils;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * @author Emanuele Poggi     - emanuele.poggi@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class Map {
    public static final String copyright = "Copyright (c) 2005-2015, ManyDesigns srl";

    //--------------------------------------------------------------------------
    // Fields
    //--------------------------------------------------------------------------

    protected String id;
    protected String name;
    protected List<Marker> markers ;

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    public Map() {
       this.markers = new ArrayList<Marker>();
    }

    public Map( String id, String name ) {
       this.id = id;
       this.name = name;
       this.markers = new ArrayList<Marker>();
    }

    public Map(String id, String name, List<Marker> markers ) {
        this.id = id;
        this.name = name;
        this.markers = markers;
    }

    //--------------------------------------------------------------------------
    // Getters/setters
    //--------------------------------------------------------------------------

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean addMarker( Marker marker ){
        return markers.add(marker);
    }

    public List<Marker> getMarkers(){
        return markers;
    }

}
