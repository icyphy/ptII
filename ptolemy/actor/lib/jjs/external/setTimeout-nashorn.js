// From: https://gist.github.com/josmardias/20493bd205e24e31c0a406472330515a
// See also https://www.terraswarm.org/accessors/wiki/Notes/NashornHost#SetTimeout

// https://gist.github.com/josmardias/20493bd205e24e31c0a406472330515a
// at least one timeout needs to be set, larger then your code bootstrap
//  or Nashorn will run forever
// preferably, put a timeout 0 after your code bootstrap

(function (context) {
    'use strict';

    var Timer = Java.type('java.util.Timer');
    var Phaser = Java.type('java.util.concurrent.Phaser');

    var timer = new Timer('jsEventLoop', false);
    var phaser = new Phaser();

    var timeoutStack = 0;

    function pushTimeout() {
        timeoutStack++;
    }

    function popTimeout() {
        timeoutStack--;
        if (timeoutStack > 0) {
            return;
        }
        timer.cancel();
        phaser.forceTermination();
    }

    var onTaskFinished = function () {
        phaser.arriveAndDeregister();
    };

    context.setTimeout = function (fn, millis /* [, args...] */ ) {
	print("setTimeout-nashorn.js: setTimeout(, " + millis + ")");
        var args = [].slice.call(arguments, 2, arguments.length);

        var phase = phaser.register();
        var canceled = false;
        // If you get
        // "java.lang.IllegalStateException: Timer already cancelled."
        // here, then it is because setTimeout( "function () {}", 99999)
        // was not called after after loading bootstrap files.
        timer.schedule(function () {
            if (canceled) {
                return;
            }

            try {
                fn.apply(context, args);
            } catch (e) {
                print(e);
            } finally {
                onTaskFinished();
                popTimeout();
            }
        }, millis);

        pushTimeout();

        return function () {
            onTaskFinished();
            canceled = true;
            popTimeout();
        };
    };

    context.clearTimeout = function (cancel) {
        cancel();
    };

    context.setInterval = function (fn, delay /* [, args...] */ ) {
	print("setTimeout-nashorn.js: setInterval(, " + delay + ")");
        var args = [].slice.call(arguments, 2, arguments.length);

        var cancel = null;

        var loop = function () {
            cancel = context.setTimeout(loop, delay);
            fn.apply(context, args);
        };

        cancel = context.setTimeout(loop, delay);
        return function () {
            cancel();
        };
    };

    context.clearInterval = function (cancel) {
        cancel();
    };

})(this);
