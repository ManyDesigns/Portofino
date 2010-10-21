function updateSelectOptions(relName, selectionProviderIndex) {
    var selectFieldId = arguments[2 + selectionProviderIndex];

    var data = {
        relName : relName,
        selectionProviderIndex : selectionProviderIndex,
        'method:jsonSelectFieldOptions' : ''
    };
    for (var i = 2; i < arguments.length; i++ ) {
        var currentId = arguments[i];
        var current = $(currentId);
        data[current.attr('name')] = current.attr('value');
    }

    jQuery.ajax({
        type: 'POST',
        url: location.href,
        data: data,
        success: function(responseData) {
            var options = jQuery.parseJSON(responseData);

            var selectField = $(selectFieldId);
            var selOptions = selectField.attr('options');
            selOptions.length = 0;
//            selectField.empty();

            for (var i = 0; i < options.length; i++) {
                var option = options[i];
                var y = document.createElement('option');
                y.value = option['v'];
                y.text = option['l'];
                y.selected = option['s'];

                selOptions[selOptions.length] = y;
            }
            selectField.change();
        }
    });
}

function setupAutocomplete(autocompleteId, relName, selectionProviderIndex) {
    var setupArguments = arguments;
    var selectFieldId = setupArguments[3 + selectionProviderIndex];
    var autocompleteObj = $(autocompleteId);
    autocompleteObj.autocomplete({
        source: function( request, response ) {
            var data = {
                relName : relName,
                selectionProviderIndex : selectionProviderIndex,
                'method:jsonAutocompleteOptions' : '',
                labelSearch : request.term
            };
            for (var i = 3; i < setupArguments.length; i++ ) {
                var currentId = arguments[i];
                var current = $(currentId);
                data[current.attr('name')] = current.attr('value');
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
                            };
						}));
                },
                error: function(request, textStatus) {
                    alert(textStatus);
                }
            });
        },
        minLength: 1,
        select: function( event, ui ) {
            var selectField = $(selectFieldId);
            if (ui.item) {
                selectField.val(ui.item.optionValue);
            } else {
                selectField.val("");
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

function setupDatePicker(dateFieldId, dateFormat) {
    var dateField = $(dateFieldId);
    dateField.datepicker({ dateFormat: dateFormat });
}