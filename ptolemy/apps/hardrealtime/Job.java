/* A unit of execution demand on the processor.

@Copyright (c) 2013 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

package ptolemy.apps.hardrealtime;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.util.Time;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// Job

/**
 A job represents a block of execution demand on the processing units of a
 hard real-time system. It is characterized by an execution time, which is the
 number of units of time that the job needs to be executed, an arrival time, which
 is the time when the job became available to the processor for processing, and a
 deadline, which the time by which the processing of the job has to be completed.

 @author Christos Stergiou
 @version $Id$
 @Pt.ProposedRating Red (chster)
 @Pt.AcceptedRating Red (chster)
 */
public class Job {
    /** Construct a new job released by a task, with an execution time, a relative deadline,
     *  and release time equal to the current time of a director.
     *  @param director The director whose current time is the release time of the job.
     *  @param relativeDeadline The relative deadline of the job.
     *  @param executionTime The execution time of the job.
     *  @param task The task that released the job.
     *  @throws IllegalActionException If the Time constructor throws it.
     */
    public Job(Director director, double relativeDeadline,
            double executionTime, Actor task) throws IllegalActionException {
        this(director, relativeDeadline, executionTime, task, null);
    }

    /** Construct a new job released by a task contained in a parent task, with an execution
     *  time, a relative deadline, and release time equal to the current time of a director.
     *  @param director The director whose current time is the release time of the job.
     *  @param relativeDeadline The relative deadline of the job.
     *  @param executionTime The execution time of the job.
     *  @param task The task that released the job.
     *  @param parentTask The parent of the task that released the job.
     *  @throws IllegalActionException If the Time constructor throws it.
     */
    public Job(Director director, double relativeDeadline,
            double executionTime, Actor task, Actor parentTask)
                    throws IllegalActionException {
        this._startTime = director.getModelTime();
        this._absoluteDeadline = this._startTime.add(relativeDeadline);
        this._remainingTime = new Time(director, executionTime);
        this._task = task;
        this._parentTask = parentTask;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Execute the job for an amount of time.
     *  @param time The time to execute the job for.
     */
    public void executeFor(Time time) {
        if (time.compareTo(_remainingTime) > 0) {
            throw new IllegalArgumentException("time is " + time
                    + " and remaining time is " + _remainingTime);
        }
        _remainingTime = _remainingTime.subtract(time);
    }

    /** Return the absolute deadline of the job.
     *  @return The absolute deadline.
     */
    public Time getAbsoluteDeadline() {
        return this._absoluteDeadline;
    }

    /** Return the parent of the task that released the job.
     *  @return The parent of the task that released the job.
     */
    public Actor getParentTask() {
        return _parentTask;
    }

    /** Return the time the job still needs to execute for to complete its execution.
     *  @return The remaining execution time.
     */
    public Time getRemainingTime() {
        return _remainingTime;
    }

    /** Return the time that the job was released.
     *  @return The time the job was released.
     */
    public Time getStartTime() {
        return _startTime;
    }

    /** Return the task that released the job.
     *  @return The task that released the job.
     */
    public Actor getTask() {
        return _task;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Time _absoluteDeadline;
    private Actor _parentTask;
    private Time _remainingTime;
    private Time _startTime;
    private Actor _task;
}
