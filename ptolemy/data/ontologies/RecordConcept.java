/* A concept that represents the concept values of entries in a record token.
 * 
 * Copyright (c) 2010 The Regents of the University of California. All
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
package ptolemy.data.ontologies;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import ptolemy.graph.CPO;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;

/** A concept that represents the concept values of entries in a record token.
 *  
 *  A conceptable model element such as a port or node in a Ptolemy expression
 *  could contain a token value that is a record data type.  A record token
 *  is a token that is a collection of multiple token values of different
 *  types. For example, we might want to specify a record that indicates the
 *  (x,y) pixel position on a black-and-white screen, and also true or false
 *  for whether that pixel position is on or off.  We can use a record of the form:
 *  {x = 34, y = 26, pixelOn = true}
 *  <p>
 *  This RecordConcept allows any record to be assigned concept values for its
 *  individual elements from an arbitrary finite ontology. For example, if we
 *  wanted to assign a concept to the token above from the constAbstractInterpretation
 *  ontology, it would be:
 *  {x = Positive, y = Positive, pixelOn = BooleanTrue}
 *  
 *  This code is adapted from the
 *  {@link ptolemy.data.ontologies.lattice.adapters.monotonicityAnalysis.MonotonicityConcept}
 *  implementation.
 *  
 *  @author Charles Shelton, Ben Lickly, Elizabeth Latronico
 *  @version $Id$
 *  @since Ptolemy II 9.0
 *  @Pt.ProposedRating Red (blickly)
 *  @Pt.AcceptedRating Red (blickly)
 */
public class RecordConcept extends InfiniteConcept {

    ///////////////////////////////////////////////////////////////////
    ////             public constructors/factories                 ////
    
    /** Create a new record concept, belonging to the given
     *  ontology, with an automatically generated name.
     * 
     *  @param ontology The ontology to which this concept belongs.
     *  @return The newly created RecordConcept.
     *  @throws InternalErrorException If there .
     */
    public static RecordConcept createRecordConcept(Ontology ontology) {
        try {
            return new RecordConcept(ontology);
        } catch (NameDuplicationException e) {
            throw new InternalErrorException(
                    "Name conflict with automatically generated infinite concept name.\n"
                  + "This should never happen.\n"
                  + "Original exception:" + e.toString());
        } catch (IllegalActionException e) {
            throw new InternalErrorException(
                    "There was an error creating a new RecordConcept"
                  + "in the " + ontology + "ontology\n."
                  + "Original exception:" + e.toString());
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                          public methods                   ////

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
    public int compare(Concept concept) throws IllegalActionException {
        if (concept == null) {
            return CPO.INCOMPARABLE;
        }
        
        if (concept.getOntology() == null || !(concept.getOntology().equals(getOntology()))) {
            throw new IllegalActionException(this,
                    "Attempt to compare elements from two distinct ontologies");
        }

        // Original bottom and top remain bottom and top.
        if (concept.equals(getOntology().getConceptGraph().bottom())) {
            return CPO.HIGHER;
        } else if (concept.equals(getOntology().getConceptGraph().top())) {
            return CPO.LOWER;
        } else if (!(concept instanceof RecordConcept)) {
            return CPO.INCOMPARABLE;
        }

        RecordConcept righthandSide = (RecordConcept) concept;
        CPO graph = getOntology().getConceptGraph();

        boolean isSuperset = fieldSet().containsAll(righthandSide.fieldSet());
        boolean isSubset = righthandSide.fieldSet().containsAll(fieldSet());
        if (!isSubset && !isSuperset) {
            return CPO.INCOMPARABLE;
        }
        
        Set<String> commonFields = _commonFields(righthandSide);
        
        boolean seenHigher = false;
        boolean seenLower = false;
        boolean seenIncomparable = false;
        for (String field : commonFields) {
            int result = graph.compare(getFieldConcept(field),
                    righthandSide.getFieldConcept(field));
            switch (result) {
            case CPO.HIGHER:       seenHigher = true; break;
            case CPO.LOWER:        seenLower = true; break;
            case CPO.INCOMPARABLE: seenIncomparable = true; break;
            case CPO.SAME:         break;
            default:
                throw new IllegalActionException(this, "ConceptGraph compare " +
                		"did not return one of the defined CPO values. " +
                		"Return value was " + result + ". This should " +
                                "never happen.");
            }
        }
        // Following the Ptolemy type system conventions, a record that has a superset
        // of fields is lower in the lattice, and a record that has a subset of fields
        // is higher in the lattice.
        if (!seenHigher && !seenLower && !seenIncomparable && isSubset && isSuperset) {
            return CPO.SAME;
        } else if (!seenLower && !seenIncomparable && isSubset) {
            return CPO.HIGHER;
        } else if (!seenHigher && !seenIncomparable && isSuperset) {
            return CPO.LOWER;
        } else {
            return CPO.INCOMPARABLE;            
        }
    }
    
    /** Get the concept contained by the given field of this record concept.
     *  @param fieldName The field of the record concept whose concept value
     *   we are querying.
     *  @return The concept value held by this field in the record concept.
     */
    public Concept getFieldConcept(String fieldName) {
        return _fieldToConcept.get(fieldName);
    }

    /** Get the set of all record label names referred to by this record
     *  concept.
     *
     *  @return A set of label names of fields which are in this record concept.
     */
    public Set<String> fieldSet() {
        return _fieldToConcept.keySet();
    }

    /** Compute the least upper bound (LUB) of this and another concept.
     *  
     *  @param concept The other concept
     *  @return The concept that is the LUB of this and the given concept.
     */
    public Concept leastUpperBound(Concept concept) {
        Concept top = (Concept)getOntology().getConceptGraph().top();

        if (!(concept instanceof RecordConcept)) {
            if (concept.equals(getOntology().getConceptGraph().bottom())) {
                return this;
            } else {
                return top;
            }
        } else {
            // We have two RecordConcepts
            return leastUpperBound((RecordConcept) concept);
        }
    }
    
    /** Compute the least upper bound (LUB) of this and another monotonicity concept.
     *  
     *  @param concept The other monotonicity concept
     *  @return The concept that is the LUB of this and the given concept.
     */
    public Concept leastUpperBound(RecordConcept concept) {
        RecordConcept result = createRecordConcept(getOntology());
        
        Set<String> commonFields = _commonFields(concept);
        
        // The least upper bound is the record concept that only contains
        // the common fields and the least upper bound of each concept in that
        // field.
        for (String field : commonFields) {
            ConceptGraph graph = this.getOntology().getConceptGraph();
            Concept fieldConcept = graph.leastUpperBound(
                    this.getFieldConcept(field),
                    concept.getFieldConcept(field));
            result.putFieldConcept(field, fieldConcept);
        }
        return result;
    }

    /** Return the hash code of this record concept, which is uniquely
     *  determined by the ontology and the set of record field-concept
     *  mappings.
     *  @return The hash code of this concept.
     */
    public int hashCode() {
        return getOntology().hashCode() + _fieldToConcept.hashCode();
    }

    /** Set the specified field of this record concept with the given concept
     *  value.
     *
     *  @param fieldLabel The record field whose concept value we are setting.
     *  @param fieldConcept The concept value of the record field.
     *  @see #getFieldConcept(String)
     */
    public void putFieldConcept(String fieldLabel, Concept fieldConcept) {
        _fieldToConcept.put(fieldLabel, fieldConcept);
    }
    
    /** Return the string representation of this record concept.
     *  Note that the syntax here is similar to that used for records tokens
     *  (e.g. { x = Const, y = NonConst }).
     *  
     *  @return The string representation of this concept.
     */
    public String toString() {
        StringBuffer result = new StringBuffer("{");
        for (String key : _fieldToConcept.keySet()) {
            result.append(' ');
            result.append(key);
            result.append(" = ");
            result.append(getFieldConcept(key));
            result.append(',');
        }
        if (result.charAt(result.length() - 1) == ',') {
            result.deleteCharAt(result.length() - 1);
        }
        result.append(" }");
        return result.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                    private methods                        ////
    
    /** Return the record fields common to this and the given record concept.
     *  @param otherConcept The other record concept.
     *  @return The common fields, as a set of Strings.
     */
    private Set<String> _commonFields(RecordConcept otherConcept) {
        Set<String> fieldLabels = this._fieldToConcept.keySet();
        Set<String> otherFieldLabels = otherConcept._fieldToConcept.keySet();

        Set<String> commonFields = new HashSet<String>();
        for (String label : fieldLabels) {
            if (otherFieldLabels.contains(label)) {
                commonFields.add(label);
            }
        }
        return commonFields;
    }
    ///////////////////////////////////////////////////////////////////
    ////                    protected constructors                 ////

    /** Create a new Record concept, belonging to the given
     *  ontology.
     * 
     *  @param ontology The ontology to which this RecordConcept belongs.
     *  @throws NameDuplicationException Should never be thrown.
     *  @throws IllegalActionException If the base class throws it.
     */
    protected RecordConcept(Ontology ontology)
            throws IllegalActionException, NameDuplicationException {
          super(ontology);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** Mapping of record field names to concept values.
     *  The map must be sorted to ensure that the toString method
     *  returns a unique representation of the concept.
     */
    private SortedMap<String, Concept> _fieldToConcept =
        new TreeMap<String, Concept>();
}
