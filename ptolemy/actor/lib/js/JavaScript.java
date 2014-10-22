/* AnExecute a script in JavaScript.

   Copyright (c) 2014 The Regents of the University of California.
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
package ptolemy.actor.lib.js;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.OAuthProviderType;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.FunctionObject;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.WrappedException;
import org.ptolemy.ptango.lib.HttpRequest;
import org.ptolemy.ptango.lib.HttpResponse;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.BrowserLauncher;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.actor.util.Time;
import ptolemy.data.ActorToken;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.LongToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.graph.Inequality;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.MessageHandler;

///////////////////////////////////////////////////////////////////
//// JavaScript

/**
   Execute a script in JavaScript that can read inputs and parameters,
   perform calculations, and write outputs. The script may be provided
   as the textual value of the <i>script</i> parameter, or as an input
   on the <i>scriptIn</i> port.
   If it is an input, then it can be different on each firing.
   If it is a parameter, then the script is compiled in the initialize()
   method, and any changes made to it are ignored until the next initiatlization
   of the model.
   <p>
   To use this actor, add input and output ports and parameters, and specify
   a script.
   Your script should define one or more of the following functions:
   <ul>
   <li> <b>initialize</b>. This function is invoked each time this actor
   is initialized, or if an initialize function is provided on the
   <i>scriptIn</i> input port, then it will be invoked when this
   actor fires and consumes that input. This function can read
   parameter values using valueOf() (see below), normally there
   will be no inputs, so it should not read inputs.
   </li>
   <li> <b>fire</b>. This function is invoked each time this actor fires.
   It can read parameters, read inputs using get(), and write outputs
   using send(). Note that all inputs from all ports are consumed during
   a firing, even if the script does not contain a call to get() for
   a port. Multiple calls to get() during a
   firing will return the same value.
   </li>
   <li> <b>wrapup</b>. This function is invoked at the end of execution of
   of the model. It can read parameters, but normally should not
   read inputs nor write outputs.
   </li>
   </ul>
   <p>
   Usually, you will need to explicitly specify the types
   of the output ports. Alternatively, you can enable backward
   type inference on the enclosing model, and the types of output
   ports will be inferred by how the data are used.
   <p>
   You may also need to set the type of input ports. Usually, forward
   type inference will work, and the type of the input port will be based
   on the source of data. However,
   if the input comes from an output port whose output is undefined,
   such as JSONToToken or HttpRequestHandler, then you may want to
   enable backward type inference, and specify here the type of input
   that your script requires.
   <p>
   The context in which your functions run provide the following methods:
   <ul>
   <li> alert(string): pop up a dialog with the specified message.
   <li> clearTimeout(int): clear a timeout with the specified handle.
   <li> error(string): throw an IllegalActionException with the specified message.
   <li> get(port, n): get an input from a port on channel n (return null if there is no input).
   <li> httpRequest(url, method, properties, body, timeout): HTTP request (GET, POST, PUT, etc.)
   <li> print(string): print the specified string to the console (standard out).
   <li> readURL(string): read the specified URL and return its contents as a string (HTTP GET).
   <li> send(value, port, n): send a value to an output port on channel n
   <li> setTimeout(function, int): set the function to execute after specified time and return handle.
   <li> valueOf(parameter): retrieve the value of a parameter.
   </ul>
   The last argument of get() and send() is optional.
   If you leave it off, the channel number will be assumed to be zero
   (designating the first channel connected to the port).
   <p>
   The following example script calculates the factorial of the input.
   <pre>
   function fire() {
   var value = get(input);
   if (value < 0) {
   error("Input must be greater than or equal to 0.");
   } else {
   var total = 1;
   while (value > 1) {
   total *= value;
   value--;
   }
   send(total, output);
   }
   }
   </pre>
   <p>
   Your script may also store values from one firing to the next, or from
   initialization to firing.  For example,
   <pre>
   var init;
   function initialize() {
   init = 0;
   }
   function fire() {
   init = init + 1;
   send(init, output);
   }
   </pre>
   will send a count of firings to the output named "output".
   <p>
   Notice that you may name ports or parameters of this actor in such
   a way as to shadow objects in the global scope. For example,
   JavaScript provides a JSON object that you can use to operate
   on JSON strings, for example with the function JSON.parse().
   However, if you name a port or parameter "JSON", then any reference
   to JSON in your fire() method, for example, will refer to the port
   or parameter, and not to the global object.  To explicitly refer
   to the global object, use the syntax "this.JSON".
   <p>
   In addition, the symbol "actor" is defined to be the instance of
   this actor. In JavaScript, you can invoke methods on it.
   For example, the JavaScript
   <pre>
   actor.toplevel().getName();
   </pre>
   will return the name of the top-level composite actor containing
   this actor.
   <p>
   Not all Ptolemy II data types translate naturally to
   JavaScript types. This actor converts Ptolemy types int,
   double, string, and boolean to and from equivalent JavaScript
   types. In addition, arrays are converted to native
   JavaScript arrays. Ptolemy II records are converted into
   JavaScript objects, and JavaScript objects with enumerable
   properties into records.
   <p>
   For other Ptolemy II types, the script will see the
   corresponding Token object.  For example, if you send
   a number of type long to an input of a JavaScript actor,
   the script (inside a fire() function):
   <pre>
   var value = get(input);
   print(value.getClass().toString());
   </pre>
   will print on standard out the string
   <pre>
   "class ptolemy.data.LongToken"
   </pre>
   JavaScript does not have a long data type (as of this writing), so instead the
   get() call returns a JavaScript Object wrapping the Ptolemy II
   LongToken object. You can then invoke methods on that token,
   such as getClass(), as done above.
   <p>
   Scripts can instantiate Java classes and invoke methods on them.
   For example, the following script will build a simple Ptolemy II model:
   <pre>
   importPackage(Packages.ptolemy.actor);
   importClass(Packages.ptolemy.actor.lib.Ramp);
   importClass(Packages.ptolemy.actor.lib.FileWriter);
   importClass(Packages.ptolemy.domains.sdf.kernel.SDFDirector);

   var toplevel = new TypedCompositeActor();
   var ramp = new Ramp(toplevel, "ramp");
   var writer = new FileWriter(toplevel, "writer");

   toplevel.connect(ramp.output, writer.input);

   var director = new SDFDirector(toplevel, "SDFDirector");
   director.iterations.setExpression("10");
   </pre>
   You can even send this out on an output port.
   For example,
   <pre>
   send(toplevel, output, 0);
   </pre>
   where "output" is the name of the output port.
   <p>
   Subclasses of this actor may put it in "restricted" mode, which
   limits the functionality as follows:
   <ul>
   <li> The "actor" variable (referring to this instance of the actor) does not get created.
   <li> The readURL method only supports the HTTP protocol (in particular,
   it does not support the "file" protocol, and hence cannot access local files).
   </ul>
   </p>

   @author Edward A. Lee
   @version $Id$
   @since Ptolemy II 10.0
   @Pt.ProposedRating Yellow (eal)
   @Pt.AcceptedRating Red (bilung)
 */
public class JavaScript extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  In addition to invoking the base class constructors, construct
     *  the <i>init</i> and <i>step</i> parameter and the <i>step</i>
     *  port. Initialize <i>init</i>
     *  to IntToken with value 0, and <i>step</i> to IntToken with value 1.
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

        script = new StringAttribute(this, "script");

        // Set the visibility to expert, as casual users should
        // not see the script.  This is particularly true if one
        // installs an actor that is an instance of this with a
        // particular script in the library.
        // FIXME: Not until we have the editors.
        // script.setVisibility(Settable.EXPERT);

        // Create the script input.
        // NOTE: This cannot be a PortParameter because it can't be a string-mode
        // parameter. The parameter syntax uses $var substitution, which is likely
        // to conflict with JavaScript, particularly if jQuery is used.
        scriptIn = new TypedIOPort(this, "scriptIn", true, false);
        scriptIn.setTypeEquals(BaseType.STRING);
        StringAttribute cardinal = new StringAttribute(scriptIn, "_cardinal");
        cardinal.setExpression("SOUTH");

        // initialize the script to provide an empty template:
        script.setExpression("// Put your JavaScript program here.\n"
                + "// Add ports and parameters.\n"
                + "// Define JavaScript functions initialize(), fire(), and/or wrapup().\n"
                + "// Use valueOf(parameterName) to refer to parameters.\n"
                + "// In the fire() function, use get(parameterName, channel) to read inputs.\n"
                + "// Send to output ports using send(value, portName, channel).\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The script to execute when this actor fires. */
    public StringAttribute script;

    /** Alternative way to provide a script to execute when this actor fires.
     *  This input port has type string.
     *  If this is connected and provided an input, then the
     *  script provided as input is executed instead of the one given
     *  by the script parameter.
     */
    public TypedIOPort scriptIn;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        JavaScript newObject = (JavaScript) super.clone(workspace);
        newObject._inputTokens = new HashMap<IOPort, HashMap<Integer, Token>>();
        newObject._outputTokens = null;

        newObject._pendingTimeoutFunctions = null;
        newObject._pendingTimeoutIDs = null;

        return newObject;
    }

    /** Execute the script, handling inputs and producing outputs as described.
     *  <ol>
     *  <li>
     *  First, send any outputs that have been queued to be sent by calling send()
     *  from outside any firing of this JavaScript actor.
     *  <li>
     *  Next, read all inputs, making a record of which ones have new tokens.
     *  <li>
     *  If there is a new token on the scriptIn input port, then replace the current
     *  script with the one specified on that port. Any previously defined methods
     *  such as fire() will be replaced or deleted (if the new script does not have one).
     *  If the new script has an initialize() method, then invoke that method.
     *  <li>
     *  Next, if the current script has a fire() method, invoke that method.
     *  <li>
     *  Next, if any input has a new token and there is a method bound to that port
     *  via the setInputHandler() method, then invoke that method.
     *  Such methods will be invoked in the order in which handleInput() was invoked.
     *  <li>
     *  Next, if setTimeout() has been called, and the current time of the director
     *  matches the time specified in setTimeout(), then invoke the function specified
     *  in setTimeout().
     *  </ol>
     *  <p>
     *  @exception IllegalActionException If calling send() or super.fire()
     *  throws it.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        // If there is an input at scriptIn, evaluate that script.
        try {
            if (scriptIn.getWidth() > 0 && scriptIn.hasToken(0)) {
                // A script is provided as input.
                String scriptValue = ((StringToken) scriptIn.get(0))
                        .stringValue();

                // FIXME: Need to delete any side effects from previously read scripts,
                // such as function definitions and bindings. Timeouts?
                // See the initialize() method.

                // Compile the script.
                _compiledScript = _context.compileString(scriptValue,
                        getName(), 1, null);

                // Execute the script (Note that the script defines functions and variables;
                // the functions are not invoked here. Just defined.)
                _compiledScript.exec(Context.getCurrentContext(), _scope);

                // Execute the initialize() function, if it exists.
                Object initializeFunction = _scope.get("initialize", _scope);
                if (initializeFunction instanceof Function) {
                    ((Function) initializeFunction).call(
                            Context.getCurrentContext(), _scope, _global, null);
                }
            }
        } catch (WrappedException ex) {
            Throwable original = ex.getWrappedException();
            throw new IllegalActionException(this, ex,
                    "Exception during executing script at line "
                            + ex.lineNumber() + ".\n" + original.getMessage());
        }

        // Send out buffered outputs, if there are any.
        // This has to be synchronized in case a callback calls send() at the
        // same time.
        synchronized (this) {
            if (_outputTokens != null) {
                for (IOPort port : _outputTokens.keySet()) {
                    HashMap<Integer, List<Token>> tokens = _outputTokens
                            .get(port);
                    for (Map.Entry<Integer, List<Token>> entry : tokens
                            .entrySet()) {
                        List<Token> queue = entry.getValue();
                        if (queue != null) {
                            for (Token token : queue) {
                                port.send(entry.getKey(), token);
                            }
                        }
                    }
                }
                _outputTokens.clear();
            }
        }

        // Update any port parameters that have been added.
        for (PortParameter portParameter : attributeList(PortParameter.class)) {
            // FIXME: Record which ones have new tokens.
            portParameter.update();
        }

        // Read all the available inputs.
        _inputTokens.clear();
        for (IOPort input : this.inputPortList()) {
            // Skip the scriptIn input.
            if (input == scriptIn) {
                continue;
            }
            // Skip ParameterPorts, as those are handled by the update() call above.
            if (input instanceof ParameterPort) {
                continue;
            }
            HashMap<Integer, Token> tokens = new HashMap<Integer, Token>();
            for (int i = 0; i < input.getWidth(); i++) {
                if (input.hasToken(i)) {
                    // FIXME: Record that there is a token.
                    tokens.put(i, input.get(i));
                }
            }
            _inputTokens.put(input, tokens);
        }

        try {
            // Mark that we are in the fire() method, enabling outputs to be
            // sent immediately.
            // Synchronize to ensure that this function invocation is atomic
            // w.r.t. to any callbacks.
            synchronized (JavaScript.this) {
                _inFire = true;
                try {
                    // If there is a fire() function, invoke it.
                    Object fireFunction = _scope.get("fire", _scope);
                    if (fireFunction instanceof Function) {
                        ((Function) fireFunction).call(
                                Context.getCurrentContext(), _scope, _global,
                                _EMPTY_ARGS);
                    }

                    // FIXME: If setInputHandler() has been called, invoke the appropriate methods.

                    // Handle timeout requests that match the current time.
                    if (_pendingTimeoutIDs != null) {
                        // If current time matches pending timeout requests, invoke them.
                        Time currentTime = getDirector().getModelTime();
                        List<Integer> ids = _pendingTimeoutIDs.get(currentTime);
                        if (ids != null) {
                            for (Integer id : ids) {
                                Function function = _pendingTimeoutFunctions
                                        .get(id);
                                if (function != null) {
                                    function.call(Context.getCurrentContext(),
                                            _scope, _global, _EMPTY_ARGS);
                                    _pendingTimeoutFunctions.remove(id);
                                }
                            }
                            _pendingTimeoutIDs.remove(currentTime);
                        }
                    }
                } finally {
                    _inFire = false;
                }
            }
        } catch (WrappedException ex) {
            Throwable original = ex.getWrappedException();
            throw new IllegalActionException(this, ex,
                    "Exception during executing script at line "
                            + ex.lineNumber() + ".\n" + original.getMessage());
        }
    }

    /** Register the ports and parameters with the JavaScript engine.
     *  @exception IllegalActionException If calling send() or super.fire()
     *  throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _executing = true;

        _pendingTimeoutFunctions = null;
        _pendingTimeoutIDs = null;

        // Expose the ports and parameters by name as JavaScript variables.
        for (TypedIOPort port : portList()) {
            // Do not convert the scriptIn port to a JavaScript variable.
            if (port == scriptIn) {
                continue;
            }
            Object jsObject;
            if (_restricted) {
                jsObject = Context.javaToJS(new PortProxy(port), _scope);
            } else {
                jsObject = Context.javaToJS(port, _scope);
            }
            _scope.put(port.getName(), _scope, jsObject);
        }
        for (Parameter parameter : attributeList(Parameter.class)) {
            Object jsObject;
            if (_restricted) {
                jsObject = Context.javaToJS(new ParameterProxy(parameter),
                        _scope);
            } else {
                jsObject = Context.javaToJS(parameter, _scope);
            }
            _scope.put(parameter.getName(), _scope, jsObject);
        }

        // Compile the script to execute at run time.
        String scriptValue = script.getValueAsString();
        _compiledScript = _context.compileString(scriptValue, getName(), 1,
                null);

        // Execute the script (Note that the script defines functions and variables;
        // the functions are not invoked here. Just defined.)
        _compiledScript.exec(Context.getCurrentContext(), _scope);

        // Execute the initialize() function, if it exists.
        // Synchronize to ensure that this is atomic w.r.t. any callbacks.
        // Note that the callbacks might be invoked after a model has terminated
        // execution.
        synchronized (JavaScript.this) {
            // Clear any queued output tokens.
            if (_outputTokens != null) {
                _outputTokens.clear();
            }

            Object initializeFunction = _scope.get("initialize", _scope);
            if (initializeFunction instanceof Function) {
                ((Function) initializeFunction).call(
                        Context.getCurrentContext(), _scope, _global,
                        _EMPTY_ARGS);
            }
        }
    }

    /** Return true if the specified string is not a JavaScript keyword and is a valid JavaScript identifier.
     *  @param identifier The proposed name.
     *  @return True if it is a valid identifier name.
     */
    public static boolean isValidIdentifierName(String identifier) {
        // Pathetically, neither JavaScript nor Rhino provide this method.
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
        return !_KEYWORDS.contains(identifier);
    }

    /** Set the state to equal the value of the <i>init</i> parameter.
     *  The state is incremented by the value of the <i>step</i>
     *  parameter on each iteration (in the postfire() method).
     *  @exception IllegalActionException If the parent class throws it.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        // Create a context for the current thread.
        _context = Context.enter();
        // If you want to include standard Rhino methods, like print, do this:
        // _context.initStandardObjects();

        // Create a scope for this actor.
        _scope = new ImporterTopLevel(Context.getCurrentContext());
        // Create also also a global scope so that if the actor shadows any
        // global objects, they can still be referenced using this.objectName.
        _global = new ImporterTopLevel(Context.getCurrentContext());

        // Define built-in methods in the context.
        PtolemyJavaScript scriptable = new PtolemyJavaScript();
        scriptable.setParentScope(_scope);
        // Get a reference to the instance method to be made available in JavaScript as a global function.
        try {
            // Create an array of method names and a matching array of arrays of argument types
            // for functions that will be provided in the JavaScript environment.
            // Keep these alphabetical.
            String[] methodNames = { "alert", "clearTimeout", "error", "get",
                    "httpRequest", "localHostAddress", "openBrowser", "print",
                    "readProtectedURL", "readURL", "requestAccess",
                    "requestAuth", "send", "setTimeout", "socketX", "valueOf" };
            Class[][] args = {
                    { String.class }, // alert
                    { Integer.class }, // clearTimeout
                    { String.class }, // error
                    { NativeJavaObject.class, Double.class }, // get
                    { String.class, String.class, NativeObject.class,
                        String.class, Integer.class }, // httpRequest
                        {}, // localHostAddress
                        { String.class }, // openBrowser
                        { String.class }, // print
                        { String.class, String.class }, // readProtectedURL
                        { String.class }, // readURL
                        { String.class, String.class, String.class, String.class,
                            String.class }, // requestAccess
                            { String.class, String.class, String.class, Boolean.class }, // requestAuth
                            { Object.class, NativeJavaObject.class, Double.class }, // send
                            { Function.class, Integer.class }, // setTimeout
                            { String.class, NativeObject.class, NativeJavaObject.class }, // socketX
                            { NativeJavaObject.class }, // valueOf

            };
            int count = 0;
            for (String methodName : methodNames) {
                Method scriptableInstanceMethod = PtolemyJavaScript.class
                        .getMethod(methodName, args[count]);
                FunctionObject scriptableFunction = new PtolemyFunctionObject(
                        methodName, scriptableInstanceMethod, scriptable);
                // Make it accessible within the scriptExecutionScope.
                _scope.put(methodName, _scope, scriptableFunction);
                count++;
            }

            // This actor is exposed as an object named "actor", unless the actor is restricted.
            if (!_restricted) {
                Object jsObject = Context.javaToJS(this, _scope);
                _scope.put("actor", _scope, jsObject);
            }
        } catch (Throwable throwable) {
            throw new IllegalActionException(this, throwable,
                    "Failed to create built-in JavaScript methods.");
        }
    }

    /** Execute the wrapup function, if it is defined, and exit the context for this thread.
     *  @exception IllegalActionException If the parent class throws it.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        try {
            // If there are open sockets, disconnect.
            if (_openSockets != null && _openSockets.size() > 0) {
                for (SocketIO socket : _openSockets) {
                    socket.disconnect();
                }
                _openSockets.clear();
            }
            // If there is a wrapup() function, invoke it.
            Object wrapupFunction = _scope.get("wrapup", _scope);
            // Synchronize so that this invocation is atomic w.r.t. any callbacks.
            synchronized (JavaScript.this) {
                if (wrapupFunction instanceof Function) {
                    ((Function) wrapupFunction).call(
                            Context.getCurrentContext(), _scope, _global,
                            _EMPTY_ARGS);
                }
            }
            // This is static because the context depends on the current thread.
            // So this exits the context associated with the current thread.
            Context.exit();
        } finally {
            _executing = false;
        }
        super.wrapup();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return null, becuase the default type constraints, where output
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

    ///////////////////////////////////////////////////////////////////
    ////                         protected fields                  ////

    /** Compiled JavaScript. */
    protected Script _compiledScript;

    /** Rhino context. */
    protected Context _context;

    /** Empty argument list for JavaScript function invocation. */
    protected final static Object[] _EMPTY_ARGS = new Object[] {};

    /** True while the model is executing (between initialize() and
     *  wrapup(), inclusive.
     */
    protected boolean _executing;

    /** Global scope, unpolluted by anything in this actor. */
    protected Scriptable _global;

    /** Keywords. */
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

    /** If set to true in the constructor of a base class, then put this actor in "restricted"
     *  mode.  This limits the functionality as described in the class comment.
     */
    protected boolean _restricted = false;

    /** Scope in which to evaluate scripts. */
    protected Scriptable _scope;

    ///////////////////////////////////////////////////////////////////
    ////                        Private Variables                  ////

    /** True while the actor is firing, false otherwise. */
    private boolean _inFire;

    /** Buffer for all input tokens before calling the fire function
     *  in the script. Calls to get in the script will then retrieve
     *  the value from this buffer instead of actually calling get on
     *  the port.
     */
    private HashMap<IOPort, HashMap<Integer, Token>> _inputTokens = new HashMap<IOPort, HashMap<Integer, Token>>();

    /** List of open sockets. */
    List<SocketIO> _openSockets;

    /** Buffer for output tokens that are produced in a call to send
     *  while the actor is not firing. This makes sure that actors can
     *  spontaneously produce outputs.
     */
    private HashMap<IOPort, HashMap<Integer, List<Token>>> _outputTokens;

    /** Map from timeout ID to pending timeout functions. */
    private Map<Integer, Function> _pendingTimeoutFunctions;

    /** Map from timeout time to pending timeout IDs. */
    private Map<Time, List<Integer>> _pendingTimeoutIDs;

    /** Count to give a unique handle to pending timeouts. */
    private int _timeoutCount = 0;

    ///////////////////////////////////////////////////////////////////
    ////                        Inner Classes                      ////

    /** Proxy for a parameter. This is used to wrap parameters for security
     *  reasons.  If we expose the port to the JavaScript environment,
     *  then the script can access all aspects of the model containing
     *  this actor. E.g., it can call getContainer() on the object.
     *  This wrapper provides access to the port only via a protected
     *  method, which JavaScript cannot access.
     */
    public static class ParameterProxy {
        /** Construct a parameter proxy.
         *  @param parameter The parameter to be proxied.
         */
        protected ParameterProxy(Parameter parameter) {
            _parameter = parameter;
        }

        /** Return the name of the proxied parameter.
         *  @return The name of the proxied parameter.
         */
        @Override
        public String toString() {
            return _parameter.getName();
        }

        /** The parameter that is proxied. */
        protected Parameter _parameter;
    }

    /** Proxy for a port. This is used to wrap ports for security
     *  reasons.  If we expose the port to the JavaScript environment,
     *  then the script can access all aspects of the model containing
     *  this actor. E.g., it can call getContainer() on the object.
     *  This wrapper provides access to the port only via a protected
     *  method, which JavaScript cannot access.
     */
    public static class PortProxy {
        /** Construct a port proxy.
         *  @param port The port to be proxied.
         */
        protected PortProxy(TypedIOPort port) {
            _port = port;
        }

        /** Return the name of the proxied port.
         *  @return The name of the proxied port.
         */
        @Override
        public String toString() {
            return _port.getName();
        }

        /** The port that is proxied. */
        protected TypedIOPort _port;
    }

    /** Container class for built-in methods.
     */
    @SuppressWarnings("serial")
    public class PtolemyJavaScript extends ScriptableObject {

        /** Alert the user with a message.
         *  @param message The message
         */
        public void alert(String message) {
            MessageHandler.message(message);
        }

        /** Clear the timeout with the specified handle, if it has not already executed.
         *  @param handle The timeout handle.
         *  @see #setTimeout(Function, Integer)
         */
        public void clearTimeout(Integer handle) {
            // NOTE: The handle for this timeout remains in the
            // _pendingTimeoutIDs map, but it is more efficient to remove
            // it from that map when the firing occurs.
            _pendingTimeoutFunctions.remove(handle);
        }

        /** Throw an IllegalActionException with the specified message.
         *  @param message The specified message.
         *  @exception IllegalActionException Always thrown.
         */
        public void error(String message) throws IllegalActionException {
            throw new IllegalActionException(JavaScript.this, message);
        }

        /** Get buffered inputs for a given input port.
         *  @param portWrapper A JavaScript wrapper for a Port.
         *  @param channel A channel number, or NaN to use the default (0).
         *  @return The buffered inputs.
         */
        public Object get(NativeJavaObject portWrapper, Double channel) {
                if (portWrapper == null) {
                throw new InternalErrorException(JavaScript.this, null,
                        "Invalid (null) port argument to get(port, channel).");
                }
            // In JavaScript, all numbers are doubles. So we have to convert
            // to an integer.
            int channelNumber = 0;
            // Tolerate null or NaN as an argument.
            // Interestingly, Rhino's implementation of JavaScript, if get(port) is
            // called with only one argument, does in fact call this method, passing
            // NaN as the second argument.
            // This seems unlikely to be robust, so I'm not documenting the "feature"
            // that you can leave off the channel number.
            if (channel != null && !channel.isNaN()) {
                if (channel.doubleValue() % 1 == 0) {
                    // The value is actually an integer.
                    channelNumber = channel.intValue();
                } else {
                    throw new InternalErrorException(JavaScript.this, null,
                            "Second argument to get(port, channel) is required to be an integer. Got "
                                    + channel);
                }
            }

            Object unwrapped = portWrapper.unwrap();
            // The port reference will be a PortProxy in restricted mode, and a port otherwise.
            if (unwrapped instanceof PortProxy) {
                unwrapped = ((PortProxy) unwrapped)._port;
            } else if (unwrapped instanceof ParameterProxy) {
                unwrapped = ((ParameterProxy) unwrapped)._parameter;
            }
            if (unwrapped instanceof TypedIOPort) {
                TypedIOPort port = (TypedIOPort) unwrapped;
                if (!port.isInput()) {
                    throw new InternalErrorException(JavaScript.this, null,
                            "Cannot get from " + port.getName()
                            + ", which is not an input port.");
                }
                try {
                    if (port.getWidth() < 1) {
                        // Port is not connected.
                        return null;
                    }
                } catch (KernelException e) {
                    throw new InternalErrorException(JavaScript.this, e,
                            "Failed to get the width of the port "
                                    + port.getName() + ".");
                }
                HashMap<Integer, Token> portEntry = _inputTokens.get(port);
                if (portEntry == null) {
                    return null;
                }
                return _wrapToken(portEntry.get(channelNumber));
            } else if (unwrapped instanceof PortParameter) {
                try {
                    PortParameter parameter = (PortParameter) unwrapped;
                    return _wrapToken(parameter.getToken());
                } catch (KernelException e) {
                    throw new InternalErrorException(JavaScript.this, e,
                            "Failed to get value of "
                                    + ((PortParameter) unwrapped).getName()
                                    + ".");
                }
            } else {
                throw new InternalErrorException(JavaScript.this, null,
                        "First argument of get() is required to be an input port. It is "
                                + unwrapped.toString() + ".");
            }
        }

        @Override
        public String getClassName() {
            return getClass().getName();
        }

        /** Make the specified HTTP request. For example, to perform the equivalent of
         *  readURL, you can do:
         *  <pre>
         *    httpRequest("http://ptolemy.org", "GET", null, "", 1000);
         *  </pre>
         *  which specifies a URL to read, the method for the read, no properties, no
         *  text to send, and a timeout of one second.
         *  @param url The URL to which to make the request.
         *  @param method One of OPTIONS, GET, HEAD, POST, PUT, DELETE, TRACE, or CONNECT.
         *  @param properties The HTTP property to set for the connection, or null to not
         *   give any. For example: ['Content-Type':'application/x-www-form-urlencoded']
         *  @param body The body of the request, or null if none.
         *  @param timeout The timeout for a connection or a read, in milliseconds, or 0 to have no timeout.
         *  @return The response to the request.
         *  @exception IOException If the request fails.
         */
        public String httpRequest(String url, String method,
                NativeObject properties, String body, Integer timeout)
                        throws IOException {
            // FIXME: Should have a version that takes a callback function for the response,
            // and where the response gives access to the return stream.  See Node.js http object.
            StringBuffer response = new StringBuffer();
            OutputStreamWriter writer = null;
            BufferedReader reader = null;
            String line = "";

            URL theURL = new URL(url);
            // If the actor is restricted, support only HTTP protocols.
            // FIXME: Should this also apply the usual browser same-source restriction?
            // Same as what?
            if (!theURL.getProtocol().equalsIgnoreCase("http")) {
                throw new SecurityException(
                        "Only HTTP requests are honored by httpRequest().");
            }
            HttpURLConnection connection = (HttpURLConnection) theURL
                    .openConnection();

            // Set all fields in the request header.
            if (properties != null) {
                Set<Object> keys = properties.keySet();
                if (keys != null && !keys.isEmpty()) {
                    for (Object key : keys) {
                        if (key instanceof String) {
                            Object value = properties.get(key);
                            if (value != null) {
                                connection.setRequestProperty((String) key,
                                        value.toString());
                            }
                        }
                    }
                }
            }

            // Specify request method (GET, POST, PUT...)
            connection.setRequestMethod(method);

            // If a timeout has been specified, set it.
            if (timeout >= 0) {
                connection.setConnectTimeout(timeout);
                connection.setReadTimeout(timeout);
            }

            // Send body if applicable.
            if (body != null && !body.equals("")) {
                connection.setDoOutput(true);
                writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write(body);
                writer.flush();
            }

            // Wait for response.
            reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));

            // Read response.
            String lineBreak = System.getProperty("line.separator");
            while ((line = reader.readLine()) != null) {
                response.append(line);
                if (!line.endsWith(lineBreak)) {
                    response.append(lineBreak);
                }
            }

            if (writer != null) {
                writer.close();
            }
            reader.close();

            // Return response.
            return response.toString();
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

        /** Open a WebSocket connection.
         *
         *  This method uses Enno Boland's Java implementation of a Socket.IO client,
         *  found at <a href="https://github.com/Gottox/socket.io-java-client">https://github.com/Gottox/socket.io-java-client</a>.
         *  <p>
         *  NOTE: This is a temporary placeholder method! This will go away. Please do not use it.
         * @deprecated Pending a better design.
         * @param url The URL of the WebSocket connection
         * @param query The query
         * @param portWrapper The port wrapper
         * @exception MalformedURLException If the URL is malformed.
         */
        @Deprecated
        public void socketX(String url, final NativeObject query,
                NativeJavaObject portWrapper) throws MalformedURLException {
            final SocketIO socket = new SocketIO(url);
            if (_openSockets == null) {
                _openSockets = new LinkedList<SocketIO>();
            }
            _openSockets.add(socket);
            socket.connect(new IOCallback() {
                @Override
                public void onMessage(JSONObject json, IOAcknowledge ack) {
                    try {
                        System.out.println("Server said:" + json.toString(2));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onMessage(String data, IOAcknowledge ack) {
                    System.out.println("Server said: " + data);
                }

                @Override
                public void onError(SocketIOException socketIOException) {
                    System.out.println("an Error occured");
                    socketIOException.printStackTrace();
                }

                @Override
                public void onDisconnect() {
                    System.out.println("Connection terminated.");
                }

                @Override
                public void onConnect() {
                    // FIXME: GATD-specific.
                    socket.emit("query", query);
                    System.out.println("Connection established");
                }

                @Override
                public void on(String event, IOAcknowledge ack, Object... args) {
                    System.out
                    .println("Server triggered event '" + event + "'");
                    IOPort port = (IOPort) getPort(event);
                    if (port == null) {
                        return;
                    }
                    try {
                        for (Object arg : args) {
                            Token token = _createToken(arg);
                            if (_inFire) {
                                if (_debugging) {
                                    _debug("Sending " + token + " to "
                                            + port.getName());
                                }
                                port.send(0, token);
                            } else {
                                if (!_executing) {
                                    // This is probably being called in a callback, but the model
                                    // execution has ended.
                                    throw new InternalErrorException(
                                            "Attempt to send "
                                                    + token
                                                    + " to "
                                                    + port.getName()
                                                    + ", but the model is not executing.");
                                }

                                // Not currently firing. Queue the tokens and request a firing.
                                // This should be being called in a callback that holds a
                                // synchronization lock, so synchronizing this isn't really
                                // necessary, but just in case...
                                synchronized (this) {
                                    if (_outputTokens == null) {
                                        _outputTokens = new HashMap<IOPort, HashMap<Integer, List<Token>>>();
                                    }
                                    HashMap<Integer, List<Token>> tokens = _outputTokens
                                            .get(port);
                                    if (tokens == null) {
                                        tokens = new HashMap<Integer, List<Token>>();
                                        _outputTokens.put(port, tokens);
                                    }
                                    List<Token> queue = tokens.get(0);
                                    if (queue == null) {
                                        queue = new LinkedList<Token>();
                                        tokens.put(0, queue);
                                    }
                                    queue.add(token);
                                    if (_debugging) {
                                        _debug("Queueing " + token
                                                + " to be sent on "
                                                + port.getName()
                                                + " and requesting a firing.");
                                    }
                                }
                                // Request a firing at the current time.
                                getDirector()
                                .fireAtCurrentTime(JavaScript.this);
                            }
                        }
                    } catch (IllegalActionException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            });
        }

        /** Print a message to standard out.
         *  @param message The message to be printed
         */
        public void print(String message) {
            System.out.println(message);
        }

        /** Read the specified URL and return its contents.
         *  @param url The URL to read.
         *  @return The content of the URL.
         *  @exception IOException If the specified URL can't be read (that is, a response code
         *   was received that is not in the range 100 to 399.
         */
        public String readURL(String url) throws IOException {
            // FIXME: We should have a version that takes a callback function
            // to return the reply, and a version that supports a streaming reply.
            URL theURL = new URL(url);
            // If the actor is restricted, support only HTTP protocols.
            // FIXME: Should this also apply the usual browser same-source restriction?
            // Same as what?
            if (_restricted && !theURL.getProtocol().equalsIgnoreCase("http")) {
                throw new SecurityException(
                        "Actor is restricted. Only HTTP requests will be honored by readURL().");
            }

            // Create a new HttpRequest.  Default method is GET.
            HttpRequest request = new HttpRequest();
            request.setUrl(new URL(url));

            HttpResponse response = request.execute();
            if (!response.isSuccessful()) {
                    throw new IOException("Failed to read URL: " + url +
                                    "\nResponse code: " + response.getResponseCode() +
                                    "\nResponse message: " + response.getResponseMessage());
            }
            return response.getBody();
        }

        /** Send outputs via an output port. If this is called outside the
         *  invocation of a fire() method, then this method records the
         *  outputs to be produced and calls fireAtCurrentTime() on the
         *  director.
         *  @param data The data to send via the port.
         *  @param portWrapper A JavaScript wrapper for a Port.
         *  @param channel A channel number, or NaN to use the default (0).
         */
        public void send(Object data, NativeJavaObject portWrapper,
                Double channel) {

            // In JavaScript, all numbers are doubles. So we have convert
            // to an integer.
            int channelNumber = 0;
            // Tolerate null or NaN as an argument.
            // Interestingly, Rhino's implementation of JavaScript, if get(port) is
            // called with only one argument, does in fact call this method, passing
            // NaN as the second argument.
            // This seems unlikely to be robust, so I'm not documenting the "feature"
            // that you can leave off the channel number.
            if (channel != null && !channel.isNaN()) {
                if (channel.doubleValue() % 1 == 0) {
                    // The value is actually an integer.
                    channelNumber = channel.intValue();
                } else {
                    throw new InternalErrorException(JavaScript.this, null,
                            "Second argument to send(port, channel) is required to be an integer. Got "
                                    + channel);
                }
            }

            if (portWrapper == null) {
                throw new InternalErrorException(JavaScript.this, null,
                        "Send failed. Port argument is null.");
            }
            Object unwrappedPort = portWrapper.unwrap();
            // The port reference will be a PortProxy in restricted mode, and a port otherwise.
            if (unwrappedPort instanceof PortProxy) {
                unwrappedPort = ((PortProxy) unwrappedPort)._port;
            }
            if (unwrappedPort instanceof TypedIOPort) {
                TypedIOPort port = (TypedIOPort) unwrappedPort;
                if (!port.isOutput()) {
                    throw new InternalErrorException(JavaScript.this, null,
                            "Cannot send via " + port.getName()
                            + ", which is not an output port.");
                }
                if (data instanceof NativeJavaObject) {
                    data = ((NativeJavaObject) data).unwrap();
                }
                try {
                    Token token = _createToken(data);
                    if (_inFire) {
                        if (_debugging) {
                            _debug("Sending " + token + " to " + port.getName());
                        }
                        port.send(channelNumber, token);
                    } else {
                        if (!_executing) {
                            // This is probably being called in a callback, but the model
                            // execution has ended.
                            throw new InternalErrorException("Attempt to send "
                                    + token + " to " + port.getName()
                                    + ", but the model is not executing.");
                        }

                        // Not currently firing. Queue the tokens and request a firing.
                        // This should be being called in a callback that holds a
                        // synchronization lock, so synchronizing this isn't really
                        // necessary, but just in case...
                        synchronized (this) {
                            if (_outputTokens == null) {
                                _outputTokens = new HashMap<IOPort, HashMap<Integer, List<Token>>>();
                            }
                            HashMap<Integer, List<Token>> tokens = _outputTokens
                                    .get(port);
                            if (tokens == null) {
                                tokens = new HashMap<Integer, List<Token>>();
                                _outputTokens.put(port, tokens);
                            }
                            List<Token> queue = tokens.get(channelNumber);
                            if (queue == null) {
                                queue = new LinkedList<Token>();
                                tokens.put(channelNumber, queue);
                            }
                            queue.add(token);
                            if (_debugging) {
                                _debug("Queueing " + token + " to be sent on "
                                        + port.getName()
                                        + " and requesting a firing.");
                            }
                        }

                        // Request a firing at the current time.
                        getDirector().fireAtCurrentTime(JavaScript.this);
                    }
                } catch (KernelException e) {
                    throw new InternalErrorException(JavaScript.this, e,
                            "Failed to send output via port " + port.getName()
                            + ".");
                }
            } else {
                throw new InternalErrorException(JavaScript.this, null,
                        "Second argument of send() is required to an output port. It is "
                                + unwrappedPort.toString() + ".");
            }
        }

        /** After the specified amount of time (in milliseconds), invoke the specified function.
         *  The function is invoked during a firing of this JavaScript actor, so the function
         *  can read inputs, produce outputs, and do anything else that might be done in the
         *  JavaScript fire() method.  The specified function will be invoked after the fire()
         *  JavaScript method, if one is defined in the script, and also after any input
         *  handlers created by setInputHandler().
         *  <p>
         *  If the model stops executing before the timeout period elapses, then the
         *  specified function will not be invoked.</p>
         *  @param function The function to invoke.
         *  @param time The time in milliseconds.
         *  @exception IllegalActionException If the director cannot respect the time request.
         *  @return A handle to the delayed function, to be used by clearTimeout()
         *   to cancel the function invocation if it hasn't occurred yet.
         */
        public Integer setTimeout(final Function function, final Integer time)
                throws IllegalActionException {
            // FIXME: setTimeout() needs an optional third argument, arguments to the function
            // to be passed when it is invoked. Presumably those arguments should be evaluated
            // in the context of the invocation of the function, not in the context of
            // this setTimeout() call.

            // NOTE: The API of this method is intended to match that of Node.js.
            final Integer id = Integer.valueOf(_timeoutCount++);
            Time currentTime = getDirector().getModelTime();
            final Time callbackTime = currentTime.add(time * 0.001);

            // FIXME: Check that synchronizeToRealTime is present and set to true
            // in the director.

            Time responseTime = getDirector().fireAt(JavaScript.this,
                    callbackTime);
            if (!responseTime.equals(callbackTime)) {
                throw new IllegalActionException(
                        JavaScript.this,
                        "Director is unable to fire this actor at the requested time "
                                + callbackTime
                                + ". It replies that it will fire the actor at "
                                + responseTime + ".");
            }

            // Record the callback function indexed by ID.
            if (_pendingTimeoutFunctions == null) {
                _pendingTimeoutFunctions = new HashMap<Integer, Function>();
            }
            _pendingTimeoutFunctions.put(id, function);

            // Record the ID of the timeout indexed by time.
            if (_pendingTimeoutIDs == null) {
                _pendingTimeoutIDs = new HashMap<Time, List<Integer>>();
            }
            List<Integer> ids = _pendingTimeoutIDs.get(callbackTime);
            if (ids == null) {
                ids = new LinkedList<Integer>();
                _pendingTimeoutIDs.put(callbackTime, ids);
            }
            ids.add(id);

            return id;
        }

        /** Get parameter values.
         *  @param paramWrapper A JavaScript wrapper for a Variable.
         *  @return A wrapped token that contains the parameter.
         */
        public Object valueOf(NativeJavaObject paramWrapper) {
            Object unwrappedParam = paramWrapper.unwrap();
            if (unwrappedParam instanceof ParameterProxy) {
                unwrappedParam = ((ParameterProxy) unwrappedParam)._parameter;
            }
            if (unwrappedParam instanceof Parameter) {
                Parameter parameter = (Parameter) unwrappedParam;
                try {
                    return _wrapToken(parameter.getToken());
                } catch (KernelException e) {
                    throw new InternalErrorException(JavaScript.this, e,
                            "Failed to get parameter value for "
                                    + parameter.getName() + ".");
                }
            } else {
                throw new InternalErrorException(JavaScript.this, null,
                        "Argument of valueOf() is required to be parameter. It is "
                                + unwrappedParam.toString() + ".");
            }
        }

        /**
         * Request an OAuth 2.0 authorization token. This method requires interaction with an Authorization server
         * which is usually implemented as a web service.
         * @param providerName References an internally stored end point URL for popular OAuth providers like Google,
         *         Twitter, Facebook, etc.
         * @param clientId This identifies the application that accesses a resource. Usually, client ids are issued
         *         after registering an application via a developer console at a resource provider.
         * @param redirectUrl After completion of the authorization request this URL is used to redirect the browser.
         * @param openBrowser Indicates, if the method should invoke the system's default browser to enable the user
         *         to login to the server in order to grant access, or if the script handles this on its own.
         * @return returns The authorization code, if the authentication at the Authorization Server was successful.
         * @exception IllegalActionException
         */
        public String requestAuth(String providerName, String clientId,
                String redirectUrl, Boolean openBrowser)
                        throws IllegalActionException {
            OAuthProviderType oauthProvider;
            if ("google".equals(providerName.toLowerCase())) {
                oauthProvider = OAuthProviderType.GOOGLE;
            } else {
                throw new IllegalActionException(
                        "Provider '"
                                + providerName
                                + "' not known. Please consult documentation for built-in provider names.");
            }

            OAuthClientRequest request;
            try {
                request = OAuthClientRequest
                        .authorizationProvider(oauthProvider)
                        .setClientId(clientId).setRedirectURI(redirectUrl)
                        .setResponseType("code").setScope("email profile")
                        .buildQueryMessage();
            } catch (OAuthSystemException ex) {
                throw new IllegalActionException(null, ex,
                        "Could not build OAuth request message.");
            }

            if (openBrowser) {
                openBrowser(request.getLocationUri());
            }

            return request.getLocationUri();
        }

        /**
         * Uses an Authorization code to retrieve an Access code.
         * @param providerName The name of the provider, for example "google".
         * @param clientId The clientID.
         * @param clientSecret The client password
         * @param redirectUrl The URL to which to redirect.
         * @param authCode The authorization code issued by the Authorization server.
         * @return The Access code token. This can be used to access the Resource server.
         * @exception IllegalActionException
         */
        public String requestAccess(String providerName, String clientId,
                String clientSecret, String redirectUrl, String authCode)
                        throws IllegalActionException {
            OAuthProviderType oauthProvider;
            if ("google".equals(providerName.toLowerCase())) {
                oauthProvider = OAuthProviderType.GOOGLE;
            } else {
                throw new IllegalActionException(
                        "Provider '"
                                + providerName
                                + "' not known. Please consult documentation for built-in provider names.");
            }
            OAuthClientRequest request;
            OAuthJSONAccessTokenResponse response;
            try {
                request = OAuthClientRequest.tokenProvider(oauthProvider)
                        .setGrantType(GrantType.AUTHORIZATION_CODE)
                        .setClientId(clientId).setClientSecret(clientSecret)
                        .setRedirectURI(redirectUrl).setCode(authCode)
                        .buildBodyMessage();

                OAuthClient oAuthClient = new OAuthClient(
                        new URLConnectionClient());
                response = oAuthClient.accessToken(request);

            } catch (OAuthSystemException ex) {
                throw new IllegalActionException(null, ex,
                        "Could not build OAuth request message.");
            } catch (OAuthProblemException ex2) {
                throw new IllegalActionException(null, ex2,
                        "Could not build OAuth request message.");
            }
            return response.getAccessToken();
        }

        /**
         * Open a URL of web service that is protected by OAuth 2.0.
         * @param url The protected URL on a Resource server. Usually this is some kind of RESTful API.
         * @param accessToken The code used to prove access authorization to the Resource server.
         * @return The OAuth Client resource response.
         * @exception IllegalActionException
         */
        public String readProtectedURL(String url, String accessToken)
                throws IllegalActionException {
            OAuthResourceResponse resourceResponse = null;
            try {
                OAuthClientRequest bearerClientRequest = new OAuthBearerClientRequest(
                        url).setAccessToken(accessToken).buildQueryMessage();

                OAuthClient client = new OAuthClient(new URLConnectionClient());
                resourceResponse = client.resource(bearerClientRequest,
                        OAuth.HttpMethod.GET, OAuthResourceResponse.class);
            } catch (OAuthSystemException ex) {
                throw new IllegalActionException(null, ex,
                        "Could not connect to resource server: "
                                + ex.getMessage());
            } catch (OAuthProblemException ex2) {
                throw new IllegalActionException(null, ex2,
                        "Could not connect to resource server: "
                                + ex2.getMessage());
            }
            if (resourceResponse != null) {
                if (resourceResponse.getResponseCode() == 200) {
                    return resourceResponse.getBody();
                } else {
                    return "Could not access resource: "
                            + resourceResponse.getResponseCode() + " "
                            + resourceResponse.getBody();
                }
            } else {
                throw new IllegalActionException(
                        "Could not execute resource access request.");
            }
        }

        /**
         * Open a new tab in the default system browser.
         * See also <a href="http://www.mkyong.com/java/open-browser-in-java-windows-or-linux/">http://www.mkyong.com/java/open-browser-in-java-windows-or-linux/></a>.
         *
         * @param url The URL that the browser shall retrieve.
         * @return The empty string.
         * @exception IllegalActionException If the browser is not found
         */
        public String openBrowser(String url) throws IllegalActionException {
            try {
                BrowserLauncher.openURL(url);
            } catch (IOException ex) {
                throw new IllegalActionException(JavaScript.this, ex,
                        "Failed to open \"" + url + "\".");
            }
            //             String os = System.getProperty("os.name").toLowerCase();
            //             Runtime rt = Runtime.getRuntime();

            //             try {
            //                 if (os.indexOf( "win" ) >= 0) {
            //                     // this doesn't support showing urls in the form of "page.html#nameLink"
            //                     rt.exec( "rundll32 url.dll,FileProtocolHandler " + url);
            //                 } else if (os.indexOf( "mac" ) >= 0) {
            //                     rt.exec( "open " + url);
            //                 } else if (os.indexOf( "nix") >=0 || os.indexOf( "nux") >=0) {
            //                     // Do a best guess on unix until we get a platform independent way
            //                     // Build a list of browsers to try, in this order.
            //                     String[] browsers = {"epiphany", "firefox", "mozilla", "konqueror", "netscape","opera","links","lynx"};
            //                     // Build a command string which looks like "browser1 "url" || browser2 "url" ||..."
            //                     StringBuffer cmd = new StringBuffer();
            //                     for (int i=0; i<browsers.length; i++)  {
            //                         cmd.append( (i==0  ? "" : " || " ) + browsers[i] +" \"" + url + "\" ");
            //                     }
            //                     rt.exec(new String[] { "sh", "-c", cmd.toString() });
            //                 } else {
            //                     throw new IllegalActionException("No browser found.");
            //                 }
            //             } catch (IOException e) {
            //                 throw new IllegalActionException("Could not open browser.");
            //             }
            return ""; //FIXME Return empty string to suppress 'null' output. Is this the right thing to do?
        }

        /** Create a Ptolemy Token from an object sent by JavaScript.
         *  @exception IllegalActionException If constructing a Ptolemy Token fails.
         */
        private Token _createToken(Object data) throws IllegalActionException {
            //**********************************************************************
            // This the key place to support additional output data types.
            if (data instanceof Integer) {
                return new IntToken(((Integer) data).intValue());
            } else if (data instanceof Boolean) {
                return new BooleanToken(((Boolean) data).booleanValue());
            } else if (data instanceof Double) {
                // Since JavaScript represents all numbers as double, we first
                // check to see whether this is actually a long or an integer.
                if (((Double) data).doubleValue() % 1 == 0) {
                    // The value is a long or an integer.
                    if (((Double) data).intValue() == Integer.MAX_VALUE) {
                        return new LongToken(((Double) data).longValue());
                    } else {
                        return new IntToken(((Double) data).intValue());
                    }
                }
                return new DoubleToken(((Double) data).doubleValue());
            } else if (data instanceof String) {
                return new StringToken(data.toString());
            } else if (data instanceof NativeArray) {
                NativeArray array = (NativeArray) data;
                int length = array.size();
                Token[] result = new Token[length];
                for (int i = 0; i < length; i++) {
                    result[i] = _createToken(array.get(i));
                }
                if (result.length > 0) {
                    return new ArrayToken(result);
                } else {
                    // Need to return an empty array. But of what element type?
                    // FIXME: For now, return an empty array of strings.
                    return new ArrayToken(BaseType.STRING);
                }
            } else if (data instanceof NativeObject) {
                // If the object has a non-empty key set, then construct
                // a record. Otherwise, just return an ObjectToken.
                NativeObject object = (NativeObject) data;
                Set<Object> properties = object.keySet();
                if (properties == null || properties.isEmpty()) {
                    return new ObjectToken(data);
                }
                boolean foundOne = false;
                ArrayList<String> keys = new ArrayList<String>();
                ArrayList<Token> values = new ArrayList<Token>();
                for (Object key : properties) {
                    if (key instanceof String) {
                        Object value = object.get(key);
                        if (value != null) {
                            foundOne = true;
                            keys.add((String) key);
                            values.add(_createToken(value));
                        }
                    }
                }
                if (foundOne) {
                    return new RecordToken(
                            keys.toArray(new String[keys.size()]),
                            values.toArray(new Token[values.size()]));
                }
                return new ObjectToken(data);
            } else if (data instanceof Entity) {
                return new ActorToken((Entity) data);
            } else {
                return new ObjectToken(data);
            }
        }

        /** Convert a Ptolemy II into a JavaScript object, or into a Java
         *  object that will be automatically converted into a corresponding
         *  JavaScript object.
         *  @param token The token to convert.
         *  @return A JavaScript object (Scriptable) or a native Java object
         *   (such as Integer or Double) that can be automatically converted
         *   to a JavaScript object, or null if the argument is null.
         */
        private Object _wrapToken(Token token) {
            //**********************************************************************
            // This the key place to support additional input and parameter data types.
            // Given a Ptolemy II token, if there is a corresponding native
            // JavaScript type, then return something that will naturally be
            // converted to that type.
            if (token instanceof BooleanToken) {
                return Boolean.valueOf(((BooleanToken) token).booleanValue());
            } else if (token instanceof DoubleToken) {
                return Double.valueOf(((DoubleToken) token).doubleValue());
            } else if (token instanceof IntToken) {
                return Integer.valueOf(((IntToken) token).intValue());
            } else if (token instanceof StringToken) {
                return ((StringToken) token).stringValue();
            } else if (token instanceof ActorToken) {
                Entity entity = ((ActorToken) token).getEntity();
                return new NativeJavaObject(_scope, entity, entity.getClass());
            } else if (token == null) {
                return null;
            } else if (token instanceof ArrayToken) {
                int length = ((ArrayToken) token).length();
                Object[] result = new Object[length];
                for (int i = 0; i < length; i++) {
                    Token element = ((ArrayToken) token).getElement(i);
                    result[i] = _wrapToken(element);
                }
                // Don't directly construct a NativeArray because
                // that creates an array with no context or scope.
                return _context.newArray(_scope, result);
            } else if (token instanceof RecordToken) {
                RecordToken record = (RecordToken) token;
                Scriptable result = _context.newObject(_scope);
                for (String name : record.labelSet()) {
                    Token element = record.get(name);
                    result.put(name, result, _wrapToken(element));
                }
                // Don't directly construct a NativeArray because
                // that creates an array with no context or scope.
                return result;
            }
            return Context.toObject(token, _scope);
        }
    }

    /** Specialized FunctionObject that provides the parent scope when evaluating the function.
     */
    @SuppressWarnings("serial")
    private static class PtolemyFunctionObject extends FunctionObject {

        private PtolemyFunctionObject(String name, Member methodOrConstructor,
                Scriptable parentScope) {
            super(name, methodOrConstructor, parentScope);
        }

        @Override
        public Object call(Context context, Scriptable scope,
                Scriptable thisObj, Object[] args) {
            return super.call(context, scope, getParentScope(), args);
        }
    }
}
