/* A simple singleton type for all ConceptTokens.
 *
 * Copyright (c) 2012-2013 The Regents of the University of California. All
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

import ptolemy.data.Token;
import ptolemy.data.type.StructuredType;
import ptolemy.data.type.Type;
import ptolemy.graph.CPO;

/** A simple singleton type for all ConceptTokens.
 *
 *  @author Ben Lickly
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (blickly)
 *  @Pt.AcceptedRating Red (blickly)
 */
public class ConceptType extends StructuredType implements Cloneable {

    /** The representative type for all ConceptTokens.
     */
    public static final ConceptType CONCEPT = new ConceptType();

    /** Convert the specified token into a ConceptToken.
     *  @param token Any token.
     *  @return The argument unchanged, if it was a ConceptToken.
     */
    @Override
    public ConceptToken convert(Token token) {
        if (!(token instanceof ConceptToken)) {
            throw new IllegalArgumentException(
                    Token.notSupportedIncomparableConversionMessage(token,
                            toString()));
        }
        return (ConceptToken) token;
    }

    /** Return the class for tokens that this type represents.
     *  @return ConceptToken.class
     */
    @Override
    public Class<ConceptToken> getTokenClass() {
        return ConceptToken.class;
    }

    /** Test if the argument is compatible with this type.
     *  @param type A type.
     *  @return True if it is a ConceptType.
     */
    @Override
    public boolean isCompatible(Type type) {
        return type instanceof ConceptType;
    }

    /** Test if this type is constant.
     *  @return True.
     */
    @Override
    public boolean isConstant() {
        return true;
    }

    /** Test if this Type corresponds to an instantiable token class.
     *  @return True.
     */
    @Override
    public boolean isInstantiable() {
        return true;
    }

    /** Detect if the specified type is a substitution instance of this type.
     *  @param type A type to check.
     *  @return True, if the given type is equal to ConceptType.
     */
    @Override
    public boolean isSubstitutionInstance(Type type) {
        return equals(type);
    }

    /** Do nothing, since this is a singleton type.
     *  @return The instance being cloned.
     */
    @Override
    public ConceptType clone() {
        return this;
    }

    /** Do nothing, since there are no unknown subtypes.
     *  @param type Ignored.
     */
    @Override
    public void initialize(Type type) {
    }

    /** Compare this type with the specified type.
     *  @param type Another instance of the ConceptType singleton type.
     *  @return CPO.SAME, since this is a singleton type.
     */
    @Override
    protected int _compare(StructuredType type) {
        return CPO.SAME;
    }

    /** Return the representative of this type.
     *  @return The unique representative of this singleton type.
     */
    @Override
    protected StructuredType _getRepresentative() {
        return CONCEPT;
    }

    /** Take the greatest lower bound of this type with the specified type.
     *  @param type Another instance of the ConceptType singleton type.
     *  @return The unique representative of this singleton type.
     */
    @Override
    protected StructuredType _greatestLowerBound(StructuredType type) {
        return CONCEPT;
    }

    /** Take the least upper bound of this type with the specified type.
     *  @param type Another instance of the ConceptType singleton type.
     *  @return The unique representative of this singleton type.
     */
    @Override
    protected StructuredType _leastUpperBound(StructuredType type) {
        return CONCEPT;
    }

}
