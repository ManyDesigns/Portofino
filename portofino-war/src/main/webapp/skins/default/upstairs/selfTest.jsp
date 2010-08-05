<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="mdes" uri="/manydesigns-elements-struts2" %>
<s:include value="/skins/default/header.jsp"/>
<h1>Self test</h1>
Differences between "<s:property value="diff.modelName1"/>"
and "<s:property value="diff.modelName2"/>":
<s:if test="diff.size() > 0">
    <s:form method="post">
        <table>
            <s:iterator var="message" value="diff">
                <tr><td><s:property value="#message"/></td></tr>
            </s:iterator>
        </table>
        <s:submit method="sync" value="Synchronize in-memory model"/>
    </s:form>
</s:if><s:else>
    there are no differences.
</s:else>
<s:include value="/skins/default/upstairs/upstairsFooter.jsp"/>