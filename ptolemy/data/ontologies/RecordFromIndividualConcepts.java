/* A concept function that returns a record concept with specified field names
 * from a list of concept inputs.
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
import java.util.SortedSet;
import java.util.TreeSet;

import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// RecordFromIndividualConcepts

/** A concept function that returns a record concept with specified field names
 *  from a list of concept inputs.
 *
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Green (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public class RecordFromIndividualConcepts extends ConceptFunction {

    /** Create a new RecordFromIndividualConcepts concept function with
     *  the specified set of field labels.
     *  @param name The name of the concept function.
     *  @param fieldLabels The set of field labels for the output record concept.
     *  @param ontology The domain and range ontology for this concept function.
     *  @exception IllegalActionException Thrown if the concept function cannot be created.
     */
    public RecordFromIndividualConcepts(String name,
            SortedSet<String> fieldLabels, Ontology ontology)
                    throws IllegalActionException {
        super(name, fieldLabels.size(), ontology);
        _fieldLabels = new TreeSet<String>(fieldLabels);
    }

    /** Return the function output from the given input arguments. The output
     *  concept is a RecordConcept with the given Concept input arguments
     *  as values for the fields.  The concept input arguments are assumed
     *  to be in the same order that the fields are specified in the fieldLabels set
     *  argument in the constructor.
     *  @param argValues The Concept input arguments.
     *  @return The RecordConcept output concept.
     *  @exception IllegalActionException Thrown if there is a problem creating
     *   the output RecordConcept.
     */
    @Override
    protected RecordConcept _evaluateFunction(List<Concept> argValues)
            throws IllegalActionException {
        RecordConcept returnRecord = RecordConcept
                .createRecordConcept(_outputRangeOntology);

        Object[] fieldArray = _fieldLabels.toArray();
        for (int i = 0; i < fieldArray.length; i++) {
            returnRecord.putConcept((String) fieldArray[i], argValues.get(i));
        }
        return returnRecord;
    }

    /** The sorted set of fields for the output RecordConcept for this concept
     *  function.
     */
    SortedSet<String> _fieldLabels;
}
