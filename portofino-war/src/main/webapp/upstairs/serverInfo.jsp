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
        <td><s:property value="serverInfo.realPath"/></td>
    </tr>
    <tr>
        <td>Servlet context name:</td>
        <td><s:property value="serverInfo.servletContextName"/></td>
    </tr>
    <tr>
        <td>Server info:</td>
        <td><s:property value="serverInfo.serverInfo"/></td>
    </tr>
    <tr>
        <td>Servlet API version:</td>
        <td><s:property value="serverInfo.servletApiVersion"/></td>
    </tr>
    <tr>
        <td>java.runtime.name:</td>
        <td><s:property value="serverInfo.javaRuntimeName"/></td>
    </tr>
    <tr>
        <td>java.runtime.version:</td>
        <td><s:property value="serverInfo.javaRuntimeVersion"/></td>
    </tr>
    <tr>
        <td>java.vm.name:</td>
        <td><s:property value="serverInfo.javaVmName"/></td>
    </tr>
    <tr>
        <td>java.vm.version:</td>
        <td><s:property value="serverInfo.javaVmVersion"/></td>
    </tr>
    <tr>
        <td>java.vm.vendor:</td>
        <td><s:property value="serverInfo.javaVmVendor"/></td>
    </tr>
    <tr>
        <td>os.name:</td>
        <td><s:property value="serverInfo.osName"/></td>
    </tr>
    <tr>
        <td>user.language:</td>
        <td><s:property value="serverInfo.userLanguage"/></td>
    </tr>
    <tr>
        <td>user.region:</td>
        <td><s:property value="serverInfo.userRegion"/></td>
    </tr>
    </tbody>
</table>
<s:include value="/upstairs/footer.jsp"/>