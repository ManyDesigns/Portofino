<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
            </div>
        </div>
        <div id="sidebar" class="yui-b">
            <div>
                <h2>Upstairs functions:</h2>
                <ul>
                    <li><a href="<s:url namespace="/upstairs" action="ServerInfo"/>">Server info</a></li>
                    <li><a href="<s:url namespace="/upstairs" action="Logs"/>">Logs</a></li>
                </ul>
                <hr/>
                <a href="<s:url value="/"/>">Go downstairs</a>
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