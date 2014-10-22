/* Representation of a set of counterexamples to monotonicity.
 *
 * Copyright (c) 2010-2014 The Regents of the University of California. All
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

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ptolemy.data.ArrayToken;
import ptolemy.data.Token;
import ptolemy.data.TupleToken;
import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.ConceptToken;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.util.MultiHashMap;

/** Representation of a set of counterexamples to monotonicity.
 *
 *  Each counterexample is of the form of a pair of elements (x1,x2)
 *  where x1 &le; x2, but f(x1) \not &le; f(x2), for the non-monotonic function
 *  f under consideration.
 *
 *  @author Ben Lickly
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (blickly)
 *  @Pt.AcceptedRating Red (blickly)
 */
public class MonotonicityCounterexamples {

    /** Construct an empty set of counterexamples.
     *  We can't really consider this a set of counterexamples
     *  until we add some counterexamples.
     */
    public MonotonicityCounterexamples() {
        _counterexamples = new MultiHashMap<Concept, Concept>();
    }

    /** Create a counterexample set from a specific representation as a
     *  Ptolemy Token.
     *  This Token must be an ArrayToken whose entries are TupleTokens each of
     *  length 2, where the lesser concept is on the left hand side and the
     *  greater concept is on the right hand side.
     *  @param token The Ptolemy Token representing a set of counterexamples
     *   to monotonicity.
     *  @return The counterexample set represented by the given token.
     *  @exception IllegalActionException If the given Token does not conform to
     *   the required structure;
     *  @see #toToken()
     */
    public static MonotonicityCounterexamples fromToken(Token token)
            throws IllegalActionException {
        if (!(token instanceof ArrayToken)) {
            throw new IllegalActionException("Invalid token structure"
                    + "for creating MonotonicityCounterexamples:"
                    + "Token must be ArrayToken.");
        }
        MonotonicityCounterexamples result = new MonotonicityCounterexamples();
        for (Token insideToken : ((ArrayToken) token).arrayValue()) {
            if (!(insideToken instanceof TupleToken)) {
                throw new IllegalActionException("Invalid token structure"
                        + "for creating MonotonicityCounterexamples:"
                        + "ArrayToken must contain TupleTokens.");
            }
            TupleToken tupleToken = (TupleToken) insideToken;
            if (tupleToken.length() != 2) {
                throw new IllegalActionException("Invalid token structure"
                        + "for creating MonotonicityCounterexamples:"
                        + "TupleTokens must be of length 2.");
            }
            ConceptToken x1 = (ConceptToken) tupleToken.getElement(0);
            ConceptToken x2 = (ConceptToken) tupleToken.getElement(1);
            result.add(x1.conceptValue(), x2.conceptValue());
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a pair of concepts as a counterexample to this set.
     *  This means that x1 &le; x2, but for the (non-monotonic) function in
     *  question, f(x1) \not &le; f(x2).
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

    /** Return the sorted entry array.
     *  @return The sorted entry array.
     */
    public ConceptPair[] entryArraySorted() {
        ConceptPair[] entries = new ConceptPair[0];
        entries = entrySet().toArray(entries);
        Arrays.sort(entries, new Comparator<ConceptPair>() {
            @Override
            public int compare(ConceptPair o1, ConceptPair o2) {
                if (o1.lesser.toString().equals(o2.lesser.toString())) {
                    return o1.greater.toString().compareTo(
                            o2.greater.toString());
                } else {
                    return o1.lesser.toString().compareTo(o2.lesser.toString());
                }
            }
        });
        return entries;
    }

    /** Return a set of all the counterexamples.
     *  For each pair in the set returned, (x1, x2),
     *  we have x1 &le; x2, but on the function under consideration
     *  f(x1) \not &le; f(x2)
     *
     *  @return A set of pairs of counterexamples.
     */
    public Set<ConceptPair> entrySet() {
        Set<ConceptPair> entrySet = new HashSet<ConceptPair>();
        for (Concept lesser : _counterexamples.keySet()) {
            for (Concept greater : _counterexamples.get(lesser)) {
                entrySet.add(new ConceptPair(lesser, greater));
            }
        }
        return entrySet;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MonotonicityCounterexamples)) {
            return false;
        }
        MonotonicityCounterexamples mc = (MonotonicityCounterexamples) o;
        if (!mc._counterexamples.keySet().equals(_counterexamples.keySet())) {
            return false;
        }
        for (Concept key : _counterexamples.keySet()) {
            if (!mc._counterexamples.get(key).equals(_counterexamples.get(key))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return _counterexamples.hashCode();
    }

    /** Return the string representation of the counterexample set.
     *  @return The string representation of this set.
     *  @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer resultBuffer = new StringBuffer();

        resultBuffer.append("{");
        for (Entry<Concept, Concept> pair : entryArraySorted()) {
            resultBuffer.append("(" + pair.getKey().toString() + ","
                    + pair.getValue() + ")");
        }
        resultBuffer.append("}");

        return resultBuffer.toString();
    }

    /** Return a representation of the counterexample set as a Ptolemy Token.
     *  This Token is an ArrayToken whose entries are TupleTokens each of
     *  length 2, where the lesser concept is on the left hand side and the
     *  greater concept is on the right hand side.
     *
     *  @return The Token representation of this set.
     *  @exception IllegalActionException If any of the concepts are null
     *  @see #fromToken(Token)
     */
    public ArrayToken toToken() throws IllegalActionException {

        TupleToken[] array = new TupleToken[entrySet().size()];
        int i = 0;
        for (Entry<Concept, Concept> pair : entryArraySorted()) {
            ConceptToken[] conceptArr = { new ConceptToken(pair.getKey()),
                    new ConceptToken(pair.getValue()) };
            TupleToken pairTuple = new TupleToken(conceptArr);
            array[i] = pairTuple;
            i++;
        }

        return new ArrayToken(array);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** A multimap to keep track of the mapping of lesser concepts
     *  to greater concepts.  This must be a multimap, because there
     *  could be multiple counterexamples with the same lesser concept.
     *
     *  If our MultiMap supported Java Generics, this would have a type of
     *    MultiMap<Concept, Concept>
     */
    private MultiHashMap<Concept, Concept> _counterexamples;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Encapsulate counterexample pairs.
     *  These are pairs of the form (x1, x2) where x1 &le; x2, but
     *  f(x1) \not &le; f(x2) for the function under consideration.
     *  Thus, these pairs prove as counterexamples to the
     *  monotonicity of f.
     */
    public static class ConceptPair implements Map.Entry<Concept, Concept> {
        // FindBugs indicates that this should be a static class.

        /** Create a counterexample pair given both of the concepts that  make
         *  up the counterexample.
         *
         *  @param l The lesser concept, i.e. x1
         *  @param g The greater concept, i.e. x2
         */
        public ConceptPair(Concept l, Concept g) {
            lesser = l;
            greater = g;
        }

        /** The lesser of the concepts (i.e. x1)
         */
        public Concept lesser;
        /** The greater of the concepts (i.e. x2)
         */
        public Concept greater;

        /** Return the lesser concept of the counterexample.
         *
         *  @return The lesser concept.
         *  @see java.util.Map.Entry#getKey()
         */
        @Override
        public Concept getKey() {
            return lesser;
        }

        /** Return the greater concept of the counterexample.
         *
         *  @return The greater concept
         *  @see java.util.Map.Entry#getValue()
         *  @see #setValue(Concept)
         */
        @Override
        public Concept getValue() {
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
        @Override
        public Concept setValue(Concept value) {
            return null;
        }
    }

}
