/* A CT Director that handles the interaction with event based domains.

Copyright (c) 1998-2004 The Regents of the University of California.
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

package ptolemy.domains.ct.kernel;

import java.util.Iterator;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.ct.kernel.util.TotallyOrderedSet;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// CTMixedSignalDirector
/**
   This is a CTDirector that supports the interaction of the continuous-time
   simulation with event-based domains. This director can both serve as
   a top-level director and an inside director that is contained by
   a composite actor in an event-based domain. If it is a top-level
   director, it acts exactly like a CTMultiSolverDirector. If it is
   embedded in another event-based domain, it will run ahead of the global
   time and prepare to roll back if necessary.
   <P>
   This class has an extra parameter as compare to the CTMultiSolverDirector,
   which is the maximum run ahead of time length (<code>runAheadLength</code>).
   The default value is 1.0.
   <P>
   The running ahead of time is achieved by the following mechanism.<br>
   <UL>
   <LI> At the initialize stage of the execution, the director will request
   a fire at the global current time.
   <LI> At each prefire stage of the execution, the fire end time is computed
   based on the current time of the executive director, t1, the next iteration
   time of the executive director, t2, and the value of the parameter
   <code>runAheadLength</code>, t3. The fire end time is t1+min(t2, t3)
   <LI> At the prefire stage, the local current time is compared with the
   current time of the executive director. If the local time is later than
   the executive director time, then the directed system will rollback to a
   "known good" state.
   <LI> The "known good" state is the state of the system at the time when
   local time is equal to the current time of the executive director.
   <LI> At the fire stage, the director will stop at the first of the
   following two times, the fire end time and the first detected event time.
   </UL>

   @author  Jie Liu, Haiyang Zheng
   @version $Id$
   @since Ptolemy II 0.2
   @Pt.ProposedRating Green (liuj)
   @Pt.AcceptedRating Green (chf)
*/
public class CTMixedSignalDirector extends CTMultiSolverDirector {
    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  All the parameters take their default values.
     */
    public CTMixedSignalDirector() {
        super();
    }

    /** Construct a director in the workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  All the parameters take their default values.
     *  @param workspace The workspace of this object.
     */
    public CTMixedSignalDirector(Workspace workspace) {
        super(workspace);
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  All the parameters take their default values.
     *  @param container The container.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container.  May be thrown in a derived class.
     *  @exception NameDuplicationException If the container is not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public CTMixedSignalDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                           parameters                      ////

    /** Parameter of the run ahead length. The default value is 1.0.
     */
    public Parameter runAheadLength;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute. If the changed attribute matches
     *  a parameter of the director, then the corresponding private copy of the
     *  parameter value will be updated.
     *  @param attribute The changed attribute.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == runAheadLength) {
            if (_debugging) _debug("run ahead length updating.");
            double value =
                ((DoubleToken)runAheadLength.getToken()).doubleValue();
            if (value < 0) {
                throw new IllegalActionException(this,
                        " runAheadLength cannot be negative.");
            }
            _runAheadLength = value;
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Return true indicating that this director can be an inside director.
     *  @return True always.
     */
    public boolean canBeInsideDirector() {
        return true;
    }

    // FIXME: This method may not be necessary.
    /** If this is a top-level director, behave exactly as a
     *  CTMultiSolverDirector, otherwise always return true.
     *  @return True if this is not a top-level director or the simulation
     *     is not finished.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public boolean postfire() throws IllegalActionException {
        if (_isTopLevel()) {
            return super.postfire();
        } else {
            Time firstBreakPoint = (Time) getBreakPoints().first();
            CompositeActor container = (CompositeActor) getContainer();
            container.getExecutiveDirector().fireAt(container, firstBreakPoint);
            return super.postfire();
        }
    }

    /** Always returns true, indicating that the (sub)system is always ready
     *  for one iteration. The schedule is recomputed if there are mutations
     *  that occurred after last iteration. Note that mutations can only
     *  occur between iterations in the CT domain.
     *  <P>
     *  If this is not a top-level director, some additional work is done
     *  to synchronize time with the executive director. In particular,
     *  it will compare its local time, say t, with the current time
     *  of the executive director, say t0.
     *  If t == t0, do nothing. <BR>
     *  If t > t0, then rollback to the "known good" time (which should be
     *  less than the outside time) and catch up
     *  to the outside time. <BR>
     *  If t < t0, then throw an exception because the CT subsystem
     *  should always run ahead of time. <BR>
     *  <P>
     *  The iteration end time is computed. If this is a
     *  top-level director, the iteration end time is the (local) current
     *  time plus the (local) current step size.
     *  If this director is not a top-level director, the time is
     *  resolved from the current time of the outside domains, say t1,
     *  the next iteration time of the outside domain, say t2, and
     *  the runAheadLength parameter of this director, say t3.
     *  The iteration end time is set
     *  to be <code>t1 + min(t2, t3)</code>. The iteration end time may be
     *  further refined by the fire() method due to event detection.
     *  In particular, when the first event is detected, say at t4,
     *  then the iteration end time is set to t4.
     *  @return true Always
     *  @exception IllegalActionException If the local time is
     *       less than the current time of the executive director,
     *       or thrown by a directed actor.
     */
    public boolean prefire() throws IllegalActionException {
        if (!_isTopLevel()) {
            if (_debugging) _debug(getName(), " prefire: <<<");
            CompositeActor container = (CompositeActor) getContainer();
            // ca should have beed checked in _isTopLevel()
            Director exe = container.getExecutiveDirector();
            _outsideTime = exe.getCurrentTimeObject();
            double timeResolution = getTimeResolution();
            Time nextIterationTime = exe.getNextIterationTimeObject();
    
            double aheadLength 
                = nextIterationTime.subtract(_outsideTime).getTimeValue();
            if (_debugging) _debug(getName(),
                    " current time = " + getCurrentTimeObject(),
                    " Outside Time = " + _outsideTime,
                    " NextIterationTime = " + nextIterationTime +
                    " Inferred run length = " + aheadLength);
    
            if (aheadLength < timeResolution ) {
                // We should use the runAheadLength parameter.
                // FIXME: what is this about?
                // In fact, it does not matter which step size to use,
                // since it is a discrete phase execution.
                setSuggestedNextStepSize(
                    _outsideTime.add(_runAheadLength).getTimeValue());
                if (_debugging) _debug( "Outside next iteration length",
                        " is zero. We proceed any way with length "
                        + _runAheadLength);
            } else if (aheadLength < _runAheadLength) {
                setSuggestedNextStepSize(nextIterationTime.getTimeValue());
            } else {
                // aheadLength > _runAheadLength parameter.
                setSuggestedNextStepSize(
                    _outsideTime.add(_runAheadLength).getTimeValue());
            }
            // Now it is safe to execute the continuous part.
            if (_debugging) _debug(getName(), "End of Prefire. >>>");
            return super.prefire();
        }
        if (_debugging) _debug(getName(), " End of prefire. >>>");
        return super.prefire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Initialize parameters in addition to the parameters inherited
     *  from CTMultiSolverDirector. In this class the additional
     *  parameter is the maximum run ahead time length
     *  (<code>runAheadLength</code>). The default value is 1.0.
     */
    protected void _initParameters() {
        super._initParameters();
        try {
            _runAheadLength = 0.1;
            runAheadLength = new Parameter(this,
                    "runAheadLength", new DoubleToken(_runAheadLength));
            runAheadLength.setTypeEquals(BaseType.DOUBLE);
        } catch (IllegalActionException e) {
            //Should never happens. The parameters are always compatible.
            throw new InternalErrorException("Parameter creation error.");
        } catch (NameDuplicationException ex) {
            throw new InvalidStateException(this,
                    "Parameter name duplication.");
        }
    }

    /** Mark the current state as the known good state. Call the
     *  markStates() method on all CTStatefulActors. Save the current time
     *  as the "known good" time.
     *  @exception IllegalActionException If thrown by the scheduler.
     */
    protected void _markStates() throws IllegalActionException {
        CTSchedule schedule = (CTSchedule) getScheduler().getSchedule();
        Iterator actors = schedule.get(
                CTSchedule.STATEFUL_ACTORS).actorIterator();
        while (actors.hasNext()) {
            CTStatefulActor actor = (CTStatefulActor)actors.next();
            if (_debugging) _debug("Save State..."+
                    ((Nameable)actor).getName());
            actor.markState();
        }
        _knownGoodTime = getCurrentTimeObject();
    }

    /** The number of rollbacks. Used for statistics.
     */
    protected int _Nroll = 0;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The held output tokens. It maps receivers to the tokens they
    // contains.
    // private HashMap _heldTokens = new HashMap();

    // Indicate whether there are pending discrete events.
    private boolean _hasDiscreteEvents;

    // Indicate whether this is the second time that prefire has been
    // called in a row.
    private boolean _secondPrefire = false;

    // The version of mutation. If this version is not the workspace
    // version then every thing related to mutation need to be updated.
    private long _mutationVersion = -1;

    // Indicate if this is the top level director.
    private boolean _isTop;

    // The time for the "known good" state.
    private Time _knownGoodTime;

    // The current outside time.
    private Time _outsideTime;

    // The local variable of the run ahead length parameter.
    private double _runAheadLength;

    // The end time of an iteration.
    private Time _iterationEndTime;

    // Indicate whether this is an event phase;
    //private boolean _inEventPhase = false;
}
