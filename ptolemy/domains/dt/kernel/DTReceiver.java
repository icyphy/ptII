/* Discrete Time (DT) domain receiver.

 Copyright (c) 1997-2000 The Regents of the University of California.
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

@ProposedRating Red (chf@eecs.berkeley.edu)
@AcceptedRating Red (chf@eecs.berkeley.edu)
*/

package ptolemy.domains.dt.kernel;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.data.Token;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.actor.util.*;
import ptolemy.actor.*;
import ptolemy.domains.sdf.lib.Delay;

import java.util.NoSuchElementException;
import java.util.Enumeration;
import java.util.List;
import java.util.ListIterator;
import java.util.Hashtable;
import java.util.Iterator;
import javax.swing.*;

//////////////////////////////////////////////////////////////////////////
//// DTReceiver
/**
A first-in, first-out (FIFO) queue receiver with variable capacity and
optional history. Tokens are put into the receiver with the put() method,
and removed from the receiver with the get() method. The token removed is
the oldest one in the receiver. By default, the capacity is unbounded, but
it can be set to any nonnegative size. If the history capacity is greater
than zero (or infinite, indicated by a capacity of INFINITE_CAPACITY),
then tokens removed from the receiver are stored in a history queue rather
than simply removed. By default, the history capacity is zero.

@author C. Fong
@version $Id$
*/
public class DTReceiver extends SDFReceiver implements Receiver {

    /** Construct an empty receiver with no container.
     */
    public DTReceiver() {
        super();
        _init();
    }

    /** Construct an empty receiver with no container and given size.
     *  @param size The size of the buffer for the receiver.
     */
    public DTReceiver(int size) {
        super(size);
        _init();
    }

    /** Construct an empty receiver with the specified container.
     *  @param container The container of the receiver.
     */
    public DTReceiver(IOPort container) {
        super(container);
        _init();
    }

    /** Construct an empty receiver with the specified container and size.
     *  @param container The container of the receiver.
     *  @param size  The size of the buffer for the receiver.
     */
    public DTReceiver(IOPort container, int size) {
        super(container,size);
        _init();
    }
    

    /** Remove the first token (the oldest one) from the receiver and
     *  return it. If there is no token in the receiver, throw an
     *  exception.  Increment the time of the local director by
     *  the value period/(rate * repeats) where:
     *  - period is the time needed by the director per iteration 
     *  - rate   is the rate of the port that holds this receiver
     *  - repeats is the firing count per iteration the actor 
     *            that holds this receiver
     *  @return The oldest token in the receiver.
     *  @exception IllegalActionException If current time cannot be modified.
     */
    public Token get() {

        Actor actor = (Actor) super.getContainer().getContainer();
        IOPort currentPort = (IOPort) super.getContainer();
        DTDirector director = (DTDirector) ((Actor) actor).getDirector();

        // FIXME: may need to consider different cases for TypedComposositeActor ports
        director.setCurrentTime(_localGetTime);

        String sourceName = ((Nameable) to).getName();
        String destinationName = ((Nameable) from).getName();
        debug.print("   == get call by "+sourceName+" from "+destinationName+" time:"+_localGetTime);
        if (currentPort.getWidth()>1) {
            debug.println(" _multiport_");
        } else debug.println(" ");
        
        
        // FIXME: timing does not work for DT inside DT
        _localGetTime = _localGetTime + _deltaT;
        return super.get();
    }

   
    /** Remove the first tokens (the oldest ones) from the receiver and
     *  fill the array with them.
     *  If there are not enough tokens in the receiver, throw an exception
     *  and remove none of the tokens from the receiver.
     *  @exception NoTokenException If there is no token in the receiver.
     */
    public void getArray(Token t[]) {
        super.getArray(t);

        Actor actor = (Actor) super.getContainer().getContainer();
        DTDirector director = (DTDirector) (actor.getDirector());
        
        _localGetTime = _localGetTime + t.length * _deltaT;
    }

   
    public void initialPut(MatrixToken outputsArray) {
    }
    
    
    public void put(Token token) {

    	Actor actor = (Actor) super.getContainer().getContainer();
        IOPort currentPort = (IOPort) super.getContainer();
        DTDirector director = (DTDirector) (actor.getDirector());
        String fromName = ((Nameable)from).getName();
        String toName = ((Nameable)to).getName();

        // FIXME: may need to consider different cases for TypedComposositeActor ports
        //director.setCurrentTime(_localPutTime);
        
        
        debug.print("   --put token from "+fromName+" to "+toName+" with divider "+_periodDivider+" // time is:"+_localPutTime);
        if (fromPort == null) debug.prompt("null fromPort");     
        if (fromPort.getWidth()>1) {
            debug.println(" _multiport_");
        } else debug.println(" ");
        _localPutTime = _localPutTime + _deltaT;
        super.put(token);        
    }
    
    
    

    /** Put an array of tokens in the receiver.
     *  If the receiver has insufficient room, throw an
     *  exception, and add none of the tokens to the receiver.
     *  @param token The token to be put to the receiver.
     */
    public void putArray(Token token[]) {
        super.putArray(token);

        Actor actor = (Actor) super.getContainer().getContainer();
        DTDirector director = (DTDirector) (actor.getDirector());
        director.setCurrentTime(_localPutTime);
        _localPutTime = _localPutTime + token.length * _deltaT;
    }
    
    
    /** Determine the source and destination ports that use this
     *  receiver in their communications.  The source and destination
     *  ports are unique  
     *  @param dtDirector 
     */
    public void determineEnds(DTDirector dtDirector) {
        toPort = this.getContainer();
    	to = (Actor) toPort.getContainer();
    	fromPort = null;
        IOPort connectedPort = null;
        List listOfConnectedPorts = null;
        boolean isCompositeContainer = !((ComponentEntity) to).isAtomic();
    	
    	_localDirector = dtDirector;
    	
    	if (isCompositeContainer && (toPort.isOutput()) ) {
    	    listOfConnectedPorts = toPort.insidePortList();
    	} else {
    	    listOfConnectedPorts = toPort.connectedPortList();
    	}
    	    
    	Iterator portListIterator = listOfConnectedPorts.iterator();
    	
    	debug.println("determine ends "+this+" of "+toPort);
    	foundReceiver:
    	while (portListIterator.hasNext()) {
    	    connectedPort = (IOPort) portListIterator.next();
      	    debug.println("portListIterator listing "+connectedPort+" connected to "+toPort);
    	
    	    if (connectedPort.isOutput() == true) {
    	        debug.println("is output "+connectedPort);
    		    Receiver[][] remoteReceivers = connectedPort.getRemoteReceivers();
    		    
    		    for(int i=0;i<connectedPort.getWidth();i++) {
    		        debug.println("width of "+connectedPort+" :"+connectedPort.getWidth());
    			    for(int j=0;j<remoteReceivers[i].length;j++) {
    			        debug.println("length of receiver "+remoteReceivers[i].length);
    			        debug.println(remoteReceivers[i][j]);
    			        debug.println(this);
    			        //debug.prompt(remoteReceivers[i][j] +" "+ this);
    			        if (remoteReceivers[i][j] == this) {
                            from = (Actor) connectedPort.getContainer();
                            fromPort = connectedPort;
                            debug.println("fromPort "+connectedPort);
                            if (fromPort == null) debug.prompt("fromport is null");
                            break foundReceiver;
    			        } else {
    			            debug.println("not equal receivers");
    			        }
    			    }
    		    }
    	    } else if (connectedPort.getContainer() instanceof TypedCompositeActor) {
    	    // FIXME: should use at isAtomic()?
    	    // FIXME: code body 
    	        from = (Actor) connectedPort.getContainer();
    	        fromPort = connectedPort;
    	        if (fromPort == null) debug.prompt("fromport is null");
    	        break foundReceiver;
    	    } else if (connectedPort.isInput() == true) {
    	    // This case occurs when the destination port and the queried connected
    	    // port are both inputs. This case should be ignored
    	    } else {
    	    // should throw exception
    	        debug.prompt("internal bug: unexpected case");
    	    }
    	}
    	
    	if (fromPort == null) {
    	    debug.prompt(" container to actor " + ((ComponentEntity)to).getContainer());
    	    debug.println("to   "+toPort);
    	    debug.println("from "+fromPort);
    	}
    }
    
    
    public void calculateArcSamplingRate() throws IllegalActionException {
        int repeats;
        boolean isCompositeContainer = !((ComponentEntity) to).isAtomic();
    	
    	if ((from == null)/*||to instanceof TypedCompositeActor*/)  {
    	    debug.prompt("illegal from==null in calculateArcSamplingRate");
    	} else {
    	    
    	    Parameter param = (Parameter) fromPort.getAttribute("tokenProductionRate");
    	    if(param == null) {
               outrate = 1;
            } else {
               outrate = ((IntToken)param.getToken()).intValue();
            }

            if ((isCompositeContainer) && (toPort.isOutput())) {
                inrate = 1;
            } else {
    		    param = (Parameter) toPort.getAttribute("tokenConsumptionRate");
    	        if(param == null) {
                    inrate = 1;
                } else {
                    inrate = ((IntToken)param.getToken()).intValue();
                    if (inrate==0) debug.prompt("alright found bad seed "+to);
                }
            }       		
        	
            repeats = _localDirector.getRepeats(to);
        	_periodDivider = repeats * inrate; 
        	
        	double period = _localDirector.getPeriod();
        	
        	_deltaT = period/_periodDivider;
        }

    }
    
    /** This function is to be used in hierarchical DT where an inside director
     *  may need to resync with an outside director.
     */ 
    public void resyncLocalTime(double time) {
      
    }
    
    
    private void _init() {
        from = null;
        to = null;
        _localTime = 0.0; 
        _localPutTime = 0.0;
        _localGetTime = 0.0; 
        _periodDivider = 0; 
        _deltaT = 0.0;
        debug = new DTDebug(false);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    
    private DTDirector _localDirector;
    private int _periodDivider;
    double _deltaT;
    private double _localTime;
    private double _localPutTime;
    private double _localGetTime;
    int inrate;
    int outrate;
    Actor from;
    Actor to;
    IOPort fromPort;
    IOPort toPort;
    private DTDebug debug;
}
