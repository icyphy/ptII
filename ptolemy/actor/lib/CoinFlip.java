/* An actor that outputs a random sequence of booleans.

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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import java.util.Random;

//////////////////////////////////////////////////////////////////////////
//// CoinFlip
/**
Produce a random sequence of booleans.
The values that are generated are independent and identically distributed,
where the probability of <i>true</i> is given by the parameter
<i>trueProbability</i>.
The seed can be specified as a parameter to control the sequence that is
generated.

@author Edward A. Lee
@version $Id$
*/

public class CoinFlip extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public CoinFlip(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        output = new TypedIOPort(this, "output", false, true);
        output.setDeclaredType(BooleanToken.class);

        trueProbability = new Parameter(this, "trueProbability",
                new DoubleToken(0.5));
        seed = new Parameter(this, "seed", new LongToken(0));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The output port. */
    public TypedIOPort output;

    /** The probability of <i>true</i>.
     *  This parameter contains a DoubleToken, initially with value 0.5.
     */
    public Parameter trueProbability;

    /** The seed that controls the random number generation.
     *  A seed of zero is interpreted to mean that no seed is specified,
     *  which means that each execution of the system could result in
     *  distinct data.
     *  This parameter contains a LongToken, initially with value 0.
     */
    public Parameter seed;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the public variables.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) {
        try {
            CoinFlip newobj = (CoinFlip)super.clone(ws);
            newobj.output = (TypedIOPort)newobj.getPort("output");
            // newobj.output.setDeclaredType(DoubleToken.class);
	    newobj.trueProbability =
                (Parameter)newobj.getAttribute("trueProbability");
	    newobj.seed = (Parameter)newobj.getAttribute("seed");
            return newobj;
        } catch (CloneNotSupportedException ex) {
            // Errors should not occur here...
            throw new InternalErrorException(
                    "Clone failed: " + ex.getMessage());
        }
    }

    /** Initialize the random number generator with the seed, if it
     *  has been given.  A seed of zero is interpreted to mean that no
     *  seed is specified.
     *  @exception IllegalActionException Not thrown in this class.
     */
    public void initialize() throws IllegalActionException {
	long sd = ((LongToken)(seed.getToken())).longValue();
        if(sd != (long)0) {
            _random.setSeed(sd);
        }
    }

    /** Send the next output.
     */
    public void fire() {
	double tp = ((DoubleToken)(trueProbability.getToken())).doubleValue();
        double rawnum = _random.nextDouble();
        // Adjust so that 1.0 is not a possible outcome.
        if (rawnum == 1.0) rawnum -= Double.MIN_VALUE;
        boolean result;
        if (rawnum < tp) {
            result = true;
        } else {
            result = false;
        }
        try {
            output.broadcast(new BooleanToken(result));
        } catch (IllegalActionException ex) {
            // Should not be thrown because this is an output port.
            throw new InternalErrorException(ex.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Random _random = new Random();
}

