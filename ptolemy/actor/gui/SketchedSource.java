/* A source of sketched signals.

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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Yellow (vogel@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.*;
import ptolemy.actor.*;
import ptolemy.actor.lib.SequenceActor;
import ptolemy.actor.lib.Source;
import ptolemy.plot.*;

import java.awt.Container;

/** This actor produces as its output a signal that has been sketched by the
 *  user on the screen.  The <i>length</i> parameter specifies the
 *  number of samples in the sketched signal.  The <i>period</i>
 *  parameter, if greater than zero, specifies the period with which
 *  the signal should be repeated (in samples).  If the period is longer
 *  than the length, then zeros will be inserted. If the period is less
 *  than the length, then the last few values will not be used.
 *  If this parameter is zero or
 *  negative, then the sketched signal is produced exactly once,
 *  at the beginning of the execution of the model.  If the period
 *  is greater than zero, and the sketch is modified during execution
 *  of the model, then the modification appears in the next cycle of
 *  the period after the modification has been completed.  In other
 *  words, the change does not appear mid-cycle.
 *  <p>
 *  This actor can be used with its own plot widget, which is the
 *  default behavior, or more interestingly, it can share a plot
 *  widget with another plot object.  This way, the sketch can be
 *  be made right on a plot that also displays the results of
 *  processing the sketched signal.
 *  <p>
 *  When this actor has its own plot widget, you can specify where that
 *  widget appears by calling place().  If you do not, then the
 *  plot widget will be created in its own window.  You can also
 *  call place() with an argument that is an instance of EditablePlot.
 *  This is how you create a shared plot.  That same instance of
 *  EditablePlot can be used by another actor, such as SequencePlotter,
 *  to display data.  Be sure to set the <i>dataset</i> parameter
 *  of this actor or the <i>dataset</i> parameter of the
 *  other actor so that they do not use the same dataset numbers.
 *
 *  @author  Edward A. Lee
 *  @version $Id$
 */
public class SketchedSource extends Source
    implements Placeable, SequenceActor {

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
        output.setTypeEquals(BaseType.DOUBLE);

        // Create the parameters.
        length = new Parameter(this, "length", new IntToken(100));
        period = new Parameter(this, "period", new IntToken(0));
        dataset = new Parameter(this, "dataset", new IntToken(0));
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

    /** The starting dataset number to which data is plotted.
     *  This parameter has type IntToken, with default value 0.
     *  Its value must be non-negative.
     */
    public Parameter dataset;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the specified attribute is <i>length</i> or <i>dataset</i>,
     *  then set the trace to its initial value;  if it is
     *  <i>dataset</i>, then check that it is not negative.
     *  @exception IllegalActionException If the specified attribute
     *   is <i>dataset</i> and its value is negative.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == dataset) {
            if(((IntToken)dataset.getToken()).intValue() < 0) {
                throw new IllegalActionException(this,
                        "dataset: negative value is not allowed.");
            }
            setInitialTrace();
        } else if (attribute == length) {
            setInitialTrace();
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        SketchedSource newobj = (SketchedSource)super.clone(workspace);
        newobj.length = (Parameter)newobj.getAttribute("length");
        newobj.period = (Parameter)newobj.getAttribute("period");
        newobj.dataset = (Parameter)newobj.getAttribute("dataset");
        return newobj;
    }

    /** Produce one data sample from the sketched signal on the output
     *  port.
     *  @exception IllegalActionException If there is no director, or
     *   if the base class throws it.
     */
    public void fire() throws IllegalActionException {
        // Read the trigger input, if there is one.
        super.fire();
        int prd = ((IntToken)period.getToken()).intValue();
        if (_count < _data[1].length) {
            // NOTE: X value ignored.
            output.send(0, new DoubleToken(_data[1][_count]));
            _count++;
        } else {
            output.send(0, _zero);
            if (_count < prd) {
                _count++;
            }
        }
        if (prd > 0 && _count >= prd) {
            // Reread the data in case it has changed.
            _count = 0;
            int set = ((IntToken)dataset.getToken()).intValue();
            _data = ((EditablePlot)plot).getData(set);
        }
    }

    /** Override the base class to reset the X axis counter, ensuring
     *  that the next output produced by fire() is the first value of the
     *  sketch.  Also, read the sketched data.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        if (plot == null) {
            place(_container);
        }
        // NOTE: Do not clear the plot here, as that will erase
        // user-entered data!
        _count = 0;
        int set = ((IntToken)dataset.getToken()).intValue();
        _data = ((EditablePlot)plot).getData(set);
    }

    /** Specify the container into which this editable plot should be placed.
     *  This method needs to be called before the first call to initialize().
     *  Otherwise, the plot will be placed in its own frame.
     *  The plot is also placed in its own frame if this method
     *  is called with a null argument.
     *  This method can be called with an instance of EditablePlot
     *  as an argument, in which case, it will use that instance.
     *  This way, the same plot object can be shared by a SequencePlotter
     *  actor and this actor.
     *
     *  @param container The container into which to place the plot.
     */
    public void place(Container container) {
        _container = container;
        if (_container == null) {
            // place the plot in its own frame.
            plot = new EditablePlot();
            PlotFrame frame = new PlotFrame(getFullName(), plot);
        } else {
            if (_container instanceof EditablePlot) {
                plot = (EditablePlot)_container;
            } else {
                plot = new EditablePlot();
                _container.add(plot);
                plot.setButtons(true);
                plot.setBackground(_container.getBackground());
            }
        }
        // Set the default signal value in the plot.
        try {
            setInitialTrace();
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(ex.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public members                    ////

    /** The editable plot object. */
    public EditablePlot plot;

    ///////////////////////////////////////////////////////////////////
    ////                         protected members                 ////

    /** Graphical container into which this plot should be placed */
    protected Container _container;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Set the initial value of the plot and fill the plot.
    // If the plot is null, return without doing anything.
    private void setInitialTrace() throws IllegalActionException {
        if (plot == null) return;
        int set = ((IntToken)dataset.getToken()).intValue();
        plot.clear(set);
        int len = ((IntToken)length.getToken()).intValue();
        boolean connected = false;
        for(int i = 0; i < len; i++) {
            plot.addPoint(set, (double)i, 0.0, connected);
            connected = true;
        }
        plot.repaint();
        plot.fillPlot();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    /** Current position in the signal. */
    private int _count;

    /** Sketched data. */
    private double[][] _data;

    /** Zero token. */
    private DoubleToken _zero = new DoubleToken(0.0);
}
