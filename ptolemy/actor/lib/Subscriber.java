/* A subscriber that transparently receives tunneled messages from publishers.

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
package ptolemy.actor.lib;

import java.util.Set;

import ptolemy.actor.AtomicActor;
import ptolemy.actor.SubscriberPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.ActorDependencies;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// Subscriber

/**
 This actor subscribes to tokens on a named channel. The tokens are
 "tunneled" from an instance of Publisher that names the same channel.
 If {@link #global} is false (the default), then this subscriber
 will only see instances of Publisher that are under the
 control of the same director. That is, it can
 be at a different level of the hierarchy, or in an entirely different
 composite actor, as long as the relevant composite actors are
 transparent (have no director). If {@link #global} is true,
 then the publisher may be anywhere in the model, as long as its
 <i>global</i> parameter is also true.
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

        _createInputPort();
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

        // Refer the parameters of the input port to those of
        // this actor.
        input.channel.setExpression("$channel");
        input.global.setExpression("global");

        output.setTypeAtLeast(input);
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

    /** The input port.  This port is hidden and should not be
     *  directly used. This base class imposes no type constraints except
     *  that the type of the input cannot be greater than the type of the
     *  output.
     */
    public SubscriberPort input;

    /** The output port. This is a multiport. If the corresponding
     *  publisher has multiple input signals, then those multiple signals
     *  will appear on this output port.
     *  By default, the type of this output is constrained
     *  to be at least that of the input. This port is hidden by default
     *  and the actor handles creating connections to it.
     */
    public TypedIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Subscriber newObject = (Subscriber) super.clone(workspace);

        // We only have constraints from the publisher on the subscriber
        // and the output of the subscriber and not the other way around
        // to not break any existing models.
        newObject.output.setWidthEquals(newObject.input, false);

        newObject.output.setTypeAtLeast(newObject.input);

        return newObject;
    }

    /** Read at most one input token from each input
     *  channel and send it to the output.
     *  @exception IllegalActionException If there is no director, or
     *   if there is no input connection.
     */
    @Override
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

    /** Return a Set of Publishers that are connected to this Subscriber.
     *  @return A Set of Publishers that are connected to this Subscriber.
     *  @exception KernelException If thrown when a Manager is added to
     *  the top level or if preinitialize() fails.
     */
    public Set<AtomicActor> publishers() throws KernelException {
        return ActorDependencies.prerequisites(this, Publisher.class);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create an input port. This is a protected method so that
     *  subclasses can create different input ports. This is called
     *  in the constructor, so subclasses cannot reliably access
     *  local variables.
     *  @exception IllegalActionException If creating the input port fails.
     *  @exception NameDuplicationException If there is already a port named "input".
     */
    protected void _createInputPort() throws IllegalActionException,
    NameDuplicationException {
        input = new SubscriberPort(this, "input");
    }
}
