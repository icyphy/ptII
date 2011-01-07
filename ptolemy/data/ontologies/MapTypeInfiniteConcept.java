/* A concept that represents a type mapping tokens to concepts.
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

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/** A concept that represents a type mapping tokens to concepts.
 *  Each concept of this type contains a mapping of token values to concepts.
 *  This is used in situations like the RecordConcept, which is a mapping
 *  of strings to arbitrary concepts, and the MontonicityConcept, which is
 *  a mapping of Strings to a fixed set of FiniteConcepts.
 *
 *  @author Ben Lickly
 *  @version $Id$
 *  @param <C> The type of the concepts that form the range of this mapping.
 *  @since Ptolemy II 9.0
 *  @Pt.ProposedRating Red (blickly)
 *  @Pt.AcceptedRating Red (blickly)
 *
 */
public abstract class MapTypeInfiniteConcept<C extends Concept> extends InfiniteConcept {

    /** Create a new MapTypeInfiniteConcept contained in the given ontology,
     *  with the given default concept.
     *  @param ontology The containing ontology.
     *  @param defaultConcept The concept value mapped to by all keys not
     *    contained in this map.
     *  @exception NameDuplicationException Not thrown.
     *  @exception IllegalActionException If the base class throws it.
     */
    protected MapTypeInfiniteConcept(Ontology ontology, C defaultConcept)
            throws IllegalActionException, NameDuplicationException {
        super(ontology);
        _defaultConcept = defaultConcept;
    }
    
    /** Create a new MapTypeInfiniteConcept contained in the given ontology.
     *  @param ontology The containing ontology.
     *  @exception NameDuplicationException Not thrown.
     *  @exception IllegalActionException If the base class throws it.
     */
    protected MapTypeInfiniteConcept(Ontology ontology)
            throws IllegalActionException, NameDuplicationException {
        super(ontology);
        _defaultConcept = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                          public methods                   ////

    /** Get the concept contained by at the given key in this map concept,
     *  or the default value if the key is not contained in this map concept.
     *  @param key The key whose concept value we are querying.
     *  @return The concept value held by this map concept at the key, or
     *   the default value if there is no such concept.
     */
    public C getConcept(String key) {
        if (_keyToConcept.containsKey(key)) {
            return _keyToConcept.get(key);
        }
        return _defaultConcept;
    }

    /** Get the set of all record label names referred to by this record
     *  concept.
     *
     *  @return A set of label names of fields which are in this record concept.
     */
    public Set<String> keySet() {
        return _keyToConcept.keySet();
    }

    /** Return the hash code of this record concept, which is uniquely
     *  determined by the ontology and the set of record field-concept
     *  mappings.
     *  @return The hash code of this concept.
     */
    public int hashCode() {
        return getOntology().hashCode() + _keyToConcept.hashCode();
    }

    /** Set the specified ket of this map to the given concept value.
     *
     *  @param key The key whose concept value we are setting.
     *  @param concept The concept value of the given key.
     *  @see #getConcept(String)
     */
    public void putConcept(String key, C concept) {
        if (concept.equals(_defaultConcept)) {
            _keyToConcept.remove(key);
        } else {
            _keyToConcept.put(key, concept);
        }
    }
    
    /** Return the string representation of this map concept.
     *  Note that the syntax here is similar to that used for records tokens
     *  (e.g. { x = Const, y = NonConst }).
     *  
     *  @return The string representation of this concept.
     */
    public String toString() {
        StringBuffer result = new StringBuffer("{");
        for (String key : _keyToConcept.keySet()) {
            result.append(' ');
            result.append(key);
            result.append(" = ");
            result.append(getConcept(key));
            result.append(',');
        }
        if (result.charAt(result.length() - 1) == ',') {
            result.deleteCharAt(result.length() - 1);
        }
        result.append(" }");
        return result.toString();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                   protected methods                       ////
    
    /** Return the string keys common to this and the given map concept.
     *  @param otherConcept The other map concept.
     *  @return The common fields, as a set of Strings.
     */
    protected Set<String> _commonKeys(MapTypeInfiniteConcept<C> otherConcept) {
        Set<String> fieldLabels = this._keyToConcept.keySet();
        Set<String> otherFieldLabels = otherConcept._keyToConcept.keySet();

        Set<String> commonFields = new HashSet<String>();
        for (String label : fieldLabels) {
            if (otherFieldLabels.contains(label)) {
                commonFields.add(label);
            }
        }
        return commonFields;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** Mapping of string keys to concept values.
     *  The map must be sorted to ensure that the toString method
     *  returns a unique representation of the concept.
     */
    protected SortedMap<String, C> _keyToConcept =
        new TreeMap<String, C>();
    
    /** The value of the map concept to be returned when there is no explicit
     *  concept mapped.  Defaults to null.
     */
    private C _defaultConcept;

}
