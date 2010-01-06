/* Top level adapter class for all dimensionSystem ontology adapters.

 Copyright (c) 2006-2009 The Regents of the University of California.
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
package ptolemy.data.ontologies.adapters.dimensionSystem;

import java.util.LinkedList;
import java.util.List;

import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.Ontology;
import ptolemy.data.ontologies.OntologySolver;
import ptolemy.data.ontologies.PropertyConstraintHelper;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// DimensionSystemPropertyConstraintHelper

/**
 The top level adapter class for dimensionSystem adapters.

 @author Charles Shelton
 @version $Id$
 @since Ptolemy II 8.1
 @Pt.ProposedRating Red (cshelton)
 @Pt.AcceptedRating Red (cshelton)
 */
public class DimensionSystemPropertyConstraintHelper extends PropertyConstraintHelper {

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
    public DimensionSystemPropertyConstraintHelper(OntologySolver solver,
            Object component) throws IllegalActionException {
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
    public DimensionSystemPropertyConstraintHelper(OntologySolver solver,
            Object component, boolean useDefaultConstraints)
            throws IllegalActionException {
        super(solver, component, useDefaultConstraints);
        
        // Instantiate the dimensionSystem ontology
        _dimensionSystemOntology = getSolver().getOntology();
        
        // FIXME: Should we hard code all the Concept name strings here?
        // Instantiate all the concepts for the dimensionSystem ontology
        // Throw an exception if any of them are not found
        _unknownConcept = (Concept) _dimensionSystemOntology.getEntity("Unknown");
        if (_unknownConcept == null) {
            throw new IllegalActionException(_dimensionSystemOntology,
                    "Concept Unknown not found in dimensionSystem ontology.");
        }
        
        _dimensionlessConcept = (Concept) getSolver().getOntology().getEntity("Dimensionless");
        if (_dimensionlessConcept == null) {
            throw new IllegalActionException(_dimensionSystemOntology,
                    "Concept Dimensionless not found in dimensionSystem ontology.");
        }
        
        _timeConcept = (Concept) getSolver().getOntology().getEntity("Time");
        if (_timeConcept == null) {
            throw new IllegalActionException(_dimensionSystemOntology,
                    "Concept Time not found in dimensionSystem ontology.");
        }
        
        _positionConcept = (Concept) getSolver().getOntology().getEntity("Position");
        if (_positionConcept == null) {
            throw new IllegalActionException(_dimensionSystemOntology,
                    "Concept Position not found in dimensionSystem ontology.");
        }
        
        _velocityConcept = (Concept) getSolver().getOntology().getEntity("Velocity");
        if (_velocityConcept == null) {
            throw new IllegalActionException(_dimensionSystemOntology,
                    "Concept Velocity not found in dimensionSystem ontology.");
        }
        
        _accelerationConcept = (Concept) getSolver().getOntology().getEntity("Acceleration");
        if (_accelerationConcept == null) {
            throw new IllegalActionException(_dimensionSystemOntology,
                    "Concept Acceleration not found in dimensionSystem ontology.");
        }
        
        _conflictConcept = (Concept) getSolver().getOntology().getEntity("Conflict");
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
    protected Concept _unknownConcept;
    
    /** The "Dimensionless" Concept from the dimensionSystem ontology. */
    protected Concept _dimensionlessConcept;
    
    /** The "Time" Concept from the dimensionSystem ontology. */
    protected Concept _timeConcept;
    
    /** The "Position" Concept from the dimensionSystem ontology. */
    protected Concept _positionConcept;
    
    /** The "Velocity" Concept from the dimensionSystem ontology. */
    protected Concept _velocityConcept;
    
    /** The "Acceleration" Concept from the dimensionSystem ontology. */
    protected Concept _accelerationConcept;
    
    /** The "Conflict" Concept from the dimensionSystem ontology. */
    protected Concept _conflictConcept;
}
