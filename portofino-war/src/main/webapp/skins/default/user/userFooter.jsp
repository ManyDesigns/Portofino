<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
            </div>
        </div>
        <div id="sidebar" class="yui-b">
            <ul class="areas">
                <li><a href="<s:url namespace="/" action="Homepage"/>">Homepage</a></li>
                <li><a href="<s:url namespace="/" action="TableData"/>">Table data</a></li>
                <li><a href="<s:url namespace="/" action="TableDesign"/>">Table design</a></li>
                <li><a href="<s:url namespace="/upstairs" action="Homepage"/>">Admin</a></li>
                <li class="selected"><a href="<s:url namespace="/" action="Profile"/>">Personal area</a></li>
            </ul>
            <hr/>
            <ul>
                <li><a href="<s:url namespace="/" action="Profile"/>">Profile</a></li>
                <li><a href="<s:url namespace="/" action="Settings"/>">Settings</a></li>
                <li><a href="<s:url namespace="/" action="Help"/>">Help</a></li>
            </ul>
        </div>
    </div>
<s:include value="/skins/default/footer.jsp"/></html>