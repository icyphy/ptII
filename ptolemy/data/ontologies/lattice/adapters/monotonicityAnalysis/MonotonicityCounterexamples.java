/* Representation of a set of counterexamples to monotonicity.
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
package ptolemy.data.ontologies.lattice.adapters.monotonicityAnalysis;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.FiniteConcept;
import ptolemy.data.ontologies.util.MultiHashMap;

/** Representation of a set of counterexamples to monotonicity.
 * 
 *  Each counterexample is of the form of a pair of elements (x1,x2)
 *  where x1 <= x2, but f(x1) \not <= f(x2), for the non-monotonic function
 *  f under consideration.
 *
 *  @author Ben Lickly
 *  @version $Id$
 *  @since Ptolemy II 9.0
 *  @Pt.ProposedRating Red (blickly)
 *  @Pt.AcceptedRating Red (blickly)
 */
public class MonotonicityCounterexamples {
    
    /** Construct an empty set of counterexamples.
     *  We can't really consider this a set of counterexamples
     *  until we add some counterexamples.
     */
    public MonotonicityCounterexamples() {
        _counterexamples = new MultiHashMap();
    }
    
    /** Add a pair of concepts as a counterexample to this set.
     *  This means that x1 <= x2, but for the (non-monotonic) function in
     *  question, f(x1) \not <= f(x2).
     *  
     *  @param x1 The lesser concept.
     *  @param x2 The greater concept.
     */
    public void add(Concept x1, Concept x2) {
        _counterexamples.remove(x1, x2);
        _counterexamples.put(x1, x2);
    }
    
    /** Determine if this set contains any counterexamples.
     *
     *  @return True, if this set contains at least one counterexample.
     *    False, otherwise.
     */
    public boolean containsCounterexamples() {
        return !_counterexamples.isEmpty();
    }
    

    /** Return a set of all the counterexamples.
     *  For each pair in the set returned, (x1, x2),
     *  we have x1 <= x2, but on the function under consideration
     *  f(x1) \not <= f(x2)
     *  
     *  @return A set of pairs of counterexamples.
     */
    public Set<ConceptPair> entrySet() {
        Set<ConceptPair> entrySet = new HashSet<ConceptPair>();
        for (FiniteConcept lesser : (Set<FiniteConcept>)_counterexamples.keySet()) {
            for (FiniteConcept greater : (Collection<FiniteConcept>)_counterexamples.get(lesser)) {
                entrySet.add(new ConceptPair(lesser, greater));
            }
        }
        return entrySet;
    }
    
    /** Return the string representation of the counterexample set.
     *  @return The string representation of this set.
     *  @see java.lang.Object#toString()
     */
    public String toString() {
        String result = "{";
        for (Entry<FiniteConcept, FiniteConcept> pair : entrySet()) {
            result += "(" + pair.getKey().toString() + "," + pair.getValue() + ")";
        }
        return result  + "}";
    }
    
    /** A multimap to keep track of the mapping of lesser concepts
     *  to greater concepts.  This must be a multimap, because there
     *  could be multiple counterexamples with the same lesser concept.
     *
     *  If our MultiMap supported Java Generics, this would have a type of
     *    MultiMap<Concept, Concept>
     */
    private MultiHashMap _counterexamples;
    
    /** Encapsulate counterexample pairs.
     *  These are pairs of the form (x1, x2) where x1 <= x2, but
     *  f(x1) \not <= f(x2) for the function under consideration.
     *  Thus, these pairs prove as counterexamples to the
     *  monotonicity of f.
     */
    public class ConceptPair implements Map.Entry<FiniteConcept,FiniteConcept> {
        /** Create a counterexample pair given both of the concepts that  make
         *  up the counterexample.
         *
         *  @param l The lesser concept, i.e. x1
         *  @param g The greater concept, i.e. x2
         */
        public ConceptPair(FiniteConcept l, FiniteConcept g) {
            lesser = l; greater = g;
        }
        /** The lesser of the concepts (i.e. x1)
         */
        public FiniteConcept lesser;
        /** The greater of the concepts (i.e. x2)
         */
        public FiniteConcept greater;
        
        
        /** Return the lesser concept of the counterexample.
         * 
         *  @return The lesser concept.
         *  @see java.util.Map.Entry#getKey()
         */
        public FiniteConcept getKey() {
            return lesser;
        }
        /** Return the greater concept of the counterexample.
         *  
         *  @return The greater concept
         *  @see java.util.Map.Entry#getValue()
         *  @see #setValue(FiniteConcept)
         */
        public FiniteConcept getValue() {
            return greater;
        }
        /** Do nothing.
         *  (Counterexamples are immutable)
         *
         *  @param value Ignored
         *  @return Always return null.
         *  @see java.util.Map.Entry#setValue(java.lang.Object)
         *  @see #getValue()
         */
        public FiniteConcept setValue(FiniteConcept value) {
            return null;
        }  
    }
    
}
