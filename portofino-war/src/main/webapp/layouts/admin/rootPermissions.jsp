<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><stripes:layout-render name="/skins/default/admin-page.jsp">
    <jsp:useBean id="actionBean" scope="request" type="com.manydesigns.portofino.actions.admin.RootPermissionsAction"/>
    <stripes:layout-component name="pageTitle">
        Root permission
    </stripes:layout-component>
    <stripes:layout-component name="contentHeader">
        <stripes:submit name="updatePagePermissions" value="Update" class="contentButton"/>
        <stripes:submit name="returnToPages" value="Return to pages" class="contentButton"/>
    </stripes:layout-component>
    <stripes:layout-component name="portletTitle">
        Root permission
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
    </stripes:layout-component>
    <stripes:layout-component name="contentFooter">
        <stripes:submit name="updatePagePermissions" value="Update" class="contentButton"/>
        <stripes:submit name="returnToPages" value="Return to pages" class="contentButton"/>
        <script type="text/javascript">
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
    </stripes:layout-component>
</stripes:layout-render>