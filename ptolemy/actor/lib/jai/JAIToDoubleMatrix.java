/* Takes a JAIImageToken, and outputs the data as a DoubleMatrixToken.

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

import java.awt.image.DataBuffer;
import java.awt.image.Raster;

import javax.media.jai.RenderedOp;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// JAIToDoubleMatrix
/**
   This actor takes a single banded image and outputs a DoubleMatrixToken
   containing the data.

   To convert multiple banded images (for instance, color images or the
   output of a Discrete Fourier Transform), use either the BandSelect or
   BandCombine actors to seperate the bands.

   @see DoubleMatrixToJAI
   @see JAIBandSelect
   @see JAIBandCombine
   @author James Yeh
   @version $Id$
   @since Ptolemy II 3.0
*/

public class JAIToDoubleMatrix extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public JAIToDoubleMatrix(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setTypeEquals(BaseType.OBJECT);
        output.setTypeEquals(BaseType.DOUBLE_MATRIX);
    }

    /** Fire this actor.
     *  Output the data contained inside the image as a DoubleMatrixToken.
     *  @exception IllegalActionException If a contained method throws it.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        JAIImageToken jaiImageToken = (JAIImageToken) input.get(0);
        RenderedOp jaiImage = jaiImageToken.getValue();
        int height = jaiImage.getHeight();
        int width = jaiImage.getWidth();
        Raster raster = jaiImage.getData();
        DataBuffer dataBuffer = raster.getDataBuffer();
        //Construct a matrix of doubles.
        double data[][] = new double[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                data[i][j] = dataBuffer.getElemDouble(i*height + j);
            }
        }
        output.send(0, new DoubleMatrixToken(data));
    }
}
