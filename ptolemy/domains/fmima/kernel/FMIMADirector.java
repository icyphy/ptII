/* A Director for FMI Hybrid Co-simulation.

   Copyright (c) 2015 The Regents of the University of California.
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
package ptolemy.domains.fmima.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.util.Time;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.sr.kernel.SRDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// FMIMA Director

/**
 * This is an experimental director.
 * We don't really know if we need it or not.
 * The idea is to have a director that coordinates the execution
 * of a model based on Co-simulation FMI
 *
 * @author Fabio Cremona
 * @version $Id: FMIMADirector.java$
 * @since Ptolemy II 11.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */

public class FMIMADirector extends SRDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public FMIMADirector() throws IllegalActionException, NameDuplicationException {
        super();
        _isFirstFire = true;
        _defaultPeriod = new Parameter(this, "default period");
        _defaultPeriod.setTypeEquals(BaseType.DOUBLE);
        _defaultPeriod.setExpression(period.getValueAsString());
    }

    /** Construct a director in the given workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace for this object.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public FMIMADirector(Workspace workspace) throws IllegalActionException, NameDuplicationException {
        super(workspace);
        _isFirstFire = true;
        _defaultPeriod = new Parameter(this, "default period");
        _defaultPeriod.setTypeEquals(BaseType.DOUBLE);
        _defaultPeriod.setExpression(period.getValueAsString());
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container.
     *  @exception NameDuplicationException If the name collides with an
     *   attribute in the container.
     */
    public FMIMADirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _isFirstFire = true;
        _defaultPeriod = new Parameter(this, "default period");
        _defaultPeriod.setTypeEquals(BaseType.DOUBLE);
        _defaultPeriod.setExpression(period.getValueAsString());
    }

    /** Fire FMUs according to a given scheduler until the iteration
     *  converges.
     *  An iteration converges when a pass through the schedule does
     *  not change the status of any receiver.
     *  @exception IllegalActionException If we couldn't process an event
     *  or if an event of smaller timestamp is found within the event queue.
     */
    @SuppressWarnings("deprecation")
    @Override
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("FMIMADirector: invoking fire().");
        }

        if (getModelTime().getDoubleValue() > getStopTime()) {
            stop();
        }

        // Calling FixedPointDirector.fire()
        // When super.fire() returns we reached a fixed point:
        // all FMUs propagated I/O signals.
        super.fire();
    }

    /** Request a firing of the given actor at the given absolute
     *  time, and return the time at which the specified will be
     *  fired. If the <i>period</i> is 0.0 and there is no enclosing
     *  director, then this method returns the current time. If
     *  the period is 0.0 and there is an enclosing director, then
     *  this method delegates to the enclosing director, returning
     *  whatever it returns. If the <i>period</i> is not 0.0, then
     *  this method checks to see whether the
     *  requested time is equal to the current time plus an integer
     *  multiple of the period. If so, it returns the requested time.
     *  If not, it returns current time plus the period.
     *  @param actor The actor scheduled to be fired.
     *  @param time The requested time.
     *  @param microstep The microstep (ignored by this director).
     *  @exception IllegalActionException If the operation is not
     *    permissible (e.g. the given time is in the past).
     *  @return Either the requested time or the current time plus the
     *  period.
     */
    @Override
    public Time fireAt(Actor actor, Time time, int microstep)
            throws IllegalActionException {
        if (_debugging) {
            _debug("FMIMADirector: fireAt("
                    + time.getLongValue() + ", "
                    + microstep + ")");
            _debug("* Current period: " + periodValue());
        }
        Time newTime = time;
        Time currentTime = getModelTime();
        Time hypoteticFutureTime = currentTime.add(periodValue());
        Time stepSize = newTime.subtract(currentTime);

        if (newTime.compareTo(hypoteticFutureTime) < 0 && !time.isZero()) {
            String newPeriod = stepSize.toString();
            period.setExpression(newPeriod);
        }

        if (_debugging) {
            _debug("* New period: " + periodValue());
        }
        return newTime;
    }

    /** Initialize the director and all deeply contained actors by calling
     *  the super.initialize() method.
     *  If the <i>period</i> parameter is greater than zero, then
     *  request a first firing of the executive director, if there
     *  is one.
     *  @exception IllegalActionException If the superclass throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _defaultPeriod.setExpression(period.getValueAsString());
    }

//        // We can now compute the step size of the FMU
//        // Consult all actors that implement TimeRegulator interface.
//        // FMUs for example, can implement TimeRegulator interface
//        // to check the acceptance of a step size.
//
//
//        Time proposedFmiTime = getModelTime().add(1E-8);//Time.POSITIVE_INFINITY;
//
//        Nameable container = getContainer();
//        Iterator<?> actors = ((CompositeActor) container).deepEntityList()
//                .iterator();
//        while (actors.hasNext()) {
//            Actor actor = (Actor) actors.next();
//            if (actor instanceof TimeRegulator) {
//                Time modifiedTime = ((TimeRegulator) actor).proposeTime(proposedFmiTime);
//                if (proposedFmiTime.compareTo(modifiedTime) > 0) {
//                    proposedFmiTime = modifiedTime;
//                }
//                if (_debugging) {
//                    _debug("FMU " + actor.getFullName() + " proposed: "
//                            + modifiedTime.getLongValue()
//                            + " at time: "
//                            + getModelTime());
//                }
//            }
//        }
//        if (_debugging) {
//            _debug("Computed future time: " + proposedFmiTime);
//        }
//    }

    /** Call postfire() on all contained FMUs that were fired in the current
     *  iteration.  Return false if the model
     *  has finished executing, either by reaching the iteration limit, or if
     *  no actors in the model return true in postfire(), or if stop has
     *  been requested, or if no actors fired at all in the last iteration.
     *  This method is called only once for each iteration.
     *  Note that FMUs are postfired in arbitrary order.
     *  @return True if the execution is not finished.
     *  @exception IllegalActionException If the iterations parameter does
     *   not have a valid token, or if there still some unknown inputs (which
     *   indicates a causality loop).
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        boolean result=  super.postfire();
        // restore the default period
        period.setExpression(_defaultPeriod.getValueAsString());
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected variables                 ////

    /**
     * A boolean variable used to flag the first fire() execution.
     * It is true before the first fire. It is set to true in postfire.
     * It is set to false before returning in fire.
     */
    protected boolean _isFirstFire;

    ///////////////////////////////////////////////////////////////////
    ////                       private variables                   ////

    Parameter _defaultPeriod;
}
