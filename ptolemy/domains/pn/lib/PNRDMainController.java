/* Higher level main controller for the RD scheme of
Ramachandra-Vetterli

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
import ptolemy.kernel.event.*;
import ptolemy.actor.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import java.util.Enumeration;
import java.lang.Math;
import collections.LinkedList;
//import gui.DynamicGraphView;


//////////////////////////////////////////////////////////////////////////
//// PNRDMainController
/**
Higher level main controller for the RD scheme of Ramachandra-Vetterli

This main controller runs through the following cycles:
(1) request a segment of data (to the audio source).
(2) receive a segment of data (from the audio source).
(3) spawn a encoder controller to encode the data.
(4) get back an array of RDInfo from the encoder controller. Each RDInfo
    in the array corresponds to one block.  kill the encoder controller.
(6) spawn a decoder controller.
(7) wait for the decoder to finish. kill the decoder.
(8) goto (1)

@author Yuhong Xiong, Mudit Goel
@(#)PNRDMainController.java	1.19 09/13/98
*/
public class PNRDMainController extends AtomicActor {

    /** Constructor  Adds port
     * @exception NameDuplicationException is thrown if more than one port
     *  with the same name is added to the star
     */
    public PNRDMainController(CompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        _signalIn = new IOPort(this, "input", true, false);
        //FIXME; A hack fo rthe demo alone
        //_signalIn.ISINPUT = true;
	_signalRequest = new IOPort(this, "output", false, true);
        //_signalRequest = newOutPort(this, "output");
        //FIXME: A hack for the demo alone
        //_signalRequest.ISINPUT = true;

        _done = new IOPort(this, "done", true, false);
        _signalOut = new IOPort(this, "signalOut", false, true);
        _infoOut = new IOPort(this, "infoOut", false, true);
	_paramsegsize = new Parameter(this,"Segment_Size",new IntToken(1024));
	_parammaxblock = new Parameter(this,"Maximum_Number_Of_Blocks", 
		new IntToken(4));
	_paramnumblocks = new Parameter(this, "Different_Number_Of_Blocks",
		new IntToken(3));
	_paramrbudget = new Parameter(this, "Rate_Budget", new IntToken(1024));
	_parammaxdepth = new Parameter(this, "Maximum_Tree_Depth",
		new IntToken(3));
    }


    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    //FIXME: Correct this
    /** Initializes the actor. Should be called before execution
     */
    public void setInitState(int segmentSize, int maxNumBlocks,
            int diffNumBlocks, int rbudget, int maxDepth,
            double[] highpass, double[] lowpass)
            throws NameDuplicationException {
        _segmentSize = segmentSize;
        _maxNumBlocks = maxNumBlocks;
	_diffNumBlocks = diffNumBlocks;
        _rbudget = rbudget;
        _maxDepth = maxDepth;
        _highpass = highpass;
        _lowpass = lowpass;
    }

    //FIXME: Correct this
    public void setInitState(int segmentSize, int maxNumBlocks,
            int diffNumBlocks, int rbudget, int maxDepth)
            throws NameDuplicationException {
        _segmentSize = segmentSize;
        _maxNumBlocks = maxNumBlocks;
        _diffNumBlocks = diffNumBlocks;
        _rbudget = rbudget;
        _maxDepth = maxDepth;
        
        double[] lowpass = {-0.010597401785, 0.032883011667, 
			    0.030841381836, -0.187034811719, -0.027983769417, 
			    0.630880767930, 0.71484650553, 0.230377813309};
        double[] highpass = new double[lowpass.length];
        for (int i = 0; i < lowpass.length; i++) {
            highpass[i] = lowpass[lowpass.length-1-i];
            if (i%2 == 1) {
                highpass[i] = -highpass[i];
            }
            System.out.println("hp["+i + "] = " + highpass[i]);
        }
        _highpass = highpass;
        _lowpass = lowpass;
    }

    //FIXME:
    public void setParam(String name, double value) 
            throws IllegalActionException {
        if (name.equals("segmentSize")) {
            _segmentSize = (int) value;
        } else if (name.equals("maxNumBlocks")) {
            _maxNumBlocks = (int) value;
        } else if (name.equals("diffNumBlocks")) {
            _diffNumBlocks = (int) value;
        } else if (name.equals("rbudget")) {
            _rbudget = (int) value;
        } else if (name.equals("maxDepth")) {
            _maxDepth = (int) value;
        } else {
            throw new IllegalActionException("Unknown parameter: " + name);
        }
    }

    //public void initialize() {
    public boolean prefire() throws IllegalActionException {
	_infoIn = new IOPort[_diffNumBlocks];
        double[] lowpass = {-0.010597401785, 0.032883011667, 
			    0.030841381836, -0.187034811719, -0.027983769417, 
			    0.630880767930, 0.71484650553, 0.230377813309};
        double[] highpass = new double[lowpass.length];
        for (int i = 0; i < lowpass.length; i++) {
            highpass[i] = lowpass[lowpass.length-1-i];
            if (i%2 == 1) {
                highpass[i] = -highpass[i];
            }
            System.out.println("hp["+i + "] = " + highpass[i]);
        }
        _highpass = highpass;
        _lowpass = lowpass;

	TopologyChangeRequest request = new TopologyChangeRequest(this) {
	    public void constructEventQueue() {
		try {
                    for (int i = 0; i < _diffNumBlocks; i++) {
                        _infoIn[i] = new IOPort(PNRDMainController.this, 
				"infoIn" + i, true, false);
                    }
                } catch (NameDuplicationException e) {
                    System.err.println("NameDuplicationException: "+e.getMessage());
                } catch (IllegalActionException e) {
		    System.err.println("IllegalActionException: "+e.getMessage());
		}
		for (int i=0; i<_diffNumBlocks; i++) {
		    queuePortAddedEvent(PNRDMainController.this, _infoIn[i]);
		}
	    }
	};

	PNDirector director = (PNDirector)getDirector();
	// Queue the new mutation
	director.queueTopologyChangeRequest(request);

	//Set the param values
	_segmentSize = ((IntToken)_paramsegsize.getToken()).intValue() ;
	_maxNumBlocks = ((IntToken)_parammaxblock.getToken()).intValue();
	_diffNumBlocks = ((IntToken)_paramnumblocks.getToken()).intValue();
	_rbudget = ((IntToken)_paramrbudget.getToken()).intValue();
	_maxDepth = ((IntToken)_parammaxdepth.getToken()).intValue();
	return true;
    }

    /** Reads one Token from it's input port and writes this token to
     *  it's output ports. Needs to read one token for every output
     *  port.
     */
    public void fire() throws IllegalActionException {
	while (true){
	    //UNlink all ports connected to the inner galaxies
	    makeInitUnlink();
	    // request data to audio source.
	    IntToken request = new IntToken(_segmentSize);
	    //System.out.println("Writing to source");
	    _signalRequest.broadcast(request);
	    //System.out.println("written to source");
	    
	    // get data from audio source
	    DoubleMatrixToken signal = null;
	    signal = (DoubleMatrixToken)_signalIn.get(0);
	    //System.out.println("Read in MAin");
	    
	    if (signal.getColumnCount() != _segmentSize) {   // run out of data
		System.out.println("breaking");
		break;
	    }
	    
	    CompositeActor myContainer = (CompositeActor)getContainer();
	    
	    //Create the encoding galaxies
	    _encodingGal = new CompositeActor[_diffNumBlocks];
	    createEncoders();
	    //writeTo(_signalOut, signal);
	    _signalOut.broadcast(signal);
	    System.out.println("writitn after whatedver");
	    
	    // select the best block size.
	    ObjectToken infoArrayToken = null;
	    double distortion = 10e10;
	    int bestNumBlocks = 0;		// will be removed later
	    int numblocks = _maxNumBlocks;
	    for (int j = 0; j < _diffNumBlocks; j++) {
		ObjectToken infoToken = null;
		//relations = _infoIn[j].linkedRelations();
		//while (relations.hasMoreElements()) {
		//IORelation relation = (IORelation)relations.nextElement();
		//infoToken = (ObjectToken)(readFrom(_infoIn[j], relation))[0];
		infoToken = (ObjectToken)_infoIn[j].get(0);
		//}
		
		killGalaxy(_encodingGal[j]);
		_encodingGal[j] = null;
		
		PNRDInfo[] currInfos = (PNRDInfo[])infoToken.getValue();
		if (currInfos.length != numblocks) {
		    // throw new InvalidStateException();
		    System.out.println("ERROR: PNRDMainController: " +
			    "number of blocks inconsistent. expected: "+
			    numblocks+" got "+currInfos.length
			    +"diffNumBlocks" + _diffNumBlocks);
		}
		numblocks /= 2;
		
		// get total distortion for current encoder
		double currDist = currInfos[0].distortion;
		for (int k = 1; k < currInfos.length; k++) {
		    currDist += currInfos[k].distortion;
		}
		
		if (currDist < distortion) {
		    infoArrayToken = infoToken;
		    distortion = currDist;
		    bestNumBlocks = currInfos.length;
		}
	    }
	    
	    // create decoder galaxy.
	    
	    createDecoders(bestNumBlocks);
	    System.out.println("Mutated and waitng t wr ");
	    //writeTo(_infoOut, infoArrayToken);
	    _infoOut.broadcast(infoArrayToken);
	    System.out.println("written ");
	    //relations = _done.linkedRelations();
	    //while (relations.hasMoreElements()) {
	    //IORelation relation = (IORelation)relations.nextElement();
	    //IntToken doneToken = (IntToken)(readFrom(_done, relation))[0];
	    IntToken doneToken = (IntToken)_done.get(0);
	    System.out.println("read done from decoder ");
	    //}
	    
	    
	    // kill decoder.
	    killGalaxy(_decoderGal);
	    _decoderGal = null;
	}
    }
    
    public boolean postfire() {
	return false;
    }
    
    private void makeInitUnlink() {
	TopologyChangeRequest request = new TopologyChangeRequest(this) {
	    public void constructEventQueue() {
		//Mutation m = new Mutation() {
		//public void perform() {
                _signalOut.unlinkAll();
                _done.unlinkAll();
                _infoOut.unlinkAll();
                for (int j=0; j<_infoIn.length; j++) {
                    _infoIn[j].unlinkAll();
                }
		Enumeration relations = _signalOut.linkedRelations();
		while (relations.hasMoreElements()) {
		    IORelation rel = (IORelation)relations.nextElement();
		    queuePortUnlinkedEvent(rel, _signalOut);
		}
		relations = _done.linkedRelations();
		while (relations.hasMoreElements()) {
		    IORelation rel = (IORelation)relations.nextElement();
		    queuePortUnlinkedEvent(rel, _done);
		}
		relations = _infoOut.linkedRelations();
		while (relations.hasMoreElements()) {
		    IORelation rel = (IORelation)relations.nextElement();
		    queuePortUnlinkedEvent(rel, _infoOut);
		}
		for (int j=0; j<_infoIn.length; j++) {
		    relations = _infoIn[j].linkedRelations();
		    while (relations.hasMoreElements()) {
			IORelation rel = (IORelation)relations.nextElement();
			queuePortUnlinkedEvent(rel, _infoIn[j]);
		    }
		}
	    }
	    
	};
        PNDirector director = (PNDirector)getDirector();
        // Queue the new mutation
	director.queueTopologyChangeRequest(request);
    }


    private void createEncoders() {
        // create encoder galaxies.
        PNDirector director = (PNDirector)PNRDMainController.this.getDirector();   
        TopologyChangeRequest request = new TopologyChangeRequest(this) {
	    public void constructEventQueue() {
		IOPort[] galports = new IOPort[_diffNumBlocks];
		IOPort[] infogalports = new IOPort[_diffNumBlocks];
		PNRDController[] encoders = new PNRDController[_diffNumBlocks];
		int numblocks = _maxNumBlocks;
		CompositeActor myContainer = 
		    (CompositeActor)PNRDMainController.this.getContainer();
		Relation rel1 = null;
		Relation rel2 = null;
		//public void perform() {
		try {
		    for (int j = 0; j < _diffNumBlocks; j++) {
			_encodingGal[j] = new CompositeActor(myContainer,"enc"+
				"odingGalaxy"+j);
			//galports[j] = new TransparentIOPort(_encodingGal[j], "encodingGalaxyPort"+j, true, true);
			galports[j] = (IOPort)_encodingGal[j].newPort("enco"+
				"dingGalaxyPort"+j);
                        //galports[j].ISINPUT = true;
                        rel1 = myContainer.connect(galports[j], _signalOut);
                        infogalports[j] = (IOPort)_encodingGal[j].newPort("e"+
				"ncodingGalaxyInfoPort"+j);
                        //infogalports[j].ISINPUT = false;
                        rel2 = myContainer.connect(_infoIn[j],infogalports[j]);

                        //This is inside the galaxy and hence listeners are NOT updated
                        encoders[j] = new PNRDController(_encodingGal[j], "encoder"+j);
                        encoders[j].setInitState(numblocks, _rbudget, 
				_maxDepth, _highpass, _lowpass);
                        numblocks /= 2;
                        IOPort portin = (IOPort)encoders[j].getPort("signal"+
				"port");
                        _encodingGal[j].connect(portin, galports[j]);
                        //myContainer.connect(galports[j], _signalOut);
                        IOPort portout=(IOPort)encoders[j].getPort("infoOut");
                        _encodingGal[j].connect(infogalports[j], portout);
                        // myContainer.connect(_infoIn[j], infogalports[j]);
                    }
                }  catch (IllegalActionException e) {
                    //This should never be thrown
                    System.err.println("Exception: " + e.toString());
                } catch (NameDuplicationException e) {
                    //This should never be thrown
                    System.err.println("Exception: " + e.toString());
                }
		
		//public void update(MutationListener listener) {
		for (int j = 0; j < _diffNumBlocks; j++) {
		    //listener.addEntity(myContainer, _encodingGal[j]);
		    queueEntityAddedEvent(myContainer, _encodingGal[j]);
		    // listener.addPort(_encodingGal[j], galports[j]);
		    // listener.addPort(_encodingGal[j], infogalports[j]);
		    queueRelationAddedEvent(myContainer, (ComponentRelation)rel1);
		    // Fool the graph viewer into thinking this
		    // is acyclic...
		    //galports[j].ISINPUT = true;
		    //_signalOut.ISINPUT = false;
		    queuePortLinkedEvent(rel1, galports[j]);
		    queuePortLinkedEvent(rel1, _signalOut);
		    
		    //_infoIn[j].ISINPUT = false;
		    //infogalports[j].ISINPUT = true;
		    
		    queueRelationAddedEvent(myContainer, (ComponentRelation)rel2);
		    queuePortLinkedEvent(rel2, _infoIn[j]);
		    queuePortLinkedEvent(rel2, infogalports[j]);
		    
		    //listener.addEntity(mycontainer, );
		    //listener.done();
		}
		//listener.done();
	    }
        };
	//PNDirector director = (PNDirector)getDirector();
	// Queue the new mutation
	director.queueTopologyChangeRequest(request);
        //director.queueMutation(m);
        //director.processPendingMutations();
        //director.startNewActors();
        //((PNDirector)getDirector()).setMutate(true);
    }

    private void createDecoders(final int bestNumBlocks) {
        PNDirector director = (PNDirector)PNRDMainController.this.getDirector();
	TopologyChangeRequest request = new TopologyChangeRequest(this) {
            public void constructEventQueue() {
		//Mutation m = new Mutation() {
		CompositeActor myContainer = (CompositeActor)PNRDMainController.this.getContainer();
		Relation rel1 = null;
		Relation rel2 = null;
		IOPort decodport = null;
		IOPort doneport = null;
		
		//public void perform() {
		try {
		    _decoderGal = new CompositeActor(myContainer, 
			    "DecodingGalaxy");
		    decodport = (IOPort)_decoderGal.newPort("decodport");
		    //decodport.ISINPUT = true;
		    doneport = (IOPort)_decoderGal.newPort("doneport");
		    //doneport.ISINPUT = false;
		    rel1 = myContainer.connect(decodport, _infoOut);
		    rel2 = myContainer.connect(_done, doneport);
		    
		    //Creating Synthesis filters
		    double[] synHighpass = new double[_highpass.length];
		    for (int j = 0; j < _highpass.length; j++) {
			synHighpass[j] = _highpass[_highpass.length-1-j];
		    }
		    double[] synLowpass = new double[_lowpass.length];
		    for (int j = 0; j < _lowpass.length; j++) {
			synLowpass[j] = _lowpass[_lowpass.length-1-j];
		    }
		    
		    //INternals of a galay. Need not be reported to the listeners
		    PNRDDecoder decoder = new PNRDDecoder(_decoderGal, 
			    "decoder");                
		    System.out.println("bestNumBlocks "+bestNumBlocks);
		    decoder.setInitState(synHighpass, synLowpass, bestNumBlocks);
		    IOPort portin = (IOPort)decoder.getPort("infoin");
		    _decoderGal.connect(portin, decodport);
		    
		    IOPort portout = (IOPort)decoder.getPort("doneout");
		    Enumeration temps = portout.linkedRelations();
		    if (temps.hasMoreElements()) {
			doneport.link((IORelation)temps.nextElement());
		    } else {
			_decoderGal.connect(doneport, portout);
		    }
		} catch (NameDuplicationException e) {
		    System.err.println("Exception: " + e.toString());
		    //This should never be thrown
		}  catch (IllegalActionException e) {
		    //This should never be thrown
		    System.err.println("Exception: " + e.toString());
		}
		//}
		//public void update(MutationListener listener) {
                queueEntityAddedEvent(myContainer, _decoderGal);
                // listener.addPort(_decoderGal, decodport);
                // listener.addPort(_decoderGal, doneport);
 
                // Fool the graph viewer into thinking this
                // is acyclic...
                //decodport.ISINPUT = true;
                //_infoOut.ISINPUT = false;
                queueRelationAddedEvent(myContainer, (ComponentRelation)rel1);
                queuePortLinkedEvent(rel1, decodport);
                queuePortLinkedEvent(rel1, _infoOut);

                //doneport.ISINPUT = true;
                //_done.ISINPUT = false;
                queueRelationAddedEvent(myContainer, (ComponentRelation)rel2);
                queuePortLinkedEvent(rel2, _done);
                queuePortLinkedEvent(rel2, doneport);
                //listener.done();
            }
        };
	// Queue the new mutation
	director.queueTopologyChangeRequest(request);
        //System.out.println("Mutating ");
        //((PNDirector)getDirector()).setMutate(true);
    }



    //Kill the encoding galaxies and remove them from the container
    private void killGalaxy(final CompositeActor galaxy) {
        PNDirector director =(PNDirector)PNRDMainController.this.getDirector();
	TopologyChangeRequest request = new TopologyChangeRequest(this) {
            public void constructEventQueue() {
		//Mutation m = new Mutation() {
		CompositeActor myContainer = 
		        (CompositeActor)PNRDMainController.this.getContainer();
		LinkedList rel = new LinkedList();
		LinkedList po = new LinkedList();
		Enumeration ports = galaxy.getPorts();
		LinkedList allrels = new LinkedList();
		
		//public void perform() {
		// kill encoder galaxy
                while (ports.hasMoreElements()) {
                    Port port = (Port)ports.nextElement();
                    Enumeration relations = port.linkedRelations();
                    while (relations.hasMoreElements()) {
                        Relation r = (Relation)relations.nextElement();
                        allrels.insertFirst(r);
                        Enumeration temports = r.linkedPorts();
                        while (temports.hasMoreElements()) {
                            Port p = (Port)temports.nextElement();
                            po.insertFirst(p);
                            rel.insertFirst(r);
                        }
                    }
                }
                try {
                    try {
                        galaxy.wrapup();
                    } catch (InvalidStateException e) {
                        System.err.println("InvalidStateException: " + e.toString());
                    }
                    //myContainer.removeEntity(galaxy);
		    galaxy.setContainer(null);
                }  catch (IllegalActionException e) {
                    //This should never be thrown
                    System.err.println("Exception: " + e.toString());
                } catch (NameDuplicationException e) {
                    //This should never be thrown
                    System.err.println("Exception: " + e.toString());
                }
		
		//public void update(MutationListener listener) {
                //Delete the relations
                Enumeration enumrel = rel.elements();
                Enumeration enumpo  = po.elements();
                while (enumpo.hasMoreElements()) {
                    Relation r = (Relation)enumrel.nextElement();
                    Port p = (Port)enumpo.nextElement();
		    queuePortUnlinkedEvent(r, p);
                }
                enumrel = allrels.elements();
                while (enumrel.hasMoreElements()) {
                    Relation r = (Relation)enumrel.nextElement();
		    queueRelationRemovedEvent(myContainer, (ComponentRelation)r);
                }
                rel = null;
                po = null;
                allrels = null;
                queueEntityRemovedEvent(myContainer, galaxy);
                //listener.done();
            }
        };
	director.queueTopologyChangeRequest(request);
    }
        
    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    private Parameter _paramsegsize;
    private Parameter _parammaxblock;
    private Parameter _paramnumblocks;
    private Parameter _paramrbudget;
    private Parameter _parammaxdepth;

    private int _segmentSize;
    private int _maxNumBlocks;
    private int _diffNumBlocks;
    private int _rbudget;
    private int _maxDepth;
    private double[] _highpass;
    private double[] _lowpass;

    private IOPort _signalIn;
    private IOPort[] _infoIn;      // _infoIn.length = _blocks.length
    private IOPort _done;

    private IOPort _signalOut;
    private IOPort _signalRequest;
    private IOPort _infoOut;

    private CompositeActor[] _encodingGal;
    private CompositeActor _decoderGal;
}






