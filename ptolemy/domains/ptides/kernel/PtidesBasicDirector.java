/* Basic Ptides director that uses DE and delivers correct
 * but not necessarily optimal execution.
 * 
@Copyright (c) 2008 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

package ptolemy.domains.ptides.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.util.Time;
import ptolemy.actor.util.TimedEvent;
import ptolemy.data.BooleanToken;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.domains.de.kernel.DEEventQueue;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

/** FIXME
 * 
 *  This director has a local notion time, decoupled from that of the
 *  enclosing director. The enclosing director's time
 *  represents physical time, whereas this time represents model
 *  time in the Ptides model.
 *
 *  @author Patricia Derler, Edward A. Lee, Ben Lickly, Isaac Liu, Slobodan Matic, Jia Zou
 *  @version $Id$
 *  @since Ptolemy II 7.1
 *  @Pt.ProposedRating Yellow (cxh)
 *  @Pt.AcceptedRating Red (cxh)
 *
 */
public class PtidesBasicDirector extends DEDirector {

    /** Construct a director with the specified container and name.
     *  @param container The container
     *  @param name The name
     *  @throws IllegalActionException If the superclass throws it.
     *  @throws NameDuplicationException If the superclass throws it.
     */
    public PtidesBasicDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        // TODO Auto-generated constructor stub
    }

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////
    
    /** Initialize the actors and request a refiring at the current
     *  time of the executive director. This overrides the base class to
     *  throw an exception if there is no executive director.
     *  @throws IllegalActionException If the superclass throws
     *   it or if there is no executive director.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _currentlyExecuting = null;
        _physicalTimeExecutionStarted = null;
        
        NamedObj container = getContainer();
        if (!(container instanceof Actor)) {
            throw new IllegalActionException(this, 
                    "No container, or container is not an Actor.");
        }
        Director executiveDirector = ((Actor)container).getExecutiveDirector();
        if (executiveDirector == null) {
            throw new IllegalActionException(this, 
                    "The PtidesBasicDirector can only be used within an enclosing director.");
        }
        executiveDirector.fireAtCurrentTime((Actor)container);
    }

    /** Return false if there are no more actors to be fired or the stop()
     *  method has been called. Otherwise, if the director is an embedded
     *  director and the local event queue is not empty, request a refiring
     *  at the current time of the enclosing director.
     *  FIXME: This assumes no sensors and actuators, and that
     *  execution time of all actors is zero.
     *  @return True If this director will be fired again.
     *  @exception IllegalActionException If stopWhenQueueIsEmpty parameter
     *   does not contain a valid token, or refiring can not be requested.
     */
    public boolean postfire() throws IllegalActionException {
        // Do not call super.postfire() because that requests a
        // refiring at the next event time on the event queue.
        
        boolean stop = ((BooleanToken) stopWhenQueueIsEmpty.getToken())
                .booleanValue();

        Boolean result = !_stopRequested;
        if (getModelTime().compareTo(getModelStopTime()) >= 0) {
            result = false;
        }
        DEEventQueue eventQueue = getEventQueue();
        if (eventQueue.isEmpty() && stop && _currentlyExecuting == null) {
            result = false;
        }
        if (result) {
            // initialize() checks that these aren't null.
            NamedObj container = getContainer();
            Director executiveDirector = ((Actor)container).getExecutiveDirector();
            // FIXME: Ptides with execution times and sensors will want to change
            // the time of the requested firing.
            executiveDirector.fireAtCurrentTime((Actor)container);
        }
        return result;
    }

    /** Override the base class to not set model time to that of the
     *  enclosing director.
     */
    public boolean prefire() throws IllegalActionException {
        // Do not invoke the superclass prefire() because that
        // sets model time to match the enclosing director's time.
        if (_debugging) {
            _debug("Prefiring: Current time is: " + getModelTime());
        }
        
        if (_currentlyExecuting != null) {
            // We are currently executing an actor
            Time remainingExecutionTime = _currentlyExecuting.timeStamp;
            Time finishTime = _physicalTimeExecutionStarted.add(remainingExecutionTime);
            if (!finishTime.equals(_getPhysicalTime())) {
                return false;
            }
            

        }
        
        DEEventQueue eventQueue = getEventQueue();
        if (eventQueue.isEmpty()) {
            if (_debugging) {
                _debug("Event queue is empty. prefire() returns false.");
            }
            return false;
        } else {
            setModelTime(eventQueue.get().timeStamp());
            return true;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                     protected methods                     ////

    /** Return false to get the superclass DE director to behave exactly
     *  as if it is executing at the top level.
     *  @return False.
     */
    protected boolean _isEmbedded() {
        return false;
    }
    
    /** Dequeue the events that have the smallest tag from the event queue.
     *  Return their destination actor. 
     *  This overrides the DEDirector's implementation in order to not
     *  return this actor when it has a non-zero execution time.
     *  Instead, it calls fireAt of the enclosing director, and returns null.
     *  
     *  @return The next actor to be fired, which can be null.
     *  @exception IllegalActionException If event queue is not ready, or
     *  an event is missed, or time is set backwards, or if the enclosing
     *  director does not respect the fireAt call.
     */
    protected Actor _getNextActorToFire() throws IllegalActionException {
        if (_currentlyExecuting != null) {
            // FIXME: Here, we want to implement a Ptides preemption policy.
            // For now, we do not allow preemption, and continue executing
            // the currently executing actor until it finishes.
            Actor result = (Actor) _currentlyExecuting.contents;
            
            _currentlyExecuting = null;
            _physicalTimeExecutionStarted = null;
            return result;
        }
            
        Actor actorToFire = super._getNextActorToFire();
        
        double executionTime = PtidesActorProperties.getExecutionTime(actorToFire);
        
        if (executionTime == 0.0) {
            return actorToFire;
        } else {
            Actor container = (Actor) getContainer();
            Director director = container.getExecutiveDirector();
            Time physicalTime = _getPhysicalTime();
            Time requestedTime = physicalTime.add(executionTime);
            Time result = director.fireAt(container, requestedTime);
            
            if (!result.equals(requestedTime)) {
                throw new IllegalActionException(actorToFire, director,
                        "Ptides director requires refiring at time "
                        + requestedTime
                        + ", but the enclosing director replied that it will refire at time "
                        + result);
            }
            
            TimedEvent event = new TimedEvent(new Time(this, executionTime), actorToFire);
            _currentlyExecuting = event;
            _physicalTimeExecutionStarted = physicalTime;
            
            return null;
        }
    }
    
    /** Return the model time of the enclosing director, which is our model
     *  of physical time.
     *  @return Physical time.
     */
    protected Time _getPhysicalTime() {
        Actor container = (Actor) getContainer();
        Director director = container.getExecutiveDirector();
        return director.getModelTime();
    }
    
    
    ///////////////////////////////////////////////////////////////////
    ////                     private variables                     ////
    
    /** The currently executing actor and its remaining execution time, 
     *  or null if there is none.
     */
    private TimedEvent _currentlyExecuting;
    
    /** The physical time at which the currently executing actor, if any,
     *  last resumed execution.
     */
    private Time _physicalTimeExecutionStarted;
    
}
