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

import java.util.Enumeration;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.NoRoomException;
import ptolemy.data.Token;
import ptolemy.data.expr.StringParameter;
import ptolemy.domains.de.kernel.DEIOPort;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.IllegalActionException;
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
        
        insideChannel = new StringParameter(this, "insideChannel");
        insideChannel.setExpression("");

        outsideChannel = new StringParameter(this, "outsideChannel");
        outsideChannel.setExpression("");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The name of the inside channel.  This is a string that defaults to
     *  the empty string, indicating that communication is not wireless.
     */
    public StringParameter insideChannel;
    
    /** The name of the outside channel.  This is a string that defaults to
     *  the empty string, indicating that communication is not wireless.
     */
    public StringParameter outsideChannel;

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
            // FIXME: Need the transmit power to replace 0.0.
            channel.transmit(token, this, 0.0);
        } else {
            super.broadcast(token);
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
            ComponentRelation relation
                    = ((CompositeEntity)container).getRelation(channelName);
            if (relation instanceof WirelessChannel) {
                return (WirelessChannel)relation;
            }
        }
        return null;
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
                ComponentRelation relation
                        = ((CompositeEntity)containersContainer)
                        .getRelation(channelName);
                if (relation instanceof WirelessChannel) {
                    return (WirelessChannel)relation;
                }
            }
        }
        return null;
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
            // FIXME: Need the transmit power to replace 0.0.
            channel.transmit(token, this, 0.0);
        } else {
            super.send(channelIndex, token);
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
    
    // FIXME: Move these up to public space.
    

    /* (non-Javadoc)
     * @see ptolemy.actor.IOPort#broadcast(ptolemy.data.Token[], int)
     */
    public void broadcast(Token[] tokenArray, int vectorLength)
        throws IllegalActionException, NoRoomException {
        // TODO Auto-generated method stub
        super.broadcast(tokenArray, vectorLength);
    }

    /* (non-Javadoc)
     * @see ptolemy.actor.IOPort#send(int, ptolemy.data.Token[], int)
     */
    public void send(int channelIndex, Token[] tokenArray, int vectorLength)
        throws IllegalActionException, NoRoomException {
        // TODO Auto-generated method stub
        super.send(channelIndex, tokenArray, vectorLength);
    }

    /* (non-Javadoc)
     * @see ptolemy.actor.IOPort#sendInside(int, ptolemy.data.Token)
     */
    public void sendInside(int channelIndex, Token token)
        throws IllegalActionException, NoRoomException {
        // TODO Auto-generated method stub
        super.sendInside(channelIndex, token);
    }

    /* (non-Javadoc)
     * @see ptolemy.actor.IOPort#broadcastClear()
     */
    public void broadcastClear() throws IllegalActionException {
        // TODO Auto-generated method stub
        super.broadcastClear();
    }

    /* (non-Javadoc)
     * @see ptolemy.actor.IOPort#deepConnectedInPortList()
     */
    public List deepConnectedInPortList() {
        // TODO Auto-generated method stub
        return super.deepConnectedInPortList();
    }

    /* (non-Javadoc)
     * @see ptolemy.actor.IOPort#deepConnectedOutPortList()
     */
    public List deepConnectedOutPortList() {
        // TODO Auto-generated method stub
        return super.deepConnectedOutPortList();
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
     * @see ptolemy.actor.IOPort#hasToken(int, int)
     */
    public boolean hasToken(int channelIndex, int tokens)
        throws IllegalActionException {
        // TODO Auto-generated method stub
        return super.hasToken(channelIndex, tokens);
    }

    /* (non-Javadoc)
     * @see ptolemy.actor.IOPort#hasToken(int)
     */
    public boolean hasToken(int channelIndex) throws IllegalActionException {
        // TODO Auto-generated method stub
        return super.hasToken(channelIndex);
    }

    /* (non-Javadoc)
     * @see ptolemy.actor.IOPort#hasTokenInside(int)
     */
    public boolean hasTokenInside(int channelIndex)
        throws IllegalActionException {
        // TODO Auto-generated method stub
        return super.hasTokenInside(channelIndex);
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
     * @see ptolemy.actor.IOPort#isKnown()
     */
    public boolean isKnown() throws IllegalActionException {
        // TODO Auto-generated method stub
        return super.isKnown();
    }

    /* (non-Javadoc)
     * @see ptolemy.actor.IOPort#isKnown(int)
     */
    public boolean isKnown(int channelIndex) throws IllegalActionException {
        // TODO Auto-generated method stub
        return super.isKnown(channelIndex);
    }

    /* (non-Javadoc)
     * @see ptolemy.actor.IOPort#isKnownInside(int)
     */
    public boolean isKnownInside(int channelIndex)
        throws IllegalActionException {
        // TODO Auto-generated method stub
        return super.isKnownInside(channelIndex);
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
     * @see ptolemy.kernel.ComponentPort#insideRelationList()
     */
    public List insideRelationList() {
        // TODO Auto-generated method stub
        return super.insideRelationList();
    }

    /* (non-Javadoc)
     * @see ptolemy.kernel.ComponentPort#insideRelations()
     */
    public Enumeration insideRelations() {
        // TODO Auto-generated method stub
        return super.insideRelations();
    }

    /* (non-Javadoc)
     * @see ptolemy.kernel.ComponentPort#isDeeplyConnected(ptolemy.kernel.ComponentPort)
     */
    public boolean isDeeplyConnected(ComponentPort port) {
        // TODO Auto-generated method stub
        return super.isDeeplyConnected(port);
    }

    /* (non-Javadoc)
     * @see ptolemy.kernel.ComponentPort#isInsideLinked(ptolemy.kernel.Relation)
     */
    public boolean isInsideLinked(Relation relation) {
        // TODO Auto-generated method stub
        return super.isInsideLinked(relation);
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
     * @see ptolemy.kernel.Port#connectedPorts()
     */
    public Enumeration connectedPorts() {
        // TODO Auto-generated method stub
        return super.connectedPorts();
    }

    /* (non-Javadoc)
     * @see ptolemy.kernel.Port#isLinked(ptolemy.kernel.Relation)
     */
    public boolean isLinked(Relation r) {
        // TODO Auto-generated method stub
        return super.isLinked(r);
    }

    /* (non-Javadoc)
     * @see ptolemy.kernel.Port#linkedRelationList()
     */
    public List linkedRelationList() {
        // TODO Auto-generated method stub
        return super.linkedRelationList();
    }

    /* (non-Javadoc)
     * @see ptolemy.kernel.Port#linkedRelations()
     */
    public Enumeration linkedRelations() {
        // TODO Auto-generated method stub
        return super.linkedRelations();
    }

    /* (non-Javadoc)
     * @see ptolemy.kernel.Port#numLinks()
     */
    public int numLinks() {
        // TODO Auto-generated method stub
        return super.numLinks();
    }

}
