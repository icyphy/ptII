/* This actor implements a Quantity Manager Monitor.

@Copyright (c) 2010-2013 The Regents of the University of California.
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

package ptolemy.actor.lib.qm;

import java.awt.Color;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.QuantityManager;
import ptolemy.actor.QuantityManagerListener;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.gui.ColorAttribute;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.EditorFactory;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PlotEffigy;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.plot.Plot;

/** This actor implements a Quantity Manager Monitor. 
 *  This monitor shows when quantity managers in the model receive messages
 *  and send messages in a 2D plot. The x-Axis is the time, the y-Axis shows how
 *  many messages are currently processed by the quantity manager.
 *
 *  @author Patricia Derler
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Yellow (derler)
 *  @Pt.AcceptedRating Red (derler)
 */
public class QuantityManagerMonitor extends TypedAtomicActor implements
        QuantityManagerListener {

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
    public QuantityManagerMonitor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-50\" y=\"-20\" width=\"130\" height=\"40\" "
                + "style=\"fill:blue\"/>" + "<text x=\"-40\" y=\"-5\" "
                + "style=\"font-size:12; font-family:SansSerif; fill:white\">"
                + "Double click to\nplot the schedule.</text></svg>");

        new QuantityManagerEditorFactory(this, "_editorFactory");

        SingletonParameter hide = new SingletonParameter(this, "_hideName");
        hide.setToken(BooleanToken.TRUE);
        hide.setVisibility(Settable.EXPERT);

    }

    ///////////////////////////////////////////////////////////////////
    //                            public variables                   //

    /** The plotter. */
    public Plot plot;

    ///////////////////////////////////////////////////////////////////
    //                           public methods                      //

    /** The event is displayed.
     *  @param qm The quantity manager that sent the event.
     *  @param source The source actor that caused the event in the
     *      quantity manager.
     *  @param messageId The ID of the message that caused the event in
     *      the quantity manager.
     *  @param messageCnt The amount of messages currently being processed
     *      by the quantity manager.
     *  @param time The time when the event happened.
     *  @param event The type of the event. e.g. message received, message sent, ...
     */
    public void event(final QuantityManager qm, Actor source, int messageId,
            int messageCnt, double time, EventType event) {

        if (plot == null) {
            return;
        }

        double x = time;
        double y = 0;//_quantityManagers.indexOf(qm);
        int actorDataset = _quantityManagers.indexOf(qm);
        if (event == null) {
            plot.addPoint(actorDataset, x, y, false);
        } else if (event == EventType.RECEIVED) {
            plot.addPoint(actorDataset, x, y + 0.1 * messageCnt, true);
        } else if (event == EventType.SENT) {
            plot.addPoint(actorDataset, x, y + 0.1 * messageCnt, true);
        }
        plot.fillPlot();
        plot.repaint();
    }

    Color[] colors;

    /** Initialize the plot and the legend with the list of quantity managers used
     *  in this model.
     *  @exception IllegalActionException If thrown by the parent class.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _quantityManagers = new ArrayList();
        if (getContainer() instanceof CompositeActor) {
            Director director = ((CompositeActor) getContainer()).getDirector();
            List<Actor> list = ((CompositeActor) director.getContainer())
                    .entityList();
            for (Actor actor : list) {
                if (actor instanceof QuantityManager) {
                    _quantityManagers.add((QuantityManager) actor);
                    ((QuantityManager) actor).registerListener(this);
                }
            }
        }

        if (plot != null) {
            plot.clear(false);
            plot.clearLegends();
            colors = new Color[_quantityManagers.size()];
            for (QuantityManager qm : _quantityManagers) {
                int idx = _quantityManagers.indexOf(qm);
                plot.addLegend(idx, ((NamedObj) qm).getName());
                plot.addPoint(idx, 0.0, /*idx*/0, false);
                colors[idx] = ((ColorAttribute)((NamedObj) qm).getAttribute(QuantityManager.decoratorHighlightColorName)).asColor();
            }

            plot.doLayout();
        }
        // FIXME: This affects all plots in the model:
        // plot.setColors(colors);
    }

    ///////////////////////////////////////////////////////////////////
    //                           private variables                   //

    /** List of quantity managers used in the model. */
    private List<QuantityManager> _quantityManagers;

    ///////////////////////////////////////////////////////////////////
    //                        inner classes                          //

    /** Factory that creates the schedule plotter. */
    public class QuantityManagerEditorFactory extends EditorFactory {
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
        public QuantityManagerEditorFactory(NamedObj container, String name)
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
                plot.setTitle("Quantity Manager Monitor");
                plot.setButtons(true);
                plot.setMarksStyle("dots");

                // We put the plotter as a sub-effigy of the toplevel effigy,
                // so that it closes when the model is closed.
                Effigy effigy = Configuration.findEffigy(toplevel());
                PlotEffigy schedulePlotterEffigy = new PlotEffigy(effigy,
                        container.uniqueName("schedulePlotterEffigy"));
                schedulePlotterEffigy.setPlot(plot);
                schedulePlotterEffigy.setModel(this.getContainer());
                schedulePlotterEffigy.identifier.setExpression("Monitor");

                configuration.createPrimaryTableau(schedulePlotterEffigy);

                plot.setVisible(true);
            } catch (Throwable throwable) {
                throw new InternalErrorException(object, throwable,
                        "Cannot create Plotter");
            }
        }
    }

}
