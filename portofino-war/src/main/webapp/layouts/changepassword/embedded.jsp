<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"%>
<%@taglib prefix="mde" uri="/manydesigns-elements"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="portofino" %>
<stripes:layout-render name="/skins/${skin}/portlet.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.changepassword.ChangePasswordAction"/>
    <stripes:layout-component name="portletHeader"></stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <div class="embedded-content">
            <a class="change-password-link" href="${actionBean.dispatch.absoluteOriginalPath}">
                <fmt:message key="changepasswordaction.change.link" />
            </a>
            <div class="portletHeaderButtons">
                <stripes:layout-component name="portletHeaderButtons">
                    <portofino:buttons list="portletHeaderButtons" />
                </stripes:layout-component>
            </div>
        </div>
    </stripes:layout-component>
</stripes:layout-render>