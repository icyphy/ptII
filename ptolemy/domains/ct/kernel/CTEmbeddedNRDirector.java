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
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
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
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// CTEmbeddedNRDirector
/**
Don't need to run ahead of time and don't need to rollback.
@author  liuj
@version $Id$
*/
public class CTEmbeddedNRDirector  extends CTMultiSolverDirector
        implements CTEmbeddedDirector{
    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  All the parameters takes their default values.
     */
    public CTEmbeddedNRDirector() {
        super();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  All the parameters takes their default values.
     *  @param workspace The workspace of this object.
     */
    public CTEmbeddedNRDirector(Workspace workspace)  {
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
    public CTEmbeddedNRDirector(CompositeActor container, String name)
            throws IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public double getIterEndTime() {
        return _iterEndTime;
    }

    /** can only be a embeded director, so check it here.
     */
    public void initialize() throws IllegalActionException {
        _debug(this.getFullName() + " initialize.");
        CompositeActor ca = (CompositeActor) getContainer();
        if (ca == null) {
            throw new IllegalActionException(this, "Has no container.");
        }
        if (!(ca instanceof CTCompositeActor)) {
            throw new IllegalActionException(this, 
                    "must be the director of a CTCompositeActor.");
        }
        if (ca.getExecutiveDirector() == null) {
            throw new IllegalActionException(this, 
            "Can not be top-level director.");
        }
        CTScheduler sch = (CTScheduler)getScheduler();
        if (sch == null) {
            _debug("Director does not have a scheduler.");
            throw new IllegalActionException( this,
            "does not have a scheduler.");
        }
        sch.setValid(false);
        _first = true;
        _debug(getName() + " _first is set to true.");
        // don't need fireAt to run ahead of time.
        Director exe = ca.getExecutiveDirector();
        double tnow = exe.getCurrentTime();
        setStartTime(tnow);
        _debug("Director.super initialize.");
        _initialize();
    }

    /** fire
     */
    public void fire() throws IllegalActionException {
       
        _eventPhaseExecution();
        _prefireSystem();
        ODESolver solver = getCurrentODESolver();
        if(!solver.resolveStates()) {
            _stateAcceptable = false;
            //_debug(getFullName() + "resolve state failed.");
        }
        
        _debug(getFullName() + " current time after" +
                    " solver.resolveStates() is " + getCurrentTime());
        //setCurrentTime(getIterEndTime());
        produceOutput();
    }
    /** Return true if this is an embedded director and the current fire
     *  is successful. The success is determined by asking all the 
     *  step size control actors in the output schedule. If this is a 
     *  top level director, then return true always.
     *  @return True if the current step is successful.
     */
    public boolean isThisStepSuccessful() {
        try {
            if (!_isStateAcceptable()) {
                //_debug(getFullName() + 
                //        " current step not successful because of STATE."); 
                _stateAcceptable = false;
                return false;
            } else if(!_isOutputAcceptable()) {
                //_debug(getFullName() + 
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

    /** If this is a top-level director, returns true if the current time
     *  is less than the stop time, otherwise, return true always.
     *  If this is a top-level director, and the current time is greater
     *  than the stop time, an InvalidStateException is thrown. 
     *  @return True if this is not a top-level director, or the simulation
     *     is not finished.
     *  @exception IllegalActionException Never thrown.
     */
    public boolean postfire() throws IllegalActionException {
        //super.postfire();
        _debug(this.getFullName() + " postfire.");
        _eventPhaseExecution();
        updateStates();
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
        return true;
    }

    /** Return the suggested next step size.
     */
    public double predictedStepSize() {
        try {
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
        _debug(this.getFullName() + "prefire.");
        if(STAT) {
            NSTEP++;
        }
        CompositeActor ca = (CompositeActor) getContainer();
        (Thread.currentThread()).yield();
        if(!isScheduleValid()) {
            // mutation occurred, redo the schedule;
            CTScheduler scheduler = (CTScheduler)getScheduler();
            if (scheduler == null) {
                throw new IllegalActionException (this,
                "does not have a Scheuler.");
            }
            scheduler.schedule();
            setScheduleValid(true);
        }
        double timeAcc = getTimeResolution();
        Director exe = ca.getExecutiveDirector();
        _outsideTime = exe.getCurrentTime();
        _debug("Outside Time =" + _outsideTime);
        double nextIterTime = exe.getNextIterationTime();
        _debug("Next Iter Time =" + nextIterTime);
        /**
        if(_outsideTime < getCurrentTime()-timeAcc) {
            throw new IllegalActionException(exe, this,
                    " time collapse. The outside time is " +
                    _outsideTime + ", but the local time is " +
                    getCurrentTime());
        }
        */
       
        // if break point now, change solver.
        double bp;
        TotallyOrderedSet breakPoints = getBreakPoints();
        if(breakPoints != null) {
            while (!breakPoints.isEmpty()) {
                bp = ((Double)breakPoints.first()).doubleValue();
                if(Math.abs(bp - getCurrentTime()) < getTimeResolution()) {
                    _setCurrentODESolver(getBreakpointSolver());
                    //_debug(getFullName() + 
                    //        " Change to break point solver " + 
                    //        getCurrentODESolver().getFullName());
                    break;
                } else {
                    break;
                }
            }
        }    
        setCurrentTime(_outsideTime);
        _outsideStepSize = nextIterTime - _outsideTime;
        setCurrentStepSize(_outsideStepSize);
        if(_first) { // && (getCurrentTime() == getStartTime())) {
            produceOutput();
            updateStates();
            _first = false;
        }
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
    ////                         protected methods                 ////

    /**
     */
    protected void _setIterEndTime(double endTime) {
        _iterEndTime = endTime;
    }

 
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private double _outsideStepSize;

    private boolean _outputAcceptable;
    
    private boolean _stateAcceptable;
    
    private double _outsideTime;
    
    private double _iterEndTime;

    // whether in the emit event phase;
    private boolean _eventPhase = false;

    // If this fire is successful (not interrupted by events) 
    private boolean _isFireSuccessful = true;
    
    // The refined step size if this fire is not successful
    private double _refinedStep;

    //private boolean _first;
}
