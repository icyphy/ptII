/* An adapter class for ptolemy.actor.lib.logic.Comparator.

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
package ptolemy.data.ontologies.lattice.adapters.dimensionSystem.actor.lib.logic;

import java.util.List;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.lattice.LatticeOntologySolver;
import ptolemy.data.ontologies.lattice.adapters.dimensionSystem.actor.AtomicActor;
import ptolemy.data.properties.lattice.MonotonicFunction;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// Comparator

/**
 An adapter class for ptolemy.actor.lib.Comparator.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
*/
public class Comparator extends AtomicActor {

    /**
     * Construct a Comparator adapter for the flatUnitSystem lattice.
     * @param solver The given solver.
     * @param actor The given Comparator actor
     * @exception IllegalActionException
     */
    public Comparator(LatticeOntologySolver solver,
            ptolemy.actor.lib.logic.Comparator actor)
            throws IllegalActionException {

        super(solver, actor, false);
    }

    public List<Inequality> constraintList() throws IllegalActionException {
        ptolemy.actor.lib.logic.Comparator actor = (ptolemy.actor.lib.logic.Comparator) getComponent();

        setAtLeast(actor.output, new FunctionTerm(actor.left, actor.right));

        return super.constraintList();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    // This class implements a monotonic function of the input port
    // type. The result of the function is the same as the input type
    // if is not Complex; otherwise, the result is Double.
    private class FunctionTerm extends MonotonicFunction {

        TypedIOPort _left;
        TypedIOPort _right;

        public FunctionTerm(TypedIOPort left, TypedIOPort right) {
            _left = left;
            _right = right;
        }

        ///////////////////////////////////////////////////////////////
        ////                       public inner methods            ////

        /** Return the function result.
         *  @return A Property.
         * @exception IllegalActionException
         */
        public Object getValue() throws IllegalActionException {

            Concept leftProperty = getSolver().getConcept(_left);
            Concept rightProperty = getSolver().getConcept(_right);

            Concept unknown = _unknownConcept;

            if (leftProperty == unknown || rightProperty == unknown) {
                return unknown;
            }

            if (leftProperty == rightProperty) {
                return _dimensionlessConcept;
            }

            return _conflictConcept;
        }

        public boolean isEffective() {
            return true;
        }

        public void setEffective(boolean isEffective) {
        }

        protected InequalityTerm[] _getDependentTerms() {
            return new InequalityTerm[] { getPropertyTerm(_left),
                    getPropertyTerm(_right) };
        }
    }
}
