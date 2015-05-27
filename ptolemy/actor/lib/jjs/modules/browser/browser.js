/**
 * Module to use the default browser as a display and a GUI.
 * NOTE: This is very incomplete! Just a placeholder for now.
 * @module browser
 * @authors Edward A. Lee
 * @copyright http://terraswarm.org/accessors/copyright.txt
 */

/** Display the specified HTML text.
 *  @param html The HTML to display.
 */
module.exports.display = function(html) {
    // FIXME: Probably should provide an initialize() function to start the
    // server.
    var helper = Java
            .type('ptolemy.actor.lib.jjs.modules.browser.VertxBrowserHelper');
    // FIXME: Use a port selection algorithm here to avoid port conflicts.
    var port = 8080;
    var server = helper.createServer(port);
    server.setResponse(html);

    var browserLauncher = Java.type('ptolemy.actor.gui.BrowserLauncher');
    browserLauncher.openURL('http://localhost:' + port);
}
