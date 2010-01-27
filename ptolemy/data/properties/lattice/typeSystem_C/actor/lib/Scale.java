/* An adapter class for ptolemy.actor.lib.Scale

 Copyright (c) 2008-2010 The Regents of the University of California.
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
package ptolemy.data.properties.lattice.typeSystem_C.actor.lib;

import java.util.List;

import ptolemy.data.properties.lattice.PropertyConstraintSolver;
import ptolemy.data.properties.lattice.typeSystem_C.actor.AtomicActor;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// Scale

/**
 An adapter class for ptolemy.actor.lib.Scale.

 @author Man-Kit Leung, Thomas Mandl
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
*/
public class Scale extends AtomicActor {
    /**
     * Construct the Scale property constraint adapter associated
     * with the given component and solver. The constructed adapter
     * implicitly uses the default constraints set by the solver.
     * @param solver The given solver.
     * @param actor The given Scale actor
     * @exception IllegalActionException If the adapter cannot be
     * initialized in the superclass.
     */
    public Scale(PropertyConstraintSolver solver, ptolemy.actor.lib.Scale actor)
            throws IllegalActionException {

        super(solver, actor, false);
        _actor = actor;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return the constraints of this component. The constraints are a list of
     * inequalities.
     * @return The constraints of this component.
     * @exception IllegalActionException If thrown while manipulating the lattice
     * or getting the solver.
     */
    public List<Inequality> constraintList() throws IllegalActionException {
        setAtLeast(_actor.output, _actor.factor);
        setAtLeast(_actor.output, _actor.input);

        return super.constraintList();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private ptolemy.actor.lib.Scale _actor;
}
