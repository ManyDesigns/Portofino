<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"%>
<%@ taglib prefix="mde" uri="/manydesigns-elements"%>
<jsp:useBean id="portofinoConfiguration" scope="application"
             type="org.apache.commons.configuration.Configuration"/>
<jsp:useBean id="stopWatch" scope="request"
             type="org.apache.commons.lang.time.StopWatch"/>
<div id="responseTime">
    <fmt:message key="skins.default.footer.response_time"/>: <c:out value="${stopWatch.time}"/> ms.
</div>
Powered by <a href="http://www.manydesigns.com/">Portofino</a>
<c:out value="${mde:getString(portofinoConfiguration, 'portofino.version')}"/>