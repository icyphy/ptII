/* An actor that outputs a random sequence with a Gaussian distribution.

 Copyright (c) 1998-1999 The Regents of the University of California.
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

import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import java.util.Random;

//////////////////////////////////////////////////////////////////////////
//// Gaussian.
/**
Produce a random sequence with a Gaussian distribution.
The values that are generated are independent and identically distributed
with the mean and the variance given by parameters.  In addition, the
seed can be specified as a parameter to control the sequence that is
generated.

@author Edward A. Lee
@version $Id$
*/

public class Gaussian extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Gaussian(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        output = new TypedIOPort(this, "output", false, true);
        output.setDeclaredType(DoubleToken.class);

        mean = new Parameter(this, "mean", new DoubleToken(0.0));
        stddev = new Parameter(this, "stddev", new DoubleToken(1.0));
        seed = new Parameter(this, "seed", new LongToken(0));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The output port. */
    public TypedIOPort output;

    /** The mean of the random number.
     *  This parameter contains a DoubleToken, initially with value 0.
     */
    public Parameter mean;

    /** The standard deviation of the random number.
     *  This parameter contains a DoubleToken, initially with value 1.
     */
    public Parameter stddev;

    /** The seed that controls the random number generation.
     *  A seed of zero is interpreted to mean that no seed is specified,
     *  which means that each execution of the system could result in
     *  distinct data.
     *  This parameter contains a Longoken, initially with value 0.
     */
    public Parameter seed;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) {
        try {
            Gaussian newobj = (Gaussian)super.clone(ws);
            newobj.output = new TypedIOPort(this, "output", false, true);
            newobj.output.setDeclaredType(DoubleToken.class);
            newobj.mean = new Parameter(this, "mean", new DoubleToken(0.0));
            newobj.stddev = new Parameter(this, "stddev", new DoubleToken(1.0));
            newobj.seed = new Parameter(this, "seed", new LongToken(0));
            return newobj;
        } catch (KernelException ex) {
            // Errors should not occur here...
            throw new InternalErrorException(
                    "Internal error: " + ex.getMessage());
        } catch (CloneNotSupportedException ex) {
            // Errors should not occur here...
            throw new InternalErrorException(
                    "Clone failed: " + ex.getMessage());
        }
    }

    /** Initialize the random number generator with the seed, if it
     *  has been given.  A seed of zero is interpreted to mean that no
     *  seed is specified.
     */
    public void initialize() throws IllegalActionException {
	long sd = ((LongToken)(seed.getToken())).longValue();
        if(sd != (long)0) {
            _random.setSeed(sd);
        }
    }

    /** Send out the next ramp output.
     */
    public void fire() {
	double mn = ((DoubleToken)(mean.getToken())).doubleValue();
	double sd = ((DoubleToken)(stddev.getToken())).doubleValue();
        double rawnum = _random.nextGaussian();
        double result = (rawnum*sd) + mn;
        try {
            output.broadcast(new DoubleToken(result));
        } catch (IllegalActionException ex) {
            // Should not be thrown because this is an output port.
            throw new InternalErrorException(ex.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Random _random = new Random();
}

