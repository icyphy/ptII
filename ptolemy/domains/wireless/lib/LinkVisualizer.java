/* An actor that displays link properties.

 Copyright (c) 2006-2014 The Regents of the University of California.
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
package ptolemy.domains.wireless.lib;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.domains.wireless.kernel.ChannelListener;
import ptolemy.domains.wireless.kernel.WirelessChannel;
import ptolemy.domains.wireless.kernel.WirelessIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.vergil.kernel.attributes.LineAttribute;

///////////////////////////////////////////////////////////////////
//// LinkVisualizer

/**
 This actor implements the ChannelListener interface.
 It creates a line between two communicating nodes that
 are within range of one another. It registers itself
 with the wireless channel specified by the
 <i>channelName</i> parameter. This is notified whenever a transmission
 occurs on the channel on which it is listening.

 This is an actor because it registers itself with the channel in the
 initialize() method.

 @author Heather Taylor, Elaine Cheong
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Yellow (celaine)
 @Pt.AcceptedRating Yellow (celaine)
 */
public class LinkVisualizer extends TypedAtomicActor implements ChannelListener {
    /** Construct an actor with the specified container and name.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public LinkVisualizer(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        channelName = new StringParameter(this, "channelName");
        channelName.setExpression("AtomicWirelessChannel");

        sleepTime = new Parameter(this, "sleepTime", new IntToken(500));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The name of the channel.  The default name is "AtomicWirelessChannel".
     */
    public StringParameter channelName;

    /** The amount of time to sleep in milliseconds between drawing the
     *  line and then removing the line.  The initial default value
     *  is an IntToken with a value of 500, meaning sleep for 500 ms.
     */
    public Parameter sleepTime;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Register ChannelListeners with the WirelessChannel
     *  specified in the channelName parameter.
     *
     *  @exception IllegalActionException If the super class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        _isOff = Boolean.TRUE;

        // Get the channel specified by the channelName parameter.
        CompositeEntity container = (CompositeEntity) getContainer();
        Entity channel = container.getEntity(channelName.stringValue());

        if (channel instanceof WirelessChannel) {
            _channel = (WirelessChannel) channel;
            ((WirelessChannel) channel).addChannelListener(this);
        } else {
            throw new IllegalActionException(this,
                    "The channel name does not refer to a valid channel.");
        }
    }

    /** Visualize a line between the sender and destination containers
     *  by starting a thread that will create and remove the line after some
     *  amount of time.
     *  @param properties The properties of this transmission.
     *  @param token The token of this transmission, which can be processed here.
     *  @param sender The sending port.
     *  @param destination The receiving port.
     */
    @Override
    public void channelNotify(RecordToken properties, Token token,
            WirelessIOPort sender, WirelessIOPort destination) {
        // Create a name for the line to be visualized.
        String lineName = getContainer().uniqueName("_senderDestLine");
        // Create a thread to visualize the line.
        _LinkVisualizerThread linkVisualizerThread = new _LinkVisualizerThread(
                sender, destination, lineName);
        // Start the thread.
        linkVisualizerThread.start();
    }

    /** Override the base class to remove this channel listener.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the action would result in a
     *   recursive containment structure, or if
     *   this entity and container are not in the same workspace.
     *  @exception NameDuplicationException If the container already has
     *   an entity with the name of this entity.
     */
    @Override
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        super.setContainer(container);
        if (container == null) {
            if (_channel != null) {
                _channel.removeChannelListener(this);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Draw a line from the sender container to the destination container
     *  with name lineName.
     *  @param sender The sender port.
     *  @param destination The destination port.
     *  @param lineName The name of the line attribute to create.
     */
    protected void _drawLine(WirelessIOPort sender, WirelessIOPort destination,
            final String lineName) {
        Location senderLocation = (Location) sender.getContainer()
                .getAttribute("_location");
        Location destinationLocation = (Location) destination.getContainer()
                .getAttribute("_location");
        double x = destinationLocation.getLocation()[0]
                - senderLocation.getLocation()[0];
        double y = destinationLocation.getLocation()[1]
                - senderLocation.getLocation()[1];
        String moml = "<property name=\""
                + lineName
                + "\" class=\"ptolemy.vergil.kernel.attributes.LineAttribute\">"
                + senderLocation.exportMoML() + "<property name=\"x\" value=\""
                + x + "\"/>" + "<property name=\"y\" value=\"" + y + "\"/>"
                + "</property>";
        ChangeRequest request = new MoMLChangeRequest(this, getContainer(),
                moml) {
            @Override
            protected void _execute() throws Exception {
                try {
                    super._execute();
                    LineAttribute line = (LineAttribute) getContainer()
                            .getAttribute(lineName);
                    line.moveToLast();
                    line.setPersistent(false);
                } catch (Throwable throwable) {
                    // Do nothing.
                }
            }
        };
        requestChange(request);
    }

    /** Remove the line previously created with name lineName.
     *
     * @param lineName Name of line previously created.
     */
    protected void _removeLine(String lineName) {
        String moml = "<deleteProperty name=\"" + lineName + "\"/>";
        ChangeRequest request = new MoMLChangeRequest(this, getContainer(),
                moml) {
            @Override
            protected void _execute() throws Exception {
                try {
                    super._execute();
                } catch (Exception e) {
                    // Do nothing.
                }
            }
        };
        requestChange(request);
    }

    /** Private class that visualizes a link in a thread.
     */
    protected class _LinkVisualizerThread extends Thread {
        /** Create a _LinkVisualizerThread.
         *  @param inputStream The stream to read from.
         *  @param name The name of this _LinkVisualizerThread.
         *  @param stringWriter The StringWriter that is written.
         */

        /** Create a _LinkVisualizerThread.
         *  @param sender The sender port.
         *  @param destination The destination port.
         *  @param lineName The name of the line attribute to create.
         */
        public _LinkVisualizerThread(WirelessIOPort sender,
                WirelessIOPort destination, final String lineName) {
            _sender = sender;
            _destination = destination;
            _lineName = lineName;
        }

        /** Draw a line, sleep for specified amount of time,
         *  and then remove the line.
         */
        @Override
        public void run() {
            _drawLine(_sender, _destination, _lineName);
            try {
                int time;
                try {
                    time = ((IntToken) sleepTime.getToken()).intValue();
                } catch (IllegalActionException e) {
                    // If getting the parameter value was unsuccessful,
                    // use the default value instead.
                    time = _millisToSleep;
                }
                Thread.sleep(time);
            } catch (InterruptedException e) {
                // Do nothing.
            }
            _removeLine(_lineName);
        }

        WirelessIOPort _sender;

        WirelessIOPort _destination;

        final String _lineName;

        /** Default value of time to sleep. */
        final static int _millisToSleep = 500;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Status of line that visualizes the radio link.
     *  Initialized to true. */
    protected Boolean _isOff;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Channel specified by the channelName parameter. */
    private WirelessChannel _channel;
}
