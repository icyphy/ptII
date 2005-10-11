/* Graphics (GR) domain director with synchronous/reactive semantics

 Copyright (c) 1998-2005 The Regents of the University of California.
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

 */
package ptolemy.domains.gr.kernel;

import java.util.Iterator;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.FiringEvent;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.sched.Scheduler;
import ptolemy.actor.sched.StaticSchedulingDirector;
import ptolemy.actor.util.Time;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// GRDirector

/**
 GR is a domain for displaying three-dimensional graphics in Ptolemy
 II.  GR is an untimed domain in where actors are connected in an
 acyclic directed graph.  Actors are fired according to a simple
 topological sort of the graph.  Nodes in the graph that have no
 descendants are assumed to be consumers of data produced by the rest
 of the model.

 <p>The basic idea behind the GR domain is to arrange geometry and
 transform actors in a directed acyclic graph to represent the location
 and orientation of objects in a scene. This topology of connected GR
 actors form what is commonly called a scene graph in computer graphics
 literature.  The GR director converts the GR scene graph into a Java3D
 representation for rendering on the computer screen.

 @see GRReceiver
 @see GRActor

 @author C. Fong, Steve Neuendorffer, Contributor: Christopher Hylands
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating yellow (chf)
 @Pt.AcceptedRating yellow (vogel)
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

    /** A parameter representing the number of times that postfire()
     *  may be called before it returns false.  If the value is less
     *  than or equal to zero, the execution will never return false
     *  in postfire(), and thus the execution continues indefinitely.
     *  This parameter must contain an IntToken.
     *  The default value is an IntToken with the value zero.
     */
    public Parameter iterations;

    /** A parameter that indicates the time lower bound of each
     *  iteration.  This parameter is useful for guaranteeing that
     *  each frame of an animation takes at least a certain amount of
     *  time before proceeding to the next frame. This parameter is
     *  measured in milliseconds.
     *  This parameter must contain an IntToken.
     *  The default value is an IntToken with value the 33, which
     *  corresponds roughly to 30 frames per second.
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
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        _reset();

        GRDirector newObject = (GRDirector) (super.clone(workspace));
        return newObject;
    }

    /** Override the super class method. This method does nothing and 
     *  everything is postponed to the postfire() method.
     */
    public void fire() throws IllegalActionException {
        // do nothing.
        // Everything is postponed to the postfire() method.
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
    public void fireAt(Actor actor, Time time) throws IllegalActionException {
        setModelTime(time);
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
        return getModelTime().getDoubleValue();
    }

    /** Return the current "time". The GR domain is not a timed domain,
     *  so this method is semantically meaningless.  However, this method
     *  is implemented in order to get timed domains to work inside the
     *  GR domain. In particular, this method will give actors a "fake"
     *  impression of advancement of time.
     *
     *  @return The current "time"
     */
    public Time getModelTime() {
        if (_pseudoTimeEnabled == true) {
            return _insideDirector.getModelTime();
        } else {
            return super.getModelTime();
        }
    }

    /** Return maximum value for type double. Since the GR domain is not a
     *  timed domain, so this method does not return any meaningful value.
     *  However, this method is implemented so that GR will work within
     *  timed domains.
     *
     *  @return The maximum value for type double.
     */
    public double getNextIterationTime() {
        return getModelNextIterationTime().getDoubleValue();
    }

    /** Return maximum value for type double. Since the GR domain is not a
     *  timed domain, so this method does not return any meaningful value.
     *  However, this method is implemented so that GR will work within
     *  timed domains.
     *
     *  @return The maximum value for type double.
     */
    public Time getModelNextIterationTime() {
        try {
            return new Time(this, Double.POSITIVE_INFINITY);
        } catch (IllegalActionException e) {
            // If the time resolution of the director is invalid,
            // it should have been caught before this.
            throw new InternalErrorException(e);
        }
    }

    /** Initialize all the actors associated with this director. Perform
     *  some internal initialization for this director.
     *
     *  @exception IllegalActionException If the initialize() method of
     *  one of the associated actors throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _buildActorTable();
        _iteration = 0;

        // Get the ViewScreen.
        ViewScreenInterface viewScreen = null;
        TypedCompositeActor container = (TypedCompositeActor) getContainer();
        Iterator actors = container.deepEntityList().iterator();

        while (actors.hasNext()) {
            Object actor = actors.next();

            if (actor instanceof ViewScreenInterface) {
                if (viewScreen != null) {
                    throw new IllegalActionException(this,
                            "GR model cannot contain more than one view screen.");
                }

                viewScreen = (ViewScreenInterface) actor;
            }
        }

        if (viewScreen == null) {
            throw new IllegalActionException(this,
                    "GR model does not contain a view screen.");
        }

        // Set the view screen for all the actors.
        actors = container.deepEntityList().iterator();

        while (actors.hasNext()) {
            NamedObj actor = (NamedObj) actors.next();

            if (actor instanceof GRActor) {
                ((GRActor) actor)._setViewScreen((GRActor) viewScreen);
            }
        }
    }

    /** Process the mutation that occurred.  Reset this director
     *  to an uninitialized state to prepare for rescheduling.
     *  Notify parent class about invalidated schedule.
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

    /** Iterate all actors under control of this director and fire them.
     *  Return false if the system has finished executing. This
     *  happens when the iteration limit is reached. The iteration
     *  limit is specified by the <i>iterations</i> parameter. If the
     *  <i>iterations</i> parameter is set to zero, this method will always
     *  return true and the model will run indefinitely.
     *
     *  @return Return true if the iterations parameter is 0 or
     *  if the iteration limit has not been exceeded.
     *  @exception IllegalActionException If unable to get the parameter
     *  <i>iterations</i>.
     */
    public boolean postfire() throws IllegalActionException {
        // iterate all actors under control of this director and fire them.
        _fire();
        // Note: actors return false on postfire(), if they wish never
        // to be fired again during the execution. This can be
        // interpreted as the actor being dead.
        // Also, fireAt() calls by the actor will be ignored.
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
     *  @return Always returns True.
     *  @exception IllegalActionException Not thrown in this base class
     */
    public boolean prefire() throws IllegalActionException {
        // Note: Actors return false on prefire if they don't want to be
        // fired and postfired in the current iteration.
        return true;
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

        Scheduler scheduler = getScheduler();

        if (scheduler == null) {
            throw new IllegalActionException("Attempted to initialize "
                    + "GR system with no scheduler");
        }

        // force the schedule to be computed.
        if (_debugging) {
            _debug("Computing schedule");
        }

        try {
            scheduler.getSchedule();
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex,
                    "Failed to compute schedule:");
        }
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
    ////                         private methods                   ////

    /** Create an actor table that caches all the actors directed by this
     *  director.  This method is called once in initialize().
     *  @exception IllegalActionException If the scheduler is null.
     */
    private void _buildActorTable() throws IllegalActionException {
        Scheduler currentScheduler = getScheduler();

        if (currentScheduler == null) {
            throw new IllegalActionException(this, "Attempted to fire "
                    + "GR system with no scheduler");
        }

        currentScheduler.getSchedule();
        _debugViewActorTable();
    }

    /** For debugging purposes.  Display the list of contained actors
     *  and other pertinent information about them.
     *
     *  @exception IllegalActionException If there is a problem in
     *   obtaining the number of initial token for delay actors
     */
    private void _debugViewActorTable() throws IllegalActionException {
        TypedCompositeActor container = (TypedCompositeActor) getContainer();
        List entityList = container.entityList();
        _debug("\nACTOR TABLE with " + entityList.size() + " unique actors");
        _debug("---------------------------------------");

        Iterator actors = entityList.iterator();

        while (actors.hasNext()) {
            ComponentEntity actor = (ComponentEntity) actors.next();

            if (!actor.isAtomic()) {
                _debug(" **COMPOSITE** ");
            }

            _debug(" ");
        }
    }

    /** Make sure that <i>iterationLowerUpperBound</i> milliseconds have
     *  elapsed since the last iteration.  Go through the schedule and
     *  iterate every actor. If an actor returns false in its prefire(),
     *  fire() and postfire() will not be called on it.
     *
     *  @exception IllegalActionException If an actor executed by this
     *  director returns false in its prefire().
     */
    private void _fire() throws IllegalActionException {
        TypedCompositeActor container = (TypedCompositeActor) getContainer();
    
        // FIXME: This code works differently than all other
        // synchronizeToRealTime implementations.
        long currentTime = System.currentTimeMillis();
        int frameRate = ((IntToken) iterationTimeLowerBound.getToken())
                .intValue();
        long timeElapsed = currentTime - _lastIterationTime;
        long timeRemaining = frameRate - timeElapsed;
    
        if (timeRemaining > 0) {
            try {
                java.lang.Thread.sleep(timeRemaining);
            } catch (InterruptedException e) {
                // Ignored.
            }
        }
    
        _lastIterationTime = currentTime;
    
        if (container == null) {
            throw new InvalidStateException(this, getName()
                    + " fired, but it has no container!");
        }
    
        Scheduler scheduler = getScheduler();
    
        if (scheduler == null) {
            throw new IllegalActionException(this, "Attempted to fire "
                    + "GR system with no scheduler");
        }
    
        Schedule schedule = scheduler.getSchedule();
    
        Iterator actors = schedule.actorIterator();
    
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
    
            // If an actor returns true to prefire(), fire() and postfire()
            // will be called.
            if (_debugging) {
                _debug(new FiringEvent(this, actor, FiringEvent.BEFORE_PREFIRE,
                        1));
            }
    
            if (actor instanceof CompositeActor) {
                CompositeActor compositeActor = (CompositeActor) actor;
                Director insideDirector = compositeActor.getDirector();
    
                _insideDirector = insideDirector;
                _pseudoTimeEnabled = true;
            }
    
            boolean flag = actor.prefire();
    
            if (_debugging) {
                _debug(new FiringEvent(this, actor, FiringEvent.AFTER_PREFIRE,
                        1));
            }
    
            if (flag) {
                if (_debugging) {
                    _debug(new FiringEvent(this, actor,
                            FiringEvent.BEFORE_FIRE, 1));
                }
    
                actor.fire();
    
                if (_debugging) {
                    _debug(new FiringEvent(this, actor, FiringEvent.AFTER_FIRE,
                            1));
                    _debug(new FiringEvent(this, actor,
                            FiringEvent.BEFORE_POSTFIRE, 1));
                }
    
                actor.postfire();
    
                if (_debugging) {
                    _debug(new FiringEvent(this, actor,
                            FiringEvent.AFTER_POSTFIRE, 1));
                }
            }
    
            // Make sure we reset the pseudotime flag.
            _pseudoTimeEnabled = false;
    
            // FIXME: should remove actor from schedule
            // if it returns false on postfire()
        }
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
            Class.forName("javax.vecmath.Tuple3f");
        } catch (Exception ex) {
            throw new InternalErrorException(
                    this,
                    ex,
                    "The GR domain requires that Java 3D be installed.\n"
                            + "Java 3D can be downloaded from\n"
                            + "http://java.sun.com/products/java-media/3D/\n"
                            + "For details see $PTII/ptolemy/domains/gr/main.htm");
        }

        try {
            GRScheduler scheduler = new GRScheduler(workspace());
            setScheduler(scheduler);
        } catch (Exception ex) {
            // if setScheduler fails, then we should just set it to Null.
            // this should never happen because we don't override
            // setScheduler() to do sanity checks.
            throw new InternalErrorException(this, ex,
                    "Could not create Default scheduler.");
        }

        try {
            iterations = new Parameter(this, "iterations", new IntToken(0));
            iterations.setTypeEquals(BaseType.INT);
            iterationTimeLowerBound = new Parameter(this,
                    "iterationTimeLowerBound", new IntToken(33));
            iterationTimeLowerBound.setTypeEquals(BaseType.INT);
        } catch (Throwable throwable) {
            throw new InternalErrorException(this, throwable,
                    "Cannot create default iterations parameter.");
        }

        _reset();
    }

    private void _reset() {
        _lastIterationTime = (long) 0;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private long _lastIterationTime;

    private boolean _pseudoTimeEnabled = false;

    private Director _insideDirector;

    private int _iteration = 0;
}
