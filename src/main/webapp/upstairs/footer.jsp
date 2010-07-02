<%@ page contentType="text/html;charset=ISO-8859-1" language="java"
         pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
            </div>
        </div>
        <div id="sidebar" class="yui-b">
            <div>
                <h2>Upstairs functions:</h2>
                <ul>
                    <li><a href="<s:url action="ServerInfo"/>">Server info</a></li>
                </ul>
                <hr/>
                <a href="<s:url value="/"/>">Go downstairs</a>
            </div>
        </div>
    </div>
    <div id="ft">Footer</div>
</div>
<script type="text/javascript" src="<s:url value="/yui-2.8.1/build/yahoo-dom-event/yahoo-dom-event.js"/>"></script>
<script type="text/javascript">
    YAHOO.example.fixSideBar = function(){
        var outerContainer = YAHOO.util.Dom.get('doc2') || YAHOO.util.Dom.get('doc');
        if(outerContainer){
            var currentWidth = YAHOO.util.Dom.getViewportWidth();
            outerContainer.id = (currentWidth < 950) ? 'doc' : 'doc2';
        };
        var mainContainer = YAHOO.util.Dom.get('yui-main');
        var sideBar = YAHOO.util.Dom.get('sidebar');
        if(mainContainer && sideBar){
            YAHOO.util.Dom.setStyle(sideBar,'height',mainContainer.offsetHeight + 'px');
        };
    }();
</script>
<s:include value="/footer.jsp"/>