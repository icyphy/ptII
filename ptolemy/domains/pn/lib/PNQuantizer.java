/* Quantize input using certain number of bits and step size.

 Copyright (c) 1997-1998 The Regents of the University of California.
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
*/

package ptolemy.domains.pn.lib;
import ptolemy.domains.pn.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import java.util.Enumeration;
import java.lang.Math;

//////////////////////////////////////////////////////////////////////////
//// PNQuantizer
/** 
Quantize input using certain number of bits and step size.
The output amplitude is rounded to the nearest quantization level.
The input and amplitude output tokens are DoubleMatrixToken.
The output code is in 2's complement format.

@author Yuhong Xiong, Mudit Goel
@(#)PNQuantizer.java	1.19 09/13/98
*/
public class PNQuantizer extends AtomicActor {

    /** Constructor. Creates ports
     * @exception NameDuplicationException is thrown if more than one port 
     *  with the same name is added to the star or if another star with an
     *  an identical name already exists.
     */
    public PNQuantizer(CompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    	_unquantized = new IOPort(this, "unquantized", true, false);
    	_quantized = new IOPort(this, "quantized", false, true);
    	_codeBook = new IOPort(this, "codeBook", false, true);
    	_numBitsOut = new IOPort(this, "numBits", false, true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    // FIXME: should this method check validity of input?
    //FIXME: Correct this
    public void setInitState(int numBits, double stepSize)
            throws IllegalActionException {
	if (numBits < 1) {
	    throw new IllegalActionException(
                    "number of bits in PNQuantizer can't be " + numBits);
	}
	if (stepSize <= 0.0) {
	    throw new IllegalActionException(
                    "step size in PNQuantizer can't be " + stepSize);
	}
        _numBits = numBits;
	_stepSize = stepSize;

	_maxCode = (int)Math.pow(2, numBits-1) - 1;
	_minCode = -(_maxCode+1);

	_upperBound = _maxCode * stepSize;
	_lowerBound = -_upperBound - stepSize;
    }

    /** Do quantization.  The DoubleMatrixToken carrying input is modified
     *  with quantized data and reused for output.
     */
    public void fire() throws IllegalActionException {
        //try {
	//int i;
	//for (i=0; _noOfCycles < 0 || i < _noOfCycles; i++) {
	//System.gc();
	//System.out.println(getName()+" upper/lower = "+_upperBound+"/"+_lowerBound);
	//System.out.println(getName()+" step/num = "+_stepSize+"/"+_numBits);
	// Enumeration outports = _unquantized.deepConnectedOutputPorts();
	//while (outports.hasMoreElements()) {
	//PNOutPort outport = (PNOutPort)outports.nextElement();
	
	//Enumeration relations = _unquantized.linkedRelations();
	//while (relations.hasMoreElements()) {
	//IORelation relation = (IORelation)relations.nextElement();
	Token temp = _unquantized.get(0);
	//Token[] temp = readFrom(_unquantized, relation);
	DoubleMatrixToken signalToken = (DoubleMatrixToken)temp;
	double[][] signal = signalToken.doubleMatrix();
	
	int[][] code = new int[1][signal[0].length];
	// only quantize the first row of the matrix.
	for (int k = 0; k < signal[0].length; k++) {
	    if (signal[0][k] <= _lowerBound) {
		signal[0][k] = _lowerBound;
		code[0][k] = _minCode;
		if (signal[0][k] < _lowerBound) {
		    System.out.println("Under in "+getName());
		}
	    } else if (signal[0][k] >= _upperBound) {
		signal[0][k] = _upperBound;
		code[0][k] = _maxCode;
		if (signal[0][k] > _upperBound) {
		    System.out.println(" Overflow in "+getName());
		}
	    } else {
		double position = (double)
		    (signal[0][k] - _lowerBound) / _stepSize;
		int level = (int)Math.floor(position);
		if (position - level >= 0.5) {
		    level++;
		} 
		signal[0][k] = _lowerBound + (level*_stepSize);
		code[0][k] = _minCode + level;
	    }
	}
	
	signalToken = new DoubleMatrixToken(signal);
	IntMatrixToken codeToken = new IntMatrixToken(code);
	IntToken bitToken = new IntToken(_numBits);
	_quantized.broadcast(signalToken);
	_codeBook.broadcast(codeToken);
	_numBitsOut.broadcast(bitToken);
	//writeTo(_quantized, signalToken);
	//writeTo(_codeBook, codeToken);
	//writeTo(_numBitsOut, bitToken);
    }
    //}                
    //((PNDirector)getDirector()).processStopped();
    //} catch (NoSuchItemException e) {
    // System.out.println("Terminating "+ this.getName());
    //return;
    //}
    //}

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    int _numBits = 0;
    double _stepSize = 0.0;

    double _upperBound = 0.0;
    double _lowerBound = 0.0;

    int _minCode = 0;		// lowest code book entry.
    int _maxCode = 0;		// highest code book entry.
    
    // The input port 
    private IOPort _unquantized;
    // The output port 
    private IOPort _quantized;
    private IOPort _codeBook;
    private IOPort _numBitsOut;
}

