<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="mdes" uri="/manydesigns-elements-struts2" %>
            </div>
        </div>
        <div id="sidebar" class="yui-b">
            <mdes:write value="#request.navigation"/>
        </div>
    </div>
    <div id="ft"><div id="responseTime">Page response time: <s:property value="#request.stopWatch.time"/> ms. DB time: <s:property value="#application.mdContext.dbTime"/> ms.</div>
        Powered by <a href="http://www.manydesigns.com/">ManyDesigns Portofino</a>
        <s:property value="#application.portofinoProperties['portofino.version']"/>
    </div>
</div>
<script type="text/javascript">
YAHOO.example.fixSideBar();
</script>
</body>
</html>