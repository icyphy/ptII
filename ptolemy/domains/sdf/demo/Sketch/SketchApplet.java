/* An applet demonstrating EditablePlot.

 Copyright (c) 1999-2000 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.demo.Sketch;

import java.awt.event.*;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.lib.*;
import ptolemy.actor.gui.*;
import ptolemy.actor.util.*;
import ptolemy.domains.sdf.gui.*;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.domains.sdf.lib.*;

import ptolemy.plot.*;

//////////////////////////////////////////////////////////////////////////
//// SketchApplet
/**

@author Edward A. Lee
@version $Id$
*/
public class SketchApplet extends SDFApplet implements EditListener {

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
            if (_manager.getState() == _manager.IDLE) {
                _go();
            }
        } catch (IllegalActionException ex) {
            report(ex);
        }
    }

    /** Initialize the applet.
     */
    public void init() {
        super.init();
        try {
            // Find out how many iterations the director expects to run for.
            int iterations =
                ((IntToken)(_director.iterations.getToken())).intValue();

            Ramp ramp = new Ramp(_toplevel, "ramp");
            ramp.step.setExpression("PI/10.0");
            Sine sine = new Sine(_toplevel, "sine");
            MultiplyDivide mult = new MultiplyDivide(_toplevel, "mult");
            SketchedSource source = new SketchedSource(_toplevel,"source");
            SequencePlotter plotter = new SequencePlotter(_toplevel,"plotter");

            // Note: The order of the following is important.
            // First, specify how long the sketched plot should be.
            source.length.setToken(new IntToken(iterations));

            // Specify that the plotter should put its display at dataset 1.
            // (If you give it additional inputs, it will use dataset 2,
            // 3, etc., in order).  This must be called before you call
            // place() on plotter, or the plotter will clear the wrong
            // dataset.
            plotter.startingDataset.setExpression("1");

            // Do not fill on wrapup, since this is disoriented when the
            // user has just drawn a trace to have the trace rescaled.
            plotter.fillOnWrapup.setExpression("false");

            // Then, create the plot and place it in this applet,
            // and specify to both the source and destination actors
            // to use the same plot widget.
            EditablePlot plot = new EditablePlot();
            plot.setSize(700, 300);
            plot.setTitle("Editable envelope");
            plot.setYRange(-1, 1);
            plot.setXRange(0, iterations);
            plot.setButtons(true);
            getContentPane().add(plot);
            plotter.place(plot);
            source.place(plot);
            plot.setBackground(getBackground());
            plot.addEditListener(this);

            _toplevel.connect(ramp.output, sine.input);
            _toplevel.connect(sine.output, mult.multiply);
            _toplevel.connect(source.output, mult.multiply);
            _toplevel.connect(mult.output, plotter.input);
        } catch (Exception ex) {
            report("Error constructing model.", ex);
        }
    }

    /** Do not execute the model on startup.
     */
    public void start() {
    }
}
