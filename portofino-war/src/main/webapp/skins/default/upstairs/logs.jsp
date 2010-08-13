<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="mdes" uri="/manydesigns-elements-struts2" %>
<s:include value="/skins/default/header.jsp"/>
<div id="inner-content">
    <h1>Handlers</h1>
    <table>
        <tr>
            <th>Name</th>
            <th>Level</th>
        </tr>
    <s:iterator value="handlers">
        <tr>
            <td><s:property value="toString()"/></td>
            <td><s:property value="level.name"/></td>
        </tr>
    </s:iterator>
    </table>

    <h1>Loggers</h1>
    Current LogManager: <s:property value="logManager"/><br/>
    <table>
        <tr>
            <th>Name</th>
            <th>Level</th>
        </tr>
    <s:iterator value="loggers">
        <tr>
            <td><s:property value="name"/></td>
            <td><s:property value="level.name"/></td>
        </tr>
    </s:iterator>
    </table>
</div>
<s:include value="/skins/default/footer.jsp"/>