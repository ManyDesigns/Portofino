<%@ page import="com.manydesigns.portofino.modules.ModuleRegistry"
%><%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino"
%><stripes:layout-definition><%--
--%><!DOCTYPE html>
    <html lang="<%= request.getLocale() %>">
    <jsp:include page="/theme/head.jsp">
        <jsp:param name="pageTitle" value="${pageTitle}" />
    </jsp:include>
    <body class="dialog">
        <div class="dialog-container">
            <div class="contentHeader">
                <stripes:layout-component name="contentHeader">
                    <mde:sessionMessages />
                </stripes:layout-component>
            </div>
            <div class="pageHeader">
                <stripes:layout-component name="pageHeader">
                    <h2 class="pageTitle">
                        <stripes:layout-component name="pageTitle">
                            <c:out value="${actionBean.pageInstance.description}"/>
                        </stripes:layout-component>
                    </h2>
                </stripes:layout-component>
            </div>
            <div class="pageBody spacingTop">
                <stripes:layout-component name="pageBody" />
            </div>
            <div class="pageFooter">
                <stripes:layout-component name="pageFooter" />
            </div>
            <div class="contentFooter">
                <stripes:layout-component name="contentFooter">
                    <hr />
                    <jsp:useBean id="portofinoConfiguration" scope="application"
                                 type="org.apache.commons.configuration.Configuration"/>
                    Powered by <a href="http://portofino.manydesigns.com/">Portofino</a>
                    <c:out value="<%= ModuleRegistry.getPortofinoVersion() %>"/>
                </stripes:layout-component>
            </div>
        </div>
    </body>
</html>
</stripes:layout-definition>