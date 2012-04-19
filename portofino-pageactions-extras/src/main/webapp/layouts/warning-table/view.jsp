<%@ page import="com.manydesigns.portofino.pageactions.treeview.configuration.TreeViewConfiguration" %>
<%@ page import="com.manydesigns.portofino.pageactions.treeview.model.Node" %>
<%@ page import="java.io.IOException" %>
<%@ page
        import="com.manydesigns.portofino.pageactions.warningtable.configuration.WarningTableConfiguration" %>
<%@ page
        import="com.manydesigns.portofino.pageactions.warningtable.model.MessagesModel" %>
<%@ page
        import="com.manydesigns.portofino.pageactions.warningtable.model.Message" %>
<%@ page
        import="com.manydesigns.portofino.pageactions.warningtable.model.Color" %>
<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="stripes"
           uri="http://stripes.sourceforge.net/stripes-dynattr.tld" %>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.warningtable.WarningTableAction"/>
<stripes:layout-render name="/skins/${skin}/portlet.jsp">
    <stripes:layout-component name="portletTitle">
        <c:out value="${actionBean.page.title}"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <style type="text/css">
            @import "<stripes:url value="/layouts/warning-table/style.css"/>";
        </style>
        <%
            final WarningTableConfiguration configuration = actionBean.getConfiguration();
        %>

        <table class="warningTable">
            <%
                actionBean.loadMessagesModel();
                MessagesModel model = actionBean.getMessagesModel();
            %>
            <thead>
                <tr>
                   <th  width="5%">&nbsp;</th>
                   <th  width="40%"><%= configuration.getHeader1()%></th>
                   <th width="55%"><%= configuration.getHeader2()%></th>
                </tr>
            </thead>
            <tbody>
            <%
                for(Message obj : model.getMessages()) {
                    out.print("<tr>");
                    if(obj.getLevel()== Color.GREEN) {
                        out.println("<td width=\"5%\"><img width=\"16px\" src=\""+request.getContextPath()+"/layouts/warning-table/img/green.png\" alt=\"green\"></td>");
                    } else if (obj.getLevel()== Color.YELLOW) {
                        out.println("<td  width=\"5%\"><img width=\"16px\" src=\""+request.getContextPath()+"/layouts/warning-table/img/yellow.png\" alt=\"yellow\"></td>");

                    } else {
                        out.println("<td  width=\"5%\"><img width=\"16px\" src=\""+request.getContextPath()+"/layouts/warning-table/img/red.png\" alt=\"red\"></td>");
                    }
                    out.println("<td  width=\"40%\">"+obj.getTitle()+"</td>");
                    out.println("<td  width=\"55%\">"+obj.getMessage()+"</td>");
                    out.print("</tr>");
                }
            %>
            </tbody>
        </table>
    </stripes:layout-component>
    <stripes:layout-component name="portletFooter">
    </stripes:layout-component>
</stripes:layout-render>
