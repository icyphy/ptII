/* A helper class for ptolemy.actor.lib.Ramp.

 Copyright (c) 2006 The Regents of the University of California.
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
package ptolemy.data.properties.lattice.logicalAND.actor.lib;

import java.util.List;

import ptolemy.data.ScalarToken;
import ptolemy.data.properties.lattice.PropertyConstraintHelper;
import ptolemy.data.properties.lattice.PropertyConstraintSolver;
import ptolemy.data.properties.lattice.logicalAND.Lattice;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// Const

/**
 A helper class for ptolemy.actor.lib.Ramp.

 @author Thomas Mandl
*/
public class Ramp extends PropertyConstraintHelper {

    /**
     */
    public Ramp(PropertyConstraintSolver solver, 
            ptolemy.actor.lib.Ramp actor)
            throws IllegalActionException {

        super(solver, actor, false);
    }

    public List<Inequality> constraintList() throws IllegalActionException {
        ptolemy.actor.lib.Ramp actor =
            (ptolemy.actor.lib.Ramp) getComponent();
                
        Lattice lattice = (Lattice) getSolver().getLattice();        

        if (actor.step.getPort().connectedPortList().isEmpty()) {
            if (((ScalarToken)actor.init.getToken()).doubleValue() == 0) {
                setAtLeast(actor.output, lattice.FALSE);
            } else {
                setAtLeast(actor.output, lattice.TRUE);                
            }
        } else {
            setAtLeast(actor.output, actor.step);
        }

        return super.constraintList();
    }
}
