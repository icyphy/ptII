/* Base class for plotters.

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
@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Yellow (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.actor.*;
import ptolemy.plot.*;
import java.awt.Panel;
import java.awt.Dimension;

/** Base class for plotters.  This class contains an instance of the
 *  Plot class from the Ptolemy plot package as a public member.
 *  It provides a parameter that determines whether to fill the plot
 *  when wrapup is invoked.
 *
 *  @author  Edward A. Lee
 *  @version $Id$
 */
public class Plotter extends TypedAtomicActor implements Placeable {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Plotter(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        fillOnWrapup = new Parameter(this, "fillOnWrapup",
                new BooleanToken(true));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** @serial The plot object. */
    public Plot plot;

    /** @serial If true, fill the plot when wrapup is called.
     *  This parameter has type BooleanToken, and default value true.
     */
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
            Plotter newobj = (Plotter)super.clone(ws);
            newobj.fillOnWrapup
                = (Parameter)newobj.getAttribute("fillOnWrapup");
            return newobj;
        } catch (CloneNotSupportedException ex) {
            // Errors should not occur here...
            throw new InternalErrorException(
                    "Clone failed: " + ex.getMessage());
        }
    }

    /** If the plot has not already been created, create it.
     *  If a panel has been specified, and that panel is an instance
     *  of Plot, then plot data to that instance.  If a panel has been
     *  specified but it is not an instance of Plot, then create a new
     *  instance of Plot and place the plot in that panel
     *  using its add() method.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        if (plot == null) {
            setPanel(_panel);
        } else {
            plot.clear(false);
        }
        plot.repaint();
    }

    /** Specify the panel into which this plot should be placed.
     *  This method needs to be called before the first call to initialize().
     *  Otherwise, the plot will be placed in its own frame.
     *  The plot is also placed in its own frame if this method
     *  is called with a null argument.
     *
     *  @param panel The panel into which to place the plot.
     */
    public void setPanel(Panel panel) {
        _panel = panel;
        if (_panel == null) {
            // place the plot in its own frame.
            plot = new Plot();
            PlotFrame frame = new PlotFrame(getFullName(), plot);
        } else {
            if (_panel instanceof Plot) {
                plot = (Plot)_panel;
            } else {
                plot = new Plot();
                _panel.add(plot);
                plot.setButtons(true);
                Dimension size = _panel.getSize();
                plot.setSize(size.width, size.height);
            }
        }
    }

    /** If the <i>fillOnWrapup</i> parameter is true, rescale the
     *  plot so that all the data is visible.
     */
    public void wrapup() {
        try {
            if(((BooleanToken)fillOnWrapup.getToken()).booleanValue()) {
                plot.fillPlot();
            }
        } catch (IllegalActionException ex) {
            // fillOnWrapup does not evaluate to a valid token,
            // skip fillPlot()
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    /** @serial Panel into which this histogram should be placed */
    private Panel _panel;
}
