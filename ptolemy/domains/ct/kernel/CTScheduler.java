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
//// CTClusterScheduler
/**
A CT (sub)system can be represented mathematiclly as:
    dx/xt = f(x, u, t)
    y = g(x, u, t)
    e: h(x, u, t) = 0

A culster graph sort scheduler for continuous time simulation sorts
the graph and manage of list of schedules, for states, state transitions,
outputs, and event detections.
The state schedule is a list of dynamic actors (integrators or actors
that can produce initial token) which are sorted backward to avoid
casaulity mismatch.
The state transition schedule is the actors in f() function sorted
in the topological order. It is acheived by sorting backward from the
inputs of dynamic actors until a source or an output of a dynamic actor
is found. Dynamic actors are not in this schedule.

The output schedule is the actors in g() function sorted in the topological
order. It is acheived by sorting backward from the sinks, traces back until
a dynamic actor or a source is reached. The dynamic actors is not in the
schedule.

The event detection schedule is the actors in the h() function sorted
in the topological order. It is acheived by sorting bakward from the
event detector's inputs, traces back to a dynamic actor or a source.
dynamic actors are not in this list.

If thers are loops of non-dynamic actors or loops of integrators,
then the subsystem is not schedulable, and a NotSchedulableException
will be throw by the schedule(), stateSchdule() and outputSchedule() methods.

@author Jie Liu
@version $Id$
@see ptolemy.actor.Scheduler
*/

public class CTScheduler extends Scheduler{

    public static final boolean VERBOSE = false;
    public static final boolean DEBUG = false;

    /** Construct a CT topological sort schduler in the defaul workspace
     *  with the default name "CT Topological Sort Scheduler".
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

    /** Construct a CT topological sort schduler in the given workspace
     *  with the name "CT Topological Sort Scheduler".
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
     *  Otherwise, it calls _classifyActors to reconstruct, and save
     *  the new version.
     *  This method read-synchronize on the workspace.
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
     *  Otherwise, it calls _classifyActors to reconstruct, and save
     *  the new version.
     *  This method read-synchronize on the workspace.
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

    /** Return an enumeration of error control actors.
     *  This enumeration is locally
     *  cached. If workspace version equals to the cached version,
     *  then it returns the cached enumeration.
     *  Otherwise, it calls _classifyActors to reconstruct, and save
     *  the new version.
     *  This method read-synchronize on the workspace.
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

    /** Return an enumeration of event detectors.
     *  This enumeration is locally
     *  cached. If workspace version equals to the cached version,
     *  then it returns the cached enumeration.
     *  Otherwise, it calls _classifyActors to reconstruct, and save
     *  the new version.
     *  This method read-synchronize on the workspace.
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

    /** Return an enumeration of Memaris actors
     *  This enumeration is locally
     *  cached. If workspace version equals to the cached version,
     *  then it returns the cached enumeration.
     *  Otherwise, it calls _classifyActors to reconstruct, and save
     *  the new version.
     *  This method read-synchronize on the workspace.
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

    /** Return an enumeration of sinks. This enumeration is locally
     *  cached. If workspace version equals to the cached version,
     *  then it returns the cached enumeration.
     *  Otherwise, it calls _classifyActors to reconstruct, and save
     *  the new version.
     *  This method read-synchronize on the workspace.
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

    /** Returns an enumeration of the schedule of dynamic actors.
     *  This enumeration is locally cached.
     *  If workspace version equals to the cached version,
     *  then it returns the cached enumeration.
     *  Otherwise, it calls _schedule to reconstruct, and save
     *  the new version.
     *  The state schedule lists all the integrators and dynamic actors
     *  in the reverse topology order. This order is considered safe
     *  for both implicit methods and explicit methods. For implicit
     *  method, the order is irrelavant. But for explicit method,
     *  we should guarantee that the input of the integrator is
     *  one step earlier than the output.
     *  This method read-synchronize on the workspace.
     */
    public Enumeration dynamicActorSchedule()
        throws NotSchedulableException, IllegalActionException {
        try {
            workspace().getReadAccess();
            if(!valid()) {
                if(DEBUG) {
                    System.out.println("The schedule is not valid" +
                        " when calling dynamicActorSchedule().");
                }
                schedule();
            }
            return _stateschedule.elements();
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
     */
    public Enumeration outputSchedule()
        throws NotSchedulableException, IllegalActionException  {
        try {
	    workspace().getReadAccess();
            if(!valid()) {
                if(DEBUG) {
                    System.out.println("The schedule is not valid" +
                        " when calling outputSchedule().");
                }
                schedule();
            }
            return _outputschedule.elements();
        } finally {
            workspace().doneReading();
        }
    }

    /** Return the actors in the event detection path in the
     *  topological order.
     */
    public Enumeration eventGenerationSchedule()
            throws NotSchedulableException, IllegalActionException {
        try {
	    workspace().getReadAccess();
            if(!valid()) {
                if(DEBUG) {
                    System.out.println("The schedule is not valid" +
                        " when calling eventGenerationSchedule().");
                }
                schedule();
            }
            return _eventschedule.elements();
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
     */
    public Enumeration stateTransitionSchedule()
            throws NotSchedulableException, IllegalActionException {
        try {
	    workspace().getReadAccess();
            if(!valid()) {
                if(DEBUG) {
                    System.out.println("The schedule is not valid" +
                        " when calling stateTransitionSchedule().");
                }
                schedule();
            }
            return _transitionschedule.elements();
        } finally {
            workspace().doneReading();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Find out the dynamic actors and sink actors in the
     *  CompositeActor.
     */
    protected void _classifyActors() {
        if(VERBOSE) {
            System.out.println("in _classifyActors");
        }
        if(_dynamicversion == -1) {
            _sink = new LinkedList();
            _dynam = new LinkedList();
            _arith = new LinkedList();
            _ectrl = new LinkedList();
            _evdct = new LinkedList();
            _memaris = new LinkedList();
        }else {
            _sink.clear();
            _dynam.clear();
            _arith.clear();
            _ectrl.clear();
            _evdct.clear();
            _memaris.clear();
        }

        CompositeActor ca = (CompositeActor) getContainer().getContainer();
        Enumeration actors = ca.deepGetEntities();
        while(actors.hasMoreElements()) {
            Actor a = (Actor) actors.nextElement();
            if (a instanceof CTErrorControlActor) {
                _ectrl.insertLast(a);
            }
            if (a instanceof CTEventGenerateActor) {
                _evdct.insertLast(a);
            }
            if (a instanceof CTMemarisActor) {
                _memaris.insertLast(a);
            }
            if (!((a.outputPorts()).hasMoreElements())) {
                _sink.insertLast(a);
            }
            if (a instanceof CTDynamicActor) {
                _dynam.insertLast(a);
            } else {
                _arith.insertLast(a);
            }
        }
    }

    /** return an Enumeration of the detail schedules. The first
     *  element in the Enumeration is the stateschedule, then
     *  transitionschedule, then output schedule. Each schedule is
     *  an Enumeration of actors in their topological order.
     * @return an Enumeration of sub-schdules.
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
        _eventschedule = new LinkedList();
        LinkedList _scheList = new LinkedList();

        DirectedAcyclicGraph g =  _toGraph(ca.deepGetEntities());
        if(!g.isAcyclic()) {
            throw new NotSchedulableException("Arithmetic loop found.");
        }
        _classifyActors();
        // construct an array of dynamic actors.
        int numofdyn = _dynam.size();
        if(DEBUG) {
            System.out.println("Number of Dynamic Actors:"+numofdyn);
        }
        if(numofdyn > 0) {
            Object[] dynactors = new Object[numofdyn];
            Enumeration enumdynactors = _dynam.elements();
            int count = 0;
            while (enumdynactors.hasMoreElements()) {
                dynactors[count++] = enumdynactors.nextElement();
            }

            // Dynamic actors are reverse ordered.
            Object[] xsort = g.topologicalSort(dynactors);
            if(DEBUG) {
                System.out.println("Num of dynamic actors after sort: "+
                    xsort.length);
            }
            for(int i=0; i < xsort.length; i++) {
                _stateschedule.insertFirst((Actor)xsort[i]);
            }
            if(VERBOSE) {
                System.out.println("state schedule constructed.");
            }
            _scheList.insertLast(_stateschedule);

            // State transition map
            Object[] fx = g.backwardReachableNodes(dynactors);
            if(DEBUG) {
                System.out.println("Number of reachable nodes from" +
                    "integrators="+fx.length);
            }
            Object[] fxsort = g.topologicalSort(fx);
            for(int i=0; i < fxsort.length; i++) {
                _transitionschedule.insertLast(fxsort[i]);
            }
            if(VERBOSE) {
                System.out.println("state transition schedule constructed.");
            }
            _scheList.insertLast(_transitionschedule);
        }

        // construct an array of sink actors.
        int numofsink = _sink.size();
        if(DEBUG) {
            System.out.println("Number of Sink Actors:"+numofsink);
        }
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
            if(VERBOSE) {
                System.out.println("output schedule constructed.");
            }
            _scheList.insertLast(_outputschedule);
        }

        // construct an array of event detectors.
        int numofevdct = _evdct.size();
        if(DEBUG) {
            System.out.println("Number of Event Detector:"+numofevdct);
        }
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
            if(VERBOSE) {
                System.out.println("event detection schedule constructed.");
            }
            _scheList.insertLast(_outputschedule);
        }
        return _scheList.elements();
    }

    /** Return the successor actors of the given actor in the topology.
     *  If the actor is a sink, returns null. If any of the output
     *  port is not connected, throw an InvalidStateException.
     *  @param The specified actor. If the actor is null, returns null.
     *  @return The enumerations of predecessors,  unordered.
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

    /** Return the predecessor actors of the given actor in the topology.
     *  If the actor is a source, returns null. If any of the input
     *  port is not connected, throw an InvalidStateException.
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

    /** Convert the actors under schedule a directed acyclic graph,
     *  where the loops are broken at the output of the integrators.
     *  Each actor
     *  in the LinkedList is a node in the graph, each link between a pair
     *  of actors is a edge between the
     *  corresponding nodes unless the source node is an integrator.
     *  The presentance of the director and containers is not checked
     *  in this method, so the caller should check.
     *  @return A graph representation of the actors.
     */
    protected DirectedAcyclicGraph _toGraph(Enumeration actors) {
        if(VERBOSE) {
            System.out.println("in _toGraph()");
        }
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
        if(DEBUG) {
            System.out.println("Size of the graph="+g.getNodeCount());
        }
        // Create the edges.
        Enumeration allactors = actorlist.elements();
        while (allactors.hasMoreElements()) {
            Actor a = (Actor) allactors.nextElement();
            if(DEBUG) {
                System.out.println("For node:"+((Nameable)a).getName());
            }
            if(!(a instanceof CTDynamicActor)) {
                // Find the successors of a
                Enumeration successors = _successors(a);
                while (successors.hasMoreElements()) {
                    Actor s = (Actor) successors.nextElement();
                    if(actorlist.includes(s)) {
                        g.addEdge(a, s);
                        if(DEBUG) {
                            System.out.println("edge added" );
                        }
                    }
                }
            }
        }

	return g;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The static name of the scheduler.
    private static final String _staticname = "CT_Cluster_Scheduler";
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

    // Version of the lists.
    private transient long _dynamicversion = -1;
}
