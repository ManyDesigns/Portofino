<%@ page import="com.manydesigns.portofino.modules.DatabaseModule" %>
<%@ page import="com.manydesigns.portofino.persistence.Persistence" %>
<%@ page import="com.manydesigns.portofino.persistence.QueryUtils" %>
<%@ page import="org.hibernate.Session" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"%>
<%@taglib prefix="mde" uri="/manydesigns-elements"%>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.custom.CustomAction"/>
<stripes:layout-render name="/theme/templates/${actionBean.pageInstance.layout.template}/normal.jsp">
    <stripes:layout-component name="portletTitle">
        <c:out value="${actionBean.page.title}"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody"><%
        Persistence persistence = (Persistence) application.getAttribute(DatabaseModule.PERSISTENCE);
        Session hSession = persistence.getSession("redmine");
        List<?> objects = QueryUtils.runSql
                (hSession, "select count(*), \"project_id\", status.\"name\", \"projects\".\"name\" " +
                        "from \"issues\" join \"issue_statuses\" status on \"status_id\" = status.\"id\" " +
                        "join \"projects\" on \"project_id\" = \"projects\".\"id\" " +
                        "group by \"status_id\", \"project_id\" order by \"project_id\"");%>
        <table id="projectTree">
        <tr style="background-color: #ECEEF0; width: 100%;">
            <th style="width: 40%;">Project</th>
            <th style="width: 40%;">Status</th>
            <th style="width: 20%;">Issues</th>
        </tr><%
            String lastProject = "none";
            int lastFather = 0;
            int id = 1;
            boolean odd = true;
            String color;
            for(Object obj : objects) {
                Object[] obArr = (Object[]) obj;
                String count = obArr[0].toString();
                String statusName = (String) obArr[2];
                String projName = (String) obArr[3];
                if(!projName.equals(lastProject)){
                    out.print("<tr data-tt-id=\"" + id + "\"><td colspan=3>"+projName+"</td></tr>");
                    lastFather = id;
                    id++;
                    lastProject=projName;
                }

                if (odd) {
                    color = "#ECE9D8";
                } else {
                    color = "#FFFDF0";
                }
                odd = !odd;
                out.print("<tr data-tt-id=\"" + id + "\" data-tt-parent-id=\"" + lastFather + "\" style=\"background-color: "+color+"\">");
                out.print("<td ></td><td>"+statusName+"</td><td>"+count+"</td>");
                id++;
                out.print("</tr>");
            }
        %>
        </table>
        <script type="text/javascript" src="<%= request.getContextPath() %>/theme/jquery-treetable/jquery.treetable.js" >
        </script>
        <script type="text/javascript">
            $("#projectTree").treetable({"clickableNodeNames": true, "expandable":true, "treeColumn":0, "indent":20 });
        </script>
    </stripes:layout-component>
</stripes:layout-render>