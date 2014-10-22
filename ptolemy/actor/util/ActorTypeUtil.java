/* Utilities for generating type constraints conveniently.

 Copyright (c) 2007-2014 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.actor.util;

import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.MonotonicFunction;
import ptolemy.data.type.Type;
import ptolemy.data.type.Typeable;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// ActorTypeUtil

/**
 <p>This class includes a number of utility methods and classes that
simplify the generation of type constraints.
 </p>

 @author Stephen neuendorffer
 @version $Id$
@since Ptolemy II 6.1
 @Pt.ProposedRating Red (neuendor)
 @Pt.AcceptedRating Red (neuendor)
 */
public class ActorTypeUtil {

    /** Return a type constraint that can be used to constrain
     *  another typeable object to have a type related to an
     *  array whose element type is the type of the specified
     *  typeable.  A typical usage of this is as follows:
     *  <pre>
     *      output.setTypeAtLeast(ArrayType.arrayOf(input, length));
     *  </pre>
     *  where input and output are ports (this is the type
     *  constraint of SequenceToArray, for example), and length
     *  is a Variable that determines the length of the array.
     *  If <i>length</i> has a constant value, then the resulting type
     *  will have a length specified.  If length does not have a
     *  constant value, the the type will have an indeterminate length.
     *  @param typeable A typeable.
     *  @param length Variable specifying the length of array.
     *  @return An InequalityTerm that can be passed to methods
     *   like setTypeAtLeast() of the Typeable interface.
     *  @exception IllegalActionException If the specified typeable
     *   cannot be set to an array type.
     */
    public static InequalityTerm arrayOf(Typeable typeable, Variable length)
            throws IllegalActionException {
        return new TypeableSizedArrayVariableTypeTerm(typeable, length);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** An InequalityTerm representing an array types whose elements
     *  have the type of the specified typeable.  The purpose of this class
     *  is to defer to as late as possible actually accessing
     *  the type of the typeable, since it may change dynamically.
     *  This term is not variable and cannot be set.
     */
    private static class TypeableSizedArrayVariableTypeTerm extends
            MonotonicFunction {

        /** Construct a term that will defer to the type of the
         *  specified typeable.
         *  @param typeable The object to defer requests to.
         */
        public TypeableSizedArrayVariableTypeTerm(Typeable typeable,
                Variable lengthVariable) {
            _typeable = typeable;
            _lengthVariable = lengthVariable;

        }

        ///////////////////////////////////////////////////////////////
        ////                   public inner methods                ////

        /** Return an array type with element types given by the associated typeable.
         *  @return An ArrayType.
         *  @exception IllegalActionException If the type of the associated typeable
         *   cannot be determined.
         */
        @Override
        public Object getValue() throws IllegalActionException {
            ConstVariableModelAnalysis analysis = ConstVariableModelAnalysis
                    .getAnalysis(_lengthVariable);
            if (analysis.isConstant(_lengthVariable)) {
                Token lengthToken = analysis.getConstantValue(_lengthVariable);
                int length = ((IntToken) lengthToken).intValue();
                return _getArrayTypeRaw(length);
            }
            return _getArrayTypeRaw();
        }

        /** Return an array of size zero.
         *  @return An array of InequalityTerm.
         */
        @Override
        public InequalityTerm[] getVariables() {
            InequalityTerm[] array = new InequalityTerm[1];
            array[0] = _typeable.getTypeTerm();
            return array;
        }

        ///////////////////////////////////////////////////////////////
        ////                   private methods                     ////

        /** Get an array type with element type matching the type
         *  of the associated typeable.
         *  @return An array type for the associated typeable.
         *  @exception IllegalActionException If the type of the typeable
         *   cannot be determined.
         */
        private ArrayType _getArrayTypeRaw() throws IllegalActionException {
            Type type = _typeable.getType();
            if (_arrayType == null || !_arrayType.getElementType().equals(type)) {
                _arrayType = new ArrayType(type);
            }
            return _arrayType;
        }

        /** Get an array type with element type matching the type
         *  of the associated typeable.
         *  @return An array type for the associated typeable.
         *  @exception IllegalActionException If the type of the typeable
         *   cannot be determined.
         */
        private ArrayType _getArrayTypeRaw(int length)
                throws IllegalActionException {
            Type type = _typeable.getType();
            if (_arrayType == null || !_arrayType.getElementType().equals(type)) {
                _arrayType = new ArrayType(type, length);
            }
            return _arrayType;
        }

        ///////////////////////////////////////////////////////////////
        ////                   private members                     ////

        /** The associated typeable. */
        private Typeable _typeable;

        /** The array type with element types matching the typeable. */
        private ArrayType _arrayType;

        private Variable _lengthVariable;
    }
}
