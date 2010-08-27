/*
 * Copyright (C) 2005-2009 ManyDesigns srl.  All rights reserved.
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

import com.manydesigns.elements.Element;
import com.manydesigns.elements.Mode;
import com.manydesigns.elements.util.Util;
import com.manydesigns.elements.xml.XhtmlBuffer;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.servlet.http.HttpServletRequest;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class JFreeChartElement implements Element{
    public static final String copyright =
            "Copyright (c) 2005-2009, ManyDesigns srl";
    private String id;
    private Integer height;
    private Integer width;
    private String alt;
    private Integer chartType;
    private String title;
    private Dataset dataset;
    private ChartRenderingInfo info = null;
    private String servletName;
    private String axisName;
    private String valueName;

    public void readFromRequest(HttpServletRequest httpServletRequest) {
        //Do nothing
    }

    public boolean validate() {
        return true;
    }

    public void readFromObject(Object o) {
        //Do nothing
    }

    public void writeToObject(Object o) {
        //Do nothing
    }

    public Mode getMode() {
        return Mode.VIEW;
    }

    public void setMode(Mode mode) {
        //Do nothing
    }

    public void toXhtml(XhtmlBuffer xb) {

        String filename = ChartGenerator.generateChart(getChartType(), getTitle(),
                getAxisName(), getValueName(), getDataset(), info, getWidth(), getHeight());
        xb.openElement("div");
        xb.addAttribute("id", id);
        xb.openElement("img");
        xb.addAttribute("height", getHeight().toString());
        xb.addAttribute("width", getWidth().toString());
        //xb.addAttribute("usemap", "#prptmap"+id);
        alt = "";
        xb.addAttribute("alt", alt);
        xb.addAttribute("src",
                Util.getAbsoluteUrl(getServletName()+"?f="+filename));
        xb.closeElement("img");
        xb.closeElement("div");
    }


    public Integer getHeight() {
        if (height!=null) {
            return height;
        } else {
            return 314;
        }
    }

    public Integer getWidth() {
        if (width!=null) {
            return width;
        } else {
            return 292;
        }
    }

    public String getAlt() {
        if (title!=null) {
            return title;
        } else {
            return "alt not provided";
        }
    }

    public int getChartType() {
        if (chartType!=null) {
            return chartType;
        } else {
            return ChartUtil.PIE_CHART;
        }
    }

    public String getTitle() {
        if (title!=null) {
            return title;
        } else {
            return "";
        }
    }

    public Dataset getDataset() {
        if (dataset != null) {
            return dataset;
        } else {
            DefaultPieDataset defaultPieDataset = new DefaultPieDataset();
            defaultPieDataset.setValue("One", new Double(43.2));
            defaultPieDataset.setValue("Two", new Double(10.0));
            defaultPieDataset.setValue("Three", new Double(27.5));
            defaultPieDataset.setValue("Four", new Double(17.5));
            defaultPieDataset.setValue("Five", new Double(11.0));
            defaultPieDataset.setValue("Six", new Double(19.4));
            return defaultPieDataset;
        }
    }

    public ChartRenderingInfo getInfo() {
        return info;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public void setAlt(String alt) {
        this.alt = alt;
    }

    public void setChartType(Integer chartType) {
        this.chartType = chartType;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    public void setInfo(ChartRenderingInfo info) {
        this.info = info;
    }

    public String getServletName() {
        if (servletName!=null) {
            return servletName;
        } else {
            return "/DisplayChart";
        }

    }

    public void setServletName(String servletName) {
        this.servletName = servletName;
    }

    public String getAxisName() {
        if(axisName != null) {
            return axisName;
        } else {
            return "";
        }
    }

    public void setAxisName(String axisName) {
        this.axisName = axisName;
    }

    public String getValueName() {
        if (valueName!=null) {
            return valueName;
        } else {
            return "";
        }
    }

    public void setValueName(String valueName) {
        this.valueName = valueName;
    }
}
