/* Real Time Operating System Director

 Copyright (c) 2001 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (eal@eecs.berkeley.edu)
*/

package ptolemy.domains.rtos.kernel;

import ptolemy.actor.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.gui.MessageHandler;
import ptolemy.gui.CancelException;

import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;


//////////////////////////////////////////////////////////////////////////
//// RTOSDirector
/**
@author Edward A. Lee
@version $Id$
*/

public class RTOSDirector extends DEDirector {

    public RTOSDirector() {
        super();
    }

    public RTOSDirector(Workspace workspace) {
        super(workspace);
    }

    public RTOSDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }


    public double getCurrentTime() {
        double elaps = (double)(System.currentTimeMillis() - _startingTime);
        return elaps/1000.0;
    }

    public void setCurrentTime(double newTime) throws IllegalActionException {
        //_currentTime = newTime;
    }

    public void initialize() throws IllegalActionException {
        _startingTime = System.currentTimeMillis();
        super.initialize();
    }

    public void fire() throws IllegalActionException {
        _stopRequested = false;
        Actor actorToFire = _dequeueEvents();
        if (actorToFire == null) {
            // There is nothing more to do.
            if (_debugging) _debug("No more events on the event queue.");
            _noMoreActorsToFire = true;
            return;
        }
        if (_debugging) {
            _debug("Found actor to fire: "
                    + ((NamedObj)actorToFire).getFullName());
        }
        // It is possible that the next event to be processed is on
        // an inside receiver of an output port of an opaque composite
        // actor containing this director.  In this case, we simply
        // return, giving the outside domain a chance to react to
        // event.
        if (actorToFire == getContainer()) {
            return;
        }
        if (((Nameable)actorToFire).getContainer() == null) {
            if (_debugging) _debug(
                    "Actor has no container. Disabling actor.");
            _disableActor(actorToFire);
        } else {
            if (!actorToFire.prefire()) {
                if (_debugging) _debug("Prefire returned false.");
            } else {
                actorToFire.fire();
                if (!actorToFire.postfire()) {
                    if (_debugging) _debug("Postfire returned false:",
                           ((Nameable)actorToFire).getName());
                    // Actor requests that it not be fired again.
                    _disableActor(actorToFire);
                }
            }
        }
        Thread.currentThread().yield();
    }

    public void fireAt(Actor actor, double time)
            throws IllegalActionException {
        long requestTime = (long)(time*1000.0) + _startingTime;
        long currentTime = System.currentTimeMillis();
        if (requestTime < currentTime) {
            throw new IllegalActionException((Nameable)actor,
                    "Requested fire time: " + time + " is earlier than" +
                    " the current time." + ((double)currentTime)/1000.0);
        }
        ActorTask actorTask = new ActorTask(actor);
        //FIXME: Should maintain a colletions of Timers and kill them
        //when stopFire() or wrapup is called.
        Timer timer = new Timer();
        try {
            timer.schedule(actorTask, requestTime - 
                    System.currentTimeMillis());
        } catch (IllegalArgumentException ex) {
            _debug("Missed the real time deadline. Still execute the task");
            actorTask.run();
        }
    }
        

    public Receiver newReceiver() {
        if(_debugging) _debug("Creating new DE receiver.");
	return new RTOSReceiver();
    }

    ////////////////////////////////////////////////////////////////////////
    ////                    protected methods                           ////


    protected void _enqueueEvent(Actor actor, double time)
            throws IllegalActionException {

        if (_eventQueue == null) return;
        int microstep = 0;

        int depth = _getDepth(actor);
        if(_debugging) _debug("enqueue a pure event: ",
                ((NamedObj)actor).getName(),
                "time = "+ time + " microstep = "+ microstep + " depth = "
                + depth);
        _eventQueue.put(new DEEvent(actor, time, microstep, depth));
    }
    
    protected void _enqueueEvent(DEReceiver receiver, Token token,
            double time) throws IllegalActionException {

        if (_eventQueue == null) return;
        int microstep = 0;
        Actor destination = (Actor)(receiver.getContainer()).getContainer();
        int depth = _getDepth(destination);
        if(_debugging) _debug("enqueue event: to",
                receiver.getContainer().getName()+ " ("+token.toString()+") ",
                "time = "+ time + " microstep = "+ microstep + " depth = "
                + depth);
        _eventQueue.put(new DEEvent(receiver, token, time, microstep, depth));
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         inner class                            ////
     
    private class ActorTask extends TimerTask {
        public ActorTask(Actor actor) {
            _actor = actor;
        }

        // iterate the actor for one iteration when the time is scheduled.
        public void run() {
            if (_actor != null) {
                try {
                _actor.iterate(1);
                } catch (IllegalActionException ex) {
                    throw new InvalidStateException((NamedObj)_actor,
                            ex.getMessage());
                }
            }
        }
        
        private Actor _actor;
    }


    ////////////////////////////////////////////////////////////////////////
    ////                    private variables                           ////
    
    // starting time as the system clock time
    long _startingTime;
}
