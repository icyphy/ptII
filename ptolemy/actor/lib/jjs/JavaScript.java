/* Execute a script in JavaScript using Nashorn.

   Copyright (c) 2014-2017 The Regents of the University of California.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.TypeAttribute;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.conversions.json.TokenToJSON;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Constants;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.graph.Inequality;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.SingletonAttribute;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.CancelException;
import ptolemy.util.FileUtilities;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// JavaScript

/**
   An actor whose functionality is given in JavaScript using the accessor
   interface defined at
   <a href="https://accessors.org">https://accessors.org</a>.
   Refer to that page for complete documentation of the
   functions and modules that are provided to the script.
   <p>
   The script defines one or more functions that configure this actor
   with ports and parameters, initialize the actor,
   perform runtime functions such as reacting to inputs and producing outputs,
   and perform finalization (wrapup) functions. The script may be provided
   as the textual value of the <i>script</i> parameter, or as an input
   on the <i>script</i> port. You can add to the script or modify
   function definitions on each firing.
   </p><p>
   To use this actor, specify a script. Define an exports.setup() function
   that declares inputs, outputs, and parameters.
   </p>
   <p>
   Your script can define zero or more of the following functions:</p>
   <ul>
   <li> <b>exports.setup</b>. This function is invoked when the script parameter
   is first set and whenever the script parameter is updated. This function can
   be used to configure this actor with input and output ports
   and parameters.  For example,
   <pre>
     exports.setup = function() {
         this.input('foo', {'type':'string'});
     }
   </pre>
   will create an input port named "foo" (if one does not already exist), and set
   its type to "string", possibly overriding any previously set data type.
   The methods that are particularly useful to use in setup are input, output,
   parameter, instantiate, and connect.
   </li>
   <li> <b>exports.initialize</b>. This function is invoked each time this actor
   is initialized. This function should not read inputs or produce outputs.
   </li>
   <li> <b>exports.fire</b>. This function is invoked each time this actor fires.
   It can read inputs using get() and write outputs using send().
   This actor will consume at most one input token from each input port on
   each firing, if one is available. Any number of calls to get() during the
   firing will return the same consumed value, or will return null if there
   is no available input on that firing.  If you want it to instead return
   a previously read input, then mark the port persistent by giving it a
   <i>value</i> option when you call input() in setup().
   This provides a default value and makes any newly provided values
   persistent.
   </li>
   <li> <b>exports.wrapup</b>. This function is invoked at the end of execution of
   of the model. It can read parameters, but normally should not
   read inputs nor write outputs.
   </li>
   </ul>
   These functions are fields of the exports object, as usual JavaScript CommonJS modules.
   For example, to define the fire function, specify code like this:
   <pre>
      exports.fire = function () {... function body ...};
   </pre>
   Alternatively, you can do
   <pre>
      var fire = function() {... function body ...};
      exports.fire = fire;
   </pre>
   When these functions are invoked, 'this' will be bound to the accessor instance.
   <p>
   Your script may also register <b>input handler</b> functions by invoking
   <pre>
      var handle = this.addInputHandler(portName, function);
   </pre>
   <p>Normally you would do this in initialize().
   The returned handle can be used to call this.removeInputHandler().
   Handlers will be automatically
   unregistered upon wrapup(), so unless you want to cancel a handler earlier,
   you do not need to explicitly unregister a handler.
   </p><p>
   The function specified as the input handler
   will be invoked whenever the port receives a new input.
   Note that the fire() function, if defined, will also be invoked (after the
   specified function) and will see
   the same input. If the specified function is null, then only the fire() function
   will be invoked.
   If the portName is null, then the handler will be invoked whenever
   a new input arrives on any input, after which the fire function will
   be invoked, if it exists.
   </p>
   <p>
   Often you can leave the types of input and output ports unspecified.
   Sometimes, however, you will need to explicitly set the types
   of the output ports. You can do this by specifying a 'type'
   option to the input() function in setup().
   This implementation extends the accessor interface definition by allowing
   any Ptolemy II type to be specified.
   Keep in mind, however, that if you specify Ptolemy II types that are
   not also accessor types, then your script will likely not work in some
   other accessor host.
   </p>
   <p>
   You may also need to set the type of input ports. Usually, forward
   type inference will work, and the type of the input port will be based
   on the source of data. However,
   if the input comes from an output port whose output is undefined,
   such as JSONToToken, then you may want to
   enable backward type inference, and specify here the type of input
   that your script requires. Again, you do this with a 'type' option
   to the input() function in setup().
   </p>
   <p>
   The accessor instance (the value of 'this' inside setup(), initialize(),
   fire(), and wrapup()) has the following functions,  at least:</p>
   <ul>
   <li> addInputHandler(function, input): Specify a function to invoke when the input
        with name input (a string) receives a new input value. Note that after that
        function is invoked, the accessor's fire() function will also be invoked,
        if it is defined. If the specified function is null, then only the fire()
        function will be invoked. If the input argument is null or omitted, then
        the specified function will be invoked when any new input arrives to the
        accessor. This function returns a handle that can be used to call removeInputHandler().</li>
   <li> get(portName): get an input from a port on channel 0.</li>
   <li> getParameter(parameterName): get a value from a parameter.</li>
   <li> removeInputHandler(handle): Remove the callback function with the specified handle
        (returned by addInputHandler()). </li>
   <li> send(portName, value): send a value to an output port on channel 0</li>
   <li> setParameter(parameterName, value): set the value of a parameter of this JavaScript actor. </li>
   </ul>
   In addition, there are some top-level functions provided (which do not require a this prefix):
   <ul>
   <li> alert(string): pop up a dialog with the specified message.</li>
   <li> clearInterval(int): clear an interval with the specified handle.</li>
   <li> clearTimeout(int): clear a timeout with the specified handle.</li>
   <li> error(string): send a message to error port, or throw an exception if the error port is not connected.</li>
   <li> httpRequest(url, method, properties, body, timeout): HTTP request (GET, POST, PUT, etc.)</li>
   <li> localHostAddress(): If not in restricted mode, return the local host IP address as a string. </li>
   <li> print(string): print the specified string to the console (standard out).</li>
   <li> readURL(string): read the specified URL and return its contents as a string (HTTP GET).</li>
   <li> require(string): load and return a CommonJS module by name. See
        <a href="https://accessors.org">https://accessors.org</a> for
        supported modules. See
        <a href="http://wiki.commonjs.org/wiki/Modules">http://wiki.commonjs.org/wiki/Modules</a>
        for what a CommonJS module is.</li>
   <li> setInterval(function, int): set the function to execute after specified time and then periodically and return handle.</li>
   <li> setTimeout(function, int): set the function to execute after specified time and return handle.</li>
   </ul>
   <p>
   Note that get() may be called within a JavaScript callback function. In that case,
   if the callback function is invoked during the firing of this actor, then the get()
   will return immediately. Otherwise, the get() method will request a firing of this
   actor at the current time and block the JavaScript thread until this actor is in
   that firing.  This way, this actor ensures that get() reads a proper input.
   Note that although blocking JavaScript functions is not normally done, this actor
   has its own JavaScript engine, so no other JavaScript anywhere in the model will be
   affected. Those JavaScript threads are not blocked.</p>
   <p>
   The following example script calculates the factorial of the input.</p>
   <pre>
   exports.setup = function() {
       this.input('input', {'type':'int'});
       this.output('output', {'type':'int'});
   }
   exports.fire = function() {
       var value = this.get('input');
       if (value &lt; 0) {
           error("Input must be greater than or equal to 0.");
       } else {
           var total = 1;
           while (value &gt; 1) {
               total *= value;
               value--;
           }
           this.send('output', total);
       }
   }
   </pre>
   <p>
   Your script may also store values from one firing to the next, or from
   initialization to firing.  For example,</p>
   <pre>
   exports.setup = function() {
       this.output('output', {'type':'int'});
   }
   var init;
   exports.initialize() = function() {
       init = 0;
   }
   exports.fire = function() {
       init = init + 1;
       this.send('output', init);
   }
   </pre>
   <p>will send a count of firings to the output named "output".</p>
   <p>
   In addition, the symbols "actor" and "accessor" are defined to be the instance of
   this actor. In JavaScript, you can invoke methods on it.
   (Note that in an accessor, which is implemented by a subclass of this
   JavaScript actor, invocation of these functions is blocked for security reasons.)
   For example, the JavaScript</p>
   <pre>
   actor.toplevel().getName();
   </pre>
   <p>will return the name of the top-level composite actor containing
   this actor.</p>
   <p>
   This actor can be used in any Ptolemy II model and can interact with native
   Ptolemy II actors through its ports.
   However, not all Ptolemy II data types translate naturally to
   JavaScript types. Simple types will "just work."
   This actor converts Ptolemy types int,
   double, string, and boolean to and from equivalent JavaScript
   types when sending and getting to and from ports.
   In addition, arrays at input ports are converted to native
   JavaScript arrays. When sending a JavaScript array to an output
   port, it will be converted into a Ptolemy II array, but keep in
   mind that Ptolemy II arrays have a single element type, namely
   a type that every element can be converted to. So, for example,
   sending the JavaScript array [1, 2, "foo"] to an output port
   will result in the Ptolemy II array {"1", "2", "foo"}, an array of strings.
   If you wish to send a JavaScript array (or any other JavaScript
   object) without modifying it, wrap it in an ObjectToken, as
   in this example:</p>
   <pre>
   var ObjectToken = Java.type('ptolemy.data.ObjectToken');
   exports.fire = function() {
      var token = new ObjectToken([1, 2, 'foo']);
      this.send('output', token);
   }
   </pre>
   <p>The type of the output port will need to be set to object or general.
   If you send this to another JavaScript actor, that actor can
   retrieve the original JavaScript object as follows:</p>
   <pre>
   var ObjectToken = Java.type('ptolemy.data.ObjectToken');
   exports.fire = function() {
      var token = this.get('input');
      var array = token.getValue();
      ... operate on array, which is the original [1, 2, 'foo'] ...
   }
   </pre>
   <p>
   Ptolemy II records are also converted into
   JavaScript objects, and JavaScript objects with enumerable
   properties into records.
   When converting a JavaScript object to a record, each enumerable
   property (own or inherited) is included as a field in the resulting
   record, with the value of the field is converted in the same manner.
   To send a JavaScript object without any conversion, wrap it in an
   ObjectToken as with the array example above.
   </p><p>
   These automatic conversions do not cover all cases of interest.
   For example, if you provide inputs to this JavaScript actor that
   cannot be converted, the script will see the
   corresponding Token object.  For example, if you send
   a number of type long to an input of a JavaScript actor,
   the script (inside a fire() function):</p>
   <pre>
      var value = this.get('input');
      print(value.getClass().toString());
   </pre>
   <p>will print on standard out the string</p>
   <pre>
      "class ptolemy.data.LongToken"
   </pre>
   <p>JavaScript does not have a long data type (as of this writing), so instead the
   get() call returns a JavaScript Object wrapping the Ptolemy II
   LongToken object. You can then invoke methods on that token,
   such as getClass(), as done above.</p>
   <p>
   When sending tokens using send(), you can explicitly prevent any conversions
   from occurring by creating a Ptolemy II token explicitly and sending that.
   For example, the JavaScript nested array [[1, 2], [3, 4]] will be automatically
   converted into a Ptolemy II array of arrays {{1,2}, {3,4}}. If instead you want
   to send a Ptolemy II integer matrix [1,2;3,4], you can do this:</p>
   <pre>
   var IntMatrixToken = Java.type('ptolemy.data.IntMatrixToken');
   exports.fire = function() {
      var token = new IntMatrixToken([[1, 2], [3, 4]]);
      this.send('output', token);
   }
   </pre>
   <p>
   Scripts can instantiate Java classes and invoke methods on them.
   For example, the following script will build a simple Ptolemy II model
   and execute it each time this JavaScript actor fires.
   <pre>
   var Ramp = Java.type('ptolemy.actor.lib.Ramp');
   var FileWriter = Java.type('ptolemy.actor.lib.FileWriter');
   var SDFDirector = Java.type('ptolemy.domains.sdf.kernel.SDFDirector');
   var TypedCompositeActor = Java.type('ptolemy.actor.TypedCompositeActor');
   var Manager = Java.type('ptolemy.actor.Manager');

   var toplevel = new TypedCompositeActor();
   var ramp = new Ramp(toplevel, "ramp");
   var writer = new FileWriter(toplevel, "writer");

   toplevel.connect(ramp.output, writer.input);

   var director = new SDFDirector(toplevel, "SDFDirector");
   director.getAttribute('iterations').setExpression("10");
   var manager = new Manager();
   toplevel.setManager(manager);

   exports.fire = function() {
      manager.execute();
   }
   </pre>
   <p>You can even send this model out on an output port.
   For example,</p>
   <pre>
   exports.fire = function() {
      this.send('output', toplevel);
   }
   </pre>
   where "output" is the name of the output port. Note that the manager
   does not get included with the model, so the recipient will need to
   associate a new manager to be able to execute the model.
   Note further that you may want to declare the type of the output
   to be 'actor', a Ptolemy II type.
   <p>
   Subclasses of this actor may put it in "restricted" mode, which
   limits the functionality as follows:</p>
   <ul>
   <li> The "actor" variable (referring to this instance of the actor) provides limited capabilities.</li>
   <li> The localHostAddress() function throws an error.</li>
   <li> The readURL and httpRequest function only support the HTTP protocol (in particular,
   they do not support the "file" protocol, and hence cannot access local files).</li>
   </ul>
   <p>
   For debugging, it can be useful to right click on this actor and select Listen to Actor.
   Refer to <a href="https://accessors.org">https://accessors.org</a>
   for a complete definition of the available functionality. For example,
   it is explained there how to create composite accessors, which instantiate and connect
   multiple subaccessors within this one.
   </p>

   @author Edward A. Lee
   @version $Id$
   @since Ptolemy II 10.0
   @Pt.ProposedRating Yellow (eal)
   @Pt.AcceptedRating Red (bilung)
 */
public class JavaScript extends TypedAtomicActor implements AccessorOrchestrator {

    /** Construct an actor with the given container and name.
     *  In addition to invoking the base class constructors, construct
     *  the <i>error</i> port and the <i>script</i> port parameter.
     *  Initialize <i>script</i> to a block of JavaScript.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public JavaScript(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // Initialize in alphabetical order.

        // Create an error port.
        error = new TypedIOPort(this, "error", false, true);
        error.setTypeEquals(BaseType.STRING);
        StringAttribute cardinal = new StringAttribute(error, "_cardinal");
        cardinal.setExpression("SOUTH");
        SingletonParameter showName = new SingletonParameter(error, "_showName");
        showName.setExpression("true");
        showName.setPersistent(false);

        // Create the script parameter and input port.
        script = new PortParameter(this, "script");
        script.setStringMode(true);
        // We don't want $param substitution in the script.
        // Causes no end of headaches, and anyway, the script should
        // reference parameters using getParameter('name');
        script.setSuppressVariableSubstitution(true);

        ParameterPort scriptIn = script.getPort();
        cardinal = new StringAttribute(scriptIn, "_cardinal");
        cardinal.setExpression("SOUTH");
        showName = new SingletonParameter(scriptIn, "_showName");
        showName.setExpression("true");
        showName.setPersistent(false);

        // initialize the script to provide an empty template:
        script.setToken(_INITIAL_SCRIPT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Output port on which to produce a message when an error occurs
     *  when executing this actor. Note that if nothing is
     *  connected to this port, then an error will cause this
     *  JavaScript actor to throw an exception. Otherwise, a
     *  description of the error will be produced on this port and the
     *  actor will continue executing. Note that any errors that occur
     *  during loading the script or invoking the initialize() or
     *  wrapup() functions always result in an exception, since it
     *  makes no sense to produce an output during those phases of
     *  execution.
     */
    public TypedIOPort error;

    /** The script defining the behavior of this actor. */
    public PortParameter script;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute, and if the attribute is the
     *  script parameter, and the script parameter possibly contains a
     *  'setup' function, then evaluate that function. This means that as
     *  soon as this script parameter is set, any ports that are created
     *  in setup will appear. Note that the JavaScript engine that evaluates
     *  setup is discarded, and a new one is created when the model executes.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If evaluating the script fails.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == script) {
            // NOTE: The try-catch is a bad idea: During copying, exceptions
            // occur during MoMLVariableCheck to find out whether a variable
            // is defined. This causes errors to be reported that should not
            // be reported.
            /*
            try {
                _createEngineAndEvaluateSetup();
            } catch (Exception e) {
                // Turn exceptions into warnings.
                // Otherwise, Vergil may refuse to instantiate the actor
                // and novice users will loose data.
                try {
                    // FIXME: For some reason, this gets invoked twice!
                    MessageHandler.warning("Failed to evaluate script.", e);
                } catch (CancelException e1) {
                    throw new IllegalActionException(this, e, "Cancelled");
                }
            }
            */
            _createEngineAndEvaluateSetup();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Specify author information to appear in the documentation for this actor.
     *  @param author Author information to appear in documentation.
     */
    public void author(String author) {
        // Use a change request so as to not create dependencies on vergil here.
        StringBuffer moml = new StringBuffer(
                "<property name=\"documentation\" class=\"ptolemy.vergil.basic.DocAttribute\">");
        moml.append("<property name=\"author\" class=\"ptolemy.kernel.util.StringAttribute\" value=\"");
        moml.append(StringUtilities.escapeForXML(author));
        moml.append("\"></property></property>");
        MoMLChangeRequest request = new MoMLChangeRequest(this, this,
                moml.toString());
        requestChange(request);
    }

    /** Clear the interval with the specified handle, if it
     *  has not already executed.
     *  @param handle The timeout handle.
     *  @see #setTimeout(Runnable, int)
     *  @see #setInterval(Runnable, int)
     */
    public synchronized void clearInterval(Object handle) {
        // NOTE: The handle for this timeout remains in the
        // _pendingTimeoutIDs map, but it is more efficient to remove
        // it from that map when the firing occurs.
        _pendingTimeoutFunctions.remove(handle);
    }

    /** Clear the timeout with the specified handle, if it
     *  has not already executed.
     *  @param handle The timeout handle.
     *  @see #setTimeout(Runnable, int)
     *  @see #setInterval(Runnable, int)
     */
    public synchronized void clearTimeout(Object handle) {
        // NOTE: The handle for this timeout remains in the
        // _pendingTimeoutIDs map, but it is more efficient to remove
        // it from that map when the firing occurs.
        _pendingTimeoutFunctions.remove(handle);
    }

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        JavaScript newObject = (JavaScript) super.clone(workspace);
        ScriptEngineManager factory = new ScriptEngineManager();
        newObject._engine = factory.getEngineByName("nashorn");
        if (newObject._engine == null) {
            // Coverity Scan is happier if we check for null here.
            throw new CloneNotSupportedException(
                    "Could not get the nashorn engine from the javax.script.ScriptEngineManager.  Nashorn present in JDK 1.8 and later.");
        }
        newObject._exports = null;
        newObject._instance = null;
        newObject._pendingCallbacks = new ConcurrentLinkedQueue<Runnable>();
        newObject._pendingTimeoutFunctions = new HashMap<Integer, Runnable>();
        newObject._pendingTimeoutIDs = new HashMap<Time, Queue<Integer>>();
        newObject._proxies = null;
        newObject._proxiesByName = null;
        return newObject;
    }

    /** Create a script engine and initialize it.
     *  @param actor The JavaScript actor that is to use the script
     *  engine.  If the actor parameter is null, then the restricted
     *  parameter must be false.  The actor parameter is also used for
     *  exception handling.  The JavaScriptApplication class typically passes
     *  a null value for this parameter.
     *  @param debugging True if the _debug JavaScript variable should be set to true.
     *  @param restricted True if script engine should be restricted
     *  so that it can execute unrusted code.  The default is typically false.
     *  @return The JavaScript engine.
     *  @exception IllegalActionException If the "nashorn" JavaScript
     *  engine cannot be found.  The Nashorn engine is only present in
     *  JDK 1.8 and later.
     */
    public static ScriptEngine createEngine(JavaScript actor, boolean debugging, boolean restricted)
        throws IllegalActionException {
        ScriptEngineManager factory = new ScriptEngineManager();
        // Create a Nashorn script engine
        ScriptEngine engine = factory.getEngineByName("nashorn");
        if (engine == null) {
            // Coverity Scan is happier if we check for null here.
            throw new IllegalActionException(
                    actor,
                    "Could not get the nashorn engine from the javax.script.ScriptEngineManager.  Nashorn present in JDK 1.8 and later.");
        }
        /* FIXME: The following should intercept errors, but if doesn't!
         * Perhaps Thread.setUncaughtExceptionHandler()? How to get the thread?
         * or Thread.setDefaultUncaughtExceptionHandler().
         * This should be a top-level Ptolemy II thing...
        ScriptContext context = engine.getContext();
        context.setErrorWriter(new StringWriter() {
            @Override
            public void close() throws IOException {
                super.close();
                // FIXME: Request a firing that can then throw an exception so
                // error port is handled correctly.
                MessageHandler.error(toString());
            }
        });
         */
        if (debugging) {
            // Set a global variable for debugging.
            engine.put("_debug", true);
        } else {
            engine.put("_debug", false);
        }
        
        // Define the actor and accessor variables.
        // capeCodeHost.js refers to actor at eval-time, so set it now.
        if (!restricted) {
            engine.put("accessor", actor);
            engine.put("actor", actor);
        } else {
            if (actor == null) {
                throw new IllegalArgumentException("Restricted JavaScript sandboxes only supported with JavaScript container actors.");
            }
            RestrictedJavaScriptInterface restrictedInterface = new RestrictedJavaScriptInterface(
                    actor);
            engine.put("accessor", restrictedInterface);
            engine.put("actor", restrictedInterface);
        }

        // First load the Nashorn host, which defines functions that are independent
        // of Ptolemy II.
        try {
            engine.eval(FileUtilities.openForReading(
                    "$CLASSPATH/ptolemy/actor/lib/jjs/nashornHost.js", null,
                    null));
        } catch (Throwable throwable) {
            throw new IllegalActionException(actor, throwable,
                    "Failed to load nashornHost.js");
        }

        try {
            engine.eval(FileUtilities.openForReading(
                    "$CLASSPATH/ptolemy/actor/lib/jjs/capeCodeHost.js", null,
                    null));
        } catch (Throwable throwable) {
            throw new IllegalActionException(actor, throwable,
                    "Failed to load capeCodeHost.js");
        }

        String localFunctionsPath = "$CLASSPATH/ptolemy/actor/lib/jjs/localFunctions.js";
        try {
            engine.eval(FileUtilities.openForReading(localFunctionsPath, null, null));
        } catch (Throwable throwable) {
            // eval() can throw a ClassNotFoundException if a Ptolemy class is not found.

            if (throwable instanceof ClassNotFoundException) {
                // FIXME: Temporary code (2015-08-15)

                // Attempting to debug why, after 348 tests, these tests
                // can't find the Ptolemy classes:

                // ptolemy.vergil.basic.export.test.junit.ExportModelJUnitTest.[348] ptolemy/demo/Robot/RandomWalkIntruder.xml
                // ptolemy.vergil.basic.export.test.junit.ExportModelJUnitTest.[350] ptolemy/demo/Robot/RobotChase.xml
                // ptolemy.vergil.basic.export.test.junit.ExportModelJUnitTest.[351] ptolemy/demo/Robot/RobotCollaborativeChase.xml
                // ptolemy.vergil.basic.export.test.junit.ExportModelJUnitTest.[353] ptolemy/demo/Robot/RobotMonitor.xml
                // ptolemy.vergil.basic.export.test.junit.ExportModelJUnitTest.[360] ptolemy/demo/Robot/SmartIntruder6Teams.xml

                String message =  "Parsing "
                    + localFunctionsPath
                    + " resulted in a ClassNotFoundException?"
                    + " Checking ClassLoaders: "
                    + " ClassLoader.getSystemClassLoader(): "
                    + ClassLoader.getSystemClassLoader()
                    + " Thread.currentThread().getContextClassLoader(): "
                    + Thread.currentThread().getContextClassLoader();
                Class<?> clazz = null;
                try {
                    clazz = Class.forName("ptolemy.data.ArrayToken");
                } catch (ClassNotFoundException ex) {
                    throw new IllegalActionException(actor, throwable, message
                            + "???? Failed to get ptolemy.data.ArrayToken?"
                            + ex);
                }
                throw new IllegalActionException(actor, throwable, message
                        + " Class.forName(\"ptolemy.data.ArrayToken\"): "
                        + clazz);
                // FIXME: End of temporary code.
            } else {
                throw new IllegalActionException(actor, throwable,
                        "Failed to load " + localFunctionsPath + ".");
            }
        }

        return engine;
    }


    /** Declare that any output that is marked as spontanous does does
     *  not depend on the input in a firing.
     *  @exception IllegalActionException If the causality interface
     *  cannot be computed.
     */
    @Override
    public void declareDelayDependency() throws IllegalActionException {
        for (IOPort output : outputPortList()) {
            SingletonParameter spontaneity = (SingletonParameter)output.getAttribute(_SPONTANEOUS);
            if (spontaneity != null) {
                Token token = spontaneity.getToken();
                if (token instanceof BooleanToken) {
                    if (((BooleanToken)token).booleanValue()) {
                        for (IOPort input : inputPortList()) {
                            _declareDelayDependency(input, output, 0.0);
                        }
                    }
                }
            }
        }
    }

    /** Specify a description to appear in the documentation for this actor.
     *  The assume format for documentation is HTML.
     *  @param description A description to appear in documentation.
     */
    public void description(String description) {
        description(description, null);
    }

    /** Specify a description to appear in the documentation for this actor.
     *  The recommended format for documentation is HTML, Markdown, or plain text.
     *  @param description A description to appear in documentation.
     *  @param type The type, which should be one of "text/html" (the default if null
     *   is given), "text/markdown",  or "text/plain".
     */
    public void description(String description, String type) {
        // FIXME: The type is currently ignored.
        // Use a change request so as to not create dependencies on vergil here.
        StringBuffer moml = new StringBuffer(
                "<property name=\"documentation\" class=\"ptolemy.vergil.basic.DocAttribute\">");
        moml.append("<property name=\"description\" class=\"ptolemy.kernel.util.StringAttribute\" value=\"");
        moml.append(StringUtilities.escapeForXML(description));
        moml.append("\"></property></property>");
        MoMLChangeRequest request = new MoMLChangeRequest(this, this,
                moml.toString());
        requestChange(request);
    }

    /** If the model is executing and the error port is connected, then send the
     *  message to the error port; otherwise, use the MessageHandler to display the
     *  error. Note that this should not be used for fatal errors, because it returns.
     *  For fatal errors, a script should throw an exception.
     *  In addition, if debugging is turned on, then send the specified message to the
     *  _debug() method, and otherwise send it out to stderr.
     *  @param message The message
     */
    public void error(String message) {
        if (_debugging) {
            _debug(message);
        }
        try {
            // FIXME: Might not be in a phase where anyone will receive this
            // error message. It might be after the last firing, and just before
            // wrapup() sets _executing = false. In this case, the error message
            // will never appear anywhere!
            if (_executing && error.getWidth() > 0) {
                error.send(0, new StringToken(message));
                return;
            }
        } catch (Throwable e) {
            // Sending to the error port fails.
            // Revert to directly reporting.
        }
        MessageHandler.error(getName() + ": " + message);
    }

    /** If the model is executing and the error port is connected, then send the
     *  message to the error port; otherwise, use the MessageHandler to display the
     *  error. Note that this should not be used for fatal errors, because it returns.
     *  For fatal errors, a script should throw an exception.
     *  In addition, if debugging is turned on, then send the specified message to the
     *  _debug() method, and otherwise send it out to stderr.
     *  @param message The message
     *  @param throwable The throwable
     */
    public void error(String message, Throwable throwable) {
        if (_debugging) {
            _debug(message + ": " + throwable);
        }
        try {
            // FIXME: Might not be in a phase where anyone will receive this
            // error message. It might be after the last firing, and just before
            // wrapup() sets _executing = false. In this case, the error message
            // will never appear anywhere!
            if (_executing && error.getWidth() > 0) {
                error.send(0, new StringToken(message + ": " + throwable));
                return;
            }
        } catch (Throwable e) {
            // Sending to the error port fails.
            // Revert to directly reporting.
        }
        MessageHandler.error(getName() + ": " + message, throwable);
    }

    /** Escape a string for use within JavaScript.
     *  @param unescapedString The unescaped string to be escaped for use in JavaScript.
     *  @return The encodedString
     *  @exception IllegalActionException If the string cannot be escaped.
     */
    public String escapeForJavaScript(String unescapedString) throws
        IllegalActionException {
        try {
            // escape() is deprecated, but encodeURIComponent() does not encode -_.!~*'()
            // https://stackoverflow.com/questions/75980/when-are-you-supposed-to-use-escape-instead-of-encodeuri-encodeuricomponent
            return (String) ((Invocable)_engine).invokeFunction("escape", unescapedString);
        } catch (Throwable throwable) {
            throw new IllegalActionException(this, throwable, "Failed to escape: \""
                                             + unescapedString + "\".");
        }
    }                              

    /** Produce any pending outputs specified by send() since the last firing,
     *  invoke any timer tasks that match the current time, and invoke the
     *  fire function. Specifically:
     *  <ol>
     *
     *  <li> First, if there is a new token on the script input port,
     *  then evaluate the script specified on that port. Any
     *  previously defined methods such as fire() will be replaced if
     *  the new script has a replacement, and preserved otherwise.  If
     *  the new script has an initialize() method, that method will
     *  not be invoked until the next time this actor is
     *  initialized.</li>
     *
     *  <li> Next, send any outputs that have been queued to be sent
     *  by calling send() from outside any firing of this JavaScript
     *  actor.</li>
     *
     *  <li> Next, read all available inputs, recording their values
     *  for subsequent calls to get().</li>
     *
     *  <li> Next, invoke any pending timer tasks whose timing matches
     *  the current time.</li>
     *
     *  <li> After updating all the inputs, for each input port that
     *  had a new token on any channel and for which there is a
     *  handler function bound to that port via the addInputHandler()
     *  method, invoke that function.  Such a function will be invoked
     *  in the order that the inputs are defined in the accessor.</li>
     *
     *  <li> Next, if the current script has a fire() function, then
     *  invoke it.</li>
     *  </ol>
     *
     *  @exception IllegalActionException If calling send() or super.fire()
     *  throws it.
     */
    @Override
    public void fire() throws IllegalActionException {
        // This method needs to be synchronized because Nashorn executes
        // the JavaScript functions in a separate thread, for some reason.
        // Need to ensure this is atomic w.r.t. callbacks.
        super.fire();

        if (_debugging) {
            // Set a global variable for debugging.
            _engine.put("_debug", true);
        } else {
            _engine.put("_debug", false);
        }

        // If there is an input script, evaluate that script.
        // Note that if this redefines the initialize() method, it will
        // have no effect until the next run unless the script actually invokes it.
        ParameterPort scriptPort = script.getPort();
        if (scriptPort.getWidth() > 0 && scriptPort.hasToken(0)) {
            script.update();
            String scriptValue = ((StringToken) script.getToken())
                    .stringValue();
            try {
                _engine.put("_topLevelCode", scriptValue);
                _instance = ((Invocable)_engine).invokeFunction(
                        "evaluateCode", getName(), scriptValue);
                _exports = ((Map)_instance).get("exports");
            } catch (Throwable throwable) {
                if (error.getWidth() > 0) {
                    error.send(0, new StringToken(throwable.getMessage()));
                } else {
                    throw new IllegalActionException(this, throwable,
                            "Loading input script triggers an exception.");
                }
            }
            if (_debugging) {
                _debug("Evaluated new script on input port: " + scriptValue);
            }
        }

        // Update port parameters.
        for (PortParameter portParameter : attributeList(PortParameter.class)) {
            if (portParameter == script) {
                continue;
            }
            PortOrParameterProxy proxy = _proxies.get(portParameter.getPort());
            if (portParameter.update()) {
                Token token = portParameter.getToken();
                _provideInput(portParameter.getName(), token);
                if (_debugging) {
                    _debug("Received new input on " + portParameter.getName()
                            + " with value " + token);
                }
            } else if (proxy._localInputTokens != null
                    && proxy._localInputTokens.size() > 0) {
                // There is no new input, but there is a locally provided one.
                Token token = proxy._localInputTokens.remove(0);
                portParameter.setCurrentValue(token);
                _provideInput(portParameter.getName(), token);
            }
        }

        // Read all the available inputs.
        for (IOPort input : inputPortList()) {
            // Skip the scriptIn input.
            if (input == script.getPort()) {
                continue;
            }
            // Skip ParameterPorts, as those are handled by the update() call above.
            if (input instanceof ParameterPort) {
                continue;
            }
            PortOrParameterProxy proxy = _proxies.get(input);
            for (int i = 0; i < input.getWidth(); i++) {
                if (input.hasToken(i)) {
                    Token token = input.get(i);
                    _provideInput(input.getName(), token);
                    if (_debugging) {
                        _debug("Received new input on " + input.getName()
                                + " with value " + token);
                    }
                } else if (proxy._localInputTokens != null
                        && proxy._localInputTokens.size() > 0) {
                    // There is no external input, but there is one
                    // sent by the accessor to itself.
                    Token token = proxy._localInputTokens.remove(0);
                    _provideInput(input.getName(), token);
                } else {
                    // There is no input. Notify by providing an input of null.
                    // Per the current specification for accessors, if the port
                    // has a default value (it is a PortParameter, handled above),
                    // then sending null will trigger input handlers and get() will
                    // return null. However, in this case, the port presumably has
                    // no default value (or this would be a PortParameter), and sending
                    // null will be interpreted by commonHost.provideInput() as an
                    // indication that there is no input, no input handler will
                    // be invoked, and calls to get() will return null.
                    _provideInput(input.getName(), null);
                }
            }
            // Even if the input width is zero, there might be a token
            // that the actor has sent to itself or has set a default value.
            if (input.getWidth() == 0) {
                if (proxy._localInputTokens != null
                        && proxy._localInputTokens.size() > 0) {
                    Token token = proxy._localInputTokens.remove(0);
                    _provideInput(input.getName(), token);
                } else {
                    // The input is always null if there is no local token
                    // and the width is zero.
                    _provideInput(input.getName(), null);
                }
            }
        }

        // Mark that we are in the fire() method, enabling outputs to be
        // sent immediately.
        _inFire = true;
        try {
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
            synchronized(this) {
                // Now, holding a lock on this actor, invoke the pending callbacks.
                for (Runnable callbackFunction : callbacks) {
                    callbackFunction.run();
                }
                // Handle timeout requests that match the current time.
                if (_pendingTimeoutIDs.size() > 0) {
                    // If current time matches pending timeout requests, invoke them.
                    Time currentTime = getDirector().getModelTime();
                    Queue<Integer> ids = _pendingTimeoutIDs.get(currentTime);
                    if (ids != null) {
                        // Copy the list, because setInterval reschedules a firing
                        // and will cause a concurrent modification exception otherwise.
                        List<Integer> copy = new LinkedList<Integer>(ids);
                        for (Integer id : copy) {
                            Runnable function = _pendingTimeoutFunctions.get(id);
                            if (function != null) {
                                // Previously, we removed the id _before_ firing the function
                                // because it may reschedule itself using the same id.
                                // But that's not right, because the function may or may not
                                // cancel a setInterval, and if we remove the id here, we
                                // have no way to tell in _runThenReschedule
                                // whether it cancelled it. So don't do this now:
                                // _pendingTimeoutFunctions.remove(id);
                                // Instead, to prevent a memory leak, _runThenReschedule
                                // will set the following flag to true to prevent removal
                                // of the function.
                                _removePendingIntervalFunction = true;

                                // FIXME: Is it OK to run this holding the synchronization lock?
                                // Probably should replace all the synchronized keywords here with
                                // use of a ReentrantLock, and release the lock before making any
                                // callback calls.
                                function.run();

                                if (_debugging) {
                                    _debug("Invoked timeout function.");
                                }
                                if (_removePendingIntervalFunction) {
                                    _pendingTimeoutFunctions.remove(id);
                                }
                            }
                            // Remove this ID, and this ID only, from the pending IDs
                            // at the current time.  The callback may have inserted
                            // additional ids at the current time.
                            ids.remove(id);
                        }
                        if (ids.isEmpty()) {
                            _pendingTimeoutIDs.remove(currentTime);
                        }
                    }
                }

                // Invoke react.
                _invokeMethodInContext(_instance, "react");
                if (_debugging) {
                    _debug("Invoked react function.");
                }
            }
        } finally {
            _inFire = false;
        }
    }

    /** Return the string contents of the file from the classpath.
     *  @param path The location.  This is used in localFunctions.js.
     *  The path should be a relative path.
     *  @return The contents as a string, assuming the default encoding of
     *   this JVM (probably utf-8).
     *  @exception IOException If the file cannot be read.
     */
    public static String getFileFromClasspathAsString(String path) throws IOException {
        URL url = FileUtilities.nameToURL(path, null, null);
        byte[] encoded = FileUtilities.binaryReadURLToByteArray(url);
        return new String(encoded, Charset.defaultCharset());
    }
    /** If this actor has been initialized, return the JavaScript engine,
     *  otherwise return null.
     *  @return The JavaScript engine for this actor.
     */
    public ScriptEngine getEngine() {
        return _engine;
    }

    /** Get the proxy for a port or parameter with the specified name.
     *  This is an object on which JavaScript can directly invoke methods.
     *  @param name The name of the port or parameter.
     *  @return The proxy for the specified name, or null if there is none.
     */
    public PortOrParameterProxy getPortOrParameterProxy(String name) {
        if (_proxiesByName != null) {
            return _proxiesByName.get(name);
        }
        return null;
    }

    /** Get a resource, which may be a file name or a URL, and return the
     *  value of the resource as a string. If this instance of JavaScript
     *  is restricted (e.g., it is an accessor), then restrict relative file
     *  names to be in the same directory where the model is located or
     *  in a subdirectory, or if the resource begins with "$CLASSPATH/", to the
     *  classpath of the current Java process.
     *
     *  If the accessor is not restricted, the $KEYSTORE is resolved to
     *  $HOME/.ptKeystore.
     *
     *  The options parameter may have the following values:
     *  <ul>
     *  <li>If the type of the options parameter is a Number, then it is assumed
     *    to be the timeout in milliseconds.</li>
     *  <li> If the type of the options parameter is a String, then it is assumed
     *    to be the encoding, for examle "UTF-8".  If the value is "Raw" or "raw"
     *    then the data is returned as an unsigned array of bytes.  For backward
     *    compatibility, if the encoding is not set, it is assumed to be
     *    the default encoding of the platform, see
     *    <a href="https://docs.oracle.com/javase/8/docs/api/java/nio/charset/Charset.html#in_browser">Charset</a>
     *  </li>
     *  <li> If the type of the options parameter is an Object, then it may
     *    have the following optional fields:
     *    <ul>
     *      <li> encoding {string} The encoding of the file, see above for values.</li>
     *      <li> timeout {number} The timeout in milliseconds.</li>
     *    </ul>
     *  </li>
     *
     *  If the callback parameter is not present, then getResource() will
     *  be synchronous read like Node.js's
     *  <a href="https://nodejs.org/api/fs.html#in_browser">fs.readFileSync()</a>
     *  If the callback argument is present, then getResource() will be asynchronous like
     *  <a href="https://nodejs.org/api/fs.html#in_browser">fs.readFile()</a>.
     *
     *  @param uri A specification for the resource.
     *  @param arguments A variable number of arguments, where the
     *  first optional argument is an Object that can be a String (the
     *  encoding), an integer (the timeout) or a JSON object with
     *  encoding and timeout fields, See above.
     *  The second optional argument is a callback, the first argument to 
     *  the callback is the error, if any, the second element is the data, if any.
     *  @return The contents of the resource.
     *  @exception IllegalActionException If the uri specifies any protocol other
     *   than "http" or "https", or if the uri contains any "../", or if the uri
     *   begins with "/".
     */
    public Object getResource(String uri, Object... arguments) throws IllegalActionException {
        // We need to use a varargs method because in JavaScript we
        // want the options and callback args to be optional.  See
        // https://stackoverflow.com/questions/25603191/nashorn-bug-when-calling-overloaded-method-with-varargs-parameter
        Object options = null;
        Runnable callback = null;
        if (arguments.length > 0) {
            // Sometimes the second argument will be jdk.nashorn.internal.runtime.Undefined, so we skip it.
            if (!arguments[0].toString().equals("undefined")) {
                options = arguments[0];
            }
        }
        if (arguments.length > 1) {
            if (!arguments[1].toString().equals("undefined")) {
                callback = (Runnable) arguments[1];
            }
        }
        URI baseDirectory = null;
        uri = uri.trim();
        int colon = uri.indexOf(":");
        if (_restricted && colon >= 0
                && !(uri.startsWith("http:") || uri.startsWith("https:"))) {
            throw new IllegalActionException(this, "Protocol not permitted: "
                    + uri.substring(0, colon)
                    + " in URI "
                    + uri);
        }
        if (_restricted && (uri.contains("$CWD")
                || uri.contains("$HOME")
                || uri.contains("$USERNAME")
                || uri.contains("$USER")
                || uri.contains("~"))) {
            throw new IllegalActionException(this, "Illegal file name for resource: " + uri);
        }

        // If options is a String, then if it is a number, it is the
        // timeout, if it is a String, it is the encoding.
        // Otherwise, it is a Map.
        int timeout = -1;
        String encoding = null;
        Map<String,Object> optionsMap = null;
        if (options instanceof Integer) {
            // This is tested by
            // $PTII/bin/capecode /Users/cxh/src/ptII11.0.devel/ptolemy/actor/lib/jjs/modules/httpClient/demo/REST/Weather.xml
            // Which calls getResource() with an integer timeout.
            timeout = ((Integer)options).intValue();
        } else if (options instanceof String) {
            encoding = ((String)options).trim();
        } else if (options instanceof Map) {
            optionsMap = (Map<String,Object>) options;
        } else if (options != null) {
            throw new IllegalActionException("options was a " + options.getClass() +
                                             ", which is neither an Integer, String nor a Map<String,Object>");
        }
            
        if (callback != null) {
            System.err.println("JavaScript.java: getResource() invoked with a callback of " + callback
                       + ", which is not yet supported.");
        }

        if (colon < 0) {
            // Relative file name is given.
            if (_restricted && uri.startsWith("/")) {
                throw new IllegalActionException(this, "Absolute file names not permitted: " + uri);
            }
            if (_restricted && uri.contains("../")) {
                throw new IllegalActionException(this,
                        "Only file names at or within the model location are permitted: " + uri);
            }
            baseDirectory = URIAttribute.getModelURI(this);
        }

        // If the uri starts with $KEYSTORE, then try to create
        // the directory and subsitute the name.
        // To test this code, try:
        // $PTII/bin/capecode $PTII/ptolemy/actor/lib/jjs/modules/httpClient/demo/REST/Weather.xml 
        if ( uri.startsWith("$KEYSTORE")) {
            String home = System.getenv("HOME");
            if (home == null || home.length() == 0) {
                home = System.getenv("USERHOME");
            }
            String ptKeystoreName = home + File.separator + ".ptKeystore";
            File ptKeystore = new File(ptKeystoreName);
            if (!ptKeystore.isDirectory()) {
                System.out.println("JavaScript.java: Creating " + ptKeystore);
                if (!ptKeystore.mkdirs()) {
                    System.err.println("JavaScript.java: Could not create " + ptKeystoreName);
                }
            }
            if (ptKeystore.isDirectory()) {
                uri = uri.replace("$KEYSTORE", ptKeystoreName);
            }
        }

        // Read the resource.  If the encoding is [Rr][Aa][Ww], then
        // read it as a raw binary.  Otherwise read it using the encoding as the
        // Charset.  If there is no encoding, use the default Charset.
        try {
            URL url = FileUtilities.nameToURL(uri, baseDirectory, getClass().getClassLoader());
            if (encoding != null && encoding.toLowerCase().equals("raw")) {
                System.out.println("JavaScript.js: Reading raw data from " + url);
                byte [] dataBytes = FileUtilities.binaryReadURLToByteArray(url);
                System.out.println("JavaScript.js: read " + dataBytes.length + "bytes.");
                return dataBytes;
            } else {
                Charset charset = Charset.defaultCharset();
                if (encoding != null && encoding.length() > 0) {
                    try {
                        charset = Charset.forName(encoding);
                    } catch (Throwable throwable) {
                        throw new IllegalActionException(this, throwable, "\"" + encoding +
                                                         "\" is not a legal Charset name.  Try UTF-8 or Raw.");
                    }
                }
                BufferedReader in = null;
                try {
                    in = new BufferedReader(new InputStreamReader(url.openStream(), charset));
                    StringBuffer contents = new StringBuffer();
                    String input;
                    while ((input = in.readLine()) != null) {
                        contents.append(input);
                        contents.append("\n");
                    }
                    return contents.toString();
                } finally {
                    if (in != null) {
                        in.close();
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalActionException(this, e, "Failed to read URI: " + uri);
        }
    }

    /** Create a new JavaScript engine, load the default functions, and
     *  register the ports so that send() and get() can work.
     *  @exception IllegalActionException If a port name is either a
     *   a JavaScript keyword or not a valid identifier, if loading the
     *   default JavaScript files fails, or if the superclass throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        _directorThread = Thread.currentThread();

        // Create proxy for ports that don't already have one.
        for (TypedIOPort port : portList()) {
            // Do not convert the script or error ports to a JavaScript variable.
            if (port == script.getPort() || port == error) {
                continue;
            }
            // Do not expose ports that are already exposed.
            if (_proxies.get(port) != null) {
                continue;
            }
            PortOrParameterProxy proxy = new PortOrParameterProxy(port);
            _proxies.put(port, proxy);
            _proxiesByName.put(port.getName(), proxy);
        }

        // Create proxy for parameters that don't already have one.
        List<Variable> attributes = attributeList(Variable.class);
        for (Variable parameter : attributes) {
            // Do not convert the script parameter to a JavaScript variable.
            if (parameter == script) {
                continue;
            }
            // Do not expose parameters that are already exposed.
            if (_proxies.get(parameter) != null) {
                continue;
            }
            // Do not create a proxy for a PortParameter. There is already
            // a proxy for the port.
            if (parameter instanceof PortParameter) {
                continue;
            }
            PortOrParameterProxy proxy = new PortOrParameterProxy(parameter);
            _proxies.put(parameter, proxy);
            _proxiesByName.put(parameter.getName(), proxy);
        }

        // Invoke the initialize function.
        // Synchronize to ensure that this is atomic w.r.t. any callbacks.
        // Note that the callbacks might be invoked after a model has terminated
        // execution.
        synchronized (this) {
            // Be sure to use the instance version, not the exports version.
            // The instance version invokes initialize
            // on contained accessors, and invokes exports.initialize(), if defined.
            _invokeMethodInContext(_instance, "initialize");
        }
        _running = true;
    }

    /** Create a new input port if it does not already exist.
     *  This port will have an undeclared type and no description.
     *  @param name The name of the port.
     *  @exception IllegalActionException If no name is given.
     *  @exception NameDuplicationException If the name is a reserved word.
     *  @return The previous value of this input, if it has one, and
     *   null otherwise.
     */
    public Token input(String name) throws IllegalActionException,
            NameDuplicationException {
        return input(name, null);
    }

    /** Create a new input port if it does not already exist.
     *  The options argument can specify a "type", a "description",
     *  and/or a "value".
     *  If a type is given, and neither the port nor its
     *  corresponding parameter contain a TypeAttribute, then
     *  set the type as specified. Otherwise,
     *  leave the type unspecified so that it will be inferred.
     *  If a description is given, then create, append to, or modify the
     *  DocAttribute named "documentation" contained by this actor to
     *  include documentation of this output.
     *  If a value is given, then create a PortParameter instead of
     *  an ordinary port and set its default value, unless it already
     *  has a value that overrides the default.
     *  In that case, the prior value will be returned.
     *  Otherwise, null will be returned.
     *  If a Parameter already exists with the same name, then convert
     *  it to a PortParameter and preserve and return its value.
     *
     *  The options can also include a field
     *  "visibility" with one of the values "none", "expert",
     *  "noteditable" or "full" (the default). This is a hint
     *  to restrict visibility that a user has of the port.
     *
     *  @param name The name of the port.
     *  @param options The options, or null to accept the defaults.
     *   To give options, this argument must implement the Map interface.
     *  @return The previous value of this input, if it has one, and
     *   null otherwise.
     *  @exception IllegalActionException If no name is given.
     *  @exception NameDuplicationException If the name is a reserved word.
     */
    public Token input(String name, Map<String,Object> options)
            throws IllegalActionException, NameDuplicationException {
        // FIXME: Should check whether the model is running and use a change
        // request if so.
        if (name == null) {
            throw new IllegalActionException(this,
                    "Must specify a name to create an input.");
        }
        TypedIOPort port = (TypedIOPort) getPort(name);
        PortParameter parameter = null;
        Object token = null;
        Token previousValue = null;
        String previousExpression = "";
        boolean deletedPriorParameter = false;
        if (port == null) {
            // No pre-existing port.
            // If there is a parameter with the same name, then
            // remove it, but preserve its value.
            Attribute previous = getAttribute(name);
            if (previous instanceof Parameter) {
                previousValue = ((Parameter)previous).getToken();
                // Treat an empty string as no value.
                if (previousValue instanceof StringToken
                        && ((StringToken)previousValue).stringValue().trim().equals("")) {
                    previousValue = null;
                }
                previous.setContainer(null);
                deletedPriorParameter = true;
            }
            if (options == null) {
                // No options given. Use defaults.
                if (previousValue == null) {
                    // No previous value, so just create an ordinary port.
                    port = (TypedIOPort) newPort(name);
                } else {
                    // There is a previous value. Create a PortParameter to
                    // preserve its value.
                    parameter = new PortParameter(this, name);
                    port = parameter.getPort();
                }
            } else {
                // No pre-existing port, and options are given.
                Object value = ((Map<String,Object>) options).get("value");
                if (value == null && previousValue == null) {
                    // No value. Use an ordinary port.
                    port = (TypedIOPort) newPort(name);
                } else {
                    parameter = new PortParameter(this, name);
                    port = parameter.getPort();
                }
            }
        } else {
            // Pre-existing port.
            if (port == script.getPort() || port == error) {
                throw new NameDuplicationException(this, "Name is reserved: "
                        + name);
            }
            if (port instanceof ParameterPort) {
                parameter = ((ParameterPort) port).getParameter();
                // Oddly, we need to initialize PortParameter so that its current
                // value matches its previous value. Otherwise, the token
                // retrieved here could be left over from a previous run.
                ((PortParameter)parameter).initialize();
                previousValue = parameter.getToken();
                previousExpression = parameter.getExpression();
            } else {
                // Check to see whether there is now a default value.
                // This might be a derived class that is adding a default
                // value to a port that did not have one.
                if (options != null) {
                    Object value = ((Map<String,Object>) options).get("value");
                    if (value != null) {
                        // Sure enough, we need to replace the port
                        // with a PortParameter.
                        // NOTE: If there are connections to the port, these will be lost!
                        // But there should never be connections in this case. The port
                        // is the wrong type only if the base class provided no default value.
                        // But in this case, we are still constructing the accessor, so
                        // connections have not been made.
                        port.setContainer(null);
                        parameter = new PortParameter(this, name);
                        port = parameter.getPort();
                    }
                }
            }
        }

        if (options != null) {
            // If the port has its own type already (a TypeAttribute),
            // do not override it.
            if (port.attributeList(TypeAttribute.class).isEmpty()
                    && (parameter == null
                    || parameter.attributeList(TypeAttribute.class).isEmpty())) {
                Object type = ((Map<String,Object>) options).get("type");
                if (type instanceof String) {
                    // The following will put the parameter in string mode,
                    // if appropriate.
                    Type ptType = _typeAccessorToPtolemy((String) type, port);
                    port.setTypeEquals(ptType);
                    if (parameter != null) {
                        parameter.setTypeEquals(ptType);
                    }
                } else if (type != null) {
                    throw new IllegalActionException(this, "Unsupported type: "
                            + type);
                }
            }
            _setOptionsForSelect(port, options);
            Object description = ((Map<String,Object>) options).get("description");
            if (description != null) {
                _setPortDescription(port, description.toString());
            }
            Object value = ((Map<String,Object>) options).get("value");
            if (value != null) {
                // Convert value to a Ptolemy Token.
                try {
                    token = ((Invocable) _engine).invokeFunction(
                            "convertToToken", value);
                } catch (Exception e) {
                    throw new IllegalActionException(this, e,
                            "Cannot convert value to a Ptolemy Token: "
                                    + value);
                }
                if (!(token instanceof Token)) {
                    throw new IllegalActionException(this,
                            "Unsupported value: " + value);
                }
            }
            _setPortVisibility(options, port, parameter);
        }
        Token result = null;
        // Make sure to do this after setting the type to catch
        // string mode and type checks.
        if (parameter != null) {
            // Set the value of the parameter unless
            // the parameter already has a value that is an override,
            // in which case, allow that to prevail by doing nothing here.
            if (token != null && !parameter.isOverridden()) {
                if (parameter.getAttribute("_JSON") != null && !(token instanceof StringToken)) {
                    // Attempt to convert the token to a JSON string.
                    String json = TokenToJSON.constructJSON((Token)token);
                    token = new StringToken(json);
                }
                parameter.setToken((Token) token);
                // Indicate that this parameter is defined as part of the class definition
                // of the container.
                parameter.setDerivedLevel(1);
                // The above will have the side effect that a parameter will not be saved
                // when you save the model unless it is overridden.
            }
            if (parameter.isOverridden()) {
                result = previousValue;
            }
            // If there was a previous value from a parameter that got deleted, then override the
            // specified value.
            if (deletedPriorParameter && previousValue != null) {
                parameter.setExpression(previousExpression);
            }
        }
        port.setInput(true);
        return result;
    }

    /** Invoke the specified function in the fire() method as soon as possible.
     *  If this is called within the director thread and we are currently inside the
     *  fire() function, then invoke the function immediately. Otherwise, defer it using
     *  the director's fireAtCurrentTime() function.
     *  @param function The function to invoke.
     *  @exception IllegalActionException If the director cannot respect the request.
     */
    public void invokeCallback(final Runnable function) throws IllegalActionException {
        if (Thread.currentThread().equals(_directorThread)) {
            function.run();
        } else {
            _pendingCallbacks.offer(function);
            getDirector().fireAtCurrentTime(this);
        }
    }

    /** Return true if the model is executing (between initialize() and
     *  wrapup(), including initialize() but not wrapup()).
     *  This is (more or less) the phase of execution during which
     *  it is OK to send outputs. Note that if an asynchronous callback
     *  calls send() when this returns true, the output may still not
     *  actually reach its destination. It is possible that the last
     *  firing has already occurred, but wrapup() has not yet been called.
     *  @return true if the model is executing.
     */
    public boolean isExecuting() {
        return _executing;
    }

    /** Return true if the specified string is a JavaScript keyword.
     *  @param identifier The identifier name.
     *  @return True if it is a JavaScript keyword.
     */
    public static boolean isJavaScriptKeyword(String identifier) {
        return _KEYWORDS.contains(identifier);
    }

    /** Return true if this actor is restricted.
     *  A restricted instance of this actor limits the capabilities available
     *  to the script it executes so that it can execute untrusted code.
     *  This base class is not restricted, but subclasses may be.
     *  @return True if this actor is restricted.
     */
    public boolean isRestricted() {
        return _restricted;
    }

    /** Return true if the specified string is not a JavaScript keyword
     *  and is a valid JavaScript identifier.
     *  @param identifier The proposed name.
     *  @return True if it is a valid identifier name.
     */
    public static boolean isValidIdentifier(String identifier) {
        int length = identifier.length();
        if (length == 0) {
            return false;
        }
        if (!Character.isJavaIdentifierStart(identifier.charAt(0))) {
            return false;
        }
        for (int i = 1; i != length; ++i) {
            if (!Character.isJavaIdentifierPart(identifier.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /** Return the local host IP address as a string.
     *  @return A string representation of the local host address.
     *  @exception UnknownHostException If the local host is not known.
     *  @exception SecurityException If this actor is in restricted mode.
     */
    public String localHostAddress() throws UnknownHostException,
            SecurityException {
        if (_restricted) {
            throw new SecurityException(
                    "Actor is restricted. Cannot invoke localHostAddress().");
        }
        return InetAddress.getLocalHost().getHostAddress();
    }

    /** If debugging is turned on, then send the specified message to the
     *  _debug() method, and otherwise send it out to stdout.
     *  @param message The message
     */
    public void log(String message) {
        if (_debugging) {
            _debug(message);
        } else {
            System.out.println(getName() + ": " + message + " (" + Thread.currentThread() + ")");
        }
    }

    /** Create a new output port if it does not already exist.
     *  Set the type to general.
     *  @param name The name of the port.
     *  @exception IllegalActionException If no name is given.
     *  @exception NameDuplicationException If the name is a reserved word.
     */
    public void output(String name) throws IllegalActionException,
            NameDuplicationException {
        output(name, null);
    }

    /** Create a new output port if it does not already exist.
     *  The options argument can specify a "type" and/or a "description".
     *  If a type is given and the port does not contain a TypeAttribute,
     *  then set the type as specified. Otherwise,
     *  set the type to general.
     *  If a description is given, then create, append to, or modify the
     *  DocAttribute named "documentation" contained by this actor to
     *  include documentation of this output.
     *  @param name The name of the port.
     *  @param options The options, or null to accept the defaults.
     *   To give options, this argument must implement the Map interface.
     *  @exception IllegalActionException If no name is given.
     *  @exception NameDuplicationException If the name is a reserved word.
     */
    public void output(String name, Map<String,Object> options)
            throws IllegalActionException, NameDuplicationException {
        // FIXME: Should check whether the model is running a use a change
        // request if so.
        if (name == null) {
            throw new IllegalActionException(this,
                    "Must specify a name to create an output.");
        }
        TypedIOPort port = (TypedIOPort) getPort(name);
        if (port == null) {
            port = (TypedIOPort) newPort(name);
        } else {
            if (port == script.getPort()) {
                throw new NameDuplicationException(this, "Name is reserved: "
                        + name);
            }
        }
        if (options != null) {
            if (port.attributeList(TypeAttribute.class).isEmpty()) {
                // Do not override the type if the port has a TypeAttribute.
                Object type = ((Map<String,Object>) options).get("type");
                if (type instanceof String) {
                    port.setTypeEquals(_typeAccessorToPtolemy((String) type, port));
                } else {
                    if (type == null) {
                        port.setTypeEquals(BaseType.GENERAL);
                    } else {
                        throw new IllegalActionException(this, "Unsupported type: "
                                + type);
                    }
                }
            }
            _setOptionsForSelect(port, options);
            Object description = ((Map<String,Object>) options).get("description");
            if (description != null) {
                _setPortDescription(port, description.toString());
            }
            _setPortSpontaneity(options, port);
            _setPortVisibility(options, port, null);
        } else {
            if (port.attributeList(TypeAttribute.class).isEmpty()) {
                port.setTypeEquals(BaseType.GENERAL);
            }
        }
        port.setOutput(true);
    }

    /** Create a new parameter if it does not already exist.
     *  This parameter will have an undeclared type and no description.
     *  @param name The name of the parameter.
     *  @return The previous value of this parameter, if it has one, and
     *   null otherwise.
     *  @exception IllegalActionException If no name is given, or if the
     *   model is executing.
     *  @exception NameDuplicationException If the name is a reserved word, or if an attribute
     *   already exists with the name and is not a parameter.
     */
    public Token parameter(String name) throws IllegalActionException,
            NameDuplicationException {
        return parameter(name, null);
    }

    /** Create a new parameter if it does not already exist.
     *  The options argument can specify a "type", a "description",
     *  "visibility", and/or a "value".
     *  If a type is given, set the type as specified. Otherwise,
     *  leave the type unspecified so that it will be inferred from the value.
     *  If a description is given, then create, append to, or modify the
     *  DocAttribute named "documentation" contained by this actor to
     *  include documentation of this output.
     *  If a value is given, then set the default value of the parameter
     *  if it does not already have a value.
     *  @param name The name of the parameter.
     *  @param options The options, or null to accept the defaults.
     *  @return The previous value of this parameter, if it has one, and
     *   null otherwise.
     *  @exception IllegalActionException If no name is given.
     *  @exception NameDuplicationException If the name is a reserved word, or if an attribute
     *   already exists with the name and is not a parameter.
     */
    public Token parameter(String name, Map<String,Object> options)
            throws IllegalActionException, NameDuplicationException {
        /* Don't constrain when this executes.
         * FIXME: Instead, we should use a ChangeRequest if we are executing.
        if (_executing) {
            throw new IllegalActionException(this,
                    "Cannot create a parameter while the model is executing. Stop the model.");
        }
         */
        if (name == null) {
            throw new IllegalActionException(this,
                    "Must specify a name to create a parameter.");
        }

        Attribute parameter = getAttribute(name);
        if (parameter == null) {
            parameter = new Parameter(this, name);
        } else {
            if (parameter == script) {
                // FIXME: Should instead do a name transformation?
                throw new NameDuplicationException(this, "Name is reserved: "
                        + name);
            } else if (!(parameter instanceof Parameter)) {
                throw new NameDuplicationException(this, "Name is taken: "
                        + name);
            }
        }
        Token result = null;
        if (options != null) {
            // Find the type, if it is specified.
            Type ptType = null;
            Object type = options.get("type");
            if (type instanceof String) {
                ptType = _typeAccessorToPtolemy((String) type, parameter);
            } else if (type != null) {
                throw new IllegalActionException(this, "Unsupported type: "
                        + type);
            }
            if (ptType != null) {
                ((Parameter) parameter).setTypeEquals(ptType);
            }
            _setOptionsForSelect(parameter, options);
            // Check for value option.
            Object value = options.get("value");
            if (value != null && !parameter.isOverridden()) {
                // There is a specified value, and the parameter value
                // has not been overridden.

                // Convert value to a Ptolemy Token.
                Object token;
                try {
                    token = ((Invocable) _engine).invokeFunction(
                            "convertToToken", value);
                } catch (Exception e) {
                    throw new IllegalActionException(this, e,
                            "Cannot convert value to a Ptolemy Token: "
                                    + value);
                }
                if (token instanceof Token) {
                    ((Parameter) parameter).setToken((Token) token);
                } else {
                    throw new IllegalActionException(this,
                            "Unsupported value: " + value);
                }
                // Indicate that this parameter is defined as part of the class definition
                // of the container.
                parameter.setDerivedLevel(1);
                // The above will have the side effect that a parameter will not be saved
                // when you save the model unless it is overridden.
            }
            if (parameter.isOverridden()) {
                result = ((Parameter)parameter).getToken();
            }

            Object visibility = ((Map<String,Object>) options).get("visibility");
            if (visibility instanceof String) {
                String generic = ((String) visibility).trim().toLowerCase();
                switch (generic) {
                case "none":
                    if (parameter instanceof Variable) {
                        ((Variable) parameter).setVisibility(Settable.NONE);
                    }
                    break;
                case "expert":
                    if (parameter instanceof Variable) {
                        ((Variable) parameter).setVisibility(Settable.EXPERT);
                    }
                    break;
                case "noteditable":
                    if (parameter instanceof Variable) {
                        ((Variable) parameter).setVisibility(Settable.NOT_EDITABLE);
                    }
                    break;
                default:
                    // Assume "full".
                    if (parameter instanceof Variable) {
                        ((Variable) parameter).setVisibility(Settable.FULL);
                    }
                }
            }

            Object description = options.get("description");
            if (description != null) {
                _setPortDescription(parameter, description.toString());
            }
        }

        // Parameters may be immediately referenced in the setup function, so we
        // need to create a proxy for them now.
        if (_proxies.get(parameter) == null) {
            PortOrParameterProxy proxy = new PortOrParameterProxy(parameter);
            _proxies.put(parameter, proxy);
            _proxiesByName.put(parameter.getName(), proxy);
        }
        return result;
    }

    /** Create a new JavaScript engine, load the default functions,
     *  and evaluate the script parameter.
     *  @exception IllegalActionException If a port name is either a
     *   a JavaScript keyword or not a valid identifier, if loading the
     *   default JavaScript files fails, or if the superclass throws it.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        _executing = true;

        // Note that this will clear out any pending
        // callbacks from the previous execution.
        _pendingCallbacks.clear();
        synchronized (this) {
            _pendingTimeoutFunctions.clear();
            _pendingTimeoutIDs.clear();
            _timeoutCount = 0;

            _createEngineAndEvaluateSetup();
        }
        _running = false;
    }

    /** Utility method to read a string from an input stream.
     *  @param stream The stream.
     *  @return The string.
     *  @exception IOException If the stream cannot be read.
     *  @deprecated Invoke FileUtilities.readFromInputStream() directly.
     */
    public static String readFromInputStream(InputStream stream)
            throws IOException {
	// This method was moved to FileUtilities
	// when the pure Nashorn host and the Cape Code host were
	// split.
	return FileUtilities.readFromInputStream(stream);
    }

    /** Invoke the specified function after the specified amount of time and again
     *  at multiples of that time.
     *  The time will be added to the current time of the director, and fireAt()
     *  request will be made of the director. If the director cannot fulfill the
     *  request, this method will throw an exception. Note that if you want
     *  real-time behavior, then the director's synchronizeToRealTime parameter
     *  needs to be set to true.
     *  @param function The function to invoke.
     *  @param milliseconds The number of milliseconds in the future to invoke it.
     *  @return A unique ID for this callback.
     *  @exception IllegalActionException If the director cannot respect the request.
     *  @see #clearTimeout(Object)
     */
    @Override
    public synchronized Object setInterval(final Runnable function, final int milliseconds)
            throws IllegalActionException {
        final Integer id = Integer.valueOf(_timeoutCount++);
        // Create a new function that invokes the specified function and then reschedules
        // itself.
        final Runnable reschedulingFunction = new Runnable() {
            @Override
            public void run() {
                _runThenReschedule(function, milliseconds, id);
            }
        };
        _setTimeout(reschedulingFunction, milliseconds, id);
        // Avoid FindBugs: "Boxed value is unboxed and then immediately reboxed"
        return id;
    }

    /** Invoke the specified function after the specified amount of time.
     *  The time will be added to the current time of the director, and fireAt()
     *  request will be made of the director. If the director cannot fulfill the
     *  request, this method will throw an exception. Note that if you want
     *  real-time behavior, then the director's synchronizeToRealTime parameter
     *  needs to be set to true.
     *  @param function The function to invoke.
     *  @param milliseconds The number of milliseconds in the future to invoke it.
     *  @return A unique ID for this callback.
     *  @exception IllegalActionException If the director cannot respect the request.
     *  @see #clearTimeout(Object)
     */
    @Override
    public synchronized Object setTimeout(final Runnable function, final int milliseconds)
            throws IllegalActionException {
        final Integer id = Integer.valueOf(_timeoutCount++);
        _setTimeout(function, milliseconds, id);
        return Integer.valueOf(id);
    }
    
    /** Stop execution of the enclosing model.
     */
    public void stopEnclosingModel() {
        getDirector().finish();
        getDirector().stopFire();
    }

    /** Convert the specified array into a native JavaScript array.
     *  @param array The array to convert.
     *  @return The native JavaScript array.
     *  @exception IllegalActionException If the conversion fails.
     */
    public Object toJSArray(Object[] array) throws IllegalActionException {
        try {
            // Sadly, nashorn can't pass arrays... It treats them as separate arguments.
            // Adding a second dummy argument (1 below) seems to solve this problem.
            return ((Invocable) _engine).invokeFunction("convertToJSArray", array, 1);
        } catch (Throwable e) {
            throw new IllegalActionException(this, e,
                    "Conversion to JavaScript array failed.");
        }
    }

    /** Specify version information to appear in the documentation for this actor.
     *  @param version Version information to appear in documentation.
     */
    public void version(String version) {
        // Use a change request so as to not create dependencies on vergil here.
        StringBuffer moml = new StringBuffer(
                "<property name=\"documentation\" class=\"ptolemy.vergil.basic.DocAttribute\">");
        moml.append("<property name=\"version\" class=\"ptolemy.kernel.util.StringAttribute\" value=\"");
        moml.append(StringUtilities.escapeForXML(version));
        moml.append("\"></property></property>");
        MoMLChangeRequest request = new MoMLChangeRequest(this, this,
                moml.toString());
        requestChange(request);
    }

    /** Execute the wrapup function, if it is defined, and exit the context for this thread.
     *  @exception IllegalActionException If the parent class throws it.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        _executing = false;
        _running = false;
        // Synchronize so that this invocation is atomic w.r.t. any callbacks.
        synchronized (this) {
            // Invoke the wrapup function.
            // Be sure to use the instance version, not the exports version.
            // The instance version removes input handlers, invokes wrapup
            // on contained accessors, and invokes exports.wrapup(), if defined.
            _invokeMethodInContext(_instance, "wrapup");

            if (_pendingTimeoutIDs.size() > 0) {
                String message = "WARNING: "
                        + getName()
                        + ": Model stopped before executing actions (e.g. producing outputs)"
                        + " scheduled for execution at times "
                        + _pendingTimeoutIDs.keySet().toString();
                if (_debugging) {
                    _debug(message);
                } else {
                    System.err.println(message);
                }
            }
        }
        super.wrapup();
        _directorThread = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class so that the name of any port added is shown.
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    @Override
    protected void _addPort(TypedIOPort port)
            throws IllegalActionException, NameDuplicationException {
        super._addPort(port);
        SingletonParameter showName = new SingletonParameter(port, "_showName");
        showName.setExpression("true");
        // Prevent export.
        showName.setPersistent(false);
    }

    /** Check the validity of a name. This implementation throws an exception
     *  if either the name is not a valid JavaScript identifier or it is a
     *  JavaScript keyword.
     *  @param name The name to check.
     *  @exception IllegalActionException If the name is either not a valid
     *   identifier or is a keyword.
     */
    protected void _checkValidity(String name) throws IllegalActionException {
        if (!isValidIdentifier(name)) {
            throw new IllegalActionException(this,
                    "Port name is not a valid JavaScript identifier: " + name);
        }
        if (isJavaScriptKeyword(name)) {
            try {
                MessageHandler.warning("Port name is a JavaScript keyword: "
                        + name);
            } catch (CancelException e) {
                throw new IllegalActionException(this, "Cancelled.");
            }
        }
    }

    /** Return null, because the default type constraints, where output
     *  types are greater than or equal to all input types, make no sense
     *  for this actor. Output types need to be set explicitly or inferred
     *  from backward type inference, and input types need to be set explicitly
     *  or inferred from forward type inference.
     *  @return Null.
     */
    @Override
    protected Set<Inequality> _defaultTypeConstraints() {
        return null;
    }

    /** Invoke the specified method in the context of the exports object.
     *  If there is no such method in that context, attempt to invoke the
     *  method in the top-level context. The exports object becomes the
     *  value of the 'this' variable during execution of the method.
     *  @param context The context.
     *  @param methodName The method name.
     *  @param args Arguments to pass to the function.
     *  @exception IllegalActionException If the method does not exist in either
     *   context, or if an error occurs invoking the method.
     */
    protected void _invokeMethodInContext(Object context, String methodName, Object... args)
            throws IllegalActionException {
        try {
            ((Invocable) _engine).invokeMethod(context, methodName, args);
        } catch (NoSuchMethodException e) {
            // Attempt to invoke it in the top-level contenxt.
            try {
                ((Invocable) _engine).invokeFunction(methodName, args);
            } catch (NoSuchMethodException e1) {
                throw new IllegalActionException(this, e1,
                        "No function defined named " + methodName);
            } catch (ScriptException e1) {
                if (error.getWidth() > 0) {
                    error.send(0, new StringToken(e1.getMessage()));
                } else {
                    throw new IllegalActionException(this, e1,
                            "Failure executing the " + methodName
                                    + " function: " + e1.getMessage());
                }
            }
        } catch (ScriptException e) {
            if (error.getWidth() > 0) {
                error.send(0, new StringToken(e.getMessage()));
            } else {
                throw new IllegalActionException(this, e,
                        "Failure executing the " + methodName + " function: "
                                + e.getMessage());
            }
        } catch (Throwable throwable) {
            if (error.getWidth() > 0) {
                error.send(0, new StringToken(throwable.getMessage()));
            } else {
                throw new IllegalActionException(this, throwable,
                        "Failure executing the " + methodName + " function: "
                                + throwable.getMessage());
            }
        }
    }

    /** Set the description of a port or parameter.
     *  @param portOrParameter The port.
     *  @param description The description.
     */
    protected void _setPortDescription(NamedObj portOrParameter,
            String description) {
        // Use a change request so as to not create dependencies on vergil here.
        StringBuffer moml = new StringBuffer(
                "<property name=\"documentation\" class=\"ptolemy.vergil.basic.DocAttribute\">");
        moml.append("<property name=\"");
        moml.append(portOrParameter.getName());
        moml.append("\" class=\"ptolemy.kernel.util.StringAttribute\" value=\"");
        moml.append(StringUtilities.escapeForXML(description));
        moml.append("\"></property></property>");
        MoMLChangeRequest request = new MoMLChangeRequest(this, this,
                moml.toString());
        requestChange(request);
    }

    /** Empty argument list for JavaScript function invocation. */
    protected final static Object[] _EMPTY_ARGS = new Object[] {};

    /** JavaScript engine, shared among all instances of this class. */
    protected ScriptEngine _engine;

    /** True while the model is executing (between initialize() and
     *  wrapup(), including the initialize() but not wrapup().
     *  This is the phase of execution during which it is OK to
     *  send outputs.
     */
    protected boolean _executing;

    /** The exports object defined in the script that is evaluated. */
    protected Object _exports;

    /** Initial script as a token. */
    protected static final StringToken _INITIAL_SCRIPT = new StringToken(
            "// Put your JavaScript program here.\n"
            + "// Add ports and parameters.\n"
            + "// Define JavaScript functions initialize(), fire(), and/or wrapup().\n"
            + "// Refer to parameters in scope using dollar-sign{parameterName}.\n"
            + "// In the fire() function, use get(parameterName, channel) to read inputs.\n"
            + "// Send to output ports using send(value, portName, channel).\n");

    /** The instance returned when evaluating the script. */
    protected Object _instance;

    /** JavaScript keywords. */
    protected static final String[] _JAVASCRIPT_KEYWORDS = new String[] {
            "abstract", "as", "boolean", "break", "byte", "case", "catch",
            "char", "class", "continue", "const", "debugger", "default",
            "delete", "do", "double", "else", "enum", "export", "extends",
            "false", "final", "finally", "float", "for", "function", "goto",
            "if", "implements", "import", "in", "instanceof", "int",
            "interface", "is", "long", "namespace", "native", "new", "null",
            "package", "private", "protected", "public", "return", "short",
            "static", "super", "switch", "synchronized", "this", "throw",
            "throws", "transient", "true", "try", "typeof", "use", "var",
            "void", "volatile", "while", "with" };

    /** Keywords as a Set. */
    protected static final Set<String> _KEYWORDS = new HashSet<String>(
            Arrays.asList(_JAVASCRIPT_KEYWORDS));

    /** If set to true in the constructor of a base class, then put
     *  this actor in "restricted" mode.  This limits the
     *  functionality as described in the class comment.
     */
    protected boolean _restricted = false;

    /** True while the model is running (past initialize() and before
     *  wrapup()).
     *  This is the phase of execution during which it is OK to
     *  get inputs.
     */
    protected boolean _running = false;

    ///////////////////////////////////////////////////////////////////
    ////                        Private Methods                    ////

    /** Construct a JavaScript options object for the specified port
     *  or parameter.
     *  @param portOrParameter A port or parameter.
     *  @param valueToken The value as a Ptolemy II token.
     *  @return A JavaScript object of the form {'value': value}.
     *  @exception IllegalActionException If conversion fails and the user
     *   responds "Cancel" to the warning.
     */
    private Object _constructOptionsObject(NamedObj portOrParameter, Token valueToken)
            throws IllegalActionException {
        // Song and dance to get JSON representation of the default value.
        PortOrParameterProxy proxy = _proxies.get(portOrParameter);
        if (proxy == null) {
            // Skip this. Probably PortParameter.
            return null;
        }
        Map<String,Object> options = new HashMap<String,Object>();
        try {
            Object converted = ((Invocable) _engine).invokeFunction(
                    "convertFromToken", valueToken, proxy.isJSON());
            options.put("value", converted);
        } catch (Exception ex) {
            try {
                MessageHandler.warning(
                        "Failed to specify default value for port "
                                + portOrParameter.getName(),
                        ex);
            } catch (CancelException e) {
                throw new IllegalActionException(this, ex,
                        "Failed to specify default value for port "
                                + portOrParameter.getName());
            }
        }
        return options;
    }

    /** Create a script engine, evaluate basic function definitions,
     *  define the 'actor' variable, evaluate the script, and invoke the
     *  setup method if it exists.
     *  @exception IllegalActionException If an error occurs.
     */
    private void _createEngineAndEvaluateSetup() throws IllegalActionException {

        // Create a script engine.
        // This is private to this class, so each instance of JavaScript
        // has its own completely isolated script engine.
        // If this later creates a performance problem, we may want to
        // provide an option to share a script engine. But then all accesses
        // to the engine will have to be synchronized to ensure that there
        // never is more than one thread trying to evaluate a script.
        // Note that an engine is recreated on each run. This ensures
        // that the JS context does not remember data from one run to the next.

        _engine = JavaScript.createEngine(this, _debugging, _restricted);

        // Create proxies for the port.
        // Note that additional proxies may need to be created in initialize(),
        // if they are created by a setup() function call.
        _proxies = new HashMap<NamedObj, PortOrParameterProxy>();
        _proxiesByName = new HashMap<String, PortOrParameterProxy>();
        for (TypedIOPort port : portList()) {
            // Do not convert the script or error ports to a JavaScript variable.
            if (port == script.getPort() || port == error) {
                continue;
            }
            PortOrParameterProxy proxy = new PortOrParameterProxy(port);
            _proxies.put(port, proxy);
            _proxiesByName.put(port.getName(), proxy);
        }

        // Create proxies for parameters.
        // Note that additional parameters may need to be exposed in initialize(),
        // if they are created by a setup() function call.
        List<Variable> attributes = attributeList(Variable.class);
        for (Variable parameter : attributes) {
            // Do not convert the script parameter to a JavaScript variable.
            if (parameter == script) {
                continue;
            }
            // Do not expose parameters that are already exposed.
            if (_proxies.get(parameter) != null) {
                continue;
            }
            // Do not create a proxy for a PortParameter. There is already
            // a proxy for the port.
            if (parameter instanceof PortParameter) {
                continue;
            }
            PortOrParameterProxy proxy = new PortOrParameterProxy(parameter);
            _proxies.put(parameter, proxy);
            _proxiesByName.put(parameter.getName(), proxy);
        }

        // Evaluate the script.
        String scriptValue = script.getValueAsString();
        try {
            _instance = ((Invocable)_engine).invokeFunction(
                    "evaluateCode", getName(), scriptValue);
            _exports = ((Map)_instance).get("exports");
        } catch (Throwable throwable) {
            throw new IllegalActionException(this, throwable,
                    "Failed to evaluate script.");
        }

        // Invoke the setup function.
        // Synchronize to ensure that this is atomic w.r.t. any callbacks.
        // Note that the callbacks might be invoked after a model has terminated
        // execution.
        // This is now handled by evaluateCode() above.
        /*
        synchronized (this) {
            _invokeMethodInContext("setup");
        }
        */
        // For backward compatibility, if there is no setup() function,
        // then define all inputs, outputs, and parameters.
        if (((Map)_exports).get("setup") == null) {
            for (TypedIOPort port : portList()) {
                // Do not handle the script or error ports to a JavaScript variable.
                if (port == script.getPort() || port == error) {
                    continue;
                }
                if (port instanceof ParameterPort) {
                    Token valueToken = ((ParameterPort)port).getParameter().getToken();
                    if (valueToken != null) {
                        Object options = _constructOptionsObject(port, valueToken);
                        _invokeMethodInContext(_instance, "input", port.getName(), options);
                        continue;
                    }
                }
                // For ordinary inputs or ParameterPort with no default value, just
                // create an input or output with no default.
                if (port.isInput()) {
                    _invokeMethodInContext(_instance, "input", port.getName());
                } else {
                    _invokeMethodInContext(_instance, "output", port.getName());
                }
            }
            for (Attribute attribute : attributes) {
                if (attribute instanceof Parameter && !(attribute instanceof PortParameter)) {
                    Token valueToken = ((Parameter)attribute).getToken();
                    if (valueToken != null) {
                        Object options = _constructOptionsObject(attribute, valueToken);
                        _invokeMethodInContext(_instance, "parameter", attribute.getName(), options);
                        continue;
                    } else {
                        _invokeMethodInContext(_instance, "parameter", attribute.getName());
                    }
                }
            }
        }
    }

    /** Fire me again at the current model time, one microstep later.
     *  Unlike calling the director's fireAtCurrentTime() method, this
     *  method is not affected by the current real time.
     *  @exception IllegalActionException If the director throws it.
     */
    private Time _fireAtCurrentTime() throws IllegalActionException {
        Director director = getDirector();
        Time currentTime = director.getModelTime();
        // Note that it is not correct to call director.fireAtCurrentTime().
        // At least in DE, that method uses current _real_ time, not
        // model time.
        director.fireAt(this, currentTime);
        return currentTime;
    }

    /** If the second argument is true, mark the first argument
     *  as requiring its value to be JSON. The mark has the form
     *  of a (non-persistent) singleton parameter named "_JSON".
     *
     *  @param typeable The object to mark.
     *  @param JSONmode Whether to mark it.
     *  @exception NameDuplicationException Not thrown.
     *  @exception IllegalActionException If adding the mark fails.
     */
    private void _markJSONmode(NamedObj typeable, boolean JSONmode)
            throws NameDuplicationException, IllegalActionException {
        if (JSONmode) {
            // Put in a hint that the string value needs to be JSON.
            /* SingletonAttribute mark = */ new SingletonAttribute(typeable, "_JSON");
            // Do not make this non-persistent, so when the model is saved and then
            // re-opened, it will give an error that it cannot evaluate the value
            // because it does not know to interpret it as an arbitrary string rather
            // an expression.
            // mark.setPersistent(false);
            if (typeable instanceof PortParameter) {
                _markJSONmode(((PortParameter)typeable).getPort(), true);
            }
        }
    }

    /** Provide an input value (a token) to the specified input name.
     *  This will convert the token to a suitable form.
     *  @param name The input name.
     *  @param token The token.
     *  @exception IllegalActionException If the token cannot be provided.
     */
    private void _provideInput(String name, Token token)
            throws IllegalActionException {
        try {
            Object converted = ((Invocable) _engine).invokeFunction("convertFromToken", token);
            _invokeMethodInContext(_instance, "provideInput", name, converted);
        } catch (Exception e) {
            throw new IllegalActionException(this, e, "Failed to provide input token.");
        }
    }

    /** Invoke the specified function, then schedule another call to this
     *  same method after the specified number of milliseconds, using the specified
     *  id for the timeout function.
     *  @param function The function to repeatedly invoke.
     *  @param milliseconds The number of milliseconds in a period.
     *  @param id The id to use for the timeout function.
     */
    private synchronized void _runThenReschedule(final Runnable function,
            final int milliseconds, final Integer id) {
            // Invoke the function.
        function.run();

        // If the above function does not cancel the interval, reschedule it.
        if (_pendingTimeoutFunctions.get(id) == null) {
            // The callback function itself may cancel the interval.
            return;
        }
        final Runnable reschedulingFunction = new Runnable() {
            @Override
            public void run() {
                _runThenReschedule(function, milliseconds, id);
            }
        };
        try {
            _setTimeout(reschedulingFunction, milliseconds, id);
        } catch (IllegalActionException e) {
            // This should not have happened here. Should have been caught
            // the first time. But just in case...
            throw new InternalErrorException(this, e,
                    "Failed to reschedule function scheduled with setInterval().");
        }
        // Prevent removal of the id from _pendingTimeoutFunctions
        _removePendingIntervalFunction = false;
    }

    /** If the options argument inlucdes an "options" field, then use that field
     *  to add choices to the parameter given by the first argument. The value of
     *  options field should be an array of strings.
     *  @param typeable A Parameter or a ParameterPort. Other types are ignored.
     *  @param options The options argument for an input or parameter specification.
     */
    private void _setOptionsForSelect(NamedObj typeable, Map<String,Object> options) {
        if (options != null) {
            Object possibilities = ((Map<String,Object>) options).get("options");
            if (possibilities != null) {
                // Possibilities are specified.
                Parameter parameter = null;
                if (typeable instanceof Parameter) {
                    parameter = (Parameter) typeable;
                } else if (typeable instanceof ParameterPort) {
                    parameter = ((ParameterPort) typeable).getParameter();
                } else {
                    // Can't set possibilities. Ignore this option.
                    return;
                }
                Iterable<Object> choices = null;
                if (possibilities instanceof ScriptObjectMirror) {
                    choices = ((ScriptObjectMirror) possibilities).values();
                } else if (possibilities instanceof Object[]) {
                    // Pathetically, Arrays.asList() doesn't work.
                    // It returns a list with one element, the array!
                    // choices = Arrays.asList(possibilities);
                    choices = new LinkedList<Object>();
                    for (int i = 0; i < ((Object[])possibilities).length; i++) {
                        if (((Object [])possibilities)[i] == null) {
                            // When SerialHelper was handling longs,
                            // we would get a NPE here, so we print a
                            // better message.
                            new NullPointerException("possibility " + i + " was null?").printStackTrace();
                            ((LinkedList<Object>)choices).add("null");
                        } else {
                            ((LinkedList<Object>)choices).add(((Object[])possibilities)[i].toString());
                        }
                    }
                } else if (possibilities instanceof int[]) {
                    choices = new LinkedList<Object>();
                    for (int i = 0; i < ((int[])possibilities).length; i++) {
                        // FindBugs says to use Integer.valueOf() instead of new Integer().
                        ((LinkedList<Object>)choices).add(Integer.valueOf(((int[])possibilities)[i]));
                    }
                } else if (possibilities instanceof double[]) {
                    choices = new LinkedList<Object>();
                    for (int i = 0; i < ((double[])possibilities).length; i++) {
                        // FindBugs says to use Double.valueOf() instead of new Double().
                        ((LinkedList<Object>)choices).add(Double.valueOf(((double[])possibilities)[i]));
                    }
                }
                if (choices != null) {
                    for (Object possibility : choices) {
                        if (possibility instanceof String) {
                            if (parameter.isStringMode()) {
                                parameter.addChoice((String) possibility);
                            } else {
                                parameter.addChoice("\"" + (String) possibility
                                        + "\"");
                            }
                        }
                    }
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Set the port spontaneity if the options argument includes a
     *  "spontaneous" field with one of the values "true" or "false".
     *  @param options The options map.
     *  @param port The port of which to set the spontaneity.
     *  @exception IllegalActionException If there is a problem accessing
     *  the spontaneous argument or setting the SingletonParameter.
     *  @exception NameDuplicationException Should not be thrown.
     */
    private void _setPortSpontaneity(Map<String,Object> options, TypedIOPort port)
            throws IllegalActionException, NameDuplicationException {
        Object spontaneity = ((Map<String,Object>) options).get("spontaneous");
        if (spontaneity instanceof Boolean) {
            BooleanToken spontaneityToken = null;
            if ((Boolean)spontaneity) {
                spontaneityToken = BooleanToken.TRUE;
            } else {
                spontaneityToken = BooleanToken.FALSE;
            }
            new SingletonParameter(port, _SPONTANEOUS, spontaneityToken);
        }
    }

    /** Set the port visibility if the options argument includes a
     *  "visibility" field with one of the values "none", "expert",
     *  "noteditable" or "full" (the default).
     *  @param options The options map.
     *  @param port The port set restrict visibility of.
     *  @param parameter The parameter, if this is a port-parameter.
     *  @exception IllegalActionException If setting fails.
     *  @exception NameDuplicationException Should not be thrown.
     */
    private void _setPortVisibility(Map<String,Object> options, TypedIOPort port,
            PortParameter parameter) throws IllegalActionException,
            NameDuplicationException {
        Object visibility = ((Map<String,Object>) options).get("visibility");
        if (visibility instanceof String) {
            String generic = ((String) visibility).trim().toLowerCase();
            boolean hide = false;

            switch (generic) {
            case "none":
                if (parameter != null) {
                    parameter.setVisibility(Settable.NONE);
                }
                hide = true;
                break;
            case "expert":
                if (parameter != null) {
                    parameter.setVisibility(Settable.EXPERT);
                }
                hide = true;
                break;
            case "noteditable":
                if (parameter != null) {
                    parameter.setVisibility(Settable.NOT_EDITABLE);
                }
                hide = true;
                break;
            default:
                // Assume "full".
                if (parameter != null) {
                    parameter.setVisibility(Settable.FULL);
                }
                if (port != null) {
                    Attribute hideParam = port.getAttribute("_hide");
                    if (hideParam != null) {
                        hideParam.setContainer(null);
                    }
                }
            }
            if (hide && port != null) {
                SingletonParameter hideParam = new SingletonParameter(port, "_hide", BooleanToken.TRUE);
                // Prevent export.
                hideParam.setPersistent(false);
            }
        }
    }

    /** Put the argument in string mode. If the argument is a parameter,
     *  do that directly. Otherwise, if it is ParameterPort, then put its associated
     *  parameter in string mode. Otherwise, do nothing.
     *  @param typeable A Parameter or ParameterPort.
     *  @param JSONmode True to indicate that the string needs to be JSON.
     *  @exception IllegalActionException If the marker attribute cannot be added.
     */
    private void _setStringMode(NamedObj typeable, boolean JSONmode)
            throws NameDuplicationException, IllegalActionException {
        Parameter parameter = null;
        if (typeable instanceof Parameter) {
            parameter = (Parameter)typeable;
        } else if (typeable instanceof ParameterPort) {
            parameter = ((ParameterPort)typeable).getParameter();
            _markJSONmode(parameter, JSONmode);
        } else if (JSONmode) {
            // Argument must be a simple port.
            _markJSONmode(typeable, JSONmode);
        }
        if (parameter != null) {
            /** The following doesn't work. Not persistent.
            parameter.setStringMode(true);
            */
            /* SingletonAttribute mark = */ new SingletonAttribute(parameter, "_stringMode");
            // Mark this implied; otherwise it's existence forces moml export.
            // NO! This makes it non-persistent, so when the model is saved and then
            // re-opened, it will give an error that it cannot evaluate the value
            // because it does not know to interpret it as an arbitrary string rather
            // an expression.
            // mark.setDerivedLevel(1);
            _markJSONmode(parameter, JSONmode);
        }
    }

    /** Invoke the specified function after the specified amount of
     *  time.  Unlike the public method, this method uses the
     *  specified id.  The time will be added to the current time of
     *  the director, and fireAt() request will be made of the
     *  director. If the director cannot fulfill the request, this
     *  method will throw an exception. Note that if you want
     *  real-time behavior, then the director's synchronizeToRealTime
     *  parameter needs to be set to true.
     *  @param function The function to invoke.
     *  @param milliseconds The number of milliseconds in the future to invoke it.
     *  @param id The id for the callback function.
     *  @return A unique ID for this callback.
     *  @exception IllegalActionException If the director cannot respect the request.
     */
    private synchronized void _setTimeout(final Runnable function,
            int milliseconds, Integer id) throws IllegalActionException {
        Director director = getDirector();
        Time currentTime = director.getModelTime();
        if (currentTime == null) {
            throw new IllegalActionException(this,
                    "Model is not running. Please set timeouts and intervals in initialize.");
        }
        // If the director has a synchronizeToRealTime parameter, check that its
        // value is true.
        Attribute sync = director.getAttribute("synchronizeToRealTime");
        if (sync instanceof Parameter) {
            Token token = ((Parameter)sync).getToken();
            if (token instanceof BooleanToken && !((BooleanToken)token).booleanValue()) {
                try {
                    MessageHandler.warning("Using a timeout in JavaScript, but the director's"
                            + " synchronizeToRealTime parameter is set to false."
                            + " To get real-time behavior, set it to true.");
                } catch (CancelException e) {
                    throw new IllegalActionException(this, "Execution cancelled");
                }
            }
        }
        final Time callbackTime = currentTime.add(milliseconds * 0.001);

        Time responseTime = director.fireAt(this, callbackTime);
        if (!responseTime.equals(callbackTime)) {
            throw new IllegalActionException(this,
                    "Director is unable to fire this actor at the requested time "
                            + callbackTime
                            + ". It replies that it will fire the actor at "
                            + responseTime + ".");
        }

        // Record the callback function indexed by ID.
        _pendingTimeoutFunctions.put(id, function);

        // Record the ID of the timeout indexed by time.
        Queue<Integer> ids = _pendingTimeoutIDs.get(callbackTime);
        if (ids == null) {
            ids = new LinkedList<Integer>();
            _pendingTimeoutIDs.put(callbackTime, ids);
        }
        ids.add(id);
    }

    /** Convert an accessor type definition into a Ptolemy type.
     *  If the specified type is "JSON" or "string", then the parameter
     *  is put into string mode.
     *  @param type The type designation.
     *  @param typeable The object to be typed.
     *  @return The Ptolemy II type.
     *  @exception IllegalActionException If the type is not supported.
     */
    private Type _typeAccessorToPtolemy(String type, NamedObj typeable)
            throws IllegalActionException, NameDuplicationException {
        if (type.equals("number")) {
            return (BaseType.DOUBLE);
        } else if (type.equals("JSON")) {
            _setStringMode(typeable, true);
            return (BaseType.STRING);
        } else if (type.equals("int")) {
            return (BaseType.INT);
        } else if (type.equals("string")) {
            _setStringMode(typeable, false);
            return (BaseType.STRING);
        } else if (type.equals("boolean")) {
            return (BaseType.BOOLEAN);
        } else if (type.equals("select")) {
            _setStringMode(typeable, false);
            return (BaseType.STRING);
        } else {
            // Support any Ptolemy type as a last resort.
            // The type of the port will have to be set manually.
            Type ptType = Constants.nameToType(type);
            if (ptType == null) {
                // Print out all the acceptable types.
                List types = new ArrayList(Constants.types().keySet());
                types.add("number");
                types.add("JSON");
                types.add("select");
                Collections.sort(types);
                throw new IllegalActionException(this, "Unsupported type: " + type
                                                 + ".  Should be one of: "
                                                 + Arrays.toString(types.toArray()));
            }
            return ptType;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                        Private Variables                  ////

    /** The director thread. This is set in initialize() and unset in wrapup. */
    private Thread _directorThread;

    /** True while the actor is firing, false otherwise. */
    private boolean _inFire;

    /** Queue containing callback functions that are to be invoked in the fire
     *  method as soon as possible.
     */
    private ConcurrentLinkedQueue<Runnable> _pendingCallbacks = new ConcurrentLinkedQueue<Runnable>();

    /** Map from timeout ID to pending timeout functions. */
    private Map<Integer, Runnable> _pendingTimeoutFunctions = new HashMap<Integer, Runnable>();

    /** Map from timeout time to pending timeout IDs. */
    private Map<Time, Queue<Integer>> _pendingTimeoutIDs = new HashMap<Time, Queue<Integer>>();

    /** Map of proxies for ports and parameters. */
    private HashMap<NamedObj, PortOrParameterProxy> _proxies;

    /** Map of proxies for ports and parameters by name */
    private HashMap<String, PortOrParameterProxy> _proxiesByName;

    /** Flag indicating that timeoutID should be removed. */
    private boolean _removePendingIntervalFunction = true;

    /** Used to indicate that an output port was created with a
     * spontaneous argument of true or false.
     */
    private static String _SPONTANEOUS = "_spontaneous";

    /** Count to give a unique handle to pending timeouts. */
    private int _timeoutCount = 0;

    ///////////////////////////////////////////////////////////////////
    ////                        Inner Classes                      ////

    /** Proxy for a port or parameter.
     *
     *  This is used to wrap ports and parameters for security
     *  reasons.  If we expose the port or parameter to the JavaScript
     *  environment, then the script can access all aspects of the
     *  model containing this actor. E.g., it can call getContainer()
     *  on the object.
     *
     *  This wrapper provides access to the port or parameter only via
     *  a protected method, which JavaScript cannot access.
     */
    public class PortOrParameterProxy {
        /** Construct a proxy.
         *  @param portOrParameter The object to be proxied.
         *  @exception IllegalActionException If the argument is neither a port nor a parameter.
         */
        protected PortOrParameterProxy(NamedObj portOrParameter)
                throws IllegalActionException {
            if (portOrParameter instanceof Variable) {
                _parameter = (Variable) portOrParameter;
            } else if (portOrParameter instanceof TypedIOPort) {
                _port = (TypedIOPort) portOrParameter;
            } else {
                throw new IllegalActionException(JavaScript.this,
                        portOrParameter,
                        "Cannot create a proxy for something that is neither a port nor a parameter.");
            }
        }

        /** Return true if the port or parameter value is required to be JSON.
         *  @return True for JSON ports and parameters.
         */
        public boolean isJSON() {
            if (_parameter != null) {
                return (_parameter.getAttribute("_JSON") != null);
            } else {
                return (_port.getAttribute("_JSON") != null);
            }
        }

        /** Get the current value of the input port or a parameter.
         *  If it is a ParameterPort, then retrieve the value
         *  from the corresponding parameter instead (the fire() method
         *  will have done update). This should now only be used by getParameter.
         *  @param channelIndex The channel index. This is ignored for parameters.
         *  @return The current value of the input or parameter, or null if there is none.
         *  @exception IllegalActionException If the port is not an input port
         *   or retrieving the value fails.
         */
        public Token get(int channelIndex) throws IllegalActionException {
            if (_parameter != null) {
                Token token = _parameter.getToken();
                if (token == null || token.isNil()) {
                    return null;
                }
                return token;
            }
            if (_port instanceof ParameterPort) {
                Token token = ((ParameterPort) _port).getParameter().getToken();
                if (token == null || token.isNil()) {
                    return null;
                }
                return token;
            }
            throw new IllegalActionException(
                    JavaScript.this, "Use provideInput() rather than get()");
        }

        /** Expose the send() method of the port.
         *  @param channelIndex The channel index.
         *  @param data The token to send.
         *  @param value The JavaScript value to pass back when the send actually occurs.
         *  @exception IllegalActionException If this is a proxy for a parameter or if sending fails.
         *  @exception NoRoomException If there is no room at the destination.
         */
        public void send(int channelIndex, Token data, Object value) throws NoRoomException,
                IllegalActionException {
            if (_port == null) {
                throw new IllegalActionException(JavaScript.this,
                        "Cannot call send on a parameter: "
                                + _parameter.getName() + ". Use setParameter().");
            }
            // If the model is not in a phase of execution where it can send (in initialize()
            // or during execution), then do not send the token. Instead, send messages
            // to the debug channel and stderr. This is likely occurring in a callback that
            // is being invoked after the model has terminated or while the model is in its
            // wrapup phase.
            //
            // NOTE: There is a window between the last firing of this actor and the
            // invocation of wrapup() where _executing == true, but nevertheless, there
            // will be no opportunity to actually send the data token.  In this case,
            // we queue the token and deal with the problem in the wrapup() method.
            //
            // NOTE: This has to be outside the synchronized block! Do not try to
            // get the lock because a Vertx thread might be trying to
            // send while the actor has already obtained the lock in
            // wrapup() and may be calling WebSocketHelper.close(). See
            // https://chess.eecs.berkeley.edu/ptolemy/wiki/Ptolemy/Deadlock
            //
            // FIXME: The above NOTE appears to be wrong.
            // WebSocketHelper is holding a lock on _actor
            // when it calls this in DataHandler.
            if (!_executing) {
                String message = "WARNING: Invoking send() too late (probably in a callback), so "
                        + getName()
                        + " is not able to send the token "
                        + data
                        + " to the output "
                        + _port.getName()
                        + ". Token is discarded.";
                if (_debugging) {
                    _debug(message);
                } else {
                    System.err.println(message);
                }
                return;
            }

            // FIXME: Race condition. wrapup() could be invoked now, setting _executing == false
            // and then acquiring a lock on this and calling WebSocketHelper.close().
            // So we may still have a deadlock risk.
            synchronized (JavaScript.this) {
                if (!_port.isOutput()) {
                    if (!_port.isInput()) {
                        throw new IllegalActionException(JavaScript.this,
                                "Cannot send to a port that is neither an input nor an output.");
                    }
                    // Port is an input.
                    // Record the token value to be provided in get().
                    if (_localInputTokens == null) {
                        _localInputTokens = new LinkedList<Token>();
                    }
                    _localInputTokens.add(data);
                    if (_inFire) {
                        // The following uses the current model time of the director,
                        // unlike the director's fireAtCurrentTime() method, which could use the current
                        // real time if the director is synchronized to real time.
                        _fireAtCurrentTime();
                    } else {
                        // The following will use real time if the director is synchronized to
                        // real time. This seems reasonable if we are not currently in the fire method.
                        getDirector().fireAtCurrentTime(JavaScript.this);
                    }
                } else if (_inFire) {
                    // Currently firing. Can go ahead and send data.
                    if (_debugging) {
                        _debug("Sending " + data + " to " + _port.getName());
                    }
                    _port.send(channelIndex, data);
                    // Invoke the basic send() functionality of commonHost so that latestOutput()
                    // works. Make sure the context is this, not the prototype.
                    // Do not do this if sending to my own input, however.
                    // The JavaScript actor handles that.
                    _invokeMethodInContext(_instance, "superSend", _port.getName(), value, channelIndex);
                } else {
                    // Not currently firing.
                    // Enqueue runnable object to be invoked upon the next firing.

                    // Request a firing at the current time.
                    // If the director is synchronized to real time, that will be real
                    // time, not the current model time. This allows the director to advance
                    // time before processing this output.
                    // In this case, we _do_ want to use the director's
                    // fireAtCurrentTime(), and not our _fireAtCurrentTime()
                    // method, because we are not in firing, so if we are
                    // synchronized to real time, we want the firing to
                    // occur later than the current model time, at a time
                    // matching current real time.
                    final Time now = getDirector().fireAtCurrentTime(JavaScript.this);
                    final Integer id = Integer.valueOf(_timeoutCount++);
                    final Runnable function = new DeferredSend(this, channelIndex, data, value);

                    // Record the callback function indexed by ID.
                    _pendingTimeoutFunctions.put(id, function);

                    // Record the ID of the timeout indexed by time.
                    Queue<Integer> ids = _pendingTimeoutIDs.get(now);
                    if (ids == null) { // FIXME: again, why not initialize in constructor instead?
                        ids = new LinkedList<Integer>();
                        _pendingTimeoutIDs.put(now, ids);
                    }
                    ids.add(id);
                }
            }
        }

        /** Set the current value of the parameter.
         *  @param token The value of the parameter.
         *  @exception IllegalActionException If the set fails or if this is a proxy for a port.
         */
        public void set(Token token) throws IllegalActionException {
            if (_parameter == null) {
                if (_port instanceof ParameterPort) {
                    _parameter = ((ParameterPort)_port).getParameter();
                } else {
                    if (!_port.isInput()) {
                        throw new IllegalActionException(JavaScript.this,
                                "Cannot set the default value a port that is not an input: " + _port.getName());
                    }
                    // Ideally, we would change port from an ordinary port to a ParameterPort.
                    // Instead, we just set the default value of the port.
                    _port.defaultValue.setToken(token);
                    return;
                }
            }
            _parameter.setToken(token);
        }

        /** Return the name of the proxied port or parameter.
         *  @return The name of the proxied port or parameter
         */
        @Override
        public String toString() {
            if (_port != null) {
                return _port.getName();
            }
            return _parameter.getName();
        }

        /////////////////////////////////////////////////////////////////
        ////           Protected variables

        /** A list of tokens that this JavaScript actor has sent to its own input. */
        protected List<Token> _localInputTokens;

        /** The parameter that is proxied, or null if it's a port. */
        protected Variable _parameter;

        /** The port that is proxied, or null if it's a parameter. */
        protected TypedIOPort _port;
    }

    /** Runnable object intended to be run inside of the fire method to
     *  send out a token that was attempted to be send out at an earlier
     *  time, asynchronous with fire.
     */
    public class DeferredSend implements Runnable {

            /** Construct an object that defers a send operation.
             * @param proxy A proxy corresponding to the port or parameter.
             * @param channelIndex The channel to send data on.
             * @param data The data token to send through the port or update the parameter with.
         * @param value The JavaScript value to pass back when the send actually occurs.
             */
        public DeferredSend(
                PortOrParameterProxy proxy, int channelIndex, Token data, Object value) {
           _proxy = proxy;
           _channelIndex = channelIndex;
           _token = data;
           _value = value;
        }

        /** Invoke send on the port or parameter proxy.
         */
        public void run() {
            try {
                _proxy.send(_channelIndex, _token, _value);
            } catch (KernelException e) {
                error("Send to "
                        + _proxy._port.getName()
                        + " failed: "
                        + e.getMessage());
            }
        }
        /** The output channel on the port to send data on. */
        private int _channelIndex;
        /** The data to send. */
        private Token _token;
        /** The proxy for the port to send data through. */
        private PortOrParameterProxy _proxy;
        /** The JavaScript value to pass back. */
        private Object _value;
        }
}
