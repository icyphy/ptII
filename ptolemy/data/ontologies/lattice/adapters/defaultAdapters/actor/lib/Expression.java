/* The default adapter class for ptolemy.actor.lib.Expression.

 Copyright (c) 2006-2014 The Regents of the University of California.
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
package ptolemy.data.ontologies.lattice.adapters.defaultAdapters.actor.lib;

import java.util.List;

import ptolemy.data.ontologies.lattice.LatticeOntologyAdapter;
import ptolemy.data.ontologies.lattice.LatticeOntologySolver;
import ptolemy.graph.Inequality;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// Expression

/** The default adapter class for ptolemy.actor.lib.Expression.
 *
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public class Expression extends LatticeOntologyAdapter {

    /** Construct a default lattice ontology adapter for the Expression actor.
     *  @param solver The given solver.
     *  @param actor The given Expression actor.
     *  @exception IllegalActionException If the adapter cannot be initialized.
     */
    public Expression(LatticeOntologySolver solver,
            ptolemy.actor.lib.Expression actor) throws IllegalActionException {
        super(solver, actor, false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the list of constraints for the Expression actor. The only
     *  constraint specified is that the ontology concept applied to the output
     *  port of the actor must be greater than or equal to the concept applied
     *  to the expression evaluated in the actor.
     *  @return The list of constraints for this adapter.
     *  @exception IllegalActionException If there is an error creating
     *   the constraint list.
     */
    @Override
    public List<Inequality> constraintList() throws IllegalActionException {
        ptolemy.actor.lib.Expression actor = (ptolemy.actor.lib.Expression) getComponent();
        setAtLeast(actor.output, actor.expression);
        return super.constraintList();
    }
}
