/* Graphics (GR) domain director with synchronous/reactive semantics

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating yellow (chf@eecs.berkeley.edu)
@AcceptedRating yellow (vogel@eecs.berkeley.edu)
*/

package ptolemy.domains.gr.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.sched.Scheduler;
import ptolemy.actor.sched.StaticSchedulingDirector;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.*;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

//////////////////////////////////////////////////////////////////////////
//// GRDirector
/**

<h1>GR overview</h1>
GR is a domain for displaying three-dimensional graphics in
Ptolemy II.  GR is an untimed domain that follows loop-less
synchronous/reactive (SR) semantics.

The basic idea behind the GR domain is to arrange geometry and transform
actors in a directed acyclic graph to represent the location and orientation
of objects in a scene. This topology of connected GR actors form what is
commonly called a scene graph in computer graphics literature.
The GR director converts the GR scene graph into a Java3D representation for
rendering on the computer screen.

@see GRReceiver
@see GRActor

@author C. Fong, Contributor: Christopher Hylands
@version $Id$
@since Ptolemy II 1.0
*/
public class GRDirector extends StaticSchedulingDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public GRDirector() {
            super();
        _init();
    }

    /** Construct a director in the workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *
     *  @param workspace The workspace of this object.
     */
    public GRDirector(Workspace workspace) {
        super(workspace);
        _init();
    }

    /** Construct a director in the given container with the given name.
     *  If the container argument is null, a NullPointerException will
     *  be thrown. If the name argument is null, then the name is set
     *  to the empty string. Increment the version number of the workspace.
     *
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the
     *   director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container is not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public GRDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** A parameter representing the number of times that postfire may be
     *  called before it returns false.  If the value is less than or
     *  equal to zero, the execution will never return false in postfire,
     *  and thus the execution continues indefinitely.
     *  The default value is an IntToken with the value zero.
     */
    public Parameter iterations;

    /** A parameter that indicates the time lower bound of each iteration.
     *  This parameter is useful for guaranteeing that each frame of an
     *  animation takes at least a certain amount of time before proceeding
     *  to the next frame. This parameter is measured in milliseconds.
     *  The default value is an IntToken with value the 33.
     */
    public Parameter iterationTimeLowerBound;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the director into the specified workspace. This calls the
     *  base class and then copies the parameter of this director. The new
     *  actor will have the same parameter values as the old.
     *
     *  @param workspace The workspace for the new object.
     *  @return A new object.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        _reset();
        GRDirector newObject = (GRDirector)(super.clone(workspace));
        return newObject;
    }



    /** Make sure that <i>iterationLowerUpperBound</i> milliseconds have
     *  elapsed since the last iteration.  Go through the schedule and
     *  iterate every actor if an actor returns false in its prefire(),
     *  fire() and postfire() will not be called on it.
     *
     *  @exception IllegalActionException If an actor executed by this
     *  director returns false in its prefire().
     */
    public void fire() throws IllegalActionException {
        TypedCompositeActor container = (TypedCompositeActor) getContainer();
        Director outsideDirector = _getOutsideDirector();

        long currentTime = System.currentTimeMillis();
        int frameRate =
            ((IntToken)iterationTimeLowerBound.getToken()).intValue();
        long timeElapsed = currentTime - _lastIterationTime;
        long timeRemaining = frameRate - timeElapsed;
        if (timeRemaining > 0) {
            try {
                java.lang.Thread.sleep(timeRemaining);
            } catch (InterruptedException e) {

            }
        }
        _lastIterationTime = currentTime;

        if (container == null) {
            throw new InvalidStateException("GRDirector " + getName() +
                    " fired, but it has no container!");
        }

        Scheduler scheduler = getScheduler();
        if (scheduler == null)
            throw new IllegalActionException(this,"Attempted to fire " +
                    "GR system with no scheduler");

        Schedule schedule = scheduler.getSchedule();

        Iterator allActors = schedule.actorIterator();
        while (allActors.hasNext()) {

            Actor actor = (Actor)allActors.next();

            // If an actor returns true to prefire(), fire() and postfire()
            // will be called.
            if (actor.prefire() == true) {
                if (actor instanceof CompositeActor) {
                    CompositeActor compositeActor = (CompositeActor) actor;
                    Director  insideDirector = compositeActor.getDirector();

                    _insideDirector = insideDirector;
                    _pseudoTimeEnabled = true;
                    actor.fire();
                    _pseudoTimeEnabled = false;
                } else {
                    actor.fire();
                }
            }

            actor.postfire();
            // FIXME: should remove actor from schedule
            // if it returns false on postfire()
        }
    }


    /** Advance "time" to the next requested firing time.  The GR domain is
     *  not a timed domain, so this method is semantically meaningless.
     *  However, this method is implemented in order to get other timed
     *  domains to work inside the GR domain. In particular, this method
     *  will give actors a fake impression of the advancement of time.
     *
     *  @param actor The actor to be fired.
     *  @param time The next time when the actor should be fired.
     */
    public void fireAt(Actor actor, double time)
            throws IllegalActionException {
        _currentTime = time;
    }

    /** Return the current "time". The GR domain is not a timed domain,
     *  so this method is semantically meaningless.  However, this method
     *  is implemented in order to get timed domains to work inside the
     *  GR domain. In particular, this method will give actors a "fake"
     *  impression of advancement of time.
     *
     *  @return The current "time"
     */
    public double getCurrentTime() {
        if (_pseudoTimeEnabled == true) {
            return _insideDirector.getCurrentTime();
        } else {
            return super.getCurrentTime();
        }
    }

    /** Return maximum value for type double. Since the GR domain is not a
     *  timed domain, so this method does not return any meaningful value.
     *  However, this method is implemented in order to get timed domains
     *  to work inside the GR domain.
     *
     *  @return The maximum value for type double.
     */
    public double getNextIterationTime() {
        return Double.MAX_VALUE;
    }



    /** Initialize all the actors associated with this director. Perform
     *  some internal initialization for this director.
     *
     *  @exception IllegalActionException If the preinitialize() method of
     *  one of the associated actors throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _buildActorTable();
        _buildReceiverTable();
    }



    /** Process the mutation that occurred.  Reset this director
     *  to an uninitialized state to prepare for rescheduling.
     *  Notify parent class about invalidated schedule.
     *
     *  see other mutation methods:
     *
     *  @see ptolemy.kernel.util.NamedObj#attributeChanged
     *  @see ptolemy.kernel.util.NamedObj#attributeTypeChanged
     */
    public void invalidateSchedule() {
        // This method is called when an entity is instantiated under
        // this director. This method is also called when a link is
        // made between ports and/or relations.
        _reset();
        super.invalidateSchedule();
    }

    /** Return a new receiver consistent with the GR domain.
     *
     *  @return A new GRReceiver.
     */
    public Receiver newReceiver() {
        return new GRReceiver();
    }

    /** Return false if the system has finished executing. This happens when
     *  the iteration limit is reached. The iteration limit is specified by
     *  the "iterations" parameter. If the "iterations" parameter is set to
     *  zero, the model will run indefinitely.
     *
     *  @return True if the Director wants to be fired again in the
     *  future.
     *  @exception IllegalActionException If unable to get the parameter
     *  <i>iterations</i>
     */
    public boolean postfire() throws IllegalActionException {
        // Note: actors return false on postfire(), if they wish never to be
        // fired again during the execution. This can be interpreted as the
        // actor being dead. Also, fireAt() calls by the actor will be ignored.
        // In SDF, an actor returning false on postfire will effectively
        // stop the SDF composite actor container too.
        super.postfire();
        int totalIterations = ((IntToken) (iterations.getToken())).intValue();
        _iteration++;
        if ((totalIterations > 0) && (_iteration >= totalIterations)) {
            _iteration = 0;
            return false;
        }
        return true;
    }

    /** Always return true. A GR composite actor will always be iterated.
     *  Note that this does not call prefire() on the contained actors.
     *
     *  @return True.
     *  @exception IllegalActionException Not thrown in this base class
     */
    public boolean prefire() throws IllegalActionException {
        // Note: Actors return false on prefire if they don't want to be
        // fired and postfired in the current iteration.
        return true;
        // _prefire_
    }


    /** Preinitialize the actors associated with this director and
     *  initialize the number of iterations to zero.  The order in which
     *  the actors are preinitialized is non-deterministic.
     *
     *  @exception IllegalActionException If the preinitialize() method of
     *  one of the associated actors throws it.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        _iteration = 0;
    }

    /** Reset this director to an uninitialized state to prepare
     *  for the end of an execution.
     *
     *  @exception IllegalActionException If the parent class
     *  throws it
     */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        _reset();
    }


    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    /** Override the base class to indicate that this director does not
     *  need write access on the workspace during an iteration.
     *
     *  @return False.
     */
    protected boolean _writeAccessRequired() {
        return false;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////


    /** Create an actor table that caches all the actors directed by this
     *  director.  This method is called once at initialize();
     *  @exception IllegalActionException If the scheduler is null
     */
    private void _buildActorTable() throws IllegalActionException {
        Scheduler currentScheduler = getScheduler();
        if (currentScheduler == null)
            throw new IllegalActionException(this,"Attempted to fire " +
                    "GR system with no scheduler");

        Schedule schedule = currentScheduler.getSchedule();
        Iterator allActorsScheduled = schedule.actorIterator();


        int actorsInSchedule = 0;
        while (allActorsScheduled.hasNext()) {
            Actor actor = (Actor) allActorsScheduled.next();
            String name = ((Nameable)actor).getFullName();
            ContainedGRActor grActor =
                (ContainedGRActor) _allActorsTable.get(actor);
            if (grActor == null) {
                _allActorsTable.put(actor, new ContainedGRActor(actor));
                grActor = (ContainedGRActor) _allActorsTable.get(actor);
                _actorTable.add(grActor);
            }
            actorsInSchedule++;
        }

        // include the container as an actor.
        // This is needed for TypedCompositeActors
        String name = getContainer().getFullName();
        Actor actor = (Actor) getContainer();
        _allActorsTable.put(actor,
                new ContainedGRActor((Actor)getContainer()));
        ContainedGRActor grActor =
            (ContainedGRActor) _allActorsTable.get(actor);
        _actorTable.add(grActor);

        _debugViewActorTable();
        ListIterator receiverIterator = _receiverTable.listIterator();
        while (receiverIterator.hasNext()) {
            GRReceiver currentReceiver = (GRReceiver) receiverIterator.next();
            //currentReceiver.determineEnds(this);
        }

        receiverIterator = _receiverTable.listIterator();


        _debugViewActorTable();
        _debugViewReceiverTable();
    }

    /** Build the internal cache of all the ports directed by this director
     *
     *  @exception IllegalActionException If methods called throw it.
     */
    private void _buildOutputPortTable() throws IllegalActionException {
        TypedCompositeActor container = (TypedCompositeActor) getContainer();

        Iterator outports = container.outputPortList().iterator();
        while (outports.hasNext()) {
            IOPort port = (IOPort)outports.next();

            _outputPortTable.add(new GRIOPort(port));
        }
    }

    /** Build the internal cache of all the receivers directed by this
     *  director.
     *
     *  @exception IllegalActionException If methods called throw it.
     */
    private void _buildReceiverTable() throws IllegalActionException {
        CompositeActor container = (CompositeActor) getContainer();
        if (container != null) {
            Iterator allActors = container.deepEntityList().iterator();
            while (allActors.hasNext()) {
                Actor actor = (Actor) allActors.next();
                // Get all input ports
                Iterator allInputs = actor.inputPortList().iterator();
                while (allInputs.hasNext()){
                    IOPort inputPort = (IOPort)allInputs.next();
                    Receiver[][] receivers = inputPort.getReceivers();
                    if (receivers != null) {
                        for (int i = 0; i < receivers.length; i++) {
                            if (receivers[i] != null) {
                                for (int j = 0; j < receivers[i].length; j++) {
                                    if (receivers[i][j] != null) {
                                        _receiverTable.add(receivers[i][j]);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Also add the inside receivers in the ports of the
            // composite actor that contains this director.
            Iterator compositePorts = container.outputPortList().iterator();
            while (compositePorts.hasNext()) {
                IOPort outputPort = (IOPort)compositePorts.next();
                Receiver[][] receivers = outputPort.getInsideReceivers();
                if (receivers != null) {
                    for (int i = 0; i < receivers.length; i++) {
                        if (receivers[i] != null) {
                            for (int j = 0; j < receivers[i].length; j++) {
                                if (receivers[i][j] != null) {
                                    _receiverTable.add(receivers[i][j]);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /** For debugging purposes.  Display the list of contained actors
     *  and other pertinent information about them.
     *
     *  @exception IllegalActionException If there is a problem in
     *   obtaining the number of initial token for delay actors
     */
    private void _debugViewActorTable() throws IllegalActionException {
        debug.println("\nACTOR TABLE with " + _actorTable.size()
                + " unique actors");
        debug.println("---------------------------------------");
        ListIterator actorIterator = _actorTable.listIterator();
        while (actorIterator.hasNext()) {
            ContainedGRActor currentActor =
                (ContainedGRActor) actorIterator.next();
            String actorName = ((Nameable) currentActor._actor).getName();

            if ( !((ComponentEntity) currentActor._actor).isAtomic() ) {
                debug.print(" **COMPOSITE** ");
            }
            debug.println(" ");
        }
    }


    /** For debugging purposes.  Display the list of attributes
     *  inside a given named object
     *  @param object The named object that has a list of attributes
     */
    private void _debugViewAttributesList(NamedObj object) {
        List list = object.attributeList();
        Iterator listIterator = list.iterator();

        debug.println("attribute List:");
        while (listIterator.hasNext()) {
            Attribute attribute = (Attribute) listIterator.next();
            debug.println(attribute);
        }
    }

    /** For debugging purposes. This is mainly used for figuring out
     *  the list of output ports in a TypedCompositeActor container.
     *  Note: This method seems to work only for opaque
     *        TypedCompositeActor containers.
     *  Note: Output ports of actors inside the TypedCompositeActor
     *  container will not be listed.
     */
    private void _debugViewContainerOutputPorts()
            throws IllegalActionException {

        List list = ((TypedCompositeActor)getContainer()).outputPortList();
        Iterator listIterator = list.iterator();

        debug.println("\nTypedCompositeActor container output port list:");
        while (listIterator.hasNext()) {
            IOPort port = (IOPort) listIterator.next();
            debug.println(" ->" + port);
            _debugViewPortInsideReceivers(port);
            //_debugViewPortRemoteReceivers(port);
        }
        debug.println("\n");
    }

    /** For debugging purposes.  Display the list of contained entities
     *  inside the composite object
     *  @param object The composite entity with a list of contained entities.
     */
    private void _debugViewEntityList(CompositeEntity object) {

        List list = object.entityList();
            Iterator listIterator = list.iterator();

            debug.println("\nentity List:");
            while (listIterator.hasNext()) {
                Entity entity = (Entity) listIterator.next();
                debug.println(entity);
            }
            debug.println("\n");
    }

    /** For debugging purposes.  This function only makes sense
     *  if the port argument is an opaque / cross-hierarchy
     *  output port.
     *  Note: The only output ports with receivers are
     *        opaque / cross-hierarchy output ports.
     *  IMPORTANT: There are no receivers inside transparent
     *             TypedCompositeActors ports
     */
    private void _debugViewPortInsideReceivers(IOPort port)
            throws IllegalActionException {
        Receiver[][] portReceivers = port.getInsideReceivers();

            for (int i = 0; i < port.getWidth(); i++) {
                for (int j = 0; j < portReceivers[i].length; j++) {
                    debug.println("  ->" + portReceivers[i][j]);
                    debug.println("  = >"
                            + portReceivers[i][j].getContainer());
                    // FIXME: remove comments when debugging
                    // ((GRReceiver)portReceivers[i][j]).displayReceiverInfo();
                }
            }
    }

    /** For debugging purposes. This is mainly used for figuring out
     *  which outside receivers are connected to an output port of an
     *  TypedCompositeActor. The TypedCompositeActor can be transparent
     *  or opaque.
     */
    private void _debugViewPortRemoteReceivers(IOPort port) {
        Receiver[][] remoteReceivers = port.getRemoteReceivers();

            for (int i = 0; i < port.getWidth(); i++) {
                for (int j = 0; j < remoteReceivers[i].length; j++) {
                    debug.println("  -->" + remoteReceivers[i][j]);
                    debug.println("  == >"
                            + remoteReceivers[i][j].getContainer());
                }
            }
    }

    /** For debugging purposes.  Display the list of contained receivers
     *  and other pertinent information about them.
     */
    private void _debugViewReceiverTable() {
        //  -displayReceiverTable-
        debug.print("\nARC RECEIVER table with "
                + _receiverTable.size());
        debug.println(" unique receivers");

        ListIterator receiverIterator = _receiverTable.listIterator();

        while (receiverIterator.hasNext()) {
            GRReceiver currentReceiver = (GRReceiver) receiverIterator.next();
            debug.println(" receiver "
                    + currentReceiver);
        }
        debug.println("\n");
    }

    /** Convenience method for getting the director of the container that
     *  holds this director.  If this director is inside a toplevel
     *  container, then the returned value is null.
     *  @return The executive director.
     */
    private Director _getOutsideDirector() {
        TypedCompositeActor container = (TypedCompositeActor) getContainer();
        Director outsideDirector = container.getExecutiveDirector();

        return outsideDirector;
    }


    /** Most of the constructor initialization is relegated to this method.
     *  Initialization process includes :
     *    - create a new actor table to cache all actors contained
     *    - create a new receiver table to cache all receivers contained
     *    - set default number of iterations
     *    - set period value
     */
    private void _init() {
        try {
            // If Java3D is not present, then this class is usually
            // the class that is reported as missing.
            Class java3dClass = Class.forName("javax.vecmath.Tuple3f");
        } catch (Exception ex) {
            throw new InternalErrorException(this, ex,
                    "The GR domain requires that Java 3D be installed.\n" +
                    "Java 3D can be downloaded from\n" +
                    "http://java.sun.com/products/java-media/3D/\n" +
                    "For details see $PTII/ptolemy/domains/gr/main.htm");
        }

        try {
            GRScheduler scheduler = new GRScheduler(workspace());
            setScheduler(scheduler);
        } catch (Exception e) {
            // if setScheduler fails, then we should just set it to Null.
            // this should never happen because we don't override
            // setScheduler() to do sanity checks.
            throw new InternalErrorException(
                    "Could not create Default Scheduler:\n" +
                    e.getMessage());
        }

        try {
            iterations = new Parameter(this,"iterations",new IntToken(0));
            iterationTimeLowerBound = new Parameter(this,
                    "iteration time lower bound",new IntToken(33));
        } catch (Exception e) {
            throw new InternalErrorException(
                    "Cannot create default iterations parameter:\n" +
                    e.getMessage());
        }
            try {
            //period = new Parameter(this,"period",new DoubleToken(1.0));
            _reset();
            iterations.setToken(new IntToken(0));
            debug = new GRDebug(false);
            } catch (Exception e) {
                throw new InternalErrorException(
                    "unable to initialize GR Director:\n" +
                    e.getMessage());
            }
    }

    private void _reset() {
        _actorTable = new ArrayList();
        _receiverTable = new ArrayList();
        _outputPortTable = new ArrayList();
        _allActorsTable = new Hashtable();
        _currentTime = 0.0;
        _formerTimeFired = 0.0;
        _formerValidTimeFired = 0.0;
        _lastIterationTime = (long)0;
    }




    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // ArrayList to keep track of all actors scheduled by GRDirector
    private ArrayList _actorTable;

    // ArrayList used to cache all receivers managed by GRDirector
    private ArrayList _receiverTable;

    // Hashtable for keeping track of actor information
    private Hashtable _allActorsTable;

    // The time when the previous valid prefire() was called
    private double _formerValidTimeFired;

    // The time when the previous valid or invalid prefire() was called
    private double _formerTimeFired;

    // ArrayList to keep track of all container output ports
    private ArrayList _outputPortTable;

    // display for debugging purposes
    private GRDebug debug;

    private long _lastIterationTime;
    private boolean _pseudoTimeEnabled = false;
    private Director _insideDirector;
    private int _iteration = 0;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    // Inner class to cache important variables for contained actors
    private class ContainedGRActor {
            private Actor    _actor;

            /* Construct the information on the contained Actor
             * @param a The actor
             */
            public ContainedGRActor(Actor actor) {
            _actor = actor;
            }
    }

    // Inner class to cache important variables for container output ports
    private class GRIOPort {
        private IOPort _port;
        private boolean _shouldTransferOutputs;

        /*  Construct the information on the output port
         *  @param p The port
         */
        public GRIOPort(IOPort port) {
            _port = port;
            _shouldTransferOutputs = false;
        }
    }

    // TO DO:
    // 1.) AttributeChanged on several actors
    // 2.)
}
