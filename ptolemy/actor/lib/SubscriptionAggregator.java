/* A subscriber that aggregates messages from multiple publishers.

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

import java.util.regex.Pattern;

import java.util.Iterator;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// SubscriptionAggregator

/**
 Aggregate data produced by multiple publishers.
 This is a generalization of the Subscriber (the base class)
 where the channel name is interpreted as a regular expression.
 Data produced by all publishers that publish on a channel name
 that matches the regular expression are aggregated using the
 operation given by the <i>operation</i> parameter.

 <p>Note that the {@link ptolemy.actor.lib.Subscriber#channel <i>channel</i>}
 parameter of the superclass is now a regular expression in this class.
 Thus, this class is usually slower than the superclass.  One thing
 to watch out for is using <code>.</code> instead of <code>\.</code>
 and <code>*</code> instead of <code>.+</code>.
 For example, <code>channel.foo.*</code> might be faster as
 <code>channel\.foo.+</code>.


 @author Edward A. Lee, Raymond A. Cardillo, contributor: Christopher Brooks, Bert Rodiers
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Green (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class SubscriptionAggregator extends Subscriber {

    /** Construct a subscriber with the specified container and name.
     *  @param container The container actor.
     *  @param name The name of the actor.
     *  @exception IllegalActionException If the actor is not of an acceptable
     *   class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public SubscriptionAggregator(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        output.setMultiport(false);

        operation = new StringParameter(this, "operation");
        operation.addChoice("add");
        operation.addChoice("multiply");
        operation.setExpression("add");
    }

    ///////////////////////////////////////////////////////////////////
    ////                   ports and parameters                    ////

    /** The operation used to aggregate the data produced by
     *  matching publishers. The choices are "add" and "multiply".
     *  Note that "multiply" is a poor choice if the data type
     *  has a non-commutative multiplication operation (e.g.
     *  matrix types) because the result will be nondeterministic.
     *  This is a string that defaults to "add".
     */
    public StringParameter operation;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to record the operation choice.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == operation) {
            String newValue = operation.stringValue();
            if (newValue.equals("add")) {
                _addOperation = true;
            } else {
                _addOperation = false;
            }
        } else if (attribute == channel) {
            String newValue = channel.stringValue();
            if (!newValue.equals(_channel)) {
                NamedObj container = getContainer();
                if (container instanceof CompositeActor
                        && !(_channel == null || _channel.trim().equals(""))) {
                    ((CompositeActor) container).unlinkToPublishedPort(
                            _channelPattern, input, _global);
                }
                _channel = newValue;
                // Don't call super here because super.attributeChanged() tries to unlink _channel
                // as a non-regular expression string, which seems wrong.
                super.attributeChanged(attribute);
                _channelPattern = Pattern.compile(_channel);
            }
        } else if (attribute == global) {
            boolean newValue = ((BooleanToken) global.getToken()).booleanValue();
            if(newValue == false && _global == true) {
                NamedObj container = getContainer();
                if (container instanceof CompositeActor
                        && !(_channel == null || _channel.trim().equals(""))) {
                    ((CompositeActor) container).unlinkToPublishedPort(
                            _channelPattern, input, _global);
                }
            }
            _global = newValue;
            // Do not call SubscriptionAggregator.attributeChanged()
            // because it will remove the published port name by _channel.
            // If _channel is set to a real name (not a regex pattern),
            // Then chaos ensues.  See test 3.0 in SubscriptionAggregator.tcl
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        SubscriptionAggregator newObject = (SubscriptionAggregator) super
                .clone(workspace);
        newObject._channel = _channel;
        return newObject;
    }

    /** Read at most one input token from each input
     *  channel, add all the tokens, and send the result
     *  to the output.
     *  @exception IllegalActionException If there is no director, or
     *   if there is no input connection.
     */
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("Called fire()");
        }

        int width = input.getWidth();
        if (width == 0) {
            throw new IllegalActionException(this,
                    "SubscriptionAggregator has no matching Publisher, "
                            + "channel was \"" + channel.getExpression()
                            + "\".");
        }
        Token result = null;
        for (int i = 0; i < width; i++) {
            if (input.hasToken(i)) {
                Token token = input.get(i);
                if (result == null) {
                    result = token;
                } else {
                    if (_addOperation) {
                        result = result.add(token);
                    } else {
                        result = result.multiply(token);
                    }
                }
            }
        }
        output.send(0, result);
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
                        _channelPattern, input);
            }
        }

        super.setContainer(container);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Update the connection to the publishers, if there are any.
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
                try {
                    ((CompositeActor) container).linkToPublishedPort(
                            _channelPattern, input, _global);
                } catch (IllegalActionException ex) {
                    // If we have a LazyTypedCompositeActor that
                    // contains the Publisher, then populate() the
                    // model, expanding the LazyTypedCompositeActors
                    // and retry the link.  This is computationally
                    // expensive.
                    // See $PTII/ptolemy/actor/lib/test/auto/LazyPubSub.xml
                    Iterator namedObjs = ((TypedCompositeActor)toplevel()).allAtomicEntityList().iterator();
                    while (namedObjs.hasNext()) {
                        NamedObj namedObj = (NamedObj)namedObjs.next();
                        if (namedObj instanceof Publisher) {
                            Publisher publisher = (Publisher)namedObj;
                            publisher.attributeChanged(publisher.channel);
                        }
                    }
                    
                    ((CompositeActor) container).linkToPublishedPort(
                            _channelPattern, input, _global);
                }
            } catch (NameDuplicationException e) {
                throw new IllegalActionException(this, e,
                "Can't link SubscriptionAggregator with Publisher.");
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Indicator that the operation is "add" rather than "multiply". */
    private boolean _addOperation = true;

    /** Regex Pattern for _channelName. */
    private Pattern _channelPattern;

}
