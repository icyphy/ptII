/**
 * Module for HTTP clients.
 * @module http
 * @author Marten Lohstroh and Edward A. Lee
 * @copyright http://terraswarm.org/accessors/copyright.txt
 *
 * This module provides a Nashorn implementation of a subset of the Node.js http module.
 */

// Java types used.
var HttpClientHelper = Java.type('ptolemy.actor.lib.jjs.modules.httpClient.HttpClientHelper');
var URL = Java.type('java.net.URL');

var EventEmitter = require('events').EventEmitter;

/** Issue an HTTP request and provide a callback function for responses.
 *  The callback is a function that is passed an instance of IncomingMessage,
 *  defined here. This function returns an instance of ClientRequest, also defined here.
 *  The options argument is a JSON object with the following optional fields:
 *  <ul>
 *  <li> '''host''': A string giving the domain name or IP address of the server to issue the request to.
 *       This defaults to 'localhost'.</li>
 *  <li> '''port''': Port of remote server. Defaults to 80.
 *  <li> '''localAddress''': A string giving a name or IP address for the local network interface to use
 *       for network connections. This defaults to 'localhost', but on machines with more than one
 *       network interface (e.g. WiFi and Ethernet), you may need to specify which one to use.
 *  <li> '''method''': A string specifying the HTTP request method. This defaults to 'GET', but can
 *       also be 'PUT', 'POST', or 'DELETE'.
 *  <li> '''path''': Request path as a string. This defaults to '/'. This can include a
 *       query string, e.g. '/index.html?page=12'. An exception is thrown if the request
 *       path contains illegal characters. Currently, only spaces are rejected but that
 *       may change in the future.
 *  <li> '''protocol''': The protocol. This is a string that defaults to 'http'.
 *  <li> '''headers''': A JSON object containing request headers. By default this is an empty object.
 *       Items may have a value that is an array of values, for headers with more than one value.
 *  <li> '''keepAlive''': A boolean that specified whether to keep sockets around in a pool
 *       to be used by other requests in the future. This defaults to false.
 *  <li> '''keepAliveMsecs''': When using HTTP KeepAlive, this is an integer that specifies
 *       how often (in milliseconds) to send a TCP KeepAlive packet over sockets being kept alive.
 *       This defaults 1000 and is only relevant if keepAlive is set to true.
 *  </ul>
 *  Alternatively, the options argument may be given as a URL (a string), in which case
 *  an HTTP GET will be issued to that URL.
 *  @param options The options.
 *  @param responseCallback The callback function to call with an instance of IncomingMessage.
 */
exports.request = function(options, responseCallback) {
  return new ClientRequest(options, responseCallback);
};

// NOTE: The following options are supported by http.request() in Node.js, but not here,
// or at least not tested (yet):
// hostname: To support url.parse() hostname is preferred over host
// socketPath: Unix Domain Socket (use one of host:port or socketPath)
// auth: Basic authentication i.e. 'user:password' to compute an Authorization header.
// agent: Controls Agent behavior. When an Agent is used request will default to Connection: keep-alive. Possible values:
// undefined (default): use global Agent for this host and port.
// Agent object: explicitly use the passed in Agent.
// false: opts out of connection pooling with an Agent, defaults request to Connection: close.

/** Convenience method to issue an HTTP GET.  This just calls request() and then
 *  calls end() on the object returned by request(). It returns the object returned
 *  by request() (an instance of ClientRequest). See request() for documentation of
 *  the arguments.
 *  @param options The options.
 *  @param responseCallback The callback function to call with an instance of IncomingMessage.
 */
exports.get = function(options, reponseCallback) {
  var request = exports.request(options, reponseCallback);
  request.end();
  return request;
};

// NOTE: The following events are produce by ClientRequest in Node.js
// From: http.ClientRequest 
// Event: 'response'
// Event: 'socket'
// Event: 'connect'
// Event: 'upgrade'
// Event: 'continue'
// From stream.Writeable
// Event: 'finish'
// Event: 'pipe'
// Event: 'unpipe'
// Event: 'error'

/** The class returned by the request function.
 *  This class provides the following functions:
 *  <ul>
 *  <li> '''end'''(): Call this to end the request. </li>
 *  <li> '''write'''(''data'', ''encoding''): Write data (e.g. for a POST request). </li>
 *  </ul>
 *  See the documentation of the request function for an explanation of the arguments.
 *  This is an event emitter that emits the following events:
 *  <ul>
 *  <li> 'error': If an error occurs. The message is passed as an argument. </li>
 *  <li> 'response': A response is received from the server. This event is automatically
 *       handled by calling responseCallback, if responseCallback is not null.</li>
 *  </ul>
 *  @param options The options.
 *  @param responseCallback The callback function to call with an instance of IncomingMessage.
 */
function ClientRequest(options, reponseCallback) {
  var self = this;
  
  var defaultOptions = {
    'host':'localhost',
    'port':80,
    'protocol':'http',
    'localAddress':'localhost',
    'method':'GET',
    'path':'/',
    'headers':{},
    'keepAlive':false,
    'keepAliveMsecs':1000
  };
    
  if (util.isString(options)) {
    var url = new URL(options);
    var port = url.getPort();
    if (port < 0) {
        port = url.getDefaultPort();
        if (port < 0) {
            port = 80;
        }
    }
    options = {
        'host':url.getHost(),
        'path':url.getPath(),
        'protocol':url.getProtocol(),
        'port':port
    };
  }
  // Fill in default values.
  options = util._extend(defaultOptions, options);

  // Attach the callback to be invoked when this object issues
  // a 'response' event.  
  if (reponseCallback) {
    self.once('response', reponseCallback);
  }
  
  self.on('error', function(message) {
    throw(message);
  });

  this.helper = HttpClientHelper.createHttpClient(this, options);
}
util.inherits(ClientRequest, EventEmitter);
exports.ClientRequest = ClientRequest;

/** End a request. */
ClientRequest.prototype.end = function() {
  this.helper.end();
};

// FIXME:
ClientRequest.prototype.write = function(data, encoding) {
   throw("Write not supported yet");
}

/** Internal method used to handle a response. The argument is an
 *  an instance of the Java class org.vertx.java.core.http.HttpClientResponse.
 *  This method uses the data therein to construct an IncomingMessage object
 *  and pass that as an argument to the 'response' event of the ClientRequest.
 *  @param response The response from the server.
 */
ClientRequest.prototype._response = function(response, body) {
    var code = response.statusCode();
    if (code >= 400) {
        // An error occurred.
        this.emit('error', 'Received response code ' + code + ". " + response.statusMessage());
    } else {
        var message = new IncomingMessage(response, body);
        this.emit('response', message);
    }
}

// NOTE: The following events are produce by IncomingMessage in Node.js
// From stream.Readable
// Event: 'readable'
// Event: 'data'
// Event: 'end'
// Event: 'close'
// Event: 'error'

/** Incoming message class.  This should not be constructed by the user,
 *  but rather is constructed by the _response function above.
 *  An instance of this class will be passed to the callback passed to the
 *  request() or get() functions. The instance contains:
 *  <ul>
 *  <li> '''statusCode''': an integer indicating the status of the response. </li>
 *  <li> '''statusMessage''': a string with the status message of the response. </li>
 *  </ul>
 *  @param response An instance of the Java class org.vertx.java.core.http.HttpClientResponse.
 */
IncomingMessage = function(response, body) {
    this.statusCode = response.statusCode();
    this.statusMessage = response.statusMessage();
    this.body = body;
}
