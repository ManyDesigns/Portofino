<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><stripes:layout-render name="/skins/default/admin-page.jsp">
    <jsp:useBean id="actionBean" scope="request"
                 type="com.manydesigns.portofino.actions.user.admin.UserAdminAction"/>
    <stripes:layout-component name="contentHeader">
        <stripes:submit name="update" class="contentButton"><fmt:message key="commons.update"/></stripes:submit>
        <stripes:submit name="cancel" class="contentButton"><fmt:message key="commons.cancel"/></stripes:submit>
    </stripes:layout-component>
    <stripes:layout-component name="pageTitle">
        <c:out value="${actionBean.crud.editTitle}"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletTitle">
        <c:out value="${actionBean.crud.editTitle}"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <c:if test="${actionBean.requiredFieldsPresent}">
            <fmt:message key="commons.fields_required"/>.
        </c:if>
        <div class="yui-gc">
            <div class="yui-u first">
                <mde:write name="actionBean" property="form"/>
            </div>
            <div class="yui-u">
                <h2>Groups</h2>
                <div>
                    <div class="groupBoxTitle"><fmt:message key="layouts.admin.rootPermissions.allow_groups"/></div>
                    <ul class="groupBox">
                        <c:forEach var="group" items="${actionBean.userGroups}">
                            <li id="group_<c:out value='${group.name}'/>" class="group ui-state-default"><c:out value="${group.name}"/></li>
                        </c:forEach>
                    </ul>
                    <input type="hidden" name="groupNames"/>
                </div>
                <div>
                    <div class="groupBoxTitle"><fmt:message key="layouts.admin.rootPermissions.available_groups"/></div>
                    <ul class="groupBox">
                        <c:forEach var="group" items="${actionBean.availableUserGroups}">
                            <li id="group_<c:out value='${group.name}'/>" class="group ui-state-default"><c:out value="${group.name}"/></li>
                        </c:forEach>
                    </ul>
                    <input type="hidden" name="availableGroupNames"/>
                </div>
            </div>
        </div>
        <input type="hidden" name="pk" value="<c:out value="${actionBean.pk}"/>"/>
        <c:if test="${not empty actionBean.searchString}">
            <input type="hidden" name="searchString" value="<c:out value="${actionBean.searchString}"/>"/>
        </c:if>
        <input type="hidden" name="cancelReturnUrl" value="<c:out value="${actionBean.cancelReturnUrl}"/>"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletFooter"/>
    <stripes:layout-component name="contentFooter">
        <stripes:submit name="update" class="contentButton"><fmt:message key="commons.update"/></stripes:submit>
        <stripes:submit name="cancel" class="contentButton"><fmt:message key="commons.cancel"/></stripes:submit>
        <script type="text/javascript">
            $(".groupBox").sortable({
                connectWith: ".groupBox",
                placeholder: "groupPlaceholder",
                cursor: "move", // cursor image
                revert: true, // moves the portlet to its new position with a smooth transition
                tolerance: "pointer" // mouse pointer overlaps the droppable
            }).disableSelection();
            $("input[name=update]").click(function() {
                $('.groupBox').each( function(index, element) {
                    var wrapper = $(element);
                    var toArray = wrapper.sortable('toArray');
                    var hidden = wrapper.siblings('input');
                    hidden.val(toArray);
                });
                return true;
            });
        </script>
    </stripes:layout-component>
</stripes:layout-render>
