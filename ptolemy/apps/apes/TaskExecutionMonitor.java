/*
@Copyright (c) 2008 The Regents of the University of California.
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

package ptolemy.apps.apes;

import java.awt.Frame;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
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
import ptolemy.data.expr.SingletonParameter; 
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.plot.Plot;

// ////////////////////////////////////////////////////////////////////////
// // SchedulePlotter

/**
 * This Plotter shows the exeuction of actors in a model. 
 * 
 * @author Patricia Derler
 */
public class TaskExecutionMonitor extends TypedAtomicActor implements TaskExecutionListener {

    /**
     * Construct a factory with the specified container and name.
     * 
     * @param container
     *                The container.
     * @param name
     *                The name of the factory.
     * @exception IllegalActionException
     *                    If the factory is not of an acceptable attribute for
     *                    the container.
     * @exception NameDuplicationException
     *                    If the name coincides with an attribute already in the
     *                    container.
     */
    public TaskExecutionMonitor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-50\" y=\"-20\" width=\"130\" height=\"40\" "
                + "style=\"fill:blue\"/>" + "<text x=\"-40\" y=\"-5\" "
                + "style=\"font-size:12; font-family:SansSerif; fill:white\">"
                + "Double click to\nplot the schedule.</text></svg>");

        new SchedulePlotterEditorFactory(this, "_editorFactory");

        SingletonParameter hide = new SingletonParameter(this, "_hideName");
        hide.setToken(BooleanToken.TRUE);
        hide.setVisibility(Settable.EXPERT);

        
    }

    // /////////////////////////////////////////////////////////////////
    // // public variables and parameters ////

    /** The plotter. */
    public Plot plot;

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * The event is displayed.
     * 
     * @param node
     *                The node where the event happened.
     * @param actor
     *                The actor where the event happened. This parameter can be
     *                null if the event is TRANSFEROUTPUT or TRANSFERINPUT.
     * @param time
     *                The physical time when the event happened.
     * @param scheduleEvent
     *                The type of the event.
     */
    public void event(final Actor actor, double time,
            ScheduleEventType scheduleEvent) {
        if (plot == null) {
            return;
        }
        double x = time;
        int actorDataset = actors.indexOf(actor);
        if (scheduleEvent == ScheduleEventType.START
                || scheduleEvent == ScheduleEventType.STOP) {
            plot.addPoint(actorDataset, x, actorDataset,
                    scheduleEvent == ScheduleEventType.STOP);
            plot.addPoint(actorDataset, x, actorDataset,
                    scheduleEvent == ScheduleEventType.STOP); 
        } 
        plot.fillPlot();
        plot.repaint();
    }

    /**
     * Contains the nodes (=platforms).
     */
    private List<Actor> actors;

    
    
    
    /**
     * Initialize the plot and the legend. The legend will be created for all
     * nodes and actors.
     * 
     * @param nodesActors
     *                A list of nodes and contained actors.
     */
    public void initialize() {  
        actors = new ArrayList();
        if (getContainer() instanceof CompositeActor) {
            // We need to check if the container is a CompositeActor
            // because the reference to SchedulePlotter in tmentities.xml
            // is not a CompositeActor
            Director director = ((CompositeActor) getContainer()).getDirector();

            List<Actor> list = ((CompositeActor)director.getContainer()).entityList();
            for (Actor actor : list) {
                if (actor instanceof CPUScheduler) {
                    ((CPUScheduler)actor).registerExecutionListener(this);
                } else if (actor instanceof CTask) {
                    actors.add(actor);
                }
            }
        } 
        
        if (plot != null) {
            plot.clear(false);
            plot.clearLegends();

            for (Actor actor : actors) {
                plot.addLegend(actors.indexOf(actor), actor
                        .getName()); 
            }
            plot.doLayout();
        }
    }

    // /////////////////////////////////////////////////////////////////
    // // inner classes ////

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
                plot.setTitle("Ptides Schedule");
                plot.setButtons(true);
                plot.setMarksStyle("dots");

                // We put the plotter as a sub-effigy of the toplevel effigy,
                // so that it closes when the model is closed.
                Effigy effigy = Configuration.findEffigy(toplevel());
                PlotEffigy schedulePlotterEffigy = new PlotEffigy(effigy,
                        container.uniqueName("schedulePlotterEffigy"));
                schedulePlotterEffigy.setPlot(plot);
                schedulePlotterEffigy.identifier.setExpression("TM Schedule");

                configuration.createPrimaryTableau(schedulePlotterEffigy);

                plot.setVisible(true);
            } catch (Throwable throwable) {
                throw new InternalErrorException(object, throwable,
                        "Cannot create Schedule Plotter");
            }
        }
    }

}
