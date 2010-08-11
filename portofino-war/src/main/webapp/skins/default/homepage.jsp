<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@taglib prefix="mdes" uri="/manydesigns-elements-struts2" %>
<s:include value="/skins/default/header.jsp"/>
                <div id="inner-content">
                    <h1>Homepage</h1>
                </div>
            </div>
        </div>
        <div id="sidebar" class="yui-b">
            <ul class="areas">
                <li class="selected"><a href="<s:url namespace="/" action="Homepage"/>">Homepage</a></li>
                <li><a href="<s:url namespace="/" action="%{qualifiedTableName}/TableData"/>">Table data</a></li>
                <li><a href="<s:url namespace="/" action="%{qualifiedTableName}/TableDesign"/>">Table design</a></li>
                <li><a href="<s:url namespace="/upstairs" action="Homepage"/>">Admin</a></li>
                <li><a href="<s:url namespace="/" action="Profile"/>">Personal area</a></li>
            </ul>
        </div>
    </div>
<s:include value="/skins/default/footer.jsp"/>