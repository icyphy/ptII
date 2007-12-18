/* A publisher that transparently tunnels messages to subscribers.

 Copyright (c) 2006-2007 The Regents of the University of California.
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
import java.util.LinkedList;
import java.util.List;

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
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

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
 You can re-use channel names within opaque composite actors.
 <p>
 This actor actually has a hidden output port that is connected
 to all subcribers via hidden "liberal links" (links that are
 allowed to cross levels of the hierarchy).  Consequently,
 any data dependencies that the director might assume on a regular
 "wired" connection will also be assumed across Publisher-Subscriber
 pairs. Similarly, type constraints will propagate across
 Publisher-Subscriber pairs. That is, the type of the Subscriber
 output will match the type of the Publisher input.

 @author Edward A. Lee, Raymond A. Cardillo
 @version $Id$
 @since Ptolemy II 5.2
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
        super(container, name);

        channel = new StringParameter(this, "channel");
        channel.setExpression(_uniqueChannelName());

        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);
        output = new TypedIOPort(this, "output", false, true);
        output.setMultiport(true);

        Parameter hide = new SingletonParameter(output, "_hide");
        hide.setToken(BooleanToken.TRUE);
        // hide = new SingletonParameter(this, "_hideName");
        // hide.setToken(BooleanToken.TRUE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                   ports and parameters                    ////

    /** The name of the channel.  Subscribers that reference this same
     *  channel will receive any transmissions to this port.
     *  This is a string that defaults to "channelX", where X is an
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
            // We only get the value if we are not in a class definition.
            // The reason is that some of the Actor Oriented Classes
            // that use Publishers do not have the parameter defined
            // in the definition.  See
            // ptolemy/actor/lib/test/auto/PublisherClassNoParameter.xml
            if (!isWithinClassDefinition()) {
                String newValue = channel.stringValue();
                if (!newValue.equals(_channel)) {
                    _channel = newValue;
                    // If we are within a class definition, then we should
                    // not create any links.  The links should only exist
                    // within instances. Otherwise, we could end up creating
                    // a link between a class definition and an instance.
                    Nameable container = getContainer();
                    if ((container instanceof TypedCompositeActor)) {
                        // NOTE: There used to be some logic here to call
                        // _updateLinks() only if the manager indicates we
                        // are running, and otherwise to set _updatedLinks
                        // to false. This is no longer necessary as
                        // the attributeChanged() method is called at
                        // appropriate times, and tests show it is called
                        // exactly once per publisher. Moreover, it really
                        // isn't correct to defer this, as _updateLinks()
                        // handles connectivity information. If we want to,
                        // for example, highlight dependents before a model
                        // has been run, this has to be done here.
                        // However, it could fail due to a transient error.
                        // For example, suppose we create two publishers
                        // that have the same channel name while editing a
                        // model, e.g., by instantiating a class containing
                        // a publisher twice. But during editing, we plan
                        // to change the channel name before running the
                        // model.  Thus, we should tolerate errors here
                        // and continue until the error is caught in
                        // preinitialize().
                        try {
                            _updateLinks();
                        } catch (IllegalActionException ex) {
                            _updatedLinks = false;
                            if (_running) {
                                throw ex;
                            }
                        }
                    }
                }
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then set the filename public member.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Publisher newObject = (Publisher) super.clone(workspace);
        try {
            newObject._updatedLinks = false;
            //newObject._updateLinks();
        } catch (Throwable throwable) {
            CloneNotSupportedException exception = new CloneNotSupportedException();
            exception.initCause(throwable);
            throw exception;
        }
        return newObject;
    }

    /** Override the base class to update the width of the hidden link.
     *  @param port The port that has connection changes.
     */
    public void connectionsChanged(Port port) {
        super.connectionsChanged(port);
        if (port == input) {
            if (_relation != null && !_inConnectionsChanged) {
                try {
                    _inConnectionsChanged = true;
                    int width = input.getWidth();
                    _relation.setWidth(width);
                } catch (IllegalActionException e) {
                    throw new InternalErrorException(e);
                } finally {
                    _inConnectionsChanged = false;
                }
            }
        }
    }

    /** Read at most one input token from each
     *  input channel and send it to the subscribers,
     *  if any.
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

    /** Override the base class to ensure that links to subscribers
     *  have been updated.
     *  @exception IllegalActionException If there is already a publisher
     *   publishing on the same channel.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        _running = true;
        // If this was created by instantiating a container class,
        // then the links would not have been updated when setContainer()
        // was called, so we must do it now.
        if (!_updatedLinks) {
            _updateLinks();
        }
    }

    /** If the new container is null, delete the named channel.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the action would result in a
     *   recursive containment structure, or if
     *   this entity and container are not in the same workspace.
     *  @exception NameDuplicationException If the container already has
     *   an entity with the name of this entity.
     */
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {

        if (container == null && _relation != null) {
            try {
                _relation.setContainer(null);
            } catch (NameDuplicationException e) {
                throw new InternalErrorException(e);
            }
            _relation = null;
        } 

        super.setContainer(container);
    }

    /** Override the base class to ensure to record that the actor
     *  is no longer running.
     *  @exception IllegalActionException If there is already a publisher
     *   publishing on the same channel.
     */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        _running = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected method                    ////

    /** Update connections to subscribers.
     *  @exception IllegalActionException If there is already a publisher
     *   publishing on the same channel.
     */
    protected void _updateLinks() throws IllegalActionException {
        // If the channel has not been set, then there is nothing
        // to do.  This is probably the first setContainer() call,
        // before the object is fully constructed.
        if (_channel == null) {
            return;
        }

        // Do this before making any changes to the model in case
        // it throws an exception.
        Iterator subscribers = _findSubscribers().iterator();

        // Remove the previous relation, if necessary.
        if (_relation != null) {
            try {
                _relation.setContainer(null);
            } catch (NameDuplicationException e) {
                throw new InternalErrorException(e);
            }
            _relation = null;
        }

        NamedObj container = getContainer();
        if (container instanceof TypedCompositeActor) {
            // If the container is not a typed composite actor, then don't create
            // a relation. Probably the container is a library.
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
            output.link(_relation);

            // Link to the subscribers.
            while (subscribers.hasNext()) {
                Subscriber subscriber = (Subscriber) subscribers.next();
                subscriber.input.liberalLink(_relation);
            }
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

    /** The relation used to link to subscribers. */
    protected TypedIORelation _relation;

    /** An indicator that _updateLinks has been called at least once. */
    protected boolean _updatedLinks = false;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Find subscribers.
     *  @return A list of subscribers.
     *  @exception IllegalActionException If there is already a publisher
     *   using the same channel.
     */
    private List _findSubscribers() throws IllegalActionException {
        LinkedList result = new LinkedList();
        // Find the nearest opaque container above in the hierarchy.
        CompositeEntity container = (CompositeEntity) getContainer();
        while (container != null && !container.isOpaque()) {
            container = (CompositeEntity) container.getContainer();
        }
        if (container != null) {
            Iterator actors = container.deepEntityList().iterator();
            while (actors.hasNext()) {
                Object actor = actors.next();
                if (actor instanceof Subscriber) {
                    if (((Subscriber) actor).channelMatches(_channel)) {
                        result.add(actor);
                    }
                } else if (actor instanceof Publisher && actor != this) {
                    // Throw an exception if there is another publisher
                    // trying to publish on the same channel.
                    if (_channel.equals(((Publisher) actor)._channel)) {
                        throw new IllegalActionException(this,
                                "There is already a publisher using channel \""
                                        + _channel + "\": "
                                        + ((NamedObj) actor).getFullName());
                    }
                }
            }
        }
        return result;
    }

    /** Return a channel name of the form "channelX", where X is an integer
     *  that ensures that this channel name is not already in use.
     *  @return A unique channel name.
     */
    private String _uniqueChannelName() {
        int suffix = 1;
        // Find the nearest opaque container above in the hierarchy.
        CompositeEntity container = (CompositeEntity) getContainer();
        while (container != null && !container.isOpaque()) {
            container = (CompositeEntity) container.getContainer();
        }
        if (container != null) {
            Iterator actors = container.deepEntityList().iterator();
            while (actors.hasNext()) {
                Object actor = actors.next();
                if (actor instanceof Publisher && actor != this) {
                    String nameInUse = ((Publisher) actor)._channel;
                    if (nameInUse != null && nameInUse.startsWith("channel")) {
                        String suffixInUse = nameInUse.substring(7);
                        try {
                            int suffixAsInt = Integer.parseInt(suffixInUse);
                            if (suffix <= suffixAsInt) {
                                suffix = suffixAsInt + 1;
                            }
                        } catch (NumberFormatException ex) {
                            // Not a number suffix, so it can't collide.
                            continue;
                        }
                    }
                }
            }
        }
        return "channel" + suffix;
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private variables                   ////

    /** An indicator that connectionsChanged() has been called. */
    private boolean _inConnectionsChanged = false;

    /** Indicator that preinitialize has been called and not wrapup. */
    private boolean _running;
}
