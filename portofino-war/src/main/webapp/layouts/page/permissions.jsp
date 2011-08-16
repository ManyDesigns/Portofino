<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><stripes:layout-render name="/skins/${skin}/modal-page.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.PortletAction"/>
    <stripes:layout-component name="contentHeader">
        <stripes:submit name="updatePagePermissions" value="Save" class="contentButton"/>
        <stripes:submit name="cancel" value="Cancel" class="contentButton"/>
        <div class="breadcrumbs">
            <div class="inner">
                <mde:write name="breadcrumbs"/>
            </div>
        </div>
    </stripes:layout-component>
    <stripes:layout-component name="portletTitle">
        Page permissions for: <c:out value="${actionBean.pageInstance.page.title}"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletBody">
        <div class="yui-gb">
            <div class="yui-u first">
                <div class="groupBoxTitle">Allow groups</div>
                <ul class="groupBox">
                    <c:forEach var="group" items="${actionBean.allowGroups}">
                        <li id="group_<c:out value='${group.name}'/>" class="group ui-state-default"><c:out value="${group.name}"/></li>
                    </c:forEach>
                </ul>
                <input type="hidden" name="allowGroupNames"/>
            </div>
            <div class="yui-u">
                <div class="groupBoxTitle">Deny groups</div>
                <ul class="groupBox">
                    <c:forEach var="group" items="${actionBean.denyGroups}">
                        <li id="group_<c:out value='${group.name}'/>" class="group ui-state-default"><c:out value="${group.name}"/></li>
                    </c:forEach>
                </ul>
                <input type="hidden" name="denyGroupNames"/>
            </div>
            <div class="yui-u">
                <div class="groupBoxTitle">Available groups</div>
                <ul class="groupBox">
                    <c:forEach var="group" items="${actionBean.availableGroups}">
                        <li id="group_<c:out value='${group.name}'/>" class="group ui-state-default"><c:out value="${group.name}"/></li>
                    </c:forEach>
                </ul>
                <input type="hidden" name="availableGroupNames"/>
            </div>
        </div>
        <script>
            $(".groupBox").sortable({
                connectWith: ".groupBox",
                placeholder: "groupPlaceholder",
                cursor: "move", // cursor image
                revert: true, // moves the portlet to its new position with a smooth transition
                tolerance: "pointer" // mouse pointer overlaps the droppable
            }).disableSelection();
            $("input[name=updatePagePermissions]").click(function() {
                $('.groupBox').each( function(index, element) {
                    var wrapper = $(element);
                    var toAttay = wrapper.sortable('toArray');
                    var hidden = wrapper.siblings('input');
                    hidden.val(toAttay);
                });
                return true;
            });
        </script>
        <div class="horizontalSeparator"></div>
        Inherited permissions:
        <input type="hidden" name="cancelReturnUrl" value="<c:out value="${actionBean.cancelReturnUrl}"/>"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletFooter"/>
    <stripes:layout-component name="contentFooter">
        <stripes:submit name="updatePagePermissions" value="Save" class="contentButton"/>
        <stripes:submit name="cancel" value="Cancel" class="contentButton"/>
    </stripes:layout-component>
</stripes:layout-render>