<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="s" uri="/struts-tags"
%><%@ taglib prefix="mdes" uri="/manydesigns-elements-struts2"
%><s:include value="/skins/default/header.jsp"/>
<script>

    $(document).ready(function(){
     $("#column_columnType").autocomplete({source: './TableDesign!jsonTypes.action?table_databaseName='+$("#table_databaseName").val()});
 } );
  </script>

<s:form method="post">
    <s:include value="/skins/default/model/tableDesign/createButtonsBar.jsp"/>
    <div id="inner-content">
        <div id="table-create">
            <h1>Create new table </h1>
            <mdes:write value="tableForm"/>
            <h1>Columns </h1>
            <s:if test="columnTableForm != null">
                <mdes:write value="columnTableForm"/>
                <s:submit id="rem_col" method="create" value="Remove column" />
            </s:if>
            <p/>
            <mdes:write value="columnForm"/>
            <s:submit id="add_col" method="create" value="Add column" />
            <h1>Primary key</h1>
            <mdes:write value="pkForm"/>
            <s:if test="pkColumnTableForm != null">
                <mdes:write value="pkColumnTableForm"/>
                <s:submit id="rem_pkCol" method="remPkCol" value="Remove primary key column" />
            </s:if>
            <table class="details">
                <tbody>
                <tr>
                    <th><label class="field" for="pk_column">Column name:</label>
                    </th>
                    <td>
                        <select type="text" class="text" name="pk_column" id="pk_column">
                            <option id="-1">-- add column --</option>
                            <s:iterator value="columnNames">
                                <option id="<s:property/>"><s:property/></option>
                            </s:iterator>
                        </select>
                    </td>
                </tr>
                </tbody>
            </table>
            <s:submit id="add_pkCol" method="create" value="Add primary key column" />
            <p/>
        </div>
        <s:hidden name="cancelReturnUrl" value="%{cancelReturnUrl}"/>
        <s:hidden name="ncol" value="%{ncol}"/>
        <s:hidden name="npkcol" value="%{npkcol}"/>
    </div>
    <s:include value="/skins/default/model/tableDesign/createButtonsBar.jsp"/>
</s:form>
<script>

    $("#table_databaseName").change(function(){
     $("#column_columnType").autocomplete({source: './TableDesign!jsonTypes.action?table_databaseName='+$("#table_databaseName").val()});
 } );
  </script>
<s:include value="/skins/default/footer.jsp"/>