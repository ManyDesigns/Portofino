/*
 * Copyright (C) 2005-2013 ManyDesigns srl.  All rights reserved.
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
            "Copyright (c) 2005-2013, ManyDesigns srl";

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
