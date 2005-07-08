/* An applet demonstrating EditablePlot.

 Copyright (c) 1999-2005 The Regents of the University of California.
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
package ptolemy.domains.sdf.demo.Sketch;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.actor.gui.MoMLApplet;
import ptolemy.actor.lib.gui.SketchedSource;
import ptolemy.data.IntToken;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.plot.EditListener;
import ptolemy.plot.EditablePlot;

//////////////////////////////////////////////////////////////////////////
//// SketchApplet

/**
 This applet demonstrates the use of the SketchSource actor,
 and in particular, how to share the same plot display between
 an instance of SketchedSource and an instance of SequencePlotter.

 @see SketchedSource
 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.4
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (reviewmoderator)
 */
public class SketchApplet extends MoMLApplet implements EditListener {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Execute the model.  This is called by the editable plot widget
     *  when the user edits the data in the plot.  The execution is
     *  carried out only if the model is idle (not currently executing).
     *  @param source The plot containing the modified data.
     *  @param dataset The data set that has been modified.
     */
    public void editDataModified(EditablePlot source, int dataset) {
        try {
            if (_manager.getState() == Manager.IDLE) {
                _sketchedSource.editDataModified(source, dataset);
                _go();
            }
        } catch (IllegalActionException ex) {
            report(ex);
        }
    }

    /** Create the shared plot and set it up based on the director parameters.
     */
    public void _createView() {
        super._createView();

        try {
            // Find out how many iterations the director expects to run for.
            CompositeActor toplevel = (CompositeActor) _toplevel;
            SDFDirector director = (SDFDirector) toplevel.getDirector();
            int iterations = ((IntToken) (director.iterations.getToken()))
                    .intValue();

            _sketchedSource = (SketchedSource) toplevel
                    .getEntity("SketchedSource");

            // Note: The order of the following is important.
            // First, specify how long the sketched plot should be.
            _sketchedSource.length.setToken(new IntToken(iterations));

            // Then, create the plot and place it in this applet,
            // and specify to both the source and destination actors
            // to use the same plot widget.
            EditablePlot plot = new EditablePlot();
            plot.setSize(700, 300);
            plot.setTitle("Editable envelope");
            plot.setXRange(0, iterations);
            plot.setButtons(true);
            getContentPane().add(plot);
            _sketchedSource.place(plot);
            plot.setBackground(null);
            plot.addEditListener(this);
        } catch (Exception ex) {
            report("Error constructing model.", ex);
        }
    }

    /** The SketchedSource actor */
    public SketchedSource _sketchedSource;
}
