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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import ptolemy.data.IntToken;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.plot.Plot;

///////////////////////////////////////////////////////////////////
//// TaskPlot

/**
 * A plotter for the demand bound function of periodic and multiframe tasks.
 * 
 * @author Patricia Derler
 * @version $Id: TaskPlot.java 68364 2014-02-08 19:24:51Z cxh $
 * @Pt.ProposedRating Red (chster)
 * @Pt.AcceptedRating Red (chster)
 */
@SuppressWarnings("serial")
public class DBFPlot extends Plot {
    /** Construct a task plot. */
    public DBFPlot() {
        super();
        _jobs = new ArrayList<Job>();
        _executionTimesAndDeadlines = new HashMap<>();
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    

    /**
     * Add the release time and deadline of a job in the execution being drawn,
     * and redraw the plot.
     * 
     * @param job
     *            The job to be plotted.
     */
    public synchronized void addJob(Job job) {
        if (_jobs.size() == 0) {
            this.addPoint(0, 0, 0, true);
            this.addPoint(1, 0, 0, true);
        }
        _jobs.add(job);
        double deadline = job.getAbsoluteDeadline().getDoubleValue();
        try {
            double _executionTime = 0.0;

            if (job.getTask() instanceof PeriodicTask) {
                _executionTime += ((IntToken) ((PeriodicTask) job.getTask()).executionTime
                        .getToken()).intValue();
            } else if (job.getTask() instanceof TaskFrame) {
                _executionTime += ((IntToken) ((TaskFrame) job.getTask()).executionTime
                        .getToken()).intValue();
            } else {
                System.out.println("job is not PeriodicTask or TaskFrame but "
                        + job);
            }
            List<Double> list = _executionTimesAndDeadlines.get(deadline);
            if (list == null) {
                list = new ArrayList<Double>();
            }
            list.add(_executionTime);
            _executionTimesAndDeadlines.put(deadline, list);

        } catch (IllegalActionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    
    public void finalize() {
        TreeSet<Double> set = new TreeSet<Double>();
        set.addAll(_executionTimesAndDeadlines.keySet());
        Iterator<Double> iterator = set.iterator();
        double executionTime = 0.0;
        while (iterator.hasNext()) {
            Double key = (Double) iterator.next();
            List<Double> executionTimes = _executionTimesAndDeadlines.get(key);
            this.addPoint(0, key, executionTime, true);
            for (int i = 0; i < executionTimes.size(); i++) {
                executionTime += executionTimes.get(i);
            }
            this.addPoint(0, key, executionTime, true);
            this.addPoint(1, key, key, true);
        }
        fillPlot();
        repaint();
        
        _executionTimesAndDeadlines.clear();
        _jobs.clear();
    }
    
    /** Clear the state associated with the plotter. */
    public void clear() {
        _executionTimesAndDeadlines.clear();
        _jobs.clear();
        clearLegends();
        clear(false);
    }

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    private HashMap<Double, List<Double>> _executionTimesAndDeadlines;

    // /////////////////////////////////////////////////////////////////
    // // private variables ////

    private List<Job> _jobs;
}
