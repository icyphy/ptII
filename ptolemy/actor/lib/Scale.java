/* An actor that outputs a scaled version of the input.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Yellow (yuhong@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.MonotonicFunction;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeLattice;
import ptolemy.graph.CPO;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// Scale
/**
Produce an output token on each firing with a value that is
equal to a scaled version of the input.  The actor is polymorphic
in that it can support any token type that supports multiplication
by the <i>factor</i> parameter.  If the input type is a scalar, the output
type is constrained to be at least as general as both the input and the
<i>factor</i> parameter; if the input is an array, the output is also
an array with the elements scaled. The input can be an array of array,
in which case the elements of the inner most array will be scaled.
For data types where multiplication is not commutative (such
as matrices), whether the factor is multiplied on the left is controlled
by the <i>scaleOnLeft</i> parameter. Setting the parameter to true means
that the factor is  multiplied on the left, and the input
on the right. Otherwise, the factor is multiplied on the right.

@author Edward A. Lee, Steve Neuendorffer
@version $Id$
@since Ptolemy II 0.3
*/

public class Scale extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Scale(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        factor = new Parameter(this, "factor", new IntToken(1));
        scaleOnLeft = new Parameter(this, "scaleOnLeft",
                new BooleanToken(true));

        // set the type constraints.
        output.setTypeAtLeast(new PortParameterFunction(input, factor));

        // icon
        _attachText("_iconDescription", "<svg>\n"
                + "<polygon points=\"-30,-20 30,-4 30,4 -30,20\" "
                + "style=\"fill:white\"/>\n"
                + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The factor.
     *  This parameter can contain any scalar token that supports
     *  multiplication.  The default value of this parameter is the
     *  IntToken 1.
     */
    public Parameter factor;

    /** Multiply on the left.
     *  This parameter controls whether the scale factor is multiplied
     *  on the left. The default value is a boolean token of value true.
     *  Setting is to false will multiply the factor on the right.
     */
    public Parameter scaleOnLeft;


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the type constraints.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        Scale newObject = (Scale)super.clone(workspace);
        PortParameterFunction function =
            new PortParameterFunction(newObject.input, newObject.factor);
        newObject.output.setTypeAtLeast(function);
        return newObject;
    }

    /** Compute the product of the input and the <i>factor</i>.
     *  If there is no input, then produce no output.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        if (input.hasToken(0)) {
            Token in = input.get(0);
            Token factorToken = factor.getToken();
            Token result;
            if (((BooleanToken)scaleOnLeft.getToken()).booleanValue()) {
                // Scale on the left.
                result = _scaleOnLeft(in, factorToken);
            } else {
                // Scale on the right.
                result = _scaleOnRight(in, factorToken);
            }
            output.send(0, result);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                      private methods                      ////

    // Scale the given input token on the left by the given factor.
    private Token _scaleOnLeft(Token input, Token factor)
            throws IllegalActionException {
        if (input instanceof ArrayToken) {
            Token[] argArray = ((ArrayToken)input).arrayValue();
            Token[] result = new Token[argArray.length];
            for (int i = 0; i < argArray.length; i++) {
                result[i] = _scaleOnLeft(argArray[i], factor);
            }

            return new ArrayToken(result);
        } else {
            return factor.multiply(input);
        }
    }

    // Scale the given input token on the right by the given factor.
    private Token _scaleOnRight(Token input, Token factor)
            throws IllegalActionException {
        if (input instanceof ArrayToken) {
            Token[] argArray = ((ArrayToken)input).arrayValue();
            Token[] result = new Token[argArray.length];
            for (int i = 0; i < argArray.length; i++) {
                result[i] = _scaleOnRight(argArray[i], factor);
            }

            return new ArrayToken(result);
        } else {
            return input.multiply(factor);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    // This class implements a monotonic function of the type of a
    // port and a parameter.
    // The function value is determined by:
    // f(portType, paramType) =
    //     UNKNOWN,                  if portType = UNKNOWN
    //     LUB(portType, paramType), if portType is a BaseType
    //     {f(elemType(portType), paramType)}, if portType is an array type.
    //
    // The last case is a recursive one. If portType is an array type, the
    // function value is also an array. The element type of the function value
    // array is the result of a recursive call to this function. This allows
    // the port type to be an array or array, for example.
    private class PortParameterFunction extends MonotonicFunction {

        private PortParameterFunction(TypedIOPort port, Parameter param) {
            _port = port;
            _param = param;
        }

        ///////////////////////////////////////////////////////////////
        ////                       public inner methods            ////

        /** Return the function result.
         *  @return A Type.
         */
        public Object getValue() {
            Type portType = _port.getType();
            Type paramType = _param.getType();
            return compute(portType, paramType);
        }

        /** Return the type variable in this inequality term. If the type
         *  of the input port is not declarad, return an one element array
         *  containing the inequality term representing the type of the port;
         *  otherwise, return an empty array.
         *  @return An array of InequalityTerm.
         */
        public InequalityTerm[] getVariables() {
            InequalityTerm portTerm = _port.getTypeTerm();
            if (portTerm.isSettable()) {
                InequalityTerm[] variable = new InequalityTerm[1];
                variable[0] = portTerm;
                return variable;
            }
            return (new InequalityTerm[0]);
        }

        ///////////////////////////////////////////////////////////////
        ////                      private inner methods            ////

        // compute the function value based on the types of the port
        // and the parameter.
        private Object compute(Type portType, Type paramType) {
            if (portType == BaseType.UNKNOWN) {
                return BaseType.UNKNOWN;
            } else if (portType instanceof ArrayType) {
                Type elementType = ((ArrayType)portType).getElementType();
                Type newElementType = (Type)compute(elementType, paramType);
                return new ArrayType(newElementType);
            } else {
                CPO lattice = TypeLattice.lattice();
                return lattice.leastUpperBound(portType, paramType);
            }
        }

        ///////////////////////////////////////////////////////////////
        ////                       private inner variable          ////

        private TypedIOPort _port;
        private Parameter _param;
    }
}
