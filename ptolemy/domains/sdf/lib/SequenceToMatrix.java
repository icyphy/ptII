/* Bundle a sequence of N by M input tokens into a matrix.

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
package ptolemy.domains.sdf.lib;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.IntToken;
import ptolemy.data.MatrixToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.MatrixType;
import ptolemy.data.type.MonotonicFunction;
import ptolemy.data.type.Type;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// SequenceToMatrix

/**
 This actor bundles a specified number of input tokens into a matrix.
 On each firing, it reads <i>rows</i> times <i>columns</i> input tokens
 and writes one output matrix token with the specified number of rows
 and columns.
 This actor is polymorphic. It can accept inputs of any scalar type
 that has a corresponding matrix type.
 <p>

 @author Edward Lee
 @version $Id$
 @since Ptolemy II 0.4
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (neuendor)
 */
public class SequenceToMatrix extends SDFTransformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SequenceToMatrix(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        output.setTypeAtLeast(new FunctionTerm(input));

        // Set parameters.
        rows = new Parameter(this, "rows");
        rows.setExpression("1");
        columns = new Parameter(this, "columns");
        columns.setExpression("1");

        input_tokenConsumptionRate.setExpression("rows * columns");

        // Set the icon.
        _attachText("_iconDescription", "<svg>\n"
                + "<polygon points=\"-15,-15 15,15 15,-15 -15,15\" "
                + "style=\"fill:white\"/>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The number of rows in the output.  This is an integer that defaults
     *  to 1.
     */
    public Parameter rows;

    /** The number of columns in the output.  This is an integer that defaults
     *  to 1.
     */
    public Parameter columns;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Ensure that the rows and columns parameters are both positive.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the parameters are out of range.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == rows) {
            if (rows.getToken() != null) {
                int rowsValue = ((IntToken) rows.getToken()).intValue();

                if (rowsValue <= 0) {
                    throw new IllegalActionException(this,
                            "Invalid number of rows: " + rowsValue);
                }
            }
        } else if (attribute == columns) {
            if (columns.getToken() != null) {
                int columnsValue = ((IntToken) columns.getToken()).intValue();

                if (columnsValue <= 0) {
                    throw new IllegalActionException(this,
                            "Invalid number of columns: " + columnsValue);
                }
            }
        }

        super.attributeChanged(attribute);
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the type constraints.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        SequenceToMatrix newObject = (SequenceToMatrix) super.clone(workspace);
        newObject.output.setTypeAtLeast(new FunctionTerm(newObject.input));
        return newObject;
    }

    /** Consume the inputs and produce the output matrix.
     *  @exception IllegalActionException If not enough tokens are available.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        int rowsValue = ((IntToken) rows.getToken()).intValue();
        int columnsValue = ((IntToken) columns.getToken()).intValue();
        int length = rowsValue * columnsValue;
        Token[] valueArray = input.get(0, length);

        Token outputToken = MatrixToken.arrayToMatrix(input.getType(),
                valueArray, rowsValue, columnsValue);
        output.send(0, outputToken);
    }

    /** Return true if the input port has enough tokens for this actor to
     *  fire. The number of tokens required is the product of the
     *  values of the <i>rows</i> and <i>columns</i> parameters.
     *  @return boolean True if there are enough tokens at the input port
     *   for this actor to fire.
     *  @exception IllegalActionException If the hasToken() query to the
     *   input port throws it.
     *  @see ptolemy.actor.IOPort#hasToken(int, int)
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        int rowsValue = ((IntToken) rows.getToken()).intValue();
        int columnsValue = ((IntToken) columns.getToken()).intValue();
        int length = rowsValue * columnsValue;

        if (!input.hasToken(0, length)) {
            if (_debugging) {
                _debug("Called prefire(), which returns false.");
            }

            return false;
        } else {
            return super.prefire();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** A monotonic function of the input port type. The result of the
     * function is a matrix type with elements that are the same as
     * the input type. If there is no such matrix type, then the
     * result is unknown.
     */
    private static class FunctionTerm extends MonotonicFunction {

        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.

        /** The constructor takes a port argument so that the clone()
         *  method can construct an instance of this class for the
         *  input port on the clone.
         *  @param port The port
         */
        private FunctionTerm(TypedIOPort port) {
            _port = port;
        }

        ///////////////////////////////////////////////////////////////
        ////                       public inner methods            ////

        /** Return the function result.
         *  @return A Type.
         */
        @Override
        public Object getValue() {
            Type inputType = _port.getType();

            try {
                return MatrixType.getMatrixTypeForElementType(inputType);
            } catch (IllegalActionException ex) {
                return BaseType.UNKNOWN;
            }
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
        ////                       private inner variable          ////
        private TypedIOPort _port;
    }
}
