/**
 * Module for HTTP support.
 * @module http
 * @author Marten Lohstroh
 * @copyright http://terraswarm.org/accessors/copyright.txt
 */

var HttpClientHelper = Java.type('ptolemy.actor.lib.jjs.modules.httpClient.HttpClientHelper');
var HashMap = Java.type('java.util.HashMap');
var EventEmitter = require('events').EventEmitter;
var url = require('url');

exports.request = function(options, cb) {
  return new ClientRequest(options, cb);
};

// based on node.js http module
 // OPTIONS:
 // host: A domain name or IP address of the server to issue the request to. Defaults to 'localhost'.
 // hostname: To support url.parse() hostname is preferred over host
 // port: Port of remote server. Defaults to 80.
 // localAddress: Local interface to bind for network connections.
 // socketPath: Unix Domain Socket (use one of host:port or socketPath)
 // method: A string specifying the HTTP request method. Defaults to 'GET'.
 // path: Request path. Defaults to '/'. Should include query string if any. E.G. '/index.html?page=12'. An exception is thrown when the request path contains illegal characters. Currently, only spaces are rejected but that may change in the future.
 // headers: An object containing request headers.
 // auth: Basic authentication i.e. 'user:password' to compute an Authorization header.
 // agent: Controls Agent behavior. When an Agent is used request will default to Connection: keep-alive. Possible values:
 // undefined (default): use global Agent for this host and port.
 // Agent object: explicitly use the passed in Agent.
 // false: opts out of connection pooling with an Agent, defaults request to Connection: close.
 // keepAlive: {Boolean} Keep sockets around in a pool to be used by other requests in the future. Default = false
 // keepAliveMsecs: {Integer} When using HTTP KeepAlive, how often to send TCP KeepAlive packets over sockets being kept alive. Default = 1000. Only relevant if keepAlive is set to true.

function ClientRequest(options, cb) {
  var self = this;
  var jOptions = new HashMap();
  
  if (util.isString(options)) {
    options = url.parse(options);
  } else {
    options = util._extend({}, options);
  }

  var agent = options.agent;
  var defaultAgent = options._defaultAgent || Agent.globalAgent;
//  if (agent === false) {
//    agent = new defaultAgent.constructor();
//  } else if (util.isNullOrUndefined(agent) && !options.createConnection) {
//    agent = defaultAgent;
//  }
//  self.agent = agent;

  var protocol = options.protocol || defaultAgent.protocol;
//  var expectedProtocol = defaultAgent.protocol;
//  if (self.agent && self.agent.protocol)
//    expectedProtocol = self.agent.protocol;
  
  
  var defaultPort = options.defaultPort || self.agent && self.agent.defaultPort;

  var port = options.port = options.port || defaultPort || 80;
  var host = options.host = options.hostname || options.host || 'localhost';

  jOptions.put('port', port);
  jOptions.put('host', host);

//  if (util.isUndefined(options.setHost)) {
//    var setHost = true;
//  }

//  self.socketPath = options.socketPath;

  var method = self.method = (options.method || 'GET').toUpperCase();
  var path = self.path = options.path || '/';
  
  jOptions.put('method', method);
  jOptions.put('path', path);
  
  if (cb) {
    self.once('response', cb);
  }

  // FIXME: implement setHeader()
//  if (!util.isArray(options.headers)) {
//    if (options.headers) {
//      var keys = Object.keys(options.headers);
//      for (var i = 0, l = keys.length; i < l; i++) {
//        var key = keys[i];
//        self.setHeader(key, options.headers[key]);
//      }
//    }
//    if (host && !this.getHeader('host') && setHost) {
//      var hostHeader = host;
//      if (port && +port !== defaultPort) {
//        hostHeader += ':' + port;
//      }
//      this.setHeader('Host', hostHeader);
//    }
//  }

//  if (options.auth && !this.getHeader('Authorization')) {
//    //basic auth
//    this.setHeader('Authorization', 'Basic ' +
//                   new Buffer(options.auth).toString('base64'));
//  }

  if (method === 'GET' ||
      method === 'HEAD' ||
      method === 'DELETE' ||
      method === 'OPTIONS' ||
      method === 'CONNECT') {
    self.useChunkedEncodingByDefault = false;
  } else {
    self.useChunkedEncodingByDefault = true;
  }

//  if (util.isArray(options.headers)) {
//    self._storeHeader(self.method + ' ' + self.path + ' HTTP/1.1\r\n',
//                      options.headers);
//  } else if (self.getHeader('expect')) {
//    self._storeHeader(self.method + ' ' + self.path + ' HTTP/1.1\r\n',
//                      self._renderHeaders());
//  }

  // This is where the helper kicks in and Vert.x takes over
  // FIXME: why bother passing options separately?
  // It looks like ScriptMirrorObject should have access to this info
  this.helper = HttpClientHelper.createClientRequest(this, jOptions);

}

util.inherits(ClientRequest, EventEmitter);

exports.ClientRequest = ClientRequest;

exports.get = function(options, cb) {
  var req = exports.request(options, cb);
  req.end();
  return req;
};


ClientRequest.prototype._implicitHeader = function() {
// FIXME: implement _storeHeader(), _renderHeders()
//  this._storeHeader(this.method + ' ' + this.path + ' HTTP/1.1\r\n',
//                    this._renderHeaders());
};

// was originally implemented by OutGoingMessage
ClientRequest.prototype.end = function(data, encoding, callback) {
  if (util.isFunction(data)) {
    callback = data;
    data = null;
  } else if (util.isFunction(encoding)) {
    callback = encoding;
    encoding = null;
  }

  if (data && !util.isString(data) && !util.isBuffer(data)) {
    throw new TypeError('first argument must be a string or Buffer');
  }

  if (this.finished) {
    return false;
  }

  var self = this;
  function finish() {
    self.emit('finish');
  }

  if (util.isFunction(callback))
    this.once('finish', callback);


  if (!this._header) {
    this._implicitHeader();
  }

  if (data && !this._hasBody) {
    debug('This type of response MUST NOT have a body. ' +
          'Ignoring data passed to end().');
    data = null;
  }

  if (this.connection && data)
    this.connection.cork();

  var ret;
  if (data) {
    // Normal body write.
    ret = this.write(data, encoding); // FIXME: implement write()
  }

// FIXME: delegate this to helper 

//  if (this._hasBody && this.chunkedEncoding) {
//    ret = this._send('0\r\n' + this._trailer + '\r\n', 'binary', finish);
//  } else {
//    // Force a flush, HACK.
//    ret = this._send('', 'binary', finish);
//  }

// FIXME: disconnect

  this.finished = true;

  return ret;
};


// FIXME: missing methods...

