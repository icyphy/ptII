/* A subscriber that transparently receives tunneled messages from publishers.

 Copyright (c) 2006 The Regents of the University of California.
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
package ptolemy.actor.lib;

import java.util.Iterator;

import ptolemy.actor.Director;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// Subscriber

/**
 This actor subscribes to tokens on a named channel. The tokens are
 "tunneled" from an instance of Publisher that names the same channel
 and that is under the control of the same director. That is, it can
 be at a different level of the hierarchy, or in an entirely different
 composite actor, as long as the relevant composite actors are
 transparent (have no director).
 <p>
 Any number of instances of Subscriber can subscribe to the same
 channel.
 <p>
 This actor actually has a hidden input port that is connected
 to the publisher via hidden "liberal links" (links that are
 allowed to cross levels of the hierarchy).  Consequently,
 any data dependencies that the director might assume on a regular
 "wired" connection will also be assumed across Publisher-Subscriber
 pairs.  Similarly, type constraints will probagate across
 Publisher-Subscriber pairs. That is, the type of the Subscriber
 output will match the type of the Publisher input.
 
 @author Edward A. Lee, Raymond A. Cardillo
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Green (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class Subscriber extends TypedAtomicActor {

    /** Construct a subscriber with the specified container and name.
     *  @param container The container actor.
     *  @param name The name of the actor.
     *  @exception IllegalActionException If the actor is not of an acceptable
     *   class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public Subscriber(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        // Set this up as input port.
        super(container, name);

        channel = new StringParameter(this, "channel");
        channel.setExpression("channel1");

        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);
        output = new TypedIOPort(this, "output", false, true);
        output.setMultiport(true);

        new Parameter(input, "_hide", BooleanToken.TRUE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                   ports and parameters                    ////

    /** The name of the channel.  Subscribers that reference this same
     *  channel will receive any transmissions to this port.
     *  This is a string that defaults to "channel1".
     */
    public StringParameter channel;

    /** The input port.  This base class imposes no type constraints except
     *  that the type of the input cannot be greater than the type of the
     *  output.
     */
    public TypedIOPort input;

    /** The output port. By default, the type of this output is constrained
     *  to be at least that of the input. This port is hidden by default
     *  and the actor handles creating connections to it.
     */
    public TypedIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute is the channel, increment the workspace version
     *  to force cached receiver lists to be updated, and invalidate
     *  the schedule and resolved types of the director, if there is one.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == channel) {
            String newValue = channel.stringValue();
            if (!newValue.equals(_channel)) {
                _channel = newValue;
                // If we are within a class definition, then we should
                // not create any links.  The links should only exist
                // within instances. Otherwise, we could end up creating
                // a link between a class definition and an instance.
                if (!isWithinClassDefinition()) {
                    _updateLinks();
                }
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Determine whether a channel name matches this subscriber.
     *  This base class returns true if the specified string
     *  is equal to the value of the <i>channel</i> parameter.
     *  @param channelName A channel name.
     *  @return True if this subscriber subscribes to the specified channel.
     */
    protected boolean channelMatches(String channelName) {
        if (_channel == null) {
            return false;
        }
        return _channel.equals(channelName);
    }

    /** Read at most one input token from each input
     *  channel and send it to the output.
     *  @exception IllegalActionException If there is no director, or
     *   if there is no input connection.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        int width = input.getWidth();
        if (width == 0) {
            throw new IllegalActionException(this,
                    "Subscriber has no matching Publisher.");
        }
        for (int i = 0; i < width; i++) {
            if (input.hasToken(i)) {
                Token token = input.get(i);
                if (i < output.getWidth()) {
                    output.send(i, token);
                }
            }
        }
    }

    /** Override the base class to ensure that there is a publisher.
     *  @exception IllegalActionException If there is no matching
     *   publisher.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        // If this was created by instantiating a container class,
        // then the links would not have been updated when setContainer()
        // was called, so we must do it now.
        if (!_updatedLinks) {
            _updateLinks();
        }
        int width = input.getWidth();
        if (width == 0) {
            throw new IllegalActionException(this,
                    "Subscriber has no matching Publisher.");
        }
    }

    /** Override the base class to remove prior links, if any, and
     *  to create new links, if appropriate.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the action would result in a
     *   recursive containment structure, or if
     *   this entity and container are not in the same workspace.
     *  @exception NameDuplicationException If the container already has
     *   an entity with the name of this entity.
     */
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        if (container != getContainer()) {
            super.setContainer(container);
            // If we are within a class definition, then we should
            // not create any links.  The links should only exist
            // within instances. Otherwise, we could end up creating
            // a link between a class definition and an instance.
            // Note that if we are within an instantiate of a
            // containing class, then we will not update links
            // because this is (temporarily) within a class definition.
            if (!isWithinClassDefinition()) {
                _updateLinks();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                   ////

    /** Update the connection to the publisher, if there is one.
     *  @exception IllegalActionException If creating the link
     *   triggers an exception.
     */
    protected void _updateLinks() throws IllegalActionException {
        // If the channel has not been set, then there is nothing
        // to do.  This is probably the first setContainer() call,
        // before the object is fully constructed.
        if (_channel == null) {
            return;
        }
        Publisher publisher = _findPublisher();

        // Remove the link to a previous relation, if necessary.
        if (_relation != null) {
            input.unlink(_relation);
            _relation = null;
        }
        if (publisher != null) {
            _relation = publisher._relation;
            input.liberalLink(_relation);
        }
        Director director = getDirector();
        if (director != null) {
            director.invalidateSchedule();
            director.invalidateResolvedTypes();
        }
        _updatedLinks = true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected variables                 ////

    /** Cached channel name. */
    protected String _channel;
    
    /** An indicator that _updateLinks has been called at least once. */
    protected boolean _updatedLinks = false;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Find the publisher, if there is one.
     *  @return A publisher, or null if none is found.
     */
    private Publisher _findPublisher() {
        // Find the nearest opaque container above in the hierarchy.
        CompositeEntity container = (CompositeEntity) getContainer();
        while (container != null && !container.isOpaque()) {
            container = (CompositeEntity) container.getContainer();
        }
        if (container != null) {
            Iterator actors = container.deepEntityList().iterator();
            while (actors.hasNext()) {
                Object actor = actors.next();
                if (actor instanceof Publisher) {
                    if (_channel.equals(((Publisher) actor)._channel)) {
                        return (Publisher) actor;
                    }
                }
            }
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The relation used to link to subscribers. */
    private TypedIORelation _relation;
}
