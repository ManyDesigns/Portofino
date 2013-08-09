<%@ page import="com.manydesigns.elements.ElementsThreadLocals" %>
<%@ page import="com.manydesigns.portofino.modules.DatabaseModule" %>
<%@ page import="com.manydesigns.portofino.persistence.Persistence" %>
<%@ page import="com.manydesigns.portofino.persistence.QueryUtils" %>
<%@ page import="ognl.Ognl" %>
<%@ page import="org.hibernate.Session" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"%>
<%@taglib prefix="mde" uri="/manydesigns-elements"%>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.custom.CustomAction"/>
<stripes:layout-render name="/skins/${skin}${actionBean.pageTemplate}/normal.jsp">
    <stripes:layout-component name="portletTitle">
        <c:out value="${actionBean.page.title}"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <%
            Persistence persistence = (Persistence) application.getAttribute(DatabaseModule.PERSISTENCE);
            Session hSession = persistence.getSession("redmine");
            List<?> objects = QueryUtils.getObjects(hSession,
                    "SELECT t.name, sum(1 - st.is_closed) as sum, count(*) as count " +
                            "FROM issues i, trackers t, issue_statuses st " +
                            "WHERE t.id = i.tracker_id " +
                            "  AND i.project_id = %{#project.id} " +
                            "  AND i.status_id = st.id " +
                            "GROUP BY t.name", null, null);
            for(Object obj : objects) {
                Object[] obArr = (Object[]) obj;
                out.print(obArr[0] + ": " + obArr[1] + " open / " + obArr[2] + "<br />");
            }

            Object projectId = Ognl.getValue("#project.id", ElementsThreadLocals.getOgnlContext(), (Object) null);
        %>
        <a href="<%= request.getContextPath() %>/projects/<%= projectId %>/issues">Show all issues</a>
    </stripes:layout-component>
</stripes:layout-render>
