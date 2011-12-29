function updateSelectOptions(relName, selectionProviderIndex, methodName) {
    var selectFieldId = arguments[3 + selectionProviderIndex];

    var data = {
        relName : relName,
        selectionProviderIndex : selectionProviderIndex,
    };
    data[methodName] = '';
    for (var i = 3; i < arguments.length; i++ ) {
        var currentId = arguments[i];
        var current = $(currentId);
        data[current.attr('name')] = current.attr('value');
    }

    var postUrl = stripQueryString(location.href);
    jQuery.ajax({
        type: 'POST',
        url: postUrl,
        data: data,
        success: function(responseData) {
            var options = responseData;
            if('string' === typeof(options)) {
                options = jQuery.parseJSON(options);
            }

            var selectField = $(selectFieldId);
            var selOptions = selectField.attr('options');
            selOptions.length = 0;

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
                jsonAutocompleteOptions : '',
                labelSearch : request.term
            };
            for (var i = 3; i < setupArguments.length; i++ ) {
                var currentId = setupArguments[i];
                var current = $(currentId);
                data[current.attr('name')] = current.attr('value');
            }

            var postUrl = stripQueryString(location.href);
            $.ajax({
                type: 'POST',
                dataType: 'json',
                url: postUrl,
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

function stripQueryString(url) {
    var regexp = new RegExp("\\?.*");
    return url.replace(regexp, "");
}
