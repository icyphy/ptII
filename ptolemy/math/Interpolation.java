/* An interpolator for a specified array of indexes and values.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Yellow (yuhong@eecs.berkeley.edu)
@AcceptedRating red (cxh@eecs.berkeley.edu)
*/

package ptolemy.math;

//////////////////////////////////////////////////////////////////////////
//// Interpolation
/**
This class provides algorithms to do interpolation. Currently, zero,
first, and third order interpolations are supported. These are the
interpolation orders most often used in practice. zero order interpolation
holds the last reference value; first order does linear interpolation;
and third order interpolation is based on the Hermite curves in chapter
11 of "Computer Graphic, Principles and Practice", by Foley, van Dam, Feiner
and Hughes, 2nd ed. in C, 1996.
<p>
The setValues() method specifies the reference values as a double
array. setIndexes() specifies the indexes of those values as an
int array. These two arrays must have the same length, and the indexes
must be increasing and non-negative; otherwise an exception will be thrown.
The values are periodic if a positive period is set by setPeriod(). In
this case, the period must be greater than the largest index, and
values within the index range 0 to (period-1) are repeated indefinitely.
If the period is zero, the values are not periodic, and the values
outside the range of the indexes are considered to be 0.0.
The interpolation order is set by setOrder().
<p>
The default reference values are {1.0, 0.0} and the indexes are {0, 1}.
The default period is 2 and the order is 0.
<p>

@author Sarah Packman, Yuhong Xiong
@version $Id$
*/

public class Interpolation {

    /** Construct an instance of Interpolation using the default parameters.
     */
    public Interpolation() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the reference indexes.
     *  @return An int array.
     */
    public int[] getIndexes() {
	return _indexes;
    }

    /** Return the interpolation order.
     *  @return An int.
     */
    public int getOrder() {
	return _order;
    }

    /** Return the value repetition period.
     *  @return An int.
     */
    public int getPeriod() {
	return _period;
    }

    /** Return the reference values.
     *  @return An double array.
     */
    public double[] getValues() {
	return _values;
    }

    /** Set the reference indexes.
     *  @param indexes An int array.
     *  @exception IllegalArgumentException If the argument array is
     *   not increasing and non-negative.
     */
    public void setIndexes(int[] indexes) {
	int prev = -1;
        for (int i = 0; i < indexes.length; i++) {
            if (indexes[i] <= prev ) {
                throw new IllegalArgumentException("Interpolation.setIndexes" +
		    " index array is not increasing and non-negative.");
            }
            prev = indexes[i];
        }

	_indexes = indexes;
    }

    /** Set the value repetition period.
     *  @param period An int.
     *  @exception IllegalArgumentException If the period is negative.
     */
    public void setPeriod(int period) {
	if (period < 0) {
	    throw new IllegalArgumentException("Interpolation.setPeriod: " +
		"The period is negative.");
	}
	_period = period;
    }

    /** Set the interpolation order.
     *  @param order An int.
     *  @exception IllegalArgumentException If the order is not 0, 1, or 3.
     */
    public void setOrder(int order) {
	if (order != 0 && order != 1 && order != 3) {
	    throw new IllegalArgumentException("Interpolation.setOrder: " +
		"The order " + order + " is not valid.");
	}
	_order = order;
    }

    /** Set the reference values.
     *  @param values A double array.
     */
    public void setValues(double[] values) {
	_values = values;
    }

    /** Return the interpolation result for the specified index.
     *  @param index The point of interpolation. Can be negative
     *  @return A double.
     *  @exception IllegalStateException If the index and value arrays
     *   do not have the same length, or the period is not 0 and not
     *   greater than the largest index.
     */
    public double interpolate(int index) {
	int numRefPoints = _indexes.length;
	if (numRefPoints != _values.length) {
	    throw new IllegalStateException("Interpolation.interpolate " +
		"The index and value arrays do not have the same length.");
	}

	int largestIndex = _indexes[numRefPoints-1];
	if (_period != 0 && _period <= largestIndex) {
	    throw new IllegalStateException("Interpolation.interpolate " +
		"The period is not 0 and not greater than the largest index.");
	}

	if (index < 0 || index > largestIndex) {
	    if (_period == 0) {
		return 0.0;
	    } else {
		// convert index to a value within [0, period-1]
		if (index < 0) {
		    index += ((-index/_period)+1) * _period;
		}
		index %= _period;
	    }
	}

	// index is now within [0, period-1]. If it is outside the range of
	// the smallest and the largest index, values must be periodic.

	// Handle a special case where the number of reference points is
	// 1. The code for order 3 later won't work for this case.
	if (numRefPoints == 1) {
	    return _values[0];
	}

	// indexIndexStart is the index to _indexes whose entry is the
	// index to the left of the interpolation point.
	int indexIndexStart = -1;

	// search though all indexes to find iStart.
	for (int i = 0; i < numRefPoints; i++) {
	    if (_indexes[i] == index) {
		return _values[i];
	    } else if (_indexes[i] < index) {
		indexIndexStart = i;
	    } else {
		break;
	    }
	}

	// Perform interpolation
	if (_order == 0) {
	    if (indexIndexStart != -1) {
		return _values[indexIndexStart];
	    } else {
		return _values[numRefPoints-1];
	    }
	}

	// order must be 1 or 3, need at least the two points surrounding
	// the interpolation point.
	int iStart, iEnd;
	double vStart, vEnd;
	if (indexIndexStart == -1) {
	    iStart = _indexes[numRefPoints-1] - _period;
	    vStart = _values[numRefPoints-1];
	} else {
	    iStart = _indexes[indexIndexStart];
	    vStart = _values[indexIndexStart];
	}

	if (indexIndexStart == numRefPoints-1) {
	    iEnd = _indexes[0] + _period;
	    vEnd = _values[0];
	} else {
	    iEnd = _indexes[indexIndexStart+1];
	    vEnd = _values[indexIndexStart+1];
	}

	if (_order == 1) {
	    return vStart + (index-iStart)*(vEnd-vStart)/(iEnd-iStart);
	}

	// order is 3. Need the points before Start and the point after End
	// to compute the tangent at Start and End.
	int iBeforeStart, iAfterEnd;
	double vBeforeStart, vAfterEnd;
	if (indexIndexStart == -1) {
	    iBeforeStart = _indexes[numRefPoints-2] - _period;
	    vBeforeStart = _values[numRefPoints-2];
	} else if (indexIndexStart == 0) {
	    if (_period > 0) {
		iBeforeStart = _indexes[numRefPoints-1] - _period;
		vBeforeStart = _values[numRefPoints-1];
	    } else {
		// Not periodic
		iBeforeStart = _indexes[0] - 1;
		vBeforeStart = 0.0;
	    }
	} else {
	    iBeforeStart = _indexes[indexIndexStart-1];
	    vBeforeStart = _values[indexIndexStart-1];
	}

	if (indexIndexStart == numRefPoints-1) {
	    iAfterEnd = _indexes[1] + _period;
	    vAfterEnd = _values[1];
	} else if (indexIndexStart == numRefPoints-2) {
	    if (_period > 0) {
		iAfterEnd = _indexes[0] + _period;
		vAfterEnd = _values[0];
	    } else {
		// Not periodic
		iAfterEnd = _indexes[numRefPoints-1] + 1;
		vAfterEnd = 0.0;
	    }
	} else {
	    iAfterEnd = _indexes[indexIndexStart+2];
	    vAfterEnd = _values[indexIndexStart+2];
	}

	// computer the tangent at Start and End.
	double tanBefore2Start = (vStart-vBeforeStart)/(iStart-iBeforeStart);
	double tanStart2End = (vEnd-vStart)/(iEnd-iStart);
	double tanEnd2After = (vAfterEnd-vEnd)/(iAfterEnd-iEnd);

	double tanStart = 0.5 * (tanBefore2Start + tanStart2End);
	double tanEnd = 0.5 * (tanStart2End + tanEnd2After);

	return _hermite(index, iStart, vStart, tanStart, iEnd, vEnd, tanEnd);
    }

    ///////////////////////////////////////////////////////////////////
    ////                          private methods                  ////

    // Return the Hermite curve interpolation. The arguments are: the
    // interpolation point index, the index/value/tangent of the starting
    // reference point, the index/value/tangent of the ending reference
    // point.
    private double _hermite(int index,
			   int iStart, double vStart, double tanStart,
			   int iEnd, double vEnd, double tanEnd) {
	// forming the Hermite matrix M
	double[][] M = new double[4][4];
	double iStartSqr = iStart * iStart;
	double iEndSqr = iEnd * iEnd;
	M[0][0] = iStartSqr * iStart;
	M[0][1] = iStartSqr;
	M[0][2] = iStart;
	M[0][3] = 1;

	M[1][0] = iEndSqr * iEnd;
	M[1][1] = iEndSqr;
	M[1][2] = iEnd;
	M[1][3] = 1;

	M[2][0] = 3 * iStartSqr;
	M[2][1] = 2 * iStart;
	M[2][2] = 1;
	M[2][3] = 0;

	M[3][0] = 3 * iEndSqr;
	M[3][1] = 2 * iEnd;
	M[3][2] = 1;
	M[3][3] = 0;

	double[][] MInverse = MatrixMath.inverse(M);

	// forming the column vector of values and tangents
	double[] Gh = new double[4];
	Gh[0] = vStart;
	Gh[1] = vEnd;
	Gh[2] = tanStart;
	Gh[3] = tanEnd;

	// compute the coefficients vector coef[a, b, c, d] or the 3rd order
	// curve.
	double[] coef = MatrixMath.multiply(Gh, MInverse);

	// compute the interpolated value
	double indexSqr = index*index;
	return coef[0]*indexSqr*index + coef[1]*indexSqr
		+ coef[2]*index + coef[3];
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private int[] _indexes = {0, 1};
    private double[] _values = {1.0, 0.0};
    private int _period = 2;
    private int _order = 0;
}

