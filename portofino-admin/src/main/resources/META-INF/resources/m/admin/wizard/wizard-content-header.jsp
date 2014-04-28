<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino" %>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.admin.appwizard.ApplicationWizard"/>
<div class="wizard-header">
    <ul>
    <c:forEach items="${actionBean.steps}" var="step" varStatus="status">
        <li style="z-index: ${20 - status.index}"
            class="${status.first ? 'first' : ''}
                   ${status.index < actionBean.currentStepIndex ? 'before' : ''}
                   ${status.index eq actionBean.currentStepIndex ? 'active' : ''}">
            <span class="badge ${status.index < actionBean.currentStepIndex ? 'alert-success' : 'alert-info'}"><c:out value="${step.number}" /></span>
            <c:if test="${status.index eq actionBean.currentStepIndex}">
                <c:out value="${step.title}" />
            </c:if>
            <c:if test="${not (status.index eq actionBean.currentStepIndex)}">
                <fmt:message key="step._">
                    <fmt:param value="${step.number}" />
                </fmt:message>
            </c:if>
            <span class="chevron"></span>
        </li>
    </c:forEach>
    </ul>
</div>
<div style="border-bottom: solid 1px #E5E5E5; margin-top: 10px; margin-bottom: 10px;"></div>