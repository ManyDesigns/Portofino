<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><stripes:layout-definition>
    <stripes:layout-render name="/skins/default/common.jsp">
        <stripes:layout-component name="content">
            <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.UseCaseAction"/>
            <stripes:form action="${actionBean.dispatch.absolutePath}" method="post"
                          enctype="${actionBean.multipartRequest ? 'multipart/form-data' : 'application/x-www-form-urlencoded'}">
                <div class="buttons-bar-top">
                    <stripes:layout-component name="buttons" />
                </div>
                <div id="inner-content">
                    <stripes:layout-component name="innerContent" />
                </div>
                <div class="buttons-bar-bottom">
                    <stripes:layout-component name="buttons" />
                </div>
            </stripes:form>
        </stripes:layout-component>
    </stripes:layout-render>
</stripes:layout-definition>