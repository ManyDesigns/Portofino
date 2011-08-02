function fixSideBar() {
    $(
        function() {
            var contentNode = $('#content');
            var sideBarNode = $('#sidebar');
            if (contentNode.offsetHeight < sideBarNode.offsetHeight) {
                contentNode.css('min-height', sideBarNode.offsetHeight + 'px')
            }
        }
    )
}

$(function() {
    $("input:submit.portletPageButton, button.portletPageButton").button();
    
    $("input:submit.portletButton, button.portletButton").button();

    $("input:submit.wrench, button.wrench").button({
            icons: {
                primary: "ui-icon-wrench"
            },
            text: false
        });
});