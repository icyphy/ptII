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
package ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.actor;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.PubSubPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.hoc.MirrorPort;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.cg.kernel.generic.CodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.ProgramCodeGenerator;
import ptolemy.cg.kernel.generic.program.TemplateParser;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.domains.de.kernel.DEReceiver;
import ptolemy.domains.modal.modal.ModalBasePort;
import ptolemy.domains.ptides.kernel.PtidesReceiver;
import ptolemy.domains.ptides.lib.PtidesPort;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
////IOPort

/**
 * Code generator C adapter for {@link ptolemy.actor.IOPort}.
 *
 * @author William Lucas
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (wlc)
 * @Pt.AcceptedRating Red (wlc)
 */

public class IOPort
extends
ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.IOPort {

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
     * Generate code for replacing the get() macro.
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

        int channelIndex = Integer.parseInt(channel);

        TypedIOPort port = (TypedIOPort) getComponent();
        Type type = port.getType();
        String typeString = getCodeGenerator().codeGenType(type);
        if (!((ptolemy.actor.IOPort) getComponent()).isOutsideConnected()) {
            return processCode("$new(" + typeString + "(0))->payload."
                    + typeString);
        }
        String result = "(*(" + port.getName() + "->get))((struct IOPort*) "
                + port.getName() + "_X_COMA_X_ " + channelIndex + ")";
        if (type instanceof BaseType) {
            result += "->payload." + typeString;
        } else if (type instanceof RecordType) {
            result += "->payload.Record";
        }

        return result;
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
        int channelIndex = Integer.parseInt(channel);

        TypedIOPort port = (TypedIOPort) getComponent();
        Type type = port.getType();
        String typeString = getCodeGenerator().codeGenType(type);
        if (!((ptolemy.actor.IOPort) getComponent()).isOutsideConnected()) {
            return processCode("$new(" + typeString + "(0))->payload."
                    + typeString);
        }
        String result = "(*(" + port.getName() + "->get))((struct IOPort*) "
                + port.getName() + "_X_COMA_X_ " + channelIndex + ")";

        // if (type instanceof BaseType) {
        //     result += "->payload." + typeString;
        // } else if (type instanceof RecordType) {
        //     result += "->payload.Record";
        // }

        return result;


    }

    /**
     * Generate the code of the declaration of an IOPort (or a subclass)
     * Initialize all its dependencies (container, widths ...)
     *
     * In this base class we deal with all the known kinds of ports, however
     * ideally we should create an adapter for each type of port.
     *
     * There are essentially four types of ports :
     * - input/output
     * - from a composite/atomic actor
     * The creation of the receivers relies on this distinction.
     *
     * @return the port declaration to include in the actor constructor
     * @exception IllegalActionException if the getWidth or getWidthInside
     *          throws an exception
     */

    public String generatePortDeclaration() throws IllegalActionException {
        // Basic parameters needed
        StringBuffer result = new StringBuffer();

        TemplateParser tParser = getTemplateParser();
        ProgramCodeGenerator codeGenerator = getCodeGenerator();

        ptolemy.actor.IOPort port = (ptolemy.actor.IOPort) getComponent();
        String portName = port.getName();
        String typePort = port.getClass().getSimpleName();

        // FIXME : in first approximation, a parameter port can be seen as a
        // regular TypedIOPort
        if (port instanceof ParameterPort || port instanceof PubSubPort
                || port instanceof ModalBasePort) {
            typePort = "TypedIOPort";
        }

        NamedObj actor = port.getContainer();
        String sanitizedActorName = CodeGeneratorAdapter.generateName(actor);

        if (!port.isInsideConnected() && !port.isOutsideConnected()) {
            // No need to deal with a disconnected port
            return "";
        }

        // Common part to all types of ports
        result.append(portName + " = (struct TypedIOPort*)" + typePort
                + "_New();" + _eol

                + "#ifdef _debugging" + _eol
                + portName + "->setName((struct IOPort*)"
                + portName + ", \"" + port.getName() + "\");" + _eol
                + "#endif" + _eol

                + portName + "->container = (struct Actor*)"
                + sanitizedActorName + ";" + _eol

                + portName + "->_isInsideConnected = "
                + port.isInsideConnected() + ";" + _eol

                + portName + "->_isOutsideConnected = "
                + port.isOutsideConnected() + ";" + _eol

                + portName + "->_isInput = " + port.isInput() + ";" + _eol

                + portName + "->_isOutput = " + port.isOutput() + ";"
                + _eol

                + portName + "->_isMultiport = " + port.isMultiport() + ";"
                + _eol

                + portName + "->_width = " + port.getWidth() + ";" + _eol
                + portName + "->_insideWidth = " + port.getWidthInside()
                + ";" + _eol

                + portName + "->_numberOfSinks = " + port.numberOfSinks()
                + ";" + _eol

                + portName + "->_numberOfSources = "
                + port.numberOfSources() + ";" + _eol);

        Parameter parameter = (Parameter) ((NamedObj) port)
                .getAttribute("delayOffset");
        Double ioPortDelayOffset = null;
        if (parameter != null) {
            Token token = parameter.getToken();
            if (token instanceof DoubleToken) {
                ioPortDelayOffset = ((DoubleToken) token).doubleValue();
            } else if (token instanceof ArrayToken) {
                ioPortDelayOffset = ((DoubleToken) ((ArrayToken) token)
                        .getElement(0)).doubleValue();
            }
        }
        if (ioPortDelayOffset != null) {
            result.append(portName + "->delayOffset = "
                    + ioPortDelayOffset.doubleValue() + ";" + _eol);
        }

        if (port instanceof MirrorPort) {
            MirrorPort mport = (MirrorPort) port;
            MirrorPort associatedPort = mport.getAssociatedPort();
            String accessorAssociatedPort = CodeGeneratorAdapter
                    .generateName(associatedPort.getContainer())
                    + "_get_"
                    + associatedPort.getName() + "()";
            result.append("((struct PtidesPort*)" + portName
                    + ")->_associatedPort = (struct PtidesPort*)"
                    + accessorAssociatedPort + ";" + _eol);
            if (mport instanceof PtidesPort) {
                PtidesPort ptidesPort = (PtidesPort) mport;
                result.append("((struct PtidesPort*)"
                        + portName
                        + ")->actuateAtEventTimestamp = "
                        + ((BooleanToken) ptidesPort.actuateAtEventTimestamp
                                .getToken()).booleanValue() + ";" + _eol);
                result.append("((struct PtidesPort*)"
                        + portName
                        + ")->deviceDelay = "
                        + ((DoubleToken) ptidesPort.deviceDelay.getToken())
                        .doubleValue() + ";" + _eol);
                result.append("((struct PtidesPort*)"
                        + portName
                        + ")->deviceDelayBound = "
                        + ((DoubleToken) ptidesPort.deviceDelayBound.getToken())
                        .doubleValue() + ";" + _eol);
                result.append("((struct PtidesPort*)"
                        + portName
                        + ")->isNetworkPort = "
                        + ((BooleanToken) ptidesPort.isNetworkPort.getToken())
                        .booleanValue() + ";" + _eol);
                if (ptidesPort.isNetworkReceiverPort()
                        || ptidesPort.isNetworkTransmitterPort()) {
                    result.append("((struct PtidesPort*)"
                            + portName
                            + ")->networkDelayBound = "
                            + ((DoubleToken) ptidesPort.networkDelayBound
                                    .getToken()).doubleValue() + ";" + _eol);
                    result.append("((struct PtidesPort*)"
                            + portName
                            + ")->sourcePlatformDelayBound = "
                            + ((DoubleToken) ptidesPort.sourcePlatformDelayBound
                                    .getToken()).doubleValue() + ";" + _eol);
                }
            }
        }

        // In the case of a TypedPort we have to add the type
        // Moreover, if it is a PtidesPort, the record type and
        // all its subtypes have to be added
        if (port instanceof TypedIOPort) {
            TypedIOPort typedPort = (TypedIOPort) port;
            String type = codeGenerator.codeGenType(typedPort.getType());
            if (type.contains(",")) {
                result.append(portName + "->_type = TYPE_Record;" + _eol);
                tParser.addNewTypesUsed("Record");
                for (String typ : type.split(",")) {
                    tParser.addNewTypesUsed(typ);
                }
            } else {
                result.append(portName + "->_type = TYPE_" + type + ";" + _eol);
                tParser.addNewTypesUsed(type);
            }
        }

        int foo = 0;
        if (port.isInput()) {
            result.append("pblListAdd(" + sanitizedActorName
                    + "->_inputPorts, " + portName + ");" + _eol);
        }
        if (port.isOutput()) {
            result.append("pblListAdd(" + sanitizedActorName
                    + "->_outputPorts, " + portName + ");" + _eol);
        }

        Receiver[][] receiverss;
        if (port.isInput()) {
            receiverss = port.getReceivers();
        } else if (port.isOutput() && actor instanceof CompositeActor) {
            receiverss = port.getInsideReceivers();
        } else {
            receiverss = new Receiver[0][];
        }

        String directorCall = (actor instanceof CompositeActor ? "getExecutiveDirector"
                : "getDirector");
        if (port instanceof PtidesPort) {
            directorCall = (!port.isInput() && actor instanceof CompositeActor
                    && ((CompositeActor) actor).isOpaque() ? "getDirector"
                            : "getExecutiveDirector");
        }
        String localReceiver = (port.isInput() ? "_localReceivers"
                : "_localInsideReceivers");

        for (Receiver[] receivers : receiverss) {
            result.append("PblList* " + portName + "_" + foo
                    + " = pblListNewArrayList();" + _eol);
            int bar = 0;
            for (Receiver receiver : receivers) {
                String typeReceiver = receiver.getClass().getSimpleName();
                // Quick fix : a QueueReceiver is a simple FIFO, we can replace it by a DEReceiver
                if (typeReceiver.compareTo("QueueReceiver") == 0) {
                    typeReceiver = "DEReceiver";
                }
                String receiverName = portName + "_" + foo + "_" + bar;
                result.append("struct " + typeReceiver + "* " + receiverName
                        + " = " + typeReceiver + "_New();" + _eol);
                result.append(receiverName + "->container = (struct IOPort*)"
                        + portName + ";" + _eol);
                // FIXME : not a good way to do this
                if (receiver instanceof PtidesReceiver) {
                    result.append(receiverName
                            + "->_director = (struct PtidesDirector*)(*("
                            + sanitizedActorName + "->" + directorCall + "))("
                            + sanitizedActorName + ");" + _eol);
                } else if (receiver instanceof DEReceiver) {
                    result.append(receiverName
                            + "->_director = (struct DEDirector*)(*("
                            + sanitizedActorName + "->" + directorCall + "))("
                            + sanitizedActorName + ");" + _eol);
                }
                result.append("pblListAdd(" + portName + "_" + foo + ", "
                        + receiverName + ");" + _eol);
                bar++;
            }
            result.append("pblListAdd(" + portName + "->" + localReceiver
                    + " , " + portName + "_" + foo + ");" + _eol);
            foo++;
        }

        // In case of a composite actor the port has two sides
        if (port.isInput() && actor instanceof CompositeActor) {
            for (foo = 0; foo < port.getWidthInside(); foo++) {
                result.append("PblList* " + portName + "__" + foo
                        + " = pblListNewArrayList();" + _eol);
                result.append("pblListAdd(" + portName
                        + "->_insideReceivers , " + portName + "__" + foo
                        + ");" + _eol);
            }
        }
        if (port.isOutput()) {
            for (foo = 0; foo < port.getWidth(); foo++) {
                result.append("PblList* " + portName + "__" + foo
                        + " = pblListNewArrayList();" + _eol);
                result.append("pblListAdd(" + portName + "->_farReceivers, "
                        + portName + "__" + foo + ");" + _eol);
            }
        }

        return result.toString();
    }

    /**
     * Generate code to check if the receiver has a token.
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
        if (!((ptolemy.actor.IOPort) getComponent()).isOutsideConnected()) {
            return "false";
        }
        int channelNumber = 0;
        channelNumber = Integer.parseInt(channel);
        TypedIOPort port = (TypedIOPort) getComponent();
        String result = "(*(" + port.getName()
                + "->hasToken))((struct IOPort*) " + port.getName() + ", "
                + channelNumber + ")";

        return result;
    }

    /**
     * Generate code for replacing the send() macro.
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

        if (!((ptolemy.actor.IOPort) getComponent()).isOutsideConnected()) {
            return "";
        }
        int channelIndex = Integer.parseInt(channel);
        TypedIOPort port = (TypedIOPort) getComponent();
        Type type = port.getType();
        String typeString = getCodeGenerator().codeGenType(type);
        String tokenCode;
        if (type instanceof BaseType) {
            tokenCode = "$new(" + typeString + "(" + dataToken + "))";
        } else if (type instanceof RecordType) {
            tokenCode = "$new(Record(" + dataToken + "->timestamp, "
                    + dataToken + "->microstep, " + dataToken + "->payload))";
        } else {
            tokenCode = dataToken;
        }
        String result = "(*(" + port.getName() + "->send))((struct IOPort*) "
                + port.getName() + ", " + channelIndex + ", " + tokenCode + ")";

        return result;
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

        if (!((ptolemy.actor.IOPort) getComponent()).isOutsideConnected()) {
            return "";
        }
        int channelIndex = Integer.parseInt(channel);
        TypedIOPort port = (TypedIOPort) getComponent();
        Type type = port.getType();
        String typeString = getCodeGenerator().codeGenType(type);
        String tokenCode;
        if (type instanceof BaseType) {
            tokenCode = "$new(" + typeString + "(" + dataToken + "))";
        } else if (type instanceof RecordType) {
            tokenCode = "$new(Record(" + dataToken + "->timestamp, "
                    + dataToken + "->microstep, " + dataToken + "->payload))";
        } else {
            tokenCode = dataToken;
        }
        String result = "(*(" + port.getName()
                + "->sendLocalInside))((struct IOPort*) " + port.getName()
                + ", " + channelIndex + ", " + tokenCode + ")";

        return result;
    }
}
