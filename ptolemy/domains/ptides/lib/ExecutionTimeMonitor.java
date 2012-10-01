/* Execution time monitor.
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

import java.awt.Frame;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.EditorFactory;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PlotEffigy;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.domains.ptides.kernel.PtidesBasicDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;
import ptolemy.plot.Plot;

/** This plotter monitors the execution of actors and displays the
 *  executions on a time line.
 *
 *  @author Patricia Derler
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (derler)
 *  @Pt.AcceptedRating
 */
public class ExecutionTimeMonitor extends TypedAtomicActor implements
        ExecutionTimeListener {

    /** Construct a factory with the specified container and name.
     *  @param container The container.
     *  @param name The name of the factory.
     *  @exception IllegalActionException
     *      If the factory is not of an acceptable attribute for
     *      the container.
     *  @exception NameDuplicationException
     *      If the name coincides with an attribute already in the
     *      container.
     */
    public ExecutionTimeMonitor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-50\" y=\"-20\" width=\"130\" height=\"40\" "
                + "style=\"fill:blue\"/>" + "<text x=\"-40\" y=\"-5\" "
                + "style=\"font-size:12; font-family:SansSerif; fill:white\">"
                + "Double click to\nplot the executions.</text></svg>");

        new SchedulePlotterEditorFactory(this, "_editorFactory");

        SingletonParameter hide = new SingletonParameter(this, "_hideName");
        hide.setToken(BooleanToken.TRUE);
        hide.setVisibility(Settable.EXPERT);

    }

    ///////////////////////////////////////////////////////////////////
    //                         public variables                      //

    /** The plotter. */
    public Plot plot;

    ///////////////////////////////////////////////////////////////////
    //                           public methods                      //

    /** The event is displayed.
     *  @param actor The actor where the event happened. This parameter can be
     *     null if the event is TRANSFEROUTPUT or TRANSFERINPUT.
     *  @param oracleTime The oracle time when the event happened.
     *  @param physicalTime The physical time when the event happened.
     *  @param modelTime The model time when the event happened.
     *  @param scheduleEvent The type of the event.
     */
    public void event(final Actor actor, double oracleTime,
            double physicalTime, double modelTime,
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
            plot.addPoint(actorDataset, x, actorDataset + 0.6, true);
            plot.addPoint(actorDataset, x, actorDataset, true);
            _previousY.put(actor, (double) actorDataset);
        } else if (scheduleEvent == ExecutionEventType.PREEMPTED) {
            plot.addPoint(actorDataset, x, actorDataset + 0.6, true);
            plot.addPoint(actorDataset, x, actorDataset + 0.4, true);
            _previousY.put(actor, actorDataset + 0.4);
        }
        plot.fillPlot();
        plot.repaint();
    }

    /** The event is displayed (core is ignored).
     *  @param actor The actor where the event happened. This parameter can be
     *     null if the event is TRANSFEROUTPUT or TRANSFERINPUT.
     *  @param time The physical time when the event happened.
     *  @param event The type of the event.
     *  @param core Not used.
     */
    public void event(Actor actor, double time, ExecutionEventType event,
            int core) {
        event(actor, time, event, 0);
    }

    /** Clone this actor into the specified workspace. The new actor is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new actor with the same ports as the original, but
     *  no connections and no container.  A container must be set before
     *  much can be done with this actor.
     *
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If cloned ports cannot have
     *   as their container the cloned entity (this should not occur), or
     *   if one of the attributes cannot be cloned.
     *  @return A new Bus.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ExecutionTimeMonitor newObject = (ExecutionTimeMonitor) super
                .clone(workspace);
        newObject._actors = new ArrayList<Actor>();
        newObject._previousY = new HashMap<Actor, Double>();
        return newObject;
    }

    /** Initialize the plot and the legend. The legend will be created for all
     *  nodes and actors.
     */
    public void initialize() {
        _actors = new ArrayList();
        if (getContainer() instanceof CompositeActor) {
            // We need to check if the container is a CompositeActor
            // because the reference to SchedulePlotter in tmentities.xml
            // is not a CompositeActor
            Director director = ((CompositeActor) getContainer()).getDirector();

            List<Actor> list = ((CompositeActor) director.getContainer())
                    .entityList();
            for (Actor actor : list) {
                if (actor instanceof CompositeActor) {
                    if (actor.getDirector() instanceof PtidesBasicDirector) {
                        ((PtidesBasicDirector) actor.getDirector())
                                .registerExecutionTimeListener(this);
                    }
                    _addActors((CompositeActor) actor);
                }
            }
        }
        if (plot != null) {
            plot.clear(false);
            plot.clearLegends();

            for (Actor actor : _actors) {
                plot.addLegend(_actors.indexOf(actor), actor.getName());
                event(actor, 0.0, 0.0, 0.0, null);
            }
            plot.doLayout();
        }
    }

    ///////////////////////////////////////////////////////////////////
    //                   private methods                             //

    /** Add actors contained by the composite actor passed as an argument.
     *  @param compositeActor New actor to be added.
     */
    private void _addActors(CompositeActor compositeActor) {
        List<Actor> containedActors = compositeActor.entityList();
        for (Actor containedActor : containedActors) {
            if (containedActor instanceof CompositeActor) {
                if (containedActor.getDirector() instanceof PtidesBasicDirector) {
                    ((PtidesBasicDirector) containedActor.getDirector())
                            .registerExecutionTimeListener(this);
                }
                _addActors((CompositeActor) containedActor);
            }
            boolean monitor = false;
            try {
                Parameter parameter = (Parameter) ((NamedObj) containedActor)
                        .getAttribute("monitor");
                if (parameter != null) {
                    BooleanToken token = (BooleanToken) parameter.getToken();
                    monitor = token.booleanValue();
                }
            } catch (Exception ex) {
                // do nothing, assume the actor should not be monitored
            }
            if (monitor) {
                _actors.add(containedActor);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    //                   private variables                           //

    /** Contains the actors inside a ptides platform (=platforms). */
    private List<Actor> _actors;

    /** Previous positions of the actor data set. */
    private HashMap<Actor, Double> _previousY = new HashMap();

    ///////////////////////////////////////////////////////////////////
    //                    inner classes                              //

    /** Factory that creates the schedule plotter. */
    public class SchedulePlotterEditorFactory extends EditorFactory {
        // This class needs to be public for shallow code generation.
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
        public SchedulePlotterEditorFactory(NamedObj container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        /**
         * Create an editor for configuring the specified object with the
         * specified parent window.
         *
         * @param object
         *                The object to configure.
         * @param parent
         *                The parent window, or null if there is none.
         */
        public void createEditor(NamedObj object, Frame parent) {
            try {
                Configuration configuration = ((TableauFrame) parent)
                        .getConfiguration();

                NamedObj container = object.getContainer();

                plot = new Plot();
                plot.setTitle("Execution Time Monitor");
                plot.setButtons(true);
                plot.setMarksStyle("none");
                plot.setXLabel("platform time");
                plot.setYLabel("actor ID");

                // We put the plotter as a sub-effigy of the toplevel effigy,
                // so that it closes when the model is closed.
                Effigy effigy = Configuration.findEffigy(toplevel());
                PlotEffigy schedulePlotterEffigy = new PlotEffigy(effigy,
                        container.uniqueName("schedulePlotterEffigy"));
                schedulePlotterEffigy.setPlot(plot);
                schedulePlotterEffigy.setModel(this.getContainer());
                schedulePlotterEffigy.identifier
                        .setExpression("Execution Time Monitor");

                configuration.createPrimaryTableau(schedulePlotterEffigy);

                plot.setVisible(true);
            } catch (Throwable throwable) {
                throw new InternalErrorException(object, throwable,
                        "Cannot create Schedule Plotter");
            }
        }
    }

}
