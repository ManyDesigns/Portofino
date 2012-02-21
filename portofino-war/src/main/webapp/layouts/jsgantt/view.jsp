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
            div#GanttChartDIV td, div#GanttChartDIV td {
                padding: 0;
            }
        </style>
        <script language="javascript"
                src="<stripes:url value="/layouts/jsgantt/jsgantt.js"/>"></script>

        <div>
            <div style="position:relative" class="gantt" id="GanttChartDIV"></div>
        </div>
        <script>


            var g = new JSGantt.GanttChart('g', document.getElementById('GanttChartDIV'), 'day');

            g.setShowRes(1); // Show/Hide Responsible (0/1)
            g.setShowDur(1); // Show/Hide Duration (0/1)
            g.setShowComp(1); // Show/Hide % Complete(0/1)
            g.setCaptionType('Resource');  // Set to Show Caption (None,Caption,Resource,Duration,Complete)
            g.setDateInputFormat('dd/mm/yyyy')  // Set format of input dates ('mm/dd/yyyy', 'dd/mm/yyyy', 'yyyy-mm-dd')
            g.setDateDisplayFormat('dd/mm/yyyy') // Set format to display dates ('mm/dd/yyyy', 'dd/mm/yyyy', 'yyyy-mm-dd')


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