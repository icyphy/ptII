/* An actor that disassemble an ArrayToken to a multiport output.

 Copyright (c) 2003-2014 The Regents of the University of California.
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

import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanMatrixToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.ComplexMatrixToken;
import ptolemy.data.ComplexToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.FixMatrixToken;
import ptolemy.data.FixToken;
import ptolemy.data.IntMatrixToken;
import ptolemy.data.IntToken;
import ptolemy.data.LongMatrixToken;
import ptolemy.data.LongToken;
import ptolemy.data.MatrixToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.FixType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.math.Complex;
import ptolemy.math.FixPoint;

///////////////////////////////////////////////////////////////////
//// ArrayToMatrix

/**
 Convert an array to a row or column vector encoded as a matrix token.
 The input array has to have a type that is convertible to a matrix.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (zhouye)
 @Pt.AcceptedRating Red (cxh)
 */
public class ArrayToMatrix extends Transformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be
     *   contained by the proposed container.
     *  @exception NameDuplicationException If the container
     *   already has an actor with this name.
     */
    public ArrayToMatrix(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // Set type constraints.
        input.setTypeAtLeast(ArrayType.ARRAY_BOTTOM);
        output.setTypeAtMost(BaseType.MATRIX);

        rowVector = new Parameter(this, "rowVector");
        rowVector.setTypeEquals(BaseType.BOOLEAN);
        rowVector.setExpression("true");

        // Set the icon.
        _attachText("_iconDescription", "<svg>\n"
                + "<polygon points=\"-15,-15 15,15 15,-15 -15,15\" "
                + "style=\"fill:white\"/>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** If true, then convert the array to a matrix with one row
     *  and multiple columns (a row vector), and otherwise, convert
     *  the input to a matrix with one column and multiple rows
     *  (a column vector). This is a boolean that defaults to true.
     */
    public Parameter rowVector;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class
     *   contains an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ArrayToMatrix newObject = (ArrayToMatrix) super.clone(workspace);
        newObject.input.setTypeAtLeast(ArrayType.ARRAY_BOTTOM);
        newObject.output.setTypeAtMost(BaseType.MATRIX);
        return newObject;
    }

    /** If there is a token at the input, read the array
     *  from the input port, and construct and send to the
     *  output a matrix containing the values from the array.
     *  @exception IllegalActionException If a runtime
     *   type conflict occurs.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (input.hasToken(0)) {
            ArrayToken token = (ArrayToken) input.get(0);
            int size = token.length();
            int rows = 1;
            int columns = size;
            if (!((BooleanToken) rowVector.getToken()).booleanValue()) {
                rows = size;
                columns = 1;
            }
            Type arrayElementType = token.getElementType();
            // Have to look for matching types.
            if (arrayElementType.equals(BaseType.INT)) {
                int[] contents = new int[size];
                for (int i = 0; i < token.length(); i++) {
                    contents[i] = ((IntToken) token.getElement(i)).intValue();
                }
                MatrixToken result = new IntMatrixToken(contents, rows,
                        columns, MatrixToken.DO_NOT_COPY);
                output.broadcast(result);
            } else if (arrayElementType.equals(BaseType.DOUBLE)) {
                double[] contents = new double[size];
                for (int i = 0; i < token.length(); i++) {
                    contents[i] = ((DoubleToken) token.getElement(i))
                            .doubleValue();
                }
                MatrixToken result = new DoubleMatrixToken(contents, rows,
                        columns, MatrixToken.DO_NOT_COPY);
                output.broadcast(result);
            } else if (arrayElementType.equals(BaseType.BOOLEAN)) {
                boolean[][] contents = new boolean[rows][columns];
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < columns; j++) {
                        contents[i][j] = ((BooleanToken) token.getElement(j + i
                                * columns)).booleanValue();
                    }
                }
                MatrixToken result = new BooleanMatrixToken(contents);
                output.broadcast(result);
            } else if (arrayElementType.equals(BaseType.COMPLEX)) {
                Complex[][] contents = new Complex[rows][columns];
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < columns; j++) {
                        contents[i][j] = ((ComplexToken) token.getElement(j + i
                                * columns)).complexValue();
                    }
                }
                MatrixToken result = new ComplexMatrixToken(contents,
                        MatrixToken.DO_NOT_COPY);
                output.broadcast(result);
            } else if (arrayElementType instanceof FixType) {
                FixPoint[][] contents = new FixPoint[rows][columns];
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < columns; j++) {
                        contents[i][j] = ((FixToken) token.getElement(j + i
                                * columns)).fixValue();
                    }
                }
                MatrixToken result = new FixMatrixToken(contents);
                output.broadcast(result);
            } else if (arrayElementType.equals(BaseType.LONG)) {
                long[] contents = new long[size];
                for (int i = 0; i < token.length(); i++) {
                    contents[i] = ((LongToken) token.getElement(i)).longValue();
                }
                MatrixToken result = new LongMatrixToken(contents, rows,
                        columns, MatrixToken.DO_NOT_COPY);
                output.broadcast(result);
            } else {
                // Not a supported type.
                throw new IllegalActionException(
                        this,
                        "Received an input array with elements of type "
                                + arrayElementType
                                + " for which there is no corresponding matrix type.");
            }
        }
    }
}
