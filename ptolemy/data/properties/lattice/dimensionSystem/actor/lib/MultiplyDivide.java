/* An adapter class for ptolemy.actor.lib.MultiplyDivide.

 Copyright (c) 2006-2009 The Regents of the University of California.
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
package ptolemy.data.properties.lattice.dimensionSystem.actor.lib;

import java.util.List;

import ptolemy.data.properties.lattice.PropertyConstraintSolver;
import ptolemy.data.properties.lattice.dimensionSystem.MultiplyMonotonicFunction;
import ptolemy.data.properties.lattice.dimensionSystem.DivideMonotonicFunction;
import ptolemy.data.properties.lattice.dimensionSystem.actor.AtomicActor;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// MultiplyDivide

/**
 An adapter class for ptolemy.actor.lib.MultiplyDivide.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
*/
public class MultiplyDivide extends AtomicActor {

    /**
     * Construct a MultiplyDivide adapter for the flatUnitSystem lattice.
     * @param solver The given solver.
     * @param actor The given MultiplyDivide actor
     * @exception IllegalActionException
     */
    public MultiplyDivide(PropertyConstraintSolver solver,
            ptolemy.actor.lib.MultiplyDivide actor)
            throws IllegalActionException {

        super(solver, actor, false);
    }

    public List<Inequality> constraintList() throws IllegalActionException {
        ptolemy.actor.lib.MultiplyDivide actor = (ptolemy.actor.lib.MultiplyDivide) getComponent();

        if (actor.multiply.getWidth() != 1 || actor.divide.getWidth() != 1) {
            throw new IllegalActionException(
                    actor,
                    "The property analysis "
                            + "currently supports only binary division (e.g. exactly 1 "
                            + "connection to the multiply port and 1 connection "
                            + "to the divide port.");
        }

        // The output is the quotient of the multiply and divide ports, so use the 
        // DivideMonotonicFunction
        setAtLeast(actor.output, new DivideMonotonicFunction(actor.multiply, actor.divide,
                                                               _lattice, this));
        
        // The multiply input port is the dividend, so use the DividendMonotonicFunction
        setAtLeast(actor.multiply, new MultiplyMonotonicFunction(actor.output, actor.divide,
                                                               _lattice, this));
        
        // The divide input port is the divisor, so use the DivisorMonotonicFunction
        setAtLeast(actor.divide, new DivideMonotonicFunction(actor.multiply, actor.output,
                                                              _lattice, this));

        return super.constraintList();
    }
}
