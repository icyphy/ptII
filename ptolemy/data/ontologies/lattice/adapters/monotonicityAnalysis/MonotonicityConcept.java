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

import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.FiniteConcept;
import ptolemy.data.ontologies.InfiniteConcept;
import ptolemy.data.ontologies.Ontology;
import ptolemy.graph.CPO;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 *  @author Ben Lickly
 *  @version $Id$
 *  @since Ptolemy II 9.0
 *  @Pt.ProposedRating Red (blickly)
 *  @Pt.AcceptedRating Red (blickly)
 *
 */
public class MonotonicityConcept extends InfiniteConcept {

    /** 
     * 
     *  @param ontology
     *  @param name
     *  @throws NameDuplicationException
     *  @throws IllegalActionException
     */
    public MonotonicityConcept(Ontology ontology)
            throws IllegalActionException, NameDuplicationException {
          super(ontology, "InfiniteMonotonicityConcept_" + _conceptNumber);
          ++_conceptNumber;
    }
    
    public static MonotonicityConcept createMonotonicityConcept(Ontology ontology)
            throws IllegalActionException {
        try {
            return new MonotonicityConcept(ontology);
        } catch (NameDuplicationException e) {
            throw new IllegalActionException(
                    "Name conflict with automatically generated infinite concept name.\n"
                  + "This should never happen."
                  + "Original exception:" + e.toString());
        }
    }
    
    private static int _conceptNumber = 0;
    
    ///////////////////////////////////////////////////////////////////
    ////                          public methods                   ////

    /**
     *  @param concept
     *  @return
     *  @throws IllegalActionException
     *  @see ptolemy.data.ontologies.Concept#isAboveOrEqualTo(ptolemy.data.ontologies.Concept)
     */
    public int compare(Concept concept) throws IllegalActionException {
        if (!(concept.getOntology().equals(getOntology()))) {
            throw new IllegalActionException(this,
                    "Attempt to compare elements from two distinct ontologies");
        }
        if (concept instanceof FiniteConcept) {
            return CPO.INCOMPARABLE;
        }
        MonotonicityConcept righthandSide = (MonotonicityConcept) concept;

        Set<String> keys = _variableToMonotonicity.keySet();
        keys.addAll(righthandSide._variableToMonotonicity.keySet());
        
        boolean seenHigher = false;
        boolean seenLower = false;
        boolean seenIncomparable = false;
        for (String key : keys) {
            int result = getMonotonicity(key)
                    .compare(righthandSide.getMonotonicity(key));
            switch (result) {
            case CPO.HIGHER:       seenHigher = true; break;
            case CPO.LOWER:        seenLower = true; break;
            case CPO.INCOMPARABLE: seenIncomparable = true; break;
            }
        }
        if (!seenHigher && !seenLower && !seenIncomparable) {
            return CPO.SAME;
        } else if (seenHigher && !seenLower && !seenIncomparable) {
            return CPO.HIGHER;
        } else if (seenLower && !seenHigher && !seenIncomparable) {
            return CPO.LOWER;
        } else {
            return CPO.INCOMPARABLE;            
        }
    }
    
    public FiniteConcept getMonotonicity(String variableName) {
        if (_variableToMonotonicity.containsKey(variableName)) {
            return _variableToMonotonicity.get(variableName);
        }
        return (FiniteConcept)getOntology().getGraph().bottom();
    }

    public void putMonotonicity(String variable, FiniteConcept monotonicity) {
        if (monotonicity.equals((FiniteConcept)getOntology().getGraph().bottom())) {
            _variableToMonotonicity.remove(monotonicity);
        } else {
            _variableToMonotonicity.put(variable, monotonicity);
        }
    }
    
    /**
     *  @return
     *  @see ptolemy.data.ontologies.Concept#toString()
     */
    public String toString() {
        StringBuffer result = new StringBuffer("{ ");
        for (String key : _variableToMonotonicity.keySet()) {
            result.append(key);
            result.append(":");
            result.append(getMonotonicity(key));
            result.append(' ');
        }
        result.append('}');
        return result.toString();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** Mapping of free variable names to monotonicity values.
     *  The map must be sorted to ensure that the toString method
     *  returns a unique representation of the concept.
     */
    private SortedMap<String, FiniteConcept> _variableToMonotonicity =
        new TreeMap<String, FiniteConcept>();

}
