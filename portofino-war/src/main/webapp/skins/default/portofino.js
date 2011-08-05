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

function enablePortletDragAndDrop(button) {
    $("div.portletContainer").sortable({
        connectWith: "div.portletContainer",
        placeholder: "sortablePlaceholder",
        cursor: "move", // cursor image
        revert: true, // moves the portlet to its new position with a smooth transition
        tolerance: "pointer", // mouse pointer overlaps the droppable
        update: function(event, ui) {
            console.log($(this).find("hidden"));
            $(this).find(".updateLayout").remove();
            var elements = $(this).sortable('toArray');
            for(var index in elements) {
                var hiddenField = document.createElement("input");
                hiddenField.setAttribute("type", "hidden");
                hiddenField.setAttribute("name", "portletWrapper_" + this.id);
                hiddenField.setAttribute("value", elements[index].substring("portletWrapper_".length));
                hiddenField.setAttribute("class", "updateLayout");
                $(this).append(hiddenField);
            }
        }
    }).disableSelection()
            .css('padding', '1em 0')
            .css("border", "1px dashed grey")
            .css("margin-bottom", "1em")
            .css("min-height", "12em");
    var container = $(button).parent();
    $(button).remove();
    container.append('<button name="updateLayout">Save</button>');
    container.append('<button>Cancel</button>');
}
