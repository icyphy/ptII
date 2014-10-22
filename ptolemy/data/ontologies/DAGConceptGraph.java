/* A data structure representing DAGs of concepts in an ontology.
 *
 * Copyright (c) 2007-2014 The Regents of the University of California. All
 * rights reserved. Permission is hereby granted, without written agreement and
 * without license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the above
 * copyright notice and the following two paragraphs appear in all copies of
 * this software.
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
 *
 * PT_COPYRIGHT_VERSION_2 COPYRIGHTENDKEY
 *
 */
package ptolemy.data.ontologies;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ptolemy.graph.CPO;
import ptolemy.graph.DirectedAcyclicGraph;
import ptolemy.graph.Graph;
import ptolemy.graph.NonLatticeCounterExample;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// DAGConceptGraph

/** A data structure representing relationships of ontologies whose structure
 *  can be represented as a directed acyclic graph of concepts.
 *  This corresponds directly to the subset of ontologies that users can
 *  construct in the Ontology Editor.
 *
 *  @author Thomas Mandl, Man-Kit Leung, Edward A. Lee, Ben Lickly, Dai Bui, Christopher Brooks
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (mankit)
 *  @Pt.AcceptedRating Red (mankit)
 *  @see ptolemy.data.ontologies.ConceptGraph
 */
public class DAGConceptGraph extends ConceptGraph {

    /** Create an empty concept graph with no concepts in it.
     */
    public DAGConceptGraph() {
        _dag = new DirectedAcyclicGraph();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a concept to this concept graph.
     *  @param concept The concept.
     *  @exception IllegalArgumentException If the concept we are trying to
     *   add is already contained in this concept graph.
     */
    public void addConcept(FiniteConcept concept) {
        if (!_dag.containsNodeWeight(concept)) {
            _dag.addNodeWeight(concept);
        } else {
            throw new IllegalArgumentException("Cannot add concept " + concept
                    + " as it is already contained in this concept graph.");
        }
    }

    /** Add a relation between two Concepts as an edge to the graph.
     *
     *  @param concept1 The source concept
     *  @param concept2 The sink concept
     *  @param conceptRelation The ConceptRelation between the two concepts
     *   concept1 and concept2.
     */
    public void addRelation(FiniteConcept concept1, FiniteConcept concept2,
            ConceptRelation conceptRelation) {
        _dag.addEdge(concept1, concept2, conceptRelation);
    }

    /** Return the least element of this concept graph.
     *  @return The least element of this graph.
     */
    @Override
    public Concept bottom() {
        return (Concept) _dag.bottom();
    }

    /** Return a list of the concepts which are not acceptable, but are also
     *  not at the top of the lattice, as required for non-acceptable concepts.
     *
     *  @return A list of concepts erroneously marked as not acceptable, or an
     *   empty list if there are no errors.
     */
    public List<Concept> checkUnacceptableConcepts() {
        Set<Concept> invalidConcepts = new HashSet<Concept>();
        // Not sure why this doesn't return a unique set
        for (Object o : Graph.weightArray(_dag.nodes())) {
            if (o instanceof FiniteConcept) {
                FiniteConcept c = (FiniteConcept) o;
                if (!c.isValueAcceptable()) {
                    for (Concept aboveConcept : c.getCoverSetAbove()) {
                        if (aboveConcept.isValueAcceptable()) {
                            invalidConcepts.add(c);
                        }
                    }
                }
            }
        }
        return new LinkedList<Concept>(invalidConcepts);
    }

    /** Compare two concepts in the ontology. The arguments must be
     *  instances of {@link Concept}, otherwise an exception will be thrown.
     *  This method returns one of ptolemy.graph.CPO.LOWER, ptolemy.graph.CPO.SAME,
     *  ptolemy.graph.CPO.HIGHER, ptolemy.graph.CPO.INCOMPARABLE, indicating the
     *  first argument is lower than, equal to, higher than, or incomparable with
     *  the second argument in the property hierarchy, respectively.
     *
     *  @param e1 An instance of {@link Concept}.
     *  @param e2 An instance of {@link Concept}.
     *  @return One of CPO.LOWER, CPO.SAME, CPO.HIGHER, CPO.INCOMPARABLE.
     *  @exception IllegalArgumentException If one or both arguments are not
     *   instances of {@link Concept}.
     */
    @Override
    public int compare(Object e1, Object e2) {
        Concept concept1 = _getInputObjectAsAConcept(e1);
        Concept concept2 = _getInputObjectAsAConcept(e2);

        if (concept1 instanceof FiniteConcept
                && concept2 instanceof FiniteConcept) {
            return _dag.compare(concept1, concept2);
        } else if (concept1 instanceof InfiniteConcept) {
            try {
                return ((InfiniteConcept) concept1).compare(concept2);
            } catch (IllegalActionException e) {
                return CPO.INCOMPARABLE;
            }
        } else if (concept2 instanceof InfiniteConcept) {
            try {
                int oppositeResult = ((InfiniteConcept) concept2)
                        .compare(concept1);
                return DirectedAcyclicGraph.reverseCompareCode(oppositeResult);
            } catch (IllegalActionException e) {
                return CPO.INCOMPARABLE;
            }
        } else { // This case should never happen.
            throw new IllegalArgumentException("Invalid concepts '" + e1
                    + "' and '" + e2 + "' (neither finite nor infinite)");
        }
    }

    /** Compute the down-set of an element in this concept graph.
     *  The down-set of an element is the subset consisting of
     *  all the elements less than or equal to the specified element.
     *
     *  @param e An Object representing an element in this concept graph.
     *  @return An array of Concepts of the down-set of the
     *   specified element.
     *  @exception IllegalArgumentException If the specified Object is not
     *   an element in this concept graph, or the resulting set is infinite.
     */
    @Override
    public Concept[] downSet(Object e) {
        // FIXME: What happens if the downSet should contain some
        // InfiniteConcepts that are lower in the lattice?
        if (e instanceof FiniteConcept) {
            Object[] set = _dag.downSet(e);
            Concept[] downSet = new Concept[set.length];

            for (int i = 0; i < set.length; i++) {
                downSet[i] = (Concept) set[i];
            }

            return downSet;
        } else {
            // TODO: Implement downSet for InfiniteConcepts.
            throw new IllegalArgumentException("downSet method not implemented"
                    + " for Concept subclass " + e.getClass().getName() + ".");
        }
    }

    /** Compute the greatest lower bound (GLB) of two elements.
     *  The GLB of two elements is the greatest element in the concept graph
     *  that is less than or equal to both of the two elements.
     *
     *  @param e1 An Object representing an element in this concept graph.
     *  @param e2 An Object representing an element in this concept graph.
     *  @return A Concept representing the GLB of the two specified
     *   elements, or <code>null</code> if the GLB does not exist.
     *  @exception IllegalArgumentException If at least one of the
     *   specified Objects is not an element of this concept graph.
     */
    @Override
    public Concept greatestLowerBound(Object e1, Object e2) {
        Concept concept1 = _getInputObjectAsAConcept(e1);
        Concept concept2 = _getInputObjectAsAConcept(e2);

        return _getBoundForConcepts(concept1, concept2, BoundType.GREATESTLOWER);
    }

    /** Return why this concept graph is not a lattice, or null if it is.
     *  Should be null for all existing concept graphs.
     *  @return Null, if the concept graph is a lattice.
     */
    @Override
    public NonLatticeCounterExample nonLatticeReason() {
        return _dag.nonLatticeReason();
    }

    /** Compute the least upper bound (LUB) of two elements.
     *  The LUB of two elements is the least element in the concept graph
     *  that is greater than or equal to both of the two elements.
     *
     *  @param e1 An Object representing an element in this concept graph.
     *  @param e2 An Object representing an element in this concept graph.
     *  @return A Concept representing the LUB of the two specified
     *   elements, or <code>null</code> if the LUB does not exist.
     *  @exception IllegalArgumentException If at least one of the
     *   specified Objects is not an element of this concept graph.
     */
    @Override
    public Concept leastUpperBound(Object e1, Object e2) {
        Concept concept1 = _getInputObjectAsAConcept(e1);
        Concept concept2 = _getInputObjectAsAConcept(e2);

        return _getBoundForConcepts(concept1, concept2, BoundType.LEASTUPPER);
    }

    /** Return the greatest element in this concept graph.
     *  @return The greatest element in this concept graph.
     */
    @Override
    public Concept top() {
        return (Concept) _dag.top();
    }

    /** Compute the up-set of an element in this concept graph.
     *  The up-set of an element is the subset consisting of
     *  all the elements greater than or equal to the specified element.
     *
     *  @param e An Object representing an element in this concept graph.
     *  @return An array of Concepts of the up-set of the
     *   specified element.
     *  @exception IllegalArgumentException Always thrown.
     */
    @Override
    public Concept[] upSet(Object e) {
        // FIXME: What happens if the upSet should contain some InfiniteConcepts
        // that are higher in the lattice?
        if (e instanceof FiniteConcept) {
            Object[] set = _dag.upSet(e);
            Concept[] upSet = new Concept[set.length];

            for (int i = 0; i < set.length; i++) {
                upSet[i] = (Concept) set[i];
            }

            return upSet;
        } else {
            // TODO: Implement upSet for InfiniteConcepts.
            throw new IllegalArgumentException("upSet method not implemented"
                    + " for Concept subclass " + e.getClass().getName() + ".");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return the concept that is either the greatest lower or least upper
     *  bound for the two given concepts.
     *
     *  @param concept1 The first concept.
     *  @param concept2 The second concept.
     *  @param boundType Specifies the type of bound to be returned; either
     *   GREATESTLOWER or LEASTUPPER.
     *  @return The concept that is the correct bound for the two concepts.
     */
    private Concept _getBoundForConcepts(Concept concept1, Concept concept2,
            BoundType boundType) {
        if (concept1 instanceof FiniteConcept
                && concept2 instanceof FiniteConcept) {
            switch (boundType) {
            case GREATESTLOWER:
                return (Concept) _dag.greatestLowerBound(concept1, concept2);
            case LEASTUPPER:
                return (Concept) _dag.leastUpperBound(concept1, concept2);
            default:
                throw new IllegalArgumentException("Unrecognized bound type: "
                        + boundType + ". Expected either GREATESTLOWER or "
                        + "LEASTUPPER");
            }
        } else if (concept1 instanceof InfiniteConcept) {
            return _getBoundForInfiniteConcept((InfiniteConcept) concept1,
                    concept2, boundType);
        } else if (concept2 instanceof InfiniteConcept) {
            return _getBoundForInfiniteConcept((InfiniteConcept) concept2,
                    concept1, boundType);
        } else { // This case should never happen.
            throw new IllegalArgumentException("Invalid concepts '" + concept1
                    + "' and '" + concept2 + "' (neither finite nor infinite)");
        }
    }

    /** Return the concept that is the correct bound for the specified infinite
     *  concept and another concept.
     *
     *  @param infiniteConcept The infinite concept.
     *  @param otherConcept The other concept.
     *  @param boundType Specifies the type of bound to be returned; either
     *   GREATESTLOWER or LEASTUPPER.
     *  @return The concept that is the correct bound for the two concepts.
     */
    private Concept _getBoundForInfiniteConcept(
            InfiniteConcept infiniteConcept, Concept otherConcept,
            BoundType boundType) {
        switch (boundType) {
        case GREATESTLOWER:
            return infiniteConcept.greatestLowerBound(otherConcept);
        case LEASTUPPER:
            return infiniteConcept.leastUpperBound(otherConcept);
        default:
            throw new IllegalArgumentException("Unrecognized bound type: "
                    + boundType + ". Expected either GREATESTLOWER or "
                    + "LEASTUPPER");
        }
    }

    /** Return the input object as a Concept, or throw an exception if the
     *  input object cannot be cast to a Concept.
     *  @param input The specified input object.
     *  @return The input object cast to a Concept.
     *  @exception IllegalArgumentException Thrown if the input object cannot
     *   be cast to a Concept.
     */
    private Concept _getInputObjectAsAConcept(Object input) {
        if (input == null || input instanceof Concept) {
            return (Concept) input;
        } else {
            String methodName = Thread.currentThread().getStackTrace()[1]
                    .getMethodName();
            throw new IllegalArgumentException("ConceptGraph." + methodName
                    + ": an argument is not an instance of " + "Concept: "
                    + input + " is an instance of " + input.getClass());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** A directed acyclic graph representing the connectivity of the
     *  concepts in this concept graph.
     */
    private DirectedAcyclicGraph _dag;
}
