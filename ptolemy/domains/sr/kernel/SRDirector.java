/* Director for the Synchronous Reactive model of computation.

 Copyright (c) 2000-2007 The Regents of the University of California.
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
package ptolemy.domains.sr.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.sched.FixedPointDirector;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// SRDirector

/**
 A director for the Synchronous Reactive (SR) model of computation.
 </p><p>
 The SR director has a <i>period</i> parameter which specifies the
 amount of model time that elapses per iteration. If the value of
 <i>period</i> is 0.0 (the default), then it has no effect, and
 this director never increments time nor calls fireAt() on the
 enclosing director. If the period is greater than 0.0, then
 if this director is at the top level, it increments
 time by this amount in each invocation of postfire().
 If it is not at the top level, then it calls
 fireAt(currentTime + period) in postfire().
 </p><p>
 This behavior gives an interesting use of SR within DE:
 You can "kick start" an SDF submodel with a single
 event, and then if the director of that SDF submodel
 has a period greater than 0.0, then it will continue to fire
 periodically with the specified period.
 </p><p>
 If <i>period</i> is greater than 0.0 and the parameter
 <i>synchronizeToRealTime</i> is set to <code>true</code>,
 then the prefire() method stalls until the real time elapsed
 since the model started matches the period multiplied by
 the iteration count.
 This ensures that the director does not get ahead of real time. However,
 of course, this does not ensure that the director keeps up with real time.

 @author Paul Whitaker, Contributor: Ivan Jeukens, Edward A. Lee, Haiyang Zheng
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Green (pwhitake)
 @Pt.AcceptedRating Green (pwhitake)
 */
public class SRDirector extends FixedPointDirector {
    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public SRDirector() throws IllegalActionException, NameDuplicationException {
        super();
        _init();
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
    public SRDirector(Workspace workspace) throws IllegalActionException,
            NameDuplicationException {
        super(workspace);
        _init();
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
    public SRDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The time period of each iteration.  This parameter has type double
     *  and default value 0.0, which means that this director does not
     *  increment model time and does not request firings by calling
     *  fireAt() on any enclosing director.  If the value is set to
     *  something greater than 0.0, then if this director is at the
     *  top level, it will increment model time by the specified
     *  amount in its postfire() method. If it is not at the top
     *  level, then it will call fireAt() on the enclosing executive
     *  director with the argument being the current time plus the
     *  specified period.
     */
    public Parameter period;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Request a firing of the given actor at the given absolute
     *  time.  This method delegates to the enclosing director
     *  if there is one, and otherwise ignores the request.
     *  @param actor The actor scheduled to be fired.
     *  @param time The scheduled time.
     *  @exception IllegalActionException If the operation is not
     *    permissible (e.g. the given time is in the past).
     */
    public void fireAt(Actor actor, Time time) throws IllegalActionException {
        Actor container = (Actor) getContainer();
        if (container != null) {
            Director executiveDirector = container.getExecutiveDirector();
            if (executiveDirector != null) {
                executiveDirector.fireAt(container, time);
            }
        }
    }

    /** Request a firing of the given actor at the current
     *  time.  This method delegates to the enclosing director
     *  if there is one, and otherwise ignores the request.
     *  @param actor The actor scheduled to be fired.
     *  @exception IllegalActionException If the enclosing director
     *   throws it.
     */
    public void fireAtCurrentTime(Actor actor) throws IllegalActionException {
        Actor container = (Actor) getContainer();
        if (container != null) {
            Director executiveDirector = container.getExecutiveDirector();
            if (executiveDirector != null) {
                executiveDirector.fireAtCurrentTime(container);
            }
        }
    }
    
    /** Return the time value of the next iteration.
     *  If this director is at the top level, then the returned value
     *  is the current time plus the period. Otherwise, this method
     *  delegates to the executive director.
     *  @return The time of the next iteration.
     */
    public Time getModelNextIterationTime() {
        if (!_isTopLevel()) {
            return super.getModelNextIterationTime();
        }
        try {
            double periodValue = ((DoubleToken) period.getToken())
                    .doubleValue();

            if (periodValue > 0.0) {
                return getModelTime().add(periodValue);
            } else {
                return _currentTime;
            }
        } catch (IllegalActionException exception) {
            // This should have been caught by now.
            throw new InternalErrorException(exception);
        }
    }
    
    /** Initialize the director and all deeply contained actors by calling
     *  the super.initialize() method. Reset all private variables.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _realStartTime = System.currentTimeMillis();
    }

    /** Invoke super.prefire() and then synchronize to real time, if appropriate.
     *  @exception IllegalActionException If port methods throw it.
     *  @return true If all of the input ports of the container of this
     *  director have enough tokens.
     */
    public boolean prefire() throws IllegalActionException {
        // Set current time based on the enclosing model.
        boolean result = super.prefire();

        double periodValue = ((DoubleToken) period.getToken()).doubleValue();
        boolean synchronizeValue = ((BooleanToken) synchronizeToRealTime
                .getToken()).booleanValue();

        if ((periodValue > 0.0) && synchronizeValue) {
            synchronized (this) {
                while (true) {
                    long elapsedTime = System.currentTimeMillis()
                            - _realStartTime;

                    // NOTE: We assume that the elapsed time can be
                    // safely cast to a double.  This means that
                    // the SDF domain has an upper limit on running
                    // time of Double.MAX_VALUE milliseconds.
                    double elapsedTimeInSeconds = elapsedTime / 1000.0;
                    double currentTime = getModelTime().getDoubleValue();

                    if (currentTime <= elapsedTimeInSeconds) {
                        break;
                    }

                    long timeToWait = (long) ((currentTime - elapsedTimeInSeconds) * 1000.0);

                    if (_debugging) {
                        _debug("Waiting for real time to pass: " + timeToWait);
                    }

                    try {
                        // NOTE: The built-in Java wait() method
                        // does not release the
                        // locks on the workspace, which would block
                        // UI interactions and may cause deadlocks.
                        // SOLUTION: workspace.wait(object, long).
                        if (timeToWait > 0) {
                            // Bug fix from J. S. Senecal:
                            //
                            //  The problem was that sometimes, the
                            //  method Object.wait(timeout) was called
                            //  with timeout = 0. According to java
                            //  documentation:
                            //
                            // " If timeout is zero, however, then
                            // real time is not taken into
                            // consideration and the thread simply
                            // waits until notified."
                            _workspace.wait(this, timeToWait);
                        }
                    } catch (InterruptedException ex) {
                        // Continue executing.
                    }
                }
            }
        }
        return result;
    }
    
    /** Call postfire() on all contained actors that were fired on the last
     *  invocation of fire().  If <i>synchronizeToRealTime</i> is true, then
     *  wait for real time elapse to match or exceed model time. Return false if the model
     *  has finished executing, either by reaching the iteration limit, or if
     *  no actors in the model return true in postfire(), or if stop has
     *  been requested. This method is called only once for each iteration.
     *  Note that actors are postfired in arbitrary order.
     *  <p>
     *  If the <i>period</i> parameter is greater than 0.0, then
     *  if this director is at the top level, then increment time
     *  by the specified period, and otherwise request a refiring
     *  at the current time plus the period.
     *  @return True if the Director wants to be fired again in the
     *  future.
     *  @exception IllegalActionException If the iterations parameter
     *  does not contain a legal value.
     */
    public boolean postfire() throws IllegalActionException {
        boolean result = super.postfire();
        double periodValue = ((DoubleToken) period.getToken()).doubleValue();
        if (periodValue > 0.0) {
            Actor container = (Actor) getContainer();
            Director executiveDirector = container.getExecutiveDirector();
            Time currentTime = getModelTime();

            if (executiveDirector != null) {
                // Not at the top level.
                executiveDirector.fireAt(container, currentTime
                        .add(periodValue));
            } else {
                // At the top level.
                setModelTime(currentTime.add(periodValue));
            }
        }
        return result;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize the object.   In this case, we give the SDFDirector a
     *  default scheduler of the class SDFScheduler, an iterations
     *  parameter and a vectorizationFactor parameter.
     */
    private void _init() throws IllegalActionException,
            NameDuplicationException {
        period = new Parameter(this, "period", new DoubleToken(1.0));
        period.setTypeEquals(BaseType.DOUBLE);
        period.setExpression("0.0");
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The real time at which the model begins executing. */
    private long _realStartTime = 0L;
}
