/* Plot XY functions.

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
@ProposedRating Green (liuj@eecs.berkeley.edu)
@AcceptedRating Green (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.*;
import ptolemy.actor.*;
import ptolemy.plot.*;
import java.awt.Panel;

/**
An XY plotter.  This plotter contains an instance of the Plot class
from the Ptolemy plot package as a public member.
Data at <i>inputX</i> and <i>inputY</i> are plotted on this instance.
Both <i>inputX</i> and <i>inputY</i> are multiports with type DOUBLE.
When plotted, the first channel of <i>inputX</i> and the first channel
of <i>inputY</i> are together considered the first signal,
then the second channel of <i>inputX</i> and the second channel
of <i>inputY</i> are considered the second signal, and so on.
This requires that <i>inputX</i> and
<i>inputY</i> have the same width. The actor
assumes that there is at least one token available on each channel
when it fires. The horizontal axis is given by the value of the
input from <i>inputX</i> and vertical axis is given by <i>inputY</i>.

@author Jie Liu
@version $Id$
 */
public class XYPlotter extends Plotter {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public XYPlotter(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Create the input ports and make them single ports.
        inputX = new TypedIOPort(this, "inputX", true, false);
        inputX.setMultiport(true);
        inputX.setTypeEquals(BaseType.DOUBLE);

        inputY = new TypedIOPort(this, "inputY", true, false);
        inputY.setMultiport(true);
        inputY.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Input port for the horizontal axis, with type DOUBLE. */
    public TypedIOPort inputX;

    /** Input port for the vertical axis, with type DOUBLE. */
    public TypedIOPort inputY;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has an
     *   attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        XYPlotter newobj = (XYPlotter)super.clone(workspace);
        newobj.inputX = (TypedIOPort)newobj.getPort("inputX");
        newobj.inputY = (TypedIOPort)newobj.getPort("inputY");
        return newobj;
    }

    /** Read at most one token from each channel of each input port
     *  and plot it.
     *  This is done in postfire to ensure that data has settled.
     *  The width of the inputs should be the same, otherwise a
     *  exception will be thrown. The channels from the two input
     *  ports are matched to give the X and Y position of a single
     *  data point.  Each matched channel pair must have
     *  at least one token, or
     *  a token will be consumed from the input channel that has
     *  a token, but nothing will be plotted.
     *  @exception IllegalActionException If there is no director,
     *   the width of the ports are not the same, or
     *   if the base class throws it.
     *  @return True if it is OK to continue.
     */
    public boolean postfire() throws IllegalActionException {
        boolean hasX = false, hasY = false;
        double xValue = 0.0;
        double yValue = 0.0;
        int widthX = inputX.getWidth();
        int widthY = inputY.getWidth();
        if (widthX != widthY) {
            throw new IllegalActionException(this,
                    " The number of input channels mismatch.");
        }
        int offset = ((IntToken)startingDataset.getToken()).intValue();
        for (int i = widthX - 1; i >= 0; i--) {
            if (inputX.hasToken(i)) {
                xValue = ((DoubleToken)inputX.get(i)).doubleValue();
                hasX = true;
            }
            if (inputY.hasToken(i)) {
                yValue = ((DoubleToken)inputY.get(i)).doubleValue();
                hasY = true;
            }
            if(hasX && hasY) {
                plot.addPoint(i + offset, xValue, yValue, true);
            }
        }
        return super.postfire();
    }
}
