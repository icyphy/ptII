/* An FIR filter that is optimized for operating on DoubleTokens.

 Copyright (c) 1998-2001 The Regents of the University of California.
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

import ptolemy.actor.*;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.data.Token;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.sdf.kernel.*;

import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// FIRDouble
/**
This actor implements a finite-impulse response filter with multirate
capability.  It differs from the standard FIR filter in that its input,
output and taps are restricted to be doubles, which allows faster
computation.

@author Edward A. Lee, Bart Kienhuis
@version $Id$
@see ptolemy.domains.sdf.lib.FIR
*/

public class FIRDouble extends FIR {
    // FIXME: use past sample support.
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public FIRDouble(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        input.setTypeEquals(BaseType.DOUBLE);
        output.setTypeEquals(BaseType.DOUBLE);
        taps.setTypeEquals(new ArrayType(BaseType.DOUBLE));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Set a flag that causes recalculation of various local variables
     *  that are used in execution on the next invocation of fire().
     *  @param attribute The attribute that changed.
     */
    public void attributeChanged(Attribute attribute)
             throws IllegalActionException {
        if (attribute == taps) {
            ArrayToken tapsToken = (ArrayToken)(taps.getToken());
            _taps = new double[tapsToken.length()];
            for (int i = 0; i < _taps.length; i++) {
                _taps[i] =
                    ((DoubleToken)tapsToken.getElement(i)).doubleValue();
            }
            _reinitializeNeeded = true;
            // note that we do NOT call the super class.
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters. The new
     *  actor will have the same parameter values as the old.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
	    throws CloneNotSupportedException {
        FIRDouble newObject = (FIRDouble)(super.clone(workspace));
        return newObject;
    }

    // FIXME: State update should occur in postfire.

    /** Consume the inputs and produce the outputs of the FIR filter.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void fire() throws IllegalActionException {

        // If an attribute has changed since the last fire(), or if
        // this is the first fire(), then renitialize.
        if (_reinitializeNeeded) _reinitialize();

        // phase keeps track of which phase of the filter coefficients
        // are used. Starting phase depends on the _decPhase value.
        int phase = _dec - _decPhase - 1;

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

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Reinitialize local variables in response to changes in attributes.
    private void _reinitialize() throws IllegalActionException {
        if (_decPhase >= _dec) {
            throw new IllegalActionException(this,
                    "Invalid decimationPhase: " + _decPhase
                    + ". Must be less than decimation: " + _dec + ".");
        }

        _phaseLength = (int)(_taps.length / _interp);
        if ((_taps.length % _interp) != 0) _phaseLength++;

        // Create new data array and initialize index into it.
        // Avoid losing the data if possible.
        // FIXME: the data shouldn't be lost at all.
        if (_data == null || _data.length != _phaseLength) {
            _data = new double[_phaseLength];
            for(int i = 0; i < _phaseLength; i++ ) {
                _data[i] = 0;
            }
            _mostRecent = _phaseLength;
        }
        _reinitializeNeeded = false;
    }

    /** Override the supper class method so that the type constraints there
     *  are not used. This class has the types of the ports and the taps
     *  parameter fixed to double, so no constraints are needed.
     *  @return an empty list.
     */
    public List typeConstraintList() {
        return new LinkedList();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Local cache of these parameter values.
    private double[] _taps;
    private double[] _data;
    private int _mostRecent;
}
