/* This is the main controller for the RD scheme of Ramachandra-Vetterli

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
import ptolemy.kernel.event.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.data.*;
import java.util.Enumeration;
import java.lang.Math;
import java.io.*;

//////////////////////////////////////////////////////////////////////////
//// PNRDController
/** 
@author Mudit Goel
@version @(#)PNRDController.java	1.32 09/13/98
*/
public class PNRDController extends AtomicActor {
    
    /** Constructor  Adds port   
     * @exception NameDuplicationException is thrown if more than one port 
     *  with the same name is added to the star
     */
    public PNRDController(CompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _signalport = new IOPort(this, "signalport", true, false);
        _lambdaOut = new IOPort(this, "lambdaOut", false, true);
        //THIS IS A HACK
        //_lambdaOut.makeMultiplex(true);
        _parentPort = new IOPort(this, "infoOut", false, true);
    }
    

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    //FIXME: Correct this
    /** Initializes the Star. Should be called before execution
     * @param prime is the prime for this sieve
     */	
    public void setInitState(int numberofblocks, int rbudget, int maxdepth, 
	    double[] highpass, double[] lowpass) {
        _taps = new double[2][];
        //NOte that one of the following 2 is redundant
        _numberofblocks = numberofblocks;
        _rbudget = rbudget;
        _maxdepth = maxdepth;
        _rdnew = new PNRDInfo[numberofblocks];
        _taps[0] = highpass;
        _taps[1] = lowpass;
        System.out.println("In controller setINit");
    }
    
    /** Reads one Token from it's input port and writes this token to 
     *  it's output ports. Needs to read one token for every output
     *  port. 
     */
    public void fire() throws IllegalActionException {
        DoubleMatrixToken data = null;
        //setCycles(((PNCompositeActor)getContainer()).getCycles());
	
        //try {
	//for (i=0; _noOfCycles < 0 || i < _noOfCycles; i++) {
	while (true) {
	    // Enumeration outports = _signalport.deepConnectedOutputPorts();
	    // while (outports.hasMoreElements()) {
	    // PNOutPort outport = (PNOutPort)outports.nextElement();
	    //Enumeration relations = _signalport.linkedRelations();
	    //while (relations.hasMoreElements()) {
	    //IORelation relation = (IORelation)relations.nextElement();
	    //Token[] temp = readFrom(_signalport, relation);
	    //data = (DoubleMatrixToken)temp[0];
	    data = (DoubleMatrixToken)_signalport.get(0);
	    //System.out.println("read form signalport in controller");
	    //}
	    
	    double[][] signalin = data.doubleMatrix();
	    int blocklength = data.getColumnCount();
	    blocklength = blocklength/_numberofblocks;
	    double[][][] signalout = 
		    new double[_numberofblocks][1][blocklength];
	    _rootNodes = new IOPort[_numberofblocks];
	    _infoPorts = new IOPort[_numberofblocks];
	    //Copying the signal and breaking it up in blocks
	    for(int j1=0; j1<_numberofblocks; j1++ ) {
		for (int k1=0; k1<blocklength; k1++) {
		    signalout[j1][0][k1] = signalin[0][j1*blocklength + k1];
		}
	    }
	    
	    //Creating initial subtrees for each block
	    //FIXME: LIst as mutations!
	    TopologyChangeRequest request = new TopologyChangeRequest(this) {
		public void constructEventQueue() {
		    try {
			_outsignal = new IOPort[_numberofblocks];
			IOPort inport;
			IOPort outport;
			for(int j=0; j<_numberofblocks; j++) {
			    _outsignal[j] = new IOPort(PNRDController.this, 
				    "outputBlock"+j, 
				    false, true); 
			    //_outsignal[j].makeMultiplex(true);
			    _infoPorts[j] = new IOPort(PNRDController.this, 
				    "infoIn"+j, true, false);
			    
			    boolean isLeaf;
			    if (_maxdepth == 0) isLeaf = true;
			    else isLeaf = false;
			    
			    PNRDDecider parent = new PNRDDecider(
				    (CompositeActor)getContainer(),"decider-"+
				    j+"-0-0", 0, 0, isLeaf);
			    
			    //rootNode's infoOut to the Controller's input
			    _rootNodes[j] = (IOPort)parent.getPort("infoOut");
			    Enumeration temps= _rootNodes[j].linkedRelations();
			    if (temps.hasMoreElements()) {
				_infoPorts[j].link((IORelation)temps.nextElement());
			    } else {
				((CompositeActor)getContainer()).connect(
					_infoPorts[j], _rootNodes[j]);
			    }
			    
			    //Source to Decider
			    inport = (IOPort)parent.getPort("unquantized");
			    outport = _outsignal[j];
			    temps = outport.linkedRelations();
			    if (temps.hasMoreElements()) {
				inport.link((IORelation)temps.nextElement());
			    } else {
				((CompositeActor)getContainer()).connect(
					inport, outport);
			    }
			    //inport.getQueue(outport).setCapacity(1);
			    
			    //Quantizer being added to it
			    int numQuant =4;
			    PNQuantizer[] quantizer = new PNQuantizer[numQuant];
			    for(int k=0; k<numQuant; k++) {
				quantizer[k] = new PNQuantizer(
					(CompositeActor)getContainer(), 
					"quantizer-"+j+"-0-0-"+k);
				//Qunatizer to decider -- quantized
				inport = (IOPort)parent.getPort("quantized"+k);
				outport = (IOPort)quantizer[k].getPort("quantized");
				temps = outport.linkedRelations();
				if (temps.hasMoreElements()) {
				    inport.link((IORelation)temps.nextElement());
				} else {
				    ((CompositeActor)getContainer()).connect(
					    inport, outport);
				}
				//inport.getQueue(outport).setCapacity(1);
				
				//Quantizer to decider -- codeBook
				inport = (IOPort)parent.getPort("codeBook"+k);
				outport = (IOPort)quantizer[k].getPort("codeBook");
				temps = outport.linkedRelations();
				if (temps.hasMoreElements()) {
				    inport.link((IORelation)temps.nextElement());
				} else {
				    ((CompositeActor)getContainer()).connect(
					    inport, outport);
				}
				//inport.getQueue(outport).setCapacity(1);
				
				//Qunatizer to decider -- numBits
				inport = (IOPort)parent.getPort("numBits"+k);
				outport = (IOPort)quantizer[k].getPort("numBits");
				temps = outport.linkedRelations();
				if (temps.hasMoreElements()) {
				    inport.link((IORelation)temps.nextElement());
				} else {
				    ((CompositeActor)getContainer()).connect(
					    inport, outport);
				}
				//inport.getQueue(outport).setCapacity(1);
				
				
				//Source to quantizer
				inport = (IOPort)quantizer[k].getPort(
					"unquantized");
				outport = _outsignal[j];
				Enumeration relations1 = outport.linkedRelations();
				inport.link(((IORelation)relations1.nextElement()));
				//FIXME: Setting initial state in quantizer. This might change
				int numbits = 14-k*4;
				int stepsize = (int)(Math.pow(2,16)/Math.pow(2,
					numbits));
				quantizer[k].setInitState(numbits, stepsize);
			    }
			    //Creating all the subtrees...
			    createSubtree(parent, _outsignal[j],j, 1, 0);
			}
		    } catch (IllegalActionException ex) {
			throw new InvalidStateException("Cannot create " +
				"new Decider or port.");
		    } catch (NameDuplicationException ex) {
			throw new InvalidStateException("Cannot create " +
				"new Decider or port.");
		    }
		}
	    };
	    
	    //Inform the director to schedule all the mutations
	    //FIXME!!
	    ((PNDirector)getDirector()).queueTopologyChangeRequest(request);
	    
	    //Writing the blocks to each subtree
	    for (int j1=0; j1<_numberofblocks; j1++) {
		DoubleMatrixToken dataout=new DoubleMatrixToken(signalout[j1]);
		//writeTo(_outsignal[j], dataout);
		_outsignal[j1].broadcast(dataout);
	    }
	    
	    
	    //Now starting the algorithm
	    double lambdaU = 10000000;
	    double lambdaL = 0;
	    double lambdaNext = lambdaL;
	    
	    long rateL = 0;
	    long rateU = 0;
	    double distU = 0;
	    double distL = 0;
	    long newrate = 0;
	    double newdist = 0;
	    DoubleToken lambda;
	    boolean lambdaFlag = false;  
	    boolean first = true;
	    
	    //Writing the first lambda starts the simulation
	    lambda = new DoubleToken(lambdaNext);
	    _lambdaOut.broadcast(lambda);
	    //writeTo(_lambdaOut, lambda);
	    while (!lambdaFlag) {
		newrate = 0;
		newdist = 0;
		// System.out.println("New lambda is "+lambdaNext);
		
		for(int j=0; j<_numberofblocks; j++) {
		    //System.out.print("Printing for block "+j);
		    //relations = _infoPorts[j].linkedRelations();
		    //while (relations.hasMoreElements()) {
		    //IORelation relation = (IORelation)relations.nextElement();
		    //Token[] temp = readFrom(_infoPorts[j], relation);
		    Token temp = _infoPorts[j].get(0);
		    _rdnew[j] = (PNRDInfo)((ObjectToken)temp).getValue();
		    //}
		    newrate += _rdnew[j].rate;
		    newdist += _rdnew[j].distortion;
		}
		System.out.println("******newrate = "+newrate+
			" rateU="+rateU+" rateL="+rateL);
		System.out.println("******newdist = "+newdist+" distU="+distU+
			" distL="+distL);
		System.out.println("lambda is "+lambdaNext);
		//If this is the first iteration, then find lambda for the 
		// upper lambda limit
		if(first) {
		    lambdaNext = lambdaU;
		    rateL = newrate;
		    distL = newdist;
		    first = false;
		    if (rateL <= _rbudget) { 
			lambdaFlag = true; 
			rateU = newrate; 
			distU = newdist;
		    } else {
			lambda = new DoubleToken(lambdaNext);
			_lambdaOut.broadcast(lambda);
			//writeTo(_lambdaOut, lambda);
		    }
		} else if (Math.abs(newrate - rateU) < _error) {
		    lambdaU = lambdaNext;
		    distU = newdist;
		    rateU = newrate;
		    lambdaFlag = true;
		} else {
		    //Simulation should continue
		    if (newrate > _rbudget) {
			lambdaL = lambdaNext;
			distL = newdist;
			rateL = newrate;
		    } else {
			lambdaU = lambdaNext;
			distU = newdist;
			rateU = newrate;
		    }
		    lambdaNext = Math.abs((distL-distU)/(double)(rateL-rateU))+
			_epsilon;
		    //Start the next simulation
		    lambda = new DoubleToken(lambdaNext);
		    _lambdaOut.broadcast(lambda);
		    //writeTo(_lambdaOut, lambda);
		} 
	    }
	    for (int j = 0; j<_numberofblocks; j++) {
		_debugging(_rdnew[j],j);
	    }
	    
	    ObjectToken dataout = new ObjectToken(_rdnew);
	    //writeTo(_parentPort, dataout);
	    _parentPort.broadcast(dataout);
	}
	//System.out.println("Terminating Controller self");
	//((PNDirector)getDirector()).processStopped();
	//} catch (NoSuchItemException e) {
	// System.out.println("Terminating "+ this.getName());
	//return;
	//} catch (NameDuplicationException e) {
	//System.err.println("Exception: " + e.toString());
	//This should never be thrown
	//} catch (IllegalActionException e) {
	//This should never be thrown
	//System.err.println("Exception: " + e.toString());
	//}
    }
    
    
    public void createSubtree(PNRDDecider parent, IOPort outSignal, 
            int blocknumber, int currentdepth, int parentbranch) 
	 throws IllegalActionException, NameDuplicationException {
	     
        int i, j;
        PNCirConvFilter[] filter = new PNCirConvFilter[2];
        PNRDDecider[] child = new PNRDDecider[2];
        int numQuant = 4;
        PNQuantizer[][] quantizer = new PNQuantizer[2][numQuant];

        // System.out.println("Entering subtree ");
        boolean isLeaf;
        if (currentdepth == _maxdepth) isLeaf = true;
        else isLeaf = false;
        int mybranch;
        for (i=0; i<2; i++) {
            mybranch = 2*parentbranch+i;
            filter[i] = new PNCirConvFilter((CompositeActor)getContainer(), 
		    "filter-"+blocknumber+"-"+currentdepth+"-"+mybranch);
            filter[i].setInitState(_taps[i], 1, 2);
            //System.out.println(filter[i].getName()+" is "+i);
            child[i] = new PNRDDecider((CompositeActor)getContainer(),
		    "decider-"+blocknumber+"-"+currentdepth+"-"+mybranch, 
		    currentdepth, mybranch, isLeaf);
            //Filter to decider
            IOPort inport = (IOPort)child[i].getPort("unquantized");
            IOPort outport = (IOPort)filter[i].getPort("output");
            Enumeration temps = outport.linkedRelations();
            if (temps.hasMoreElements()) {
                inport.link((IORelation)temps.nextElement());
            } else {
                ((CompositeActor)getContainer()).connect(inport, outport);
            }
            //inport.getQueue(outport).setCapacity(1);
            
            //Source to filters
            inport = (IOPort)filter[i].getPort("input");
            outport = outSignal;
            Enumeration relations1 = outport.linkedRelations();
            if (relations1.hasMoreElements()) {
                inport.link((IORelation)relations1.nextElement());
            } else {
                ((CompositeActor)getContainer()).connect(inport, outport);
            }
            //inport.getQueue(outport).setCapacity(1);           

            //Decider lambda to parent
            if (i==0) {
                PNSink sink = new PNSink((CompositeActor)getContainer(), 
			"sink-"+blocknumber+"-"+currentdepth+"-"+mybranch);
                inport = (IOPort)sink.getPort("input");
                outport = (IOPort)parent.getPort("lambdaOut");
                temps = outport.linkedRelations();
                if (temps.hasMoreElements()) {
                    inport.link((IORelation)temps.nextElement());
                } else {
                    ((CompositeActor)getContainer()).connect(inport, outport);
                }
                inport = (IOPort)parent.getPort("lambdaIn");
                outport = (IOPort)child[i].getPort("lambdaOut");
                temps = outport.linkedRelations();
                if (temps.hasMoreElements()) {
                    inport.link((IORelation)temps.nextElement());
                } else {
                    ((CompositeActor)getContainer()).connect(inport, outport);
                }
                //inport.getQueue(outport).setCapacity(1);
            } else {
                PNSink sink = new PNSink((CompositeActor)getContainer(), 
			"sink-"+blocknumber+"-"+currentdepth+"-"+mybranch);
                inport = (IOPort)sink.getPort("input");
                outport = (IOPort)child[i].getPort("lambdaOut");
                temps = outport.linkedRelations();
                if (temps.hasMoreElements()) {
                    inport.link((IORelation)temps.nextElement());
                } else {
                    ((CompositeActor)getContainer()).connect(inport, outport);
                }
                //inport.getQueue(outport).setCapacity(1);
            }

            //Decider info to parent
            inport = (IOPort)parent.getPort("infoIn"+i);
            outport = (IOPort)child[i].getPort("infoOut");
            temps = outport.linkedRelations();
            if (temps.hasMoreElements()) {
                inport.link((IORelation)temps.nextElement());
            } else {
                ((CompositeActor)getContainer()).connect(inport, outport);
            }
            //inport.getQueue(outport).setCapacity(1);

            //Quantizers -- connections et. al.
            //int numQuant =4;
            for (j=0; j<numQuant; j++) {
                quantizer[i][j] = new PNQuantizer(
			(CompositeActor)getContainer(), "quantizer-"+
			blocknumber+"-"+currentdepth+"-"+mybranch+"-"+j);
                int numbits = 14-j*4;
                int stepsize = (int)(Math.pow(2,16)/Math.pow(2,numbits));
                quantizer[i][j].setInitState(numbits, stepsize);                
                //Quantizer to decider
                inport = (IOPort)child[i].getPort("quantized"+j);
                outport = (IOPort)quantizer[i][j].getPort("quantized");
                temps = outport.linkedRelations();
                if (temps.hasMoreElements()) {
                    inport.link((IORelation)temps.nextElement());
                } else {
                    ((CompositeActor)getContainer()).connect(inport, outport);
                }
                //inport.getQueue(outport).setCapacity(1);

                //Filter to quantizer
                inport = (IOPort)quantizer[i][j].getPort("unquantized");
                outport = (IOPort)filter[i].getPort("output");
                temps = outport.linkedRelations();
                if (temps.hasMoreElements()) {
                    inport.link((IORelation)temps.nextElement());
                } else {
                    ((CompositeActor)getContainer()).connect(inport, outport);
                }
                //inport.getQueue(outport).setCapacity(1);

                //Quantizer to decider
                inport = (IOPort)child[i].getPort("codeBook"+j);
                outport = (IOPort)quantizer[i][j].getPort("codeBook");
                temps = outport.linkedRelations();
                if (temps.hasMoreElements()) {
                    inport.link((IORelation)temps.nextElement());
                } else {
                    ((CompositeActor)getContainer()).connect(inport, outport);
                }
                //inport.getQueue(outport).setCapacity(1);

                //Quantizer to decider
                inport = (IOPort)child[i].getPort("numBits"+j);
                outport = (IOPort)quantizer[i][j].getPort("numBits");
                temps = outport.linkedRelations();
                if (temps.hasMoreElements()) {
                    inport.link((IORelation)temps.nextElement());
                } else {
                    ((CompositeActor)getContainer()).connect(inport, outport);
                }
                //inport.getQueue(outport).setCapacity(1);

            }
            if (currentdepth == _maxdepth) {
                //Leafnode lambda to Controller
                inport = (IOPort)child[i].getPort("lambdaIn");
                outport = _lambdaOut;
                temps = outport.linkedRelations();
                if (temps.hasMoreElements()) {
                    inport.link((IORelation)temps.nextElement());
                } else {
                    ((CompositeActor)getContainer()).connect(inport, outport);
                }
                //inport.getQueue(outport).setCapacity(1);
            } else {
                createSubtree(child[i], (IOPort)filter[i].getPort("output"),
			blocknumber, currentdepth+1, mybranch );   
            }
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private void _debugging(PNRDInfo info, int j) {
        if (info.left != null) {
            if (info.right == null) {
                System.out.println("inforight is null");
            }
            _debugging(info.left,j);
            _debugging(info.right,j);
        } else {
            if (info.right != null) System.out.println("inforight is null");
            System.out.println("block ="+j+" left = depth/branch = "+info.depth+"/"+info.nodeId);
            
        }
    }
    
    private IOPort _parentPort;
    private IOPort[] _outsignal;
    private IOPort[] _rootNodes;
    private IOPort[] _infoPorts;
    private IOPort _signalport;
    private IOPort _lambdaOut;
    private PNRDInfo[] _rdnew;
    private double[][] _taps;
    private int _numberofblocks;
    private int _rbudget;
    private int _maxdepth;
    private double _epsilon = 0.01;
    private int _error = 10;
}









