/* A task that releases jobs whose deadline and execution time circles
through a predefined set of specifications.

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

import java.util.ArrayList;
import java.util.List;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.util.Time;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.SingletonConfigurableAttribute;

///////////////////////////////////////////////////////////////////
//// MultiFrameTask

/**
 <p>A multiframe task is an edge-labeled and vertex-labeled directed cyclic graph
 together with a current location predicate. The nodes are annotated with task frames,
 the edges are labeled with minimum separation times, and the current location contains
 a specific node of the graph. A task frame {@link TaskFrame} is represented by an
 execution time and a relative deadline.</p>

 <p>During an execution a multiframe task is at the node specified by current location predicate.
 If it has not released a job since the time it entered the node, it may do so at any time.
 The job's deadline and execution time are as specified by the task frame associated with the
 node. After a job is released at a node, the multiframe task waits for time equal to the
 separation time specified by the outgoing edge (there is a unique outgoing edge, since it is
 a cyclic graph) and then moves the current location to the node downstream.</p>

 <p>The minimum separation time of an edge, that connects two nodes A and B, describes then
 the minimum time that can separate a release of a job from node B from a release of a job
 from node A. This class simulates the case where the separation time is always equal to the
 minimum value.</p>

 <p>For more information on multiframe tasks and the schedulability problem of such tasks look
 at: Sanjoy Baruah, Deji Chen, Sergey Gorinsky, and Aloysius Mok (1999).
 Generalized Multiframe Tasks. Real-Time Systems, July 1999, Volume 17, Issue 1, pp 5-22.</p>

 @author Christos Stergiou
 @version $Id$
 @Pt.ProposedRating Red (chster)
 @Pt.AcceptedRating Red (chster)
 */
public class MultiFrameTask extends TypedCompositeActor implements Task {
    /** Construct a multiframe task with the specified container and name.
     *  Construct a dummy director which is visually hidden, in order to make this
     *  an opaque composite.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  @param container The container.
     *  @param name The name of the actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container, if the construction of the director throws it,
     *   or if hiding the director fails.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container, or if the director constructor throws it.
     */
    public MultiFrameTask(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _taskFrames = new ArrayList<TaskFrame>();
        Director director = new Director(workspace());
        director.setContainer(this);
        director.setPersistent(false);
        try {
            (new SingletonConfigurableAttribute(director, "_hide"))
                    .configure(null, null, "true");
        } catch (Exception ex) {
            throw new InternalErrorException(this, ex,
                    "Error trying to hide director.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If it is time, release a new job with the deadline and execution time of the
     *  current task frame.
     *  @exception IllegalActionException
     */
    @Override
    public void fire() throws IllegalActionException {
        if (nextFireTime().compareTo(_scheduler.getModelTime()) == 0) {
            int deadline = _currentFrame._deadline;
            int executionTime = _currentFrame._executionTime;
            _scheduler.releaseJob(new Job(_scheduler, deadline, executionTime,
                    _currentFrame, this));
            _nextFireTime = _scheduler.getModelTime()
                    .add(_currentFrame.getSeparationUntilNextFrame());
            _currentFrame = _currentFrame.getNextFrame();
        }
    }

    /** Initialize the task frames contained by the multiframe task by calling the base class
     *   initialize, set the current frame to the task frame that is set to be initial,
     *   and register this actor as a schedulable task to the director.
     *  @exception IllegalActionException If the director's initialize throws it, since it is
     *   guaranteed that a director is contained, or if more than one task frames are declared
     *   initial.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        for (TaskFrame taskFrame : _taskFrames) {
            if (taskFrame._initial) {
                if (_initialFrame == null) {
                    _initialFrame = taskFrame;
                } else {
                    throw new IllegalActionException(_initialFrame, taskFrame,
                            "Multiframe task contains at least two initial frames");
                }
            }
        }
        _currentFrame = _initialFrame;
        _scheduler.addSchedulableTask(this);
    }

    /** Return true
     *  @return True.
     */
    @Override
    public boolean isOpaque() {
        return true;
    }

    /** Return the next time that the multiframe task will release a job.
     *  @return The next fire time.
     */
    @Override
    public Time nextFireTime() {
        return _nextFireTime;
    }

    /** Clear the task frames, the initial frame, get a reference to the toplevel scheduler,
     *  and set the next fire time to be zero.
     *  @exception IllegalActionException If the superclass preinitialize() throws it.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        _taskFrames.clear();
        _initialFrame = null;
        _scheduler = (EDF) ((CompositeActor) getContainer()).getDirector();
        _nextFireTime = new Time(_scheduler, 0);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add a task frame to the multiframe task.
     *  @param taskFrame The task frame to add.
     */
    protected void _addTaskFrame(TaskFrame taskFrame) {
        _taskFrames.add(taskFrame);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private TaskFrame _currentFrame;
    private TaskFrame _initialFrame;
    private Time _nextFireTime;
    private EDF _scheduler;
    private List<TaskFrame> _taskFrames;
}
