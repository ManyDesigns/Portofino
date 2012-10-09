<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@ taglib tagdir="/WEB-INF/tags" prefix="portofino" %>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.wizard.AbstractWizardPageAction "/>
<div class="contentHeader wizard">
    <c:forEach items="${actionBean.steps}" var="step" varStatus="status">
        <c:set var="baseClass"
               value="wizard-step ${status.first ? 'first' : ''} ${status.last ? 'last' : ''}"
        />
        <c:if test="${status.index eq actionBean.currentStepIndex}">
            <span class="${baseClass} current">
                <c:out value="${step.title}" />
            </span>
            <c:if test="${status.last}">
                <span class="wizard-step immediately-after-current" style="background-color: transparent;">&nbsp;</span>
            </c:if>
        </c:if>
        <c:if test="${status.index < actionBean.currentStepIndex}">
            <span class="${baseClass} before">
                <c:out value="${step.number}" />
            </span>
        </c:if>
        <c:if test="${status.index > actionBean.currentStepIndex}">
            <span class="${baseClass} after ${status.index eq actionBean.currentStepIndex + 1 ? 'immediately-after-current' : ''}">
                <c:out value="${step.number}" />
            </span>
            <c:if test="${status.last}">
                <span class="wizard-step" style="background-color: transparent;">&nbsp;</span>
            </c:if>
        </c:if>
    </c:forEach>
</div>