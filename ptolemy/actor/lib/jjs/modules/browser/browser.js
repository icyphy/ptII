// Module to use the default browser as a display.

////////////////////
// Display the specified HTML text.
module.exports.display = function(html) {
    // FIXME: Probably should provide an initialize() function to start the server.
    var helper = Java.type('ptolemy.actor.lib.jjs.modules.browser.VertxHelper');
    // Use a port selection algorithm here to avoid port conflicts.
    var port = 8080;
    var server = helper.createServer(port);
    server.setResponse(html);

    var browserLauncher = Java.type('ptolemy.actor.gui.BrowserLauncher');
    browserLauncher.openURL('http://localhost:' + port);
}
