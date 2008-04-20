/* A helper class for ptolemy.actor.lib.Comparator.

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
package ptolemy.data.properties.lattice.typeSystem_C.actor.lib.logic;

import java.util.List;

import ptolemy.data.properties.lattice.PropertyConstraintHelper;
import ptolemy.data.properties.lattice.PropertyConstraintSolver;
import ptolemy.data.properties.lattice.typeSystem_C.Lattice;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// Comparator

/**
 A helper class for ptolemy.actor.lib.logic.Comparator.

 @author Thomas Mandl
 @version $Id$
 @since Ptolemy II 6.2
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
*/
public class Comparator extends PropertyConstraintHelper {
    /**
     * Construct an Comparator helper.
     * @param actor the associated actor
     */
    public Comparator(PropertyConstraintSolver solver, 
            ptolemy.actor.lib.logic.Comparator actor) throws IllegalActionException {
        super(solver, actor, false);
    }

    public List<Inequality> constraintList() throws IllegalActionException {
        ptolemy.actor.lib.logic.Comparator actor =
            (ptolemy.actor.lib.logic.Comparator) getComponent();
        
        Lattice lattice = (Lattice) getSolver().getLattice();                
        setEquals(actor.output, lattice.BOOLEAN);        

        return super.constraintList();
    }
}
