/* A D/A converter.

 Copyright (c) 1997-1999 The Regents of the University of California.
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
import java.io.*;

//////////////////////////////////////////////////////////////////////////
//// PNDAConverter
/** 
A D/A converter.
The input is an IntMatrixToken which stores the digital representation
of the samples in 2's complement format.  The output is a DoubleMatrixToken
storing the amplitude of the signal.

@author Yuhong Xiong, Mudit Goel
@(#)PNDAConverter.java	1.11 09/13/98
*/
public class PNDAConverter extends AtomicActor {

    /** Constructor. Creates ports
     * @exception NameDuplicationException is thrown if more than one port 
     *  with the same name is added to the star or if another star with an
     *  an identical name already exists.
     */
    public PNDAConverter(CompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    	_digital = new IOPort(this, "digital", true, false);
    	_analog  = new IOPort(this, "analog", false, true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    //FIXME: Correct this
    public void setInitState(double stepSize) {
	_stepSize = stepSize;
    }

    /** Convert the input digital samples to analog.
     */
    public void fire() throws IllegalActionException {

	Token inputemp;
	IntMatrixToken input;
	inputemp = _digital.get(0);
	input = (IntMatrixToken)inputemp;
	int[][] code = input.intMatrix();
	
	int size = code[0].length;
	double[][] amplitude = new double[1][size];
	for (int k = 0; k < size; k++) {
	    amplitude[0][k] = code[0][k] * _stepSize;
	}
	
	DoubleMatrixToken output = new DoubleMatrixToken(amplitude);
	//System.out.println("Printing in DAC with stepsize = "+_stepSize);
	_analog.broadcast(output);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    double _stepSize = 0.0;

    // The input port 
    private IOPort _digital;
    // The output port 
    private IOPort _analog;
}







