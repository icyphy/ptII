/* A helper class for ptolemy.actor.lib.Sink.

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
package ptolemy.data.properties.lattice.logicalOrBackward.actor.lib;

import java.util.Iterator;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.properties.lattice.PropertyConstraintSolver;
import ptolemy.data.properties.lattice.PropertyConstraintSolver.ConstraintType;
import ptolemy.data.properties.lattice.logicalOrBackward.actor.AtomicActor;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// Const

/**
 A helper class for ptolemy.actor.lib.Sink.

 @author Man-Kit Leung, Thomas Mandl
 @version $Id: Const.java,v 1.1 2007/06/26 16:48:54 mankit Exp $
 @since Ptolemy II 6.2
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
*/
public class Sink extends AtomicActor {

    /**
     * Construct a Const helper for the staticDynamic lattice.
     * This set a permanent constraint for the output port to
     * be STATIC, but does not use the default actor constraints.
     * @param actor The given Source actor
     * @param lattice The staticDynamic lattice.
     * @throws IllegalActionException 
     */
    public Sink(PropertyConstraintSolver solver, 
            ptolemy.actor.lib.Sink actor)
            throws IllegalActionException {

        super(solver, actor, false);
     }

    public Sink(PropertyConstraintSolver solver, 
            ptolemy.actor.lib.Sink actor,
            boolean useDefaultConstraints)
            throws IllegalActionException {

        super(solver, actor, useDefaultConstraints);
     }
    
    protected void _setEffectiveTerms() {
        Entity actor = (Entity) getComponent();

        Iterator ports = actor.portList().iterator();        
        while (ports.hasNext()) {                    
            TypedIOPort port = (TypedIOPort) ports.next();
            if ((port.numLinks() <= 0) &&
                (port.isOutput()) && 
                (interconnectConstraintType == ConstraintType.SRC_EQUALS_GREATER)) {
                    
                 getPropertyTerm(port).setEffective(false);
            }
        }
        
    }
}