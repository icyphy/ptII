/* A concept function that returns the concept value from the specified field
 * from an input RecordConcept.
 *
 * Copyright (c) 1998-2014 The Regents of the University of California. All
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
 */
package ptolemy.data.ontologies;

import java.util.List;

import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// ConceptFromRecordField

/** A concept function that returns the concept value from the specified field
 * from an input RecordConcept.
 *
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Green (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public class ConceptFromRecordField extends ConceptFunction {

    /** Create a new ConceptFromRecordField concept function with
     *  the specified field label.
     *  @param name The name of the concept function.
     *  @param fieldLabel The field label from which to get a concept value from
     *   the input RecordConcept.
     *  @param ontology The domain and range ontology for this concept function.
     *  @exception IllegalActionException Thrown if the concept function cannot be created.
     */
    public ConceptFromRecordField(String name, String fieldLabel,
            Ontology ontology) throws IllegalActionException {
        super(name, 1, ontology);
        _fieldLabel = fieldLabel;
    }

    /** Return the function output from the given input arguments. The output
     *  concept is the concept from the field label specified in the constructor
     *  from the input RecordConcept.
     *  @param argValues The Concept input arguments which should be a single RecordConcept.
     *  @return The output concept that is the value of the RecordConcept at the specified field.
     *   The function will return the bottom of the lattice if the field is not
     *   contained in the RecordConcept.
     *  @exception IllegalActionException Thrown if there is a problem creating
     *   the output Concept or if the input is not a RecordConcept.
     */
    @Override
    protected Concept _evaluateFunction(List<Concept> argValues)
            throws IllegalActionException {
        Concept inputRecord = argValues.get(0);
        if (!(inputRecord instanceof RecordConcept)) {
            // If the input is not a RecordConcept, then it should be top, bottom or null.
            if (inputRecord == null
                    || inputRecord.getOntology().getConceptGraph().bottom()
                    .equals(inputRecord)
                    || inputRecord.getOntology().getConceptGraph().top()
                    .equals(inputRecord)) {
                return inputRecord;
            } else {
                throw new IllegalActionException("The input concept for the "
                        + "ConceptFromRecordField concept function must be a "
                        + "RecordConcept. The input was: " + inputRecord);
            }
        }

        Concept result = ((RecordConcept) inputRecord).getConcept(_fieldLabel);
        if (result == null) {
            result = inputRecord.getOntology().getConceptGraph().bottom();
        }

        return result;
    }

    /** The field label name for which this function should return a concept
     *  value from a RecordConcept.
     */
    String _fieldLabel;
}
