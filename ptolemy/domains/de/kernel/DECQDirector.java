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
     *  The default startTime, stopTime and stepSize are all zeros. 
     *  The director can be set to a CTSubSystem in the same workspace
     *  by calling
     *  the setDirector method of CTSubSystem.  The director,  when
     *  constructed, has a CTTopSortScheduler attached. If another
     *  scheduler is to be used, use setScheduler method.
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
     *  The default startTime, stopTime and stepSize are all zeros.
     *
     *  The director can be set to a CTSubSystem in the workspace by 
     *  calling
     *  the setDirector method of CTSubSystem.  The director,  when
     *  constructed, has a CTTopSortScheduler attached. If another
     *  scheduler is to be used, use setScheduler method.
     *
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this director.
     */
    public DECQDirector(Workspace workspace, String name) {
        super(workspace, name);
    }
    
    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////
    /** Override the default fire method so that only one actor fire.
        The firing actor would be the one obtained from the global queue.
        Global queue keeps track of the deeply contained actors.
       
     */


    public void fire() throws CloneNotSupportedException, IllegalActionException {
        CompositeActor container = ((CompositeActor)getContainer());
        if (container != null) {
            CQValue cqValue;
            try {
                cqValue = (CQValue)_cQueue.take();
                _currentActor = cqValue.actor;
                _currentDEReceiver = cqValue.deReceiver;
                _currentDEToken = cqValue.deToken;

            } catch (IllegalAccessException e) {
                // FIXME: do nothing. The queue is empty here...
            }
            _currentActor.fire();
        } else {
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
        }
        double currentTime = tag.timeStamp();
        if (currentTime > _stopTime) {
            return false;
        } else {
            return true;
        }
        

    }

    /** Return a new receiver of a type compatible with this director.
     *  In this base class, this returns an instance of Mailbox.
     *  @return A new Mailbox.
     */
    public Receiver newReceiver() {
        return new DECQReceiver(this);
    }


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

    public DEToken dequeueEvent() {
        // FIXME: debug stuff
        NamedObj b = (NamedObj) _currentActor;
        System.out.println("Dequeueing event: " + b.description(CLASSNAME | FULLNAME) + " at time: " + ((DETag)_currentDEToken.getTag()).timeStamp());
        // FIXME: debug stuff

        // actually already got dequeued, during the fire() method.
        return _currentDEToken;
    }

    public double startTime() {
        //FIXME: find the first event in calendar queue and find out the 
        // time stamp
        return 0.0;
    }

    public double stopTime() {
        return _stopTime;
    }

    public void setStopTime(double st) {
        _stopTime = st;
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private inner class                    ////
    
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
    


    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////
    
    //_cQueue: an instance of CalendarQueue is used for sorting.
    private CalendarQueue _cQueue = new CalendarQueue(new DECQComparator());

    // variables to keep track of the objects currently firing.
    private Actor _currentActor = null;
    private DECQReceiver _currentDEReceiver = null;
    private DEToken _currentDEToken = null;

    // _stopTime defines the stopping condition
    private double _stopTime = 0.0;
    
}







