var portofino = portofino || {};

portofino.notifications = {
    reconnectInterval: 5000,
    transport: 'websocket',
    fallbackTransport: 'long-polling',

    createSubscription: function(topicName, handlers) {
        var url = portofino.contextPath + '/m/atmosphere/services/notifications/'  + topicName;
        handlers = handlers || {};
        var request = {
            url: url,
            contentType: "application/json",
            transport: portofino.notifications.transport,
            fallbackTransport: portofino.notifications.fallbackTransport,
            reconnectInterval: portofino.notifications.reconnectInterval,
            trackMessageLength: true
        };

        request.onMessage = function(response) {
            var message = response.responseBody;
            try {
                if(handlers.onMessage) {
                    if(request.contentType == "application/json") {
                        message = atmosphere.util.parseJSON(message);
                    }
                    handlers.onMessage(message, response);
                } else if(console && console.debug) {
                    console.debug("No handler for message", message, response);
                }
            } catch (e) {
                if(handlers.onError) {
                    handlers.onError(e, response);
                } else if(console && console.debug) {
                    console.debug("No error handler for message", message, e);
                }
            }
        };

        return request;
    },

    subscribe: function(subscription) {
        return atmosphere.subscribe(subscription);
    }
};