<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<s:iterator var="crudButton" value="crudButtons">
    <s:set var="button" value="#crudButton.button"/>
    <s:submit id="%{#button.name}"
              method="%{#button.actualMethod}"
              value="%{#button.label}"
              disabled="%{#crudButton.enabled?'false':'true'}"/>
</s:iterator>
