<%@ page contentType="text/html;charset=UTF-8" language="java"
         pageEncoding="UTF-8"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="mde" uri="/manydesigns-elements"
%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:include page="/skins/default/header.jsp"/>
<s:form method="post">
    <jsp:include page="/skins/default/model/selfTestButtonsBar.jsp"/>
    <div id="inner-content">
        <h1><fmt:message key="layouts.model.selfTest.self_test"/></h1>
        <p><fmt:message key="layouts.model.selfTest.source_db_target_mem"/>.</p>
        <p>
        Show:
        <br/>
        <s:checkbox name="showBothNull" value="showBothNull"/><fmt:message key="layouts.model.selfTest.both_null"/>
        <br/>
        <s:checkbox name="showSourceNull" value="showSourceNull"/><fmt:message key="layouts.model.selfTest.source_null"/>
        <br/>
        <s:checkbox name="showTargetNull" value="showTargetNull"/><fmt:message key="layouts.model.selfTest.target_null"/>
        <br/>
        <s:checkbox name="showEqual" value="showEqual"/><fmt:message key="layouts.model.selfTest.equal"/>
        <br/>
        <s:checkbox name="showDifferent" value="showDifferent"/><fmt:message key="layouts.model.selfTest.different"/>
        </p><p>
        <fmt:message key="layouts.model.selfTest.expand_tree"/>? <s:checkbox name="expandTree" value="expandTree"/>
        </p><table id="tree">
            <thead>
                <tr><th><fmt:message key="layouts.model.selfTest.datamodel_object"/></th><th><fmt:message key="layouts.model.selfTest.type"/></th><th><fmt:message key="layouts.model.selfTest.status"/></th></tr>
            </thead>
            <tbody>
                <mdes:write value="treeTableDiffer"/>
            </tbody>
        </table>
        <script type="text/javascript">
            $("#tree").treeTable({
                initialState : "<s:property value="expandTree ? 'expanded' : 'collapsed'"/>"
            });
        </script>
    </div>
    <jsp:include page="/skins/default/model/selfTestButtonsBar.jsp"/>
</s:form>
<jsp:include page="/skins/default/footer.jsp"/>