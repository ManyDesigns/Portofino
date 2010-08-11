<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
            </div>
        </div>
        <div id="sidebar" class="yui-b">
            <ul class="areas">
                <li><a href="<s:url namespace="/" action="Homepage"/>">Homepage</a></li>
                <li><a href="<s:url namespace="/" action="%{qualifiedTableName}/TableData"/>">Table data</a></li>
                <li class="selected"><a href="<s:url namespace="/" action="%{qualifiedTableName}/TableDesign"/>">Table design</a></li>
                <li><a href="<s:url namespace="/upstairs" action="Homepage"/>">Admin</a></li>
                <li><a href="<s:url namespace="/" action="Profile"/>">Personal area</a></li>
            </ul>
            <hr/>
            <ul>
                <s:iterator value="dataModel.allTables">
                    <s:if test="qualifiedName.equals(qualifiedTableName)">
                        <li class="selected">
                    </s:if><s:else>
                        <li>
                    </s:else>
                    <a href="<s:url namespace="/" action="%{qualifiedName}/TableDesign"/>"><s:property value="qualifiedName"/></a></li>
                </s:iterator>
            </ul>
        </div>
    </div>
<s:include value="/skins/default/footer.jsp"/>