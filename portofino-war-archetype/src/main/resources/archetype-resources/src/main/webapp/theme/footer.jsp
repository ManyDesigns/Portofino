<%@ page import="com.manydesigns.portofino.modules.ModuleRegistry" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"%>
<jsp:useBean id="portofinoConfiguration" scope="application"
             type="org.apache.commons.configuration.Configuration"/>
<jsp:useBean id="stopWatch" scope="request"
             type="org.apache.commons.lang.time.StopWatch"/>
<footer>
    <div class="container">
        <div class="pull-right">
            <fmt:message key="page.response.time"/>: <c:out value="${stopWatch.time}"/> ms.
        </div>
        Powered by <a href="http://portofino.manydesigns.com/">Portofino</a>
        <c:out value="<%= ModuleRegistry.getPortofinoVersion() %>"/>
    </div>
</footer>