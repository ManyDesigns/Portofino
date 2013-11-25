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
        <div>
            <strong><i class="icon-tag"></i> Ticket description:</strong>
        </div>
        <div><c:out value="${actionBean.ticket.description}"/></div>
        <c:forEach var="activityItem" items="${actionBean.activityItems}">
            <div class="media">
                <stripes:link class="pull-left" href="/users/${activityItem.user}">
                    <img class="media-object" alt="user" src="<stripes:url value="/images/user-placeholder-40x40.png"/>" />
                </stripes:link>
                <div class="media-body">
                    <div>
                        <strong>
                        <stripes:link href="/users/${activityItem.user}"><c:out value="${activityItem.fk_activity_user.first_name} ${activityItem.fk_activity_user.last_name}"/></stripes:link>
                        </strong>
                    </div>
                    <div>
                        <strong>
                        <small class="muted">
                            <fmt:message key="${activityItem.fk_activity_type.type}">
                                <fmt:param value="${activityItem.date}"/>
                            </fmt:message>
                        </small>
                        </strong>
                    </div>
                    <div><c:out value="${activityItem.message}"/></div>
                </div>
            </div>
        </c:forEach>
        <hr/>
        <stripes:form action="${actionBean.context.actionPath}" method="post">
            <label>Add a comment:
            <textarea name="comment" class="input-block-level"></textarea>
            </label>
            <button name="addComment" class="btn">Post comment</button>
        </stripes:form>
    </stripes:layout-component>
</stripes:layout-render>