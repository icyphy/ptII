/* This is the main decoder for the RD scheme of Ramachandra-Vetterli

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
import ptolemy.kernel.event.*;
import ptolemy.actor.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import java.util.Enumeration;
import java.lang.Math;

//////////////////////////////////////////////////////////////////////////
//// PNRDDecoder
/** 
@author Mudit Goel
@version @(#)PNRDDecoder.java	1.18 09/13/98
*/
public class PNRDDecoder extends AtomicActor {
    
    /** Constructor  Adds port   
     * @exception NameDuplicationException is thrown if more than one port 
     *  with the same name is added to the star
     */
    public PNRDDecoder(CompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _infoport = new IOPort(this, "infoin", true, false);
        _donein = new IOPort(this, "donein", true, false);
        _doneout = new IOPort(this, "doneout", false, true);
    }
    
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Initialize the actor. Should be called before execution
     * @param prime is the prime for this sieve
     */	
    public void setInitState(double[] highpass, double[] lowpass, 
	    int numberofblocks) throws IllegalActionException, 
		NameDuplicationException {

        _taps = new double[2][];
        _numberofblocks = numberofblocks;
        for (int i=0; i<_numberofblocks; i++) {
	    //FIXME: Make it a mutation and put it in prefire
            new IOPort(this, "infoout"+i, false, true);
        }
	
        _taps[0] = highpass;
        _taps[1] = lowpass;
    }
    
    /** Reads one Token from it's input port and writes this token to 
     *  it's output ports. Needs to read one token for every output
     *  port. 
     */
    public void fire() throws IllegalActionException {
        
	TopologyChangeRequest r = new TopologyChangeRequest(this) {
	    public void constructEventQueue() {
		try {
		    ObjectToken data = (ObjectToken)_infoport.get(0);
		    PNRDInfo[] infoarray = (PNRDInfo[])data.getValue();
		    PNAudioPacker packer = new PNAudioPacker(
			    (CompositeActor)getContainer(), "packer", 
			    _numberofblocks);
		    Parameter p=(Parameter)packer.getAttribute("AudioFileName");
		    p.setToken(new StringToken("/tmp/result.bin"));
		    //packer.setInitState("/tmp/result.bin");
		    IOPort outport = (IOPort)packer.getPort("done");
		    Enumeration temps = outport.linkedRelations();
		    if (temps.hasMoreElements()) {
			_donein.link((IORelation)temps.nextElement());
		    } else {
			((CompositeActor)getContainer()).connect(_donein, 
				outport);
		    }
		    //((CompositeActor)getContainer()).connect(_donein,outport);
		    //_donein.getQueue(outport).setCapacity(1);
		    
		    for (int j=0; j<_numberofblocks; j++) {
			//root case is special as no filter
			PNRDInfo info = infoarray[j];
			//PNOutPort outport = null;
			IOPort inport = null;
			//root case is special as no filter
			if (info.left==null) { //No split
			    //rootnode to identifier
			    PNRDIdentifier identifier = new PNRDIdentifier(
				    (CompositeActor)getContainer(), 
				    "identifier-"+j+"-0-0");
			    identifier.setInitState(0,0);
			    inport = (IOPort)identifier.getPort("input");
			    outport = (IOPort)PNRDDecoder.this.getPort("infoout"+j);
			    temps = outport.linkedRelations();
			    if (temps.hasMoreElements()) {
				inport.link((IORelation)temps.nextElement());
			    } else {
				((CompositeActor)getContainer()).connect(
					inport, outport);    
			    }
			    
			    double step;
			    //Computing the step size
			    if (info.quantizerId == 0) 
				step = Math.pow(2,16)/Math.pow(2,14);
			    else if (info.quantizerId == 1) 
				step = Math.pow(2,16)/Math.pow(2,10);
			    else if (info.quantizerId == 2) 
				step = Math.pow(2,16)/Math.pow(2,6);
			    else if (info.quantizerId == 3) 
				step = Math.pow(2,16)/Math.pow(2,2);
			    else 
				throw new IllegalActionException(
					PNRDDecoder.this, "wrong step size"+
					" and ID ="+info.quantizerId);
			    
			    //identifier to DAC
			    PNDAConverter dequant = new PNDAConverter(
				    (CompositeActor)getContainer(), 
				    "dequantizer-"+j+"-0-0");
			    dequant.setInitState(step);
			    outport = (IOPort)identifier.getPort("output");
			    inport = (IOPort)dequant.getPort("digital");
			    temps = outport.linkedRelations();
			    if (temps.hasMoreElements()) {
				inport.link((IORelation)temps.nextElement());
			    } else {
				((CompositeActor)getContainer()).connect(
					inport, outport);    
			    }
			    //inport.getQueue(outport).setCapacity(1);
			    
			    //DAC to Packer
			    outport = (IOPort)dequant.getPort("analog");
			    inport = (IOPort)packer.getPort("input"+j);
			    temps = outport.linkedRelations();
			    if (temps.hasMoreElements()) {
				inport.link((IORelation)temps.nextElement());
			    } else {
				((CompositeActor)getContainer()).connect(
					inport, outport);    
			    }
			} else { //The node split
			    PNAdder adder = new PNAdder(
				    (CompositeActor)getContainer(), 
				    "adder-"+j+"-0-0");
			    //System.out.println("Addercreated"+adder.getName());
			    IOPort out1 = createSubtree(j,1,0,info.left); 
			    IOPort out2 = createSubtree(j,1,1,info.right);
			    inport = (IOPort)adder.getPort("input-0");
			    temps = out1.linkedRelations();
			    if (temps.hasMoreElements()) {
				inport.link((IORelation)temps.nextElement());
			    } else {
				((CompositeActor)getContainer()).connect(
					inport, out1);    
			    }
			    //((CompositeActor)getContainer()).connect(inport, out1);
			    //inport.getQueue(out1).setCapacity(1);
			    inport = (IOPort)adder.getPort("input-1"); 
			    temps = out2.linkedRelations();
			    if (temps.hasMoreElements()) {
				inport.link((IORelation)temps.nextElement());
			    } else {
				((CompositeActor)getContainer()).connect(
					inport, out2);    
			    }
			    //((CompositeActor)getContainer()).connect(inport, out2);
			    //inport.getQueue(out2).setCapacity(1);
			    
			    outport = (IOPort)adder.getPort("output");
			    inport = (IOPort)packer.getPort("input"+j);
			    temps = outport.linkedRelations();
			    if (temps.hasMoreElements()) {
				inport.link((IORelation)temps.nextElement());
			    } else {
				((CompositeActor)getContainer()).connect(
					inport, outport);    
			    }
			}
			outport =(IOPort)PNRDDecoder.this.getPort("infoout"+j);
			ObjectToken infotok =new ObjectToken(infoarray[j]);
			//writeTo(outport, infotok);
			outport.broadcast(infotok);
		    }
		} catch (NameDuplicationException e) {
		    System.err.println("Exception: " + e.toString());
		    //This should never be thrown
		}  catch (IllegalActionException e) {
		    System.err.println("Exception: " + e.toString());
		    //This should never be thrown
		} 
	    }
	};
	
	//FIXME!!!
	((PNDirector)getDirector()).queueTopologyChangeRequest(r);
	
	IntToken done = null;
	done = (IntToken)_donein.get(0);
	//System.out.println("Decoder read done from packer ");
	//}
	_doneout.broadcast(done); 
    }
    
    public boolean postfire() throws IllegalActionException {
	return false;
    }
	
    
    public IOPort createSubtree(int blocknumber, int currentdepth, 
            int branch, PNRDInfo info) 
	 throws IllegalActionException, NameDuplicationException {
	     
	PNCirConvFilter filter = new PNCirConvFilter(
		(CompositeActor)getContainer(), 
		"revfilter-"+blocknumber+"-"+currentdepth+"-"+branch);
        if(branch%2 == 0) { //Upper branch
            filter.setInitState(_taps[0], 2, 1);
            //System.out.println(filter.getName()+" is a highpass");
        } else { //Lower branch
            filter.setInitState(_taps[1], 2, 1);
            //System.out.println(filter.getName()+" is a lowpass");
        }
        if (info.left == null) { //Doesn't split
            //rootnode to Identifier
            IOPort outport = (IOPort)getPort("infoout"+blocknumber);
            PNRDIdentifier identifier = new PNRDIdentifier(
		    (CompositeActor)getContainer(), 
		    "identifier-"+blocknumber+"-"+currentdepth+"-"+branch);
            identifier.setInitState(currentdepth, branch);
            IOPort inport = (IOPort)identifier.getPort("input");
            Enumeration temps = outport.linkedRelations();
            if (temps.hasMoreElements()) {
                inport.link((IORelation)temps.nextElement());
            } else {
                ((CompositeActor)getContainer()).connect(inport, outport);    
            }
            //((CompositeActor)getContainer()).connect(inport, outport);
            //inport.getQueue(outport).setCapacity(1);
	    
            double step;
            if (info.quantizerId == 0) 
                step = Math.pow(2,16)/Math.pow(2,14);
            else if (info.quantizerId == 1) 
                step = Math.pow(2,16)/Math.pow(2,10);
            else if (info.quantizerId == 2) 
                step = Math.pow(2,16)/Math.pow(2,6);
            else if (info.quantizerId == 3) 
                step = Math.pow(2,16)/Math.pow(2,2);
            else throw new IllegalActionException(this, "wrong step size and ID ="+info.quantizerId);
            //             if (info.quantizerId == 0) step = 1.0;
            //             else if (info.quantizerId == 1) step = 16.0;
            //             else if (info.quantizerId == 2) step = 256.0;
            //             else throw new IllegalActionException(this, "wrong step size");
            PNDAConverter dequant = new PNDAConverter(
		    (CompositeActor)getContainer(), 
		    "dequantizer-"+blocknumber+"-"+currentdepth+"-"+branch);
            dequant.setInitState(step);
            //Identifier to DAC
            outport = (IOPort)identifier.getPort("output");
            inport = (IOPort)dequant.getPort("digital");
            temps = outport.linkedRelations();
            if (temps.hasMoreElements()) {
                inport.link((IORelation)temps.nextElement());
            } else {
                ((CompositeActor)getContainer()).connect(inport, outport);    
            }
            //((CompositeActor)getContainer()).connect(inport, outport);
            //inport.getQueue(outport).setCapacity(1);            
	    
            //DAC to filter
            outport = (IOPort)dequant.getPort("analog");
            inport = (IOPort)filter.getPort("input");
            temps = outport.linkedRelations();
            if (temps.hasMoreElements()) {
                inport.link((IORelation)temps.nextElement());
            } else {
                ((CompositeActor)getContainer()).connect(inport, outport);    
            }
            //((CompositeActor)getContainer()).connect(inport, outport);
            //inport.getQueue(outport).setCapacity(1);
            outport = (IOPort)filter.getPort("output");
            return outport;
        } else {
            IOPort out1 = createSubtree(blocknumber, currentdepth+1, 
		    2*branch, info.left);
            IOPort out2 = createSubtree(blocknumber, currentdepth+1, 
		    2*branch+1, info.right);
            PNAdder adder = new PNAdder((CompositeActor)getContainer(), 
		    "adder-"+blocknumber+"-"+currentdepth+"-"+branch);
            //System.out.println("Adder created "+adder.getName());
            IOPort inport = (IOPort)adder.getPort("input-0");
            Enumeration temps = out1.linkedRelations();
            if (temps.hasMoreElements()) {
                inport.link((IORelation)temps.nextElement());
            } else {
                ((CompositeActor)getContainer()).connect(inport, out1);    
            }
            //((CompositeActor)getContainer()).connect(inport, out1);
            //inport.getQueue(out1).setCapacity(1);
            inport = (IOPort)adder.getPort("input-1");
            temps = out2.linkedRelations();
            if (temps.hasMoreElements()) {
                inport.link((IORelation)temps.nextElement());
            } else {
                ((CompositeActor)getContainer()).connect(inport, out2);    
            }
            //((CompositeActor)getContainer()).connect(inport, out2);
            //inport.getQueue(out2).setCapacity(1);
	    
            //Adder to filter
            inport = (IOPort)filter.getPort("input");
            IOPort outport = (IOPort)adder.getPort("output");
            temps = outport.linkedRelations();
            if (temps.hasMoreElements()) {
                inport.link((IORelation)temps.nextElement());
            } else {
                ((CompositeActor)getContainer()).connect(inport, outport);    
            }
            //((CompositeActor)getContainer()).connect(inport, outport);
            //inport.getQueue(outport).setCapacity(1); 
	    
            outport = (IOPort)filter.getPort("output");
            return outport;
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    private double[][] _taps;
    private IOPort _infoport;
    private IOPort _doneout;
    private IOPort _donein;
    private int _numberofblocks;
}






