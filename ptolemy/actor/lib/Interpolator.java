/* An interpolator for a specified array of indexes and values.

 Copyright (c) 1998-1999 The Regents of the University of California.
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

@ProposedRating red (sarah@eecs.berkeley.edu)
@AcceptedRating red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.math.*;

//////////////////////////////////////////////////////////////////////////
//// Interpolator
/**
Produce an interpolation based on the parameters.
The <i>values</i> parameter specifies a sequence of values
to produce at the output.  The <i>indexes</i> parameter specifies
when those values should be produced.  The <i>order</i> parameter 
specifies which order of interpolation to apply  whenever the 
iteration count does not match an index in <i>indexes</i>. 
The values and indexes parameters must both contain one dimensional 
arrays, and have equal lengths or an exception will be thrown.
The <i>indexes</i> array must be increasing 
and non-negative and the <i>order</i> must be 0, 1 or 3, or an exception 
will be thrown by attributeChanged().
<p>
The <i>values</i> and <i>indexes</i> parameters have type DoubleMatrixToken 
and IntMatrixToken respectively; the <i>indexes</i> order is an integer.
If not set by the user, <i>values</i> contains by default a DoubleMatrix 
of form [1.0, 0.0] (one row, two columns, with values 1.0 and 0.0).  Similarly, 
by default <i>indexes</i> contains an IntMatrixToken containing [0, 1].  
The default for order is zero.
<p>
This actor counts iterations.  Whenever the iteration count matches an entry 
in the <i>indexes</i> array, the corresponding entry (at the same position)
in the <i>values</i> array is produced at the output.  Whenever the iteration 
dount does not match a value in the <i>indexes</i> array, an interpolation 
of the values is produced at the output.  The order of the interpolation is 
specified by the user in the <i>order</i> parameter as zero, one or three.
<p>
Output type is DoubleToken.

@author Sarah Packman 
@version $Id$
*/

public class Interpolator extends SequenceSource {

    /** Construct an actor with the specified container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Interpolator(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        // construct the parameters containing the values and indices.
	// attributeChanged will create transient arrays containing the 
	// values and indices.  these arrays pad the given values array 
	// with zeros on either end, and the given indices array with 
	// consecutive indices on each end.  this facilitates later 
	// computations (approximations for inter-index iteration numbers 
	// depend at most on values corresponding to the two nearest indices 
	// on each side of the iteration number).
 
        indexes = new Parameter(this, "indexes", defaultIndexToken);
        indexes.setTypeEquals(BaseType.INT_MATRIX);
        values = new Parameter(this, "values", defaultValueToken);
        values.setTypeEquals(BaseType.DOUBLE_MATRIX);
        order = new Parameter(this, "order", defaultOrderToken);
        order.setTypeEquals(BaseType.INT);
        attributeChanged(order);
        output.setTypeEquals(BaseType.DOUBLE);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The indexes at which the specified values will be produced.
     *  This parameter must contain an IntMatrixToken.
     */
    public Parameter indexes;

    /** The values that will be produced at the specified indexes.
     *  This parameter can contain any MatrixToken.
     */
    public Parameter values;

    /** The order of interpolation for non-index iterations.
     *  This parameter must contain an IntToken.
     */
    public Parameter order;
 
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** If the argument is the <i>values</i> or the <i>indexes</i> parameter,
     *  check that they contain one dimensional arrays.
     *  @exception IllegalActionException If specified parameters aren't
     *  valid.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
                
        _values = ((DoubleMatrixToken)values.getToken()).doubleMatrix();      
        _indexes = ((IntMatrixToken)indexes.getToken()).intMatrix();
        _order = ((IntToken)order.getToken()).intValue();
        
        // Verify that the tokens in the values and indexes parameters 
        // contain arrays which are one dimensional (have only one row) 
        // and of equal lengths.  If not, throw IllegalActionException.
        if (((DoubleMatrixToken)(values.getToken())).getRowCount() > 1) {
            throw new IllegalActionException(this,
                    "Values must be a one-dimensional array.");
        }
        if (((IntMatrixToken)(indexes.getToken())).getRowCount() > 1) {
            throw new IllegalActionException(this,
                    "Indexes must be a one-dimensional array.");
        }
        
        // Verify that the token in the indexes parameter contains an 
        // array whose elements are nonnegative and strictly increasing.  
        // If not, throw IllegalActionException.      
        int previous = -1;
        for (int j=0; j<_indexes[0].length; j++) {
            if (_indexes[0][j] <= previous) {
                throw new IllegalActionException(this,
                        "Indexes must be an array of nonnegative integers "
                        + "increasing in value.");
            }
            previous = _indexes[0][j];
        }      
        
        // Verify that the tokens in the indexes and values parameter 
        // contain arrays of the same length.  If not, throw 
        // IllegalActionException.     
        if (_indexes[0].length != _values[0].length) {
            throw new IllegalActionException(this,
                    "Indexes and Values must have the same length.");
        }            
        
        // Verify that the token in the order parameter contains a
        // value of 0, 1 or 3.  If not, throw IllegalActionException.  
        if ((_order != 0) && (_order != 1) && (_order != 3)) {
            throw new IllegalActionException(this,
                    "Order must be 0, 1 or 3.");
        }            
        
        // Create arrays from the values and indexes, padded on each 
        // end as described in the constructor.
        indexessize = _indexes[0].length;
        valuessize = _values[0].length;
        _valuesarray = new double[valuessize + 4];
        _indexesarray = new int[indexessize + 4];
        _valuesarray[0]=0; 
        _valuesarray[1]=0;      
        for(int i=0; i< _values[0].length; i++) {
            _valuesarray[i+2] = _values[0][i];
        }
        _valuesarray[_values[0].length + 2] = 0;
        _valuesarray[_values[0].length + 3] = 0;
        _indexesarray[0] = -2; 
        _indexesarray[1] = -1;
        for(int j=0; j< _indexes[0].length; j++) {
                _indexesarray[j+2] = _indexes[0][j];
        }
        _indexesarray[_indexes[0].length + 2] = _indexes[0][
                (_indexes[0].length)-1] + 1;
        _indexesarray[_indexes[0].length + 3] = _indexes[0][
                (_indexes[0].length)-1] + 2;
    }  

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the parameter public members to refer
     *  to the parameters of the new actor.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @throws CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws) {
        Interpolator newobj = (Interpolator)super.clone(ws);  
        try {
            newobj.indexes = (Parameter)newobj.getAttribute("indexes");
            newobj.values = (Parameter)newobj.getAttribute("values");
            newobj.order = (Parameter)newobj.getAttribute("order"); 
            indexes.setTypeEquals(BaseType.INT_MATRIX);
            values.setTypeEquals(BaseType.DOUBLE_MATRIX);
            order.setTypeEquals(BaseType.INT);
            newobj.attributeChanged(order);
            newobj.output.setTypeEquals(BaseType.DOUBLE);
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(ex.getMessage());
        }
        return newobj;
    } 
    
    /** If the count of iterations matches one of the indexes, output 
     *  the corresponding value.  Otherwise interpolate the current and 
     *  previous values with respect to the specified order and output the 
     *  approximation corresponding to the current iteration count.
     *  If iteration count is out of range of indexes, output zeros.
     *  @exception IllegalActionException If the <i>values</i> and
     *   <i>indexes</i> parameters do not contain arrays of the same length.
     */  
    public void fire() throws IllegalActionException {

	if (_valuesarray.length != _indexesarray.length) {
	    throw new IllegalActionException(this, "The values and the " +
		"index parameters do not contain arrays of the same length.");
	}
   
        // Create relevant named variables to make simpler approximation 
        // calculations later
        if (_indexCount < _indexes[0].length) {
            
            // Iteration is still within the range of the indexes
            int currentIndex = _indexesarray[_indexCount + 2];
            double currentValue = _valuesarray[_indexCount + 2];
            int previousIndex = _indexesarray[_indexCount + 1];
            double previousValue = _valuesarray[
                    _indexCount + 1];
            int nextIndex = _indexesarray[_indexCount + 3];
            int twopreviousIndex = _indexesarray[
                    _indexCount];
            double nextValue = _valuesarray[
                    _indexCount + 3];
            double twopreviousValue = _valuesarray[
                    _indexCount];
            double currentTangent = 0.5 * ((nextValue - currentValue)/(
                    nextIndex - currentIndex) + (
                    currentValue - previousValue)/(
                    currentIndex - previousIndex));
            double previousTangent = 0.5 * (
                    (currentValue - previousValue)/(
                    currentIndex - previousIndex) + (
                    previousValue - twopreviousValue)/(
                    previousIndex - twopreviousIndex));
                
            if (_iterationCount == currentIndex) {
                    
                /** The current iteration corresponds to an index.
                 *  Output the corresponding value.
                 */
                InterpolatedResult = currentValue;
                ResultToken = new DoubleToken(InterpolatedResult); 
                output.send(0, ResultToken);
                _match = true;
                return;     
            } else {
                    
                // _iterationCount hasn't caught up to _currentIndex yet 
                // (in particular it isn't an index), so interpolate
                // to approximate a value to associate with _iterationCount.
                // Output this value. 
                
                // If interpolation is order 0, the interpolated value is 0.
                // Output this result.
                if (_order == 0) {
                    InterpolatedResult = 0;
                    ResultToken = new DoubleToken(InterpolatedResult);
                }  
                
                // If interpolation is order 1, use a linear approximation 
                // to associate a value with _iterationCount based on 
                // the adjacent indexes and the corresponding values.  
                // Output this result.
                if (_order == 1) {
                    InterpolatedResult = previousValue + _iterationCount*(
                            currentValue - previousValue)/(
                                    currentIndex - previousIndex);
                    ResultToken = new DoubleToken(InterpolatedResult);
                }
                
                // If interpolation is order 3, use a cubic Hermite 
                // approximation to associate a value with 
                // _iterationCount based on the adjacent indexes and 
                // the corresponding values.  Output this result.
                if (_order == 3) {
                    
                    // Create the Hermite matrix M based on indices.
                    M = new double[4][4];
                    for(int col=0; col<=2; col++) {
                        M[0][col] = Math.pow(previousIndex, 3-col);
                        M[1][col] = Math.pow(currentIndex, 3-col);
                    }
                    for(int col=0; col<=1; col++) {
                        M[2][col] = (3-col)*Math.pow(
                                previousIndex, 2-col);
                        M[3][col] = (3-col)*Math.pow(
                                currentIndex, 2-col); 
                    }
                    M[0][3] = 1;
                    M[1][3] = 1;
                    M[2][3] = 0;
                    M[3][3] = 0;
                    M[2][2] = 1;
                    M[3][2] = 1;
                    
                    // Create the constraints vector P=(Vo,V,So,S) 
                    // where Vo and V are values corresponding to 
                    // previous and current indexes, So and S are the 
                    // tangents contrived to correspond to the previous 
                    // and current index.
                    P = new double[4][1];
                    P[0][0] = previousValue;
                    P[1][0] = currentValue;
                    P[2][0] = previousTangent;
                    P[3][0] = currentTangent;
                    
                    // Create the polynomial vector T=(t^3,t^2,t,1) 
                    // where t=iterationCount
                    T = new double[1][4];
                    for(int col=0; col<=3; col++) {
                        T[0][col] = Math.pow(_iterationCount, 3 - col);
                    }
                    
                    // Invert M.  THIS SHOULD BE DONE SYMBOLICALLY 
                    // ONCE AND HARDWIRED IN FOR EFFICIENCY!!!  But, 
                    // to avoid doing it by hand someone should feed 
                    // it into Mathematica.
                    double[][] Minverse;                        
                    Minverse = MatrixMath.inverse(M);
                    
                    // Multiply MinverseP to obtain Coefficient matrix 
                    // for interpolation (the Coefficient matrix contains 
                    // the coefficients of the (local) approximating cubic 
                    // polynomial.
                    double[][] Coefficient; 
                    Coefficient = MatrixMath.multiply(
                            Minverse,P);
                    
                    // Use Coefficient matrix to evaluate approximating 
                    // cubic on the particular _iterationCount.  
                    InterpolatedResult = MatrixMath.multiply(
                            T,Coefficient)[0][0];
                    ResultToken = new DoubleToken(InterpolatedResult);
                }
                output.send(0, ResultToken);
                _match = false;
            }
        } else {
            
            // Iteration is now out of range of indices.  Output zeros.
            InterpolatedResult = 0;
            ResultToken = new DoubleToken(InterpolatedResult);
            output.send(0, ResultToken);
            _match = false;
        }
    }
    
    /** Set the iteration count to zero.
     *  @exception IllegalActionException If the super class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _iterationCount = 0;
        _indexCount = 0;
    }

    /** Update the interation counters, then call the super class method.
     *  @return A boolean returned by the super class method.
     */
    public boolean postfire() throws IllegalActionException {
        ++_iterationCount;
        if (_match) {
            ++_indexCount;
        }
        return super.postfire();
    }

    /** Start an interation.
     *  @return A boolean returned by the super class method.
     *  @exception IllegalActionException If the super class throws it.
     */
    public boolean prefire() throws IllegalActionException {
        _match = false;
        return super.prefire();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // FIXME: What happens when these overflow?
    private int _iterationCount = 0;
    private int _indexCount = 0;
   
    private boolean _match = false;

    private int defaultIndexes[][] = {
        {0, 1}
    };
    private IntMatrixToken defaultIndexToken =
        new IntMatrixToken(defaultIndexes);
    
    private double defaultValues[][] = {
        {1, 0}
    };
    private DoubleMatrixToken defaultValueToken =
        new DoubleMatrixToken(defaultValues);
    
    private int defaultOrder = 0;
    private IntToken defaultOrderToken =
        new IntToken(defaultOrder);

    private int _order;
    private int indexessize;
    private int valuessize;

    private double InterpolatedResult; 
    private DoubleToken ResultToken;
    
    private int[] _indexesarray;
    private double[] _valuesarray;
   
    // Locally cached data
    private transient double[][] _values;
    private transient int[][] _indexes;
    private transient double[][] M;
    private transient double[][] T;
    private transient double[][] P;
  
}


