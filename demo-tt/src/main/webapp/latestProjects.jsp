<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"%>
<%@ page import="com.manydesigns.portofino.modules.DatabaseModule" %>
<%@ page import="com.manydesigns.portofino.persistence.Persistence" %>
<%@ page import="com.manydesigns.portofino.persistence.QueryUtils" %>
<%@ page import="org.apache.commons.lang.time.FastDateFormat" %>
<%@ page import="org.hibernate.Session" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.util.Map" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"%>
<%@taglib prefix="mde" uri="/manydesigns-elements"%>

<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.custom.CustomAction"/>
<stripes:layout-render name="/m/theme${actionBean.pageTemplate}/normal.jsp">
    <stripes:layout-component name="portletTitle">
        <c:out value="${actionBean.page.title}"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <ul>
            <%
                Persistence persistence = (Persistence) application.getAttribute(DatabaseModule.PERSISTENCE);
                String queryString = "FROM projects order by updated_on desc, created_on desc";
                Session hSession = persistence.getSession("redmine");
                List objects = QueryUtils.runHqlQuery(hSession, queryString, new Object[0], 0, 3);
                for(Object obj : objects) {
                    Map map = (Map) obj;
                    %>
                    <li>
                        <a href="<%= request.getContextPath() %>/projects/<%= map.get("id") %>"><%= map.get("name") %></a>
                        <%
                            FastDateFormat fastDateFormat =
                                    FastDateFormat.getDateTimeInstance(
                                            FastDateFormat.MEDIUM, FastDateFormat.SHORT, Locale.ITALY);
                            Date updatedOn = (Date) map.get("updated_on");
                            if(updatedOn != null) {
                                out.print("(" + fastDateFormat.format(updatedOn) + ")");
                            } else {
                                Object createdOn = map.get("created_on");
                                if(createdOn != null) {
                                    out.print("(" + fastDateFormat.format(createdOn) + ")");
                                }
                            }
                        %>
                        <br />
                        <%= map.get("description") %>
                    </li>
                <% }
            %>
        </ul>
    </stripes:layout-component>
</stripes:layout-render>
