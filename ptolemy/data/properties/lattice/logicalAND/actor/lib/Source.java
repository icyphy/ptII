/* A helper class for ptolemy.actor.lib.Const.

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

import java.util.Iterator;
import java.util.List;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.properties.lattice.PropertyConstraintSolver;
import ptolemy.data.properties.lattice.PropertyConstraintSolver.ConstraintType;
import ptolemy.data.properties.lattice.logicalAND.Lattice;
import ptolemy.data.properties.lattice.logicalAND.actor.AtomicActor;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// Const

/**
 A helper class for ptolemy.actor.lib.Source.

 @author Man-Kit Leung, Thomas Mandl
 @version $Id: Const.java,v 1.1 2007/06/26 16:48:54 mankit Exp $
 @since Ptolemy II 6.2
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
*/
public class Source extends AtomicActor {

    /**
     * Construct a Const helper for the staticDynamic lattice.
     * This set a permanent constraint for the output port to
     * be STATIC, but does not use the default actor constraints.
     * @param actor The given Source actor
     * @param lattice The staticDynamic lattice.
     * @throws IllegalActionException 
     */
    public Source(PropertyConstraintSolver solver, 
            ptolemy.actor.lib.Source actor)
            throws IllegalActionException {

        super(solver, actor, false);
        _actor = actor;
        _lattice = (Lattice) getSolver().getLattice();        
     }

    public Source(PropertyConstraintSolver solver, 
            ptolemy.actor.lib.Source actor,
            boolean useDefaultConstraints)
            throws IllegalActionException {

        super(solver, actor, useDefaultConstraints);
        _actor = actor;
        _lattice = (Lattice) getSolver().getLattice();        
     }
    
    public List<Inequality> constraintList() 
    throws IllegalActionException {
        // add default constraints if no constraints specified in actor helper 
        
        if (_ownConstraints.isEmpty()) {
            // force outputs to FALSE by default; overwrite in actor specific helper class
            setAtLeast(_actor.output, _lattice.FALSE);        
        }
        
        return super.constraintList();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private ptolemy.actor.lib.Source _actor;
    private Lattice _lattice;

    protected void _setEffectiveTerms() {
        Iterator ports = _actor.portList().iterator();        
        while (ports.hasNext()) {                    
            TypedIOPort port = (TypedIOPort) ports.next();
            if ((port.numLinks() <= 0) &&
                (port.isInput()) && 
                (interconnectConstraintType == ConstraintType.SINK_EQUALS_GREATER)) {
                    
                getPropertyTerm(port).setEffective(false);
            }
        }
        
    }
}