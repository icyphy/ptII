/* A type polymorphic FIR filter.

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
import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// FIR
/**
This actor implements a type polymorphic finite-impulse response
filter with multirate capability. Since this filter operates on
Tokens, it is polymorphic in the type of data it operates on.
<p>
Note that the current implementation of this actor only reads its
parameters during initialization, so the filter cannot be
changed during execution.
<p>
When the <i>decimation</i> (<i>interpolation</i>)
parameters are different from unity, the filter behaves exactly
as it were followed (preceded) by a DownSample (UpSample) actor.
However, the implementation is much more efficient than
it would be using UpSample or DownSample actors;
a polyphase structure is used internally, avoiding unnecessary use
of memory and unnecessary multiplication by zero.
Arbitrary sample-rate conversions by rational factors can
be accomplished this way.
<p>
To design a filter for a multirate system, simply assume the
sample rate is the product of the interpolation parameter and
the input sample rate, or equivalently, the product of the decimation
parameter and the output sample rate.
In particular, considerable care must be taken to avoid aliasing.
Specifically, if the input sample rate is <i>f</i>,
then the filter stopband should begin before <i>f</i>/2.
If the interpolation ratio is <i>i</i>, then <i>f</i>/2 is a fraction
1/2<i>i</i> of the sample rate at which you must design your filter.
<p>
The <i>decimationPhase</i> parameter is somewhat subtle.
It is exactly equivalent the phase parameter of the DownSample actor.
Its interpretation is as follows; when decimating,
samples are conceptually discarded (although a polyphase structure
does not actually compute the discarded samples).
If you are decimating by a factor of three, then you will select
one of every three outputs, with three possible phases.
When decimationPhase is zero (the default),
the latest (most recent) samples are the ones selected.
The decimationPhase must be strictly less than
the decimation ratio.
<p>
<i>Note: in this description "sample rate" refers to the physical sampling
rate of an A/D converter in the system.  In other words, the number of
data samples per second.  This is not usually specified anywhere in an
SDF system, and most definitely does NOT correspond to the SDF rate parameters
of this actor.  This actor automatically sets the rates of the input
and output ports to the decimation and interpolation ratios, respectively.</i>
<p>
For more information about polyphase filters, see F. J. Harris,
"Multirate FIR Filters for Interpolating and Desampling", in
<i>Handbook of Digital Signal Processing</i>, Academic Press, 1987.

@author Edward A. Lee, Bart Kienhuis, Steve Neuendorffer
@version $Id$
@since Ptolemy II 0.2
@see ptolemy.data.Token
*/
public class FIR extends SDFTransformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public FIR(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        decimation = new Parameter(this, "decimation", new IntToken(1));
        decimation.setTypeEquals(BaseType.INT);

        decimationPhase = new Parameter(this, "decimationPhase",
                new IntToken(0));
        decimationPhase.setTypeEquals(BaseType.INT);

        interpolation = new Parameter(this, "interpolation", new IntToken(1));
        interpolation.setTypeEquals(BaseType.INT);

        taps = new Parameter(this, "taps");
        taps.setTypeEquals(new ArrayType(BaseType.UNKNOWN));
        taps.setExpression("{1.0}");

        input_tokenConsumptionRate.setExpression("decimation");
        output_tokenProductionRate.setExpression("interpolation");

        // Set type constraints.
        ArrayType paramType = (ArrayType)taps.getType();
        InequalityTerm elementTerm = paramType.getElementTypeTerm();
        output.setTypeAtLeast(elementTerm);
        output.setTypeAtLeast(input);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The decimation ratio of the filter. This must contain an
     *  IntToken, and by default it has value one.
     */
    public Parameter decimation;

    /** The decimation phase of the filter. This must contain an
     *  IntToken, and by default it has value zero.
     */
    public Parameter decimationPhase;

    /** The interpolation ratio of the filter. This must contain an
     *  IntToken, and by default it has value one.
     */
    public Parameter interpolation;

    /** The taps of the filter. This has a type of ArrayToken.
     *  By default, it contains an array with a single integer one,
     *  meaning that the output of the filter is the same as the input.
     */
    public Parameter taps;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Set a flag that causes recalculation of various local variables
     *  that are used in execution on the next invocation of fire().
     *  @param attribute The attribute that changed.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == interpolation) {
            IntToken token = (IntToken)(interpolation.getToken());
            _interpolationValue = token.intValue();
            if (_interpolationValue <= 0) {
                throw new IllegalActionException(this,
                        "Invalid interpolation: " + _interpolationValue
                        + ". Must be positive.");
            }

            _reinitializeNeeded = true;
        } else if (attribute == decimation) {
            IntToken token = (IntToken)(decimation.getToken());
            _decimationValue = token.intValue();
            if (_decimationValue <= 0) {
                throw new IllegalActionException(this,
                        "Invalid decimation: " + _decimationValue
                        + ". Must be positive.");
            }

            _reinitializeNeeded = true;
        } else if (attribute == decimationPhase) {
            IntToken token = (IntToken)(decimationPhase.getToken());
            _decimationPhaseValue = token.intValue();
            if (_decimationPhaseValue < 0) {
                throw new IllegalActionException(this,
                        "Invalid decimationPhase: " + _decimationPhaseValue
                        + ". Must be nonnegative.");
            }
            _reinitializeNeeded = true;
        } else if (attribute == taps) {
            ArrayToken tapsToken = (ArrayToken)(taps.getToken());
            _taps = tapsToken.arrayValue();

            // Get a token representing zero in the appropriate type.
            _zero = _taps[0].zero();

            _reinitializeNeeded = true;
        } else {
            super.attributeChanged(attribute);
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
        FIR newObject = (FIR)(super.clone(workspace));

        // Set the type constraints.
        ArrayType paramType = (ArrayType)newObject.taps.getType();
        InequalityTerm elementTerm = paramType.getElementTypeTerm();
        newObject.output.setTypeAtLeast(elementTerm);
        newObject.output.setTypeAtLeast(newObject.input);
        return newObject;
    }

    // FIXME: State update should occur in postfire.

    /** Consume the inputs and produce the outputs of the FIR filter.
     *  @exception IllegalActionException If parameter values are invalid,
     *   or if there is no director, or if runtime type conflicts occur.
     */
    public void fire() throws IllegalActionException {
        super.fire();

        // Phase keeps track of which phase of the filter coefficients
        // are used. Starting phase depends on the _decimationPhaseValue value.
        int phase = _decimationValue - _decimationPhaseValue - 1;

        // Transfer _decimationValue inputs to _data[]
        for (int inC = 1; inC <= _decimationValue; inC++) {
            if (--_mostRecent < 0) _mostRecent = _data.length - 1;
            // Note explicit type conversion, which is required to generate
            // code.
            _data[_mostRecent] = output.getType().convert(input.get(0));
        }

        // Interpolate once for each input consumed
        for (int inC = 1; inC <= _decimationValue; inC++) {

            // Produce however many outputs are required
            // for each input consumed
            while (phase < _interpolationValue) {
                _outToken = _zero;

                // Compute the inner product.
                for (int i = 0; i < _phaseLength; i++) {
                    int tapsIndex = i * _interpolationValue + phase;

                    int dataIndex =
                        (_mostRecent + _decimationValue - inC + i)%(_data.length);

                    if (tapsIndex < _taps.length) {
                        _tapItem = _taps[tapsIndex];
                        _dataItem = _data[dataIndex];
                        _dataItem = _tapItem.multiply( _dataItem );
                        _outToken = _outToken.add( _dataItem );
                    }
                    // else assume tap is zero, so do nothing.
                }

                output.send(0, _outToken);
                phase += _decimationValue;
            }
            phase -= _interpolationValue;
        }
    }

    /** Return false if the input does not have enough tokens to fire.
     *  Otherwise, return what the superclass returns.
     *  @return False if the number of input tokens available is not at least
     *   equal to the decimation parameter.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public boolean prefire() throws IllegalActionException {
        // If an attribute has changed since the last fire(), or if
        // this is the first fire(), then reinitialize.
        if (_reinitializeNeeded) _reinitialize();

        if (input.hasToken(0, _decimationValue)) {
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
    public void initialize() throws IllegalActionException {
        super.initialize();
        // Must be sure to throw away the old data buffer.
        _data = null;
        _reinitializeNeeded = true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    // Reinitialize local variables in response to changes in attributes.
    protected void _reinitialize() throws IllegalActionException {
        if (_decimationPhaseValue >= _decimationValue) {
            throw new IllegalActionException(this,
                    "Invalid decimationPhase: "
                    + _decimationPhaseValue
                    + ". Must be less than decimation: "
                    + _decimationValue
                    + ".");
        }
        _phaseLength = (int)(_taps.length / _interpolationValue);
        if ((_taps.length % _interpolationValue) != 0) _phaseLength++;

        // Create new data array and initialize index into it.
        // Avoid losing the data if possible.
        // NOTE: If the filter length increases, then it is impossible
        // to correctly initialize the delay line to contain previously
        // seen data, because that data has not been saved.
        int length = _phaseLength + _decimationValue;
        if (_data == null) {
            _data = new Token[length];
            for (int i = 0; i < length; i++ ) {
                _data[i] = _zero;
            }
            _mostRecent = _phaseLength;
        } else if (_data.length != length) {
            Token[] _oldData = _data;
            _data = new Token[length];
            for (int i = 0; i < length; i++ ) {
                if (i < _oldData.length) {
                    _data[i] = _oldData[i];
                } else {
                    _data[i] = _zero;
                }
            }
            _mostRecent = _phaseLength;
        }
        _reinitializeNeeded = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The delay line. */
    protected Token[] _data;

    /** The index into the delay line of the most recent input. */
    protected int _mostRecent;

    /** The phaseLength is ceiling(length/interpolation), where
     *  length is the number of taps.
     */
    protected int _phaseLength;

    /** Decimation value. */
    protected int _decimationValue = 1;

    /** Interpolation value. */
    protected int _interpolationValue = 1;

    /** DecimationPhase value. */
    protected int _decimationPhaseValue = 0;

    /** Indicator that at least one attribute has been changed
     *  since the last initialization.
     */
    protected boolean _reinitializeNeeded = true;

    /** Local cache of the tap values. */
    protected Token[] _taps;

    /** Local cache of the zero token. */
    protected Token _zero;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The tokens needed in FIR
    private Token _outToken;
    private Token _tapItem;
    private Token _dataItem;
}
