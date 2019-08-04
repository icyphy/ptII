/* Convert to dB.

 Copyright (c) 1997-2014 The Regents of the University of California.
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

import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// DB

/**
 Produce a token that is the value of the input in decibels.
 That is, if the input is <i>z</i>, then the output is
 <i>k</i>*log<sub>10</sub>(<em>z</em>).
 The constant <i>k</i> depends on the value of the <i>inputIsPower</i>
 parameter.  If that parameter is true, then <i>k</i> = 10.
 Otherwise (the default) <i>k</i> = 20.
 Normally, you would set <i>inputIsPower</i> to true if
 the input is the square of a signal, and to false otherwise.
 <p>
 The output is never smaller than the value of the <i>min</i> parameter.
 This makes it easier to plot by limiting the range of output values.
 If the input is zero or negative, then the output is the
 value of the <i>min</i> parameter.
 <p>
 The input and output both have type double.

 @author Bart Kienhuis and Edward A. Lee
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Yellow (ssachs)
 */
public class DB extends Transformer {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public DB(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setTypeEquals(BaseType.DOUBLE);
        output.setTypeEquals(BaseType.DOUBLE);

        inputIsPower = new Parameter(this, "inputIsPower", new BooleanToken(
                false));
        inputIsPower.setTypeEquals(BaseType.BOOLEAN);
        min = new Parameter(this, "min", new DoubleToken(-100.0));
        min.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         ports and parameters              ////

    /** If the input is proportional to power, then set this to true.
     *  This must be a boolean, and defaults to false.
     */
    public Parameter inputIsPower;

    /** The minimum value of the output.  This is a double,
     *  and defaults to -100.0.
     */
    public Parameter min;

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
        DB newObject = (DB) super.clone(workspace);

        newObject._resultArray = new DoubleToken[_resultArray.length];
        System.arraycopy(_resultArray, 0, newObject._resultArray, 0,
                _resultArray.length);
        return newObject;
    }

    /** Read a token from the input and convert its value into a
     *  decibel representation. If the input does not contain any tokens,
     *  do nothing.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (input.hasToken(0)) {
            DoubleToken in = (DoubleToken) input.get(0);
            double number = in.doubleValue();
            double minValue = ((DoubleToken) min.getToken()).doubleValue();
            output.send(0, _doFunction(number, minValue));
        }
    }

    /** Invoke a specified number of iterations of this actor. Each
     *  iteration converts a single token to decibels. An invocation
     *  of this method therefore applies the conversion to <i>count</i>
     *  successive input tokens.
     *  <p>
     *  This method should be called instead of the usual prefire(),
     *  fire(), postfire() methods when this actor is used in a
     *  domain that supports vectorized actors.  This leads to more
     *  efficient execution.
     *  @param count The number of iterations to perform.
     *  @return COMPLETED if the actor was successfully iterated the
     *   specified number of times. Otherwise, return NOT_READY, and do
     *   not consume any input tokens.
     *  @exception IllegalActionException If iterating cannot be
     *  performed.
     */
    @Override
    public int iterate(int count) throws IllegalActionException {
        // Check whether we need to reallocate the output token array.
        if (count > _resultArray.length) {
            _resultArray = new DoubleToken[count];
        }

        if (input.hasToken(0, count)) {
            double minValue = ((DoubleToken) min.getToken()).doubleValue();

            // NOTE: inArray.length may be > count, in which case
            // only the first count tokens are valid.
            Token[] inArray = input.get(0, count);

            for (int i = 0; i < count; i++) {
                double input = ((DoubleToken) inArray[i]).doubleValue();
                _resultArray[i] = _doFunction(input, minValue);
            }

            output.send(0, _resultArray, count);
            return COMPLETED;
        } else {
            return NOT_READY;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return the specified number in decibels,
     *  but no less than <i>minValue</i>.
     */
    private DoubleToken _doFunction(double number, double minValue)
            throws IllegalActionException {
        double outNumber;

        if (number <= 0.0) {
            outNumber = minValue;
        } else {
            outNumber = ptolemy.math.SignalProcessing.toDecibels(number);

            if (((BooleanToken) inputIsPower.getToken()).booleanValue()) {
                outNumber /= 2.0;
            }

            if (outNumber < minValue) {
                outNumber = minValue;
            }
        }

        return new DoubleToken(outNumber);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private DoubleToken[] _resultArray = new DoubleToken[1];
}
