/* Plot sequences that are potentially infinitely long.

@Copyright (c) 1998-2000 The Regents of the University of California.
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

package ptolemy.actor.gui;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.*;
import ptolemy.actor.*;
import ptolemy.actor.lib.SequenceActor;
import ptolemy.plot.*;

import javax.swing.SwingUtilities;

/**
A sequence plotter for sequences that are potentially infinitely long.
This plotter contains an instance of the Plot
class from the Ptolemy plot package as a public member. Data at
the input, which can consist of any number of channels, is plotted
on this instance.  Each channel is plotted as a separate data set.
The horizontal axis represents the count of the iterations, modulo
the <i>width</i> parameter, scaled by the <i>xUnit</i> parameter.
The <i>width</i> parameter must is an integer that gives the width
of the plot in number of samples. It defaults to 100.
If the <i>persistence</i> parameter is positive, then it specifies
the number of points that are remembered. It also defaults to 100.
Any points older than these are erased and forgotten.
The horizontal increment between samples is given by the
<i>xUnit</i> parameter. Its default value is 1.0. The horizontal value
of the first sample is given by the <i>xInit</i> parameter.
Its default value is 0.0. The input is of type DoubleToken.

@author  Edward A. Lee
@version $Id$
 */
public class SequenceScope extends SequencePlotter implements SequenceActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SequenceScope(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // set the parameters
        width = new Parameter(this, "width", new IntToken(100));
        persistence = new Parameter(this, "persistence", new IntToken(100));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The increment of the X axis. */
    public Parameter width;

    /** The start point of the X axis. */
    public Parameter persistence;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Notification that an attribute has changed.
        @exception IllegalActionException If the expression of the
        attribute cannot be parsed or cannot be evaluated.
    */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == width && plot != null) {
            double xunit = ((DoubleToken)xInit.getToken()).doubleValue();
            int widthValue = ((IntToken)width.getToken()).intValue();
            plot.setXRange(0.0, xunit*widthValue);
        } else if (attribute == persistence && plot != null) {
            int persValue = ((IntToken)persistence.getToken()).intValue();
            plot.setPointsPersistence(persValue);
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets up the parameters.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has an
     *   attribute that cannot be cloned.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        SequenceScope newobj = (SequenceScope)super.clone(ws);
        newobj.width = (Parameter)newobj.getAttribute("width");
        newobj.persistence = (Parameter)newobj.getAttribute("persistence");
        return newobj;
    }

    /** Configure the plotter using the current parameter values.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        double xunit = ((DoubleToken)xUnit.getToken()).doubleValue();
        int widthValue = ((IntToken)width.getToken()).intValue();
        plot.setXRange(0.0, xunit*widthValue);
        plot.setWrap(true);
        plot.setGrid(false);
        // Default marks style is consistent with scope style.
        plot.setMarksStyle("pixels");
        int persValue = ((IntToken)persistence.getToken()).intValue();
        plot.setPointsPersistence(persValue);
        plot.repaint();
    }

    /** Read at most one token from each input channel and plot it as
     *  a function of the iteration number, scaled by <i>xUnit</i>.
     *  The first point is plotted at the horizontal position given by
     *  <i>xInit</i>. The increments on the position are given by
     *  <i>xUnit</i>. The input data are plotted in postfire() to
     *  ensure that the data have settled.
     *  @exception IllegalActionException If there is no director,
     *   or if the base class throws it.
     *  @return True if it is OK to continue.
     */
    public boolean postfire() throws IllegalActionException {
        Runnable doPostfire = new Runnable() {
            public void run() {
                try {
                    _xValue += _xUnit;
                    int width = input.getWidth();
                    int offset = 
                        ((IntToken)startingDataset.getToken()).intValue();
                    for (int i = width - 1; i >= 0; i--) {
                        if (input.hasToken(i)) {
                            DoubleToken curToken = (DoubleToken)input.get(i);
                            double curValue = curToken.doubleValue();
                            plot.addPoint(i + offset, _xValue, curValue, true);
                        }
                    }
                } catch (IllegalActionException ex) {
                    getManager().notifyListenersOfException(ex);
                }
            }
        };
        try {
            SwingUtilities.invokeAndWait(doPostfire);
        } catch (Exception ex) {
            // Ignore InterruptedException.
            // Other exceptions should not occur.
        }
        return true;
    }

    /** If the <i>fillOnWrapup</i> parameter is true, rescale the
     *  plot so that all the data is visible.  This overrides the base
     *  class to do the fill in the event thread.
     */
    public void wrapup() {
        try {
            if(((BooleanToken)fillOnWrapup.getToken()).booleanValue()) {
                Runnable doFill = new Runnable() {
                    public void run() {
                        plot.fillPlot();
                    }
                };
                try {
                    SwingUtilities.invokeAndWait(doFill);
                } catch (Exception ex) {
                    // Ignore InterruptedException.
                    // Other exceptions should not occur.
                }
            }
        } catch (IllegalActionException ex) {
            // fillOnWrapup does not evaluate to a valid token,
            // skip fillPlot()
        }
    }
}
