/* A source of sketched signals.

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
import ptolemy.actor.lib.SequenceActor;
import ptolemy.actor.lib.Source;
import ptolemy.plot.*;
import java.awt.Panel;

/** This actor produces at its output a signal that is sketched by the
 *  user on the screen.
 *  FIXME- more details.  Have to add a plot.
 *
 *  @author  Edward A. Lee
 *  @version $Id$
 */
public class SketchedSource extends Source implements SequenceActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SketchedSource(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Set the type of the output port.
        output.setTypeEquals(DoubleToken.class);

        // Create the parameters.
        length = new Parameter(this, "length", new IntToken(100));
        length.setTypeEquals(IntToken.class);

        period = new Parameter(this, "period", new IntToken(0));
        period.setTypeEquals(IntToken.class);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The length of the output signal that will be generated.
     *  This parameter must contain an IntToken.  By default, it has
     *  value 100.
     */
    public Parameter length;

    /** An indicator of whether the signal should be periodically
     *  repeated, and if so, at what period.  If the value is negative
     *  or zero, it is not repeated.  Otherwise, it is repeated with
     *  the specified period.  This parameter must contain an IntToken.
     *  By default, it has value 0.
     */
    public Parameter period;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) {
        SketchedSource newobj = (SketchedSource)super.clone(ws);
        newobj.length = (Parameter)newobj.getAttribute("length");
        newobj.period = (Parameter)newobj.getAttribute("period");
        return newobj;
    }

    /** Produce one data sample from the sketched signal.
     *  FIXME - more details.
     *  @exception IllegalActionException If there is no director, or
     *   if the base class throws it.
     */
    public void fire() throws IllegalActionException {
        // Read the trigger input, if there is one.
        super.fire();
        // FIXME: Deal with periodicity.
        if (_count < _data[1].length) {
            // NOTE: X value ignored.
            output.broadcast(new DoubleToken(_data[1][_count]));
            _count++;
        } else {
            output.broadcast(_zero);
        }
    }

    /** Reset the x axis counter, and call the base class.
     *  Also, read the sketched data.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        if (plot == null) {
            setPanel(_panel);
        }
        // NOTE: Do not clear the plot here, as that will erase
        // user-entered data!

        _count = 0;
        // FIXME: Dataset fixed at 0
        _data = ((EditablePlot)plot).getData(0);
    }

    /** Specify the panel into which this editable plot should be placed.
     *  This method needs to be called before the first call to initialize().
     *  Otherwise, the plot will be placed in its own frame.
     *  The plot is also placed in its own frame if this method
     *  is called with a null argument.  The size of the plot,
     *  unfortunately, cannot be effectively determined from the size
     *  of the panel because the panel may not yet be laid out
     *  (its size will be zero).  Thus, you will have to explicitly
     *  set the size of the plot by calling plot.setSize().
     *  This method can be called with an instance of EditablePlot
     *  as an argument, in which case, it will use that instance.
     *  This way, the same plot object can be shared by a SequencePlotter
     *  actor and an instance of this actor.
     *
     *  @param panel The panel into which to place the plot.
     */
    public void setPanel(Panel panel) {
        _panel = panel;
        if (_panel == null) {
            // place the plot in its own frame.
            plot = new EditablePlot();
            PlotFrame frame = new PlotFrame(getFullName(), plot);
        } else {
            if (_panel instanceof EditablePlot) {
                plot = (EditablePlot)_panel;
            } else {
                plot = new EditablePlot();
                _panel.add(plot);
                plot.setButtons(true);
            }
        }
        // Set the default signal value in the plot.
        // FIXME: using dataset 0.
        try {
            plot.clear(0);
            int len = ((IntToken)length.getToken()).intValue();
            boolean connected = false;
            for(int i = 0; i < len; i++) {
                plot.addPoint(0, (double)i, 0.0, connected);
                connected = true;
            }
            plot.repaint();
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(ex.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public members                    ////

    /** @serial The editable plot object. */
    public EditablePlot plot;

    ///////////////////////////////////////////////////////////////////
    ////                         protected members                 ////

    /** @serial Panel into which this plot should be placed */
    protected Panel _panel;

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    /** @serial Current position in the signal. */
    private int _count;

    /** @serial Sketched data. */
    private double[][] _data;

    /** @serial Zero token. */
    private DoubleToken _zero = new DoubleToken(0.0);
}
