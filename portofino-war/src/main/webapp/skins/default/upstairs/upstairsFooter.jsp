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
                <li class="selected"><a href="<s:url namespace="/upstairs" action="Homepage"/>">Admin</a></li>
                <li><a href="<s:url namespace="/" action="Profile"/>">Personal area</a></li>
            </ul>
            <hr/>
            <s:url var="currentUrl" />
            <ul>
                <s:url var="url" namespace="/upstairs" action="ServerInfo"/>
                <s:if test="#url == #currentUrl">
                    <li class="selected"><s:a href="%{#url}">Server info</s:a></li>
                </s:if><s:else>
                    <li><s:a href="%{#url}">Server info</s:a></li>
                </s:else>

                <s:url var="url" namespace="/upstairs" action="ConnectionProviders"/>
                <s:if test="#url == #currentUrl">
                    <li class="selected"><s:a href="%{#url}">Connection providers</s:a></li>
                </s:if><s:else>
                    <li><s:a href="%{#url}">Connection providers</s:a></li>
                </s:else>

                <s:url var="url" namespace="/upstairs" action="ConfigurationProperties"/>
                <s:if test="#url == #currentUrl">
                    <li class="selected"><s:a href="%{#url}">Configuration properties</s:a></li>
                </s:if><s:else>
                    <li><s:a href="%{#url}">Configuration properties</s:a></li>
                </s:else>

                <s:url var="url" namespace="/upstairs" action="Logs"/>
                <s:if test="#url == #currentUrl">
                    <li class="selected"><s:a href="%{#url}">Logs</s:a></li>
                </s:if><s:else>
                    <li><s:a href="%{#url}">Logs</s:a></li>
                </s:else>

                <s:url var="url" namespace="/upstairs" action="PrintModel"/>
                <s:if test="#url == #currentUrl">
                    <li class="selected"><s:a href="%{#url}">Model</s:a></li>
                </s:if><s:else>
                    <li><s:a href="%{#url}">Model</s:a></li>
                </s:else>

                <s:url var="url" namespace="/upstairs" action="SelfTest"/>
                <s:if test="#url == #currentUrl">
                    <li class="selected"><s:a href="%{#url}">Self test</s:a></li>
                </s:if><s:else>
                    <li><s:a href="%{#url}">Self test</s:a></li>
                </s:else>
            </ul>
        </div>
    </div>
<s:include value="/skins/default/footer.jsp"/>