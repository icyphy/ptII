/* One line description of file.

 Copyright (c) 1999-2003 The Regents of the University of California.
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
@ProposedRating Red (acataldo@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.apps.softwalls;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;

import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// ThreeDFunction
/**
This creates a function of three variables, defined over some subset
of R^3.  The function is read from a file which stores the value of the
function along a lattice of gridpoints.  The begining of the file also
specifes the index value at each gridpoint, which in turn specifies the
subset of R^3 for which the dataset is defined.

@author Adam Cataldo
@version $Id$
@since Ptolemy II 2.0.1
*/
public class ThreeDFunction {
    /** Constructs the 3D dataset.
     *  @exception IllegalActionException if any exception is 
     *     is generated during file i/o.
     */
    public ThreeDFunction(String fileName) throws IllegalActionException {
	int xPoints, yPoints, thetaPoints;
        double xSpan, ySpan, thetaSpan;
	double dimension;

	try {
	    BufferedReader in = 
		new BufferedReader(new FileReader(fileName));
	    
            /** Read the dimension of the state space and ignore it,
             * since we know it's value is 3.
             */
	    dimension = _readDouble(in);

	    //Read x grid information.
	    _xLowerBound = _readDouble(in);
	    _xStepSize = _readDouble(in);
	    _xUpperBound = _readDouble(in);

	    //Read y grid information.
	    _yLowerBound = _readDouble(in);
	    _yStepSize = _readDouble(in);
	    _yUpperBound = _readDouble(in);

	    //Read theta grid information.
	    _thetaLowerBound = _readDouble(in);
	    _thetaStepSize = _readDouble(in);
	    _thetaUpperBound = _readDouble(in);

	    //Initialize the values array;
            xSpan = _xUpperBound - _xLowerBound;
            ySpan = _yUpperBound - _yLowerBound;
            thetaSpan = _thetaUpperBound - _thetaLowerBound;
	    xPoints = (int)Math.round(xSpan / _xStepSize) + 1;
	    yPoints = (int)Math.round(ySpan / _yStepSize) + 1;
	    thetaPoints = (int)Math.round(thetaSpan / _thetaStepSize) + 1;
	    _values = new double[xPoints][yPoints][thetaPoints];
	    
	    /** Fill in the values array with values, sorted in
             *lexicographical order.
             */
	    for (int x = 0; x < xPoints; x++) {
		for (int y = 0; y < yPoints; y++) {
		    for (int theta = 0; theta < thetaPoints; theta++) {
			_values[theta][y][x] = _readDouble(in);
		    }
		}
	    }

	    //Close the file.
	    in.close();

            //Calculate the error tolerances
            _createTolerances(0.0001);

            double x = 0.9;
            double y = -1.0;
            double theta = 0.01;

	}
        catch (FileNotFoundException f) {
	    throw new IllegalActionException(f.getMessage());
	}
	catch (IOException i) {
            throw new IllegalActionException(i.getMessage());
	}
	catch (NumberFormatException n) {
	    throw new IllegalActionException(n.getMessage());
	}
	catch (NoSuchElementException e) {
	    throw new IllegalActionException(e.getMessage());
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                      public mehtods                       ////

    /* Returns the approximate value at the current x,y,theta value,
     * using trilinear interpolation.
     */
    public double getValue(double x, double y, double theta) {
        double[] thisPoint = new double[] {x, y, theta};
        double[][] gridPoints = _nearestGridPoints(x, y, theta);
	if (_inRange(gridPoints)) {
            return _linInterp(thisPoint, gridPoints);
        }
        else {
            return Double.POSITIVE_INFINITY;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                      private variables                    ////
    
    /* Lower bound for each dimension */
    double _xLowerBound,  _yLowerBound, _thetaLowerBound;	

    /* Step size for each dimension */
    double _xStepSize, _yStepSize, _thetaStepSize;
	
    /* Upper bound for each dimension */
    double _xUpperBound, _yUpperBound, _thetaUpperBound;

    /* The matrix of values on the gridpoint */
    double[][][] _values;

    /* The StringTokenizer being used to read the next double */
    StringTokenizer _tokenizer = new StringTokenizer("");

    /* The error tolerances. */
    double _xTolerance, _yTolerance, _thetaTolerance;

    ///////////////////////////////////////////////////////////////////
    ////                       private methods                     ////
    
    /*  If a line has no data, it tries to return the next line.
     *  If no next line exists, it returns null, 
     **/
    private double _readDouble(BufferedReader reader) throws IOException {
	String line;
	String token;
	if (_tokenizer.hasMoreTokens()) {
	    return (new Double(_tokenizer.nextToken())).doubleValue();
	}
	else {
	    try {
		_tokenizer = new StringTokenizer(reader.readLine());
		return _readDouble(reader);
	    }
	    catch (IOException i) {
		throw i;
	    }
	}
    }

    /** Uses linear interpolation to give the value of the input point
     * relative to the grid points.
     */

    private double _linInterp(double[] point, double[][] gridPoints) {
        int length = gridPoints.length;
        if (length == 1) {
            double x, y, theta;
            x = gridPoints[0][0];
            y = gridPoints[0][1];
            theta = gridPoints[0][2];
            int[] indices = _getIndices(x, y, theta);
            return _values[indices[0]][indices[1]][indices[2]];
        }
        else {
            double[][][] split = _splitArray(gridPoints);
            double[][] first = split[0];
            double[][] second = split[1];
            
            /** Determine the dimension along which the division
             * occured, and do linear interpolation along this
             * dimension.
             */
            double firstX, secondX, firstY, secondY, firstTheta, secondTheta;
            firstX = first[0][0];
            secondX = second[0][0];
            firstY = first[0][1];
            secondY = second[0][1];
            firstTheta = first[0][2];
            secondTheta = second[0][2];
            
            double x, y, theta;
            x = point[0];
            y = point[1];
            theta = point[2];

            double firstWeight, secondWeight;
            if (firstX != secondX) {
                secondWeight = (x - firstX) / _xStepSize;
                firstWeight = (secondX - x) / _xStepSize;
            }
            else if (firstY != secondY) {
                secondWeight = (y - firstY) / _yStepSize;
                firstWeight = (secondY - y) / _yStepSize;
            }
            else if (firstTheta != secondTheta) {
                secondWeight = (theta - firstTheta) / _thetaStepSize;
                firstWeight = (secondTheta - theta) / _thetaStepSize;
            }
            else {
                return Double.NaN;
            }
            
            return firstWeight * _linInterp(point, first)
                + secondWeight * _linInterp(point, second);
        }
    }


    /** Returns the nearest gridpoints, in lexicographical order.
     * There will be 1,2,4, or 8 points, epending on how many gird
     * boundaries the point covers.
     */

    private double[][] _nearestGridPoints(double x, double y, double theta) {
 	double xRem, yRem, thetaRem;
        double[] xPoints, yPoints, thetaPoints;
        double[][] output;

        /** Calculates the nearest gridpoint with lesser or equal
         * value.
         */
	xRem = (x - _xLowerBound) % _xStepSize;
	yRem = (y - _yLowerBound) % _yStepSize;
	thetaRem = (theta - _thetaLowerBound) % _thetaStepSize;

        /** Calculate the x point or points.
         */
        if (xRem == 0.0) {
            xPoints = new double[1];
            xPoints[0] = x;
        }
        else {
            xPoints = new double[2];
            xPoints[0] = x - xRem;
            xPoints[1] = xPoints[0] + _xStepSize;
        }

        /** Calculate the y point or points.
         */
        if (yRem == 0.0) {
            yPoints = new double[1];
            yPoints[0] = y;
        }
        else {
            yPoints = new double[2];
            yPoints[0] = y - yRem;
            yPoints[1] = yPoints[0] + _yStepSize;
        }

        /** Calculate the theta point or points.
         */
        if (thetaRem == 0.0) {
            thetaPoints = new double[1];
            thetaPoints[0] = theta;
        }
        else {
            thetaPoints = new double[2];
            thetaPoints[0] = theta - thetaRem;
            thetaPoints[1] = thetaPoints[0] + _thetaStepSize;
        }

        /** Calculate the neighboring points.
         */
        int outLength = xPoints.length * yPoints.length * thetaPoints.length;
        output = new double[outLength][3];

        int outputIndex = 0;
        for (int i = 0; i < xPoints.length; i += 1) {
            for (int j = 0; j < yPoints.length; j += 1) {
                for (int k = 0; k < thetaPoints.length; k += 1) {
                    output[outputIndex] = 
                        new double[] {xPoints[i], yPoints[j], thetaPoints[k]};
                    outputIndex += 1;
                }
            }
        }

        return output;
    }

    /** Returns the array index values given the x,y,theta values.
     * The output is given as an array representing the x,y,theta
     * indices.  This method assumes that x, y, and theta are
     * approximately gridpoints.  Rounding is used to make up for
     * errors caused by binary representations of decimal number.
     */
    private int[] _getIndices(double x, double y, double theta) {
	int[] indices = new int[3];
	indices[0] = (int)Math.round((x - _xLowerBound) / _xStepSize);
	indices[1] = (int)Math.round((y - _yLowerBound) / _yStepSize);
	indices[2] = 
	    (int)Math.round((theta - _thetaLowerBound) / _thetaStepSize);
        return indices;
    }

    /** Returns true if all the gridpoints have values in the values
     * array.  Because the step sizes might not have exact binary
     * representations, the error tolerances are added to the upper
     * bounds and subtracted from the lower bounds so that boundaries
     * get included.
     */

    private boolean _inRange(double[][] gridPoints) {
        if (gridPoints.length == 1) {
            double x,y,t;
            double xLB, yLB, tLB;
            double xUB, yUB, tUB;
            boolean xOK, yOK, tOK;
            
            x = gridPoints[0][0];
            y = gridPoints[0][1];
            t = gridPoints[0][2];

            /** xLB is the lowerbound on x, allowing for some
             * numerical error. 
             */
            xLB = _xLowerBound - _xTolerance;
            yLB = _yLowerBound - _yTolerance;
            tLB = _thetaLowerBound - _thetaTolerance;

            /** xUB is the upperbound on x, allowing for some
             * numerical error.
             */
            xUB = _xUpperBound + _xTolerance;
            yUB = _yUpperBound + _yTolerance;
            tUB = _thetaUpperBound + _thetaTolerance;

            /* xOK is true if x is in the allowable range. */
            xOK = ((x >= xLB) && (x <= xUB));
            yOK = ((y >= yLB) && (y <= yUB));
            tOK = ((t >= tLB) && (t <= tUB));

            return (xOK && yOK && tOK);
        }
        else {
            double[][][] split = _splitArray(gridPoints);
            return (_inRange(split[0]) && _inRange(split[1]));
        }
    }

    /** Splits the values of a double[][] array into two new arrays.
     * If out = _splitArray(array), then out[0] is the first half of
     * the array and out[1] is the second half of the array.  This
     * assumes the array has an even length, and all subarrays have
     * length 3.
     */

    private double[][][] _splitArray(double[][] array) {
        int halfLength = array.length / 2;
        double[][][] output = new double[2][halfLength][3];
        for (int i = 0; i < halfLength; i += 1) {
            output[0][i] = (double[])array[i].clone();
            output[1][i] = (double[])array[i + halfLength].clone();
        }
        return output;
    }

    /** Creates the error tolerances used in determining if values are
     * in the allowed range.  These tolerances help correct errors
     * caused when decimal numbers are converted to binary.
     */
    private void _createTolerances(double relativeTolerance) {
        _xTolerance = _xStepSize * relativeTolerance;
        _yTolerance = _yStepSize * relativeTolerance;
        _thetaTolerance = _thetaStepSize * relativeTolerance;
    }
}
