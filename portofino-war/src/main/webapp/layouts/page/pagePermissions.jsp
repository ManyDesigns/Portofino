<%
    Page currentPage = actionBean.getPageInstance().getPage();
%>
<table>
    <tr>
        <th>Group</th>
        <th>Page permissions</th>
        <th>Effective permissions</th>
    </tr>
    <c:forEach var="group" items="${actionBean.groups}">
        <%
            Group group = (Group) pageContext.getAttribute("group");
            String groupId = group.getGroupId();
            String permissionLevel = actionBean.getPermissionLevelName(currentPage, groupId);
            String effectivePermissionLevel = actionBean.getEffectivePermissionLevel(currentPage, groupId);
            if(effectivePermissionLevel == null) {
                effectivePermissionLevel = "None";
            }
            String parentPermissionLevel = null;
            Page parentPage = currentPage.getParent();
            while(parentPermissionLevel == null && parentPage != null) {
                parentPermissionLevel = actionBean.getEffectivePermissionLevel(parentPage, groupId);
                parentPage = parentPage.getParent();
            }
            if(parentPermissionLevel == null) {
                parentPermissionLevel = "None";
            }
        %>
        <tr>
            <td>
                <c:out value="${group.name}"/>
            </td>
            <td>
                <select name="permissions[${group.groupId}]">
                    <option value="__inherited">
                        Inherited - <%= parentPermissionLevel %>
                    </option>
                    <option value="<%= Permissions.NONE %>"
                            <%
                                if (Permissions.NONE.equals(permissionLevel)) {
                                    out.print("selected='selected'");
                                }
                            %>>
                        ---
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
                    <option value="<%= PortletAction.DENY %>"
                            <%
                                if (PortletAction.DENY.equals(permissionLevel)) {
                                    out.print("selected='selected'");
                                }
                            %>>
                        Deny
                    </option>
                </select>
            </td>
            <td><%= effectivePermissionLevel %></td>
        </tr>
    </c:forEach>
</table>
<input type="hidden" name="cancelReturnUrl" value="<c:out value="${actionBean.cancelReturnUrl}"/>"/>