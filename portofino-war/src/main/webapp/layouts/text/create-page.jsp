<%@ page import="com.manydesigns.portofino.logic.SecurityLogic" %>
<%@ page import="com.manydesigns.portofino.security.AccessLevel" %>
<%@ page import="org.apache.shiro.SecurityUtils" %>
<%@ page import="org.apache.shiro.subject.Subject" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.text.TextAction"
/><%
    response.setStatus(404);
    List<String> parameters = actionBean.getPageInstance().getParameters();
    String fragment = parameters.get(0);
    String path = actionBean.getDispatch().getOriginalPath();
    path = path.substring(0, path.length() - fragment.length());
%><stripes:layout-render name="/skins/${skin}${actionBean.pageTemplate}/normal.jsp">
    <stripes:layout-component name="contentHeader">
        <jsp:include page="/skins/${skin}/breadcrumbs.jsp" />
    </stripes:layout-component>
    <stripes:layout-component name="contentBody">
        <stripes:form action="/actions/admin/page" method="post">
            <input type="hidden" name="originalPath" value="<%= path %>" />
            <input type="hidden" name="fragment" value="<%= fragment%>" />
            The page <%= actionBean.getDispatch().getOriginalPath() %> does not exist.
            <button type="submit" name="newPage" class="portletButton">
                <span class="ui-button-text">Create it.</span>
            </button>
        </stripes:form>
        <br />
    </stripes:layout-component>
    <stripes:layout-component name="contentFooter">
    </stripes:layout-component>
</stripes:layout-render>