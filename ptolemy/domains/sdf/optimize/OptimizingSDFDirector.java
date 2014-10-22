/* Director for the synchronous dataflow model of computation which
 finds an optimized schedule.

 Copyright (c) 1997-2014 The Regents of the University of California.
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
package ptolemy.domains.sdf.optimize;

import java.util.Iterator;

import ptolemy.actor.Actor;
import ptolemy.actor.FiringEvent;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.util.PeriodicDirectorHelper;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
////OptimizingSDFDirector

/**
Director for the synchronous dataflow (SDF) model of computation which finds
an optimized schedule according to a defined criterion.

<h1>Class comments</h1>
An OptimizingSDFDirector is the class that controls execution of actors under the
SDF domain, while using an optimal schedule.  For this it uses the OptimizingSDFScheduler
class.
<p>
See {@link ptolemy.domains.sdf.kernel.SDFDirector},
{@link ptolemy.domains.sdf.kernel.SDFScheduler} and
{@link ptolemy.domains.sdf.optimize.OptimizingSDFScheduler} for more information.
</p><p>
The <i>optimizationCriterion</i> parameter of this director
selects the optimization criterion.
</p>
@see ptolemy.domains.sdf.kernel.SDFScheduler
@see ptolemy.domains.sdf.kernel.SDFDirector
@see ptolemy.domains.sdf.optimize.OptimizingSDFScheduler

@author Marc Geilen
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Red (mgeilen)
@Pt.AcceptedRating Red ()
 */

public class OptimizingSDFDirector extends SDFDirector {

    /**
     * Enumeration type to define the supported optimization criteria.
     */
    public enum OptimizationCriteria {
        /**
         * Optimize for minimum number of frame buffers.
         */
        BUFFERS,
        /**
         * Optimize execution time (estimate).
         */
        EXECUTIONTIME
    }

    /**
     * Parameter defining the optimization criterion.
     */
    public Parameter optimizationCriterion;

    /**
     * Construct on OptimizingSDFDirector in the given container with the given name.
     * The container argument must not be null, or a
     * NullPointerException will be thrown.
     * If the name argument is null, then the name is set to the
     * empty string. Increment the version number of the workspace.
     * The OptimizingSDFDirector will have a default scheduler of type
     * OptimizingSDFScheduler.
     *
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container.  May be thrown in a derived class.
     *  @exception NameDuplicationException If the container is not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public OptimizingSDFDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        // FIXME: super(container, name) above calls SDFDirector._init in which the scheduler is set to an
        // SDFScheduler, but we need an OptimizingSDFScheduler (set in OptimizingSDFDirector._init)
        // before setting the value of the OptimizationCriterion parameter
        // Perhaps getting the preferred Scheduler type could be factored out, but I don't want to
        // touch the SDFDirector code.

        // initialize the parameter
        optimizationCriterion = new Parameter(this, "OptimizationCriterion");
        optimizationCriterion.setTypeEquals(BaseType.STRING);
        optimizationCriterion.addChoice("\"Buffers\"");
        optimizationCriterion.addChoice("\"Execution Time\"");

        // initialize
        _init();

    }

    /** Calculate the current schedule, if necessary, and iterate the
     *  contained actors in the order given by the schedule.
     *  method copied from super class SDFDirector. Modifies only the firing
     *  by firing the BufferingProfile if the interface is defined.
     *  Factoring out difference might be a good idea.
     *
     *  @exception IllegalActionException If any actor executed by this
     *   actor return false in prefire.
     *  @exception InvalidStateException If this director does not have a
     *   container.
     */
    @Override
    public void fire() throws IllegalActionException {
        // Don't call "super.fire();" here because if you do then
        // everything happens twice.

        OptimizingSDFScheduler scheduler = (OptimizingSDFScheduler) getScheduler();

        if (scheduler == null) {
            throw new IllegalActionException("Attempted to fire "
                    + "system with no scheduler");
        }

        // This will throw IllegalActionException if this director
        // does not have a container.
        Schedule schedule = scheduler.getSchedule();
        Iterator firings = schedule.firingIterator();

        while (firings.hasNext() && !_stopRequested) {
            Firing firing = (Firing) firings.next();
            Actor actor = firing.getActor();
            int iterationCount = firing.getIterationCount();

            if (_debugging) {
                _debug(new FiringEvent(this, actor, FiringEvent.BEFORE_ITERATE,
                        iterationCount));
            }

            int returnValue;
            // check if the actor has the interface BufferingProfile
            if (actor instanceof BufferingProfile) {
                BufferingProfile bp = (BufferingProfile) actor;
                // Then the firing must be a BufferProfileFiring
                BufferingProfileFiring f = (BufferingProfileFiring) firing;
                returnValue = bp.iterate(iterationCount, f.fireExclusive);
            } else {
                returnValue = actor.iterate(iterationCount);
            }

            if (returnValue == STOP_ITERATING) {
                _postfireReturns = false;
            } else if (returnValue == NOT_READY) {
                // See de/test/auto/knownFailedTests/DESDFClockTest.xml
                throw new IllegalActionException(this, actor, "Actor "
                        + "is not ready to fire.  Perhaps " + actor.getName()
                        + ".prefire() returned false? "
                        + "Try debugging the actor by selecting "
                        + "\"Listen to Actor\".  Also, for SDF check moml for "
                        + "tokenConsumptionRate on input.");
            }

            if (_debugging) {
                _debug(new FiringEvent(this, actor, FiringEvent.AFTER_ITERATE,
                        iterationCount));
            }
        }
    }

    /** React to a change in an attribute. If the changed attribute
     *  matches a parameter of the director, then the corresponding
     *  local copy of the parameter value will be updated.
     *  @param attribute The changed parameter.
     *  @exception IllegalActionException If the parameter set is not valid.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        // NOTE: Invalidate the schedules only if the values of these
        // parameters have changed.
        if (attribute == optimizationCriterion) {
            _setOptimizationCriterionValue();
        }
        super.attributeChanged(attribute);
    }

    ///////////////////////////////////////////////////////////////////
    ////                   private fields                                  ////

    /** Initialize the object.   In this case, we give the OptimizingSDFDirector a
     *  default scheduler of the class OptimizingSDFScheduler, an iterations
     *  parameter and a vectorizationFactor parameter.
     */
    private void _init() throws IllegalActionException,
            NameDuplicationException {

        // set the schedule to an instance of OptimizingSDFScheduler
        OptimizingSDFScheduler scheduler = new OptimizingSDFScheduler(this,
                uniqueName("OptimizingSDFScheduler"),
                _optimizationCriterionValue);

        // set the default value of the parameter
        optimizationCriterion.setExpression("\"Buffers\"");

        // if necessary, copy the string parameter to the enumerated value
        _setOptimizationCriterionValue();

        // set the constrainBufferSizes parameter
        scheduler.constrainBufferSizes.setExpression("constrainBufferSizes");
        setScheduler(scheduler);

        // Subclasses may set this to null and handle this themselves.
        _periodicDirectorHelper = new PeriodicDirectorHelper(this);
    }

    /**
     * Set the _optimizationCriterionValue to the proper value corresponding to the
     * OptimizationCriterion string parameter
     * @exception IllegalActionException if the criterion used is unknown
     */
    private void _setOptimizationCriterionValue() throws IllegalActionException {
        OptimizingSDFScheduler os = (OptimizingSDFScheduler) getScheduler();
        if (optimizationCriterion.getValueAsString().equals("\"Buffers\"")) {
            _optimizationCriterionValue = OptimizationCriteria.BUFFERS;
        } else if (optimizationCriterion.getValueAsString().equals(
                "\"Execution Time\"")) {
            _optimizationCriterionValue = OptimizationCriteria.EXECUTIONTIME;
        } else {
            throw new IllegalActionException("Unknown optimization criterion");
        }
        if (os != null) {
            os.optimizationCriterion = _optimizationCriterionValue;
        }
        // invalidate the schedule, because the criterion has changed
        invalidateSchedule();
    }

    private OptimizationCriteria _optimizationCriterionValue = OptimizationCriteria.BUFFERS;

}
