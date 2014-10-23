/** A static class that provides a method to display a message and highlight
    concepts in an ontology model based on whether the ontology is a lattice.

 Copyright (c) 1997-2014 The Regents of the University of California.
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
package ptolemy.vergil.ontologies;

///////////////////////////////////////////////////////////////////
//// ReportOntologyLatticeStatus

import java.util.LinkedList;
import java.util.List;

import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.ConceptGraph;
import ptolemy.data.ontologies.DAGConceptGraph;
import ptolemy.data.ontologies.Ontology;
import ptolemy.graph.NonLatticeCounterExample;
import ptolemy.graph.NonLatticeCounterExample.GraphExampleType;
import ptolemy.util.MessageHandler;

/**
A static class that provides a method to display a message and highlight
concepts in an ontology model based on whether the ontology is a lattice.

@author Charles Shelton
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Red (cshelton)
@Pt.AcceptedRating Red (cshelton)
 */
public class ReportOntologyLatticeStatus {

    /** Show a status message depending on whether the given ontology model
     *  is a valid lattice, and if the isAcceptable parameters form acceptance
     *  criteria.
     *  Also highlight the concepts in the ontology that demonstrate the
     *  counterexample if the ontology is not a lattice, or the non-acceptable
     *  concepts do not form acceptance criteria.
     *  @param ontologyModel The specified ontology model.
     *  @param modelGraphController The graph controller for the ontology model.
     */
    public static void showStatusAndHighlightCounterExample(
            Ontology ontologyModel, OntologyGraphController modelGraphController) {
        modelGraphController.clearAllErrorHighlights();
        boolean isLattice = ontologyModel.isLattice();
        List<Concept> invalidUnacceptables = _invalidUnacceptableConcepts(ontologyModel);

        if (!isLattice) {
            NonLatticeCounterExample nonLatticeExample = ontologyModel
                    .getConceptGraph().nonLatticeReason();
            GraphExampleType exampleType = (GraphExampleType) nonLatticeExample
                    .getExampleType();
            List<Concept> concepts = nonLatticeExample.getNodeList();

            StringBuffer errorMessageBuffer = new StringBuffer();
            errorMessageBuffer
            .append("The ontology model graph is not a valid lattice.\n");

            switch (exampleType) {
            case LEASTUPPER:
                errorMessageBuffer
                .append("These concepts have no least upper bound: ");
                break;
            case GREATESTLOWER:
                errorMessageBuffer
                .append("These concepts have no greatest lower bound: ");
                break;
            case GRAPHCYCLE:
                errorMessageBuffer
                .append("There is a cycle involving the concept: ");
                break;
            default:
                errorMessageBuffer
                .append("unknown lattice counterexample type: "
                        + exampleType + ": ");
            }

            _highlightErrors(modelGraphController, errorMessageBuffer, concepts);
        } else if (!invalidUnacceptables.isEmpty()) {
            _highlightErrors(modelGraphController, new StringBuffer(
                    "There following unacceptable concepts are not at the "
                            + "top of the lattice:\n"), invalidUnacceptables);
        } else {
            MessageHandler
            .message("The ontology model graph is a valid lattice.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Highlight the given concepts in the graph, and then display the
     *  given error message.
     *  @param modelGraphController The graph controller in which to highlight
     *   this error.
     *  @param errorMessageBuffer The text of the error message.
     *  @param concepts The concepts involved in the error.
     */
    private static void _highlightErrors(
            OntologyGraphController modelGraphController,
            StringBuffer errorMessageBuffer, List<Concept> concepts) {
        boolean first = true;
        for (Concept concept : concepts) {
            modelGraphController.highlightError(concept);
            if (!first) {
                errorMessageBuffer.append(", ");
            }
            errorMessageBuffer.append(concept.toString());
            first = false;
        }
        errorMessageBuffer.append(".");

        MessageHandler.error(errorMessageBuffer.toString());
    }

    /** Return a list of the concepts in the given ontology which are marked
     *  as not isAcceptable, but are not at the top of the lattice, as required.
     *  @param ontology The ontology to check.
     *  @return A list of invalid concepts marked as not acceptable, or an
     *   empty list if there are no errors.
     */
    private static List<Concept> _invalidUnacceptableConcepts(Ontology ontology) {
        ConceptGraph cg = ontology.getConceptGraph();
        if (cg instanceof DAGConceptGraph) {
            return ((DAGConceptGraph) cg).checkUnacceptableConcepts();
        } else {
            return new LinkedList<Concept>();
        }
    }
}
