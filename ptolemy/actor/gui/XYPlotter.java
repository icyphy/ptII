/* Plot XY functions of time.

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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.actor.*;
import ptolemy.actor.lib.TimedActor;
import ptolemy.plot.*;
import java.awt.Panel;

/** A XY signal plotter.  This plotter contains an instance of the Plot class
 *  from the Ptolemy plot package as a public member.  Data at the inputX and
 *  input Y, which consist of only one channel, is plotted on this instance.
 *  The horizontal axis is inputX and vertical axis is inputY.
 *
 *  @author Jie Liu
 *  @version $Id$
 */
public class XYPlotter extends Plotter implements Placeable {

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

        // create the input ports and make them single ports.
        inputX = new TypedIOPort(this, "inputX", true, false);
        inputX.setMultiport(false);
        inputX.setTypeEquals(DoubleToken.class);

        inputY = new TypedIOPort(this, "inputY", true, false);
        inputY.setMultiport(false);
        inputY.setTypeEquals(DoubleToken.class);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Input port for data stream X, which has type DoubleToken. */
    public TypedIOPort inputX;

    /** Input port for data stream Y, which has type DoubleToken. */
    public TypedIOPort inputY;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) {
        XYPlotter newobj = (XYPlotter)super.clone(ws);
        newobj.inputX = (TypedIOPort)newobj.getPort("inputX");
        newobj.inputX.setMultiport(false);
        newobj.inputX.setTypeEquals(DoubleToken.class);
        newobj.inputY = (TypedIOPort)newobj.getPort("inputY");
        newobj.inputY.setMultiport(false);
        newobj.inputY.setTypeEquals(DoubleToken.class);
        return newobj;
    }

    /** Read at most one input from each input port and plot it.
     *  This is done in postfire to ensure that data has settled.
     *  Both input port should have at least one tokens. Otherwise,
     *  one token will be consumed from the input port that has
     *  a token, but nothing will be plotted.
     *  @exception IllegalActionException If there is no director, or
     *   if the base class throws it.
     *  @return True if it is OK to continue.
     */
    public boolean postfire() throws IllegalActionException {
        boolean hasX = false, hasY = false;
        double xValue = 0.0;
        double yValue = 0.0;
        if (inputX.hasToken(0)) {
            xValue = ((DoubleToken)inputX.get(0)).doubleValue();
            hasX = true;
        }
        if (inputY.hasToken(0)) {
            yValue = ((DoubleToken)inputY.get(0)).doubleValue();
            hasY = true;
        }
        if(hasX && hasY) {
            plot.addPoint(0, xValue, yValue, true);
        }
        return super.postfire();
    }
}
