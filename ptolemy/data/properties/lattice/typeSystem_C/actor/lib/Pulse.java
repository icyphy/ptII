/* A helper class for ptolemy.actor.lib.Pulse.

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
package ptolemy.data.properties.lattice.typeSystem_C.actor.lib;

import java.util.List;

import ptolemy.data.ArrayToken;
import ptolemy.data.properties.lattice.PropertyConstraintSolver;
import ptolemy.data.properties.lattice.typeSystem_C.Lattice;
import ptolemy.data.properties.lattice.typeSystem_C.actor.AtomicActor;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// Pulse

/**
 A helper class for ptolemy.actor.lib.Const.

 @author Man-Kit Leung, Thomas Mandl
 @version $Id$
 @since Ptolemy II 6.2
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
*/
public class Pulse extends AtomicActor {

    /**
     * Construct a Const helper for the staticDynamic lattice.
     * This set a permanent constraint for the output port to
     * be STATIC, but does not use the default actor constraints.
     * @param actor The given Source actor
     * @param lattice The staticDynamic lattice.
     * @throws IllegalActionException 
     */
    public Pulse(PropertyConstraintSolver solver, 
            ptolemy.actor.lib.Pulse actor)
            throws IllegalActionException {

        super(solver, actor, false);
        _lattice = (Lattice) getSolver().getLattice();
        _actor = actor;
    }

    public List<Inequality> constraintList() throws IllegalActionException {
        ArrayToken valuesArray = (ArrayToken) _actor.values.getToken();
        
        setEquals(_actor.output, _lattice.leastUpperBound(
                        _lattice.convertJavaToCtype(valuesArray.getElement(0).getType(),valuesArray.getElement(0)),
                        _lattice.convertJavaToCtype(valuesArray.getElement(1).getType(),valuesArray.getElement(1))));

        return super.constraintList();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private ptolemy.actor.lib.Pulse _actor;
    private Lattice _lattice;
/*    
    protected List<Attribute> _getPropertyableAttributes() {
        ptolemy.actor.lib.Expression actor =
            (ptolemy.actor.lib.Expression) _component;

        List<Attribute> result = super._getPropertyableAttributes();
        result.add(actor.expression);
        return result;
    }
*/    
}
