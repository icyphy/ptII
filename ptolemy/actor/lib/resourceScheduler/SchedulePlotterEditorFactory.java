/* Factory that creates the schedule plotter.

@Copyright (c) 2008-2012 The Regents of the University of California.
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
package ptolemy.actor.lib.resourceScheduler;

import java.awt.Frame;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.EditorFactory;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PlotEffigy;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.plot.Plot;

/** Factory that creates the schedule plotter.
 * *
 * @author Patricia Derler
   @version $Id$
   @since Ptolemy II 9.0

   @Pt.ProposedRating Red (derler)
   @Pt.AcceptedRating Red (derler)
 */
public class SchedulePlotterEditorFactory extends EditorFactory {

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
        } catch (Throwable throwable) {
            throw new InternalErrorException(object, throwable,
                    "Cannot create Schedule Plotter");
        }
    }

    /** This static variable is increased by 1 every time a new
     *  SchedulePlotter is generated. The id is assigned as a unique
     *  id to every schedule plotter.
     */
    private static int id = 1;
}
