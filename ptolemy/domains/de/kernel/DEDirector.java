/* An abstract base class for DE domain director.

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
import ptolemy.graph.*;
import collections.LinkedList;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// DEDirector
//
/** Abstract base class for DE domain director. 
 
 *
 *  @author Lukito Muliadi
 *  @version $Id$
 *  @see DEReceiver
 *  @see CalendarQueue
 */
// FIXME:
// The topological depth of the receivers are static and computed once
// in the initialization() method. This means that mutations are not
// currently supported.
public abstract class DEDirector extends Director {

    /** Construct a director with empty string as name in the
     *  default workspace.
     */
    public DEDirector() {
	super();
    }
  
    /** Construct a director with the specified name in the default
     *  workspace. If the name argument is null, then the name is set to the
     *  empty string. This director is added to the directory of the workspace,
     *  and the version of the workspace is incremented.
     *  @param name The name of this director.
     */
    public DEDirector(String name) {
	super(name);
    }
  
    /** Construct a director in the given workspace with the given name.
     *  If the workspace argument is null, use the default workspace.
     *  The director is added to the list of objects in the workspace.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  @param workspace Object for synchronization and version tracking.
     *  @param name The name of this director.
     */
    public DEDirector(Workspace workspace, String name) {
	super(workspace, name);
    }
  
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Put a "pure event" into the event queue with the specified delay and
     *  depth. The time stamp of the event is the current time plus the
     *  delay.  The depth is used to prioritize events that have equal
     *  time stamps.  A smaller depth corresponds to a higher priority.
     *  A "pure event" is one where no token is transfered.  The event
     *  is associated with a destination actor.  That actor will be fired
     *  when the time stamp of the event is the oldest in the system.
     *  Note that the actor may have no new data at its input ports
     *  when it is fired.
     *
     *  @param actor The destination actor.
     *  @param delay The delay, relative to current time.
     *  @param depth The depth.
     *  @exception IllegalActionException If the delay is negative.
     */
    public abstract void enqueueEvent(Actor actor, double delay, long depth)
	 throws IllegalActionException;
       
    /** Put a token into the event queue with the specified destination
     *  receiver, delay and depth. The time stamp of the token is the
     *  current time plus the delay.  The depth is used to prioritize
     *  events that have equal time stamps.  A smaller depth corresponds
     *  to a higher priority.
     *
     *  @param receiver The destination receiver.
     *  @param token The token destined for that receiver.
     *  @param delay The delay, relative to current time.
     *  @param depth The depth.
     *  @exception IllegalActionException If the delay is negative.
     */
    public abstract void enqueueEvent(DEReceiver receiver, 
	    Token token,
	    double delay, 
	    long depth) 
	 throws IllegalActionException;

    /** Fire the one actor identified by the prefire() method as ready to fire.
     *  If there are multiple simultaneous events destined to this actor,
     *  then they have all been dequeued from the global queue and put into
     *  the corresponding receivers.
     *  <p>
     *  NOTE: Currently, this means that there may be multiple simultaneous
     *  events in a given receiver.  Since many actors may be written in
     *  such a way that they do not expect this, this may change in the future.
     *  I.e., these events may be made visible over multiple firings rather
     *  than all at once.
     */
    public void fire()
	 throws CloneNotSupportedException, IllegalActionException {
      super.fire();
    }

    /** Return the current time of the simulation. Firing actors that need to
     *  know the current time (e.g. for calculating the time stamp of the
     *  delayed outputs) call this method.
     */
    public double getCurrentTime() {
	return _currentTime;
    }
    
    /** Return the time of the earliest event seen in the simulation.
     *  Before the simulation begins, this is java.lang.Double.MAX_VALUE.
     *  @return The start time of the simulation.
     */
  public double getStartTime() {
      return _startTime;
  }
    
    /** Return the stop time of the simulation, as set by setStopTime().
     *  @return The stop time of the simulation.
     */
    public double getStopTime() {
	return _stopTime;
    }

    /** Set current time to zero, calculate priorities for simultaneous
     *  events, and invoke the initialize() methods of all actors deeply
     *  contained by the container.  To be able to calculate the priorities,
     *  it is essential that the graph not have a delay-free loop.  If it
     *  does, then this can be corrected by inserting a DEDelay actor
     *  with a zero-valued delay.  This has the effect of breaking the
     *  loop for the purposes of calculating priorities, without introducing
     *  a time delay.
     *  <p>
     *  This method should be invoked once per execution, before any
     *  iteration. Actors may produce output data in their initialize()
     *  methods, or more commonly, they may schedule pure events.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @exception CloneNotSupportedException If the initialize() method of the
     *   container or one of the deeply contained actors throws it.
     *  @exception IllegalActionException If there is a delay-free loop, or
     *   if the initialize() method of the
     *   container or one of the deeply contained actors throws it.
     */
    public void initialize()
	 throws CloneNotSupportedException, IllegalActionException {
      super.initialize();
    }

    /** Return a new receiver of a type DEReceiver.
     *  @return A new DEReceiver.
     */
    public Receiver newReceiver() {
	return new DEReceiver();
    }
    
    /** Invoke the base class prefire() method, and if it returns true,
     *  dequeue the next event from the event queue, advance time to its
     *  time stamp, and mark its destination actor for firing.
     *  If there are multiple events on the queue with the same time
     *  stamp that are destined for the same actor, dequeue all of them,
     *  making them available in the input ports of the destination actor.
     *  If the time stamp is greater than the stop time, or there are no
     *  events on the event queue, then return false,
     *  which will have the effect of stopping the simulation.
     *
     *  @return True if there is an actor to fire.
     *  @exception CloneNotSupportedException If the base class throws it.
     *  @exception IllegalActionException If the base class throws it.
     *  @exception NameDuplicationException If the base class throws it.
     */
    // FIXME: This isn't quite right in that it may put multiple simultaneous
    // events into the same receiver.  Actors are unlikely to be written
    // in such a way as to look for these.  Perhaps such actors need to
    // be fired repeatedly?
    public boolean prefire()
	 throws CloneNotSupportedException, IllegalActionException,
	     NameDuplicationException {
      return super.prefire();
    }
    
    /** Set the stop time of the simulation.
     *  @param stopTime The new stop time.
     */
    public void setStopTime(double stopTime) {
	this._stopTime = stopTime;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    
    // Defines the stopping condition
    protected double _stopTime = 0.0;
    
    // The time of the earliest event seen in the current simulation.
    protected double _startTime = Double.MAX_VALUE;
    
    // The current time of the simulation.
    // Firing actors may get the current time by calling getCurrentTime()
    protected double _currentTime = 0.0;
}


