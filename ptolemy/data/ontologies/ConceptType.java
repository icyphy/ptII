/* 
 * 
 * Copyright (c) 2012 The Regents of the University of California. All
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
import ptolemy.data.TupleToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.StructuredType;
import ptolemy.data.type.Type;
import ptolemy.graph.CPO;
import ptolemy.kernel.util.IllegalActionException;

/**
 *  @author Ben Lickly
 *  @version $Id$
 *  @since Ptolemy II 9.0
 *  @Pt.ProposedRating Red (blickly)
 *  @Pt.AcceptedRating Red (blickly)
 *
 */
public class ConceptType extends StructuredType {

    public static final ConceptType CONCEPT = new ConceptType();
    /**
     *  
     */
    public ConceptType() {
    }

    /**
     *  @param t
     *  @return
     *  @throws IllegalActionException
     *  @see ptolemy.data.type.BaseType#convert(ptolemy.data.Token)
     */
    @Override
    public ConceptToken convert(Token token) throws IllegalActionException {
        if (!(token instanceof ConceptToken)) {
            throw new IllegalArgumentException(
                    Token.notSupportedIncomparableConversionMessage(token,
                            toString()));
        }
        return (ConceptToken)token;
    }

    /**
     *  @return
     *  @see ptolemy.data.type.Type#getTokenClass()
     */
    @Override
    public Class getTokenClass() {
        return ConceptToken.class;
    }

    /**
     *  @param type
     *  @return
     *  @see ptolemy.data.type.Type#isCompatible(ptolemy.data.type.Type)
     */
    @Override
    public boolean isCompatible(Type type) {
        return (type instanceof ConceptType);
    }

    /**
     *  @return
     *  @see ptolemy.data.type.Type#isConstant()
     */
    @Override
    public boolean isConstant() {
        return true;
    }

    /**
     *  @return
     *  @see ptolemy.data.type.Type#isInstantiable()
     */
    @Override
    public boolean isInstantiable() {
        return true;
    }

    /**
     *  @param type
     *  @return
     *  @see ptolemy.data.type.Type#isSubstitutionInstance(ptolemy.data.type.Type)
     */
    @Override
    public boolean isSubstitutionInstance(Type type) {
        return equals(type);
    }

    /**
     *  @return
     *  @throws CloneNotSupportedException
     *  @see ptolemy.data.type.StructuredType#clone()
     */
    @Override
    public ConceptType clone() throws CloneNotSupportedException {
        return new ConceptType();
    }

    /**
     *  @param type
     *  @see ptolemy.data.type.StructuredType#initialize(ptolemy.data.type.Type)
     */
    @Override
    public void initialize(Type type) {

    }

    /**
     *  @param type
     *  @return
     *  @see ptolemy.data.type.StructuredType#_compare(ptolemy.data.type.StructuredType)
     */
    @Override
    protected int _compare(StructuredType type) {
        return CPO.SAME;
    }

    /**
     *  @return
     *  @see ptolemy.data.type.StructuredType#_getRepresentative()
     */
    @Override
    protected StructuredType _getRepresentative() {
        return CONCEPT;
    }

    /**
     *  @param type
     *  @return
     *  @see ptolemy.data.type.StructuredType#_greatestLowerBound(ptolemy.data.type.StructuredType)
     */
    @Override
    protected StructuredType _greatestLowerBound(StructuredType type) {
        return CONCEPT;
    }

    /**
     *  @param type
     *  @return
     *  @see ptolemy.data.type.StructuredType#_leastUpperBound(ptolemy.data.type.StructuredType)
     */
    @Override
    protected StructuredType _leastUpperBound(StructuredType type) {
        return CONCEPT;
    }

}
