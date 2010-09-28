YAHOO.example.fixSideBar = function() {
    var outerContainer = YAHOO.util.Dom.get('doc2') || YAHOO.util.Dom.get('doc');
    if (outerContainer) {
        var currentWidth = YAHOO.util.Dom.getViewportWidth();
        outerContainer.id = (currentWidth < 950) ? 'doc' : 'doc2';
    }
    ;
    var mainContainer = YAHOO.util.Dom.get('content');
    var sideBar = YAHOO.util.Dom.get('sidebar');
    if (mainContainer && sideBar && mainContainer.offsetHeight < sideBar.offsetHeight) {
        YAHOO.util.Dom.setStyle(mainContainer, 'height', sideBar.offsetHeight + 'px');
    }
    ;
};

function updateSelectOptions(relName, optionProviderIndex, optionProviderValues) {
    var data = {
        relName : relName,
        optionProviderIndex : optionProviderIndex
    };
    for (var key in optionProviderValues) {
        data[key] = optionProviderValues[key];
    }
    $.ajax({
        type: 'POST',
        url: 'TableData!jsonSelectFieldOptions.action',
        data: data,
        success: function(data) {
            var options = jQuery.parseJSON(data);
            for (var i = 0; i < options.length; i++) {
                var option = options[i];
                alert(option['value'] + ' = ' + option['label']);
            }
        }
//                    "  dataType: dataType\n" +
    });
}
