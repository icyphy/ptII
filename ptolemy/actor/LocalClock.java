/* A clock that keeps track of model time at a level of the model hierarchy.

 Copyright (c) 1999-2010 The Regents of the University of California.
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
package ptolemy.actor;

import ptolemy.actor.util.Time;
import ptolemy.kernel.util.IllegalActionException;

/** A clock that keeps track of model time at a level of the model hierarchy
 *  and relates it to the time of the enclosing model, if there is one. The time
 *  of the enclosing model is referred to as the environment time. This
 *  clock has a notion of local time and committed time. The committed time 
 *  is "simultaneous" with the environment time.  
 *  
 *  The local time is
 *  not allowed to move backwards past the committed time, but ahead
 *  of that time, it can move around at will. 
 *  <p>
 *  There is no way of explicitly committing time, but 
 *  several methods have the side effect of committing the current
 *  local time. For example, {@link #setClockDrift(double)} will commit
 *  the current local time and change the clock drift.  So will
 *  {@link #start()} and {@link #stop()}
 *  

 @author Ilge Akkaya, Patricia Derler, Edward A. Lee, Christos Stergiou, Michael Zimmer
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating yellow (eal)
 @Pt.AcceptedRating red (eal)
 */

public class LocalClock {

    /** Create a local clock. 
     *  @param director The associated director.
     */
    public LocalClock(Director director) {
        _director = director;
        
        // Make sure getCurrentTime() never returns null.
        _localTime = _director._zeroTime;
        
        _offset = _director._zeroTime;
        _drift = 1.0;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public method                     ////

    /** Get current local time. If it has never been set, then this will return
     *  Time.NEGATIVE_INFINITY. The returned value may have been set by
     *  {@link #setLocalTime(Time)}.
     */
    public Time getLocalTime() {
        return _localTime;
    }
    
    /** Get the environment time that corresponds to the given local time.
     *  The given local time is required to be either equal to or
     *  greater than the committed time when this method is called.
     *  @param time The local Time.
     *  @return The corresponding environment Time.
     *  @throws IllegalActionException If the specified local time
     *   is in the past, or if Time objects cannot be created.
     */
    public Time getEnvironmentTimeForLocalTime(Time time) throws IllegalActionException {
        if (time.compareTo(_lastCommitLocalTime) < 0) {
            throw new IllegalActionException(
                    "Cannot compute environment time for local time " 
                    + time + " because "
                    + "the last commit of the local time occured at " 
                    + "local time " + _lastCommitLocalTime);
        }
        // FIXME: Use the drift.
        return time.add(_offset);
    }
    
    /** Get the local time that corresponds to the current environment time.
     *  The current environment time is required to be greater than or equal
     *  to the environment time corresponding to the last committed local time.
     *  @return The corresponding local time.
     *  @throws IllegalActionException If Time objects cannot be created, or
     *   if the current environment time is less than the time
     *   corresponding to the last committed local time.
     */
    public Time getLocalTimeForCurrentEnvironmentTime() throws IllegalActionException {   
        return getLocalTimeForEnvironmentTime(_director.getEnvironmentTime()); 
    }
    
    /** Get the local time that corresponds to the given environment time.
     *  The given environment time is required to be greater than or equal
     *  to the environment time corresponding to the last committed local time.
     *  @param time The environment time.
     *  @return The corresponding local time.
     *  @throws IllegalActionException If the specified environment time
     *   is less than the environment time corresponding to the last
     *   committed local time, or if Time objects cannot be created.
     */
    public Time getLocalTimeForEnvironmentTime(Time time) throws IllegalActionException {
        if (time.compareTo(_lastCommitEnvironmentTime) < 0) {
            throw new IllegalActionException(
                    "Cannot compute local time for environment time " 
                    + time + " because "
                    + "the last commit of the local time occured at " 
                    + "local time " + _lastCommitLocalTime + " which " 
                    + "corresponds to environment time " 
                    + _lastCommitEnvironmentTime);
        }
        // FIXME: Use the drift.
        return time.subtract(_offset);
    }
    
    /** Set the new clock drift.
     *  This method asserts that the current local time is
     *  simultaneous with the current environment time.
     *  This method commits current local time.
     *  @param drift New clock drift.  
     *  @throws IllegalActionException If the specified drift is
     *   non-positive.
     */
    public void setClockDrift(double drift) throws IllegalActionException {
        Time environmentTime = _director.getEnvironmentTime();
        if (drift <= 0.0) {
            throw new IllegalActionException(_director,
                    "Illegal clock drift: "
                    + drift
                    + ". Clock drift is required to be positive.");
        }
        _drift = drift; 
        _lastCommitEnvironmentTime = environmentTime;
        _lastCommitLocalTime = _localTime;
    }
    
    /** Set local time without committing.
     *  This is not allowed to set
     *  time earlier than the last committed local time.
     *  @param time The new local time.
     *  @throws IllegalActionException If the specified time is
     *   earlier than the current time.
     */
    public void setLocalTime(Time time) throws IllegalActionException {
        if (_lastCommitLocalTime != null && time.compareTo(_lastCommitLocalTime) < 0) {
            throw new IllegalActionException(_director,
                    "Cannot set local time to "
                    + time
                    + ", which is earlier than the last committed current time "
                    + _lastCommitLocalTime);
        }
        _localTime = time;
    }
    
    /** Start the clock with the current drift as specified by the
     *  last call to {@link #setClockDrift(double)}.
     *  If {@link #setClockDrift(double)} has never been called, then
     *  the drift is 1.0.
     *  This method commits current local time.
     */
    public void start() {
        commit();
    }
    
    /** Stop the clock. The current time will remain the
     *  same as its current value until the next call to
     *  {@link #start()}.
     *  This method commits current local time.
     */
    public void stop() {
        commit();
    }
    
    /** Set local time without committing.
     *  This is allowed to set
     *  time earlier than the last committed local time.
     *  @param time The new local time. 
     */
    public void resetLocalTime(Time time) {
        _localTime = time;
    } 
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    
    /** Commit the current local time. 
     */
    private void commit() {
        Time environmentTime = _director.getEnvironmentTime();  
        _offset = environmentTime.subtract(_localTime);  
        _lastCommitEnvironmentTime = environmentTime; 
        _lastCommitLocalTime = _localTime;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variable                  ////
    
    /** The current time of this clock. */
    private Time _localTime;
    
    /** The associated director. */
    private Director _director;
    
    /** The current clock drift.
     *  The drift is initialized to 1.0 which means that the
     *  local time matches to the environment time.
     */
    private double _drift;
        
    /** The environment time minus the local time at the the point
     *  at which a commit occurred.
     *  By default, the offset is zero.
     */
    private Time _offset;
    
    /** The environment time at which a change to local time, drift,
     *  or resumption occurred.
     */
    private Time _lastCommitEnvironmentTime;

    /** The local time at which a change to local time, drift,
     *  or resumption occurred.
     */
    private Time _lastCommitLocalTime;

}
