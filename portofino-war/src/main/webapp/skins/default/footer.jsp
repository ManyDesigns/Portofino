<%@ page contentType="text/html;charset=ISO-8859-1" language="java" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"%>
<%@ taglib prefix="mde" uri="/manydesigns-elements"%>
<jsp:useBean id="portofinoConfiguration" scope="application"
             type="org.apache.commons.configuration.Configuration"/>
<jsp:useBean id="stopWatch" scope="request"
             type="org.apache.commons.lang.time.StopWatch"/>
<div id="responseTime">
    Page response time: <c:out value="${stopWatch.time}"/> ms.
</div>
Powered by <a href="http://www.manydesigns.com/">ManyDesigns Portofino</a>
<c:out value="${mde:getString(portofinoConfiguration, 'portofino.version')}"/>