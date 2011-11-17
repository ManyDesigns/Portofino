<%
    Page currentPage = actionBean.getPageInstance().getPage();
%>
<div class="yui-gb">
    <table class="yui-u first">
        <c:forEach var="group" items="${actionBean.groups}">
            <%
                Group group = (Group) pageContext.getAttribute("group");
                String groupName = group.getName();
                String permissionLevel = actionBean.getPermissionLevelName(currentPage, groupName);
                String parentPermissionLevel = null;
                Page parentPage = currentPage.getParent();
                while(parentPermissionLevel == null && parentPage != null) {
                    parentPermissionLevel = actionBean.getEffectivePermissionLevel(parentPage, groupName);
                    parentPage = parentPage.getParent();
                }
            %>
            <tr>
                <td>
                    <c:out value="${group.name}"/>
                </td>
                <td>
                    <select name="permissions[${group.name}]">
                        <option value="__inherited">
                            Inherited - <%= parentPermissionLevel %>
                        </option>
                        <option value="<%= PortletAction.DENY %>"
                                <%
                                    if (PortletAction.DENY.equals(permissionLevel)) {
                                        out.print("selected='selected'");
                                    }
                                %>>
                            Deny
                        </option>
                        <option value="<%= Permissions.VIEW %>"
                                <%
                                    if (Permissions.VIEW.equals(permissionLevel)) {
                                        out.print("selected='selected'");
                                    }
                                %>>
                            View
                        </option>
                        <option value="<%= Permissions.EDIT %>"
                                <%
                                    if (Permissions.EDIT.equals(permissionLevel)) {
                                        out.print("selected='selected'");
                                    }
                                %>>
                            Edit
                        </option>
                    </select>
                </td>
            </tr>
        </c:forEach>
    </table>

    <div class="yui-u">
        <h3>Effective permissions</h3>
        <c:forEach var="group" items="${actionBean.groups}">
            <div>
                <c:out value="${group.name}"/>: <%
                    Group group = (Group) pageContext.getAttribute("group");
                    String groupName = group.getName();

                    String permissionLevel = actionBean.getEffectivePermissionLevel(currentPage, groupName);

                    out.print(permissionLevel);
                %>
            </div>
        </c:forEach>
    </div>
</div>

<input type="hidden" name="cancelReturnUrl" value="<c:out value="${actionBean.cancelReturnUrl}"/>"/>