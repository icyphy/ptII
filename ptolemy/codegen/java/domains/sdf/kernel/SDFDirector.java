/* Code generator helper class associated with the SDFDirector class.

 Copyright (c) 2005-2011 The Regents of the University of California.
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
package ptolemy.codegen.java.domains.sdf.kernel;

import java.util.Iterator;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.embeddedJava.CompiledCompositeActor;
import ptolemy.actor.util.DFUtilities;
import ptolemy.codegen.java.actor.sched.StaticSchedulingDirector;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.codegen.kernel.CodeStream;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.domains.sdf.kernel.SDFReceiver;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
////SDFDirector

/**
 Code generator helper associated with the SDFDirector class. This class
 is also associated with a code generator.

 @author Ye Zhou, Gang Zhou
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (zgang)
 @Pt.AcceptedRating Red (eal)
 */
public class SDFDirector extends StaticSchedulingDirector {

    /** Construct the code generator helper associated with the given
     *  SDFDirector.
     *  @param sdfDirector The associated
     *  ptolemy.domains.sdf.kernel.SDFDirector
     */
    public SDFDirector(ptolemy.domains.sdf.kernel.SDFDirector sdfDirector) {
        super(sdfDirector);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generate code for declaring read and write offset variables if needed.
     *
     *  @return The generated code.
     *  @exception IllegalActionException If thrown while creating
     *  offset variables.
     */
    public String createOffsetVariablesIfNeeded() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(_createOffsetVariablesIfNeeded());
        code.append(super.createOffsetVariablesIfNeeded());
        return code.toString();
    }

    /** Generate the initialize code for the associated SDF director.
     *  @return The generated initialize code.
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating initialize code for the actor.
     */
    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generateInitializeCode());

        ptolemy.actor.CompositeActor container = (ptolemy.actor.CompositeActor) getComponent()
                .getContainer();
        CodeGeneratorHelper containerHelper = (CodeGeneratorHelper) _getHelper(container);

        // Generate code for creating external initial production.
        Iterator<?> outputPorts = container.outputPortList().iterator();
        while (outputPorts.hasNext()) {
            IOPort outputPort = (IOPort) outputPorts.next();
            int rate = DFUtilities.getTokenInitProduction(outputPort);

            if (rate > 0) {
                for (int i = 0; i < outputPort.getWidthInside(); i++) {
                    if (i < outputPort.getWidth()) {
                        String name = generateSimpleName(outputPort);

                        if (outputPort.isMultiport()) {
                            name = name + '#' + i;
                        }

                        for (int k = 0; k < rate; k++) {
                            code.append(containerHelper.getReference(name + ","
                                    + k));
                            code.append(" = ");
                            code.append(containerHelper.getReference("@" + name
                                    + "," + k));
                            code.append(";" + _eol);
                        }
                    }
                }

                // The offset of the ports connected to the output port is
                // updated by outside director.
                _updatePortOffset(outputPort, code, rate);
            }
        }
        return code.toString();
    }

    /** Generate the preinitialize code for this director.
     *  The preinitialize code for the director is generated by appending
     *  the preinitialize code for each actor.
     *  @return The generated preinitialize code.
     *  @exception IllegalActionException If getting the helper fails,
     *   or if generating the preinitialize code for a helper fails,
     *   or if there is a problem getting the buffer size of a port.
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generatePreinitializeCode());

        _updatePortBufferSize();
        _portNumber = 0;

        return code.toString();
    }

    /** Generate code for transferring enough tokens to complete an internal
     *  iteration.
     *  @param inputPort The port to transfer tokens.
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException If thrown while transferring tokens.
     */
    public void generateTransferInputsCode(IOPort inputPort, StringBuffer code)
            throws IllegalActionException {
        code.append(CodeStream.indent(_codeGenerator.comment("SDFDirector: "
                + "Transfer tokens to the inside.")));
        int rate = DFUtilities.getTokenConsumptionRate(inputPort);

        CompositeActor container = (CompositeActor) getComponent()
                .getContainer();
        ptolemy.codegen.java.actor.TypedCompositeActor compositeActorHelper = (ptolemy.codegen.java.actor.TypedCompositeActor) _getHelper(container);

        if (container instanceof CompiledCompositeActor
                && ((BooleanToken) _codeGenerator.generateEmbeddedCode
                        .getToken()).booleanValue()) {

            // FindBugs wants this instanceof check.
            if (!(inputPort instanceof TypedIOPort)) {
                throw new InternalErrorException(inputPort, null,
                        " is not an instance of TypedIOPort.");
            }
            Type type = ((TypedIOPort) inputPort).getType();
            String portName = generateSimpleName(inputPort);

            for (int i = 0; i < inputPort.getWidth(); i++) {
                if (i < inputPort.getWidthInside()) {

                    String tokensFromOneChannel = "tokensFromOneChannelOf"
                            + portName + i;

                    if (type == BaseType.INT) {
                        code.append("int[] " + tokensFromOneChannel
                                + " = (int[])(" + portName + "["
                                + String.valueOf(i) + "]);" + _eol);
                    } else if (type == BaseType.DOUBLE) {
                        code.append("double[] " + tokensFromOneChannel
                                + " = (double[])" + portName + "["
                                + String.valueOf(i) + "];" + _eol);
                    } else if (type == BaseType.BOOLEAN) {
                        code.append("boolean[] " + tokensFromOneChannel
                                + " = (boolean[])" + portName + "["
                                + String.valueOf(i) + "];" + _eol);
                    } else {
                        // FIXME: need to deal with other types
                    }

                    String portNameWithChannelNumber = portName;
                    if (inputPort.isMultiport()) {
                        portNameWithChannelNumber = portName + '#' + i;
                    }
                    for (int k = 0; k < rate; k++) {
                        code.append(compositeActorHelper.getReference("@"
                                + portNameWithChannelNumber + "," + k));
                        /*if (type == PointerToken.POINTER) {
                            code.append(" = (void *) "
                                    + pointerToTokensFromOneChannel + "[" + k
                                    + "];" + _eol);
                        } else {*/
                        code.append(" = " + tokensFromOneChannel + "[" + k
                                + "];" + _eol);
                        //}
                    }
                }
            }

        } else {
            for (int i = 0; i < inputPort.getWidth(); i++) {
                if (i < inputPort.getWidthInside()) {
                    String name = generateSimpleName(inputPort);

                    if (inputPort.isMultiport()) {
                        name = name + '#' + i;
                    }

                    for (int k = 0; k < rate; k++) {
                        code.append(compositeActorHelper.getReference("@"
                                + name + "," + k));
                        code.append(" = " + _eol);
                        code.append(compositeActorHelper.getReference(name
                                + "," + k));
                        code.append(";" + _eol);
                    }
                }
            }
        }

        // Generate the type conversion code before fire code.
        code.append(compositeActorHelper.generateTypeConvertFireCode(true));

        // The offset of the input port itself is updated by outside director.
        _updateConnectedPortsOffset(inputPort, code, rate);
    }

    /** Generate code for transferring enough tokens to fulfill the output
     *  production rate.
     *  @param outputPort The port to transfer tokens.
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException If thrown while transferring tokens.
     */
    public void generateTransferOutputsCode(IOPort outputPort, StringBuffer code)
            throws IllegalActionException {
        code.append(CodeStream.indent(_codeGenerator.comment("SDFDirector: "
                + "Transfer tokens to the outside.")));

        int rate = DFUtilities.getTokenProductionRate(outputPort);

        CompositeActor container = (CompositeActor) getComponent()
                .getContainer();
        ptolemy.codegen.java.actor.TypedCompositeActor compositeActorHelper = (ptolemy.codegen.java.actor.TypedCompositeActor) _getHelper(container);

        if (container instanceof CompiledCompositeActor
                && ((BooleanToken) _codeGenerator.generateEmbeddedCode
                        .getToken()).booleanValue()) {

            if (_portNumber == 0) {
                int numberOfOutputPorts = container.outputPortList().size();

                code.append("Object[] tokensToAllOutputPorts = "
                        + " new Object[" + String.valueOf(numberOfOutputPorts)
                        + "];" + _eol);
            }

            String portName = generateSimpleName(outputPort);
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
                    String portReference = compositeActorHelper
                            .getReference("@" + portNameWithChannelNumber + ","
                                    + k);
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
            code.append("tokensToAllOutputPorts ["
                    + String.valueOf(_portNumber) + "] = " + tokensToThisPort
                    + ";" + _eol);

            _portNumber++;

        } else {
            for (int i = 0; i < outputPort.getWidthInside(); i++) {
                if (i < outputPort.getWidth()) {
                    String name = generateSimpleName(outputPort);

                    if (outputPort.isMultiport()) {
                        name = name + '#' + i;
                    }

                    for (int k = 0; k < rate; k++) {
                        code.append(CodeStream.indent(compositeActorHelper
                                .getReference(name + "," + k)));
                        code.append(" =" + _eol);
                        code.append(CodeStream.indent(compositeActorHelper
                                .getReference("@" + name + "," + k)));
                        code.append(";" + _eol);
                    }
                }
            }
        }

        // The offset of the ports connected to the output port is
        // updated by outside director.
        _updatePortOffset(outputPort, code, rate);
    }

    /** Return the buffer size of a given channel (i.e, a given port
     *  and a given channel number). The default value is 1. If the
     *  port is an output port, then the buffer size is obtained
     *  from the inside receiver. If it is an input port, then it
     *  is obtained from the specified port.
     *  @param port The given port.
     *  @param channelNumber The given channel number.
     *  @return The buffer size of the given channel.
     *  @exception IllegalActionException If the channel number is
     *   out of range or if the port is neither an input nor an
     *   output.
     */
    public int getBufferSize(IOPort port, int channelNumber)
            throws IllegalActionException {
        Receiver[][] receivers;

        if (port.isInput()) {
            receivers = port.getReceivers();
        } else if (port.isOutput()) {

            receivers = port.getInsideReceivers();
        } else {
            // Findbugs: receivers could be null, so we throw an exception.
            throw new IllegalActionException(port,
                    "Port is neither an input nor an output.");
        }

        //try {
        int size = 0;

        for (int copy = 0; copy < receivers[channelNumber].length; copy++) {
            int copySize = ((SDFReceiver) receivers[channelNumber][copy])
                    .getCapacity();

            if (copySize > size) {
                size = copySize;
            }

            // When an output port of a composite actor is directly
            // connected to an input port of the same composite actor,
            // calling getCapacity() will return 0. Therefore we use
            // the rate to determine the buffer size.
            if (port.isOutput()) {
                copySize = DFUtilities.getRate(port);
                if (copySize > size) {
                    size = copySize;
                }
            }
        }

        return size;
        //}
        //catch (ArrayIndexOutOfBoundsException ex) {
        //    throw new IllegalActionException(port, "Channel out of bounds: "
        //            + channelNumber);
        //}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create offset variables for the channels of the given port.
     *  The offset variables are generated unconditionally, but this
     *  method is only called when dynamic multiport references are
     *  desired.  The buffers should be padded before this method is
     *  called.
     *
     *  @param port The port whose offset variables are generated.
     *  @return Code that declares the read and write offset variables.
     *  @exception IllegalActionException If getting the rate or
     *   reading parameters throws it.
     */
    protected String _createDynamicOffsetVariables(IOPort port)
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        CodeGeneratorHelper helper = (CodeGeneratorHelper) _getHelper(port
                .getContainer());

        int width;
        if (port.isInput()) {
            width = port.getWidth();
        } else {
            width = port.getWidthInside();
        }

        if (width != 0) {
            // Declare the read offset variable.
            String channelReadOffset = CodeGeneratorHelper.generateName(port);
            channelReadOffset += "_readOffset";

            // Now replace the concrete offset with the variable.
            for (int i = 0; i < width; i++) {
                helper.setReadOffset(port, i, channelReadOffset + "[" + i + "]");
            }
            channelReadOffset += "[" + width + "]";
            code.append("static int " + channelReadOffset + ";\n");

            // Declare the write offset variable.
            String channelWriteOffset = CodeGeneratorHelper.generateName(port);

            channelWriteOffset += "_writeOffset";

            // Now replace the concrete offset with the variable.
            for (int i = 0; i < width; i++) {
                helper.setWriteOffset(port, i, channelWriteOffset + "[" + i
                        + "]");
            }
            channelWriteOffset += "[" + width + "]";
            code.append("static int " + channelWriteOffset + ";\n");
        }
        return code.toString();
    }

    /** Check to see if variables are needed to represent read and
     *  write offsets for the ports in the director's container.  If
     *  variables are needed, create them.
     *  @return Code that declares the read and write offset variables.
     *  @exception IllegalActionException If getting the rate or
     *   reading parameters throws it.
     */
    protected String _createOffsetVariablesIfNeeded()
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        CompositeActor container = (CompositeActor) getComponent()
                .getContainer();

        boolean inline = ((BooleanToken) _codeGenerator.inline.getToken())
                .booleanValue();

        //boolean padBuffers = ((BooleanToken) _codeGenerator.padBuffers
        //        .getToken()).booleanValue();

        StringBuffer tempCode = new StringBuffer();
        Iterator<?> outputPorts = container.outputPortList().iterator();
        while (outputPorts.hasNext()) {

            IOPort outputPort = (IOPort) outputPorts.next();
            // If dynamic references are desired, conditionally pad buffers
            // and append the dynamic offset variables for output ports.
            for (int i = 0; i < outputPort.getWidthInside(); i++) {
                int readTokens = 0;
                int writeTokens = 0;
                // If each actor firing is inlined in the code, then read
                // and write positions in the buffer must return to the
                // previous values after one iteration of the container actor
                // in order to avoid using read and write offset variables.
                if (inline) {
                    readTokens = DFUtilities.getRate(outputPort);
                    writeTokens = readTokens;
                    // If each actor firing is wrapped in a function, then read
                    // and write positions in the buffer must return to the
                    // previous values after one firing of this actor or one
                    // firing of the actor that produces tokens consumed by the
                    // inside receiver of this actor in order to avoid using
                    // read and write offset variables.
                } else {
                    readTokens = DFUtilities.getRate(outputPort);
                    Iterator<?> sourcePorts = outputPort.insideSourcePortList()
                            .iterator();
                    label1:
                    while (sourcePorts.hasNext()) {
                        IOPort sourcePort = (IOPort) sourcePorts.next();
                        //                            CodeGeneratorHelper helper = (CodeGeneratorHelper) _getHelper(sourcePort
                        //                                    .getContainer());
                        int width;
                        if (sourcePort.isInput()) {
                            width = sourcePort.getWidthInside();
                        } else {
                            width = sourcePort.getWidth();
                        }
                        for (int j = 0; j < width; j++) {
                            Iterator<?> channels = CodeGeneratorHelper
                                    .getSinkChannels(sourcePort, j).iterator();
                            while (channels.hasNext()) {
                                Channel channel = (Channel) channels.next();
                                if (channel.port == outputPort
                                        && channel.channelNumber == i) {
                                    writeTokens = DFUtilities
                                            .getRate(sourcePort);
                                    break label1;
                                }
                            }
                        }
                    }
                }
                tempCode.append(_createOffsetVariablesIfNeeded(outputPort, i,
                        readTokens, writeTokens));
            }
        }
        if (tempCode.length() > 0) {
            code.append("\n"
                    + _codeGenerator.comment(generateSimpleName(container)
                            + "'s offset variables"));
            code.append(tempCode);
        }

        Iterator<?> actors = container.deepEntityList().iterator();
        while (actors.hasNext()) {
            StringBuffer tempCode2 = new StringBuffer();
            Actor actor = (Actor) actors.next();
            Iterator<?> inputPorts = actor.inputPortList().iterator();
            while (inputPorts.hasNext()) {
                IOPort inputPort = (IOPort) inputPorts.next();
                // If dynamic references are desired, conditionally pad buffers
                // and append the dynamic offset variables for input ports.

                for (int i = 0; i < inputPort.getWidth(); i++) {
                    int readTokens = 0;
                    int writeTokens = 0;
                    // If each actor firing is inlined in the code,
                    // then read and write positions in the buffer
                    // must return to the previous values after one
                    // iteration of the container actor in order to
                    // avoid using read and write offset variables.
                    if (inline) {
                        Variable firings = (Variable) ((NamedObj) actor)
                                .getAttribute("firingsPerIteration");
                        int firingsPerIteration = ((IntToken) firings
                                .getToken()).intValue();
                        readTokens = DFUtilities.getRate(inputPort)
                                * firingsPerIteration;
                        writeTokens = readTokens;

                        // If each actor firing is wrapped in a
                        // function, then read and write positions in
                        // the buffer must return to the previous
                        // values after one firing of this actor or
                        // one firing of the actor that produces
                        // tokens consumed by this actor in order to
                        // avoid using read and write offset
                        // variables.
                    } else {
                        readTokens = DFUtilities.getRate(inputPort);
                        Iterator<?> sourcePorts = inputPort.sourcePortList()
                                .iterator();
                        label2:
                        while (sourcePorts.hasNext()) {
                            IOPort sourcePort = (IOPort) sourcePorts.next();
                            //                                CodeGeneratorHelper helper = (CodeGeneratorHelper) _getHelper(sourcePort
                            //                                        .getContainer());
                            int width;
                            if (sourcePort.isInput()) {
                                width = sourcePort.getWidthInside();
                            } else {
                                width = sourcePort.getWidth();
                            }
                            for (int j = 0; j < width; j++) {
                                Iterator<?> channels = CodeGeneratorHelper
                                        .getSinkChannels(sourcePort, j)
                                        .iterator();
                                while (channels.hasNext()) {
                                    Channel channel = (Channel) channels.next();
                                    if (channel.port == inputPort
                                            && channel.channelNumber == i) {
                                        writeTokens = DFUtilities
                                                .getRate(sourcePort);
                                        break label2;
                                    }
                                }
                            }
                        }
                    }
                    tempCode2.append(_createOffsetVariablesIfNeeded(inputPort,
                            i, readTokens, writeTokens));
                }
            }
            if (tempCode2.length() > 0) {
                code.append("\n"
                        + _codeGenerator
                                .comment(generateSimpleName((NamedObj) actor)
                                        + "'s offset variables"));
                code.append(tempCode2);
            }
        }
        return code.toString();
    }

    /** Check for the given channel of the given port to see if
     *  variables are needed for recording read offset and write
     *  offset. If the buffer size of a channel divides the readTokens
     *  and writeTokens given in the argument, then there is no need
     *  for the variables. Otherwise the integer offsets are replaced
     *  with variables and the code to initialize these variables are
     *  generated.  If padded buffers are desired (based on the padBuffers
     *  parameter of the CodeGenerator), pad the buffers.
     *
     *  @param port The port to be checked.
     *  @param channelNumber The channel number.
     *  @param readTokens The number of tokens read.
     *  @param writeTokens The number of tokens written.
     *  @return Code that declares the read and write offset variables.
     *  @exception IllegalActionException If getting the rate or
     *   reading parameters throws it.
     */
    protected String _createOffsetVariablesIfNeeded(IOPort port,
            int channelNumber, int readTokens, int writeTokens)
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        CodeGeneratorHelper helper = (CodeGeneratorHelper) _getHelper(port
                .getContainer());
        boolean padBuffers = ((BooleanToken) _codeGenerator.padBuffers
                .getToken()).booleanValue();

        int bufferSize = helper.getBufferSize(port, channelNumber);

        // Increase the buffer size of that channel to the power of two.
        if (bufferSize > 0 && padBuffers) {
            bufferSize = _padBuffer(port, channelNumber);
        }

        if (bufferSize != 0
                && (readTokens % bufferSize != 0 || writeTokens % bufferSize != 0)) {
            int width;
            if (port.isInput()) {
                width = port.getWidth();
            } else {
                width = port.getWidthInside();
            }

            // We check again if the new bufferSize divides readTokens or
            // writeTokens. If yes, we could avoid using variable to represent
            // offset.
            if (readTokens % bufferSize != 0) {

                // Declare the read offset variable.
                StringBuffer channelReadOffset = new StringBuffer();
                channelReadOffset
                        .append(CodeGeneratorHelper.generateName(port));
                if (width > 1) {
                    channelReadOffset.append("_" + channelNumber);
                }
                channelReadOffset.append("_readOffset");
                String channelReadOffsetVariable = channelReadOffset.toString();
                //code.append("static int " + channelReadOffsetVariable + " = "
                //        + helper.getReadOffset(port, channelNumber) + ";\n");
                code.append("static int " + channelReadOffsetVariable + ";\n");
                // Now replace the concrete offset with the variable.
                helper.setReadOffset(port, channelNumber,
                        channelReadOffsetVariable);
            }

            if (writeTokens % bufferSize != 0) {

                // Declare the write offset variable.
                StringBuffer channelWriteOffset = new StringBuffer();
                channelWriteOffset.append(CodeGeneratorHelper
                        .generateName(port));
                if (width > 1) {
                    channelWriteOffset.append("_" + channelNumber);
                }
                channelWriteOffset.append("_writeOffset");
                String channelWriteOffsetVariable = channelWriteOffset
                        .toString();
                code.append("static int " + channelWriteOffsetVariable + ";\n");
                // Now replace the concrete offset with the variable.
                helper.setWriteOffset(port, channelNumber,
                        channelWriteOffsetVariable);
            }
        }
        return code.toString();
    }

    /** Check to see if the buffer size for the current schedule is greater
     *  than the previous size. If so, set the buffer size to the current
     *  buffer size needed.
     *  @exception IllegalActionException If thrown while getting helper
     *   or buffer size.
     */
    protected void _updatePortBufferSize() throws IllegalActionException {

        ptolemy.domains.sdf.kernel.SDFDirector director = (ptolemy.domains.sdf.kernel.SDFDirector) getComponent();
        CompositeActor container = (CompositeActor) director.getContainer();
        ptolemy.codegen.java.actor.TypedCompositeActor containerHelper = (ptolemy.codegen.java.actor.TypedCompositeActor) _getHelper(container);

        Iterator<?> actors = container.deepEntityList().iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            CodeGeneratorHelper actorHelper = (CodeGeneratorHelper) _getHelper((NamedObj) actor);
            Iterator<?> inputPorts = actor.inputPortList().iterator();
            while (inputPorts.hasNext()) {
                IOPort inputPort = (IOPort) inputPorts.next();
                for (int k = 0; k < inputPort.getWidth(); k++) {
                    int newCapacity = getBufferSize(inputPort, k);
                    int oldCapacity = actorHelper.getBufferSize(inputPort, k);
                    if (newCapacity > oldCapacity) {
                        actorHelper.setBufferSize(inputPort, k, newCapacity);
                    }
                }
            }
        }

        Iterator<?> outputPorts = container.outputPortList().iterator();
        while (outputPorts.hasNext()) {
            IOPort outputPort = (IOPort) outputPorts.next();
            for (int k = 0; k < outputPort.getWidthInside(); k++) {
                int newCapacity = getBufferSize(outputPort, k);
                int oldCapacity = containerHelper.getBufferSize(outputPort, k);
                if (newCapacity > oldCapacity) {
                    containerHelper.setBufferSize(outputPort, k, newCapacity);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Pad the buffer for the channel of the given port with the given
     *  channel number to a power of two.  Return the new buffer size.
     * @param port The port which has the buffer to pad.
     * @param channelNumber The number of the channel which has the buffer
     * to pad.
     * @return The size of the new buffer.
     * @exception IllegalActionException If thrown when getting the port's helper.
     */
    private int _padBuffer(IOPort port, int channelNumber)
            throws IllegalActionException {
        CodeGeneratorHelper helper = (CodeGeneratorHelper) _getHelper(port
                .getContainer());

        int bufferSize = helper.getBufferSize(port, channelNumber);
        int newBufferSize = _ceilToPowerOfTwo(bufferSize);
        helper.setBufferSize(port, channelNumber, newBufferSize);

        return newBufferSize;
    }

    private int _portNumber = 0;

}
