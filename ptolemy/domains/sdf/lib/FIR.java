/* A type polymorphic FIR filter.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.lib;

import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.sdf.kernel.*;

//////////////////////////////////////////////////////////////////////////
//// FIR
/**

This actor implements a type polymorphic finite-impulse response
filter with multirate capability. Since this filter operates on
Tokens, it is polymorphic in the type of data it operates on. It can
operate on Double values like the default FIR filter, but also on
Complex or FixPoint values.

<p>

When the <i>decimation</i> (<i>interpolation</i>)
parameters are different from unity, the filter behaves exactly
as it were followed (preceded) by a DownSample (UpSample) actor.
However, the implementation is much more efficient than
it would be using UpSample and DownSample stars;
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
If the interpolation ratio is <i>i < /i>, then <i>f</i>/2 is a fraction
1/2<i>i < /i> of the sample rate at which you must design your filter.
<p>
The <i>decimationPhase</i> parameter is somewhat subtle.
It is exactly equivalent the phase parameter of the DownSample
star.Its interpretation is as follows; when decimating,
samples are conceptually discarded (although a polyphase structure
does not actually compute the discarded samples).
If you are decimating by a factor of three, then you will select
one of every three outputs, with three possible phases.
When decimationPhase is zero (the default),
the latest (most recent) samples are the ones selected.
The decimationPhase must be strictly less than
the decimation ratio.
<p>
For more information about polyphase filters, see F. J. Harris,
"Multirate FIR Filters for Interpolating and Desampling", in
<i>Handbook of Digital Signal Processing</i>, Academic Press, 1987.

@author Edward A. Lee, Bart Kienhuis
@version $Id$
@see ptolemy.data.Token
@see ptolemy.domains.sdf.lib.FIR
*/

public class FIR extends SDFAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public FIR(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        input = new SDFIOPort(this, "input", true, false);
        output = new SDFIOPort(this, "output", false, true);

        taps = new Parameter(this, "taps", new DoubleMatrixToken());
        interpolation = new Parameter(this, "interpolation", new IntToken(1));

        // FIXME: Added decimation and decimationPhase parameters
        attributeTypeChanged( taps );
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The input port. */
    public SDFIOPort input;

    /** The output port. */
    public SDFIOPort output;

    /** The interpolation ratio of the filter. This must contain an
     *  IntToken, and by default it has value one.
     */
    public Parameter interpolation;

    /** The taps of the filter. This is a row vector embedded in in a
     *  token of type FixMatrixToken. By default, it is empty,
     *  meaning that the output of the filter is zero.
     */
    public Parameter taps;

    // FIXME: Check that the above comment is correct.

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the argument is the meanTime parameter, check that it is
     *  positive.
     * @exception IllegalActionException If the
     *  meanTime value is not positive.
     */
    public void attributeTypeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == taps) {

	    // Get the first token from the Matrix
	    // Used this tokne to extract its type.
	    Token tmpToken =
                ((MatrixToken)taps.getToken()).getElementAsToken(0, 0);

	    // Get a token representing zero in the requested type.
	    // _zero = tmpToken.zero();

	    // Set the type to the input and output port.
	    input.setTypeEquals( tmpToken.getType() );
	    output.setTypeEquals( tmpToken.getType() );

        } else {
            super.attributeTypeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.  The new
     *  actor will have the same parameter values as the old.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) {
        try {
            FIR newobj = (FIR)(super.clone(ws));
            newobj.input = (SDFIOPort)newobj.getPort("input");
            newobj.output = (SDFIOPort)newobj.getPort("output");
            newobj.interpolation =
                (Parameter)newobj.getAttribute("interpolation");
            newobj.taps = (Parameter)newobj.getAttribute("taps");
            return newobj;
        } catch (CloneNotSupportedException ex) {
            // Errors should not occur here...
            throw new InternalErrorException(
                    "Clone failed: " + ex.getMessage());
        }
    }

    /** Consume the inputs and produce the outputs of the FIR filter.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void fire() throws IllegalActionException {

        // phase keeps track of which phase of the filter coefficients
        // are used. Starting phase depends on the _decPhase value.
        int phase = _dec - _decPhase - 1;

        // FIXME: consume just one input for now.
        if (--_mostRecent < 0) _mostRecent = _data.length - 1;
        _data[_mostRecent] = input.get(0);

        // Interpolate once for each input consumed
        for (int inC = 1; inC <= _dec; inC++) {

            // Produce however many outputs are required
            // for each input consumed
            while (phase < _interp) {
                _outToken = _zero;

                // Compute the inner product.
                for (int i = 0; i < _phaseLength; i++) {
                    int tapsIndex = i * _interp + phase;

                    int dataIndex =
                        (_mostRecent + _dec - inC + i)%(_data.length);
                    if (tapsIndex < _taps.length) {
                        _tapItem = _taps[tapsIndex];
                        _dataItem = _data[dataIndex];
                        _dataItem = _tapItem.multiply( _dataItem );
                        _outToken = _outToken.add( _dataItem );
                    } else {
                        _dataItem = _data[dataIndex];
                        _outToken = _outToken.add( _dataItem );
                    }
                }

                output.send(0, _outToken);
                phase += _dec;
            }
            phase -= _interp;
        }
    }

    /** Set up the consumption and production constants.
     *  @exception IllegalActionException If the parameters are out of range.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();

        IntToken interptoken = (IntToken)(interpolation.getToken());
        _interp = interptoken.intValue();

        // FIXME: Support multirate.  Get values from parameters.
        _dec = 1;
        _decPhase = 0;

        // FIXME: Does the SDF infrastructure support accessing past samples?
        // FIXME: Handle mutations.
        input.setTokenConsumptionRate(_dec);
        output.setTokenProductionRate(_interp);
        if (_decPhase >= _dec) {
            throw new IllegalActionException(this,"decimationPhase too large");
        }

	// Get the taps now to allows for type resolution before initialize
        _tapsToken = (MatrixToken)(taps.getToken());

        // Get a token representing zero in the appropriate type.
	_zero = _tapsToken.getElementAsToken(0, 0).zero();
    }

    /** Initialize the taps of the FIR filter.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws  IllegalActionException {
	super.initialize();

        _taps = new Token[_tapsToken.getColumnCount()];
        for (int i = 0; i < _taps.length; i++) {
            _taps[i] = _tapsToken.getElementAsToken(0, i);
        }

        _phaseLength = (int)(_taps.length / _interp);
        if ((_taps.length % _interp) != 0) _phaseLength++;

        // Create new data array and initialize index into it.
        int datalength = _taps.length/_interp;
        if (_taps.length%_interp != 0) datalength++;
        _data = new Token[datalength];
        for(int i = 0; i < datalength; i++ ) {
            _data[i] = _zero;
        }
        _mostRecent = datalength;

    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The phaseLength is ceiling(length/interpolation), where
     *  length is the number of taps.
     */
    protected int _phaseLength;

    /** Control variables for the FIR main loop. */
    protected int _dec, _interp, _decPhase;

    /** The MatrixToken containing the taps of the FIR. */
    protected MatrixToken _tapsToken;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Local cache of these parameter values.
    private Token[] _taps;
    private Token[] _data;
    private int _mostRecent;

    // The tokens needed in FIR
    private Token _outToken;
    private Token _tapItem;
    private Token _dataItem;

    // Cache of the zero token.
    private Token _zero;

}
