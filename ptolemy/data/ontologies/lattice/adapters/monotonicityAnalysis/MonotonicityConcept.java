/* A concept that represents the monotoncity of an expression.
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

/** A concept that represents the monotoncity of an expression.
 *  
 *  Note that for an arbitrary expression, it will not have a
 *  monotonicity concept of simply Monotonic, Constant, etc.
 *  Rather, the expression will have a monotonicity that depends
 *  on it's free variables.  For example, an expression of the form
 *  <code>
 *    (x &lt;= Concept1) ? Bottom :
 *    (y &lt;= Concept2) ? Top :
 *    Concept1
 *  </code>
 *  may have a monotonicity that is monotonic with respect to the
 *  variable x, but antimonotonic with respect to y (and constant
 *  with respect to all other variables).
 *  
 *  This class represents exactly such concepts, representing them as
 *  { x : Monotonic, y : Antimonotonic }, in a manner and syntax
 *  similar to records of the Ptolemy II type system.  In records,
 *  however, accessing an undefined tag is an error, whereas in
 *  expressions, they are simply constant with respect to any
 *  variables that are not free.
 *  
 *  @author Ben Lickly
 *  @version $Id$
 *  @since Ptolemy II 9.0
 *  @Pt.ProposedRating Red (blickly)
 *  @Pt.AcceptedRating Red (blickly)
 *
 */
public class MonotonicityConcept extends InfiniteConcept {

    ///////////////////////////////////////////////////////////////////
    ////                    constructors/factories                 ////
    
    /** Create a new monotonicity concept, belonging to the given
     *  ontology.
     * 
     *  @param ontology The finite ontology to which this belongs.
     *    This should be the 4 element monotonicity lattice if we
     *    are really going to be doing inference on monotonicity
     *    of expressions.
     *  @return The newly created MonotonicityConcept.
     *  @throws IllegalActionException If the base class throws it.
     */
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
    
    /** Get the monotonicity of this concept with respect to a specific
     *  variable.  While the overall monotonicity of an expression
     *  cannot be represented so simply, the monotonicity with
     *  respect to a single variable can be represented as one
     *  of:
     *  <ul>
     *    <li>Constant</li>
     *    <li>Monotonic</li>
     *    <li>Antimonotonic</li>
     *    <li>General</li>
     *  </ul>
     *  This method returns one these concepts.
     *  @param variableName The variable whose monotonicity we are querying.
     *  @return The monotonicity of this concept with respect to the given
     *    variable; one of Constant, Monotonic, Antimonotonic, or General.
     */
    public FiniteConcept getMonotonicity(String variableName) {
        if (_variableToMonotonicity.containsKey(variableName)) {
            return _variableToMonotonicity.get(variableName);
        }
        return (FiniteConcept)getOntology().getGraph().bottom();
    }

    /** Set the monotonicity of this concept with respect to a specific
     *  variable.
     *
     *  @param variable The variable whose monotonicity we are querying.
     *  @param monotonicity The monotonicity of this concept with respect
     *    to the given variable.
     *  @see MonotonicityConcept#getMonotonicity(String)
     */
    public void putMonotonicity(String variable, FiniteConcept monotonicity) {
        if (monotonicity.equals((FiniteConcept)getOntology().getGraph().bottom())) {
            _variableToMonotonicity.remove(monotonicity);
        } else {
            _variableToMonotonicity.put(variable, monotonicity);
        }
    }
    
    /** Return the string representation of this monotonicity concept.
     *  Note that the syntax here is similar to that used for records
     *  (e.g. { x : Monotonic, y : Antimonotonic }).
     *  
     *  @return The string representation of this concept.
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
    ////                    protected constructors                 ////

    /** Create a new Monotonicity concept, belonging to the given
     *  ontology.
     * 
     *  @param ontology The finite ontology to which this belongs.
     *    This should be the 4 element monotonicity lattice if we
     *    are really going to be doing inference on monotonicity
     *    of expressions.
     *  @throws NameDuplicationException Should never be thrown.
     *  @throws IllegalActionException If the base class throws it.
     */
    protected MonotonicityConcept(Ontology ontology)
            throws IllegalActionException, NameDuplicationException {
          super(ontology, "InfiniteMonotonicityConcept_" + _conceptNumber);
          ++_conceptNumber;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** Mapping of free variable names to monotonicity values.
     *  The map must be sorted to ensure that the toString method
     *  returns a unique representation of the concept.
     */
    private SortedMap<String, FiniteConcept> _variableToMonotonicity =
        new TreeMap<String, FiniteConcept>();
    

    /** Used for internal bookkeeping to make sure that generated
     *  concept names are unique.
     */
    private static int _conceptNumber = 0;

}
