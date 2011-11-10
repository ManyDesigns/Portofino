<%@ page import="com.manydesigns.portofino.actions.admin.AdminAction" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.apache.commons.lang.ObjectUtils" %>
<%@ page contentType="text/html;charset=ISO-8859-1" language="java" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"%>
<stripes:layout-definition>
    <li class="navigationItem<%
            {
                AdminAction adminAction = (AdminAction) request.getAttribute("actionBean");
                if(adminAction != null) {
                    String link = ObjectUtils.toString(pageContext.getAttribute("link"), "/");
                    if(!"/".equals(link) && adminAction.getActionPath().startsWith(link)) {
                        out.print(" selected");
                    }
                }
            }
        %>">
        <stripes:link href="${link}"><c:out value="${text}"/></stripes:link>
    </li>
</stripes:layout-definition>