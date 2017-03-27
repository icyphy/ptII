# Modification of Node.js SDK for Api.ai to run in Nashorn.

This plugin allows integrating agents from the [Api.ai](http://api.ai) natural language processing service with your JavaScript application.

The key change is to use the httpClient module from the accessors
framework (http://accessors.org) so that the same module works for
Node and for Nashorn hosts.

* [Installation](#installation)
* [Usage](#usage)

# Installation

* Install [Node.js](https://nodejs.org/)
* Install Api.ai SDK with `npm`:
```shell
npm install apiai
```

# Usage
* Create `main.js` file with the following code:
```javascript
var apiai = require('apiai');

var app = apiai("<your client access token>");

var request = app.textRequest('<Your text query>', {
    sessionId: '<unique session id>'
});

request.on('response', function(response) {
    console.log(response);
});

request.on('error', function(error) {
    console.log(error);
});

request.end();
```
* Run following command.
```shell
node main.js
```
* Your can find more examples in [`examples`](examples) directory.
