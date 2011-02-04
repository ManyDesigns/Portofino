<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="s" uri="/struts-tags"
%><%@ taglib prefix="mdes" uri="/manydesigns-elements-struts2"
%><s:include value="/skins/default/header.jsp"/>
<script type="text/javascript">
    $(document).ready(function(){
        $("#column_columnType")
                .autocomplete({source: './TableDesign!jsonTypes.action?table_databaseName='
                +$("#table_databaseName").val()});
        $("div.wizardContent").css('display', "none");

        $("#tableDiv").click(function(){
            $("#tableContent").slideDown('fast');
            $("#colContent").slideUp();
            $("#pkContent").slideUp();
            $("#annContent").slideUp();
        });


        $("#colDiv").click(function(){
            $("#colContent").slideDown('fast');
            $("#tableContent").slideUp();
            $("#pkContent").slideUp();
            $("#annContent").slideUp();
        });


        $("#pkDiv").click(function(){
            $("#pkContent").slideDown('fast');
            $("#tableContent").slideUp();
            $("#colContent").slideUp();
            $("#annContent").slideUp();
        });



        $("#annDiv").click(function(){
            $("#annContent").slideDown('fast');
            $("#tableContent").slideUp();
            $("#colContent").slideUp();
            $("#pkContent").slideUp();
        });

        <s:if test="step == null">
            $("#tableContent").slideDown('fast');
        </s:if>

        <s:if test="step == 1">
            $("#tableContent").slideDown('fast');
        </s:if>
        <s:if test="step == 2">
            $("#colContent").slideDown('fast');
        </s:if>
        <s:if test="step == 3">
            $("#pkContent").slideDown('fast');
        </s:if>
        <s:if test="step == 4">
            $("#annContent").slideDown('fast');
        </s:if>

    } );
  </script>

<s:form method="post">
    <s:include value="/skins/default/model/tableDesign/createButtonsBar.jsp"/>
    <div id="inner-content">
        <div id="table-create">
            <h1>Create new table </h1>

            <!-- Table Section -->
            <div id="tableDiv" class="wizardStep">
                <h2>Step 1. Table defintion</h2>
                <div id="tableContent" class="wizardContent">
                    <mdes:write value="tableForm"/>
                </div>
            </div>

                <!-- Column Section -->
            <div id="colDiv" class="wizardStep">
                <h2>Step 2. Columns</h2>
                <div id="colContent" class="wizardContent">
                <s:if test="ncol != 0">
                    <mdes:write value="columnTableForm"/>
                    <s:submit id="rem_col" method="remCol" value="Remove column" />
                </s:if>
                <p></p>
                <mdes:write value="columnForm"/>
                <s:submit id="add_col" method="addCol" value="Add column" />
                </div>
            </div>

            <!-- Primarykey Section -->
            <div id="pkDiv" class="wizardStep">
                <h2>Step 3. Primary key</h2>
                <div id="pkContent" class="wizardContent">
                <mdes:write value="pkForm"/>
                <s:if test="npkcol != 0">
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
                                    onchange="show(this.value);">
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
                <s:submit id="add_pkCol" method="addPkCol" value="Add primary key column" />
                <p></p>
                </div>
            </div>

            <!-- Annotation Section -->
            <div id="annDiv" class="wizardStep">
                <h2>Step 4. Annotations</h2>
                <div id="annContent" class="wizardContent">
                <s:if test="nAnnotations != 0">
                    <mdes:write value="colAnnotationTableForm"/>
                    <s:submit id="remColAnnotation" method="remColAnnotation" value="Remove annotation" />
                </s:if>
                <p></p>
                <mdes:write value="colAnnotationForm"/>
                <s:submit id="addColAnnotation" method="addColAnnotation" value="Add annotation" />
                </div>
            </div>
        </div>
        <s:hidden name="cancelReturnUrl" value="%{cancelReturnUrl}"/>
        <s:hidden name="ncol" value="%{ncol}"/>
        <s:hidden name="npkcol" value="%{npkcol}"/>
        <s:hidden name="nAnnotations" value="%{nAnnotations}"/>
    </div>
    <s:include value="/skins/default/model/tableDesign/createButtonsBar.jsp"/>
</s:form>
<script type="text/javascript">

    $("#table_databaseName").change(function(){
        $("#column_columnType").autocomplete({source: './TableDesign!jsonTypes.action?table_databaseName='+$("#table_databaseName").val()});
    } );
    $("#colAnn_columnName").focusin(function(){
        var columns = [<s:iterator value="columnNames" status="itStatus">"<s:property/>"<s:if test="#itStatus.last != true ">,</s:if></s:iterator>];
        $("#colAnn_columnName")
                .autocomplete({source: columns});
    });
    $("#colAnn_typeName").focusin(function(){
        var columns = [<s:iterator value="annotations" status="itStatus">"<s:property/>"<s:if test="#itStatus.last != true ">,</s:if></s:iterator>];
        $("#colAnn_typeName")
                .autocomplete({source: columns});
    });
    $("#column_columnType").focusout(function(){
        $("#column_javaType")
                .autocomplete({source: './TableDesign!jsonJavaTypes.action?table_databaseName='
                +$("#table_databaseName").val()+'&column_columnType='
                +$("#column_columnType").val()}, {minLength: 0});
        $.getJSON('./TableDesign!jsonTypeInfo.action?table_databaseName='
                +$("#table_databaseName").val()+'&column_columnType='
                +$("#column_columnType").val(), function(data) {
                    if (data.precision=='false') {
                        $("#column_length").attr('disabled', true);
                    } else {
                        $("#column_length").attr('disabled', false);
                    }

                    if (data.scale=='false') {
                        $("#column_scale").attr('disabled', true);
                    } else {
                        $("#column_scale").attr('disabled', false);
                    }

                    if (data.searchable=='false') {
                        $("#column_searchable").attr('disabled', true);
                    } else {
                        $("#column_searchable").attr('disabled', false);
                    }

                    if (data.autoincrement=='false') {
                        $("#column_autoincrement").attr('disabled', true);
                    } else {
                        $("#column_autoincrement").attr('disabled', false);
                    }
                 });
    });
  </script>
<s:include value="/skins/default/footer.jsp"/>