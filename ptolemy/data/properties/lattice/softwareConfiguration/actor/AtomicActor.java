/* An adapter class for ptolemy.actor.AtomicActor.

 Copyright (c) 2009-2010 The Regents of the University of California.
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
package ptolemy.data.properties.lattice.softwareConfiguration.actor;

import ptolemy.data.properties.lattice.PropertyConstraintHelper;
import ptolemy.data.properties.lattice.PropertyConstraintSolver;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// AddSubtract

/**
 An adapter class for ptolemy.actor.AtomicActor.

 @author Man-Kit Leung, Thomas Mandl
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class AtomicActor extends PropertyConstraintHelper {
    /**
     * Construct the AtomicActor property constraint adapter associated
     * with the given component and solver. The constructed adapter
     * implicitly uses the default constraints set by the solver.
     * @param solver The associated solver.
     * @param actor The associated actor.
     * @exception IllegalActionException If thrown by the super class.
     */
    public AtomicActor(PropertyConstraintSolver solver,
            ptolemy.actor.AtomicActor actor) throws IllegalActionException {
        super(solver, actor);
    }

    /**
     * Construct the AtomicActor property constraint adapter for the
     * given component and property lattice.
     * @param solver The associated solver.
     * @param actor The associated actor.
     * @param useDefaultConstraints Indicate whether this adapter
     *  uses the default actor constraints.
     * @exception IllegalActionException If thrown by the super class.
     */
    public AtomicActor(PropertyConstraintSolver solver,
            ptolemy.actor.AtomicActor actor, boolean useDefaultConstraints)
            throws IllegalActionException {
        super(solver, actor, useDefaultConstraints);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return true if this property term is effective.
     * @return Always return true in this base class.
     * @see #setEffective(boolean)
     */
    public boolean isEffective() {
        return true;
    }

    /**
     * Set the effectiveness of this property term to the specified value. Do
     * nothing in this base by default.
     * @param isEffective The specified effective value.
     */
    public void setEffective(boolean isEffective) {
    }
}
