/* Decide whether to split the child wavelet packet tree.

 Copyright (c) 1997 The Regents of the University of California.
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

//////////////////////////////////////////////////////////////////////////
//// PNRDDecider
/** 
/* Controller for wavelet packet tree using rate/distortion criteria.
   Each controller has a depth and nodeId indicating the position
   in the tree, and the parameter lambda.  See the paper
   by Ramchandran and Vetterli for details.

@author Yuhong Xiong, Mudit Goel
@(#)PNRDDecider.java	1.25 08/20/98
*/
public class PNRDDecider extends AtomicActor {

    /** Constructor initialize depth and nodeId, lambda and isLeaf.
     * @param myExecutive is the executive responsible for the simulation
     * @exception NameDuplicationException indicates that an attempt to add
     *  two ports with the same name has been made or a star with an 
     *  identical name already exists.
     */ 
    public PNRDDecider(CompositeActor container, String name,
		int depth, int nodeId, boolean isLeaf)
             throws IllegalActionException, NameDuplicationException {
        super(container, name);

	_info = new PNRDInfo();

	_info.depth  = depth;
	_info.nodeId = nodeId;
	_isLeaf     = isLeaf;

        _unquantized = new IOPort(this, "unquantized", true, false);
        _quantized   = new IOPort[_numQuantizers];
	_codeBook    = new IOPort[_numQuantizers];
	_numBits     = new IOPort[_numQuantizers];
	for (int i = 0; i < _numQuantizers; i++) {
	    _quantized[i] = new IOPort(this, "quantized" + i, true, false);
	    _codeBook[i]  = new IOPort(this, "codeBook" + i, true, false);
	    _numBits[i]   = new IOPort(this, "numBits" + i, true, false);
	}

	_infoIn0  = new IOPort(this, "infoIn0", true, false);
	_infoIn1  = new IOPort(this, "infoIn1", true, false);
	_lambdaIn = new IOPort(this, "lambdaIn", true, false);

        _infoOut   = new IOPort(this, "infoOut", false, true);
        _lambdaOut = new IOPort(this, "lambdaOut", false, true);
        //THIS IS A HACK
        //_lambdaOut.makeMultiplex(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** This decider goes through the following sequence repeatedly:
     *  (1) read filter output and 3 quantizer outputs.
     *	    computer rate/distortion/quantizerId/codeBookEntry(RDQC).
     *  (2) read lambda.(leaf decider gets lambda from global controller,
     *	    other deciders get lambda from even child decider.
     *      if I'm not root && nodeId is even
     *		send parent deicider lambda.
     *  (3) if I'm a leaf,
     *		send parent decider PNRDInfo.
     *      else
     *		read from both children PNRDInfo.
     *		decide whether to split, if so, update rate/distortion.
     *		send out PNRDInfo. // to globalController is I'm root;
     *				   // to parentDecider if I'm not roo.
     *  (4) goto (2)
     */	
    public void fire() throws IllegalActionException {
	ObjectToken infoToken = new ObjectToken();
        int i;
        
        //try {
	_computeRateDistortion();
	// System.out.println("After the comp the loop and noc = " + _noOfCycles);
	//for (i=0; _noOfCycles < 0 || i < _noOfCycles; i++) {
	while(true) {
	    // System.out.println("Inside the  for loop");
	    //System.gc();
	    _readPassLambda();
	    _computeCost();
	    if (!_isLeaf) {
		_decide();
	    }
	    try {
		infoToken = new ObjectToken(_info);
	    } catch (IllegalActionException e) {
		System.out.println("Error: failed to set PNRDInfo to " +
			"object token.");
	    }
	    _infoOut.broadcast(infoToken);
	    //writeTo(_infoOut, infoToken);
	    //System.gc();
	    // debuggin
	    //_print();
	}
	//((PNDirector)getDirector()).processStopped();
	//} catch (NoSuchItemException e) {
	// System.out.println("Terminating "+ this.getName());
	//return;
    }
    //}
    
    ///////////////////////////////////////////////////////////////////
    ////                         private method                    ////
    
    // read filter and 3 quantizer output, compute rate, distortion, etc.
    private void _computeRateDistortion() 
            throws IllegalActionException {

	_rate = new int[_numQuantizers];
	_distortion = new double[_numQuantizers];
	_codeBookEntry = new int[_numQuantizers][];

	double[] signal = null;
        // System.out.println("Inside CRD");

	// read filter output.
// 	Enumeration outports = _unquantized.deepConnectedOutputPorts();
// 	while (outports.hasMoreElements()) {
//             PNOutPort outport = (PNOutPort)outports.nextElement();

        //Enumeration relations = _unquantized.linkedRelations();
        //while (relations.hasMoreElements()) {
	//IORelation relation = (IORelation)relations.nextElement();
	Token signalToken = _unquantized.get(0);
	//Token[] signalToken = readFrom(_unquantized, relation);
	signal = ((DoubleMatrixToken)signalToken).doubleMatrix()[0];
        //}

	// get the rate, distorion, and codeBook entries from all quantizers.
	for (int i = 0; i < _numQuantizers; i++) {

	    // compute distortion.
	    DoubleMatrixToken quantSigToken = null;
	    //outports = _quantized[i].deepConnectedOutputPorts();
	    //while (outports.hasMoreElements()) {
	    //PNOutPort outport = (PNOutPort)outports.nextElement();
            //relations = _quantized[i].linkedRelations();
            //while (relations.hasMoreElements()) {
	    //IORelation relation = (IORelation)relations.nextElement();
	    //quantSigToken = (DoubleMatrixToken)(readFrom(_quantized[i], relation))[0];
	    quantSigToken = ((DoubleMatrixToken)_quantized[i].get(0));
	    //}
	    double[] quantSig = quantSigToken.doubleMatrix()[0];
	    _distortion[i] = 0.0;
	    for (int j = 0; j < quantSig.length; j++) {
		_distortion[i] += (signal[j]-quantSig[j]) *
				  (signal[j]-quantSig[j]);
	    }

	    // get codeBookEntry.
	    IntMatrixToken codeToken = null;
	    //outports = _codeBook[i].deepConnectedOutputPorts();
	    //while (outports.hasMoreElements()) {
	    //PNOutPort outport = (PNOutPort)outports.nextElement();
            //relations = _codeBook[i].linkedRelations();
            //while (relations.hasMoreElements()) {
	    //IORelation relation = (IORelation)relations.nextElement();
	    codeToken = (IntMatrixToken)_codeBook[i].get(0);
	    //}
	    _codeBookEntry[i] = codeToken.intMatrix()[0];
	    
	    // compute rate.
	    IntToken numBitsToken = null;
	    //outports = _numBits[i].deepConnectedOutputPorts();
	    //while (outports.hasMoreElements()) {
	    //     PNOutPort outport = (PNOutPort)outports.nextElement();
            //relations = _numBits[i].linkedRelations();
            //while (relations.hasMoreElements()) {
	    //IORelation relation = (IORelation)relations.nextElement();
	    numBitsToken = (IntToken)_numBits[i].get(0);
	    //}
	    _rate[i] = _codeBookEntry[i].length * numBitsToken.intValue();
	}
    }

    // The parameter lambda is passed to leaf nodes from global controller.
    // The node whose nodeId is even pass it up to the parent node.
    private void _readPassLambda() 
            throws IllegalActionException {
	DoubleToken lambdaToken = null;

	// there should be only one port connected to _lmbdaIn, although
	// the standard enumeration is used.
	//Enumeration outputs = _lambdaIn.deepConnectedOutputPorts();
	//while (outputs.hasMoreElements()) {
	//PNOutPort outport = (PNOutPort)outputs.nextElement();
        //Enumeration relations = _lambdaIn.linkedRelations();
        //while (relations.hasMoreElements()) {
	//IORelation relation = (IORelation)relations.nextElement();
	lambdaToken = (DoubleToken)_lambdaIn.get(0);
	//}
	_lambda = lambdaToken.doubleValue();

	//if ((_info.nodeId & 1) == 0 && _info.depth != 0) {
        // even node, not root
	_lambdaOut.broadcast(lambdaToken);
        //writeTo(_lambdaOut, lambdaToken);
        //}
    }

    // choose the best quantizer based on lambda.  This step just considers
    // local filter, not including child info.
    // set info parameters.
    private void _computeCost() {
	_cost = _distortion[0] + _lambda * _rate[0];
	_info.quantizerId = 0;
	for (int i = 1; i < _numQuantizers; i++) {
	    double c = _distortion[i] + _lambda * _rate[i];
	    if (c < _cost) {
		_info.quantizerId = i;
		_cost = c;
	    }
	}
	
	_info.rate = _rate[_info.quantizerId];
	_info.distortion = _distortion[_info.quantizerId];
	_info.codeBookEntry = _codeBookEntry[_info.quantizerId];
	_info.left = null;
	_info.right = null;
    }
    
    // read info from both children, decide whether to split.
    private void _decide() throws IllegalActionException {
	ObjectToken infoToken = null;
	PNRDInfo leftInfo, rightInfo;
	
	//Enumeration outports = _infoIn0.deepConnectedOutputPorts();
	//while (outports.hasMoreElements()) {
	//PNOutPort outport = (PNOutPort)outports.nextElement();
        //Enumeration relations = _infoIn0.linkedRelations();
        //while (relations.hasMoreElements()) {
	//IORelation relation = (IORelation)relations.nextElement();
	infoToken = (ObjectToken)_infoIn0.get(0);
	//}
	leftInfo = (PNRDInfo)infoToken.getValue();
	
	//outports = _infoIn1.deepConnectedOutputPorts();
	//while (outports.hasMoreElements()) {
	//PNOutPort outport = (PNOutPort)outports.nextElement();
        //relations = _infoIn1.linkedRelations();
        //while (relations.hasMoreElements()) {
	//IORelation relation = (IORelation)relations.nextElement();
	infoToken = (ObjectToken)_infoIn1.get(0);
	//}
	rightInfo = (PNRDInfo)infoToken.getValue();
        //System.out.println("the name ="+getName());
        // System.out.println("leftrate="+leftInfo.rate+" rgtrate="+rightInfo.rate);
        int subTreeRate = leftInfo.rate + rightInfo.rate;
	double subTreeDist = leftInfo.distortion + rightInfo.distortion;
	double subTreeCost = subTreeDist + _lambda * subTreeRate;
        // System.out.println("orig rate="+_info.rate+" subtree rate="+subTreeRate);

	if (subTreeCost < _cost) {		// split
	    _info.rate = subTreeRate;
	    _info.distortion = subTreeDist;
	    _info.left = leftInfo;
	    _info.right = rightInfo;
	}
    }
    
    // for debugging.
    private void _print() {
	System.out.println("PNRDDecider: depth/nodeId = " + _info.depth +
		"/" + _info.nodeId + "lambda = " + _lambda);
	if (_rate != null && _distortion != null) {
	    System.out.println("rate0/1/2 = " + _rate[0] + "/" + _rate[1] +
		    "/" + _rate[2]);
	    System.out.println("distortion/1/2 = " + _distortion[0] + "/" +
		    _distortion[1] + "/" + _distortion[2]);
	}
	
	System.out.println("_info.rate/distortion = " + _info.rate + "/" +
		_info.distortion);
	System.out.println("_info.qunatizerId = " + _info.quantizerId);
	if (_info.left == null) {
	    System.out.println("Not splitting subtree.");
	} else {
	    System.out.println("Splitting subtree.");
	}
	
	for (int i=0; i<_info.codeBookEntry.length; i++) {
	    System.out.print("  " + _info.codeBookEntry[i]);
	}
	System.out.println("\n\n");
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    private double _lambda = 0.0;
    private boolean _isLeaf = false;
    private int _numQuantizers = 4;
    
    // The following are based on my local filter/quantizers.
    private int _rate[];
    private double _distortion[];
    private int[][] _codeBookEntry;
    private double _cost;
    
    private PNRDInfo _info;
    
    // Input ports 
    private IOPort _unquantized;
    private IOPort[] _quantized;
    private IOPort[] _codeBook;
    private IOPort[] _numBits;
    private IOPort _infoIn0;
    private IOPort _infoIn1;
    private IOPort _lambdaIn;

    // Output port 
    private IOPort _infoOut;
    private IOPort _lambdaOut;
}

