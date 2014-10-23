/* This actor implements a communication aspect Monitor.

@Copyright (c) 2010-2014 The Regents of the University of California.
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

import java.awt.Color;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CommunicationAspect;
import ptolemy.actor.CommunicationAspectListener;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.plot.Plot;

/** This actor implements a Communication aspect Monitor.
 *  This monitor shows when communication aspects in the model receive messages
 *  and send messages in a 2D plot. The x-Axis is the time, the y-Axis shows how
 *  many messages are currently processed by the communication aspect.
 *
 *  @author Patricia Derler
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Yellow (derler)
 *  @Pt.AcceptedRating Red (derler)
 */
public class CommunicationAspectMonitor extends TypedAtomicActor implements
CommunicationAspectListener {

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
    public CommunicationAspectMonitor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-50\" y=\"-20\" width=\"130\" height=\"40\" "
                + "style=\"fill:blue\"/>" + "<text x=\"-40\" y=\"-5\" "
                + "style=\"font-size:12; font-family:SansSerif; fill:white\">"
                + "Double click to\nplot the schedule.</text></svg>");

        new CommunicationAspectMonitorEditorFactory(this, "_editorFactory");

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
     *  @param qm The communication aspect that sent the event.
     *  @param source The source actor that caused the event in the
     *      communication aspect.
     *  @param messageId The ID of the message that caused the event in
     *      the communication aspect.
     *  @param messageCnt The amount of messages currently being processed
     *      by the communication aspect.
     *  @param time The time when the event happened.
     *  @param event The type of the event. e.g. message received, message sent, ...
     */
    @Override
    public void event(final CommunicationAspect qm, Actor source,
            int messageId, int messageCnt, double time, EventType event) {

        if (plot == null) {
            return;
        }

        double x = time;
        double y = 0;
        int actorDataset = _communicationAspects.indexOf(qm);
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

    /** Initialize the plot and the legend with the list of communication aspects used
     *  in this model.
     *  @exception IllegalActionException If thrown by the parent class.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _communicationAspects = new ArrayList<CommunicationAspect>();
        if (getContainer() instanceof CompositeActor) {
            Director director = ((CompositeActor) getContainer()).getDirector();
            List<Actor> list = ((CompositeActor) director.getContainer())
                    .entityList();
            for (Actor actor : list) {
                if (actor instanceof CommunicationAspect) {
                    _communicationAspects.add((CommunicationAspect) actor);
                    ((CommunicationAspect) actor).registerListener(this);
                }
            }
        }

        if (plot != null) {
            plot.clear(false);
            plot.clearLegends();
            colors = new Color[_communicationAspects.size()];
            for (CommunicationAspect aspect : _communicationAspects) {
                int idx = _communicationAspects.indexOf(aspect);
                plot.addLegend(idx, ((NamedObj) aspect).getName());
                plot.addPoint(idx, 0.0, /*idx*/0, false);
                colors[idx] = ((ColorAttribute) ((NamedObj) aspect)
                        .getAttribute(CommunicationAspect.decoratorHighlightColorName))
                        .asColor();
            }

            plot.doLayout();
        }
        // FIXME: This affects all plots in the model:
        // plot.setColors(colors);
    }

    ///////////////////////////////////////////////////////////////////
    //                           private variables                   //

    /** List of communication aspects used in the model. */
    private List<CommunicationAspect> _communicationAspects;

    ///////////////////////////////////////////////////////////////////
    //                        inner classes                          //

    /** Factory that creates the schedule plotter. */
    public class CommunicationAspectMonitorEditorFactory extends EditorFactory {
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
        public CommunicationAspectMonitorEditorFactory(NamedObj container,
                String name) throws IllegalActionException,
                NameDuplicationException {
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
        @Override
        public void createEditor(NamedObj object, Frame parent) {
            try {
                Configuration configuration = ((TableauFrame) parent)
                        .getConfiguration();

                NamedObj container = object.getContainer();

                plot = new Plot();
                plot.setTitle("communication aspect Monitor");
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
