/* Instantiate and Invoke Accessors using Nashorn.

   Copyright (c) 2016-2017 The Regents of the University of California.
   All rights reserved.
   Permission is hereby granted, without written agreement and without
   license or royalty fees, to use, copy, modify, and distribute this
   software and its documentation for any purpose, provided that the above
   copyright notice and the following two paragraphs appear in all copies
   of this software.

   IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
   FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
   ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
   THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
   SUCH DAMAGE.

   THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
   INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
   MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
   PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
   CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
   ENHANCEMENTS, OR MODIFICATIONS.

   PT_COPYRIGHT_VERSION_2
   COPYRIGHTENDKEY

 */
package ptolemy.actor.lib.jjs;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.FileUtilities;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// NashornAccessorHostApplication

/**
 * Instantiate and Invoke Accessors using Nashorn.
 * Evaluate the arguments, which are expected to be JavaScript files
 * that define Composite Accessors.
 *
 * <p>The Nashorn and Cape Code Accessor hosts are similar in that they
 * both use Nashorn as the underlying JavaScript engine.  They also
 * both can invoke JavaScript accessors that use modules defined in
 * $PTII/ptolemy/actor/lib/jjs/modules.</p>
 *
 * <p>The Nashorn Accessor Host differs from the Cape Code Accessor
 * Host in that Cape Code Accessor Host reads in Ptolemy II .xml MoML
 * files and can invoke regular Ptolemy II actors written in Java such
 * a ptolemy.actor.lib.Ramp.  The Nashorn Accessor Host reads in .js
 * files that define CompositeAccessors.  The Nashorn Accessor Host is
 * not intended to invoke regular Ptolemy II actors written in Java
 * and it does not invoke the Ptolemy II actor execution semantics
 * code implemented in Java.</p>
 *
 * <p>Note that by using code generation, Cape Code .xml MoML files
 * can be converted in to .js Composite Accessor files provided that
 * the .xml file only uses JavaScript and JSAccessor actors.</p>
 *
 * <p>The main() method of this class takes the following command
 * line arguments:</p>
 * <dl>
 * <dt><code>-e|--e|-echo|--echo</code></dt>
 * <dd>Echo the command that would be run by hand to replicate the
 * test. This is helpful for use under Ant apply.</dd>
 * <dt><code>-h|--h|-help|--help</code></dt>
 * <dd>Print a usage message</dd>
 * <dt><code>-j|--j|-js|--js filename</code></dt>
 * <dd>Interpret the next argument as the name of a regular
 * JavaScript file to evaluate.</dd>
 * <dt><code>-k|--k|-keepalive|--keepalive</code></dt>
 * <dd>Keep the calling process alive until either a timeout option
 * expires or all instanted accessors have called wrapup.</dd>
 * <dt><code>-timeout|--timeout <i>milliseconds</i></code></dt>
 * <dd>The minimum amount of time the script should run.</dd>
 * <dt><code>-v|--v|-version|--version</code></dt>
 * <dd>Print out the version number</dd>
 * </dl>
 *
 * <p>After the flags, the one or more JavaScript files are present that
 * name either or regular JavaScript files.</p>
 *
 * <p>To run a very simple test:</p>
 * <pre>
 * (cd $PTII/org/terraswarm/accessor/accessors/web; $PTII/bin/ptinvoke ptolemy.actor.lib.jjs.NashornAccessorHostApplication -timeout 10000 -js hosts/nashorn/test/testNashornHost.js)
 * </pre>
 *
 * <p>To run a composite accessor:</p>
 * <pre>
 * (cd $PTII/org/terraswarm/accessor/accessors/web; $PTII/bin/ptinvoke ptolemy.actor.lib.jjs.NashornAccessorHostApplication -timeout 10000 test/auto/RampJSDisplay.js)
 * </pre>
 *
 * <p> The command line syntax is:</p>
 * <pre>
 * java -classpath $PTII ptolemy.actor.lib.jjs.NashornAccessorHostApplication \
 *  [-h|--h|-help|--help] \
 *  [-e|--e|-echo|--echo] \
 *  [-j|--j|-js|--js filename] \
 *  [-timeout|--timeout milliseconds] \
 *  [-v|--v|-version|--version] \
 *  accessorClassName [accessorClassName ...]
 * </pre>
 *
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 11.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class NashornAccessorHostApplication {

    /** Create an orchestrator for a top-level accessor.
     *  @param name The name for the orchestrator.
     *  @return an aorchestrator for the top-level accessor.
     */
    public static ActorSubstitute createOrchestrator(String name) {
        return new ActorSubstitute(name);
    }

    /** Evaluate the files named by the arguments.
     *  @param args An array of one or more file names.  See the class comment for
     *  the syntax.
     *  @return 0 for success, 3 for argument problems.  See main() in commonHost.js.
     *  @exception Throwable If the Nashorn engine cannot be found, if
     *  a file cannot be read or closed.  if evaluateCode() JavaScript
     *  method is not defined or if there is a problem evaluating a
     *  file.
     */
    public static int evaluate(String[] args) throws Throwable {

        // Create a Nashorn script engine, if we don't already have one.
        if (_engine == null) {
            // NOTE: This is somewhat similar to JavaScript.createEngine().
            ScriptEngineManager factory = new ScriptEngineManager();
            _engine = factory.getEngineByName("nashorn");
            if (_engine == null) {
                // Coverity Scan is happier if we check for null here.
                throw new Exception(
                        "Could not get the nashorn engine from the javax.script.ScriptEngineManager."
                                + " Nashorn is present in JDK 1.8 and later.");
            }

            // Load nashornHost.js, which provides top-level functions.
            _engine.eval(FileUtilities.openForReading(
                    "$CLASSPATH/ptolemy/actor/lib/jjs/nashornHost.js", null,
                    null));

            // Create a top-level orchestrator that provides setTimeout(), etc.
            _orchestrator = new ActorSubstitute("main");
            // The following will make setTimeout(), etc., available.
            _engine.put("actor", _orchestrator);
            // Start an event loop to handle all invocations of setTimeout(), etc., in
            // plain JavaScript files and in the accessors.
            _orchestrator.eventLoop();
        }

        // Evaluate the command-line arguments. This will either instantiate and
        // initialize accessors or evaluate specified JavaScript code.
        // This needs to be evaluated in the orchestrator thread.
        _orchestrator.invokeCallback(new Runnable() {
            @Override
            public void run() {
                try {
                    ((Invocable) _engine).invokeFunction(
                            "processCommandLineArguments", (Object) args);
                } catch (NoSuchMethodException | ScriptException e) {
                    System.err
                            .println("NashornAccessorHostApplication.evaluate("
                                    + Arrays.toString(args) + ") failed with: "
                                    + e);
                    e.printStackTrace();
                }
            }
        });
        return 0;
    }

    /** Invoke one or more JavaScript files.
     *  @param args One or more JavaScript files.  See the class
     *  comment for the syntax.
     */
    public static void main(String[] args) {
        try {
            int returnValue = NashornAccessorHostApplication.evaluate(args);
            // If we have an error condition, then call exit() immediately, otherwise wait for
            // the last setTimeout() and/or setInterval to return.
            if (returnValue != 0) {
                StringUtilities.exit(returnValue);
            }
        } catch (Throwable throwable) {
            System.err.println("Command Failed: " + throwable);
            throwable.printStackTrace();
            StringUtilities.exit(1);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                       Private Variables                  ////

    /** JavaScript engine to execute scripts. We use only one and share
     *  it across all scripts and accessor instances created by this process.
     */
    private static ScriptEngine _engine;

    /** The orchestrator that provides the top-level event loop thread. */
    private static ActorSubstitute _orchestrator;

    ///////////////////////////////////////////////////////////////////
    ////                       Inner Classes                      ////

    /** Class that substitute for the JavaScript actor so that setTimeout,
     *  setInterval, and CapeCode modules work in a pure Nashorn host.
     *  This class provides an event loop that invokes callback functions.
     *  To start the event loop, call {@link #eventLoop()}.
     *  To request that a callback be invoked, call {@link #invokeCallback(Runnable)}
     *  from any thread. The argument will be appended to a list of callbacks
     *  to be invoked, and the event loop thread will be notified.
     *  Callbacks will be executed in the same thread as the thread that
     *  calls {@link #eventLoop()}.
     */
    public static class ActorSubstitute implements AccessorOrchestrator {

        /** Construct an actor substitute instance.
         *  @param name The name of the actor substitute instance.
         */
        public ActorSubstitute(String name) {
            _name = name;
        }

        /** Clear the interval with the specified handle, if it
         *  has not already executed.
         *  @param timer The timeout handle.
         *  @exception IllegalActionException If the handle is invalid.
         *  @see #setTimeout(Runnable, int)
         *  @see #setInterval(Runnable, int)
         */
        @Override
        public synchronized void clearInterval(Object timer)
                throws IllegalActionException {
            clearTimeout(timer);
        }

        /** Clear the timeout with the specified handle, if it
         *  has not already executed.
         *  @param timer The timeout handle.
         *  @exception IllegalActionException If the handle is invalid.
         *  @see #setTimeout(Runnable, int)
         *  @see #setInterval(Runnable, int)
         */
        @Override
        public synchronized void clearTimeout(Object timer)
                throws IllegalActionException {
            if (timer instanceof Timer) {
                ((Timer) timer).cancel();
                _pendingTimers.remove(timer);
            } else {
                throw new IllegalActionException(this,
                        "Invalid timer handle: " + timer);
            }
        }

        /** Return a description. */
        @Override
        public String description() throws IllegalActionException {
            return "Orchestrator for executing accessors.";
        }

        /** Report an error. */
        @Override
        public void error(String message) {
            System.err.println(message);
        }

        /** Start an event loop in a new thread that does not end until
         *  {@link #wrapup()} is called.
         */
        public synchronized void eventLoop() {
            // System.out.println(_name + ": **** Creating new event loop thread from thread: " + Thread.currentThread());
            Thread thread = new Thread() {
                @Override
                public void run() {
                    synchronized (ActorSubstitute.this) {
                        while (!_wrapupRequested) {
                            // System.out.println(_name + ": event loop thread: " + Thread.currentThread());
                            // If there are no pending callbacks, then wait for notification.
                            while (_pendingCallbacks.isEmpty()) {
                                // System.out.println(_name + ": waiting for callbacks: " + Thread.currentThread());
                                try {
                                    ActorSubstitute.this.wait();
                                } catch (InterruptedException e) {
                                    // Thread was interrupted. Invoke wrapup and return.
                                    try {
                                        ActorSubstitute.this.wrapup();
                                    } catch (IllegalActionException e1) {
                                        ActorSubstitute.this.error(_name
                                                + ": Event loop thread interrupted.");
                                    }
                                    return;
                                }
                            }
                            // Invoke any pending callback functions.
                            // First, copy the _pendingCallbacks list, because a callback may
                            // trigger additional callbacks (e.g. setTimeout(f, 0)), but those
                            // should not be handled until the _next_ reaction.
                            // Note that we cannot bulk copy the elements of the _pendingCallbacks
                            // queue because then we would have to separately clear it, and
                            // between the last copy and the clear, another element might be added
                            // and then lost.
                            Runnable callback = _pendingCallbacks.poll();
                            List<Runnable> callbacks = new LinkedList<Runnable>();
                            while (callback != null) {
                                callbacks.add(callback);
                                callback = _pendingCallbacks.poll();
                            }
                            for (Runnable callbackFunction : callbacks) {
                                // System.out.println(_name + ": invoking callback: " + Thread.currentThread());
                                try {
                                    callbackFunction.run();
                                } catch (Throwable throwable) {
                                    System.err.println(
                                            "*** Callback function failed: "
                                                    + throwable);
                                    throwable.printStackTrace();
                                    // Do not terminate the event loop. Keep running.
                                }
                            }
                        }
                    }
                }
            };
            thread.start();
        }

        /** Return null. */
        @Override
        public NamedObj getContainer() {
            return null;
        }

        /** Return the name specified in the constructor. */
        @Override
        public String getDisplayName() {
            return _name;
        }

        /** Return the name specified in the constructor. */
        @Override
        public String getFullName() {
            return _name;
        }

        /** Return the name specified in the constructor.
         *  @see #setName(String)
         */
        @Override
        public String getName() {
            return _name;
        }

        /** Return the name specified in the constructor.
         *  @see #setName(String)
         */
        @Override
        public String getName(NamedObj relativeTo)
                throws InvalidStateException {
            return _name;
        }

        @Override
        public synchronized void invokeCallback(Runnable function) {
            _pendingCallbacks.offer(function);
            notifyAll();
        }

        /** Print a message. */
        @Override
        public void log(String message) {
            System.out.println(message);
        }

        /** Set the name.
         *  @param name The name to use in reporting errors.
         *  @see #getName()
         */
        @Override
        public void setName(String name) {
            _name = name;
        }

        /** Specify a function to invoke as a callback periodically with
         *  the specified period (in milliseconds).
         *  @param function The function to invoke.
         *  @param periodMS The period in milliseconds.
         *  @return handle A handle that can be used to cancel the timeout.
         */
        @Override
        public synchronized Timer setInterval(Runnable function, long periodMS) {
            // System.out.println(_name + ": requesting interval: " + periodMS + "ms for " + function + " in thread " + Thread.currentThread());
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    // Cannot run this directly because it will be
                    // invoked in an arbitrary thread, creating race conditions.
                    invokeCallback(function);
                }
            };
            Timer timer = new Timer();
            timer.schedule(task, periodMS, periodMS);
            _pendingTimers.add(timer);
            return timer;
        }

        /** Specify a function to invoke as a callback after the specified
         *  time (in milliseconds) has elapsed.
         *  @param function The function to invoke.
         *  @param timeMS The time in milliseconds.
         *  @return handle A handle that can be used to cancel the timeout.
         */
        @Override
        public synchronized Timer setTimeout(Runnable function, long timeMS) {
            // System.out.println(_name + ": requesting timeout: " + timeMS + "ms for " + function + " in thread " + Thread.currentThread());
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    // Cannot run this directly because it will be
                    // invoked in an arbitrary thread, creating race conditions.
                    // System.out.println(_name + ": queueing callback: " + timeMS + "ms for " + function + " in thread " + Thread.currentThread());
                    invokeCallback(function);
                }
            };
            Timer timer = new Timer();
            timer.schedule(task, timeMS);
            _pendingTimers.add(timer);
            return timer;
        }

        /** Specify a top-level accessor to associate with this orchestrator
         *  and start an event loop to invoke callbacks. This implementation
         *  ignores the accessor argument.
         *  @param accessor A top-level accessor.
         */
        public void setTopLevelAccessor(ScriptObjectMirror accessor) {
            eventLoop();
        }

        /** Stop the event loop, canceling all pending callbacks. */
        @Override
        public synchronized void wrapup() throws IllegalActionException {
            _pendingCallbacks.clear();
            for (Timer timer : _pendingTimers) {
                timer.cancel();
            }
            _pendingTimers.clear();
            _wrapupRequested = true;
            notifyAll();
        }

        ///////////////////////////////////////////////////////////
        ////              Private Variables                    ////

        /** Name of this orchestrator. */
        private String _name;

        /** Queue containing callback functions to be invoked. */
        private ConcurrentLinkedQueue<Runnable> _pendingCallbacks = new ConcurrentLinkedQueue<Runnable>();

        /** Set containing pending timers. */
        private HashSet<Timer> _pendingTimers = new HashSet<Timer>();

        /** Flag indicating that wrapup has been called. */
        private boolean _wrapupRequested;
    }
}
