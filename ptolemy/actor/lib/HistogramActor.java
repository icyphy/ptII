/*
@Copyright (c) 1998-1999 The Regents of the University of California.
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
@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.actor.*;
import ptolemy.plot.*;
import java.awt.Panel;

/** A histogram plotter.  This plotter contains an instance of the Histogram
 *  class from the Ptolemy plot package as a public member.  A histogram
 *  of data at the input, which can consist of any number of channels,
 *  is plotted on this instance.
 *
 *  @author  Edward A. Lee
 *  @version $Id$
 */
public class HistogramActor extends TypedAtomicActor implements Placeable {

    public HistogramActor(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // create the input port and make it a multiport.
        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);
        input.setTypeEquals(DoubleToken.class);
        fillOnWrapup = new Parameter(this, "fillOnWrapup",
                new BooleanToken(true));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public members                    ////

    /** Input port. */
    public TypedIOPort input;

    /** The histogram object. */
    public Histogram histogram;

    /** If true, fill the histogram when wrapup is called. */
    public Parameter fillOnWrapup;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) {
        try {
            HistogramActor newobj =
                (HistogramActor)super.clone(ws);
            newobj.input = (TypedIOPort)newobj.getPort("input");
            newobj.input.setMultiport(true);
            newobj.input.setTypeEquals(DoubleToken.class);
            newobj.fillOnWrapup
                = (Parameter)newobj.getAttribute("fillOnWrapup");
            return newobj;
        } catch (CloneNotSupportedException ex) {
            // Errors should not occur here...
            throw new InternalErrorException(
                    "Clone failed: " + ex.getMessage());
        }
    }

    /** Read all available inputs and update the histogram.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        int width = input.getWidth();
        for (int i = width - 1; i >= 0; i--) {
            if (input.hasToken(i)) {
                DoubleToken curToken = (DoubleToken)input.get(i);
                double curValue = curToken.doubleValue();
                histogram.addPoint(i, curValue);
            }
        }
    }

    /** If the histogram has not already been created, create it using
     *  setPanel().
     */
    public void initialize() {
        if (histogram == null) {
            setPanel(_panel);
        } else {
            histogram.clear(false);
        }
        histogram.repaint();
    }

    /** Specify the panel into which this histogram should be placed.
     *  This method needs to be called before the first call to initialize().
     *  Otherwise, the histogram will be placed in its own frame.
     *  The histogram is also placed in its own frame if this method
     *  is called with the argument null.
     *  If the panel argument is an instance of
     *  of Histogram, then plot data to that instance.  If a panel has been
     *  specified but it is not an instance of histogram, then create a new
     *  instance of Histogram and place it in that panel
     *  using its add() method.
     *
     *  @param panel The panel into which to place the histogram.
     */
    public void setPanel(Panel panel) {
        _panel = panel;
        if (_panel == null) {
            // place the histogram in its own frame.
            histogram = new Histogram();
            PlotFrame frame = new PlotFrame(getFullName(), histogram);
        } else {
            if (_panel instanceof Histogram) {
                histogram = (Histogram)_panel;
            } else {
                histogram = new Histogram();
                _panel.add(histogram);
                histogram.setButtons(true);
            }
        }
    }

    /** Rescale the histogram so that all the data is visible if the
     *  fillOnWrapup parameter is true.
     *
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void wrapup() throws IllegalActionException {
        if(((BooleanToken)fillOnWrapup.getToken()).booleanValue()) {
            histogram.fillPlot();
        }
        super.wrapup();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    private Panel _panel;
}
