/* Moving average.

 Copyright (c) 2007-2014 The Regents of the University of California.
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

import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// MovingAverage

/**
 This actor outputs the moving average of the input sequence.
 The maximum number of prior inputs to be averaged is given by
 the <i>maxPastInputsToAverage</i> parameter. The output is
 the average of the all the previous inputs that have been
 received if fewer than <i>maxPastInputsToAverage</i> have
 been received, and the average of the previous
 <i>maxPastInputsToAverage</i> inputs otherwise.
 The input can be any data type that supports division
 by a double.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 @see ptolemy.data.Token
 */
public class MovingAverage extends Transformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public MovingAverage(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        maxPastInputsToAverage = new Parameter(this, "maxPastInputsToAverage");
        maxPastInputsToAverage.setTypeEquals(BaseType.INT);
        maxPastInputsToAverage.setExpression("10");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The maximum number of past inputs to average. This is an integer
     *  that defaults to 10.
     */
    public Parameter maxPastInputsToAverage;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Set a flag that causes recalculation of various local variables
     *  that are used in execution on the next invocation of fire().
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the attribute contains
     *  an invalid value or if the super method throws it.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == maxPastInputsToAverage) {
            _maxPastInputsToAverage = ((IntToken) maxPastInputsToAverage
                    .getToken()).intValue();
            if (_maxPastInputsToAverage <= 0) {
                throw new IllegalActionException(this,
                        "Value of maxPastInputsToAverage is required to be positive.");
            }
            _reinitializeNeeded = true;
        } else {
            super.attributeChanged(attribute);
        }
    }

    // FIXME: State update should occur in postfire.

    /** Consume the inputs and produce the output.
     *  @exception IllegalActionException If parameter values are invalid,
     *   or if there is no director, or if runtime type conflicts occur.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        // Transfer current input to _data[]
        if (_mostRecent <= 0) {
            _mostRecent = _maxPastInputsToAverage - 1;
        } else {
            --_mostRecent;
        }
        _data[_mostRecent] = input.get(0);

        if (_count < _maxPastInputsToAverage) {
            _count++;
            _factor = new DoubleToken(1.0 / _count);
        }

        // Compute the average.
        Token sum = _data[_mostRecent];
        for (int i = 1; i < _count; i++) {
            int dataIndex = (_mostRecent + i) % _data.length;
            sum = sum.add(_data[dataIndex]);
        }
        output.send(0, _factor.multiply(sum));
    }

    /** Return false if the input does not have a token.
     *  Otherwise, return what the superclass returns.
     *  @return False if the number of input tokens available is not at least
     *   equal to the decimation parameter.
     *  @exception IllegalActionException If the superclass throws it.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        // If an attribute has changed since the last fire(), or if
        // this is the first fire(), then reinitialize.
        if (_reinitializeNeeded) {
            _reinitialize();
        }

        if (input.hasToken(0)) {
            return super.prefire();
        } else {
            if (_debugging) {
                _debug("Called prefire(), which returns false.");
            }
            return false;
        }
    }

    /** Perform domain-specific initialization by calling the
     *  initialize(Actor) method of the director. The director may
     *  reject the actor by throwing an exception if the actor is
     *  incompatible with the domain.
     *  Set a flag that reinitializes the data buffer at the first firing.
     *  @exception IllegalActionException If the superclass throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        // Must be sure to throw away the old data buffer.
        _data = null;
        _reinitializeNeeded = true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Reinitialize local variables in response to changes in attributes.
     *  @exception IllegalActionException If there is a problem reinitializing.
     */
    protected void _reinitialize() throws IllegalActionException {
        // Create new data array and initialize index into it.
        int length = _maxPastInputsToAverage;
        _data = new Token[length];
        _count = 0;
        _mostRecent = _maxPastInputsToAverage;
        _reinitializeNeeded = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The delay line. */
    protected Token[] _data;

    /** The index into the delay line of the most recent input. */
    protected int _mostRecent;

    /** Count of the number of inputs received, bounded by the
     *  size of the _data array.
     */
    protected int _count;

    /** Maximum number of past inputs to average. */
    protected int _maxPastInputsToAverage = 10;

    /** Indicator that at least an attribute has been changed
     *  since the last initialization.
     */
    protected boolean _reinitializeNeeded = true;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The factor by which to multiply the sum of the past inputs. */
    private DoubleToken _factor;
}
