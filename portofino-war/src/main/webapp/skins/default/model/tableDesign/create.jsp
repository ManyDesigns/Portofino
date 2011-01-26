<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="s" uri="/struts-tags"
%><%@ taglib prefix="mdes" uri="/manydesigns-elements-struts2"
%><s:include value="/skins/default/header.jsp"/>
<s:form method="post">
    <s:include value="/skins/default/model/tableDesign/createButtonsBar.jsp"/>
    <div id="inner-content">
        <div id="table-create">
            <h1>Create new table </h1>
            <mdes:write value="tableForm"/>
            <h1>Columns </h1>
            <s:if test="table != null">
                <mdes:write value="columnTableForm"/>
            </s:if>
            <mdes:write value="columnForm"/>
            <s:submit id="add_col" method="create" value="Add column" />
            <h1>Primary key</h1>
            <mdes:write value="pkForm"/>
        </div>

        <s:hidden name="cancelReturnUrl" value="%{cancelReturnUrl}"/>
        <s:hidden name="ncol" value="%{ncol}"/>
    </div>
    <s:include value="/skins/default/model/tableDesign/createButtonsBar.jsp"/>
</s:form>
<s:include value="/skins/default/footer.jsp"/>