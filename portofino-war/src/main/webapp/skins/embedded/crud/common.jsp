<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><stripes:layout-definition>
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.UseCaseAction"/>
    <stripes:form action="${actionBean.dispatch.absolutePath}" method="post"
                  enctype="${actionBean.multipartRequest ? 'multipart/form-data' : 'application/x-www-form-urlencoded'}">
        <div class="buttons-bar-top">
            <stripes:layout-component name="buttons"></stripes:layout-component>
        </div>
        <div id="inner-content">
            <stripes:layout-component name="body"></stripes:layout-component>
        </div>
        <div class="buttons-bar-bottom">
            <stripes:layout-component name="buttons"></stripes:layout-component>
        </div>
    </stripes:form>
</stripes:layout-definition>