/* An IOPort with a type.

 Copyright (c) 1997-2003 The Regents of the University of California.
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

@ProposedRating Green (yuhong@eecs.berkeley.edu)
@AcceptedRating Yellow (neuendor@eecs.berkeley.edu)
review sendInside
*/

package ptolemy.actor;

import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.StructuredType;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeConstant;
import ptolemy.data.type.TypeLattice;
import ptolemy.data.type.Typeable;
import ptolemy.graph.CPO;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// TypedIOPort
/**
An IOPort with a type. This class implements the Typeable interface.
The type is represented by an instance of Type in data.type package.
It can be declared by calling setTypeEquals(). If this method is not
called, or called with a BaseType.UNKNOWN argument, the type of this port
will be set by type resolution using the type constraints. The type
constraints on this port can be specified using the methods defined in
the Typeable interface.

<p>This class keeps a list of TypeListeners. Whenever the type
changes, this class will generate an instance of TypeEvent and pass it
to the listeners by calling their typeChanged() method. A TypeListener
register its interest in the type change event of this port by calling
addTypeListener(), and can be removed from the listener list by calling
the removeTypeListener().

<p>A TypedIOPort can only link to instances of TypedIORelation. Derived
classes may further constrain links to a subclass of TypedIORelation.
To do this, they should override the protected methods _link() and
_linkInside() to throw an exception if their arguments are not of the
appropriate type.  Similarly, an TypeIOPort can only be contained by a
class derived from ComponentEntity and implementing the TypedActor
interface.  Subclasses may further constrain the containers by overriding
_checkContainer().

<p>Note that actors that call some of the setType<i>XXX</i> methods
may also need to have a clone() method.  Although the base classes
neatly handle most aspects of the clone operation, there are
subtleties involved with cloning type constraints. Absolute type
constraints on ports and parameters are carried automatically into the
clone, so clone() methods should never call setTypeEquals(). However,
relative type constraints of the other setType<i>XXX</I>() methods are
not cloned automatically because of the difficulty of ensuring that
the other object being referred to in a relative constraint is the
intended one.
<p> For example the Ramp actor constructor calls:
<pre>
output.setTypeAtLeast(init);
</pre>
so the clone() method of the Ramp actor calls:
<pre>
newObject.output.setTypeAtLeast(newObject.init);
</pre>


@author Yuhong Xiong, Lukito Muliadi
@version $Id$
@since Ptolemy II 0.2
*/

public class TypedIOPort extends IOPort implements Typeable {

    // all the constructors are wrappers of the super class constructors.

    /** Construct a TypedIOPort with no container and no name that is
     *  neither an input nor an output.
     */
    public TypedIOPort() {
        super();
    }

    /** Construct a port in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument
     *  is null, then use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the port.
     */
    public TypedIOPort(Workspace workspace) {
        super(workspace);
    }

    /** Construct a TypedIOPort with a containing actor and a name
     *  that is neither an input nor an output.  The specified container
     *  must implement the TypedActor interface, or an exception will be
     *  thrown.
     *
     *  @param container The container actor.
     *  @param name The name of the port.
     *  @exception IllegalActionException If the port is not of an acceptable
     *   class for the container, or if the container does not implement the
     *   TypedActor interface.
     *  @exception NameDuplicationException If the name coincides with
     *   a port already in the container.
     */
    public TypedIOPort(ComponentEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Construct a TypedIOPort with a container and a name that is
     *  either an input, an output, or both, depending on the third
     *  and fourth arguments. The specified container must implement
     *  the TypedActor interface or an exception will be thrown.
     *
     *  @param container The container actor.
     *  @param name The name of the port.
     *  @param isInput True if this is to be an input port.
     *  @param isOutput True if this is to be an output port.
     *  @exception IllegalActionException If the port is not of an acceptable
     *   class for the container, or if the container does not implement the
     *   TypedActor interface.
     *  @exception NameDuplicationException If the name coincides with
     *   a port already in the container.
     */
    public TypedIOPort(ComponentEntity container, String name,
            boolean isInput, boolean isOutput)
            throws IllegalActionException, NameDuplicationException {
        super(container, name, isInput, isOutput);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a type listener to this port. The listener will
     *  be notified of all the type changes.  If the listener is already
     *  listening to this port, then do nothing.
     *  @param listener The TypeListener to add.
     */
    public void addTypeListener(TypeListener listener) {
        if (!_typeListeners.contains(listener))
            _typeListeners.add(listener);
    }

    /** React to a change in an attribute.  This method is called by
     *  a contained attribute when its value changes.  This overrides
     *  the base class so that if the attribute is an instance of
     *  TypeAttribute, then it sets the type of the port.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute instanceof TypeAttribute) {
            Type type = ((TypeAttribute)attribute).getType();
            if (type != null) {
                setTypeEquals(type);
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Send a token to all connected receivers.
     *  Tokens are in general immutable, so each receiver is given a
     *  reference to the same token and no clones are made.
     *  The transfer is accomplished by calling getRemoteReceivers()
     *  to determine the number of channels with valid receivers and
     *  then calling send() on the appropriate channels.
     *  It would probably be faster to call put() directly on the receivers.
     *  If there are no destination receivers, then nothing is sent.
     *  If the port is not connected to anything, or receivers have not been
     *  created in the remote port, then just return.
     *  <p>
     *  Some of this method is read-synchronized on the workspace.
     *  Since it is possible for a thread to block while executing a put,
     *  it is important that the thread does not hold read access on
     *  the workspace when it is blocked. Thus this method releases
     *  read access on the workspace before calling put.
     *
     *  @param token The token to send
     *  @exception IllegalActionException If the token to be sent cannot
     *   be converted to the type of this port
     *  @exception NoRoomException If a send to one of the channels throws
     *     it.
     */
    public void broadcast(Token token)
            throws IllegalActionException, NoRoomException {
        Receiver[][] farReceivers;
        if (_debugging) {
            _debug("broadcast " + token);
        }
        try {
            _workspace.getReadAccess();
            _checkType(token);
            farReceivers = getRemoteReceivers();
            if (farReceivers == null) {
                return;
            }
        } finally {
            _workspace.doneReading();
        }
        // NOTE: This does not call send() here, because send()
        // repeats the above on each call.
        for (int i = 0; i < farReceivers.length; i++) {
            if (farReceivers[i] == null) continue;

            for (int j = 0; j < farReceivers[i].length; j++) {
                TypedIOPort port =
                    (TypedIOPort)farReceivers[i][j].
                    getContainer();
                Token newToken = port.convert(token);
                farReceivers[i][j].put(newToken);
            }
        }
    }


    /** Send the specified portion of a token array to all receivers connected
     *  to this port. The first <i>vectorLength</i> tokens
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
     *  @param tokenArray The token array to send
     *  @param vectorLength The number of elements of the token
     *   array to send.
     *  @exception NoRoomException If there is no room in the receiver.
     *  @exception IllegalActionException If the tokens to be sent cannot
     *   be converted to the type of this port
     */
    public void broadcast(Token[] tokenArray, int vectorLength)
            throws IllegalActionException, NoRoomException {
        Receiver[][] farReceivers;
        if (_debugging) {
            _debug("broadcast token array of length " + vectorLength);
        }
        Token token = null;
        try {
            _workspace.getReadAccess();
            // check types
            for (int i = 0; i < tokenArray.length; i++) {
                token = tokenArray[i];
                _checkType(token);
            }
            farReceivers = getRemoteReceivers();
            if (farReceivers == null) {
                return;
            }
        } finally {
            _workspace.doneReading();
        }
        // NOTE: This does not call send() here, because send()
        // repeats the above on each call.
        for (int i = 0; i < farReceivers.length; i++) {
            if (farReceivers[i] == null) continue;

            for (int j = 0; j < farReceivers[i].length; j++) {
                TypedIOPort port =
                    (TypedIOPort)farReceivers[i][j].getContainer();
                Type farType = port.getType();

                boolean needConversion = false;
                for (int k = 0; k < tokenArray.length; k++) {
                    if ( !farType.equals(tokenArray[k].getType())) {
                        needConversion = true;
                    }
                }

                if (!needConversion) {
                    // Good, no conversion necessary.
                    farReceivers[i][j].putArray(tokenArray, vectorLength);
                } else {
                    // Note: This is very bad for performance!
                    // For better efficiency, make sure
                    // all ports have the same type.
                    for (int k = 0; k < vectorLength; k++) {
                        farReceivers[i][j].put(port.convert(tokenArray[k]));
                    }
                }
            }
        }
    }

    /** Clone this port into the specified workspace. The new port is
     *  <i>not</i> added to the directory of that workspace (you must
     *  do this yourself if you want it there).
     *  The result is a new port with no connections and no container.
     *  The new port will have the same type as this one, but will not
     *  have any type listeners and type constraints attached to it.
     *
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one or more of the
     *   attributes cannot be cloned.
     *  @return A new TypedIOPort.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        TypedIOPort newObject = (TypedIOPort)super.clone(workspace);
        // set _declaredType and _resolvedType
        if (_declaredType instanceof StructuredType &&
                !_declaredType.isConstant()) {
            newObject._declaredType =
                (Type)((StructuredType)_declaredType).clone();
            newObject._resolvedType = newObject._declaredType;
        }

        newObject._typeTerm = null;
        newObject._typeListeners = new LinkedList();
        newObject._constraints = new LinkedList();
        return newObject;
    }
    
    /** Convert the specified token into a token with type equal
     *  to the type returned by getType(). If the token is already
     *  of this type, then simply return the specified token.
     *  @param token The token to convert.
     *  @exception IllegalActionException If the convertion is
     *   invalid.
     */
    public Token convert(Token token) throws IllegalActionException {
        Type type = getType();
        if (type.equals(token.getType())) {
            return token;
        } else {
            Token newToken = type.convert(token);
            return newToken;
        }
    }

    /** Return the type of this port.  If this port is opaque, this method
     *  returns the resolved type of this port; if this port is a transparent
     *  input port, this method returns the greatest lower bound of the types
     *  of the inside ports; if this port is a transparent output port, this
     *  method returns the least upper bound of the types of the inside ports.
     *  This method is read-synchronized on the workspace.
     *  @return An instance of Type.
     */
    public Type getType() {
        try {
            _workspace.getReadAccess();
            Type result = BaseType.UNKNOWN;
            if (isOpaque()) {
                result = _resolvedType;
            } else if (isInput()) {
                // is a transparent input port. Get all the ports connected
                // on the inside through receivers.
                Receiver[][] receivers = this.deepGetReceivers();
                List portTypeList = new LinkedList();
                if (receivers != null) {
                    for (int i = 0; i < receivers.length; i++) {
                        if (receivers[i] != null) {
                            for (int j = 0; j < receivers[i].length; j++) {
                                TypedIOPort port =
                                    (TypedIOPort)receivers[i][j].getContainer();
                                portTypeList.add(port.getType());
                            }
                        }
                    }
                }

                CPO lattice = TypeLattice.lattice();
                Object[] portTypeArray = portTypeList.toArray();
                result = (Type)lattice.greatestLowerBound(portTypeArray);
            } else if (isOutput()) {
                // is a transparent output port. Get all the ports connected
                // on the inside through deepInsidePortList().
                Iterator ports = deepInsidePortList().iterator();
                List portTypeList = new LinkedList();
                while (ports.hasNext()) {
                    TypedIOPort port = (TypedIOPort) ports.next();
                    // Rule out case where this port itself is listed...
                    if (port != this && port.isOutput()) {
                        portTypeList.add(port.getType());
                    }
                }

                CPO lattice = TypeLattice.lattice();
                Object[] portTypeArray = portTypeList.toArray();
                result = (Type)lattice.leastUpperBound(portTypeArray);
            }
            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Return an InequalityTerm encapsulating the type of
     *  this port. The InequalityTerm can be used to form type constraints.
     *  If the type is set through setTypeEquals(), the inequality
     *  term represents a type constant; otherwise, it represents a
     *  type variable.
     *  @return An InequalityTerm whose value is the type of this port.
     */
    public InequalityTerm getTypeTerm() {
        if (_typeTerm == null) {
            _typeTerm = new TypeTerm();
        }
        return _typeTerm;
    }

    /** Check whether the current type of this port is acceptable.
     *  A type is acceptable if it represents an instantiable object.
     *  Any type is acceptable (including non-instantiable types)
     *  if the associated port is not connected to anything.
     *  @return True if the current type is acceptable.
     */
    public boolean isTypeAcceptable() {
        if (this.getType().isInstantiable()) {
            return true;
        }
        // For a disconnected port, any type is acceptable.
        if (this.numLinks() == 0) {
            return true;
        }
        return false;
    }

    /** Remove a type listener from this port.  If the listener is
     *  not attached to this port, do nothing.
     *  @param listener The TypeListener to be removed.
     */
    public void removeTypeListener(TypeListener listener) {
        if (_typeListeners.contains(listener))
            _typeListeners.remove(listener);
    }

    /** Send a token to the specified channel, checking the type
     *  and converting the token if necessary.
     *  If the port is not connected to anything, or receivers have not been
     *  created in the remote port, or the channel index is out of
     *  range, or the port is not an output port,
     *  then just silently return.  This behavior makes it
     *  easy to leave output ports unconnected when you are not interested
     *  in the output.
     *  If the type of the specified token is the type of this
     *  port, or the token can be converted to that type
     *  losslessly, the token is sent to all receivers connected to the
     *  specified channel. Otherwise, IllegalActionException is thrown.
     *  Before putting the token into the destination receivers, this
     *  method also checks the type of the remote input port,
     *  and converts the token if necessary.
     *  The conversion is done by calling the
     *  convert() method of the type of the remote input port.
     *  <p>
     *  Some of this method is read-synchronized on the workspace.
     *  Since it is possible for a thread to block while executing a put,
     *  it is important that the thread does not hold read access on
     *  the workspace when it is blocked. Thus this method releases
     *  read access on the workspace before calling put.
     *
     *  @param channelIndex The index of the channel, from 0 to width-1.
     *  @param token The token to send.
     *  @exception IllegalActionException If the token to be sent cannot
     *   be converted to the type of this port, or if the token is null.
     *  @exception NoRoomException If there is no room in the receiver.
     */
    public void send(int channelIndex, Token token)
            throws IllegalActionException, NoRoomException {
        if (token == null) {
            throw new IllegalActionException(this,
                    "Cannot send a null token.");
        }
        Receiver[][] farReceivers;
        if (_debugging) {
            _debug("send to channel " + channelIndex + ": " + token);
        }
        try {
            try {
                _workspace.getReadAccess();
                _checkType(token);

                // Note that the getRemoteReceivers() method doesn't throw
                // any non-runtime exception.
                farReceivers = getRemoteReceivers();
                if (farReceivers == null
                        || farReceivers.length <= channelIndex
                        || farReceivers[channelIndex] == null) {
                    return;
                }
            } finally {
                _workspace.doneReading();
            }

            for (int j = 0; j < farReceivers[channelIndex].length; j++) {
                TypedIOPort port =
                    (TypedIOPort)farReceivers[channelIndex][j].getContainer();
                Token newToken = port.convert(token);
                farReceivers[channelIndex][j].put(newToken);
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            // NOTE: This may occur if the channel index is out of range.
            // This is allowed, just do nothing.
        }
    }

    /** Send the specified portion of a token array to all receivers
     *  connected to the specified channel, checking the type
     *  and converting the token if necessary. The first
     *  <i>vectorLength</i> tokens of the token array are sent.
     *  If the port is not connected to anything, or receivers have not been
     *  created in the remote port, or the channel index is out of
     *  range, or the port is not an output port,
     *  then just silently return.  This behavior makes it
     *  easy to leave output ports unconnected when you are not interested
     *  in the output.
     *  <p>
     *  To improve efficiency for the common case where the type of the
     *  tokens to send matches the type of this port and all connected
     *  ports, this method assumes that all of the tokens in the
     *  specified portion of the token array are of the same
     *  type. If this is not the case, then the non-vectorized send()
     *  method should be used instead.
     *  The implementation only actually checks the
     *  type of the first token in the array, and then assumes that
     *  the remaining tokens are of the same type.
     *  <p>
     *  If the type of the tokens in the specified portion of the
     *  token array is the type of this
     *  port, or the tokens in the specified portion of the
     *  token array can be converted to that type
     *  losslessly, the tokens in the specified portion of the
     *  token array are sent to all receivers connected to the
     *  specified channel. Otherwise, IllegalActionException is thrown.
     *  Before putting the tokens in the specified portion of the
     *  token array into the destination receivers, this
     *  method also checks the type of the remote input port,
     *  and converts the tokens if necessary.
     *  The conversion is done by calling the
     *  convert() method of the type of the remote input port.
     *  <p>
     *  Some of this method is read-synchronized on the workspace.
     *  Since it is possible for a thread to block while executing a put,
     *  it is important that the thread does not hold read access on
     *  the workspace when it is blocked. Thus this method releases
     *  read access on the workspace before calling put.
     *
     *  @param channelIndex The index of the channel, from 0 to width-1
     *  @param tokenArray The token array to send
     *  @param vectorLength The number of elements of the token
     *   array to send.
     *  @exception NoRoomException If there is no room in the receiver.
     *  @exception IllegalActionException If the tokens to be sent cannot
     *   be converted to the type of this port, or if the <i>vectorLength</i>
     *   argument is greater than the length of the <i>tokenArray</i>
     *   argument.
     */
    public void send(int channelIndex, Token[] tokenArray, int vectorLength)
            throws IllegalActionException, NoRoomException {
        if (vectorLength > tokenArray.length) {
            throw new IllegalActionException(this,
                    "Not enough data supplied to send specified number of samples.");
        }
        Receiver[][] farReceivers;
        if (_debugging) {
            _debug("send to channel " + channelIndex
                    + " token array of length " + vectorLength);
        }
        Token token = null;
        try {
            try {
                _workspace.getReadAccess();
                // check types
                for (int i = 0; i < vectorLength; i++) {
                    token = tokenArray[i];
                    _checkType(token);
                }
                // Note that the getRemoteReceivers() method doesn't throw
                // any non-runtime exception.
                farReceivers = getRemoteReceivers();
                if (farReceivers == null ||
                        farReceivers[channelIndex] == null) {
                    return;
                }
            } finally {
                _workspace.doneReading();
            }
            for (int j = 0; j < farReceivers[channelIndex].length; j++) {
                TypedIOPort port =
                    (TypedIOPort)farReceivers[channelIndex][j].getContainer();
                Type farType = port.getType();

                boolean needConversion = false;
                for (int k = 0; k < vectorLength; k++) {
                    if ( !farType.equals(tokenArray[k].getType())) {
                        needConversion = true;
                    }
                }

                if ( !needConversion) {
                    // Good, no conversion necessary.
                    farReceivers[channelIndex][j].putArray(tokenArray,
                            vectorLength);
                } else {
                    // Note: This is very bad for performance!
                    // For better efficiency, make sure
                    // all ports have the same type.
                    for (int i = 0; i < vectorLength; i++) {
                        farReceivers[channelIndex][j].put(
                                port.convert(tokenArray[i]));
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            // NOTE: This may occur if the channel index is out of range.
            // This is allowed, just do nothing.
        }
    }

    /** Send the specified token to all receivers connected to the
     *  specified inside channel of this port, checking the type and
     *  converting the token if necessary.  Tokens are in general
     *  immutable, so each receiver is given a reference to the same
     *  token and no clones are made.  If the port is not connected to
     *  anything on the inside, or receivers have not been created in
     *  the remote port, or the channel index is out of range, or the
     *  port is not an input port, then just silently return.  This
     *  behavior makes it easy to leave external input ports of a
     *  composite unconnected when you are not interested in the
     *  received values.  The transfer is accomplished by calling the
     *  put() method of the inside remote receivers.  If the port is
     *  not connected to anything, or receivers have not been created
     *  in the remote port, then just return.  This method is normally
     *  called only by the transferInputs method of directors of
     *  composite actors, as AtomicActors do not usually have any
     *  relations on the inside of their ports.  Before putting the
     *  token into the destination receivers, this method also checks
     *  the type of the inside input port, and converts the token if
     *  necessary.  The conversion is done by calling the convert()
     *  method of the type of the inside input port.
     *
     *  <p> Some of this method is read-synchronized on the workspace.
     *  Since it is possible for a thread to block while executing a
     *  put, it is important that the thread does not hold read access
     *  on the workspace when it is blocked. Thus this method releases
     *  read access on the workspace before calling put.
     *
     *  @param channelIndex The index of the channel, from 0 to width-1
     *  @param token The token to send
     *  @exception NoRoomException If there is no room in the receiver.
     *  @exception IllegalActionException If conversion to the type of
     *   the destination port cannot be done.
     */
    public void sendInside(int channelIndex, Token token)
            throws IllegalActionException, NoRoomException {
        Receiver[][] farReceivers;
        if (_debugging) {
            _debug("send inside to channel " + channelIndex + ": " + token);
        }
        try {
            try {
                _workspace.getReadAccess();
                _checkType(token);

                // Note that the getRemoteReceivers() method doesn't throw
                // any non-runtime exception.
                farReceivers = deepGetReceivers();
                if (farReceivers == null ||
                        farReceivers[channelIndex] == null) return;
            } finally {
                _workspace.doneReading();
            }
            for (int j = 0; j < farReceivers[channelIndex].length; j++) {
                TypedIOPort port =
                    (TypedIOPort)farReceivers[channelIndex][j].getContainer();
                Token newToken = port.convert(token);
                farReceivers[channelIndex][j].put(newToken);
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            // NOTE: This may occur if the channel index is out of range.
            // This is allowed, just do nothing.
        }
    }

    /** Constrain the type of this port to be equal to or greater
     *  than the type of the specified Typeable object.
     *  <p>Actors that call this method should have a clone() method that
     *  repeats the relative type constraints that were specified in
     *  the constructor.
     *  @param lesser A Typeable object.
     */
    public void setTypeAtLeast(Typeable lesser) {
        Inequality inequality = new Inequality(lesser.getTypeTerm(),
                this.getTypeTerm());
        _constraints.add(inequality);
    }

    /** Constrain the type of this port to be equal to or greater
     *  than the type represented by the specified InequalityTerm.
     *  <p>Actors that call this method should have a clone() method that
     *  repeats the relative type constraints that were specified in
     *  the constructor.
     *  @param typeTerm An InequalityTerm.
     */
    public void setTypeAtLeast(InequalityTerm typeTerm) {
        Inequality inequality = new Inequality(typeTerm, this.getTypeTerm());
        _constraints.add(inequality);
    }

    /** Constrain the type of this port to be equal to or less
     *  than the argument.
     *  <p>Actors that call this method should have a clone() method that
     *  repeats the relative type constraints that were specified in
     *  the constructor.
     */
    public void setTypeAtMost(Type type) {
        Inequality inequality = new Inequality(this.getTypeTerm(),
                new TypeConstant(type));
        _constraints.add(inequality);
    }

    /** Set the type of this port. The type is represented by an instance
     *  of Type. If the type is BaseType.UNKNOWN, the determination of the type
     *  is left to type resolution.
     *
     *  <p>Actors that call setTypeEquals() are not required to have a
     *  clone() method.  Absolute type constraints on ports and
     *  parameters are carried automatically into the clone, so
     *  clone() methods of actors should never call setTypeEquals().
     *  Actors that call the other setType<i>XXX</i>() methods should
     *  have a clone() method.
     *
     *  <p> This method is write-synchronized on the workspace.
     *  @param type A Type.
     */
    public void setTypeEquals(Type type) {
        try {
            _workspace.getWriteAccess();

            try {
                _declaredType = (Type)type.clone();
            } catch (CloneNotSupportedException cloneNotSupported) {
                throw new InternalErrorException(this, cloneNotSupported,
                        "TypedIOPort.setTypeEquals: Cannot clone type");
            }

            // Note: we are careful here to set the type before notifying
            // type listeners.
            Type oldType = _resolvedType;
            _resolvedType = _declaredType;
            if (!oldType.equals(_declaredType)) {
                _notifyTypeListener(oldType, _declaredType);
            }
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Constrain the type of this port to be the same as the type
     *  of the specified Typeable object.
     *  <p>Actors that call this method should have a clone() method that
     *  repeats the relative type constraints that were specified in
     *  the constructor.
     *  @param equal A Typeable object.
     */
    public void setTypeSameAs(Typeable equal) {
        Inequality inequality = new Inequality(this.getTypeTerm(),
                equal.getTypeTerm());
        _constraints.add(inequality);
        inequality = new Inequality(equal.getTypeTerm(),
                this.getTypeTerm());
        _constraints.add(inequality);
    }

    /** Return the type constraints of this port in the form of a
     *  list of inequalities.
     *  @return A list of inequalities.
     *  @see ptolemy.graph.Inequality
     */
    public List typeConstraintList() {
        return _constraints;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Indicate that the description(int) method should include
     *  information about the type of this port.
     */
    public static final int TYPE = 4096;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class to ensure that the proposed container
     *  implements the TypedActor interface (the base class ensures that
     *  the container implements the Actor interface) or null.
     *
     *  @param container The proposed container.
     *  @exception IllegalActionException If the proposed container is not a
     *   TypedActor, or if the base class throws it.
     */
    protected void _checkContainer(Entity container)
            throws IllegalActionException {
        if (!(container instanceof TypedActor) && (container != null)) {
            throw new IllegalActionException(container, this,
                    "TypedIOPort can only be contained by objects " +
                    "implementing the TypedActor interface.");
        }
    }

    /** Override the method in the super class to ensure compatibility of
     *  the relation. This method is <i>not</i> synchronized on the
     *  workspace, so the caller should be.
     *
     *  @param relation The relation to link to on the inside.
     *  @exception IllegalActionException If this port has no container or
     *   the relation is not an TypedIORelation, or the port already linked
     *   to a relation and is not a multiport, or the relation has width
     *   not exactly one and the port is not a multiport, or the
     *   relation is incompatible with this port, or the port is not
     *   in the same workspace as the relation.
     */
    protected void _checkLiberalLink(ComponentRelation relation)
            throws IllegalActionException {
        if (!(relation instanceof TypedIORelation)) {
            throw new IllegalActionException(this, relation,
                    "Attempt to link to an incompatible relation." +
                    " TypedIOPort requires TypedIORelation.");
        }
        super._checkLiberalLink((TypedIORelation)relation);
    }

    /** Override the method in the super class to ensure compatibility of
     *  the relation. This method is <i>not</i> synchronized on the
     *  workspace, so the caller should be.
     *
     *  @param relation The relation to link to.
     *  @exception IllegalActionException If this port has no container or
     *   the relation is not an TypedIORelation, or the port already linked
     *   to a relation and is not a multiport, or if the relation has width
     *   not exactly one and the port is not a multiport, or the port is
     *   not in the same workspace as the relation.
     */
    protected void _checkLink(Relation relation)
            throws IllegalActionException {
        if (!(relation instanceof TypedIORelation)) {
            throw new IllegalActionException(this, relation,
                    "Attempt to link to an incompatible relation." +
                    " TypedIOPort requires TypedIORelation.");
        }
        super._checkLink((TypedIORelation)relation);
    }

    /** Check that the specified token is compatible with the
     *  resolved type of this port.     * 
     *  @param token The token to check.
     *  @throws IllegalActionException If the specified token is
     *   either incomparable to the resolved type or higher in the
     *   type lattice.
     */
    protected void _checkType(Token token) throws IllegalActionException {
        int compare = TypeLattice.compare(token.getType(),
                _resolvedType);
        if (compare == CPO.HIGHER || compare == CPO.INCOMPARABLE) {
            throw new IllegalActionException(
                    "Run-time type checking failed. Token type: "
                    + token.getType().toString() + ", port: "
                    + getFullName() + ", port type: "
                    + getType().toString());
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
     *  TYPE, then append to the description a field of the form
     *  "type {declared <i>declaredType</i> resolved <i>resolvedType</i>}".
     *  The declared type is the type set through setTypeEquals(). If this
     *  method is not called, the declared type is BaseType.UNKNOWN.
     *  The resolved type is the type of this port.  Both types are
     *  represented by the names of the corresponding tokens.
     *  <p>
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

            if ((detail & TYPE) != 0) {
                if (result.trim().length() > 0) {
                    result += " ";
                }
                result += "type {declared ";
                result += _declaredType.toString();
                result += " resolved ";
                result += getType().toString();
                result += "}";
            }
            if (bracket == 2) result += "}";
            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Notify the type listener about type change.

    private void _notifyTypeListener(Type oldType, Type newType) {

        if (_typeListeners.size() > 0) {
            TypeEvent event = new TypeEvent(this, oldType, newType);
            Iterator listeners = _typeListeners.iterator();
            while (listeners.hasNext()) {
                ((TypeListener)listeners.next()).typeChanged(event);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Type _declaredType = BaseType.UNKNOWN;
    private Type _resolvedType = BaseType.UNKNOWN;

    private TypeTerm _typeTerm = null;

    // Listeners for type change.
    private List _typeListeners = new LinkedList();

    // type constraints
    private List _constraints = new LinkedList();

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    private class TypeTerm implements InequalityTerm {

        ///////////////////////////////////////////////////////////////
        ////                       public inner methods            ////

        /** Return this TypedIOPort.
         *  @return A TypedIOPort.
         */
        public Object getAssociatedObject() {
            return TypedIOPort.this;
        }

        /** Return the type of this TypedIOPort.
         */
        public Object getValue() {
            return getType();
        }

        /** Return this TypeTerm in an array if this term represent
         *  a type variable. This term represents a type variable
         *  if the type of this port is not set through setTypeEquals().
         *  If the type of this port is set, return an array of size zero.
         *  @return An array of InequalityTerm.
         */
        public InequalityTerm[] getVariables() {
            if (isSettable()) {
                InequalityTerm[] variable = new InequalityTerm[1];
                variable[0] = this;
                return variable;
            }
            return (new InequalityTerm[0]);
        }

        /** Reset the variable part of this type to the specified type.
         *  @param type A Type.
         *  @exception IllegalActionException If the type is not settable,
         *   or the argument is not a Type.
         */
        public void initialize(Object type)
                throws IllegalActionException {
            if ( !isSettable()) {
                throw new IllegalActionException("TypeTerm.initialize: " +
                        "Cannot initialize a constant type.");
            }

            if ( !(type instanceof Type)) {
                throw new IllegalActionException("TypeTerm.initialize: " +
                        "The argument is not a Type.");
            }

            Type oldType = _resolvedType;
            if (_declaredType == BaseType.UNKNOWN) {
                _resolvedType = (Type)type;
            } else {
                // _declaredType is a StructuredType
                ((StructuredType)_resolvedType).initialize((Type)type);
            }

            if (!oldType.equals(_resolvedType)) {
                _notifyTypeListener(oldType, _resolvedType);
            }
        }

        /** Test if the type of this TypedIOPort can be changed.
         *  The type can be changed if setTypeEquals() is not called,
         *  or called with a BaseType.UNKNOWN argument.
         *  @return True if the type of this TypedIOPort can be changed;
         *   false otherwise.
         */
        public boolean isSettable() {
            return !_declaredType.isConstant();
        }

        /** Check whether the current value of this term is acceptable.
         *  This method delegates the check to the isTypeAcceptable()
         *  method of the outer class.
         *  @return True if the current value is acceptable.
         */
        public boolean isValueAcceptable() {
            return isTypeAcceptable();
        }

        /** Set the type of this port.
         *  @parameter type A Type.
         *  @exception IllegalActionException If the new type violates
         *   the declared type of this port.
         */
        public void setValue(Object type) throws IllegalActionException {
            if ( !isSettable()) {
                throw new IllegalActionException(
                        "TypedIOPort$TypeTerm.setValue: The type is not "
                        + "settable.");
            }

            if ( !_declaredType.isSubstitutionInstance((Type)type)) {
                throw new IllegalActionException(
                        "Type conflict on port "
                        + TypedIOPort.this.getFullName() + ".\n"
                        + "Declared type is " + _declaredType.toString()
                        + ".\n"
                        + "The connection or type constraints, however, "
                        + "require type " + type.toString());
            }

            Type oldType = _resolvedType;
            if (_declaredType == BaseType.UNKNOWN) {
                _resolvedType = (Type)type;
            } else {
                // _declaredType is a StructuredType
                ((StructuredType)_resolvedType).updateType(
                        (StructuredType)type);
            }

            if (!oldType.equals((Type)type)) {
                _notifyTypeListener(oldType, _resolvedType);
            }
        }

        /** Override the base class to give a description of the port
         *  and its type.
         *  @return A description of the port and its type.
         */
        public String toString() {
            return "(" + TypedIOPort.this.toString() + ", " + getType() + ")";
        }
    }
}
