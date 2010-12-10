/* Abstract class that provides helper methods for applying mathematical
 * operations to token-based infinite concepts for a particular ontology.
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

import ptolemy.data.BooleanToken;
import ptolemy.kernel.util.IllegalActionException;

/** Abstract class that provides helper methods for applying mathematical
 *  operations to token-based infinite concepts for a particular ontology.
 *  Derived subclasses should be implemented for each ontology that has any
 *  flat token infinite concepts and requires math operations on its concepts
 *  for concept functions within its ontology solver.
 * 
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 9.0
 *  @Pt.ProposedRating Red (blickly)
 *  @Pt.AcceptedRating Red (blickly)
 */
public abstract class ExpressionOperationsForInfiniteConcepts {
    
    /** Create a new ExpressionOperationsForInfiniteConcepts object.
     *  @param ontology The ontology over which these operations are defined.
     */
    public ExpressionOperationsForInfiniteConcepts(Ontology ontology) {
        _ontology = ontology;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                          public methods                   ////
    
    /** Add two token infinite concepts and return the resulting concept.
     * 
     *  @param addend1 The first addend concept.
     *  @param addend2 The second addend concept.
     *  @return The concept that represents the sum of the two input concepts.
     *  @throws IllegalActionException Thrown if the operation cannot be
     *   performed on the input concepts.
     */
    public abstract Concept add(Concept addend1, Concept addend2)
        throws IllegalActionException;
    
    /** Divide two token infinite concepts and return the resulting concept.
     * 
     *  @param dividend The dividend concept.
     *  @param divisor The divisor concept.
     *  @return The concept that represents the quotient of the two input
     *   concepts.
     *  @throws IllegalActionException Thrown if the operation cannot be
     *   performed on the input concepts.
     */
    public abstract Concept divide(Concept dividend, Concept divisor)
        throws IllegalActionException;
    
    /** Return the boolean token that represents boolean true if the left concept
     *  value is less than the right concept values, and false otherwise.
     * 
     *  @param left The left concept.
     *  @param right The right concept.
     *  @return The boolean token that represents true if the left concept
     *   value is less than the right concept value, or false otherwise.
     *  @throws IllegalActionException Thrown if the operation cannot be
     *   performed on the input concepts.
     */
    public abstract BooleanToken isLessThan(Concept left, Concept right)
        throws IllegalActionException;
    
    /** Multiply two token infinite concepts and return the resulting concept.
     * 
     *  @param factor1 The first factor.
     *  @param factor2 The second factor.
     *  @return The concept that represents the product of the two input
     *    concepts.
     *  @throws IllegalActionException Thrown if the operation cannot be
     *   performed on the input concepts.
     */
    public abstract Concept multiply(Concept factor1, Concept factor2)
        throws IllegalActionException;
    
    /** Return the concept that represents the multiplicative reciprocal
     *  of the given token infinite concept.
     * 
     *  @param concept The input concept.
     *  @return The concept that represents the multiplicative reciprocal
     *   of the given token infinite concept.
     *  @throws IllegalActionException Thrown if the operation cannot be
     *   performed on the input concept.
     */
    public abstract Concept reciprocal(Concept concept)
        throws IllegalActionException;
    
    /** Subtract two token infinite concepts and return the resulting concept.
     * 
     *  @param subtractor The first subtraction argument.
     *  @param subtractee The second subtraction argument.
     *  @return The concept that represents the difference of the two input
     *   concepts.
     *  @throws IllegalActionException Thrown if the operation cannot be
     *   performed on the input concepts.
     */
    public abstract Concept subtract(Concept subtractor, Concept subtractee)
        throws IllegalActionException;
    
    /** Return the concept that represents the zero concept for this
     *  token infinite concept.
     * 
     *  @param concept The input concept.
     *  @return The concept that represents the zero concept for the given
     *   token infinite concept.
     *  @throws IllegalActionException Thrown if the operation cannot be
     *   performed on the input concept.
     */
    public abstract Concept zero(Concept concept)
        throws IllegalActionException;
    
    ///////////////////////////////////////////////////////////////////
    ////                          protected variables              ////
    
    /** The ontology for which this object defines operations. */
    protected Ontology _ontology;
}
