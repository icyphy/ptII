/* An aggregation of actors.

 Copyright (c) 2003 The Regents of the University of California.
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

@ProposedRating Green (cxh@eecs.berkeley.edu)
@AcceptedRating Yellow (neuendor@eecs.berkeley.edu)
setDirector throws NameDuplicationException
fire: call transferOutputs on local, not executive director.
preinitialize: validate attributes of this composite and
    the attributes of its ports.
*/

package ptolemy.domains.wireless.kernel;

//import ptolemy.kernel.*;
import java.util.List;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;



//////////////////////////////////////////////////////////////////////////
//// CompositeWirelessChannel
/**
A composite channel that can contains a model inside for its computation.

@author Yang Zhao
@version $Id$
@since Ptolemy II 2.1
*/
public class CompositeWirelessChannel extends TypedCompositeActor
    implements WirelessMedia{
    /** Create an actor with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  This actor will have no
     *  local director initially, and its executive director will be simply
     *  the director of the container.
     *  You should set a director before attempting to execute it.
     *
     *  @param container The container actor.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CompositeWirelessChannel(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        _attachText("_iconDescription", "<svg>\n" +
                "<polygon points=\"-25,0 8,-8 2,2 25,0 -8,8 -2,-2 -25,0\" " +
                "style=\"fill:blue\"/>\n" +
                "</svg>\n");
        _init();
    }



    public ChannelInput channelInput;

    public ChannelOutput channelOutput;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /* (non-Javadoc)
     * @see ptolemy.domains.wireless.kernel.WirelessMedia#listeningInputPorts()
     */
    public List listeningInputPorts() throws IllegalActionException {
        try {
            workspace().getReadAccess();
            CompositeEntity container = (CompositeEntity)getContainer();
            return ModelTopology.listeningInputPorts(container,
                    this.getName());
        } finally {
            workspace().doneReading();
        }
    }

    /* (non-Javadoc)
     * @see ptolemy.domains.wireless.kernel.WirelessMedia#listeningOutputPorts()
     */
    public List listeningOutputPorts() throws IllegalActionException {
        try {
            workspace().getReadAccess();
            CompositeEntity container = (CompositeEntity)getContainer();
            return ModelTopology.listeningOutputPorts(container,
                    this.getName());
        } finally {
            workspace().doneReading();
        }
    }

    /* (non-Javadoc)
     * @see ptolemy.domains.wireless.kernel.WirelessMedia#sendingInputPorts()
     */
    public List sendingInputPorts() throws IllegalActionException {
        try {
            workspace().getReadAccess();
            CompositeEntity container = (CompositeEntity)getContainer();
            return ModelTopology.sendingInputPorts(container, this.getName());
        } finally {
            workspace().doneReading();
        }
    }

    /* (non-Javadoc)
     * @see ptolemy.domains.wireless.kernel.WirelessMedia#sendingOutputPorts()
     */
    public List sendingOutputPorts() throws IllegalActionException {
        try {
            workspace().getReadAccess();
            CompositeEntity container = (CompositeEntity)getContainer();
            return ModelTopology.sendingOutputPorts(container, this.getName());
        } finally {
            workspace().doneReading();
        }
    }

    /** Transmit the specified token from the specified port with the
     *  specified properties. This method call the put() method of
     *  the contained ChannelInput actor to transfer the token to the
     *  inside model.
     *  @param token The token to transmit, or null to clear all
     *   receivers that are in range.
     *  @param port The port from which this is being transmitted.
     *  @param properties The transmit properties (ignored in this base class).
     *  @exception IllegalActionException If a location cannot be evaluated
     *   for a port, or if a type conflict occurs.
     */
    public void transmit(Token token, WirelessIOPort port,
            RecordToken properties)
            throws IllegalActionException {
        try {
            workspace().getReadAccess();
            if (_debugging) {
                _debug("----\nTransmitting from port: " + port.getFullName());
                _debug("Token value: " + token.toString());
            }
            channelInput.put(token, port, properties);
        } finally {
            workspace().doneReading();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // create the ChannelInput and ChannelOutput actor.
    private void _init()
            throws IllegalActionException, NameDuplicationException {

        try {
            channelInput = new ChannelInput(this, "ChannelInput");
            channelOutput = new ChannelOutput(this, "ChannelOutput");
        } catch (NameDuplicationException ex) {
            throw new InternalErrorException("NameDuplication");
        } catch (IllegalActionException ex) {
            throw new InternalErrorException("IllegalAction:"+
                    ex.getMessage());
        }
        // The base class identifies the class name as TypedCompositeActor
        // irrespective of the actual class name.  We override that here.
        getMoMLInfo().className =
            "ptolemy.domains.wireless.kernel.CompositeWirelessChannel";
    }
}
