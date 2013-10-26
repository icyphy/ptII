/* A simple adapter for expressions that just connects the output port to the
 *  overall expression result.
 *
 * Copyright (c) 2010-2011 The Regents of the University of California. All
 * rights reserved.
 *
 * Permission is hereby granted, without written agreement and without license
 * or royalty fees, to use, copy, modify, and distribute this software and its
 * documentation for any purpose, provided that the above copyright notice and
 * the following two paragraphs appear in all copies of this software.
 *
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN
 * "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 */
package ptolemy.data.ontologies.lattice.adapters.monotonicityAnalysis.actor.lib;

import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.data.ontologies.lattice.LatticeOntologyAdapter;
import ptolemy.data.ontologies.lattice.LatticeOntologySolver;
import ptolemy.graph.Inequality;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;

/** A simple adapter for expressions that just connects the output port to the
 *  overall expression result.
 *
 *  @author Ben Lickly
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (blickly)
 *  @Pt.AcceptedRating Red (blickly)
 */
public class Expression extends LatticeOntologyAdapter {

    /** Return the list of conceptables with the input ports omitted.
     *  Since the new monotonicity analysis gives the output monotonicity
     *  in terms of the free variables, there is no need to infer concepts
     *  for the input variables, and in fact doing so may be confusing, since
     *  those values are ignored by the expression actor.
     *
     *  @return A list of conceptable objects with the input ports removed.
     *  @see ptolemy.data.ontologies.OntologyAdapter#getPropertyables()
     */
    public List<Object> getPropertyables() {
        List<Object> originalConceptables = super.getPropertyables();
        List<Object> newConceptables = new LinkedList<Object>();
        for (Object conceptable : originalConceptables) {
            if (conceptable instanceof IOPort
                    && ((IOPort) conceptable).isInput()) {
                continue;
            } else {
                newConceptables.add(conceptable);
            }

        }
        return newConceptables;
    }

    /** Construct a Expression adapter for the monotonicityAnalysis lattice.
     *  @param solver The given solver.
     *  @param actor The given Expression actor
     *  @exception IllegalActionException If the parent throws it.
     */
    public Expression(LatticeOntologySolver solver,
            ptolemy.actor.lib.Expression actor) throws IllegalActionException {

        super(solver, actor, false);
    }

    /** Return the list of constraints for this actor, including the
     *  constraint between the expression actor's output port and the
     *  evaluated expression.
     *  @return The list of constraints.
     *  @exception IllegalActionException If the parent throws it.
     *  @see ptolemy.data.ontologies.lattice.LatticeOntologyAdapter#constraintList()
     */
    public List<Inequality> constraintList() throws IllegalActionException {
        ptolemy.actor.lib.Expression actor = (ptolemy.actor.lib.Expression) getComponent();

        if (((LatticeOntologySolver) _solver).isLeastFixedPoint()) {
            setAtLeast(actor.output, actor.expression);
        } else {
            setAtMost(actor.output, actor.expression);
        }
        return super.constraintList();
    }

    /** Return the actor's expression, as it is the only propertyable
     *  attribute inside the expression actor.
     *  @return A list with just the expression.
     *  @see ptolemy.data.ontologies.OntologyAdapter#_getPropertyableAttributes()
     */
    protected List<Attribute> _getPropertyableAttributes() {
        List<Attribute> result = new LinkedList<Attribute>();

        ptolemy.actor.lib.Expression actor = (ptolemy.actor.lib.Expression) getComponent();
        result.add(actor.expression);
        return result;
    }

}
