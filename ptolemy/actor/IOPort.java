/* A port supporting message passing.

 Copyright (c) 1997-2014 The Regents of the University of California.
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

 Review vectorized methods.
 Review broadcast/get/send/hasRoom/hasToken.
 Review setInput/setOutput/setMultiport.
 Review isKnown/broadcastClear/sendClear.
 createReceivers creates inside receivers based solely on insideWidth, and
 outsideReceivers based solely on outside width.
 connectionsChanged: no longer validates the attributes of this port.  This is
 now done in Manager.initialize().
 Review sendInside, getInside, getWidthInside, transferInputs/Outputs, etc.
 */
package ptolemy.actor;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Decorator;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
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
 If this port is at the boundary of an composite actor, then it
 can have both inside and outside links, with corresponding inside
 and outside receivers if it opaque. The inside links are to
 relations inside the opaque composite actor, whereas the outside
 links are to relations outside. If it is not specified, then a link
 is an outside link.

 <p>
 The port has a <i>defaultValue</i> parameter that, by default, is
 empty. If this parameter is not empty, the port always has a token.
 The value of the port is initially specified by the defaultValue.
 Afterwards, the previous token of the port is remembered.

 <p>
 The port has a <i>width</i>, which by default is constrained to
 be either zero or one.
 The width is the sum of the widths of the linked relations.
 A port with a width greater than one behaves as a bus interface,
 so if the width is <i>w</i>, then the port can simultaneously
 handle <i>w</i> distinct input or output channels of data.

 <p>
 In general, an input port might have more than one receiver for
 each channel.  This occurs particularly for transparent input ports,
 which treat the receivers of the ports linked on the inside as its own.
 This might also occur for opaque ports in some derived classes.
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
 more or less than the width.  If it is more, then the excess inside
 relations will be treated as if they are unconnected.  If it is
 less, then the excess outside relations will be treated as if they
 are unconnected.

 <p>
 An IOPort can only link to instances of IORelation. Derived classes
 may further constrain links to a subclass of IORelation.  To do
 this, they should override the protected methods _checkLink() and
 _checkLiberalLink() to throw an exception if their arguments are
 not of the appropriate type.  Similarly, an IOPort can only be
 contained by a class derived from ComponentEntity and implementing
 the Actor interface.  Subclasses may further constrain the
 containers by overriding the protected method _checkContainer().

 @author Edward A. Lee, Jie Liu, Neil Smyth, Lukito Muliadi, Contributor: Bert Rodiers
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Red (neuendor)
 */
public class IOPort extends ComponentPort {

    /** Construct an IOPort with no container and no name that is
     *  neither an input nor an output.
     */
    public IOPort() {
        super();
        try {
            _init();
        } catch (IllegalActionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /** Construct a port in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument
     *  is null, then use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the port.
     *  @exception IllegalActionException If thrown by the superclass
     *  or while initializing 
     */
    public IOPort(Workspace workspace) throws IllegalActionException {
        super(workspace);
        _init();
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
        _init();
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
    public IOPort(ComponentEntity container, String name, boolean isInput,
            boolean isOutput) throws IllegalActionException,
            NameDuplicationException {
        this(container, name);
        setInput(isInput);
        setOutput(isOutput);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public parameters                 ////

    /** The default value of the port. By default, this parameter is
     *  empty. If this value is not empty, then the port is persistent,
     *  which means that the get methods always return a token (they never
     *  throw NoTokenException), and that {@link #hasToken(int)},
     *  {@link #hasToken(int, int)},
     *  and {@link #hasTokenInside(int)}
     *  always return true, indicating that a token is available.
     *  To determine whether there is a new token, use
     *  {@link #hasNewToken(int)} or {@link #hasNewTokenInside(int)}.
     *  <p>
     *  If this port is an output port, then the persistent value is
     *  used only when retrieving a token from the inside. I.e., it
     *  will be used only if the output port belongs to an opaque
     *  composite actor.
     */
    public Parameter defaultValue;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Append a listener to the current set of port event listeners.
     *  If the listener is already in the set, it will not be added again.
     *  Note that this method is basically the same as addDebugListener
     *  in the class NamedObj.
     *  @param listener The listener to which to send token sent messages.
     *  @see #removeIOPortEventListener(IOPortEventListener)
     */
    public void addIOPortEventListener(IOPortEventListener listener) {
        // NOTE: This method needs to be synchronized to prevent two
        // threads from each creating a new _portEventListeners list.
        synchronized (this) {
            if (_portEventListeners == null) {
                // Sean Riddle writes: "I am writing an
                // IOPortEventListener that under certain
                // circumstances removes itself, so it is not notified
                // again. This triggers a
                // ConcurrentModificationException, but this can be
                // avoided with the attached patch. If one listener
                // removes another listener that has not been called
                // yet, then that listener will be called one last
                // time in that iteration through the now-stale copy."
                //
                // See IOPortEventListener-1.1 in test/IOPortEventListener.tcl
                _portEventListeners = new CopyOnWriteArrayList<IOPortEventListener>();
            }
        }

        // NOTE: This has to be synchronized to prevent
        // concurrent modification exceptions.
        synchronized (_portEventListeners) {
            if (_portEventListeners.contains(listener)) {
                return;
            } else {
                _portEventListeners.add(listener);
            }

            _hasPortEventListeners = true;
        }
    }

    /** If a communication aspect is added, removed or modified,
     *  invalidate the list of communication aspects which is read again
     *  in the preinitialize phase.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the new color
     *      attribute cannot be created.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute instanceof ExecutionAttributes) {
            Decorator decorator = ((ExecutionAttributes) attribute)
                    .getDecorator();
            if (decorator != null && decorator instanceof CommunicationAspect) {
                if (isOpaque()) {
                    createReceivers();
                }
            }
        } else if (attribute == defaultValue) {
        	_persistentToken = defaultValue.getToken();
        }
        super.attributeChanged(attribute);
    }

    /** Send a token to all connected receivers.
     *  Tokens are in general immutable, so each receiver is given a
     *  reference to the same token and no clones are made.
     *  The transfer is accomplished by calling getRemoteReceivers()
     *  to determine the number of channels with valid receivers and
     *  then putting the token into the receivers.
     *  If there are no destination receivers, then nothing is sent.
     *  If the port is not connected to anything, or receivers have not been
     *  created in the remote port, then just return.
     *  <p>
     *  Some of this method is read-synchronized on the workspace.
     *  Since it is possible for a thread to block while executing a put(),
     *  it is important that the thread does not hold read access on
     *  the workspace when it is blocked. Thus this method releases
     *  read access on the workspace before calling put().
     *
     *  @param token The token to send
     *  @exception IllegalActionException Not thrown in this base class.
     *  @exception NoRoomException If a send to one of the channels throws
     *     it.
     */
    public void broadcast(Token token) throws IllegalActionException,
            NoRoomException {
        Receiver[][] farReceivers;

        if (_debugging) {
            _debug("broadcast " + token);
        }

        if (_hasPortEventListeners) {
            _notifyPortEventListeners(new IOPortEvent(this,
                    IOPortEvent.SEND_BEGIN, IOPortEvent.ALLCHANNELS, true,
                    token));
        }

        try {
            try {
                _workspace.getReadAccess();
                farReceivers = getRemoteReceivers();

                if (farReceivers == null) {
                    return;
                }
            } finally {
                _workspace.doneReading();
            }

            // NOTE: This does not call send() here, because send()
            // repeats the above on each call.
            for (Receiver[] farReceiver : farReceivers) {
                if (farReceiver == null) {
                    continue;
                }

                if (farReceiver.length > 0) {
                    // Delegate to the receiver to handle putting to all
                    // receivers, since domain-specific techniques might be relevant.
                    farReceiver[0].putToAll(token, farReceiver);
                }
            }
        } finally {
            if (_hasPortEventListeners) {
                _notifyPortEventListeners(new IOPortEvent(this,
                        IOPortEvent.SEND_END, IOPortEvent.ALLCHANNELS, true,
                        token));
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
     *  read access on the workspace before calling put().
     *
     *  @param tokenArray The token array to send
     *  @param vectorLength The number of elements of the token
     *   array to send.
     *  @exception NoRoomException If there is no room in the receiver.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void broadcast(Token[] tokenArray, int vectorLength)
            throws IllegalActionException, NoRoomException {
        Receiver[][] farReceivers;

        if (_debugging) {
            _debug("broadcast token array of length " + vectorLength);
        }

        if (_hasPortEventListeners) {
            _notifyPortEventListeners(new IOPortEvent(this,
                    IOPortEvent.SEND_BEGIN, IOPortEvent.ALLCHANNELS, true,
                    tokenArray, vectorLength));
        }

        try {
            try {
                _workspace.getReadAccess();
                farReceivers = getRemoteReceivers();

                if (farReceivers == null) {
                    return;
                }
            } finally {
                _workspace.doneReading();
            }

            // NOTE: This does not call send() here, because send()
            // repeats the above on each call.
            for (Receiver[] farReceiver : farReceivers) {
                if (farReceiver == null) {
                    continue;
                }

                if (farReceiver.length > 0) {
                    // Delegate to the receiver to handle putting to all
                    // receivers, since domain-specific techniques might be relevant.
                    farReceiver[0].putArrayToAll(tokenArray, vectorLength,
                            farReceiver);
                }
            }
        } finally {
            if (_hasPortEventListeners) {
                _notifyPortEventListeners(new IOPortEvent(this,
                        IOPortEvent.SEND_END, IOPortEvent.ALLCHANNELS, true,
                        tokenArray, vectorLength));
            }
        }
    }

    /** Set all receivers connected on the outside to have no
     *  tokens. The transfer is accomplished by calling clear() on the
     *  appropriate receivers.  If there are no destination receivers,
     *  or if this is not an output port, then do nothing.  Some of
     *  this method is read-synchronized on the workspace.
     *  @see #sendClear(int )
     *  @exception IllegalActionException If a receiver does not support
     *   clear().
     */
    public void broadcastClear() throws IllegalActionException {
        Receiver[][] farReceivers;

        if (_debugging) {
            _debug("broadcast clear.");
        }

        try {
            _workspace.getReadAccess();
            farReceivers = getRemoteReceivers();

            if (farReceivers == null) {
                return;
            }
        } finally {
            _workspace.doneReading();
        }

        // NOTE: Conceivably, clear() in some domains may block,
        // so we make sure to release read access above before calling
        // clear().
        for (Receiver[] farReceiver : farReceivers) {
            if (farReceiver == null) {
                continue;
            }

            for (int j = 0; j < farReceiver.length; j++) {
                farReceiver[j].clear();
            }
        }
    }

    /**
     * Check whether the widths constraints are met.
     * @exception IllegalActionException If the width constraints or not met.
     */
    public void checkWidthConstraints() throws IllegalActionException {
        int width = _getOutsideWidth(null);
        for (Parameter parameter : _widthEqualToParameter) {
            IntToken t = (IntToken) parameter.getToken();

            if (t != null) {
                if (width != t.intValue()) {
                    throw new IllegalActionException(
                            this,
                            "The outside width ("
                                    + width
                                    + ") of port "
                                    + getFullName()
                                    + " is different from the width constrain specified by parameter "
                                    + parameter
                                    + " with container "
                                    + parameter.getContainer()
                                    + ". A"
                                    + " possible fix is to right clicking on the"
                                    + " outside relation(s) and set the width -1.");
                }
            }
        }

        for (IOPort port : _widthEqualToPort) {
            int otherWidth = port._getOutsideWidth(null);
            if (width != otherWidth) {
                throw new IllegalActionException(this, "The outside width ("
                        + width + ") of port " + getFullName()
                        + " is different from the outside width (" + otherWidth
                        + ") of port " + port.getFullName()
                        + ". A possible fix is to right clicking on the"
                        + " connected relation(s) and setting the width AUTO"
                        + " or some fixed number.");
            }
        }
    }

    /** Clone this port into the specified workspace. The new port is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new port with no connections and no container.
     *
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one or more of the attributes
     *   cannot be cloned.
     *  @return A new IOPort.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        IOPort newObject = (IOPort) super.clone(workspace);
        newObject._localReceiversTable = null;
        newObject._insideInputVersion = -1;
        newObject._insideOutputVersion = -1;
        newObject._width = 0;
        newObject._widthVersion = -1;
        newObject._insideWidth = 0;
        newObject._insideWidthVersion = -1;
        newObject._farReceivers = null;
        newObject._farReceiversVersion = -1;
        newObject._localReceivers = null;
        newObject._localReceiversVersion = -1;
        newObject._localInsideReceivers = null;
        newObject._localInsideReceiversVersion = -1;
        newObject._insideReceivers = null;
        newObject._insideReceiversVersion = -1;
        newObject._numberOfSinksVersion = -1;
        newObject._numberOfSourcesVersion = -1;

        newObject._hasPortEventListeners = false;
        newObject._portEventListeners = null;

        newObject._widthEqualToParameter = new HashSet<Parameter>();
        newObject._widthEqualToPort = new HashSet<IOPort>();
        newObject._defaultWidth = -1;
        newObject._communicationAspects = new ArrayList();

        return newObject;
    }

    /** Convert the specified token into a token acceptable to
     *  this port. In this base class, this method simply returns
     *  the same token passed to it, performing no conversion.
     *  @param token The token to convert.
     *  @return The converted token.
     *  @exception IllegalActionException If the conversion is
     *   invalid (not thrown in this base class).
     */
    public Token convert(Token token) throws IllegalActionException {
        return token;
    }

    /** Create new receivers for this port, replacing any that may
     *  previously exist, and validate any instances of Settable that
     *  this port may contain. This method should only be called on
     *  opaque ports.
     *  <p>
     *  If the port is an input port, receivers are created as necessary
     *  for each relation connecting to the port from the outside.
     *  If the port is an output port, receivers are created as necessary
     *  for each relation connected to the port from the inside. Note that
     *  only composite entities will have relations connecting to ports
     *  from the inside.
     *  <p>
     *  Note that it is perfectly allowable for a zero width output port to
     *  have insideReceivers.  This can be used to allow a model to be
     *  embedded in a container that does not connect the port to anything.
     *  <p>
     *  This method is <i>not</i> write-synchronized on the workspace, so the
     *  caller should be.
     *  @exception IllegalActionException If this port is not
     *   an opaque input port or if there is no director.
     */
    public void createReceivers() throws IllegalActionException {
        //System.out.println("IOPort.createReceivers() " + getFullName());
        if (!isOpaque()) {
            throw new IllegalActionException(this,
                    "createReceivers: Can only create "
                            + "receivers on opaque ports. Perhaps there is no "
                            + "top level director?");
        }

        if (_debugging) {
            _debug("create receivers");
        }

        // Create the hashtable of lists of receivers in this port, keyed by
        // relation.  This replaces any previous table, so we first remove
        // the receivers that are currently in the table.
        if (_localReceiversTable != null) {
            for (IORelation relation : _localReceiversTable.keySet()) {
                _removeReceivers(relation);
            }
        }
        _localReceiversTable = new HashMap<IORelation, List<Receiver[][]>>();
        // Make sure _localReceivers is updated next time it is accessed.
        _localReceiversVersion = -1;
        _insideReceiversVersion = -1;

        boolean input = isInput();
        boolean output = isOutput();

        if (input) {
            Iterator<?> outsideRelations = linkedRelationList().iterator();

            // Avoid an infinite loop because getWidth() calls createReceivers().
            boolean createReceivers = false;
            int myWidth = _getWidth(createReceivers);
            int channel = 0;
            boolean madeOne = false;

            while (outsideRelations.hasNext()) {
                IORelation relation = (IORelation) outsideRelations.next();

                // A null link which can be created using insertLink()
                // with an index might result in an null relation.
                if (relation != null) {
                    int width = relation.getWidth();

                    if (!madeOne && myWidth == 1 && width > 1) {
                        width = 1;
                    }
                    if (width == -1) {
                        throw new IllegalActionException(this,
                                "Width of relation \"" + relation.getName()
                                        + "\" was -1?");
                    }

                    Receiver[][] result = new Receiver[width][1];

                    for (int i = 0; i < width; i++) {
                        // This throws an exception if there is no director.
                        result[i][0] = _newReceiver(channel);
                        madeOne = true;
                    }

                    // Save it.  If we have previously seen this relation,
                    // then we simply add the new array to the list
                    // of occurrences for this relation.  Otherwise,
                    // we create a new list with one element.
                    // EAL 7/30/99.
                    if (_localReceiversTable.containsKey(relation)) {
                        List<Receiver[][]> occurrences = _localReceiversTable
                                .get(relation);
                        occurrences.add(result);
                    } else {
                        List<Receiver[][]> occurrences = new LinkedList<Receiver[][]>();
                        occurrences.add(result);
                        _localReceiversTable.put(relation, occurrences);
                    }

                    if (myWidth == 1 && madeOne) {
                        // Made the one receiver we need. Nothing more to do.
                        break;
                    }
                }
            }
            //channel++;
        }

        if (output) {
            Iterator<?> insideRelations = insideRelationList().iterator();

            while (insideRelations.hasNext()) {
                IORelation relation = (IORelation) insideRelations.next();
                int width = relation.getWidth();

                if (width > 0) {
                    Receiver[][] result = new Receiver[width][1];

                    // Inside links need to have receivers compatible
                    // with the local director.  We need to create those
                    // receivers here.
                    for (int i = 0; i < width; i++) {
                        // This throws an exception if there is no director.
                        result[i][0] = _newInsideReceiver(i);
                    }

                    // Save it.  If we have previously seen this relation,
                    // then we simply add the new array to the list
                    // of occurrences for this relation.  Otherwise,
                    // we create a new list with one element.
                    // EAL 7/30/99.
                    if (_localReceiversTable.containsKey(relation)) {
                        List<Receiver[][]> occurrences = _localReceiversTable
                                .get(relation);
                        occurrences.add(result);
                    } else {
                        List<Receiver[][]> occurrences = new LinkedList<Receiver[][]>();
                        occurrences.add(result);
                        _localReceiversTable.put(relation, occurrences);
                    }
                }
            }
        }
    }

    /** Return a list of input ports connected to this port on the
     *  outside. NOTE: This method is not as useful as it might seem.
     *  In particular, it includes in the returned list input ports that
     *  are higher in the hierarchy to which this port is connected
     *  on the <i>inside</i>.  This can be confusing because such ports
     *  cannot receive data produced by this port.  To get a list of
     *  the ports that can receive data from this port, use the
     *  sinkPortList() method.
     *
     *  @see ptolemy.kernel.ComponentPort#deepConnectedPortList
     *  @return A list of IOPort objects.
     */
    public List<IOPort> deepConnectedInPortList() {
        try {
            _workspace.getReadAccess();

            LinkedList<IOPort> result = new LinkedList<IOPort>();

            Iterator<?> ports = deepConnectedPortList().iterator();

            while (ports.hasNext()) {
                IOPort port = (IOPort) ports.next();

                if (port.isInput()) {
                    result.addLast(port);
                }
            }

            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Deeply enumerate the ports connected to this port on the
     *  outside that are input ports.  This method is deprecated and calls
     *  deepConnectedInPortList(). It is read-synchronized on the
     *  workspace.
     *
     *  @see ptolemy.kernel.ComponentPort#deepConnectedPorts
     *  @deprecated Use deepConnectedInPortList() instead.
     *  @return An enumeration of input IOPort objects.
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public Enumeration deepConnectedInPorts() {
        return Collections.enumeration(deepConnectedInPortList());
    }

    /** Return a list of output ports connected to this port on the
     *  outside. NOTE: This method is not as useful as it might seem.
     *  In particular, it includes in the returned list output ports that
     *  are higher in the hierarchy to which this port is connected
     *  on the <i>inside</i>.  This can be confusing because such ports
     *  cannot send data to this port.  To get a list of
     *  the ports that can send data to this port, use the
     *  sourcePortList() method.
     *
     *  @see ptolemy.kernel.ComponentPort#deepConnectedPorts
     *  @return An enumeration of IOPort objects.
     */
    public List<IOPort> deepConnectedOutPortList() {
        try {
            _workspace.getReadAccess();

            LinkedList<IOPort> result = new LinkedList<IOPort>();

            Iterator<?> ports = deepConnectedPortList().iterator();

            while (ports.hasNext()) {
                IOPort port = (IOPort) ports.next();

                if (port.isOutput()) {
                    result.addLast(port);
                }
            }

            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Deeply enumerate the ports connected to this port on the
     *  outside that are output ports. This method is deprecated and calls
     *  deepConnectedInPortList(). It is read-synchronized on the
     *  workspace.
     *
     *  @see ptolemy.kernel.ComponentPort#deepConnectedPorts
     *  @deprecated Use deepConnectedInPortList() instead.
     *  @return An enumeration of output IOPort objects.
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public Enumeration deepConnectedOutPorts() {
        return Collections.enumeration(deepConnectedOutPortList());
    }

    /** If the port is an input, return the receivers deeply linked on
     *  the inside.  This method is used to obtain the receivers that
     *  are to receive data at this input port.  The returned value is
     *  an array of arrays in the same format as that returned by
     *  getReceivers(). The difference between this method and
     *  getReceivers() is that this method treats the port as a
     *  transparent port regardless of whether it is one.  That is,
     *  the returned receivers are contained by ports connected on the
     *  inside to this port.  The number of channels is the inside
     *  width of this port.  If there are no relations linked on the
     *  inside, it returns an empty array.  This method is used for opaque,
     *  non-atomic entities.  It "sees through" the boundary of opaque
     *  ports and actors.  This method is <i>not</i> read-synchronized
     *  on the workspace, so the caller should be.
     *  @return The inside receivers, or an empty receiver array if there
     *   are none.
     * @exception IllegalActionException If thrown while getting the
     * deep receivers of the relation or while getting the inside
     * width.
     * @exception InvalidStateException Not thrown in this base class
     */
    public Receiver[][] deepGetReceivers() throws InvalidStateException,
            IllegalActionException {
        if (!isInput()) {
            return _EMPTY_RECEIVER_ARRAY;
        }

        // Note that this is the inside width, which may not be equal to the
        // outside width of the port.
        int width = getWidthInside();

        if (width <= 0) {
            return _EMPTY_RECEIVER_ARRAY;
        }

        if (_insideReceiversVersion != _workspace.getVersion()) {
            // Cache is invalid.  Update it.
            _insideReceivers = new Receiver[width][0];

            int index = 0;
            Iterator<?> insideRelations = insideRelationList().iterator();

            while (insideRelations.hasNext()) {
                IORelation relation = (IORelation) insideRelations.next();
                Receiver[][] deepReceiver = relation.deepReceivers(this);

                if (deepReceiver != null) {
                    int size = java.lang.Math.min(deepReceiver.length, width
                            - index);

                    for (int i = 0; i < size; i++) {
                        if (deepReceiver[i] != null) {
                            _insideReceivers[index++] = deepReceiver[i];
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
     *  read access on the workspace before calling get().
     *
     *  @param channelIndex The channel index.
     *  @return A token from the specified channel.
     *  @exception NoTokenException If there is no token.
     *  @exception IllegalActionException If there is no director, and hence
     *   no receivers have been created, if the port is not an input port, or
     *   if the channel index is out of range.
     */
    public Token get(int channelIndex) throws NoTokenException,
            IllegalActionException {
        Receiver[][] localReceivers;

        if (_hasPortEventListeners) {
            _notifyPortEventListeners(new IOPortEvent(this,
                    IOPortEvent.GET_BEGIN, channelIndex, true, null));
        }

        try {
            _workspace.getReadAccess();

            // Note that the getReceivers() method might throw an
            // IllegalActionException if there's no director.
            localReceivers = getReceivers();

            if (channelIndex >= localReceivers.length) {
                if (!isInput()) {
                    throw new IllegalActionException(this,
                            "Port is not an input port!");
                } else {
                    throw new IllegalActionException(this, "Channel index "
                            + channelIndex
                            + " is out of range, because width is only "
                            + getWidth() + ".");
                }
            }

            if (localReceivers[channelIndex] == null) {
                throw new NoTokenException(this, "No receiver at index: "
                        + channelIndex + ".");
            }
        } finally {
            _workspace.doneReading();
        }

        Token token = null;

        for (int j = 0; j < localReceivers[channelIndex].length; j++) {
            Token localToken = null;
            try {
                localToken = localReceivers[channelIndex][j].get();
            } catch (NoTokenException ex) {
                if (_persistentToken == null) {
                    throw ex;
                }
            }

            if (token == null) {
                token = localToken;
                if (_hasPortEventListeners) {
                    _notifyPortEventListeners(new IOPortEvent(this,
                            IOPortEvent.GET_END, channelIndex, true, token));
                }
            }
        }

        if (token == null) {
            if (_persistentToken != null) {
                token = _persistentToken;
            } else {
                throw new NoTokenException(this, "No token to return.");
            }
        }

        if (_debugging) {
            _debug("get from channel " + channelIndex + ": " + token);
        }
        // If this port is persistent, then remember the value of this input.
        if (_persistentToken != null) {
        	_persistentToken = token;
        }

        return token;
    }

    /** Get an array of tokens from the specified channel. The
     *  parameter <i>channelIndex</i> specifies the channel and
     *  the parameter <i>vectorLength</i> specifies the number of
     *  valid tokens to get in the returned array. The length of
     *  the returned array will be equal to <i>vectorLength</i>.
     *  <p>
     *  If the channel has a group with more than one receiver (something
     *  that is possible if this is a transparent port), then this method
     *  calls get() on all receivers, but returns only the result from
     *  the first in the group.
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
        Receiver[][] localReceivers;

        if (_hasPortEventListeners) {
            _notifyPortEventListeners(new IOPortEvent(this,
                    IOPortEvent.GET_BEGIN, channelIndex, true, null,
                    vectorLength));
        }

        try {
            _workspace.getReadAccess();

            // Note that the getReceivers() method might throw an
            // IllegalActionException if there's no director.
            localReceivers = getReceivers();
        } finally {
            _workspace.doneReading();
        }

        if (channelIndex >= localReceivers.length) {
            // NOTE: This may be thrown if the port is not an input port.
            throw new IllegalActionException(this,
                    "get: channel index is out of range.");
        }

        if (localReceivers[channelIndex] == null) {
            throw new NoTokenException(this, "get: no receiver at index: "
                    + channelIndex + ".");
        }

        // if there are not enough tokens
        Token[] retArray = new Token[vectorLength];
        if (_persistentToken != null && !hasToken(channelIndex, vectorLength)) {
            int i = 0;
            while (localReceivers[channelIndex][0].hasToken()) {
                retArray[i] = localReceivers[channelIndex][0].get();
                _persistentToken = retArray[i++];
            }
            // If there are not enough tokens, fill up the vector with
            // the persistent token.
            while (i < vectorLength) {
                retArray[i++] = _persistentToken;
            }
        } else {
            retArray = localReceivers[channelIndex][0].getArray(vectorLength);
        }

        if (retArray == null) {
            throw new NoTokenException(this, "get: No token array "
                    + "to return.");
        }

        if (_hasPortEventListeners) {
            _notifyPortEventListeners(new IOPortEvent(this,
                    IOPortEvent.GET_END, channelIndex, true, retArray,
                    vectorLength));
        }

        int index = 1;

        while (index < localReceivers[channelIndex].length) {
            // Read and discard data from other channels in the group.
            localReceivers[channelIndex][index].getArray(vectorLength);
            index++;
        }

        if (_debugging) {
            _debug("get vector from channel " + channelIndex + " of length "
                    + vectorLength);
        }

        return retArray;
    }

    /** Return the corresponding channel in this port for the given receiver.
     *  The given receiver may be contained by this port or a port that is
     *  connected to this port.
     *  @param receiver A receiver that is contained in this port or
     *   connected to another receiver contained in this port.
     *  @return The corresponding channel for the receiver.
     *  @exception IllegalActionException If the given receiver does not
     *   take part in any connections pertaining to this port.
     */
    public int getChannelForReceiver(Receiver receiver)
            throws IllegalActionException {

        // Get the channel number for the receiver.
        Receiver[][] receivers;
        if (isInput()) {
            receivers = getReceivers();
        } else {
            receivers = getInsideReceivers();
        }

        for (int channel = 0; channel < receivers.length; channel++) {
            if (receivers[channel] != null) {
                for (int copy = 0; copy < receivers[channel].length; copy++) {
                    if (receivers[channel][copy] == receiver) {
                        return channel;
                    }
                }
            }
        }

        // FIXME: this is for backwards compatibility, but really should go
        // in a getChannelForRemoteReceiver()
        if (!isInput()) {
            receivers = getRemoteReceivers();

            for (int channel = 0; channel < receivers.length; channel++) {
                if (receivers[channel] != null) {
                    for (int copy = 0; copy < receivers[channel].length; copy++) {
                        if (receivers[channel][copy] == receiver) {
                            return channel;
                        }
                    }
                }
            }
        }

        throw new IllegalActionException(this,
                "Attempt to get a channel for a receiver that"
                        + " is not related to this port.");
    }

    /** Call the {@link #getModelTime} method and return a double
     *  representation of the model time.
     *  @param channelIndex The channel index.
     *  @return The current time associated with a certain channel.
     *  @exception IllegalActionException If the channel index
     *  is out of range or if the port is not an input port.
     *  @deprecated As Ptolemy II 4.1,
     *  replaced by {@link #getModelTime}.
     */
    @Deprecated
    public double getCurrentTime(int channelIndex)
            throws IllegalActionException {
        return getModelTime(channelIndex).getDoubleValue();
    }

    /** Get the default width. In case there is no unique solution for a relation
     *  connected to this port the default width will be used.
     *  @return The default width.
     *  @see #setDefaultWidth(int)
     */
    public int getDefaultWidth() {
        return _defaultWidth;
    }

    /** Get a token from the specified inside channel of this port.
     *  This method is usually called on the output port of a
     *  composite actor.
     *
     *  <p>If the channel has a group with more than one receiver
     *  (something that is possible if this is a transparent port),
     *  then this method calls get() on all receivers, but returns
     *  only the first non-null token returned by these calls.
     *  Normally this method is not used on transparent ports.  If
     *  there is no token to return, then throw an exception.  This
     *  method is usually called only by the director of a composite
     *  actor during transferOutputs(), as atomic actors do not normally
     *  have relations connected on the inside of their ports.
     *
     *  <p> Some of this method is read-synchronized on the workspace.
     *  Since it is possible for a thread to block while executing a
     *  get(), it is important that the thread does not hold read access
     *  on the workspace when it is blocked. Thus this method releases
     *  read access on the workspace before calling get().
     *
     *  @param channelIndex The channel index.
     *  @return A token from the specified channel.
     *  @exception NoTokenException If there is no token.
     *  @exception IllegalActionException If there is no director, and hence
     *   no receivers have been created, if the port is not an output port, or
     *   if the channel index is out of range.
     */
    public Token getInside(int channelIndex) throws NoTokenException,
            IllegalActionException {
        Receiver[][] localReceivers;

        if (_hasPortEventListeners) {
            _notifyPortEventListeners(new IOPortEvent(this,
                    IOPortEvent.GET_BEGIN, channelIndex, false, null));
        }

        try {
            _workspace.getReadAccess();

            // Note that the getInsideReceivers() method might throw an
            // IllegalActionException if there's no director.
            localReceivers = getInsideReceivers();

            if (channelIndex >= localReceivers.length) {
                if (!isOutput()) {
                    throw new IllegalActionException(this,
                            "Port is not an output port!");
                } else {
                    throw new IllegalActionException(this, "Channel index "
                            + channelIndex
                            + " is out of range, because inside width is only "
                            + localReceivers.length + ".");
                }
            }

            if (localReceivers[channelIndex] == null) {
                throw new IllegalActionException(this,
                        "No receiver at inside index: " + channelIndex + ".");
            }
        } finally {
            _workspace.doneReading();
        }

        Token token = null;

        for (int j = 0; j < localReceivers[channelIndex].length; j++) {
            Token localToken = null;
            try {
                localToken = localReceivers[channelIndex][j].get();
            } catch (NoTokenException ex) {
                if (_persistentToken == null) {
                    throw ex;
                }
            }

            if (token == null) {
                token = localToken;
                if (_hasPortEventListeners) {
                    _notifyPortEventListeners(new IOPortEvent(this,
                            IOPortEvent.GET_END, channelIndex, false, token));
                }
            }
        }

        if (token == null) {
            if (_persistentToken != null) {
                token = _persistentToken;
            } else {
                throw new NoTokenException(this, "No token to return.");
            }
        }

        if (_debugging) {
            _debug("get from inside channel " + channelIndex + ": " + token);
        }
        // If the port is persistent, update the remembered value.
        if (_persistentToken != null) {
        	_persistentToken = token;
        }
        return token;
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
     *  @return The local inside receivers, or an empty array if there are
     *   none.
     *  @see #getInside(int)
     */
    public Receiver[][] getInsideReceivers() {
        try {
            _workspace.getReadAccess();

            if (!isOutput() || !isOpaque()) {
                return _EMPTY_RECEIVER_ARRAY;
            }

            // Check to see whether cache is valid.
            if (_localInsideReceiversVersion == _workspace.getVersion()) {
                return _localInsideReceivers;
            }

            // Have to compute the _inside_ width.
            int width = getWidthInside();

            if (width <= 0) {
                return _EMPTY_RECEIVER_ARRAY;
            }

            // Cache not valid.  Reconstruct it.
            _localInsideReceivers = new Receiver[width][0];

            int index = 0;
            Iterator<?> relations = insideRelationList().iterator();

            // NOTE: Have to be careful here to keep track of the
            // occurrence number of the receiver.
            // EAL 7/30/00.
            HashMap<IORelation, Integer> seen = new HashMap<IORelation, Integer>();

            while (relations.hasNext()) {
                IORelation relation = (IORelation) relations.next();

                int occurrence = 0;

                if (seen.containsKey(relation)) {
                    // Have seen this relation before.  Increment
                    // the occurrence number.
                    occurrence = seen.get(relation).intValue();
                    occurrence++;
                }

                seen.put(relation, Integer.valueOf(occurrence));

                Receiver[][] receivers = getReceivers(relation, occurrence);

                if (receivers != null) {
                    for (Receiver[] receiver : receivers) {
                        _localInsideReceivers[index++] = receiver;
                    }
                }
            }

            _localInsideReceiversVersion = _workspace.getVersion();
            return _localInsideReceivers;
        } catch (IllegalActionException ex) {
            // This would be thrown only if the above call to
            // getReceivers(IORelation, int) throws. This should not
            // occur because we are sure the IORelation is connected.
            throw new InternalErrorException(this, ex,
                    "Expected relation to be connected!");
        } finally {
            _workspace.doneReading();
        }
    }

    /**
     * Retrieve the index of the relation at the port.
     * In case the relation is not connected with this port -1
     * will be returned.
     * @param port The port.
     * @param relation The relation.
     * @param isOutsideRelation A flag that specifies that the
     *          relation is an outside relation of the port.
     * @return The index of the relation at the port.
     */
    @SuppressWarnings("unchecked")
    static public int getRelationIndex(IOPort port, Relation relation,
            boolean isOutsideRelation) {
        List<Relation> relations = isOutsideRelation ? port
                .linkedRelationList() : port.insideRelationList();
        int i = 0;
        for (Relation relation2 : relations) {
            if (relation == relation2) {
                return i;
            }
            ++i;
        }
        return -1;
    }

    /** Return the current time associated with a certain channel.
     *  In most domains, this is just the current time of the director.
     *  However, in some domains, the current time is a per-channel
     *  concept.  If the channel has a token to be read (i.e. hasToken()
     *  returns true), then the current time is the time associated with
     *  that token.  If there is no token to be read, then the current
     *  time is the time of most recently read token. If no token has been
     *  previously read, then the current time is 0.0.  Notice that this
     *  means that an actor accessing time should do things in the
     *  following order:
     *  <pre>
     *     if (hasToken(n)) {
     *        double time = port.getCurrentTime(n);
     *        Token token = port.get(n);
     *     }
     *  </pre>
     *  I.e., getCurrentTime() is called before get().
     *  Currently, only the DT domain uses this per-channel time feature.
     *
     *  @param channelIndex The channel index.
     *  @return The current time associated with a certain channel.
     *  @exception IllegalActionException If the channel index
     *  is out of range or if the port is not an input port.
     */
    public Time getModelTime(int channelIndex) throws IllegalActionException {
        Receiver[][] localReceivers;

        try {
            try {
                _workspace.getReadAccess();

                // Note that the getReceivers() method might throw an
                // IllegalActionException if there's no director.
                localReceivers = getReceivers();

                if (localReceivers[channelIndex] == null) {
                    throw new IllegalActionException(this,
                            "no receiver at index: " + channelIndex + ".");
                }
            } finally {
                _workspace.doneReading();
            }

            AbstractReceiver receiver = (AbstractReceiver) localReceivers[channelIndex][0];
            return receiver.getModelTime();
        } catch (ArrayIndexOutOfBoundsException ex) {
            // NOTE: This may be thrown if the port is not an input port.
            throw new IllegalActionException(this,
                    "getCurrentTime: channel index is out of range.");
        }
    }

    /** Return the list of communication aspects in this port.
     *  A communication aspect is a {@link Parameter} whose value is an
     *  {@link ObjectToken} that references an object that implements
     *  the {@link CommunicationAspect} interface.
     *  Update the sequence number of communication aspects.
     *  @return The list of communication aspects.
     *  @exception IllegalActionException Thrown if the token of the parameter
     *      containing the communication aspect object cannot be retrieved.
     */
    public List<CommunicationAspect> getCommunicationAspects()
            throws IllegalActionException {
        if (_communicationAspects == null) {
            _communicationAspects = new ArrayList<CommunicationAspect>();
        }

        HashMap<Integer, CommunicationAspectAttributes> _communicationAspectMap = new HashMap<Integer, CommunicationAspectAttributes>();
        int sequenceNumber = 1;
        List<CommunicationAspectAttributes> communicationAspectList = this
                .attributeList(CommunicationAspectAttributes.class);
        if (communicationAspectList.size() > 0) {
            try {
                _workspace.getWriteAccess();

                List<CommunicationAspectAttributes> enabledAttributes = new ArrayList();

                for (int i = 0; i < communicationAspectList.size(); i++) {
                    CommunicationAspectAttributes attribute = communicationAspectList
                            .get(i);
                    if (((BooleanToken) attribute.enable.getToken())
                            .booleanValue()) {
                        enabledAttributes.add(attribute);
                        int s = ((IntToken) attribute.sequenceNumber.getToken())
                                .intValue();
                        if (s > sequenceNumber) {
                            sequenceNumber = s;
                        }
                    } else {
                        attribute.sequenceNumber.setToken(new IntToken(-1));
                    }
                }
                sequenceNumber = sequenceNumber + 1;
                for (int i = 0; i < enabledAttributes.size(); i++) {
                    final CommunicationAspectAttributes attribute = enabledAttributes
                            .get(i);
                    final int seqNum = sequenceNumber;
                    CommunicationAspect communicationAspect = (CommunicationAspect) attribute
                            .getDecorator();
                    int oldSeqNum = ((IntToken) attribute.sequenceNumber
                            .getToken()).intValue();
                    if (oldSeqNum == -1
                            && communicationAspect != null
                            && !_communicationAspects
                                    .contains(communicationAspect)) {
                        attribute.sequenceNumber.setToken(new IntToken(seqNum));
                        _communicationAspectMap.put(seqNum, attribute);
                        sequenceNumber = sequenceNumber + 1;
                    } else {
                        _communicationAspectMap.put(oldSeqNum, attribute);

                    }
                }
                _communicationAspects.clear();
                Iterator<Integer> iterator = _communicationAspectMap.keySet()
                        .iterator();
                int i = 1;
                while (iterator.hasNext()) {

                    CommunicationAspectAttributes attribute = _communicationAspectMap
                            .get(iterator.next());
                    attribute.sequenceNumber.setToken(new IntToken(i));
                    i = i + 1;
                    Decorator decorator = attribute.getDecorator();
                    if (decorator != null) {
                        _communicationAspects
                                .add((CommunicationAspect) decorator);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                _workspace.doneWriting();
            }
        }
        return _communicationAspects;
    }

    /** If the port is an input, return the receivers that receive data
     *  from all linked relations. For an input
     *  port, the returned value is an array of arrays.  The first index
     *  specifies the channel number.  The second index specifies the
     *  receiver number within the group of receivers that get copies from
     *  the same channel.
     *  <p>
     *  For a transparent port (a port of a non-opaque entity), this method
     *  returns receivers in ports connected to this port on the inside.
     *  For an opaque port, the receivers returned are contained directly by
     *  this port.
     *  <p>
     *  The number of channels (number of groups) is the width of the port.
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
     *  @return The local receivers, or an empty array if there are none.
     */
    public Receiver[][] getReceivers() {
        try {
            _workspace.getReadAccess();

            if (!isInput()) {
                return _EMPTY_RECEIVER_ARRAY;
            }

            if (isOpaque()) {
                // Check to see whether cache is valid.
                if (_localReceiversVersion == _workspace.getVersion()) {
                    return _localReceivers;
                }

                // Cache not valid.  Reconstruct it.
                int width = getWidth();

                if (width <= 0) {
                    return _EMPTY_RECEIVER_ARRAY;
                }

                _localReceivers = new Receiver[width][0];

                int index = 0;
                Iterator<?> relations = linkedRelationList().iterator();

                // NOTE: Have to be careful here to keep track of the
                // occurrence number of the receiver.
                // EAL 7/30/00.
                HashMap<IORelation, Integer> seen = new HashMap<IORelation, Integer>();

                while (relations.hasNext()) {
                    IORelation relation = (IORelation) relations.next();

                    // A null link (supported since indexed links) might
                    // yield a null relation here. EAL 7/19/00.
                    if (relation != null) {
                        int occurrence = 0;

                        if (seen.containsKey(relation)) {
                            // Have seen this relation before.  Increment
                            // the occurrence number.
                            occurrence = seen.get(relation).intValue();
                            occurrence++;
                        }
                        seen.put(relation, Integer.valueOf(occurrence));

                        Receiver[][] receiverRelation = getReceivers(relation,
                                occurrence);

                        if (receiverRelation != null) {
                            for (Receiver[] element : receiverRelation) {
                                _localReceivers[index++] = element;
                            }
                        }
                    }
                }

                _localReceiversVersion = _workspace.getVersion();
                return _localReceivers;
            } else {
                // Transparent port.
                return deepGetReceivers();
            }
        } catch (IllegalActionException ex) {
            // This would be thrown only if the above call to
            // getReceivers(IORelation, int) throws. This should not
            // occur because we are sure the IORelation is connected.
            throw new InternalErrorException(this, ex,
                    "Expected relation to be connected!");
        } finally {
            _workspace.doneReading();
        }
    }

    /** If the port is an input, return receivers that handle incoming
     *  channels from the specified relation. If the port is an opaque output
     *  and the relation is inside linked, return the receivers that handle
     *  incoming channels from the inside. Since the port may be linked
     *  multiple times to the specified relation, this method only returns
     *  the relations correspond to the first occurrence.
     *  The returned value is an array of arrays of the same form
     *  as that returned by getReceivers() with no arguments.  Note that a
     *  single occurrence of a relation may represent multiple channels
     *  because it may be a bus.  If there are no matching receivers,
     *  then return an empty array.
     *  <p>
     *  This method is read-synchronized on the workspace.
     *
     *  @param relation Relations that are linked on the outside or inside.
     *  @return The local receivers.
     *  @exception IllegalActionException If the relation is not linked
     *   from the outside, or if there is no director.
     */
    public Receiver[][] getReceivers(IORelation relation)
            throws IllegalActionException {
        return getReceivers(relation, 0);
    }

    /** If the port is an input, return receivers that handle incoming
     *  channels from the specified relation. If the port is an opaque output
     *  and the relation is inside linked, return the receivers that handle
     *  incoming channels from the inside. Since the port may be linked
     *  multiple times to the specified relation, the <i>occurrences</i>
     *  argument specifies which of the links we wish to examine.
     *  The returned value is an array of arrays of the same form
     *  as that returned by getReceivers() with no arguments.  Note that a
     *  single occurrence of a relation may represent multiple channels
     *  because it may be a bus.  If there are no matching receivers,
     *  then return an empty array.
     *  <p>
     *  This method is read-synchronized on the workspace.
     *
     *  @param relation Relations that are linked on the outside or inside.
     *  @param occurrence The occurrence number that we are interested in,
     *   starting at 0.
     *  @return The local receivers, or an empty array if there are none.
     *  @exception IllegalActionException If the relation is not linked
     *   from the outside.
     */
    public Receiver[][] getReceivers(IORelation relation, int occurrence)
            throws IllegalActionException {
        try {
            _workspace.getReadAccess();

            // Allow inside relations also to support opaque,
            // non-atomic entities.
            boolean insideLink = isInsideLinked(relation);

            if (!isLinked(relation) && !insideLink) {
                throw new IllegalActionException(this, relation,
                        "getReceivers: Relation argument is not linked "
                                + "to me.");
            }

            return _getReceivers(relation, occurrence, insideLink);
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
     *  @return The receivers for output data, or an empty array if there
     *   are none.
     * @exception IllegalActionException If thrown while getting the
     *  width of this port, getting the deep receives of a relation or getting the
     *  width of a relation.
     */
    public Receiver[][] getRemoteReceivers() throws IllegalActionException {
        try {
            _workspace.getReadAccess();

            if (!isOutput()) {
                return _EMPTY_RECEIVER_ARRAY;
            }

            int width = getWidth();

            if (width <= 0) {
                return _EMPTY_RECEIVER_ARRAY;
            }

            // For opaque port, try the cached _farReceivers
            // Check validity of cached version
            if (isOpaque() && _farReceiversVersion == _workspace.getVersion()) {
                return _farReceivers;
            }

            if (_intermediateFarReceiver != null) {
                _farReceivers = new Receiver[width][];
                for (int i = 0; i < width; i++) {
                    _farReceivers[i] = new Receiver[1];
                    _farReceivers[i][0] = _intermediateFarReceiver;
                }
                return _farReceivers;
            }

            // If not an opaque port or Cache is not valid.  Reconstruct it.
            Receiver[][] farReceivers = new Receiver[width][0];
            Iterator<?> relations = linkedRelationList().iterator();
            int index = 0;

            while (relations.hasNext()) {
                IORelation relation = (IORelation) relations.next();

                // A null link (supported since indexed links) might
                // yield a null relation here. EAL 7/19/00.
                if (relation != null) {
                    Receiver[][] deepReceivers = relation.deepReceivers(this);

                    if (deepReceivers != null) {
                        for (int i = 0; i < deepReceivers.length; i++) {
                            try {
                                farReceivers[index] = deepReceivers[i];
                            } catch (ArrayIndexOutOfBoundsException ex) {
                                // ArrayIndexOutOfBounds was thrown by a bug in code generation.
                                // The previous error message was meaningless.
                                throw new InternalErrorException(this, ex,
                                        "Failed to set farReceivers[" + index
                                                + "] = deepReceivers[" + i
                                                + "]. "
                                                + "farReceivers.length = "
                                                + farReceivers.length
                                                + " deepReceivers.length = "
                                                + deepReceivers.length + ".");
                            }
                            index++;
                        }
                    } else {
                        // create a number of null entries in farReceivers
                        // corresponding to the width of relation r
                        index += relation.getWidth();
                    }
                }
            }

            // For an opaque port, cache the result.
            if (isOpaque()) {
                _farReceiversVersion = _workspace.getVersion();
                _farReceivers = farReceivers;
            }

            return farReceivers;
        } finally {
            _workspace.doneReading();
        }
    }

    /** If this port is an output, return the remote receivers that can
     *  receive data from this port through the specified relation or
     *  any relation in its relation group.
     *  The relation or one in its relation group should be linked to the port
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
     *  @param relation The specified relation from which the remote
     *  receivers can receive data.
     *  @return The receivers for output data, or an empty array if there
     *   are none.
     *  @exception IllegalActionException If the IORelation is not linked
     *   to the port from the inside.
     */
    public Receiver[][] getRemoteReceivers(IORelation relation)
            throws IllegalActionException {
        try {
            _workspace.getReadAccess();

            if (!isInsideGroupLinked(relation)) {
                throw new IllegalActionException(this, relation,
                        "not linked from the inside.");
            }

            if (!isOutput()) {
                return _EMPTY_RECEIVER_ARRAY;
            }

            int width = relation.getWidth();

            if (width <= 0) {
                return _EMPTY_RECEIVER_ARRAY;
            }

            // no cache used.
            Receiver[][] outsideReceivers = getRemoteReceivers();

            if (outsideReceivers == null) {
                return _EMPTY_RECEIVER_ARRAY;
            }

            Receiver[][] result = new Receiver[width][];
            Iterator<?> insideRelations = insideRelationList().iterator();
            int index = 0;

            while (insideRelations.hasNext()) {
                IORelation insideRelation = (IORelation) insideRelations.next();

                if (insideRelation.relationGroupList().contains(relation)) {
                    int size = java.lang.Math.min(width,
                            outsideReceivers.length - index);

                    //NOTE: if size = 0, the for loop is skipped.
                    for (int i = 0; i < size; i++) {
                        result[i] = outsideReceivers[i + index];
                    }

                    break;
                }
                // Calling getWidth on a relation for which the width has to be
                // inferred will trigger the width inference algorithm here.
                index += insideRelation.getWidth();
            }

            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Return the width of the port.  The width is the sum of the
     *  widths of the relations that the port is linked to (on the outside).
     *  Note that this method cannot be used to determine whether a port
     *  is connected (deeply) to another port that can either supply it with
     *  data or consume data it produces.  The correct methods to use to
     *  determine that are numberOfSinks() and numberOfSources().
     *  This method is read-synchronized on the workspace.
     *  This method will trigger the width inference algorithm if necessary.
     *  @see #numberOfSinks()
     *  @see #numberOfSources()
     *  @return The width of the port.
     * @exception IllegalActionException If thrown while calling _getWidth()
     */
    public int getWidth() throws IllegalActionException {
        boolean createReceivers = true;
        return _getWidth(createReceivers);
    }

    /** Get the width from the constraints put on the width
     *  of this port if the width is fully determined.
     *  If it is not possible to determine the width yet
     *  (for example because dependent relations also don't have
     *  their width inferred), -1 is returned
     *  @return The width.
     */
    public int getWidthFromConstraints() {
        for (Parameter parameter : _widthEqualToParameter) {
            try {
                IntToken t = (IntToken) parameter.getToken();

                if (t != null) {
                    return t.intValue();
                }
            } catch (Exception e) {
                // It it throws, it means we can't evaluate the
                // parameter yet.
                continue;
            }
        }

        for (IOPort port : _widthEqualToPort) {
            try {
                Set<IORelation> outsideUnspecifiedWidths = RelationWidthInference
                        ._relationsWithUnspecifiedWidths(port
                                .linkedRelationList());
                // It there is still a outsideUnspecifiedWidths, the width
                // is not yet completely specified.
                if (outsideUnspecifiedWidths.isEmpty()) {
                    int outsideWidth = port._getOutsideWidth(null);
                    return outsideWidth;
                }
            } catch (Exception e) {
                // It it throws, it means we can't evaluate the
                // width yet.
                continue;
            }
        }

        // No information available yet
        return -1;
    }

    /** Return the inside width of this port.  The inside width is the
     *  sum of the widths of the relations that the port is linked to
     *  on the inside.  This method is read-synchronized on the
     *  workspace.
     *  This method will trigger the width inference algorithm if necessary.
     *
     *  @return The width of the inside of the port.
     *  @exception IllegalActionException If thrown while getting the
     *  width of the relations or while creating receivers.
     */
    public int getWidthInside() throws IllegalActionException {
        try {
            _workspace.getReadAccess();

            long version = _workspace.getVersion();

            if (_insideWidthVersion != version) {

                int sum = 0;
                Iterator<?> relations = insideRelationList().iterator();

                while (relations.hasNext()) {
                    IORelation relation = (IORelation) relations.next();

                    // A null link (supported since indexed links) might
                    // yield a null relation here. EAL 7/19/00.
                    if (relation != null) {
                        // Calling getWidth on a relation for which the width has to be
                        // inferred will trigger the width inference algorithm here.
                        sum += relation.getWidth();
                    }
                }
                if (_insideWidth != sum) {
                    // Need to re-create receivers for Pub/Sub in Opaques.
                    // See _getWidth() for a similar piece of code.
                    // If we don't check to see that sum < _insideWidth,
                    // then actor/process/test/Branch.tcl has tests that
                    // fail because we end up creating new receivers that
                    // are not producer receivers.
                    if (sum < _insideWidth && isOpaque()) {
                        createReceivers();
                    }
                    _insideWidth = sum;
                    _insideWidthVersion = version;
                }
            }

            return _insideWidth;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Return true if the specified channel has a new token to deliver
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
    public boolean hasNewToken(int channelIndex) throws IllegalActionException {
        // The getReceivers() method throws an IllegalActionException if
        // there's no director.
        Receiver[][] receivers = getReceivers();
        boolean result = false;

        if (receivers != null && channelIndex >= receivers.length) {
            if (!isInput()) {
                throw new IllegalActionException(this,
                        "Port is not an input port!");
            } else {
                throw new IllegalActionException(this, "Channel index "
                        + channelIndex
                        + " is out of range, because width is only "
                        + getWidth() + ".");
            }
        }

        if (receivers != null && receivers[channelIndex] != null) {
            for (int j = 0; j < receivers[channelIndex].length; j++) {
                if (receivers[channelIndex][j].hasToken()) {
                    result = true;
                    break;
                }
            }
        }

        if (_debugging) {
            _debug("hasToken on channel " + channelIndex + " returns " + result);
        }

        return result;
    }

    /** Return true if the specified channel has a token to deliver
     *  via the getInside() method.  If this port is not an output, or
     *  if the channel index is out of range, then throw an exception.
     *  Note that this does not report any tokens in receivers of an
     *  input port.
     *
     *  @param channelIndex The channel index.
     *  @return True if there is a token in the channel.
     *  @exception IllegalActionException If the receivers do not support
     *   this query, if there is no director, and hence no receivers,
     *   if the port is not an output port, or if the channel index is out
     *   of range.
     */
    public boolean hasNewTokenInside(int channelIndex)
            throws IllegalActionException {
        // The getInsideReceivers() method throws an
        // IllegalActionException if there's no director.
        Receiver[][] receivers = getInsideReceivers();
        boolean result = false;

        if (channelIndex >= receivers.length) {
            if (!isOutput()) {
                throw new IllegalActionException(this,
                        "Port is not an output port!");
            } else {
                throw new IllegalActionException(this, "Channel index "
                        + channelIndex
                        + " is out of range, because inside width is only "
                        + getWidthInside() + ".");
            }
        }

        if (receivers[channelIndex] != null) {
            for (int j = 0; j < receivers[channelIndex].length; j++) {
                if (receivers[channelIndex][j].hasToken()) {
                    result = true;
                    break;
                }
            }
        }

        if (_debugging) {
            _debug("hasTokenInside on channel " + channelIndex + " returns "
                    + result);
        }

        return result;
    }

    /** Return true if the specified channel can accept a token via the
     *  put() method.  If this port is not an output, or the channel index
     *  is out of range, then throw IllegalActionException.  If there
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
        boolean result = true;

        try {
            Receiver[][] farReceivers = getRemoteReceivers();

            if (farReceivers == null || farReceivers[channelIndex] == null) {
                result = false;
            } else {
                for (int j = 0; j < farReceivers[channelIndex].length; j++) {
                    if (!farReceivers[channelIndex][j].hasRoom()) {
                        result = false;
                        break;
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            // NOTE: This might be thrown if the port is not an output port.
            throw new IllegalActionException(this,
                    "hasRoom: channel index is out of range.");
        }

        if (_debugging) {
            _debug("hasRoom on channel " + channelIndex + " returns " + result);
        }

        return result;
    }

    /** Return true if the specified channel can accept a token via
     *  the putInside() method.  If this port is not an input, or the
     *  channel index is out of range, then throw
     *  IllegalActionException.  If there are multiple receivers in
     *  the group associated with the channel, then return true only
     *  if all the receivers can accept a token.
     *
     *  @param channelIndex The channel index.
     *  @return True if there is room for a token in the channel.
     *  @exception IllegalActionException If the receivers do not
     *  support this query, if this is not an input port, or if the
     *  channel index is out of range.
     */
    public boolean hasRoomInside(int channelIndex)
            throws IllegalActionException {
        boolean result = true;

        try {
            Receiver[][] farReceivers = getInsideReceivers();

            if (farReceivers == null || farReceivers[channelIndex] == null) {
                result = false;
            } else {
                for (int j = 0; j < farReceivers[channelIndex].length; j++) {
                    if (!farReceivers[channelIndex][j].hasRoom()) {
                        result = false;
                        break;
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            // NOTE: This might be thrown if the port is not an output port.
            throw new IllegalActionException(this,
                    "hasRoom: channel index is out of range.");
        }

        if (_debugging) {
            _debug("hasRoomInside on channel " + channelIndex + " returns "
                    + result);
        }

        return result;
    }

    /** Return true if the port is persistent (@see defaultValue) or
     *  if the specified channel has a token to deliver
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
        if (_persistentToken != null) {
            return true;
        }
        return hasNewToken(channelIndex);
    }
    
    /** Return true if the specified channel has the specified number
     *  of tokens to deliver via the get() method.
     *  If this port is not an input, or if the
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
        if (_persistentToken != null) {
            return true;
        }

        boolean result = false;

        try {
            // The getReceivers() method throws an IllegalActionException if
            // there's no director.
            Receiver[][] receivers = getReceivers();

            if (receivers != null && receivers[channelIndex] != null) {
                for (int j = 0; j < receivers[channelIndex].length; j++) {
                    if (receivers[channelIndex][j].hasToken(tokens)) {
                        result = true;
                        break;
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            // NOTE: This might be thrown if the port is not an output port.
            throw new IllegalActionException(this,
                    "hasToken: channel index is out of range.");
        }

        if (_debugging) {
            _debug("hasToken on channel " + channelIndex + " returns " + result
                    + ", with " + tokens + " tokens requested");
        }

        return result;
    }
    
    /** Return true if the port is persisent or the specified channel 
     *  has a token to deliver
     *  via the getInside() method.  If this port is not an output, or
     *  if the channel index is out of range, then throw an exception.
     *  Note that this does not report any tokens in receivers of an
     *  input port.
     *
     *  @param channelIndex The channel index.
     *  @return True if there is a token in the channel.
     *  @exception IllegalActionException If the receivers do not support
     *   this query, if there is no director, and hence no receivers,
     *   if the port is not an output port, or if the channel index is out
     *   of range.
     */
    public boolean hasTokenInside(int channelIndex)
            throws IllegalActionException {
        if (_persistentToken != null) {
            return true;
        }

        return hasNewTokenInside(channelIndex);
    }

    /** Return whether there are constraints on the width of
     *  this port. There are constraints in case the method
     *  setWidthEquals has been called.
     *  @return True when there are constraints on the width.
     *  @see #setWidthEquals(Parameter)
     *  @see #setWidthEquals(IOPort, boolean)
     */
    public boolean hasWidthConstraints() {
        return !_widthEqualToParameter.isEmpty()
                || !_widthEqualToPort.isEmpty();
    }

    /** Override the base class to invalidate the schedule and resolved
     *  types of the director of the container, if there is one, in addition
     *  to what the base class does.
     *  @param index The index at which to insert the link.
     *  @param relation The relation to link to this port.
     *  @exception IllegalActionException If the link would cross levels of
     *   the hierarchy, or the relation is incompatible,
     *   or the port has no container, or the port is not in the
     *   same workspace as the relation, or if this port is not a multiport
     *   and the index is greater than zero or if another link already exists.
     */
    @Override
    public void insertLink(int index, Relation relation)
            throws IllegalActionException {
        if (!isMultiport()) {
            if (index > 0) {
                throw new IllegalActionException(this,
                        "Cannot insert link at an index greater than "
                                + "zero in a port that is not a multiport.");
            } else if (_isInsideLinkable(relation)) {
                if (numInsideLinks() > 0) {
                    throw new IllegalActionException(this,
                            "Cannot insert a second inside link in a "
                                    + "port that is not a multiport.");
                }
            } else if (numLinks() > 0) {
                throw new IllegalActionException(this,
                        "Cannot insert a second link in a port that is not a "
                                + "multiport.");
            }
        }

        super.insertLink(index, relation);
        _invalidate();
    }

    /** Return a list of the ports that may accept data from this port
     *  when it sends on the inside.  In this base class, this includes
     *  both input ports and opaque output ports that are
     *  connected on the inside to this port, which are the ports that
     *  will receive data from this one if data is sent on the inside.
     *  However, derived classes are free to return ports on this list
     *  that <i>may</i> receive data from this port, even if they are
     *  not actually currently connected.  The wireless domain, for
     *  example, takes advantage of this and includes ports on the
     *  inside that share the same channel. This port must
     *  be an opaque input port, otherwise return an empty list.
     *  @see #deepGetReceivers()
     *  @return A list of IOPort objects.
     */
    public List<IOPort> insideSinkPortList() {
        // NOTE: This doesn't just get the containers of the
        // receivers returned by deepGetReceivers()
        // because the receivers may not have yet been created.
        // FIXME: This should cache the result.
        try {
            _workspace.getReadAccess();

            Nameable container = getContainer();

            if (!(container instanceof CompositeActor && isInput() && isOpaque())) {
                // Return an empty list, since this port cannot send data
                // to the inside.
                return new LinkedList<IOPort>();
            }

            Director dir = ((CompositeActor) container).getDirector();
            int depthOfDirector = dir.depthInHierarchy();
            LinkedList<IOPort> result = new LinkedList<IOPort>();
            Iterator<?> ports = deepInsidePortList().iterator();

            while (ports.hasNext()) {
                IOPort port = (IOPort) ports.next();
                int depth = port.getContainer().depthInHierarchy();

                if (port.isInput() && depth >= depthOfDirector) {
                    result.addLast(port);
                } else if (port.isOutput() && depth < depthOfDirector) {
                    result.addLast(port);
                }
            }

            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Return a list of the ports that can send data to this port
     *  from the inside.  This includes both output ports and opaque
     *  input ports that are connected on the inside to this port.
     *  These are the ports that will send data from this one from the inside.
     *  However, derived classes are free to return ports on this list
     *  that <i>may</i> send data to this port, even if they are
     *  not actually currently connected.  The wireless domain, for
     *  example, takes advantage of this and includes ports on the
     *  inside that share the same channel. This port must
     *  be an opaque output port, otherwise return an empty list.
     *  @return A list of IOPort objects.
     */
    public List<IOPort> insideSourcePortList() {
        try {
            _workspace.getReadAccess();

            Nameable container = getContainer();

            if (!(container instanceof CompositeActor && isOutput() && isOpaque())) {
                // Return an empty list, since this port cannot receive data
                // from the inside.
                return new LinkedList<IOPort>();
            }

            Director dir = ((CompositeActor) container).getDirector();
            int depthOfDirector = dir.depthInHierarchy();
            LinkedList<IOPort> result = new LinkedList<IOPort>();
            Iterator<?> ports = deepInsidePortList().iterator();

            while (ports.hasNext()) {
                IOPort port = (IOPort) ports.next();
                int depth = port.getContainer().depthInHierarchy();

                if (port.isInput() && depth < depthOfDirector) {
                    result.addLast(port);
                } else if (port.isOutput() && depth >= depthOfDirector) {
                    result.addLast(port);
                }
            }

            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Invalidate the communication aspect list. */
    public void invalidateCommunicationAspects() {
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
        if (_isInputOutputStatusSet) {
            return _isInput;
        }

        // Status has not been set.  Try to infer it.
        long version = _workspace.getVersion();

        if (_insideInputVersion != version) {
            try {
                _workspace.getReadAccess();

                // Check to see whether any port linked on the inside
                // is an input.
                _isInput = false; // By default we are not an input port.

                Iterator<?> ports = deepInsidePortList().iterator();

                while (ports.hasNext()) {
                    IOPort p = (IOPort) ports.next();

                    // Rule out case where this port itself is listed...
                    if (p != this && p.isInput()) {
                        _isInput = true;
                    }
                }

                _insideInputVersion = version;
            } finally {
                _workspace.doneReading();
            }
        }

        return _isInput;
    }

    /** Return whether the port has relations connected on the inside.
     * @return True when a relation != null is connected on the inside.
     */
    public boolean isInsideConnected() {
        for (Object relationObject : insideRelationList()) {
            if (relationObject != null) {
                return true;
            }
        }
        return false;
    }

    /** Return true if all channels of this port have known state; that is,
     *  the tokens on each channel are known, or each channel is known not to
     *  have any tokens.
     *  <p>
     *  This method supports domains, such as SR, which have fixed-point
     *  semantics.  In such domains, an iteration of a model starts with
     *  the state of all channels unknown, and the iteration concludes when
     *  the state of all channels is known.
     *  @see #isKnown(int)
     *  @see #isKnownInside(int)
     *  @return True if it is known whether there is a token in each channel.
     *  @exception IllegalActionException If the receivers do not support
     *   this query, or if there is no director, and hence no receivers.
     */
    public boolean isKnown() throws IllegalActionException {
        boolean result = true;

        for (int j = 0; j < getWidth(); j++) {
            if (!isKnown(j)) {
                result = false;
                break;
            }
        }

        if (_debugging) {
            _debug("isKnown returns " + result);
        }

        return result;
    }

    /** Return <i>true</i> if the specified channel has known state;
     *  that is, the tokens on this channel are known, or this channel
     *  is known not to have any tokens.
     *  If the channel index is out of range, then throw
     *  an exception. If the port is an input and an output, then both
     *  the receivers in this port (for the input) and the remote
     *  receivers (for the output) must be known to return true.
     *  If the port is neither an input nor an output, then return true.
     *  <p>
     *  This method supports domains, such as SR, which have fixed-point
     *  semantics.  In such domains, an iteration of a model starts with
     *  the state of all channels unknown, and the iteration concludes when
     *  the state of all channels is known.
     *  @see #isKnown()
     *  @see #isKnownInside(int)     *
     *  @param channelIndex The channel index.
     *  @return True if it is known whether there is a token in the channel.
     *  @exception IllegalActionException If the receivers do not support
     *   this query, if there is no director, and hence no receivers,
     *   if the port is not an input port, or if the channel index is out
     *   of range.
     */
    public boolean isKnown(int channelIndex) throws IllegalActionException {
        boolean result = true;

        try {
            if (isInput()) {
                Receiver[][] receivers = getReceivers();

                if (receivers.length <= channelIndex) {
                    throw new IllegalActionException(this,
                            "Channel index is out of range: " + channelIndex);
                }

                if (receivers[channelIndex] != null) {
                    for (int j = 0; j < receivers[channelIndex].length; j++) {
                        if (!receivers[channelIndex][j].isKnown()) {
                            result = false;
                            break;
                        }
                    }
                }
            }

            if (result && isOutput()) {
                Receiver[][] receivers = getRemoteReceivers();

                if (receivers.length <= channelIndex) {
                    throw new IllegalActionException(this,
                            "Channel index is out of range: " + channelIndex);
                }

                if (receivers[channelIndex] != null) {
                    for (int j = 0; j < receivers[channelIndex].length; j++) {
                        if (!receivers[channelIndex][j].isKnown()) {
                            result = false;
                            break;
                        }
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new IllegalActionException(this,
                    "isKnown: channel index is out of range.");
        }

        if (_debugging) {
            _debug("isKnown on channel " + channelIndex + " returns " + result);
        }

        return result;
    }

    /** Return <i>true</i> if the specified inside channel has known state;
     *  that is, the tokens on this channel are known, or this channel
     *  is known not to have any tokens.  If the channel index is out
     *  of range, then throw an exception.
     *  If the port is an input and an output, then both
     *  the receivers in this port (for the input) and the remote
     *  receivers (for the output) must be known to return true.
     *  If the port is neither an input nor an output, then return true.
     *  <p>
     *  This method supports domains, such as SR, which have fixed-point
     *  semantics.  In such domains, an iteration of a model starts with
     *  the state of all channels unknown, and the iteration concludes when
     *  the state of all channels is known.
     *
     *  @param channelIndex The channel index.
     *  @return True if it is known whether there is a token in the channel.
     *  @exception IllegalActionException If the receivers do not
     *   support this query, if there is no director, and hence no
     *   receivers, or if the inside channel index is out of range.
     */
    public boolean isKnownInside(int channelIndex)
            throws IllegalActionException {
        boolean result = true;

        try {
            if (isOutput()) {
                Receiver[][] receivers = getInsideReceivers();

                if (receivers != null && receivers[channelIndex] != null) {
                    for (int j = 0; j < receivers[channelIndex].length; j++) {
                        if (!receivers[channelIndex][j].isKnown()) {
                            result = false;
                            break;
                        }
                    }
                }
            }

            if (result && isInput()) {
                Receiver[][] receivers = deepGetReceivers();

                if (receivers != null && receivers[channelIndex] != null) {
                    for (int j = 0; j < receivers[channelIndex].length; j++) {
                        if (!receivers[channelIndex][j].isKnown()) {
                            result = false;
                            break;
                        }
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new IllegalActionException(this,
                    "isKnownInside: channel index is out of range.");
        }

        if (_debugging) {
            _debug("isKnownInside on channel " + channelIndex + " returns "
                    + result);
        }

        return result;
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
        return _isMultiport;
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
        if (_isInputOutputStatusSet) {
            return _isOutput;
        }

        // Status has not been set.  Try to infer it.
        long version = _workspace.getVersion();

        if (_insideOutputVersion != version) {
            try {
                _workspace.getReadAccess();

                // Check to see whether any port linked on the
                // inside is an output.
                _isOutput = false; // By default we are not an output port.

                Iterator<?> ports = deepInsidePortList().iterator();

                while (ports.hasNext()) {
                    IOPort p = (IOPort) ports.next();

                    // Rule out case where this port itself is listed...
                    if (p != this && p.isOutput()) {
                        _isOutput = true;
                    }
                }

                _insideOutputVersion = version;
            } finally {
                _workspace.doneReading();
            }
        }

        return _isOutput;
    }

    /** Return whether the port has relations connected on the outside.
     * @return True when a relation != null is connected on the outside.
     */
    public boolean isOutsideConnected() {
        for (Object relationObject : linkedRelationList()) {
            if (relationObject != null) {
                return true;
            }
        }
        return false;
    }

    /** Override the base class to invalidate the schedule and resolved
     *  types of the director of the container, if there is one, in addition
     *  to what the base class does.
     *  @param relation The relation to link to.
     *  @exception IllegalActionException If the relation does not share
     *   the same workspace, or the port has no container.
     */
    @Override
    public void liberalLink(ComponentRelation relation)
            throws IllegalActionException {
        super.liberalLink(relation);
        _invalidate();
    }

    /** Override the base class to invalidate the schedule and resolved
     *  types of the director of the container, if there is one, in addition
     *  to what the base class does.
     *  @param relation The relation to link to.
     *  @exception IllegalActionException If the link crosses levels of
     *   the hierarchy, or the port has no container, or the relation
     *   is not an instance of IORelation.
     */
    @Override
    public void link(Relation relation) throws IllegalActionException {
        super.link(relation);
        _invalidate();
    }

    /** Return the number of sink ports that may receive data from this one.
     *  This is the number of ports returned by sinkPortList(), but
     *  this method is more efficient to call than that one if you only
     *  need to know how many ports there are (because the result is cached).
     *  This method is typically used to determine whether an output port
     *  is connected (deeply) to any input port that can consume its
     *  data.  Note that it is not sufficient to call getWidth() to determine
     *  this; it is possible for getWidth() to return a number greater than
     *  zero when this method returns zero. In particular, if this port
     *  is connected to the inside of an opaque output port, but that opaque
     *  output port is not connected on the outside, then this method will
     *  return zero, but getWidth() will return the width of the relation
     *  mediating the connection.
     *  @see #sinkPortList()
     *  @see #numberOfSources()
     *  @see #getWidth()
     *  @return The number of ports that can receive data from this one.
     */
    public int numberOfSinks() {
        try {
            _workspace.getReadAccess();
            if (_numberOfSinksVersion != _workspace.getVersion()) {
                _numberOfSinks = 0;
                Nameable container = getContainer();
                // Do not use getExecutiveDirector() here because some
                // actors fool with that (like RunCompositeActor).
                Nameable containersContainer = container.getContainer();
                if (containersContainer instanceof Actor) {
                    Director excDirector = ((Actor) containersContainer)
                            .getDirector();
                    int depthOfDirector = excDirector.depthInHierarchy();
                    LinkedList<IOPort> result = new LinkedList<IOPort>();
                    Iterator<?> ports = deepConnectedPortList().iterator();

                    while (ports.hasNext()) {
                        IOPort port = (IOPort) ports.next();
                        int depth = port.getContainer().depthInHierarchy();

                        if (port.isInput() && depth >= depthOfDirector) {
                            result.addLast(port);
                        } else if (port.isOutput() && depth < depthOfDirector
                                && port.numberOfSinks() > 0) {
                            result.addLast(port);
                        }
                    }
                    _numberOfSinks = result.size();
                }
                _numberOfSinksVersion = _workspace.getVersion();
            }
            return _numberOfSinks;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Return the number of source ports that may send data to this one.
     *  This is no greater than number of ports returned by sourcePortList().
     *  This method is typically used to determine whether an input port
     *  is connected (deeply) to any output port that can supply it with
     *  data.  Note that it is not sufficient to call getWidth() to determine
     *  this; it is possible for getWidth() to return a number greater than
     *  zero when this method returns zero. In particular, if this port
     *  is connected to the inside of an opaque input port, but that opaque
     *  input port is not connected on the outside, then this method will
     *  return zero, but getWidth() will return the width of the relation
     *  mediating the connection.
     *  @see #sourcePortList()
     *  @see #numberOfSinks()
     *  @see #getWidth()
     *  @return The number of ports that can send data to this one.
     */
    public int numberOfSources() {
        // The following code is similar to sourcePortList(),
        // but handles the special case of opaque ports as in
        // the method comment.
        try {
            _workspace.getReadAccess();
            if (_numberOfSourcesVersion != _workspace.getVersion()) {
                Nameable container = getContainer();
                int depthOfDirector = -1;

                if (container != null) {
                    // Do not use getExecutiveDirector() here because some
                    // actors fool with that (like RunCompositeActor).
                    Nameable containersContainer = container.getContainer();
                    if (containersContainer instanceof Actor) {
                        Director director = ((Actor) containersContainer)
                                .getDirector();
                        if (director != null) {
                            depthOfDirector = director.depthInHierarchy();
                        }
                    }
                }

                LinkedList<IOPort> result = new LinkedList<IOPort>();
                Iterator<?> ports = deepConnectedPortList().iterator();

                while (ports.hasNext()) {
                    IOPort port = (IOPort) ports.next();
                    int depth = port.depthInHierarchy();

                    if (port.isInput() && depth <= depthOfDirector
                            && port.numberOfSources() > 0) {
                        result.addLast(port);
                    } else if (port.isOutput() && depth > depthOfDirector) {
                        result.addLast(port);
                    }
                }
                _numberOfSources = result.size();
                _numberOfSourcesVersion = _workspace.getVersion();
            }
            return _numberOfSources;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Unregister a token sent listener.  If the specified listener has not
     *  been previously registered, then do nothing.  Note that this method
     *  is basically the same as removeDebugListener in the class NamedObj.
     *  @param listener The listener to remove from the list of listeners
     *   to which token sent messages are sent.
     *  @see #addIOPortEventListener(IOPortEventListener)
     */
    public void removeIOPortEventListener(IOPortEventListener listener) {
        if (_portEventListeners == null) {
            return;
        }

        // NOTE: This has to be synchronized to prevent
        // concurrent modification exceptions.
        synchronized (_portEventListeners) {
            _portEventListeners.remove(listener);

            if (_portEventListeners.size() == 0) {
                _hasPortEventListeners = false;
            }

            return;
        }
    }

    /** If port has default value reset the saved persistent value.
     *  @exception IllegalActionException If defaultValue cannot be retrieved.
     */
    public void reset() throws IllegalActionException {
    	_persistentToken = defaultValue.getToken();
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
     *  If a null token is specified, this is interpreted as an assertion
     *  that no token is being sent. For some domains, specifically those
     *  that queue tokens such as PN and SDF, this has no effect. For
     *  others, specifically those that have a well-defined notion of
     *  "absent" inputs such as SR, modal, and Continuous, sending a null
     *  token corresponds to asserting that the inputs of destination
     *  actors will be absent in this round.
     *  <p>
     *  Some of this method is read-synchronized on the workspace.
     *  Since it is possible for a thread to block while executing a put,
     *  it is important that the thread does not hold read access on
     *  the workspace when it is blocked. Thus this method releases
     *  read access on the workspace before calling put.
     *
     *  @param channelIndex The index of the channel, from 0 to width-1
     *  @param token The token to send, or null to send no token.
     *  @exception NoRoomException If there is no room in the receiver.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void send(int channelIndex, Token token)
            throws IllegalActionException, NoRoomException {
        /* Effective 11/27/09, null tokens are accepted.
        if (token == null) {
            throw new IllegalActionException(this, "Cannot send a null token.");
        }
         */

        Receiver[][] farReceivers;

        if (_debugging) {
            _debug("send to channel " + channelIndex + ": " + token);
        }

        if (_hasPortEventListeners) {
            _notifyPortEventListeners(new IOPortEvent(this,
                    IOPortEvent.SEND_BEGIN, channelIndex, true, token));
        }

        try {
            try {
                _workspace.getReadAccess();

                // Note that the getRemoteReceivers() method doesn't throw
                // any non-runtime exception.
                farReceivers = getRemoteReceivers();

                if (farReceivers == null || farReceivers.length <= channelIndex
                        || farReceivers[channelIndex] == null) {
                    return;
                }
            } finally {
                _workspace.doneReading();
            }

            if (farReceivers[channelIndex].length > 0) {
                // Delegate to the receiver to handle putting to all
                // receivers, since domain-specific techniques might be relevant.
                farReceivers[channelIndex][0].putToAll(token,
                        farReceivers[channelIndex]);
            }
        } finally {
            if (_hasPortEventListeners) {
                _notifyPortEventListeners(new IOPortEvent(this,
                        IOPortEvent.SEND_END, channelIndex, true, token));
            }
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
        Receiver[][] farReceivers;

        if (_debugging) {
            _debug("send to channel " + channelIndex
                    + " token array of length " + vectorLength);
        }

        if (_hasPortEventListeners) {
            _notifyPortEventListeners(new IOPortEvent(this,
                    IOPortEvent.SEND_BEGIN, channelIndex, true, tokenArray,
                    vectorLength));
        }

        try {
            try {
                _workspace.getReadAccess();

                // Note that the getRemoteReceivers() method doesn't throw
                // any non-runtime exception.
                farReceivers = getRemoteReceivers();

                if (farReceivers == null || farReceivers.length <= channelIndex
                        || farReceivers[channelIndex] == null) {
                    return;
                }
            } finally {
                _workspace.doneReading();
            }

            if (farReceivers[channelIndex].length > 0) {
                // Delegate to the receiver to handle putting to all
                // receivers, since domain-specific techniques might be relevant.
                farReceivers[channelIndex][0].putArrayToAll(tokenArray,
                        vectorLength, farReceivers[channelIndex]);
            }
        } finally {
            if (_hasPortEventListeners) {
                _notifyPortEventListeners(new IOPortEvent(this,
                        IOPortEvent.SEND_END, channelIndex, true, tokenArray,
                        vectorLength));
            }
        }
    }

    /** Set all destination receivers connected via the specified to channel
     *  to have no token. The transfer is accomplished by calling
     *  clear() on the appropriate receivers. If there are no
     *  destination receivers on the specified channel, or if this is not
     *  an output port, or if the array index is out of bounds,
     *  then do nothing.
     *  Some of this method is read-synchronized on the workspace.
     *  @see #broadcastClear()
     *  @see #sendClearInside(int)
     *  @param channelIndex The index of the channel, from 0 to width-1
     *  @exception IllegalActionException If a receiver does not support
     *   clear().
     */
    public void sendClear(int channelIndex) throws IllegalActionException {
        Receiver[][] farReceivers;

        if (_debugging) {
            _debug("sendClear to channel " + channelIndex);
        }

        try {
            _workspace.getReadAccess();

            // Note that the getRemoteReceivers() method doesn't throw
            // any non-runtime exception.
            farReceivers = getRemoteReceivers();

            if (farReceivers == null || farReceivers.length <= channelIndex
                    || farReceivers[channelIndex] == null) {
                return;
            }
        } finally {
            _workspace.doneReading();
        }

        // NOTE: Conceivably, clear() in some domains may block,
        // so we make sure to release read access above before calling
        // clear().
        for (int j = 0; j < farReceivers[channelIndex].length; j++) {
            farReceivers[channelIndex][j].clear();
        }
    }

    /** Set all destination receivers connected on the inside via the specified
     *  to channel to have no token. This is accomplished by calling
     *  clear() on the appropriate receivers. If there are no
     *  destination inside receivers on the specified channel,
     *  or if the channel index is out of bounds, then do nothing.
     *  Some of this method is read-synchronized on the workspace.
     *  @see #sendClear(int)
     *  @param channelIndex The index of the channel, from 0 to insideWidth-1.
     *  @exception IllegalActionException If a receiver does not support
     *   clear().
     */
    public void sendClearInside(int channelIndex) throws IllegalActionException {
        Receiver[][] farReceivers;

        if (_debugging) {
            _debug("sendClearInside to channel " + channelIndex);
        }

        try {
            _workspace.getReadAccess();
            farReceivers = deepGetReceivers();

            if (farReceivers == null || farReceivers.length <= channelIndex
                    || farReceivers[channelIndex] == null) {
                return;
            }
        } finally {
            _workspace.doneReading();
        }

        for (int j = 0; j < farReceivers[channelIndex].length; j++) {
            farReceivers[channelIndex][j].clear();
        }
    }

    /** Send the specified token to all receivers connected to the
     *  specified inside channel of this port.  Tokens are in general
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
     *  relations on the inside of their ports.
     *  <p>
     *  If a null token is specified, this is interpreted as an assertion
     *  that no token is being sent. For some domains, specifically those
     *  that queue tokens such as PN and SDF, this has no effect. For
     *  others, specifically those that have a well-defined notion of
     *  "absent" inputs such as SR, modal, and Continuous, sending a null
     *  token corresponds to asserting that the inputs of destination
     *  actors will be absent in this round.
     *
     *  <p> Some of this method is read-synchronized on the workspace.
     *  Since it is possible for a thread to block while executing a
     *  put, it is important that the thread does not hold read access
     *  on the workspace when it is blocked. Thus this method releases
     *  read access on the workspace before calling put.
     *
     *  @param channelIndex The index of the channel, from 0 to width-1
     *  @param token The token to send, or null to send no token.
     *  @exception NoRoomException If there is no room in the receiver.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void sendInside(int channelIndex, Token token)
            throws IllegalActionException, NoRoomException {
        Receiver[][] farReceivers;

        if (_debugging) {
            _debug("send inside to channel " + channelIndex + ": " + token);
        }

        if (_hasPortEventListeners) {
            _notifyPortEventListeners(new IOPortEvent(this,
                    IOPortEvent.SEND_BEGIN, channelIndex, false, token));
        }

        try {
            try {
                _workspace.getReadAccess();

                // Note that the getRemoteReceivers() method doesn't throw
                // any non-runtime exception.
                farReceivers = deepGetReceivers();

                if (farReceivers == null || farReceivers.length <= channelIndex
                        || farReceivers[channelIndex] == null) {
                    return;
                }
            } finally {
                _workspace.doneReading();
            }

            if (farReceivers[channelIndex].length > 0) {
                // Delegate to the receiver to handle putting to all
                // receivers, since domain-specific techniques might be relevant.
                farReceivers[channelIndex][0].putToAll(token,
                        farReceivers[channelIndex]);
            }
        } finally {
            if (_hasPortEventListeners) {
                _notifyPortEventListeners(new IOPortEvent(this,
                        IOPortEvent.SEND_END, channelIndex, false, token));
            }
        }
    }

    /** Override the base class to ensure that the proposed container
     *  implements the Actor interface (the base class ensures that the
     *  container is an instance of ComponentEntity) or null. A null
     *  argument will remove the port from the container.  This method
     *  invalidates the schedule and type resolution of the director
     *  of the container, if there is one.
     *
     *  @param container The proposed container.
     *  @exception IllegalActionException If the proposed container is not a
     *   ComponentEntity, doesn't implement Actor, or has no name,
     *   or the port and container are not in the same workspace. Or
     *   it's not null
     *  @exception NameDuplicationException If the container already has
     *   a port with the name of this port.
     */
    @Override
    public void setContainer(Entity container) throws IllegalActionException,
            NameDuplicationException {
        // Invalidate schedule and type resolution of the old container.
        Actor oldContainer = (Actor) getContainer();

        if (oldContainer != null) {
            Director director = oldContainer.getDirector();

            if (director != null) {
                director.invalidateSchedule();
                director.invalidateResolvedTypes();
            }
        }

        // Invalidate schedule and type resolution of the new container.
        if (container instanceof Actor) {
            Director director = ((Actor) container).getDirector();

            if (director != null) {
                director.invalidateSchedule();
                director.invalidateResolvedTypes();
            }
        }

        super.setContainer(container);
    }

    /** Set the default width. In case there is no unique solution for a relation
     *  connected to this port the default width will be used.
     *  If the default width is not set, the value will be -1
     *  which corresponds to no default width.
     *  @param defaultWidth The default width.
     *  @see #getDefaultWidth()
     */
    public void setDefaultWidth(int defaultWidth) {
        _defaultWidth = defaultWidth;
    }

    /** If the argument is true, make the port an input port.
     *  If the argument is false, make the port not an input port.
     *  If this is never called, and setOutput() is never called,
     *  and the port is a transparent port of a composite actor,
     *  then the input/output status will be inferred from the connection.
     *  This method invalidates the schedule and resolved types of the
     *  director of the container, if there is one.
     *  It is write-synchronized on the workspace, and increments
     *  the version of the workspace.
     *  @param isInput True to make the port an input.
     *  @exception IllegalActionException If changing the port status is
     *   not permitted (not thrown in this base class).
     */
    public void setInput(boolean isInput) throws IllegalActionException {
        // No need for the try ... finally construct here because no
        // exception can occur.  Note that although the action here is
        // atomic, we still need to obtain write access to be sure that
        // the change is not made in the middle of another read in another
        // thread.
        _workspace.getWriteAccess();
        _isInput = isInput;

        // Flag that the input status has been set,
        // and therefore should not be inferred.
        _isInputOutputStatusSet = true;
        _invalidate();
        _workspace.doneWriting();
    }

    /** If the argument is true, make the port a multiport.
     *  That is, make it capable of linking with multiple IORelations,
     *  or with IORelations that have width greater than one.
     *  If the argument is false, allow only links with a single
     *  IORelation of width one.
     *  This method invalidates the schedule and resolved types of the
     *  director of the container, if there is one.
     *  It is write-synchronized on the workspace.
     *  @param isMultiport True to make the port a multiport.
     *  @exception IllegalActionException If changing the port status is
     *   not permitted (not thrown in this base class).
     */
    public void setMultiport(boolean isMultiport) throws IllegalActionException {
        // No need for the try ... finally construct here because no
        // exception can occur.  Note that although the action here is
        // atomic, we still need to obtain write access to be sure that
        // the change is not made in the middle of another read in another
        // thread.
        _workspace.getWriteAccess();
        _isMultiport = isMultiport;
        _invalidate();
        _workspace.doneWriting();
    }

    /** If the argument is true, make the port an output port.
     *  If the argument is false, make the port not an output port.
     *  If this is never called, and setInput() is never called,
     *  and the port is a transparent port of a composite actor,
     *  then the input/output status will be inferred from the connection.
     *  This method invalidates the schedule and resolved types of the
     *  director of the container, if there is one.
     *  It is write-synchronized on the workspace, and increments
     *  the version of the workspace.
     *  @param isOutput True to make the port an output.
     *  @exception IllegalActionException If changing the port status is
     *   not permitted (not thrown in this base class).
     */
    public void setOutput(boolean isOutput) throws IllegalActionException {
        // No need for the try ... finally construct here because no
        // exception can occur.  Note that although the action here is
        // atomic, we still need to obtain write access to be sure that
        // the change is not made in the middle of another read in another
        // thread.
        _workspace.getWriteAccess();
        _isOutput = isOutput;

        // Flag that the output status has been set,
        // and therefore should not be inferred.
        _isInputOutputStatusSet = true;
        _invalidate();
        _workspace.doneWriting();
    }

    /** Constrain the width of this port to be equal to the parameter.
     *  <p>Actors that call this method should have a clone() method that
     *  repeats the width constraints that were specified in
     *  the constructor.
     *  @param parameter A parameter.
     */
    public void setWidthEquals(Parameter parameter) {
        _widthEqualToParameter.add(parameter);
    }

    /** Constrain the width of this port to be equal to the width of
     *  the IOPort port. If bidirectional equals true, the width of port
     *  can also be inferred from the width of this.
     *  <p>Actors that call this method should have a clone() method that
     *  repeats the width constraints that were specified in
     *  the constructor.
     *  @param port A port.
     *  @param bidirectional A flag that specifies whether the constraint
     *  work in two directions.
     */
    public void setWidthEquals(IOPort port, boolean bidirectional) {
        if (!_widthEqualToPort.contains(port)) {
            _widthEqualToPort.add(port);
            if (bidirectional) {
                port.setWidthEquals(this, false);
            }
        }
    }

    /** Return a list of the ports that may accept data from this port when
     *  it sends on the outside.  This includes
     *  opaque input ports that are connected on the outside to this port
     *  and opaque output ports that are connected on the inside to this one.
     *  These are the ports that
     *  will receive data from this one if data is sent on the outside.
     *  However, derived classes are free to return ports on this list
     *  that <i>may</i> receive data from this port, even if they are
     *  not actually currently connected.  The wireless domain, for
     *  example, takes advantage of this and includes ports on the
     *  outside that share the same channel.
     *  @see #getRemoteReceivers()
     *  @return A list of IOPort objects.
     */
    public List<IOPort> sinkPortList() {
        // NOTE: This doesn't just get the containers of the
        // receivers returned by deepGetReceivers()
        // because the receivers may not have yet been created.
        try {
            _workspace.getReadAccess();
            if (_sinkPortListVersion != _workspace.getVersion()) {
                Nameable container = getContainer();
                // Do not use getExecutiveDirector() here because some
                // actors fool with that (like RunCompositeActor).
                Nameable containersContainer = container.getContainer();
                _sinkPortList = new LinkedList<IOPort>();
                if (containersContainer instanceof Actor) {
                    Director excDirector = ((Actor) containersContainer)
                            .getDirector();
                    int depthOfDirector = excDirector.depthInHierarchy();
                    Iterator<?> ports = deepConnectedPortList().iterator();

                    while (ports.hasNext()) {
                        IOPort port = (IOPort) ports.next();
                        int depth = port.getContainer().depthInHierarchy();

                        if (port.isInput() && depth >= depthOfDirector) {
                            _sinkPortList.addLast(port);
                        } else if (port.isOutput() && depth < depthOfDirector) {
                            _sinkPortList.addLast(port);
                        }
                    }
                }
                _sinkPortListVersion = _workspace.getVersion();
            }
            return _sinkPortList;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Return a list of ports that may send data to this port from the
     *  outside.  This includes all opaque output ports that are
     *  connected on the outside to this port, and opaque input ports
     *  that are connected on the inside to this port. These are the ports that
     *  will send data to this one.
     *  However, derived classes are free to return ports on this list
     *  that <i>may</i> send data to this port, even if they are
     *  not actually currently connected.  The wireless domain, for
     *  example, takes advantage of this and includes ports on the
     *  outside that share the same channel.
     *  @return A list of IOPort objects, or an empty list if there are none.
     */
    public List<IOPort> sourcePortList() {
        try {
            _workspace.getReadAccess();
            if (_sourcePortListVersion != _workspace.getVersion()) {
                Nameable container = getContainer();
                int depthOfDirector = -1;

                if (container != null) {
                    // Do not use getExecutiveDirector() here because some
                    // actors fool with that (like RunCompositeActor).
                    Nameable containersContainer = container.getContainer();
                    if (containersContainer instanceof Actor) {
                        Director director = ((Actor) containersContainer)
                                .getDirector();
                        if (director != null) {
                            depthOfDirector = director.depthInHierarchy();
                        }
                    }
                }

                _sourcePortList = new LinkedList<IOPort>();
                Iterator<?> ports = deepConnectedPortList().iterator();

                while (ports.hasNext()) {
                    IOPort port = (IOPort) ports.next();
                    int depth = port.depthInHierarchy();

                    if (port.isInput() && depth <= depthOfDirector) {
                        _sourcePortList.addLast(port);
                    } else if (port.isOutput() && depth > depthOfDirector) {
                        _sourcePortList.addLast(port);
                    }
                }
                _sourcePortListVersion = _workspace.getVersion();
            }
            return _sourcePortList;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Return a list of ports connected to this port on the
     *  outside that can send data to this port such that the data
     *  is received by the specified receiver.  This includes all
     *  opaque output ports that are connected on the outside to this port,
     *  and opaque input ports that are connected on the inside to this port.
     *  If there are multiple paths from a source port to the specified
     *  channel, then the source port will appear more than once in the
     *  resulting list.
     *  If the channel is out of bounds, then return an empty list.
     *  @see #sourcePortList()
     *  @param receiver The receiver.
     *  @return A list of IOPort objects, or an empty list if there are none.
     */

    /* NOTE: This method is commented out because it is untested,
     * extremely complicated, and appears to be not needed (yet).
     public List sourcePortList(Receiver receiver) {
     // This is a surprisingly difficult thing to do...
     List result = new LinkedList();

     // Next, we iterate over ports in the sourcePortList() to find
     // those that have one of these receivers in their remote
     // receiver list.
     Iterator sourcePorts = sourcePortList().iterator();
     while (sourcePorts.hasNext()) {
     IOPort sourcePort = (IOPort)sourcePorts.next();
     Receiver[][] sourcesRemoteReceivers
     = sourcePort.getRemoteReceivers();
     if (sourcesRemoteReceivers == null) continue;
     for (int i = 0; i < sourcesRemoteReceivers.length; i++) {
     if (sourcesRemoteReceivers[i] == null) continue;
     for (int j = 0; j < sourcesRemoteReceivers[i].length; j++) {
     if (sourcesRemoteReceivers[i][j] == null) continue;
     if (sourcesRemoteReceivers[i][j] == receiver) {
     result.add(sourcesRemoteReceivers[i][j].getContainer());
     }
     }
     }
     }
     return result;
     }
     */

    /** Transfer data from this port to the ports it is connected to
     *  on the inside.
     *  This port must be an opaque input port.  If any
     *  channel of the this port has no data, then that channel is
     *  ignored. This method will transfer exactly one token on
     *  each input channel that has at least one token available.
     *
     *  @exception IllegalActionException If this port is not an
     *  opaque input port.
     *  @return True if at least one data token is transferred.
     *  @deprecated Domains should use sendInside directly to
     *  implement their transferInputs method.
     */
    @Deprecated
    public boolean transferInputs() throws IllegalActionException {
        if (!isInput() || !isOpaque()) {
            throw new IllegalActionException(this,
                    "transferInputs: this port is not an opaque"
                            + "input port.");
        }

        boolean wasTransferred = false;

        for (int i = 0; i < getWidth(); i++) {
            // NOTE: This is not compatible with certain cases in PN,
            // where we don't want to block on a port if nothing is connected
            // to the port on the inside.
            try {
                if (isKnown(i)) {
                    if (hasToken(i)) {
                        Token t = get(i);

                        if (_debugging) {
                            _debug(getName(), "transferring input from "
                                    + getName());
                        }

                        sendInside(i, t);
                        wasTransferred = true;
                    }

                    // NOTE: Used to call sendClear() here, but most
                    // domains now throw an exception if this is called.
                    // Presumably this was called to support SR, but
                    // SR needs to handle this itself. EAL 10/31/02.
                    // else:    sendClearInside(i);
                }
            } catch (NoTokenException ex) {
                // this shouldn't happen.
                throw new InternalErrorException(this, ex, null);
            }
        }

        return wasTransferred;
    }

    /** Transfer data from this port to the ports it is connected to on
     *  the outside.
     *  This port must be an opaque output port.  If any
     *  channel of this port has no data, then that channel is
     *  ignored. This method will transfer exactly one token on
     *  each output channel that has at least one token available.
     *
     *  @exception IllegalActionException If the port is not an opaque
     *  output port.
     *  @return True if at least one data token is transferred.
     *  @deprecated domains should use getInside directly to implement their
     *  transferOutputs method.
     */
    @Deprecated
    public boolean transferOutputs() throws IllegalActionException {
        if (_debugging) {
            _debug("calling transferOutputs.");
        }

        if (!this.isOutput() || !this.isOpaque()) {
            throw new IllegalActionException(this,
                    "transferOutputs: this port is not "
                            + "an opaque output port.");
        }

        boolean wasTransferred = false;

        for (int i = 0; i < getWidthInside(); i++) {
            try {
                if (isKnownInside(i)) {
                    if (hasTokenInside(i)) {
                        Token t = getInside(i);
                        send(i, t);
                        wasTransferred = true;
                    }

                    // NOTE: Used to call sendClear() here, but most
                    // domains now throw an exception if this is called.
                    // Why was it called?  EAL 10/31/02.
                    // else:    sendClear(i);
                }
            } catch (NoTokenException ex) {
                throw new InternalErrorException(this, ex, null);
            }
        }

        return wasTransferred;
    }

    /** Unlink whatever relation is currently linked at the specified index
     *  number. If there is no such relation, do nothing.
     *  If a link is removed, then any links at higher index numbers
     *  will have their index numbers decremented by one.
     *  If there is a container, notify it by calling connectionsChanged().
     *  Invalidate the schedule and resolved types of the director of the
     *  container, if there is one.
     *  This method is write-synchronized on the
     *  workspace and increments its version number.
     *  @param index The index number of the link to remove.
     */
    @Override
    public void unlink(int index) {
        // Override the base class to update localReceiversTable.
        try {
            _workspace.getWriteAccess();

            Relation toDelete = (Relation) _relationsList.get(index);

            if (toDelete != null && _localReceiversTable != null) {
                _removeReceivers(toDelete);
                _localReceiversTable.remove(toDelete);
                _localReceiversVersion = -1;
            }

            super.unlink(index);
            _invalidate();
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Unlink the specified Relation. The receivers associated with
     *  this relation, and any data they contain, are lost. If the Relation
     *  is not linked to this port, do nothing. If the relation is linked
     *  more than once, then unlink all occurrences.
     *  Invalidate the schedule and resolved types of the director of the
     *  container, if there is one.
     *  Invalidate the schedule and resolved types of the director of the
     *  container, if there is one.
     *  This method is write-synchronized on the workspace.
     *
     *  @param relation The relation to unlink.
     */
    @Override
    public void unlink(Relation relation) {
        try {
            _workspace.getWriteAccess();
            super.unlink(relation);

            if (_localReceiversTable != null) {
                _removeReceivers(relation);
                _localReceiversTable.remove(relation);
                _localReceiversVersion = -1;
                _insideReceiversVersion = -1;
            }

            _invalidate();
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Unlink all relations that are linked on the outside.
     *  This method is write-synchronized on the
     *  workspace.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void unlinkAll() {
        try {
            _workspace.getWriteAccess();

            // NOTE: Can't just clear the localReceiversTable because
            // that would unlink inside relations as well.
            if (_localReceiversTable != null) {
                // Have to clone the local receivers table to avoid
                // a ConcurrentModificationException.
                HashMap<IORelation, List<Receiver[][]>> clonedMap = (HashMap<IORelation, List<Receiver[][]>>) _localReceiversTable
                        .clone();

                for (IORelation relation : clonedMap.keySet()) {
                    if (!isInsideLinked(relation)) {
                        _removeReceivers(relation);
                        _localReceiversTable.remove(relation);
                        _localReceiversVersion = -1;
                        _insideReceiversVersion = -1;
                    }
                }
            }

            super.unlinkAll();
            _invalidate();
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Unlink all relations that are linked on the inside.
     *  This method is write-synchronized on the
     *  workspace.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void unlinkAllInside() {
        try {
            _workspace.getWriteAccess();

            // NOTE: Can't just clear the localReceiversTable because
            // that would unlink inside relations as well.
            if (_localReceiversTable != null) {
                // Have to clone the local receivers table to avoid
                // a ConcurrentModificationException.
                HashMap<IORelation, List<Receiver[][]>> clonedMap = (HashMap<IORelation, List<Receiver[][]>>) _localReceiversTable
                        .clone();

                for (IORelation relation : clonedMap.keySet()) {

                    if (isInsideLinked(relation)) {
                        _removeReceivers(relation);
                        _localReceiversTable.remove(relation);
                        _localReceiversVersion = -1;
                        _insideReceiversVersion = -1;
                    }
                }
            }

            super.unlinkAllInside();
            _invalidate();
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Unlink whatever relation is currently linked on the inside
     *  with the specified index number. If the relation
     *  is not linked to this port on the inside, do nothing.
     *  If a link is removed, then any links at higher index numbers
     *  will have their index numbers decremented by one.
     *  If there is a container, notify it by calling connectionsChanged().
     *  This method is write-synchronized on the workspace
     *  and increments its version number.
     *  @param index The index number of the link to remove.
     */
    @Override
    public void unlinkInside(int index) {
        // Override the base class to update localReceiversTable.
        try {
            _workspace.getWriteAccess();

            Relation toDelete = (Relation) _insideLinks.get(index);

            if (toDelete != null) {
                if (_localReceiversTable != null) {
                    _removeReceivers(toDelete);
                    _localReceiversTable.remove(toDelete);
                    _insideReceiversVersion = -1;
                }
            }

            super.unlinkInside(index);
            _invalidate();
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Unlink the specified Relation on the inside. The receivers associated
     *  with this relation, and any data they contain, are lost. If the Relation
     *  is not linked to this port, do nothing. If the relation is linked
     *  more than once, then unlink all occurrences.
     *  This method is write-synchronized on the workspace.
     *
     *  @param relation The relation to unlink.
     */
    @Override
    public void unlinkInside(Relation relation) {
        try {
            _workspace.getWriteAccess();
            super.unlinkInside(relation);

            if (_localReceiversTable != null) {
                _removeReceivers(relation);
                _localReceiversTable.remove(relation);
                _insideReceiversVersion = -1;
            }

            _invalidate();
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

    /** Check that the specified container implements the Actor interface
     *  (or is null).
     *  @param container The proposed container.
     *  @exception IllegalActionException If the container is not of
     *   an acceptable class.
     */
    @Override
    protected void _checkContainer(Entity container)
            throws IllegalActionException {
        if (!(container instanceof Actor) && container != null) {
            throw new IllegalActionException(container, this,
                    "IOPort can only be contained by objects implementing "
                            + "the Actor interface.");
        }
    }

    /** Override parent method to ensure compatibility of the relation
     *  and validity of the width of the port.
     *  If this port is not a multiport, then the width of the
     *  relation is required to be specified to be one.  This method
     *  allows level-crossing links.
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
    @Override
    protected void _checkLiberalLink(Relation relation)
            throws IllegalActionException {
        if (!(relation instanceof IORelation)) {
            throw new IllegalActionException(this, relation,
                    "Attempt to link to an incompatible relation."
                            + " IOPort requires IORelation.");
        }

        _checkMultiportLink((IORelation) relation);
        super._checkLiberalLink(relation);
    }

    /** Override parent method to ensure compatibility of the relation
     *  and validity of the width of the port.
     *  If this port is not a multiport, then the width of the
     *  relation is required to be specified to be one.
     *  This method is <i>not</i> synchronized on the
     *  workspace, so the caller should be.
     *
     *  @param relation The relation to link to.
     *  @exception IllegalActionException If this port has no container or
     *   the relation is not an IORelation, or the port already linked to a
     *   relation and is not a multiport, or if the relation has width
     *   not exactly one and the port is not a multiport, or the port is
     *   not in the same workspace as the relation.
     */
    @Override
    protected void _checkLink(Relation relation) throws IllegalActionException {
        if (!(relation instanceof IORelation)) {
            throw new IllegalActionException(this, relation,
                    "Attempt to link to an incompatible relation."
                            + " IOPort requires IORelation.");
        }

        _checkMultiportLink((IORelation) relation);
        super._checkLink(relation);
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
     *  The field keyword is "configuration".
     *  <p>
     *  If the detail argument sets the bit defined by the constant
     *  RECEIVERS, then append to the description a field containing
     *  the receivers contained by this port.  The keyword is "receivers"
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
     *  @exception IllegalActionException If thrown while getting the
     *  description of subcomponents.
     */
    @Override
    protected String _description(int detail, int indent, int bracket)
            throws IllegalActionException {
        try {
            _workspace.getReadAccess();

            StringBuffer result = new StringBuffer();

            if (bracket == 1 || bracket == 2) {
                result.append(super._description(detail, indent, 1));
            } else {
                result.append(super._description(detail, indent, 0));
            }

            if ((detail & CONFIGURATION) != 0) {
                if (result.toString().trim().length() > 0) {
                    result.append(" ");
                }

                result.append("configuration {");

                boolean space = false;

                if (isInput()) {
                    space = true;
                    result.append("input");
                }

                if (isOutput()) {
                    if (space) {
                        result.append(" ");
                    }

                    space = true;
                    result.append("output");
                }

                if (isMultiport()) {
                    if (space) {
                        result.append(" ");
                    }

                    space = true;
                    result.append("multiport");
                }

                if (isOpaque()) {
                    if (space) {
                        result.append(" ");
                    }

                    space = true;
                    result.append("opaque");
                }

                if (space) {
                    result.append(" ");
                }

                result.append("{width " + getWidth() + "}}");
            }

            if ((detail & RECEIVERS) != 0) {
                if (result.toString().trim().length() > 0) {
                    result.append(" ");
                }

                result.append("receivers {\n");

                Receiver[][] receivers = getReceivers();

                for (Receiver[] receiver : receivers) {
                    // One list item per group
                    result.append(_getIndentPrefix(indent + 1) + "{\n");

                    for (int j = 0; j < receiver.length; j++) {
                        result.append(_getIndentPrefix(indent + 2));
                        result.append("{");

                        if (receiver[j] != null) {
                            result.append(receiver[j].getClass().getName());
                        }

                        result.append("}\n");
                    }

                    result.append(_getIndentPrefix(indent + 1) + "}\n");
                }

                result.append(_getIndentPrefix(indent) + "}");
            }

            if ((detail & REMOTERECEIVERS) != 0) {
                if (result.toString().trim().length() > 0) {
                    result.append(" ");
                }

                result.append("remotereceivers {\n");

                Receiver[][] receivers = getRemoteReceivers();

                if (receivers != null) {
                    for (Receiver[] receiver : receivers) {
                        // One list item per group
                        result.append(_getIndentPrefix(indent + 1) + "{\n");

                        if (receiver != null) {
                            for (int j = 0; j < receiver.length; j++) {
                                result.append(_getIndentPrefix(indent + 2));
                                result.append("{");

                                if (receiver[j] != null) {
                                    result.append(receiver[j].getClass()
                                            .getName());
                                    result.append(" in ");
                                    result.append(receiver[j].getContainer()
                                            .getFullName());
                                }

                                result.append("}\n");
                            }
                        }

                        result.append(_getIndentPrefix(indent + 1) + "}\n");
                    }
                }

                result.append(_getIndentPrefix(indent) + "}");
            }

            if (bracket == 2) {
                result.append("}");
            }

            return result.toString();
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
    @Override
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

        if (_isMultiport) {
            output.write(_getIndentPrefix(depth)
                    + "<property name=\"multiport\"/>\n");
        }

        super._exportMoMLContents(output, depth);
    }

    /** Return the sums of the widths of the relations linked on the
     *  inside, except the specified relation.  If any of these relations
     *  has not had its width specified, throw an exception.  This is
     *  used by IORelation to infer the width of a bus with
     *  unspecified width and to determine whether more than one
     *  relation with unspecified width is linked on the inside, and
     *  by the liberalLink() method to check validity of the link.  If
     *  the argument is null, all relations linked on the inside are
     *  checked.  This method is not read-synchronized on the
     *  workspace, so the caller should be.
     *  This method ignores the relations for which the width still has
     *  to be inferred.
     *
     *  @param except The relation to exclude.
     *  @return The sums of the width of the relations linked on the inside,
     *  except for the specified port.
     *  @exception IllegalActionException If thrown while checking if a
     *  relation needs width inference, while getting the width of the
     *  relation, while checking if the width or a relation is fixed.
     */
    protected int _getInsideWidth(IORelation except)
            throws IllegalActionException {
        if (IORelation._USE_NEW_WIDTH_INFERENCE_ALGO) {
            int result = 0;
            Iterator<?> relations = insideRelationList().iterator();

            while (relations.hasNext()) {
                IORelation relation = (IORelation) relations.next();

                if (relation != except) {
                    if (!relation.needsWidthInference()) {
                        result += relation.getWidth();
                    }
                }
            }

            return result;
        } else {
            int result = 0;
            Iterator<?> relations = insideRelationList().iterator();

            while (relations.hasNext()) {
                IORelation relation = (IORelation) relations.next();

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
    }

    /** Return the sums of the widths of the relations linked on the
     *  outside, except the specified relation.  If any of these relations
     *  has not had its width specified, throw an exception.  This is
     *  used by IORelation to infer the width of a bus with
     *  unspecified width and to determine whether more than one
     *  relation with unspecified width is linked on the outside, and
     *  by the liberalLink() method to check validity of the link.  If
     *  the argument is null, all relations linked on the inside are
     *  checked.  This method is not read-synchronized on the
     *  workspace, so the caller should be.
     *  This method ignores the relations for which the width still has
     *  to be inferred.

     *  @param except The relation to exclude.
     *  @return The sums of the width of the relations linked on the outside,
     *  except for the specified port.
     *  @exception IllegalActionException If thrown while checking if a
     *  relation needs width inference, while getting the width of the
     *  relation, while checking if the width or a relation is fixed.
     */
    protected int _getOutsideWidth(IORelation except)
            throws IllegalActionException {
        if (IORelation._USE_NEW_WIDTH_INFERENCE_ALGO) {
            int result = 0;
            Iterator<?> relations = linkedRelationList().iterator();

            while (relations.hasNext()) {
                IORelation relation = (IORelation) relations.next();

                if (relation != except) {
                    if (!relation.needsWidthInference()) {
                        result += relation.getWidth();
                    }
                }
            }

            return result;
        } else {
            int result = 0;
            Iterator<?> relations = linkedRelationList().iterator();

            while (relations.hasNext()) {
                IORelation relation = (IORelation) relations.next();

                if (relation != except) {
                    if (!relation.isWidthFixed()) {
                        throw new InvalidStateException(this,
                                "Width of outside relations cannot "
                                        + "be determined.");
                    }

                    result += relation.getWidth();
                }
            }

            return result;
        }
    }

    /** If the port is an input, return receivers that handle incoming
     *  channels from the specified relation or any relation in its
     *  relation group. If the port is an opaque output
     *  and the relation is inside linked, return the receivers that handle
     *  incoming channels from the inside. Since the port may be linked
     *  multiple times to the specified relation, the <i>occurrences</i>
     *  argument specifies which of the links we wish to examine.
     *  The returned value is an array of arrays of the same form
     *  as that returned by getReceivers() with no arguments.  Note that a
     *  single occurrence of a relation may represent multiple channels
     *  because it may be a bus.  If there are no matching receivers,
     *  then return an empty array.
     *  <p>
     *  This method handles relation groups. That is, given any relation
     *  in a relation group, it returns the combined receivers of all
     *  the relations in the relation group, in the order as returned
     *  by the getRelationGroup() method of Receiver.
     *  <p>
     *  This method is read-synchronized on the workspace.
     *
     *  @param relation Relations that are linked on the outside or inside.
     *  @param occurrence The occurrence number that we are interested in,
     *   starting at 0.
     *  @return The local receivers, or an empty array if there are none.
     *  @exception IllegalActionException If the relation is not linked
     *   from the outside.
     */
    protected Receiver[][] _getReceiversLinkedToGroup(IORelation relation,
            int occurrence) throws IllegalActionException {
        try {
            _workspace.getReadAccess();

            // Allow inside relations also to support opaque,
            // non-atomic entities.
            boolean insideLink = isInsideGroupLinked(relation);

            if (!isGroupLinked(relation) && !insideLink) {
                throw new IllegalActionException(this, relation,
                        "getReceivers: Relation argument is not linked "
                                + "to me on the inside.");
            }

            List<?> groupRelationsList = relation.relationGroupList();

            // For efficiency, if there is only one element, then just
            // return the results of the private method.
            if (groupRelationsList.size() == 1) {
                return _getReceivers(relation, occurrence, insideLink);
            }

            // Create a linked list of the results to be merged.
            Receiver[][][] results = new Receiver[groupRelationsList.size()][][];
            int index = 0;
            int width = 0;
            Iterator<?> groupRelations = groupRelationsList.iterator();

            while (groupRelations.hasNext()) {
                IORelation groupRelation = (IORelation) groupRelations.next();
                Receiver[][] oneResult = _getReceivers(groupRelation,
                        occurrence, insideLink);
                results[index] = oneResult;
                index++;

                if (oneResult != null && oneResult.length > width) {
                    width = oneResult.length;
                }
            }

            Receiver[][] result = new Receiver[width][];

            // Now fill in the result for each channel i.
            for (int i = 0; i < width; i++) {
                // First find out how many replicas there are
                // for each channel.
                int numberOfReplicas = 0;

                for (Receiver[][] result2 : results) {
                    if (result2 == null || i >= result2.length
                            || result2[i] == null) {
                        // This result has no more replicas to contribute.
                        continue;
                    }

                    numberOfReplicas += result2[i].length;
                }

                result[i] = new Receiver[numberOfReplicas];

                // Next, copy the replicas into the result.
                index = 0;

                for (Receiver[][] result2 : results) {
                    if (result2 == null || i >= result2.length
                            || result2[i] == null) {
                        // This result has no more replicas to contribute.
                        continue;
                    }

                    for (int k = 0; k < result2[i].length; k++) {
                        result[i][index] = result2[i][k];
                        index++;
                    }
                }
            }

            return result;
        } finally {
            _workspace.doneReading();
        }
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
     *  <p>
     *  The returned receiver is either the new receiver, or another
     *  receiver wrapping it as specified in {@link #_wrapReceiver(Receiver, int)}.
     *  @return A new receiver.
     *  @exception IllegalActionException If the port has no container,
     *   or the container is unable to return a new receiver (for example
     *   if it has no local director).
     */
    protected Receiver _newInsideReceiver() throws IllegalActionException {
        return _newInsideReceiver(0);
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
     *  <p>
     *  The returned receiver is either the new receiver, or another
     *  receiver wrapping it as specified in {@link #_wrapReceiver(Receiver, int)}.
     *  @param channel Used to determine source port.
     *  @return A new receiver.
     *  @exception IllegalActionException If the port has no container,
     *   or the container is unable to return a new receiver (for example
     *   if it has no local director).
     */
    protected Receiver _newInsideReceiver(int channel)
            throws IllegalActionException {
        Nameable container = getContainer();

        if (container instanceof CompositeActor) {
            CompositeActor castContainer = (CompositeActor) container;

            if (castContainer.isOpaque() && !castContainer.isAtomic()) {
                Receiver receiver = castContainer.newInsideReceiver();
                receiver.setContainer(this);
                // return receiver;
                return _wrapReceiver(receiver, channel);
            }
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
     *  <p>
     *  The returned receiver is either the new receiver, or another
     *  receiver wrapping it as specified in {@link #_wrapReceiver(Receiver, int)}.
     *  @return A new receiver.
     *  @exception IllegalActionException If the port has no container,
     *   or the container is unable to return a new receiver (for example
     *   if it has no executive director).
     */
    protected Receiver _newReceiver() throws IllegalActionException {
        return _newReceiver(0);
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
     *  <p>
     *  The returned receiver is either the new receiver, or another
     *  receiver wrapping it as specified in {@link #_wrapReceiver(Receiver, int)}.
     *  @param channel Channel id used to determine the source port.
     *  @return A new receiver.
     *  @exception IllegalActionException If the port has no container,
     *   or the container is unable to return a new receiver (for example
     *   if it has no executive director).
     */
    protected Receiver _newReceiver(int channel) throws IllegalActionException {
        Actor container = (Actor) getContainer();

        if (container == null) {
            throw new IllegalActionException(this,
                    "Cannot create a receiver without a container.");
        }
        Receiver receiver = container.newReceiver();
        receiver.setContainer(this);
        // return receiver;
        return _wrapReceiver(receiver, channel);
    }

    /** Remove the receivers associated with the specified
     *  relation, if there are any.  This sets the container
     *  of each receiver to null.
     *  @param relation The relation.
     */
    protected void _removeReceivers(Relation relation) {
        boolean removed = false;
        if (_localReceiversTable != null) {
            List<Receiver[][]> receivers = _localReceiversTable.get(relation);
            if (receivers != null) {
                Iterator<Receiver[][]> iterator = receivers.iterator();
                while (iterator.hasNext()) {
                    Receiver[][] receiverArray = iterator.next();
                    for (Receiver[] element : receiverArray) {
                        if (element != null) {
                            for (int j = 0; j < element.length; j++) {
                                if (element[j] != null) {
                                    try {
                                        removed = true;
                                        element[j].setContainer(null);
                                    } catch (IllegalActionException e) {
                                        // This should not happen because we are setting
                                        // the container to null.
                                        throw new InternalErrorException(e);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (removed) {
            // Must increment the workspace version if receivers have
            // been removed. Otherwise, there will be lists of receivers
            // cached where the receivers have no containers.
            _workspace.incrVersion();
        }
    }

    /** Send a PortEvent to all port event listeners that
     *  have registered with this IOPort.
     *  @param event The event.
     *  @exception IllegalActionException If thrown by portEvent().
     */
    protected final void _notifyPortEventListeners(IOPortEvent event)
            throws IllegalActionException {
        if (_hasPortEventListeners) {
            for (IOPortEventListener listener : _portEventListeners) {
                listener.portEvent(event);
            }
        }
    }

    /** Set a constant token so that every call to {@link #get(int)}
     *  or {@link #get(int,int)} replaces the returned token(s) with
     *  this specified token. This is a rather specialized piece
     *  of functionality added to be able to support
     *  {@link ConstantPublisherPort}.
     *  @param token The token to return instead of received tokens,
     *   or null to cancel this functionality.
     *  @param limit If a non-negative number is given here, then
     *   limit the number of constant tokens provided.
     */
    protected void _setConstant(Token token, int limit) {
        _constantToken = token;
        _constantLimit = limit;
        _constantTokensSent = 0;
    }

    /** If this port has parameters whose values are tokens that contain
     *  an object implementing {@link CommunicationAspect}, then wrap the
     *  receiver specified in the argument using those communication aspects.
     *  If there are no such parameters, then simply return the specified
     *  receiver. If there is one such parameter, then use the quantity
     *  manager to wrap the specified receiver in a new receiver, and return
     *  that receiver. If there are two such parameters, then use the second
     *  communication aspect to create a receiver that wraps that created by the
     *  first communication aspect. Etc.
     *  @see CommunicationAspect
     *  @param receiver The receiver to wrap.
     *  @param channel Channel id used to determine the source port.
     *  @return Either a new receiver wrapping the specified receiver,
     *   or the specified receiver.
     *  @exception IllegalActionException If any parameter of the port
     *   cannot be evaluated.
     */
    protected Receiver _wrapReceiver(Receiver receiver, int channel)
            throws IllegalActionException {
        Receiver result = receiver;
        List<CommunicationAspect> communicationAspects = getCommunicationAspects();
        if (isInput()) {
            for (int i = communicationAspects.size() - 1; i >= 0; i--) {
                CommunicationAspect communicationAspect = communicationAspects
                        .get(i);
                if (communicationAspect != null) {
                    result = communicationAspect
                            .createIntermediateReceiver(result);
                }
            }
            if (result instanceof IntermediateReceiver) {
                IntermediateReceiver intermediateReceiver = (IntermediateReceiver) result;
                intermediateReceiver.source = (Actor) this.sourcePortList()
                        .get(channel).getContainer();
            }
        } else {
            for (int i = 0; i < communicationAspects.size(); i++) {
                CommunicationAspect communicationAspect = communicationAspects
                        .get(i);
                result = communicationAspect.createIntermediateReceiver(result);
            }
        }
        // FIXME what if isInput && isOutput??

        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The limit of the number of constant values to return instead
     * of the received tokens. This is protected so that AbstractReceiver
     * can access it.
     */
    protected int _constantLimit = -1;

    /** The constant value to return instead of the received tokens.
     *  This is protected so that AbstractReceiver can access it.
     */
    protected Token _constantToken;

    /** The number of constant tokens that have been sent since the last
     *  call to _setConstant(). This is protected so that AbstractReceiver
     * can access it.
     */
    protected int _constantTokensSent = 0;

    /** Flag that is true if there are port event listeners. */
    protected boolean _hasPortEventListeners = false;

    /** The list of IOPortEventListeners registered with this object.
     *  NOTE: Because of the way we synchronize on this object, it should
     *  never be reset to null after the first list is created.
     */
    protected List<IOPortEventListener> _portEventListeners = null;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Check that a port that is not a multiport will not have too many
     *  links if a link is established with the specified relation.
     *  @exception IllegalActionException If the port will have too many
     *  links.
     */
    private void _checkMultiportLink(IORelation relation)
            throws IllegalActionException {
        if (IORelation._USE_NEW_WIDTH_INFERENCE_ALGO) {
            if (_isInsideLinkable(relation.getContainer())) {
                // An inside link
                // Check for existing inside links
                if (!isMultiport() && numInsideLinks() >= 1) {
                    throw new IllegalActionException(this, relation,
                            "Attempt to link more than one relation "
                                    + "to a single port.");
                }

                if (relation.isWidthFixed() && relation.getWidth() > 1) {
                    // Relation is a bus.
                    if (!isMultiport()) {
                        throw new IllegalActionException(this, relation,
                                "Attempt to link a bus relation "
                                        + "to a single port.");
                    }
                }
            } else {
                // An outside link
                // Check for existing outside links
                if (!isMultiport() && numLinks() >= 1) {
                    throw new IllegalActionException(this, relation,
                            "Attempt to link more than one relation "
                                    + "to a single port.");
                }
            }
        } else {
            if (_isInsideLinkable(relation.getContainer())) {
                // An inside link
                // Check for existing inside links
                if (!isMultiport() && numInsideLinks() >= 1) {
                    throw new IllegalActionException(this, relation,
                            "Attempt to link more than one relation "
                                    + "to a single port.");
                }

                if (relation.getWidth() != 1 || !relation.isWidthFixed()) {
                    // Relation is a bus.
                    if (!isMultiport()) {
                        throw new IllegalActionException(this, relation,
                                "Attempt to link a bus relation "
                                        + "to a single port.");
                    }

                    if (!relation.isWidthFixed()) {
                        // Make sure there are no other busses already
                        // connected with unspecified widths.
                        try {
                            _getInsideWidth(null);
                        } catch (InvalidStateException ex) {
                            throw new IllegalActionException(
                                    this,
                                    relation,
                                    "Attempt to link a second bus relation "
                                            + "with unspecified width to the inside "
                                            + "of a port.");
                        }
                    }
                }
            } else {
                // An outside link
                // Check for existing outside links
                if (!isMultiport() && numLinks() >= 1) {
                    throw new IllegalActionException(this, relation,
                            "Attempt to link more than one relation "
                                    + "to a single port.");
                }

                if (relation.getWidth() != 1 || !relation.isWidthFixed()) {
                    // Relation is a bus.

                    /* This is now allowed.
                     if (!isMultiport()) {
                     throw new IllegalActionException(this, relation,
                     "Attempt to link a bus relation "
                     + "to a single port.");
                     }
                     */
                    Iterator<?> relations = linkedRelationList().iterator();

                    while (relations.hasNext()) {
                        IORelation theRelation = (IORelation) relations.next();

                        // A null link (supported since indexed links) might
                        // yield a null relation here. EAL 7/19/00.
                        if (theRelation != null && !theRelation.isWidthFixed()) {
                            throw new IllegalActionException(
                                    this,
                                    relation,
                                    "Attempt to link a second bus relation "
                                            + "with unspecified width to the outside "
                                            + "of a port.");
                        }
                    }
                }
            }
        }
    }

    /** If the port is an input, return receivers that handle incoming
     *  channels from the specified relation. If the port is an opaque output
     *  and the relation is inside linked, return the receivers that handle
     *  incoming channels from the inside. Since the port may be linked
     *  multiple times to the specified relation, the <i>occurrences</i>
     *  argument specifies which of the links we wish to examine.
     *  The returned value is an array of arrays of the same form
     *  as that returned by getReceivers() with no arguments.  Note that a
     *  single occurrence of a relation may represent multiple channels
     *  because it may be a bus.  If there are no matching receivers,
     *  then return an empty array.
     *  <p>
     *  This method works only with relations directly linked to this
     *  port. Use the public method to work on relation groups. If the
     *  specified relation is not linked to this port, then this method
     *  returns an empty array.
     *  @param relation Relations that are linked on the outside or inside.
     *  @param occurrence The occurrence number that we are interested in,
     *   starting at 0.
     *  @param insideLink True if this is an inside link.
     *  @return The local receivers, or an empty array if there are none.
     *  @exception IllegalActionException If the relation is not linked
     *   from the outside.
     */
    private Receiver[][] _getReceivers(IORelation relation, int occurrence,
            boolean insideLink) throws IllegalActionException {
        boolean opaque = isOpaque();

        if (!isInput() && !(opaque && insideLink && isOutput())) {
            return _EMPTY_RECEIVER_ARRAY;
        }

        int width = relation.getWidth();

        if (getWidth() == 1 && width > 1) {
            // This can occur if we have a wide relation driving a narrower
            // port.
            width = 1;
        }

        if (width <= 0) {
            return _EMPTY_RECEIVER_ARRAY;
        }

        Receiver[][] result = null;

        // If the port is opaque, return the local Receivers for the
        // relation.
        if (opaque) {
            // If localReceiversTable is null, then createReceivers()
            // hasn't been called, so there is nothing to return.
            if (_localReceiversTable == null) {
                return _EMPTY_RECEIVER_ARRAY;
            }

            if (_localReceiversTable.containsKey(relation)) {
                // Get the list of receivers for this relation.
                List<?> list = _localReceiversTable.get(relation);

                try {
                    result = (Receiver[][]) list.get(occurrence);
                } catch (IndexOutOfBoundsException ex) {
                    return _EMPTY_RECEIVER_ARRAY;
                }

                if (result.length != width) {
                    throw new InvalidStateException(this,
                            "getReceivers(IORelation, int): "
                                    + "Invalid receivers. "
                                    + "result.length = " + result.length
                                    + " width = " + width
                                    + ". Need to call createReceivers()?");
                }
            }

            return result;
        } else {
            // If a transparent input port, ask its all inside receivers,
            // and trim the returned Receivers array to get the
            // part corresponding to this occurrence of the IORelation.
            Receiver[][] insideReceivers = getReceivers();

            if (insideReceivers == null) {
                return _EMPTY_RECEIVER_ARRAY;
            }

            int insideWidth = insideReceivers.length;
            int index = 0;
            result = new Receiver[width][];

            Iterator<?> outsideRelations = linkedRelationList().iterator();
            int seen = 0;

            while (outsideRelations.hasNext()) {
                IORelation outsideRelation = (IORelation) outsideRelations
                        .next();

                // A null link (supported since indexed links) might
                // yield a null relation here. EAL 7/19/00.
                if (outsideRelation != null) {
                    if (outsideRelation == relation) {
                        if (seen == occurrence) {
                            // Have to be careful here to get the right
                            // occurrence of the relation.  EAL 7/30/00.
                            result = new Receiver[width][];

                            int receiverSize = java.lang.Math.min(width,
                                    insideWidth - index);

                            for (int i = 0; i < receiverSize; i++) {
                                result[i] = insideReceivers[index++];
                            }

                            break;
                        } else {
                            seen++;
                            index += outsideRelation.getWidth();

                            if (index > insideWidth) {
                                break;
                            }
                        }
                    } else {
                        index += outsideRelation.getWidth();

                        if (index > insideWidth) {
                            break;
                        }
                    }
                }
            }

            return result;
        }
    }

    /** Return the width of the port.  The width is the sum of the
     *  widths of the relations that the port is linked to (on the outside).
     *  Note that this method cannot be used to determine whether a port
     *  is connected (deeply) to another port that can either supply it with
     *  data or consume data it produces.  The correct methods to use to
     *  determine that are numberOfSinks() and numberOfSources().
     *  This method is read-synchronized on the workspace.
     *  This method will trigger the width inference algorithm if necessary.
     *  @param createReceivers True if {@link #getReceivers()} should be called
     *  if the cached width of the port is not the same as the sum of the width
     *  of the linked relations.
     *  @return The width of the port.
     *  @exception IllegalActionException
     *  @see #numberOfSinks()
     *  @see #numberOfSources()
     */
    private int _getWidth(boolean createReceivers)
            throws IllegalActionException {
        try {
            _workspace.getReadAccess();

            long version = _workspace.getVersion();

            if (_widthVersion != version) {

                // If this is not a multiport, the width is always zero or one.
                int sum = 0;
                Iterator<?> relations = linkedRelationList().iterator();

                while (relations.hasNext()) {
                    IORelation relation = (IORelation) relations.next();

                    // A null link (supported since indexed links) might
                    // yield a null relation here. EAL 7/19/00.
                    if (relation != null) {

                        // Calling getWidth on a relation for which the width has to be
                        // inferred will trigger the width inference algorithm here.
                        sum += relation.getWidth();
                    }
                }

                if (!isMultiport()) {
                    if (sum > 0) {
                        _width = 1;
                    } else {
                        _width = 0;
                    }
                } else {
                    if (_width != sum) {
                        // Need to re-create receivers for Pub/Sub in Opaques
                        // See getWidthInside() for a similar piece of code.
                        // Need to check to see if there is a director, otherwise
                        // _description will fail on MaximumEntropySpectrum, see
                        // ptolemy/domains/sdf/lib/test/MaximumEntropySpectrum.tcl
                        if (createReceivers
                                && isOpaque()
                                && ((Actor) getContainer()).getDirector() != null) {
                            // FIXME: maybe we should only call createReceivers()
                            // if the sum is less than the _width (see below).
                            createReceivers();
                        }
                        _width = sum;
                    }
                }
                _widthVersion = version;
            }

            return _width;
        } finally {
            _workspace.doneReading();
        }
    }

    private void _init() throws IllegalActionException {
        try {
            defaultValue = new Parameter(this, "defaultValue");
        } catch (NameDuplicationException e) {
            throw new IllegalActionException(this, e.getCause(), e.getMessage());
        }
    }

    // Invalidate schedule and type resolution and width inference of the director
    // of the container, if there is one.
    private void _invalidate() {
        Nameable container = getContainer();

        if (container instanceof Actor) {
            if (container instanceof CompositeActor) {
                ((CompositeActor) container).notifyConnectivityChange();
            }

            Director director = ((Actor) container).getDirector();

            if (director != null) {
                director.invalidateSchedule();
                director.invalidateResolvedTypes();
            }
            // Need to do this for the executive director as well because the port
            // may belong to an opaque composite actor.
            Director executiveDirector = ((Actor) container)
                    .getExecutiveDirector();
            if (executiveDirector != null && executiveDirector != director) {
                executiveDirector.invalidateSchedule();
                executiveDirector.invalidateResolvedTypes();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** List of communication aspects specified for the port. */
    private List<CommunicationAspect> _communicationAspects;

    /** The default width. In case there is no unique solution for a relation
     *  connected to this port the default width will be used.
     */
    private int _defaultWidth = -1;

    /** To avoid creating this repeatedly, we use a single version. */
    private static final Receiver[][] _EMPTY_RECEIVER_ARRAY = new Receiver[0][0];

    /** Indicate whether the port is an input, an output, or both.
     * The value may be overridden in transparent ports, in that if
     * a transparent port is inside linked to an input or output port,
     * then it will be considered an inside or output port respectively.
     * This determination is cached, so we need variables to track the
     * validity of the cache.
     * 'transient' means that the variable will not be serialized.
     */
    private boolean _isInput;

    // Indicate whether the port is an input, an output, or both.
    // The value may be overridden in transparent ports, in that if
    // a transparent port is inside linked to an input or output port,
    // then it will be considered an inside or output port respectively.
    // This determination is cached, so we need variables to track the
    // validity of the cache.
    // 'transient' means that the variable will not be serialized.
    private boolean _isOutput;

    private transient long _insideInputVersion = -1;

    private transient long _insideOutputVersion = -1;

    // Flag that the input/output status has been set.
    private boolean _isInputOutputStatusSet = false;

    // Indicate whether the port is a multiport. Default false.
    private boolean _isMultiport = false;

    // The cached inside width of the port, which is the sum of the
    // widths of the inside relations.  The default 0 because
    // initially there are no linked relations.  It is set or updated
    // when getWidthInside() is called.  'transient' means that the
    // variable will not be serialized.
    private transient int _insideWidth = 0;

    // The workspace version number on the last update of the _insideWidth.
    // 'transient' means that the variable will not be serialized.
    private transient long _insideWidthVersion = -1;

    // A cache of the deeply connected Receivers, and the versions.
    // 'transient' means that the variable will not be serialized.
    private transient Receiver[][] _farReceivers;

    private transient long _farReceiversVersion = -1;

    // A cache of the local Receivers, and the version.
    // 'transient' means that the variable will not be serialized.
    private transient Receiver[][] _localReceivers;

    // Lists of local receivers, indexed by relation.
    private HashMap<IORelation, List<Receiver[][]>> _localReceiversTable;

    private transient long _localReceiversVersion = -1;

    // A cache of the local Receivers, and the version.
    // 'transient' means that the variable will not be serialized.
    private transient Receiver[][] _localInsideReceivers;

    private transient long _localInsideReceiversVersion = -1;

    // A cache of the inside Receivers, and the version.
    private transient Receiver[][] _insideReceivers;

    private transient long _insideReceiversVersion = -1;

    // If port is linked to a communication aspect then all tokens
    // are sent to the communication aspect. Receivers and farreceivers
    // are replaced by this intermediate receiver.
    private IntermediateReceiver _intermediateFarReceiver;

    // A cache of the number of sinks, since it's expensive to compute.
    private transient int _numberOfSinks;
    private transient long _numberOfSinksVersion = -1;

    // A cache of the number of sources, since it's expensive to compute.
    private transient int _numberOfSources;
    private transient long _numberOfSourcesVersion = -1;

    /** Value of defaultValue or the most recently received token. */
    private Token _persistentToken;

    // A cache of the sink port list.
    private transient LinkedList<IOPort> _sinkPortList;
    private transient long _sinkPortListVersion;

    // A cache of the source port list.
    private transient LinkedList<IOPort> _sourcePortList;
    private transient long _sourcePortListVersion;

    // The cached width of the port, which is the sum of the widths of the
    // linked relations.  The default 0 because initially there are no
    // linked relations.  It is set or updated when getWidth() is called.
    // 'transient' means that the variable will not be serialized.
    private transient int _width = 0;

    // Constrains on the width of this port (it has to be equal to the parameters).
    private Set<Parameter> _widthEqualToParameter = new HashSet<Parameter>();

    // Constraints on the width of this port (it has to be equal to the width of the port).
    private Set<IOPort> _widthEqualToPort = new HashSet<IOPort>();

    // The workspace version number on the last update of the _width.
    // 'transient' means that the variable will not be serialized.
    private transient long _widthVersion = -1;
}
