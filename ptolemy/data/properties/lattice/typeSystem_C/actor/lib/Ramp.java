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
package ptolemy.data.properties.lattice.typeSystem_C.actor.lib;

import java.util.List;

import ptolemy.data.properties.lattice.PropertyConstraintSolver;
import ptolemy.data.properties.lattice.typeSystem_C.Lattice;
import ptolemy.data.properties.lattice.typeSystem_C.actor.AtomicActor;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// Ramp

/**
 A helper class for ptolemy.actor.lib.Ramp.

 @author Thomas Mandl, Man-Kit Leung
 @version $Id$
 @since Ptolemy II 6.2
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
*/
public class Ramp extends AtomicActor {
    /**
     * Construct an Ramp helper.
     * @param actor the associated actor
     * @throws IllegalActionException 
     */
    public Ramp(PropertyConstraintSolver solver, 
            ptolemy.actor.lib.Ramp actor) throws IllegalActionException {

        super(solver, actor);
        _lattice = (Lattice) getSolver().getLattice();
        _actor = actor;
   }       

    public List<Inequality> constraintList() throws IllegalActionException {
//        setAtLeast(actor.output, actor.init);         
//        setAtLeast(actor.output, actor.step);
//        // FIXME: Is this the right thing to do???
//        //        How do we make sure that Ptolemy and EDC type systems are consistent?
//        
//        // this does not work!
//        setAtLeast(actor.output, lattice.getEDCtype(actor.output.getType(), null));         

//FIXME: consider firingCountLimit for output type        
        setEquals(_actor.output, _lattice.convertJavaToCtype(_actor.output.getType(), null));         
        return super.constraintList();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private ptolemy.actor.lib.Ramp _actor;
    private Lattice _lattice;
}

