<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="s" uri="/struts-tags"
%><%@ taglib prefix="mdes" uri="/manydesigns-elements-struts2"
%><s:include value="/skins/default/header.jsp"/>
<script>
    $(document).ready(function(){
        $("#column_columnType")
                .autocomplete({source: './TableDesign!jsonTypes.action?table_databaseName='
                +$("#table_databaseName").val()});

    } );
  </script>

<s:form method="post">
    <s:include value="/skins/default/model/tableDesign/createButtonsBar.jsp"/>
    <div id="inner-content">
        <div id="table-create">
            <h1>Create new table </h1>
            <h2>Table</h2>
            <mdes:write value="tableForm"/>
            <h2>Columns </h2>
            <s:if test="columnTableForm != null">
                <mdes:write value="columnTableForm"/>
                <s:submit id="rem_col" method="create" value="Remove column" />
            </s:if>
            <p/>
            <mdes:write value="columnForm"/>
            <s:submit id="add_col" method="create" value="Add column" />
            <h2>Primary key</h2>
            <mdes:write value="pkForm"/>
            <s:if test="pkColumnTableForm != null">
                <mdes:write value="pkColumnTableForm"/>
                <s:submit id="rem_pkCol" method="create" value="Remove primary key column" />
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
                                <option value="<s:property/>"><s:property/></option>
                            </s:iterator>
                        </select>
                    </td>
                </tr>
                <tr>
                    <th><label class="field" for="pk_gentype">Generator type:</label>
                    </th>
                    <td>
                        <script type="text/javascript">
                            function show(divName){
                                $("#" + divName).show().siblings().hide();
                            };
                        </script>
                        <select type="text" class="text" name="pk_genType" id="pk_gentype"
                                onchange="show(this.value)">
                            <option value="none">none</option>
                            <option value="increment">auto increment</option>
                            <option value="sequence">sequence</option>
                            <option value="table">table</option>
                        </select>
                        <div id="options">
                            <div id="none">
                                <em>no data required</em>
                            </div>
                            <div id="increment" style="display:none">
                                <em>no data required</em>
                            </div>
                            <div id="sequence" style="display:none">
                                <label class="field" for="pk_seqName">Sequence name:</label>
                                <input id="pk_seqName" name="pk_seqName" type="text"/>
                            </div>
                            <div id="table" style="display:none">
                                <label class="field" for="pk_tabName">Table name:</label>
                                <input id="pk_tabName" name="pk_tabName" type="text"/><br/>
                                <label class="field" for="pk_colName">Column name:</label>
                                <input id="pk_colName" name="pk_colName" type="text"/><br/>
                                <label class="field" for="pk_colValue">Column value:</label>
                                <input id="pk_colValue" name = "pk_colValue" type="text"/><br/>
                            </div>
                        </div>
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
    $("#column_columnType").focusout(function(){
    $("#column_javaType")
                .autocomplete({source: './TableDesign!jsonJavaTypes.action?table_databaseName='
                +$("#table_databaseName").val()+'&column_columnType='
                +$("#column_columnType").val()}, {minLength: 0});
    });
  </script>
<s:include value="/skins/default/footer.jsp"/>