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

import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;

//////////////////////////////////////////////////////////////////////////
//// CodeGeneratorHelper
/**
 * Base class for code generator helper.
 * 
 * @author Christopher Brooks, Edward Lee, Jackie Leung, Gang Zhou, Rachel Zhou
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

    ///////////////////////////////////////////////////////////////////
    ////                      public methods                       ////
    
    /** Do nothing. Subclasses may extend this method to generate the fire 
     *  code of the associated component and append the code to the given
     *  string buffer.
     * @param stream The given string buffer.
     */
    public void generateFireCode(StringBuffer stream)
            throws IllegalActionException {
    }

    /** Set the _firingCount and _firingPerIteration to their default
     *  values. Subclasses may extend this method to generate the
     *  initialize code of the associated component and append the
     *  code to the given string buffer.
     * @param stream The given string buffer.
     */
    public void generateInitializeCode(StringBuffer stream)
            throws IllegalActionException {
        _firingCount = 0;
        _firingsPerIteration = 1;
    }

    /** Reset the _firingCount and _firingPerIteration to their default
     *  values. Subclasses may extend this method to generate 
     *  the wrapup code of the associated component and append the
     *  code to the give string buffer.
     * @param stream The given string buffer.
     */
    public void generateWrapupCode(StringBuffer stream)
            throws IllegalActionException {
        _firingCount = 0;
        _firingsPerIteration = 1;
    }
       
    /** Get the component associated with this helper.
     * @return The associated component.
     */
    public NamedObj getComponent() {
        return _component;
    }

    /** Get the number of times that the generateFireCode() method
     *  of this helper has been called.
     * @return The number of times that the generateFireCode() method
     *  of this helper has been called.
     */
    public int getFiringCount() {
        return _firingCount;
    }
    
    /** Get the number of times that the generateFireCode() method
     *  of this helper is called per iteration.
     * @return The number of times that the generateFireCode() method
     *  of this helper has been called.
     */
    public int getFiringsPerIteration() {
        return _firingsPerIteration;
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
        if (_component instanceof Actor) {
            Actor actor = (Actor) _component;
            StringTokenizer tokenizer = new StringTokenizer(name, "#%", true);
            if (tokenizer.countTokens() != 1 && tokenizer.countTokens() != 3
                    && tokenizer.countTokens() != 5) {
                throw new IllegalActionException(_component,
                        "Reference not found: " + name);
            }
            
            // Get the port name.
            String portName = tokenizer.nextToken().trim();
            
            Iterator inputPorts = actor.inputPortList().iterator();
            while (inputPorts.hasNext()) {
                IOPort port = (IOPort) inputPorts.next();
                // The channel is specified as $ref(port#channelNumber).
                if (port.getName().equals(portName)) {
                    result.append(port.getFullName().replace('.', '_'));
                    int[] channelAndOffset = _getChannelAndOffset(name);
                    if (channelAndOffset[0] >= 0) {
                        // Channel number specified. This must be a multiport.
                        result.append("[" + channelAndOffset[0] + "]");
                    }
                    if (channelAndOffset[1] >= 0) {
                        int offset = channelAndOffset[1] + 
                            _firingCount * port.getReceivers()[0].length;
                        result.append("[" + channelAndOffset[1] + "]");
                    } else if (_firingsPerIteration > 1) {
                        // Did not specify offset, so the receiver length is 1.
                        // This is multiple firing.
                        result.append("[" + _firingCount + "]");
                    }
                    return result.toString();
                }
            }
            
            Iterator outputPorts = actor.outputPortList().iterator();
            while (outputPorts.hasNext()) {
                IOPort port = (IOPort) outputPorts.next();
                if (port.getName().equals(portName)) {
                    Receiver[][] remoteReceivers 
                        = (port.getRemoteReceivers());
                    if (remoteReceivers.length == 0) {
                        // This channel of this output port doesn't have any sink.
                        result.append(_component.getFullName().replace('.', '_'));
                        result.append("_");
                        result.append(port.getName());
                        return result.toString();
                    }
                    
                    int[] channelAndOffset = _getChannelAndOffset(name);
                    if (channelAndOffset[0] < 0) {
                        result.append(getSinkChannels(port, 0));
                    } else {
                        result.append(getSinkChannels(port,
                                channelAndOffset[0]));
                    }
                    if (channelAndOffset[1] >= 0) {
                        result.append("[" + channelAndOffset[1] + "]");
                    } else if (_firingsPerIteration > 1) {
                        // Did not specify offset, so the receiver length is 1.
                        // This is multiple firing.
                        result.append("[" + _firingCount + "]");
                    }
                    return result.toString();
                }
            }
        }
        // Try if the name is a parameter.
        Attribute attribute = _component.getAttribute(name);
        
        if (attribute != null) {
            if (attribute instanceof Parameter) {
                _referencedParameters.add(attribute);
            }
            result.append(_component.getFullName().replace('.', '_'));
            result.append("_");
            result.append(name);
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
    

    /** Return a string that contains all the sink input ports
     *  and channels given an output port and a given channel.
     *  The returned string is in the form "inputPortName1[channelNumber1]
     *  = inputPortName2[channelNumber2] = ...". If the given output
     *  channel doesn't have a sink input, the method returns an empty string.
     * @param outputPort The given output port.
     * @param channelNumber The given channel number.
     * @return The string.
     */
    public String getSinkChannels(IOPort outputPort, int channelNumber) {
        
        Receiver[][] remoteReceivers 
            = (outputPort.getRemoteReceivers());
       
        if (remoteReceivers.length == 0) {
            // This is an escape method. This class will not call this
            // method if the output port does not have a remote receiver.
            return "";
        }
        
        StringBuffer result = new StringBuffer();
        boolean foundIt = false;
        for (int i = 0; i < remoteReceivers[channelNumber].length; i ++) {
            IOPort sinkPort = remoteReceivers[channelNumber][i].getContainer();
            Receiver[][] portReceivers = sinkPort.getReceivers();
            for (int j = 0; j < portReceivers.length; j ++) {
                for (int k = 0; k < portReceivers[j].length; k ++) {
                    if (remoteReceivers[channelNumber][i]
                            == portReceivers[j][k]) {
                        if (!foundIt) {
                            foundIt = true;
                        } else {
                            result.append(" = ");
                        }
                        result.append(sinkPort.getContainer()
                                .getFullName().replace('.', '_'));
                        result.append("_");
                        result.append(sinkPort.getName());
                        if (sinkPort.isMultiport()) {
                            result.append("[" + j + "]");
                        }
                        break;
                        //sinkPorts.add(sinkPort);
                        //sinkChannels.add(new Integer(j));
                    }
                }
                    
            }
        }
        return result.toString();
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

            boolean foundIt = false;
            StringTokenizer tokenizer = new StringTokenizer(subcode, "()", true);
            if (tokenizer.hasMoreTokens()) {
                // Do the trim so "$ ref (" can be recognized.
                String token = (tokenizer.nextToken()).trim();
                if (token.equals("ref") && tokenizer.hasMoreTokens()) {
                    String openParen = tokenizer.nextToken();
                    if (openParen.equals("(") && tokenizer.hasMoreTokens()) {
                        String name = tokenizer.nextToken();
                        if (name.equals("(") || name.equals(")")) {
                            // found "$ref((" or "$ref()"
                            throw new IllegalActionException(_component,
                                    "Illegal expression: $" + subcode);
                        }
                        if (tokenizer.hasMoreTokens()) {
                            String closeParen = tokenizer.nextToken();
                            if (closeParen.equals(")")) {
                                if (name.trim().equals("")) {
                                    throw new IllegalActionException(
                                            _component, "Illegal expression: $"
                                                    + subcode);
                                }
                                foundIt = true;
                                name = name.trim();
                                result.append(getReference(name));
                                while (tokenizer.hasMoreTokens()) {
                                    result.append(tokenizer.nextToken());
                                }
                            }
                        }
                    }
                } else if (token.equals("val") && tokenizer.hasMoreTokens()) {
                    String openParen = tokenizer.nextToken();
                    if (openParen.equals("(") && tokenizer.hasMoreTokens()) {
                        String macroName = tokenizer.nextToken();
                        if (macroName.equals("(") || macroName.equals(")")) {
                            // found "val((" or "val()"
                            throw new IllegalActionException(_component,
                                    "Illegal expression: " + subcode);
                        }
                        if (tokenizer.hasMoreTokens()) {
                            String closeParen = tokenizer.nextToken();
                            if (closeParen.equals(")")) {
                                if (macroName.trim().equals("")) {
                                    throw new IllegalActionException(
                                            _component, "Illegal expression: "
                                                    + "val(" + macroName + ")");
                                }
                                foundIt = true;
                                macroName = macroName.trim();
                                result.append(getParameterValue(macroName));
                                while (tokenizer.hasMoreTokens()) {
                                    result.append(tokenizer.nextToken());
                                }
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

    /** Set the number of times that the generateFireCode() method
     *  of this helper has been called.
     * @param firingCount The number of times that the generateFireCode()
     *  method of this helper has been called.
     */
    public void setFiringCount(int firingCount) {
        _firingCount = firingCount;
    }
    
    /** Set the number of times that the generateFireCode() method
     *  of this helper is called per iteration.
     * @param firingsPerIteration The number of times that the
     *  generateFireCode() method of this helper is called per iteration.
     */
    public void setFiringsPerIteration(int firingsPerIteration) {
        _firingsPerIteration = firingsPerIteration;
    }
    
    
    /////////////////////////////////////////////////////////////
    ////               private methods                       ////
    
    /** Return the channel number and offset given in a string.
     *  The result is an integer array of length 2. The first element
     *  indicates the channel number, the second the offset. If either
     *  element is -1, it means that channel/offset is not specified.
     * @param name The given string.
     * @return An integer array of length 2, indicating the channel
     *  number and offset.
     * @throws IllegalActionException If the channel number or offset
     *  specified in the given string is illegal.
     */
    private int[] _getChannelAndOffset(String name)
            throws IllegalActionException {
        int[] result = {-1, -1};
        StringTokenizer tokenizer = new StringTokenizer(name, "#%", true);
        tokenizer.nextToken();
        if (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (token.equals("#")){
                int channel = new Integer(tokenizer.nextToken().trim())
                        .intValue();
                if (channel < 0) {
                    throw new IllegalActionException(_component,
                            "Invalid channel number in " + name);
                }
                result[0] = channel;
                if (tokenizer.hasMoreTokens()) {
                    if (tokenizer.nextToken().equals("%")) {
                        int offset = new Integer(tokenizer.nextToken().trim())
                                .intValue();
                        if (offset < 0) {
                            throw new IllegalActionException(_component,
                                    "Invalid offset in" + name);
                        }
                        result[1] = offset;
                    }
                }
            } else if (token.equals("%")) {
                int offset = new Integer(tokenizer.nextToken().trim())
                        .intValue();
                if (offset < 0 || tokenizer.hasMoreTokens()) {
                    throw new IllegalActionException(_component,
                            "Invalid offset in " + name);
                }
                result[1] = offset;
            }
        }
        return result;
    }
    
    
    ///////////////////////////////////////////////////////////////////
    ////                      private variables                    ////

    /** The associated component. */
    private NamedObj _component;
    // Number of firings already fired. The default value is 0.
    private int _firingCount;
    // Total number of firings per iteration. The default value is 1.
    private int _firingsPerIteration;
    // A set of parameters that have been referenced.
    private HashSet _referencedParameters = new HashSet();
}