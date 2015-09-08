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
<script src="<stripes:url value="/webjars/ace/1.2.0/src-min/ace.js" />" type="text/javascript" charset="utf-8"></script>
<script src="<stripes:url value="/webjars/ace/1.2.0/src-min/theme-twilight.js" />" type="text/javascript" charset="utf-8"></script>
<script src="<stripes:url value="/webjars/ace/1.2.0/src-min/mode-groovy.js" />" type="text/javascript" charset="utf-8"></script>
<script src="<stripes:url value="/webjars/ace/1.2.0/src-min/ext-language_tools.js" />" type="text/javascript" charset="utf-8"></script>
<script src="<stripes:url value="/webjars/ace/1.2.0/src-min/ext-settings_menu.js" />" type="text/javascript" charset="utf-8"></script>
<script src="<stripes:url value="/webjars/javascript-detect-element-resize/0.5.3/jquery.resize.js"/>"  type="text/javascript"></script>
<script type="text/javascript">
    var editor;
    $(function() {
        $("#scriptEditor").css('display', 'block');

        editor = ace.edit("scriptEditor");
        editor.$blockScrolling = Infinity ;
        ace.require('ace/ext/settings_menu');

        var GroovyMode = require("ace/mode/groovy").Mode;
        //editor.setTheme("ace/theme/tomorrow");

        ace.require("ace/ext/language_tools");
        ace.config.loadModule("ace/ext/language_tools", function() {
            editor.setOptions({
                enableBasicAutocompletion: true,
                enableSnippets: true
            });
        });

        editor.commands.addCommands([{
            name: "showSettingsMenu",
            bindKey: {win: "Ctrl-j", mac: "Command-j"},
            exec: function(editor) {
                ace.config.loadModule("ace/ext/settings_menu", function(module) {
                    module.init(editor);
                    editor.showSettingsMenu();
                });
            },
            readOnly: true
        }]);

        editor.getSession().setMode(new GroovyMode());

        var textarea = $("#scriptEditorTextArea");
        textarea.css('display', 'none');

        editor.getSession().setValue(textarea.val());
        $('button[name=updateConfiguration]').click(function() {
            textarea.val(editor.getSession().getValue());
        });

        $( "#scriptEditor" ).resize(function() {
            editor.resize();
        });
    });
</script>


<fieldset id="scriptFieldset">
    <legend>Script</legend>
    <textarea id="scriptEditorTextArea" name="script" style="min-height: 20em; width: 100%;"><c:out value="${actionBean.script}" /></textarea>
    <pre style="resize: both;" id="scriptEditor"></pre>

</fieldset>
<% } %>