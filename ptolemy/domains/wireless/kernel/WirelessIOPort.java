/* A port for sending and receiving in the wireless domain.

 Copyright (c) 1998-2003 The Regents of the University of California.
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
@AcceptedRating Red (sanjeev@eecs.berkeley.edu)
*/

package ptolemy.domains.wireless.kernel;

import java.util.List;

import ptolemy.actor.NoRoomException;
import ptolemy.actor.Receiver;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.domains.de.kernel.DEIOPort;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;

//////////////////////////////////////////////////////////////////////////
//// WirelessIOPort
/**
This port communicates via channels without wired connections.
Channels are instances of WirelessChannel, a subclass of TypedIORelation.
The port references a channel by name, where the name is specified
by either the <i>outsideChannel</i> or <i>insideChannel</i> parameter.
<p>
This port can be used on the boundary of wireless domain models.
In particular, it will use wireless communications on the inside
if an inside channel name is given.
It will use wireless communication on the outside if an outside
channel name is given.  If the named channel does not exist,
then the behavior of the port reverts to that of the base class.
Specifically, it will only communicate if it is wired.
<p>
The width of this port on either side that is using wireless
communication is fixed at one.

@author Edward A. Lee and Xiaojun Liu
@version $Id$
@since Ptolemy II 0.2
*/

public class WirelessIOPort extends DEIOPort {

    /** Construct a port with the specified container and name
     *  that is neither an input nor an output.  The specified container
     *  must implement the Actor interface, or an exception will be thrown.
     *  @param container The container actor.
     *  @param name The name of the port.
     *  @exception IllegalActionException If the port is not of an acceptable
     *   class for the container, or if the container does not implement the
     *   Actor interface.
     *  @exception NameDuplicationException If the name coincides with
     *   a port already in the container.
     */
    public WirelessIOPort(ComponentEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        this(container, name, false, false);
    }

    /** Construct a port with a container and a name that is
     *  either an input, an output, or both, depending on the third
     *  and fourth arguments. The specified container must implement
     *  the Actor interface or an exception will be thrown.
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
    public WirelessIOPort(ComponentEntity container, String name,
            boolean isInput, boolean isOutput)
            throws IllegalActionException, NameDuplicationException {
        super(container, name, isInput, isOutput);
        
        outsideChannel = new StringParameter(this, "outsideChannel");
        outsideChannel.setExpression("");
        
        outsideTransmitProperties = new Parameter(this, "outsideTransmitProperties");
        // FIXME: set type to RecordToken.
        
        insideChannel = new StringParameter(this, "insideChannel");
        insideChannel.setExpression("");
        
        insideTransmitProperties = new Parameter(this, "insideTransmitProperties");
        // FIXME: set type to RecordToken.
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The name of the inside channel.  This is a string that defaults to
     *  the empty string, indicating that communication is not wireless.
     */
    public StringParameter insideChannel;

    /** The transmit properties of this port for inside transmissions.
     *  This is a record containing
     *  fields that may be used by the channel to determine transmission
     *  range or other properties of the transmission. By default, this
     *  has no value, which indicates to channels to use their default
     *  properties, whatever those might be.
     */
    public Parameter insideTransmitProperties;
    
    /** The name of the outside channel.  This is a string that defaults to
     *  the empty string, indicating that communication is not wireless.
     */
    public StringParameter outsideChannel;
    
    /** The transmit properties of this port for outside transmissions.
     *  This is a record containing
     *  fields that may be used by the channel to determine transmission
     *  range or other properties of the transmission. By default, this
     *  has no value, which indicates to channels to use their default
     *  properties, whatever those might be.
     */
    public Parameter outsideTransmitProperties;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to delegate to the channel if there is
     *  one. If there is not outside channel, then send as in the base
     *  class to connected ports.
     *  @param token The token to send.
     *  @exception IllegalActionException If the port is not an output,
     *   or if the <i>outsideChannel</i> parameter cannot be evaluated.
     */
    public void broadcast(Token token) throws IllegalActionException {
        WirelessChannel channel = getOutsideChannel();
        if (channel != null) {
            if (_debugging) {
                _debug("broadcast to wireless channel " + channel.getName() + ": " + token);
            }
            channel.transmit(token, this, outsideTransmitProperties.getToken());
        } else {
            super.broadcast(token);
        }
    }
    
    /** Override the base class to delegate to the channel if there is
     *  one. If there is not outside channel, then send as in the base
     *  class to connected ports.
     *  @param tokenArray The token array to send
     *  @param vectorLength The number of elements of the token
     *   array to send.
     *  @exception NoRoomException If there is no room in the receiver.
     *  @exception IllegalActionException If the tokens to be sent cannot
     *   be converted to the type of this port
     */
    public void broadcast(Token[] tokenArray, int vectorLength)
            throws IllegalActionException, NoRoomException {
            
        WirelessChannel channel = getOutsideChannel();
        if (channel != null) {
            if (_debugging) {
                _debug("broadcast array of tokens to wireless channel "
                        + channel.getName());
            }
            for (int i = 0; i < tokenArray.length; i++) {
                Token token = tokenArray[i];
                // FIXME: Check types.
                channel.transmit(token, this, outsideTransmitProperties.getToken());
            }
        } else {
            super.broadcast(tokenArray, vectorLength);
        }
    }

    
    /** Override the base class to create a single receiver if
     *  @exception IllegalActionException If this port is not
     *   an opaque input port or if there is no director.
     */
    public void createReceivers() throws IllegalActionException {
        // This call will create receivers based on relations that
        // are linked to the port.
        super.createReceivers();
        if (getOutsideChannel() != null) {
            _receivers = new Receiver[1][1];
            _receivers[0][0] = _newReceiver();
        }
        if (getInsideChannel() != null) {
            _insideReceivers = new Receiver[1][1];
            _insideReceivers[0][0] = _newInsideReceiver();
        }
    }

    /** Get the channel specified by the <i>insideChannel</i> parameter.
     *  The channel is contained by the container of this  port.
     *  @return A channel, or null if there is none.
     *  @exception IllegalActionException If the <i>insideChannel</i> parameter
     *   value cannot be evaluated.
     */
    public WirelessChannel getInsideChannel() throws IllegalActionException {
        // FIXME: Should this be cached?
        String channelName = insideChannel.stringValue();
        Nameable container = getContainer();
        if (container instanceof CompositeEntity) {
            ComponentEntity entity
                    = ((CompositeEntity)container).getEntity(channelName);
            if (entity instanceof WirelessChannel) {
                return (WirelessChannel)entity;
            }
        }
        return null;
    }
    
    /** Override the base class to return the inside receiver for wireless
     *  communication if wireless communication is being used. Otherwise,
     *  defer to the base class.
     *  @return The local inside receivers, or an empty array if there are
     *   none.
     */
    public Receiver[][] getInsideReceivers() {
        try {
            if (getInsideChannel() != null) {
                return _insideReceivers;
            } else {
                return super.getInsideReceivers();
            }
        } catch (IllegalActionException e) {
            // We should not get this far without being able
            // to parse the inside channel specification.
            throw new InternalErrorException(e);
        }
    }

    /** Get the channel specified by the <i>outsideChannel</i> parameter.
     *  The channel is contained by the container of the container of this
     *  port.
     *  @return A channel, or null if there is none.
     *  @exception IllegalActionException If the <i>outsideChannel</i> parameter
     *   value cannot be evaluated.
     */
    public WirelessChannel getOutsideChannel() throws IllegalActionException {
        // FIXME: Should this be cached?
        String channelName = outsideChannel.stringValue();
        Nameable container = getContainer();
        if (container != null) {
            Nameable containersContainer = container.getContainer();
            if (containersContainer instanceof CompositeEntity) {
                ComponentEntity channel
                        = ((CompositeEntity)containersContainer)
                        .getEntity(channelName);
                if (channel instanceof WirelessChannel) {
                    return (WirelessChannel)channel;
                }
            }
        }
        return null;
    }

    /** Override the base class to return the outside receiver for wireless
     *  communication if wireless communication is being used. Otherwise,
     *  defer to the base class.
     *  @return The local receivers, or an empty array if there are none.
     */
    public Receiver[][] getReceivers() {
        try {
            if (getOutsideChannel() != null) {
                return _receivers;
            } else {
                return super.getReceivers();
            }
        } catch (IllegalActionException e) {
            // We should not get to here if the channel name cannot be
            // parsed.
            throw new InternalErrorException(e);
        }
    }
    
    /** Get the width of the port. If the outside is wireless, then
     *  the width is always 1. Otherwise, it depends on the number of
     *  links to the port.
     *  @return The width of the port.
     */
    public int getWidth() {
        if (_outsideIsWireless()) {
            return 1;
        } else {
            return super.getWidth();
        }
    }

    /** Return the inside width of this port. If the inside is wireless,
     *  then the width is always 1. Otherwise, the width is determined by
     *  the number of links to the port.
     *  @return The inside width of this port.
     */
    public int getWidthInside() {
        if (_insideIsWireless()) {
            return 1;
        } else {
            return super.getWidthInside();
        }
    }

    /** Override the base class to delegate to the channel if there is
     *  one. If there is not outside channel, then send as in the base
     *  class to connected ports.  If there is an outside channel, then
     *  the channelIndex argument is ignored.
     *  @param channelIndex The index of the channel, from 0 to width-1.
     *  @param token The token to send.
     *  @exception IllegalActionException If the port is not an output,
     *   or if the token to be sent cannot
     *   be converted to the type of this port, or if the token is null.
     *  @exception NoRoomException If there is no room in the receiver.
     *   This should not occur in the DE domain.
     */
    public void send(int channelIndex, Token token)
            throws IllegalActionException, NoRoomException {
        WirelessChannel channel = getOutsideChannel();
        if (channel != null) {
            if (_debugging) {
                _debug("send to wireless channel " + channel.getName() + ": " + token);
            }
            channel.transmit(token, this, outsideTransmitProperties.getToken());
        } else {
            super.send(channelIndex, token);
        }
    }
    
    /** Override the base class to delegate to the channel if there is
     *  one. If there is not outside channel, then send as in the base
     *  class to connected ports.
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
        WirelessChannel channel = getOutsideChannel();
        if (channel != null) {
            if (_debugging) {
                _debug("broadcast array of tokens to wireless channel "
                        + channel.getName());
            }
            for (int i = 0; i < tokenArray.length; i++) {
                Token token = tokenArray[i];
                // FIXME: Check types.
                channel.transmit(token, this, outsideTransmitProperties.getToken());
            }
        } else {
            super.send(channelIndex, tokenArray, vectorLength);
        }
    }
    
    // FIXME: Where is the sendInside vector version?

    /** Override the base class so that if the inside is wireless, then
     *  the wireless channel is used.
     *  @param channelIndex The index of the channel, from 0 to width-1
     *  @param token The token to send
     *  @exception NoRoomException If there is no room in the receiver.
     *  @exception IllegalActionException If conversion to the type of
     *   the destination port cannot be done.
     */
    public void sendInside(int channelIndex, Token token)
            throws IllegalActionException, NoRoomException {
        WirelessChannel channel = getInsideChannel();
        if (channel != null) {
            if (_debugging) {
                _debug("send inside to wireless channel " + channel.getName() + ": " + token);
            }
            channel.transmit(token, this, insideTransmitProperties.getToken());
        } else {
            super.sendInside(channelIndex, token);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                   ////

    /** Return true if the inside of this port is wireless.
     *  The inside is wireless if getInsideChannel() returns non-null
     *  and does not throw an exception.
     *  @return True if the inside is wireless.
     */
    protected boolean _insideIsWireless() {
        try {
            return (getInsideChannel() != null);
        } catch (IllegalActionException e) {
            return false;
        }
    }

    /** Return true if the outside of this port is wireless.
     *  The outside is wireless if getOutsideChannel() returns non-null
     *  and does not throw an exception.
     *  @return True if the outside is wireless.
     */
    protected boolean _outsideIsWireless() {
        try {
            return (getOutsideChannel() != null);
        } catch (IllegalActionException e) {
            return false;
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // To ensure that getReceivers() and variants never return null.
    private static Receiver[][] _EMPTY_RECEIVERS = new Receiver[0][0];
    
    // Receivers for this port for outside wireless connections.
    private Receiver[][] _receivers;
    
    // Receivers for this port for inside wireless connections.
    private Receiver[][] _insideReceivers;


    
    // FIXME: Move these up to public space and implement.
    

    /* (non-Javadoc)
     * @see ptolemy.actor.IOPort#broadcastClear()
     */
    public void broadcastClear() throws IllegalActionException {
        // TODO Auto-generated method stub
        super.broadcastClear();
    }

    /* (non-Javadoc)
     * @see ptolemy.actor.IOPort#hasRoom(int)
     */
    public boolean hasRoom(int channelIndex) throws IllegalActionException {
        // TODO Auto-generated method stub
        return super.hasRoom(channelIndex);
    }

    /* (non-Javadoc)
     * @see ptolemy.actor.IOPort#hasRoomInside(int)
     */
    public boolean hasRoomInside(int channelIndex)
        throws IllegalActionException {
        // TODO Auto-generated method stub
        return super.hasRoomInside(channelIndex);
    }

    /* (non-Javadoc)
     * @see ptolemy.actor.IOPort#insideSinkPortList()
     */
    public List insideSinkPortList() {
        // TODO Auto-generated method stub
        return super.insideSinkPortList();
    }

    /* (non-Javadoc)
     * @see ptolemy.actor.IOPort#insideSourcePortList()
     */
    public List insideSourcePortList() {
        // TODO Auto-generated method stub
        return super.insideSourcePortList();
    }

    /* (non-Javadoc)
     * @see ptolemy.actor.IOPort#numberOfSinks()
     */
    public int numberOfSinks() {
        // TODO Auto-generated method stub
        return super.numberOfSinks();
    }

    /* (non-Javadoc)
     * @see ptolemy.actor.IOPort#numberOfSources()
     */
    public int numberOfSources() {
        // TODO Auto-generated method stub
        return super.numberOfSources();
    }

    /* (non-Javadoc)
     * @see ptolemy.actor.IOPort#sendClear(int)
     */
    public void sendClear(int channelIndex) throws IllegalActionException {
        // TODO Auto-generated method stub
        super.sendClear(channelIndex);
    }

    /* (non-Javadoc)
     * @see ptolemy.actor.IOPort#sendClearInside(int)
     */
    public void sendClearInside(int channelIndex)
        throws IllegalActionException {
        // TODO Auto-generated method stub
        super.sendClearInside(channelIndex);
    }

    /* (non-Javadoc)
     * @see ptolemy.actor.IOPort#sinkPortList()
     */
    public List sinkPortList() {
        // TODO Auto-generated method stub
        return super.sinkPortList();
    }

    /* (non-Javadoc)
     * @see ptolemy.actor.IOPort#sourcePortList()
     */
    public List sourcePortList() {
        // TODO Auto-generated method stub
        return super.sourcePortList();
    }

    /* (non-Javadoc)
     * @see ptolemy.kernel.ComponentPort#numInsideLinks()
     */
    public int numInsideLinks() {
        // TODO Auto-generated method stub
        return super.numInsideLinks();
    }

    /* (non-Javadoc)
     * @see ptolemy.kernel.Port#connectedPortList()
     */
    public List connectedPortList() {
        // TODO Auto-generated method stub
        return super.connectedPortList();
    }

    /* (non-Javadoc)
     * @see ptolemy.kernel.Port#numLinks()
     */
    public int numLinks() {
        // TODO Auto-generated method stub
        return super.numLinks();
    }
}
