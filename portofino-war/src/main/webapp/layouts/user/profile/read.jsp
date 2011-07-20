<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@taglib prefix="mde" uri="/manydesigns-elements"
%><jsp:include page="/skins/default/header.jsp"/>
<s:form method="post">
    <jsp:include page="/skins/default/user/profile/readButtonsBar.jsp"/>
    <div id="inner-content">

        <h1>Profile</h1>

        <mdes:write value="form"/>
        <h1>My groups</h1>
        <table>
            <thead>
                <tr>
                    <th>Name</th> <th>Description</th>
                </tr>
            </thead>
            <tbody>
                <s:iterator value="groups" var="group">
                <tr>
                    <td><s:property value="#group.name"/></td>
                    <td><s:property value="#group.description"/></td>
                </tr>
            </s:iterator>
            </tbody>
        </table>

    </div>
    <jsp:include page="/skins/default/user/profile/readButtonsBar.jsp"/>
</s:form>
<jsp:include page="/skins/default/footer.jsp"/>