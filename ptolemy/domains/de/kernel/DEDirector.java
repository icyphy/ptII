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
import ptolemy.data.expr.Parameter;

//////////////////////////////////////////////////////////////////////////
//// DEDirector
//
/** Abstract base class for DE domain director. In general, the methods
 *  provided in this base class are ones that do not depend on the
 *  implementation of the global event queue. This will enable different
 *  implementation to be compared in term of efficiency. The bottleneck
 *  in a typical DE simulator is in the mantainance of the global event
 *  queue. The 'best' current implementation is the calendar queue
 *  algorithm which gives us O(1) time in both enqueue and dequeue operation.
 *  The DECQDirector class, which derives from DEDirector, uses 
 *  this implementation.
 *  <p>
 *  Several of the methods provided in this base class have two versions.
 *  One that deal with relative time (with regards to the current time) and
 *  another that deal with absolute time. While it is theoretically equivalent
 *  to use one or the other, it is practically better to use the one with
 *  absolute time in case your datas are already in that form. This will
 *  eliminate unnecessary quantization error, e.g. <i>A-B+B</i>. For example,
 *  if the current time is 10.0 and the actor need to be refired at time
 *  20.0, then use fireAt(20.0) rather than fireAfterDelay(10.0). 
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
	this(null, null);
    }

    /** Construct a director with the specified name in the default
     *  workspace. If the name argument is null, then the name is set to the
     *  empty string. This director is added to the directory of the workspace,
     *  and the version of the workspace is incremented.
     *  @param name The name of this director.
     */
    public DEDirector(String name) {
	this(null, name);
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
        try {
            _stopTime = new Parameter(this, 
                    "StopTime", 
                    new DoubleToken(0.0));
        } catch (IllegalActionException e) {
            // shouldn't happen, because we know the Parameter class is an
            // acceptable type for this director.
            e.printStackTrace();
            throw new InternalErrorException("IllegalActionException: " + 
                    e.getMessage());
        } catch (NameDuplicationException e) {
            // The name is guaranteed to be unique here..
            e.printStackTrace();
            throw new InternalErrorException("NameDuplicationException: " +
                    e.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Schedule an actor to be fired after the specified delay. If the delay
     *  argument is equal to zero, then the actor will be refired after all
     *  actors enabled at current time are fired.
     *
     *  @param actor The scheduled actor to fire.
     *  @param delay The scheduled time to fire.
     *  @exception IllegalActionException If the delay is negative.
     */
    public void fireAfterDelay(Actor actor, double delay) 
            throws IllegalActionException {
        // FIXME: Check if the actor is in the composite actor containing this
        // director. I.e. the specified actor is under this director
        // responsibility. This could however be an expensive operation. So,
        // leave it out for now, and see if this will turn out to be an issue.

        // Check the special case, when the delay is equal to zero
        if (delay == 0 && _isInitialized) {
            this._enqueueEvent(actor, getCurrentTime(), Long.MAX_VALUE);
            return;
        }

        fireAt(actor, getCurrentTime() + delay);
    }
    
    /** Schedule an actor to be fired at the specified time.
     *  @param actor The scheduled actor to fire.
     *  @param time The scheduled time to fire.
     *  @exception IllegalActionException If the specified time is in the past.
     */
    public void fireAt(Actor actor, double time) 
            throws IllegalActionException {
        
        // FIXME: Check if the actor is in the composite actor containing this
        // director. I.e. the specified actor is under this director
        // responsibility. This could however be an expensive operation. So,
        // leave it out for now, and see if this will turn out to be an issue.
        
        // Check the special case, when the delay is equal to zero
        if (time == getCurrentTime() && _isInitialized) {
            this._enqueueEvent(actor, getCurrentTime(), Long.MAX_VALUE);
            return;
        }

        // If this actor has input ports, then the depth is set to be
        // one higher than the max depth of the input ports.
        // If this actor has no input ports, then the depth is set to
        // to be zero.
        long maxdepth = -1;
        Enumeration iports = actor.inputPorts();
        while (iports.hasMoreElements()) {
            IOPort p = (IOPort) iports.nextElement();
            Receiver[][] r = p.getReceivers();
            if (r == null) continue;
            DEReceiver rr = (DEReceiver) r[0][0];
            if (rr._depth > maxdepth) {
                maxdepth = rr._depth;
            }
        }
        this._enqueueEvent(actor, time, maxdepth+1);
    }

    /** Return the current time of the simulation. Firing actors that need to
     *  know the current time (e.g. for calculating the time stamp of the
     *  delayed outputs) call this method.
     */
    public double getCurrentTime() {
	return _currentTime;
    }
    
    /** Return the next future time of the next iterations. This means
     *  simultaneous iterations will be skipped, and only look at the next
     *  future time stamp (i.e. not equal to the current time).
     */
    public double getNextIterationTime() {
        return _nextIterationTime;
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
        // since _stopTime field is set in the constructor, it is guarantee
        // to be non-null.
        DoubleToken token = (DoubleToken)_stopTime.getToken();
        return token.doubleValue();
    }

    /** Return true if this director is embedded inside an opaque composite
     *  actor contained by another composite actor.
     *  @return True is the above condition is satisfied, false otherwise.
     */
    public boolean isEmbedded() {
        if (getContainer().getContainer() == null) {
            return false;
        } else {
            return true;
        }
    }

    /** Return a new receiver of a type DEReceiver.
     *  @return A new DEReceiver.
     */
    public Receiver newReceiver() {
	return new DEReceiver();
    }

    /** Return true or false according to the flag _shouldPostfireReturnFalse.
     *  This flag is set to the appropriate value during the prefire()
     *  or fire() phase of this director.
     *  @return False if _shouldPostfireReturnFalse, true otherwise.
     */
    public boolean postfire() throws IllegalActionException {
        if (_shouldPostfireReturnFalse) {
            return false;
        } else {
            return true;
        }
    }

    /** Set the stop time of the simulation.
     *  @param stopTime The new stop time.
     */
    public void setStopTime(double stopTime) {
        // since the _stopTime is field is set in the constructor,
        // it's guarantee to be non-null here.
        _stopTime.setToken(new DoubleToken(stopTime));
    }

    /** Decide whether the simulation should be stopped when there's no more
     *  events in the global event queue.
     *  By default, the value is 'true', meaning that the simulation will stop
     *  under that circumstances. Setting it to 'false', instruct the director
     *  to wait on the queue while some other threads might enqueue events in
     *  it.
     *  @param flag The new value for the flag.
     */
    public void stopWhenQueueIsEmpty(boolean flag) {
        _stopWhenQueueIsEmpty = flag;
    }

    /** Transfer data from an input port of the container to the
     *  ports it is connected to on the inside.  The port argument must
     *  be an opaque input port.  If any channel of the input port
     *  has no data, then that channel is ignored.
     *
     *  @param port The input port from which tokens are transferred.
     *  @exception IllegalActionException If the port is not an opaque
     *   input port.
     */
    // FIXME: Maybe this can be removed and update current time differently...
    public void transferInputs(IOPort port) throws IllegalActionException {
        if (!port.isInput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "transferInputs: port argument is not an opaque input port.");
        }
        Receiver[][] insiderecs = port.deepGetReceivers();
        for (int i=0; i < port.getWidth(); i++) {
            if (port.hasToken(i)) {
                try {
                    Token t = port.get(i);
                    if (insiderecs != null && insiderecs[i] != null) {
                        for (int j=0; j < insiderecs[i].length; j++) {
                            DEReceiver deRec =
                                (DEReceiver)insiderecs[i][j];

                            Actor container = (Actor)getContainer();
                            double outsideCurrTime = container.getExecutiveDirector().getCurrentTime();

                            deRec.put(t, outsideCurrTime - _currentTime);
                        }
                    }
                } catch (NoTokenException ex) {
                    // this shouldn't happen.
                    throw new InternalErrorException(
                            "Director.transferInputs: Internal error: " +
                            ex.getMessage());
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Put a "pure event" into the event queue with the specified delay and
     *  the time stamp of the event. 
     *  The depth is used to prioritize events that have equal
     *  time stamps.  A smaller depth corresponds to a higher priority.
     *  A "pure event" is one where no token is transferred.  The event
     *  is associated with a destination actor.  That actor will be fired
     *  when the time stamp of the event is the oldest in the system.
     *  Note that the actor may have no new data at its input ports
     *  when it is fired.
     *  <p>
     *  Derived class should implement this method according to the
     *  implementation of its global event queue.
     *
     *  @param actor The destination actor.
     *  @param time The time stamp of the "pure event".
     *  @param depth The depth.
     *  @exception IllegalActionException If the delay is negative.
     */
    protected abstract void _enqueueEvent(Actor actor, 
            double time, 
            long depth)
	 throws IllegalActionException;

    /** Put a token into the event queue with the specified destination
     *  receiver, delay and time stamp. The depth is used to prioritize
     *  events that have equal time stamps.  A smaller depth corresponds
     *  to a higher priority.
     *  <p>
     *  Derived class should implement this method according to the
     *  implementation of its global event queue.
     *
     *  @param receiver The destination receiver.
     *  @param token The token destined for that receiver.
     *  @param time The time stamp of the event.
     *  @param depth The depth.
     *  @exception IllegalActionException If the delay is negative.
     */
    protected abstract void _enqueueEvent(DEReceiver receiver,
	    Token token,
	    double time,
	    long depth)
	 throws IllegalActionException;

    /** Override the default Director implementation, because in DE
     *  domain, we don't need write access inside an iteration.
     *  @return false.
     */
    protected boolean _writeAccessPreference() {
        // Return false to let the workspace be write-protected.
        // Return true to debug the PtolemyThread.
        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    // The current time of the simulation.
    // Firing actors may get the current time by calling getCurrentTime()
    protected double _currentTime = 0.0;

    // Indicate whether the actors (not the director) is initialized.
    protected boolean _isInitialized = false;

    // The time of the next iteration.
    protected double _nextIterationTime;

    // Set to true when it's time to end the simulation.
    // e.g. The earliest time in the global event queue is greater than
    // the stop time.
    // FIXME: This is a hack :(
    protected boolean _shouldPostfireReturnFalse = false;

    // The time of the earliest event seen in the current simulation.
    protected double _startTime = Double.MAX_VALUE;

    // Decide whether the simulation should be stopped when there's no more
    // events in the global event queue.
    // By default, its value is 'true', meaning that the simulation will stop
    // under that circumstances. Setting it to 'false', instruct the director
    // to wait on the queue while some other threads might enqueue events in
    // it.
    protected boolean _stopWhenQueueIsEmpty = true;

    // The stop time parameter.
    private Parameter _stopTime;
}

