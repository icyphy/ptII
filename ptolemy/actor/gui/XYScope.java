/* Plot X-Y data with finite persistence.

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
import ptolemy.actor.lib.TimedActor;
import ptolemy.plot.*;
import java.awt.Panel;

import javax.swing.SwingUtilities;

/**
An X-Y plotter that plots with finite persistence.
This plotter contains an instance of the Plot class
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
If the <i>persistence</i> parameter is positive, then it specifies
the number of points that are shown.
It defaults to 100, so any point older than 100 samples is
erased and forgotten. The inputs are of type DoubleToken.

@author  Edward A. Lee
@version $Id$
 */
public class XYScope extends XYPlotter {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public XYScope(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // set the parameters
        persistence = new Parameter(this, "persistence", new IntToken(100));
        persistence.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The number of samples from each input channel
     *  displayed at any one time (an integer).
     */
    public Parameter persistence;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Notification that an attribute has changed.
     *  @exception IllegalActionException If the expression of the
     *   attribute cannot be parsed or cannot be evaluated.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == persistence && plot != null) {
            int persValue =
                    ((IntToken)persistence.getToken()).intValue();
            plot.setPointsPersistence(persValue);
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then updates the ports and parameters.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has an
     *   attribute that cannot be cloned.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        XYScope newobj = (XYScope)super.clone(ws);
        newobj.persistence = (Parameter)newobj.getAttribute("persistence");
        return newobj;
    }

    /** Configure the plotter using the current parameter values.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        int persValue = ((IntToken)persistence.getToken()).intValue();
        plot.setPointsPersistence(persValue);
        plot.repaint();
        // Override the default so that there are not gaps in the lines.
        if (plot.getMarksStyle().equals("none")) {
            plot.setMarksStyle("pixels");
        }
    }

    /** Call the base class postfire() method, then yield so that the
     *  event thread gets a chance.
     *  @exception IllegalActionException If there is no director,
     *   or if the base class throws it.
     *  @return True if it is OK to continue.
     */
    public boolean postfire() throws IllegalActionException {
        boolean result = super.postfire();
        Thread.yield();
        return result;
    }
}
