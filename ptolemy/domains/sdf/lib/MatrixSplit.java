/* Split an input matrix into a sequence of input matrices.

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
package ptolemy.domains.sdf.lib;

import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.MatrixToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// MatrixSplit

/**
 This actor splits an input matrix into a sequence of matrices.
 This actor reads 1 input matrix and produces N*M output matrices,
 where N is the size of the <i>rowSplit</i> array, and M is
 the size of the <i>columnSplit</i> parameter. The matrices
 are produced from submatrices extracted left-to-right,
 top-to-bottom, in a raster scan pattern.
 For example, if <i>rowSplit</i> = {1, 2},
 <i>columnSplit</i> = {2, 1}, and the input matrix is
 as follows:
 <pre>
   1  2  3
   4  5  6
   7  8  9
 </pre>
 then the first matrix out is a column vector:
  <pre>
   1
   4
 </pre>
 The second matrix out is
  <pre>
   2  3
   5  6
 </pre>
 The third is
  <pre>
   7
 </pre>
 (a 1x1 matrix) and the fourth is
  <pre>
   8  9
 </pre>
 a row vector.
 If the input does not have enough elements to fill the specified
 output matrices, then zeros (of the same type as the input elements)
 are used. If the input is larger than is required to fill the specified
 output, then the additional values are discarded.
 <p>

 @author Edward Lee
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (neuendor)
 */
public class MatrixSplit extends SDFTransformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public MatrixSplit(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // Set parameters.
        rowSplit = new Parameter(this, "rowSplit");
        rowSplit.setTypeEquals(new ArrayType(BaseType.INT));
        rowSplit.setExpression("{1}");

        columnSplit = new Parameter(this, "columnSplit");
        columnSplit.setTypeEquals(new ArrayType(BaseType.INT));
        columnSplit.setExpression("{1}");

        output_tokenProductionRate
                .setExpression("rowSplit.length() * columnSplit.length()");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** An array specifying the number of columns in the output matrices.
     *  This is an array of integers, with default {1}.
     */
    public Parameter columnSplit;

    /** An array specifying the number of rows in the output matrices.
     *  This is an array of integers, with default {1}.
     */
    public Parameter rowSplit;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Consume the input matrix and produce the output matrices.
     *  @exception IllegalActionException If there is an error reading the inputs
     *   or parameters.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (!input.hasToken(0)) {
            return;
        }
        ArrayToken columns = (ArrayToken) columnSplit.getToken();
        ArrayToken rows = (ArrayToken) rowSplit.getToken();
        int[] columnsValue = new int[columns.length()];
        for (int i = 0; i < columnsValue.length; i++) {
            columnsValue[i] = ((IntToken) columns.getElement(i)).intValue();
        }
        int[] rowsValue = new int[rows.length()];
        for (int i = 0; i < rowsValue.length; i++) {
            rowsValue[i] = ((IntToken) rows.getElement(i)).intValue();
        }
        MatrixToken inputValue = (MatrixToken) input.get(0);
        MatrixToken[][] result = inputValue.split(rowsValue, columnsValue);
        for (MatrixToken[] element : result) {
            for (int j = 0; j < element.length; j++) {
                output.send(0, element[j]);
            }
        }
    }
}
