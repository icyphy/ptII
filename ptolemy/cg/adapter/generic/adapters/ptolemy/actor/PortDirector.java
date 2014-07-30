/* A Director that use Ports and PortInfo.

 Copyright (c) 2013-2014 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.adapters.ptolemy.actor;

import java.util.List;
import java.util.StringTokenizer;

import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.util.DFUtilities;
import ptolemy.cg.kernel.generic.CodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.GenericCodeGenerator;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.ProgramCodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.TemplateParser;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// PortDirector

/**
 A Director that use Ports and PortInfo.

 @see GenericCodeGenerator
 @author Christopher Brooks
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Yellow (zhouye)
 @Pt.AcceptedRating Yellow (zhouye)
 */
public abstract class PortDirector extends Director {

    /** Construct the code generator adapter associated with the given director.
     *  Note before calling the generate*() methods, you must also call
     *  setCodeGenerator(GenericCodeGenerator).
     *  @param director The associated director.
     */
    public PortDirector(ptolemy.actor.Director director) {
        super(director);
        ports = new Ports(getComponent(), this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return whether the channels in multiports can be dynamically
     *  referenced using the $ref macro.
     *  @return True when the channels in multiports can be dynamically
     *  referenced using the $ref macro.
     *  @exception IllegalActionException If the expression cannot
     *   be parsed or cannot be evaluated, or if the result of evaluation
     *   violates type constraints, or if the result of evaluation is null
     *   and there are variables that depend on this one.
     */
    abstract public Boolean allowDynamicMultiportReference()
            throws IllegalActionException;

    /**
     * Generate sanitized name for the given named object. Remove all
     * underscores to avoid conflicts with systems functions.
     * @param port The port for which the name is generated.
     * @return The sanitized name.
     * @exception IllegalActionException If the variablesAsArrays parameter
     * cannot be read or if the buffer size of the port cannot be read.
     */
    abstract public String generatePortName(TypedIOPort port)
            throws IllegalActionException;

    /** Return the reference to the specified parameter or port of the
     *  associated actor. For a parameter, the returned string is in
     *  the form "fullName_parameterName". For a port, the returned string
     *  is in the form "fullName_portName[channelNumber][offset]", if
     *  any channel number or offset is given.
     *
     * <p>Usually given the name of an input port, getReference(String
     * name) returns a target language variable name representing the
     * input port. Given the name of an output port,
     * getReference(String name) returns variable names representing
     * the input ports connected to the output port.  However, if the
     * name of an input port starts with '@', getReference(String
     * name) returns variable names representing the input ports
     * connected to the given input port on the inside.  If the name
     * of an output port starts with '@', getReference(String name)
     * returns variable name representing the the given output port
     * which has inside receivers.  The special use of '@' is for
     * composite actor when tokens are transferred into or out of the
     * composite actor.</p>
     *
     *  @param name The name of the parameter or port
     *  @param isWrite Whether to generate the write or read offset.
     *  @param target The ProgramCodeGeneratorAdapter for which code needs to be generated.
     *  @return The reference to that parameter or port (a variable name,
     *   for example).
     *  @exception IllegalActionException If the parameter or port does not
     *   exist or does not have a value.
     */
    @Override
    public String getReference(String name, boolean isWrite,
            NamedProgramCodeGeneratorAdapter target)
                    throws IllegalActionException {
        name = processCode(name);
        String castType = _getCastType(name);
        String refName = _getRefName(name);
        String[] channelAndOffset = _getChannelAndOffset(name);

        boolean forComposite = false;
        if (refName.charAt(0) == '@') {
            forComposite = true;
            refName = refName.substring(1);
        }

        TypedIOPort port = target.getTemplateParser().getPort(refName);
        if (port != null) {

            if (port instanceof ParameterPort && port.numLinks() <= 0) {

                // Then use the parameter (attribute) instead.
            } else {
                String result = getReference(port, channelAndOffset,
                        forComposite, isWrite, target);

                String refType = getCodeGenerator().codeGenType(port.getType());

                String returnValue = _templateParser.generateTypeConvertMethod(
                        result, castType, refType);
                return returnValue;
            }
        }

        // Try if the name is a parameter.
        Attribute attribute = target.getComponent().getAttribute(refName);
        if (attribute != null) {
            String refType = _getRefType(attribute);

            String result = _getParameter(target, attribute, channelAndOffset);

            result = _templateParser.generateTypeConvertMethod(result,
                    castType, refType);

            return result;
        }

        throw new IllegalActionException(target.getComponent(),
                "Reference not found: " + name);
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
     * @param target The ProgramCodeGeneratorAdapter for which code
     * needs to be generated.
     * @return an unique reference label for the given port channel.
     * @exception IllegalActionException If the adapter throws it while
     *  generating the label.
     */
    public String getReference(TypedIOPort port, String[] channelAndOffset,
            boolean forComposite, boolean isWrite,
            NamedProgramCodeGeneratorAdapter target)
                    throws IllegalActionException {
        boolean dynamicReferencesAllowed = allowDynamicMultiportReference();

        int channelNumber = 0;
        boolean isChannelNumberInt = true;
        if (!channelAndOffset[0].equals("")) {
            // If dynamic multiport references are allowed, catch errors
            // when the channel specification is not an integer.
            if (dynamicReferencesAllowed) {
                try {
                    channelNumber = Integer.parseInt(channelAndOffset[0]);
                } catch (NumberFormatException ex) {
                    isChannelNumberInt = false;
                }
            } else {
                channelNumber = Integer.parseInt(channelAndOffset[0]);
            }
        }
        if (!isChannelNumberInt) { // variable channel reference.
            if (port.isOutput()) {
                throw new IllegalActionException(
                        "Variable channel reference not supported"
                                + " for output ports");
            } else {
                String returnValue = _generatePortReference(port,
                        channelAndOffset, isWrite);
                return returnValue;
            }
        }

        StringBuffer result = new StringBuffer();

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
                // The channel of this output port doesn't have any
                // sink or is a PortParameter.
                // Needed by $PTII/bin/ptcg -language java -variablesAsArrays true $PTII/ptolemy/cg/kernel/generic/program/procedural/java/test/auto/PortParameterOpaque.xml
                String returnValue = generatePortName(port);
                return returnValue;
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
                TypedIOPort sinkPort = (TypedIOPort) channel.port;
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
                    result.append(generatePortName(sinkPort));

                    if (sinkPort.isMultiport()) {
                        result.append("[" + sinkChannelNumber + "]");
                    }
                    if (channelAndOffset[1].equals("")) {
                        channelAndOffset[1] = "0";
                    }
                    result.append(ports.generateOffset(sinkPort,
                            channelAndOffset[1], sinkChannelNumber, true));
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

            result.append(/*NamedProgramCodeGeneratorAdapter.*/generatePortName(port));

            //if (!channelAndOffset[0].equals("")) {
            if (port.isMultiport()) {
                // Channel number specified. This must be a multiport.
                result.append("[" + channelAndOffset[0] + "]");
            }

            result.append(ports.generateOffset(port, channelAndOffset[1],
                    channelNumber, isWrite));
            return result.toString();
        }

        // FIXME: when does this happen?
        return "";
    }

    /** Return the sanitized name of this director adapter.
     * @return The name of the director
     */
    public String getSanitizedDirectorName() {
        _sanitizedDirectorName = CodeGeneratorAdapter.generateName(_director);
        return _sanitizedDirectorName;
    }

    /** Return whether we need to pad buffers or not.
     *  @return True when we need to pad buffers.
     *  @exception IllegalActionException If the expression cannot
     *   be parsed or cannot be evaluated, or if the result of evaluation
     *   violates type constraints, or if the result of evaluation is null
     *   and there are variables that depend on this one.
     */
    abstract public Boolean padBuffers() throws IllegalActionException;

    /** The meta information about the ports in the container. */
    public Ports ports;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return true if the port is a local port.
     *  @param forComposite True if we are checking for a composite
     *  @param port The port to be checked.
     *  @return A boolean true when the port is local.
     */
    static protected boolean _checkLocal(boolean forComposite, IOPort port) {
        return port.isInput() && !forComposite && port.isOutsideConnected()
                || port.isOutput() && forComposite;
    }

    /** Return true if the port is a remote port.
     *  @param forComposite True if this for a composite.
     *  @param port The port to check.
     *  @return True if the port is an output and not a composite
     *  or the port is an input and this is for a composite.
     */
    static protected boolean _checkRemote(boolean forComposite, IOPort port) {
        return port.isOutput() && !forComposite || port.isInput()
                && forComposite;
    }

    /**
     * Generate a string that represents the offset for a dynamically determined
     *  channel of a multiport.
     * @param port The referenced port.
     * @param isWrite Whether to generate the write or read offset.
     * @param channelString The string that will determine the channel.
     * @return The expression that represents the offset for a channel determined
     *  dynamically in the generated code.
     * @exception IllegalActionException If thrown while generating the port name.
     */
    protected/*static*/String _generateChannelOffset(TypedIOPort port,
            boolean isWrite, String channelString)
                    throws IllegalActionException {
        // By default, return the channel offset for the first channel.
        if (channelString.equals("")) {
            channelString = "0";
        }

        String channelOffset = generatePortName(port)
                + (isWrite ? "_writeOffset" : "_readOffset") + "["
                + channelString + "]";

        return channelOffset;
    }

    /**
     * Generate a string that represents the reference for an IOPort.
     * @param port The port to get the reference.
     * @param isWrite Whether to generate the write or read offset.
     * @param channelAndOffset The string[] that will determine the channel and the offset.
     * @return The expression that represents the reference for the port
     * @exception IllegalActionException If thrown while generating
     * the channel offsite or generating the port name.
     */
    protected String _generatePortReference(TypedIOPort port,
            String[] channelAndOffset, boolean isWrite)
                    throws IllegalActionException {

        StringBuffer result = new StringBuffer();
        String channelOffset;
        if (channelAndOffset[1].equals("")) {
            channelOffset = _generateChannelOffset(port, isWrite,
                    channelAndOffset[0]);
        } else {
            channelOffset = channelAndOffset[1];
        }

        result.append(generatePortName(port));

        if (port.isMultiport()) {
            result.append("[" + channelAndOffset[0] + "]");
        }
        result.append("[" + channelOffset + "]");

        return result.toString();
    }

    /** Get the cast type.
     *  @param name The name with # syntax.
     *  @return The cast type
     *  @exception IllegalActionException If there are more than two #
     *  in the name, indicating an invalid cast type.
     */
    protected String _getCastType(String name) throws IllegalActionException {
        StringTokenizer tokenizer = new StringTokenizer(name, "#,", true);

        // Get the referenced name.
        String refName = tokenizer.nextToken().trim();

        // Get the cast type (if any), so we can add the proper convert method.
        StringTokenizer tokenizer2 = new StringTokenizer(refName, "()", false);
        if (tokenizer2.countTokens() != 1 && tokenizer2.countTokens() != 2) {
            throw new IllegalActionException(getComponent(),
                    "Invalid cast type: " + refName);
        }

        if (tokenizer2.countTokens() == 2) {
            String type = tokenizer2.nextToken().trim();
            return type.length() > 0 ? type : null;
        }
        return null;
    }

    /** Return the channel and offset given in a string.
     *  The result is an string array of length 2. The first element
     *  indicates the channel index, and the second the offset. If either
     *  element is an empty string, it means that channel/offset is not
     *  specified.
     * @param name The given string.
     * @return An string array of length 2, containing expressions of the
     *  channel index and offset.
     * @exception IllegalActionException If the channel index or offset
     *  specified in the given string is illegal.
     */
    protected String[] _getChannelAndOffset(String name)
            throws IllegalActionException {

        String[] result = { "", "" };

        // Given expression of forms:
        //     "port"
        //     "port, offset", or
        //     "port#channel, offset".

        int poundIndex = TemplateParser.indexOf("#", name, 0);
        int commaIndex = TemplateParser.indexOf(",", name, 0);

        if (commaIndex < 0) {
            commaIndex = name.length();
        }
        if (poundIndex < 0) {
            poundIndex = commaIndex;
        }

        if (poundIndex < commaIndex) {
            result[0] = name.substring(poundIndex + 1, commaIndex);
        }

        if (commaIndex < name.length()) {
            result[1] = name.substring(commaIndex + 1);
        }
        return result;
    }

    /**
     * Generate a string that represents the reference to a parameter or a port
     * named "name".
     * @param name The name.
     * @return The string which represents the reference
     * @exception IllegalActionException If the reference cannot
     * be found or is an invalid cast type.
     */
    protected String _getRefName(String name) throws IllegalActionException {
        StringTokenizer tokenizer = new StringTokenizer(name, "#,", true);

        if (tokenizer.countTokens() != 1 && tokenizer.countTokens() != 3
                && tokenizer.countTokens() != 5) {
            throw new IllegalActionException(getComponent(),
                    "Reference not found: " + name);
        }

        // Get the referenced name.
        String refName = tokenizer.nextToken().trim();

        // Get the cast type (if any), so we can add the proper convert method.
        StringTokenizer tokenizer2 = new StringTokenizer(refName, "()", false);
        if (tokenizer2.countTokens() != 1 && tokenizer2.countTokens() != 2) {
            throw new IllegalActionException(getComponent(),
                    "Invalid cast type: " + refName);
        }

        if (tokenizer2.countTokens() == 2) {
            // castType
            tokenizer2.nextToken();
        }

        return tokenizer2.nextToken().trim();
    }

    /**
     * Generate a string that represents the type of an attribute (only if it
     * is a parameter).
     * @param attribute The attribute, which is typically a Parameter.
     * @return the code generation type if the attribute
     *  is a parameter, otherwise, null.
     */
    protected String _getRefType(Attribute attribute) {
        if (attribute instanceof Parameter) {
            return getCodeGenerator().codeGenType(
                    ((Parameter) attribute).getType());
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected members                 ////

    /** The name of the director in a format suitable to be used as a
     * variable name.
     */
    protected String _sanitizedDirectorName;
}
