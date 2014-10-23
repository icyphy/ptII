/* A port for mediate type constraints between senders and receivers.

 Copyright (c) 2003-2014 The Regents of the University of California.
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
package ptolemy.domains.wireless.kernel;

import java.util.List;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// ChannelPort

/**
 This port is used in a channel to mediate type constraints between
 senders and receivers on a wireless channel. The way it works is by
 pretending that it receives data from the sender and sends data to
 the receiver. It does not actually get involved in the transmission
 of data (that is handled by the transmit() method of the channel).
 This port is always both an input and an output.  This port is not
 persistent and is always hidden in a user interface.
 <p>
 NOTE: Someday, perhaps
 this port will be augmented to mediate the communication and
 facilitate the construction of channel models as composite
 actors instead of atomic actors).

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Green (cxh)
 @Pt.AcceptedRating Yellow (cxh)
 */
public class ChannelPort extends WirelessIOPort {
    /** Construct a port with the specified container and name
     *  that is both an input and an output.
     *  @param container The container actor.
     *  @param name The name of the port.
     *  @exception IllegalActionException If the port is not of an acceptable
     *   class for the container, or if the container does not implement the
     *   Actor interface.
     *  @exception NameDuplicationException If the name coincides with
     *   a port already in the container.
     */
    public ChannelPort(ComponentEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name, true, true);
        setPersistent(false);

        SingletonParameter hide = new SingletonParameter(this, "_hide");
        hide.setToken(BooleanToken.TRUE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new Attribute.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ChannelPort newObject = (ChannelPort) super.clone(workspace);
        newObject._sourcePortList = null;
        newObject._sourcePortListVersion = -1;
        newObject._sinkPortList = null;
        newObject._sinkPortListVersion = -1;
        return newObject;
    }

    /** Return the number of sink ports that can potentially receive data
     *  from the containing channel.
     *  @return The number of ports that can receive data from this one.
     */
    @Override
    public int numberOfSinks() {
        return sinkPortList().size();
    }

    /** Return the number of source ports that can potentially send data
     *  to the containing channel.
     *  @return The number of ports that can send data to this one.
     */
    @Override
    public int numberOfSources() {
        return sourcePortList().size();
    }

    /** Override the base class to ensure that the proposed container
     *  implements the WirelessChannel interface.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the proposed container is not a
     *   ComponentEntity, doesn't implement WirelessChannel, or has no name,
     *   or the port and container are not in the same workspace.
     *  @exception NameDuplicationException If the container already has
     *   a port with the name of this port.
     */
    @Override
    public void setContainer(Entity container) throws IllegalActionException,
    NameDuplicationException {
        if (container instanceof WirelessChannel) {
            super.setContainer(container);
        } else {
            throw new IllegalActionException(this,
                    "ChannelPort can only be contained by an "
                            + "instance of WirelessChannel.");
        }
    }

    /** Return a list of the ports that can potentially accept data from
     *  the containing channel.  This includes all input ports that use that
     *  channel on the outside and all output ports that use the channel
     *  on the inside.
     *  @return A list of WirelessIOPort objects.
     */
    @Override
    public List sinkPortList() {
        if (_sinkPortListVersion == workspace().getVersion()) {
            return _sinkPortList;
        }

        try {
            WirelessChannel channel = (WirelessChannel) getContainer();
            _sinkPortList = channel.listeningInputPorts();
            _sinkPortList.addAll(channel.listeningOutputPorts());
            _sinkPortListVersion = workspace().getVersion();
            return _sinkPortList;
        } catch (IllegalActionException e) {
            // This is not ideal, but the base class doesn't
            // declare exceptions here.
            throw new InternalErrorException(e);
        }
    }

    /** Return a list of the ports that can potentially send data to
     *  the containing channel. This includes all output ports that
     *  use that channel on the outside and all input ports that
     *  use it on the inside.
     *  @return A list of IOPort objects.
     */
    @Override
    public List sourcePortList() {
        if (_sourcePortListVersion == workspace().getVersion()) {
            return _sourcePortList;
        }

        try {
            WirelessChannel channel = (WirelessChannel) getContainer();
            _sourcePortList = channel.sendingOutputPorts();
            _sourcePortList.addAll(channel.sendingInputPorts());
            _sourcePortListVersion = workspace().getVersion();
            return _sourcePortList;
        } catch (IllegalActionException e) {
            // This is not ideal, but the base class doesn't
            // declare exceptions here.
            throw new InternalErrorException(e);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    private List _sourcePortList;

    private long _sourcePortListVersion = -1;

    private List _sinkPortList;

    private long _sinkPortListVersion = -1;
}
