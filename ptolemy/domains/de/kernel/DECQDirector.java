/* A DE domain director that uses the CalendarQueue class for scheduling.

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
//// DECQDirector
//
/** A Discrete Event (DE) domain director handles the execution of actors
 *  contained by its CompositeActor container. It plays the role of
 *  <i>executive director</i> of the contained actors and
 *  <i>local director</i> of the CompositeActor container, simultaneously.
 *  <p>
 *  The execution schedule (FIXME: scheduling ?) is done in the DECQDirector
 *  class by means of a global event queue (a priority queue) implemented as
 *  an instance of the CalendarQueue class.
 *  <p>
 *  Sorting in the CalendarQueue class is done with respect to sort-keys
 *  which are implemented by the DESortKey class. DESortKey consists of a
 *  time stamp (double) and a 'receiver-depth' (double). Time stamp
 *  indicates the time when the event occurs, and 'receiver-depth'
 *  indicates the topological depth of the receiver receiving the event.
 *  The topological depth of the receivers are static and computed once
 *  in the initialization() method. This is done by performing the
 *  topological sort algorithm on the directed graph with input ports as
 *  nodes.
 *  <p>
 *  Ports in Discrete Event domain are instances of DEIOPort. DEIOPort define
 *  two additional field, namely beforePort and triggerList. Edges of the
 *  directed graph is constructed with respect to these fields. In order to
 *  perform topological sort successfully, the constructed graph is
 *  necessary to be acyclic. A special actor, called DEDelta, can be used to
 *  break the loop in graph.
 *
 *  FIXME: the term 'firing actor' doesn't sound quite right.. no suggestion
 *         though
 *
 *  On invocation of the prefire() method, DECQDirector dequeues
 *  the 'appropriate' oldest events (i.e. ones with smallest time
 *  stamp) from the global event queue and put those into
 *  their corresponding receivers. The term 'appropriate' means that
 *  the events dequeued are chosen such that all events are destined for
 *  the same actor. That particular actor will then be called the 'firing
 *  actor'. If the oldest events are destined for multiple actors, then
 *  the choice of the firing actor is determined by the topological depth
 *  of the input ports of the actors.
 *  <p>
 *  In the fire() method, the 'firing actor' is fired (i.e. invoke the
 *  fire() method of the 'firing actor'). The actor will consume events from
 *  its input port(s) and will usually produce new events on its output
 *  port(s). These new events will be enqueued into the queue where they're
 *  waiting to be dequeued when the time comes.
 *  <p>
 *  A DE domain simulation ends when the time stamp of the oldest events
 *  exceeds a preset stop time. This stopping condition is checked inside
 *  the postfire() method.
 *  <p>
 *  Note that as mentioned before, all oldest events for the 'firing actor'
 *  are dequeued and put into the corresponding receivers. It is thus
 *  possible to have multiple simultaneous events put into the same receiver.
 *  These events will all be accessible by the actor during the firing
 *  phase, but it's not clear which one is ahead of which. This is, in fact,
 *  one source of indeterminancy in DE semantic. How to handle this is
 *  up to the designer of the actor.
@author Lukito Muliadi
@version $Id$
@see DEDelta
@see DEReceiver
@see CalendarQueue
@
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
   /** Override the default initialize() method. This method constructs
    *  the directed graph with input ports as nodes and check for zero delay
    *  loop.
    *
    *  @exception CloneNotSupportedException If the initialize() method of the
    *   container or one of the deeply contained actors throws it.
    *  @exception IllegalActionException If the initialize() method of the
    *   container or one of the deeply contained actors throws it.
    */
    public void initialize()
            throws CloneNotSupportedException, IllegalActionException {

        _constructDirectedGraph();
	if (!_isDelayFreeLoopExist()) {
	    throw new IllegalActionException("Can't initialize a "+
		    "cyclic graph in DECQDirector.initialize()");
	}
        _computeDepth();
        super.initialize();
    }

    /** Invoke the default prefire() method and if it returns true, put
     *  tokens in the appropriate receivers. If there are multiple
     *  simultaneous events for the firing actor, then all events are
     *  dequeued from the global queue and put into the corresponding
     *  receivers.
     *
     *  @return True if the iteration can proceed.
     *  @exception CloneNotSupportedException If the prefire() method of the
     *   container or one of the deeply contained actors throws it.
     *  @exception IllegalActionException If the prefire() method of the
     *   container or one of the deeply contained actors throws it, or a
     *   pending mutation throws it.
     *  @exception NameDuplicationException If a pending mutation throws it.
     */
    public boolean prefire()
            throws CloneNotSupportedException, IllegalActionException,
            NameDuplicationException {
        boolean result = super.prefire();
	if (result) {
	    _fillReceiver();
	}
	return result;
    }

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

        // First check if container is null, which indicates an error in
        // the topology.
        if (container != null) {

            // Done with dequeueing necessary events, time to fire the actor!
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
        double nextTimeStamp = 0.0;
        try {
            nextTimeStamp = ((DESortKey)_cQueue.getNextKey()).timeStamp();
        } catch (IllegalAccessException e) {
            // FIXME: can't happen ?
            System.out.println("Check DECQDirector.postfire() for a bug!");
        }
        if (nextTimeStamp > _stopTime) {
            return false;
        } else {
            return true;
        }
    }

    /** Return a new receiver of a type DEReceiver.
     *
     *  @return A new DEReceiver.
     */
    public Receiver newReceiver() {
        return new DEReceiver(this);
    }

    /** Put the new event into the global event queue. The event consists
     *  of the destination actor, the destination receiver, and the
     *  transferred token along with its sort key.
     *  <p>
     *  Only the receiver should call this method.
     *
     *  @param a The destination actor.
     *  @param r The destination receiver.
     *  @param t The transferred token.
     *  @param k The sort key for the token.
     *
     */
    public void enqueueEvent(Actor a, DEReceiver r, Token t, DESortKey k) {

	/*
        // FIXME: debug stuff
        NamedObj b = (NamedObj) a;
        System.out.println("Enqueuing event: " + b.description(CLASSNAME | FULLNAME) + " at time: " + k.timeStamp());
        // FIXME: end debug stuff
	*/

        // FIXME: need to check if Actor == null ??
        if (a==null) {
            throw new IllegalArgumentException("DECQDirector, trying to enqueue null actor!");
        }
        CQValue newValue = new CQValue(a, r, t, k);
        _cQueue.put(k, newValue);
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

    /** Return the current time of the simulation. Firing actors that need to
     *  know the current time (e.g. for calculating the time stamp of the
     *  delayed outputs) call this method.
     */
    public double currentTime() {
        return _currentTime;
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private methods                        ////

    // Construct a directed graph with the nodes representing input ports and
    // directed edges representing zero delay path.
    // FIXME: this method is too complicated and probably means
    // there's a flaw in the design.
    private void _constructDirectedGraph() {

        LinkedList portList = new LinkedList();
	LinkedList deltaList = new LinkedList();

        // First, include all input ports to be nodes in the graph.
        CompositeActor container = ((CompositeActor)getContainer());
        if (container != null) {
            // get all the contained actors.
            Enumeration allactors = container.deepGetEntities();
            while (allactors.hasMoreElements()) {
		// get all the input ports in that actor
		Actor actor = (Actor)allactors.nextElement();
		// exclude input ports of DEDelta actors.
		if (!(actor instanceof DEDelta)) {
		    Enumeration allports = actor.inputPorts();
		    while (allports.hasMoreElements()) {
			IOPort port = (IOPort)allports.nextElement();
			// create the nodes in the graph.
			_dag.add(port);
			portList.insertLast(port);
		    }
		} else {
		    Enumeration allports = actor.inputPorts();
		    while (allports.hasMoreElements()) {
			IOPort port = (IOPort)allports.nextElement();
			deltaList.insertLast(port);
		    }
		}
            }
        }

        // Next, create the directed edges.
        Enumeration copiedPorts = portList.elements();
        while (copiedPorts.hasMoreElements()) {
            IOPort ioPort = (IOPort)copiedPorts.nextElement();

	    System.out.println("#################################");
	    System.out.println("Observing : " + ioPort.description(CLASSNAME | FULLNAME));
	    System.out.println("#################################");

	    // Find the successor of p
            if (ioPort instanceof DEIOPort) {
                DEIOPort p = (DEIOPort) ioPort;
                if (p.beforePort != null) {
                    // create an arc from p.beforePort to p
                    if (_dag.contains(p.beforePort)) {
                        _dag.addEdge(p.beforePort, p);
                    } else {
			throw new InvalidStateException("Check in "+
				"DECQDirector.computeDepth for bug (1)");
		    }
		}
		Enumeration triggers = p.triggerList.elements();
		while (triggers.hasMoreElements()) {
		    IOPort outPort = (IOPort) triggers.nextElement();
		    IOPort deltaInPort = _searchDeltaPort(outPort);
		    // find out the input ports connected to outPort
		    Enumeration inPortEnum = outPort.deepConnectedInPorts();
		    while (inPortEnum.hasMoreElements()) {
                        IOPort pp = (IOPort)inPortEnum.nextElement();
                        // create an arc from p to pp
                        if (_dag.contains(pp)) {
			    if (pp != deltaInPort)
			       _dag.addEdge(p,pp);
                        } else {
			    throw new InvalidStateException("Check in "+
				    "DECQDirector.computeDepth "+
				    "for bug (2)");
			}
		    }
		}
	    } else {
		// It is not a DEIOPort, so assume zero delay actor.
		// Therefore it triggers all.
		Enumeration triggers = ((Actor)ioPort.getContainer()).outputPorts();
                while (triggers.hasMoreElements()) {
		    IOPort outPort = (IOPort) triggers.nextElement();
		    IOPort deltaInPort = _searchDeltaPort(outPort);
                    // find out the input ports connected to outPort
                    Enumeration inPortEnum = outPort.deepConnectedInPorts();
                    while (inPortEnum.hasMoreElements()) {
                        IOPort pp = (IOPort)inPortEnum.nextElement();
                        // create an arc from p to pp
                        if (_dag.contains(pp)) {
			    if (pp != deltaInPort)
				_dag.addEdge(ioPort,pp);
                        } else {
			    throw new InvalidStateException("Check in "+
				    "DECQDirector.computeDepth "+
				    "for bug (3)");
                        }
                    }
                }
	    }
        }
    }

    // Return true if there's no delay free loop, false otherwise.
    private boolean _isDelayFreeLoopExist() {
        return (_dag.isAcyclic());
    }

    // Perform topological sort on the directed graph and use the result
    // to set the fine level field of the DEReceiver objects.
    private void _computeDepth() {
	Object[] sort = (Object[]) _dag.topSort();
	for(int i=sort.length-1; i >= 0; i--) {
            IOPort p = (IOPort)sort[i];
            // set the fine levels of all DEReceiver instances in IOPort p
            // to be i
            // FIXME: should I use deepGetReceivers() here ?
            Receiver[][] r;
	    try {
                r = p.getReceivers();
            } catch (IllegalActionException e) {
                // do nothing
                throw new InvalidStateException("Bug in DECQDirector."+
                        "computeDepth() (3)");
            }
	    if (r == null) {
		// dangling input port..
		continue;
	    }
	    for (int j=r.length-1; j >= 0; j--) {
                for (int k=r[j].length-1; k >= 0; k--) {
                    DEReceiver der = (DEReceiver)r[j][k];
                    der.setDepth(i);
                }
            }
	}
    }

    // Get the tokens with the oldest time stamp destined for the same
    // actor and put them into the receivers.
    //
    private void _fillReceiver() throws IllegalActionException {
	CQValue cqValue = null;
	DEReceiver receiver;
	_currentActor = null;
	FIFOQueue fifo = new FIFOQueue();

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

	    // On first iteration always accept the event.
	    if (_currentActor == null) {
		// These are done only once per execution of this fire()
		// method.
		_currentActor = cqValue.actor;
		_currentTime = cqValue.key.timeStamp();
	    }

	    // Check if the event occured at current time.
	    if (cqValue.key.timeStamp() != _currentTime) {
		// The event occured not at current time, therefore put the
		// event into the fifo queue to be enqueued back into the
		// calendar queue later outside the loop.
                fifo.put(cqValue);
                break;
	    }

	    // check if it is for the same actor
	    if (cqValue.actor == _currentActor) {


		// FIXME: assume it's always for different receiver.
		// FIXME: What do do if there are multiple events destined
		// for the same receiver.

		// Each DEReceiver can contains multiple simultaneous
		// events. Therefore, one can simply put the token into
		// the receiver.
		cqValue.deReceiver.triggerEvent(cqValue.token);

	    } else {
		// put it into a FIFOQueue to be returned to queue later.
		fifo.put(cqValue);
	    }
	}

	// Transfer back the events from the fifo queue into the calendar
	// queue.
	while (fifo.size() > 0) {
	    CQValue cqval = (CQValue)fifo.take();
	    _cQueue.put(cqval.key,cqval);
	}
    }

    private IOPort _searchDeltaPort(IOPort outPort) {
	Enumeration cpEnum = outPort.connectedPorts();
	while (cpEnum.hasMoreElements()) {
	    IOPort cp = (IOPort) cpEnum.nextElement();
	    if (cp.isInput() && ((cp.getContainer()) instanceof DEDelta)) {
		Actor a = (Actor)cp.getContainer();
		IOPort p = (IOPort)a.outputPorts().nextElement();
		return (IOPort)p.connectedPorts().nextElement();
	    }
	}
	return null;
    }
    ///////////////////////////////////////////////////////////////////
    ////                         private inner class               ////

    // private inner class CQValue: wrapper for the datas that want to be
    // stored in the queue.
    // FIXME: CQValue.. bad name ?
    private class CQValue {
        // constructor
        CQValue(Actor a, DEReceiver r, Token t, DESortKey k) {
            actor = a;
            deReceiver = r;
            token = t;
            key = k;
        }

        // public fields
        public Actor actor;
        public DEReceiver deReceiver;
        public Token token;
        // FIXME: key is not really needed, but have it anyway for convenience.
        public DESortKey key;
    }



    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    //_cQueue: an instance of CalendarQueue is used for sorting.
    private CalendarQueue _cQueue = new CalendarQueue(new DECQComparator());

    // variables to keep track of the objects currently firing.
    private Actor _currentActor = null;

    // _stopTime defines the stopping condition
    private double _stopTime = 0.0;

    // _currentTime the current time of the simulation.
    // Firing stars may get the current time by calling the getCurrentTime()
    // method.
    private double _currentTime = 0.0;

    // _dag Directed Graph whose nodes represent input ports and whose
    // edges represent delay free paths.
    private DirectedGraph _dag = new DirectedGraph();

}












