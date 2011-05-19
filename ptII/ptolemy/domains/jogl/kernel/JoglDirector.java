/* A director for the Jogl Domain.

 Copyright (c) 2009-2010 The Regents of the University of California.
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
package ptolemy.domains.jogl.kernel;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.FiringEvent;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.sched.Scheduler;
import ptolemy.actor.sched.StaticSchedulingDirector;
import ptolemy.actor.util.Time;
import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Workspace;

/**
 * A director for the Jogl Domain.
 * 
 * <p>
 * The Jogl domain uses JOGL to display three-dimensional graphics in Ptolemy
 * II. JOGL has a working principle that starts a thread that repeatedly calls a 
 * display() method at your fps (frames per second) rate. Everytime this method 
 * is called, it clears and rebuilds the entire scene. JoglDirector uses the 
 * {@link ptolemy.domains.jogl.kernel.JoglScheduler}, which implements the 
 * GLEventListener interface that is needed for lifecycle callbacks.
 *
 * @author Yasemin Demir
 * @version $Id: JoglDirector.java 57401 2010-03-03 23:11:41Z ydemir $
 * @since Ptolemy II 1.0
 * @Pt.ProposedRating yellow (chf)
 * @Pt.AcceptedRating yellow (vogel)
 */
public class JoglDirector extends StaticSchedulingDirector{

    /**
     * Construct a director in the default workspace with an empty string as its
     * name. The director is added to the list of objects in the workspace.
     * Increment the version number of the workspace.
     */
    
    //FIXME: Container and a name
    public JoglDirector() {        
        super();
        _init();
    }

    /**
     * Construct a director in the workspace with an empty name. The director is
     * added to the list of objects in the workspace. Increment the version
     * number of the workspace.
     * 
     * @param workspace
     *            The workspace of this object.
     */
    public JoglDirector(Workspace workspace) {
        super(workspace);
        _init();
    }

    /**
     * Construct a director in the given container with the given name. If the
     * container argument is null, a NullPointerException will be thrown. If the
     * name argument is null, then the name is set to the empty string.
     * Increment the version number of the workspace.
     * 
     * @param container
     *            Container of the director.
     * @param name
     *            Name of this director.
     * @exception IllegalActionException
     *                If the director is not compatible with the specified
     *                container.
     * @exception NameDuplicationException
     *                If the container is not a CompositeActor and the name
     *                collides with an entity in the container.
     */
    public JoglDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                    public parameters                      ////  

    /**
     * A parameter representing the number of times that postfire() may be
     * called before it returns false. If the value is less than or equal to
     * zero, the execution will never return false in postfire(), and thus the
     * execution continues indefinitely. This parameter must contain an
     * IntToken. The default value is an IntToken with the value zero.
     */
    public Parameter iterationInterval;

    /**
     * A parameter that indicates the time lower bound of each iteration. This
     * parameter is useful for guaranteeing that each frame of an animation
     * takes at least a certain amount of time before proceeding to the next
     * frame. This parameter is measured in milliseconds. This parameter must
     * contain an IntToken. The default value is an IntToken with value the 33,
     * which corresponds roughly to 30 frames per second.
     */
    public Parameter iterationTimeLowerBound;
    //FixME: Write comments here
   

    ///////////////////////////////////////////////////////////////////
    //// public methods ////

    /**
     * Clone the director into the specified workspace. This calls the base
     * class and then copies the parameter of this director. The new actor will
     * have the same parameter values as the old.
     * 
     * @param workspace
     *            The workspace for the new object.
     * @return A new object.
     * @exception CloneNotSupportedException
     *                If one of the attributes cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        _reset();

        JoglDirector newObject = (JoglDirector) (super.clone(workspace));
        return newObject;
    }


    /** In this director, do nothing. Override the super class method. This method does nothing and
     *  everything is postponed to the postfire() method. This assures
     *  that inputs are stable. 
     */
    public void fire() throws IllegalActionException {
        // Do nothing, because the super.fire() calculates the current schedule.
    }

    /**
     * Schedule a firing of the given actor at the given time. If there is an
     * executive director, this method delegates to it. Otherwise, it sets its
     * own notion of current time to that specified in the argument. The reason
     * for this is to enable JoglDirector to be a top-level director and to
     * support the design pattern where a director requests a refiring at the
     * next time it wishes to be awakened, just prior to returning from fire().
     * DEDirector, for example, does that, as does the SDFDirector if the period
     * parameter is set.
     * 
     * @param actor
     *            The actor scheduled to be fired.
     * @param time
     *            The scheduled time.
     * @return The time returned by the executive director, or or the specified
     *         time if there isn't one.
     * @exception IllegalActionException
     *                If by the executive director.
     */
    public Time fireAt(Actor actor, Time time) throws IllegalActionException {
        // Note that the actor parameter is ignored, because it does not
        // matter which actor requests firing.
        Nameable container = getContainer();

        if (container instanceof Actor) {
            Actor modalModel = (Actor) container;
            Director executiveDirector = modalModel.getExecutiveDirector();

            if (executiveDirector != null) {
                return executiveDirector.fireAt(modalModel, time);
            }
        }
        setModelTime(time);
        return time;

    }    

    /**
     * Initialize all the actors associated with this director. Perform some
     * internal initialization for this director.
     * 
     * @exception IllegalActionException
     *                If the initialize() method of one of the associated actors
     *                throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _disabledActors = new HashSet<Actor>();
        _buildActorTable();
        _iteration = 0;

        //_startIteration = ((IntToken) ((ArrayToken) (iterationInterval
        //        .getToken())).getElement(0)).intValue();

        _stopIteration = ((IntToken) ((ArrayToken) (iterationInterval
                .getToken())).getElement(1)).intValue();

    }

    /**
     * Process the mutation that occurred. Reset this director to an
     * uninitialized state to prepare for rescheduling. Notify parent class
     * about invalidated schedule.
     * 
     * @see ptolemy.kernel.util.NamedObj#attributeChanged
     * @see ptolemy.kernel.util.NamedObj#attributeTypeChanged
     */
    public void invalidateSchedule() {
        // This method is called when an entity is instantiated under
        // this director. This method is also called when a link is
        // made between ports and/or relations.
        _reset();
        super.invalidateSchedule();
    }

    /**
     * Iterate all actors under control of this director and fire them. Return
     * false if the system has finished executing. This happens when the
     * iteration limit is reached. The iteration limit is specified by the
     * <i>iterations</i> parameter. If the <i>iterations</i> parameter is set to
     * zero, this method will always return true and the model will run
     * indefinitely.
     * 
     * @return Return true if the iterations parameter is 0 or if the iteration
     *         limit has not been exceeded.
     * @exception IllegalActionException
     *                If unable to get the parameter <i>iterations</i>.
     */
    public boolean postfire() throws IllegalActionException {
        // Iterate all actors under control of this director and fire them.
        // This is done in postfire() to ensure that all inputs are stable.

        _fire();

        // Have to transfer outputs! Presumably outputs are only
        // instances of GLPipelineObjectToken going to a higher-level
        // JoglDirector, so producing those outputs in postfire()
        // is OK.
        Iterator outports = ((Actor) getContainer()).outputPortList()
                .iterator();
        while (outports.hasNext() && !_stopRequested) {
            IOPort p = (IOPort) outports.next();
            transferOutputs(p);
        }

        // Note: actors return false on postfire(), if they wish never
        // to be fired again during the execution. This can be
        // interpreted as the actor being dead.
        // Also, fireAt() calls by the actor will be ignored.
        super.postfire();

        _iteration++;

        if ((_stopIteration > 0) && (_iteration > _stopIteration)) {
            _iteration = 0;
            return false;
        }

        return true;
    }

    /**
     * Always return true. A GR composite actor will always be iterated. Note
     * that this does not call prefire() on the contained actors.
     * 
     * @return Always returns True.
     * @exception IllegalActionException
     *                Not thrown in this base class
     */
    public boolean prefire() throws IllegalActionException {
        // Note: Actors return false on prefire if they don't want to be
        // fired and postfired in the current iteration.
        return true;
    }

    /**
     * Preinitialize the actors associated with this director and initialize the
     * number of iterations to zero. The order in which the actors are
     * preinitialized is non-deterministic.
     * 
     * @exception IllegalActionException
     *                If the preinitialize() method of one of the associated
     *                actors throws it.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();

        Scheduler scheduler = getScheduler();

        if (scheduler == null) {
            throw new IllegalActionException("Attempted to initialize "
                    + "Jogl system with no scheduler");
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


    
    /**
     * Reset this director to an uninitialized state to prepare for the end of
     * an execution.
     * 
     * @exception IllegalActionException
     *                If the parent class throws it
     */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        _reset();
    }

   

    /**
     * Create an actor table that caches all the actors directed by this
     * director. This method is called once in initialize().
     * 
     * @exception IllegalActionException
     *                If the scheduler is null.
     */
    private void _buildActorTable() throws IllegalActionException {
        // FIXME: it seems like this is done in preinitialize()!!
        Scheduler currentScheduler = getScheduler();

        if (currentScheduler == null) {
            throw new IllegalActionException(this, "Attempted to fire "
                    + "Jogl system with no scheduler");
        }

        currentScheduler.getSchedule();
        _debugViewActorTable();
    }

    /**
     * For debugging purposes. Display the list of contained actors and other
     * pertinent information about them.
     * 
     * @exception IllegalActionException
     *                If there is a problem in obtaining the number of initial
     *                token for delay actors
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

    /**
     * Make sure that <i>iterationLowerUpperBound</i> milliseconds have elapsed
     * since the last iteration. Go through the schedule and iterate every
     * actor. If an actor returns false in its prefire(), fire() and postfire()
     * will not be called on it.
     * 
     * @exception IllegalActionException
     *                If an actor executed by this director returns false in its
     *                prefire().
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
                    + "Jogl system with no scheduler");
        }

        Schedule schedule = scheduler.getSchedule();

        Iterator actors = schedule.actorIterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();

            if (_disabledActors.contains(actor)) {
                continue;
            }

            // If an actor returns true to prefire(), fire() and postfire()
            // will be called.
            if (_debugging) {
                _debug(new FiringEvent(this, actor, FiringEvent.BEFORE_PREFIRE,
                        1));
            }

            //if (actor instanceof CompositeActor) {
                //CompositeActor compositeActor = (CompositeActor) actor;
                //Director insideDirector = compositeActor.getDirector();
                // FIXME: This is bogus.  This is assuming there is no
                // more than one inside director, and is delegating the
                // incrementing of time to that inside director.
                //_insideDirector = insideDirector;
                //_pseudoTimeEnabled = true;
            //}

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

                if (!actor.postfire()) {
                    _disabledActors.add(actor);
                }

                if (_debugging) {
                    _debug(new FiringEvent(this, actor,
                            FiringEvent.AFTER_POSTFIRE, 1));
                }
            }

            // Make sure we reset the pseudotime flag.
            //_pseudoTimeEnabled = false;
        }
    }
 
    /**
     * Most of the constructor initialization is relegated to this method.
     * Initialization process includes : - create a new actor table to cache all
     * actors contained - create a new receiver table to cache all receivers
     * contained - set default number of iterations - set period value
     */
    private void _init() {
        try {
            // Scheduler scheduler = new Scheduler(workspace());
            Scheduler scheduler = new JoglScheduler(workspace());
            setScheduler(scheduler);
        } catch (Exception ex) {
            // if setScheduler fails, then we should just set it to Null.
            // this should never happen because we don't override
            // setScheduler() to do sanity checks.
            throw new InternalErrorException(this, ex,
                    "Could not create Default scheduler.");
        }

        try {
            iterationInterval = new Parameter(this, "iterationInterval");
            iterationInterval.setExpression("{0, 1000000}");
            iterationInterval.setTypeEquals(new ArrayType(BaseType.INT, 2));
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
        _lastIterationTime = 0;
    }

    // /////////////////////////////////////////////////////////////////
    // // private variables ////

    /**
     * The set of actors that have returned false in their postfire() methods.
     * These actors will not be fired again.
     */
    private Set<Actor> _disabledActors;
    
    private long _lastIterationTime;

    //private Director _insideDirector;

    private int _iteration = 0;
    
    //private boolean _pseudoTimeEnabled = false;
    
    //private int _startIteration = 0;

    private int _stopIteration = 0;

    

}

