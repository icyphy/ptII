/* An actor for 2-D visualization of matrices.

@Copyright (c) 1998-2002 The Regents of the University of California.
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


package ptolemy.actor.lib.gui;

import ptolemy.actor.AtomicActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.Placeable;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntMatrixToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.plot.PlotFrame;
import ptolemy.plot.Render;

import java.awt.Container;
import java.util.List;

//import java.net.URL;



/**
An actor that visualizes a matrix using a 2-D visualization.
This actor uses an instance of the Render
class from the Ptolemy plot package to do the rendering.

@author  Neil Turner and Steve Neuendorffer
@version $Id$
 */
public class MatrixVisualizer extends TypedAtomicActor implements Placeable {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public MatrixVisualizer(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Create the input port and make it a multiport.
        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);
        input.setTypeEquals(BaseType.INT_MATRIX);

        // set the parameters
        xMax = new Parameter(this, "xMax", new DoubleToken(0.0));
        xMax.setTypeEquals(BaseType.DOUBLE);
        xMin = new Parameter(this, "xMin", new DoubleToken(0.0));
        xMin.setTypeEquals(BaseType.DOUBLE);
        yMax = new Parameter(this, "yMax", new DoubleToken(0.0));
        yMax.setTypeEquals(BaseType.DOUBLE);
        yMin = new Parameter(this, "yMin", new DoubleToken(0.0));
        yMin.setTypeEquals(BaseType.DOUBLE);

        // initialize the parameters
        attributeChanged(xMax);
        attributeChanged(xMin);
        attributeChanged(yMax);
        attributeChanged(yMin);

    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Input port, which has type DoubleToken. */
    public TypedIOPort input;

    /** The render object. */
    public transient Render render;

    /** The maximum of the x-axis. */
    public Parameter xMax;

    /** The minimum of the x-axis. */
    public Parameter xMin;

    /** The maximum of the y-axis. */
    public Parameter yMax;

    /** The minimum of the y-axis. */
    public Parameter yMin;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Notification that an attribute has changed.
     *  @exception IllegalActionException If the expression of the
     *  attribute cannot be parsed or cannot be evaluated.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == xMax) {
            _xMax = ((DoubleToken)xMax.getToken()).doubleValue();
        } else if (attribute == xMin) {
            _xMin = ((DoubleToken)xMin.getToken()).doubleValue();
        } else if (attribute == yMax) {
            _yMax = ((DoubleToken)yMax.getToken()).doubleValue();
        } else if (attribute == yMin) {
            _yMin = ((DoubleToken)yMin.getToken()).doubleValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Reset the x axis counter, and call the base class.
     *  Also, clear the datasets that this actor will use.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        if (render == null) {
            place(_container);
        }
        if (_frame != null) {
	    _frame.setVisible(true);
        }
        render.clearData();
        render.setXIncrement(1.0);
        render.setYIncrement(1.0);
        render.setXOffset(0.0);
        render.setYOffset(0.0);
        render.repaint();
    }

    /** Specify the container into which this render should be placed.
     *  This method needs to be called before the first call to initialize().
     *  Otherwise, the render will be placed in its own frame.
     *  The render is also placed in its own frame if this method
     *  is called with a null argument.  The size of the render,
     *  unfortunately, cannot be effectively determined from the size
     *  of the container because the container may not yet be laid out
     *  (its size will be zero).  Thus, you will have to explicitly
     *  set the size of the render by calling render.setSize().
     *  The background of the plot is set equal to that of the container
     *  (unless it is null).
     *  <p>
     *  If configure() has been called (prior to the plot getting created),
     *  then the configurations that it specified have been deferred. Those
     *  configurations are performed at this time.
     *
     *  @param container The container into which to place the plot.
     */
    public void place(Container container) {
        _container = container;
        if (_container == null) {
            // place the render in its own frame.
            render = new Render();
            _frame = new PlotFrame(getFullName(), render);
            _frame.setVisible(true);
        } else if (_container instanceof Render) {
            render = (Render)_container;
        } else {
            if (render == null) {
                render = new Render();
                render.setButtons(true);
            }
            _container.add(render);
            render.setBackground(null);
        }
    }


    /**
     *  Read at most one token from each input channel and plot it as
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
        IntMatrixToken token = (IntMatrixToken)input.get(0);
        int rows = token.getRowCount();
        int columns = token.getColumnCount();

        int stripe[] = new int[rows];

        // Clear the render object's image data.
        render.clearData();
        // Add the matrix stripe by stripe to the render object.
        for (int i = 0; i < columns; i++) {
            for (int j = 0; j < rows; j++) {
                stripe[j] = token.getElementAt(j, i);
            }
            render.addStripe(stripe);
        }
        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected members                 ////


    /** Container into which this plot should be placed */
    protected Container _container;

    /** X axis counter. */
    protected double _xValue;

    /** The maximum of the x-axis. */
    protected double _xMax;

    /** The minimum of the x-axis. */
    protected double _xMin;

    /** The maximum of the y-axis. */
    protected double _yMax;

    /** The minimum of the y-axis. */
    protected double _yMin;


    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    // The bases and input streams given to the configure() method.
    private List _configureBases = null;
    private List _configureSources = null;
    private List _configureTexts = null;

    /** Frame into which plot is placed, if any. */
    private transient PlotFrame _frame;

    // Flag indicating that the place() method has been called at least once.
    private boolean _placeCalled = false;
}
