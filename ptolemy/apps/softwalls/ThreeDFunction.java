/* Read from a file and create a function of three variables. 

 Copyright (c) 2003-2004 The Regents of the University of California.
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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
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

@author Adam Cataldo, Christopher X. Brooks
@version $Id$
@since Ptolemy II 2.0.1
*/
public class ThreeDFunction implements Serializable {
    /** Construct the functional representation of the 3D dataset by
     *  reading a compressed file.
     *  
     *  @param fileName name of file storing the dataset.
     *  @exception IllegalActionException If any exception is
     *     is generated during file I/O.
     */
    public ThreeDFunction(String fileName) throws IllegalActionException {
        // Read uncompressed data and do not write out compressed data.
        this(fileName, true, false);

        // Read compressed data and do not write out uncompressed data.
        //this(fileName, true, false);
    }

    /** 
     *  Constructs the functional representation of the 3D dataset by
     *  reading a compressed or uncompressed file.
     *
     *  <p>The human readable data file has the following format:
     *
     *  <p>The first line is the integer dimension of the state space, which is
     *  should be 3, but is currently ignored
     *  
     *  <p>The second line consists of three doubles that define the
     *  x grid information.  The first field is the lower bound, the second
     *  is the step size, the third is the upper bound
     *
     *  <p>The third line consists of three doubles that define the y
     *  grid information.  The format is the same as the x grid
     *  information.
     *
     *  <p>The fourth line consists of three doubles that define the
     *  theta grid information. The format is the same as the x grid
     *  information.
     *
     *  <p> The fifth and successive lines consist of the array.
     *  Each line is one double.  

     *  <p> The data can be stored in two formats, uncompressed and
     *  compressed.  In the uncompressed format, a 101 x 101 x 101
     *  array will be about 10Mb.  In compressed format, the same
     *  array will be about 3Mb.  
     *  
     *  <p>In the compressed format, the 5th line is the initial
     *  value of the [0, 0, 0]th element of the array, and each
     *  successive line is the difference from the previous element.
     *  Values in the compressed format stored with three or four
     *  digits of precision as integers, so there are likely to be
     *  rounding errors.  However, usually this level of precision
     *  is sufficient for our needs.
     *  
     *  @param fileName name of file storing the dataset.
     *  @param compressed True if the input data is compressed
     *  @param wrieOutData True if the data should be written out
     *  in a file with the same name as the input file, but with a .tmp
     *  suffix. If we read in compressed data, we write out uncompressed.
     *  If we read in uncompressed data, we write out compressed.
     *
     *  @exception IllegalActionException If any exception is
     *     is generated during file I/O.
     */
    public ThreeDFunction(String fileName, boolean compressed,
            boolean writeOutData) throws IllegalActionException {
        int xPoints, yPoints, thetaPoints;
        double xSpan, ySpan, thetaSpan;
        double dimension;

        BufferedReader in = null;
            
        try {
            in = new BufferedReader(new FileReader(fileName));

            // Read the dimension of the state space and ignore it,
            // since we know it's value is 3.
            dimension = _readDouble(in);

            // Read x grid information.
            _xLowerBound = _readDouble(in);
            _xStepSize = _readDouble(in);
            _xUpperBound = _readDouble(in);

            // Read y grid information.
            _yLowerBound = _readDouble(in);
            _yStepSize = _readDouble(in);
            _yUpperBound = _readDouble(in);

            // Read theta grid information.
            _thetaLowerBound = _readDouble(in);
            _thetaStepSize = _readDouble(in);
            _thetaUpperBound = _readDouble(in);

//             //Complain if the theta values don't make sense
//             if ((_thetaLowerBound != 0.0) || (_thetaUpperBound >= Math.PI)) {
//                 throw new IllegalActionException("Bad bounds on theta");
//             }

            // Initialize the values array;
            xSpan = _xUpperBound - _xLowerBound;
            ySpan = _yUpperBound - _yLowerBound;
            thetaSpan = _thetaUpperBound - _thetaLowerBound;
            xPoints = (int)Math.round(xSpan / _xStepSize) + 1;
            yPoints = (int)Math.round(ySpan / _yStepSize) + 1;
            thetaPoints = (int)Math.round(thetaSpan / _thetaStepSize) + 1;
            _values = new double[xPoints][yPoints][thetaPoints];

            int last = Integer.MIN_VALUE;

            // Fill in the values array with values, sorted in
            // reverse lexicographical order.
            for (int t = 0; t < thetaPoints; t = t + 1) {
                for (int y = 0; y < yPoints; y = y + 1) {
                    for (int x = 0; x < xPoints; x = x + 1) {
                        if (compressed) {
                            // The data is stored in a delta format,
                            // where we record the difference between
                            // the last and the current values.
                            if (last == Integer.MIN_VALUE) {
                                // First data point
                                last = _readInteger(in);
                                _values[x][y][t] = last/1000.0;
                            } else {
                                last = last - _readInteger(in);
                                _values[x][y][t] = last/1000.0; 
                            }
                        } else {
                            _values[x][y][t] = _readDouble(in);
                        }
                    }
                }
            }

            // Set writeOutData to true to write out the other form of data.
            // If we read in compressed data, we write out uncompressed.
            // If we read in uncompressed data, we write out compressed.
            if (writeOutData) {
                BufferedWriter output = null;
                try {
                    output =
                        new BufferedWriter(new FileWriter(fileName + ".tmp"));
                    write(output, !compressed);
                } finally {
                    if (output != null) {
                        output.close();
                    }
                }
            }

        } catch (Exception ex) {
            throw new IllegalActionException(null, ex,
                    "Failed to parse '" + fileName + "'");
        } finally {
            if (in != null) {
                try {
                in.close();
                } catch (IOException ex) {
                    throw new IllegalActionException(null, ex,
                            "Failed to close '" + fileName + "'");
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                      public methods                       ////

    /**
     *  Return the approximate value of f(x, y, theta) using trilinear
     *  interpolation.  If x < _xLowerBound, or x >= _xUpperBound, or
     *  y < _yLowerBound, or y >= _yUpperBound, this returns Infinity.
     *
     *  @param x double x input
     *  @param y double y input
     *  @param theta double theta input
     *  @return double represention of f(x, y, theta)
     */
    public double getValue(double x, double y, double theta) {
        int x0Index, x1Index;
        int y0Index, y1Index;
        int theta0Index, theta1Index;
        double xDis, yDis, thetaDis;
        double value;
        double xWeight, yWeight, thetaWeight;
        int xIndex, yIndex, thetaIndex;
        double point;
        double weight;

        // Get the proper value of theta
        theta = _angleWrap(theta);

        if (_inRange(x, y)) {

            // Get the indices for the neighboring points.  x0Index
            // is the x index of the nearest gridpoint less than x,
            // and x1Index is the x index of the nearest gridpoint
            // greater than x.
            x0Index = (int)((x - _xLowerBound) / _xStepSize);
            x1Index = x0Index + 1;
            y0Index = (int)((y - _yLowerBound) / _yStepSize);
            y1Index = y0Index + 1;
            theta0Index =
                (int)((theta - _thetaLowerBound) / _thetaStepSize);

            // theta1Index will be 0 if the nearest gridpoint greater
            // than theta is 2*pi
//             double thetaSpan = _thetaUpperBound - _thetaLowerBound;
//             int maxThetaIndex = (int)Math.round(thetaSpan / _thetaStepSize);
//             if (theta0Index == maxThetaIndex) {
//                 theta1Index = 0;
//             }

            //  When the proper dataset is loaded, use the previous
            //  method.  This will be more error prone.

            if ((2 * Math.PI - theta) < _thetaStepSize) {
                theta1Index = 0;
            } else {
                theta1Index = theta0Index + 1;
            }

            // Get the normalized distance of x, y, and theta from
            //  the point corresponding to x0Index, y0Index, and
            //  theta0Index.  The distance is scaled by the step size
            //  in each dimension, so these numbers will be between 0
            //  and 1.
            xDis = (x - _xLowerBound) / _xStepSize - x0Index;
            yDis = (y - _yLowerBound) / _yStepSize - y0Index;
            thetaDis =
                (theta - _thetaLowerBound) / _thetaStepSize - theta0Index;

            // Through a for loop, compute the value.  At each step
            // of the for loop, add the contribution from one of the
            // gridpoints.
            value = 0;
            for (int i = 0; i <= 1; i = i + 1) {
                for (int j = 0; j <= 1; j = j + 1) {
                    for (int k = 0; k <= 1; k = k + 1) {
                        if (i == 0) {
                            xWeight = 1 - xDis;
                            xIndex = x0Index;
                        } else {
                            xWeight = xDis;
                            xIndex = x1Index;
                        } if (j == 0) {
                            yWeight = 1 - yDis;
                            yIndex = y0Index;
                        } else {
                            yWeight = yDis;
                            yIndex = y1Index;
                        } if (k == 0) {
                            thetaWeight = 1 - thetaDis;
                            thetaIndex = theta0Index;
                        } else {
                            thetaWeight = thetaDis;
                            thetaIndex = theta1Index;
                        }
                        point = _values[xIndex][yIndex][thetaIndex];
                        value =
                            value + point * xWeight * yWeight * thetaWeight;
                    }
                }
            }

            return value;
        } else {
            // The value is out of range, return Infinity.
            return Double.POSITIVE_INFINITY;
        }
    }

    /** Write the data out in human readable uncompressed format
     *  @param output The output file.
     */
    public void write(BufferedWriter output) throws IOException {
        write(output, false);
    }

    /** Write out the data in the human readable format that is
     *  either compressed or uncompressed.
     *  @param output The output file.
     *  @param compressed True if the output should be compressed.
     */
    public void write(BufferedWriter output, boolean compressed)
            throws IOException {
        output.write("3" + "\n");
        output.write(_xLowerBound
                + "   " + _xStepSize
                + "   " + _xUpperBound + "\n");

        output.write(_yLowerBound
                + "   " + _yStepSize
                + "   " + _yUpperBound + "\n");

        output.write(_thetaLowerBound
                + "   " + _thetaStepSize
                + "   " + _thetaUpperBound + "\n");
        
        // FIXME: we assume the array is regular, that is that
        // all the rows have the same length.

        long last = Math.round(_values[0][0][0] * 1000.0) ;
        boolean sawFirst = false;

        for (int t = 0; t < _values[0][0].length; t = t + 1) {
            for (int y = 0; y < _values[0].length; y = y + 1) {
                for (int x = 0; x < _values.length; x = x + 1) {
                    if (compressed) {
                        if (!sawFirst) {
                            sawFirst = true;
                            output.write( last + "\n");
                        } else {
                            long current =
                                Math.round(_values[x][y][t] * 1000.0);
                            output.write( last - current + "\n");
                            last = current;
                        }
                    } else {
                        output.write( _values[x][y][t] + "\n");
                    }
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                      private variables                    ////

    // Lower bound for each dimension.
    private double _xLowerBound,  _yLowerBound, _thetaLowerBound;

    // Step size for each dimension.
    private double _xStepSize, _yStepSize, _thetaStepSize;

    // Upper bound for each dimension.
    private double _xUpperBound, _yUpperBound, _thetaUpperBound;

    // The matrix of values on the gridpoint.
    private double[][][] _values;

    // The StringTokenizer being used to read the next double.
    private transient StringTokenizer _tokenizer = new StringTokenizer("");

    ///////////////////////////////////////////////////////////////////
    ////                       private methods                     ////

    /** 
     *  Takes in an angular value and returns the equivalant value in
     *  the range [0, 2*Pi).
     *
     *  @param angle radian value of the angle as any real number
     *  @return double in range [0, 2*Pi].
     */
    private double _angleWrap(double angle) {
        double pi = Math.PI;
        while (angle < 0) {
            angle = angle + 2 * pi;
        }
        while (angle >= 2 * pi) {
            angle = angle - 2 * pi;
        }
        return angle;
    }

    /**
     *  Returns true if the input is in the range stored by the array.
     *  That is, it returns true if x in [_xLowerBound, _xUpperBound),
     *  y in [_yLowerBound, _yUpperBound), and theta in Reals.
     *
     *  The upper bounds are excluded to make the interpolation
     *  routine simpler.
     *
     *  @param x double storing x value
     *  @param y double storing y value
     *  @return boolean
     */
    private boolean _inRange(double x, double y) {
        boolean xOK, yOK;

        /* xOK is true if x is in the allowable range. */
        xOK = ((x >= _xLowerBound) && (x < _xUpperBound));
        yOK = ((y >= _yLowerBound) && (y < _yUpperBound));

        return (xOK && yOK);
    }

    /**
     *  If a line has no data, it tries to return the next line.
     *  If no next line exists, it returns null,
     *
     *  @param reader BufferedReader storing the file of interest
     *  @exception IOException if reader throws an IOException
     *  @return double value read from the file
     **/
    private double _readDouble(BufferedReader reader) throws IOException {
        String line;
        String token;
        if (_tokenizer.hasMoreTokens()) {
            return (new Double(_tokenizer.nextToken())).doubleValue();
        } else {
            _tokenizer = new StringTokenizer(reader.readLine());
            return _readDouble(reader);
        }
    }

    /**
     *  If a line has no data, it tries to return the next line.
     *  If no next line exists, it returns null,
     *
     *  @param reader BufferedReader storing the file of interest
     *  @exception IOException if reader throws an IOException
     *  @return double value read from the file
     **/
    private int _readInteger(BufferedReader reader) throws IOException {
        String line;
        String token;
        if (_tokenizer.hasMoreTokens()) {
            return (new Integer(_tokenizer.nextToken())).intValue();
        } else {
            _tokenizer = new StringTokenizer(reader.readLine());
            return _readInteger(reader);
        }
    }
}
