/* The DE Director that uses Calendar Queeu for scheduling

 Copyright (c) 1998 The Regents of the University of California.
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

@ProposedRating red (lmuliadi@eecs.berkeley.edu)
*/

package ptolemy.domains.de.kernel;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.actor.util.*;
import ptolemy.data.*;
import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// DECQDirector
/** 

@author Lukito Muliadi
@version $Id$
@see DEDirector
*/
public class DECQDirector extends Director {
    /** Construct a director with empty string as name in the
     *  default workspace.
     *
     */
    public DECQDirector() {
        super();
    }

    /** Construct a director with a name in the default
     *  workspace.  The director is added to the list of objects in
     *  the workspace.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  The default stopTime is zero. 
     *  
     *  @param name The name
     */	
    public DECQDirector(String name) {
        super(name);
    }
    
    /** Construct a director in the given workspace with the given name.
     *  If the workspace argument is null, use the default workspace.
     *  The director is added to the list of objects in the workspace.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  The default startTime is zero.
     *
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this director.
     */
    public DECQDirector(Workspace workspace, String name) {
        super(workspace, name);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /** Override the default fire method so that only one actor fire.
     *  The firing actor would be the one obtained from the global queue.
     *  <p>
     *  If there are multiple simultaneous events for this firing actor,
     *  then all events are dequeued from the global queue and put into 
     *  the corresponding receivers.
     *  <p>
     *  FIXME: What to do if there are simultaneous events for the same
     *  actor via the same receiver.
     */

    public void fire()
            throws CloneNotSupportedException, IllegalActionException {
        
        CompositeActor container = ((CompositeActor)getContainer());

        CQValue cqValue = null;
        DEToken token;
        DECQReceiver receiver;
        
        if (container != null) {
            
            _currentActor = null;
            FIFOQueue fifo = new FIFOQueue();
            double eventTime = 0.0;
            
            // Keep taking events out until there are no more simultaneous
            // events or until the queue is empty.
            while (true) {
                
                if (_cQueue.size() == 0) {
                    // FIXME: this check is not needed, just for debugging
                    if (_currentActor==null) {
                        System.out.println("Why invoke DECQDirector.fire()" +
                                           " when queue is empty ??!?!");
                    }
                    break;
                }
                
                try {
                    cqValue = (CQValue)_cQueue.take();
                } catch (IllegalAccessException e) {
                    //This shouldn't happen.
                    System.out.println("Bug in DECQDirector.fire()");
                }
                
                // At first iteration always accept the event.
                if (_currentActor == null) {
                    // only do this once.
                    _currentActor = cqValue.actor;
                    eventTime = ((DETag)cqValue.deToken.getTag()).timeStamp();
                }
                
                // check if it is at the same time stamp
                if (((DETag)cqValue.deToken.getTag()).timeStamp() != eventTime) {
                    fifo.put(cqValue);
                    break;
                }

                // check if it is for the same actor
                if (cqValue.actor == _currentActor) {
                    // FIXME: assuming it's always for different port.
                    cqValue.deReceiver.superPut(cqValue.deToken);
                    
                    // FIXME: start debug stuff
                    NamedObj b = (NamedObj) _currentActor;
                    System.out.println("Dequeueing event: " + 
                            b.description(CLASSNAME | FULLNAME) + 
                        " at time: " + 
                        ((DETag)cqValue.deToken.getTag()).timeStamp());
                    // FIXME: end debug stuff

                } else {
                    // put it into a FIFOQueue to be returned to queue later.
                    fifo.put(cqValue);
                }
            }             
            
            // Transfer back the events from the fifo queue into the calendar
            // queue.
            while (fifo.size() > 0) {
                CQValue cqval = (CQValue)fifo.take();
                _cQueue.put(cqval.deToken.getTag(),cqval);
            }
            
            _currentActor.fire();
        
        } else {
            // Error because the container is null.
            // FIXME: Is this needed ? Cuz, the ptolemy.actor.Director.java
            // doesn't do this.
            throw new IllegalActionException("No container. Invalid topology");
        }
           
    }

    /** Override the postfire method, so that it'll return false when the time
     *  reaches stopTime.
     *
     *  @return True if the execution can continue into the next iteration.
     *  @exception CloneNotSupportedException If the postfire() method of the
     *   container or one of the deeply contained actors throws it.
     *  @exception IllegalActionException If the postfire() method of the
     *   container or one of the deeply contained actors throws it.
     */
    public boolean postfire()
            throws CloneNotSupportedException, IllegalActionException {
        DETag tag = null;
        try {
            tag = (DETag)_cQueue.getNextKey();
        } catch (IllegalAccessException e) {
            // FIXME: can't happen ?
            System.out.println("Check DECQDirector.postfire() for a bug!");
        }
        double currentTime = tag.timeStamp();
        if (currentTime > _stopTime) {
            return false;
        } else {
            return true;
        }
    }
    
    /** Return a new receiver of a type DECQReceiver.
     *  
     *  @return A new DECQReceiver.
     */
    public Receiver newReceiver() {
        return new DECQReceiver(this);
    }

    /** Put the new event into the global event queue. The event consists 
     *  of the destination actor, the destination receiver, and the 
     *  transferred token.
     *
     *  @param a The destination actor.
     *  @param r The destination receiver.
     *  @param t The transferred token.
     *
     */
    public void enqueueEvent(Actor a, DECQReceiver r, DEToken t) {

        // FIXME: debug stuff
        NamedObj b = (NamedObj) a;
        System.out.println("Enqueuing event: " + b.description(CLASSNAME | FULLNAME) + " at time: " + ((DETag)t.getTag()).timeStamp());
        // FIXME: end debug stuff

        // FIXME: need to check if Actor == null ??
        if (a==null) {
            throw new IllegalArgumentException("DECQDirector, trying to enqueue null actor!");
        }
        CQValue newValue = new CQValue(a, r, t);
        DETag newTag = (DETag)(t.getTag());
        _cQueue.put(newTag, newValue);
    }

    /** Return the start time of the simulation. 
     *  FIXME: Right now it's only used to determine the axis range
     *  for the DEPlot star.
     *  FIXME: This can be obtained from the time stamp of the first token
     *  enqueued using the enqueueEvent() method.
     *  FIXME: Or this can also be set by the user.
     *
     *  @return The start time of the simulation.  
     */
    public double startTime() {
        //FIXME: find the first event in calendar queue and find out the 
        // time stamp
        return 0.0;
    }

    /** Return the stop time of the simulation.
     *  FIXME: Right now, it's only used to determine the axis range
     *  for the DEPlot star.
     *  This quantity is set by the user.
     *  
     *  @return The stop time of the simulation.
     */
    public double stopTime() {
        return _stopTime;
    }

    /** Set the stop time of the simulation.
     * 
     *  @param st The new stop time.
     */
    public void setStopTime(double st) {
        _stopTime = st;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private inner class               ////
    
    // private inner class CQValue: wrapper for the datas that want to be
    // stored in the queue.
    // FIXME: CQValue.. bad name ?
    private class CQValue {
        // constructor
        CQValue(Actor a, DECQReceiver r, DEToken t) {
            actor = a;
            deReceiver = r;
            deToken = t;
        }
        
        // public fields
        public Actor actor;
        public DECQReceiver deReceiver;
        public DEToken deToken;
    }
    


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    //_cQueue: an instance of CalendarQueue is used for sorting.
    private CalendarQueue _cQueue = new CalendarQueue(new DECQComparator());

    // variables to keep track of the objects currently firing.
    private Actor _currentActor = null;
    
    // _stopTime defines the stopping condition
    private double _stopTime = 0.0;
    
}
