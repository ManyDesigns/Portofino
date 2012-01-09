<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes-dynattr.tld"%>
<script src="<stripes:url value="/ace-0.2.0/ace.js" />" type="text/javascript" charset="utf-8"></script>
<script src="<stripes:url value="/ace-0.2.0/theme-twilight.js" />" type="text/javascript" charset="utf-8"></script>
<script src="<stripes:url value="/ace-0.2.0/mode-groovy.js" />" type="text/javascript" charset="utf-8"></script>
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


<fieldset id="scriptFieldset" class="mde-form-fieldset"
          style="position: relative; padding-top: 1em; margin-top: 1em; min-height: 20em;">
    <legend>Script</legend>
    <textarea id="scriptEditorTextArea"
              name="script" style="min-height: 20em; width: 100%;"
            ><c:out value="${actionBean.script}" /></textarea>
    <pre id="scriptEditor"
         style="min-height: 20em; width: 100%; display: none;"></pre>
</fieldset>