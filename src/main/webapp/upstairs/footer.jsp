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
                </ul>
                <hr/>
                <a href="<s:url value="/"/>">Go downstairs</a>
            </div>
        </div>
    </div>
    <div id="ft">Upstairs footer</div>
</div>
<script type="text/javascript">
YAHOO.example.fixSideBar();
</script>
</body>
</html>