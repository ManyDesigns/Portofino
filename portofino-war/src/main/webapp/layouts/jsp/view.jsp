<%@ page import="org.slf4j.LoggerFactory" %>
<%@ page import="com.manydesigns.portofino.actions.JspAction" %>
<%@ page import="com.manydesigns.portofino.actions.PortletAction" %>
<%@ page contentType="text/html;charset=ISO-8859-1" language="java" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"%>
<%@taglib prefix="mde" uri="/manydesigns-elements"%>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.JspAction"/>
<% try { %>
    <jsp:include page="/apps/${actionBean.application.appId}/web/${actionBean.targetJsp}" />
<% } catch (Exception e) {
    actionBean.logger.error("Custom JSP threw an exception", e);
    request.setAttribute(PortletAction.PORTOFINO_PORTLET_EXCEPTION, e);
%>
    <jsp:include page="../portlet-error.jsp" />
<% } %>