/*
 * Copyright (C) 2005-2012 ManyDesigns srl.  All rights reserved.
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

package com.manydesigns.elements.jfreechart;

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
            "Copyright (c) 2005-2012, ManyDesigns srl";


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
        xb.addAttribute("width", "" + width);
        xb.addAttribute("height", "" + height);
        xb.addAttribute("alt", alt);
        xb.addAttribute("usemap", "#" + mapId);
        xb.addAttribute("style", "border: none;");
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
