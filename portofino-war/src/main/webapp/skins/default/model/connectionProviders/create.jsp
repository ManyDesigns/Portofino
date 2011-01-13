<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="s" uri="/struts-tags"
%><%@taglib prefix="mdes" uri="/manydesigns-elements-struts2"
%><s:include value="/skins/default/header.jsp"/>
<s:form method="post">
    <s:include value="/skins/default/model/connectionProviders/createButtonsBar.jsp"/>
    

    <div id="inner-content">
       <script type="text/javascript">
        function show(divName){
            $("#" + divName).show().siblings().hide();
        };
      </script>
        <h1>Create new connection provider</h1>
        Connection Type: <select name="connectionType" onchange="show(this.value)">
            <option value="none">-- Connection type --</option>
            <option value="jdbc">Jdbc</option>
            <option value="jndi">Jndi</option>
        </select>
        <div id="options">
            <div id="none">
                <em>select a type</em>
            </div>
            <div id="jdbc" style="display:none">
                <h2>Jdbc Connection Provider</h2>
                <mdes:write value="jdbcForm"/>
            </div>
            <div id="jndi" style="display:none">
                <h2>Jndi Connection Provider</h2>
                <mdes:write value="jndiForm"/>
            </div>
        </div>
    </div>
    <s:include value="/skins/default/model/connectionProviders/createButtonsBar.jsp"/>
</s:form>
<s:include value="/skins/default/footer.jsp"/>