/* Change the contrast of an image.
@Copyright (c) 1998-2003 The Regents of the University of California.
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
@ProposedRating Red (mikele@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.lib.vq;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.IntMatrixToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// ImageContrast
/**
Change the constrast of an image.

<p>If the input image has a lot of pixels with the same or similar color,
This actor uses gray scale equalization to redistribute the value of each
pixel between 0 and 255.

@author Michael Leung, Steve Neuendorffer
@version $Id$
@since Ptolemy II 0.2
*/

public class ImageContrast extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ImageContrast(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {

        super(container, name);

        output.setTypeEquals(BaseType.INT_MATRIX);
        input.setTypeEquals(BaseType.INT_MATRIX);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Fire the actor.
     *  Consume one image on the input port.
     *
     *  Summary:
     *  Contrast the image so that
     *  an more evenly color distributed image can be obtained.
     *  Assume that color is bounded from 0 to 255 inclusively.
     *  @exception IllegalActionException If image color is out-of-bound.
     *
     *  Algorithm:
     *  Construct a color histogram for the input image.
     *  Construct a cdf for the color histogram.
     *  Using Gray Scale Equalization, re-map each image pixel color.
     *
     *  Send the new image out the output port.
     */

    public void fire() throws IllegalActionException {

        int i, j;
        int frame[][];
        int frameElement;

        IntMatrixToken message = (IntMatrixToken) input.get(0);
        frame = message.intMatrix();

        // Construct a color distribution histogram for the input image:
        // Assuming the color bound for the input 0 and 255. If color detected
        // that has color either bigger than 255 OR small than 0, then throw an
        // illegal action exception.

        for (i = 0; i < 256; i ++)
            colorHistogram[i] = 0;

        int pixels = frame.length * frame[0].length;

        for (i = 0; i < frame.length; i++) {
            for (j = 0; j < frame[i].length; j++) {
                frameElement = frame[i][j];
                if ((frameElement < 0) || (frameElement > 255 )) {
                    throw new IllegalActionException("ImageContrast:"
                            + "input image pixel contains at"
                            + j + "," + i
                            + "with value" + frameElement
                            + "that is out of bounds."
                            + " Not between 0 and 255.");
                }
                colorHistogram[frameElement]++;
            }
        }

        //Construct the cdf of the color distribution histogram
        //colorHistogram[0] = colorHistogram[0]

        for (i = 1; i < 256; i ++)
            colorHistogram[i] = colorHistogram[i-1] + colorHistogram[i];

        // Search each pixel in the image and re-map it to a new
        // color number to make a new relatively even color distribution
        // image.

        int distributionConstant = pixels / 255;

        for (i = 0; i < frame.length; i ++) {
            for (j = 0; j < frame[i].length; j++) {
                frameElement = frame[i][j];
                frame[i][j] = colorHistogram[frameElement] /
                    distributionConstant;
            }
        }

        message = new IntMatrixToken(frame);
        output.send(0, message);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private int colorHistogram[] = new int[256];
}
