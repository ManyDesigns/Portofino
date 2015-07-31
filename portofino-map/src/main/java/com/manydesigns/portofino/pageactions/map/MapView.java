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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Emanuele Poggi    - emanuele.poggi@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
public class MapView {
    public static final String copyright = "Copyright (c) 2005-2015, ManyDesigns srl";
    public static final Logger logger = LoggerFactory.getLogger(MapView.class);
    List<Marker> markers = new ArrayList<Marker>();

    //--------------------------------------------------------------------------
    // Constructors and builder overrides
    //--------------------------------------------------------------------------

    public MapView() {
    }

    public MapView(Position center) {

    }

    //--------------------------------------------------------------------------
    // Markers
    //--------------------------------------------------------------------------

    public int addMarkers(Collection<Marker> markers) {
        int counter = 0;
        for (Marker marker : markers) {
            boolean result = addMarker(marker);
            if (result) {
                counter++;
            }
        }
        logger.debug("Added {} events", counter);
        return counter;
    }

    public boolean addMarker(Marker marker) {
       return markers.add(marker);
    }

    public void clearMarkers() {
        logger.debug("Clearing events");
        /*for (MonthViewWeek week : weeks) {
            week.clearEvents();
        }*/
    }

    public void sortMarkers() {
        logger.debug("Sorting events");
        /*for (MonthViewWeek week : weeks) {
            week.sortEvents();
        } */
    }


}
