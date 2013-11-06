<%@ page import="com.manydesigns.portofino.dispatcher.PageInstance" %>
<%@ page import="com.manydesigns.portofino.logic.SecurityLogic" %>
<%@ page import="com.manydesigns.portofino.pageactions.AbstractPageAction" %>
<%@ page import="com.manydesigns.portofino.security.AccessLevel" %>
<%@ page import="org.apache.commons.configuration.Configuration" %>
<%@ page import="org.apache.shiro.SecurityUtils" %>
<%@ page import="org.apache.shiro.subject.Subject" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    AbstractPageAction theActionBean = (AbstractPageAction) request.getAttribute("actionBean");
    Subject subject = SecurityUtils.getSubject();
    Configuration portofinoConfiguration = theActionBean.getPortofinoConfiguration();
    PageInstance pageInstance = theActionBean.getPageInstance();
    if(SecurityLogic.hasPermissions(portofinoConfiguration, pageInstance, subject, AccessLevel.DEVELOP)) { %>
<script src="<stripes:url value="/m/pageactions/ace/ace.js" />" type="text/javascript" charset="utf-8"></script>
<script src="<stripes:url value="/m/pageactions/ace/theme-twilight.js" />" type="text/javascript" charset="utf-8"></script>
<script src="<stripes:url value="/m/pageactions/ace/mode-groovy.js" />" type="text/javascript" charset="utf-8"></script>
<script type="text/javascript">
    $(function() {
        $("#scriptEditor").css('display', 'block');

        var editor = ace.edit("scriptEditor");

        var GroovyMode = require("ace/mode/groovy").Mode;
        editor.getSession().setMode(new GroovyMode());

        var textarea = $("#scriptEditorTextArea");

        textarea.css('display', 'none');

        editor.getSession().setValue(textarea.val());
        $('button[name=updateConfiguration]').click(function() {
            textarea.val(editor.getSession().getValue());
        });
    });
</script>


<fieldset id="scriptFieldset">
    <legend>Script</legend>
    <textarea id="scriptEditorTextArea"
              name="script" style="min-height: 20em; width: 100%;"
            ><c:out value="${actionBean.script}" /></textarea>
    <pre id="scriptEditor"></pre>
</fieldset>
<% } %>