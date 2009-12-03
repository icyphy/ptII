/* A property constraint adapter for composite actor.

 Copyright (c) 2008-2009 The Regents of the University of California.
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
package ptolemy.data.properties.lattice.typeSystem_C.kernel;

import ptolemy.data.properties.lattice.PropertyConstraintCompositeHelper;
import ptolemy.data.properties.lattice.PropertyConstraintSolver;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// CompositeActor

/**
 A property constraint adapter for composite actor.

 @author Man-Kit Leung, Thomas Mandl
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class CompositeEntity extends PropertyConstraintCompositeHelper {

    /**
     * Construct a property constraint adapter for the given
     * CompositeActor. This is the adapter class for any
     * CompositeActor that does not have a specific defined
     * adapter class.
     * @param solver The given solver.
     * @param entity The given CompositeEntity.
     * @exception IllegalActionException
     */
    public CompositeEntity(PropertyConstraintSolver solver,
            ptolemy.kernel.CompositeEntity entity)
            throws IllegalActionException {

        super(solver, entity);
    }

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
     * @param isEffective The specified effective value, not used by this class.
     * @see #isEffective()
     */
    public void setEffective(boolean isEffective) {
    }
}
