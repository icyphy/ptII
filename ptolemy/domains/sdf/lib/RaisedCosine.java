/* An FIR filter with a raised cosine frequency response.

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
package ptolemy.domains.sdf.lib;

import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.math.DoubleUnaryOperation;
import ptolemy.math.SignalProcessing;

///////////////////////////////////////////////////////////////////
//// RaisedCosine

/**
 This actor implements an FIR filter with
 a raised cosine or square-root raised cosine frequency response.
 The excess bandwidth is given
 by <i>excessBW</i> and the symbol interval (in number of samples)
 by <i>interpolation</i> (which by default is 16).
 The length of the filter (the number of taps) is given by <i>length</i>.
 <p>
 For the ordinary raised cosine response,
 the impulse response of the filter would ideally be
 <pre>
        sin(pi n/T)   cos(alpha pi n/T)
 h(n) = ----------- * -----------------
          pi n/T      1-(2 alpha n/T)<sup>2</sup>
 </pre>
 where <i>alpha</i> is <i>excessBW</i> and <i>T</i> is the
 <i>interpolation</i> factor.
 However, this pulse is centered at zero, and we can only implement causal
 filters in the SDF domain in Ptolemy.  Hence, the impulse response is
 actually
 <pre>
 g(n) = h(n - M)
 </pre>
 where <i>M</i> = <i>length/</i>2 if <i>length</i> is even, and <i>M
 </i>= (<i>length+</i>1)<i>/</i>2 if <i>length</i> is odd.
 The impulse response is simply truncated outside this range, so
 the impulse response will generally not be symmetric if <i>length</i> is even
 because it will have one more sample to the left than to the right of center.
 Unless this extra sample is zero, the filter will not have linear phase
 if <i>length</i> is even.
 <p>
 For the ordinary raised cosine response, the
 distance (in number of samples) from the center
 to the first zero crossing is given by <i>symbolInterval</i>.
 For the square-root raised cosine response, a cascade of two identical
 square-root raised cosine filters would be equivalent to a single
 ordinary raised cosine filter.
 <p>
 The impulse response of the square-root raised cosine pulse is given by
 <pre>
         4 alpha(cos((1+alpha)pi n/T)+Tsin((1-alpha)pi n/T)/(4n alpha/T))
 h(n) = -----------------------------------------------------------------
                      pi sqrt(T)(1-(4 alpha n/T)<sup>2</sup>)
 </pre>
 This impulse response convolved with itself will, in principle, be equal
 to a raised cosine pulse.  However, because of the abrupt rectangular
 windowing of the pulse, with low excess bandwidth, this ideal is not
 closely approximated except for very long filters.
 <p>
 The output sample rate is <i>interpolation</i> times the input.
 This is set by default to 16 because in digital communication systems
 this pulse is used for the line coding of symbols, and upsampling is necessary.
 Typically, the value of <i>interpolation</i> is the same as that of
 <i>symbolInterval</i>, at least when the filter is being used
 as a transmit pulse shaper.
 <h3>References</h3>
 <p>[1]
 E. A. Lee and D. G. Messerschmitt,
 <i>Digital Communication,</i> Kluwer Academic Publishers, Boston, 1988.
 <p>[2]
 I. Korn, <i>Digital Communications</i>, Van Nostrand Reinhold, New York, 1985.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Yellow (neuendor)
 @Pt.AcceptedRating Yellow (neuendor)
 */
public class RaisedCosine extends FIR {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public RaisedCosine(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        length = new Parameter(this, "length", new IntToken(64));
        interpolation.setToken(new IntToken(16));
        excessBW = new Parameter(this, "excessBW", new DoubleToken(1.0));
        root = new Parameter(this, "root", new BooleanToken(false));
        symbolInterval = new Parameter(this, "symbolInterval", new IntToken(16));

        // Hide taps from UI.
        taps.setVisibility(Settable.NONE);
        _initialize();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The excess bandwidth.  This contains a
     *  DoubleToken, and by default it has value 1.0.
     */
    public Parameter excessBW;

    /** The length of the pulse.  This contains an
     *  IntToken, and by default it has value 64.
     */
    public Parameter length;

    /** If true, use the square root of the raised cosine instead of the
     *  raised cosine.  This contains a
     *  BooleanToken, and by default it has value false.
     */
    public Parameter root;

    /** The symbol interval, which is the number of samples to the first
     *  zero crossing on each side of the main lobe.  Its value is an
     *  IntToken, and by default it has value 16.
     */
    public Parameter symbolInterval;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Reevaluate the filter taps if the attribute is any of the ones
     *  defined locally, and otherwise call the superclass.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the parameters are out of range.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == excessBW || attribute == length || attribute == root
                || attribute == symbolInterval) {
            _initialize();
        } else {
            super.attributeChanged(attribute);
        }
    }

    // Initialize the state of the actor based on the current state of the
    // parameters.
    private void _initialize() throws IllegalActionException {
        double excessBWValue = ((DoubleToken) excessBW.getToken())
                .doubleValue();
        int symbolIntervalValue = ((IntToken) symbolInterval.getToken())
                .intValue();
        int lengthValue = ((IntToken) length.getToken()).intValue();
        boolean sqrt = ((BooleanToken) root.getToken()).booleanValue();

        if (excessBWValue < 0.0) {
            throw new IllegalActionException(this, "Excess bandwidth was "
                    + excessBWValue
                    + " which is not greater than or equal to zero.");
        }

        if (lengthValue <= 0) {
            throw new IllegalActionException(this, "Length was " + lengthValue
                    + " which is not greater than zero.");
        }

        double center = lengthValue * 0.5;

        DoubleUnaryOperation raisedCosineSampleGenerator = sqrt ? (DoubleUnaryOperation) new SignalProcessing.SqrtRaisedCosineSampleGenerator(
                symbolIntervalValue, excessBWValue)
                : (DoubleUnaryOperation) new SignalProcessing.RaisedCosineSampleGenerator(
                        symbolIntervalValue, excessBWValue);

        double[] tapsArray = SignalProcessing.sampleWave(lengthValue, -center,
                1.0, raisedCosineSampleGenerator);
        DoubleToken[] tapsArrayToken = new DoubleToken[tapsArray.length];

        for (int i = 0; i < tapsArray.length; i++) {
            tapsArrayToken[i] = new DoubleToken(tapsArray[i]);
        }

        taps.setToken(new ArrayToken(BaseType.DOUBLE, tapsArrayToken));
    }
}
