/* A static scheduler for the continuous time domain.

 Copyright (c) 1998-2001 The Regents of the University of California.
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

@ProposedRating Green (liuj@eecs.berkeley.edu)
@AcceptedRating Yellow (johnr@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.kernel;

import ptolemy.kernel.util.Workspace;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.lib.SequenceActor;
import ptolemy.actor.sched.NotSchedulableException;
import ptolemy.actor.sched.Scheduler;
import ptolemy.actor.sched.StaticSchedulingDirector;
import ptolemy.graph.DirectedAcyclicGraph;
import java.util.Collections;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Enumeration;

//This need to use the Schedule class.

//////////////////////////////////////////////////////////////////////////
//// CTScheduler
/**
Static scheduler for the CT domain.
A CT (sub)system can be mathematically represented as:<Br>
<pre>
<pre>    dx/dt = f(x, u, t)<Br>
<pre>    y = g(x, u, t)<BR>
</pre></pre></pre>
where x is the state of the system, u is the input, y is the output,
f() is the state transition map and g() is the output map.
<P>
The system is built by actors. That is, all the functions, f() and g(),
are built up by chains of actors.  For high order systems,
x is a vector, which is built up by more than one integrators.
In general, actors that have the functionality of integration
from their inputs to their outputs are called <I>dynamic actors</I>.
Other actors are called <I>arithmetic actors</I>.
<P>
In order to interact with discrete domains, some actors in the
CT domain are able to convert continuous waveforms to discrete events,
and vice versa. An actor that has continuous input and discrete
output is call an <I>event generator</I>; an actor that has
discrete input and continuous output is called a
<I>waveform generator</I>.
<P>
The interaction with some discrete domains requires that the
CT simulation be able to remember its state and roll-back
to the remembered state when needed. This in turn requires
that all actors which have internal states should be able
to remember and restore their states. These actors are called
<I>stateful actors</I>.
<P>
In the continuous time simulation, time progresses in a discrete way.
The distance between consecutive simulation time points are called
<I>integration step size</I> or step size, in short. Some actors
require specific step sizes of the simulation. These actors are
called <I>step size control actors</I>. Examples of step size
control actors include integrators, which control the
accuracy and speed of numerical ODE solutions, and some event
generators, which detect events.
<P>
To help the scheduling, a system topology is partitioned into
several clusters:
the <I>arithmetic actors</I>, the <I>dynamic actors</I>,
the <I>step size control actors</I>, the <I>sink actors</I>,
the <I>stateful actors</I>, the <I> event generators</I>,
and the <I> waveform generators</I>.
This scheduler uses the clustered information and the system topology,
to provide the firing sequences for evaluating f() and g().
It also provides a firing order for all the dynamic actors.
The firing sequence for evaluating f() is
called the <I> state transition schedule</I>; the firing
sequence for evaluating g() is called the <I> output schedule</I>;
and the firing sequence for dynamic actors is called the
<I>dynamic actor schedule</I>.
<P>
The state transition schedule is the actors in f() function sorted
in the topological order, such that, after the integrators emit their
state x, a chain of firings according to the schedule evaluates the
f() function and returns tokens corresponding to dx/dt to the
integrators.
<P>
The output schedule is the actors in g() function sorted in the topological
order.
<P>
The dynamic actor schedule is a list of dynamic actors in their reverse
topological order.
<P>
If there are loops of arithmetic actors or loops of integrators,
then the (sub)system is not schedulable, and a NotSchedulableException
will be thrown if schedules are requested.

@author Jie Liu
@version $Id$
@see ptolemy.actor.sched.Scheduler
*/

public class CTScheduler extends Scheduler{

    /** Construct a CT scheduler in the default workspace
     *  with the default name "CTScheduler". There is no director
     *  containing this scheduler. To attach this scheduler to a
     *  CTDirector, call setScheduler() on the CTDirector.
     */
    public CTScheduler() {
        this(null);
    }

    /** Construct a CT scheduler in the given workspace
     *  with the name "CTScheduler". There is no director
     *  containing this scheduler. To attach this scheduler to a
     *  CTDirector, call setScheduler() on the CTDirector.
     *
     *  @param workspace The workspace.
     */
    public CTScheduler(Workspace workspace) {
        super(workspace);
        try {
            setName(_STATIC_NAME);
        } catch (KernelException ex) {
            throw new InternalErrorException(
                    "Internal error when setName to a CTScheduler");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /** Returns a list of arithmatic actors. This list is
     *  locally cached. If workspace version equals to the cached version,
     *  then it returns the cached list.
     *  Otherwise, it will reconstruct, cache and return the
     *  updated list.
     *  This method read-synchronizes on the workspace.
     *  @return A list of arithmetic actors.
     */
    public List arithmaticActorList() {
        try {
            workspace().getReadAccess();
            if(_workspaceVersion != workspace().getVersion()) {
                _schedule();
                _workspaceVersion = workspace().getVersion();
            }
            return _arithmetic;
        } finally {
            workspace().doneReading();
        }
    }

    /** Returns an enumeration of arithmetic actors.
     *  @return An enumeration of arithmetic actors.
     *  @deprecated Use arithmaticActorList() instead.
     */
    public Enumeration arithmaticActors() {
        return Collections.enumeration(arithmaticActorList());
    }

    /** Clone the scheduler to the specified work space. All cached
     *  schedules are lost.
     *  @param The new workspace.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     *  @return The new Scheduler.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        CTScheduler newObject = (CTScheduler) super.clone(workspace);
        newObject._workspaceVersion = -1;
        return newObject;
    }

    /** Returns a list of discrete actors in their topological order.
     *  This list is locally
     *  cached. If workspace version equals to the cached version,
     *  then it returns the cached list.
     *  Otherwise, it will reconstruct, cache and return the
     *  updated list.
     *  This method read-synchronizes on the workspace.
     *  @return A list of discrete actors.
     */
    public List discreteActorSchedule() {
        try {
	    workspace().getReadAccess();
            if(_workspaceVersion != workspace().getVersion()) {
                // construct the discrete actor schedule.
                _schedule();
                _workspaceVersion = workspace().getVersion();
            }
            return _discreteSchedule;
        } finally {
            workspace().doneReading();
        }
    }
    
    /** Returns a list of dynamic actors. This list is locally
     *  cached. If workspace version equals to the cached version,
     *  then it returns the cached list.
     *  Otherwise, it will reconstruct, cache and return the
     *  updated list.
     *  This method read-synchronizes on the workspace.
     *  @return A list of dynamic actors.
     */
    public List dynamicActorList() {
        try {
	    workspace().getReadAccess();
            if(_workspaceVersion != workspace().getVersion()) {
                _schedule();
                _workspaceVersion = workspace().getVersion();
            }
            return _dynamic;
        } finally {
            workspace().doneReading();
        }
    }

    /** Returns an enumeration of dynamic actors.
     *  @return An enumeration of dynamic actors.
     *  @deprecated Use dynamicActorList() in stead.
     */
    public Enumeration dynamicActors() {
        return Collections.enumeration(dynamicActorList());
    }

    /** Returns an enumeration of the schedule of dynamic actors.
     *  @return An enumeration of the schedule of dynamic actors.
     *  @exception IllegalActionException If the scheduler has no container,
     *      or the container has no container.
     *  @exception NotSchedulableException If the system is not schedulable.
     *  @deprecated Use scheduledDynamicActorList() instead.
     */
    public Enumeration dynamicActorSchedule()
            throws NotSchedulableException, IllegalActionException {
        return Collections.enumeration(scheduledDynamicActorList());
    }

    /** Return the list of event generators in their creation order.
     *  This list is locally cached.
     *  If the workspace version equals to the cached version,
     *  then it returns the cached list.
     *  Otherwise, it reconstructs the list, and cache the new version.
     *  This method read-synchronizes on the workspace.
     *  @return The list of event generators.
     */
    public List eventGeneratorList() {
        try {
	    workspace().getReadAccess();
            if(_workspaceVersion != workspace().getVersion()) {
                _schedule();
                _workspaceVersion = workspace().getVersion();
            }
            return _eventGenerators;
        } finally {
            workspace().doneReading();
        }
    }

    /** Return an enumeration of event generators.
     *  @return An enumeration of event generators.
     *  @deprecated Use eventGeneratorList() instead.
     */
    public Enumeration eventGenerators() {
        return Collections.enumeration(eventGeneratorList());
    }

    /** Return an enumeration of event interpreters.
     *  @return An enumeration of event interpreters.
     *  @deprecated Use waveformGeneratorList() instead.
     */
    public Enumeration eventInterpreters() {
        return Collections.enumeration(waveformGeneratorList());
    }

    /** Returns an enumeration of schedule actors that form the output map.
     *  @return An enumeration of the schedule of the output path.
     *  @exception IllegalActionException If the scheduler has no container,
     *      or the container has no container.
     *  @exception NotSchedulableException If the system is not schedulable.
     *  @deprecated Use scheduledOutputActorList() instead.
     */
    public Enumeration outputSchedule()
            throws NotSchedulableException, IllegalActionException  {
        return Collections.enumeration(scheduledOutputActorList());
    }

    /** Return a list of step size control (SSC) actors in the output
     *  schedule. These are the step size control actors in the
     *  y = g(x, u, t) equations.
     *  These actors are in their creation order.
     *  The list is locally
     *  cached. If the workspace version equals to the cached version,
     *  then it returns the cached version.
     *  Otherwise, it will reconstruct, cache, and return the new
     *  version.
     *  This method read-synchronizes on the workspace.
     *  @return A list of step size control actors.
     *  @exception IllegalActionException If topology is not schedulable.
     */
    public List outputSSCActorList() throws IllegalActionException {
        try {
	    workspace().getReadAccess();
            if(!isValid()) {
                schedule();
            }
            _workspaceVersion = workspace().getVersion();
            return _outputSSC;
        } finally {
            workspace().doneReading();
        }
    }

    /** Return an enumeration of step size control (SSC) actors in the output
     *  schedule.
     *  @return An enumeration of step size control actors.
     *  @exception IllegalActionException If thrown by the schedule() method.
     *  @deprecated Use outputSSCActorList() instead.
     */
    public Enumeration outputSSCActors() throws IllegalActionException {
        return Collections.enumeration(outputSSCActorList());
    }

    /** Return the predecessors of the given actor in the topology.
     *  If the argument is null, returns null.
     *  If the actor is a source, returns an empty list.
     *  @param The specified actor.
     *  @return The list of predecessors, unordered.
     */
    public List predecessorList(Actor actor) {
        if(actor == null) {
            return null;
        }
        LinkedList predecessors = new LinkedList();
        Iterator inPorts = actor.inputPortList().iterator();
        while(inPorts.hasNext()) {
            IOPort port = (IOPort) inPorts.next();
            Iterator outPorts = port.deepConnectedOutPortList().iterator();
            while(outPorts.hasNext()) {
                IOPort outPort = (IOPort)outPorts.next();
                Actor pre = (Actor)outPort.getContainer();
                if(!predecessors.contains(pre)) {
                    predecessors.addLast(pre);
                }
            }
        }
        return predecessors;
    }

    /** Returns a list of scheduled dynamic actors.
     *  This list is locally cached.
     *  If workspace version equals to the cached version,
     *  then it returns the cached list.
     *  Otherwise, it will reconstruct, cache, and return the
     *  updated schedule.
     *  The dynamic actor schedule lists all the
     *  dynamic actors
     *  in the reverse topology order. This order can be used
     *  for both implicit methods and explicit methods. For implicit
     *  methods, the order is consistent with Gauss-Jacobi iteration.
     *  For explicit methods,
     *  it guarantees that the input of the integrator is
     *  one step earlier than the output.
     *  This method read-synchronizes on the workspace.
     *  @return A list of the scheduled dynamic actors.
     *  @exception IllegalActionException If the scheduler has no container,
     *      or the container has no container.
     *  @exception NotSchedulableException If the system is not schedulable.
     */
    public List scheduledDynamicActorList()
            throws NotSchedulableException, IllegalActionException {
        try {
            workspace().getReadAccess();
            if(!isValid()) {
                schedule();
            }
            _workspaceVersion = workspace().getVersion();
            return _stateSchedule;
        } finally {
            workspace().doneReading();
        }
    }

    /** Returns a list of scheduled actors that form the output map.
     *  The output schedule lists all the actors in the computational
     *  path from
     *  integrators (or dynamic actors) to sink actors (or composite
     *  actor's output ports) in the topological order. The firing of
     *  of the actors in this order corresponds to evaluating the output map
     *  of the system.
     *  The list is locally
     *  cached. If the workspace version equals to the cached version,
     *  then it returns the cached list.
     *  Otherwise, it will reconstruct, cache, and return the new
     *  list.
     *  This method read-synchronize on the workspace.
     *  @return A list of the schedule of the output path.
     *  @exception IllegalActionException If the scheduler has no container,
     *      or the container has no container.
     *  @exception NotSchedulableException If the system is not schedulable.
     */
    public List scheduledOutputActorList()
            throws NotSchedulableException, IllegalActionException  {
        try {
	    workspace().getReadAccess();
            if(!isValid()) {
                schedule();
            }
            _workspaceVersion = workspace().getVersion();
            return _outputSchedule;
        } finally {
            workspace().doneReading();
        }
    }

    /** Returns a list of scheduled actors that form the state transition map.
     *  The list is locally
     *  cached. If the workspace version equals to the cached version,
     *  then it returns the cached list.
     *  Otherwise, it will reconstruct, cache, and return the new
     *  list.
     *  This method read-synchronize on the workspace.
     *  @return A list of the scheduled actors of the state transition path.
     *  @exception IllegalActionException If the scheduler has no container,
     *      or the container has no container.
     *  @exception NotSchedulableException If the system is not schedulable.
     */
    public List scheduledStateTransitionActorList()
            throws NotSchedulableException, IllegalActionException {
        try {
	    workspace().getReadAccess();
            if(!isValid()) {
                schedule();
            }
            return _transitionSchedule;
        } finally {
            workspace().doneReading();
        }
    }

    /** Return a list of sink actors (actors with no outputs).
     *  The list is locally
     *  cached. If the workspace version equals to the cached version,
     *  then it returns the cached list.
     *  Otherwise, it will reconstruct, cache, and return the new
     *  list.
     *  This method read-synchronize on the workspace.
     *  @return A list of sinks.
     */
    public List sinkActorList() {
        try {
	    workspace().getReadAccess();
            if(_workspaceVersion != workspace().getVersion()) {
                _schedule();
                _workspaceVersion = workspace().getVersion();
            }
            return _sink;
        } finally {
            workspace().doneReading();
        }
    }

    /** Return an enumeration of sinks.
     *  @return An enumeration of sinks.
     *  @deprecated Use sinkActorList() instead.
     */
    public Enumeration sinkActors() {
        return Collections.enumeration(sinkActorList());
    }

    /** Return a list of stateful actors. Stateful actors are actors
     *  that has states. They implement the CTStatefulActor interface.
     *  The list is locally
     *  cached. If the workspace version equals to the cached version,
     *  then it returns the cached enumeration.
     *  Otherwise, it will reconstruct, cache, and return the new
     *  version.
     *  This method read-synchronizes on the workspace.
     *  @return A list of stateful actors.
     */
    public List statefulActorList() {
        try {
	    workspace().getReadAccess();
            if(_workspaceVersion != workspace().getVersion()) {
                _schedule();
                _workspaceVersion = workspace().getVersion();
            }
            return _stateful;
        } finally {
            workspace().doneReading();
        }
    }

    /** Return an enumeration of stateful actors.
     *  @return An enumeration of stateful actors.
     *  @deprecated Use statefulActorList() instead.
     */
    public Enumeration statefulActors() {
        return Collections.enumeration(statefulActorList());
    }

    /** Return a list of step size control (SSC) actors in the
     *  dynamic actor and state transition schedule. These are the step size
     *  control actors in the dx/dt = f(x, u, t) equation.
     *  The list is locally
     *  cached. If the workspace version equals to the cached version,
     *  then it returns the cached enumeration.
     *  Otherwise, it will reconstruct, cache, and return the new
     *  version.
     *  This method read-synchronizes on the workspace.
     *  @return A list of step size control actors in the dynamic-actor and
     *       state transition schedule.
     *  @exception IllegalActionException If the topology is not schedulable..
     *
     */
    public List stateTransitionSSCActorList()
            throws IllegalActionException {
        try {
	    workspace().getReadAccess();
            if(!isValid()) {
                schedule();
            }
            _workspaceVersion = workspace().getVersion();
            return _stateSSC;
        } finally {
            workspace().doneReading();
        }
    }

    /** Return an enumeration of step size control (SSC) actors in the
     *  state and state transition schedule.
     *  @return An enumeration of step size control actors.
     *  @exception IllegalActionException If thrown by the schedule() method.
     *  @deprecated Use stateTransitionSSCActorList()instead.
     */
    public Enumeration stateTransitionSSCActors()
            throws IllegalActionException {
        return Collections.enumeration(stateTransitionSSCActorList());
    }


    /** Returns an enumeration of the schedule of the state transition path.
     *  @return An enumeration of the schedule of the state transition path.
     *  @exception IllegalActionException If the scheduler has no container,
     *      or the container has no container.
     *  @exception NotSchedulableException If the system is not schedulable.
     *  @deprecated Use scheduledStateTransitionList() instead.
     */
    public Enumeration stateTransitionSchedule()
            throws NotSchedulableException, IllegalActionException {
        return Collections.enumeration(scheduledStateTransitionActorList());
    }

    /** Return the successive actors of the given actor in the topology.
     *  If the argument is null, returns null.
     *  If the actor is a sink, returns an empty list.
     *  @param The specified actor. If the actor is null, returns null.
     *  @return The enumerations of predecessors.
     */
    public List successorList(Actor actor) {
        if(actor == null) {
            return null;
        }
        LinkedList successors = new LinkedList();
        Iterator outports = actor.outputPortList().iterator();
        while(outports.hasNext()) {
            IOPort outPort = (IOPort) outports.next();
            Iterator inPorts = outPort.deepConnectedInPortList().iterator();
            while(inPorts.hasNext()) {
                IOPort inPort = (IOPort)inPorts.next();
                Actor post = (Actor)inPort.getContainer();
                if(!successors.contains(post)) {
                    successors.addLast(post);
                }
            }
        }
        return successors;
    }

    /** Return all the scheduling information in a Sting.
     *  @return All the schedules.
     */
    public String toString() {
        String result = new String();
        Iterator iterator;
        result += "CTSchedule {\n";
        result += "    arithmaticActors {\n";
        iterator = arithmaticActorList().iterator();
        try {
            while(iterator.hasNext()) {
                result += "\t" +
                    ((NamedObj)iterator.next()).getFullName() + "\n";
            }
            result += "    }\n";
            result += "    dynamicActors {\n";
            iterator = dynamicActorList().iterator();
            while(iterator.hasNext()) {
                result += "\t" +
                    ((NamedObj)iterator.next()).getFullName() + "\n";
            }
            result += "    }\n";
            result += "    stateTransitionSSCActors {\n";
            iterator = stateTransitionSSCActorList().iterator();
            while(iterator.hasNext()) {
                result += "\t" +
                    ((NamedObj)iterator.next()).getFullName() + "\n";
            }
            result += "    }\n";
            result += "    outputSSCActors {\n";
            iterator = outputSSCActorList().iterator();
            while(iterator.hasNext()) {
                result += "\t" +
                    ((NamedObj)iterator.next()).getFullName() + "\n";
            }
            result += "    }\n";
            result += "    eventGenerators {\n";
            iterator = eventGeneratorList().iterator();
            while(iterator.hasNext()) {
                result += "\t" +
                    ((NamedObj)iterator.next()).getFullName() + "\n";
            }
            result += "    }\n";
            result += "    eventInterpreters {\n";
            iterator = waveformGeneratorList().iterator();
            while(iterator.hasNext()) {
                result += "\t" +
                    ((NamedObj)iterator.next()).getFullName() + "\n";
            }
            result += "    }\n";
            result += "    statefulActors {\n";
            iterator = statefulActorList().iterator();
            while(iterator.hasNext()) {
                result += "\t" +
                    ((NamedObj)iterator.next()).getFullName() + "\n";
            }
            result += "    }\n";
            result += "    sinkActors {\n";
            iterator = sinkActorList().iterator();
            while(iterator.hasNext()) {
                result += "\t" +
                    ((NamedObj)iterator.next()).getFullName() + "\n";
            }
            result += "    }\n";
            result += "    dynamicActorSchedule {\n";
            iterator = scheduledDynamicActorList().iterator();
            while(iterator.hasNext()) {
                result += "\t" +
                    ((NamedObj)iterator.next()).getFullName() + "\n";
            }
            result += "    }\n";
            result += "    stateTransitionSchedule {\n";
            iterator = scheduledStateTransitionActorList().iterator();
            while(iterator.hasNext()) {
                result += "\t" +
                    ((NamedObj)iterator.next()).getFullName() + "\n";
            }
            result += "    }\n";
            result += "    outputSchedule {\n";
            iterator = scheduledOutputActorList().iterator();
            while(iterator.hasNext()) {
                result += "\t" +
                    ((NamedObj)iterator.next()).getFullName() + "\n";
            }
            result += "    }\n";
            result += "    discreteSchedule {\n";
            iterator = discreteActorSchedule().iterator();
            while(iterator.hasNext()) {
                result += "\t" +
                    ((NamedObj)iterator.next()).getFullName() + "\n";
            }
            result += "    }\n";
            result += "}\n";


        } catch (IllegalActionException ex) {
            throw new InvalidStateException(this,
                    "Failed to generate CT schedule.");
        }
        return result;
    }

    /** Return a list of waveform generators in their creation order.
     *  The list is locally
     *  cached. If the workspace version equals to the cached version,
     *  then it returns the cached enumeration.
     *  Otherwise, it will reconstruct, cache, and return the new
     *  version.
     *  This method read-synchronizes on the workspace.
     *  @return An list of event interpreters.
     */
    public List waveformGeneratorList() {
        try {
	    workspace().getReadAccess();
            if(_workspaceVersion != workspace().getVersion()) {
                _schedule();
                _workspaceVersion = workspace().getVersion();
            }
            return _waveGenerators;
        } finally {
            workspace().doneReading();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Partition the system topology into clusters.
     *  Find out the arithmetic actors, dynamic actors, sink actors,
     *  event generators, waveform generators
     *  and stateful actors in the
     *  CompositeActor.
     *  @exception  NotSchedulableException If the system is not
     *  schedulable.
     */
    protected void _classifyActors() throws NotSchedulableException {
        if(_workspaceVersion == -1) {
            _sink = new LinkedList();
            _dynamic = new LinkedList();
            _arithmetic = new LinkedList();
            _eventGenerators = new LinkedList();
            _waveGenerators = new LinkedList();
            _stateful = new LinkedList();
            _discretes = new LinkedList();
        }else {
            _sink.clear();
            _dynamic.clear();
            _arithmetic.clear();
            _eventGenerators.clear();
            _waveGenerators.clear();
            _stateful.clear();
            _discretes.clear();
        }

        CompositeActor compositeActor =
            (CompositeActor)getContainer().getContainer();
        Iterator actors = compositeActor.deepEntityList().iterator();
        while(actors.hasNext()) {
            Actor a = (Actor) actors.next();
            if (a instanceof CTStatefulActor) {
                _stateful.addLast(a);
            }
            if (a instanceof CTEventGenerator) {
                _eventGenerators.addLast(a);
            }
            if (a instanceof CTWaveformGenerator) {
                _waveGenerators.addLast(a);
            }
            if (successorList(a).isEmpty()) {
                _sink.addLast(a);
            }
            if (a instanceof CTDynamicActor) {
                _dynamic.addLast(a);
            } else {
                _arithmetic.addLast(a);
            }
            if ((a instanceof TypedCompositeActor) && 
                    !( a instanceof CTCompositeActor)) {
                _discretes.addLast(a);
            }
            if (a instanceof SequenceActor) {
                _discretes.addLast(a);
            }
        }

        // Step size control (SSC) actors are classified according to
        // their location in the schedule. So they are created
        // in the _schedule() method.
        
        // For each eventGenerator, its successors should either
        // be waveformGenerator or a discrete (composite) actor.
        // For each discrete (composite) actor, its predecessors
        // should be eventGenerators, and its successors should be
        // waveformGenerator. Note that all of the tests below are
        // needed, although they overlap.
        Iterator generators = _eventGenerators.iterator();
        while(generators.hasNext()) {
            Actor generator = (Actor) generators.next();
            Iterator successors = successorList(generator).iterator();
            while(successors.hasNext()) {
                Object nextActor = successors.next();
                if (!_discretes.contains(nextActor) && 
                    !_waveGenerators.contains(nextActor) &&
                    !_sink.contains(nextActor)) {
                    throw new NotSchedulableException(getContainer(),
                            (NamedObj)nextActor
                            + " is a successor of an event generator "
                            + (NamedObj)generator 
                            + ", but it is not a discrete actor.");
                }
            }
        }
        generators = _waveGenerators.iterator();
        while(generators.hasNext()) {
            Actor generator = (Actor) generators.next();
            Iterator predecessors = predecessorList(generator).iterator();
            while(predecessors.hasNext()) {
                Object nextActor = predecessors.next();
                if (!_discretes.contains(nextActor) && 
                    !_eventGenerators.contains(nextActor)) {
                    throw new NotSchedulableException (getContainer(),
                            (NamedObj)nextActor
                            + " is a predecessor of a waveform generator "
                            + (NamedObj)generator 
                            + ", but it is not a discrete actor.");
                }
            }
        }
        Iterator discreteActors = _discretes.iterator();
        while(discreteActors.hasNext()) {
            Actor actor = (Actor) discreteActors.next();
            Iterator predecessors = predecessorList(actor).iterator();
            while(predecessors.hasNext()) {
                Object nextActor = predecessors.next();
                if (!_eventGenerators.contains(nextActor)) {
                    throw new NotSchedulableException (getContainer(),
                            (NamedObj)nextActor
                            + " is a predecessor of a discrete actor "
                            + (NamedObj)actor 
                            + ", but it is not an event generator.");
                }
            }
            Iterator successors = successorList(actor).iterator();
            while(successors.hasNext()) {
                Object nextActor = successors.next();
                if (!_waveGenerators.contains(nextActor)) {
                    throw new NotSchedulableException(getContainer(),
                            (NamedObj)nextActor
                            + " is a successor of a discrete actor "
                            + (NamedObj)actor
                            + ", but it is not a waveform generator.");
                }
            }
        }
    }



    /** Return an enumeration of the detail schedules. The first
     *  element in the Enumeration is the states schedule, then
     *  transition schedule, then output schedule. Each schedule is
     *  a list of actors in a certain order.
     *  Each schedule can also be accessed by individual scheduledXXList
     *  methods.
     * @return an Enumeration of sub-schedules.
     * @exception NotSchedulableException If either the system topology
     * is wrong or arithmetic loop exists.
     */
    protected Enumeration _schedule() throws NotSchedulableException {
        CTDirector dir = (CTDirector)getContainer();
        if(dir == null) {
            return null;
        }
        CompositeActor container = (CompositeActor)(dir.getContainer());
        if(container == null) {
            return null;
        }
        _stateSchedule = new LinkedList();
        _transitionSchedule = new LinkedList();
        _outputSchedule = new LinkedList();
        _stateSSC = new LinkedList();
        _outputSSC = new LinkedList();
        _discreteSchedule = new LinkedList();
        LinkedList _scheduleList = new LinkedList();

        _classifyActors();

        // Topological sort discrete actors.
        DirectedAcyclicGraph discreteGraph = _toGraph(_discretes);
        if(!discreteGraph.isAcyclic()) {
            throw new NotSchedulableException(
                    "Found a loop of discrete actors.");
        }
        Object[] sorted = discreteGraph.topologicalSort();
        for(int i = 0; i < sorted.length; i++) {
            _discreteSchedule.addLast(sorted[i]);
        }

        // Integrators and arithmetic actors.
        DirectedAcyclicGraph arithmetic
            = _toArithmeticGraph(container.deepEntityList());
        DirectedAcyclicGraph dynamic = _toGraph(_dynamic);
        if(!arithmetic.isAcyclic()) {
            throw new NotSchedulableException("Arithmetic loop found.");
        }
        int dynamicSize = _dynamic.size();
        if(dynamicSize > 0) {
            Object[] dynamicActors = _dynamic.toArray();
            // Dynamic actors are reverse ordered.
            Object[] xSorted = dynamic.topologicalSort(dynamicActors);
            for(int i = 0; i < xSorted.length; i++) {
                Actor a = (Actor)xSorted[i];
                _stateSchedule.addFirst(a);
                if (a instanceof CTStepSizeControlActor) {
                    // Note: they are not ordered, but addFirst() is
                    // considered more efficient.
                    _stateSSC.addFirst(a);
                }
            }
            _scheduleList.addLast(_stateSchedule);
            // State transition map
            Object[] fx = arithmetic.backwardReachableNodes(dynamicActors);
            Object[] fxSorted = arithmetic.topologicalSort(fx);
            for(int i = 0; i < fxSorted.length; i++) {
                Actor a = (Actor)fxSorted[i];
                if (!_discretes.contains(a)) {
                    _transitionSchedule.addLast(a);
                    if (a instanceof CTStepSizeControlActor) {
                        // Note: they are not ordered, but addFirst() is
                        // considered more efficient.
                        _stateSSC.addFirst(a);
                    }
                }
            }
            _scheduleList.addLast(_transitionSchedule);
        }

        // construct an array of sink actors.
        int sinkSize = _sink.size();
        if(sinkSize > 0) {
            Object[] sinkActors =  _sink.toArray();
            //Output map.
            Object[] gx = arithmetic.backwardReachableNodes(sinkActors);
            Object[] gxSorted = arithmetic.topologicalSort(gx);
            for(int i = 0; i < gxSorted.length; i++) {
                Actor a = (Actor)gxSorted[i];
                if (!_discretes.contains(a)) {
                    _outputSchedule.addLast(a);
                    if (a instanceof CTStepSizeControlActor) {
                        _outputSSC.addLast(a);
                    }
                }
            }
            // add sinks to the output schedule
            for (int i = 0; i < sinkActors.length; i++) {
                Actor a = (Actor)sinkActors[i];
                if (!_discretes.contains(a)) {
                    _outputSchedule.addLast(a);
                    if (a instanceof CTStepSizeControlActor) {
                        _outputSSC.addLast(a);
                    }
                }
            }

            _scheduleList.addLast(_outputSchedule);
        }

        setValid(true);
        return Collections.enumeration(_scheduleList);
    }

    /** Convert the given list of actors to a directed acyclic graph.
     *  CTDynamicActors are treated as sinks to break closed loops.
     *  Each actor in the argument is a node in the graph,
     *  each link between a pair of actors, except the output links
     *  from dynamic actors, is a edge between the
     *  corresponding nodes.
     *  The existence of the director and containers is not checked
     *  in this method, so the caller should check.
     *  @param list The list of actors to be scheduled.
     *  @return A graph representation of the actors.
     */
    protected DirectedAcyclicGraph _toArithmeticGraph(List list) {
        DirectedAcyclicGraph graph = new DirectedAcyclicGraph();
        // Create the nodes.
        Iterator actors = list.iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor)actors.next();
            graph.add(actor);
        }

        // Create the edges.
        actors = list.iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            if(!(actor instanceof CTDynamicActor)) {
                // Find the successors of the actor
                Iterator successors = successorList(actor).iterator();
                while (successors.hasNext()) {
                    Actor successor = (Actor)successors.next();
                    if(list.contains(successor)) {
                        graph.addEdge(actor, successor);
                    }
                }
            }
        }
	return graph;
    }

    /** Convert the given actors to a directed acyclic graph.
     *  CTDynamicActors are NOT treated as sinks. This method
     *  is used to construct the dynamic actor schedule.
     *  Each actor in the argument is a node in the graph,
     *  and each link between a pair of actors is a edge between the
     *  corresponding nodes.
     *  @param list The list of actors to be converted to a graph.
     *  @return A graph representation of the actors.
     */
    protected DirectedAcyclicGraph _toGraph(List list) {

        DirectedAcyclicGraph g = new DirectedAcyclicGraph();
        // Create the nodes.
        Iterator actors = list.iterator();
        while (actors.hasNext()) {
            Actor a = (Actor)actors.next();
            g.add(a);
        }
        // Create the edges.
        actors = list.iterator();
        while (actors.hasNext()) {
            Actor a = (Actor) actors.next();
            // Find the successors of a
            Iterator successors = successorList(a).iterator();
            while (successors.hasNext()) {
                Actor s = (Actor) successors.next();
                if(list.contains(s)) {
                    g.addEdge(a, s);
                }
            }
        }
	return g;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The static name of the scheduler.
    private static final String _STATIC_NAME = "CTScheduler";
    // schedule lists
    private  transient LinkedList _stateSchedule;
    private  transient LinkedList _transitionSchedule;
    private  transient LinkedList _outputSchedule;
    private  transient LinkedList _discreteSchedule;

    // A LinkedList of the dynamic actors.
    private transient LinkedList _dynamic;
    // A LinkedList of discrete actors.
    private transient LinkedList _discretes;
    // A LinkedList of the sink actors.
    private transient LinkedList _sink;
    // A LinkedList of the arithmetic actors.
    private transient LinkedList _arithmetic;
    // A linkedList of SSC actors in the state and transition schedule.
    private transient LinkedList _stateSSC;
    // A linkedList of SSC actors in the state and transition schedule.
    private transient LinkedList _outputSSC;
    // A linkedList of event generators.
    private transient LinkedList _eventGenerators;
    // A linkedList of waveform generators.
    private transient LinkedList _waveGenerators;
    // A linkedList of stateful actors.
    private transient LinkedList _stateful;

    // Version of the lists.
    private transient long _workspaceVersion = -1;
}
