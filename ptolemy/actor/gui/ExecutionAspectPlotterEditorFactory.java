/* Factory that creates the schedule plotter.

@Copyright (c) 2008-2014 The Regents of the University of California.
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
package ptolemy.actor.gui;

import java.awt.Frame;
import java.util.HashMap;
import java.util.List;

import ptolemy.actor.ActorExecutionAspect;
import ptolemy.actor.ExecutionAspectListener;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.plot.Plot;

/** Factory that creates the plotter for the schedule of actors on a
 *  resource scheduler.
 *
 * @author Patricia Derler
   @version $Id$
   @since Ptolemy II 10.0

   @Pt.ProposedRating Red (derler)
   @Pt.AcceptedRating Red (derler)
 */
public class ExecutionAspectPlotterEditorFactory extends EditorFactory
        implements ExecutionAspectListener {

    /**
     * Constructs a SchedulePlotter$SchedulePlotterEditorFactory object.
     *
     * @param container
     *                The container.
     * @param name
     *                The name of the factory.
     * @exception IllegalActionException
     *                    If the factory is not of an acceptable attribute
     *                    for the container.
     * @exception NameDuplicationException
     *                    If the name coincides with an attribute already in
     *                    the container.
     */
    public ExecutionAspectPlotterEditorFactory(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** The plot displayed by this ScheduleFactory. */
    public Plot plot;

    /**
     * Create an editor for configuring the specified object with the
     * specified parent window.
     *
     * @param object
     *                The object to configure.
     * @param parent
     *                The parent window, or null if there is none.
     */
    @Override
    public void createEditor(NamedObj object, Frame parent) {
        try {
            Configuration configuration = ((TableauFrame) parent)
                    .getConfiguration();

            object.getContainer();

            plot = new Plot();
            plot.setTitle("Execution Time Monitor");
            plot.setButtons(true);
            plot.setMarksStyle("none");
            plot.setXLabel("platform time");
            plot.setYLabel("actor ID");

            // We put the plotter as a sub-effigy of the toplevel effigy,
            // so that it closes when the model is closed.
            Effigy effigy = Configuration.findEffigy(toplevel());
            String name = "plotterEffigy" + String.valueOf(id++);
            PlotEffigy schedulePlotterEffigy = new PlotEffigy(effigy, name);
            schedulePlotterEffigy.setPlot(plot);
            schedulePlotterEffigy.setModel(this.getContainer());
            schedulePlotterEffigy.identifier
                    .setExpression("Execution Time Monitor");

            configuration.createPrimaryTableau(schedulePlotterEffigy);

            plot.setVisible(true);

            if (_actors != null) {
                _initPlot();
            }
        } catch (Throwable throwable) {
            throw new InternalErrorException(object, throwable,
                    "Cannot create Schedule Plotter");
        }
    }

    /** Plot a new execution event for an actor (i.e. an actor
     *  started/finished execution, was preempted or resumed).
     * @param actor The actor.
     * @param physicalTime The physical time when this scheduling event occurred.
     * @param scheduleEvent The scheduling event.
     */
    @Override
    public void event(final NamedObj actor, double physicalTime,
            ExecutionEventType scheduleEvent) {
        if (plot == null) {
            return;
        }

        double x = physicalTime;
        int actorDataset = _actors.indexOf(actor);
        if (actorDataset == -1) {
            return; // actor is not being monitored
        }
        if (scheduleEvent == null) {
            if (_previousY.get(actor) == null) {
                _previousY.put(actor, (double) actorDataset);
            }
            plot.addPoint(actorDataset, x, _previousY.get(actor), true);
            _previousY.put(actor, (double) actorDataset);
        } else if (scheduleEvent == ExecutionEventType.START) {
            plot.addPoint(actorDataset, x, _previousY.get(actor), true);
            plot.addPoint(actorDataset, x, actorDataset + 0.6, true);
            _previousY.put(actor, actorDataset + 0.6);
        } else if (scheduleEvent == ExecutionEventType.STOP) {
            if (_previousY.get(actor) != actorDataset) {
                plot.addPoint(actorDataset, x, actorDataset + 0.6, true);
                plot.addPoint(actorDataset, x, actorDataset, true);
            }
            _previousY.put(actor, (double) actorDataset);
        } else if (scheduleEvent == ExecutionEventType.PREEMPTED) {
            plot.addPoint(actorDataset, x, actorDataset + 0.6, true);
            plot.addPoint(actorDataset, x, actorDataset + 0.4, true);
            _previousY.put(actor, actorDataset + 0.4);
        }
        plot.fillPlot();
        plot.repaint();
    }

    /** Initialize plot.
     *  @param actors Actors scheduled by the resource scheduler associated with
     *    this plot.
     *  @param scheduler Resource Scheduler associated with this plot.
     */
    @Override
    public void initialize(List<NamedObj> actors, ActorExecutionAspect scheduler) {
        _actors = actors;
        _scheduler = scheduler;
        if (plot != null) {
            _initPlot();
        }
    }

    /** Contains the actors inside a ptides platform (=platforms). */
    protected List<NamedObj> _actors;

    /** Initialize legend.
     */
    private void _initPlot() {
        plot.clearLegends();
        plot.clear(false);
        plot.addLegend(_actors.size() - 1, _scheduler.getName());
        _previousY.put((NamedObj) _scheduler,
                Double.valueOf(_actors.size() - 1));
        plot.clear(false);
        plot.clearLegends();

        for (NamedObj actor : _actors) {
            plot.addLegend(_actors.indexOf(actor), actor.getName());
            event(actor, 0.0, null);
            _previousY.put(actor, Double.valueOf(_actors.indexOf(actor)));
        }
        plot.doLayout();
    }

    /** The resource scheduler associated with this plot.
     */
    private ActorExecutionAspect _scheduler;

    /** Previous positions of the actor data set. */
    private HashMap<NamedObj, Double> _previousY = new HashMap<NamedObj, Double>();

    /** This static variable is increased by 1 every time a new
     *  SchedulePlotter is generated. The id is assigned as a unique
     *  id to every schedule plotter.
     */
    private static int id = 1;
}
