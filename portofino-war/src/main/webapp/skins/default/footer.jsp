<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%@ taglib prefix="mde" uri="/manydesigns-elements" %>
            </div>
        </div>
        <div id="sidebar" class="yui-b">
            <mde:write name="navigation"/>
        </div>
    </div>
    <div id="ft">
        <div id="responseTime">
            Page response time:
            <c:out value="${stopWatch.time}"/>
            ms. DB time:
            <c:out value="${context.dbTime}"/>
            ms.
        </div>
        Powered by <a href="http://www.manydesigns.com/">ManyDesigns Portofino</a>
        <c:out value="${portofinoProperties['portofino.version']}"/>
    </div>
</div>
<script type="text/javascript">
YAHOO.example.fixSideBar();
</script>
</body>
</html>