/* Base class for code generator helper.

Copyright (c) 2005 The Regents of the University of California.
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

package ptolemy.codegen.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.data.ArrayToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

//////////////////////////////////////////////////////////////////////////
//// CodeGeneratorHelper
/**
 * Base class for code generator helper.
 *
 * @author Ye Zhou, Edward A. Lee, Contributors: Gang Zhou, Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 5.0
 * @Pt.ProposedRating Red (eal)
 * @Pt.AcceptedRating Red (eal)
 */
public class CodeGeneratorHelper implements ActorCodeGenerator {

    /** Construct the code generator helper associated with the given component.
     *  @param component The associated componenet.
     */
    public CodeGeneratorHelper(NamedObj component) {
        _component = component;
    }

    /////////////////////////////////////////////////////////////////////
    ////                      public inner classes                   ////
    
    /** A class that defines a channel object. A channel object is
     *  specified by its port and its channel index in that port.
     */
    public class Channel {
        /** Construct the channel with the given port and channel number.
         * @param portObject The given port.
         * @param channel The channel number of this object in the given port.
         */
        public Channel (IOPort portObject, int channel) {
            port = portObject;
            channelNumber = channel;
        }
        /** The port that contains this channel.
         */
        public IOPort port;
        
        /** The channel number of this channel.
         */
        public int channelNumber;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create the channel map, which is associated with this helper object.
     *  A key of the map is an IOPort of the actor. The corresponding value
     *  is an array of Channel objects. The i-th channel object corresponds
     *  to the i-th channel of that IOPort. This method is used to maintain
     *  a hashmap of channels of the actor. The channel objects in the map
     *  is used to keep track of the offsets in their buffer.
     */
    public void createBufferAndOffsetMap() {
        Set ioPortsSet = new HashSet();
        ioPortsSet.addAll(((Actor) _component).inputPortList());
        ioPortsSet.addAll(((Actor) _component).outputPortList());
        Iterator ioPorts = ioPortsSet.iterator();
        while (ioPorts.hasNext()) {
            IOPort port = (IOPort) ioPorts.next();
            int length = port.getWidth();
            if (length == 0) {
                length = 1;
            }
            int[] bufferSizes = new int[length];
            _bufferSizes.put(port, bufferSizes);
            Object[] offsets = new Object[length];
            _offsets.put(port, offsets);
        }
    }
    
    /** Do nothing. Subclasses may extend this method to generate the fire
     *  code of the associated component and append the code to the given
     *  string buffer.
     *  @param stream The given string buffer.
     *  @exception illegalActionException Subclasses may throw it.
     */
    public void generateFireCode(StringBuffer stream)
            throws IllegalActionException {
    }

    /** Return an empty string. Subclasses may extend this method to
     *  generate the initialize code of the associated component and
     *  append the code to the given string buffer.
     *  @param stream The given string buffer.
     *  @exception IllegalActionException Subclass may throw it.
     */
    public String generateInitializeCode()
            throws IllegalActionException {
        resetOffsets();
        return "";
    }

    /** Do nothing. Subclasses may extend this method to generate
     *  the wrapup code of the associated component and append the
     *  code to the give string buffer.
     *  @param stream The given string buffer.
     *  @exception IllegalActionException Subclasses may throw it.
     */
    public void generateWrapupCode(StringBuffer stream)
            throws IllegalActionException {
    }

    /** Return the buffer size of a given port, which is the maximum of
     *  the bufferSizes of all channels of the given port.
     *  @param port The given port.
     *  @return The buffer size of the given port.
     *  @exception IllegalActionException If the getBufferSize(IOPort, int)
     *   method throws it.
     */
    public int getBufferSize(IOPort port) 
            throws IllegalActionException {
        int bufferSize = 1;
        if (port.getContainer() == _component) {
            for (int i = 0; i < port.getWidth(); i ++) {
                int channelBufferSize = getBufferSize(port, i);
                if (channelBufferSize > bufferSize) {
                    bufferSize = channelBufferSize;
                }
            }
        } else {
            CodeGeneratorHelper actorHelper = (CodeGeneratorHelper)
                    _getHelper((NamedObj) port.getContainer());
            bufferSize = actorHelper.getBufferSize(port);
        }
        return bufferSize;
    }

    /** Get the buffer size of the given port of this actor.
     *  @param port The given port.
     *  @return The buffer size of the given port of this actor.
     */
    public int getBufferSize(IOPort port, int channelNumber)
            throws IllegalActionException {
        if (port.getContainer() == _component) {
            return ((int[]) _bufferSizes.get(port))[channelNumber];
        } else {
            CodeGeneratorHelper actorHelper = (CodeGeneratorHelper)
                    _getHelper((NamedObj) port.getContainer());
            return actorHelper.getBufferSize(port, channelNumber);
        }
    }

    /** Get the component associated with this helper.
     *  @return The associated component.
     */
    public NamedObj getComponent() {
        return _component;
    }

    /** Get the files needed by the code generated from this helper class.
     *  This base class returns an empty set.
     *  @return A set of strings that are libraries needed by the code
     *  generated from this helper class.
     */
    public Set getIncludingFiles() {
        Set files = new HashSet();
        return files;
    }

    /** Get the offset in the buffer of a given channel to which a token
     *  should be put. The channel is given by its containing port and
     *  the channel number in that port. The default value is 0.
     *  @param port The given port.
     *  @param channelNumber The given channel number.
     *  @return The offset in the buffer of a given channel to which a token
     *   should be put.
     */
    public Object getOffset(IOPort port, int channelNumber) {
        return ((Object[]) _offsets.get(port))[channelNumber];
    }

    /** Return the value of the specified parameter of the associated actor.
     *  @param parameterName The name of the parameter.
     *  @return The value as a string.
     *  @exception IllegalActionException If the parameter does not exist or
     *  does not have a value.
     */
    public String getParameterValue(String name)
            throws IllegalActionException {
        Attribute attribute = _component.getAttribute(name);
        if (attribute == null) {
            throw new IllegalActionException(
                    _component, "No attribute named: " + name);
        }
        if (attribute instanceof Variable) {
            // FIXME: need to ensure that the returned string
            // is correct syntax for the target language.
            return ((Variable) attribute).getToken().toString();
        } else if (attribute instanceof Settable) {
            return ((Settable) attribute).getExpression();
        }
        // FIXME: Are there any other values that a
        // parameter might have?
        throw new IllegalActionException(_component,
                "Attribute does not have a value: " + name);
    }


    /** Return the reference to the specified parameter or port of the
     *  associated actor. For a parameter, the returned string is in
     *  the form "fullName_parameterName". For a port, the returned string
     *  is in the form "fullName_portName[channelNumber][offset]", if
     *  any channel number or offset is given.
     *  @param name The name of the parameter or port
     *  @return The reference to that parameter or port (a variable name,
     *   for example).
     *  @exception IllegalActionException If the parameter or port does not
     *   exist or does not have a value.
     */
    public String getReference(String name) throws IllegalActionException {

        name = processCode(name);
        StringBuffer result = new StringBuffer();
        Actor actor = (Actor) _component;
        StringTokenizer tokenizer = new StringTokenizer(name, "#,", true);
        if (tokenizer.countTokens() != 1 && tokenizer.countTokens() != 3
                && tokenizer.countTokens() != 5) {
            throw new IllegalActionException(_component,
                    "Reference not found: " + name);
        }

        // Get the referenced name.
        String refName = tokenizer.nextToken().trim();

        Iterator inputPorts = actor.inputPortList().iterator();
        while (inputPorts.hasNext()) {
            IOPort port = (IOPort) inputPorts.next();
            // The channel is specified as $ref(port#channelNumber).
            if (port.getName().equals(refName)) {
                result.append(port.getFullName().replace('.', '_'));
                String[] channelAndOffset = _getChannelAndOffset(name);
                int channelNumber = 0;
                if (!channelAndOffset[0].equals("")) {
                    // Channel number specified. This must be a multiport.
                    result.append("[" + channelAndOffset[0] + "]");
                    channelNumber = new Integer(channelAndOffset[0]).intValue();
                }
                if (!channelAndOffset[1].equals("")
                            && getBufferSize(port) > 1) {
                    String temp = "";
                    if (getOffset(port, channelNumber) instanceof Integer) {
                        int offset = ((Integer) getOffset(port, channelNumber))
                                .intValue();
                        offset = offset
                                + (new Integer(channelAndOffset[1])).intValue();
                        offset = offset % getBufferSize(port, channelNumber);
                        temp = new Integer(offset).toString();
                    } else {
                        // Note: This assumes the director helper will increase
                        // the buffer size of the channel to the power of two.
                        // Otherwise, use "%" instead.
                        // FIXME: We haven't check if modulo is 0. But this should
                        // never happen. For offsets that need to be represented
                        // by string expression, getBufferSize(port, channelNumber)
                        // will always return a value at least 2.
                        int modulo = getBufferSize(port, channelNumber) - 1;
                        temp = (String) getOffset(port, channelNumber);
                        temp = "(" + temp
                            + (new Integer(channelAndOffset[1])).intValue()
                            + ")&" + modulo; 
                    }
                    result.append("[" + temp + "]");
                } else if (getBufferSize(port) > 1) {
                    // Did not specify offset, so the receiver buffer size is 1.
                    // This is multiple firing.
                    String temp = "";
                    if (getOffset(port, channelNumber) instanceof Integer) {
                        int offset = ((Integer) getOffset(port, channelNumber))
                                .intValue();
                        offset = offset % getBufferSize(port, channelNumber);
                        temp = new Integer(offset).toString();
                    } else {
                        int modulo = getBufferSize(port, channelNumber) - 1;
                        temp = (String) getOffset(port, channelNumber);
                        temp = temp + "&" + modulo;
                    }
                    result.append("[" + temp + "]");
                }
                return result.toString();
            }
        }

        Iterator outputPorts = actor.outputPortList().iterator();
        while (outputPorts.hasNext()) {
            IOPort port = (IOPort) outputPorts.next();
            if (port.getName().equals(refName)) {
                Receiver[][] remoteReceivers
                    = (port.getRemoteReceivers());
                if (remoteReceivers.length == 0) {
                    // This channel of this output port doesn't have any sink.
                    result.append(_component.getFullName().replace('.', '_'));
                    result.append("_");
                    result.append(port.getName());
                    return result.toString();
                }

                String[] channelAndOffset = _getChannelAndOffset(name);
                
                List sinkChannels = new LinkedList();
                int channelNumber = 0;
                if (!channelAndOffset[0].equals("")) {
                    channelNumber
                            = (new Integer(channelAndOffset[0])).intValue();
                }
                sinkChannels = getSinkChannels(port, channelNumber);
                for (int i = 0; i < sinkChannels.size(); i ++) {
                    Channel channel = (Channel) sinkChannels.get(i);
                    IOPort sinkPort = (IOPort) channel.port;
                    int sinkChannelNumber = channel.channelNumber;
                    if (i != 0) {
                        result.append(" = ");
                    }
                    result.append(sinkPort.getFullName().replace('.', '_'));
                    if (sinkPort.isMultiport()) {
                        result.append("[" + sinkChannelNumber + "]");
                    }
                    int sinkPortBufferSize = getBufferSize(sinkPort);
                    if (!channelAndOffset[1].equals("")
                            && getBufferSize(sinkPort) > 1) {
                        // Specified offset.
                        String temp = "";
                        if (getOffset(port, 0) instanceof Integer) {
                            int offset
                                = ((Integer)(getOffset(port, channelNumber))).intValue()
                                + (new Integer(channelAndOffset[1])).intValue();
                            offset = offset % getBufferSize(sinkPort, sinkChannelNumber);
                            temp = new Integer(offset).toString();
                        } else {
                            int modulo = getBufferSize(sinkPort, sinkChannelNumber) - 1;
                            temp = "(" + (String) getOffset(port, channelNumber)
                                + (new Integer(channelAndOffset[1])).intValue()
                                + ")&" + modulo;
                        }
                        result.append("[" + temp + "]");
                    } else if (getBufferSize(sinkPort) > 1) {
                        // Did not specify offset, so the receiver buffer size is 1.
                        // This is multiple firing.
                        String temp = "";
                        if (getOffset(port, channelNumber) instanceof Integer) {
                            int offset = ((Integer) getOffset(port, 0)).intValue();
                            offset = offset % getBufferSize(sinkPort, sinkChannelNumber);
                            temp = new Integer(offset).toString();
                        } else {
                            int modulo = getBufferSize(sinkPort, sinkChannelNumber) - 1;
                            temp = (String) getOffset(port, channelNumber) + "&"
                                    + modulo;
                        }
                        result.append("[" + temp + "]");
                    }
                }
                return result.toString();
            }
        }

        // Try if the name is a parameter.
        Attribute attribute = _component.getAttribute(refName);
        if (attribute != null) {
            if (attribute instanceof Parameter) {
                _referencedParameters.add(attribute);
            }
            result.append(attribute.getFullName().replace('.', '_'));
            String[] channelAndOffset = _getChannelAndOffset(name);
            if (!channelAndOffset[0].equals("")) {
                throw new IllegalActionException(_component,
                        "a parameter cannot have channel number.");
            }
            if (!channelAndOffset[1].equals("")) {
                result.append("[" + channelAndOffset[1] + "]");
            }
            return result.toString();
        }

        throw new IllegalActionException(_component, "Reference not found: "
                + name);
    }


    /** Return a list that contains the parameters referenced in the code.
     *  @return The list.
     */
    public HashSet getReferencedParameter() {
        return _referencedParameters;
    }


    /** Return a list of channel objects that are the sink input ports given
     *  an output port and a given channel. Note the returned channels are
     *  newly created objects and therefore not associated with the helper
     *  class.
     *  @param outputPort The given output port.
     *  @param channelNumber The given channel number.
     *  @return The list of channel objects that are the sink channels
     *   of the given output channel.
     */
    public List getSinkChannels(IOPort outputPort, int channelNumber) {

        List sinkChannels = new LinkedList();
        Receiver[][] remoteReceivers
            = (outputPort.getRemoteReceivers());

        if (remoteReceivers.length == 0) {
            // This is an escape method. This class will not call this
            // method if the output port does not have a remote receiver.
            return sinkChannels;
        }

        for (int i = 0; i < remoteReceivers[channelNumber].length; i ++) {
            IOPort sinkPort = remoteReceivers[channelNumber][i].getContainer();
            Receiver[][] portReceivers = sinkPort.getReceivers();
            for (int j = 0; j < portReceivers.length; j ++) {
                for (int k = 0; k < portReceivers[j].length; k ++) {
                    if (remoteReceivers[channelNumber][i]
                            == portReceivers[j][k]) {
                        Channel sinkChannel = new Channel(sinkPort, j);
                        sinkChannels.add(sinkChannel);
                        break;
                    }
                }
            }
        }
        return sinkChannels;
    }

    /** Get the size of a parameter. The size of a parameter
     *  is the length of its array if the parameter's type is array,
     *  and 1 otherwise.
     *  @param name The name of the parameter.
     *  @return The size of a parameter.
     *  @exception IllegalActionException If no port or parameter of
     *   the given name is found.
     */
    public int getSize(String name)
            throws IllegalActionException {
        int size = 1;
        // Try if the name is a parameter.
        Attribute attribute = _component.getAttribute(name);
        if (attribute != null) {
            // FIXME:  Could it be something other than variable?
            if (attribute instanceof Variable) {
                Token token = ((Variable)attribute).getToken();
                if (token instanceof ArrayToken) {
                    return ((ArrayToken)token).length();
                }
                return 1;
            }
        }
        throw new IllegalActionException(_component,
                "Attribute not found: " + name);
    }

    /** Process the specified code, replacing macros with their values.
     * @param code The code to process.
     * @return The processed code.
     * @exception IllegalActionException if illegal macro names are found.
     */
    public String processCode(String code) throws IllegalActionException {

        StringBuffer result = new StringBuffer();
        int currentPos = code.indexOf("$");
        if (currentPos < 0) {
            // No "$" in the string
            return code;
        }

        result.append(code.substring(0, currentPos));
        while (currentPos < code.length()) {
            int openParenIndex = code.indexOf("(", currentPos + 1);
            int closeParenIndex = _findCloseParen(code, openParenIndex);
            if (closeParenIndex < 0) {
                // No matching close parenthesis is found.
                result.append(code.substring(currentPos));
                return result.toString();
            }
            int nextPos = code.indexOf("$", closeParenIndex + 1);
            if (nextPos < 0) {
                //currentPos is the last "$"
                nextPos = code.length();
            }
            String subcode = code.substring(currentPos, nextPos);
            if (currentPos > 0 && code.charAt(currentPos - 1) == '\\') {
                // found "\$", do not make replacement.
                result.append(subcode);
                currentPos = nextPos;
                continue;
            }

            boolean foundIt = false;
            String macro = code.substring(currentPos + 1, openParenIndex);
            macro = macro.trim();
            if ((macro.equals("ref") || macro.equals("val") 
                    || macro.equals("actorSymbol") || macro.equals("size"))) {
                String name = code.substring(openParenIndex + 1, closeParenIndex);    
                name = name.trim();
                if (macro.equals("ref")) {
                    result.append(getReference(name));
                } else if (macro.equals("val")) {
                    result.append(getParameterValue(name));
                } else if (macro.equals("size")){
                    result.append(getSize(name));
                } else if (macro.equals("actorSymbol")){
                    result.append(_component.getFullName()
                            .replace('.', '_'));
                    result.append("_" + name);
                }
                foundIt = true;
                result.append(code.substring(closeParenIndex + 1, nextPos));
            }
            if (!foundIt) {
                result.append(subcode);
            }
            currentPos = nextPos;
        }
        return result.toString();
    }

    /** Reset the offsets of all channels of the associated actor to the
     *  default value of 0.
     */
    public void resetOffsets() {
        Set inputAndOutputPortsSet = new HashSet();
        inputAndOutputPortsSet.addAll(((Actor) _component).inputPortList());
        inputAndOutputPortsSet.addAll(((Actor) _component).outputPortList());
        Iterator inputAndOutputPorts = inputAndOutputPortsSet.iterator();
        while (inputAndOutputPorts.hasNext()) {
            IOPort port = (IOPort) inputAndOutputPorts.next();
            for (int i = 0; i < port.getWidth(); i ++) {
                setOffset(port, i, new Integer(0));
            }
        }
    }
    
    /** Set the buffer size of a given port.
     *  @param port The given port.
     *  @param bufferSize The buffer size to be set to that port.
     */
    public void setBufferSize(IOPort port, int channelNumber, int bufferSize) {
        int[] bufferSizes =(int[]) _bufferSizes.get(port);
        bufferSizes[channelNumber] = bufferSize;
        // perhaps this step is redundant?
        _bufferSizes.put(port, bufferSizes);
    }

    /** Set the code generator associated with this helper class.
     *  @param codeGenerator The code generator associated with this
     *   helper class.
     */
    public void setCodeGenerator(CodeGenerator codeGenerator) {
        _codeGenerator = codeGenerator;
    }

    /** Set the offset in a buffer of a given channel to which a token should
     *  be put.
     *  @param channel The given channel.
     *  @param offset The offset to be set to the buffer of that channel.
     */
    public void setOffset(IOPort port, int channelNumber, Object offset) {
        Object[] offsets = (Object[]) _offsets.get(port);
        offsets[channelNumber] = offset;
        _offsets.put(port, offsets);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     protected methods.                    ////

    protected ComponentCodeGenerator _getHelper(NamedObj component)
            throws IllegalActionException {
        return _codeGenerator._getHelper(component);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Find the paired close parenthesis given a string and an index
     *  which is the position of an open parenthesis. Return -1 if no
     *  paired close parenthesis is found.
     *  @param string The given string.
     *  @param pos The given index.
     *  @return The index which indicates the position of the paired
     *   close parenthesis of the string.
     *  @exception IllegalActionException If the character at the
     *   given position of the string is not an open parenthesis.
     */
    private int _findCloseParen(String string, int pos)
            throws IllegalActionException {
        if (string.charAt(pos) != '(') {
            throw new IllegalActionException(_component,
                    "The character at index " + pos + " of string: "
                    + string + " is not a open parenthesis.");
        }
        int nextOpenParen = string.indexOf("(", pos + 1);
        if (nextOpenParen < 0) {
            nextOpenParen = string.length();
        }
        int nextCloseParen = string.indexOf(")", pos);
        if (nextCloseParen < 0) {
            return -1;
        }
        int count = 1;
        int beginIndex = pos + 1;
        while (beginIndex > 0) {
            if (nextCloseParen < nextOpenParen) {
                count --;
                if (count == 0) {
                    return nextCloseParen;
                }
                beginIndex = nextCloseParen + 1;
                nextCloseParen = string.indexOf(")", beginIndex);
                if (nextCloseParen < 0) {
                    return -1;
                }
            }
            if (nextOpenParen < nextCloseParen) {
                count ++;
                beginIndex = nextOpenParen + 1;
                nextOpenParen = string.indexOf("(", beginIndex);
                if (nextOpenParen < 0) {
                    nextOpenParen = string.length();
                }
            }
        }
        return -1;
    }

    /** Return the channel number and offset given in a string.
     *  The result is an integer array of length 2. The first element
     *  indicates the channel number, the second the offset. If either
     *  element is -1, it means that channel/offset is not specified.
     * @param name The given string.
     * @return An integer array of length 2, indicating the channel
     *  number and offset.
     * @exception IllegalActionException If the channel number or offset
     *  specified in the given string is illegal.
     */
    private String[] _getChannelAndOffset(String name)
            throws IllegalActionException {
        String[] result = {"", ""};
        StringTokenizer tokenizer = new StringTokenizer(name, "#,", true);
        tokenizer.nextToken();
        if (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (token.equals("#")) {
                result[0] = tokenizer.nextToken().trim();
                if (tokenizer.hasMoreTokens()) {
                    if (tokenizer.nextToken().equals(",")) {
                        result[1] = tokenizer.nextToken().trim();
                    }
                }
            } else if (token.equals(",")) {
                result[1] = tokenizer.nextToken().trim();
            }
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** A hashmap that keeps track of the bufferSizes of each channel
     *  of the actor.
     */
    private HashMap _bufferSizes = new HashMap();

    /** The code generator that contains this helper class.
     */
    private CodeGenerator _codeGenerator;

    /** The associated component. */
    private NamedObj _component;

    /** A hashmap that keeps track of the offsets of each channel of
     *  the actor.
     */
    private HashMap _offsets = new HashMap();

    /** A set of parameters that have been referenced.
     */
    private HashSet _referencedParameters = new HashSet();
}
