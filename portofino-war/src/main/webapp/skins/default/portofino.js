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

function updateSelectOptions(relName, optionProviderIndex) {
    var selectFieldId = arguments[2 + optionProviderIndex];
    var selectField = document.getElementById(selectFieldId);

    var data = {
        relName : relName,
        optionProviderIndex : optionProviderIndex,
        'method:jsonSelectFieldOptions' : ''
    };
    for (var i = 2; i < arguments.length; i++ ) {
        var currentId = arguments[i];
        var current = document.getElementById(currentId);
        data[current.name] = current.value;
    }

    jQuery.ajax({
        type: 'POST',
        url: location.href,
        data: data,
        success: function(responseData) {
            var options = jQuery.parseJSON(responseData);

            // empty the select field
            while (selectField.length > 0) {
                selectField.remove(0);
            }

            for (var i = 0; i < options.length; i++) {
                var option = options[i];
                var y = document.createElement('option');
                y.value = option['v'];
                y.text = option['l'];
                y.selected = option['s'];
                if (jQuery.browser.msie) {
                    selectField.add(y);
                } else {
                    selectField.add(y, null);
                }
            }
            if (selectField.onchange) {
                selectField.onchange();
            }
        }
    });
}

function setupAutocomplete(autocompleteId, relName, optionProviderIndex) {
    var setupArguments = arguments;
    var selectFieldId = setupArguments[3 + optionProviderIndex];
    var myObj = $("#" + autocompleteId);
    myObj.autocomplete({
        source: function( request, response ) {
            var data = {
                relName : relName,
                optionProviderIndex : optionProviderIndex,
                'method:jsonAutocompleteOptions' : '',
                labelSearch : request.term
            };
            for (var i = 3; i < setupArguments.length; i++ ) {
                var currentId = setupArguments[i];
                var current = document.getElementById(currentId);
                data[current.name] = current.value;
            }

            $.ajax({
                type: 'POST',
                dataType: 'json',
                url: location.href,
                data: data,
                success: function( responseData ) {
                    response( $.map( responseData, function( item ) {
							return {
								label: item.l,
								value: item.l,
								optionValue: item.v
							}
						}));
                },
                error: function(request, textStatus, errorThrown) {
                    alert(textStatus);
                }
            });
        },
        minLength: 1,
        select: function( event, ui ) {
            var selectField = document.getElementById(selectFieldId);
            if (ui.item) {
                selectField.value = ui.item.optionValue;
            } else {
                selectField.value = "";
            }
        },
        open: function() {
            $( this ).removeClass( "ui-corner-all" ).addClass( "ui-corner-top" );
        },
        close: function() {
            $( this ).removeClass( "ui-corner-top" ).addClass( "ui-corner-all" );
        }
    });
}
