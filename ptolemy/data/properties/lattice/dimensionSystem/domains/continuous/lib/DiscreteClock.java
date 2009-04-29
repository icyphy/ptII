/* A helper class for ptolemy.domains.continuous.lib.DiscreteClock.

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
package ptolemy.data.properties.lattice.dimensionSystem.domains.continuous.lib;

import java.util.List;

import ptolemy.data.properties.lattice.PropertyConstraintHelper;
import ptolemy.data.properties.lattice.PropertyConstraintSolver;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// DiscreteClock

/**
 A helper class for ptolemy.domains.continuous.lib.DiscreteClock.

 @author Charles Shelton
 @version $Id: DiscreteClock.java 53046 2009-04-10 23:04:25Z cxh $
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
*/
public class DiscreteClock extends PropertyConstraintHelper {

    /**
     * Construct a Integrator helper for the flatUnitSystem lattice.
     * @param solver The given solver.
     * @param actor The given Integrator actor
     * @exception IllegalActionException
     */
    public DiscreteClock(PropertyConstraintSolver solver,
            ptolemy.domains.continuous.lib.DiscreteClock actor)
            throws IllegalActionException {

        super(solver, actor, false);
        
        _actor = actor;
    }

    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public List<Inequality> constraintList()
            throws IllegalActionException {
        setAtLeast(_actor.output, _lattice.getElement("UNITLESS"));

        return super.constraintList();
    }
    
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    
    protected List<Attribute> _getPropertyableAttributes() {
        List<Attribute> result = super._getPropertyableAttributes();
        result.remove(_actor.trigger);
        result.remove(_actor.period);
        
        return result;
    }
    
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private ptolemy.domains.continuous.lib.DiscreteClock _actor;
}
