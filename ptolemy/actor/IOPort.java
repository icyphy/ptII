/* Abstract class for a port that can serve as an input, output, or both.

 Copyright (c) 1997- The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)

*/

package pt.actors;

import pt.kernel.*;
import pt.data.*;

import java.util.Enumeration;
import java.util.NoSuchElementException;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// IOPort
/** 
Abstract class for a port that can serve as an input, output, or both.
This class defines the interface for exchanging data between entities.
It implements sending of data (the output mechanism), but not receiving
of data (the input mechanism).  The input mechanism is implemented in
derived classes in various ways that support distinct communication
styles.  Sending data is accomplished via the
put() method, which simply invokes the receive() method of all ports
that are connected to this port and that identify themselves as input
ports.  A port identifies itself as an input port by returning true
in its isInput() method.  When sending data, if more than one input
port is found, then the token being sent is cloned so that each
recipient gets a distinct copy of the token.

@author Edward A. Lee
@version $Id$
*/
public abstract class IOPort extends ComponentPort {

    /** Construct a port with no containing entity, no name, that is
     *  neither an input nor an output.
     */	
    public IOPort() {
        super();
    }

    /** Construct a port with a containing entity and a name that is
     *  neither an input nor an output.
     *  @param container
     *  @param name
     *  @exception IllegalActionException name argument is null.
     *  @exception NameDuplicationException Name coincides with
     *   an element already on the port list of the parent.
     */	
    public IOPort(ComponentEntity container, String name) 
             throws IllegalActionException, NameDuplicationException {
	super(container,name);
    }

    /** Construct a port with a containing entity and a name that is
     *  either an input or an output or both, depending on the third
     *  and fourth arguments.
     *  @param container
     *  @param name
     *  @param isinput
     *  @param isoutput
     *  @exception IllegalActionException name argument is null.
     *  @exception NameDuplicationException Name coincides with
     *   an element already on the port list of the parent.
     */	
    public IOPort(ComponentEntity container, String name,
             boolean isinput, boolean isoutput) 
             throws IllegalActionException, NameDuplicationException {
	this(container,name);
        makeInput(isinput);
        makeOutput(isoutput);
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Deeply enumerate the input ports connected to this port. Use the 
     *  cached result if it is still valid.
     *  @see ComponentPort#deepConnectedPorts.
     */	
    public Enumeration deepConnectedInputPorts() {
        synchronized(workspace()) {
            if (_deepConnectedInputPortsVersion == workspace().getVersion()) {
                // Cache is valid.  Use it.
                return _deepConnectedInputPorts.elements();
            }
            LinkedList result = new LinkedList();
            Enumeration ports = deepConnectedPorts();
            while(ports.hasMoreElements()) {
                IOPort port = (IOPort)ports.nextElement();
                if(port.isInput()){
                    result.insertLast(port);
                }
            }
            _deepConnectedInputPorts = result;
            _deepConnectedInputPortsVersion = workspace().getVersion();
            return _deepConnectedInputPorts.elements();
        }
    }
                    
    /** Deeply enumerate the output ports connected to this port. Use the 
     *  cached result if it is still valid.
     *  @see ComponentPort#deepConnectedPorts.
     */	
    public Enumeration deepConnectedOutputPorts() {
        synchronized(workspace()) {
            if (_deepConnectedOutputPortsVersion == workspace().getVersion()) {
                // Cache is valid.  Use it.
                return _deepConnectedOutputPorts.elements();
            }
            LinkedList result = new LinkedList();
            Enumeration ports = deepConnectedPorts();
            while(ports.hasMoreElements()) {
                IOPort port = (IOPort)ports.nextElement();
                if(port.isOutput()){
                    result.insertLast(port);
                }
            }
            _deepConnectedOutputPorts = result;
            _deepConnectedOutputPortsVersion = workspace().getVersion();
            return _deepConnectedOutputPorts.elements();
        }
    }    

    /** Get all tokens from the port. The tokens must have been
     *  previously received via the receive() method. Different 
     *  implementations of this method will react differently if no token
     *  is available.  Implementations should throw an exception
     *  if the port is not an input port.
     *  @exception IllegalActionException Port is not an input.
     *  @exception NoSuchElementException No token to get.
     */	
    public abstract Token[] get() throws NoSuchElementException, 
	    IllegalActionException;

    /** Get a token received from a particular output port. The token
     *  must have been previously received via the receive() method. 
     *  The output port must be connected to this port, otherwise an 
     *  Exception is thrown. Different implementations 
     *  of this method will react differently if no token
     *  is available.  Implementations should throw an exception
     *  if the port is not an input port.
     *  @exception IllegalActionException Port is not an input.
     *  @exception NoSuchElementException No token to get.
     */	
    public abstract Token get(IOPort fromport) throws NoSuchElementException, 
	    IllegalActionException;

    /** Return true if the port is an input.  An input port is one
     *  that is capable of receiving tokens from another port 
     *  via the receive() method and delivering the tokens to an
     *  entity via the get() method.
     */	
    public boolean isInput() {
        return _isinput;
    }

    /** Return true if the port is an output.  An output port is one
     *  that is capable of sending tokens to another port 
     *  via the put() method.
     */	
    public boolean isOutput() {
        return _isoutput;
    }

    /** If the argument is true, make the port an input port.
     *  That is, make it capable of receiving tokens from another port 
     *  via the receive() method and delivering the tokens to an
     *  entity via the get() method.  If the argument is false,
     *  make the port not an input port.
     */	
    public void makeInput(boolean isinput) {
        _isinput = isinput;
    }

    /** If the argument is true, make the port an output port.
     *  That is, make it capable of sending tokens to another port 
     *  via the send() method.  If the argument is false,
     *  make the port not an output port.
     */	
    public void makeOutput(boolean isoutput) {
        _isoutput = isoutput;
    }

    /** Send a token to all connected ports that identify themselves as
     *  input ports.  The transfer is accomplished by calling the receive()
     *  method of the destination ports.  If there is more than one
     *  destination port, then clone the token so that each destination
     *  receives a distinct instance of the token.  The first recipient
     *  will receive the instance that is passed as an argument here.
     *  @param token The token to send
     *  @exception CloneNotSupportedException The token cannot be cloned
     *   and there is more than one destination.
     *  @exception IllegalActionException The port is not an output port.
     */	
    public void put(Token token) throws 
	    CloneNotSupportedException, IllegalActionException {
        if (!isOutput()) {
            throw new IllegalActionException(this,
                   "Attempt to send data from a port that is not an output.");
        }
        Enumeration ports = deepConnectedInputPorts();
        boolean first = true;
        while( ports.hasMoreElements() ) {
            IOPort port = (IOPort)ports.nextElement();
            if (first) {
                port.receive(token, this);
                first = false;
            } else {
                port.receive((Token)(token.clone()), this);
            }
        }
    }

    /** Send a token to a connected input port.
     *  The transfer is accomplished by calling the receive() method of
     *  the destination port.
     *  @param token The token to send
     *  @exception IllegalActionException This port is not an output port, 
     *             or the desination port is not an input port.
     */	
    public void put(Token token, IOPort toport) throws 
	    IllegalActionException {
        if (!isOutput()) {
            throw new IllegalActionException(this,
                   "Attempt to send data from a port that is not an output.");
        }
        if (!isDeeplyConnected(toport)) {
            throw new IllegalActionException(this,
                   "Attempt to send data to a port that is not an input.");
        }
        toport.receive(token, this);
    }

    /** FIXME: Do we need this?
     *  Send a token to all ports connected through the specified relation
     *  that identify themselves as input ports.  
     *  The transfer is accomplished by calling the receive()
     *  method of the destination ports.  If there is more than one
     *  destination port, then clone the token so that each destination
     *  receives a distinct instance of the token.  The first recipient
     *  will receive the instance that is passed as an argument here.
     *  @param token The token to send
     *  @param relation The ports connected only through this relation are 
     *  considered.
     *  @exception CloneNotSupportedException The token cannot be cloned
     *   and there is more than one destination.
     *  @exception IllegalActionException The port is not an output port.
     */	
    public void put(Token token, IORelation relation) throws 
	    CloneNotSupportedException, IllegalActionException {
        if (!isOutput()) {
            throw new IllegalActionException(this,
                   "Attempt to send data from a port that is not an output.");
        }
        Enumeration ports = relation.deepLinkedPorts();
        boolean first = true;
        while( ports.hasMoreElements() ) {
            IOPort port = (IOPort)ports.nextElement();
            if (port.isInput()) {
                if (first) {
                    port.receive(token, this);
                    first = false;
                } else {
                    port.receive((Token)(token.clone()), this);
                }
            }
        }
    }    

    /** Receive a token from another port.  Different implementations
     *  of this method will react differently if, for example, there
     *  is no room for the token, or the owning entity is not ready
     *  to receive a token.  Implementations should throw an exception
     *  if the port is not an input port.
     *  @exception IllegalActionException Port is not an input.
     */	
    public abstract void receive(Token token, IOPort fromport)
                    throws IllegalActionException;

    //////////////////////////////////////////////////////////////////////////
    ////                         protected methods                        ////

    /** Do nothing if the specified relation is compatible with this port.
     *  Otherwise, throw an exception.
     *  @param relation
     *  @exception IllegalActionException Incompatible relation.
     */	
    public void _checkRelation(Relation relation) 
            throws IllegalActionException {
        if (!(relation instanceof IORelation)) {
            throw new IllegalActionException(this,
                   "Attempt to link to an incompatible relation.");
        }
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    // Indicate whether the port is an input, an output, or both.
    private boolean _isinput, _isoutput;
    // A cache of the deeply connected Input/Output ports, and the version
    // used to construct it.
    // 'transient' means that the variable will not be serialized.
    private transient LinkedList _deepConnectedInputPorts;
    private long _deepConnectedInputPortsVersion = -1;
    private transient LinkedList _deepConnectedOutputPorts;
    private long _deepConnectedOutputPortsVersion = -1;
}
