/* Code generator adapter class associated with the SDFDirector class.

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
package ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.domains.sdf.kernel;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.util.DFUtilities;
import ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.sched.StaticSchedulingDirector;
import ptolemy.cg.kernel.generic.CodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.GenericCodeGenerator;
import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.ProgramCodeGenerator;
import ptolemy.cg.kernel.generic.program.ProgramCodeGeneratorAdapter;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.Type;
import ptolemy.domains.sdf.kernel.SDFReceiver;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
////SDFDirector

/**
Code generator adapter associated with the SDFDirector class. This class
is also associated with a code generator.

@author Bert Rodiers
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Red (rodiers)
@Pt.AcceptedRating Red (rodiers)
 */

public class SDFDirector extends StaticSchedulingDirector {

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

    /** Generate The fire function code. This method is called when the firing
     *  code of each actor is not inlined. If the <i>inline</i> parameter of
     *  the code generator is true, then each actor's firing code is in a
     *  function with the same name as that of the actor.  If the <i>inline</i>
     *  parameter is false, then the firing code is grouped in inner classes.
     *
     *  @return The fire function code.
     *  @exception IllegalActionException If thrown while generating fire code.
     */
    @Override
    public String generateFireFunctionCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        List actorList = ((CompositeActor) _director.getContainer())
                .deepEntityList();

        // Sort by name so that we retrieve the actors from the list
        // by composite.
        Collections.sort(actorList, new FullNameComparator());

        ProgramCodeGenerator codeGenerator = getCodeGenerator();
        code.append(codeGenerator
                .comment("SDFDirector.generateFireFunctionCode()"));

        boolean inline = ((BooleanToken) codeGenerator.inline.getToken())
                .booleanValue();
        String className = "";

        // Magic text used to pass the name of the inner class in which
        // the code should be appended.
        String hackStart = "/* SDFDirectorHack: ";

        ptolemy.actor.Director director = (ptolemy.actor.Director) getComponent();

        // Place the fire functions in different inner classes so as to
        // result in smaller Java code that makes it possible for the compiler
        // to compile large files.
        HashMap<String, StringBuffer> innerClasses = new HashMap<String, StringBuffer>();
        Iterator<?> actors = actorList.iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            NamedProgramCodeGeneratorAdapter actorAdapter = (NamedProgramCodeGeneratorAdapter) codeGenerator
                    .getAdapter(actor);
            if (inline) {
                code.append(actorAdapter.generateFireFunctionCode());
            } else {
                // To avoid really large .java files that won't
                // compile, we put some of the inline fire methods
                // inside inner classes.

                // FIXME: What about other directors?
                String results[] = codeGenerator
                        .generateFireFunctionVariableAndMethodName((NamedObj) actor);
                className = results[0];

                StringBuffer innerClassBuffer = innerClasses.get(className);
                if (innerClassBuffer == null) {
                    innerClassBuffer = new StringBuffer();
                    if (!director.isEmbedded()
                            || director.getContainer() instanceof ptolemy.cg.lib.CompiledCompositeActor) {
                        innerClassBuffer.append(codeGenerator
                                .generateFireFunctionCompositeStart(className));
                    } else {
                        // If this director is not the top most
                        // codegen director, then place magic text
                        // into the code that is read by the parent
                        // director so that the parent director knows
                        // into which inner class to place the code.
                        innerClassBuffer.append(hackStart + className + " */");
                    }
                    innerClasses.put(className, innerClassBuffer);
                }
                String subFireCode = actorAdapter.generateFireFunctionCode();
                int startIndex = 0;
                if ((startIndex = subFireCode.indexOf(hackStart)) != -1) {
                    // Process the magic text and get the name of the inner class in
                    // which to place the code.
                    int endIndex = subFireCode.indexOf(" */", startIndex);
                    className = subFireCode.substring(
                            startIndex + hackStart.length(), endIndex);
                    innerClassBuffer = innerClasses.get(className);
                    if (innerClassBuffer == null) {
                        innerClassBuffer = new StringBuffer();
                        innerClassBuffer.append("class " + className + "{"
                                + _eol);
                        innerClasses.put(className, innerClassBuffer);
                    }
                }
                innerClassBuffer.append(subFireCode);
            }
        }
        if (!inline) {
            // Go through each inner class, append the code and add a
            // closing curly bracket.
            for (Map.Entry<String, StringBuffer> innerClassBuffer : innerClasses
                    .entrySet()) {
                code.append(innerClassBuffer.getValue());
                if (!director.isEmbedded()
                        || director.getContainer() instanceof ptolemy.cg.lib.CompiledCompositeActor) {
                    code.append(codeGenerator
                            .generateFireFunctionCompositeEnd());
                }
            }
        }
        return code.toString();
    }

    /** Generate the initialize code for the associated SDF director.
     *  @return The generated initialize code.
     *  @exception IllegalActionException If the adapter associated with
     *   an actor throws it while generating initialize code for the actor.
     */
    @Override
    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        ptolemy.actor.CompositeActor container = (ptolemy.actor.CompositeActor) getComponent()
                .getContainer();
        NamedProgramCodeGeneratorAdapter containerAdapter = (NamedProgramCodeGeneratorAdapter) getCodeGenerator()
                .getAdapter(container);

        // Reset the offset for all of the contained actors' input ports.
        Iterator<?> actors = container.deepEntityList().iterator();
        while (actors.hasNext()) {
            NamedObj actor = (NamedObj) actors.next();
            ProgramCodeGeneratorAdapter actorAdapter = (ProgramCodeGeneratorAdapter) getCodeGenerator()
                    .getAdapter(actor);
            StringBuffer resetCode = new StringBuffer();
            Iterator<?> inputPorts = ((Actor) actorAdapter.getComponent())
                    .inputPortList().iterator();

            while (inputPorts.hasNext()) {
                IOPort port = (IOPort) inputPorts.next();
                resetCode.append(ports.initializeOffsets(port));
            }

            if (resetCode.length() > 0) {
                code.append(_eol
                        + getCodeGenerator().comment(
                                1,
                                actor.getName()
                                        + "'s input offset initialization"));
                code.append(resetCode);
            }
        }

        // Reset the offset for all of the output ports.
        String resetCode = _resetOutputPortsOffset();
        if (resetCode.length() > 0) {
            code.append(_eol
                    + getCodeGenerator().comment(
                            getComponent().getName()
                                    + "'s output offset initialization"));
            code.append(resetCode);
        }

        // Generate the input offset initialization first so that this test passes:
        // cg/adapter/generic/program/procedural/java/adapters/ptolemy/domains/sdf/lib/test/auto/SampleDelay1.xml
        code.append(super.generateInitializeCode());

        // Generate code for creating external initial production.
        Iterator<?> outputPorts = container.outputPortList().iterator();
        while (outputPorts.hasNext()) {
            IOPort outputPort = (IOPort) outputPorts.next();
            int rate = DFUtilities.getTokenInitProduction(outputPort);

            if (rate > 0) {
                for (int i = 0; i < outputPort.getWidthInside(); i++) {
                    if (i < outputPort.getWidth()) {
                        String name = outputPort.getName();

                        if (outputPort.isMultiport()) {
                            name = name + '#' + i;
                        }

                        for (int k = 0; k < rate; k++) {
                            code.append(containerAdapter.getReference(name
                                    + "," + k, true));
                            code.append(" = ");
                            code.append(containerAdapter.getReference("@"
                                    + name + "," + k, false));
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
     *  @exception IllegalActionException If getting the adapter fails,
     *   or if generating the preinitialize code for a adapter fails,
     *   or if there is a problem getting the buffer size of a port.
     */
    @Override
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generatePreinitializeCode());

        _createInputBufferSizeAndOffsetMap();

        // For the inside receivers of the output ports.
        _createOutputBufferSizeAndOffsetMap();

        code.append(_createOffsetVariablesIfNeeded());

        return code.toString();
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
    @Override
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

        int width;
        if (port.isInput()) {
            width = port.getWidth();
        } else {
            width = port.getWidthInside();
        }

        if (width != 0) {
            // Declare the read offset variable.
            String channelReadOffset = CodeGeneratorAdapter.generateName(port);
            channelReadOffset += "_readOffset";

            // Now replace the concrete offset with the variable.
            for (int i = 0; i < width; i++) {
                ports.setReadOffset(port, i, channelReadOffset + "[" + i + "]");
            }
            channelReadOffset += "[" + width + "]";
            code.append("static int " + channelReadOffset + ";\n");

            // Declare the write offset variable.
            String channelWriteOffset = CodeGeneratorAdapter.generateName(port);

            channelWriteOffset += "_writeOffset";

            // Now replace the concrete offset with the variable.
            for (int i = 0; i < width; i++) {
                ports.setWriteOffset(port, i, channelWriteOffset + "[" + i
                        + "]");
            }
            channelWriteOffset += "[" + width + "]";
            code.append("static int " + channelWriteOffset + ";\n");
        }
        return code.toString();
    }

    /** Create the input buffer and offset map.
     *  @exception IllegalActionException If thrown while
     *  getting port information.
     */
    protected void _createInputBufferSizeAndOffsetMap()
            throws IllegalActionException {

        Iterator<?> actors = ((CompositeActor) _director.getContainer())
                .deepEntityList().iterator();

        while (actors.hasNext()) {
            NamedObj actor = (NamedObj) actors.next();

            //We only care about input ports where data are actually stored
            //except when an output port is not connected to any input port.
            //In that case the variable corresponding to the unconnected output
            //port always has size 1 and the assignment to this variable is
            //performed just for the side effect.
            Iterator<?> inputPorts = ((Actor) actor).inputPortList().iterator();

            while (inputPorts.hasNext()) {
                IOPort port = (IOPort) inputPorts.next();
                int length = port.getWidth();

                for (int i = 0; i < port.getWidth(); i++) {
                    int bufferSize = this
                    /*called on the director*/.getBufferSize(port, i);
                    ports.setBufferSize(port, i, bufferSize);
                }

                for (int i = 0; i < length; i++) {
                    ports.setReadOffset(port, i, Integer.valueOf(0));
                    ports.setWriteOffset(port, i, Integer.valueOf(0));
                }
            }
        }
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

        boolean padBuffers = padBuffers();

        int bufferSize = ports.getBufferSize(port, channelNumber);

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
                channelReadOffset.append(CodeGeneratorAdapter
                        .generateName(port));
                if (width > 1) {
                    channelReadOffset.append("_" + channelNumber);
                }
                channelReadOffset.append("_readOffset");
                String channelReadOffsetVariable = channelReadOffset.toString();
                //code.append("static int " + channelReadOffsetVariable + " = "
                //        + adapter.getReadOffset(port, channelNumber) + ";\n");
                code.append("static int " + channelReadOffsetVariable + ";\n");
                // Now replace the concrete offset with the variable.
                ports.setReadOffset(port, channelNumber,
                        channelReadOffsetVariable);
            }

            if (writeTokens % bufferSize != 0) {

                // Declare the write offset variable.
                StringBuffer channelWriteOffset = new StringBuffer();
                channelWriteOffset.append(CodeGeneratorAdapter
                        .generateName(port));
                if (width > 1) {
                    channelWriteOffset.append("_" + channelNumber);
                }
                channelWriteOffset.append("_writeOffset");
                String channelWriteOffsetVariable = channelWriteOffset
                        .toString();
                code.append("static int " + channelWriteOffsetVariable + ";\n");
                // Now replace the concrete offset with the variable.
                ports.setWriteOffset(port, channelNumber,
                        channelWriteOffsetVariable);
            }
        }
        return code.toString();
    }

    /** Generate variable initialization for the referenced parameters.
     *  @param target The ProgramCodeGeneratorAdapter for which code needs to be generated.
     *  @return code The generated code.
     *  @exception IllegalActionException If the adapter class for the model
     *   director cannot be found.
     */
    @Override
    protected String _generateVariableInitialization(
            NamedProgramCodeGeneratorAdapter target)
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        ProgramCodeGenerator codeGenerator = getCodeGenerator();

        //  Generate variable initialization for referenced parameters.
        if (!_referencedParameters.isEmpty()
                && _referencedParameters.containsKey(target)) {
            code.append(_eol
                    + codeGenerator.comment(1, target.getComponent().getName()
                            + "'s parameter initialization"));

            for (Parameter parameter : _referencedParameters.get(target)) {
                try {
                    // avoid duplication.
                    if (!codeGenerator.getModifiedVariables().contains(
                            parameter)) {
                        code.append(GenericCodeGenerator.INDENT1
                                + codeGenerator.generateVariableName(parameter)
                                + " = "
                                + target.getParameterValue(parameter.getName(),
                                        target.getComponent()) + ";" + _eol);
                    }
                } catch (Throwable throwable) {
                    throw new IllegalActionException(target.getComponent(),
                            throwable,
                            "Failed to generate variable initialization for \""
                                    + parameter + "\"");
                }
            }
        }
        return code.toString();
    }

    /**
     * Return an unique label for the given attribute referenced
     * by the given adapter.
     * @param target The ProgramCodeGeneratorAdapter for which code needs to be generated.
     * @param attribute The given attribute.
     * @param channelAndOffset The given channel and offset.
     * @return an unique label for the given attribute.
     * @exception IllegalActionException If the adapter throws it while
     *  generating the label.
     */
    @Override
    protected String _getParameter(NamedProgramCodeGeneratorAdapter target,
            Attribute attribute, String[] channelAndOffset)
            throws IllegalActionException {
        StringBuffer result = new StringBuffer();
        //FIXME: potential bug: if the attribute is not a parameter,
        //it will be referenced but not declared.
        if (attribute instanceof Parameter) {
            if (!_referencedParameters.containsKey(target)) {
                _referencedParameters.put(target, new HashSet<Parameter>());
            }
            _referencedParameters.get(target).add((Parameter) attribute);
        }

        result.append(getCodeGenerator().generateVariableName(attribute));

        if (!channelAndOffset[0].equals("")) {
            throw new IllegalActionException(getComponent(),
                    "a parameter cannot have channel number.");
        }

        if (!channelAndOffset[1].equals("")) {
            //result.append("[" + channelAndOffset[1] + "]");

            // FIXME Findbugs: [M D BC] Unchecked/unconfirmed cast [BC_UNCONFIRMED_CAST]
            // We are not certain that attribute is parameter.
            if (!(attribute instanceof Parameter)) {
                throw new InternalErrorException(attribute, null,
                        "The attribute " + attribute.getFullName()
                                + " is not a Parameter.");
            } else {
                Type elementType = ((ArrayType) ((Parameter) attribute)
                        .getType()).getElementType();

                result.insert(0, "Array_get(");
                if (getCodeGenerator().isPrimitive(elementType)) {
                    // Generate type specific Array_get(). e.g. IntArray_get().
                    result.insert(0, "/*CGH77*/"
                            + getCodeGenerator().codeGenType(elementType));
                }
                result.insert(0, "/*CGH77*/");

                result.append(" ," + channelAndOffset[1] + ")");
            }
        }
        return result.toString();
    }

    /** Check to see if the buffer size for the current schedule is greater
     *  than the previous size. If so, set the buffer size to the current
     *  buffer size needed.
     *  @exception IllegalActionException If thrown while getting adapter
     *   or buffer size.
     */
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
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Check to see if variables are needed to represent read and
     *  write offsets for the ports in the director's container.  If
     *  variables are needed, create them.
     *  @return Code that declares the read and write offset variables.
     *  @exception IllegalActionException If getting the rate or
     *   reading parameters throws it.
     */
    private String _createOffsetVariablesIfNeeded()
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        CompositeActor container = (CompositeActor) getComponent()
                .getContainer();

        boolean inline = ((BooleanToken) getCodeGenerator().inline.getToken())
                .booleanValue();

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
                    label1: while (sourcePorts.hasNext()) {
                        IOPort sourcePort = (IOPort) sourcePorts.next();
                        //                            ProgramCodeGeneratorAdapter adapter = getCodeGenerator().getAdapter(sourcePort
                        //                                    .getContainer());
                        int width;
                        if (sourcePort.isInput()) {
                            width = sourcePort.getWidthInside();
                        } else {
                            width = sourcePort.getWidth();
                        }
                        for (int j = 0; j < width; j++) {
                            Iterator<?> channels = getSinkChannels(sourcePort,
                                    j).iterator();
                            while (channels.hasNext()) {
                                ProgramCodeGeneratorAdapter.Channel channel = (ProgramCodeGeneratorAdapter.Channel) channels
                                        .next();
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
                    + getCodeGenerator().comment(
                            container.getName() + "'s offset variables"));
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
                        if (firings == null) {
                            throw new InternalErrorException(
                                    actor,
                                    null,
                                    "Actor "
                                            + actor.getFullName()
                                            + " does not have a firingsPerIteration attribute? "
                                            + "This can occur if a previous run created java files that "
                                            + "cannot be compiled.");
                        }
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
                        label2: while (sourcePorts.hasNext()) {
                            IOPort sourcePort = (IOPort) sourcePorts.next();
                            //                                ProgramCodeGeneratorAdapter adapter = getCodeGenerator().getAdapter(sourcePort
                            //                                        .getContainer());
                            int width;
                            if (sourcePort.isInput()) {
                                width = sourcePort.getWidthInside();
                            } else {
                                width = sourcePort.getWidth();
                            }
                            for (int j = 0; j < width; j++) {
                                Iterator<?> channels = getSinkChannels(
                                        sourcePort, j).iterator();
                                while (channels.hasNext()) {
                                    ProgramCodeGeneratorAdapter.Channel channel = (ProgramCodeGeneratorAdapter.Channel) channels
                                            .next();
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
                        + getCodeGenerator().comment(
                                actor.getName() + "'s offset variables"));
                code.append(tempCode2);
            }
        }
        return code.toString();
    }

    /** Create the output buffer and offset map.
     *  @exception IllegalActionException If thrown while getting the
     *  director adapter or while getting the buffer size or read offset
     *  or write offset.
     */
    private void _createOutputBufferSizeAndOffsetMap()
            throws IllegalActionException {

        Iterator<?> outputPorts = ((Actor) (CompositeActor) _director
                .getContainer()).outputPortList().iterator();

        while (outputPorts.hasNext()) {

            IOPort port = (IOPort) outputPorts.next();
            int length = port.getWidthInside();

            for (int i = 0; i < port.getWidthInside(); i++) {
                // If the local director is an SDF director, then the buffer
                // size got from the director adapter is final. Otherwise
                // the buffer size will be updated later on with the maximum
                // for all possible schedules.
                int bufferSize = this
                /*directorAdapter*/.getBufferSize(port, i);
                ports.setBufferSize(port, i, bufferSize);
            }

            for (int i = 0; i < length; i++) {
                ports.setReadOffset(port, i, Integer.valueOf(0));
                ports.setWriteOffset(port, i, Integer.valueOf(0));
            }
        }
    }

    /** Pad the buffer for the channel of the given port with the given
     *  channel number to a power of two.  Return the new buffer size.
     * @param port The port which has the buffer to pad.
     * @param channelNumber The number of the channel which has the buffer
     * to pad.
     * @return The size of the new buffer.
     * @exception IllegalActionException If thrown when getting the port's adapter.
     */
    private int _padBuffer(IOPort port, int channelNumber)
            throws IllegalActionException {

        int bufferSize = ports.getBufferSize(port, channelNumber);
        int newBufferSize = _ceilToPowerOfTwo(bufferSize);
        ports.setBufferSize(port, channelNumber, newBufferSize);

        return newBufferSize;
    }

    /** Reset the offsets of all inside buffers of all output ports of the
     *  associated composite actor to the default value of 0.
     *
     *  @return The reset code of the associated composite actor.
     *  @exception IllegalActionException If thrown while getting or
     *   setting the offset.
     */
    private String _resetOutputPortsOffset() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        Iterator<?> outputPorts = ((Actor) getComponent().getContainer())
                .outputPortList().iterator();

        while (outputPorts.hasNext()) {
            IOPort port = (IOPort) outputPorts.next();

            for (int i = 0; i < port.getWidthInside(); i++) {
                Object readOffset = ports.getReadOffset(port, i);
                if (readOffset instanceof Integer) {
                    // Read offset is a number.
                    ports.setReadOffset(port, i, Integer.valueOf(0));
                } else {
                    // Read offset is a variable.
                    code.append(CodeStream.indent((String) readOffset + " = 0;"
                            + _eol));
                }
                Object writeOffset = ports.getWriteOffset(port, i);
                if (writeOffset instanceof Integer) {
                    // Write offset is a number.
                    ports.setWriteOffset(port, i, Integer.valueOf(0));
                } else {
                    // Write offset is a variable.
                    code.append(CodeStream.indent((String) writeOffset
                            + " = 0;" + _eol));
                }
            }
        }
        return code.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** A hashmap that keeps track of parameters that are referenced for
     *  the associated actor.
     */
    protected HashMap<NamedProgramCodeGeneratorAdapter, HashSet<Parameter>> _referencedParameters = new HashMap<NamedProgramCodeGeneratorAdapter, HashSet<Parameter>>();

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Compare two NamedObjs by full name.
     */
    private static class FullNameComparator implements Comparator {

        // Findbugs suggested that this be made static.

        /** Compare two NamedObjs by fullName().
         *  @return -1 if object1 has fewer dots in its fullName(),
         *  1 if object1 has more dots in its fullName(),
         *  0 if the objects are the same.
         *  If the fullName()s of both NamedObjs have the
         *  same number of dots, then return the String compareTo()
         *  of the fullName()s.
         */
        @Override
        public int compare(Object object1, Object object2) {
            String name1 = ((NamedObj) object1).getFullName();
            String name2 = ((NamedObj) object2).getFullName();

            int index = 0;
            int dots1 = 0;
            while ((index = name1.indexOf(".", index)) != -1) {
                index++;
                dots1++;
            }
            int dots2 = 0;
            while ((index = name2.indexOf('.', index)) != -1) {
                index++;
                dots2++;
            }
            if (dots1 == dots2) {
                return 0;
            } else if (dots1 < dots2) {
                return -1;
            }
            return 1;
        }
    }
}
