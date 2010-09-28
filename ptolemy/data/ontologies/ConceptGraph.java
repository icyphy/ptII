/*
 * The data structure for the graph of relations between concepts in an ontology.
 * 
 * Copyright (c) 2007-2010 The Regents of the University of California. All
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

import java.util.Collection;

import ptolemy.graph.DirectedAcyclicGraph;
import ptolemy.graph.Edge;
import ptolemy.graph.Node;

///////////////////////////////////////////////////////////////////
//// ConceptGraph

/** A data structure defining the relationships in an ontology. An ontology is a set of concepts
 *  and the relationships between them.  In a general ontology the graph describing the relationships
 *  between concepts need not be a directed acyclic graph (DAG).  But we restrict our implementation
 *  to a DAG because we currently deal only with ontologies than can be partially ordered. This is
 *  particularly important for an ontology whose graph is a lattice, where we can use the Reihof and
 *  Mogensen algorithm to do a scalable analysis
 *  and inference on a model to assign concepts from the ontology to each element in the model.
 *  This specialization is implemented as a {@linkplain ptolemy.data.ontologies.lattice.LatticeOntologySolver
 *  LatticeOntologySolver}, a subclass of {@linkplain OntologySolver}.
 * 
 * @author Thomas Mandl, Man-Kit Leung, Edward A. Lee, Ben Lickly, Dai Bui, Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 * @see ptolemy.graph.CPO
 */
public class ConceptGraph extends DirectedAcyclicGraph {

    /** Construct a new empty concept graph.
     */
    public ConceptGraph() {
        super();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Add a relation between two Concepts as an edge to the graph.
     * 
     * @param weight1 The source concept
     * @param weight2 The sink concept
     * @param newEdgeWeight The ConceptRelation between the two concepts weight1
     *  and weight2.
     * @return The set of edges that were added; each element
     *  of this set is an instance of {@link Edge}.
     * @exception IllegalArgumentException If the newEdgeWeight argument is not an
     *  instance of {@link ConceptRelation}.
     */
    public Collection addEdge(Object weight1, Object weight2,
            Object newEdgeWeight) {
        if (!(newEdgeWeight instanceof ConceptRelation)) {
            throw new IllegalArgumentException(
                    "Attempt to add a relation that is not a ConceptRelation to an Ontology graph.");
        }
        return super.addEdge(weight1, weight2, newEdgeWeight);
    }

    /** Add a concept to this concept graph.
     *  @param weight The concept.
     *  @return The constructed node in the graph.
     *  @exception IllegalArgumentException If the argument is not
     *   an instance of {@link FiniteConcept}.
     */
    public Node addNodeWeight(Object weight) {
        if (!(weight instanceof FiniteConcept)) {
            throw new IllegalArgumentException(
                    "Attempt to add a non-Concept to an Ontology graph.");
        }
        return super.addNodeWeight(weight);
    }

    /**
     * Add structured properties. The parameter flag is a bit-wise OR union of
     * the structured properties desired to add. The class provides a set of
     * symbolic constants for these structured property types. For example,
     * invoke addStructuredProperties(RECORD) to add the RecordProperty. This
     * method should be called after all base elements have been added. The user
     * is responsible for ensuring a lattice structure before calling this
     * method. The lattice structure should be preserved after calling this
     * method, so there is no need to do another check.
     * @param structuredPropertiesToAdd The bit-wise OR union of the structured
     * properties desired to add.
     * @exception IllegalActionException
     */
    /* FIXME: To support structured concepts:
    public void addStructuredProperties(int structuredPropertiesToAdd)
            throws IllegalActionException {

        if ((structuredPropertiesToAdd & RECORD) != 0) {
            Property record = new RecordProperty(this, new String[0],
                    new LatticeProperty[0]).getRepresentative();

            if (!isLattice()) {
                throw new IllegalActionException(
                        "This ontology needs to be a lattice "
                                + "before adding structured types.");
            }

            Object bottom = bottom();
            Object top = top();

            addNodeWeight(record);
            addEdge(bottom, record);
            addEdge(record, top);
        }

        if ((structuredPropertiesToAdd & ARRAY) != 0) {
            // FIXME: add Array structure.
        }
    }
    */

    /** Compare two concepts in the ontology. The arguments must be
     *  instances of {@link FiniteConcept}, otherwise an exception will be thrown.
     *  This method returns one of ptolemy.graph.CPO.LOWER, ptolemy.graph.CPO.SAME,
     *  ptolemy.graph.CPO.HIGHER, ptolemy.graph.CPO.INCOMPARABLE, indicating the
     *  first argument is lower than, equal to, higher than, or incomparable with
     *  the second argument in the property hierarchy, respectively.
     *  @param concept1 An instance of {@link FiniteConcept}.
     *  @param concept2 An instance of {@link FiniteConcept}.
     *  @return One of CPO.LOWER, CPO.SAME, CPO.HIGHER, CPO.INCOMPARABLE.
     *  @exception IllegalArgumentException If one or both arguments are not
     *   instances of {@link FiniteConcept}.
     */
    public int compare(Object concept1, Object concept2) {
        if (!(concept1 instanceof FiniteConcept) || !(concept2 instanceof FiniteConcept)) {
            throw new IllegalArgumentException("ConceptGraph.compare: "
                    + "Arguments are not instances of Concept: "
                    + " concept1 = " + concept1 + ", concept2 = " + concept2);
        }

        Concept t1Rep = _toRepresentative((Concept) concept1);
        Concept t2Rep = _toRepresentative((Concept) concept2);

        /* FIXME: Support structured concepts using something like this:
        if (t1Rep.equals(t2Rep) && t1Rep instanceof StructuredProperty) {
            return ((StructuredProperty) t1)
                    ._compare((StructuredProperty) t2);
        }
        */

        return super.compare(t1Rep, t2Rep);
    }

    ///////////////////////////////////////////////////////////////////
    ////                          public fields                    ////

    /** Public symbolic constant value for the RecordProperty. */
    // FIXME: For structured concepts.
    // public final int RECORD = 0x1;

    /** Public symbolic constant value for the ArrayProperty. */
    // FIXME: For structured concepts:
    // public final int ARRAY = 0x2;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** If the argument is a structured concept, return its representative;
     *  otherwise, return the argument.
     *  @param p The specified concept for which to return a representative.
     *  @return The representative for the specified concept.
     */
    private Concept _toRepresentative(Concept p) {
        /* FIXME: Support structured concepts using something like this:
        if (p instanceof StructuredProperty) {
            return ((StructuredProperty) p).getRepresentative();
        } else {
            return p;
        }
        */
        return p;
    }
}
