/* An attribute that displays a plot of a schedule

 Copyright (c) 1998-2014 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

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
package ptolemy.domains.tm.kernel;

import java.awt.Frame;
import java.util.ArrayList;
import java.util.HashMap;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.EditorFactory;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PlotEffigy;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.plot.Plot;

///////////////////////////////////////////////////////////////////
//// SchedulePlotter

/**
 This attribute is a visible attribute that when configured (by double
 clicking on it or by invoking Configure in the context menu) it displays
 a plot of the schedule while the model is being run.

 The SchedulePlotter attribute can be found under more libraries -&gt;
 experimental domains -&gt; timed multitasking

 @author Christopher Hylands, Contributor: Johan Ecker
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class SchedulePlotter extends Attribute implements ScheduleListener {
    /** Construct a factory with the specified container and name.
     *  @param container The container.
     *  @param name The name of the factory.
     *  @exception IllegalActionException If the factory is not of an
     *   acceptable attribute for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public SchedulePlotter(NamedObj container, String name)
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

        // FIXME: This seems wrong.
        if (container instanceof CompositeActor) {
            // We need to check if the container is a CompositeActor
            // because the reference to SchedulePlotter in tmentities.xml
            // is not a CompositeActor
            Director director = ((CompositeActor) container).getDirector();

            if (director != null) {
                if (!(director instanceof TMDirector)) {
                    throw new IllegalActionException(
                            "Director '"
                                    + director
                                    + "' is not a TMDirector, so adding a SchedulePlotter "
                                    + "makes no sense");
                }

                ((TMDirector) director).addScheduleListener(this);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////        public variables and parameters                    ////

    /** The plotter. */
    public Plot plot;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    @Override
    public void event(final String actorName, double time, int scheduleEvent) {
        try {
            if (scheduleEvent == -1) {
                if (plot != null) {
                    plot.clear(true);
                    _taskMap.clear();
                    _taskState.clear();
                }
            } else {
                if (_taskMap != null) {
                    Object taskID = _taskMap.get(actorName);
                    int id;

                    if (taskID == null) {
                        id = _taskMap.size();

                        final int finalid = id;
                        _taskMap.put(actorName, Integer.valueOf(id));
                        _taskState.add(Integer.valueOf(0));

                        // Note: addLegend is not intended to be
                        // called from outside the swing thread.
                        Runnable doAddPoint = new Runnable() {
                            @Override
                            public void run() {
                                plot.addLegend(finalid, actorName);
                            }
                        };

                        synchronized (plot) {
                            plot.deferIfNecessary(doAddPoint);
                        }
                    } else {
                        id = ((Integer) taskID).intValue();
                    }

                    int _oldState = ((Integer) _taskState.get(id)).intValue();
                    plot.addPoint(id, time, id + _oldState / 2.1, true);
                    plot.addPoint(id, time, id + scheduleEvent / 2.1, true);
                    _taskState.set(id, Integer.valueOf(scheduleEvent));
                    plot.fillPlot();
                    plot.repaint();
                }
            }
        } catch (Exception e) {
            System.out.println("event: Ignoring " + e);
            e.printStackTrace();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private HashMap _taskMap;

    private ArrayList _taskState;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Factory that creates the schedule plotter. */
    public class SchedulePlotterEditorFactory extends EditorFactory {
        // This class needs to be public for shallow code generation.
        /**
         * Constructs a SchedulePlotter$SchedulePlotterEditorFactory object.
         *
         *  @param container The container.
         *  @param name The name of the factory.
         *  @exception IllegalActionException If the factory is not of an
         *   acceptable attribute for the container.
         *  @exception NameDuplicationException If the name coincides with
         *   an attribute already in the container.
         */
        public SchedulePlotterEditorFactory(NamedObj container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        /** Create an editor for configuring the specified object with the
         *  specified parent window.
         *  @param object The object to configure.
         *  @param parent The parent window, or null if there is none.
         */
        @Override
        public void createEditor(NamedObj object, Frame parent) {
            try {
                Configuration configuration = ((TableauFrame) parent)
                        .getConfiguration();

                NamedObj container = object.getContainer();

                _taskMap = new HashMap();
                _taskState = new ArrayList();
                plot = new Plot();
                plot.setTitle("TM Schedule");
                plot.setButtons(true);

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
