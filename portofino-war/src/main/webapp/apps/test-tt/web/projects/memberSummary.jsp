<%@ page import="com.manydesigns.portofino.application.Application" %>
<%@ page import="org.apache.commons.collections.MultiHashMap" %>
<%@ page import="org.apache.commons.collections.MultiMap" %>
<%@ page import="java.util.Collection" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.hibernate.Session" %>
<%@ page import="com.manydesigns.portofino.application.QueryUtils" %>
<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"%>
<%@taglib prefix="mde" uri="/manydesigns-elements"%>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.custom.CustomAction"/>
<stripes:layout-render name="/skins/${skin}/portlet.jsp">
    <stripes:layout-component name="portletTitle">
        <c:out value="${actionBean.jspConfiguration.title}"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <ul>
            <%
                Application appl = (Application) request.getAttribute("application");
                Session hSession = appl.getSession("redmine");
                List<?> objects = QueryUtils.getObjects(hSession,
                        "SELECT r.name, u.login " +
                                "FROM members m, users u, " +
                                "     roles r, member_roles mr " +
                                "WHERE m.project_id = %{#project.id}" +
                                "  AND m.user_id = u.id " +
                                "  AND mr.member_id = m.id " +
                                "  AND mr.role_id = r.id ", null, null);
                MultiMap mm = new MultiHashMap();
                for(Object obj : objects) {
                    Object[] obArr = (Object[]) obj;
                    mm.put(obArr[0], obArr);
                }
                for(Object entry : mm.entrySet()) {
                    Map.Entry ee = (Map.Entry) entry;
                    out.print("<li>" + ee.getKey());
                    String sep = ": ";
                    for(Object[] user : (Collection<Object[]>) ee.getValue()) {
                        out.print(sep + user[1]);
                        sep = ", ";
                    }
                    out.print("</li>");
                }
            %>
        </ul>
    </stripes:layout-component>
    <stripes:layout-component name="portletFooter">
    </stripes:layout-component>
</stripes:layout-render>
