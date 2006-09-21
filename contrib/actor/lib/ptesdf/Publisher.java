/* A publisher that transparently tunnels messages to subscribers.

 Copyright (c) 2003-2006 The Regents of the University of California.
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
package contrib.actor.lib.ptesdf;

import java.util.Iterator;

import ptolemy.actor.Director;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;

//////////////////////////////////////////////////////////////////////////
//// Publisher

/**
 This actor publishes input tokens on a named channel. The tokens are
 "tunneled" to any instance of Subscriber that names the same channel
 and that is under the control of the same director. That is, it can
 be at a different level of the hierarchy, or in an entirely different
 composite actor, as long as the relevant composite actors are
 transparent (have no director).
 <p>
 It is an error to have two instances of Publisher using the same
 channel under the control of the same director. When you create a
 new Publisher, by default, it assigns a channel name that is unique.
 You can re-use channel names withing opaque composite actors.
 <p>
 This actor actually has a hidden output port that is connected
 to all subcribers via hidden "liberal links" (links that are
 allowed to cross levels of the hierarchy).  Consequently,
 any data dependencies that the director might assume on a regular
 "wired" connection will also be assumed across Publisher-Subscriber
 pairs. Similarly, type constraints will probagate across
 Publisher-Subscriber pairs. That is, the type of the Subscriber
 output will match the type of the Publisher input.
 
 @author Edward A. Lee, Raymond A. Cardillo
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Green (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class Publisher extends TypedAtomicActor {

    /** Construct a publisher with the specified container and name.
     *  @param container The container actor.
     *  @param name The name of the actor.
     *  @exception IllegalActionException If the actor is not of an acceptable
     *   class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public Publisher(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        // Set this up as input port.
        super(container, name);

        channel = new StringParameter(this, "channel");
        channel.setToken(_uniqueChannelName());

        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);

        output = new TypedIOPort(this, "output", false, true);
        output.setMultiport(true);

        Parameter hide = new SingletonParameter(output, "_hide");
        hide.setToken(BooleanToken.TRUE);
        hide.setVisibility(Settable.NOT_EDITABLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                   ports and parameters                    ////

    /** The name of the channel.  Subscribers that reference this same
     *  channel will receive any transmissions to this port.
     *  This is a string that defaults to "channel.X", where X is an
     *  integer that ensures that this channel name does not collide
     *  with a channel name already in use by another publisher.
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

    /** If the attribute is the channel, cache the string value.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == channel) {
            _channel = channel.stringValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Prepare the Publisher for use, and update and internal links lazily.
     *  @exception IllegalActionException If there is an error linking
     *  this publisher to subscribers, or if there are any duplicate publishers.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();

        _updateLinks();
    }

    /** Read at most one input token from each input channel
     *  and send it to any of the internally linked subscribers.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();

        for (int i = 0; i < input.getWidth(); i++) {
            if (input.hasToken(i)) {
                Token token = input.get(i);
                output.send(i, token);
            }
        }
    }

    /** Remove the relation after every run so we can re-wire it on the next execution.
     *
     *  @exception IllegalActionException If there was an error while trying to remove relation.
     */
    public void wrapup() throws IllegalActionException {
        _removeRelation();
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected variables                 ////

    /** The relation used to link published output to subscribers. */
    protected TypedIORelation _relation;

    /** Cached channel name string. */
    protected String _channel;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return a channel name of the form "channel.X", where X is an integer
     *  that ensures that this channel name is not already in use.
     *  @return A unique channel name.
     */
    private String _uniqueChannelName() {
        // The original technique does not work when the container is not yet
        // opaque (e.g., director has not been selected yet, or class definition).
        // This new technique is much simpler and works in all cases.
        return "channel." + System.currentTimeMillis();
    }

    /** Link the subscribers to this publisher.
     *  @exception IllegalActionException If there is an error linking
     *  this publisher to subscribers, or if there are any duplicate publishers.
     */
    private void _linkSubscribers() throws IllegalActionException {
        // Find the nearest opaque container above in the hierarchy.
        CompositeEntity container = (CompositeEntity) getContainer();
        while (container != null && !container.isOpaque()) {
            container = (CompositeEntity) container.getContainer();
        }

        // link matching Subscribers, and validate unique Publishers        
        if (container != null) {
            Iterator actors = container.deepEntityList().iterator();
            while (actors.hasNext()) {
                Object actor = actors.next();
                if (actor instanceof Subscriber) {
                    Subscriber subscriber = (Subscriber) actor;
                    if (subscriber.channelMatches(_channel)) {
                        subscriber.input.liberalLink(_relation);
                    }
                } else if ((actor instanceof Publisher) && (actor != this)) {
                    Publisher publisher = (Publisher) actor;
                    if (publisher._channel.equals(_channel)) {
                        throw new IllegalActionException(this,
                                "There is already a publisher using channel "
                                        + "\"" + _channel + "\" : "
                                        + publisher.getFullName());
                    }
                }
            }
        }
    }

    private void _removeRelation() throws IllegalActionException {
        // Remove the previous relation, if necessary.
        if (_relation != null) {
            try {
                _relation.setContainer(null);
            } catch (NameDuplicationException e) {
                throw new InternalErrorException(e);
            }
            _relation = null;
        }
    }

    /** Update connections to subscribers.
     *  @exception IllegalActionException If there is an error linking
     *  this publisher to subscribers, or if there are any duplicate publishers.
     */
    private void _updateLinks() throws IllegalActionException {
        // If the channel has not been set, then there is nothing to do.
        if (_channel == null) {
            return;
        }

        _removeRelation();

        // If the container is not a typed composite actor, then don't create
        // a relation. The container is probably a library.
        NamedObj container = getContainer();
        if (container instanceof TypedCompositeActor) {
            try {
                _relation = new TypedIORelation(
                        (TypedCompositeActor) container, container
                                .uniqueName("publisherRelation"));

                // Prevent the relation and its links from being exported.
                _relation.setPersistent(false);

                // Prevent the relation from showing up in vergil.
                new Parameter(_relation, "_hide", BooleanToken.TRUE);

                // Set the width of the relation to match the
                // width of the input.
                _relation.setWidth(input.getWidth());
            } catch (NameDuplicationException e) {
                throw new InternalErrorException(e);
            }
        }

        if (_relation != null) {
            output.link(_relation);

            // Link to the subscribers.         
            _linkSubscribers();
        }

        Director director = getDirector();
        if (director != null) {
            director.invalidateSchedule();
            director.invalidateResolvedTypes();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private variables                   ////
}
