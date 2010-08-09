<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<div class="buttons-bar">
    <s:submit id="ConnectionProviders_create" method="create" value="Create new"/>
    <s:submit id="ConnectionProviders_bulkDelete" method="bulkDelete" value="Delete"
              onclick="return confirm ('Are you sure?');"/>
</div>