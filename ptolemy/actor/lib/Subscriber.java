/* A subscriber that transparently receives tunneled messages from publishers.

 Copyright (c) 2006-2011 The Regents of the University of California.
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

import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
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
 pairs.  Similarly, type constraints will propagate across
 Publisher-Subscriber pairs. That is, the type of the Subscriber
 output will match the type of the Publisher input.

 @author Edward A. Lee, Raymond A. Cardillo, Bert Rodiers
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

        // We only have constraints from the publisher on the subscriber
        // and the output of the subscriber and not the other way around
        // to not break any existing models.
        output.setWidthEquals(input, false);

        new Parameter(input, "_hide", BooleanToken.TRUE);

        global = new Parameter(this, "global");
        global.setExpression("false");
        global.setTypeEquals(BaseType.BOOLEAN);
    }

    ///////////////////////////////////////////////////////////////////
    ////                   ports and parameters                    ////

    /** The name of the channel.  Subscribers that reference this same
     *  channel will receive any transmissions to this port.
     *  This is a string that defaults to "channel1".
     */
    public StringParameter channel;

    /** Specification of whether the data is subscribed globally.
     *  If this is set to true, then this subscriber will see values
     *  published by a publisher anywhere in the model references the same
     *  channel by name. If this is set to false (the default), then only
     *  values published by the publisher that are fired by the same
     *  director are seen by this subscriber.
     */
    public Parameter global;

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
                NamedObj container = getContainer();
                if (container instanceof CompositeActor
                        && !(_channel == null || _channel.trim().equals(""))) {
                    ((CompositeActor) container).unlinkToPublishedPort(
                            _channel, input, _global);
                }
                _channel = newValue;
                /* NOTE: We used to call _updateLinks(), as shown below,
                 * but now we defer that to preinitialize().  This
                 * improves the opening time of models considerably.
                // If we are within a class definition, then we should
                // not create any links.  The links should only exist
                // within instances. Otherwise, we could end up creating
                // a link between a class definition and an instance.
                if (!isWithinClassDefinition()) {
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
                        _updateLinks();
                    }
                }
                */
            }
        } else if (attribute == global) {
            _global = ((BooleanToken) global.getToken()).booleanValue();
            if (!_global && getContainer() instanceof CompositeActor) {
                // The vergil and config tests were failing because
                // moml.EntityLibrary sometimes contains Subscribers.
                ((CompositeActor) getContainer()).unlinkToPublishedPort(
                        _channel, input, false);
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
        Subscriber newObject = (Subscriber) super.clone(workspace);
        try {
            newObject._channel = _channel;
        } catch (Throwable throwable) {
            CloneNotSupportedException exception = new CloneNotSupportedException();
            exception.initCause(throwable);
            throw exception;
        }

        // We only have constraints from the publisher on the subscriber
        // and the output of the subscriber and not the other way around
        // to not break any existing models.
        newObject.output.setWidthEquals(newObject.input, false);

        return newObject;
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
            channel.validate();
            throw new IllegalActionException(this,
                    "Subscriber could not find a matching Publisher "
                            + "with channel \"" + channel.stringValue() + "\"");

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
        // We have two cases:
        // 1) _updateLinks is false.
        // If this was created by instantiating a container class,
        // then the links would not have been updated when setContainer()
        // was called, so we must do it now.
        //
        // 2) input.getWidth() is 0

        // If we are converting back and forth from class to instance,
        // then there is a chance that we have not called
        // _updateLinks() recently.  See Publisher-1.6 in
        // test/Publisher.tcl. A better fix might be toclear
        // _updateLinks when the relation to the input port was deleted by
        // CompositeEntity.setClassDefinition().    This would probably
        // require creating a custom port class that would notice the deletion
        // Another idea is to
        // compare the workspace version number against the version
        // number when we set _updatedLinks.  However, this could
        // result in poor performance.

        if (_channel == null) {
            throw new IllegalActionException(this, "The channel name was null?");
        }
        _updateLinks();

        // Call super.preinitialize() after updating links so that
        // we have connections made before possibly inferring widths.
        super.preinitialize();
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

        if (container == null
                && !(_channel == null || _channel.trim().equals(""))) {
            NamedObj previousContainer = getContainer();
            if (previousContainer instanceof CompositeActor) {
                ((CompositeActor) previousContainer).unlinkToPublishedPort(
                        _channel, input);
            }
        }

        super.setContainer(container);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Update the connection to the publisher, if there is one.
     *  Note that this method is computationally intensive for large
     *  models as it traverses the model by searching
     *  up the hierarchy for the nearest opaque container
     *  or the top level and then traverses the contained entities.
     *  Thus, avoid calling this method except when the model
     *  is running.
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
        NamedObj container = getContainer();
        if (container instanceof CompositeActor) {
            try {
                ((CompositeActor) container).linkToPublishedPort(_channel,
                        input, _global);
            } catch (Exception e) {
                throw new IllegalActionException(this, e,
                        "Can't link Subscriber with Publisher, channel was \""
                                + channel.stringValue() + "\"");
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Cached channel name. */
    protected String _channel;

    /** Cached global parameter. */
    protected boolean _global;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
}
