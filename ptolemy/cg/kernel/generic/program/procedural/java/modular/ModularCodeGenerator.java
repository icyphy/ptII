/* Class for modular code generators.

   Copyright (c) 2009-2014 The Regents of the University of California.
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

package ptolemy.cg.kernel.generic.program.procedural.java.modular;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Manager;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.DFUtilities;
import ptolemy.cg.kernel.generic.CodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.procedural.java.JavaCodeGenerator;
import ptolemy.cg.lib.ModularCodeGenTypedCompositeActor;
import ptolemy.cg.lib.Profile;
import ptolemy.data.BooleanToken;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.StringBufferExec;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
////GenericCodeGenerator

/**
 * Class for modular code generator.
 *
 * @author Dai Bui, Bert Rodiers
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating red (rodiers)
 * @Pt.AcceptedRating red (daib)
 */

public class ModularCodeGenerator extends JavaCodeGenerator {

    /**
     * Create a new instance of the Modular java code generator.
     *
     * @param container
     *            The container.
     * @param name
     *            The name of the Java code generator.
     * @exception IllegalActionException
     *                If the super class throws the exception or error occurs
     *                when setting the file path.
     * @exception NameDuplicationException
     *                If the super class throws the exception or an error occurs
     *                when setting the file path.
     */
    public ModularCodeGenerator(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        generatorPackageList
        .setExpression("generic.program.procedural.java.modular");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Create the profile for the model (at this level).
     *
     * @exception IllegalActionException
     *                when the profile can't be generated.
     */
    public void createProfile() throws IllegalActionException {
        String modelName = CodeGeneratorAdapter.generateName(_model);
        String profileClassName = modelName + "_profile";

        StringBuffer profileCode = new StringBuffer();

        profileCode.append("import java.util.List;" + _eol);
        profileCode.append("import java.util.LinkedList;" + _eol);
        profileCode.append("import ptolemy.cg.lib.Profile;" + _eol);
        profileCode.append("import ptolemy.kernel.util.IllegalActionException;"
                + _eol);

        profileCode.append(_eol + "public class " + profileClassName
                + " extends Profile {" + _eol);
        profileCode.append(INDENT1 + "public " + profileClassName + "() { }"
                + _eol);

        profileCode.append(createActorGraph());

        profileCode.append(INDENT1 + "public List<Profile.Port> ports() {"
                + _eol);
        profileCode.append(INDENT2
                + "List<Profile.Port> ports = new LinkedList<Profile.Port>();"
                + _eol);
        ModularCodeGenTypedCompositeActor model = (ModularCodeGenTypedCompositeActor) _model;
        for (Object object : model.portList()) {

            IOPort port = (IOPort) object;
            if (port.getWidth() > 0) {
                Profile.Port profilePort = null;
                try {
                    if (port instanceof TypedIOPort) {
                        profilePort = model
                                .convertProfilePort((TypedIOPort) port);
                    } else {
                        throw new InternalErrorException(port, null, "Port "
                                + port + " is not a TypedIOPort?");
                    }
                } catch (Throwable throwable) {
                    throw new IllegalActionException(
                            port,
                            throwable,
                            "Failed to convert profile port \""
                                    + port.getName()
                                    + "\", perhaps the type of the port needs to "
                                    + "be set from the UI or the backward type inference disabled?");
                }
                profileCode.append(INDENT2
                        + "ports.add(new Profile.Port(\""
                        + profilePort.name()
                        + "\", "
                        + profilePort.publisher()
                        + ", "
                        + profilePort.subscriber()
                        + ", "
                        + profilePort.width()
                        + ", "
                        + (port.isInput() ? DFUtilities
                                .getTokenConsumptionRate(port) : DFUtilities
                                .getTokenProductionRate(port)) + ", "
                                + ptTypeToCodegenType(((TypedIOPort) port).getType())
                                + ", " + port.isInput() + ", " + port.isOutput() + ", "
                                + profilePort.multiport() + ", \""
                                + profilePort.getPubSubChannelName() + "\"));" + _eol);
            }
        }
        profileCode.append(INDENT2 + "return ports;" + _eol);
        profileCode.append(INDENT1 + "}" + _eol);

        profileCode.append("}" + _eol);

        _writeCodeFileName(profileCode, profileClassName + ".java", true, true);

        List<String> commands = new LinkedList<String>();
        String topDirectory = ".";
        if (((BooleanToken) generateInSubdirectory.getToken()).booleanValue()) {
            topDirectory = "..";
        }

        // Be sure to include java.class.path so that if we are running under
        // a code coverage tool such as Cobertura, we get the Cobertura jars.
        // To test this, run:
        // ant -f build.default.xml test.cobertura -Dtest.batch=ptolemy/cg/lib/test/junit/*.java
        commands.add("pwd");
        commands.add("ls -l");
        commands.add("javac -classpath \"" + topDirectory
                + StringUtilities.getProperty("path.separator")
                + StringUtilities.getProperty("java.class.path")
                + StringUtilities.getProperty("path.separator")
                + StringUtilities.getProperty("ptolemy.ptII.dir") + "\""

                + profileClassName + ".java");

        StringBufferExec executeCommands = new StringBufferExec(true);
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

    /** Return the firings() method.
     *  @return The code for the firings method.
     *  @exception IllegalActionException If thrown while getting
     *  the token consumption rates for an error message
     */
    public StringBuffer createActorGraph() throws IllegalActionException {
        StringBuffer actorGraph = new StringBuffer();

        actorGraph
        .append(INDENT1
                + "public List<Profile.FiringFunction> firings() throws IllegalActionException {"
                + _eol);
        actorGraph
        .append(INDENT2
                + "List<Profile.FiringFunction> firingFunctions = new LinkedList<Profile.FiringFunction>();"
                + _eol);
        actorGraph.append(INDENT2 + "FiringFunction firingFunction;" + _eol
                + _eol);
        int index = 0;
        ModularCodeGenTypedCompositeActor model = (ModularCodeGenTypedCompositeActor) _model;
        for (Object object : model.entityList()) {

            Actor actor = (Actor) object;

            StringBuffer firingFunction = new StringBuffer();
            firingFunction.append(INDENT2
                    + "firingFunction = new Profile.FiringFunction(" + index++
                    + ");" + _eol);
            String externalPortName;

            // Add ports name and rate
            CompositeActor container = (CompositeActor) getContainer();
            boolean appendFiringFunction = false;
            Iterator inputPorts = actor.inputPortList().iterator();
            while (inputPorts.hasNext()) {
                IOPort inputPort = (IOPort) inputPorts.next();
                externalPortName = "";
                for (Object connectedPort : inputPort.connectedPortList()) {
                    if (container.portList().contains(connectedPort)) {
                        externalPortName = ((IOPort) connectedPort).getName();
                        break;
                    }
                }

                if (!externalPortName.equals("")) {
                    appendFiringFunction = true;
                    firingFunction
                    .append(INDENT2
                            + "firingFunction.ports.add(new FiringFunctionPort(\""
                            + inputPort.getName()
                            + "\",\""
                            + externalPortName
                            + "\","
                            + DFUtilities
                            .getTokenConsumptionRate(inputPort)
                            + "," + inputPort.isInput() + "));" + _eol);
                }
            }

            Iterator outputPorts = actor.outputPortList().iterator();
            while (outputPorts.hasNext()) {
                IOPort outputPort = (IOPort) outputPorts.next();
                externalPortName = "";
                for (Object connectedPort : outputPort.connectedPortList()) {
                    if (container.portList().contains(connectedPort)) {
                        externalPortName = ((IOPort) connectedPort).getName();
                        break;
                    }
                }

                if (!externalPortName.equals("")) {
                    appendFiringFunction = true;
                    firingFunction
                    .append(INDENT2
                            + "firingFunction.ports.add(new FiringFunctionPort(\""
                            + outputPort.getName()
                            + "\",\""
                            + externalPortName
                            + "\","
                            + DFUtilities
                            .getTokenProductionRate(outputPort)
                            + "," + outputPort.isInput() + "));" + _eol);
                }
            }
            firingFunction.append(INDENT2
                    + "firingFunctions.add(firingFunction);" + _eol + _eol);
            if (appendFiringFunction) {
                actorGraph.append(firingFunction);
            }

        }

        actorGraph.append(INDENT2 + "return firingFunctions;" + _eol);
        actorGraph.append(INDENT1 + "}" + _eol);

        return actorGraph;
    }

    /**
     * Generate code. This is the main entry point.
     *
     * @param code
     *            The code buffer into which to generate the code.
     * @return The return value of the last subprocess that was executed. or -1
     *         if no commands were executed.
     * @exception KernelException
     *                If a type conflict occurs or the model is running.
     */
    @Override
    public int generateCode(StringBuffer code) throws KernelException {

        int returnValue = -1;

        // If the container is in the top level, we are generating code
        // for the whole model. We have to make sure there is a manager,
        // and then preinitialize and resolve types.
        if (_isTopLevel()) {

            // If necessary, create a manager.
            Actor container = (Actor) getContainer();
            Manager manager = container.getManager();

            if (manager == null) {
                CompositeActor toplevel = (CompositeActor) ((NamedObj) container)
                        .toplevel();
                manager = new Manager(toplevel.workspace(), "Manager");
                toplevel.setManager(manager);
            }

            // set director for transparent composite actors?
            try {
                // TODO: we should bypass preinitializeAndResolveTypes
                // Otherwise we give up the lazyness of the model
                manager.preinitializeAndResolveTypes();
                returnValue = _generateCode(code);
            } finally {
                // We call wrapup here so that the state gets set to idle.
                // This makes it difficult to test the Exit actor.
                try {
                    long startTime = new Date().getTime();
                    manager.wrapup();
                    _printTimeAndMemory(startTime, "CodeGenerator: "
                            + "wrapup consumed: ");
                } catch (RuntimeException ex) {
                    // The Exit actor causes Manager.wrapup() to throw this.
                    if (!manager.isExitingAfterWrapup()) {
                        throw ex;
                    }
                }
            }
            // If the container is not in the top level, we are generating code
            // for the Java and C co-simulation.
        } else {
            returnValue = _generateCode(code);
        }
        return returnValue;
    }

    /**
     * Generate the main entry point.
     *
     * @return Return the definition of the main entry point for a program. In
     *         C, this would be defining main().
     * @exception IllegalActionException
     *                Not thrown in this base class.
     */
    @Override
    public String generateMainEntryCode() throws IllegalActionException {

        StringBuffer mainEntryCode = new StringBuffer();

        // If the container is in the top level, we are generating code
        // for the whole model.
        if (_isTopLevel()) {
            mainEntryCode
            .append(_eol
                    + _eol
                    + "public static void main(String [] args) throws Exception {"
                    + _eol + _sanitizedModelName + " model = new "
                    + _sanitizedModelName + "();" + _eol
                    + "model.run();" + _eol + "}" + _eol
                    + "public void run() throws Exception {" + _eol);
        } else {
            boolean addComma = false;

            if (getContainer() instanceof ModularCodeGenTypedCompositeActor
                    && ((Actor) getContainer()).outputPortList().size() > 0) {
                mainEntryCode.append(_eol + _eol
                        + "public Object[] fire (boolean export " + _eol);
                addComma = true;
            } else {
                mainEntryCode.append(_eol + _eol + "public Object[] fire ("
                        + _eol);
            }

            Iterator<?> inputPorts = ((Actor) getContainer()).inputPortList()
                    .iterator();
            while (inputPorts.hasNext()) {
                TypedIOPort inputPort = (TypedIOPort) inputPorts.next();
                if (inputPort.getWidth() > 0) {

                    String type = codeGenType2(inputPort.getType());
                    if (!type.equals("Token")
                            && !isPrimitive(codeGenType(inputPort.getType()))) {
                        type = "Token";
                    }
                    for (int i = 0; i < inputPort.getWidth(); i++) {
                        if (addComma) {
                            mainEntryCode.append(", ");
                        }
                        if (DFUtilities.getTokenConsumptionRate(inputPort) > 1) {
                            mainEntryCode.append(type + "[] "
                                    + inputPort.getName() + "_" + i);
                        } else {
                            mainEntryCode.append(type + " "
                                    + inputPort.getName() + "_" + i);
                        }
                        addComma = true;
                    }
                }
            }
            /*
                        Iterator<?> outputPorts = ((Actor) getContainer()).outputPortList()
                        .iterator();
                        while (outputPorts.hasNext()) {
                            TypedIOPort outputPort = (TypedIOPort) outputPorts.next();
                            if (addComma) {
                                mainEntryCode.append(", ");
                            }

                            String type = codeGenType(outputPort.getType());

                            if (!type.equals("Token") && !isPrimitive(type)) {
                                type = "Token";
                            }

                            for (int i = 0; i < outputPort.getWidth(); i++) {
                                if (DFUtilities.getTokenConsumptionRate(outputPort) > 1) {
                                    mainEntryCode.append(type + "[] " + outputPort.getName() + "_" + i);
                                } else {
                                    mainEntryCode.append(type + " " + outputPort.getName() + "_" + i);
                                }
                            }
                            addComma = true;
                        }
             */

            mainEntryCode.append(") {" + _eol);

        }

        return _processCode(mainEntryCode.toString());
    }

    /**
     * Generate the main exit point.
     *
     * @return Return a string that declares the end of the main() function.
     * @exception IllegalActionException
     *                Not thrown in this base class.
     */
    @Override
    public String generateMainExitCode() throws IllegalActionException {

        if (_isTopLevel()) {
            return INDENT1 + "System.exit(0);" + _eol + "}" + _eol + "}" + _eol;
        } else {
            if (_model instanceof CompositeActor
                    && ((CompositeActor) _model).outputPortList().isEmpty()) {
                return INDENT1 + "return null;" + _eol + "}" + _eol + "}"
                        + _eol;
            } else if (_model instanceof ModularCodeGenTypedCompositeActor) {
                return INDENT1 + INDENT1 + "return tokensToAllOutputPorts;"
                        + _eol + INDENT1 + "} else  {" + _eol + "return null;"
                        + _eol + "}" + _eol + "}" + _eol + "}" + _eol;
            } else {
                return INDENT1 + "return tokensToAllOutputPorts;" + _eol + "}"
                        + _eol + "}" + _eol;
            }
        }
    }
}
