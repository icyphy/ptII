/* A type polymorphic FIR filter with a port that sets the taps.

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
import ptolemy.data.IntToken;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.graph.InequalityTerm;
import ptolemy.domains.sdf.kernel.*;

//////////////////////////////////////////////////////////////////////////
//// VariableFIR
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

        newTaps = new TypedIOPort(this, "newTaps");
        newTaps.setInput(true);
        // FIXME: Doesn't work.
        // newTaps.setTypeSameAs(taps);
        newTaps.setTypeEquals(new ArrayType(BaseType.DOUBLE));
        output.setTypeSameAs(input);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The input for new tap values.
     */
    public TypedIOPort newTaps;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

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

        // set the type constraints
        newObject.newTaps.setTypeAtLeast(newObject.taps);
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

            _reinitializeNeeded = true;
        }
        super.fire();
    }
}
