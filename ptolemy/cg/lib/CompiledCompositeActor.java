/* An aggregation of typed actors with cosimulation option.

 Copyright (c) 2007-2014 The Regents of the University of California.
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
package ptolemy.cg.lib;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.util.DFUtilities;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.FileUtilities;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringBufferExec;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// CompiledCompositeActor

/**
 A composite actor that can be optionally code generated and then
 invoked via reflection.

 @author Gang Zhou, contributors: Christopher Brooks, Edward A. Lee, Bert Rodiers, Dai Bui
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating red (zgang)
 @Pt.AcceptedRating red (zgang)
 */
public class CompiledCompositeActor extends TypedCompositeActor {
    /** Construct a CodeGenerationCompositeActor in the default workspace
     *  with no container and an empty string as its name. Add the actor to
     *  the workspace directory.
     *  You should set a director before attempting to execute it.
     *  You should set the container before sending data to it.
     *  Increment the version number of the workspace.
     */
    public CompiledCompositeActor() {
        super();
        _init();
    }

    /** Create an actor with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  This actor will have no local director initially, and its executive
     *  director will be simply the director of the container.
     *  You should set a director before attempting to execute it.
     *
     *  @param container The container actor.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CompiledCompositeActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     parameters                            ////

    /** Construct a CodeGenerationCompositeActor in the specified workspace
     *  with no container and an empty string as a name. You can then change
     *  the name with setName(). If the workspace argument is null, then use
     *  the default workspace.
     *  You should set a director before attempting to execute it.
     *  You should set the container before sending data to it.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the actor.
     */
    public CompiledCompositeActor(Workspace workspace) {
        super(workspace);
        _init();
    }

    /** The directory in which to put the generated code.
     *  This is a file parameter that must specify a directory.
     *  The default is $HOME/cg.
     */
    public FileParameter codeDirectory;

    /** The name of the package in which to look for adapter class
     *  code generators. This is a string that defaults to
     *  "generic.program.procedural.java"
     */
    public StringParameter generatorPackage;

    /** If true, generate file with no functions.  If false, generate
     *  file with functions. The default value is a parameter with the
     *  value true.
     */
    public Parameter inline;

    /** If true, then invoke the generated code in the action methods
     *  (fire(), etc.).
     *  If the value is false, this actor will be executed
     *  executing like an ordinary composite actor.  Classes like EmbeddedJavaActor
     *  set embedded to true when there is only Java code specifying
     *  the functionality of an actor.
     */
    public Parameter executeEmbeddedCode;

    /** If true, overwrite preexisting files.  The default
     *  value is a parameter with the value true.
     */
    public Parameter overwriteFiles;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    // FIXME: preinitialize(), prefire(), and postfire() are all automatically
    // delegated to the superclass, because they are not overridden here.
    // Is that right?  It seems that codegen is not following the actor
    // semantics yet.

    /** If <i>executeEmbeddedCode</i> is true, then execute the fire() method
     *  of the generated code. Otherwise, delegate to the
     *  superclass, which executes this actor like an ordinary composite
     *  actor.
     *  @exception IllegalActionException If thrown by the super
     *  class, or if there are problems invoking the fire() method of
     *  wrapper class.
     */
    @Override
    public void fire() throws IllegalActionException {

        boolean invoked = ((BooleanToken) executeEmbeddedCode.getToken())
                .booleanValue();
        if (invoked) {
            if (_debugging) {
                _debug("Calling fire()");
            }

            try {
                _workspace.getReadAccess();

                if (!isOpaque()) {
                    throw new IllegalActionException(this,
                            "Cannot fire a non-opaque actor.");
                }

                List<Object> tokensFromAllInputPorts = new LinkedList<Object>();

                for (Iterator<?> inputPorts = inputPortList().iterator(); inputPorts
                        .hasNext() && !_stopRequested;) {
                    IOPort port = (IOPort) inputPorts.next();
                    if (!(port instanceof ParameterPort)) {
                        Object tokens = _transferInputs(port);
                        tokensFromAllInputPorts.add(tokens);
                    }
                }

                if (_stopRequested) {
                    return;
                }

                Object[] tokensToAllOutputPorts = null;
                try {
                    // Invoke the native fire method
                    tokensToAllOutputPorts = (Object[]) _fireMethod.invoke(
                            _objectWrapper, tokensFromAllInputPorts.toArray());
                } catch (Throwable throwable) {
                    throw new IllegalActionException(this, throwable,
                            "Failed to invoke the fire method on "
                                    + "the wrapper class.");
                }

                if (_stopRequested) {
                    return;
                }

                int portNumber = 0;
                for (Iterator<?> outputPorts = outputPortList().iterator(); outputPorts
                        .hasNext() && !_stopRequested;) {
                    IOPort iOPort = (IOPort) outputPorts.next();
                    ModularCodeGenLazyTypedCompositeActor._transferOutputs(
                            this, iOPort, tokensToAllOutputPorts[portNumber++]);
                }

            } finally {
                _workspace.doneReading();
            }

            if (_debugging) {
                _debug("Called fire()");
            }
        } else {
            super.fire();
        }
    }

    /** Return the sanitized file name of this actor.  The sanitized name
     *  is created by invoking
     *  {@link ptolemy.util.StringUtilities#sanitizeName(String)},
     *  removing underscores and appending a version number.
     *  The version number is necessary so that we can reload the
     *  shared object.
     *  @return The sanitized actor name.
     */
    public String getSanitizedName() {
        return _sanitizedActorName;
    }

    /** If <i>executeEmbeddedCode</i> is true, then generate and compile the
     *  code (if necessary), and then execute the initialize() method
     *  of the generated code. Otherwise, delegate to the
     *  superclass, which initializes this actor like an ordinary composite
     *  actor.
     *  @exception IllegalActionException If there is no director, or
     *   if the director's initialize() method throws it, or if the
     *   actor is not opaque.
     */
    @Override
    public void initialize() throws IllegalActionException {

        super.initialize();

        boolean invoked = ((BooleanToken) executeEmbeddedCode.getToken())
                .booleanValue();
        if (invoked) {
            if (_generatedCodeVersion != _workspace.getVersion()) {

                _updateSanitizedActorName();

                if (_buildSharedObjectFile()) {
                    //                     System.err.println("CompiledCompositeActor: #0"
                    //                            + _generatedCodeVersion + " "
                    //                            + _loadedCodeVersion + " "
                    //                            + _workspace.getVersion());
                    //                     System.err.flush();

                    if (_loadedCodeVersion != -1) {
                        // We already loaded once, so increment the
                        // version number used to generate the
                        // sanitizedActorName
                        //++_version;
                        _updateSanitizedActorName();
                    }
                    String generatorPackageString = generatorPackage
                            .getExpression();
                    if (generatorPackageString
                            .equals("generic.program.procedural.c")) {
                        _generateAndCompileJNICode();
                    }
                    _generateAndCompileEmbeddedCode();
                    _generatedCodeVersion = _workspace.getVersion();
                }
                if (_generatedCodeVersion == -1) {
                    // We did not build the shared object, we
                    // are reusing a preexisting one.
                    _generatedCodeVersion = _workspace.getVersion();
                }

                if (_loadedCodeVersion != _generatedCodeVersion) {

                    //                     System.err.println("CompiledCompositeActor: #1"
                    //                            + _generatedCodeVersion + " "
                    //                            + _loadedCodeVersion + " "
                    //                            + _workspace.getVersion());
                    //                     System.err.flush();
                    String className = _sanitizedActorName;
                    URL url = null;
                    URLClassLoader classLoader = null;
                    Class<?> classInstance = null;

                    try {
                        url = codeDirectory.asFile().toURI().toURL();
                        URL[] urls = new URL[] { url };

                        classLoader = new URLClassLoader(urls);
                        classInstance = classLoader.loadClass(className);

                    } catch (ClassNotFoundException ex) {
                        throw new IllegalActionException(
                                this,
                                ex,
                                "The class URL \""
                                        + url
                                        + "\" for \""
                                        + className
                                        + "\" could not be found.  "
                                        + "Make sure that the cg directory is not being deleted.");
                    } catch (MalformedURLException ex) {
                        throw new IllegalActionException(this, ex,
                                "The class URL \"" + url + "\" for \""
                                        + className + "\" is malformed");
                    } catch (UnsupportedClassVersionError ex) {
                        // This can occur if we have two different
                        // machines sharing ~/cg
                        throw new IllegalActionException(
                                this,
                                ex,
                                "Unsupported class version in the class \""
                                        + className
                                        + "\" from \""
                                        + url
                                        + "\".  Try deleting the \""
                                        + className
                                        + "\" class in \""
                                        + url
                                        + "\".\nThis problem can also occur "
                                        + "if the version of java that is "
                                        + "running Ptolemy and the version "
                                        + "of javac used to compile the file "
                                        + "to load into Ptolemy are different "
                                        + "and java is of a later version."
                                        + "\nTo see information about the "
                                        + "version of Java used to run "
                                        + "Ptolemy, use View -> JVM Properties."
                                        + "  To see what version of javac "
                                        + "was used, run \"java -version\".");
                    } catch (Throwable ex) {
                        throw new IllegalActionException(this, ex,
                                "Cannot load the class \"" + className
                                + "\" from \"" + url + "\"");
                        // java.net.URLClassLoader is not present in Java 1.6.
                    } finally {
                        if (classLoader != null) {
                            try {
                                classLoader.close();
                            } catch (IOException ex) {
                                throw new IllegalActionException(this, ex,
                                        "Failed to close \""
                                                + (url == null ? "null" : url)
                                                + "\".");
                            }
                        }
                    }

                    try {
                        _objectWrapper = classInstance.newInstance();
                    } catch (Throwable throwable) {
                        throw new IllegalActionException(this, throwable,
                                "Cannot instantiate the wrapper object.");
                    }

                    Method[] methods = classInstance.getMethods();
                    for (Method method : methods) {
                        String name = method.getName();
                        if (name.equals("fire")) {
                            _fireMethod = method;
                        } else if (name.equals("initialize")) {
                            _initializeMethod = method;
                        } else if (name.equals("wrapup")) {
                            _wrapupMethod = method;
                        }
                    }
                    if (_fireMethod == null) {
                        throw new IllegalActionException(this,
                                "Cannot find fire "
                                        + "method in the wrapper class.");
                    }
                    if (_initializeMethod == null) {
                        throw new IllegalActionException(this,
                                "Cannot find initialize "
                                        + "method in the wrapper class.");
                    }
                    if (_wrapupMethod == null) {
                        throw new IllegalActionException(this,
                                "Cannot find wrapup "
                                        + "method in the wrapper class.");
                    }
                    _loadedCodeVersion = _workspace.getVersion();
                }

            }

            try {
                // Java 1.4, used by Kepler, requires the two arg invoke()
                // Cast to Object() to suppress Java 1.5 warning
                _initializeMethod.invoke(_objectWrapper, (Object[]) null);
            } catch (Throwable throwable) {
                System.out.println("Failed to invoke " + _initializeMethod
                        + " " + throwable.getCause());
                throw new IllegalActionException(this, throwable,
                        "Failed to invoke the initialize method \""
                                + _initializeMethod + "\" on"
                                + " the wrapper class.");
            }

        }
    }

    /** If <i>executeEmbeddedCode</i> is true, then execute the wrapup() method
     *  of the generated code. Otherwise, delegate to the
     *  superclass, which executes this actor like an ordinary composite
     *  actor.
     *  @exception IllegalActionException If there is no director,
     *   or if the director's wrapup() method throws it, or if this
     *   actor is not opaque.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        boolean invoked = ((BooleanToken) executeEmbeddedCode.getToken())
                .booleanValue();

        if (invoked) {
            if (_objectWrapper == null) {
                // If we are generating code for an entire model,
                // we might end up here.
                return;
            }

            try {
                // Java 1.4, used by Kepler, requires the two arg invoke()
                // Cast to Object() to supress Java 1.5 warning
                _wrapupMethod.invoke(_objectWrapper, (Object[]) null);
            } catch (Throwable throwable) {
                throw new IllegalActionException(this, throwable,
                        "Failed to invoke the wrapup method on "
                                + "the wrapper class.");
            }
        }
        // _generatedCodeVersion = _workspace.getVersion();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Compile the Java code.
     *  The <code>javac</code> and <code>javah</code> commands are
     *  executed on the the java file.
     *  @exception IllegalActionException If there is a problem reading
     *  the <i>codeDirectory</i> parameter.
     */
    protected void _compileJNICode() throws IllegalActionException {
        StringBufferExec executeCommands = new StringBufferExec(true);

        List<String> commands = new LinkedList<String>();

        // Create the .class file.
        commands.add("javac -classpath . " + _sanitizedActorName + ".java");

        // We don't have to create the .h file, we create the
        // signatures by hand.
        // commands.add("javah -classpath . " + _sanitizedActorName);

        // Remove the .h file that was generated so that we avoid
        // compilation problems.
        commands.add("rm -f " + _sanitizedActorName + ".h");

        if (_debugging) {
            _debugAndSystemOut("Execute command: " + commands.get(0));
            _debugAndSystemOut("Execute command: " + commands.get(1));
        }

        executeCommands.setWorkingDirectory(codeDirectory.asFile());
        executeCommands.setCommands(commands);
        executeCommands.start();
        int lastSubprocessReturnCode = executeCommands
                .getLastSubprocessReturnCode();
        if (lastSubprocessReturnCode != 0) {
            throw new IllegalActionException(this,
                    "Execution of subcommands failed, last process returned "
                            + lastSubprocessReturnCode + ", which is not 0:\n"
                            + executeCommands.buffer.toString());
        }
    }

    /** Generate and compile Java code.
     *  @exception IllegalActionException If the adapter class cannot
     *  be found, or if the static generateCode(TypedCompositeActor)
     *  method in the adapter class cannot be found or invoked.
     */
    protected void _generateAndCompileEmbeddedCode()
            throws IllegalActionException {
        _invokeAdapterMethod("generateCode");
    }

    /** Generate and compile the JNI code.
     *  @exception IllegalActionException If thrown while getting the path
     *  to the shared object, while writing the Java file, or while
     *  compiling the Java file.
     */
    protected void _generateAndCompileJNICode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        String sharedObjectPath = _sharedObjectPath(_sanitizedActorName);
        code.append("public class " + _sanitizedActorName + " {\n" + "\n"
                + "    public native Object[] fire(" + _getArguments() + ");\n"
                + "    public native void initialize();\n"
                + "    public native void wrapup();\n"
                + "    static {\n"

                // + "        String library = \"" + _sanitizedActorName + "\";\n"
                // + "        System.loadLibrary(library);\n"

                + "        String library = \"" + sharedObjectPath + "\";\n"
                + "        System.load(library);\n" + "    }\n" + "}\n");

        String codeFileName = _sanitizedActorName + ".java";

        // Write the code to a file with the same name as the model into
        // the directory named by the codeDirectory parameter.
        try {
            File codeDirectoryFile = codeDirectory.asFile();
            if (codeDirectoryFile.isFile()) {
                throw new IOException("Error: " + codeDirectory.stringValue()
                        + " is a file, " + "it should be a directory.");
            }
            if (!codeDirectoryFile.isDirectory() && !codeDirectoryFile.mkdirs()) {
                throw new IOException("Failed to make the \""
                        + codeDirectory.stringValue() + "\" directory.");
            }

            // FIXME: Note that we need to make the directory before calling
            // getBaseDirectory()
            codeDirectory.setBaseDirectory(codeDirectory.asFile().toURI());

            // Check if needs to overwrite.
            File writeFile = new File(codeDirectoryFile, codeFileName);
            if (!((BooleanToken) overwriteFiles.getToken()).booleanValue()
                    && writeFile.exists()) {
                if (!MessageHandler.yesNoQuestion(codeDirectory.asFile()
                        + " exists. OK to overwrite?")) {
                    /*
                    throw new IllegalActionException(this,
                            "Please select another file name.");
                     */
                    return;
                }
            }

            Writer writer = null;
            try {
                if (_debugging) {
                    _debugAndSystemOut("Generate \"" + codeFileName
                            + "\" in \"" + codeDirectory.getBaseDirectory()
                            + "\"");
                }

                writer = FileUtilities.openForWriting(codeFileName,
                        codeDirectory.getBaseDirectory(), false);
                System.out
                        .println("CompiledCompositeActor wrote "
                                + codeDirectory.getBaseDirectory() + " "
                                + codeFileName);
                writer.write(code.toString());
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
        } catch (Throwable throwable) {
            throw new IllegalActionException(this, throwable,
                    "Failed to write \"" + codeFileName + "\" in "
                            + codeDirectory.getBaseDirectory());
        }

        _compileJNICode();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return true if the shared object file should be built.  The
     *  shared object file must be built if shared object file does
     *  not exist or if the model has been modified and not saved or
     *  if the modification time of the shared object file is earlier
     *  than the modification time of the model file.
     *  This method always copies files listed in the fileDependency
     *  block.
     *  @return true if the shared object file should be built.
     *  @exception IllegalActionException If there is a problem
     *  generating the path to the shared object.
     */
    private boolean _buildSharedObjectFile() throws IllegalActionException {
        String message = "CompiledCompositeActor: Building shared object: ";

        // Look for the fileDependencies code block and copy files if
        // necessary.  If we copy files, then we should rebuild.
        // FIXME: this is a side effect, and we should be sure to do
        // it before returning from this method.
        _invokeAdapterMethod("copyFilesToCodeDirectory");

        File sharedObjectFile = new File(_sharedObjectPath(_sanitizedActorName));

        Effigy effigy = Configuration.findEffigy(toplevel());

        // FIXME
        // effigy.isModified() is not the optimal way to deal with changes.
        // It might be the case that we already compiled after the change.
        if (effigy != null && effigy.isModified()) {
            System.out
                    .println(message
                            + "The effigy "
                            + effigy
                            + "(model : "
                            + ((PtolemyEffigy) effigy).getModel()
                            + ") says the model was modified and thus it does not matter "
                            + "if the shared object file is newer than the model file "
                            + "because the model file is out of date.");
            return true;
        }

        URI modelURI = URIAttribute.getModelURI(this);
        if (modelURI == null) {
            System.out.println(message
                    + "This model does not have a _uri parameter.");
            return true;
        }
        String modelPath = modelURI.getPath();
        File modelFile = null;
        try {
            modelFile = new File(modelPath);
        } catch (Exception ex) {
            // Ignore, perhaps modelURI points to a remote model.
            modelFile = null;
        }
        if (modelFile == null
                || sharedObjectFile.lastModified() < modelFile.lastModified()) {
            System.out.println(message
                    + "The sharedObjectFile has a modification time "
                    + "that is earlier than the modelFile modification time.");
            return true;
        }

        if (effigy == null) {
            System.out.println(message + "No effigy.  This can happen when "
                    + "CodeGenerator.generateCode() is called from within "
                    + "the test suite.  The code will be recompiled.");
            //_version = ++_noEffigyVersion;
            _updateSanitizedActorName();
            return true;
        }
        return false;
    }

    /** Send a debug message to all debug listeners that have registered.
     * Then print the message to System.out.
     * @param message The given debug message.
     */
    private void _debugAndSystemOut(String message) {
        super._debug(message);
        System.out.println(message);
    }

    private String _getArguments() {

        StringBuffer arguments = new StringBuffer();
        Iterator<?> inputPorts = inputPortList().iterator();
        int i = 0;
        while (inputPorts.hasNext()) {
            IOPort inputPort = (IOPort) inputPorts.next();
            if (!(inputPort instanceof ParameterPort)) {

                if (i != 0) {
                    arguments.append(", ");
                }
                i++;
                //FIXME: need to consider carefully for structured data type
                Type type = ((TypedIOPort) inputPort).getType();
                String typeName = type.toString();
                if (typeName.equals("unknown") || typeName.equals("Pointer")) {
                    // If a port is not connected, then use int as a default.
                    typeName = "int";
                }
                arguments.append(typeName + "[][] " + inputPort.getName());
            }
        }
        return arguments.toString();
    }

    /** Invoke a method in the corresponding adapter class.
     *  @param methodName The name of a method in the adapter class.
     *  The method must be static and take a TypedCompositeArgument as its
     *  only argument.
     *  @return The return value from the method, see java.lang.Method.invoke().
     *  @exception IllegalActionException If the adapter class can't be
     *  found or if the method cannot be invoked.
     */
    private Object _invokeAdapterMethod(String methodName)
            throws IllegalActionException {
        // We use reflection to avoid a compile time dependency
        // on the cg package.
        String packageName = "ptolemy.cg.adapter."
                + generatorPackage.stringValue() + ".adapters";
        String adapterClassName = getClass().getName().replaceFirst("ptolemy",
                packageName + ".ptolemy");

        Class<?> adapterClass = null;
        try {
            adapterClass = Class.forName(adapterClassName);
        } catch (ClassNotFoundException ex) {
            throw new IllegalActionException(this, ex,
                    "Cannot find adapter class " + adapterClassName);
        }

        Method generateMethod = null;
        try {
            // Find the
            // ptolemy.cg.adapters.ptolemy ... generateCode() method.
            generateMethod = adapterClass.getMethod(methodName,
                    new Class[] { ptolemy.actor.TypedCompositeActor.class });
        } catch (NoSuchMethodException ex) {
            throw new IllegalActionException(this, ex, "Cannot find the \""
                    + methodName + "\" method in \"" + adapterClassName + "\".");
        }

        try {
            // Invoke the static method that takes a TypedCompositeActor
            // as an argument.
            return generateMethod.invoke(null, new Object[] { this });
        } catch (java.lang.reflect.InvocationTargetException ex) {
            // If we get an InvocationTargetException, rethrow the
            // exception with the proper cause exception so that we
            // get a better message.
            Throwable cause = ex.getCause();
            if (cause instanceof java.lang.reflect.InvocationTargetException) {
                cause = cause.getCause();
            }
            throw new IllegalActionException(this, cause,
                    "Failed to invoke the \"" + methodName + "\" method in \""
                            + adapterClassName + "\".");
        } catch (Throwable throwable) {
            throw new IllegalActionException(this, throwable,
                    "Failed to invoke the \"" + methodName + "\" method in \""
                            + adapterClassName + "\".");
        }
    }

    /** Initialize parameters.
     */
    private void _init() {

        // The base class identifies the class name as TypedCompositeActor
        // irrespective of the actual class name.  We override that here.
        setClassName("ptolemy.cg.lib.CompiledCompositeActor");
        /*
                if (!_pointerTypeInitialized) {
                    Constants.add("pointer", new PointerToken());
                    _pointerTypeInitialized = true;
                }
         */
        try {
            generatorPackage = new StringParameter(this, "generatorPackage");
            generatorPackage.setExpression("generic.program.procedural.java");

            inline = new Parameter(this, "inline");
            inline.setTypeEquals(BaseType.BOOLEAN);
            inline.setExpression("false");

            codeDirectory = new FileParameter(this, "codeDirectory");
            codeDirectory.setExpression("$HOME/cg/");

            // FIXME: This should not be necessary, but if we don't
            // do it, then getBaseDirectory() thinks we are in the current dir.
            codeDirectory.setBaseDirectory(codeDirectory.asFile().toURI());
            new Parameter(codeDirectory, "allowFiles", BooleanToken.FALSE);
            new Parameter(codeDirectory, "allowDirectories", BooleanToken.TRUE);

            executeEmbeddedCode = new Parameter(this, "executeEmbeddedCode");
            executeEmbeddedCode.setTypeEquals(BaseType.BOOLEAN);
            executeEmbeddedCode.setExpression("true");
            // Hide the executeEmbeddedCode parameter from the user.
            // executeEmbeddedCode.setVisibility(Settable.NONE);

            overwriteFiles = new Parameter(this, "overwriteFiles");
            overwriteFiles.setTypeEquals(BaseType.BOOLEAN);
            overwriteFiles.setExpression("true");

        } catch (Exception ex) {
            throw new InternalErrorException(this, ex,
                    "Problem setting up coSimulation parameter");
        }
    }

    /** Get the name of the shared object.
     *  @param sanitizedActorName The sanitized actor name on
     *  which to base the name of the shared object.
     *  @return The name of the .dll or .so file.
     *  @exception IllegalActionException If there is a problem
     *  reading the <i>codeDirectory</i> parameter.
     */
    private String _sharedObjectPath(String sanitizedActorName)
            throws IllegalActionException {
        String sharedObjectPath = null;
        String generatorPackageString = generatorPackage.getExpression();
        String fileName = "";
        try {
            if (generatorPackageString
                    .equals("generic.program.procedural.java")) {
                fileName = sanitizedActorName + ".class";
            } else if (generatorPackageString
                    .equals("generic.program.procedural.c")) {
                String osName = StringUtilities.getProperty("os.name");
                if (osName != null) {
                    if (osName.startsWith("Windows")) {
                        fileName = sanitizedActorName + ".dll";
                    } else if (osName.startsWith("Mac OS X")) {
                        fileName = "lib" + sanitizedActorName + ".dylib";
                    } else {
                        fileName = "lib" + sanitizedActorName + ".so";
                    }
                }
            } else {
                throw new IllegalActionException(this, "generatorPackage "
                        + generatorPackage + " not supported.");
            }
            sharedObjectPath = codeDirectory.asFile().getCanonicalPath()
                    + File.separator + fileName;
            sharedObjectPath = sharedObjectPath.replace("\\", "/");
        } catch (IOException ex) {
            throw new IllegalActionException(this, ex,
                    "Cannot generate library path.");
        }

        return sharedObjectPath;
    }

    private Object _transferInputs(IOPort port) throws IllegalActionException {

        int rate = DFUtilities.getTokenConsumptionRate(port);
        Type type = ((TypedIOPort) port).getType();
        Object tokenHolder = null;

        int numberOfChannels = port.getWidth() < port.getWidthInside() ? port
                .getWidth() : port.getWidthInside();

        if (type == BaseType.INT) {
            tokenHolder = new int[numberOfChannels][];
        } else if (type == BaseType.DOUBLE) {
            tokenHolder = new double[numberOfChannels][];
            /*} else if (type == PointerToken.POINTER) {
            tokenHolder = new int[numberOfChannels][];*/
        } else if (type == BaseType.BOOLEAN) {
            tokenHolder = new boolean[numberOfChannels][];
        } else {
            // FIXME: need to deal with other types
        }

        for (int i = 0; i < port.getWidth(); i++) {
            try {
                if (i < port.getWidthInside()) {

                    if (port.hasToken(i, rate)) {
                        Token[] tokens = port.get(i, rate);

                        if (_debugging) {
                            _debug(getName(),
                                    "transferring input from " + port.getName());
                        }

                        if (type == BaseType.INT) {

                            int[] intTokens = new int[rate];
                            for (int k = 0; k < rate; k++) {
                                intTokens[k] = ((IntToken) tokens[k])
                                        .intValue();
                            }
                            ((int[][]) tokenHolder)[i] = intTokens;

                        } else if (type == BaseType.DOUBLE) {

                            double[] doubleTokens = new double[rate];
                            for (int k = 0; k < rate; k++) {
                                doubleTokens[k] = ((DoubleToken) tokens[k])
                                        .doubleValue();
                            }
                            ((double[][]) tokenHolder)[i] = doubleTokens;

                            /*} else if (type == PointerToken.POINTER) {

                            int[] intTokens = new int[rate];
                            for (int k = 0; k < rate; k++) {
                            intTokens[k] = ((PointerToken) tokens[k])
                            .getValue();
                            }
                            ((int[][]) tokenHolder)[i] = intTokens;
                             */
                        } else if (type == BaseType.BOOLEAN) {

                            boolean[] booleanTokens = new boolean[rate];
                            for (int k = 0; k < rate; k++) {
                                booleanTokens[k] = ((BooleanToken) tokens[k])
                                        .booleanValue();
                            }
                            ((boolean[][]) tokenHolder)[i] = booleanTokens;

                        } else {
                            // FIXME: need to deal with other types
                        }

                    } else {
                        throw new IllegalActionException(this, port,
                                "Port should consume " + rate
                                        + " tokens, but there were not "
                                        + " enough tokens available.");
                    }

                } else {
                    // No inside connection to transfer tokens to.
                    // In this case, consume one input token if there is one.
                    if (_debugging) {
                        _debug(getName(),
                                "Dropping single input from " + port.getName());
                    }

                    if (port.hasToken(i)) {
                        port.get(i);
                    }
                }
            } catch (NoTokenException ex) {
                // this shouldn't happen.
                throw new InternalErrorException(this, ex, null);
            }

        }
        return tokenHolder;
    }

    /** Update the _sanitizedActorName variable.
     *  Consider calling this method each time _version is updated.
     */
    private void _updateSanitizedActorName() {
        _sanitizedActorName = StringUtilities.sanitizeName(getFullName());
        // Used to be
        // Remove leading underscore
        if (_sanitizedActorName.charAt(0) == '_') {
            _sanitizedActorName = _sanitizedActorName.replaceFirst("_", "");
        }
        // But _ has a special meaning in JNI and javah will add things after a _
        // Remove all underscores to avoid confusion for JNI
        // related functions.  Each time a .dll file is
        // generated, we must use a different name for it so
        // that it can be loaded without restarting vergil.

        // _sanitizedActorName = _sanitizedActorName.replace("_", "") + _version;

    }

    private Object _objectWrapper;

    private transient Method _fireMethod;

    private transient Method _initializeMethod;

    private transient Method _wrapupMethod;

    /** The sanitized actor name.  The name has the underscores
     *  removed and the value of the _version variable appended.  Call
     *  _updateSanitizedActorName() to properly set this variable.
     */
    private String _sanitizedActorName;

    /** The workspace version for which the code was generated.
     *  If the workspace version and this variable differ,
     *  Then there is a chance we should regenerate the code.
     */
    private long _generatedCodeVersion = -1;

    /** The workspace version for which the code was loaded.  If the
     *  workspace version and this variable differ, then we should
     *  reload the code.  Note that we don't want to reload the same
     *  dll multiple times or we will get "Native Library foo.dll
     *  already loaded in another classloader"
     */
    private long _loadedCodeVersion = -1;

    /** The version of the shared object.  Each time we rebuild, the
     *  version number gets incremented.  If you change _version, then
     *  consider calling _updateSanitizedActorName.
     */
    //private int _version = 0;

    /** The version of the shared object to use if we have no effigy.
     *  If we don't have an effigy, then we increment this variable
     *  and set _version to its value.
     */
    //private static int _noEffigyVersion = 0;

    //private static boolean _pointerTypeInitialized = false;

}
