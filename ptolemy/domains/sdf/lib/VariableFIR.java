/* A type polymorphic FIR filter with a port that sets the taps.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Yellow (neuendor@eecs.berkeley.edu)
@AcceptedRating Yellow (neuendor@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.lib;

import ptolemy.actor.Director;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// VariableFIR
/**
This actor implements a type polymorphic finite-impulse response
filter with multirate capability, where the impulse response
of the filter is provided by an input. Since this filter operates on
Tokens, it is polymorphic in the type of data it operates on.
<p>
If the <i>decimation</i> parameter is unity (the default), then
the <i>blockSize</i> parameter specifies the number of inputs
of the filter that are processed per coefficient set provided on the
<i>newTaps</i> input.  Otherwise, if <i>decimation</i> is greater than unity,
then the number of tokens consumed is the product of <i>decimation</i>
and <i>blockSize</i>, and all these inputs are processed using the
filter coefficients provided on <i>newTaps</i>.
In all other respects, the behavior of this
actor is the same as that of the base class.
<p>
Note that when a new set of filter coefficients arrives on <i>newTaps</i>,
if the new set has more coefficients than the old set, then a transient
will occur that may be unexpected.  The delay line containing previously
consumed data has to be increased in length to match the number of
new coefficients.  However, the extended part of the delay line cannot
possibly be initialized with previously consumed data because that
data has not been saved.  Unless this actor were to save <i>all</i>
previously consumed data (which would be hopelessly inefficient), there
is no way it can be assured of always having the requisite data.
Thus, the actor initializes the extended part of the delay line
with zeros of the same type as the input data.

@author Edward A. Lee, Yuhong Xiong
@version $Id$
@since Ptolemy II 1.0

@see ptolemy.data.Token
*/
public class VariableFIR extends FIR {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public VariableFIR(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        blockSize = new Parameter(this, "blockSize");
        blockSize.setExpression("1");

        newTaps = new TypedIOPort(this, "newTaps");
        newTaps.setInput(true);

        newTaps.setTypeSameAs(taps);

        // The taps parameter is no longer of any use, so it is hidden.
        taps.setVisibility(Settable.NONE);

        output.setTypeSameAs(input);

        output_tokenProductionRate.setExpression("interpolation * blockSize");
        input_tokenConsumptionRate.setExpression("decimation * blockSize");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The number of inputs that use each each coefficient set is the
     *  value of this parameter multiplied by the value of the
     *  <i>decimation</i> parameter.
     *  This is an integer that defaults to 1.
     */
    public Parameter blockSize;

    /** The input for new tap values.  This is an array.
     */
    public TypedIOPort newTaps;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Set a flag that causes recalculation of various local variables
     *  that are used in execution on the next invocation of fire().
     *  @param attribute The attribute that changed.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        super.attributeChanged(attribute);
        if (attribute == interpolation
                || attribute == decimation
                || attribute == blockSize) {

            _reinitializeNeeded = true;
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then resets the type constraints.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        VariableFIR newObject = (VariableFIR)(super.clone(workspace));

        newObject.newTaps.setTypeSameAs(newObject.taps);
        newObject.output.setTypeSameAs(newObject.input);
        return newObject;
    }

    /** Consume the inputs and produce the outputs of the FIR filter.
     *  @exception IllegalActionException If parameter values are invalid,
     *   or if there is no director, or if runtime type conflicts occur.
     */
    public void fire() throws IllegalActionException {
        if (newTaps.hasToken(0)) {
            ArrayToken tapsToken = (ArrayToken)(newTaps.get(0));
            _taps = tapsToken.arrayValue();

            // Get a token representing zero in the appropriate type.
            _zero = _taps[0].zero();

            _reinitialize();
        }
        int blockSizeValue = ((IntToken)blockSize.getToken()).intValue();
        for (int i = 0; i < blockSizeValue; i++) {
            super.fire();
        }
    }

    /** Return false if the input does not have enough tokens to fire.
     *  Otherwise, return true.
     *  @return False if the number of input tokens available is not at least
     *   equal to the <i>decimation</i> parameter multiplied by the
     *   <i>blockSize</i> parameter.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public boolean prefire() throws IllegalActionException {
        // If an attribute has changed since the last fire(), or if
        // this is the first fire(), then reinitialize.
        if (_reinitializeNeeded) _reinitialize();

        if (newTaps.hasToken(0)) {
            return super.prefire();
        } else {
            if (_debugging) {
                _debug("Called prefire(), which returns false.");
            }
            return false;
        }
    }
}
