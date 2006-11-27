/* An aggregation of typed actors with cosimulation option.

 Copyright (c) 1997-2006 The Regents of the University of California.
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
package ptolemy.actor;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.util.DFUtilities;
import ptolemy.codegen.kernel.CodeGenerator;
import ptolemy.codegen.kernel.StaticSchedulingCodeGenerator;
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
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.ExecuteCommands;
import ptolemy.util.FileUtilities;
import ptolemy.util.MessageHandler;
import ptolemy.util.StreamExec;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
//// TypedCompositeActorWithCoSimulation

/**
 A TypedCompositeActorWithCoSimulation is an aggregation of typed actors with 
 cosimulation option.

 @author Gang Zhou
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating red (zgang)
 @Pt.AcceptedRating red (zgang)
 @see ptolemy.actor.TypedCompositeActor
 */

public class TypedCompositeActorWithCoSimulation extends
        ptolemy.actor.TypedCompositeActor {
    /** Construct a CodeGenerationCompositeActor in the default workspace 
     *  with no container and an empty string as its name. Add the actor to 
     *  the workspace directory.
     *  You should set a director before attempting to execute it.
     *  You should set the container before sending data to it.
     *  Increment the version number of the workspace.
     */
    public TypedCompositeActorWithCoSimulation() {
        super();
        _init();
    }

    /** Construct a CodeGenerationCompositeActor in the specified workspace
     *  with no container and an empty string as a name. You can then change 
     *  the name with setName(). If the workspace argument is null, then use 
     *  the default workspace.
     *  You should set a director before attempting to execute it.
     *  You should set the container before sending data to it.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the actor.
     */
    public TypedCompositeActorWithCoSimulation(Workspace workspace) {
        super(workspace);
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
    public TypedCompositeActorWithCoSimulation(CompositeEntity container,
            String name) throws IllegalActionException,
            NameDuplicationException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** True if in coSimulation mode.  The initial default is
     *  a BooleanToken with value false.
     */
    public Parameter coSimulation;

    ///////////////////////////////////////////////////////////////////
    ////                     parameters                            ////

    /** The directory in which to put the generated code.
     *  This is a file parameter that must specify a directory.
     *  The default is $HOME/codegen.
     */
    public FileParameter codeDirectory;

    /** The name of the package in which to look for helper class
     *  code generators. This is a string that defaults to
     *  "ptolemy.codegen.c".
     */
    public StringParameter generatorPackage;

    /** If true, generate file with no functions.  If false, generate
     *  file with functions. The default value is a parameter with the 
     *  value true.
     */
    public Parameter inline;

    /** If true, overwrite preexisting files.  The default
     *  value is a parameter with the value true.
     */
    public Parameter overwriteFiles;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If this actor is opaque, transfer any data from the input ports
     *  of this composite to the ports connected on the inside, and then
     *  invoke the fire() method of its local director.
     *  The transfer is accomplished by calling the transferInputs() method
     *  of the local director (the exact behavior of which depends on the
     *  domain).  If the actor is not opaque, throw an exception.
     *  This method is read-synchronized on the workspace, so the
     *  fire() method of the director need not be (assuming it is only
     *  called from here).  After the fire() method of the director returns,
     *  send any output data created by calling the local director's
     *  transferOutputs method.
     *
     *  @exception IllegalActionException If there is no director, or if
     *   the director's fire() method throws it, or if the actor is not
     *   opaque.
     */
    public void fire() throws IllegalActionException {

        boolean inCoSimulationMode = ((BooleanToken) coSimulation.getToken())
                .booleanValue();
        if (inCoSimulationMode) {

            if (_debugging) {
                _debug("Calling fire()");
            }

            try {
                _workspace.getReadAccess();

                if (!isOpaque()) {
                    throw new IllegalActionException(this,
                            "Cannot fire a non-opaque actor.");
                }

                List tokensFromAllInputPorts = new LinkedList();

                for (Iterator inputPorts = inputPortList().iterator(); inputPorts
                        .hasNext()
                        && !_stopRequested;) {
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
                    tokensToAllOutputPorts = (Object[]) _jniFireMethod.invoke(
                            _jniWrapper, tokensFromAllInputPorts.toArray());
                } catch (Exception e) {
                    throw new IllegalActionException(this, e,
                            "Failed to invoke the fire method on the wrapper class.");
                }

                if (_stopRequested) {
                    return;
                }

                int portNumber = 0;
                for (Iterator outputPorts = outputPortList().iterator(); outputPorts
                        .hasNext()
                        && !_stopRequested;) {
                    IOPort port = (IOPort) outputPorts.next();
                    _transferOutputs(port, tokensToAllOutputPorts[portNumber++]);
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

    /** Initialize this actor. 
     *
     *  @exception IllegalActionException If there is no director, or
     *   if the director's initialize() method throws it, or if the
     *   actor is not opaque.
     */
    public void initialize() throws IllegalActionException {

        super.initialize();

        boolean inCoSimulationMode = ((BooleanToken) coSimulation.getToken())
                .booleanValue();
        if (inCoSimulationMode) {

            if (_generatedCodeVersion != _workspace.getVersion()) {

                _sanitizedActorName = StringUtilities.sanitizeName(getName());
                _generateJavaCode();
                _generateCCode();

                String jniClassName = "Jni" + _sanitizedActorName;
                Class jniClass = null;
                try {
                    URL url = codeDirectory.asFile().toURL();
                    URL[] urls = new URL[] { url };

                    ClassLoader classLoader = new URLClassLoader(urls);
                    jniClass = classLoader.loadClass(jniClassName);

                } catch (MalformedURLException e) {
                    throw new IllegalActionException(this, "The class URL for "
                            + jniClassName + "is malformed");
                } catch (ClassNotFoundException e) {
                    throw new IllegalActionException(this,
                            "Cannot load the class " + jniClassName);
                }

                try {
                    _jniWrapper = jniClass.newInstance();
                } catch (Exception e) {
                    throw new IllegalActionException(this, e,
                            "Cannot instantiate the wrapper object.");
                }

                Method[] methods = jniClass.getMethods();
                for (int i = 0; i < methods.length; i++) {
                    String name = methods[i].getName();
                    if (name.equals("fire")) {
                        _jniFireMethod = methods[i];
                    } else if (name.equals("initialize")) {
                        _jniInitializeMethod = methods[i];
                    } else if (name.equals("wrapup")) {
                        _jniWrapupMethod = methods[i];
                    }
                }
                if (_jniFireMethod == null) {
                    throw new IllegalActionException(this,
                            "Cannot find fire method in the jni wrapper class.");
                }
                if (_jniInitializeMethod == null) {
                    throw new IllegalActionException(this,
                            "Cannot find initialize method in the jni wrapper class.");
                }
                if (_jniWrapupMethod == null) {
                    throw new IllegalActionException(this,
                            "Cannot find wrapup method in the jni wrapper class.");
                }

                _generatedCodeVersion = _workspace.getVersion();

            }

            try {
		// Java 1.4, used by Kepler, requires the two arg invoke()
                // Cast to Object() to supress Java 1.5 warning
                _jniInitializeMethod.invoke(_jniWrapper,  (Object [])null);
            } catch (Exception e) {
                throw new IllegalActionException(this, e,
                        "Failed to invoke the initialize method on the wrapper class.");
            }

        }
    }

    /** If this actor is opaque, then invoke the wrapup() method of the local
     *  director. This method is read-synchronized on the workspace.
     *
     *  @exception IllegalActionException If there is no director,
     *   or if the director's wrapup() method throws it, or if this
     *   actor is not opaque.
     */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        boolean inCoSimulationMode = ((BooleanToken) coSimulation.getToken())
                .booleanValue();
        if (inCoSimulationMode) {
            try {
		// Java 1.4, used by Kepler, requires the two arg invoke()
                // Cast to Object() to supress Java 1.5 warning
                _jniWrapupMethod.invoke(_jniWrapper, (Object []) null);
            } catch (Exception e) {
                throw new IllegalActionException(this, e,
                        "Failed to invoke the wrapup method on the wrapper class.");
            }
        }
        _generatedCodeVersion = _workspace.getVersion();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add an attribute.  This method should not be used directly.
     *  Instead, call setContainer() on the attribute.
     *  @param p The attribute to be added.
     *  @exception NameDuplicationException If this object already
     *   has an attribute with the same name.
     *  @exception IllegalActionException If the attribute is not an
     *   an instance of the expect class (in derived classes).
     */
    /*
     protected void _addAttribute(Attribute p) throws NameDuplicationException,
     IllegalActionException {
     
     if (p instanceof Director && !(p instanceof SDFDirector)) {
     throw new IllegalActionException(this, 
     "The CodeGenerationCompositeActor can only have " +
     "SDFDirector or HDFDirector as its director for code " +
     "generation purpose.");
     }
     }
     */

    // protected native void _fire();  
    //protected native void _transferInputs(String portName, int channel, int[] intTokenValues);
    //protected native void _transferInputs(String portName, int channel, double[] doubleTokenValues); 
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    private void _compileJavaCode() throws IllegalActionException {
        if (_executeCommands == null) {
            _executeCommands = new StreamExec();
        }

        List commands = new LinkedList();

        String name = "Jni" + _sanitizedActorName;

        // Create the .class file.
        commands.add("javac " + name + ".java");

        // Create the .h file.
        commands.add("javah " + name);

        _executeCommands.setWorkingDirectory(codeDirectory.asFile());
        _executeCommands.setCommands(commands);
        _executeCommands.start();
    }

    private void _generateCCode() throws IllegalActionException {
        // Get all instances of this class contained in this actor
        List codeGenerators = attributeList(CodeGenerator.class);

        CodeGenerator codeGenerator = null;
        try {
            if (codeGenerators.size() == 0) {
                // Add a codeGenerator
                codeGenerator = new StaticSchedulingCodeGenerator(this,
                        "CodeGenerator_AutoAdded");
            } else {
                // Get the last CodeGenerator in the list, maybe
                // it was added last?
                codeGenerator = (CodeGenerator) codeGenerators
                        .get(codeGenerators.size() - 1);
            }

            codeGenerator.codeDirectory.setExpression(codeDirectory
                    .getExpression());

            // FIXME: This should not be necessary, but if we don't
            // do it, then getBaseDirectory() thinks we are in the current dir.
            codeGenerator.codeDirectory
                    .setBaseDirectory(codeGenerator.codeDirectory.asFile()
                            .toURI());

            codeGenerator.generatorPackage.setExpression(generatorPackage
                    .getExpression());

            codeGenerator.inline.setExpression(inline.getExpression());

            codeGenerator.overwriteFiles.setExpression(overwriteFiles
                    .getExpression());

        } catch (NameDuplicationException e) {
            throw new IllegalActionException(this, e, "Name duplication.");
        }

        try {
            codeGenerator.generateCode();
        } catch (KernelException e) {
            throw new IllegalActionException(this, e,
                    "Failed to generate code.");
        }
    }

    private void _generateJavaCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        //FIXME: the name must be unique.
        //try {
        code.append("public class Jni" + _sanitizedActorName + " {\n" + "\n"
                + "    public native Object[] fire(" + _getArguments() + ");\n"
                + "    public native void initialize();\n"
                + "    public native void wrapup();\n" + "    static {\n"

                + "        String library = \"" + _sanitizedActorName + "\";\n"
                + "        System.loadLibrary(library);\n"

                //    + "        String library = \""
                //    +                  codeDirectory.asFile().getCanonicalPath() 
                //    +                  File.separator + _sanitizedActorName + ".dll\";\n"               
                //   + "        String library = \"" + "C:/Documents and Settings/zgang/codegen/" 
                //                           + _sanitizedActorName + ".dll\";\n"
                //   + "        System.load(library);\n"
                + "    }\n" + "}\n");

        //} catch (IOException e) {
        //    throw new IllegalActionException(this, "Cannot generate library path.");
        //}

        String codeFileName = "Jni" + _sanitizedActorName + ".java";

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
                    throw new IllegalActionException(this,
                            "Please select another file name.");
                }
            }

            Writer writer = null;
            try {
                writer = FileUtilities.openForWriting(codeFileName,
                        codeDirectory.getBaseDirectory(), false);
                writer.write(code.toString());
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
        } catch (Throwable ex) {
            throw new IllegalActionException(this, ex, "Failed to write \""
                    + codeFileName + "\" in "
                    + codeDirectory.getBaseDirectory());
        }

        _compileJavaCode();
    }

    private String _getArguments() {

        StringBuffer arguments = new StringBuffer();
        Iterator inputPorts = inputPortList().iterator();
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
                arguments.append(type.toString() + "[][] "
                        + inputPort.getName());
            }
        }
        return arguments.toString();
    }

    private void _init() {
        // The base class identifies the class name as TypedCompositeActor
        // irrespective of the actual class name.  We override that here.
        setClassName("ptolemy.codegen.c.actor.TypedCompositeActorWithCoSimulation");

        try {
            coSimulation = new Parameter(this, "coSimulation");
            coSimulation.setTypeEquals(BaseType.BOOLEAN);
            coSimulation.setExpression("false");

            codeDirectory = new FileParameter(this, "codeDirectory");
            codeDirectory.setExpression("$HOME/codegen/");

            // FIXME: This should not be necessary, but if we don't
            // do it, then getBaseDirectory() thinks we are in the current dir.
            codeDirectory.setBaseDirectory(codeDirectory.asFile().toURI());
            new Parameter(codeDirectory, "allowFiles", BooleanToken.FALSE);
            new Parameter(codeDirectory, "allowDirectories", BooleanToken.TRUE);

            generatorPackage = new StringParameter(this, "generatorPackage");
            generatorPackage.setExpression("ptolemy.codegen.c");

            inline = new Parameter(this, "inline");
            inline.setTypeEquals(BaseType.BOOLEAN);
            inline.setExpression("true");

            overwriteFiles = new Parameter(this, "overwriteFiles");
            overwriteFiles.setTypeEquals(BaseType.BOOLEAN);
            overwriteFiles.setExpression("true");

        } catch (Exception ex) {
            throw new InternalErrorException(this, ex,
                    "Problem setting up coSimulation parameter");
        }
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

        } else {
            // FIXME: need to deal with other types
        }

        for (int i = 0; i < port.getWidth(); i++) {
            try {
                if (i < port.getWidthInside()) {

                    if (port.hasToken(i, rate)) {
                        Token[] tokens = port.get(i, rate);

                        if (_debugging) {
                            _debug(getName(), "transferring input from "
                                    + port.getName());
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
                        _debug(getName(), "Dropping single input from "
                                + port.getName());
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

    private void _transferOutputs(IOPort port, Object outputTokens)
            throws IllegalActionException {

        int rate = DFUtilities.getTokenProductionRate(port);

        Type type = ((TypedIOPort) port).getType();
        if (type == BaseType.INT) {

            int[][] tokens = (int[][]) outputTokens;
            for (int i = 0; i < port.getWidthInside(); i++) {
                for (int k = 0; k < rate; k++) {
                    Token token = new IntToken(tokens[i][k]);
                    port.send(i, token);
                }
            }

        } else if (type == BaseType.DOUBLE) {

            double[][] tokens = (double[][]) outputTokens;
            for (int i = 0; i < port.getWidthInside(); i++) {
                for (int k = 0; k < rate; k++) {
                    Token token = new DoubleToken(tokens[i][k]);
                    port.send(i, token);
                }
            }

        } else {
            // FIXME: need to deal with other types
        }
    }

    private ExecuteCommands _executeCommands;

    private Object _jniWrapper;

    private Method _jniFireMethod;

    private Method _jniInitializeMethod;

    private Method _jniWrapupMethod;

    private String _sanitizedActorName;

    private long _generatedCodeVersion = -1;
}
