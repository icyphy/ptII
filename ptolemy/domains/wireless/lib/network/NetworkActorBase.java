/* An actor that provides the common functions to all wireless network models.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (pjb2e@eecs.berkeley.edu)
*/

package ptolemy.domains.wireless.lib.network;

import java.util.HashSet;
import java.util.Iterator;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// NetWorkActorBase

/** 
This is a base class designed for the Network actors. 

Currently, it mainly contains several methods for dealing with timers 
that are widely used in the OMNET c++ classes.


@author Yang Zhao, Charlie Zhong
@version $Id$
*/

public class NetworkActorBase extends TypedAtomicActor {

    /** Construct an actor with the specified name and container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the empty string.
     *  This constructor write-synchronizes on the workspace.
     *  @param container The container.
     *  @param name The name of the actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public NetworkActorBase(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }    
    
    /** Initialize the <i>_timersSet<i> variable.
     *  @exception IllegalActionException If thrown by the base class.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _timersSet = new HashSet();
    }

    // messages from higher layers
    protected static final String[] PCRequestMsgFields = {"kind", "fromMACAddr",
            "toMACAddr", "range", "angle", "num_nb", "xpos","ypos", "Length"};

    protected static final String[] PCResponseMsgFields = {"kind", "fromMACAddr",
            "toMACAddr","xpos","ypos","range","Length"};

    protected static final String[] StartRspMsgFields = {"kind","range"};

    protected static final String[] cNetwInterestMessageFields = {"kind","cost",
            "hop_distance","fromMACAddr","toMACAddr","hopcount","arrivalTime", "Length"};

    protected static final String[] cNetwDataMessageFields = {"kind","fromMACAddr",
            "toMACAddr","hopcount","arrivalTime","payload","Length"};



    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////        
    /** Check whether this has a <i>id<i> attribute. If yes, return
     *  the value of it; if no, return the default value 0.
     *  @return return the value of the <i>id<i> attribute of 0 if no
     *  such an attribute.
     *  
     * FIXME: why use 0 as the default id? some node may use 0 as its id...
     * FIXME: when this method is used?
     */
    protected int getID() {
        int id = 0;
        try {
            Attribute idAttribute = getAttribute("id");
            if (idAttribute instanceof Variable) {
                Variable idVariable = (Variable)idAttribute;
                id = ((IntToken)(idVariable.getToken())).intValue();
            }
        } catch (IllegalActionException ex) {
            // ignore, use default id 0
        }
        return id;
    }

    /** Construct a timer object with the specified <i>kink<i> and 
     *  <i>experationTime<i> and add the timer to the timers set.
     *  @return return the created timer to the caller method
     *  (make it easy for it to cancel the timer).
     *  @exception IllegalActionException If thrown by 
     *  getDirector().fireAt().
     */
    protected Timer setTimer(int kind, double expirationTime) 
            throws IllegalActionException {

	    Timer timer=new Timer();
	    timer.kind=kind;	
	    timer.expirationTime=expirationTime;
	    // put all timers of this object into a queue
	    _timersSet.add(timer);
	    getDirector().fireAt(this, expirationTime);
	    return timer;
    }
  
    /** Remove the timer that matches with the <i>timerToCancel<i> argument
     *  from the timers set. If no match is found, do nothing.
     */ 
    protected void cancelTimer(Timer timerToCancel)
            throws IllegalActionException {
        Iterator timers = _timersSet.iterator();
	// iterate through the queue to find the timer to be canceled
        while (timers.hasNext()) {
            Timer timer = (Timer) timers.next();
	        if (timer==timerToCancel){
	            _timersSet.remove(timer);
                break;
            }
	    }
    }

    /** Get the timer with experation time that matches the current time.
     *  Remove the timer from the timers set and return the <i>kind<i> 
     *  parameter of the timer to the caller method. If there are multiple
     *  timers with experation time matching the current time, return the 
     *  first one from the iterator list. 
     *  @return return the i>kind<i> parameter of the timeout timer.
     *  @exception IllegalActionException If thrown by 
     *  getDirector().getCurrentTime().
     */ 
    protected int whoTimeout()
            throws IllegalActionException {
	// find the 1st timer expired
        Iterator timers = _timersSet.iterator();
        while (timers.hasNext()) {
            Timer timer = (Timer) timers.next();
	        if (timer.expirationTime==getDirector().getCurrentTime())
		    {
		        // remove it from the set no matter that
		        // it will be processed or ignored 
		        timers.remove();
		        return timer.kind;
		    }
	    }
	    return -1;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables                 ////
    // The set for the timers to be processed when they are expried.
    protected HashSet _timersSet;
    
    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    protected class Timer {
  
	    public int kind;
        public double expirationTime;
    }

}
