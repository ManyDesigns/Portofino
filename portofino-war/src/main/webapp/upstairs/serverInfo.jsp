<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="mdes" uri="/manydesigns-elements-struts2" %>
<s:include value="/upstairs/header.jsp"/>
<h1>Server info</h1>
<table>
    <tbody>
    <tr>
        <td>Real path:</td>
        <td><s:property value="#application.serverInfo.realPath"/></td>
    </tr>
    <tr>
        <td>Servlet context name:</td>
        <td><s:property value="#application.serverInfo.servletContextName"/></td>
    </tr>
    <tr>
        <td>Server info:</td>
        <td><s:property value="#application.serverInfo.serverInfo"/></td>
    </tr>
    <tr>
        <td>Servlet API version:</td>
        <td><s:property value="#application.serverInfo.servletApiVersion"/></td>
    </tr>
    <tr>
        <td>java.runtime.name:</td>
        <td><s:property value="#application.serverInfo.javaRuntimeName"/></td>
    </tr>
    <tr>
        <td>java.runtime.version:</td>
        <td><s:property value="#application.serverInfo.javaRuntimeVersion"/></td>
    </tr>
    <tr>
        <td>java.vm.name:</td>
        <td><s:property value="#application.serverInfo.javaVmName"/></td>
    </tr>
    <tr>
        <td>java.vm.version:</td>
        <td><s:property value="#application.serverInfo.javaVmVersion"/></td>
    </tr>
    <tr>
        <td>java.vm.vendor:</td>
        <td><s:property value="#application.serverInfo.javaVmVendor"/></td>
    </tr>
    <tr>
        <td>os.name:</td>
        <td><s:property value="#application.serverInfo.osName"/></td>
    </tr>
    <tr>
        <td>user.language:</td>
        <td><s:property value="#application.serverInfo.userLanguage"/></td>
    </tr>
    <tr>
        <td>user.region:</td>
        <td><s:property value="#application.serverInfo.userRegion"/></td>
    </tr>
    <tr>
        <td>Free memory:</td>
        <td><s:property value="#application.serverInfo.freeMemory/1000"/> KB</td>
    </tr>
    <tr>
        <td>Total memory:</td>
        <td><s:property value="#application.serverInfo.totalMemory/1000"/> KB</td>
    </tr>
    <tr>
        <td>Max memory:</td>
        <td><s:property value="#application.serverInfo.maxMemory/1000"/> KB</td>
    </tr>
    <tr>
        <td>Available processors:</td>
        <td><s:property value="#application.serverInfo.availableProcessors"/></td>
    </tr>
    </tbody>
</table>
<s:include value="/upstairs/footer.jsp"/>