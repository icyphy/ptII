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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.Director;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// MACActorBase

/** 
This is a base class designed for the Network actors. 

Currently, it mainly contains several methods for dealing with timers 
that are widely used in the OMNET c++ classes.


@author Yang Zhao
@version $ $
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
        
    public int getID() {
        int id = 0;
        try {
            Parameter p = (Parameter)getAttribute("id", Parameter.class);
            id = ((IntToken)p.getToken()).intValue();
        } catch (IllegalActionException ex) {
            // ignore, use default id 0
        }
        return id;
    }

    // timer related methods

    // This function creates a Timer object, records the timer type and
    // its expiration time. Afterwards, this timer is added to the end
    // of a queue. The pointer to this timer is returned to the caller
    // function (make it easy for it to cancel the timer).
    public Timer setTimer(int kind, double expirationTime) 
	// kind corresponds to processing type
            throws IllegalActionException {

	// do director didn't do for the timers:
	Timer timer=new Timer();
	timer.kind=kind;	
	timer.expirationTime=expirationTime;
	// put all timers of this object into a queue
	timersQueue.add(timer);
	getDirector().fireAt(this, expirationTime);
	return timer;
    }
  
    // This function search in the queue for the pointer to the timer
    // to be cancled and if a match is found, the timer is removed from
    // the queue
    public void cancelTimer(Timer timerToCancel)
            throws IllegalActionException {
        Iterator timers =timersQueue.listIterator();
	// iterate through the queue to find the timer to be canceled
        while (timers.hasNext()) {
            Timer timer = (Timer) timers.next();
	    // if the address of timer object matches with
	    // that stored in the queue, remove the entry
	    if (timer==timerToCancel)
		timers.remove();
	}
    }

    // This function goes through the queue and find the 1st timer whose
    // expiration time is equal to the current time. The timer is removed
    // from the queue and its type is returned.
    public int whoTimeout()
            throws IllegalActionException {
	// find the 1st timer expired
        Iterator timers =timersQueue.listIterator();
        while (timers.hasNext()) {
            Timer timer = (Timer) timers.next();
	    if (timer.expirationTime==getDirector().getCurrentTime())
		{
		    // remove it from the queue no matter that
		    // it will be processed or ignored 
		    timers.remove();
		    return timer.kind;
		}
	}
	return -1;
    }

    public class Timer {
  
	public int kind;
        public double expirationTime;
    }
    
    protected List timersQueue=new LinkedList();

}
