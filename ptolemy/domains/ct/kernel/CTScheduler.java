/* A topological sort scheduler for continuous time systems.

 Copyright (c) 1998-1999 The Regents of the University of California.
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

@ProposedRating Yellow (liuj@eecs.berkeley.edu)
@AcceptedRating Yellow (johnr@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.kernel;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.actor.sched.*;
import ptolemy.graph.*;
import java.util.Collections;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Enumeration;


//////////////////////////////////////////////////////////////////////////
//// CTScheduler
/**
Clustered graph sorting scheduler for CT domain.
A CT (sub)system can be represented mathematically as:<Br>
    dx/dt = f(x, u, t)<Br>
    y = g(x, u, t)<BR>
    e: h(x, u, t) = 0<BR>

where x is the state of the system, u is the input, y is the output,
 f() is the state transition map, g() is the output map,
and h() is the event generation map. This formulation adds the
event generation map to the usual continuous time modeling
to specifically handle the interaction with discrete domains.
<P>
The system is built by actors. That is, all the functions, f(), g(),
and h() are built by chains of actors. The scheduler will cluster and
sort the system topology, and will provide the firing order for
evaluating f(), g(). The firing order for evaluating f() is
called the <I> state transition schedule</I>; the firing order for
evaluating g() is called the <I> output schedule</I>; and
the firing order for all the dynamic actors is called the
<I>dynamic actor schedule</I>.
<P>
To help the scheduling, a system topology is partitioned into
several clusters:
the <I>arithmetic actors</I>, the <I>dynamic actors</I>,
the <I>step size control actors</I>, the <I>sink actors</I>,
the <I>stateful actors</I>, the <I> event generator</I>,
and the <I> event interpreters</I>.
<P>
Arithmetic actors are actors that has no integrators in its
function. Dynamic actors is the opposite of arithmetic actors,
i.e. it has integrators. The most common dynamic actors are
integrators, whose output is the state x of the system.
<P>
The state schedule is a list of dynamic actors (integrators or actors
that can produce initial token) which are in the reverse
topological order.
<P>
The state transition schedule is the actors in f() function sorted
in the topological order, such that, after the integrators emit their
state x, a chain of firings according to the schedule evaluates the
f() function and returns a token corresponding to dx/dt to the
integrators.
<P>
The output schedule is the actors in g() function sorted in the topological
order.

If there are loops of arithmetic actors or loops of integrators,
then the (sub)system is not schedulable, and a NotSchedulableException
will be thrown.

@author Jie Liu
@version $Id$
@see ptolemy.actor.sched.Scheduler
*/

public class CTScheduler extends Scheduler{

    /** Construct a CT scheduler in the default workspace
     *  with the default name "CTScheduler".
     */
    public CTScheduler() {
        super();
        try {
            setName(_STATIC_NAME);
        } catch (KernelException ex) {
            throw new InternalErrorException(
                    "Internal error when setName to a CTScheduler");
        }
    }

    /** Construct a CT topological sort scheduler in the given workspace
     *  with the name "CTScheduler".
     *
     *  @param ws The workspace.
     */
    public CTScheduler(Workspace ws) {
        super(ws);
        try {
            setName(_STATIC_NAME);
        } catch (KernelException ex) {
            throw new InternalErrorException(
                    "Internal error when setName to a CTScheduler");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /** Returns a list of arithmetic actors. This list is
     *  locally cached. If workspace version equals to the cached version,
     *  then it returns the cached list.
     *  Otherwise, it is reconstructed and cached.
     *  This method read-synchronizes on the workspace.
     *  @return A list of arithmetic actors.
     */
    public List arithmaticActorList() {
        try {
            workspace().getReadAccess();
            if(_wsversion != workspace().getVersion()) {
                _classifyActors();
                _wsversion = workspace().getVersion();
            }
            return _arith;
        } finally {
            workspace().doneReading();
        }
    }

    /** Returns an enumeration of arithmetic actors. This enumeration is
     *  locally cached. If workspace version equals to the cached version,
     *  then it returns the cached enumeration.
     *  Otherwise, it is reconstructed and cached.
     *  This method read-synchronizes on the workspace.
     *  @return An enumeration of arithmetic actors.
     *  @deprecated Use arithmaticActorList() instead.
     */
    public Enumeration arithmaticActors() {
        return Collections.enumeration(arithmaticActorList());
    }

    /** Returns a list of dynamic actors. This list is locally
     *  cached. If workspace version equals to the cached version,
     *  then it returns the cached list.
     *  Otherwise, it will be reconstructed and cached.
     *  This method read-synchronizes on the workspace.
     *  @return A list of dynamic actors.
     */
    public List dynamicActorList() {
        try {
	    workspace().getReadAccess();
            if(_wsversion != workspace().getVersion()) {
                _classifyActors();
                _wsversion = workspace().getVersion();
            }
            return _dynam;
        } finally {
            workspace().doneReading();
        }
    }


    /** Returns an enumeration of dynamic actors. This enumeration is locally
     *  cached. If workspace version equals to the cached version,
     *  then it returns the cached enumeration.
     *  Otherwise, it will be reconstructed and cached.
     *  This method read-synchronizes on the workspace.
     *  @return An enumeration of dynamic actors.
     *  @deprecated Use dynamicActorList() in stead.
     */
    public Enumeration dynamicActors() {
        return Collections.enumeration(dynamicActorList());
    }

    /** Returns a list of scheduled dynamic actors.
     *  This list is locally cached.
     *  If workspace version equals to the cached version,
     *  then it returns the cached list.
     *  Otherwise, it will be reconstructed and cached.
     *  The dynamic actor schedule lists all the integrators and
     *  dynamic actors
     *  in the reverse topology order. This order can be used
     *  for both implicit methods and explicit methods. For implicit
     *  method, the order is consistent with Gauss-Jacobi iteration.
     *  For explicit method,
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
            _wsversion = workspace().getVersion();
            return _stateschedule;
        } finally {
            workspace().doneReading();
        }
    }

    /** Returns an enumeration of the schedule of dynamic actors.
     *  This enumeration is locally cached.
     *  If workspace version equals to the cached version,
     *  then it returns the cached enumeration.
     *  Otherwise, it will be reconstructed and cached.
     *  The dynamic actor schedule lists all the integrators and
     *  dynamic actors
     *  in the reverse topology order. This order is considered safe
     *  for both implicit methods and explicit methods. For implicit
     *  method, the order is consistent with Gauss-Jacobi iteration.
     *  For explicit method,
     *  it guarantees that the input of the integrator is
     *  one step earlier than the output.
     *  This method read-synchronizes on the workspace.
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
     *  This list is locally  cached.
     *  If the workspace version equals to the cached version,
     *  then it returns the cached list.
     *  Otherwise, it reconstructs the list, and cache the new version.
     *  This method read-synchronizes on the workspace.
     *  @return The list of event generators.
     */
    public List eventGeneratorList() {
        try {
	    workspace().getReadAccess();
            if(_wsversion != workspace().getVersion()) {
                _classifyActors();
                _wsversion = workspace().getVersion();
            }
            return _evgen;
        } finally {
            workspace().doneReading();
        }
    }

    /** Return an enumeration of event generators.
     *  This enumeration is locally
     *  cached. If workspace version equals to the cached version,
     *  then it returns the cached enumeration.
     *  Otherwise, it reconstructs the enumeration, and save
     *  the new version.
     *  This method read-synchronizes on the workspace.
     *  @return An enumeration of event generator.
     *  @deprecated Use eventGeneratorList() instead.
     */
    public Enumeration eventGenerators() {
        return Collections.enumeration(eventGeneratorList());
    }

    /** Return a list of waveform generators in their creation order.
     *  This method read-synchronizes on the workspace.
     *  @return An enumeration of event interpreters.
     */
    public List waveformGeneratorList() {
        try {
	    workspace().getReadAccess();
            if(_wsversion != workspace().getVersion()) {
                _classifyActors();
                _wsversion = workspace().getVersion();
            }
            return _wavegen;
        } finally {
            workspace().doneReading();
        }
    }

    /** Return an enumeration of event interpreters.
     *  This enumeration is locally
     *  cached. If workspace version equals to the cached version,
     *  then it returns the cached enumeration.
     *  Otherwise, it reconstructs the enumeration, and save
     *  the new version.
     *  This method read-synchronizes on the workspace.
     *  @return An enumeration of event interpreters.
     *  @deprecated Use waveformGeneratorList() instead.
     */
    public Enumeration eventInterpreters() {
        return Collections.enumeration(waveformGeneratorList());
    }

    /** Return a list of step size control (SSC) actors in the output
     *  schedule. These are the step size control actors in the
     *  y = g(x, u, t) equations.
     *  These actors are in their creation order.
     *  This method read-synchronizes on the workspace.
     *  @return An enumeration of step size control actors.
     *  @exception IllegalActionException If topology is not schedulable.
     */
    public List outputSSCActorList() throws IllegalActionException {
        try {
	    workspace().getReadAccess();
            if(!isValid()) {
                schedule();
            }
            _wsversion = workspace().getVersion();
            return _outputssc;
        } finally {
            workspace().doneReading();
        }
    }

    /** Return an enumeration of step size control (SSC) actors in the output
     *  schedule. These are the step size control actors in the
     *  y = g(x, u, t) equations.
     *  This enumeration is locally
     *  cached. If workspace version equals to the cached version,
     *  then it returns the cached enumeration.
     *  Otherwise, it will be reconstructed and cached.
     *  This method read-synchronizes on the workspace.
     *  @return An enumeration of step size control actors.
     *  @exception IllegalActionException If thrown by the schedule() method.
     *  @deprecated Use outputSSCActorList() instead.
     */
    public Enumeration outputSSCActors() throws IllegalActionException {
        return Collections.enumeration(outputSSCActorList());
    }

    /** Returns a list of scheduled actors that form the output map.
     *  The output schedule lists all the actors in the computational
     *  path from
     *  integrators (or dynamic actors) to sink actors (or composite
     *  actor's output ports) in the topology order. The firing of
     *  of the actors in this order corresponds to produce the output
     *  of the system.
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
            _wsversion = workspace().getVersion();
            return _outputschedule;
        } finally {
            workspace().doneReading();
        }
    }

    /** Returns an enumeration of schedule actors that form the output map.
     *  The output schedule lists all the actors in the computational
     *  path from
     *  integrators (or dynamic actors) to sink actors (or composite
     *  actor's output ports) in the topology order. The firing of
     *  of the actors in this order corresponds to produce the output
     *  of the system.
     *  This method read-synchronize on the workspace.
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

    /** Return a list of sink actors (actors with no outputs).
     *  This method read-synchronize on the workspace.
     *  @return An enumeration of sinks.
     */
    public List sinkActorList() {
        try {
	    workspace().getReadAccess();
            if(_wsversion != workspace().getVersion()) {
                _classifyActors();
                _wsversion = workspace().getVersion();
            }
            return _sink;
        } finally {
            workspace().doneReading();
        }
    }

    /** Return an enumeration of sinks.
     *  This method read-synchronize on the workspace.
     *  @return An enumeration of sinks.
     *  @deprecated Use sinkActorList() instead.
     */
    public Enumeration sinkActors() {
        return Collections.enumeration(sinkActorList());
    }

    /** Return a list of stateful actors. Stateful actors are actors
     *  that has states. They implement the CTStatefulActor interface.
     *  This method read-synchronizes on the workspace.
     *  @return A list of stateful actors.
     */
    public List statefulActorList() {
        try {
	    workspace().getReadAccess();
            if(_wsversion != workspace().getVersion()) {
                _classifyActors();
                _wsversion = workspace().getVersion();
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
     *  control actors in the dx/dt = f(x, u, t) equations.
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
            _wsversion = workspace().getVersion();
            return _statessc;
        } finally {
            workspace().doneReading();
        }
    }

    /** Return an enumeration of step size control (SSC) actors in the
     *  state and state transition schedule. These are the step size
     *  control actors in the dx/dt = f(x, u, t) equations.
     *  @return An enumeration of step size control actors.
     *  @exception IllegalActionException If thrown by the schedule() method.
     *  @deprecated Use stateTransitionSSCActorList()
     */
    public Enumeration stateTransitionSSCActors()
            throws IllegalActionException {
        return Collections.enumeration(stateTransitionSSCActorList());
    }

    /** Returns a list of scheduled actors that form the state transition map.
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
            return _transitionschedule;
        } finally {
            workspace().doneReading();
        }
    }

    /** Returns an enumeration of the schedule of the state transition path.
     *  This enumeration is locally cached.
     *  If workspace version equals to the cached version,
     *  then it returns the cached enumeration.
     *  Otherwise, it calls _schedule to reconstruct, and save
     *  the new version.
     *  This method read-synchronize on the workspace.
     *  @return An enumeration of the schedule of the state transition path.
     *  @exception IllegalActionException If the scheduler has no container,
     *      or the container has no container.
     *  @exception NotSchedulableException If the system is not schedulable.
     */
    public Enumeration stateTransitionSchedule()
            throws NotSchedulableException, IllegalActionException {
        return Collections.enumeration(scheduledStateTransitionActorList());
    }

    /** Return all the schedule information.
     *  @return All the schedules.
     */
    public String toString() {
        String res = new String();
        Iterator iter;
        res += "CTSchedule {\n";
        res += "    arithmaticActors {\n";
        iter = arithmaticActorList().iterator();
        try {
            while(iter.hasNext()) {
                res += "\t" +
                    ((NamedObj)iter.next()).getFullName() + "\n";
            }
            res += "    }\n";
            res += "    dynamicActors {\n";
            iter = dynamicActorList().iterator();
            while(iter.hasNext()) {
                res += "\t" +
                    ((NamedObj)iter.next()).getFullName() + "\n";
            }
            res += "    }\n";
            res += "    stateTransitionSSCActors {\n";
            iter = stateTransitionSSCActorList().iterator();
            while(iter.hasNext()) {
                res += "\t" +
                    ((NamedObj)iter.next()).getFullName() + "\n";
            }
            res += "    }\n";
            res += "    outputSSCActors {\n";
            iter = outputSSCActorList().iterator();
            while(iter.hasNext()) {
                res += "\t" +
                    ((NamedObj)iter.next()).getFullName() + "\n";
            }
            res += "    }\n";
            res += "    eventGenerators {\n";
            iter = eventGeneratorList().iterator();
            while(iter.hasNext()) {
                res += "\t" +
                    ((NamedObj)iter.next()).getFullName() + "\n";
            }
            res += "    }\n";
            res += "    eventInterpreters {\n";
            iter = waveformGeneratorList().iterator();
            while(iter.hasNext()) {
                res += "\t" +
                    ((NamedObj)iter.next()).getFullName() + "\n";
            }
            res += "    }\n";
            res += "    statefulActors {\n";
            iter = statefulActorList().iterator();
            while(iter.hasNext()) {
                res += "\t" +
                    ((NamedObj)iter.next()).getFullName() + "\n";
            }
            res += "    }\n";
            res += "    sinkActors {\n";
            iter = sinkActorList().iterator();
            while(iter.hasNext()) {
                res += "\t" +
                    ((NamedObj)iter.next()).getFullName() + "\n";
            }
            res += "    }\n";
            res += "    dynamicActorSchedule {\n";
            iter = scheduledDynamicActorList().iterator();
            while(iter.hasNext()) {
                res += "\t" +
                    ((NamedObj)iter.next()).getFullName() + "\n";
            }
            res += "    }\n";
            res += "    stateTransitionSchedule {\n";
            iter = scheduledStateTransitionActorList().iterator();
            while(iter.hasNext()) {
                res += "\t" +
                    ((NamedObj)iter.next()).getFullName() + "\n";
            }
            res += "    }\n";
            res += "    outputSchedule {\n";
            iter = scheduledOutputActorList().iterator();
            while(iter.hasNext()) {
                res += "\t" +
                    ((NamedObj)iter.next()).getFullName() + "\n";
            }
            res += "    }\n";
            res += "}\n";
        } catch (IllegalActionException ex) {
            throw new InvalidStateException(this,
                    "Failed to generate CT schedule.");
        }
        return res;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Partition the system topology into clusters.
     *  Find out the arithmetic actors, dynamic actors, sink actors,
     *  event generators, event interpreters
     *  and stateful actors in the
     *  CompositeActor.
     */
    protected void _classifyActors() {
        if(_wsversion == -1) {
            _sink = new LinkedList();
            _dynam = new LinkedList();
            _arith = new LinkedList();
            _evgen = new LinkedList();
            _wavegen = new LinkedList();
            _stateful = new LinkedList();
        }else {
            _sink.clear();
            _dynam.clear();
            _arith.clear();
            _evgen.clear();
            _wavegen.clear();
            _stateful.clear();
        }

        CompositeActor ca = (CompositeActor) getContainer().getContainer();
        Iterator actors = ca.deepEntityList().iterator();
        while(actors.hasNext()) {
            Actor a = (Actor) actors.next();
            if (a instanceof CTStatefulActor) {
                _stateful.addLast(a);
            }
            if (a instanceof CTEventGenerator) {
                _evgen.addLast(a);
            }
            if (a instanceof CTWaveformGenerator) {
                _wavegen.addLast(a);
            }
            if (_successorList(a).isEmpty()) {
                _sink.addLast(a);
            }
            if (a instanceof CTDynamicActor) {
                _dynam.addLast(a);
            } else {
                _arith.addLast(a);
            }
            // Step size control (SSC) actors are classified according to
            // their location in the schedule. So they are assigned
            // in the _schedule() method.

            //FIXME: Should also do the following checks:
            // For each eventGenerator, its successors should either
            // be eventInterpreters or "Discrete (composite) actors."
            // For each "discrete (composite) actor," its predecessors
            // should be eventGenerators, and its successors should be
            // eventInterpreters. Waiting for the interface for
            // "discrete (composite) actor."
        }
    }

    /** Return the predecessor actors of the given actor in the topology.
     *  If the argument is null, returns null.
     *  If the actor is a source, returns null.
     *  @param The specified actor. If the actor is null, returns null.
     *  @return The list of predecessors, in their creation order.
     */
    protected List _predecessorList(Actor actor) {
        if(actor == null) {
            return null;
        }
        LinkedList pre = new LinkedList();
        Iterator inports = actor.inputPortList().iterator();
        while(inports.hasNext()) {
            IOPort inp = (IOPort) inports.next();
            Iterator outports = inp.deepConnectedOutPortList().iterator();
            while(outports.hasNext()) {
                IOPort outp = (IOPort) outports.next();
                ComponentEntity act = (ComponentEntity)outp.getContainer();
                if(!pre.contains(act)) {
                    pre.addLast(act);
                }
            }
        }
        return pre;
    }

    /** Return an Enumeration of the detail schedules. The first
     *  element in the Enumeration is the states schedule, then
     *  transition schedule, then output schedule, and then
     *  event generating schedule. Each schedule is
     *  an Enumeration of actors in their topological order.
     *  Each schedule can also be accessed by individual getXXSchedule
     *  method.
     * @return an Enumeration of sub-schedules.
     * @exception NotSchedulableException either the system construction
     * is wrong or arithmetic loop exists.
     */
    protected Enumeration _schedule() throws NotSchedulableException {
        CTDirector dir = (CTDirector)getContainer();
        if(dir == null) {
            return null;
        }
        CompositeActor ca = (CompositeActor)(dir.getContainer());
        if(ca == null) {
            return null;
        }
        _stateschedule = new LinkedList();
        _transitionschedule = new LinkedList();
        _outputschedule = new LinkedList();
        _statessc = new LinkedList();
        _outputssc = new LinkedList();
        LinkedList _scheList = new LinkedList();

        _classifyActors();
        DirectedAcyclicGraph g =  _toArithGraph(ca.deepEntityList());
        DirectedAcyclicGraph gd = _toGraph(dynamicActorList());
        if(!g.isAcyclic()) {
            throw new NotSchedulableException("Arithmetic loop found.");
        }
        int numofdyn = _dynam.size();
        if(numofdyn > 0) {
            Object[] dynactors = _dynam.toArray();

            // Dynamic actors are reverse ordered.
            Object[] xsort = gd.topologicalSort(dynactors);
            for(int i = 0; i < xsort.length; i++) {
                Actor a = (Actor)xsort[i];
                _stateschedule.addFirst(a);
                if (a instanceof CTStepSizeControlActor) {
                    // Note: they are not ordered, but addFirst() is
                    // considered more efficient.
                    _statessc.addFirst(a);
                }
            }
            _scheList.addLast(_stateschedule);

            // State transition map
            Object[] fx = g.backwardReachableNodes(dynactors);
            Object[] fxsort = g.topologicalSort(fx);
            for(int i = 0; i < fxsort.length; i++) {
                Actor a = (Actor)fxsort[i];
                _transitionschedule.addLast(a);
                if (a instanceof CTStepSizeControlActor) {
                    // Note: they are not ordered, but addFirst() is
                    // considered more efficient.
                    _statessc.addFirst(a);
                }
            }
            _scheList.addLast(_transitionschedule);
        }

        // construct an array of sink actors.
        int numofsink = _sink.size();

        if(numofsink > 0) {
            Object[] sinkactors =  _sink.toArray();

            //Output map.
            Object[] gx = g.backwardReachableNodes(sinkactors);
            Object[] gxsort = g.topologicalSort(gx);
            for(int i = 0; i < gxsort.length; i++) {
                Actor a = (Actor)gxsort[i];
                _outputschedule.addLast(a);
                if (a instanceof CTStepSizeControlActor) {
                    _outputssc.addLast(a);
                }
            }
            // add sinks to the output schedule
            for (int i = 0; i < sinkactors.length; i++) {
                Actor a = (Actor)sinkactors[i];
                _outputschedule.addLast(a);
                if (a instanceof CTStepSizeControlActor) {
                    _outputssc.addLast(a);
                }
            }

            _scheList.addLast(_outputschedule);
        }

        return Collections.enumeration(_scheList);
    }

    /** Return the successor actors of the given actor in the topology.
     *  If the argument is null, returns null.
     *  If the actor is a sink, returns null.
     *  @param The specified actor. If the actor is null, returns null.
     *  @return The enumerations of predecessors.
     */
    protected List _successorList(Actor actor) {
        if(actor == null) {
            return null;
        }
        LinkedList post = new LinkedList();
        Iterator outports = actor.outputPortList().iterator();
        while(outports.hasNext()) {
            IOPort outp = (IOPort) outports.next();
            Iterator inports = outp.deepConnectedInPortList().iterator();
            while(inports.hasNext()) {
                IOPort inp = (IOPort)inports.next();
                ComponentEntity act = (ComponentEntity)inp.getContainer();
                if(!post.contains(act)) {
                    post.addLast(act);
                }
            }
        }
        return post;
    }

    /** Convert the given actors to a directed acyclic graph.
     *  CTDynamicActors are treated as sinks.
     *  Each actor
     *  in the given enumeration is a node in the graph,
     *  each link between a pair
     *  of actors is a edge between the
     *  corresponding nodes unless the source node is a dynamic actor.
     *  The existence of the director and containers is not checked
     *  in this method, so the caller should check.
     *  @param actorlist The list of actors to be scheduled.
     *  @return A graph representation of the actors.
     */
    protected DirectedAcyclicGraph _toArithGraph(List actorlist) {
        CTDirector dir = (CTDirector)getContainer();
        CompositeActor ca = (CompositeActor)(dir.getContainer());

        DirectedAcyclicGraph g = new DirectedAcyclicGraph();
        // Create the nodes.
        // The actors comes from deepEntityList, so it is impossible
        // that one actor can occur twice in the Enumeration. So no
        // exceptions are caught.
        //LinkedList actorlist = new LinkedList();
        Iterator actors = actorlist.iterator();
        while (actors.hasNext()) {
            Actor a = (Actor)actors.next();
            g.add(a);
            //            actorlist.addLast(a);
        }

        // Create the edges.
        Iterator allactors = actorlist.iterator();
        while (allactors.hasNext()) {
            Actor a = (Actor) allactors.next();

            if(!(a instanceof CTDynamicActor)) {
                // Find the successors of a
                Iterator successors = _successorList(a).iterator();
                while (successors.hasNext()) {
                    Actor s = (Actor) successors.next();
                    if(actorlist.contains(s)) {
                        g.addEdge(a, s);
                    }
                }
            }
        }
	return g;
    }

    /** Convert the given actors to a directed acyclic graph.
     *  CTDynamicActors are NOT treated as sinks.
     *  Each actor
     *  in the given enumeration is a node in the graph,
     *  each link between a pair
     *  of actors is a edge between the
     *  corresponding nodes unless the source node is a dynamic actor.
     *  The existence of the director and containers is not checked
     *  in this method, so the caller should check.
     *  @param actorlist The list of actors to be converted to a graph.
     *  @return A graph representation of the actors.
     */
    protected DirectedAcyclicGraph _toGraph(List actorlist) {
        CTDirector dir = (CTDirector)getContainer();
        CompositeActor ca = (CompositeActor)(dir.getContainer());

        DirectedAcyclicGraph g = new DirectedAcyclicGraph();
        // Create the nodes.
        // The actors comes from deepEntityList, so it is impossible
        // that one actor can occur twice in the Enumeration. So no
        // exceptions are caught.
        Iterator actors = actorlist.iterator();
        while (actors.hasNext()) {
            Actor a = (Actor)actors.next();
            g.add(a);
        }

        // Create the edges.
        actors = actorlist.iterator();
        while (actors.hasNext()) {
            Actor a = (Actor) actors.next();

            // Find the successors of a
            Iterator successors = _successorList(a).iterator();
            while (successors.hasNext()) {
                Actor s = (Actor) successors.next();
                if(actorlist.contains(s)) {
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
    // schedule version
    private LinkedList _stateschedule;
    private LinkedList _transitionschedule;
    private LinkedList _outputschedule;

    // A LinkedList of the source actors.
    private transient LinkedList _dynam;
    // A LinkedList of the sink actors.
    private transient LinkedList _sink;
    // A LikedList of the arithmetic (Nondynamic) actors.
    private transient LinkedList _arith;
    // A linkedLost of SSC actors in the state and transition schedule.
    private transient LinkedList _statessc;
    // A linkedLost of SSC actors in the state and transition schedule.
    private transient LinkedList _outputssc;
    // A linkedLost of event generators.
    private transient LinkedList _evgen;
    // A linkedLost of event interpreters.
    private transient LinkedList _wavegen;
    // A linkedLost of stateful actors.
    private transient LinkedList _stateful;

    // Version of the lists.
    private transient long _wsversion = -1;
}
