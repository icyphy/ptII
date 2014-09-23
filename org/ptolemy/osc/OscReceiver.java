
/* An Input-agnostic OSC message receiver and decoder. 

Copyright (c) 1998-2013 The Regents of the University of California.
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
package org.ptolemy.osc;
 
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import oscP5.OscEventListener;
import oscP5.OscMessage;
import oscP5.OscP5;
import oscP5.OscStatus;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.DoubleToken;
import ptolemy.data.FloatToken;
import ptolemy.data.IntToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
<p> Receive Open Sound Control (OSC) Messages by listening to the specified port and
send each message to the corresponding output by name matching. This implies that
the user will have to manually add output ports to the receiver, according to the
messages they would like to listen for. Type of each port is determined by the first
token that is received at that port. Allowed types are int, double, float and string. If
input type is not specified by OSC, or if the defined type does not match any of the allowed
types, tokens will still be produced as ObjectTokens.

<p> See:
http://opensoundcontrol.org/ for the full OSC Specification

@see org.ptolemy.osc.OscSender
 @author Ilge Akkaya
 @version  
 @Pt.ProposedRating Red (ilgea)
 @Pt.AcceptedRating 
 */
public class OscReceiver extends TypedAtomicActor implements OscEventListener {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor
     *  @param trainingSequence The input string that the oracle is built from
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public OscReceiver(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name); 

        port = new Parameter(this, "port");
        port.setTypeEquals(BaseType.INT);
        port.setExpression("9000");

        _receivedTokens = new ConcurrentHashMap<>(); 
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /* The receive port - set to 57946 by default */
    public Parameter port;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute.  In this case, check
     * port. if port changes, need to reinitialize the OSCreceiver with that port
     * if possible
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == port) {
            int portValue = ((IntToken) port.getToken()).intValue();
            if (portValue != _port) {
                _constructOscReceiver(portValue);
            } 
        } else { 
            super.attributeChanged(attribute);
        }
    }

    public void fire() throws IllegalActionException {

        super.fire(); 
        // Send received tokens to corresponding output ports
        // Remove from list if ports do not exist
        boolean portTypeDefined = false;
        synchronized (_receivedTokens) {
            if (_receivedTokens.size() > 0) {
                if (_receivedTokens.keySet().size() > 0) {
                    for ( String identifier : _receivedTokens.keySet()) {
                        TypedIOPort destinationPort = (TypedIOPort) this.getPort(identifier);
                        
                        if (destinationPort == null || !destinationPort.isOutput()) {
                            _receivedTokens.remove(identifier);
                        } else {
                            if (destinationPort.getType() != null) {
                                portTypeDefined = true;
                            }
                            for (Object token : _receivedTokens.get(identifier)) { 
                                
                                if (token instanceof Double) {
                                    if (!portTypeDefined) {
                                        destinationPort.setTypeEquals(BaseType.DOUBLE);
                                        portTypeDefined = true;
                                    }
                                    destinationPort.send(0, new DoubleToken((Double)token));
                                } else if (token instanceof Integer) {
                                    if (!portTypeDefined) {
                                        destinationPort.setTypeEquals(BaseType.INT);
                                        portTypeDefined = true;
                                    }
                                    destinationPort.send(0, new IntToken((Integer)token));
                                } else if (token instanceof String) {
                                    if (!portTypeDefined) {
                                        destinationPort.setTypeEquals(BaseType.STRING);
                                        portTypeDefined = true;
                                    }
                                    destinationPort.send(0, new StringToken((String)token));
                                } else if (token instanceof Float) {
                                    if (!portTypeDefined) {
                                        destinationPort.setTypeEquals(BaseType.FLOAT);
                                        portTypeDefined = true;
                                    }
                                    destinationPort.send(0, new FloatToken((Float)token));
                                } else {
                                    if (!portTypeDefined) {
                                        destinationPort.setTypeEquals(BaseType.OBJECT);
                                        portTypeDefined = true;
                                    } 
                                    destinationPort.send(0, new ObjectToken(token));
                                }
                            }
                            _receivedTokens.get(identifier).clear(); // reset list
                        }
                    }
                } 
            } 
        }
    }  
    
    public boolean postfire() throws IllegalActionException {
        this._receivedTokens.clear();
        return super.postfire();
    }

    /* incoming osc message are forwarded to the oscEvent method. */
    public void oscEvent(OscMessage theMessage) {
        synchronized (this) { 
            try { 
                Object[] receivedData = theMessage.arguments();
                String addressPattern = theMessage.addrPattern(); 
                _processMessage(receivedData, addressPattern);

                // Note that fireAt() will modify the requested firing time if it is in the past.
                getDirector().fireAtCurrentTime(this);//fireAt(this, timeOfRequest);

            } catch (IllegalActionException e) {
                System.err.println("Error Processing OSCPacket:\n"
                        + theMessage.toString());
            }
        } 
    }

    public void oscStatus( OscStatus s) {

    }
    private void _constructOscReceiver(int port) throws IllegalActionException {

         oscP5 = new OscP5(this, port); 
        _port = port; // set port if successful


    }

    private void _processMessage(Object[] message, String address) { 

        synchronized ( _receivedTokens) {

            String[] addressBits = address.split("/");
            if (addressBits != null) {
                String identifier = addressBits[addressBits.length-1];
                if (identifier.length() > 0) {
                    List<Object> tokenList;
                    if (_receivedTokens.containsKey(identifier)) {
                        tokenList = _receivedTokens.get(identifier);
                    } else {
                        tokenList = new LinkedList<>();
                    }
                    for (int i = 0; i < message.length; i++) {
                        Object b = message[i];
                        if (b instanceof Double) {
                            tokenList.add((Double)b);
                        } else if (b instanceof Integer) {
                            tokenList.add((Integer)b);
                        } else if (b instanceof String) {
                            tokenList.add((String)b);
                        } else if (b instanceof Float) {
                            tokenList.add((Float)b);
                        } else {
                            tokenList.add(b);
                        } 
                    }
                    _receivedTokens.put(identifier, tokenList);
                }
            } 
        }
    }

    private OscP5 oscP5;  

    private int _port;  
    /**
     * Data structure that holds received tokens which are not yet sent to an output
     */
    private ConcurrentHashMap<String, List<Object>> _receivedTokens;  
}
