/* An actor that outputs the absolute value of the input.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.actor.lib;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.Token;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.MonotonicFunction;
import ptolemy.data.type.Type;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// AbsoluteValue

/**
 Produce an output token on each firing with a value that is
 equal to the absolute value of the input. The input can have any
 scalar type, or it can be an array of scalars (or an array of arrays
 of scalars, etc.). If the input type is not Complex, the output has the
 same type as the input. If the input type is Complex, the output
 type is Double, in which case, the output value is the magnitude
 of the input complex.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.3
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Yellow (yuhong)
 */
public class AbsoluteValue extends Transformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public AbsoluteValue(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        output.setTypeAtLeast(new TypeOfAbsoluteValue(input));
        // FIXME: This actor accepts a rather complicated set
        // of input types. Is there a way to express the constraints?
        // input.setTypeAtMost(BaseType.SCALAR);
        // Also, type constraints do not propagate backwards from
        // the output to the input.
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the type constraints.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        AbsoluteValue newObject = (AbsoluteValue) super.clone(workspace);
        newObject.output
                .setTypeAtLeast(new TypeOfAbsoluteValue(newObject.input));
        return newObject;
    }

    /** Compute the absolute value of the input.  If there is no input, then
     *  produce no output.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (input.hasToken(0)) {
            output.send(0, _absoluteValue(input.get(0)));
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** If the argument type is an array, then return an array
     *  type with element types recursively defined by this method.
     *  Otherwise, if the argument type is complex, then return
     *  double. Otherwise, return the argument type.
     *  @return The absolute value of the input.
     *  @exception IllegalActionException If there is no absolute value
     *   operation for the specified token.
     */
    private Token _absoluteValue(Token input) throws IllegalActionException {
        if (input instanceof ArrayToken) {
            int length = ((ArrayToken) input).length();
            Token[] result = new Token[length];
            for (int i = 0; i < length; i++) {
                result[i] = _absoluteValue(((ArrayToken) input).getElement(i));
            }
            return new ArrayToken(result);
        } else if (input instanceof ScalarToken) {
            return ((ScalarToken) input).absolute();
        } else {
            throw new IllegalActionException(this,
                    "AbsoluteValue only accepts scalar inputs or arrays of scalars.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** This class implements a monotonic function of the input port
     *  type. It returns the type of the absolute value of the input
     *  port type. If the input type is an array, then the function
     *  returns an array type with element types recursively defined
     *  by this same function.
     *  Otherwise, if the port type is complex, then return
     *  double. Otherwise, return the port
     *  type.
     */
    private static class TypeOfAbsoluteValue extends MonotonicFunction {

        // FindBugs suggested making this class a static inner class:
        //
        // "This class is an inner class, but does not use its embedded
        // reference to the object which created it. This reference makes
        // the instances of the class larger, and may keep the reference
        // to the creator object alive longer than necessary. If
        // possible, the class should be made into a static inner class."

        // The constructor takes a port argument so that the clone()
        // method can construct an instance of this class for the
        // input port on the clone.
        private TypeOfAbsoluteValue(TypedIOPort port) {
            _port = port;
        }

        ///////////////////////////////////////////////////////////////
        ////                       public inner methods            ////

        /** Return the function result.
         *  @return A Type.
         */
        @Override
        public Object getValue() {
            return _outputType(_port.getType());
        }

        /** Return the variables in this term. If the type of the input port
         *  is a variable, return a one element array containing the
         *  InequalityTerm of that port; otherwise, return an array of zero
         *  length.
         *  @return An array of InequalityTerm.
         */
        @Override
        public InequalityTerm[] getVariables() {
            if (_port.getTypeTerm().isSettable()) {
                InequalityTerm[] variable = new InequalityTerm[1];
                variable[0] = _port.getTypeTerm();
                return variable;
            } else {
                return new InequalityTerm[0];
            }
        }

        ///////////////////////////////////////////////////////////////
        ////                      private inner methods            ////

        /** If the argument type is an array, then return an array
         *  type with element types recursively defined by this method.
         *  Otherwise, if the argument type is complex, then return
         *  double. Otherwise, return the argument type.
         *  @return A Type.
         */
        private Type _outputType(Type inputType) {
            if (inputType == BaseType.COMPLEX) {
                return BaseType.DOUBLE;
            } else if (inputType instanceof ArrayType) {
                Type elementType = ((ArrayType) inputType).getElementType();
                return new ArrayType(_outputType(elementType));
            } else {
                return inputType;
            }
        }

        ///////////////////////////////////////////////////////////////
        ////                       private inner variable          ////
        private TypedIOPort _port;
    }
}
