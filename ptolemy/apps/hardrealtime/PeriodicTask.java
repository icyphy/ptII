/* A task that releases jobs periodically.

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

import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.util.Time;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// PeriodicTask

/**
 A task that releases jobs with a given deadline and execution requirement
 periodically. The first job is released at time zero by default or
 at a start offset if one is specified.

 @author Christos Stergiou
 @version $Id$
 @Pt.ProposedRating Red (chster)
 @Pt.AcceptedRating Red (chster)
 */
public class PeriodicTask extends TypedAtomicActor implements Task {
    /** Construct an actor with the specified container and name.
     * 
     *  @param container The container.
     *  @param name The name of the actor.
     *  @throws IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @throws NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public PeriodicTask(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        deadline = new Parameter(this, "deadline");
        deadline.setTypeEquals(BaseType.INT);
        deadline.setExpression("10");

        executionTime = new Parameter(this, "executionTime");
        executionTime.setTypeEquals(BaseType.INT);
        executionTime.setExpression("10");

        period = new Parameter(this, "period");
        period.setTypeEquals(BaseType.INT);
        period.setExpression("10");

        startOffset = new Parameter(this, "startOffset");
        startOffset.setTypeEquals(BaseType.INT);
        startOffset.setExpression("0");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The relative deadline of the jobs released by the task. */
    public Parameter deadline;

    /** The execution time required by each job released by the task. */
    public Parameter executionTime;

    /** The period with which the task releases new jobs. */
    public Parameter period;

    /** The time instant that the task releases the first job. */
    public Parameter startOffset;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Update the properties of the jobs released when the task parameters change.
     *  @param attribute The attribute that changed.
     *  @throws IllegalActionException If getting the tokens from parameters
     *   throws it.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == deadline) {
            _deadline = ((IntToken) deadline.getToken()).intValue();
        } else if (attribute == executionTime) {
            _executionTime = ((IntToken) executionTime.getToken()).intValue();
        } else if (attribute == period) {
            _period = ((IntToken) period.getToken()).intValue();
        } else if (attribute == startOffset) {
            _startOffset = ((IntToken) startOffset.getToken()).intValue();
        }
        _drawIcon();
    }

    /** Release a job if the next fire time of the task is equal to the current time.
     *  @throws IllegalActionException If accessing the next fire time or releasing the
     *  job throws it.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        if (_debugging) {
            _debug("PeriodTask " + this + ": nextFireTime is " + nextFireTime()
                    + ", current time is " + _scheduler.getModelTime());
        }

        if (nextFireTime().compareTo(_scheduler.getModelTime()) == 0) {
            releaseJob();
        }
    }

    /** Initialize the last time a job was released and register task
     * with the scheduler.
     * @exception IllegalActionException If thrown by the parent
     * class while invoking the initializable methods (if any).
     */
    @Override
    public void initialize() throws IllegalActionException {
        // Print debugging and invoke any initializable methods.
        super.initialize();
        _lastTimeReleasedJob = null;
        _scheduler = (EDF) ((CompositeActor) getContainer()).getDirector();
        _scheduler.addSchedulableTask(this);
    }

    /** Return the next time the periodic will release a new job.
     *  @return The next time a job will be released.
     *  @throws IllegalActionException If creating the Time objects throws it.
     */
    public Time nextFireTime() throws IllegalActionException {
        if (_lastTimeReleasedJob == null) {
            return new Time(_scheduler, (double) _startOffset);
        } else {
            if (_debugging) {
                _debug("PeriodicTask " + getName()
                        + ": last time job was released "
                        + _lastTimeReleasedJob);
                _debug("PeriodicTask " + getName() + ": period is " + _period);
                _debug("PeriodicTask " + getName()
                        + ": next time job will be released "
                        + (_lastTimeReleasedJob.getDoubleValue() + _period));
            }
            return new Time(_scheduler, _lastTimeReleasedJob.getDoubleValue()
                    + _period);
        }
    }

    /** Release a job to the scheduler and update the last release time.
     *  @throws IllegalActionException If creating a new job throws it.
     */
    public void releaseJob() throws IllegalActionException {
        _scheduler.releaseJob(new Job(_scheduler, _deadline, _executionTime,
                this));
        _lastTimeReleasedJob = _scheduler.getModelTime();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private void _drawIcon() {
        String textFormat;
        int yPosition, yJump;
        if (_startOffset == 0) {
            yPosition = 30;
            yJump = 20;
            textFormat = "<text x=\"%d\" y=\"%d\" style=\"font-size:16; font-family:SansSerif; fill:white\">%s</text>";
        } else {
            yPosition = 25;
            yJump = 15;
            textFormat = "<text x=\"%d\" y=\"%d\" style=\"font-size:13; font-family:SansSerif; fill:white\">%s</text>";
        }
        String iconText = "<svg><circle cx=\"50\" cy=\"50\" r=\"40\"  style=\"fill:blue\"/>";
        iconText += String.format(textFormat, 30, yPosition, "C: "
                + _executionTime);
        yPosition += yJump;
        iconText += String.format(textFormat, 30, yPosition, "D: " + _deadline);
        yPosition += yJump;
        iconText += String.format(textFormat, 30, yPosition, "P: " + _period);
        yPosition += yJump;
        if (_startOffset != 0) {
            iconText += String.format(textFormat, 30, yPosition, "S: "
                    + _startOffset);
        }
        iconText += "</svg>";

        _attachText("_iconDescription", iconText);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private int _deadline;
    private int _executionTime;
    private Time _lastTimeReleasedJob;
    private int _period;
    private EDF _scheduler;
    private int _startOffset;

}
