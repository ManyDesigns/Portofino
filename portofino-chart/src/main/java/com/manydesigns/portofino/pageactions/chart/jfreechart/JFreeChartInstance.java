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

package com.manydesigns.portofino.pageactions.chart.jfreechart;

import com.manydesigns.elements.xml.XhtmlBuffer;
import com.manydesigns.elements.xml.XhtmlFragment;
import org.jetbrains.annotations.NotNull;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.ChartEntity;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
* @author Alessio Stalla       - alessio.stalla@manydesigns.com
*/
public class JFreeChartInstance implements XhtmlFragment {
    public static final String copyright =
            "Copyright (c) 2005-2015, ManyDesigns srl";

    protected final JFreeChart chart;
    protected final File file;
    protected final ChartRenderingInfo renderingInfo;
    protected final int width;
    protected final int height;

    protected final String chartUrl;
    protected final String alt;
    protected final String mapId;

    public JFreeChartInstance(JFreeChart chart, File file, String mapId, String alt,
                              int width, int height, String chartUrl) throws IOException {
        this.chart = chart;
        this.file = file;
        this.mapId = mapId;
        this.alt = alt;
        this.width = width;
        this.height = height;
        this.chartUrl = chartUrl;
        renderingInfo = new ChartRenderingInfo();
        ChartUtilities.saveChartAsPNG(file, chart, width, height, renderingInfo);
    }

    public void toXhtml(@NotNull XhtmlBuffer xb) {
        xb.openElement("img");
        xb.addAttribute("src", chartUrl);
        xb.addAttribute("alt", alt);
        xb.addAttribute("usemap", "#" + mapId);
        xb.addAttribute("style", "border: none;");
        xb.addAttribute("class", "img-responsive");
        xb.closeElement("img");

        xb.openElement("map");
        xb.addAttribute("id", mapId);
        xb.addAttribute("name", mapId);
        Iterator iter = renderingInfo.getEntityCollection().iterator();
        while (iter.hasNext()) {
            ChartEntity ce = (ChartEntity) iter.next();
            String shapeType = ce.getShapeType();
            String shapeCoords = ce.getShapeCoords();
            String tooltipText = ce.getToolTipText();
            String urltext = ce.getURLText();

            if (urltext == null)
                continue;

            xb.openElement("area");
            xb.addAttribute("shape", shapeType);
            xb.addAttribute("coords", shapeCoords);
            xb.addAttribute("title", tooltipText);
            xb.addAttribute("alt", tooltipText);
            xb.addAttribute("href", urltext);
            xb.closeElement("area");
        }
        xb.closeElement("map");
    }
}
