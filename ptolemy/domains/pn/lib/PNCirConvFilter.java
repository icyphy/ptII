/* This performs a circular convolution 

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
//// PNCirConvFilter
/** 

@author Mudit Goel
@version @(#)PNCirConvFilter.java	1.26 09/13/98
*/
public class PNCirConvFilter extends AtomicActor{
 
    /** Constructor Adds ports to the star
     * @param initValue is the initial token that the star puts in the stream
     * @exception NameDuplicationException indicates that an attempt to add
     *  two ports with the same name has been made
     */
    public PNCirConvFilter(CompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _input  = new IOPort(this, "input", true, false);
        _output = new IOPort(this, "output", false, true);
        //This is a HACK for the DogandPonyShow
        //_output.makeMultiplex(true);
    }
 
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    //FIXME: Correct this
    public void setInitState(double[] taps, int interpolation, int decimation) {
        _taps = taps;
        _numOfTaps = taps.length;
        _decim = decimation;
        _interp = interpolation;
    }
    
    /** Writes successive integers to the output
     */
    public void fire() throws IllegalActionException {
        //         Enumeration outports = _input.deepConnectedOutputPorts();
        //         while (outports.hasMoreElements()) {
        //             farport = (PNOutPort)outports.nextElement();
        //         }
        //try {
	//for (int i=0; _noOfCycles < 0 || i < _noOfCycles; i++) {
	//The basic stuff, ie. circular convolution
	//System.out.println("Before reading");
	//Enumeration outports = _input.deepConnectedOutputPorts();
	//                 while (outports.hasMoreElements()) {
	//                     farport = (PNOutPort)outports.nextElement();
	//                 }
	int j, k;
	Token[] dataabs = null;
	DoubleMatrixToken data;
	//PNOutPort farport = null;
	
	//Enumeration relations = _input.linkedRelations();
	//while (relations.hasMoreElements()) {
	//IORelation relation = (IORelation)relations.nextElement();
	//dataabs = readFrom(_input, relation);
	//}
	data = (DoubleMatrixToken)_input.get(0);
	//data = (DoubleMatrixToken)dataabs[0];
	//data = readFrom(_input, farport);
	//try {
	//System.out.println(getName()+": readfrom "+farport.getFullName());
	//} catch (Exception e) {}
	double[][] input = data.doubleMatrix();
	// for (j=0; j<input[0].length; j++) {
	//System.out.println(getName()+": input[0]["+j+"]= "+input[0][j]);
	//                 }
	if (data.getRowCount() != 1) 
	    throw new IllegalActionException((NamedObj)this,(NamedObj)this, "matrix not a row vector"); 
	double [] signalin = new double[input[0].length*_interp];
	int columns = signalin.length;
	for (j=0; j<input[0].length; j++) {
	    //upsampling
	    signalin[j*_interp] = input[0][j];
	    //FIXME: Is the following required or are all array point
	    //defaults to 0
	    if (_interp > 1) {
		for (k=1; k<_interp; k++) {
		    signalin[j*_interp+k] = 0;
		}
	    }
	}
	
	//FIXME:: Output length not good for phase!!
	double[][] output = new double[1][signalin.length/_decim];
	for (j=0; j<output[0].length; j++) {
	    output[0][j] = 0;
	    //FIXME: Not sure about interpolation
	    for (k=0; k<_numOfTaps; k++) {
		output[0][j] += _taps[k]*signalin[(j*_decim-k+columns)%columns];
	    }
	    // debug
	    // System.out.println(getName() + ": output[0]["+j+"] = " + output[0][j]);
	}
	
	data = new DoubleMatrixToken(output);
	// debug
	//double[][] tem = data.doubleMatrix();
	//for (k = 0; k < tem[0].length; k++) {
	// System.out.println("tem[0]["+k+"] = " + tem[0][k]);
	// 		}
	//writeTo(_output, data);
	_output.broadcast(data);
    }
    // System.out.println("Terminating at al "+this.getName());
    //((PNDirector)getDirector()).processStopped();
    //} catch (NoSuchItemException e) {
    // System.out.println("Terminating "+this.getName());
    //return;
    //} catch (IllegalActionException e) {
    //System.err.println("Exception: " + e.toString());
    //return;
    //}
    //}
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /* This is the initial value that the star puts in the stream */
    private double[] _taps = { -0.040609, -.001628, .17853, .37665, .37665, .17853, -.001628, -0.040609 };
    private int _numOfTaps = 8;
    private int _decim = 2;
    //    private int _phase = 0;
    private int _interp = 1;
    /* Output port */
    private IOPort _input;
    private IOPort _output;
}
