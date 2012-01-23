<%@ page import="com.manydesigns.portofino.application.DefaultApplication" %>
<%@ page import="org.apache.commons.lang.time.FastDateFormat" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.util.Map" %>
<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"%>
<%@taglib prefix="mde" uri="/manydesigns-elements"%>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.jsp.JspAction"/>
<stripes:layout-render name="/skins/${skin}/portlet.jsp">
    <stripes:layout-component name="portletTitle">
        <c:out value="${actionBean.jspConfiguration.title}"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <ul>
            <%
                //TODO esporre maxResults nell'interfaccia e non usare direttamente runHqlQuery
                DefaultApplication appl = (DefaultApplication) request.getAttribute("application");
                List objects = appl.runHqlQuery("FROM projects order by updated_on desc, created_on desc", new Object[0], null, 3);
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
    <stripes:layout-component name="portletFooter">
    </stripes:layout-component>
</stripes:layout-render>
