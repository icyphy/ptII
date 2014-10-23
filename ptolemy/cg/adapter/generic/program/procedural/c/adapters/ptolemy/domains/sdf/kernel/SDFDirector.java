/* Code generator adapter class associated with the SDFDirector class.

 Copyright (c) 2005-2014 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.sdf.kernel;

import java.util.Iterator;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.SuperdenseTimeDirector;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.util.DFUtilities;
import ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.TypedCompositeActor;
import ptolemy.cg.kernel.generic.CodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.GenericCodeGenerator;
import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.ProgramCodeGenerator;
import ptolemy.cg.kernel.generic.program.ProgramCodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.procedural.c.CCodeGenerator;
import ptolemy.cg.kernel.generic.program.procedural.c.CCodegenUtilities;
import ptolemy.cg.lib.CompiledCompositeActor;
import ptolemy.cg.lib.PointerToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
////SDFDirector

/**
 Code generator adapter associated with the SDFDirector class. This class
 is also associated with a code generator.

 @author Ye Zhou, Gang Zhou
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Yellow (zgang)
 @Pt.AcceptedRating Red (eal)
 */
public class SDFDirector
extends
ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.domains.sdf.kernel.SDFDirector {

    /** Construct the code generator adapter associated with the given
     *  SDFDirector.
     *  @param sdfDirector The associated
     *  ptolemy.domains.sdf.kernel.SDFDirector
     */
    public SDFDirector(ptolemy.domains.sdf.kernel.SDFDirector sdfDirector) {
        super(sdfDirector);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generate the constructor code for the specified director.
     * In this class we initialize the director with its internal
     * parameters and fields as well as with the depths of the actors
     *
     * @return The generated constructor code
     * @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public String generateConstructorCode() throws IllegalActionException {
        StringBuffer result = new StringBuffer();
        CompositeActor container = (CompositeActor) _director.getContainer();
        String sanitizedContainerName = CodeGeneratorAdapter
                .generateName(container);
        ptolemy.domains.sdf.kernel.SDFDirector director = (ptolemy.domains.sdf.kernel.SDFDirector) _director;

        result.append(_eol + getSanitizedDirectorName() + "->container = "
                + sanitizedContainerName + ";");
        result.append(_eol + _sanitizedDirectorName + "->_startTime = "
                + director.getModelStartTime() + ";");
        result.append(_eol + _sanitizedDirectorName + "->_stopTime = "
                + director.getModelStopTime() + ";");
        result.append(_eol + _sanitizedDirectorName + "->iterations = "
                + ((IntToken) director.iterations.getToken()).intValue() + ";");
        result.append(_eol + _sanitizedDirectorName + "->period = "
                + ((DoubleToken) director.period.getToken()).doubleValue()
                + ";");
        result.append(_eol + _sanitizedDirectorName
                + "->localClock->container = (struct Director*)"
                + _sanitizedDirectorName + ";");
        result.append(_eol + _sanitizedDirectorName + "->schedule = "
                + sanitizedContainerName + "_Schedule_iterate;");

        List<?> containedActors = container.deepEntityList();
        Iterator<?> actors = containedActors.iterator();
        // First loop to create the struct IOPort
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            String sanitizedActorName = CodeGeneratorAdapter
                    .generateName((NamedObj) actor);
            Iterator<?> ports = actor.inputPortList().iterator();
            while (ports.hasNext()) {
                IOPort port = (IOPort) ports.next();
                if (!port.isOutsideConnected()) {
                    continue;
                }
                result.append(_eol + "struct IOPort* " + sanitizedActorName
                        + "_" + port.getName() + " = (struct IOPort*)"
                        + sanitizedActorName + "_get_" + port.getName() + "();");
            }
            ports = actor.outputPortList().iterator();
            while (ports.hasNext()) {
                IOPort port = (IOPort) ports.next();
                if (!port.isOutsideConnected()) {
                    continue;
                }
                result.append(_eol + "struct IOPort* " + sanitizedActorName
                        + "_" + port.getName() + " = (struct IOPort*)"
                        + sanitizedActorName + "_get_" + port.getName() + "();");
            }
        }
        // Second loop to link the ports and put the depths
        actors = containedActors.iterator();
        while (actors.hasNext()) {
            NamedObj actor = (NamedObj) actors.next();
            String sanitizedActorName = CodeGeneratorAdapter
                    .generateName(actor);
            Iterator<?> ports = ((Actor) actor).inputPortList().iterator();
            ports = ((Actor) actor).outputPortList().iterator();
            while (ports.hasNext()) {
                IOPort port = (IOPort) ports.next();
                if (!port.isOutsideConnected()) {
                    continue;
                }
                int i = 0;
                int j = 0;
                Receiver[][] receiverss = port.getRemoteReceivers();
                for (i = 0; i < receiverss.length; i++) {
                    if (receiverss[i] == null) {
                        continue;
                    }
                    for (j = 0; j < receiverss[i].length; j++) {
                        Receiver receiver = receiverss[i][j];
                        IOPort farPort = receiver.getContainer();
                        NamedObj farActor = farPort.getContainer();
                        String sanitizedFarActorName = CodeGeneratorAdapter
                                .generateName(farActor);
                        String farPortName;
                        if (farActor == container) {
                            farPortName = farPort.getName()
                                    + "->_localInsideReceivers, ";
                        } else {
                            farPortName = sanitizedFarActorName + "_"
                                    + farPort.getName() + "->_localReceivers, ";
                        }

                        int foo = 0;
                        int bar = 0;
                        Receiver[][] farReceiverss;
                        if (farPort.isOutput() && farPort.isOpaque()) {
                            farReceiverss = farPort.getInsideReceivers();
                        } else {
                            farReceiverss = farPort.getReceivers();
                        }
                        loops: for (foo = 0; foo < farReceiverss.length; foo++) {
                            for (bar = 0; bar < farReceiverss[foo].length; bar++) {
                                if (farReceiverss[foo][bar].equals(receiver)) {
                                    break loops;
                                }
                            }
                        }

                        if (foo == farReceiverss.length) {
                            throw new IllegalActionException(container,
                                    "Receiver not found in port : "
                                            + port.getFullName()
                                            + "in actor : "
                                            + sanitizedActorName);
                        }

                        result.append(_eol + "pblListAdd(pblListGet("
                                + sanitizedActorName + "_" + port.getName()
                                + "->_farReceivers, " + i + ")"
                                + ", pblListGet(pblListGet(" + farPortName
                                + foo + "), " + bar + "));");
                    }
                }
            }
        }
        // In the case of a CompositeActor, we have to initialize the insideReceivers
        Iterator<?> ports = ((Actor) container).inputPortList().iterator();
        while (ports.hasNext()) {
            IOPort port = (IOPort) ports.next();
            if (!port.isInsideConnected()) {
                continue;
            }
            int i = 0;
            int j = 0;
            Receiver[][] receiverss = port.deepGetReceivers();
            for (i = 0; i < receiverss.length; i++) {
                if (receiverss[i] == null) {
                    continue;
                }
                for (j = 0; j < receiverss[i].length; j++) {
                    Receiver receiver = receiverss[i][j];
                    IOPort farPort = receiver.getContainer();
                    NamedObj farActor = farPort.getContainer();
                    String sanitizedFarActorName = CodeGeneratorAdapter
                            .generateName(farActor);
                    String farPortName = sanitizedFarActorName + "_"
                            + farPort.getName() + "->_localReceivers, ";

                    int foo = 0;
                    int bar = 0;
                    Receiver[][] farReceiverss;
                    farReceiverss = farPort.getReceivers();
                    loops: for (foo = 0; foo < farReceiverss.length; foo++) {
                        for (bar = 0; bar < farReceiverss[foo].length; bar++) {
                            if (farReceiverss[foo][bar].equals(receiver)) {
                                break loops;
                            }
                        }
                    }

                    if (foo == farReceiverss.length) {
                        throw new IllegalActionException(container,
                                "Receiver not found in port : "
                                        + port.getFullName() + " in actor : "
                                        + sanitizedContainerName);
                    }

                    result.append(_eol + "pblListAdd(pblListGet("
                            + port.getName() + "->_insideReceivers, " + i + ")"
                            + ", pblListGet(pblListGet(" + farPortName + foo
                            + "), " + bar + "));");
                }
            }
        }

        return result.toString();
    }

    /** Generate The functions' declaration code for this director.
     *
     *  @return The functions' declaration function code.
     *  @exception IllegalActionException If thrown while generating code.
     */
    public String generateFunctionsDeclaration() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        _sanitizedDirectorName = CodeGeneratorAdapter.generateName(_director);

        code.append(_eol + "void " + _sanitizedDirectorName
                + "_Preinitialize();");
        code.append(_eol + "void " + _sanitizedDirectorName + "_Initialize();");
        code.append(_eol + "boolean " + _sanitizedDirectorName + "_Prefire();");
        code.append(_eol + "void " + _sanitizedDirectorName + "_Fire();");
        code.append(_eol + "boolean " + _sanitizedDirectorName + "_Postfire();");
        code.append(_eol + "void " + _sanitizedDirectorName + "_Wrapup();");

        code.append(_eol + "void " + _sanitizedDirectorName
                + "_TransferInputs();");
        code.append(_eol + "void " + _sanitizedDirectorName
                + "_TransferOutputs();");

        return code.toString();
    }

    /** Generate the initialize function code for the associated SDF director.
     *  @return The generated initialize code.
     *  @exception IllegalActionException If the adapter associated with
     *   an actor throws it while generating initialize code for the actor.
     */
    public String generateInitializeFunctionCode()
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        CompositeActor container = ((CompositeActor) _director.getContainer());
        List actorList = container.deepEntityList();
        String sanitizedContainerName = CodeGeneratorAdapter
                .generateName(container);

        ProgramCodeGenerator codeGenerator = getCodeGenerator();

        code.append(_eol + _eol
                + codeGenerator.comment("Initialization of the director"));

        if (_director.isEmbedded()) {
            ptolemy.actor.Director executiveDirector = container
                    .getExecutiveDirector();
            // Some composites, such as RunCompositeActor want to be treated
            // as if they are at the top level even though they have an executive
            // director, so be sure to check _isTopLevel().
            if (executiveDirector instanceof SuperdenseTimeDirector) {
                code.append(_eol
                        + _sanitizedDirectorName
                        + ".currentMicrostep = "
                        + ((SuperdenseTimeDirector) executiveDirector)
                        .getIndex() + ";");
            }
        }

        Iterator<?> actors = actorList.iterator();
        while (actors.hasNext()) {
            NamedObj actor = (NamedObj) actors.next();
            String sanitizedActorName = CodeGeneratorAdapter
                    .generateName(actor);
            code.append(_eol + sanitizedActorName + "_initialize();");
        }

        code.append(_eol + _sanitizedDirectorName + ".containerActor = &"
                + sanitizedContainerName + ";");

        Attribute iterations = _director.getAttribute("iterations");
        int iterationCount = ((IntToken) ((Variable) iterations).getToken())
                .intValue();
        code.append(_eol + _sanitizedDirectorName + ".iterations = "
                + iterationCount + ";");
        code.append(_eol + _sanitizedDirectorName + ".iterationsCount = 0;");

        code.append(_eol + _sanitizedDirectorName + ".currentModelTime = "
                + _sanitizedDirectorName + ".startTime;");
        code.append(_eol + _sanitizedDirectorName + ".exceedStopTime = false;");

        code.append(_eol + _sanitizedDirectorName + ".isInitializing = false;");
        code.append(_eol
                + codeGenerator
                .comment("End of the Initialization of the director"));

        return code.toString();
    }

    /** Generate a main loop for an execution under the control of
     *  this director. If the associated director has a parameter
     *  named <i>iterations</i> with a value greater than zero,
     *  then wrap code generated by generateFireCode() in a
     *  loop that executes the specified number of iterations.
     *  Otherwise, wrap it in a loop that executes forever.
     *  In the loop, first get the code returned by generateFireCode(),
     *  and follow that with the code produced by the container
     *  help for generateModeTransitionCode(). That code will
     *  make state transitions in modal models at the conclusion
     *  of each iteration. Next, this code calls postfire(), and
     *  that returns false, breaks out of the main loop.
     *  Finally, if the director has a parameter named <i>period</i>,
     *  then increment the variable _currentTime after each
     *  pass through the loop.
     *  @return Code for the main loop of an execution.
     *  @exception IllegalActionException If something goes wrong.
     */
    @Override
    public String generateMainLoop() throws IllegalActionException {
        // Need a leading _eol here or else the execute decl. gets stripped out.
        StringBuffer code = new StringBuffer();
        //                + getCodeGenerator().getMethodVisibilityString()
        //                + " void execute() "
        //                + getCodeGenerator().getMethodExceptionString() + " {" + _eol);
        //
        //        Attribute iterations = _director.getAttribute("iterations");
        //        if (iterations == null) {
        //            code.append(_eol + "while (true) {" + _eol);
        //        } else {
        //            int iterationCount = ((IntToken) ((Variable) iterations).getToken())
        //                    .intValue();
        //            if (iterationCount <= 0) {
        //                code.append(_eol + "while (true) {" + _eol);
        //            } else {
        //                // Declare iteration outside of the loop to avoid
        //                // mode" with gcc-3.3.3
        //                code.append(_eol + "int iteration;" + _eol);
        //                code.append("for (iteration = 0; iteration < " + iterationCount
        //                        + "; iteration ++) {" + _eol);
        //            }
        //        }

        code.append(_eol + "void " + _sanitizedDirectorName
                + "_Preinitialize() {" + _eol);
        code.append(generatePreinitializeMethodBodyCode());
        code.append(_eol + "}" + _eol);

        code.append(_eol + "boolean " + _sanitizedDirectorName + "_Prefire() {"
                + _eol);
        code.append(generatePreFireFunctionCode());
        code.append(_eol + "}" + _eol);

        code.append("boolean " + _sanitizedDirectorName + "_Postfire() {"
                + _eol);
        code.append(generatePostFireFunctionCode());
        code.append(_eol + "}" + _eol);

        code.append("void " + _sanitizedDirectorName + "_Fire() {" + _eol);
        String[] splitFireCode = getCodeGenerator()._splitBody(
                "_" + CodeGeneratorAdapter.generateName(getComponent())
                + "_run_", generateFireCode());
        code.append(splitFireCode[1]);
        // The code generated in generateModeTransitionCode() is executed
        // after one global iteration, e.g., in HDF model.
        NamedProgramCodeGeneratorAdapter modelAdapter = (NamedProgramCodeGeneratorAdapter) getCodeGenerator()
                .getAdapter(_director.getContainer());
        modelAdapter.generateModeTransitionCode(code);

        /*if (callPostfire) {
            code.append(_INDENT2 + "if (!postfire()) {" + _eol + _INDENT3
                    + "break;" + _eol + _INDENT2 + "}" + _eol);
        }
         */
        //_generateUpdatePortOffsetCode(code, (Actor) _director.getContainer());
        code.append("return;");
        code.append(_eol + "}" + _eol);

        code.append(_eol + "void " + _sanitizedDirectorName + "_Initialize() {"
                + _eol);
        code.append(generateInitializeFunctionCode());
        code.append(_eol + "}" + _eol);

        code.append(_eol + "void " + _sanitizedDirectorName + "_Wrapup() {"
                + _eol);
        code.append(generateWrapupCode());
        code.append(_eol + "}" + _eol);

        return processCode(code.toString());
    }

    /** Generate The postfire function code for a SDF director.
     *  @return The postfire function code.
     *  @exception IllegalActionException If thrown while generating fire code.
     */
    public String generatePostFireFunctionCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        code.append(_eol + _sanitizedDirectorName + ".iterationsCount++;");

        code.append(_eol + "if (" + _sanitizedDirectorName
                + ".iterationsCount >= " + _sanitizedDirectorName
                + ".iterations) {");
        code.append(_eol + _sanitizedDirectorName + ".iterationsCount = 0;");
        code.append(_eol + "return false;");
        code.append(_eol + "}");

        Attribute period = _director.getAttribute("period");
        if (period != null) {
            Double periodValue = ((DoubleToken) ((Variable) period).getToken())
                    .doubleValue();
            if (periodValue != 0.0) {
                code.append(_sanitizedDirectorName + ".currentModelTime += "
                        + periodValue + ";" + _eol);
            }
        }

        code.append(_eol + "return true;");

        return code.toString();
    }

    /** Generate The prefire function code for a SDF director
     *  Usually we have to check if all the input ports have enough
     *  tokens to fire.
     *  In here we also update the time of the director with its container
     *  time.
     *  If it is top level director, there is no need for that.
     *  But in the SDFReceiver we assume that receivers have always
     *  enough tokens, therefore the prefire methods always returns true.
     *  @return The prefire function code.
     *  @exception IllegalActionException If thrown while generating fire code.
     */
    public String generatePreFireFunctionCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        if (_director.isEmbedded()) {
            code.append(_eol
                    + _sanitizedDirectorName
                    + ".currentModelTime = "
                    + _sanitizedDirectorName
                    + ".containerActor->actor.container->director->currentModelTime;");
        }

        code.append(_eol + "return true;");

        return code.toString();
    }

    /** Generate the preinitialize code for this director.
     *  The preinitialize code for the director is generated by appending
     *  the preinitialize code for each actor.
     *  @return The generated preinitialize code.
     *  @exception IllegalActionException If getting the adapter fails,
     *   or if generating the preinitialize code for a adapter fails,
     *   or if there is a problem getting the buffer size of a port.
     */
    @Override
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        //code.append(super.generatePreinitializeCode());
        // We do execute this method without using its result because we need to initialize the offsets
        super.generatePreinitializeCode();

        CompositeActor container = ((CompositeActor) _director.getContainer());
        String sanitizedContainerName = CodeGeneratorAdapter
                .generateName(container);

        getSanitizedDirectorName();

        _updatePortBufferSize();
        _portNumber = 0;
        _intFlag = false;
        _doubleFlag = false;
        _booleanFlag = false;

        code.append(_eol + "" + _sanitizedDirectorName
                + ".preinitializeFunction = " + _sanitizedDirectorName
                + "_Preinitialize;");
        code.append(_eol + "" + _sanitizedDirectorName
                + ".initializeFunction = " + _sanitizedDirectorName
                + "_Initialize;");
        code.append(_eol + "" + _sanitizedDirectorName + ".prefireFunction = "
                + _sanitizedDirectorName + "_Prefire;");
        code.append(_eol + "" + _sanitizedDirectorName + ".postfireFunction = "
                + _sanitizedDirectorName + "_Postfire;");
        code.append(_eol + "" + _sanitizedDirectorName + ".fireFunction = "
                + _sanitizedDirectorName + "_Fire;");
        code.append(_eol + "" + _sanitizedDirectorName + ".wrapupFunction = "
                + _sanitizedDirectorName + "_Wrapup;");
        //        code.append(_eol + "" + _sanitizedDirectorName + ".transferInputs = " + _sanitizedDirectorName + "_TransferInputs;");
        //        code.append(_eol + "" + _sanitizedDirectorName + ".transferOutputs = " + _sanitizedDirectorName + "_TransferOutputs;");
        code.append(_eol + "" + _sanitizedDirectorName + ".containerActor = &"
                + sanitizedContainerName + ";");

        return code.toString();
    }

    /** Generate the preinitialize code for this director.
     *  The preinitialize code for the director is generated by appending
     *  the preinitialize code for each actor.
     *  @return The generated preinitialize code.
     *  @exception IllegalActionException If getting the adapter fails,
     *   or if generating the preinitialize code for a adapter fails,
     *   or if there is a problem getting the buffer size of a port.
     */
    @Override
    public String generatePreinitializeMethodBodyCode()
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        CompositeActor container = ((CompositeActor) _director.getContainer());
        List actorList = container.deepEntityList();

        Attribute period = _director.getAttribute("period");
        if (period != null && _director.getContainer().getContainer() == null) {
            double periodValue = ((DoubleToken) ((Variable) period).getToken())
                    .doubleValue();
            code.append(_eol + _sanitizedDirectorName + "_period = "
                    + periodValue + ";");
        }

        Iterator<?> actors = actorList.iterator();
        while (actors.hasNext()) {
            NamedObj actor = (NamedObj) actors.next();
            String sanitizedActorName = CodeGeneratorAdapter
                    .generateName(actor);
            code.append(_eol + sanitizedActorName + "_preinitialize();");
        }

        return code.toString();
    }

    /** Generate the code representing the schedule statically inferred from the
     *  Ptolemy model.
     *  @return the code representing the schedule.
     *  @exception IllegalActionException if something happens while writing the code
     */
    public String generateSchedule() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        // Generate code for one iteration.
        ptolemy.actor.sched.StaticSchedulingDirector director = (ptolemy.actor.sched.StaticSchedulingDirector) getComponent();
        Schedule schedule = director.getScheduler().getSchedule();

        Iterator<?> actorsToFire = schedule.firingIterator();
        while (actorsToFire.hasNext()) {
            Firing firing = (Firing) actorsToFire.next();
            Actor actor = firing.getActor();
            String sanitizedActorName = CodeGeneratorAdapter
                    .generateName((NamedObj) actor);
            int count = firing.getIterationCount();
            code.append(_eol + "(*(" + sanitizedActorName + "->iterate))("
                    + sanitizedActorName + ", " + count + ");");
        }

        return code.toString();
    }

    /** Generate code for transferring enough tokens to complete an internal
     *  iteration.
     *  @param inputPort The port to transfer tokens.
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException If thrown while transferring tokens.
     */
    @Override
    public void generateTransferInputsCode(IOPort inputPort, StringBuffer code)
            throws IllegalActionException {
        code.append(CodeStream.indent(getCodeGenerator().comment(
                "SDFDirector: " + "Transfer tokens to the inside.")));
        int rate = DFUtilities.getTokenConsumptionRate(inputPort);
        boolean targetCpp = ((BooleanToken) getCodeGenerator().generateCpp
                .getToken()).booleanValue();

        CompositeActor container = (CompositeActor) getComponent()
                .getContainer();
        TypedCompositeActor compositeActorAdapter = (TypedCompositeActor) getCodeGenerator()
                .getAdapter(container);

        if (container instanceof CompiledCompositeActor
                && ((BooleanToken) getCodeGenerator().generateEmbeddedCode
                        .getToken()).booleanValue()) {

            // FindBugs wants this instanceof check.
            if (!(inputPort instanceof TypedIOPort)) {
                throw new InternalErrorException(inputPort, null,
                        " is not an instance of TypedIOPort.");
            }
            Type type = ((TypedIOPort) inputPort).getType();
            String portName = inputPort.getName();

            String exceptionMessage = "Failed to generate code "
                    + "to transfer tokens to for input. "
                    + "The type of the \"" + portName + "\" output port was "
                    + type + ", which is not supported. "
                    + "Try setting the type of the \"" + portName
                    + "\" port by right clicking on the actor "
                    + "and selecting Customize -> Ports.";

            for (int i = 0; i < inputPort.getWidth(); i++) {
                if (i < inputPort.getWidthInside()) {

                    String tokensFromOneChannel = "tokensFromOneChannelOf"
                            + portName + i;
                    String pointerToTokensFromOneChannel = "pointerTo"
                            + tokensFromOneChannel;
                    code.append("jobject "
                            + tokensFromOneChannel
                            + " = "
                            + CCodegenUtilities.jniGetObjectArrayElement(
                                    portName, String.valueOf(i), targetCpp)
                                    + ";" + _eol);

                    if (type == BaseType.INT) {
                        code.append("jint * "
                                + pointerToTokensFromOneChannel
                                + " = "
                                + CCodegenUtilities.jniGetArrayElements("Int",
                                        tokensFromOneChannel, targetCpp) + ";"
                                        + _eol);
                    } else if (type == BaseType.DOUBLE) {
                        code.append("jdouble * "
                                + pointerToTokensFromOneChannel
                                + " = "
                                + CCodegenUtilities.jniGetArrayElements(
                                        "Double", tokensFromOneChannel,
                                        targetCpp) + ";" + _eol);
                    } else if (type == PointerToken.POINTER) {
                        code.append("jint * "
                                + pointerToTokensFromOneChannel
                                + " = "
                                + CCodegenUtilities.jniGetArrayElements("Int",
                                        tokensFromOneChannel, targetCpp) + ";"
                                        + _eol);
                    } else if (type == BaseType.BOOLEAN) {
                        code.append("jboolean * "
                                + pointerToTokensFromOneChannel
                                + " = "
                                + CCodegenUtilities.jniGetArrayElements(
                                        "Boolean", tokensFromOneChannel,
                                        targetCpp) + ";" + _eol);
                    } else {
                        // FIXME: need to deal with other types
                        throw new IllegalActionException(inputPort,
                                exceptionMessage);

                    }
                    //if (inputPort.isMultiport()) {
                    //}
                    for (int k = 0; k < rate; k++) {
                        //code.append(_eol + "$put(" + portNameWithChannelNumber + ", ");
                        String putString = _eol + "ReceiverPut(("
                                + CodeGeneratorAdapter.generateName(container)
                                + ".actor).ports[enum_"
                                + CodeGeneratorAdapter.generateName(container)
                                + "_" + inputPort.getName() + "].farReceivers["
                                + i + "], " + _eol;
                        code.append(_eol + putString);
                        //                        code.append(compositeActorAdapter.getReference("@"
                        //                                + portNameWithChannelNumber + "," + k, false));
                        if (type == PointerToken.POINTER) {
                            code.append("(void *) "
                                    + pointerToTokensFromOneChannel + "[" + k
                                    + "]");
                        } else if (type == BaseType.INT) {
                            code.append("$Int_new("
                                    + pointerToTokensFromOneChannel + "[" + k
                                    + "]");
                        } else if (type == BaseType.DOUBLE) {
                            code.append("$Double_new("
                                    + pointerToTokensFromOneChannel + "[" + k
                                    + "]");
                        } else if (type == BaseType.BOOLEAN) {
                            code.append("$Boolean_new("
                                    + pointerToTokensFromOneChannel + "[" + k
                                    + "]");
                        } else {
                            // FIXME: need to deal with other types
                            throw new IllegalActionException(inputPort,
                                    exceptionMessage);
                        }
                        code.append("));" + _eol);
                    }

                    if (type == BaseType.INT) {
                        code.append(CCodegenUtilities.jniReleaseArrayElements(
                                "Int", tokensFromOneChannel,
                                pointerToTokensFromOneChannel, targetCpp)
                                + ";" + _eol);
                    } else if (type == BaseType.DOUBLE) {
                        code.append(CCodegenUtilities.jniReleaseArrayElements(
                                "Double", tokensFromOneChannel,
                                pointerToTokensFromOneChannel, targetCpp)
                                + ";" + _eol);
                    } else if (type == PointerToken.POINTER) {
                        code.append(CCodegenUtilities.jniReleaseArrayElements(
                                "Int", tokensFromOneChannel,
                                pointerToTokensFromOneChannel, targetCpp)
                                + ";" + _eol);
                    } else if (type == BaseType.BOOLEAN) {
                        code.append(CCodegenUtilities.jniReleaseArrayElements(
                                "Boolean", tokensFromOneChannel,
                                pointerToTokensFromOneChannel, targetCpp)
                                + ";" + _eol);
                    } else {
                        // FIXME: need to deal with other types
                        throw new IllegalActionException(inputPort,
                                exceptionMessage);
                    }
                }
            }

        } else {
            for (int i = 0; i < inputPort.getWidth(); i++) {
                if (i < inputPort.getWidthInside()) {
                    String name = inputPort.getName();

                    if (inputPort.isMultiport()) {
                        name = name + '#' + i;
                    }

                    for (int k = 0; k < rate; k++) {
                        code.append(compositeActorAdapter.getReference("@"
                                + name + "," + k, false));
                        code.append(" = " + _eol);
                        code.append(compositeActorAdapter.getReference(name
                                + "," + k, false));
                        code.append(";" + _eol);
                    }
                }
            }
        }

        // Generate the type conversion code before fire code.
        code.append(compositeActorAdapter.generateTypeConvertFireCode(true));

        // The offset of the input port itself is updated by outside director.
        //_updateConnectedPortsOffset(inputPort, code, rate);
    }

    /** Generate code for transferring enough tokens to fulfill the output
     *  production rate.
     *  @param outputPort The port to transfer tokens.
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException If thrown while transferring tokens.
     */
    @Override
    public void generateTransferOutputsCode(IOPort outputPort, StringBuffer code)
            throws IllegalActionException {
        code.append(CodeStream.indent(getCodeGenerator().comment(
                "SDFDirector: " + "Transfer tokens to the outside.")));

        int rate = DFUtilities.getTokenProductionRate(outputPort);
        boolean targetCpp = ((BooleanToken) getCodeGenerator().generateCpp
                .getToken()).booleanValue();

        CompositeActor container = (CompositeActor) getComponent()
                .getContainer();
        TypedCompositeActor compositeActorAdapter = (TypedCompositeActor) getCodeGenerator()
                .getAdapter(container);

        if (container instanceof CompiledCompositeActor
                && ((BooleanToken) getCodeGenerator().generateEmbeddedCode
                        .getToken()).booleanValue()) {

            if (_portNumber == 0) {
                int numberOfOutputPorts = container.outputPortList().size();

                // Needed by $PTII/bin/vergil $PTII/ptolemy/cg/lib/demo/Scale/ScaleC.xml to run
                // EmbeddedCodeActors in simulation mode.
                code.append("jobjectArray tokensToAllOutputPorts;" + _eol);
                code.append("jclass "
                        + _objClass
                        + " = "
                        + CCodegenUtilities.jniFindClass("Ljava/lang/Object;",
                                targetCpp) + ";" + _eol);
                code.append("tokensToAllOutputPorts = "
                        + CCodegenUtilities.jniNewObjectArray(
                                String.valueOf(numberOfOutputPorts),
                                "objClass", targetCpp) + ";" + _eol);
            }

            String portName = outputPort.getName();
            String tokensToThisPort = "tokensTo" + portName;

            // FindBugs wants this instanceof check.
            if (!(outputPort instanceof TypedIOPort)) {
                throw new InternalErrorException(outputPort, null,
                        " is not an instance of TypedIOPort.");
            }

            Type type = ((TypedIOPort) outputPort).getType();

            String exceptionMessage = "Failed to generate code "
                    + "to transfer tokens to fulfill the output rate."
                    + "The type of the \"" + outputPort.getName()
                    + "\" output port was " + type
                    + ", which is not supported. "
                    + "Try setting the type of the \"" + outputPort.getName()
                    + "\" port by right clicking on the actor "
                    + "and selecting Customize -> Ports.";

            int numberOfChannels = outputPort.getWidthInside();
            code.append("jobjectArray " + tokensToThisPort + ";" + _eol);

            // Find jni classes and methods and initialize the jni array
            // for the given type.
            if (type == BaseType.INT) {
                if (!_intFlag) {
                    code.append("jclass " + _objClassI + " = "
                            + CCodegenUtilities.jniFindClass("[I", targetCpp)
                            + ";" + _eol);
                    _intFlag = true;
                }
                code.append(tokensToThisPort
                        + " = "
                        + CCodegenUtilities.jniNewObjectArray(
                                String.valueOf(numberOfChannels), _objClassI,
                                targetCpp) + ";" + _eol);
            } else if (type == BaseType.DOUBLE) {
                if (!_doubleFlag) {
                    code.append("jclass " + _objClassD + " = "
                            + CCodegenUtilities.jniFindClass("[D", targetCpp)
                            + ";" + _eol);
                    _doubleFlag = true;
                }
                code.append(tokensToThisPort
                        + " = "
                        + CCodegenUtilities.jniNewObjectArray(
                                String.valueOf(numberOfChannels), _objClassD,
                                targetCpp) + ";" + _eol);
            } else if (type == PointerToken.POINTER) {
                if (!_intFlag) {
                    code.append("jclass " + _objClassI + " = "
                            + CCodegenUtilities.jniFindClass("[I", targetCpp)
                            + ";" + _eol);
                    _intFlag = true;
                }
                code.append(tokensToThisPort
                        + " = "
                        + CCodegenUtilities.jniNewObjectArray(
                                String.valueOf(numberOfChannels), _objClassI,
                                targetCpp) + ";" + _eol);
            } else if (type == BaseType.BOOLEAN) {
                if (!_booleanFlag) {
                    code.append("jclass objClassZ = "
                            + CCodegenUtilities.jniFindClass("[Z", targetCpp)
                            + ";" + _eol);
                    _booleanFlag = true;
                }
                code.append(tokensToThisPort
                        + " = "
                        + CCodegenUtilities.jniNewObjectArray(
                                String.valueOf(numberOfChannels), "objClassZ",
                                targetCpp) + ";" + _eol);
            } else {
                // FIXME: need to deal with other types
                throw new IllegalActionException(outputPort, exceptionMessage);
            }

            //            System.out.println("cg SDFDirector: outputPort width: "
            //                    + outputPort.getWidthInside());
            // Create an array to contain jni objects
            for (int i = 0; i < outputPort.getWidthInside(); i++) {

                String tokensToOneChannel = "tokensToOneChannelOf" + portName;
                if (i == 0) {
                    if (type == BaseType.INT) {
                        code.append("jint " + tokensToOneChannel + "[" + rate
                                + "];" + _eol);

                    } else if (type == BaseType.DOUBLE) {
                        code.append("jdouble " + tokensToOneChannel + "["
                                + rate + "];" + _eol);

                    } else if (type == PointerToken.POINTER) {
                        code.append("jint " + tokensToOneChannel + "[" + rate
                                + "];" + _eol);

                    } else if (type == BaseType.BOOLEAN) {
                        code.append("jboolean " + tokensToOneChannel + "["
                                + rate + "];" + _eol);

                    } else {
                        // FIXME: need to deal with other types
                        throw new IllegalActionException(outputPort,
                                exceptionMessage);
                    }
                }

                if (outputPort.isMultiport()) {
                }

                // Assign each token to the array of jni objects
                for (int k = 0; k < rate; k++) {
                    //                    String portReference = compositeActorAdapter.getReference(
                    //                            "@" + portNameWithChannelNumber + "," + k, false);
                    String sanitizedContainerName = CodeGeneratorAdapter
                            .generateName(container);
                    String hasTokenString = "while (ReceiverHasToken(("
                            + sanitizedContainerName + ".actor).ports[enum_"
                            + sanitizedContainerName + "_"
                            + outputPort.getName() + "].receivers + " + i
                            + "))" + _eol;
                    code.append(hasTokenString);

                    String getString = "ReceiverGet((" + sanitizedContainerName
                            + ".actor).ports[enum_" + sanitizedContainerName
                            + "_" + outputPort.getName() + "].receivers + " + i
                            + ")";

                    if (type == BaseType.INT) {
                        getString += ".payload.Int;";
                    } else if (type == BaseType.DOUBLE) {
                        getString += ".payload.Double;";
                    } else if (type == BaseType.BOOLEAN) {
                        getString += ".payload.Boolean;";
                    } else {
                        // FIXME: need to deal with other types
                        throw new IllegalActionException(outputPort,
                                exceptionMessage);
                    }

                    code.append(_eol + tokensToOneChannel + "[" + k + "] = "
                            + getString + _eol);
                    //code.append("$get(" + portNameWithChannelNumber + ");" + _eol);

                }

                String tokensToOneChannelArray = "arr" + portName + i;
                // Create and fill an array of Java objects.
                if (type == BaseType.INT) {
                    code.append("jintArray "
                            + tokensToOneChannelArray
                            + " = "
                            + CCodegenUtilities.jniNewArray("Int",
                                    String.valueOf(rate), targetCpp) + ";"
                                    + _eol);
                    code.append(CCodegenUtilities.jniSetArrayRegion("Int",
                            tokensToOneChannelArray, "0", String.valueOf(rate),
                            tokensToOneChannel, targetCpp)
                            + ";" + _eol);

                } else if (type == BaseType.DOUBLE) {
                    code.append("jdoubleArray "
                            + tokensToOneChannelArray
                            + " = "
                            + CCodegenUtilities.jniNewArray("Double",
                                    String.valueOf(rate), targetCpp) + ";"
                                    + _eol);
                    code.append(CCodegenUtilities.jniSetArrayRegion("Double",
                            tokensToOneChannelArray, "0", String.valueOf(rate),
                            tokensToOneChannel, targetCpp)
                            + ";" + _eol);

                } else if (type == PointerToken.POINTER) {
                    code.append("jintArray "
                            + tokensToOneChannelArray
                            + " = "
                            + CCodegenUtilities.jniNewArray("Int",
                                    String.valueOf(rate), targetCpp) + ";"
                                    + _eol);
                    code.append(CCodegenUtilities.jniSetArrayRegion("Int",
                            tokensToOneChannelArray, "0", String.valueOf(rate),
                            tokensToOneChannel, targetCpp)
                            + ";" + _eol);

                } else if (type == BaseType.BOOLEAN) {
                    code.append("jbooleanArray "
                            + tokensToOneChannelArray
                            + " = "
                            + CCodegenUtilities.jniNewArray("Boolean",
                                    String.valueOf(rate), targetCpp) + ";"
                                    + _eol);
                    code.append(CCodegenUtilities.jniSetArrayRegion("Boolean",
                            tokensToOneChannelArray, "0", String.valueOf(rate),
                            tokensToOneChannel, targetCpp)
                            + ";" + _eol);
                } else {
                    // FIXME: need to deal with other types
                    throw new IllegalActionException(outputPort,
                            exceptionMessage);
                }

                code.append(CCodegenUtilities.jniSetObjectArrayElement(
                        tokensToThisPort, String.valueOf(i),
                        tokensToOneChannelArray, targetCpp)
                        + ";" + _eol);
                code.append(CCodegenUtilities.jniDeleteLocalRef(
                        tokensToOneChannelArray, targetCpp) + ";" + _eol);
            }

            code.append(CCodegenUtilities.jniSetObjectArrayElement(
                    "tokensToAllOutputPorts", String.valueOf(_portNumber),
                    tokensToThisPort, targetCpp)
                    + ";" + _eol);
            code.append(CCodegenUtilities.jniDeleteLocalRef(tokensToThisPort,
                    targetCpp) + ";" + _eol);
            _portNumber++;

        } else {
            for (int i = 0; i < outputPort.getWidthInside(); i++) {
                if (i < outputPort.getWidth()) {
                    String name = outputPort.getName();

                    if (outputPort.isMultiport()) {
                        name = name + '#' + i;
                    }

                    for (int k = 0; k < rate; k++) {
                        code.append(CodeStream.indent(compositeActorAdapter
                                .getReference(name + "," + k, false)));
                        code.append(" =" + _eol);
                        code.append(CodeStream.indent(compositeActorAdapter
                                .getReference("@" + name + "," + k, false)));
                        code.append(";" + _eol);
                    }
                }
            }
        }

        // The offset of the ports connected to the output port is
        // updated by outside director.
        //_updatePortOffset(outputPort, code, rate);
    }

    /** Generate constant for the <i>period</i> parameter,
     *  if there is one.
     *  @return code The generated code.
     *  @exception IllegalActionException If the adapter class for the model
     *   director cannot be found.
     */
    @Override
    public String generateVariableDeclaration() throws IllegalActionException {
        StringBuffer variableDeclarations = new StringBuffer();

        //variableDeclarations.append(super.generateVariableDeclaration());

        ptolemy.actor.sched.StaticSchedulingDirector director = (ptolemy.actor.sched.StaticSchedulingDirector) getComponent();

        Attribute period = _director.getAttribute("period");
        if (period != null) {
            // Print period only if it is the containing actor is the top level.
            // FIXME: should this test also be applied to the other code?
            if (director.getContainer().getContainer() == null) {
                variableDeclarations.append(_eol
                        + getCodeGenerator().comment(
                                "Provide the period attribute as constant."));
                variableDeclarations.append("double " + _sanitizedDirectorName
                        + "_period;" + _eol);
            }

        }
        CompositeActor container = ((CompositeActor) _director.getContainer());
        String sanitizedContainerName = CodeGeneratorAdapter
                .generateName(container);
        variableDeclarations.append("#include \"" + sanitizedContainerName
                + ".h\"" + _eol);
        variableDeclarations.append(_eol + "Director " + _sanitizedDirectorName
                + ";");
        //
        //        if (director.getContainer().getContainer() == null) {
        //            variableDeclarations.append(_eol
        //                    + getCodeGenerator()
        //                            .comment("Provide the iteration count."));
        //            variableDeclarations.append("int " + _sanitizedDirectorName + "_iteration = 0;" + _eol);
        //        }
        return variableDeclarations.toString();
    }

    /** Generate The wrapup function code.
     *  @return The wrapup function code.
     *  @exception IllegalActionException If thrown while generating fire code.
     */
    @Override
    public String generateWrapupCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        CompositeActor container = ((CompositeActor) _director.getContainer());
        Iterator<?> actors = container.deepEntityList().iterator();

        while (actors.hasNext()) {
            NamedObj actor = (NamedObj) actors.next();
            String sanitizedActorName = CodeGeneratorAdapter
                    .generateName(actor);
            code.append(_eol + sanitizedActorName + "_wrapup();");
        }
        code.append(_eol + "return;" + _eol);

        return code.toString();
    }

    /** Get the code generator associated with this adapter class.
     *  @return The code generator associated with this adapter class.
     *  @see #setCodeGenerator(GenericCodeGenerator)
     */
    @Override
    public CCodeGenerator getCodeGenerator() {
        return (CCodeGenerator) super.getCodeGenerator();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Generate variable declarations for inputs and outputs and parameters.
     *  Append the declarations to the given string buffer.
     *  @param target The ProgramCodeGeneratorAdapter for which code needs to be generated.
     *  @return code The generated code.
     *  @exception IllegalActionException If the adapter class for the model
     *   director cannot be found.
     */
    @Override
    protected String _generateVariableDeclaration(
            NamedProgramCodeGeneratorAdapter target)
                    throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        ProgramCodeGenerator codeGenerator = getCodeGenerator();

        String name = CodeGeneratorAdapter.generateName(getComponent());
        // Generate variable declarations for referenced parameters.
        String referencedParameterDeclaration = _generateReferencedParameterDeclaration(target);
        if (referencedParameterDeclaration.length() > 1) {
            code.append(_eol
                    + codeGenerator.comment(name
                            + "'s referenced parameter declarations."));
            code.append(referencedParameterDeclaration);
        }

        //        // Generate variable declarations for input ports.
        //        String inputVariableDeclaration = _generateInputVariableDeclaration(target);
        //        if (inputVariableDeclaration.length() > 1) {
        //            code.append(_eol
        //                    + codeGenerator.comment(name
        //                            + "'s input variable declarations."));
        //            code.append(inputVariableDeclaration);
        //        }
        //
        //        // Generate variable declarations for output ports.
        //        String outputVariableDeclaration = _generateOutputVariableDeclaration(target);
        //        if (outputVariableDeclaration.length() > 1) {
        //            code.append(_eol
        //                    + codeGenerator.comment(name
        //                            + "'s output variable declarations."));
        //            code.append(outputVariableDeclaration);
        //        }

        //        // Generate type convert variable declarations.
        //        String typeConvertVariableDeclaration = _generateTypeConvertVariableDeclaration(target);
        //        if (typeConvertVariableDeclaration.length() > 1) {
        //            code.append(_eol
        //                    + codeGenerator.comment(name
        //                            + "'s type convert variable declarations."));
        //            code.append(typeConvertVariableDeclaration);
        //        }

        return processCode(code.toString());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    //    /** Generate input variable declarations.
    //     *  @param target The ProgramCodeGeneratorAdapter for which code needs to be generated.
    //     *  @return a String that declares input variables.
    //     *  @exception IllegalActionException If thrown while
    //     *  getting port information.
    //     */
    //    private String _generateInputVariableDeclaration(
    //            ProgramCodeGeneratorAdapter target) throws IllegalActionException {
    //        boolean dynamicReferencesAllowed = allowDynamicMultiportReference();
    //
    //        StringBuffer code = new StringBuffer();
    //
    //        Iterator<?> inputPorts = ((Actor) target.getComponent())
    //                .inputPortList().iterator();
    //        while (inputPorts.hasNext()) {
    //            TypedIOPort inputPort = (TypedIOPort) inputPorts.next();
    //
    //            if (!inputPort.isOutsideConnected()) {
    //                continue;
    //            }
    //
    //            code.append("static " + targetType(inputPort.getType()) + " "
    //                    + CodeGeneratorAdapter.generateName(inputPort));
    //
    //            int bufferSize = _ports.getBufferSize(inputPort);
    //            if (inputPort.isMultiport()) {
    //                code.append("[" + inputPort.getWidth() + "]");
    //                if (bufferSize > 1 || dynamicReferencesAllowed) {
    //                    code.append("[" + bufferSize + "]");
    //                }
    //            } else {
    //                if (bufferSize > 1) {
    //                    code.append("[" + bufferSize + "]");
    //                }
    //            }
    //
    //            code.append(";" + _eol);
    //        }
    //
    //        return code.toString();
    //    }
    //
    //    /** Generate output variable declarations.
    //     *  @return a String that declares output variables.
    //     *  @exception IllegalActionException If thrown while
    //     *  getting port information.
    //     */
    //    private String _generateOutputVariableDeclaration(
    //            ProgramCodeGeneratorAdapter target) throws IllegalActionException {
    //        StringBuffer code = new StringBuffer();
    //
    //        Iterator<?> outputPorts = ((Actor) target.getComponent())
    //                .outputPortList().iterator();
    //
    //        while (outputPorts.hasNext()) {
    //            TypedIOPort outputPort = (TypedIOPort) outputPorts.next();
    //
    //            // If either the output port is a dangling port or
    //            // the output port has inside receivers.
    //            if (!outputPort.isOutsideConnected()
    //                    || outputPort.isInsideConnected()) {
    //                code.append("static " + targetType(outputPort.getType()) + " "
    //                        + CodeGeneratorAdapter.generateName(outputPort));
    //
    //                if (outputPort.isMultiport()) {
    //                    code.append("[" + outputPort.getWidthInside() + "]");
    //                }
    //
    //                int bufferSize = _ports.getBufferSize(outputPort);
    //
    //                if (bufferSize > 1) {
    //                    code.append("[" + bufferSize + "]");
    //                }
    //                code.append(";" + _eol);
    //            }
    //        }
    //
    //        return code.toString();
    //    }
    //
    //    /** Generate type convert variable declarations.
    //     * @param target The ProgramCodeGeneratorAdapter for which code needs to be generated.
    //     *  @return a String that declares type convert variables.
    //     *  @exception IllegalActionException If thrown while
    //     *  getting port information.
    //     */
    //    private String _generateTypeConvertVariableDeclaration(
    //            NamedProgramCodeGeneratorAdapter target)
    //            throws IllegalActionException {
    //        StringBuffer code = new StringBuffer();
    //
    //        Iterator<?> channels = target.getTypeConvertChannels().iterator();
    //        while (channels.hasNext()) {
    //            ProgramCodeGeneratorAdapter.Channel channel = (ProgramCodeGeneratorAdapter.Channel) channels
    //                    .next();
    //            Type portType = ((TypedIOPort) channel.port).getType();
    //
    //            if (getCodeGenerator().isPrimitive(portType)) {
    //
    //                code.append("static ");
    //                code.append(targetType(portType));
    //                code.append(" " + getTypeConvertReference(channel));
    //
    //                //int bufferSize = getBufferSize(channel.port);
    //                int bufferSize = Math.max(
    //                        DFUtilities.getTokenProductionRate(channel.port),
    //                        DFUtilities.getTokenConsumptionRate(channel.port));
    //
    //                if (bufferSize > 1) {
    //                    code.append("[" + bufferSize + "]");
    //                }
    //                code.append(";" + _eol);
    //            }
    //        }
    //        return code.toString();
    //    }

    /** Generate referenced parameter declarations.
     *  @param target The target for which referenced parameter
     *  declarations should be generated.
     *  @return a String that declares referenced parameters.
     *  @exception IllegalActionException If thrown while
     *  getting modified variable information.
     */
    private String _generateReferencedParameterDeclaration(
            ProgramCodeGeneratorAdapter target) throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        if (_referencedParameters.containsKey(target)) {
            for (Parameter parameter : _referencedParameters.get(target)) {
                // avoid duplicate declaration.
                if (!getCodeGenerator().getModifiedVariables().contains(
                        parameter)) {
                    code.append(""
                            + targetType(parameter.getType())
                            + " "
                            + getCodeGenerator()
                            .generateVariableName(parameter) + ";"
                            + _eol);
                }
            }
        }

        return code.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    private int _portNumber = 0;

    private boolean _doubleFlag;

    private boolean _booleanFlag;

    private boolean _intFlag;

    /** Name of variable containing jni class for Objects. */
    private String _objClass = "objClass";

    /** Name of variable containing jni double class. */
    private String _objClassD = "objClassD";

    /** Name of variable containing jni int class. */
    private String _objClassI = "objClassI";

}
