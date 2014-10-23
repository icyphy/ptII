/* Partition an image into smaller subimages.
 @Copyright (c) 1998-2014 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

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

 PT_COPYRIGHT_VERSION 2
 COPYRIGHTENDKEY
 */
package ptolemy.domains.sdf.lib.vq;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.IntMatrixToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// ImagePartition

/**
 Partition an image into smaller subimages.  Each input image
 should have dimensions imageColumns by imageRows, and each output image
 will have dimensions partitionColumns by partitionRows.  The output matrices
 are row scanned from the top of input image.

 @author Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (neuendor)
 @Pt.AcceptedRating Red (neuendor)
 */
public class ImagePartition extends Transformer {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public ImagePartition(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        imageColumns = new Parameter(this, "imageColumns", new IntToken("176"));
        imageColumns.setTypeEquals(BaseType.INT);
        imageRows = new Parameter(this, "imageRows", new IntToken("144"));
        imageRows.setTypeEquals(BaseType.INT);
        partitionColumns = new Parameter(this, "partitionColumns",
                new IntToken("4"));
        partitionColumns.setTypeEquals(BaseType.INT);
        partitionRows = new Parameter(this, "partitionRows", new IntToken("2"));
        partitionRows.setTypeEquals(BaseType.INT);

        output_tokenProductionRate = new Parameter(output,
                "tokenProductionRate");
        output_tokenProductionRate.setTypeEquals(BaseType.INT);
        output_tokenProductionRate
        .setExpression("imageColumns * imageRows / partitionColumns / partitionRows");

        input.setTypeEquals(BaseType.INT_MATRIX);
        output.setTypeEquals(BaseType.INT_MATRIX);
    }

    ///////////////////////////////////////////////////////////////////
    ////                      ports and parameters                 ////

    /** The width of the input matrices. */
    public Parameter imageColumns;

    /** The height of the input matrices. */
    public Parameter imageRows;

    /** The width of the input partitions. */
    public Parameter partitionColumns;

    /** The height of the input partitions. */
    public Parameter partitionRows;

    /** The output rate. */
    public Parameter output_tokenProductionRate;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Initialize this actor.
     * @exception IllegalActionException If a parameter does not contain a
     * legal value, or partitionColumns does not equally divide imageColumns,
     * or partitionRows does not equally divide imageRows.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        _imageColumns = ((IntToken) imageColumns.getToken()).intValue();
        _imageRows = ((IntToken) imageRows.getToken()).intValue();
        _partitionColumns = ((IntToken) partitionColumns.getToken()).intValue();
        _partitionRows = ((IntToken) partitionRows.getToken()).intValue();

        if (_imageColumns % _partitionColumns != 0) {
            throw new IllegalActionException(imageColumns, partitionColumns,
                    "Partition size must evenly divide image size");
        }

        if (_imageRows % _partitionRows != 0) {
            throw new IllegalActionException(imageRows, partitionRows,
                    "Partition size must evenly divide image size");
        }

        _part = new int[_partitionRows][_partitionColumns];

        int partitionCount = _imageColumns * _imageRows / _partitionColumns
                / _partitionRows;
        _partitions = new IntMatrixToken[partitionCount];
    }

    /**
     * Fire this actor.  Consume a single IntMatrixToken on the input.
     * Produce IntMatrixTokens on the output port by partitioning the
     * input image.
     *
     * @exception IllegalActionException If the input size is not
     * <i>imageRows</i> by <i>imageColumns</i>.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        int i;
        int j;
        int y;
        int partitionNumber;
        IntMatrixToken message;

        message = (IntMatrixToken) input.get(0);

        if (message.getRowCount() != _imageRows
                || message.getColumnCount() != _imageColumns) {
            throw new IllegalActionException("Input data must be imageRows "
                    + "by imageColumns");
        }

        int[][] image = message.intMatrix();

        for (j = 0, partitionNumber = 0; j < _imageRows; j += _partitionRows) {
            for (i = 0; i < _imageColumns; i += _partitionColumns, partitionNumber++) {
                for (y = 0; y < _partitionRows; y++) {
                    System.arraycopy(image[j + y], i, _part[y], 0,
                            _partitionColumns);
                }

                _partitions[partitionNumber] = new IntMatrixToken(_part);
            }
        }

        output.send(0, _partitions, _partitions.length);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private IntMatrixToken[] _partitions;

    private int[][] _part;

    private int _imageColumns;

    private int _imageRows;

    private int _partitionColumns;

    private int _partitionRows;
}
