/* Unbundle a matrix into a sequence of N by M tokens.

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
@AcceptedRating Red (neuendor@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.lib;

import ptolemy.actor.Director;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.MatrixToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.MonotonicFunction;
import ptolemy.data.type.Type;
import ptolemy.data.type.UnsizedMatrixType;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// MatrixToSequence
/**
This actor unbundles a matrix into a sequence of output tokens.
On each firing, it writes the elements of the array to the output
as a sequence of output tokens.
and writes one output matrix token with the specified number of rows
and columns.
If the <i>enforceMatrixSize</i> parameter true, then if an input
matrix does not match <i>rows</i> and <i>columns</i>, then
the fire() method will throw an exception.
This feature is important in domains, such as SDF,
that do static scheduling based on production and consumption
rates.  For other domains, such as DE and PN, the <i>enforceMatrixSize</i>
parameter can be set to false, in which case the <i>rows</i> and
<i>columns</i> parameters will be ignored.
This actor is polymorphic. It can accept any matrix input and the output
will have the type of the elements of the matrix.
<p>
@author Edward Lee
@version $Id$
@since Ptolemy II 0.4
*/

public class MatrixToSequence extends SDFTransformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public MatrixToSequence(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        input.setTypeAtMost(BaseType.MATRIX);
        output.setTypeAtLeast(new FunctionTerm(input));
        
        // Set parameters.
        rows = new Parameter(this, "rows");
        rows.setExpression("1");
        columns = new Parameter(this, "columns");
        columns.setExpression("1");
        enforceMatrixSize = new Parameter(this, "enforceMatrixSize");
        enforceMatrixSize.setExpression("true");
        enforceMatrixSize.setTypeEquals(BaseType.BOOLEAN);

        output_tokenProductionRate.setExpression("rows * columns");
 
        // Set the icon.
        _attachText("_iconDescription", "<svg>\n" +
                "<polygon points=\"-15,-15 15,15 15,-15 -15,15\" "
                + "style=\"fill:white\"/>\n" +
                "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The number of rows in the input.  This is an integer that defaults
     *  to 1.
     */
    public Parameter rows;

    /** The number of columns in the input.  This is an integer that defaults
     *  to 1.
     */
    public Parameter columns;

    /** If true, then enforce the <i>rows</i> and <i>columns</i> parameters by
     *  throwing an exception if it is violated. This is a boolean
     *  that defaults to true.
     */
    public Parameter enforceMatrixSize;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the argument is <i>rows</i> or <i>columns</i>, then
     *  set the production rate of the output port, and invalidate
     *  the schedule of the director.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the parameters are out of range.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == rows) {
            int rowsValue = ((IntToken)rows.getToken()).intValue();
            if (rowsValue <= 0) {
                throw new IllegalActionException(this,
                        "Invalid number of rows: " + rowsValue);
            }   
        } else if(attribute == columns) {
            int columnsValue = ((IntToken)columns.getToken()).intValue();
            if (columnsValue <= 0) {
                throw new IllegalActionException(this,
                        "Invalid number of columns: " + columnsValue);
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
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        MatrixToSequence newObject = (MatrixToSequence)super.clone(workspace);

        newObject.input.setTypeAtMost(BaseType.MATRIX);
        newObject.output.setTypeAtLeast(new FunctionTerm(newObject.input));
        return newObject;
    }

    /** Consume the input and produce the output sequence. If there
     *  is no input token, do nothing.
     *  @exception IllegalActionException If not enough tokens are available.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        if (!input.hasToken(0)) return;
        MatrixToken token = (MatrixToken)input.get(0);
        int actualRowCount = token.getRowCount();
        int actualColumnCount = token.getColumnCount();
        boolean enforce = ((BooleanToken)enforceMatrixSize.getToken())
            .booleanValue();
        if (enforce) {
            int rowsValue = ((IntToken)rows.getToken()).intValue();
            int columnsValue = ((IntToken)columns.getToken()).intValue();
            if (actualRowCount != rowsValue
                    || actualColumnCount != columnsValue) {
                throw new IllegalActionException(this,
                        "The input matrix size does not"
                        + " match what the actor requires.");
            }
        }
        for (int i = 0; i < actualRowCount; i++) {
            for (int j = 0; j < actualColumnCount; j++) {
                output.send(0, token.getElementAsToken(i, j));
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    // This class implements a monotonic function of the input port
    // type. The result of the function is a matrix type with elements
    // that are the same as the input type. If there is no such matrix
    // type, then the result is unknown.
    // NOTE: This is largely copied from AbsoluteValue.  Should
    // there be a common base class?  It is also essentially identical
    // the inner class in SequenceToMatrix.
    private class FunctionTerm extends MonotonicFunction {

        // The constructor takes a port argument so that the clone()
        // method can construct an instance of this class for the
        // input port on the clone.
        private FunctionTerm(TypedIOPort port) {
            _port = port;
        }

        ///////////////////////////////////////////////////////////////
        ////                       public inner methods            ////

        /** Return the function result.
         *  @return A Type.
         */
        public Object getValue() {
            Type inputType = _port.getType();
            if (!(inputType instanceof UnsizedMatrixType)) {
                return BaseType.UNKNOWN;
            }
            return ((UnsizedMatrixType)inputType).getElementType();
        }

        /** Return the variables in this term. If the type of the input port
         *  is a variable, return a one element array containing the
         *  InequalityTerm of that port; otherwise, return an array of zero
         *  length.
         *  @return An array of InequalityTerm.
         */
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
