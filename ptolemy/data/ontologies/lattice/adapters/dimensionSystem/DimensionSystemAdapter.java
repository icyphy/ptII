/* Top level adapter class for all DimensionSystem ontology adapters.

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
package ptolemy.data.ontologies.lattice.adapters.dimensionSystem;

import ptolemy.data.ontologies.FiniteConcept;
import ptolemy.data.ontologies.Ontology;
import ptolemy.data.ontologies.lattice.LatticeOntologyAdapter;
import ptolemy.data.ontologies.lattice.LatticeOntologySolver;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// DimensionSystemAdapter

/**
 The top level adapter class for DimensionSystem adapters.

 @author Charles Shelton
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (cshelton)
 @Pt.AcceptedRating Red (cshelton)
 */
public class DimensionSystemAdapter extends LatticeOntologyAdapter {

    /**
     * Construct a generic adapter for the dimensionSystem Ontology. This is the
     * adapter class that all adapters for the dimensionSystem ontology should inherit from.
     * It instantiates the Concepts contained in the dimensionSystem ontology so that
     * all adapters can easily use those Concepts without repeatedly calling
     * getOntology().getEntity().  This can be a source of bugs because if
     * programmers misspell the concept names for the ontology, getEntity() will return
     * null, and if thus the adapters will create incorrect constraints.
     * 
     * @param solver The given ontology solver.
     * @param component The given Object that this adapter refers to and sets up constraints for.
     * @exception IllegalActionException If the adapter cannot be
     * initialized in the superclass.
     */
    public DimensionSystemAdapter(LatticeOntologySolver solver, Object component)
            throws IllegalActionException {
        this(solver, component, true);
    }

    /**
     * Construct a generic adapter for the dimensionSystem Ontology. This is the
     * adapter class that all adapters for the dimensionSystem ontology should inherit from.
     * It instantiates the Concepts contained in the dimensionSystem ontology so that
     * all adapters can easily use those Concepts without repeatedly calling
     * getOntology().getEntity().  This can be a source of bugs because if
     * programmers misspell the concept names for the ontology, getEntity() will return
     * null, and if thus the adapters will create incorrect constraints.
     * 
     * @param solver The given ontology solver.
     * @param component The given Object that this adapter refers to and sets up constraints for.
     * @param useDefaultConstraints Indicate whether this adapter uses the
     * default actor constraints.
     * @exception IllegalActionException If the adapter cannot be
     * initialized in the superclass or the concepts from the dimensionSystem ontology
     * cannot be found.
     */
    public DimensionSystemAdapter(LatticeOntologySolver solver,
            Object component, boolean useDefaultConstraints)
            throws IllegalActionException {
        super(solver, component, useDefaultConstraints);

        // Instantiate the dimensionSystem ontology
        _dimensionSystemOntology = getSolver().getOntology();

        // FIXME: Should we hard code all the Concept name strings here?
        // Instantiate all the concepts for the dimensionSystem ontology
        // Throw an exception if any of them are not found
        _unknownConcept = (FiniteConcept) _dimensionSystemOntology
                .getEntity("Unknown");
        if (_unknownConcept == null) {
            throw new IllegalActionException(_dimensionSystemOntology,
                    "Concept Unknown not found in dimensionSystem ontology.");
        }

        _dimensionlessConcept = (FiniteConcept) getSolver().getOntology().getEntity(
                "Dimensionless");
        if (_dimensionlessConcept == null) {
            throw new IllegalActionException(_dimensionSystemOntology,
                    "Concept Dimensionless not found in dimensionSystem ontology.");
        }

        _timeConcept = (FiniteConcept) getSolver().getOntology().getEntity("Time");
        if (_timeConcept == null) {
            throw new IllegalActionException(_dimensionSystemOntology,
                    "Concept Time not found in dimensionSystem ontology.");
        }

        _positionConcept = (FiniteConcept) getSolver().getOntology().getEntity(
                "Position");
        if (_positionConcept == null) {
            throw new IllegalActionException(_dimensionSystemOntology,
                    "Concept Position not found in dimensionSystem ontology.");
        }

        _velocityConcept = (FiniteConcept) getSolver().getOntology().getEntity(
                "Velocity");
        if (_velocityConcept == null) {
            throw new IllegalActionException(_dimensionSystemOntology,
                    "Concept Velocity not found in dimensionSystem ontology.");
        }

        _accelerationConcept = (FiniteConcept) getSolver().getOntology().getEntity(
                "Acceleration");
        if (_accelerationConcept == null) {
            throw new IllegalActionException(_dimensionSystemOntology,
                    "Concept Acceleration not found in dimensionSystem ontology.");
        }

        _conflictConcept = (FiniteConcept) getSolver().getOntology().getEntity(
                "Conflict");
        if (_conflictConcept == null) {
            throw new IllegalActionException(_dimensionSystemOntology,
                    "Concept Conflict not found in dimensionSystem ontology.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected members                 ////

    // The ontology for all the dimensionSystem adapters

    /** The dimensionSystem ontology refered to by all dimensionSystem adapters. */
    protected Ontology _dimensionSystemOntology;

    // Get all the Concepts from the ontology to use in all the dimensionSystem adapters   

    /** The "Unknown" Concept from the dimensionSystem ontology. */
    protected FiniteConcept _unknownConcept;

    /** The "Dimensionless" Concept from the dimensionSystem ontology. */
    protected FiniteConcept _dimensionlessConcept;

    /** The "Time" Concept from the dimensionSystem ontology. */
    protected FiniteConcept _timeConcept;

    /** The "Position" Concept from the dimensionSystem ontology. */
    protected FiniteConcept _positionConcept;

    /** The "Velocity" Concept from the dimensionSystem ontology. */
    protected FiniteConcept _velocityConcept;

    /** The "Acceleration" Concept from the dimensionSystem ontology. */
    protected FiniteConcept _accelerationConcept;

    /** The "Conflict" Concept from the dimensionSystem ontology. */
    protected FiniteConcept _conflictConcept;
}
