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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.data.ArrayToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;

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
        public Channel (Port portObject, int channel) {
            port = portObject;
            channelNumber = channel;
        }
        /** The port that contains this channel.
         */
        public Port port;
        
        /** The channel number of this channel.
         */
        public int channelNumber;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

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

    /** Get the component associated with this helper.
     * @return The associated component.
     */
    public NamedObj getComponent() {
        return _component;
    }

    /** Get the buffer size of the given port of this actor.
     *  @param port The given port.
     *  @return The buffer size of the given port of this actor.
     */
    public int getBufferSize(IOPort port) {
        return ((Integer) _bufferSizes.get(port)).intValue();
    }
    
    /** Get the offset in the buffer of a given port to which a token
     *  should be put.
     *  @param port The given port.
     *  @return The offset in the buffer of a given port to which a token
     *   should be put.
     */
    public int getOffset(IOPort port) {
        return ((Integer) _offsets.get(port)).intValue();
    }

    /** Return the value of the specified parameter of the associated actor.
     * @param parameterName The name of the parameter.
     * @return The value as a string.
     * @exception IllegalActionException If the parameter does not exist or
     *  does not have a value.
     */
    public String getParameterValue(String name) throws IllegalActionException {
        Attribute attribute = _component.getAttribute(name);
        if (attribute == null) {
            throw new IllegalActionException(_component, "No attribute named: "
                    + name);
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
     * @param name The name of the parameter or port
     * @return The reference to that parameter or port (a variable name,
     *  for example).
     * @exception IllegalActionException If the parameter or port does not
     *  exist or does not have a value.
     */
    public String getReference(String name) throws IllegalActionException {

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
                if (!channelAndOffset[0].equals("")) {
                    // Channel number specified. This must be a multiport.
                    result.append("[" + channelAndOffset[0] + "]");
                }
                if (!channelAndOffset[1].equals("") && getBufferSize(port) > 1) {
                    //int temp = _firingCount * DFUtilities.getRate(port);
                    //String offset = channelAndOffset[1] + " + " + temp;
                    //String temp = getOffset(port) + " + " + channelAndOffset[1];
                    int temp = getOffset(port) + (new Integer(channelAndOffset[1])).intValue();
                    temp = temp % getBufferSize(port);
                    result.append("[" + temp + "]");
                } else if (getBufferSize(port) > 1) {
                    // Did not specify offset, so the receiver buffer size is 1.
                    // This is multiple firing.
                    int temp = getOffset(port) % getBufferSize(port);
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
                if (channelAndOffset[0].equals("")) {
                    sinkChannels = getSinkChannels(port, 0);
                } else {
                    int channel = (new Integer(channelAndOffset[0])).intValue();
                    sinkChannels = getSinkChannels(port, channel);
                }
                for (int i = 0; i < sinkChannels.size(); i ++) {
                    IOPort sinkPort
                            = (IOPort) ((Channel) sinkChannels.get(i)).port;
                    int channel = ((Channel) sinkChannels.get(i)).channelNumber;
                    if (i != 0) {
                        result.append(" = ");
                    }
                    result.append(sinkPort.getFullName().replace('.', '_'));
                    if (sinkPort.isMultiport()) {
                        result.append("[" + channel + "]");
                    }
                    if (!channelAndOffset[1].equals("") && getBufferSize(port) > 1) {
                        // FIXME: This is a hack. It is using the offset of the
                        // output port to substitute the offsets of its downstream
                        // input ports.
                        //String temp = getOffset(port) + " + " + channelAndOffset[1];
                        int temp = getOffset(port) + (new Integer(channelAndOffset[1])).intValue();
                        temp = temp % getBufferSize(port);
                        result.append("[" + temp + "]");
                    } else if (getBufferSize(port) > 1) {
                        // Did not specify offset, so the receiver buffer size is 1.
                        // This is multiple firing.
                        int temp = getOffset(port) % getBufferSize(port);
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
     * @return The list.
     */
    public HashSet getReferencedParameter() {
        return _referencedParameters;
    }


    /** Return a list of channel objects that are the sink input ports
     *  given an output port and a given channel.
     *  Return a string that contains all the sink input ports
     * @param outputPort The given output port.
     * @param channelNumber The given channel number.
     * @return The list of channel objects that are the sink input ports
     *  of the given output port and channel.
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

        //StringBuffer result = new StringBuffer();
        
        boolean foundIt = false;
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
                        //sinkPorts.add(sinkPort);
                        //sinkChannels.add(new Integer(j));
                    }
                }

            }
        }
        return sinkChannels;
    }

    /** Get the size of a parameter. The size of a parameter
     *  is the length of its array if the parameter's type is array,
     *  and 1 otherwise.
     * @param name The name of the parameter.
     * @return The size of a parameter.
     * @exception IllegalActionException If no port or parameter of
     *  the given name is found.
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
            int nextPos = code.indexOf("$", currentPos + 1);
            if (nextPos < 0) {
                //currentPos is the last "$"
                nextPos = code.length();
            }
            String subcode = code.substring(currentPos + 1, nextPos);
            if (currentPos > 0 && code.charAt(currentPos - 1) == '\\') {
                // found "\$", do not make replacement.
                result.append("$");
                result.append(subcode);
                currentPos = nextPos;
                continue;
            }

            int flag = 0;
            boolean foundIt = false;
            StringTokenizer tokenizer = new StringTokenizer(subcode, "()", true);
            if (tokenizer.hasMoreTokens()) {
                // Do the trim so "$ ref (" can be recognized.
                String token = (tokenizer.nextToken()).trim();
                if ((token.equals("ref") || token.equals("val") || token.equals("actorSymbol")
                            || token.equals("size")) && tokenizer.hasMoreTokens()) {
                    if (token.equals("ref")) {
                        flag = 1;
                    } else if (token.equals("val")) {
                        flag = 2;
                    } else if (token.equals("size")){
                        flag = 3;
                    } else {
                        flag = 4;
                    }
                    String openParen = tokenizer.nextToken();
                    if (openParen.equals("(") && tokenizer.hasMoreTokens()) {
                        String name = tokenizer.nextToken();
                        if (name.equals("(") || name.equals(")")) {
                            throw new IllegalActionException(_component,
                                    "Illegal expression: $" + subcode);
                        }
                        if (tokenizer.hasMoreTokens()) {
                            String closeParen = tokenizer.nextToken();
                            if (closeParen.equals(")")) {
                                if (name.trim().equals("")) {
                                    throw new IllegalActionException(_component,
                                            "Illegal expression: $" + token + "("
                                            + name + ")");
                                }
                                name = name.trim();
                                if (flag == 1) {
                                    result.append(getReference(name));
                                } else if (flag == 2) {
                                    result.append(getParameterValue(name));
                                } else if (flag == 3) {
                                    result.append(getSize(name));
                                } else {
                                    result.append(_component.getFullName().replace('.', '_'));
                                    result.append("_" + name);
                                }
                                while (tokenizer.hasMoreTokens()) {
                                    result.append(tokenizer.nextToken());
                                }
                                foundIt = true;
                            }
                        }
                    }
                }
            }

            if (!foundIt) {
                result.append("$");
                result.append(subcode);
            }
            currentPos = nextPos;
        }
        return result.toString();
    }

    /** Reset the buffer sizes of all ports of the associated actor to the
     *  default value of 1.
     */
    public void resetBufferSizes() {
        Iterator inputPorts = ((Actor) _component).inputPortList().iterator();
        while (inputPorts.hasNext()) {
            IOPort inputPort = (IOPort) inputPorts.next();
            setBufferSize(inputPort, 1);
        }
        Iterator outputPorts = ((Actor) _component).outputPortList().iterator();
        while (outputPorts.hasNext()) {
            IOPort outputPort = (IOPort) outputPorts.next();
            setBufferSize(outputPort, 1);
        }
    }
    
    /** Reset the offsets of all ports of the associated actor to the
     *  default value of 0.
     */
    public void resetOffsets() {
        Iterator inputPorts = ((Actor) _component).inputPortList().iterator();
        while (inputPorts.hasNext()) {
            IOPort inputPort = (IOPort) inputPorts.next();
            setOffset(inputPort, 0);
        }
        Iterator outputPorts = ((Actor) _component).outputPortList().iterator();
        while (outputPorts.hasNext()) {
            IOPort outputPort = (IOPort) outputPorts.next();
            setOffset(outputPort, 0);
        }
    }
    
    /** Set the buffer size of a given port.
     *  @param port The given port.
     *  @param bufferSize The buffer size to be set to that port.
     */
    public void setBufferSize(IOPort port, int bufferSize) {
        _bufferSizes.put(port, new Integer(bufferSize));
    }

    /** Set the offset in a buffer of a given port to which a token should
     *  be put.
     *  @param port The given port.
     *  @param offset The offset to be set to the buffer of that port.
     */
    public void setOffset(IOPort port, int offset) {
        _offsets.put(port, new Integer(offset));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    
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

    /** The associated component. */
    private NamedObj _component;

    // Number of firings already fired. The default value is 0.
    //private int _firingCount;
    private HashMap _bufferSizes = new HashMap();

    // Total number of firings per iteration. The default value is 1.
    //private int _firingsPerIteration;
    private HashMap _offsets = new HashMap();

    // A set of parameters that have been referenced.
    private HashSet _referencedParameters = new HashSet();
}
