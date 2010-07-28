/* 
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.util.MultiHashMap;
import ptolemy.data.ontologies.util.MultiMap;

/**
 *  
 *
 *  @author Ben Lickly
 *  @version $Id$
 *  @since Ptolemy II 9.0
 *  @Pt.ProposedRating Red (blickly)
 *  @Pt.AcceptedRating Red (blickly)
 */
public class MonotonicityCounterexamples {
    
    public MonotonicityCounterexamples() {
        _counterexamples = new MultiHashMap();
        //_counterexamples = new MultiHashMap<Concept, Concept>();
        //_counterexamples = new HashMap<Concept, List<Concept>>();
    }
    
    public void add(Concept x1, Concept x2) {
        _counterexamples.put(x1, x2);
        /*
        List<Concept> bucket = _counterexamples.get(x1);
        if (bucket == null) {
            bucket = new LinkedList<Concept>();
        }
        bucket.add(x2);
        _counterexamples.put(x1, bucket);
        */
    }
    
    public boolean containsCounterexamples() {
        return !_counterexamples.isEmpty();
    }
    

    public Set<Map.Entry<Concept,Concept>> entrySet() {
        Set<Map.Entry<Concept,Concept>> entrySet = new HashSet<Map.Entry<Concept,Concept>>();
        for (Concept lesser : (Set<Concept>)_counterexamples.keySet()) {
            for (Concept greater : (Collection<Concept>)_counterexamples.get(lesser)) {
                entrySet.add(new ConceptPair(lesser, greater));
            }
        }
        return entrySet;
    }
    
    
    //private Map<Concept, List<Concept>> _counterexamples;
    //private MultiMap<Concept, Concept> _counterexamples;
    private MultiMap _counterexamples;
    
    public class ConceptPair implements Map.Entry<Concept,Concept> {
        public ConceptPair(Concept l, Concept g) {
            lesser = l; greater = g;
        }
        public Concept lesser;
        public Concept greater;
        
        
        /**
         *  @return
         *  @see java.util.Map.Entry#getKey()
         */
        @Override
        public Concept getKey() {
            return lesser;
        }
        /**
         *  @return
         *  @see java.util.Map.Entry#getValue()
         */
        @Override
        public Concept getValue() {
            return greater;
        }
        /**
         *  @param value
         *  @return
         *  @see java.util.Map.Entry#setValue(java.lang.Object)
         */
        @Override
        public Concept setValue(Concept value) {
            // TODO Auto-generated method stub
            return null;
        }  
    }
    
}
