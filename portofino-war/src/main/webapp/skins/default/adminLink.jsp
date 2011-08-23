<%@ page contentType="text/html;charset=ISO-8859-1" language="java" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"%>
<stripes:layout-definition>
    <li class="navigationItem <c:if test="${requestScope.__stripes_resolved_action eq link}">selected</c:if>"><stripes:link href="${link}"><c:out value="${text}"/></stripes:link></li>
</stripes:layout-definition>