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
        YAHOO.util.Dom.setStyle(mainContainer, 'min-height', sideBar.offsetHeight + 'px');
    }
    ;
};

$(function() {
    $("input:submit, button").button();
});