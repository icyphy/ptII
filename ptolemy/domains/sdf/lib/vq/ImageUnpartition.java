/* Combine subimages into a larger image.
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
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// ImageUnpartition

/**
 Combine subimages into a larger image. Each input subimage
 should have dimensions partitionColumns by partitionRows, and each output image
 will have dimensions imageColumns by imageRows.  The input images
 will be placed in row-scanned order from top to bottom into the output image.

 @author Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (neuendor)
 @Pt.AcceptedRating Yellow (neuendor)
 */
public class ImageUnpartition extends Transformer {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public ImageUnpartition(CompositeEntity container, String name)
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

        input_tokenConsumptionRate = new Parameter(input,
                "tokenConsumptionRate");
        input_tokenConsumptionRate.setTypeEquals(BaseType.INT);
        input_tokenConsumptionRate
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

    /** The input rate. */
    public Parameter input_tokenConsumptionRate;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Initialize this actor.
     * @exception IllegalActionException If a parameter does not contain a
     * legal value.
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

        _image = new int[_imageRows][_imageColumns];
        _partitionCount = _imageColumns * _imageRows / _partitionColumns
                / _partitionRows;
    }

    /**
     * Fire this actor.
     * Consume IntMatrixTokens on the input port corresponding to the
     * partitions of an image.  Reassemble the image and produce a
     * single IntMatrixToken on the output port.
     *
     * @exception IllegalActionException If the ports are not connected.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        int i;
        int j;
        int y;
        int partitionNumber;

        Token[] _partitions = input.get(0, _partitionCount);

        for (j = 0, partitionNumber = 0; j < _imageRows; j += _partitionRows) {
            for (i = 0; i < _imageColumns; i += _partitionColumns, partitionNumber++) {
                IntMatrixToken partition = (IntMatrixToken) _partitions[partitionNumber];

                if (partition.getRowCount() != _partitionRows
                        || partition.getColumnCount() != _partitionColumns) {
                    throw new IllegalActionException(
                            "input data must be partitionRows "
                                    + "by partitionColumns");
                }

                int[][] part = partition.intMatrix();

                for (y = 0; y < _partitionRows; y++) {
                    System.arraycopy(part[y], 0, _image[j + y], i,
                            _partitionColumns);
                }
            }
        }

        output.send(0, new IntMatrixToken(_image));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private int[][] _image;

    private int _imageColumns;

    private int _imageRows;

    private int _partitionColumns;

    private int _partitionRows;

    // This is the input port consumption rate.
    private int _partitionCount;
}
