/* Plot bar graphs, given arrays of doubles as inputs.

@Copyright (c) 1998-2001 The Regents of the University of California.
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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.gui;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.SequenceActor;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.plot.Plot;

import java.awt.Container;

/**
A bar graph plotter.  This plotter contains an instance of the Plot
class from the Ptolemy plot package as a public member. Data at
the input, which can consist of any number of channels, are plotted
on this instance.  Each input channel is plotted as a separate data set.
Each input token is an array of doubles.

@author  Edward A. Lee
@version $Id$
 */
public class BarGraph extends Plotter implements SequenceActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public BarGraph(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Create the input port and make it a multiport.
        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);
        input.setTypeEquals(BaseType.DOUBLE);
        input.setTypeEquals(new ArrayType(BaseType.DOUBLE));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Input port, which has type DoubleToken. */
    public TypedIOPort input;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Specify the container into which this plot should be placed.
     *  This method needs to be called before the first call to initialize().
     *  Otherwise, the plot will be placed in its own frame.
     *  The plot is also placed in its own frame if this method
     *  is called with a null argument. This method overrides the base
     *  class to set the plot style so that a bar graph is what we get.
     *  @param container The container into which to place the plot.
     */
    public void place(Container container) {
        super.place(container);
        plot.setBars(true);
        plot.setConnected(false);
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
        int width = input.getWidth();
        int offset = ((IntToken)startingDataset.getToken()).intValue();
        for (int i = width - 1; i >= 0; i--) {
            if (input.hasToken(i)) {
                plot.clear(i + offset);
                Token[] currentArray = ((ArrayToken)input.get(i)).arrayValue();
                for (int j = 0; j < currentArray.length; j++) {
                    double currentValue =
                            ((DoubleToken)currentArray[j]).doubleValue();
                    plot.addPoint(i + offset, j, currentValue, true);
                }
            }
        }
        return super.postfire();
    }
}
