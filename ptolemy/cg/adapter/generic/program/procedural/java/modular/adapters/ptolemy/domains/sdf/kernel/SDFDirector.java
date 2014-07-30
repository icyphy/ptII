/* Modular code generator adapter class associated with the SDFDirector class.

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

///////////////////////////////////////////////////////////////////
////SDFDirector

/**
 Modular code generator adapter associated with the SDFDirector class. This class
 is also associated with a code generator.

 @author Dai Bui, Bert Rodiers
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (rodiers)
 @Pt.AcceptedRating Red (rodiers)
 */

package ptolemy.cg.adapter.generic.program.procedural.java.modular.adapters.ptolemy.domains.sdf.kernel;

import java.util.Iterator;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.AtomicActor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.util.DFUtilities;
import ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.TypedCompositeActor;
import ptolemy.cg.kernel.generic.CodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.GenericCodeGenerator;
import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.ProgramCodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.TemplateParser;
import ptolemy.cg.lib.ModularCodeGenLazyTypedCompositeActor;
import ptolemy.cg.lib.ModularCodeGenTypedCompositeActor;
import ptolemy.cg.lib.ModularCompiledSDFTypedCompositeActor;
import ptolemy.data.BooleanToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
////SDFDirector

/** Class for modular code generator.
 *
 *  @author Dai Bui, Bert Rodiers
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating red (rodiers)
 *  @Pt.AcceptedRating red (daib)
 */

public class SDFDirector
extends
ptolemy.cg.adapter.generic.program.procedural.java.adapters.ptolemy.domains.sdf.kernel.SDFDirector {

    /** Construct the code generator adapter associated with the given
     *  SDFDirector.
     *  @param sdfDirector The associated
     *  ptolemy.domains.sdf.kernel.SDFDirector
     */

    public SDFDirector(ptolemy.domains.sdf.kernel.SDFDirector sdfDirector) {
        super(sdfDirector);
        // TODO Auto-generated constructor stub
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /** Generate the code for the firing of actors according to the SDF
     *  schedule.
     *  @return The generated fire code.
     *  @exception IllegalActionException If the SDF director does not have an
     *   attribute called "iterations" or a valid schedule, or the actor to be
     *   fired cannot find its associated adapter.
     */
    @Override
    public String generateFireCode() throws IllegalActionException {

        StringBuffer code = new StringBuffer();
        code.append(CodeStream.indent(getCodeGenerator().comment(
                "The firing of the StaticSchedulingDirector")));
        boolean inline = ((BooleanToken) getCodeGenerator().inline.getToken())
                .booleanValue();

        // Generate code for one iteration.
        ptolemy.actor.sched.StaticSchedulingDirector director = (ptolemy.actor.sched.StaticSchedulingDirector) getComponent();
        Schedule schedule = director.getScheduler().getSchedule();

        Iterator<?> actorsToFire = schedule.firingIterator();
        while (actorsToFire.hasNext()) {
            Firing firing = (Firing) actorsToFire.next();
            Actor actor = firing.getActor();

            // FIXME: Before looking for a adapter class, we should check to
            // see whether the actor contains a code generator attribute.
            // If it does, we should use that as the adapter.
            NamedProgramCodeGeneratorAdapter adapter = (NamedProgramCodeGeneratorAdapter) getCodeGenerator()
                    .getAdapter(actor);

            if (actor instanceof ModularCodeGenTypedCompositeActor
                    || actor instanceof ModularCompiledSDFTypedCompositeActor) {

                String className = CodeGeneratorAdapter
                        .generateName((NamedObj) actor);
                String actorName = ModularCodeGenLazyTypedCompositeActor
                        .classToActorName(className);

                NamedProgramCodeGeneratorAdapter codegeneratorAdaptor = (NamedProgramCodeGeneratorAdapter) getAdapter(actor);

                code.append("{" + _eol);

                Iterator<?> inputPorts = actor.inputPortList().iterator();
                while (inputPorts.hasNext()) {
                    TypedIOPort inputPort = (TypedIOPort) inputPorts.next();

                    String type = ptolemy.cg.kernel.generic.program.procedural.java.JavaCodeGenerator
                            .codeGenType2(inputPort.getType());
                    //                    FIXME: What happens if the type is not Primitive
                    //                    if (!type.equals("Token")
                    //                            && !isPrimitive(ptolemy.cg.kernel.generic.program.procedural.java.JavaCodeGenerator.codeGenType(inputPort.getType()))) {
                    //                        type = "Token";
                    //                    }
                    for (int i = 0; i < inputPort.getWidth(); i++) {
                        if (DFUtilities.getTokenConsumptionRate(inputPort) > 1) {
                            code.append(type
                                    + "[] "
                                    + actorName
                                    + "_"
                                    + inputPort.getName()
                                    + "_"
                                    + i
                                    + " = new "
                                    + type
                                    + "["
                                    + DFUtilities
                                    .getTokenConsumptionRate(inputPort)
                                    + "];" + _eol);
                        } else {
                            code.append(type + " " + actorName + "_"
                                    + inputPort.getName() + "_" + i + ";"
                                    + _eol);
                        }
                    }
                }

                for (int j = 0; j < firing.getIterationCount(); j++) {

                    inputPorts = actor.inputPortList().iterator();

                    while (inputPorts.hasNext()) {
                        TypedIOPort inputPort = (TypedIOPort) inputPorts.next();
                        String portName = inputPort.getName();

                        int rate = DFUtilities
                                .getTokenConsumptionRate(inputPort);

                        for (int i = 0; i < inputPort.getWidth(); i++) {
                            if (i < inputPort.getWidthInside()) {

                                String portNameWithChannelNumber = portName;
                                if (inputPort.isMultiport()) {
                                    portNameWithChannelNumber = portName + '#'
                                            + i;
                                }
                                if (rate > 1) {
                                    for (int k = 0; k < rate; k++) {
                                        code.append(actorName + "_" + portName
                                                + "_" + i + "[" + k + "] = ");
                                        code.append(codegeneratorAdaptor
                                                .getReference(
                                                        portNameWithChannelNumber
                                                        + "," + k, true));
                                        code.append(";" + _eol);
                                    }
                                } else {
                                    code.append(actorName + "_" + portName
                                            + "_" + i);
                                    code.append(" = "
                                            + codegeneratorAdaptor
                                            .getReference(
                                                    portNameWithChannelNumber,
                                                    true) + ";" + _eol);
                                }

                            }
                        }
                    }

                    if (actor.inputPortList().size() > 0) {
                        code.append(actorName + ".fire(false, ");
                    } else {
                        code.append(actorName + ".fire(");
                    }

                    inputPorts = actor.inputPortList().iterator();
                    boolean addComma = false;
                    while (inputPorts.hasNext()) {
                        TypedIOPort inputPort = (TypedIOPort) inputPorts.next();

                        for (int i = 0; i < inputPort.getWidth(); i++) {
                            if (addComma) {
                                code.append(", ");
                            }
                            //                            code.append(codegeneratorAdaptor.getReference(inputPort
                            //                                    .getName()
                            //                                    + "#" + i));
                            code.append(actorName + "_" + inputPort.getName()
                                    + "_" + i);
                            addComma = true;
                        }
                    }

                    code.append(");" + _eol);

                    //transfer to external ports
                    Iterator<?> outputPorts = actor.outputPortList().iterator();
                    while (outputPorts.hasNext()) {
                        TypedIOPort outputPort = (TypedIOPort) outputPorts
                                .next();

                        int rate = DFUtilities
                                .getTokenProductionRate(outputPort);

                        for (int i = 0; i < outputPort.getWidth(); i++) {
                            if (rate <= 1) {
                                code.append(codegeneratorAdaptor.getReference(
                                        outputPort.getName() + "#" + i, true)
                                        + " = "
                                        + actorName
                                        + "."
                                        + codegeneratorAdaptor.getReference("@"
                                                + outputPort.getName() + "#"
                                                + i, false) + ";" + _eol);
                            } else {
                                for (int k = 0; k < rate; k++) {
                                    code.append(codegeneratorAdaptor
                                            .getReference(outputPort.getName()
                                                    + "#" + i + "," + k, true)
                                                    + " = "
                                                    + actorName
                                                    + "."
                                                    + codegeneratorAdaptor.getReference(
                                                            "@" + outputPort.getName()
                                                            + "#" + i + "," + k,
                                                            false) + ";" + _eol);
                                }
                            }
                        }
                    }

                    _generateUpdatePortOffsetCode(code, actor); //FIXME: How to do it efficiently
                }

                code.append("};" + _eol);

            } else {
                if (inline) {
                    for (int i = 0; i < firing.getIterationCount(); i++) {

                        // generate fire code for the actor
                        code.append(adapter.generateFireCode());

                        _generateUpdatePortOffsetCode(code, actor);
                    }
                } else {

                    int count = firing.getIterationCount();
                    if (count > 1) {
                        code.append("for (int i = 0; i < " + count
                                + " ; i++) {" + _eol);
                    }

                    code.append(generateName((NamedObj) actor) + "();" + _eol);

                    _generateUpdatePortOffsetCode(code, actor);

                    if (count > 1) {
                        code.append("}" + _eol);
                    }
                }
            }
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

        CompositeActor container = (CompositeActor) getComponent()
                .getContainer();
        TypedCompositeActor compositeActorAdapter = (TypedCompositeActor) getCodeGenerator()
                .getAdapter(container);

        // FindBugs wants this instanceof check.
        if (!(inputPort instanceof TypedIOPort)) {
            throw new InternalErrorException(inputPort, null,
                    " is not an instance of TypedIOPort.");
        }

        String portName = inputPort.getName();

        for (int i = 0; i < inputPort.getWidth(); i++) {
            if (i < inputPort.getWidthInside()) {

                String portNameWithChannelNumber = portName;
                if (inputPort.isMultiport()) {
                    portNameWithChannelNumber = portName + '#' + i;
                }
                if (rate > 1) {
                    for (int k = 0; k < rate; k++) {
                        code.append(compositeActorAdapter.getReference("@"
                                + portNameWithChannelNumber + "," + k, false));
                        code.append(" = " + portName + "_" + i + "[" + k + "];"
                                + _eol);
                    }
                } else {
                    code.append(compositeActorAdapter.getReference("@"
                            + portNameWithChannelNumber, false));
                    code.append(" = " + portName + "_" + i + ";" + _eol);
                }

            }
        }

        // Generate the type conversion code before fire code.
        code.append(compositeActorAdapter.generateTypeConvertFireCode(true));

        // The offset of the input port itself is updated by outside director.
        _updateConnectedPortsOffset(inputPort, code, rate);
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
        CompositeActor container = (CompositeActor) getComponent()
                .getContainer();
        TypedCompositeActor compositeActorAdapter = (TypedCompositeActor) getCodeGenerator()
                .getAdapter(container);

        int rate = DFUtilities.getTokenProductionRate(outputPort);

        if (container instanceof ModularCodeGenTypedCompositeActor) {
            if (_portNumber == 0) {
                int numberOfOutputPorts = container.outputPortList().size();

                code.append("Object[] tokensToAllOutputPorts = "
                        + " new Object[" + String.valueOf(numberOfOutputPorts)
                        + "];" + _eol);
            }

            String portName = outputPort.getName();
            String tokensToThisPort = "tokensTo" + portName;

            // FindBugs wants this instanceof check.
            if (!(outputPort instanceof TypedIOPort)) {
                throw new InternalErrorException(outputPort, null,
                        " is not an instance of TypedIOPort.");
            }

            Type type = ((TypedIOPort) outputPort).getType();

            int numberOfChannels = outputPort.getWidthInside();

            // Find construct correct array type.
            if (type == BaseType.INT) {
                code.append("int[][] " + tokensToThisPort + " =" + " new int[ "
                        + String.valueOf(numberOfChannels) + "][" + rate + "];"
                        + _eol);

            } else if (type == BaseType.DOUBLE) {
                code.append("double[][] " + tokensToThisPort + " ="
                        + " new double[ " + String.valueOf(numberOfChannels)
                        + "][" + rate + "];" + _eol);
            } else if (type == BaseType.BOOLEAN) {
                code.append("boolean[][] " + tokensToThisPort + " ="
                        + " new boolean[ " + String.valueOf(numberOfChannels)
                        + "][" + rate + "];" + _eol);

            } else {
                // FIXME: need to deal with other types
            }

            for (int i = 0; i < outputPort.getWidthInside(); i++) {
                String portNameWithChannelNumber = portName;
                if (outputPort.isMultiport()) {
                    portNameWithChannelNumber = portName + '#' + i;
                }

                for (int k = 0; k < rate; k++) {
                    String portReference = compositeActorAdapter.getReference(
                            "@" + portNameWithChannelNumber + "," + k, false);
                    /*if (type == PointerToken.POINTER) {
                        code.append(tokensToOneChannel + "[" + k
                                + "] = " + "(int) " + portReference + ";"
                                + _eol);
                    } else {*/
                    code.append(tokensToThisPort + "[" + i + "][" + k + "] = "
                            + portReference + ";" + _eol);
                    //}
                }
            }

            if (outputPort.getWidthInside() > 0) {
                code.append("tokensToAllOutputPorts ["
                        + String.valueOf(_portNumber) + "] = "
                        + tokensToThisPort + ";" + _eol);
            }

            _portNumber++;
        } else {
            for (int i = 0; i < outputPort.getWidthInside(); i++) {
                if (i < outputPort.getWidth()) {
                    String name = TemplateParser.escapePortName(outputPort
                            .getName());

                    if (outputPort.isMultiport()) {
                        name = name + '#' + i;
                    }

                    for (int k = 0; k < rate; k++) {
                        code.append(CodeStream.indent(compositeActorAdapter
                                .getReference(name + "," + k, true))
                                + " ="
                                + CodeStream.indent(compositeActorAdapter
                                        .getReference("@" + name + "," + k,
                                                false)) + ";" + _eol);
                    }
                }
            }
        }

    }

    /** Generate variable declarations for inputs and outputs and parameters.
     *  Append the declarations to the given string buffer.
     *  @return code The generated code.
     *  @exception IllegalActionException If the adapter class for the model
     *   director cannot be found.
     */
    @Override
    public String generateVariableDeclaration() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        CompositeActor container = (CompositeActor) _director.getContainer();
        GenericCodeGenerator codeGenerator = getCodeGenerator();
        {
            NamedProgramCodeGeneratorAdapter adapterObject = (NamedProgramCodeGeneratorAdapter) codeGenerator
                    .getAdapter(container);
            code.append(_generateVariableDeclaration(adapterObject));
        }

        Iterator<?> actors = container.deepEntityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            NamedProgramCodeGeneratorAdapter adapterObject = (NamedProgramCodeGeneratorAdapter) codeGenerator
                    .getAdapter(actor);
            if (actor instanceof CompositeActor
                    && !(actor instanceof ModularCodeGenTypedCompositeActor)) {
                code.append(adapterObject.generateVariableDeclaration());
            } else {
                code.append(_generateVariableDeclaration(adapterObject));
            }
        }

        ptolemy.actor.sched.StaticSchedulingDirector director = (ptolemy.actor.sched.StaticSchedulingDirector) getComponent();
        Schedule schedule = director.getScheduler().getSchedule();

        Iterator<?> actorsToFire = schedule.firingIterator();
        while (actorsToFire.hasNext()) {
            Firing firing = (Firing) actorsToFire.next();
            Actor actor = firing.getActor();

            if (actor instanceof ModularCodeGenTypedCompositeActor
                    || actor instanceof ModularCompiledSDFTypedCompositeActor) {
                String className = CodeGeneratorAdapter
                        .generateName((NamedObj) actor);
                String actorName = ModularCodeGenLazyTypedCompositeActor
                        .classToActorName(className);

                code.append(className + " " + actorName + ";" + _eol);
            }
        }

        return code.toString();
    }

    /** Generate variable initialization for the referenced parameters.
     *  @return code The generated code.
     *  @exception IllegalActionException If the adapter class for the model
     *   director cannot be found.
     */
    @Override
    public String generateVariableInitialization()
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        CompositeActor container = (CompositeActor) _director.getContainer();
        GenericCodeGenerator codeGenerator = getCodeGenerator();
        {
            NamedProgramCodeGeneratorAdapter adapterObject = (NamedProgramCodeGeneratorAdapter) codeGenerator
                    .getAdapter(container);
            code.append(_generateVariableInitialization(adapterObject));
        }

        Iterator<?> actors = container.deepEntityList().iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            NamedProgramCodeGeneratorAdapter adapterObject = (NamedProgramCodeGeneratorAdapter) codeGenerator
                    .getAdapter(actor);
            code.append(_generateVariableInitialization(adapterObject));
        }

        ptolemy.actor.sched.StaticSchedulingDirector director = (ptolemy.actor.sched.StaticSchedulingDirector) getComponent();
        Schedule schedule = director.getScheduler().getSchedule();

        Iterator<?> actorsToFire = schedule.firingIterator();
        while (actorsToFire.hasNext()) {
            Firing firing = (Firing) actorsToFire.next();
            Actor actor = firing.getActor();

            if (actor instanceof ModularCodeGenTypedCompositeActor
                    || actor instanceof ModularCompiledSDFTypedCompositeActor) {
                //call the internal generated code of the composite actor

                String className = CodeGeneratorAdapter
                        .generateName((NamedObj) actor);
                String actorName = ModularCodeGenLazyTypedCompositeActor
                        .classToActorName(className);

                code.append(actorName + " = new " + className + "();" + _eol);

                code.append(actorName + ".initialize();" + _eol);
            } else {
                NamedProgramCodeGeneratorAdapter adapterObject = (NamedProgramCodeGeneratorAdapter) codeGenerator
                        .getAdapter(actor);
                code.append(adapterObject.generateVariableInitialization());
            }
        }

        return code.toString();
    }

    /**
     * Return an unique label for the given port channel referenced
     * by the given adapter. By default, this delegates to the adapter to
     * generate the reference. Subclass may override this method
     * to generate the desire label according to the given parameters.
     * @param port The given port.
     * @param channelAndOffset The given channel and offset.
     * @param forComposite Whether the given adapter is associated with
     *  a CompositeActor
     * @param isWrite The type of the reference. True if this is
     *  a write reference; otherwise, this is a read reference.
     * @param target The ProgramCodeGeneratorAdapter for which code needs to be generated.
     * @return an unique reference label for the given port channel.
     * @exception IllegalActionException If the adapter throws it while
     *  generating the label.
     */
    @Override
    public String getReference(TypedIOPort port, String[] channelAndOffset,
            boolean forComposite, boolean isWrite,
            NamedProgramCodeGeneratorAdapter target)
                    throws IllegalActionException {

        StringBuffer result = new StringBuffer();
        boolean dynamicReferencesAllowed = allowDynamicMultiportReference();

        int channelNumber = 0;
        if (!channelAndOffset[0].equals("")) {
            // If dynamic multiport references are allowed, catch errors
            // when the channel specification is not an integer.
            if (dynamicReferencesAllowed) {
                try {
                    channelNumber = Integer.parseInt(channelAndOffset[0]);
                } catch (NumberFormatException ex) {
                    // Variable channel reference.
                    if (port.isOutput()) {
                        throw new IllegalActionException(
                                "Variable channel reference not supported"
                                        + " for output ports");
                    } else {
                        return _generatePortReference(port, channelAndOffset,
                                isWrite);
                    }
                }
            } else {
                channelNumber = Integer.parseInt(channelAndOffset[0]);
            }
        }

        // To support modal model, we need to check the following condition
        // first because an output port of a modal controller should be
        // mainly treated as an output port. However, during choice action,
        // an output port of a modal controller will receive the tokens sent
        // from the same port.  During commit action, an output port of a modal
        // controller will NOT receive the tokens sent from the same port.
        if (_checkRemote(forComposite, port)) {
            Receiver[][] remoteReceivers;

            // For the same reason as above, we cannot do: if (port.isInput())...
            if (port.isOutput()) {
                remoteReceivers = port.getRemoteReceivers();
            } else {
                remoteReceivers = port.deepGetReceivers();
            }

            if (remoteReceivers.length == 0) {
                // The channel of this output port doesn't have any sink.
                result.append(CodeGeneratorAdapter.generateName(target
                        .getComponent()));
                result.append("_");
                result.append(port.getName());
                return result.toString();
            }

            ProgramCodeGeneratorAdapter.Channel sourceChannel = new ProgramCodeGeneratorAdapter.Channel(
                    port, channelNumber);

            List<ProgramCodeGeneratorAdapter.Channel> typeConvertSinks = target
                    .getTypeConvertSinkChannels(sourceChannel);

            List<ProgramCodeGeneratorAdapter.Channel> sinkChannels = getSinkChannels(
                    port, channelNumber);

            boolean hasTypeConvertReference = false;

            for (int i = 0; i < sinkChannels.size(); i++) {
                ProgramCodeGeneratorAdapter.Channel channel = sinkChannels
                        .get(i);
                IOPort sinkPort = channel.port;
                int sinkChannelNumber = channel.channelNumber;

                // Type convert.
                if (typeConvertSinks.contains(channel)
                        && getCodeGenerator().isPrimitive(
                                ((TypedIOPort) sourceChannel.port).getType())) {

                    if (!hasTypeConvertReference) {
                        if (i != 0) {
                            result.append(" = ");
                        }
                        result.append(getTypeConvertReference(sourceChannel));

                        if (dynamicReferencesAllowed && port.isInput()) {
                            if (channelAndOffset[1].trim().length() > 0) {
                                result.append("[" + channelAndOffset[1].trim()
                                        + "]");
                            } else {
                                result.append("["
                                        + _generateChannelOffset(port, isWrite,
                                                channelAndOffset[0]) + "]");
                            }
                        } else {
                            int rate = Math
                                    .max(DFUtilities
                                            .getTokenProductionRate(sourceChannel.port),
                                            DFUtilities
                                            .getTokenConsumptionRate(sourceChannel.port));
                            if (rate > 1
                                    && channelAndOffset[1].trim().length() > 0) {
                                result.append("[" + channelAndOffset[1].trim()
                                        + "]");
                            }
                        }
                        hasTypeConvertReference = true;
                    } else {
                        // We already generated reference for this sink.
                        continue;
                    }
                } else {
                    if (i != 0) {
                        result.append(" = ");
                    }
                    result.append(CodeGeneratorAdapter.generateName(sinkPort));

                    if (sinkPort.isMultiport()) {
                        result.append("[" + sinkChannelNumber + "]");
                    }

                    if (channelAndOffset[1].equals("")) {
                        channelAndOffset[1] = "0";
                    }

                    //                    result.append(ports.generateOffset(sinkPort,
                    //                            channelAndOffset[1], sinkChannelNumber, true));

                    String res = ports.generateOffset(sinkPort,
                            channelAndOffset[1], sinkChannelNumber, true);
                    if (res.equals("")) {
                        if (sinkPort.getContainer() instanceof CompositeActor) {
                            SDFDirector directorAdapter = (SDFDirector) getAdapter(((CompositeActor) sinkPort
                                    .getContainer()).getDirector());
                            result.append(directorAdapter.ports.generateOffset(
                                    sinkPort, channelAndOffset[1],
                                    sinkChannelNumber, true));
                        }
                    } else {
                        result.append(res);
                    }

                }
            }

            return result.toString();
        }

        // Note that if the width is 0, then we have no connection to
        // the port but the port might be a PortParameter, in which
        // case we want the Parameter.
        // codegen/c/actor/lib/string/test/auto/StringCompare3.xml
        // tests this.

        if (_checkLocal(forComposite, port)) {

            result.append(CodeGeneratorAdapter.generateName(port));

            //if (!channelAndOffset[0].equals("")) {
            if (port.isMultiport()) {
                // Channel number specified. This must be a multiport.
                result.append("[" + channelAndOffset[0] + "]");
            }

            //if (port.getContainer() instanceof CompositeActor) {
            //                SDFDirector directorAdapter = (SDFDirector) getAdapter(((CompositeActor)port.getContainer()).getDirector());
            //                result.append(directorAdapter.ports.generateOffset(port,
            //                        channelAndOffset[1], channelNumber, isWrite));
            result.append(ports.generateOffset(port, channelAndOffset[1],
                    channelNumber, isWrite));
            //} else {
            //result.append(ports.generateOffset(port, channelAndOffset[1],
            //        channelNumber, isWrite));
            //}
            //            result.append(ports.generateOffset(port, channelAndOffset[1],
            //                    channelNumber, isWrite));

            return result.toString();
        }

        // FIXME: when does this happen?
        return "";
    }

    /** Check to see if the buffer size for the current schedule is greater
     *  than the previous size. If so, set the buffer size to the current
     *  buffer size needed.
     *  @exception IllegalActionException If thrown while getting adapter
     *   or buffer size.
     */
    @Override
    protected void _updatePortBufferSize() throws IllegalActionException {

        ptolemy.domains.sdf.kernel.SDFDirector director = (ptolemy.domains.sdf.kernel.SDFDirector) getComponent();
        CompositeActor container = (CompositeActor) director.getContainer();

        Iterator<?> actors = container.deepEntityList().iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            Iterator<?> inputPorts = actor.inputPortList().iterator();
            while (inputPorts.hasNext()) {
                IOPort inputPort = (IOPort) inputPorts.next();
                for (int k = 0; k < inputPort.getWidth(); k++) {
                    int newCapacity = getBufferSize(inputPort, k);
                    int oldCapacity = ports.getBufferSize(inputPort, k);
                    if (newCapacity > oldCapacity) {
                        ports.setBufferSize(inputPort, k, newCapacity);
                    }
                }
            }

            if (!(actor instanceof AtomicActor)) {
                Iterator<?> outputPorts = actor.outputPortList().iterator();
                while (outputPorts.hasNext()) {
                    IOPort outputPort = (IOPort) outputPorts.next();
                    for (int k = 0; k < outputPort.getWidth(); k++) {
                        int newCapacity = getBufferSize(outputPort, k);
                        int oldCapacity = ports.getBufferSize(outputPort, k);
                        if (newCapacity > oldCapacity) {
                            ports.setBufferSize(outputPort, k, newCapacity);
                        }
                    }
                }
            }

        }

        Iterator<?> outputPorts = container.outputPortList().iterator();
        while (outputPorts.hasNext()) {
            IOPort outputPort = (IOPort) outputPorts.next();
            for (int k = 0; k < outputPort.getWidthInside(); k++) {
                int newCapacity = getBufferSize(outputPort, k);
                int oldCapacity = ports.getBufferSize(outputPort, k);
                if (newCapacity > oldCapacity) {
                    ports.setBufferSize(outputPort, k, newCapacity);
                }
            }
        }

        Iterator<?> inputPorts = container.inputPortList().iterator();
        while (inputPorts.hasNext()) {
            IOPort inputPort = (IOPort) inputPorts.next();
            for (int k = 0; k < inputPort.getWidth(); k++) {
                int newCapacity = getBufferSize(inputPort, k);
                int oldCapacity = ports.getBufferSize(inputPort, k);
                if (newCapacity > oldCapacity) {
                    ports.setBufferSize(inputPort, k, newCapacity);
                }
            }
        }
    }

    //     private static String _generatePortReference(IOPort port,
    //             String[] channelAndOffset, boolean isWrite) {

    //         StringBuffer result = new StringBuffer();
    //         String channelOffset;
    //         if (channelAndOffset[1].equals("")) {
    //             channelOffset = _generateChannelOffset(port, isWrite,
    //                     channelAndOffset[0]);
    //         } else {
    //             channelOffset = channelAndOffset[1];
    //         }

    //         result.append(generateName(port));

    //         if (port.isMultiport()) {
    //             result.append("[" + channelAndOffset[0] + "]");
    //         }
    //         result.append("[" + channelOffset + "]");

    //         return result.toString();
    //     }
}
