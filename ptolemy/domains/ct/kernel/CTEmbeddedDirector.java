/* A non rollback embedded director, for CT inside CT/FSM.

 Copyright (c) 1999 The Regents of the University of California.
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
@ProposedRating Red (liuj@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.kernel;

import ptolemy.domains.ct.kernel.util.*;
import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.actor.sched.*;
import ptolemy.data.expr.*;
import ptolemy.data.*;
import java.util.*;
//import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// CTEmbeddedDirector
/**
Don't need to run ahead of time and don't need to rollback.
Note: This class is under significant redesign, please do not use it
if possible.
@author  liuj
@version $Id$
*/
public class CTEmbeddedDirector  extends CTMultiSolverDirector
    implements CTTransparentDirector{
    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  All the parameters takes their default values.
     */
    public CTEmbeddedDirector() {
        super();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  All the parameters takes their default values.
     *  @param workspace The workspace of this object.
     */
    public CTEmbeddedDirector(Workspace workspace)  {
        super(workspace);
    }

    /** Construct a director in the given container with the given name.
     *  If the container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  All the parameters takes their default values.
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this director.
     *  @exception It may be thrown in derived classes if the
     *      director is not compatible with the specified container.
     */
    public CTEmbeddedDirector(CompositeActor container, String name)
            throws IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true since this director can be inside director.
     *  @return True always.
     */
    public boolean canBeInsideDirector() {
        return true;
    }

    /** Return flase since this director can not be top level director.
     *  @return False always.
     */
    public boolean canBeTopLevelDirector() {
        return false;
    }

    /** In addition to the initialization actions of the super class,
     *  synchronize the local current time with the outside model's
     *  current time.
     *  @exception IllegalActionException If thrown by the super class.
     */
    public void initialize() throws IllegalActionException {
        if(_debugging) _debug(getFullName(), " initialize.");
        CompositeActor ca = (CompositeActor) getContainer();
        //_first = true;
        //if(_debugging) _debug(getName(), " _first is set to true.");
        Director exe = ca.getExecutiveDirector();
        double tnow = exe.getCurrentTime();
        setStartTime(tnow);
        setCurrentTime(tnow);
        super.initialize();
    }

    /** fire
     */
    public void fire() throws IllegalActionException {
        _eventPhaseExecution();
        _prefireSystem();
        /*if(_first) {
          produceOutput();
          return;
          }*/
        ODESolver solver = getCurrentODESolver();
        if(!solver.resolveStates()) {
            _stateAcceptable = false;
            if(_debugging) _debug(getFullName() + "resolve state failed.");
        }

        if(_debugging) _debug(getFullName() + " current time after" +
                " solver.resolveStates() is " + getCurrentTime());
        produceOutput();
    }

    /** Register the break point to this director and the executive
     *  director.
     *  @param actor The actor that requested the fire
     *  @param time The fire time
     *  @exception IllegalActionException If the time is before
     *  the current time, or there is no executive director.
     */
    public void fireAt(Actor actor, double time)
            throws IllegalActionException{
        super.fireAt(actor, time);
        CompositeActor ca = (CompositeActor) getContainer();
        if (ca == null) {
            throw new IllegalActionException(this, "Has no container.");
        }
        Director exeDir = ca.getExecutiveDirector();
        if (exeDir == null) {
            throw new IllegalActionException(this,
                    "Can not be a top-level director.");
        }
        exeDir.fireAt(ca, time);
    }

    /** Return true if this is an embedded director and the current fire
     *  is successful. The success is determined by asking all the
     *  step size control actors in the output schedule. If this is a
     *  top level director, then return true always.
     *  @return True if the current step is successful.
     */
    public boolean isThisStepSuccessful() {
        try {
            _debug(getName() + ": Checking local actors for success.");
            if (!_isStateAcceptable()) {
                //if(_debugging) _debug(getFullName() +
                //        " current step not successful because of STATE.");
                _stateAcceptable = false;
                return false;
            } else if(!_isOutputAcceptable()) {
                //if(_debugging) _debug(getFullName() +
                //        " current step not successful because of OUTPUT.");
                _outputAcceptable = false;
                return false;
            } else {
                _stateAcceptable = true;
                _outputAcceptable = true;
                return true;
            }
        } catch (IllegalActionException ex) {
            throw new InternalErrorException (
                    "Nothing to schedule with out make schedule invalid." +
                    ex.getMessage());
        }
    }

    /** If the current time is greater
     *  than the stop time, an InvalidStateException is thrown.
     *  @return True if this is not a top-level director, or the simulation
     *     is not finished.
     *  @exception IllegalActionException Never thrown.
     */
    public boolean postfire() throws IllegalActionException {
        //super.postfire();
        if(_debugging) _debug(getFullName(), " postfire.");
        _eventPhaseExecution();
        updateStates();

        /*if(_first) {
          _first = false;
          }*/
        /**
           if(!_first) {
           double bp;
           TotallyOrderedSet breakPoints = getBreakPoints();
           if(breakPoints != null) {
           while (!breakPoints.isEmpty()) {
           bp = ((Double)breakPoints.first()).doubleValue();
           if(bp < getCurrentTime() + getTimeResolution()) {
           breakPoints.removeFirst();
           } else {
           // break point in the future, register to the outside.
           CompositeActor ca = (CompositeActor)getContainer();
           Director exe = ca.getExecutiveDirector();
           exe.fireAt(ca, bp);
           break;
           }
           }
           }
           }
        */
        return true;
    }

    /** Return the suggested next step size.
     */
    public double predictedStepSize() {
        try {
            if(_debugging) _debug(getName(), "at " + getCurrentTime(),
                    "predict next step size" + _predictNextStepSize());
            return _predictNextStepSize();
        } catch (IllegalActionException ex) {
            throw new InternalErrorException (
                    "Nothing to schedule with out make schedule invalid." +
                    ex.getMessage());
        }
    }

    /** prefire,
     */
    public boolean prefire() throws IllegalActionException {
        if(_debugging) _debug(this.getFullName() + "prefire.");
        if(STAT) {
            NSTEP++;
        }
        CompositeActor ca = (CompositeActor) getContainer();
        if(!isScheduleValid()) {
            // mutation occurred, redo the schedule;
            CTScheduler scheduler = (CTScheduler)getScheduler();
            if (scheduler == null) {
                throw new IllegalActionException (this,
                        "does not have a Scheduler.");
            }
            scheduler.schedule();
            setScheduleValid(true);
        }
        double timeAcc = getTimeResolution();
        Director exe = ca.getExecutiveDirector();
        _outsideTime = exe.getCurrentTime();
        if(_debugging) _debug(getName(), "Outside Time = "+ _outsideTime);
        double nextIterTime = exe.getNextIterationTime();
        if(_debugging) _debug(getName(), "Next Iter Time = " + nextIterTime);
        setCurrentTime(_outsideTime);
        // if break point now, change solver.
        double bp;
        Double tnow = new Double(_outsideTime);
        TotallyOrderedSet breakPoints = getBreakPoints();
        if(breakPoints != null && !breakPoints.isEmpty()) {
            breakPoints.removeAllLessThan(tnow);
            if(breakPoints.contains(tnow)) {
                if(_debugging) _debug(getName(), " Break point now at" + _outsideTime);
                // Breakpoints iterations are always successful
                // so remove the breakpoints.
                _setCurrentODESolver(getBreakpointSolver());
                breakPoints.removeFirst();
                // does not adjust step size, since the exe-dir should do it.
                _setIsBPIteration(true);
            } else {
                _setCurrentODESolver(getODESolver());
                _setIsBPIteration(false);
            }

        }
        _outsideStepSize = nextIterTime - _outsideTime;
        setCurrentStepSize(_outsideStepSize);
        if(_debugging) _debug(getName(), "at" + getCurrentTime(),
                "breakpt table contains ", getBreakPoints().toString());
        return true;
    }

    /** Return the refines step size if the current fire is not successful.
     *  @return The refined step size.
     */
    public double refinedStepSize() {
        try {
            if(!_stateAcceptable) {
                return _refinedStepWRTState();
            } else if(!_outputAcceptable){
                return _refinedStepWRTOutput();
            } else {
                return Double.MAX_VALUE;
            }
        } catch( IllegalActionException ex) {
            throw new InternalErrorException (
                    "Nothing to schedule with out make schedule invalid." +
                    ex.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private double _outsideStepSize;

    private boolean _outputAcceptable;

    private boolean _stateAcceptable;

    private double _outsideTime;
}
