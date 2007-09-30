/* Director for the Synchronous Reactive model of computation.

 Copyright (c) 2000-2006 The Regents of the University of California.
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
 If it is not at the top level, then it uses fireAt() to
 request that it be refired at multiples of the period, starting
 at time 0.0.
 </p><p>
 This behavior gives an interesting use of SR within DE, where
 the SR submodel can be automatically invoked periodically.
 If the period is greater than zero when initialize() is invoked,
 then the director will request periodic firings of the enclosing
 DE director.
 If at any time, the period is set to zero, the periodic firings
 will cease.  Correspondingly, if they started at zero, and are
 subsequently set to something other than zero, then the periodic
 firings will start the next time the actor fires (it will not
 fire spontaneously).

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
        Actor container = (Actor)getContainer();
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
        Actor container = (Actor)getContainer();
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
    
    /** Override the base class to request a first firing if the
     *  <i>period</i> is greater than zero.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        double periodValue = ((DoubleToken) period.getToken()).doubleValue();
        if (periodValue > 0.0) {
            Actor container = (Actor) getContainer();
            Director executiveDirector = container.getExecutiveDirector();
            if (executiveDirector != null) {
                // Not at the top level.
                executiveDirector.fireAtCurrentTime(container);
            }
            _lastFireAtTime = getModelTime();
        } else {
            _lastFireAtTime = null;
        }
    }

    /** If the <i>period</i> parameter is greater than 0.0, then
     *  if this director is at the top level, then increment time
     *  by the specified period, and otherwise request a refiring
     *  at the current time plus the period.
     *  @return True if the Director wants to be fired again in the
     *   future.
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
            // Perform the refire request only if time matches the
            // last refire time.
            if (_lastFireAtTime == null || currentTime.equals(_lastFireAtTime)) {
                _lastFireAtTime = currentTime.add(periodValue);
                if (executiveDirector != null) {
                    // Not at the top level.
                    executiveDirector.fireAt(container, _lastFireAtTime);
                } else {
                    // At the top level.
                    setModelTime(_lastFireAtTime);
                }
            }
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize the object by creating parameters.
     */
    private void _init() throws IllegalActionException,
            NameDuplicationException {
        period = new Parameter(this, "period", new DoubleToken(1.0));
        period.setTypeEquals(BaseType.DOUBLE);
        period.setExpression("0.0");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** The last time at which this actor has requested a refiring. */
    private Time _lastFireAtTime = null;
}
