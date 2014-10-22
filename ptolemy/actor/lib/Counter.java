/* An up-down counter.

 Copyright (c) 1998-2014 The Regents of the University of California.
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

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

///////////////////////////////////////////////////////////////////
//// Counter

/**
 This actor implements an up-down counter of received tokens.  Whenever
 a token is received from the <i>increment</i> input, the internal
 counter is incremented.  Whenever a token is received from the
 <i>decrement</i> port, the internal counter is decremented.  Whenever
 a token is received from either input port, a token is created on the
 output port with the integer value of the current count.  At most one
 token will be consumed from each input during each firing.  If a token
 is present on both input ports during any firing, then the increment
 and the decrement will cancel out, and only one output token will be
 produced. If any firing a <i>reset</i> input is present and true,
 then the count will be reset.

 @author Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Yellow (neuendor)
 @Pt.AcceptedRating Yellow (neuendor)
 */
public class Counter extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Counter(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        increment = new TypedIOPort(this, "increment", true, false);
        increment.setTypeEquals(BaseType.GENERAL);
        new Parameter(increment, "_showName", BooleanToken.TRUE);
        decrement = new TypedIOPort(this, "decrement", true, false);
        decrement.setTypeEquals(BaseType.GENERAL);
        new Parameter(decrement, "_showName", BooleanToken.TRUE);
        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.INT);
        reset = new TypedIOPort(this, "reset", true, false);
        reset.setTypeEquals(BaseType.BOOLEAN);
        new SingletonParameter(reset, "_showName").setToken(BooleanToken.TRUE);
        StringAttribute cardinal = new StringAttribute(reset, "_cardinal");
        cardinal.setExpression("SOUTH");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The increment port. If this input port
     *  receives a token, then the counter is incremented.  The port
     *  has type general.
     */
    public TypedIOPort increment;

    /** The decrement port. If this input port
     *  receives a token, then the counter is decremented.  The port
     *  has type general.
     */
    public TypedIOPort decrement;

    /** The output port with type IntToken.
     */
    public TypedIOPort output;

    /** The reset input port. This is of type boolean. */
    public TypedIOPort reset;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Consume at most one token from each input and update the
     *  counter appropriately. Send the current value of the counter
     *  to the output.  If there are no input tokens available, no
     *  output will be produced.  If a token is consumed from only the
     *  <i>increment</i> port the output value will be one more than
     *  the previous output value.  If a token consumed from only the
     *  <i>decrement</i> port the output value will be one less than
     *  the previous output value.  If a token is consumed from both
     *  input ports, then the output value will be the same as the
     *  previous output value.  If the fire method is invoked multiple
     *  times in one iteration, then only the input read on the last
     *  invocation in the iteration will affect future outputs of the
     *  counter.
     *
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        _latestCount = _count;
        boolean consumed = false;

        // Check the increment port.
        for (int i = 0; i < increment.getWidth(); i++) {
            if (increment.hasToken(i)) {
                increment.get(i);
                _latestCount++;
                consumed = true;
            }
        }

        // Check the decrement port.
        for (int i = 0; i < decrement.getWidth(); i++) {
            if (decrement.hasToken(i)) {
                decrement.get(i);
                _latestCount--;
                consumed = true;
            }
        }

        if (reset.getWidth() > 0) {
            if (reset.hasToken(0)) {
                if (((BooleanToken) reset.get(0)).booleanValue()) {
                    _latestCount = 0;
                    consumed = true;
                }
            }
        }

        // Produce an output if we consumed an input or got a reset.
        if (consumed) {
            Token out = new IntToken(_latestCount);
            output.send(0, out);
        }
    }

    /** Reset the count of inputs to zero.
     *  @exception IllegalActionException If the parent class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _count = 0;
    }

    /** Record the most recent output count as the actual count.
     *  @exception IllegalActionException If the base class throws it.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        _count = _latestCount;
        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    private int _count = 0;

    private int _latestCount = 0;
}
