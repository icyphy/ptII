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
@see classname
@see full-classname
*/
public class CTEmbeddedNRDirector  extends CTMixedSignalDirector
        implements CTEmbeddedDirector{
        /** Construct a CTEmbeddedNRDirector with no name and no Container.
     *  All parameters take their default values. The scheduler is a
     *  CTScheduler.
     */
    public CTEmbeddedNRDirector () {
        super();
    }

    /** Construct a CTEmbeddedNRDirector in the default workspace 
     *  with the given name.
     *  If the name argument is null, then the name is set to the empty
     *  string. The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  All parameters take their default values. The scheduler is a
     *  CTScheduler.
     *
     *  @param name The name of this director.
     */
    public CTEmbeddedNRDirector (String name) {
        super(name);
    }

    /** Construct a CTEmbeddedNRDirector in the given workspace with
     *  the given name.
     *  If the workspace argument is null, use the default workspace.
     *  The director is added to the list of objects in the workspace.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  All parameters take their default values. The scheduler is a
     *  CTScheduler.   
     *
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this director.
     */
    public CTEmbeddedNRDirector (Workspace workspace, String name) {
        super(workspace, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public double getIterBeginTime() {
        return _iterBeginTime;
    }

    /** can only be a embeded director, so check it here.
     */
    public void initialize() throws IllegalActionException {
        if (VERBOSE||DEBUG) {
            System.out.println(this.getFullName() + " initialize.");
        }
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
            if(DEBUG) {
                System.out.println("Director does not have a scheduler.");
            }
            throw new IllegalActionException( this,
            "does not have a scheduler.");
        }
        sch.setValid(false);
        _first = true;
        updateParameters();
        // don't need fireAt to run ahead of time.
        Director exe = ca.getExecutiveDirector();
        double tnow = exe.getCurrentTime();
        //System.out.println(this.getFullName() + 
        //        "Outside Current Time for INIT time" + tnow);
        setCurrentTime(tnow);
        setStartTime(tnow);
        fireAt(null, tnow);
         if (VERBOSE) {
            System.out.println("Director.super initialize.");
        }
        _initialize();
        //System.out.println(this.getFullName() + " Current Time after INIT " +
        //        getCurrentTime()) ;
    }

    /** fire
     */
    public void fire() throws IllegalActionException {
        if (_first) {
            _first = false;
            _eventPhaseExecution();
            produceOutput();
            updateStates();
            //System.out.println(this.getFullName() + 
            //        " THe first step after init.");
            //return;
        }
        if (_firstFireInIter) {
            _eventPhaseExecution();
            _firstFireInIter = false;
            _setIterBeginTime(getCurrentTime());
            _markStates();
        }
        _setFireBeginTime(getCurrentTime());
        setCurrentTime(getIterBeginTime());
        setCurrentStepSize(getSuggestedNextStepSize());
        //System.out.println(this.getFullName() +
        //        " Fire from "+getCurrentTime() +
        //        " to " + getFireEndTime());
        while(true) {
            _processBreakpoints();
            if(DEBUG) {
                System.out.println("Resolved stepsize: "+getCurrentStepSize() +
                                   " One itertion from " + getCurrentTime());
            }
            _fireOneIteration();
            if(Math.abs(getCurrentTime()-getFireEndTime()) <
                    getTimeResolution()) {
                _isFireSuccessful = true;
                return;
            } else if ((getCurrentTime() < getFireEndTime()) &&
                    (_stopByEvent())) {
                //System.out.println( this.getFullName() + 
                //        " stop by event.");
                _isFireSuccessful = false;
                _refinedStep = getCurrentTime() - getIterBeginTime();
                return;
            }
            /*
            if(_isFireSuccessful) {
                if (_stopByEvent()) {
                    //System.out.println( this.getFullName() + 
                    //        " stop by event.");
                    _isFireSuccessful = false;
                    _refinedStep = getCurrentTime() - getIterBeginTime();
                    return;
                } else {
                    _isFireSuccessful = true;
                    return;
                }
            } else {
                _isFireSuccessful = true;
                return;
            }
            */
        }
    }
    /** Return true if this is an embedded director and the current fire
     *  is successful. The success is determined by asking all the 
     *  step size control actors in the output schedule. If this is a 
     *  top level director, then return true always.
     *  @return True if the current step is successful.
     */
    public boolean isThisStepSuccessful() {
        return _isFireSuccessful;
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
        //System.out.println(this.getFullName() +
        //        "postfire...................");
        _firstFireInIter= true;
        _eventPhaseExecution();
        return true;
    }

    /** Return the suggested next step size.
     */
    public double predictedStepSize() {
        return getSuggestedNextStepSize();
    }

    /** prefire, 
     */
    public boolean prefire() throws IllegalActionException {
        if (VERBOSE) {
            System.out.println(this.getFullName() + " prefire. " + 
                    " And Current Time is " + getCurrentTime()); 
        }
        if(STAT) {
            NSTEP++;
        }
        CompositeActor ca = (CompositeActor) getContainer();
        (Thread.currentThread()).yield();
        if(!scheduleValid()) {
            // mutation occurred, redo the schedule;
            CTScheduler scheduler = (CTScheduler)getScheduler();
            if (scheduler == null) {
                throw new IllegalActionException (this,
                "does not have a Scheuler.");
            }
            scheduler.schedule();
            setScheduleValid(true);
        }
        updateParameters();
        double timeAcc = getTimeResolution();
        Director exe = ca.getExecutiveDirector();
        _outsideTime = exe.getCurrentTime();
        if(DEBUG) {
            System.out.println("Outside Time =" + _outsideTime);
        }
        double nextIterTime = exe.getNextIterationTime();
        if(DEBUG) {
            System.out.println("Next Iter Time =" + nextIterTime);
        }
        if(DEBUG) {
            System.out.println("Currnet Time =" + getCurrentTime());
        }
        if(_outsideTime < getCurrentTime()) {
            _rollback();
        }
        _setFireEndTime(nextIterTime);
        fireAt(null, nextIterTime);
        _outsideStepSize = nextIterTime - _outsideTime;
        setSuggestedNextStepSize(_outsideStepSize);
        _firstFireInIter= true;
        // remove the first breakpoint since there's no break.
        //if (!_first) {
            double bp;
            TotallyOrderedSet breakPoints = getBreakPoints();
            if(breakPoints != null) {
                while (!breakPoints.isEmpty()) {
                    bp = ((Double)breakPoints.first()).doubleValue();
                    if(bp < (_outsideTime-getTimeResolution())) {
                        // break point in the past or at now.
                        breakPoints.removeFirst();
                    } else if(Math.abs(bp-_outsideTime) < getTimeResolution()){
                        // break point now!
                        breakPoints.removeFirst();
                    } else {
                        break;
                    }
                }
            } 
            //}
        return true;
    }

    /** Return the refines step size if the current fire is not successful.
     *  @return The refined step size.
     */
    public double refinedStepSize() {
        if(!_isFireSuccessful) {
            return _refinedStep;
        } else {
            return Double.MAX_VALUE;
        }
    } 

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Set the stop time for this iteration. 
     *  For a director that is not at the top-level, set the fire end time
     *  to be the current time  will result 
     *  in that the local director
     *  release the control to the executive director.
     *  @param The fire end time.
     *
    protected void _setFireEndTime(double time ) {
        if(time < getCurrentTime()) {
            throw new InvalidStateException(this,
                " Fire end time" + time + " is less than" +
                " the current time." + getCurrentTime());
        }
        _fireEndTime = time;
    }
    */
    /**
     */
    protected void _setIterBeginTime(double beginTime) {
        _iterBeginTime = beginTime;
    }

    /**
     */
    protected boolean _stopByEvent() {
        if(Math.abs(getCurrentTime()-getFireEndTime())<getTimeResolution()) {
            return false;
        }
        // detected events
        CTScheduler sched = (CTScheduler)getScheduler();
        Enumeration evgens = sched.eventGenerators();
        while(evgens.hasMoreElements()) {
            CTEventGenerator evg = (CTEventGenerator) evgens.nextElement();
            if(evg.hasCurrentEvent()) {
                return true;
            }
        }
        // expected events
        /*
        double bp;
        TotallyOrderedSet breakPoints = getBreakPoints();
        double tnow = getCurrentTime();
        if(breakPoints != null) {
            while (!breakPoints.isEmpty()) {
                bp = ((Double)breakPoints.first()).doubleValue();
                if(bp < (tnow-getTimeResolution())) {
                    // break point in the past or at now.
                    breakPoints.removeFirst();
                } else if(Math.abs(bp-tnow) < getTimeResolution() && 
                          bp < getFireEndTime()){
                    // break point now!
                    return true;
                } else {
                    break;
                }
            }
        }
        */
        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private double _outsideStepSize;

    private double _iterBeginTime;
    
    private boolean _firstFireInIter;
    
    private double _outsideTime;
    
    private double _fireEndTime;

    // whether in the emit event phase;
    private boolean _eventPhase = false;

    // If this fire is successful (not interrupted by events) 
    private boolean _isFireSuccessful = true;
    
    // The refined step size if this fire is not successful
    private double _refinedStep;

    private boolean _first;
}
