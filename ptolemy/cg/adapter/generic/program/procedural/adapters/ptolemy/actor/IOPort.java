/* Code generator adapter for IOPort.

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
package ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor;

import ptolemy.actor.Actor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.cg.kernel.generic.PortCodeGenerator;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.TemplateParser;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
////IOPort

/**
 * Code generator adapter for {@link ptolemy.actor.IOPort}.
 *
 * @author Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */

public class IOPort extends NamedProgramCodeGeneratorAdapter implements
PortCodeGenerator {

    /**
     * Construct the code generator adapter for the given IOPort.
     *
     * @param component
     *            The IOPort.
     */
    public IOPort(ptolemy.actor.IOPort component) {
        super(component);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Generate code for replacing the get() macro. This delegates to the
     * receiver adapter for the specified channel and asks it to generate the
     * get code.
     *
     * @param channel
     *            The channel for which to generate the get code.
     * @param offset
     *            The offset in the array representation of the port.
     * @return The code that gets data from the specified channel.
     * @exception IllegalActionException
     *                If the receiver adapter is not found or it encounters an
     *                error while generating the get code.
     */
    @Override
    public String generateGetCode(String channel, String offset)
            throws IllegalActionException {
        Receiver[][] receivers = getReceiverAdapters();
        int channelIndex = Integer.parseInt(channel);
        // FIXME: take care of the offset, and why are we getting all the
        // receivers all the time?
        // FIXME: Don't know why would a channel have more than one relations
        // Thus for now to make sure we don't run into such problems, have a
        // check to ensure
        // this is not true. IF THIS IS TRUE HOWEVER, then the generated code in
        // the receivers would
        // need to change to ensure no name collisions between multiple
        // receivers within the same
        // channel would occur.
        if (receivers.length != 0) {
            if (channelIndex >= receivers.length) {
                throw new IllegalActionException(getComponent(),
                        "The channelIndex \"" + channelIndex
                        + "\" is greater than "
                        + "or equal to the length of the receiver \""
                        + receivers.length + "\".  The channel was: \""
                        + channel + "\", the offset was: \"" + offset
                        + "\".");
            }
            if (receivers[channelIndex].length > 1) {
                throw new IllegalActionException(
                        "Didn't take care of the case where one channel "
                                + "has more than one receiver");
            }
            if (receivers[channelIndex].length > 0) {
                TypedIOPort port = (TypedIOPort) getComponent();
                try {
                    // FIXME: I have no idea why generateGetCode of the receiver class is not called
                    // (for the CaseDirector??). So this is a hack that keeps PtidyOS working...
                    if (port.getContainer() != null
                            && ((Actor) port.getContainer()).getDirector() instanceof ptolemy.domains.ptides.kernel.PtidesDirector) {
                        return receivers[channelIndex][0]
                                .generateGetCode(offset);
                    }
                    if (port.getContainer() != null
                            && ((Actor) port.getContainer()).getDirector() instanceof ptolemy.actor.sched.StaticSchedulingDirector) {
                        return receivers[channelIndex][0]
                                .generateGetCode(offset);
                    }
                    if (port.getContainer() != null
                            && ((Actor) port.getContainer()).getDirector() instanceof ptolemy.domains.de.kernel.DEDirector) {
                        return receivers[channelIndex][0]
                                .generateGetCode(offset);
                    } else {
                        // Used by CaseDirector.
                        // FIXME: A real hack.  The problem is that ptolemy.actor.lib.hoc.CaseDirector
                        // extends actor.Director.  However, in cg, we end up needing a SDFDirector.
                        // This code is duplicated from StaticSchedulingDirector.generatePortName()
                        String portName = StringUtilities.sanitizeName(port
                                .getFullName());
                        if (portName.startsWith("_")) {
                            portName = portName.substring(1, portName.length());
                        }
                        portName = TemplateParser.escapePortName(portName);
                        // See ptolemy/cg/adapter/generic/program/procedural/java/adapters/ptolemy/domains/sdf/kernel/SDFDirector.java
                        //    _portVariableDeclaration(StringBuffer codeResult, TypedIOPort port)
                        if (port.isMultiport()) {
                            return portName + "[" + offset + "]";
                        } else {
                            if (!((BooleanToken) getCodeGenerator().variablesAsArrays
                                    .getToken()).booleanValue()) {
                                return portName;
                            }

                            // Get the name of the port that refers to the array of all ports.
                            // FIXME: we don't handle ports that have a BufferSize > 1.
                            return getCodeGenerator()
                                    .generatePortName(port, portName, 1 /*_ports.getBufferSize(port)*/);
                        }
                        //return "";
                    }
                } catch (Throwable throwable) {
                    throw new IllegalActionException(getComponent(), throwable,
                            " Failed to generate code for receiver "
                                    + receivers[channelIndex][0]
                                            + " on channel " + channelIndex);
                }
            }
        }
        TypedIOPort port = (TypedIOPort) getComponent();
        Type type = port.getType();
        if (port instanceof ParameterPort) {
            Parameter parameter = ((ParameterPort) getComponent())
                    .getParameter();
            // FIXME: Should this be isConnected() instead of numLinks()?
            if (port.numLinks() <= 0) {
                // Then use the parameter (attribute) instead.
                // FIXME: this seems wrong, why are we doing substitution only here?
                if (parameter.isStringMode()) {
                    // FIXME: Why do we need to escape other characters here?
                    // FIXME: Shouldn't this happen every where?
                    // Escape \d for ptII/ptolemy/actor/lib/string/test/auto/StringReplace2.xml
                    return "\""
                    + parameter.getExpression().replace("\\d", "\\\\d")
                    .replace("\\D", "\\\\D")
                    .replace("\"", "\\\"")
                    .replace("\\b", "\\\\b") + "\"";
                } else {
                    return parameter.getValueAsString();
                }
            } else {
                throw new InternalErrorException(
                        port,
                        null,
                        "Should not be happening, "
                                + "a ParameterPort is connected, but not handled earlier?");
            }
        }

        String typeString = getCodeGenerator().codeGenType(type);
        // The component port is not connected to anything, so get should
        // always return something trivial;

        String returnValue = "$convert_"
                + getCodeGenerator().codeGenType(BaseType.INT) + "_"
                + typeString + "(0)";
        System.err.println("cg IOPort: Warning: component port \""
                + getComponent().getFullName()
                + "\" is not connected, returning: " + returnValue);
        // FIXME: This is wrong, this could be a PortParameter.
        return returnValue;
    }

    /** Generate the code to get a token from a port, but don't
     *  include the type, for example payload.Int.
     *  @param channel The channel for which the get code is generated.
     *  @param offset The offset in the array representation of the port.
     *  @return The code that gets data from the channel.
     *  @exception IllegalActionException If the director adapter class cannot be found.
     */
    public String generateGetCodeWithoutType(String channel, String offset)
            throws IllegalActionException {
        return generateGetCode(channel, offset);
    }

    /**
     * Generate code to check if the receiver has a token. This delegates to the
     * receiver adapter for the specified channel and asks it to generate the
     * hasToken code.
     *
     * @param channel
     *            The channel for which to generate the hasToken code.
     * @param offset
     *            The offset in the array representation of the port.
     * @return The code that checks whether there is data in the specified
     *         channel.
     * @exception IllegalActionException
     *                If the receiver adapter is not found or it encounters an
     *                error while generating the hasToken code.
     */
    @Override
    public String generateHasTokenCode(String channel, String offset)
            throws IllegalActionException {
        Receiver[][] receivers = getReceiverAdapters();
        int channelNumber = 0;
        // try {
        channelNumber = Integer.parseInt(channel);
        // } catch (NumberFormatException e) {
        // if receivers
        // return Receiver.generateStaticGetCode(offset, channel, getName(),
        // getCodeGenerator().codeGenType(((TypedIOPort)getComponent()).getType()));
        // }
        // FIXME: take care of the offset, and why are we getting all the
        // receivers all the time?
        if (receivers.length != 0) {
            if (receivers[channelNumber].length > 1) {
                throw new IllegalActionException(
                        "Didn't take care of the case where one channel "
                                + "has more than one receiver");
            }
            if (receivers[channelNumber].length > 0) {
                return receivers[channelNumber][0].generateHasTokenCode(offset);
            }
        }
        // The component port is not connected to anything, so hasToken should
        // always return false;
        return "false";
    }

    /**
     * Generate code for replacing the send() macro. This delegates to the
     * receiver adapter for the specified channel and asks it to generate the
     * send code.
     *
     * @param channel
     *            The channel for which to generate the send code.
     * @param offset
     *            The offset in the array representation of the port.
     * @param dataToken
     *            The token to be sent.
     * @return The code that sends data to the specified channel.
     * @exception IllegalActionException
     *                If the receiver adapter is not found or it encounters an
     *                error while generating the send code.
     */
    @Override
    public String generatePutCode(String channel, String offset,
            String dataToken) throws IllegalActionException {

        Receiver[][] remoteReceivers = getRemoteReceiverAdapters();
        int channelIndex = Integer.parseInt(channel);
        // FIXME: take care of the offset, and why are we getting all the
        // receivers all the time?
        if (remoteReceivers == null || remoteReceivers.length <= channelIndex
                || remoteReceivers[channelIndex] == null) {
            return "";
        }
        StringBuffer code = new StringBuffer();

        if (remoteReceivers[channelIndex].length == 1) {
            code.append(remoteReceivers[channelIndex][0].generatePutCode(
                    (ptolemy.actor.IOPort) this.getComponent(), offset,
                    dataToken));
        } else {
            // Handle cases where the fire method gets a random number, but the output is connected
            // to two inputs.  See
            // $PTII/bin/ptcg -language java $PTII/ptolemy/cg/adapter/generic/program/procedural/java/adapters/ptolemy/actor/lib/test/auto/Uniform2.xml
            // Need two _eol here to avoid problems with comments, see
            // $PTII/bin/ptcg -language java  $PTII/ptolemy/cg/adapter/generic/program/procedural/java/adapters/ptolemy/actor/lib/test/auto/VectorAssemblerMatrix.xml
            code.append(_eol + "{" + _eol);
            TypedIOPort port = (TypedIOPort) getComponent();
            Type type = port.getType();
            if (dataToken.equals("object(null)")) {
                // This model needs to convert object(null) to object.
                // $PTII/bin/ptcg -language java $PTII/ptolemy/cg/adapter/generic/program/procedural/java/adapters/ptolemy/domains/sdf/lib/test/auto/SampleDelayObjectNull.xml
                System.out
                .println("Warning: cg IOPort hack, found object(null), converting to null");
                dataToken = "null";
            }
            code.append(targetType(type) + " temporary = " + dataToken + ";"
                    + _eol);

            boolean debug = ((IntToken) getCodeGenerator().verbosity.getToken())
                    .intValue() > 9;
                    for (int i = 0; i < remoteReceivers[channelIndex].length; i++) {
                        if (debug) {
                            code.append("/* IOPort.generatePutCode start. " + dataToken
                                    + " */" + _eol);
                        }
                        code.append(remoteReceivers[channelIndex][i].generatePutCode(
                                (ptolemy.actor.IOPort) this.getComponent(), offset,
                                "temporary"));
                        if (debug) {
                            code.append("/* IOPort.generatePutCode end. */" + _eol);
                        }
                    }
                    code.append("}" + _eol);
        }
        return code.toString();
    }

    /**
     * Generate the initialize code for this IOPort. The initialize code is
     * generated by appending the initialize code for each receiver contained by
     * this IOPort.
     *
     * @return The generated initialize code.
     * @exception IllegalActionException
     *                If an error occurs when getting the receiver adapters or
     *                generating their initialize code.
     */
    @Override
    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        Receiver[][] receivers = getReceiverAdapters();

        for (Receiver[] receiver : receivers) {
            for (int j = 0; j < receiver.length; j++) {
                code.append(receiver[j].generateInitializeCode());
            }
        }
        return code.toString();
    }

    // /** Generate the preinitialize code for this IOPort.
    // * The preinitialize code is generated by appending the
    // * preinitialize code for each receiver contained by this
    // * IOPort.
    // * @return The generated preinitialize code.
    // * @exception IllegalActionException If an error occurs
    // * when getting the receiver adapters or
    // * generating their preinitialize code.
    // */
    // public String generatePreinitializeCode() throws IllegalActionException {
    // StringBuffer code = new StringBuffer();
    //
    // Receiver[][] receivers = getReceiverAdapters();
    //
    // for (int i = 0; i < receivers.length; i++) {
    // for (int j = 0; j < receivers[i].length; j++) {
    // code.append(receivers[i][j].generatePreinitializeCode());
    // }
    // }
    // return code.toString();
    // }

    // /** Generate the wrapup code for this IOPort.
    // * The wrapup code is generated by appending the
    // * wrapup code for each receiver contained by this
    // * IOPort.
    // * @return The generated wrapup code.
    // * @exception IllegalActionException If an error occurs
    // * when getting the receiver adapters or
    // * generating their wrapup code.
    // */
    // public String generateWrapupCode() throws IllegalActionException {
    // StringBuffer code = new StringBuffer();
    //
    // Receiver[][] receivers = getReceiverAdapters();
    //
    // for (int i = 0; i < receivers.length; i++) {
    // for (int j = 0; j < receivers[i].length; j++) {
    // code.append(receivers[i][j].generateWrapupCode());
    // }
    // }
    // return code.toString();
    // }

    // /** Generate the shared code for this IOPort.
    // * The shared code is generated by appending the
    // * shared code for each receiver contained by this
    // * IOPort.
    // * @return The generated shared code.
    // * @exception IllegalActionException If an error occurs
    // * when getting the receiver adapters or
    // * generating their shared code.
    // */
    // public Set getSharedCode() throws IllegalActionException {
    // Set code = new HashSet();
    //
    // Receiver[][] receivers = getReceiverAdapters();
    //
    // for (int i = 0; i < receivers.length; i++) {
    // for (int j = 0; j < receivers[i].length; j++) {
    // code.addAll(receivers[i][j].getSharedCode());
    // }
    // }
    // return code;
    // }

    /**
     * Get the adapters for receiver contained in this port.
     *
     * @return The adapters.
     * @exception IllegalActionException
     *                Thrown if {@link #getAdapter(Object)} throws it.
     */
    public Receiver[][] getReceiverAdapters() throws IllegalActionException {
        ptolemy.actor.IOPort port = (ptolemy.actor.IOPort) getComponent();
        ptolemy.actor.Receiver[][] receivers = port.getReceivers();
        Receiver[][] receiverAdapters = new Receiver[receivers.length][];
        for (int i = 0; i < receivers.length; i++) {
            receiverAdapters[i] = new Receiver[receivers[i].length];
            for (int j = 0; j < receivers[i].length; j++) {
                receiverAdapters[i][j] = (Receiver) getAdapter(receivers[i][j]);
            }
        }
        return receiverAdapters;
    }

    /**
     * Get the adapters for the remote receivers connected to this port.
     *
     * @return The adapters.
     * @exception IllegalActionException
     *                Thrown if {@link #getAdapter(Object)} throws it.
     */
    public Receiver[][] getRemoteReceiverAdapters()
            throws IllegalActionException {
        ptolemy.actor.IOPort port = (ptolemy.actor.IOPort) getComponent();

        ptolemy.actor.Receiver[][] farReceivers = port.getRemoteReceivers();
        Receiver[][] receiverAdapters = new Receiver[farReceivers.length][];
        for (int i = 0; i < farReceivers.length; i++) {
            if (farReceivers[i] != null) {
                receiverAdapters[i] = new Receiver[farReceivers[i].length];
                for (int j = 0; j < farReceivers[i].length; j++) {
                    receiverAdapters[i][j] = (Receiver) getAdapter(farReceivers[i][j]);
                }
            }
        }
        return receiverAdapters;
    }

    /**
     * Generate code for replacing the sendLocalInside() macro.
     *
     * @param channel
     *            The channel for which to generate the send code.
     * @param offset
     *            The offset in the array representation of the port.
     * @param dataToken
     *            The token to be sent.
     * @return The code that sends data to the specified channel.
     * @exception IllegalActionException
     *                If the receiver adapter is not found or it encounters an
     *                error while generating the send code.
     */
    @Override
    public String generatePutLocalInsideCode(String channel, String offset,
            String dataToken) throws IllegalActionException {
        return this.generatePutCode(channel, offset, dataToken);
    }
}
