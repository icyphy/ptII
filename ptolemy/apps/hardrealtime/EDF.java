/* Director that implements earliest deadline first scheduling in a hard real-time system.

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
import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;

import ptolemy.actor.Director;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// EDF

/**
 <p>A hard real-time system consists of concurrently executing actors
 that release jobs. Jobs have deadlines and represent demands for
 execution by the processing elements that comprise the platform
 that the system is running on.
 </p><p>
 A scheduling policy determines the way that the available jobs are allocated
 on the processing elements. The EDF policy on a uniprocessor platform dictates
 that the job that has the smallest deadline should be given priority over
 the other jobs. Ties are broken arbitrarily.
 </p>

 @author Christos Stergiou
 @version $Id$
 @Pt.ProposedRating Red (chster)
 @Pt.AcceptedRating Red (chster)
 */
public class EDF extends Director {
    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @throws IllegalActionException If the
     *   director is not compatible with the specified container.
     *  @throws NameDuplicationException If the container not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public EDF(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _jobs = new ArrayList<Job>();
        _taskPlotEditorFactory = new TaskPlotEditorFactory(this,
                this.uniqueName("_taskPlotEditorFactory"));
        _dbfPlotEditorFactor = new DBFPlotEditorFactory(this, 
                this.uniqueName("_dbfPlotEditorFactory"));
        _schedulableTasks = new ArrayList<Task>();
        createPlot = new Parameter(this, "Create Plot");
        createPlot.setTypeEquals(BaseType.BOOLEAN);
        createPlot.setExpression("true");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** Boolean parameter that controls whether a plot of the execution is displayed. */
    public Parameter createPlot;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a task to the set of schedulable tasks, i.e. the tasks that might release a
     *  job in the system.
     *  @param task The task to add to the schedulable tasks.
     */
    public void addSchedulableTask(Task task) {
        _schedulableTasks.add(task);
    }

    /** Fire the schedulable tasks contained in the real-time system.
     *  @throws IllegalActionException If the fire of one of the tasks contained by the platform
     *  throws it.
     */
    @Override
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("EDF: Called fire().");
        }
        for (Task a : _schedulableTasks) {
            a.fire();
        }
    }

    /** Check that contained actors are periodic tasks or multiframe tasks and create a task plot
     *  to display the system's execution.
     *  @throws IllegalActionException If the composite actor contains actors that are not
     *  periodic tasks or multiframe tasks, if the top level effigy cannot be found, or if
     *  getting the model stop time throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        for (Iterator iterator = ((TypedCompositeActor) getContainer())
                .entityList().iterator(); iterator.hasNext();) {
            Object entity = iterator.next();
            if (!(entity instanceof PeriodicTask)
                    && !(entity instanceof MultiFrameTask)) {
                throw new IllegalActionException(this,
                        "Can only handle Periodic tasks and multiframe tasks");
            }
        }

        if (((BooleanToken) createPlot.getToken()).booleanValue()) {
            Effigy effigy = Configuration.findEffigy(toplevel());
            if (effigy != null && effigy.numberOfOpenTableaux() > 0) {
                JFrame jframe = effigy.entityList(Tableau.class).get(0)
                        .getFrame();
                _taskPlotEditorFactory.createEditor(this, jframe);
                _dbfPlotEditorFactor.createEditor(this, jframe);
            } else {
                throw new IllegalActionException(this,
                        "Can't find top level effigy or any open tableaux");
            }
        }

        Time modelStopTime = getModelStopTime();
        if (modelStopTime != Time.POSITIVE_INFINITY) {
            double stopTimeDouble = modelStopTime.getDoubleValue();
            _simulationEndTime = (int) Math.floor(stopTimeDouble);
        } else {
            _simulationEndTime = 100;
        }
        if (_debugging) {
            _debug("simulation end time is " + _simulationEndTime);
        }

        if (_taskPlotEditorFactory.getTaskPlot() != null) {
            _taskPlotEditorFactory.getTaskPlot().clear();
        }
        
        if (_dbfPlotEditorFactor.getTaskPlot() != null) {
            _dbfPlotEditorFactor.getTaskPlot().clear();
        }
    }

    /** Check if a deadline has been missed, if the current job finished its execution,
     *  choose the smallest deadline job, and execute it until a new job is to be released,
     *  or the current job finishes executing.
     *  @return False if a deadline has been missed or
     *  time has advanced beyond the simulation end time.
     *  @throws IllegalActionException If the Time constructor, getting the next release times,
     *  or setting model throw it.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        Time currentTime = getModelTime();

        if (_debugging) {
            _debug("EDF: time is: " + getModelTime());
            _debug("EDF: current job is " + _currentJob);
            _debug("EDF: released jobs: " + _jobs);
        }

        if (_currentJob != null
                && (currentTime.compareTo(_currentJob.getAbsoluteDeadline()) > 0 || currentTime
                        .compareTo(_currentJob.getAbsoluteDeadline()) == 0
                        && !_currentJob.getRemainingTime().isZero())) {
            if (_debugging) {
                _debug("EDF: current job missed its deadline!");
            }
            // Deadline miss
            if (_taskPlotEditorFactory.getTaskPlot() != null) {
                _taskPlotEditorFactory.getTaskPlot().addExecution(
                        _timeCurrentJobStarted.getDoubleValue(),
                        currentTime.getDoubleValue(), _currentJob.getTask());
                _taskPlotEditorFactory.getTaskPlot().addDeadlineMiss(
                        currentTime.getDoubleValue(), _currentJob.getTask());
            }
            return false;
        }

        // Check if current job is finished
        if (_currentJob != null && _currentJob.getRemainingTime().isZero()) {
            _jobs.remove(_currentJob);
            if (_taskPlotEditorFactory.getTaskPlot() != null) {
                _taskPlotEditorFactory.getTaskPlot().addExecution(
                        _timeCurrentJobStarted.getDoubleValue(),
                        currentTime.getDoubleValue(), _currentJob.getTask());
            }
            _currentJob = null;
        }

        if (!_jobs.isEmpty()) {
            Job minimumDeadlineJob = _minimumDeadlineJob();
            _debug("EDF: minimum deadline job is " + minimumDeadlineJob);
            if (_currentJob == null) {
                _currentJob = minimumDeadlineJob;
                _timeCurrentJobStarted = currentTime;
            } else if (_currentJob != minimumDeadlineJob) {
                if (_taskPlotEditorFactory.getTaskPlot() != null) {
                    _taskPlotEditorFactory.getTaskPlot()
                    .addExecution(
                            _timeCurrentJobStarted.getDoubleValue(),
                            currentTime.getDoubleValue(),
                            _currentJob.getTask());
                }
                _currentJob = minimumDeadlineJob;
                _timeCurrentJobStarted = currentTime;
            }
        }

        Time minNextTime = _timeOfNextJob();
        // Check when currently executing job finishes
        if (_currentJob != null) {
            minNextTime = new Time(this, Math.min(minNextTime.getDoubleValue(),
                    currentTime.getDoubleValue()
                    + _currentJob.getRemainingTime().getDoubleValue()));
        }

        if (currentTime.getDoubleValue() > _simulationEndTime) {
            return false;
        }

        if (_currentJob != null) {
            _currentJob.executeFor(new Time(this, minNextTime.getLongValue()
                    - currentTime.getLongValue()));
        }

        // Advance time
        if (_debugging) {
            _debug("EDF: advancing time to " + minNextTime);
        }
        setModelTime(minNextTime);
        return true;
    }

    /** Clear current job, released jobs, and schedulable tasks.
     *  @throws IllegalActionException If the preinitialize() method of
     *  one of the associated actors throws it.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        if (_debugging) {
            _debug("clearing jobs, schedulable tasks, current job, resetting current time to 0");
        }
        _currentJob = null;
        _jobs.clear();
        _schedulableTasks.clear();
    }

    /** Release a job.
     *  @param job The job to release.
     */
    public void releaseJob(Job job) {
        _jobs.add(job);
        if (_taskPlotEditorFactory.getTaskPlot() != null) {
            _taskPlotEditorFactory.getTaskPlot().addJob(job);
            _dbfPlotEditorFactor.getTaskPlot().addJob(job);
        }
        if (_debugging) {
            _debug("Added job " + job);
        }
    }

    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        _dbfPlotEditorFactor.getTaskPlot().finalize();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The job that is currently executing on the processor. */
    protected Job _currentJob;

    /** The time that the current job started executing. */
    protected Time _timeCurrentJobStarted = null;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private Job _minimumDeadlineJob() {
        if (_jobs.size() == 0) {
            return null;
        }
        Job minDeadlineJob = _jobs.get(0);
        for (Job job : _jobs) {
            if (minDeadlineJob.getAbsoluteDeadline().compareTo(
                    job.getAbsoluteDeadline()) > 0) {
                minDeadlineJob = job;
            }
        }
        return minDeadlineJob;
    }

    private Time _timeOfNextJob() throws IllegalActionException {
        Time minNextTime = Time.POSITIVE_INFINITY;
        for (Task a : _schedulableTasks) {
            Time nextTime = a.nextFireTime();
            if (_debugging) {
                _debug("EDF: task " + a + " next job will be at " + nextTime);
            }
            if (nextTime.compareTo(minNextTime) < 0) {
                minNextTime = nextTime;
            }
        }
        if (_debugging) {
            _debug("EDF: next job will be at " + minNextTime);
        }
        return minNextTime;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private List<Job> _jobs;
    private List<Task> _schedulableTasks;
    private int _simulationEndTime = 20;
    private TaskPlotEditorFactory _taskPlotEditorFactory;
    private DBFPlotEditorFactory _dbfPlotEditorFactor;
}
