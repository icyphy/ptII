/* A real-time system execution plotter.

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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ptolemy.actor.Actor;
import ptolemy.plot.PlotBox;

///////////////////////////////////////////////////////////////////
//// TaskPlot

/**
 A task execution plotter. The plot displays releases and deadlines
 of jobs, allocation intervals of the processor to each job, and
 deadline misses. Jobs and processor allocations are grouped per task,
 and each task occupies a row in the drawing. If a task is associated
 with different types of jobs, such as the case of multiframe tasks,
 each job type occupies a separate row.

 @author Christos Stergiou
 @version $Id$
 @Pt.ProposedRating Red (chster)
 @Pt.AcceptedRating Red (chster)
 */
@SuppressWarnings("serial")
public class TaskPlot extends PlotBox {
    /** Construct a task plot. */
    public TaskPlot() {
        super();
        setAutomaticRescale(true);
        _jobs = new ArrayList<Job>();
        _tasks = new ArrayList<Actor>();
        _jobsPerTask = new HashMap<Actor, List<Job>>();
        _executionsPerTask = new HashMap<Actor, List<Execution>>();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a deadline miss event in the execution drawn.
     *  @param time The time at which the deadline miss occurs.
     *  @param task The task that released the job that missed its deadline.
     */
    public void addDeadlineMiss(double time, Actor task) {
        _deadlineMissTask = task;
        _deadlineMissTime = time;
    }

    /** Add an allocation interval of the processor to a task.
     *  @param start The start time of the allocation interval.
     *  @param end The end time of the allocation interval.
     *  @param task The task that the processor is allocated to.
     *  @throws IllegalArgumentException If the task whose execution is added
     *  has not released a job yet.
     */
    public void addExecution(double start, double end, Actor task)
            throws IllegalArgumentException {
        if (!_tasks.contains(task)) {
            throw new IllegalArgumentException(
                    "Attempt to register execution time for unknown task.");
        }
        Execution execution = new Execution(start, end);
        if (_executionsPerTask.keySet().contains(task)) {
            _executionsPerTask.get(task).add(execution);
        } else {
            List<Execution> executions = new ArrayList<Execution>();
            executions.add(execution);
            _executionsPerTask.put(task, executions);
        }
        _maxExecutionEnd = Math.max(_maxExecutionEnd, end);
        _maxXRange = Math.max(_maxXRange, _maxExecutionEnd);
        setXRange(0.0, _maxXRange);
        repaint();
    }

    /** Add the release time and deadline of a job in the execution being drawn,
     *  and redraw the plot.
     *  @param job The job to be plotted.
     */
    public synchronized void addJob(Job job) {
        _jobs.add(job);
        Actor task = job.getTask();
        if (_tasks.contains(task)) {
            _jobsPerTask.get(task).add(job);
        } else {
            _tasks.add(job.getTask());
            List<Job> taskJobs = new ArrayList<Job>();
            taskJobs.add(job);
            _jobsPerTask.put(task, taskJobs);
            if (job.getParentTask() == null) {
                addYTick(job.getTask().getName(), (double) _tasks.size() - 1);
            } else {
                addYTick(job.getParentTask().getName() + "."
                        + job.getTask().getName(), (double) _tasks.size() - 1);
            }
        }
        _maxDeadline = Math.max(_maxDeadline, job.getAbsoluteDeadline()
                .getDoubleValue());
        _maxXRange = Math.max(_maxDeadline, _maxXRange);
        setYRange(-0.5, _tasks.size());
        setXRange(0.0, _maxXRange);
        repaint();
    }

    /** Clear the state associated with the plotter. */
    public void clear() {
        _jobs.clear();
        _jobsPerTask.clear();
        _tasks.clear();
        _executionsPerTask.clear();
        _maxDeadline = 0.0;
        _maxExecutionEnd = 0.0;
        _maxXRange = 0.0;
        _deadlineMissTask = null;
        clear(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    @Override
    protected synchronized void _drawPlot(Graphics graphics,
            boolean clearfirst, Rectangle drawRect) {
        super._drawPlot(graphics, clearfirst, drawRect);

        for (Map.Entry<Actor, List<Execution>> taskExecutions : _executionsPerTask
                .entrySet()) {
            Actor task = taskExecutions.getKey();
            List<Execution> executions = taskExecutions.getValue();
            int taskindex = _tasks.indexOf(task);
            long ypos = _lry - (long) ((taskindex - _yMin) * _yscale);
            for (Execution execution : executions) {
                long starti = _ulx
                        + (long) ((execution.start - _xMin) * _xscale);
                long endi = _ulx + (long) ((execution.end - _xMin) * _xscale);
                _drawExecution(graphics, (int) starti, (int) endi, (int) ypos,
                        _colors[taskindex]);
            }
        }

        for (Job job : _jobs) {
            double release = job.getStartTime().getDoubleValue();
            double deadline = job.getAbsoluteDeadline().getDoubleValue();
            long releasei = _ulx + (long) ((release - _xMin) * _xscale);
            long deadlinei = _ulx + (long) ((deadline - _xMin) * _xscale);
            int taskindex = _tasks.indexOf(job.getTask());
            long ypos = _lry - (long) ((taskindex - _yMin) * _yscale);
            _drawRelease(graphics, (int) releasei, (int) ypos, new Color(
                    0x000000));
            _drawDeadline(graphics, (int) deadlinei, (int) ypos, new Color(
                    0x000000));
        }

        if (_deadlineMissTask != null) {
            int taskindex = _tasks.indexOf(_deadlineMissTask);
            long ypos = _lry - (long) ((taskindex - _yMin) * _yscale);
            long xpos = _ulx + (long) ((_deadlineMissTime - _xMin) * _xscale);
            _drawDeadlineMiss(graphics, (int) xpos, (int) ypos, Color.RED);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private void _drawDeadline(Graphics graphics, int deadline, int ypos,
            Color color) {
        ((Graphics2D) graphics).setStroke(new BasicStroke(2F));
        graphics.setColor(color);
        graphics.drawLine(deadline, ypos, deadline, ypos
                - (int) (_deadlineReleaseHeight * _yscale));
        graphics.drawLine(deadline, ypos, deadline - _arrowHalfWidth, ypos
                - _arrowHeight);
        graphics.drawLine(deadline, ypos, deadline + _arrowHalfWidth, ypos
                - _arrowHeight);
    }

    private void _drawDeadlineMiss(Graphics graphics, int time, int ypos,
            Color color) {
        ((Graphics2D) graphics).setStroke(new BasicStroke(2F));
        graphics.setColor(color);
        int y = ypos - (int) (_deadlineMissHeight * _yscale);
        graphics.drawLine(time, y, time + _deadlineMissLength, y
                + _deadlineMissLength);
        graphics.drawLine(time, y, time - _deadlineMissLength, y
                + _deadlineMissLength);
        graphics.drawLine(time, y, time - _deadlineMissLength, y
                - _deadlineMissLength);
        graphics.drawLine(time, y, time + _deadlineMissLength, y
                - _deadlineMissLength);
    }

    private void _drawExecution(Graphics graphics, int start, int end,
            int ypos, Color color) {
        ((Graphics2D) graphics).setStroke(new BasicStroke(2F));
        graphics.setColor(color);
        int height = (int) (_executionHeight * _yscale);
        graphics.fillRect(start, ypos - height, end - start, height);
    }

    private void _drawRelease(Graphics graphics, int release, int ypos,
            Color color) {
        ((Graphics2D) graphics).setStroke(new BasicStroke(2F));
        graphics.setColor(color);
        int yarrowend = ypos - (int) (_deadlineReleaseHeight * _yscale);
        graphics.drawLine(release, ypos, release, ypos
                - (int) (_deadlineReleaseHeight * _yscale));
        graphics.drawLine(release, yarrowend, release - _arrowHalfWidth,
                yarrowend + _arrowHeight);
        graphics.drawLine(release, yarrowend, release + _arrowHalfWidth,
                yarrowend + _arrowHeight);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private int _arrowHalfWidth = 2;
    private int _arrowHeight = 6;
    private double _deadlineMissHeight = 0.2;
    private int _deadlineMissLength = 8;
    private Actor _deadlineMissTask = null;
    private double _deadlineMissTime;
    private double _deadlineReleaseHeight = 0.7;
    private double _executionHeight = 0.4;
    private Map<Actor, List<Execution>> _executionsPerTask;
    private List<Job> _jobs;
    private Map<Actor, List<Job>> _jobsPerTask;
    private double _maxDeadline;
    private double _maxExecutionEnd;
    private double _maxXRange;
    private List<Actor> _tasks;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    private static class Execution {
        public Execution(double start, double end) {
            this.start = start;
            this.end = end;
        }

        public double start, end;
    }
}
