<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.pageactions.custom.CustomAction"
/><stripes:layout-render name="/theme/templates/${actionBean.pageInstance.layout.template}/normal.jsp">
    <stripes:layout-component name="pageTitle">
        <c:out value="${actionBean.page.title}"/>
    </stripes:layout-component>
    <stripes:layout-component name="pageBody">
        <dl>
            <dt><i class="icon-tag"></i> Ticket description:</dt>
            <dd><c:out value="${actionBean.ticket.description}"/></dd>
        </dl>
        <dl>
            <c:forEach var="activityItem" items="${actionBean.activityItems}">
                <dt><i class="icon-user"></i> <a href=""><c:out value="${activityItem.fk_activity_user.first_name} ${activityItem.fk_activity_user.last_name}"/></a> <small class="muted"><c:out value="${activityItem.fk_activity_type.type}"/> on <fmt:formatDate value="${activityItem.date}" pattern="yyyy-MM-dd HH:mm:ss z"/></small></dt>
                <dd><c:out value="${activityItem.message}"/></dd>
            </c:forEach>
        </dl>
        <stripes:form action="${actionBean.context.actualServletPath}" method="post">
            <label>Add a comment:
            <textarea name="comment" class="input-block-level"></textarea>
            </label>
            <button name="addComment" class="btn">Post comment</button>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>