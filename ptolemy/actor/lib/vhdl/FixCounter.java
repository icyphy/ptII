/** A counter that counts in fixpoint values.

 Copyright (c) 2004-2014 The Regents of the University of California.
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
package ptolemy.actor.lib.vhdl;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.FixToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.math.FixPoint;

///////////////////////////////////////////////////////////////////
//// Fix Counter

/**
 A class for a fixpoint value counter.
 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 4.1
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class FixCounter extends SynchronousFixTransformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public FixCounter(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        increment = new TypedIOPort(this, "increment", true, false);
        increment.setTypeEquals(BaseType.FIX);
        decrement = new TypedIOPort(this, "decrement", true, false);
        decrement.setTypeEquals(BaseType.FIX);
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
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (increment.isKnown() && decrement.isKnown()) {
            _latestCount = _count;
            _consumed = false;

            // Check the increment port.
            for (int i = 0; i < increment.getWidth(); i++) {
                if (increment.hasToken(i)) {
                    increment.get(i);
                    _latestCount = _latestCount + 1;
                    _consumed = true;
                }
            }

            // Check the decrement port.
            for (int i = 0; i < decrement.getWidth(); i++) {
                if (decrement.hasToken(i)) {
                    decrement.get(i);
                    _latestCount--;
                    _consumed = true;
                }
            }

            // Produce an output if we consumed an input.
            if (_consumed) {
                FixPoint result = new FixPoint(_latestCount);
                Token outputToken = new FixToken(result);
                sendOutput(output, 0, outputToken);
            }
        } else {
            output.resend(0);
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

    /** Override the base class to declare that the <i>increment</i>
     *  and <i>decrement</i> ports do not depend on the <i>output</i>
     *  in a firing.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        removeDependency(increment, output);
        removeDependency(decrement, output);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    /**
     * Internal memory of the current count.
     */
    private int _count = 0;

    /**
     *
     */
    private int _latestCount = 0;

    /**
     *
     */
    private boolean _consumed;
}
