<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
            </div>
        </div>
        <div id="sidebar" class="yui-b">
            <div>
                <h2>Tables:</h2>
                <ul>
                    <s:iterator value="context.allTables">
                        <li><a href="<s:url namespace="/" action="%{qualifiedName}/TableDesign"/>"><s:property value="qualifiedName"/></a></li>
                    </s:iterator>
                </ul>
                <a href="<s:url namespace="/" action="%{qualifiedTableName}/TableData"/>">Switch to table data</a>                
                <hr/>
                <a href="<s:url namespace="/upstairs" action="Homepage"/>">Go upstairs</a>
            </div>
        </div>
    </div>
<s:include value="/skins/default/footer.jsp"/>