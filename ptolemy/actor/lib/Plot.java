/*
@Copyright (c) 1998 The Regents of the University of California.
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

						PT_COPYRIGHT_VERSION 2
						COPYRIGHTENDKEY
*/
package ptolemy.actor.lib;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import ptolemy.plot.*;
import java.awt.Panel;
import java.util.Enumeration;
import ptolemy.domains.sdf.kernel.*;

/** A signal plotter.
 *
 *  @author  Edward A. Lee
 *  @version $Id$
 */
public class Plot extends TypedAtomicActor implements Placeable {

// FIXME: Override clone() to create the port.

    public Plot(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container,name);
        // create the input port and make it a multiport.
        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);
        input.setDeclaredType(DoubleToken.class);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public void fire() throws IllegalActionException {

        double curTime =((Director)getDirector()).getCurrentTime();
        int width = input.getWidth();
        for (int i = 0; i<width; i++) {
            while (input.hasToken(i)) {
                DoubleToken curToken = (DoubleToken)input.get(i);
                double curValue = curToken.doubleValue();
                _plot.addPoint(i, curTime, curValue, true);
            }
        }
    }

    /** If the plot has not already been created, create it.
     *  If a panel has been specified, place the plot in that panel
     *  using its add() method.
     *
     *  @exception IllegalActionException Not thrown in this class.
     */
    public void initialize() throws IllegalActionException {
        if (_plot == null) {
            if (_panel == null) {
                // place the plot in its own frame.
                PlotFrame frame = new PlotFrame(getFullName());
                // FIXME: Is this cast right?
                _plot = (ptolemy.plot.Plot)(frame.plot);
            } else {
                // place the plot in the specified panel.
                _plot = new ptolemy.plot.Plot();
                _panel.add(_plot);
            }
            // FIXME: This should be parameterized?
            _plot.setButtons(true);
        } else {
            _plot.clear(true);
        }
    }

    /** Specify the panel into which this plot should be placed.
     *  This method needs to be called before the first call to initialize().
     *  Otherwise, the plot will be placed in its own frame.
     *
     *  @param panel The panel into which to place the plot.
     */
    public void setPanel(Panel panel) {
        _panel = panel;
    }

    /** Rescale the plot so that all the data is visible.
     *
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void wrapup() throws IllegalActionException {
	_plot.fillPlot();
        // FIXME: Do we really want this?
        super.wrapup();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public members                    ////

    /** Input port. */
    public TypedIOPort input;

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    private Panel _panel;
    private ptolemy.plot.Plot _plot;
}
