/* Join matrices provided in sequence.

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

import ptolemy.data.IntToken;
import ptolemy.data.MatrixToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// MatrixJoin

/**
 This actor joins matrices into a single matrix by tiling.
 It reads N*M input matrices from the input port, where N is
 the value of the <i>rows</i> parameter, and M is
 the value of the <i>columns</i> parameter. The matrices
 read are arranged left-to-right,
 top-to-bottom, in a raster scan pattern.
 For example, if <i>rows</i> = <i>columns</i> = 2, then on
 each firing, four matrices, A, B, C, D, will be read from
 the input channel.  Assuming A is the first one read, then
 the output matrix will be a matrix arranged as follows:
 <pre>
   A B
   C D
 </pre>
 The size of the output depends on the matrices in the top row
 and left column. That is, in the above examples, the number of
 columns in the output will equal the sum of the number of columns
 in A and B.  The number of rows will equal the sum of the number
 of rows in A and C.  The matrices are tiled in raster-scan order,
 first A, then B, then C, and then D.  Gaps are zero filled,
 and overlaps are overwritten, where later matrices in the raster-scan
 order overwrite earlier matrices.  For example, if B has more rows
 than A, then the bottom rows of B will be overwritten by rows of D.
 <p>

 @author Edward Lee
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (neuendor)
 */
public class MatrixJoin extends SDFTransformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public MatrixJoin(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // Set parameters.
        columns = new Parameter(this, "columns");
        columns.setTypeEquals(BaseType.INT);
        columns.setExpression("1");
        rows = new Parameter(this, "rows");
        rows.setTypeEquals(BaseType.INT);
        rows.setExpression("1");

        input_tokenConsumptionRate.setExpression("rows * columns");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The number of matrices to arrange left to right on the output.
     *  This is an integer that defaults to 1.
     */
    public Parameter columns;

    /** The number of matrices to arrange top to bottom on the output.
     *  This is an integer that defaults to 1.
     */
    public Parameter rows;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Ensure that the rows and columns parameters are both positive.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the parameters are out of range.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == columns) {
            int columnsValue = ((IntToken) columns.getToken()).intValue();
            if (columnsValue <= 0) {
                throw new IllegalActionException(this,
                        "Invalid number of columns: " + columnsValue);
            }
        } else if (attribute == rows) {
            int rowsValue = ((IntToken) rows.getToken()).intValue();
            if (rowsValue <= 0) {
                throw new IllegalActionException(this,
                        "Invalid number of rows: " + rowsValue);
            }
        }
        super.attributeChanged(attribute);
    }

    /** Consume the inputs and produce the output matrix.
     *  @exception IllegalActionException If not enough tokens are available.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        int numberOfColumns = ((IntToken) columns.getToken()).intValue();
        int numberOfRows = ((IntToken) rows.getToken()).intValue();
        MatrixToken[][] result = new MatrixToken[numberOfRows][numberOfColumns];
        for (int i = 0; i < numberOfRows; i++) {
            for (int j = 0; j < numberOfColumns; j++) {
                result[i][j] = (MatrixToken) input.get(0);
            }
        }
        ;
        output.send(0, result[0][0].join(result));
    }

    /** Return true if each input channel has enough tokens for this actor to
     *  fire. The number of tokens required is the value of the <i>columns</i> parameter.
     *  @return boolean True if there are enough tokens at the input port
     *   for this actor to fire.
     *  @exception IllegalActionException If the hasToken() query to the
     *   input port throws it.
     *  @see ptolemy.actor.IOPort#hasToken(int, int)
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        int columnsValue = ((IntToken) columns.getToken()).intValue();
        int rowsValue = ((IntToken) rows.getToken()).intValue();
        if (!input.hasToken(0, rowsValue * columnsValue)) {
            if (_debugging) {
                _debug("Called prefire(), which returns false.");
            }
            return false;
        } else {
            return super.prefire();
        }
    }
}
