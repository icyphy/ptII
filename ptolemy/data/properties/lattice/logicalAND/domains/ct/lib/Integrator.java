/*  An adapter class for ptolemy.domains.ct.lib.Integrator.

 Copyright (c) 2006-2010 The Regents of the University of California.
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
package ptolemy.data.properties.lattice.logicalAND.domains.ct.lib;

import java.util.List;

import ptolemy.data.properties.lattice.PropertyConstraintSolver;
import ptolemy.data.properties.lattice.logicalAND.actor.AtomicActor;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// Integrator

/**
 An adapter class for ptolemy.domains.ct.lib.Integrator.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
*/
public class Integrator extends AtomicActor {

    /**
     * Construct a Integrator adapter for the logicalAND ontology.
     * @param solver The given solver.
     * @param actor The given Integrator actor
     * @exception IllegalActionException Thrown if the
     *  super class throws it.
     */
    public Integrator(PropertyConstraintSolver solver,
            ptolemy.domains.ct.lib.Integrator actor)
            throws IllegalActionException {

        super(solver, actor, false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return the list of constraints that specifies the analysis
     * for the Integrator actor. This analysis is conservative. It
     * always sets the output to FALSE, which is the top of the
     * logicalAND ontology lattice.
     * @return The list of constraints.
     * @exception IllegalActionException Thrown if an error
     *  occurs when getting the FALSE element from the lattice
     *  or the super class throws it.
     */
    public List<Inequality> constraintList() throws IllegalActionException {

        ptolemy.domains.ct.lib.Integrator actor = (ptolemy.domains.ct.lib.Integrator) getComponent();

        setAtLeast(actor.output, _lattice.getElement("FALSE"));

        return super.constraintList();
    }
}
