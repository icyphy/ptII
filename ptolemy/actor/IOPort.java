/* A port supporting message passing.

 Copyright (c) 1997-2000 The Regents of the University of California.
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

@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Green (davisj@eecs.berkeley.edu)

*/

package ptolemy.actor;

// Ptolemy imports.
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;

// Java imports.
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// IOPort
/**
This class supports exchanging data between entities via message passing.
It can serve as an input port, an output port, or both. If it is an
input port, then it contains some number of receivers, which are
responsible for receiving data from remote entities. If it is an
output port, then it can send data to remote receivers.
<p>
Its receivers are created by a director.  It must therefore be
contained by an actor that has a director.  If it is not, then
any attempt to read data or list the receivers will trigger
an exception.
<p>
If this port is at the boundary of an opaque composite actor, then
then it can have both inside and outside links, with corresponding
inside and outside receivers. The inside links are to relations
inside the opaque composite actor, whereas the outside links are
to relations outside. If it is not specified, then a link is an
outside link.
<p>
The port has a <i>width</i>, which by default can be no greater
than one.  This width is the sum of the widths of the linked relations.
A port with a width greater than one behaves as a bus interface,
so if the width is <i>w</i>, then the port can simultaneously
handle <i>w</i> distinct input or output channels of data.
<p>
In general, an input port might have more than one receiver for
each channel.  This occurs particularly for transparent input ports,
which treat the receivers of the ports linked on the inside as its own.
But might also occur for opaque ports in some derived classes.
Each receiver in the group is sent the same data. Thus, an input port in
general will have <i>w</i> distinct groups of receivers, and can receive
<i>w</i> distinct channels.
<p>
By default, the maximum width of the port is one, so only one
channel is handled. A port that allows a width greater than one
is called a <i>multiport</i>. Calling setMultiport() with a
<i>true</i> argument converts the port to a multiport.
<p>
The width of the port is not set directly. It is the sum of the
widths of the relations that the port is linked to on the outside.
The sum of the widths of the relations linked on the inside can be
more or less than the width.  If it is more, then the excess inside relations
will be treated as if they are unconnected.  If it is less, then the
excess outside relations will be treated as if they are unconnected.
<p>
An IOPort can only link to instances of IORelation. Derived classes
may further constrain links to a subclass of IORelation.  To do this,
they should override the protected methods _checkLink() and _checkInsideLink()
to throw an exception if their arguments are not of the appropriate
type.  Similarly, an IOPort can only be contained by a class
derived from ComponentEntity and implementing the Actor interface.
Subclasses may further constrain the containers by overriding
setContainer().

@authors Edward A. Lee, Jie Liu, Neil Smyth, Lukito Muliadi
@version $Id$
*/
public class IOPort extends ComponentPort {

    /** Construct an IOPort with no container and no name that is
     *  neither an input nor an output.
     */
    public IOPort() {
        super();
    }

    /** Construct an IOPort with a containing actor and a name
     *  that is neither an input nor an output.  The specified container
     *  must implement the Actor interface, or an exception will be thrown.
     *
     *  @param container The container actor.
     *  @param name The name of the port.
     *  @exception IllegalActionException If the port is not of an acceptable
     *   class for the container, or if the container does not implement the
     *   Actor interface.
     *  @exception NameDuplicationException If the name coincides with
     *   a port already in the container.
     */
    public IOPort(ComponentEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
	super(container, name);
    }

    /** Construct an IOPort with a container and a name that is
     *  either an input, an output, or both, depending on the third
     *  and fourth arguments. The specified container must implement
     *  the Actor interface or an exception will be thrown.
     *
     *  @param container The container actor.
     *  @param name The name of the port.
     *  @param isInput True if this is to be an input port.
     *  @param isOutput True if this is to be an output port.
     *  @exception IllegalActionException If the port is not of an acceptable
     *   class for the container, or if the container does not implement the
     *   Actor interface.
     *  @exception NameDuplicationException If the name coincides with
     *   a port already in the container.
     */
    public IOPort(ComponentEntity container, String name,
            boolean isInput, boolean isOutput)
            throws IllegalActionException, NameDuplicationException {
        this(container, name);
        setInput(isInput);
        setOutput(isOutput);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Send a token to all connected receivers.
     *  Tokens are in general immutable, so each receiver is given a
     *  reference to the same token and no clones are made.
     *  The transfer is accomplished by calling getRemoteReceivers()
     *  to determine the number of channels with valid receivers and
     *  then calling send on the appropriate channels.
     *  It would probably be faster to call put() directly on the receivers.
     *  If there are no destination receivers, then nothing is sent.
     *  If the port is not connected to anything, or receivers have not been
     *  created in the remote port, then just return.
     *  This method is read-synchronized on the workspace.
     *
     *  @param token The token to send
     *  @exception IllegalActionException If the port is not an output.
     *  @exception NoRoomException If a send to one of the channels throws
     *     it.
     */
    public void broadcast(Token token)
	    throws IllegalActionException, NoRoomException {
        try {
            _workspace.getReadAccess();
            Receiver farRecs[][] = getRemoteReceivers();
            if (farRecs == null) {
                return;
            }

            for (int j = 0; j < farRecs.length; j++) {
                send(j, token);
            }
        } finally {
            _workspace.doneReading();
        }
    }

    /** Clone this port into the specified workspace. The new port is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new port with no connections and no container.
     *
     *  @param ws The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one or more of the attributes
     *   cannot be cloned.
     *  @return A new IOPort.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        IOPort newobj = (IOPort)super.clone(ws);
        newobj._insideinputversion = -1;
        newobj._insideoutputversion = -1;
        newobj._width = 0;
        newobj._widthVersion = -1;
        newobj._farReceivers = null;
        newobj._farReceiversVersion = -1;
        newobj._localReceivers = null;
        newobj._localReceiversVersion = -1;
        newobj._localInsideReceivers = null;
        newobj._localInsideReceiversVersion = -1;
        newobj._localReceiversTable = null;
        newobj._insideReceivers = null;
        newobj._insideReceiversVersion = -1;
        return newobj;
    }

    /** Create new receivers for this port, replacing any that may
     *  previously exist. This method should only be
     *  called on opaque ports. It should also normally only be called
     *  during the initialize and prefire methods of the director.
     *  <p>
     *  If the port is an input port, receivers are created as necessary
     *  for each relation connecting to the port from the outside.
     *  If the port is an output port, receivers are created as necessary
     *  for each relation connected to the port from the inside. Note that
     *  only composite entities will have relations connecting to ports
     *  from the inside.
     *  If the port has zero width, then do nothing.
     *  <p>
     *  This method is <i>not</i> write-synchronized on the workspace, so the
     *  caller should be.
     *  @exception IllegalActionException If this port is not
     *   an opaque input port or if there is no director.
     */
    public void createReceivers() throws IllegalActionException {
        if (!isOpaque()) {
            throw new IllegalActionException(this,
                    "createReceivers: Can only create " +
                    "receivers on opaque ports.");
        }
        int portWidth = getWidth();
        if (portWidth <= 0) return;

        // Create the hashtable of receivers in this port, keyed by
        // relation, if it does not already exist.
        if (_localReceiversTable == null) {
            _localReceiversTable = new Hashtable();
        }

        boolean input = isInput();
        boolean output = isOutput();

        if (input) {
            Iterator outsideRelations = linkedRelationList().iterator();
            while (outsideRelations.hasNext()) {
                IORelation relation =
                    (IORelation) outsideRelations.next();
                int width = relation.getWidth();

                Receiver[][] result = new Receiver[width][1];

                for (int i = 0; i< width; i++) {
                    // This throws an exception if there is no director.
                    result[i][0] = _newReceiver();
                }
                // Save it, possibly replacing a previous version.
                _localReceiversTable.put(relation, result);
            }
        }
        if (output) {
            Iterator insideRelations = insideRelationList().iterator();
            while (insideRelations.hasNext()) {
                IORelation relation = (IORelation)insideRelations.next();
                int width = relation.getWidth();

                Receiver[][] result = new Receiver[width][1];

                // Inside links need to have receivers compatible
                // with the local director.  We need to create those
                // receivers here.
                for (int i = 0; i< width; i++) {
                    // This throws an exception if there is no director.
                    result[i][0] = _newInsideReceiver();
                }
                // Save it, possibly replacing a previous version.
                _localReceiversTable.put(relation, result);
            }
        }
    }

    /** Deeply enumerate the input ports connected to this port on the
     *  outside.  This method is deprecated and calls
     *  deepConnectedInPortList(). It is read-synchronized on the
     *  workspace.
     *
     *  @see ptolemy.kernel.ComponentPort#deepConnectedPorts
     *  @deprecated Use deepConnectedInPortList() instead.
     *  @return An enumeration of input IOPort objects.
     */
    public Enumeration deepConnectedInPorts() {
        return Collections.enumeration( deepConnectedInPortList() );
    }

    /** Deeply enumerate the input ports connected to this port on the
     *  outside and return a list of these ports.  This method calls
     *  deepConnectedPortList() of the super class to get all the deeply
     *  connected ports and returns only the input ports among them.
     *  It is read-synchronized on the workspace.
     *
     *  @see ptolemy.kernel.ComponentPort#deepConnectedPortList
     *  @return A list of input IOPort objects.
     */
    public List deepConnectedInPortList() {
	try {
	    _workspace.getReadAccess();
	    LinkedList result = new LinkedList();

	    Iterator ports = deepConnectedPortList().iterator();
            while(ports.hasNext()) {
		IOPort port = (IOPort)ports.next();
		if (port.isInput()) {
		    result.addLast(port);
		}
	    }
	    return result;
	} finally {
	    _workspace.doneReading();
	}
    }

    /** Deeply enumerate the output ports connected to this port on the
     *  outside.  This method is deprecated and calls
     *  deepConnectedOutPortList().
     *  It is read-synchronized on the workspace.
     *
     *  @see ptolemy.kernel.ComponentPort#deepConnectedPorts
     *  @deprecated Use deepConnectedInPortList() instead.
     *  @return An enumeration of output IOPort objects.
     */
    public Enumeration deepConnectedOutPorts() {
        return Collections.enumeration( deepConnectedOutPortList() );
    }

    /** Deeply enumerate the output ports connected to this port on the
     *  outside and a return a list of these ports.  This method calls
     *  deepConnectedPortList() of the super *  class to get all the
     *  deeply connected ports and returns only the output ports among
     *  them. It is read-synchronized on the workspace.
     *
     *  @see ptolemy.kernel.ComponentPort#deepConnectedPorts
     *  @return An enumeration of output IOPort objects.
     */
    public List deepConnectedOutPortList() {
	try {
	    _workspace.getReadAccess();
	    LinkedList result = new LinkedList();

	    Iterator ports = deepConnectedPortList().iterator();
	    while (ports.hasNext()) {
		IOPort port = (IOPort)ports.next();
		if (port.isOutput()) {
		    result.addLast(port);
		}
	    }
	    return result;
	} finally {
	    _workspace.doneReading();
	}
    }

    /** If the port is an input, return the receivers deeply linked on the
     *  inside.  This method is used to obtain
     *  the receivers that are to receive data at this input port.
     *  The returned value is an array of
     *  arrays in the same format as that returned by getReceivers(). The
     *  difference between this method and getReceivers() is that this method
     *  treats the port as a transparent port regardless of whether it is
     *  one.  If there are no relations linked on the inside, it returns null.
     *  This method is used for opaque, non-atomic entities.  It "sees through"
     *  the boundary of opaque ports and actors.
     *  This method is <i>not</i> read-synchronized on the workspace, so the
     *  caller should be.
     *
     *  @return The inside receivers.
     *  @exception IllegalActionException If there is no director, and hence
     *   no receivers have been created.
     */
    public Receiver[][] deepGetReceivers() throws IllegalActionException {
        if (!isInput()) return _EMPTY_RECEIVER_ARRAY;
        int width = getWidth();
        if (width <= 0) return _EMPTY_RECEIVER_ARRAY;
        if (_insideReceiversVersion != _workspace.getVersion()) {
            // Cache is invalid.  Update it.
            _insideReceivers = new Receiver[width][0];
            int index = 0;
            Iterator insideRels = insideRelationList().iterator();
            while (insideRels.hasNext()) {
                IORelation r = (IORelation) insideRels.next();
                Receiver[][] rr = r.deepReceivers(this);
                if (rr != null) {
                    int size = java.lang.Math.min(rr.length, width-index);
                    for (int i = 0; i < size; i++) {
                        if (rr[i] != null) {
                            _insideReceivers[index++] = rr[i];
                        }
                    }
                }
            }
            _insideReceiversVersion = _workspace.getVersion();
        }
        return _insideReceivers;
    }

    /** Get a token from the specified channel.
     *  If the channel has a group with more than one receiver (something
     *  that is possible if this is a transparent port), then this method
     *  calls get() on all receivers, but returns only the first non-null
     *  token returned by these calls.
     *  Normally this method is not used on transparent ports.
     *  If there is no token to return, then throw an exception.
     *  <p>
     *  Some of this method is read-synchronized on the workspace.
     *  Since it is possible for a thread to block while executing a get,
     *  it is important that the thread does not hold read access on
     *  the workspace when it is blocked. Thus this method releases
     *  read access on the workspace before calling get.
     *
     *  @param channelIndex The channel index.
     *  @return A token from the specified channel.
     *  @exception NoTokenException If there is no token.
     *  @exception IllegalActionException If there is no director, and hence
     *   no receivers have been created, if the port is not an input port, or
     *   if the channel index is out of range.
     */
    public Token get(int channelIndex)
            throws NoTokenException, IllegalActionException {
        Receiver[][] localRec;
        try {
            try {
                _workspace.getReadAccess();
                // Note that the getReceivers() method might throw an
                // IllegalActionException if there's no director.
                localRec = getReceivers();
                if (localRec[channelIndex] == null) {
                    throw new NoTokenException(this,
                            "get: no receiver at index: " + channelIndex + ".");
                }
            } finally {
                _workspace.doneReading();
            }
            Token tt = null;
            for (int j = 0; j < localRec[channelIndex].length; j++) {
                Token ttt = localRec[channelIndex][j].get();
                if (tt == null) tt = ttt;
            }
            if (tt == null) {
                throw new NoTokenException(this, "get: No token to return.");
            }
            return tt;
        } catch (ArrayIndexOutOfBoundsException ex) {
            // NOTE: This may be thrown if the port is not an input port.
            throw new IllegalActionException(this,
                    "get: channel index is out of range.");
        }
    }

    /** Get an array of tokens from the specified channel. The 
     *  parameter <i>channelIndex</i> specifies the channel and
     *  the parameter <i>vectorLength</i> specifies the number of 
     *  valid tokens to get in the returned array. The length of 
     *  the returned array can be greater than the specified vector
     *  length, in which case, only the first <i>vectorLength</i> 
     *  elements are guaranteed to be valid.
     *  <p>
     *  If the channel has a group with more than one receiver (something
     *  that is possible if this is a transparent port), then this method
     *  calls get() on all receivers, but returns only the first non-null
     *  token returned by these calls.
     *  Normally this method is not used on transparent ports.
     *  If there are not enough tokens to fill the array, then throw 
     *  an exception.
     *  <p>
     *  Some of this method is read-synchronized on the workspace.
     *  Since it is possible for a thread to block while executing a get,
     *  it is important that the thread does not hold read access on
     *  the workspace when it is blocked. Thus this method releases
     *  read access on the workspace before calling get.
     *
     *  @param channelIndex The channel index.
     *  @param vectorLength The number of valid tokens to get in the
     *   returned array.
     *  @return A token array from the specified channel containing
     *   <i>vectorLength</i> valid tokens.
     *  @exception NoTokenException If there is no array of tokens.
     *  @exception IllegalActionException If there is no director, and hence
     *   no receivers have been created, if the port is not an input port, or
     *   if the channel index is out of range.
     */
    public Token[] get(int channelIndex, int vectorLength)
            throws NoTokenException, IllegalActionException {
        Receiver[][] localRec;
        try {
            try {
                _workspace.getReadAccess();
                // Note that the getReceivers() method might throw an
                // IllegalActionException if there's no director.
                localRec = getReceivers();
                if (localRec[channelIndex] == null) {
                    throw new NoTokenException(this,
                            "get: no receiver at index: " + channelIndex + ".");
                }
            } finally {
                _workspace.doneReading();
            }

	    Token[] retArray = localRec[channelIndex][0].getArray(vectorLength);
	    

            if (retArray == null) {
                throw new NoTokenException(this, "get: No token array to return.");
            }
            return retArray;
        } catch (ArrayIndexOutOfBoundsException ex) {
            // NOTE: This may be thrown if the port is not an input port.
            throw new IllegalActionException(this,
                    "get: channel index is out of range.");
        }
    }

    /** If the port is an opaque output port, return the receivers that
     *  receive data from all inside linked relations.
     *  This method is used for opaque, non-atomic entities, which have
     *  opaque ports with inside links.  Normally, those inside links
     *  are not visible.
     *  This method permits a director to transfer data across an opaque
     *  boundary by transferring it from the inside receivers to whatever
     *  receivers this might be connected to on the outside.
     *  The returned value is an an array of arrays in the same format as
     *  that returned by getReceivers().
     *  This method is read-synchronized on the workspace.
     *
     *  @return The local inside receivers.
     *  @exception IllegalActionException If there is no local director,
     *   and hence no receivers have been created.
     */
    public Receiver[][] getInsideReceivers() throws IllegalActionException {
        try {
            _workspace.getReadAccess();
            if (!isOutput() || !isOpaque()) return _EMPTY_RECEIVER_ARRAY;

            // Check to see whether cache is valid.
            if (_localInsideReceiversVersion == _workspace.getVersion()) {
                return _localInsideReceivers;
            }

            // Have to compute the _inside_ width.
            int width = 0;
            Iterator relations = insideRelationList().iterator();
            while(relations.hasNext()) {
                IORelation r = (IORelation) relations.next();
                width += r.getWidth();
            }

            if (width <= 0) return _EMPTY_RECEIVER_ARRAY;

            // Cache not valid.  Reconstruct it.
            _localInsideReceivers = new Receiver[width][0];
            int index = 0;
            relations = insideRelationList().iterator();
            while (relations.hasNext()) {
                IORelation r = (IORelation) relations.next();
                Receiver[][] rr = getReceivers(r);
                if (rr != null) {
                    for (int i = 0; i < rr.length; i++) {
                        _localInsideReceivers[index++] = rr[i];
                    }
                }
            }
            _localInsideReceiversVersion = _workspace.getVersion();
            return _localInsideReceivers;
        } finally {
            _workspace.doneReading();
        }
    }

    /** If the port is an input, return the receivers that receive data
     *  from all linked relations. For an input
     *  port, the returned value is an array of arrays.  The first index
     *  (the group) specifies the group of receivers that receive from
     *  the same channel.  The second index (the column) specifies the 
     *  receiver number within the group of receivers that get copies from 
     *  the same channel.
     *  <p>
     *  For a transparent port (a port of a non-opaque entity), this method
     *  returns receivers in ports connected to this port on the inside.
     *  For an opaque port, the receivers returned are contained directly by
     *  this port.
     *  <p>
     *  The number of channels (groups) is the width of the port.
     *  <p>
     *  For each channel, there may be any number of receivers in the group.
     *  The individual receivers are selected using the second index of the
     *  returned array of arrays.  If there are no receivers in the group,
     *  then the channel is represented by null.  I.e., if the returned
     *  array of arrays is <i>x</i> and the channel number is <i>c</i>,
     *  then <i>x</i>[<i>c</i>] is null.  Otherwise, it is an array, where
     *  the size of the array is the number of receivers in the group.
     *  If the port is opaque, then the group size is one, so only
     *  <i>x</i>[<i>c</i>][0] is defined.  If the port is transparent,
     *  the group size is arbitrary.
     *  <p>
     *  For an opaque port, this method creates receivers by calling
     *  _newReceiver() if there are no receivers or the number of receivers
     *  does not match the width of the port.  In the latter case,
     *  previous receivers are lost, together with any data they may contain.
     *  <p>
     *  This method is read-synchronized on the workspace.  If its cached
     *  list of local receivers is not valid, however, then it acquires
     *  write synchronization on the workspace to reconstruct it.
     *
     *  @return The local receivers.
     *  @exception IllegalActionException If there is no director, and hence
     *   no receivers have been created.
     */
    public Receiver[][] getReceivers() throws IllegalActionException {
        try {
            _workspace.getReadAccess();
            if (!isInput()) return _EMPTY_RECEIVER_ARRAY;

            if(isOpaque()) {
                // Check to see whether cache is valid.
                if (_localReceiversVersion == _workspace.getVersion()) {
                    return _localReceivers;
                }

                // Cache not valid.  Reconstruct it.
                int width = getWidth();
                if (width <= 0) return _EMPTY_RECEIVER_ARRAY;

                _localReceivers = new Receiver[width][0];
                int index = 0;
                Iterator relations = linkedRelationList().iterator();
                while (relations.hasNext()) {
                    IORelation r = (IORelation) relations.next();
                    Receiver[][] rr = getReceivers(r);
                    if (rr != null) {
                        for (int i = 0; i < rr.length; i++) {
                            _localReceivers[index++] = rr[i];
                        }
		    }
                }
                _localReceiversVersion = _workspace.getVersion();
                return _localReceivers;
            } else {
                // Transparent port.
                return deepGetReceivers();
            }
        } finally {
            _workspace.doneReading();
        }
    }

    /** If the port is an input, return the receivers that handle incoming
     *  channels from the specified relation. If the port is an opaque output
     *  and the relation is inside linked, return the receivers that handle
     *  incoming channels from the inside.
     *  The returned value is an array of arrays of the same form
     *  as that returned by getReceivers() with no arguments.  Note that a
     *  single relation may represent multiple channels because it may be
     *  a bus.
     *  <p>
     *  This method is read-synchronized on the workspace.
     *
     *  @param relation A relation that is linked on the outside or inside.
     *  @return The local receivers.
     *  @exception IllegalActionException If the relation is not linked
     *   from the outside, or if there is no director.
     */
    public Receiver[][] getReceivers(IORelation relation)
            throws IllegalActionException {
        try {
            _workspace.getReadAccess();
            // Allow inside relations also to support opaque,
            // non-atomic entities.
            boolean insidelink = isInsideLinked(relation);
            if (!isLinked(relation) && !insidelink) {
                throw new IllegalActionException(this, relation,
                        "getReceivers: Relation argument is not " +
                        "linked to me.");
            }
            boolean opaque = isOpaque();
            if (!isInput() && !(opaque && insidelink && isOutput())) {
                return _EMPTY_RECEIVER_ARRAY;
            }

            int width = relation.getWidth();
            if (width <= 0) return _EMPTY_RECEIVER_ARRAY;

            Receiver[][] result = null;
            // If the port is opaque, return the local Receivers for the
            // relation.
            if(opaque) {
                // If _localReceiversTable is null, then createReceivers()
                // hasn't been called, so there is nothing to return.
                if (_localReceiversTable == null) return _EMPTY_RECEIVER_ARRAY;

                if( _localReceiversTable.containsKey(relation) ) {
                    // Get the list of receivers for this relation.
                    result = (Receiver[][])_localReceiversTable.get(relation);
                    if (result.length != width)  {
                        String s = "getReceivers(IORelation): Invalid ";
                        s += "receivers. Need to call createReceivers().";
                        throw new InvalidStateException(this, s);
                    }
                }
                return result;
            } else {
                // If a transparent port, ask its all inside receivers,
                // and trim the returned Receivers array to get the
                // part corresponding to the IORelation
                Receiver[][] insideReceivers = getReceivers();
                if(insideReceivers == null) {
                    return _EMPTY_RECEIVER_ARRAY;
                }
                int insideWidth = insideReceivers.length;
                int index = 0;
                result = new Receiver[width][];
                Iterator outsideRels = linkedRelationList().iterator();
                while(outsideRels.hasNext()) {
                    IORelation outsideRel = (IORelation) outsideRels.next();
                    if(outsideRel == relation) {
                        result = new Receiver[width][];
                        int rstSize =
                            java.lang.Math.min(width, insideWidth-index);
                        for (int i = 0; i< rstSize; i++) {
                            result[i] = insideReceivers[index++];
                        }
                        break;
                    } else {
                        index += outsideRel.getWidth();
                        if(index > insideWidth) break;
                    }
                }
                return result;
            }
        } finally {
            _workspace.doneReading();
        }
    }

    /** If the port is an output, return the remote receivers that can
     *  receive from the port.  For an output
     *  port, the returned value is an array of arrays of the same form
     *  as that returned by getReceivers() with no arguments.  The length
     *  of the array is the width of the port (the number of channels).
     *  It is an array of arrays, each of which represents a group of
     *  receivers that receive data from the same channel.
     *  <p>
     *  This method may have the effect of creating new receivers in the
     *  remote input ports, if they do not already have the right number of
     *  receivers.  In this case, previous receivers are lost, together
     *  with any data they may contain.
     *  <p>
     *  This method is read-synchronized on the workspace.
     *
     *  @return The receivers for output data.
     */
    public Receiver[][] getRemoteReceivers() {
        try {
            _workspace.getReadAccess();
            if (!isOutput()) return _EMPTY_RECEIVER_ARRAY;

            int width = getWidth();
            if (width <= 0) return _EMPTY_RECEIVER_ARRAY;

            // For opaque port, try the cached _farReceivers
            // Check validity of cached version
            if(isOpaque() &&
                    _farReceiversVersion == _workspace.getVersion()) {
                return _farReceivers;
            }
            // If not an opaque port or Cache is not valid.  Reconstruct it.
            Receiver[][] farReceivers = new Receiver[width][0];
            Iterator relations = linkedRelationList().iterator();
            int index = 0;
            boolean foundremoteinput = false;
            while(relations.hasNext()) {
                IORelation relation = (IORelation) relations.next();
                Receiver[][] rr;
                rr = relation.deepReceivers(this);
                if (rr != null) {
                    for(int i = 0; i < rr.length; i++) {
                        farReceivers[index] = rr[i];
                        index++;
                        foundremoteinput = true;
                    }
                } else {
                    // create a number of null entries in farReceivers
                    // corresponding to the width of relation r
                    index += relation.getWidth();
                }
            }
            if (!foundremoteinput) {
		// No longer needed, davisj (3/29/99)
                // No remote receivers
                // farReceivers = null;
            }
            // For an opaque port, cache the result.
            if(isOpaque()) {
                _farReceiversVersion = _workspace.getVersion();
                _farReceivers = farReceivers;
            }
            return farReceivers;
        } finally {
            _workspace.doneReading();
        }
    }

    /** If this port is an output, return the remote receivers that can
     *  receive data from this port through the specified relation.
     *  The relation should linked to the port
     *  from the inside, otherwise an exception is thrown. For an output
     *  port, the returned value is an array of arrays of the same form
     *  as that returned by getReceivers() with no arguments.
     *  <p>
     *  This method may have the effect of creating new receivers in the
     *  remote input ports, if they do not already have the right number of
     *  receivers.  In this case, previous receivers are lost, together
     *  with any data they may contain.
     *  <p>
     *  This method is read-synchronized on the workspace.
     *
     *  @return The receivers for output data.
     *  @exception IllegalActionException If the IORelation is not linked
     *       to the port from the inside.
     */
    public Receiver[][] getRemoteReceivers(IORelation relation)
            throws IllegalActionException {
        try {
            _workspace.getReadAccess();
            if (!isInsideLinked(relation)) {
                throw new IllegalActionException(this, relation,
                        "not linked from the inside.");
            }

            if (!isOutput()) return _EMPTY_RECEIVER_ARRAY;

            int width = relation.getWidth();
            if (width <= 0) return _EMPTY_RECEIVER_ARRAY;

            // no cache used.
            Receiver[][] outsideReceivers = getRemoteReceivers();
            if(outsideReceivers == null) {
                return _EMPTY_RECEIVER_ARRAY;
            }
            Receiver[][] result = new Receiver[width][];
            Iterator insideRels = insideRelationList().iterator();
            int index = 0;
            while(insideRels.hasNext()) {
                IORelation insideRel = (IORelation) insideRels.next();
                if(insideRel == relation) {
                    int size = java.lang.Math.min
                        (width, outsideReceivers.length-index);
                    //NOTE: if size = 0, the for loop is skipped.
                    for(int i = 0; i < size; i++) {
                        result[i] = outsideReceivers[i+index];
                    }
                    break;
                }
                index += insideRel.getWidth();
            }
            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Return the width of the port.  The width is the sum of the
     *  widths of the relations that the port is linked to (on the outside).
     *  This method is read-synchronized on the workspace.
     *
     *  @return The width of the port.
     */
    public int getWidth() {
        try {
            _workspace.getReadAccess();
            long version = _workspace.getVersion();
            if(_widthVersion != version) {
                _widthVersion = version;
                int sum = 0;
                Iterator relations = linkedRelationList().iterator();
                while(relations.hasNext()) {
                    IORelation relation = (IORelation) relations.next();
                    sum += relation.getWidth();
                }
                _width = sum;
            }
            return _width;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Return true if the specified channel can accept a token via the
     *  put() method.  If this port is not an output, or the channel index
     *  is out of range, then throws IllegalActionException.  If there
     *  are multiple receivers in the group associated with the channel,
     *  then return true only if all the receivers can accept a token.
     *
     *  @param channelIndex The channel index.
     *  @return True if there is room for a token in the channel.
     *  @exception IllegalActionException If the receivers do not support
     *   this query, if this is not an output port, or if the channel index
     *   is out of range.
     */
    public boolean hasRoom(int channelIndex) throws IllegalActionException {
        try {
            Receiver[][] farRecs = getRemoteReceivers();
            if (farRecs == null || farRecs[channelIndex] == null) {
                return false;
            }
            for (int j = 0; j < farRecs[channelIndex].length; j++) {
                if (!farRecs[channelIndex][j].hasRoom()) return false;
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            // NOTE: This might be thrown if the port is not an input port.
            throw new IllegalActionException(this,
                    "hasRoom: channel index is out of range.");
        }
        return true;
    }

    /** Return true if the specified channel has a token to deliver
     *  via the get() method.  If this port is not an input, or if the
     *  channel index is out of range, then throw an exception.
     *  Note that this does not report any tokens in inside receivers
     *  of an output port. Those are accessible only through
     *  getInsideReceivers().
     *
     *  @param channelIndex The channel index.
     *  @return True if there is a token in the channel.
     *  @exception IllegalActionException If the receivers do not support
     *   this query, if there is no director, and hence no receivers,
     *   if the port is not an input port, or if the channel index is out
     *   of range.
     */
    public boolean hasToken(int channelIndex) throws IllegalActionException {
        try {
            // The getReceivers() method throws an IllegalActionException if
            // there's no director.
            Receiver[][] recs = getReceivers();
            if (recs == null || recs[channelIndex] == null) {
                return false;
            }
            for (int j = 0; j < recs[channelIndex].length; j++) {
                if (recs[channelIndex][j].hasToken()) return true;
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            // NOTE: This might be thrown if the port is not an input port.
            throw new IllegalActionException(this,
                    "hasToken: channel index is out of range.");
        }
        return false;
    }

    /** Return true if the specified channel has a token to deliver
     *  via the get() method.  If this port is not an input, or if the
     *  channel index is out of range, then throw an exception.
     *  Note that this does not report any tokens in inside receivers
     *  of an output port. Those are accessible only through
     *  getInsideReceivers().
     *
     *  @param channelIndex The channel index.
     *  @param tokens The number of tokens to query the channel for.
     *  @return True if there is a token in the channel.
     *  @exception IllegalActionException If the receivers do not support
     *   this query, if there is no director, and hence no receivers,
     *   if the port is not an input port, or if the channel index is out
     *   of range.
     */
    public boolean hasToken(int channelIndex, int tokens)
             throws IllegalActionException {
        try {
            // The getReceivers() method throws an IllegalActionException if
            // there's no director.
            Receiver[][] recs = getReceivers();
            if (recs == null || recs[channelIndex] == null) {
                return false;
            }
            for (int j = 0; j < recs[channelIndex].length; j++) {
                if (recs[channelIndex][j].hasToken(tokens)) return true;
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            // NOTE: This might be thrown if the port is not an input port.
            throw new IllegalActionException(this,
                    "hasToken: channel index is out of range.");
        }
        return false;
    }

    /** Return true if the port is an input.  The port is an input
     *  if either setInput() has been called with a <i>true</i> argument, or
     *  it is connected on the inside to an input port, or if it is
     *  connected on the inside to the inside of an output port.
     *  In other words, it is an input if data can be put directly into
     *  it or sent through it to an input.
     *  This method is read-synchronized on the workspace.
     *
     *  @return True if the port is an input.
     */
    public boolean isInput() {
        long version = _workspace.getVersion();
        if (_insideinputversion != version) {
            try {
                _workspace.getReadAccess();
                // Check to see whether any port linked on the inside
                // is an input.
                Iterator ports = deepInsidePortList().iterator();
                while(ports.hasNext()) {
                    IOPort p = (IOPort) ports.next();
                    // Rule out case where this port itself is listed...
                    if (p != this && p.isInput()) _isInput = true;
                }
                _insideinputversion = version;
            } finally {
                _workspace.doneReading();
            }
        }
        return _isInput;
    }

    /** Return true if the port is a multiport.  The port is a multiport
     *  if setMultiport() has been called with a true argument.
     *
     *  @return True if the port is a multiport.
     */
    public boolean isMultiport() {
        // No need to synchronize this because the action is atomic
        // and synchronization would just ensure that no write action
        // is in progress.
        return _isMulitport;
    }

    /** Return true if the port is an output. The port is an output
     *  if either setOutput() has been called with a true argument, or
     *  it is connected on the inside to an output port, or it is
     *  connected on the inside to the inside of an input port.
     *  This method is read-synchronized on the workspace.
     *
     *  @return True if the port is an output.
     */
    public boolean isOutput() {
        long version = _workspace.getVersion();
        if (_insideoutputversion != version) {
            try {
                _workspace.getReadAccess();
                // Check to see whether any port linked on the
                // inside is an output.
                Iterator ports = deepInsidePortList().iterator();
                while(ports.hasNext()) {
                    IOPort p = (IOPort) ports.next();
                    // Rule out case where this port itself is listed...
                    if (p != this && p.isOutput()) _isOutput = true;
                }
                _insideoutputversion = version;
            } finally {
                _workspace.doneReading();
            }
        }
        return _isOutput;
    }

    /** Send the specified token to all receivers connected to the
     *  specified channel.  Tokens are in general immutable, so each receiver
     *  is given a reference to the same token and no clones are made.
     *  If the port is not connected to anything, or receivers have not been
     *  created in the remote port, or the channel index is out of
     *  range, or the port is not an output port,
     *  then just silently return.  This behavior makes it
     *  easy to leave output ports unconnected when you are not interested
     *  in the output.  The transfer is
     *  accomplished by calling the put() method of the remote receivers.
     *  If the port is not connected to anything, or receivers have not been
     *  created in the remote port, then just return.
     *  <p>
     *  Some of this method is read-synchronized on the workspace.
     *  Since it is possible for a thread to block while executing a put,
     *  it is important that the thread does not hold read access on
     *  the workspace when it is blocked. Thus this method releases
     *  read access on the workspace before calling put.
     *
     *  @param channelIndex The index of the channel, from 0 to width-1
     *  @param token The token to send
     *  @exception NoRoomException If there is no room in the receiver.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void send(int channelIndex, Token token)
            throws IllegalActionException, NoRoomException {
        Receiver[][] farRec;
        try {
            try {
                _workspace.getReadAccess();
                // Note that the getRemoteReceivers() method doesn't throw
                // any non-runtime exception.
                farRec = getRemoteReceivers();
                if (farRec == null || farRec[channelIndex] == null) return;
            } finally {
                _workspace.doneReading();
            }
            for (int j = 0; j < farRec[channelIndex].length; j++) {
                farRec[channelIndex][j].put(token);
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            // NOTE: This may occur if the port is not an output port.
            // Ignore...
        }
    }

    /** Send the specified portion of a token array to all receivers connected 
     *  to the specified channel. The first <i>vectorLength</i> tokens
     *  of the token array are sent.
     *  <p>
     *  Tokens are in general immutable, so each receiver
     *  is given a reference to the same token and no clones are made.
     *  If the port is not connected to anything, or receivers have not been
     *  created in the remote port, or the channel index is out of
     *  range, or the port is not an output port,
     *  then just silently return.  This behavior makes it
     *  easy to leave output ports unconnected when you are not interested
     *  in the output.  The transfer is accomplished
     *  by calling the vectorized put() method of the remote receivers.
     *  If the port is not connected to anything, or receivers have not been
     *  created in the remote port, then just return.
     *  <p>
     *  Some of this method is read-synchronized on the workspace.
     *  Since it is possible for a thread to block while executing a put,
     *  it is important that the thread does not hold read access on
     *  the workspace when it is blocked. Thus this method releases
     *  read access on the workspace before calling put.
     *
     *  @param channelIndex The index of the channel, from 0 to width-1
     *  @param tokenArray The token array to send
     *  @param vectorLength The number of elements of of the token
     *   array to send.
     *  @exception NoRoomException If there is no room in the receiver.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void send(int channelIndex, Token[] tokenArray, int vectorLength)
            throws IllegalActionException, NoRoomException {
        Receiver[][] farRec;
        try {
            try {
                _workspace.getReadAccess();
                // Note that the getRemoteReceivers() method doesn't throw
                // any non-runtime exception.
                farRec = getRemoteReceivers();
                if (farRec == null || farRec[channelIndex] == null) return;
            } finally {
                _workspace.doneReading();
            }
            for (int j = 0; j < farRec[channelIndex].length; j++) {
                farRec[channelIndex][j].putArray(tokenArray, vectorLength);
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            // NOTE: This may occur if the port is not an output port.
            // Ignore...
        }
    }

    /** Override the base class to ensure that the proposed container
     *  implements the Actor interface (the base class ensures that the
     *  container is an instance of ComponentEntity) or null. A null
     *  argument will remove the port from the container.
     *
     *  @param container The proposed container.
     *  @exception IllegalActionException If the proposed container is not a
     *   ComponentEntity, doesn't implement Actor, or has no name,
     *   or the port and container are not in the same workspace. Or
     *   it's not null
     *  @exception NameDuplicationException If the container already has
     *   a port with the name of this port.
     */
    public void setContainer(Entity container)
            throws IllegalActionException, NameDuplicationException {
        if (!(container instanceof Actor) && (container != null)) {
            throw new IllegalActionException(container, this,
                    "IOPort can only be contained by objects implementing " +
                    "the Actor interface.");
        }
        super.setContainer(container);
    }

    /** If the argument is true, make the port an input port.
     *  If the argument is false, make the port not an input port.
     *  This has no effect if the port is a transparent port.
     *  In that case, the port
     *  is an input port regardless of whether and how this method is called.
     *  This method is write-synchronized on the workspace.
     *
     *  @param isInput True to make the port an input.
     */
    public void setInput(boolean isInput) {
        // No need for the try ... finally construct here because no
        // exception can occur.  Note that although the action here is
        // atomic, we still need to obtain write access to be sure that
        // the change is not made in the middle of another read in another
        // thread.
        _workspace.getWriteAccess();
        _isInput = isInput;
        _workspace.doneWriting();
    }

    /** If the argument is true, make the port a multiport.
     *  That is, make it capable of linking with multiple IORelations,
     *  or with IORelations that have width greater than one.
     *  If the argument is false, allow only links with a single
     *  IORelation of width one.
     *  This has no effect if the port is a transparent port that is
     *  linked on the inside to a multiport.  In that case, the port
     *  is a multiport regardless of whether and how this method is called.
     *  This method is write-synchronized on the workspace.
     *
     *  @param isMulitport True to make the port a multiport.
     */
    public void setMultiport(boolean isMulitport) {
        // No need for the try ... finally construct here because no
        // exception can occur.  Note that although the action here is
        // atomic, we still need to obtain write access to be sure that
        // the change is not made in the middle of another read in another
        // thread.
        _workspace.getWriteAccess();
        _isMulitport = isMulitport;
        _workspace.doneWriting();
    }

    /** If the argument is true, make the port an output port.
     *  If the argument is false, make the port not an output port.
     *  This has no effect if the port is a transparent port that is
     *  linked on the inside to output ports.  In that case, the port
     *  is an output port regardless of whether and how this method is called.
     *  This method is write-synchronized on the workspace.
     *
     *  @param isOutput True to make the port an output.
     */
    public void setOutput(boolean isOutput) {
        // No need for the try ... finally construct here because no
        // exception can occur.  Note that although the action here is
        // atomic, we still need to obtain write access to be sure that
        // the change is not made in the middle of another read in another
        // thread.
        _workspace.getWriteAccess();
        _isOutput = isOutput;
        _workspace.doneWriting();
    }

    /** Unlink the specified Relation. The receivers associated with
     *  this relation, and any data they contain, are lost. If the Relation
     *  is not linked to this port, do nothing.
     *  This method is write-synchronized on the workspace.
     *
     *  @param relation The relation to unlink.
     */
    public void unlink(Relation relation) {
        try {
            _workspace.getWriteAccess();
            super.unlink(relation);
            if (_localReceiversTable != null) {
                _localReceiversTable.remove(relation);
            }
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Unlink all relations.
     *  This method is write-synchronized on the
     *  workspace.
     */
    public void unlinkAll() {
        try {
            _workspace.getWriteAccess();
            super.unlinkAll();
            if (_localReceiversTable != null) {
                _localReceiversTable.clear();
            }
        } finally {
            _workspace.doneWriting();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Indicate that the description(int) method should include information
     *  about whether the port is an input, output, or multiport, whether it
     *  is opaque, and what is its width.
     */
    public static final int CONFIGURATION = 512;

    /** Indicate that the description(int) method should include receivers
     *  contained by this port (if any).
     */
    public static final int RECEIVERS = 1024;

    /** Indicate that the description(int) method should include receivers
     *  remotely connected to this port (if any).
     */
    public static final int REMOTERECEIVERS = 2048;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override parent method to ensure compatibility of the relation
     *  and validity of the width of the port.
     *  If this port is not a multiport, then the width of the
     *  relation is required to be specified to be one.  This method assumes
     *  that the relation is inside the port, so this is an inside link.
     *  <p>
     *  This method is <i>not</i> synchronized on the
     *  workspace, so the caller should be.
     *
     *  @param relation The relation to link to on the inside.
     *  @exception IllegalActionException If this port has no container or
     *   the relation is not an IORelation, or the port already linked to a
     *   relation and is not a multiport, or the relation has width
     *   not exactly one and the port is not a multiport, or the
     *   relation is incompatible with this port, or the port is not
     *   in the same workspace as the relation.
     */
    protected void _checkInsideLink(ComponentRelation relation)
            throws IllegalActionException {
        if (!(relation instanceof IORelation)) {
            throw new IllegalActionException(this, relation,
                    "Attempt to link to an incompatible relation." +
                    " IOPort requires IORelation.");
        }
        IORelation rel = (IORelation) relation;
        if(!isInsideLinked(rel)) {
            // Check for existing inside links
            if(!isMultiport() && numInsideLinks() >= 1) {
                throw new IllegalActionException(this, relation,
                        "Attempt to link more than one relation " +
                        "to a single port.");
            }
            if ((rel.getWidth() != 1) || !rel.isWidthFixed()) {
                // Relation is a bus.
                if(!isMultiport()) {
                    throw new IllegalActionException(this,  rel,
                            "Attempt to link a bus relation " +
                            "to a single port.");
                }
                if (!rel.isWidthFixed()) {
                    // Make sure there are no other busses already
                    // connected with unspecified widths.
                    try {
                        _getInsideWidth(null);
                    } catch (InvalidStateException ex) {
                        throw new IllegalActionException(this, rel,
                                "Attempt to link a second bus relation " +
                                "with unspecified width to the inside " +
                                "of a port.");
                    }
                }
            }
            super._checkInsideLink(rel);
        }
    }

    /** Override parent method to ensure compatibility of the relation
     *  and validity of the width of the port.
     *  If this port is not a multiport, then the width of the
     *  relation is required to be specified to be one. This method assumes
     *  that the relation is outside the port, so this is an outside link.
     *  <p>
     *  This method should not be used directly.  Use the public version
     *  instead. It is <i>not</i> synchronized on the
     *  workspace, so the caller should be.
     *
     *  @param relation The relation to link to.
     *  @exception IllegalActionException If this port has no container or
     *   the relation is not an IORelation, or the port already linked to a
     *   relation and is not a multiport, or if the relation has width
     *   not exactly one and the port is not a multiport, or the port is
     *   not in the same workspace as the relation.
     */
    protected void _checkLink(Relation relation)
            throws IllegalActionException {
        if (!(relation instanceof IORelation)) {
            throw new IllegalActionException(this, relation,
                    "Attempt to link to an incompatible relation." +
                    " IOPort requires IORelation.");
        }
        IORelation rel = (IORelation) relation;
        if(!isLinked(rel)) {
            // Check for existing outside links
            if(!isMultiport() && numLinks() >= 1) {
                throw new IllegalActionException(this, relation,
                        "Attempt to link more than one relation " +
                        "to a single port.");
            }
            if (rel.getWidth() != 1 || !rel.isWidthFixed()) {
                // Relation is a bus.
                if(!isMultiport()) {
                    throw new IllegalActionException(this,  rel,
                            "Attempt to link a bus relation " +
                            "to a single port.");
                }
                Iterator relations = linkedRelationList().iterator();
                while (relations.hasNext()) {
                    IORelation theRelation =
                        (IORelation)relations.next();
                    if (!theRelation.isWidthFixed()) {
                        throw new IllegalActionException(this, rel,
                                "Attempt to link a second bus relation " +
                                "with unspecified width to the outside " +
                                "of a port.");
                    }
                }
            }
            super._checkLink(rel);
        }
    }

    /** Return a description of the object.  The level of detail depends
     *  on the argument, which is an or-ing of the static final constants
     *  defined in the NamedObj class and in this class.
     *  Lines are indented according to
     *  to the level argument using the protected method _getIndentPrefix().
     *  Zero, one or two brackets can be specified to surround the returned
     *  description.  If one is specified it is the leading bracket.
     *  This is used by derived classes that will append to the description.
     *  Those derived classes are responsible for the closing bracket.
     *  An argument other than 0, 1, or 2 is taken to be equivalent to 0.
     *  <p>
     *  If the detail argument sets the bit defined by the constant
     *  CONFIGURATION, then append to the description a field containing
     *  any subset of the words "input", "output", "multiport", and "opaque",
     *  separated by spaces, plus a subfield of the form "{width
     *  <i>integer</i>}", where the integer is the width of the port.
     *  The field keywork is "configuration".
     *  <p>
     *  If the detail argument sets the bit defined by the constant
     *  RECEIVERS, then append to the description a field containing
     *  the receivers contained by this port.  The keywork is "receivers"
     *  and the format is like the Receivers array, an array of groups, with
     *  each group receiving from a channel.
     *  Each group is a list of receiver descriptions (it may also be empty).
     *  If the detail argument sets the bit defined by the constant
     *  REMOTERECEIVERS, then also append to the description a field containing
     *  the remote receivers connected to this port.
     *
     *  This method is read-synchronized on the workspace.
     *  @param detail The level of detail.
     *  @param indent The amount of indenting.
     *  @param bracket The number of surrounding brackets (0, 1, or 2).
     *  @return A description of the object.
     */
    protected String _description(int detail, int indent, int bracket) {
        try {
            _workspace.getReadAccess();
            String result;
            if (bracket == 1 || bracket == 2) {
                result = super._description(detail, indent, 1);
            } else {
                result = super._description(detail, indent, 0);
            }
            if ((detail & CONFIGURATION) != 0) {
                if (result.trim().length() > 0) {
                    result += " ";
                }
                result += "configuration {";
                boolean space = false;
                if (isInput()) {
                    space = true;
                    result += "input";
                }
                if (isOutput()) {
                    if (space) result += " ";
                    space = true;
                    result += "output";
                }
                if (isMultiport()) {
                    if (space) result += " ";
                    space = true;
                    result += "multiport";
                }
                if (isOpaque()) {
                    if (space) result += " ";
                    space = true;
                    result += "opaque";
                }
                if (space) result += " ";
                result += "{width " + getWidth() + "}}";
            }
            if ((detail & RECEIVERS) != 0) {
                if (result.trim().length() > 0) {
                    result += " ";
                }
                result += "receivers {\n";
                try {
                    Receiver[][] receivers = getReceivers();
                    for (int i = 0; i < receivers.length; i++) {
                        // One list item per group
                        result += _getIndentPrefix(indent+1) + "{\n";
                        for (int j = 0; j < receivers[i].length; j++) {
                            result += _getIndentPrefix(indent+2);
                            result += "{";
                            if (receivers[i][j] != null) {
                                result +=
                                    receivers[i][j].getClass().getName();
                            }
                            result += "}\n";
                        }
                        result += _getIndentPrefix(indent+1) + "}\n";
                    }
                } catch (IllegalActionException ex) {
                    result += _getIndentPrefix(indent+1) +
                        ex.getMessage() + "\n";
                }
                result += _getIndentPrefix(indent) + "}";
            }
            if ((detail & REMOTERECEIVERS) != 0) {
                if (result.trim().length() > 0) {
                    result += " ";
                }
                result += "remotereceivers {\n";
                Receiver[][] receivers = null;
                receivers = getRemoteReceivers();;
                if (receivers != null) {
                    for (int i = 0; i < receivers.length; i++) {
                        // One list item per group
                        result += _getIndentPrefix(indent+1) + "{\n";
                        if (receivers[i] != null) {
                            for (int j = 0; j< receivers[i].length; j++) {
                                result += _getIndentPrefix(indent+2);
                                result += "{";
                                if (receivers[i][j] != null) {
                                    result +=
                                        receivers[i][j].getClass().getName();
                                    result += " in ";
                                    result += receivers[i][j].
                                        getContainer().getFullName();
                                }
                                result += "}\n";
                            }
                        }
                        result += _getIndentPrefix(indent+1) + "}\n";
                    }
                }
                result += _getIndentPrefix(indent) + "}";
            }
            if (bracket == 2) result += "}";
            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Write a MoML description of the contents of this object, which
     *  in this class is the attributes plus possibly a special attribute
     *  to indicate whether the port is a multiport.  This method is called
     *  by _exportMoML().  If there are attributes, then
     *  each attribute description is indented according to the specified
     *  depth and terminated with a newline character.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @exception IOException If an I/O error occurs.
     */
    protected void _exportMoMLContents(Writer output, int depth)
            throws IOException {
        if (_isInput) {
            output.write(_getIndentPrefix(depth)
                    + "<property name=\"input\"/>\n");
        }
        if (_isOutput) {
            output.write(_getIndentPrefix(depth)
                    + "<property name=\"output\"/>\n");
        }
        if (_isMulitport) {
            output.write(_getIndentPrefix(depth)
                    + "<property name=\"multiport\"/>\n");
        }
        super._exportMoMLContents(output, depth);
    }

    /** Return the sums of the widths of the relations linked on the inside,
     *  except the specified port.  If any of these relations has not had
     *  its width specified, throw an exception.  This is used by IORelation
     *  to infer the width of a bus with unspecified width and to determine
     *  whether more than one relation with unspecified width is linked on the
     *  inside, and by the liberalLink() method to check validity of the link.
     *  If the argument is null, all relations linked on the inside are checked.
     *  This method is not read-synchronized on the workspace, so the caller
     *  should be.
     *
     *  @param except The relation to exclude.
     */
    protected int _getInsideWidth(IORelation except) {
        int result = 0;
        Iterator relations = insideRelationList().iterator();
        while (relations.hasNext()) {
            IORelation relation = (IORelation)relations.next();
            if (relation != except) {
                if (!relation.isWidthFixed()) {
                    throw new InvalidStateException(this,
                            "Width of inside relations cannot "
                            + "be determined.");
                }
                result += relation.getWidth();
            }
        }
        return result;
    }

    /** Create a new receiver compatible with the local director.
     *  This is done by asking the local director of the container for
     *  a new receiver, and then setting its
     *  container to this port.  This allows actors to work across
     *  several domains, since often the only domain-specific part of
     *  of an actor is its receivers.  Derived classes may choose to
     *  handle this directly, creating whatever specific type of receiver
     *  they want. This method is not read-synchronized
     *  on the workspace, so the caller should be.
     *
     *  @return A new receiver.
     *  @exception IllegalActionException If the port has no container,
     *   or the container is unable to return a new receiver (for example
     *   if it has no local director).
     */
    protected Receiver _newInsideReceiver() throws IllegalActionException {
        ComponentEntity container = (ComponentEntity)getContainer();
        if (container.isOpaque() && !container.isAtomic()) {
            Receiver rec = ((CompositeActor)container).newInsideReceiver();
            rec.setContainer(this);
            return rec;
        }
        throw new IllegalActionException(this,
                "Can only create inside receivers for a port of a non-atomic, "
                + "opaque entity.");
    }

    /** Create a new receiver compatible with the executive director.
     *  This is done by asking the
     *  containing actor for a new receiver, and then setting its
     *  container to this port.  This allows actors to work across
     *  several domains, since often the only domain-specific part of
     *  of an actor is its receivers.  Derived classes may choose to
     *  handle this directly, creating whatever specific type of receiver
     *  they want.  This method is not write-synchronized
     *  on the workspace, so the caller should be.
     *
     *  @return A new receiver.
     *  @exception IllegalActionException If the port has no container,
     *   or the container is unable to return a new receiver (for example
     *   if it has no executive director).
     */
    protected Receiver _newReceiver() throws IllegalActionException {
        Actor container = (Actor)getContainer();
        if (container == null) {
            throw new IllegalActionException(this,
                    "Cannot create a receiver without a container.");
        }
        Receiver rec = container.newReceiver();
        rec.setContainer(this);
        return rec;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // To avoid creating this repeatedly, we use a single version.
    private static final Receiver[][]
    _EMPTY_RECEIVER_ARRAY = new Receiver[0][0];

    // Indicate whether the port is an input, an output, or both.
    // The value may be overridden in transparent ports, in that if
    // a transparent port is inside linked to an input or output port,
    // then it will be considered an inside or output port respectively.
    // This determination is cached, so we need variables to track the
    // validity of the cache.
    // 'transient' means that the variable will not be serialized.
    private boolean _isInput, _isOutput;
    private transient long _insideinputversion = -1;
    private transient long _insideoutputversion = -1;

    // Indicate whether the port is a multiport. Default false.
    private boolean _isMulitport = false;

    // The cached width of the port, which is the sum of the widths of the
    // linked relations.  The default 0 because initially there are no
    // linked relations.  It is set or updated when getWidth() is called.
    // 'transient' means that the variable will not be serialized.
    private transient int _width = 0;
    // The workspace version number on the last update of the _width.
    // 'transient' means that the variable will not be serialized.
    private transient long _widthVersion = -1;

    // A cache of the deeply connected Receivers, and the versions.
    // 'transient' means that the variable will not be serialized.
    private transient Receiver[][] _farReceivers;
    private transient long _farReceiversVersion = -1;

    // A cache of the local Receivers, and the version.
    // 'transient' means that the variable will not be serialized.
    private transient Receiver[][] _localReceivers;
    private transient long _localReceiversVersion = -1;

    // A cache of the local Receivers, and the version.
    // 'transient' means that the variable will not be serialized.
    private transient Receiver[][] _localInsideReceivers;
    private transient long _localInsideReceiversVersion = -1;

    // A cache of the inside Receivers, and the version.
    private transient Receiver[][] _insideReceivers;
    private transient long _insideReceiversVersion = -1;

    // The local receivers, indexed by relation.
    // NOTE: When upgrading to jdk 1.2, we should replace this with HashMap
    // for which access methods are not synchronized.  There is no need to
    // synchronize in this case, and it is costly.
    private Hashtable _localReceiversTable;
}
