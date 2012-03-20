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

import com.manydesigns.elements.xml.XhtmlBuffer;
import com.manydesigns.elements.xml.XhtmlFragment;
import org.jetbrains.annotations.NotNull;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class GoogleMapsMarker implements XhtmlFragment {
    public static final String copyright =
            "Copyright (c) 2005-2012, ManyDesigns srl";

    private String title;
    private String icon;
    private double latitude;
    private double longitude;
    private String clickLink;

    public GoogleMapsMarker() {}

    public GoogleMapsMarker(String title, double latitude, double longitude) {
        this.title = title;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void toXhtml(@NotNull XhtmlBuffer xb) {
        // genera un id univoco
        String markerName = "marker" + this.hashCode();
        StringBuffer options = new StringBuffer();
        options.append("{title: '");
        options.append(title);
        options.append("'");
        if (icon != null) {
            options.append(", icon: ").append(icon);
        }
        options.append("}");
        xb.writeNoHtmlEscape(" var " + markerName + " = new GMarker(new GLatLng(" +
                latitude + ", " + longitude +
                "), " + options.toString() + ");");
        xb.writeNoHtmlEscape(" map.addOverlay(" + markerName + ");");
        if (clickLink != null) {
            xb.writeNoHtmlEscape(" GEvent.addListener(" + markerName + ", \"click\", function() {" +
                    " window.location = '" + clickLink + "';" +
                    "});");
        }
    }

    public String getClickLink() {
        return clickLink;
    }

    public void setClickLink(String clickLink) {
        this.clickLink = clickLink;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
