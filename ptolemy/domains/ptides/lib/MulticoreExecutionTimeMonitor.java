/* Multicore execution time monitor.
@Copyright (c) 2008-2011 The Regents of the University of California.
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

package ptolemy.domains.ptides.lib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.lib.gui.Plotter;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.ptides.kernel.PtidesBasicDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.plot.Plot;

///////////////////////////////////////////////////////////////////
////MulticoreExecutionTimeMonitor 
/** A multicore execution time monitor. This monitors the execution 
 * time of actors on a multicore platform and displays with a plotter.
 * Use parameter "monitorExecutionTime" on actor for non-default behavior.
 * The "executionTime" parameter can either be specified on ports or actors,
 * with ports taking precedence.
 *
 *  @author Michael Zimmer
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (mzimmer)
 *  @Pt.AcceptedRating
 */
public class MulticoreExecutionTimeMonitor extends Plotter implements
        ExecutionTimeListener {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public MulticoreExecutionTimeMonitor(CompositeEntity container, 
            String name) throws IllegalActionException, 
            NameDuplicationException {
        super(container, name);

        // Icon.
        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-50\" y=\"-20\" width=\"120\" height=\"38\" "
                + "style=\"fill:blue\"/>" + "<text x=\"-40\" y=\"-5\" "
                + "style=\"font-size:12; font-family:SansSerif; fill:white\">"
                + "Multicore Execution\n     Time Monitor</text></svg>");

        // Initialize parameters.
        monitorExecutionTimesByDefault = 
            new Parameter(this, "monitorExecutionTimesByDefault");
        monitorExecutionTimesByDefault.setExpression("true");
        monitorExecutionTimesByDefault.setTypeEquals(BaseType.BOOLEAN);
        
        disableMonitorExecutionTimes = 
                new Parameter(this, "disableMonitorExecutionTimes");
        disableMonitorExecutionTimes.setExpression("false");
        disableMonitorExecutionTimes.setTypeEquals(BaseType.BOOLEAN);
        
        // Hide other parameters.
        SingletonParameter hide = new SingletonParameter(this, "_hideName");
        hide.setToken(BooleanToken.TRUE);
        hide.setVisibility(Settable.EXPERT);
        fillOnWrapup.setVisibility(Settable.EXPERT);
        startingDataset.setVisibility(Settable.EXPERT);
        automaticRescale.setVisibility(Settable.EXPERT);
        legend.setVisibility(Settable.EXPERT);

    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////
    
    /** If true, displaying of execution times is disabled. */
    public Parameter disableMonitorExecutionTimes;
    
    /** If true, display the execution time of an actor unless the parameter
     * 'monitorExecutionTime' is set to false. If false, the execution time 
     * of an actor is only displayed if the parameter 'monitorExecutionTime' 
     * is set to true.
     */
    public Parameter monitorExecutionTimesByDefault;

    ///////////////////////////////////////////////////////////////////
    //                           public methods                      //

    /** Display an execution time event.
     *  @param actor Actor of event.
     *  @param time Time event occurred.
     *  @param scheduleEvent Type of event.
     */
    public void event(final Actor actor, double time,
            ExecutionEventType scheduleEvent) {
        event(actor, time, scheduleEvent, 0);
    }
    
    /** Display an execution time event.
     *  @param actor Actor of event.
     *  @param oracleTime Oracle time event occurred.
     *  @param physicalTime Physical time event occurred.
     *  @param modelTime Model time of the event.
     *  @param event Type of event.
     */
    public void event(Actor actor, double oracleTime, double physicalTime,
            double modelTime, ExecutionEventType event) {
        
    }
    
    /** Display an execution time event.
     *  @param actor Actor of event.
     *  @param time Time event occurred.
     *  @param scheduleEvent Type of event.
     *  @param core Core event occurred on.
     */
    public void event(Actor actor, double time, ExecutionEventType 
            scheduleEvent, int core) {

        if (plot == null) {
            return; // No plot exists.
        }
        
        int actorDataset = _actors.indexOf(actor);
        if (actorDataset == -1) {
            return; // Actor is not being monitored.
        }
        
        double x = time;
        double y;
        Double point[] = new Double[2];
        // The same dataset is used for each actor, regardless of core event
        // occurred on. The last point for each actor on each core is saved.
        Map<Actor, Double[]> lastPoint;
        if (!_previousPoint.containsKey(Integer.valueOf(core))) {
            
            // Core doesn't have map of last points for actors.
            lastPoint = new HashMap<Actor, Double[]>();
            _previousPoint.put(Integer.valueOf(core), lastPoint);
            
            // Add labels to y-axis.
            for (int i = 0; i < _actors.size(); i++) {
                          
                final String name = _actors.get(i).getDisplayName();
                final double offset = getOffset(_actors.get(i), core);

                Runnable doAddYTick = new Runnable() {
                    public void run() {
                        plot.addYTick(name, offset);
                    }
                };
                synchronized (plot) {
                    plot.deferIfNecessary(doAddYTick);
                } 
            }

        } else {
            // Get map of last points for core.
            lastPoint = _previousPoint.get(Integer.valueOf(core));
        }
        // Create initial point if needed.
        if (!lastPoint.containsKey(actor)) {
            point[0] = Double.valueOf(0);
            point[1] = getOffset(actor, core);
            lastPoint.put(actor, point);
        }
        // Plot last point.
        ((Plot)plot).addPoint(actorDataset, 
                lastPoint.get(actor)[0].doubleValue(),
                lastPoint.get(actor)[1].doubleValue(), false);
        ((Plot)plot).addPoint(actorDataset, x,
                lastPoint.get(actor)[1].doubleValue(), true);
        
        // Plot event as new point.
        y = getOffset(actor, core);
        if (scheduleEvent == ExecutionEventType.START) {
            y += 0.66;
            _parallelMonitor.coreStarts(x);
        } else if (scheduleEvent == ExecutionEventType.STOP) {
            _parallelMonitor.coreStops(x);
        } else if (scheduleEvent == ExecutionEventType.PREEMPTED) {
            y += 0.33;  
            _parallelMonitor.coreStops(x);
        }
        point[0] = x;
        point[1] = y;  
        ((Plot)plot).addPoint(actorDataset, x, y, true);
        lastPoint.put(actor, point);
        
        Runnable doPlotActions = new Runnable() {
            public void run() {
                plot.setXLabel(""); // Needed for padding to captions.
                // Show parallel monitoring results in caption.
                plot.clearCaptions();
                plot.addCaptionLine("Time spend with number of active cores:" + _parallelMonitor.toString());
                plot.fillPlot();
            }
        };
        synchronized (plot) {
            plot.deferIfNecessary(doPlotActions);
        } 
        
    }


    /** Initialize the plot. This involves finding all actors in the container
     * that will be monitored.
     * @exception IllegalActionException If the parent class throws it or
     * cannot get a parameter. */
    public void initialize() throws IllegalActionException {

        // Return if disabled.
        if (((BooleanToken)disableMonitorExecutionTimes
                .getToken()).booleanValue()) {
            return;
        }

        _parallelMonitor = new ParallelMonitor();
        _previousPoint = 
            new HashMap<Integer, Map<Actor, Double[]>>();
        _actors = new ArrayList<Actor>();
        TypedCompositeActor container = ((TypedCompositeActor)getContainer());
        
        // Register this monitor to director.
        Director director = container.getDirector();
        if (director instanceof PtidesBasicDirector) {
            ((PtidesBasicDirector) director).
                    registerExecutionTimeListener(this);
        }
        
        // Add actors.
        boolean monitorByDefault = ((BooleanToken)
                monitorExecutionTimesByDefault.getToken()).booleanValue();

        for (Actor actor : (List<Actor>)container.deepEntityList()) {
            
            // Ignore self.
            if (actor instanceof MulticoreExecutionTimeMonitor) {
                continue;
            }
            
            boolean monitorActor = monitorByDefault;
            
            // Check if explicitly set.
            Parameter monitor = (Parameter)
                    ((NamedObj)actor).getAttribute("monitorExecutionTime");
            if (monitor != null) {
                monitorActor = 
                        ((BooleanToken)monitor.getToken()).booleanValue();
            }
            
            if (monitorActor) {
                _actors.add(actor);
                
            } 
            
        }
        
        Collections.sort(_actors, new Comparator<Actor>() {
            public int compare(Actor a1, Actor a2) {
                Parameter a1Dataset = (Parameter)
                ((NamedObj)a1).getAttribute("dataset");
                Parameter a2Dataset = (Parameter)
                ((NamedObj)a2).getAttribute("dataset");
                if (a1Dataset != null && a2Dataset != null) {
                    try {
                        int i1 = ((IntToken)a1Dataset.getToken()).intValue();
                        int i2 = ((IntToken)a2Dataset.getToken()).intValue();
                        if (i1 < i2) {
                            return -1;
                        }  
                    } catch (IllegalActionException e) {
                    }
                    
                }
                return 1;
            }});

        // Initialize plot.
        if (plot == null) {
            plot = _newPlot();
            plot.setGrid(true);
        }
        
        if ((_getImplementation().getFrame() == null) && ((_getImplementation().getPlatformContainer() == null))) {
            // If plot was closed.
            _getImplementation().initializeEffigy();
            _implementDeferredConfigurations();
            _getImplementation().updateSize();
        } else {
            // Monitored actors may have changed so completely clear plot.
            plot.clear(true);
        }
        
        // Set properties.
        Runnable doInit = new Runnable() {
            public void run() {
                plot.setTitle(getName());
                plot.setButtons(true);
                plot.setAutomaticRescale(true);
            }
            };
        synchronized (plot) {
            plot.deferIfNecessary(doInit);
        } 
    
        _getImplementation().bringToFront();

    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    
    /** Return the y-value that an execution time event for an actor on a core
     * should be plotted at.
     *  @param actor Actor of event.
     *  @param core Core event occurred on.
     *  @return y-value offset.
     */
    protected double getOffset(Actor actor, int core) {
        int size = _actors.size();
        double offset = _actors.indexOf(actor);
        offset += (size+1)*core;
        return offset;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////
    
    /** Contains the actors to be monitored. */
    protected List<Actor> _actors;
    
    /** Monitor how much time elapses with respect to the number of cores
     * processing events at a given time.
     */
    protected ParallelMonitor _parallelMonitor;

    /** Maps a core number to a map between actors and previous (x,y)
     * points. 
     */
    protected Map<Integer, Map<Actor, Double[]>> _previousPoint;
    
    
    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    
    /** Monitor how much time elapses with respect to the number of cores
     * processing events at a given time. This provides a measure of
     * parallelization achieved.
     */
    protected class ParallelMonitor {
        
        /** Construct the parallel monitor by initializing variables. */
        protected ParallelMonitor() {
            _busyTime = new HashMap<Integer, Double>();
            _activeCores = 0;
            _lastTime = 0;
        }

        /** Call when a core starts processing an event. 
         * @param time Time when this occurs.
         */
        protected void coreStarts(double time) {
            Integer i = Integer.valueOf(_activeCores);
            double elapsedTime = time - _lastTime;
            if (_busyTime.containsKey(i)) {
                elapsedTime += _busyTime.get(i).doubleValue();
            }
            _busyTime.put(i, Double.valueOf(elapsedTime));
            _lastTime = time;
            _activeCores++;
        }
        
        /** Call when a core stops processing an event (including preemption). 
         * @param time Time when this occurs.
         */
        protected void coreStops(double time) {
            Integer i = Integer.valueOf(_activeCores);
            double elapsedTime = time - _lastTime;
            if (_busyTime.containsKey(i)) {
                elapsedTime += _busyTime.get(i).doubleValue();
            }
            _busyTime.put(i, Double.valueOf(elapsedTime));
            _lastTime = time;
            _activeCores--;
        }
        
        /** Return results as a string. */
        public String toString() {
            return _busyTime.entrySet().toString();      
        }
        
        /** How much busy time with respect to the number of active cores. */
        private Map<Integer, Double> _busyTime;
        
        /** Number of active cores. */
        private int _activeCores;
        
        /** Last time number of active cores changed. */
        private double _lastTime;

    }


    

}
