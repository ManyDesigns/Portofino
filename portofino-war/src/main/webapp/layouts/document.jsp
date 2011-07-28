<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><stripes:layout-render name="/skins/default/common-with-navigation.jsp">
    <stripes:layout-component name="content">
        <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.DocumentAction"/>
        <c:out value="${actionBean.content}" escapeXml="false"/>
    </stripes:layout-component>
</stripes:layout-render>