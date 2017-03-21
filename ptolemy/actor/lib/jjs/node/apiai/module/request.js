/*!
 * apiai
 * Copyright(c) 2015 http://api.ai/
 * Apache 2.0 Licensed
 */

'use strict';

var EventEmitter = require('events').EventEmitter;
var util = require('util');

exports.Request = module.exports.Request = Request;

util.inherits(Request, EventEmitter);

function Request (application, options) {
    var self = this;

    self.clientAccessToken = application.clientAccessToken;

    self.hostname = application.hostname;
    
    self.secure = application.secure;

    self.endpoint = options.endpoint;
    self.requestSource = application.requestSource;

    var requestOptions = self._requestOptions();

    var request = application._http.request(requestOptions, function(response) {
        self._handleResponse(response);
    });

    request.on('error', function(error) {
        self.emit('error', error);
    });

    self.request = request;
}

Request.prototype._handleResponse = function(response) {
    throw new Error("Can't call abstract method!");
};

Request.prototype._headers = function() {
    var self = this;

    return {
        'Accept': 'application/json',
        'Authorization': 'Bearer ' + self.clientAccessToken,
        'api-request-source': self.requestSource
    };
};

Request.prototype._requestOptions = function() {
    var self = this;

    return {
        url: {
            host: self.hostname,
            protocol: self.secure ? 'https' : 'http',
            port: 443
        },
        headers: self._headers(),
        keepAlive: true,
    };
};

Request.prototype.write = function(chunk) {
    this.request.write(chunk);
};

Request.prototype.end = function() {
    this.request.end();
};
