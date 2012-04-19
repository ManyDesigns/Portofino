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

package com.manydesigns.elements.googlemaps;

import com.manydesigns.elements.Element;
import com.manydesigns.elements.Mode;
import com.manydesigns.elements.xml.XhtmlBuffer;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.text.MessageFormat;
import java.util.ArrayList;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class GoogleMapsElement implements Element {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    private int height;
    private String id;
    private Mode mode = Mode.EDIT;

    private double centerLat;
    private double centerLng;
    private int zoom = 1;

    private final ArrayList<GoogleMapsMarker> markers;

    public GoogleMapsElement() {
        markers = new ArrayList<GoogleMapsMarker>();
    }

    public void addMarker(GoogleMapsMarker marker) {
        markers.add(marker);
    }

    public void readFromRequest(HttpServletRequest httpServletRequest) {}

    public boolean validate() {
        return true;
    }

    public void readFromObject(Object o) {
    }

    public void writeToObject(Object o) {
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public void toXhtml(@NotNull XhtmlBuffer xb) {
        xb.openElement("script");
        xb.addAttribute("type", "text/javascript");
        xb.writeNoHtmlEscape("function initialize() {" +
                " if (GBrowserIsCompatible()) {" +
                " var turqIcon = new GIcon();" +
                " turqIcon.image = 'http://www.google.com/uds/samples/places/temp_marker.png';" +
                " turqIcon.iconSize = new GSize(20, 34);" +
                " turqIcon.iconAnchor = new GPoint(9, 34);" +
                " var map = new GMap2(document.getElementById(\"" + id + "\"));" +
                " map.setCenter(new GLatLng(" + centerLat + ", " + centerLng + "), " + zoom + ");" +
                " map.setUIToDefault();");

        // output the markers
        for (GoogleMapsMarker marker : markers) {
            marker.toXhtml(xb);
        }

        xb.writeNoHtmlEscape(" }" +
                        " }" +
                        " addToOnLoad(initialize);");
        xb.closeElement("script");

        xb.openElement("div");
        xb.addAttribute("id", id);
        xb.addAttribute("style", MessageFormat.format("height: {0}px",
                Integer.toString(height)));
        xb.closeElement("div");
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getCenterLat() {
        return centerLat;
    }

    public void setCenterLat(double centerLat) {
        this.centerLat = centerLat;
    }

    public double getCenterLng() {
        return centerLng;
    }

    public void setCenterLng(double centerLng) {
        this.centerLng = centerLng;
    }

    public void setCenter(double centerLat, double centerLng) {
        setCenterLat(centerLat);
        setCenterLng(centerLng);
    }

    public int getZoom() {
        return zoom;
    }

    public void setZoom(int zoom) {
        this.zoom = zoom;
    }
}
