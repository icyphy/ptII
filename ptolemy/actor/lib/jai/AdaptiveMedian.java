/* An actor that performs adaptive median filtering on a double matrix.

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

*/

package ptolemy.actor.lib.jai;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// AdaptiveMedian

/**
   This actor performs adaptive median filtering on an image.  The
   algorithm is as follows.  For each pixel in the image, a square region
   of interest is formed with the pixel at the center.  If said region
   can not be formed (because it is at or near the edge).

   If the median of this region of interest is strictly less than the
   maximum value in the region of interest and strictly greater than
   the minimum value in the region of interest, then we keep the pixel
   if it too is strictly less than the maximum value in the region of
   interest and strictly greater than the minimum value in the region
   of interest.  If it is not, then use the median of the region of
   interest instead of the pixel.

   If the pixel is not strictly less than the maximum value and strictly
   greater than the minimum value, an attempt is made at using larger
   region of interest.  If successful, then this process is repeated until
   a value can be determine, or we hit the the maximum window size.  If this
   happens, then the pixel is kept.  This process is repeated for each pixel.

   @author James Yeh
   @version $Id$
   @since Ptolemy II 3.0
*/

public class AdaptiveMedian extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public AdaptiveMedian(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setTypeEquals(BaseType.DOUBLE_MATRIX);
        output.setTypeEquals(BaseType.DOUBLE_MATRIX);

        maxWindowSize = new Parameter(this, "maxWindowSize", new IntToken(7));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The largest window size to use.  This number must be an odd
     *  integer.  The default value is 7.
     */
    public Parameter maxWindowSize;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class and set the largest window size.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the largest window size is
     *  not an odd integer.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == maxWindowSize) {
            _maxWindowSize = ((IntToken)maxWindowSize.getToken()).intValue();
            if (_maxWindowSize%2 == 0) {
                throw new IllegalActionException(this,
                        "Window Size must be odd!!");
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Fire this actor.
     *  Perform the adaptive median filtering.
     *  @exception IllegalActionException If a contained method throws it.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        DoubleMatrixToken doubleMatrixToken = (DoubleMatrixToken) input.get(0);
        double data[][] = doubleMatrixToken.doubleMatrix();
        int width = doubleMatrixToken.getRowCount();
        int height = doubleMatrixToken.getColumnCount();
        double outputData[][] = new double[width][height];
        int windowSize = 3;
        //Iterate over each pixel.
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                while (true) {
                    int dist = (windowSize - 1)/2;
                    //check if we can create a region of interest or not.
                    //If we can't (i.e. we are at or near the edge of an image)
                    //then just keep the data.
                    if ((i - dist < 0) || (j - dist < 0)
                            || (i + dist >= width) || (j + dist >= height)) {
                        outputData[i][j] = data[i][j];
                        windowSize = 3;
                        break;
                    }
                    else {
                        double temp[][] = new double[windowSize][windowSize];
                        //create a local region of interest around the pixel.
                        for (int k = (i - dist); k <= (i + dist); k++) {
                            for (int l = (j - dist); l <= (j + dist); l++) {
                                temp[k - (i - dist)][l - (j - dist)] = data[k][l];
                            }
                        }
                        double median = _getMedian(temp, windowSize);
                        double max = _getMax(temp, windowSize);
                        double min = _getMin(temp, windowSize);
                        //If the median of the region of interest is
                        //strictly less than the maximum value, and strictly
                        //greater than the minimum value, we then have two
                        //routes.  If the data in the center is strictly
                        //greater than the minimum, and stricly less than
                        //the maximum, then just keep the data point.
                        //If it is either the minimum or the maximum, then
                        //output the medium because there is a very good chance
                        //that the pixel was noised.  After this, the window size
                        //is reset.
                        if ((median > min) && (median < max)) {
                            if ((data[i][j] > min) && (data[i][j] < max)) {
                                outputData[i][j] = data[i][j];
                                windowSize = 3;
                                break;
                            }
                            else {
                                outputData[i][j] = median;
                                windowSize = 3;
                                break;
                            }
                        } else if (windowSize < _maxWindowSize) {
                            //If this statement is reached, this means that
                            //the median was equal to either the minimum or the
                            //maximum (or quite possibly both if the region of
                            //interest had constant intensity.  Increase the window
                            //size, if it is less than the maximum window size.
                            windowSize = windowSize + 2;
                        } else {
                            //If this statement is reached, we've already hit
                            //the maximum window size, in which case, just output
                            //the data and reset the window size.
                            outputData[i][j] = data[i][j];
                            windowSize = 3;
                            break;
                        }
                    }
                }
            }
        }
        output.send(0, new DoubleMatrixToken(outputData));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Find the largest value in a region of interest.
     */
    private double _getMax(double[][] input, int size) {
        double temp = input[0][0];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (input[i][j] > temp) {
                    temp = input[i][j];
                }
            }
        }
        return temp;
    }

    /** Find the median value in a region of interest.
     */
    private double _getMedian(double[][] input, int size) {
        double[] temp = new double[size*size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                temp[i*size + j] = input[i][j];
            }
        }
        for (int i = 0; i < size*size; i++) {
            for (int j = 0; j < size*size - 1; j++) {
                if (temp[j] > temp[j+1]) {
                    double tempval = temp[j];
                    temp[j] = temp[j+1];
                    temp[j+1] = tempval;
                }
            }
        }
        return temp[(size*size-1)/2];
    }

    /** Find the minimum value in a region of interest.
     */
    private double _getMin(double[][] input, int size) {
        double temp = input[0][0];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (input[i][j] < temp) {
                    temp = input[i][j];
                }
            }
        }
        return temp;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The largest window size.
    private int _maxWindowSize;
}
