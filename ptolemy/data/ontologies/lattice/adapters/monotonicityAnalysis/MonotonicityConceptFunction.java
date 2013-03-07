/* Top level adapter class for all DimensionSystem ontology adapters.

 Copyright (c) 2006-2013 The Regents of the University of California.
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
package ptolemy.data.ontologies.lattice.adapters.monotonicityAnalysis;

import java.util.List;

import ptolemy.data.expr.Constants;
import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.ConceptFunction;
import ptolemy.data.ontologies.ConceptToken;
import ptolemy.data.ontologies.FiniteConcept;
import ptolemy.data.ontologies.FlatTokenRepresentativeConcept;
import ptolemy.data.ontologies.Ontology;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// DimensionSystemAdapter

/**
 The top level adapter class for MonotonicityAnalysis adapters.

 @author Ben Lickly (based on DimensionSystemAdapter)
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (cshelton)
 @Pt.AcceptedRating Red (cshelton)
 */
public abstract class MonotonicityConceptFunction extends ConceptFunction {

    /** Create the concept function over the monotonicity lattice.
     *  @param name The name of the concept function.
     *  @param numArgs The number of arguments for this function, if
     *   this number is fixed, and -1 otherwise.
     *  @param monotonicityAnalysisOntology The ontology that represents
     *   monotonicity lattice.
     *  @param domainOntologies The ontologies that represents the domain
     *   and range of the function that we are checking for monotonicity.
     *  @exception IllegalActionException If the output ontology is null,
     *   numArgs is invalid, or the monotonicity ontology does not have
     *   the expected structure
     */
    public MonotonicityConceptFunction(String name, int numArgs,
            Ontology monotonicityAnalysisOntology,
            List<Ontology> domainOntologies) throws IllegalActionException {
        super(name, numArgs, monotonicityAnalysisOntology);

        _setup(monotonicityAnalysisOntology, domainOntologies);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Add the concepts from the domain ontologies as constants
     *  for the parse tree evaluator.
     *
     *    FIXME: Is there a better alternative?
     *
     *  @param domainOntology The domain ontology containing the concepts
     *    to add.
     *  @exception IllegalActionException If there is a problem adding any
     *   of the concepts to the Constants hash table.
     */
    private void _addConceptConstants(Ontology domainOntology)
            throws IllegalActionException {
        // FIXME: this is so wrong.
        // The problem is that we are updating the Constants Hashtable
        // that is shared between runs.  See
        // OntologySolverBase.cleanConstants() for what we
        // need to do to remove the Constants that are added here.
        for (Object entity : domainOntology.allAtomicEntityList()) {
            if (entity instanceof Concept) {
                Constants.add(((Concept) entity).getName(), new ConceptToken(
                        (Concept) entity));
            }
        }
    }

    /** Initialize all the variables we need to use monotonicity concepts.
     *  This includes saving the monotonicity, domain, and range ontologies;
     *  adding all necessary concepts to the expression parser;
     *  and naming all the basic monotonicity concepts.
     *
     *  @param monotonicityAnalysisOntology The ontology over which the
     *   monotonicity analysis's results are drawn.
     *  @param domainOntologies The ontologies that represents the domain of
     *   the function that we are checking for monotonicity.
     *  @exception IllegalActionException If the monotonicity ontology
     *   does not have the expected structure.
     */
    private void _setup(Ontology monotonicityAnalysisOntology,
            List<Ontology> domainOntologies) throws IllegalActionException {
        _monotonicityAnalysisOntology = monotonicityAnalysisOntology;
        _domainOntologies = domainOntologies;

        for (Ontology domainOntology : domainOntologies) {
            _addConceptConstants(domainOntology);
        }

        // FIXME: Should we hard code all the Concept name strings here?
        // Instantiate all the concepts for the monotonicityAnalysis ontology
        // Throw an exception if any of them are not found
        _constantConcept = (FiniteConcept) _monotonicityAnalysisOntology
                .getEntity("Constant");
        if (_constantConcept == null) {
            throw new IllegalActionException(_monotonicityAnalysisOntology,
                    "Concept Constant not found in monotonicityAnalysis ontology.");
        }

        _monotonicConcept = (FiniteConcept) _monotonicityAnalysisOntology
                .getEntity("Monotonic");
        if (_monotonicConcept == null) {
            throw new IllegalActionException(_monotonicityAnalysisOntology,
                    "Concept Monotonic not found in monotonicityAnalysis ontology.");
        }

        _antimonotonicConcept = (FiniteConcept) _monotonicityAnalysisOntology
                .getEntity("Antimonotonic");
        if (_antimonotonicConcept == null) {
            throw new IllegalActionException(_monotonicityAnalysisOntology,
                    "Concept Antimonotonic not found in monotonicityAnalysis ontology.");
        }

        _generalConcept = (FiniteConcept) _monotonicityAnalysisOntology
                .getEntity("General");
        if (_generalConcept == null) {
            throw new IllegalActionException(_monotonicityAnalysisOntology,
                    "Concept General not found in monotonicityAnalysis ontology.");
        }

        _nonMonotonicRepresentative = (FlatTokenRepresentativeConcept) _monotonicityAnalysisOntology
                .getEntity("NonMonotonic");
        _nonAntimonotonicRepresentative = (FlatTokenRepresentativeConcept) _monotonicityAnalysisOntology
                .getEntity("NonAntimonotonic");
        if (_nonAntimonotonicRepresentative == null
                || null == _nonMonotonicRepresentative) {
            throw new IllegalActionException(
                    _monotonicityAnalysisOntology,
                    "Infinite representatives (NonMonotonic and NonAntimonontonic) not found in monotonicityAnalysis ontology.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected members                 ////

    // The ontology for all the monotonicityAnalysis adapters

    /** The monotonicityAnalysis ontology referred to by all monotonicityAnalysis adapters. */
    protected Ontology _monotonicityAnalysisOntology;

    /** The domain ontology.
     *  This ontology forms the domain of the functions whose monotonicity
     *  we check.
     */
    protected List<Ontology> _domainOntologies;

    // Get all the Concepts from the ontology to use in all the monotonicityAnalysis adapters

    /** The "Constant" Concept from the monotonicityAnalysis ontology. */
    protected FiniteConcept _constantConcept;

    /** The "Monotonic" Concept from the monotonicityAnalysis ontology. */
    protected FiniteConcept _monotonicConcept;

    /** The "Antimonotonic" Concept from the monotonicityAnalysis ontology. */
    protected FiniteConcept _antimonotonicConcept;

    /** The "General" Concept from the monotonicityAnalysis ontology. */
    protected FiniteConcept _generalConcept;

    /** A set of "Almost Monotonic" Concepts (parameterized by counterexamples). */
    protected FlatTokenRepresentativeConcept _nonMonotonicRepresentative;
    /** A set of "Almost Antimonotonic" Concepts (parameterized by counterexamples). */
    protected FlatTokenRepresentativeConcept _nonAntimonotonicRepresentative;
}
