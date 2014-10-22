/* A concept that represents the monotoncity of an expression.
 *
 * Copyright (c) 2010-2014 The Regents of the University of California. All
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
package ptolemy.data.ontologies.lattice.adapters.monotonicityAnalysis;

import java.util.Set;

import ptolemy.actor.gui.ColorAttribute;
import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.ConceptGraph;
import ptolemy.data.ontologies.FiniteConcept;
import ptolemy.data.ontologies.FlatTokenInfiniteConcept;
import ptolemy.data.ontologies.MapTypeInfiniteConcept;
import ptolemy.data.ontologies.Ontology;
import ptolemy.graph.CPO;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;

/** A concept that represents the monotoncity of an expression.
 *
 *  Note that for an arbitrary expression, it will not have a
 *  monotonicity concept of simply Monotonic, Constant, etc.
 *  Rather, the expression will have a monotonicity that depends
 *  on it's free variables.  For example, an expression of the form
 *  <code>
 *    (x &lt;= Concept1) ? Bottom :
 *    (y &lt;= Concept2) ? Top :
 *    Concept1
 *  </code>
 *  may have a monotonicity that is monotonic with respect to the
 *  variable x, but antimonotonic with respect to y (and constant
 *  with respect to all other variables).
 *
 *  This class represents exactly such concepts, representing them as
 *  {x = Monotonic, y = Animonotonic}, in a manner and syntax
 *  similar to records of the Ptolemy II type system.  In records,
 *  however, accessing an undefined tag is an error, whereas in
 *  expressions, they are simply constant with respect to any
 *  variables that are not free.
 *
 *  @author Ben Lickly
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (blickly)
 *  @Pt.AcceptedRating Red (blickly)
 *
 */
public class MonotonicityConcept extends MapTypeInfiniteConcept<Concept> {

    ///////////////////////////////////////////////////////////////////
    ////             public constructors/factories                 ////

    /** Create a new monotonicity concept, belonging to the given
     *  ontology, with an automatically generated name.
     *
     *  @param ontology The finite ontology to which this belongs.
     *    This should be the 4 element monotonicity lattice if we
     *    are really going to be doing inference on monotonicity
     *    of expressions.
     *  @return The newly created MonotonicityConcept.
     */
    public static MonotonicityConcept createMonotonicityConcept(
            Ontology ontology) {
        try {
            return new MonotonicityConcept(ontology);
        } catch (NameDuplicationException e) {
            throw new InternalErrorException(
                    "Name conflict with automatically generated infinite"
                            + " concept name. This should never happen.\n"
                            + "Original exception:" + e.toString());
        } catch (IllegalActionException e) {
            throw new InternalErrorException(
                    "There was an error creating a new MonotonicityConcept"
                            + "in the " + ontology + "ontology.\n"
                            + "Original exception:" + e.toString());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Compare this concept with the given concept.
     *  Returns an int value that corresponds to the ordering between
     *  the elements as given in the CPO interface.
     *
     *  @param concept The concept with which we are comparing.
     *  @return CPO.HIGHER if this concept is above the given concept,
     *          CPO.LOWER if this concept is below the given concept,
     *          CPO.SAME if both concepts are the same,
     *      and CPO.INCOMPARABLE if concepts are incomparable.
     *  @exception IllegalActionException If the specified concept
     *          does not have the same ontology as this one.
     *  @see ptolemy.data.ontologies.Concept#isAboveOrEqualTo(ptolemy.data.ontologies.Concept)
     */
    @Override
    public int compare(Concept concept) throws IllegalActionException {
        if (!concept.getOntology().equals(getOntology())) {
            throw new IllegalActionException(this,
                    "Attempt to compare elements from two distinct ontologies");
        }

        // Original bottom and top remain bottom and top.
        if (concept.equals(getOntology().getConceptGraph().bottom())) {
            return CPO.HIGHER;
        } else if (concept.equals(getOntology().getConceptGraph().top())) {
            return CPO.LOWER;
        } else if (concept instanceof MonotonicityConcept) {
            MonotonicityConcept righthandSide = (MonotonicityConcept) concept;
            CPO graph = getOntology().getConceptGraph();
            Set<String> keys = this._combinedKeys(righthandSide);

            boolean seenHigher = false;
            boolean seenLower = false;
            boolean seenIncomparable = false;
            for (String key : keys) {
                int result = graph.compare(getMonotonicity(key),
                        righthandSide.getMonotonicity(key));
                switch (result) {
                case CPO.HIGHER:
                    seenHigher = true;
                    break;
                case CPO.LOWER:
                    seenLower = true;
                    break;
                case CPO.INCOMPARABLE:
                    seenIncomparable = true;
                    break;
                case CPO.SAME:
                    break;
                default:
                    throw new IllegalActionException(
                            this,
                            "ConceptGraph compare "
                                    + "did not return one of the defined CPO values. "
                                    + "Return value was " + result
                                    + ". This should " + "never happen.");
                }
            }
            if (!seenHigher && !seenLower && !seenIncomparable) {
                return CPO.SAME;
            } else if (seenHigher && !seenLower && !seenIncomparable) {
                return CPO.HIGHER;
            } else if (seenLower && !seenHigher && !seenIncomparable) {
                return CPO.LOWER;
            } else {
                return CPO.INCOMPARABLE;
            }
        } else {
            return CPO.INCOMPARABLE;
        }
    }

    /** Return the correct color for this monotonicity concept by looking
     *  at the color of the finite monotonicity representative.
     *
     *  @return A ColorAttribute corresponding to the highlight color of
     *   this monotonicity concept.
     *  @exception IllegalActionException Thrown if there is an error getting
     *   the color for the finite monotonicity concept.
     */
    @Override
    public ColorAttribute getColor() throws IllegalActionException {
        return _toFiniteMonotonicity().getColor();
    }

    /** Get the monotonicity of this concept with respect to a specific
     *  variable.  While the overall monotonicity of an expression
     *  cannot be represented so simply, the monotonicity with
     *  respect to a single variable can be represented as one
     *  of:
     *  <ul>
     *    <li>Constant</li>
     *    <li>Monotonic</li>
     *    <li>Antimonotonic</li>
     *    <li>General</li>
     *    <li>NonMonotonic_{Counterexamples}</li>
     *    <li>NonAntimonotonic_{Counterexamples}</li>
     *  </ul>
     *  This method returns one these concepts.
     *  @param variableName The variable whose monotonicity we are querying.
     *  @return The monotonicity of this concept with respect to the given
     *    variable; one of Constant, Monotonic, Antimonotonic, or General.
     */
    public Concept getMonotonicity(String variableName) {
        return getConcept(variableName);
    }

    /** Compute the greatest lower bound (GLB) of this and another concept.
     *
     *  @param concept The other concept.
     *  @return The concept that is the GLB of this and the given concept.
     */
    // FIXME: GLB method needs to be implemented.
    @Override
    public Concept greatestLowerBound(Concept concept) {
        throw new IllegalArgumentException("greatestLowerBound method not "
                + "implemented.");
    }

    /** Compute the least upper bound (LUB) of this and another concept.
     *
     *  @param concept The other concept.
     *  @return The concept that is the LUB of this and the given concept.
     */
    @Override
    public Concept leastUpperBound(Concept concept) {
        Concept top = getOntology().getConceptGraph().top();
        if (!(concept instanceof MonotonicityConcept)) {
            Concept bottom = getOntology().getConceptGraph().bottom();
            if (concept.equals(bottom)) {
                return this;
            } else if (this.equals(bottom)) {
                return concept;
            } else {
                return top;
            }
        } else {
            // We have two MonotonicityConcepts
            return _leastUpperBound((MonotonicityConcept) concept);
        }
    }

    /** Set the monotonicity of this concept with respect to a specific
     *  variable.
     *
     *  @param variable The variable whose monotonicity we are querying.
     *  @param monotonicity The monotonicity of this concept with respect
     *    to the given variable.
     *  @see MonotonicityConcept#getMonotonicity(String)
     */
    public void putMonotonicity(String variable, Concept monotonicity) {
        putConcept(variable, monotonicity);
    }

    ///////////////////////////////////////////////////////////////////
    ////                    protected constructors                 ////

    /** Create a new Monotonicity concept, belonging to the given
     *  ontology.
     *
     *  @param ontology The finite ontology to which this belongs.
     *    This should be the 4 element monotonicity lattice if we
     *    are really going to be doing inference on monotonicity
     *    of expressions.
     *  @exception NameDuplicationException Should never be thrown.
     *  @exception IllegalActionException If the base class throws it.
     */
    protected MonotonicityConcept(Ontology ontology)
            throws IllegalActionException, NameDuplicationException {
        super(ontology, ontology.getConceptGraph().bottom());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Compute the least upper bound (LUB) of this and another monotonicity concept.
     *
     *  @param concept The other monotonicity concept
     *  @return The concept that is the LUB of this and the given concept.
     */
    private Concept _leastUpperBound(MonotonicityConcept concept) {
        MonotonicityConcept result = createMonotonicityConcept(getOntology());

        Set<String> allKeys = this._combinedKeys(concept);
        for (String variableName : allKeys) {
            CPO graph = this.getOntology().getConceptGraph();
            Concept monotonicity = (Concept) graph.leastUpperBound(
                    this.getMonotonicity(variableName),
                    concept.getMonotonicity(variableName));
            result.putMonotonicity(variableName, monotonicity);
        }
        return result;
    }

    /** Return the finite monotonicity concept that best represents the
     *  overall monotonicity of this infinite concept.  Currently, this
     *  simply takes the least upper bound of the monotonicity of all the
     *  free variables referenced in this monotonicity concept, with the
     *  empty monotonicity concept evaluating to constant.
     *
     *  So, for example the finite monotonicity of
     *   {x = Monotonic, y = Monotonic}
     *  is Monotonic, of
     *   {x = Monotonic, y = Antimonotonic}
     *  is General, of
     *   {}
     *  is Constant, etc.
     *
     *  @return The finite monotonicity concept that represents the overall
     *   behavior of this infinite monotonicity concept.
     */
    private FiniteConcept _toFiniteMonotonicity() {
        ConceptGraph monotonicityLattice = getOntology().getConceptGraph();
        FiniteConcept result = (FiniteConcept) monotonicityLattice.bottom();
        for (String var : keySet()) {
            Concept c = getMonotonicity(var);
            Concept lub = monotonicityLattice.leastUpperBound(result, c);
            if (lub instanceof FlatTokenInfiniteConcept) {
                result = ((FlatTokenInfiniteConcept) lub).getRepresentative();
            } else {
                result = (FiniteConcept) lub;
            }
        }
        return result;
    }

}
