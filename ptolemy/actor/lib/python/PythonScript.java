/* An actor that executes a Python script.

 Copyright (c) 2003-2014 The Regents of the University of California.
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
package ptolemy.actor.lib.python;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;

import org.python.core.PyClass;
import org.python.core.PyException;
import org.python.core.PyJavaType;
import org.python.core.PyMethod;
import org.python.core.PyModule;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.process.TerminateProcessException;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.ClassUtilities;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// PythonScript

/**
 An actor of this class executes a Python script.  There are two versions
 of this actor provided in the Vergil libraries.  The one called
 "PythonActor" has an input port and an output port; to view or edit
 its Python script, look inside the actor.  The second version is
 called "PythonScript" and has no ports; to view or edit its Python
 script, select Configure (or double click on the icon).

 <p> Upon creation, this actor has no ports, and no parameters other than
 {@link #script script}; The <i>script</i> parameter has visibility
 EXPERT, and therefore does not normally show up in a configure dialog
 for the actor.  To make the script visible and editable, you have two
 options. Including an instance of an attribute of class
 TextEditorConfigureFactory (with its <i>attributeName</i> parameter
 set to <i>script</i>) results in behavior like that of the Vergil
 "PythonScript." That is, to edit the script, you Configure the actor.
 If instead you include an instance of TextEditorTableauFactory,
 then to edit the script you look inside the actor.  Use the latter
 if you wish to add additional attributes to the actor and hide the
 script from the users.  Use the former if the script is the main
 means by which users interact with the actor.</p>

 <p> The functionality of an actor of this type is given by a Python script.
 As an example, a simplified version of the
 {@link ptolemy.actor.lib.Scale Scale}
 actor can be implemented by the following script:</p>
 <pre>
 1.  class Main :
 2.    "scale"
 3.    def fire(self) :
 4.      if not self.input.hasToken(0) :
 5.        return
 6.      s = self.scale.getToken()
 7.      t = self.input.get(0)
 8.      self.output.broadcast(s.multiply(t))
 </pre>

 <p>Line 1 defines a Python class Main, which matches the value of the
 <i>jythonClassName</i> parameter. An instance of this class is created when the
 actor is initialized. Line 2 is a description of the purpose of the
 script. Lines 3-8 define the fire() method, which is called by the
 {@link #fire() fire()} method of this actor. In the method body,
 <i>input</i> and <i>output</i> are ports that have to have been added
 to the actor, and <i>scale</i> is a parameter that has to have been
 added to the actor (these can be added in the XML that defines the
 actor instance in an actor library). The Main class can provide other
 methods in the {@link ptolemy.actor.Executable Executable} interface
 as needed.</p>

 <p>In the script, use <code>self.actor</code> to access the actor. For example,
 <code>self.actor.getDirector()</code> returns the current director of the
 actor. For debugging, use <code>self.actor.debug(someMessage)</code>. The
 final message sent to the debug listeners of the actor will have the string
 "From script: " inserted at the beginning. To avoid generating the debug
 message when there are no listeners, use:</p>
 <pre>
 if self.actor.isDebugging() :
 self.actor.debug(someMessage)
 </pre>

 <p>To use a Jython module, it is necessary to create a .py file
 located in a location where Jython can find it.  The Jython
 <code>sys.path</code> variable contains the Jython path.  One way to
 get the value of the sys.path variable is to enable debugging on the
 actor by right clicking and selecting "Listen to Actor", which will
 cause the preinitialize() method to print the contents of sys.path to
 standard out.  Another way to get the value of <code>sys.path</code>
 is to run the Ptolemy model at
 <code>ptolemy/actor/lib/python/test/PythonSysPath</code>.  For
 example, under Mac OS X for the ptII user, sys.path includes
 <code>/Users/ptII/lib/Lib</code>.  So, create that directory if
 necessary and place the .py file in that directory, for example
 <code>/Users/ptII/lib/Lib/PtPythonSquare.py</code></p>

 <pre>
class Main :
  "Read the input and send the square to the output"
  def fire(self) :
    token = self.input.get(0)
    self.output.broadcast(token.multiply(token))
    return
 </pre>

 <p>Then set <i>jythonClassName</i> to the name of the <b>Jython</b>
 class, for example <code>PtPythonSquare.Main</code>.  (Note that the
 <i>jythonClassName</i> parameter should be set to the value of the
 Jython class name before changing the <i>script</i> parameter to
 import a Jython module.)</p>

 <p>Then set <i>script</i> to  to:</p>
 <pre>
 import PtPythonSquare
 PtPythonSquare = reload(PtPythonSquare)
 </pre>


 <p>This class relies on <a href="http://jython.org">Jython</a>, which
 is a Java implementation of Python.

 <p>As of November, 2011 $PTII/lib/jython.jar was based on Jython 2.5.2.</p>

 <p>See <a href="https://kepler-project.org/developers/reference/python-and-kepler#in_browser">Python and Kepler notes</a>.</p>


 @author Xiaojun Liu
 @version $Id$
 @since Ptolemy II 2.3
 @Pt.ProposedRating Yellow (liuxj)
 @Pt.AcceptedRating Red (reviewmoderator)
 */
public class PythonScript extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  In addition to invoking the base class constructor,
     *  create the <i>script</i> parameter, and initialize
     *  the script to provide an empty template.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception NameDuplicationException If the container already
     *   has an actor with this name.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     */
    public PythonScript(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        jythonClassName = new StringAttribute(this, "jythonClassName");
        jythonClassName.setExpression("Main");

        script = new StringAttribute(this, "script");

        // Set the visibility to expert, as casual users should
        // not see the script.  This is particularly true if one
        // installs an actor that is an instance of this with a
        // particular script in the library.
        script.setVisibility(Settable.EXPERT);

        // initialize the script to provide an empty template:
        //
        // # This is a template.
        // class Main :
        //   "description here"
        //   def fire(self) :
        //     # read input, compute, send output
        //     return
        //
        script.setExpression("# This is a template.\n" + "class Main :\n"
                + "  \"description here\"\n" + "  def fire(self) :\n"
                + "    # Create ports, e.g. input and output.\n"
                + "    # Read input, for example using\n"
                + "    # token = self.input.get(0)\n"
                + "    # compute, and send an output using\n"
                + "    # self.output.broadcast(token)\n" + "    return\n\n");
        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-30\" y=\"-15\" " + "width=\"60\" height=\"30\" "
                + "style=\"fill:black\"/>\n" + "<text x=\"-22\" y=\"4\""
                + "style=\"font-size:14; fill:white; font-family:SansSerif\">"
                + "Python</text>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The Jython class name to be invoked.  The default value is
     *  "Main", which indicates that the <i>script</i> parameter
     *  should define a class named "Main".  If the <i>script</i>
     *  parameter imports a Jython module, for example: "import Foo",
     *  then Foo.py should define a class named "Main" and this
     *  parameter should have the value "Foo.Main".  If the value of
     *  this parameter is anything other than "Main", then
     *  preinitialize() will reread the script.  This is how Jython
     *  modules can be used.  Note that the <i>jythonClassName</i>
     *  parameter should be set to the value of the Jython class name
     *  before changing the <i>script</i> parameter to import a Jython
     *  module.
     */
    public StringAttribute jythonClassName;

    /** The script that specifies the function of this actor.
     *  The default value is a script that copies the input to the output.
     */
    public StringAttribute script;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If <i>script</i> is changed, invoke the python interpreter to
     *  evaluate the script.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If there is any error in evaluating
     *   the script.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == script) {
            _evaluateScript();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then properly sets private variables.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        PythonScript newObject = (PythonScript) super.clone(workspace);

        newObject._class = null;
        newObject._methodMap = new HashMap();
        newObject._object = null;
        return newObject;
    }

    /** Send the message to all registered debug listeners. In the script,
     *  use <code>self.actor.debug()</code> to call this method.
     *  @param message The debug message.
     */
    public void debug(String message) {
        if (_debugging) {
            _debug("From script: ", message);
        }
    }

    /** Invoke the fire() method if defined in the script.
     *  @exception IllegalActionException If there is any error in calling the
     *   fire() method defined by the script.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        _invokeMethod("fire", null);
    }

    /** Invoke the initialize() method if defined in the script.
     *  @exception IllegalActionException If there is any error in calling the
     *   initialize() method defined by the script.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _invokeMethod("initialize", null);
    }

    /** Return true if this actor has at least one debug listener.
     *  @return True if this actor has at least one debug listener.
     */
    public boolean isDebugging() {
        return _debugging;
    }

    /** Invoke the postfire() method if defined in the script. Return true
     *  when the method return value is not zero, or the method does not
     *  return a value, or the method is not defined in the script.
     *  @return False if postfire() is defined in the script and returns 0,
     *   true otherwise.
     *  @exception IllegalActionException If there is any error in calling the
     *   postfire() method defined by the script.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        boolean defaultResult = super.postfire();
        PyObject postfireResult = _invokeMethod("postfire", null);

        if (postfireResult != null) {
            return postfireResult.__nonzero__();
        } else {
            return defaultResult;
        }
    }

    /** Invoke the prefire() method if defined in the script. Return true
     *  when the method return value is not zero, or the method does not
     *  return a value, or the method is not defined in the script.
     *  @return False if prefire() is defined in the script and returns 0,
     *   true otherwise.
     *  @exception IllegalActionException If there is any error in calling the
     *   prefire() method.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        boolean defaultResult = super.prefire();
        PyObject prefireResult = _invokeMethod("prefire", null);

        if (prefireResult != null) {
            return prefireResult.__nonzero__();
        } else {
            return defaultResult;
        }
    }

    /** Create an instance of the parameter named by the
     *  jythonClassName parameter that is defined in the script.  Add
     *  all parameters and ports of this actor as attributes of the
     *  object, so that they become accessible to the methods defined
     *  in the script.
     *  @exception IllegalActionException If there is any error in
     *   creating an instance of the class named by the
     *   jythonClassName class defined in the script.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        if (_debugging) {
            _interpreter.exec("print sys.path");
        }

        _object = _createObject();
        _invokeMethod("preinitialize", null);
    }

    /** Invoke the stop() method if defined in the script. Ignore any error
     *  in calling the method.
     */
    @Override
    public void stop() {
        super.stop();

        try {
            _invokeMethod("stop", null);
        } catch (IllegalActionException e) {
            if (_debugging) {
                _debug(e.getMessage());
            }
        }
    }

    /** Invoke the stopFire() method if defined in the script. Ignore any error
     *  in calling the method.
     */
    @Override
    public void stopFire() {
        super.stopFire();

        try {
            _invokeMethod("stopFire", null);
        } catch (IllegalActionException e) {
            if (_debugging) {
                _debug(e.getMessage());
            }
        }
    }

    /** Invoke the terminate() method if defined in the script. Ignore any
     *  error in calling the method.
     */
    @Override
    public void terminate() {
        super.terminate();

        try {
            _invokeMethod("terminate", null);
        } catch (IllegalActionException e) {
            if (_debugging) {
                _debug(e.getMessage());
            }
        }
    }

    /** Invoke the wrapup() method if defined in the script. Ignore any error
     *  in calling the method.
     *  @exception IllegalActionException If there is any error in calling the
     *   wrapup() method defined in the script.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        _invokeMethod("wrapup", null);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**  Create an instance of the class named by the jythonClassName
     *  parameter which is defined in the script.  Add all parameters
     *  and ports of this actor as attributes of the object, so that
     *  they become accessible to the methods defined in the
     *  script.
     *  @exception IllegalActionException If there is any error in
     *  creating an instance of the class named by the jythonClassName
     *  parameter defined in the script.
     */
    private PyObject _createObject() throws IllegalActionException {
        // Create an instance by using the __call__ method
        // of the class object
        if (_class == null || !jythonClassName.getExpression().equals("Main")) {
            // Since _class is null, we could have been cloned.
            // Evaluate the script so that we do not use a different
            // script of another python actor. (This will set _class).
            _evaluateScript();
        }
        PyObject object = _class.__call__();

        if (object == null) {
            throw new IllegalActionException(this,
                    "Error in creating an instance of the "
                            + jythonClassName.getExpression()
                            + "defined in the script.");
        }

        // set up access to this actor
        // first create an attribute "actor" on the object
        // the PyObject class does not allow adding a new attribute to the
        // object
        object.__setattr__("actor", PyJavaType.wrapJavaObject(this));

        // give the object access to attributes and ports of this actor
        Iterator attributes = attributeList().iterator();

        while (attributes.hasNext()) {
            Attribute attribute = (Attribute) attributes.next();
            String mangledName = _mangleName(attribute.getName());

            if (_debugging) {
                _debug("set up reference to attribute \"" + attribute.getName()
                        + "\" as \"" + mangledName + "\"");
            }

            object.__setattr__(new PyString(mangledName),
                    PyJavaType.wrapJavaObject(attribute));
        }

        Iterator ports = portList().iterator();

        while (ports.hasNext()) {
            Port port = (Port) ports.next();
            String mangledName = _mangleName(port.getName());

            if (_debugging) {
                _debug("set up reference to port \"" + port.getName()
                        + "\" as \"" + mangledName + "\"");
            }

            object.__setattr__(new PyString(mangledName),
                    PyJavaType.wrapJavaObject(port));
        }

        // populate the method map
        for (int i = 0; i < _METHOD_NAMES.length; ++i) {
            String methodName = _METHOD_NAMES[i];
            PyMethod method = null;

            try {
                method = (PyMethod) object.__findattr__(methodName);
            } catch (ClassCastException ex) {
                // the object has an attribute with the methodName but
                // is not a method, ignore
            }

            _methodMap.put(methodName, method);
        }

        return object;
    }

    /**  Evaluate the script by invoking the Jython interpreter.
     *  @exception IllegalActionException If the script does
     *  not define a class with the name of the value of the
     *  jythonClassName parameter, or the python interpreter cannot be
     *  initialized.
     */
    private void _evaluateScript() throws IllegalActionException {
        synchronized (_interpreter) {
            String pythonScript = script.getExpression();

            try {
                if (_debugging) {
                    _debug("PythonScript: evaluating " + pythonScript);
                }
                _interpreter.exec(pythonScript);
            } catch (Exception ex) {
                if (ex instanceof PyException) {
                    _reportScriptError((PyException) ex,
                            "Error in evaluating script:\n");
                } else {
                    throw new IllegalActionException(this, ex,
                            "Error in evaluating script:\n");
                }
            }

            // Get the class defined by the script.
            try {
                _class = (PyClass) _interpreter.get(jythonClassName
                        .getExpression());
            } catch (ClassCastException ex) {
                try {
                    PyModule module = (PyModule) _interpreter
                            .get(jythonClassName.getExpression());
                    _class = (PyClass) module.__findattr_ex__("Main");
                } catch (ClassCastException ex2) {
                    throw new IllegalActionException(this, ex,
                            "Failed to cast _interpreter.get(jythonClassName.getExpression()) "
                                    + " which is of type "
                                    + _interpreter
                                            .get(jythonClassName
                                                    .getExpression())
                                            .getClass().getName()
                                    + " to PyClass.");
                }
            }

            if (_class == null) {
                throw new IllegalActionException(
                        this,
                        "The script does not define a \""
                                + jythonClassName.getExpression()
                                + " \" class, try setting the jythonClassName parameter "
                                + "or have the script start with \"class "
                                + jythonClassName.getExpression() + "\".");
            }
        }
    }

    /**  Invoke the specified method on the instance of the class
     *  named by the jythonClassName parameter.  Any argument that is
     *  not an instance of PyObject is wrapped in an instance of
     *  PyJavaType. The result of invoking the method is returned.
     *  @exception IllegalActionException If there is any
     *  error in calling the method.
     */
    private PyObject _invokeMethod(String methodName, Object[] args)
            throws IllegalActionException {
        PyMethod method = (PyMethod) _methodMap.get(methodName);
        PyObject returnValue = null;

        if (method != null) {
            try {
                if (args == null || args.length == 0) {
                    try {
                        returnValue = method.__call__();
                    } catch (Exception ex) {
                        // If the inner exception is TerminateProcessException,
                        // then get the exception and rethrow it.
                        if (ex instanceof PyException) {
                            PyException pyException = (PyException) ex;
                            Object exceptionValue = pyException.value
                                    .__tojava__(Exception.class);
                            if (exceptionValue instanceof Exception) {
                                Exception innerException = (Exception) exceptionValue;
                                if (innerException instanceof TerminateProcessException) {
                                    // Work around bug reported by
                                    // Norbert Podhorszki
                                    // See python/test/auto/PythonScalePN.xml
                                    throw (TerminateProcessException) innerException;
                                } else {
                                    throw ex;
                                }
                            } else {
                                // Test PythonScript-2.5 illustrates
                                // why we need this.
                                throw ex;
                            }
                        } else {
                            throw ex;
                        }
                    }
                } else {
                    PyObject[] convertedArgs = new PyObject[args.length];

                    for (int i = 0; i < args.length; ++i) {
                        if (!(args[i] instanceof PyObject)) {
                            convertedArgs[i] = PyJavaType
                                    .wrapJavaObject(args[i]);
                        } else {
                            convertedArgs[i] = (PyObject) args[i];
                        }
                    }

                    returnValue = _object.__call__(convertedArgs);
                }
            } catch (TerminateProcessException terminate) {
                // Rethrow the terminate exception.
                // See python/test/auto/PythonScalePN.xml
                throw terminate;
            } catch (Exception ex) {
                String messagePrefix = "Error in invoking the " + methodName
                        + " method:\n";
                if (ex instanceof PyException) {
                    _reportScriptError((PyException) ex, messagePrefix);
                } else {
                    throw new IllegalActionException(this, ex, messagePrefix);
                }
            }
        }

        return returnValue;
    }

    /*  Mangle the given name (usually the name of an entity, or a parameter,
     *  or a port). Any character that is not legal in Java identifiers is
     *  changed to the underscore character.
     */
    private String _mangleName(String name) {
        char[] nameChars = name.toCharArray();
        boolean mangled = false;

        for (int i = 0; i < nameChars.length; ++i) {
            if (!Character.isJavaIdentifierPart(nameChars[i])) {
                nameChars[i] = '_';
                mangled = true;
            }
        }

        if (mangled) {
            return new String(nameChars);
        }

        return name;
    }

    /*  Report an error in evaluating the script or calling a method defined
     *  in the script.
     */
    private void _reportScriptError(PyException ex, String messagePrefix)
            throws IllegalActionException {
        String message = ex.toString();
        int i = message.indexOf("line");

        if (i >= 0) {
            message = message.substring(i);
        }

        throw new IllegalActionException(this, ex, messagePrefix + message);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The class defined in the script.
    private PyClass _class;

    // The python interpreter.
    //private static PythonInterpreter _interpreter = new PythonInterpreter();
    private static PythonInterpreter _interpreter;

    static {
        try {
            // If the python.home property is not set, then set it
            // so that we can figure out where to write the jython cache.
            //
            // Under Webstart python/core/PySystemState.findRoot() first
            // looks for the python.home property, so if it is
            // not set we set it.
            if (System.getProperty("python.home") == null) {
                // Look for jython.jar in the classpath
                // Start of code based on python/core/PySystemState.findRoot()
                String classpath = StringUtilities
                        .getProperty("java.class.path");

                int jythonIndex = -1;
                if (classpath == null) {
                    System.setProperty("python.home",
                            StringUtilities.getProperty("user.home"));
                } else {
                    jythonIndex = classpath.toLowerCase(Locale.getDefault())
                            .indexOf("jython.jar");
                }

                if (jythonIndex == -1) {
                    // We did not find jython.jar, so set it to user.home.
                    // WebStart will end up here.
                    System.setProperty("python.home",
                            StringUtilities.getProperty("user.home"));
                } else {
                    // We found jython.jar, return the parent directory.
                    // Under WebStart, jython.jar will not be in the classpath
                    int start = classpath.lastIndexOf(
                            java.io.File.pathSeparator, jythonIndex) + 1;
                    System.setProperty("python.home",
                            classpath.substring(start, jythonIndex));
                }

                // End of code based on python/core/PySystemState.findRoot()
            }
        } catch (Exception ex) {
            // Ignore, we are probably under an an applet
            System.err
            .println("Warning: PythonScript threw an exception.  Perhaps we are under an applet?");
            ex.printStackTrace();
        }

        try {
            _interpreter = new PythonInterpreter();
        } catch (java.security.AccessControlException ex) {
            // In an applet, instantiating a PythonInterpreter
            // causes PySystemState.initialize() to call
            // System.getProperties(), which throws an exception
            // The solution is to pass our own custom Properties
            // Properties that are accessible via an applet.
            String[] propertyNames = { "file.separator", "line.separator",
                    "path.separator", "java.class.version", "java.vendor",
                    "java.vendor.url", "java.version", "os.name", "os.arch",
                    "os.version" };
            Properties preProperties = new Properties();

            for (String propertyName : propertyNames) {
                preProperties.setProperty(propertyName,
                        System.getProperty(propertyName));
            }

            PySystemState.initialize(preProperties, null, new String[] { "" });
            _interpreter = new PythonInterpreter();
        }

        try {
            //String ptIIDir = StringUtilities.getProperty("ptolemy.ptII.dir");
            _interpreter.exec("import sys\n");
            //_interpreter.exec("sys.path.append('" + ptIIDir
            //        + "/ptolemy/actor/lib/python/test/')");

        } catch (Exception ex) {
            ExceptionInInitializerError error = new ExceptionInInitializerError(
                    "The python command \"import sys\" failed.");
            error.initCause(ex);
            throw error;
        }

        String className = "ptolemy.kernel.util.NamedObj";
        String classResource = ClassUtilities.lookupClassAsResource(className);

        if (classResource != null) {
            //System.out.println("PythonScript: className: " + classResource);
            File classFile = new File(classResource);

            if (classFile.isDirectory()) {
                PySystemState.add_extdir(classResource);
            } else {
                PySystemState.add_classdir(classResource);
            }
        }
    }

    // Map from method name to PyMethod objects.
    private HashMap _methodMap = new HashMap();

    // The instance of the jythonClassName class defined in the script.
    private PyObject _object;

    // Invocation of methods named in this list is delegated to the instance
    // of the jythonClassName class defined in the script.
    // Listed here are all methods of the Executable interface, except
    // iterate().
    private static final String[] _METHOD_NAMES = { "fire", "initialize",
            "postfire", "prefire", "preinitialize", "stop", "stopFire",
            "terminate", "wrapup" };
}
