/* A topological sort scheduler for continuous time simulation.

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

@ProposedCodeRate red (liuj@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.kernel;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.actor.sched.*;
import ptolemy.graph.*;
import java.util.*;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// CTScheduler
/**
Clustered graph sorting scheduler for CT domain. 
A CT (sub)system can be represented mathematiclly as:<Br>
    dx/dt = f(x, u, t)<Br>
    y = g(x, u, t)<BR>
    e: h(x, u, t) = 0<BR>
<P>
The system is built by actors. That is, all the functions, f(), g(), 
and h() are built by chains of actors. The scheduler will cluster and 
sort the system topology, and will provide the firing order for 
evaluating f(), g() and h(). The firing order for evaluating f() is 
called the <I> state transition schedule</I>; the firing order for
evaluating g() is called the <I> output schedule</I>; the firing 
order for h() is called the <I>event generation schedule</I>;
the firing order for all the dynamic actors is called the 
<I>dynamic actor schedule</I>.
<P>
To help the scheduling, a system topology is partitioned into
several clusters:
the <I>aritmatic actors</I>, the <I>dynamic actors</I>, 
the <I>error control actors</I>, the <I>sink actors</I>,
the <I>memaris actors</I>, and the <I> event generate actors</I>
<P>
The state schedule is a list of dynamic actors (integrators or actors
that can produce initial token) which are sorted backward.
<P>
The state transition schedule is the actors in f() function sorted
in the topological order, such that, after the integrators emit their
state x, a chain of firings according to the schedule evaluates the
f() function and returns a token corresponding to dx/dt to the 
integrators.
<P>
The output schedule is the actors in g() function sorted in the topological
order. 
<P>
The event generation schedule is the actors in the h() function sorted
in the topological order.

If thers are loops of arithmatic actors or loops of integrators,
then the (sub)system is not schedulable, and a NotSchedulableException
will be thrown.

@author Jie Liu
@version $Id$
@see ptolemy.actor.Scheduler
*/

public class CTScheduler extends Scheduler{

    /** Construct a CT scheduler in the defaul workspace
     *  with the default name "CTScheduler".
     */
    public CTScheduler() {
        super();
        try {
            setName(_staticname);
        } catch (NameDuplicationException ex) {
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
            setName(_staticname);
        } catch (NameDuplicationException ex) {
            throw new InternalErrorException(
                "Internal error when setName to a CTScheduler");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /** Returns an enumeration of arithmetic actors. This enumeration is
     *  locally cached. If workspace version equals to the cached version,
     *  then it returns the cached enumeration.
     *  Otherwise, it is reconstructed and cached.
     *  This method read-synchronizes on the workspace.
     *  @return An enumeration of arithmetic actors.
     */
    public Enumeration arithmaticActors() {
        try {
	    workspace().getReadAccess();
            if(_dynamicversion != workspace().getVersion()) {
                _classifyActors();
                _dynamicversion = workspace().getVersion();
            }
            return _arith.elements();
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
     */
    public Enumeration dynamicActors() {
        try {
	    workspace().getReadAccess();
            if(_dynamicversion != workspace().getVersion()) {
                _classifyActors();
                _dynamicversion = workspace().getVersion();
            }
            return _dynam.elements();
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
     */
    public Enumeration dynamicActorSchedule()
        throws NotSchedulableException, IllegalActionException {
        try {
            workspace().getReadAccess();
            if(!valid()) {
                schedule();
            }
            return _stateschedule.elements();
        } finally {
            workspace().doneReading();
        }
    }

    /** Return an enumeration of error control actors.
     *  This enumeration is locally
     *  cached. If workspace version equals to the cached version,
     *  then it returns the cached enumeration.
     *  Otherwise, it will be reconstructed and cached.
     *  This method read-synchronize on the workspace.
     *  @return An enumeration of error control actors.
     *  FIXME: Deprecated!
     *  @deprecated Use stepSizeControlActors instead.
     */
    public Enumeration errorControlActors() {
        try {
	    workspace().getReadAccess();
            if(_dynamicversion != workspace().getVersion()) {
                _classifyActors();
                _dynamicversion = workspace().getVersion();
            }
            return _ectrl.elements();
        } finally {
            workspace().doneReading();
        }
    }
   
    /** Return an enumeration of step size control actors.
     *  This enumeration is locally
     *  cached. If workspace version equals to the cached version,
     *  then it returns the cached enumeration.
     *  Otherwise, it will be reconstructed and cached.
     *  This method read-synchronizes on the workspace.
     *  @return An enumeration of step size control actors.
     * 
     */
    public Enumeration stepSizeControlActors() {
        try {
	    workspace().getReadAccess();
            if(_dynamicversion != workspace().getVersion()) {
                _classifyActors();
                _dynamicversion = workspace().getVersion();
            }
            return _ssctrl.elements();
        } finally {
            workspace().doneReading();
        }
    }

    /** Return an enumeration of event generator.
     *  This enumeration is locally
     *  cached. If workspace version equals to the cached version,
     *  then it returns the cached enumeration.
     *  Otherwise, it calls _classifyActors to reconstruct, and save
     *  the new version.
     *  This method read-synchronize on the workspace.
     *  @return An enumeration of event generator.
     *  FIXME: deprecated.
     *  @deprecated Use eventGenerators
     */
    public Enumeration eventGenerateActors() {
        try {
	    workspace().getReadAccess();
            if(_dynamicversion != workspace().getVersion()) {
                _classifyActors();
                _dynamicversion = workspace().getVersion();
            }
            return _evdct.elements();
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
     */
    public Enumeration eventGenerators() {
        try {
	    workspace().getReadAccess();
            if(_dynamicversion != workspace().getVersion()) {
                _classifyActors();
                _dynamicversion = workspace().getVersion();
            }
            return _evgen.elements();
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
     */
    public Enumeration eventInterpreters() {
        try {
	    workspace().getReadAccess();
            if(_dynamicversion != workspace().getVersion()) {
                _classifyActors();
                _dynamicversion = workspace().getVersion();
            }
            return _evint.elements();
        } finally {
            workspace().doneReading();
        }
    }

    /** Return an enumeration of the actors in the event detection
     *  path in the topological order.
     *  This enumeration is locally cached.
     *  If workspace version equals to the cached version,
     *  then it returns the cached enumeration.
     *  Otherwise, it calls _schedule to reconstruct, and save
     *  the new version.
     *  This method read-synchronize on the workspace.
     *  @return An enumeration of the actors in the event detection
     *  path in the topological order.
     *  @exception IllegalActionException If the scheduler has no container,
     *      or the container has no container.
     *  @exception NotSchedulableException If the system is not schedulable.
     *  FIXME: Deprecated!
     *  @deprecated Use eventGeneratingSchedule()
     */
    public Enumeration eventGenerationSchedule()
            throws NotSchedulableException, IllegalActionException {
        try {
	    workspace().getReadAccess();
            if(!valid()) {
                schedule();
            }
            return _eventschedule.elements();
        } finally {
            workspace().doneReading();
        }
    }

    /** Return an enumeration of the actors in the event generation
     *  path in the topological order.
     *  This enumeration is locally cached.
     *  If workspace version equals to the cached version,
     *  then it returns the cached enumeration.
     *  Otherwise, it will be reconstructed and cached.
     *  This method read-synchronizes on the workspace.
     *  @return An enumeration of the actors in the event generation
     *  path in the topological order.
     *  @exception IllegalActionException If the scheduler has no container,
     *      or the container has no container.
     *  @exception NotSchedulableException If the system is not schedulable.
     */
    public Enumeration eventGeneratingSchedule()
            throws NotSchedulableException, IllegalActionException {
        try {
	    workspace().getReadAccess();
            if(!valid()) {
                schedule();
            }
            return _eventschedule.elements();
        } finally {
            workspace().doneReading();
        }
    }

    /** Return an enumeration of Memaris actors.
     *  This enumeration is locally
     *  cached. If workspace version equals to the cached version,
     *  then it returns the cached enumeration.
     *  Otherwise, it will be reconstructed and cached.
     *  This method read-synchronizes on the workspace.
     *  @return An enumeration of Memaris actors.
     *  FIXME: Deprecated!
     *  @deprecated Use statefulActors() in stead.
     */
    public Enumeration memarisActors() {
        try {
	    workspace().getReadAccess();
            if(_dynamicversion != workspace().getVersion()) {
                _classifyActors();
                _dynamicversion = workspace().getVersion();
            }
            return _memaris.elements();
        } finally {
            workspace().doneReading();
        }
    }
 
    /** Return an enumeration of stateful actors.
     *  This enumeration is locally
     *  cached. If workspace version equals to the cached version,
     *  then it returns the cached enumeration.
     *  Otherwise, it will be reconstructed and cached.
     *  This method read-synchronizes on the workspace.
     *  @return An enumeration of Memaris actors.
     */
    public Enumeration statefulActors() {
        try {
	    workspace().getReadAccess();
            if(_dynamicversion != workspace().getVersion()) {
                _classifyActors();
                _dynamicversion = workspace().getVersion();
            }
            return _stateful.elements();
        } finally {
            workspace().doneReading();
        }
    }

    /** Returns an enumeration of the schedule of the output path.
     *  This enumeration is locally cached.
     *  If workspace version equals to the cached version,
     *  then it returns the cached enumeration.
     *  Otherwise, it calls _schedule to reconstruct, and save
     *  the new version.
     *  The output schedule lists all the actors in the path from
     *  integrators (or dynamic actors) to sink actors (or composite
     *  actor's output ports) in the topology order.
     *  This method read-synchronize on the workspace.
     *  @return An enumeration of the schedule of the output path.
     *  @exception IllegalActionException If the scheduler has no container,
     *      or the container has no container.
     *  @exception NotSchedulableException If the system is not schedulable.
     */
    public Enumeration outputSchedule()
        throws NotSchedulableException, IllegalActionException  {
        try {
	    workspace().getReadAccess();
            if(!valid()) {
                schedule();
            }
            return _outputschedule.elements();
        } finally {
            workspace().doneReading();
        }
    }

    /** Return an enumeration of sinks. This enumeration is locally
     *  cached. If workspace version equals to the cached version,
     *  then it returns the cached enumeration.
     *  Otherwise, it calls _classifyActors to reconstruct, and save
     *  the new version.
     *  This method read-synchronize on the workspace.
     *  @return An enumeration of sinks.
     */
    public Enumeration sinkActors() {
        try {
	    workspace().getReadAccess();
            if(_dynamicversion != workspace().getVersion()) {
                _classifyActors();
                _dynamicversion = workspace().getVersion();
            }
            return _sink.elements();
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
        try {
	    workspace().getReadAccess();
            if(!valid()) {
                schedule();
            }
            return _transitionschedule.elements();
        } finally {
            workspace().doneReading();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Partition the system topology into clusters.
     *  Find out the arithmetic actors, dynamic actors, sink actors
     *  error control actors, event generators, and memaris actorsin the
     *  CompositeActor.
     */
    protected void _classifyActors() {
        if(_dynamicversion == -1) {
            _sink = new LinkedList();
            _dynam = new LinkedList();
            _arith = new LinkedList();
            _ectrl = new LinkedList();  //deprecated.
            _evdct = new LinkedList();
            _memaris = new LinkedList();
            _ssctrl = new LinkedList(); 
            _evgen = new LinkedList(); 
            _evint = new LinkedList(); 
            _stateful = new LinkedList(); 
        }else {
            _sink.clear();
            _dynam.clear();
            _arith.clear();
            _ectrl.clear();  // deprecated.
            _evdct.clear();
            _memaris.clear();
            _ssctrl.clear();
            _evgen.clear();
            _evint.clear();
            _stateful.clear();
        }

        CompositeActor ca = (CompositeActor) getContainer().getContainer();
        Enumeration actors = ca.deepGetEntities();
        while(actors.hasMoreElements()) {
            Actor a = (Actor) actors.nextElement();
            if (a instanceof CTErrorControlActor) {   //deprecated
                _ectrl.insertLast(a);
            }
            if (a instanceof CTEventGenerateActor) {   //deprecated
                _evdct.insertLast(a);
            }
            if (a instanceof CTMemarisActor) {  //deprecated.
                _memaris.insertLast(a);
            }
            if (a instanceof CTStatefulActor) {
                _stateful.insertLast(a);
            }
            if (a instanceof CTEventGenerator) {
                _evgen.insertLast(a);
            }
            if (a instanceof CTEventInterpreter) {
                _evint.insertLast(a);
            }
            if (a instanceof CTStepSizeControlActor) {
                _ssctrl.insertLast(a);
            }
            if (!(_successors(a).hasMoreElements())) {
                // previously !((a.outputPorts()).hasMoreElements())) {
                _sink.insertLast(a);
            }
            if (a instanceof CTDynamicActor) {
                _dynam.insertLast(a);
            } else {
                _arith.insertLast(a);
            }
            //FIXME: Sould also do the following checks:
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
     *  @return The enumerations of predecessors,  unordered.
     */
     public Enumeration _predecessors(Actor actor) {
         if(actor == null) {
             return null;
         }
         LinkedList pre = new LinkedList();
         Enumeration inports = actor.inputPorts();
         while(inports.hasMoreElements()) {
             IOPort inp = (IOPort) inports.nextElement();
             Enumeration outports = inp.deepConnectedOutPorts();
             while(outports.hasMoreElements()) {
                 IOPort outp = (IOPort) outports.nextElement();
                 ComponentEntity act = (ComponentEntity)outp.getContainer();
                 if(!pre.includes(act)) {
                     pre.insertLast(act);
                 }
             }
         }
         return pre.elements();
     }

    /** return an Enumeration of the detail schedules. The first
     *  element in the Enumeration is the stateschedule, then
     *  transitionschedule, then output schedule, and then
     *  event generating schedule. Each schedule is
     *  an Enumeration of actors in their topological order.
     *  Each schedule can also be accessed by individual getXXSchedule
     *  method.
     * @return an Enumeration of sub-schedules.
     * @exception NotSchedulableException either the system construction
     * is wrong or arithmetic loop exists.
     */
    protected Enumeration _schedule() throws NotSchedulableException {
        CTDirector dir =(CTDirector)getContainer();
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
        // _eventschedule = new LinkedList(); //deprecated
        LinkedList _scheList = new LinkedList();

        DirectedAcyclicGraph g =  _toGraph(ca.deepGetEntities());
        if(!g.isAcyclic()) {
            throw new NotSchedulableException("Arithmetic loop found.");
        }
        _classifyActors();
        // construct an array of dynamic actors.
        int numofdyn = _dynam.size();
        if(numofdyn > 0) {
            Object[] dynactors = new Object[numofdyn];
            Enumeration enumdynactors = _dynam.elements();
            int count = 0;
            while (enumdynactors.hasMoreElements()) {
                dynactors[count++] = enumdynactors.nextElement();
            }

            // Dynamic actors are reverse ordered.
            Object[] xsort = g.topologicalSort(dynactors);
            for(int i=0; i < xsort.length; i++) {
                _stateschedule.insertFirst((Actor)xsort[i]);
            }
            _scheList.insertLast(_stateschedule);

            // State transition map
            Object[] fx = g.backwardReachableNodes(dynactors);
            Object[] fxsort = g.topologicalSort(fx);
            for(int i=0; i < fxsort.length; i++) {
                _transitionschedule.insertLast(fxsort[i]);
            }
            _scheList.insertLast(_transitionschedule);
        }

        // construct an array of sink actors.
        int numofsink = _sink.size();
       
        if(numofsink > 0) {
            Object[] sinkactors = new Object[numofsink];
            Enumeration enumsinks = _sink.elements();
            int count = 0;
            while(enumsinks.hasMoreElements()) {
                sinkactors[count++] = enumsinks.nextElement();
            }
            //Output map.
            Object[] gx = g.backwardReachableNodes(sinkactors);
            Object[] gxsort = g.topologicalSort(gx);
            for(int i=0; i < gxsort.length; i++) {
                _outputschedule.insertLast(gxsort[i]);
            }
            // add sinks to the output schedule
            _outputschedule.appendElements(_sink.elements());
            _scheList.insertLast(_outputschedule);
        }

        /* construct an array of event detectors.
        int numofevdct = _evdct.size();
 
        if(numofevdct > 0) {
            Object[] eventgeneraters = new Object[numofevdct];
            Enumeration enumevdct = _evdct.elements();
            int count = 0;
            while(enumevdct.hasMoreElements()) {
                eventgeneraters[count++] = enumevdct.nextElement();
            }
            // Event detection map.
            Object[] hx = g.backwardReachableNodes(eventgeneraters);
            Object[] hxsort = g.topologicalSort(hx);
            for(int i=0; i < hxsort.length; i++) {
                _eventschedule.insertLast(hxsort[i]);
            }
            _eventschedule.appendElements(_evdct.elements());
            _scheList.insertLast(_outputschedule);
        }
        */
        return _scheList.elements();
    }

    /** Return the successor actors of the given actor in the topology.
     *  If the argument is null, returns null.
     *  If the actor is a sink, returns null. 
     *  @param The specified actor. If the actor is null, returns null.
     *  @return The enumerations of predecessors.
     */
     public Enumeration _successors(Actor actor) {
         if(actor == null) {
             return null;
         }
         LinkedList post = new LinkedList();
         Enumeration outports = actor.outputPorts();
         while(outports.hasMoreElements()) {
             IOPort outp = (IOPort) outports.nextElement();
             Enumeration inports = outp.deepConnectedInPorts();
             while(inports.hasMoreElements()) {
                 IOPort inp = (IOPort)inports.nextElement();
                 ComponentEntity act = (ComponentEntity)inp.getContainer();
                 if(!post.includes(act)) {
                     post.insertLast(act);
                 }
             }
         }
         return post.elements();
     }

    /** Convert the given actors to a directed acyclic graph.
     *  CTDynamicActors are treated as sinks.
     *  Each actor
     *  in the given enumeration is a node in the graph, 
     *  each link between a pair
     *  of actors is a edge between the
     *  corresponding nodes unless the source node is a dynamic actor.
     *  The presentance of the director and containers is not checked
     *  in this method, so the caller should check.
     *  @return A graph representation of the actors.
     */
    protected DirectedAcyclicGraph _toGraph(Enumeration actors) {
        CTDirector dir =(CTDirector)getContainer();
        CompositeActor ca = (CompositeActor)(dir.getContainer());

        DirectedAcyclicGraph g = new DirectedAcyclicGraph();
        // Create the nodes.
        // The actors comes from deepGetEntities, so it is impossible
        // that one actor can occur twice in the Enumeration. So no
        // exceptions are catched.
        LinkedList actorlist = new LinkedList();
        while (actors.hasMoreElements()) {
            AtomicActor a = (AtomicActor)actors.nextElement();
            g.add(a);
            actorlist.insertLast(a);
        }

        // Create the edges.
        Enumeration allactors = actorlist.elements();
        while (allactors.hasMoreElements()) {
            Actor a = (Actor) allactors.nextElement();
 
            if(!(a instanceof CTDynamicActor)) {
                // Find the successors of a
                Enumeration successors = _successors(a);
                while (successors.hasMoreElements()) {
                    Actor s = (Actor) successors.nextElement();
                    if(actorlist.includes(s)) {
                        g.addEdge(a, s);
                    }
                }
            }
        }
	return g;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The static name of the scheduler.
    private static final String _staticname = "CTScheduler";
    // schedule version
    private LinkedList _stateschedule;
    private LinkedList _transitionschedule;
    private LinkedList _outputschedule;
    private LinkedList _eventschedule;

    // A LinkedList of the source actors.
    private transient LinkedList _dynam;
    // A LinkedList of the sink actors.
    private transient LinkedList _sink;
    // A LikedList of the arithmetic (Nondynamic) actors.
    private transient LinkedList _arith;
    // A linkedList of the error control actors.
    private transient LinkedList _ectrl;
    // A linkedList of event detector.
    private transient LinkedList _evdct;
    // A linkedLost of memaris actors.
    private transient LinkedList _memaris;
    // A linkedLost of step size control actors.
    private transient LinkedList _ssctrl;
    // A linkedLost of event generators.
    private transient LinkedList _evgen;
    // A linkedLost of event interpreters.
    private transient LinkedList _evint;
    // A linkedLost of stateful actors.
    private transient LinkedList _stateful;

    // Version of the lists.
    private transient long _dynamicversion = -1;
}
