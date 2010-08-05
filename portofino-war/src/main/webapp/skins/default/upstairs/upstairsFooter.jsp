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
                    <li><a href="<s:url namespace="/upstairs" action="PrintModel"/>">Model</a></li>
                    <li><a href="<s:url namespace="/upstairs" action="SelfTest"/>">Self test</a></li>
                </ul>
                <hr/>
                <a href="<s:url namespace="/" action="Homepage"/>">Go downstairs</a>
            </div>
        </div>
    </div>
<s:include value="/skins/default/footer.jsp"/></html>