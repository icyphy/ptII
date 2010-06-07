/* A publisher that transparently tunnels messages to subscribers.

 Copyright (c) 2006-2010 The Regents of the University of California.
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
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
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
 new Publisher, by default, it has no channel name. You have to
 specify a channel name to use it.
 <p>
 <b>How it works:</b>
 This actor has a hidden output port. When the channel name
 is specified, typically during model construction, this actor
 causes a relation to be created in the least opaque composite
 actor above it in the hierarchy and links to that relation.
 In addition, if <i>export</i> is set to non-zero, it causes
 a port to be created in that composite, and also links that
 port to the relation.  The relation is recorded by the opaque
 composite.  When a Subscriber is preinitialized that refers
 to the same channel, that Subscriber finds the relation (by
 finding the least opaque composite actor above it) and links
 to the relation. Some of these links are "liberal links" in that
 they cross levels of the hierarchy.
 <p>
 Since publishers are linked to subscribers,
 any data dependencies that the director might assume on a regular
 "wired" connection will also be assumed across Publisher-Subscriber
 pairs. Similarly, type constraints will propagate across
 Publisher-Subscriber pairs. That is, the type of the Subscriber
 output will match the type of the Publisher input.

 @author Edward A. Lee, Raymond A. Cardillo, Bert Rodiers
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

        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);
        output = new TypedIOPort(this, "output", false, true);
        output.setMultiport(true);

        // We only have constraints from the publisher on the subscriber
        // and the output of the subscriber and not the other way around
        // to not break any existing models.
        output.setWidthEquals(input, false);

        Parameter hide = new SingletonParameter(output, "_hide");
        hide.setToken(BooleanToken.TRUE);
        // hide = new SingletonParameter(this, "_hideName");
        // hide.setToken(BooleanToken.TRUE);

        numberExportLevels = new Parameter(this, "numberExportLevels");
        numberExportLevels.setExpression("0");
        numberExportLevels.setTypeEquals(BaseType.INT);

    }

    ///////////////////////////////////////////////////////////////////
    ////                   ports and parameters                    ////

    /** The name of the channel.  Subscribers that reference this same
     *  channel will receive any transmissions to this port.
     *  This is a string that defaults to empty, indicating that
     *  no channel is specified. A channel must be set before
     *  the actor executes or an exception will occur.
     */
    public StringParameter channel;

    /** The number of export levels of publish ports. A port in this publisher 
     * will be exported to upper levels in hierarchy for other subscribers. This
     * parameter specifies the number of levels the port will be exported.
     */

    public Parameter numberExportLevels;

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
        if (attribute == channel || attribute == numberExportLevels) {
            // We only get the value if we are not in a class definition.
            // The reason is that some of the Actor Oriented Classes
            // that use Publishers do not have the parameter defined
            // in the definition.  See
            // ptolemy/actor/lib/test/auto/PublisherClassNoParameter.xml
            if (!isWithinClassDefinition()) {
                String newValue = channel.stringValue();
                int newNumberExportLevels = ((IntToken) numberExportLevels
                        .getToken()).intValue();
                if (!newValue.equals(_channel)
                        || newNumberExportLevels != _numberExportLevels) {
                    NamedObj container = getContainer();
                    if (container instanceof CompositeActor) {
                        try {
                            
                            if (attribute == numberExportLevels) {
                                if (newNumberExportLevels != CompositeActor.GLOBAL
                                        && (_numberExportLevels == CompositeActor.GLOBAL || newNumberExportLevels < _numberExportLevels)) {
                                    ((CompositeActor) container)
                                            .unregisterPublisherPort(_channel,
                                                    output,
                                                    newNumberExportLevels,
                                                    _numberExportLevels);
                                }

                            }
                            
                            ((CompositeActor) container).registerPublisherPort(
                                    newValue, output, newNumberExportLevels);
                            
                            if (attribute == channel
                                    && (!(_channel == null || _channel.trim()
                                            .equals("")))) {
                                ((CompositeActor) container)
                                        .unregisterPublisherPort(_channel,
                                                output, 0, _numberExportLevels);
                            }

                        } catch (NameDuplicationException e) {
                            throw new IllegalActionException(this, e,
                                    "Can't add published port.");
                        }
                    }
                    _channel = newValue;
                    _numberExportLevels = newNumberExportLevels;
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
            newObject._channel = _channel;
            newObject._numberExportLevels = _numberExportLevels;
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
     *   publishing on the same channel, or if the channel name has not
     *   been specified.
     */
    public void preinitialize() throws IllegalActionException {
        if (_channel == null || _channel.trim().equals("")) {
            throw new IllegalActionException(this,
                    "No channel name has been specified.");
        }

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
                ((CompositeActor) previousContainer).unregisterPublisherPort(
                        _channel, output);
            }
        }

        super.setContainer(container);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected method                    ////

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Cached channel name. */
    protected String _channel;

    /** Cached number export levels */
    protected int _numberExportLevels;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

}
