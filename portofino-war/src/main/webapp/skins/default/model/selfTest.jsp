<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="s" uri="/struts-tags"
%><%@ taglib prefix="mdes" uri="/manydesigns-elements-struts2"
%><s:include value="/skins/default/header.jsp"/>
<div id="inner-content">
    <h1>Self test</h1>
    Differences between "A" and "B":
    <s:if test="messages.size() > 0">
        <s:form method="post">
            <table>
                <s:iterator var="message" value="messages">
                    <tr><td><s:property value="#message"/></td></tr>
                </s:iterator>
            </table>
            <s:submit method="sync" value="Synchronize in-memory model"/>
        </s:form>
    </s:if><s:else>
        there are no differences.
    </s:else>
</div>
<s:include value="/skins/default/footer.jsp"/>