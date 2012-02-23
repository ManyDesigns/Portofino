<%@ page import="com.manydesigns.elements.forms.Form" %>
<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="stripes"
           uri="http://stripes.sourceforge.net/stripes-dynattr.tld" %>
<jsp:useBean id="actionBean" scope="request"
             type="com.manydesigns.portofino.pageactions.jsgantt.JsGanttAction"/>
<stripes:layout-render name="/skins/${skin}/portlet.jsp">
    <stripes:layout-component name="portletTitle">
        <c:out value="${actionBean.page.title}"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <style type="text/css">
            @import "<stripes:url value="/layouts/jsgantt/jsgantt.css"/>";
            table#theTable >tbody >tr >td {
                border: none;
                padding: 0;
            }
            div#GanttChartDIV table {
                margin-bottom: 0;
                border-collapse: separate;
            }
            div#GanttChartDIV td, div#GanttChartDIV th {
                padding: 0;
                border: 0;
                border-style: none;
            }
        </style>
        <script language="javascript"
                src="<stripes:url value="/layouts/jsgantt/jsgantt.js"/>"></script>

        <div>
            <div style="position:relative" class="gantt" id="GanttChartDIV"></div>
        </div>
        <script>


            var g = new JSGantt.GanttChart('g', document.getElementById('GanttChartDIV'), 'day');

            <%
                String captionType = actionBean.getConfiguration().getCaptionType();
                String dateDisplayFormat = actionBean.getConfiguration().getDateDisplayFormat();
                int showComplete = actionBean.getConfiguration().isShowComplete()?1:0;
                int showDuration = actionBean.getConfiguration().isShowDuration()?1:0;
                int showStartDate = actionBean.getConfiguration().isShowStartDate()?1:0;
                int showEndDate = actionBean.getConfiguration().isShowEndDate()?1:0;
                int showResources = actionBean.getConfiguration().isShowResource()?1:0;



            %>

            g.setShowRes(<%= showResources%>);
            g.setShowDur(<%= showDuration%>);
            g.setShowComp(<%= showComplete%>);
            g.setShowStartDate(<%= showStartDate%>);
            g.setShowEndDate(<%= showEndDate%>);
            g.setCaptionType('<%= captionType%>');
            g.setDateInputFormat('dd/mm/yyyy')
            g.setDateDisplayFormat('<%= dateDisplayFormat%>')


            //var gr = new Graphics();

            if (g) {
                JSGantt.parseXML('<c:out value="${actionBean.dispatch.absoluteOriginalPath}?xmlData="/>',g)
                g.Draw();
                g.DrawDependencies();
            }

            else {
                alert("not defined");
            }

        </script>

    </stripes:layout-component>
    <stripes:layout-component name="portletFooter">
    </stripes:layout-component>
</stripes:layout-render>