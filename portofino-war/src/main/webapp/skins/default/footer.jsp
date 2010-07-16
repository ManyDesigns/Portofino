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
                        <li><a href="<s:url namespace="/" action="%{qualifiedName}/Table"></s:url>"><s:property value="qualifiedName"/></a></li>
                    </s:iterator>
                </ul>
                <hr/>
                <a href="<s:url value="/upstairs"/>">Go upstairs</a>
            </div>
        </div>
    </div>
    <div id="ft"><div id="responseTime">Response time: <s:property value="#request.stopWatch.time"/> ms</div>
        Powered by <a href="http://www.manydesigns.com/">ManyDesigns Portofino</a>
        <s:property value="#application.portofinoVersion"/>
    </div>
</div>
<script type="text/javascript">
YAHOO.example.fixSideBar();
</script>
</body>
</html>