/* An FIR filter.

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
This actor implements a finite-impulse response
filter with multirate capability.

@author Edward A. Lee
@version $Id$
*/

public class FIRDouble extends FIR {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public FIRDouble(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        input.setTypeEquals(BaseType.DOUBLE);
        output.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters. The new
     *  actor will have the same parameter values as the old.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) {
        FIRDouble newobj = (FIRDouble)(super.clone(ws));
        return newobj;
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
        _data[_mostRecent] = ((DoubleToken)(input.get(0))).doubleValue();

        // Interpolate once for each input consumed
        for (int inC = 1; inC <= _dec; inC++) {
            // Produce however many outputs are required
            // for each input consumed
            while (phase < _interp) {
                double out = 0.0;
                // Compute the inner product.
                for (int i = 0; i < _phaseLength; i++) {
                    int tapsIndex = i * _interp + phase;
                    double tap = 0.0;
                    if (tapsIndex < _taps.length) tap = _taps[tapsIndex];
                    int dataIndex =
                        (_mostRecent + _dec - inC + i)%(_data.length);
                    out += tap * _data[dataIndex];
                }
                output.send(0, new DoubleToken(out));
                phase += _dec;
            }
            phase -= _interp;
        }
    }

    /** Set up the consumption and production constants.
     *  @exception IllegalActionException If the parameters are out of range.
     */
    public void initialize() throws IllegalActionException {

	// Case the tapsToken matrix because it will be
	// a matrix with doubles.
        _taps = new double[_tapsToken.getColumnCount()];
        for (int i = 0; i < _taps.length; i++) {
            _taps[i] = ((DoubleMatrixToken)_tapsToken).getElementAt(0, i);
        }
        _phaseLength = (int)(_taps.length / _interp);
        if ((_taps.length % _interp) != 0) _phaseLength++;

        // Create new data array and initialize index into it.
        int datalength = _taps.length/_interp;
        if (_taps.length%_interp != 0) datalength++;
        _data = new double[datalength];
        _mostRecent = datalength;

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Local cache of these parameter values.
    private double[] _taps;
    private double[] _data;
    private int _mostRecent;
}
