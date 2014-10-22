/* A subscriber that aggregates messages from multiple publishers.

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

import ptolemy.actor.SubscriptionAggregatorPort;
import ptolemy.data.Token;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

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
 For example, <code>channel.foo.*</code> does not mean the same thing as
 <code>channel\.foo.+</code>. The latter requires a dot between channel and
 foo, where the former does not.

 @author Edward A. Lee, Raymond A. Cardillo, contributor: Christopher Brooks, Bert Rodiers
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Green (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class SubscriptionAggregator extends Subscriber {

    // NOTE: This cannot extend Subscriber because it needs
    // a different kind of input port.

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

        operation = new StringParameter(this, "operation");
        operation.addChoice("add");
        operation.addChoice("multiply");
        operation.setExpression("add");

        // Set the operation attribute of the input port to inherit
        // the value of the operation parameter of this actor.
        ((SubscriptionAggregatorPort) input).operation
                .setExpression("$operation");
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

    /** Read at most one input token from each input
     *  channel, aggregate them, and send the result to the output.
     *  @exception IllegalActionException If there is no director, or
     *   if there is no input connection.
     */
    @Override
    public void fire() throws IllegalActionException {
        // Do not call super.fire() here because that does the wrong thing.
        if (_debugging) {
            _debug("Called fire()");
        }
        int width = input.getWidth();
        if (width == 0) {
            throw new IllegalActionException(this,
                    "SubscriptionAggregator could not find a matching Publisher "
                            + "with channel \"" + channel.stringValue() + "\"");

        }
        if (input.hasToken(0)) {
            // This will do the aggregation.
            Token token = input.get(0);
            output.send(0, token);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create an input port. This overrides the base class to create
     *  a SubscriptionAggregatorPort.
     *  @exception IllegalActionException If creating the input port fails.
     *  @exception NameDuplicationException If there is already a port named "input".
     */
    @Override
    protected void _createInputPort() throws IllegalActionException,
            NameDuplicationException {
        input = new SubscriptionAggregatorPort(this, "input");
    }
}
