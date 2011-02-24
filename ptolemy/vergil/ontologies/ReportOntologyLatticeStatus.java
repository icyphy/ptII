/** A static class that provides a method to display a message and highlight
    concepts in an ontology model based on whether the ontology is a lattice.

 Copyright (c) 1997-2010 The Regents of the University of California.
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

import java.util.List;

import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.Ontology;
import ptolemy.graph.NonLatticeCounterExample;
import ptolemy.graph.NonLatticeCounterExample.GraphExampleType;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.util.MessageHandler;

/**
A static class that provides a method to display a message and highlight
concepts in an ontology model based on whether the ontology is a lattice.

@author Charles Shelton
@version $Id$
@since Ptolemy II 8.1
@Pt.ProposedRating Red (cshelton)
@Pt.AcceptedRating Red (cshelton)
*/
public class ReportOntologyLatticeStatus {

    /** Show a status message depending on whether the given ontology model
     *  is a valid lattice. Also highlight the concepts in the ontology that
     *  demonstrate the counterexample if the ontology is not a lattice.
     *  @param ontologyModel The specified ontology model.
     *  @param modelGraphController The graph controller for the ontology model.
     */
    public static void showStatusAndHighlightCounterExample(Ontology ontologyModel, OntologyGraphController modelGraphController) {
        boolean isLattice = ontologyModel.isLattice();
        
        _clearOntologyErrorHighlights(ontologyModel, modelGraphController);

        if (isLattice) {
            MessageHandler.message("The ontology model graph is a valid lattice.");
        } else {
            NonLatticeCounterExample nonLatticeExample = ontologyModel.getConceptGraph().nonLatticeReason();
            GraphExampleType exampleType = (GraphExampleType) nonLatticeExample.getExampleType();
            List concepts = nonLatticeExample.getNodeList();

            StringBuffer errorMessageBuffer = new StringBuffer();
            errorMessageBuffer.append("The ontology model graph is not a valid lattice.\n");

            switch(exampleType) {
            case LEASTUPPER:
                errorMessageBuffer.append("These concepts have no least upper bound: ");
                break;
            case GREATESTLOWER:
                errorMessageBuffer.append("These concepts have no greatest lower bound: ");
                break;
            case GRAPHCYCLE:
                errorMessageBuffer.append("There is a cycle involving the concept: ");
                break;
            default:
                errorMessageBuffer.append("unknown lattice counterexample type: " + exampleType + ": ");
            }

            boolean first = true;
            for (Object concept : concepts) {
                modelGraphController.highlightError((Concept) concept);
                if (!first) {
                    errorMessageBuffer.append(", ");
                }
                errorMessageBuffer.append(((Concept) concept).toString());
                first = false;
            }
            errorMessageBuffer.append(".");

            MessageHandler.error(errorMessageBuffer.toString());
        }
    }
    
    /** Clear all the error highlighting from all concepts in the ontology
     *  before checking whether or not the ontology is a lattice.
     *  @param ontology The ontology which we are checking.
     *  @param modelGraphController The graph controller for the ontology model.
     */
    private static void _clearOntologyErrorHighlights(final Ontology ontology,
            OntologyGraphController modelGraphController) {        
        ChangeRequest request = new ChangeRequest(modelGraphController,
                "Clear Ontology Error Highlights") {
            protected void _execute() throws Exception {                
                List<Concept> allConcepts = ontology.entityList(Concept.class);
                for (Concept concept : allConcepts) {
                    Attribute highlightColor = concept.getAttribute("_highlightColor");
                    if (highlightColor != null) {
                        highlightColor.setContainer(null);
                    }
                }
            }
        };
        ontology.requestChange(request);
    }
}
