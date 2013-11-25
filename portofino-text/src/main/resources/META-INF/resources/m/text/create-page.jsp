<%@ page import="java.util.List"
%><%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.text.TextAction"
/><%
    response.setStatus(404);
    List<String> parameters = actionBean.getPageInstance().getParameters();
    String fragment = parameters.get(0);
    String path = actionBean.getContext().getActionPath();
    path = path.substring(0, path.length() - fragment.length());
%><stripes:layout-render name="/theme/templates/${actionBean.pageInstance.layout.template}/modal.jsp">
    <stripes:layout-component name="contentHeader" />
    <stripes:layout-component name="pageHeader" />
    <stripes:layout-component name="pageBody">
        <stripes:form action="/actions/admin/page" method="post">
            <input type="hidden" name="originalPath" value="<%= path %>" />
            <input type="hidden" name="fragment" value="<%= fragment%>" />
            The page <%= actionBean.getContext().getActionPath() %> does not exist.
            <button type="submit" name="newPage" class="btn">
                Create it.
            </button>
        </stripes:form>
        <br />
    </stripes:layout-component>
</stripes:layout-render>