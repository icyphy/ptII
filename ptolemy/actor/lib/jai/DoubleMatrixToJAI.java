/* Converts a matrix of doubles into a single-banded JAIImageToken.

@Copyright (c) 2002-2003 The Regents of the University of California.
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

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.jai;

import java.awt.Point;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_ProfileGray;
import java.awt.image.DataBuffer;
import java.awt.image.ComponentColorModel;
import java.awt.image.Raster;
import java.awt.image.renderable.ParameterBlock;

import javax.media.jai.ComponentSampleModelJAI;
import javax.media.jai.DataBufferDouble;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.TiledImage;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// DoubleMatrixToJAI
/**
   Converts a DoubleMatrix to a JAIImageToken.  This JAIImageToken is a
   single-banded grayscale image.  If the image is to be displayed or saved
   after this actor, the data should be formatted to type byte by using
   the JAIDataCaster actor.

   @see JAIDataCaster
   @see JAIToDoubleMatrix
   @author James Yeh
   @version $Id$
   @since Ptolemy II 3.0
 */

public class DoubleMatrixToJAI extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DoubleMatrixToJAI(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setTypeEquals(BaseType.DOUBLE_MATRIX);
        output.setTypeEquals(BaseType.OBJECT);
    }

    /** Fire this actor.
     *  Output the JAIImageToken constructed from the matrix of doubles.
     *  @exception IllegalActionException If a contained method throws it,
     *   or if a token is received that contains a null image.
     */
    public void fire() throws IllegalActionException {
        super.fire();

        DoubleMatrixToken doubleMatrixToken = (DoubleMatrixToken) input.get(0);
        double data[][] = doubleMatrixToken.doubleMatrix();
        int width = doubleMatrixToken.getRowCount();
        int height = doubleMatrixToken.getColumnCount();
        double newdata[] = new double[width*height];

        // Convert the matrix of doubles into an array of doubles
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                newdata[i*height + j] = data[i][j];
            }
        }

        // Create a new dataBuffer from the array of doubles
        DataBufferDouble dataBuffer =
            new DataBufferDouble(newdata, width*height);

        // The length of the bandOffset array indicates how many bands
        // there are.  Since we are just dealing with a single
        // DoubleMatrixToken, the length of this array will be one.
        // The values of the array indicate the offset to be added
        // To the bands.  This is set to 0.
        int bandOffset[] = new int[1];
        bandOffset[0] = 0;

        // Create a ComponentSampleModel, with type double, the same width
        // and height as the matrix, a pixel stride of one (the final image
        // is single-banded), and a scanline stride equal to the width.
        ComponentSampleModelJAI sampleModel =
            new ComponentSampleModelJAI(DataBuffer.TYPE_DOUBLE,
                    width, height, 1, width, bandOffset);

        // Create a new raster that has its origin at (0,0).
        Raster raster =
            Raster.createWritableRaster(sampleModel, dataBuffer, new Point());

        // Create a grayscale colormodel.
        ComponentColorModel colorModel =
            new ComponentColorModel(
                    new ICC_ColorSpace(
                            ICC_ProfileGray.getInstance(ColorSpace.CS_GRAY)),
                    false, false,
                    ComponentColorModel.OPAQUE, DataBuffer.TYPE_DOUBLE);
        TiledImage tiledImage =
            new TiledImage(0,0,width,height,0,0,sampleModel, colorModel);
        tiledImage.setData(raster);
        ParameterBlock parameters = new ParameterBlock();
        parameters.addSource(tiledImage);
        parameters.add(DataBuffer.TYPE_DOUBLE);
        RenderedOp newImage = JAI.create("format", parameters);
        output.send(0, new JAIImageToken(newImage));
    }
}
