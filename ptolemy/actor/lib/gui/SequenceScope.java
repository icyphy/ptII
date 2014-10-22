/* Plot sequences that are potentially infinitely long.

 @Copyright (c) 1998-2014 The Regents of the University of California.
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
 */
package ptolemy.actor.lib.gui;

import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.plot.Plot;

///////////////////////////////////////////////////////////////////
//// SequenceScope

/**
 <p>A sequence plotter for sequences that are potentially infinitely long.
 This plotter contains an instance of the Plot
 class from the Ptolemy plot package as a public member. Data at
 the input, which can consist of any number of channels, are plotted
 on this instance.  Each channel is plotted as a separate data set.
 The horizontal axis represents the count of the iterations, modulo
 the <i>width</i> parameter, scaled by the <i>xUnit</i> parameter.
 The <i>width</i> parameter must be an integer that gives the width
 of the plot in number of samples. It defaults to 10.
 If the <i>persistence</i> parameter is positive, then it specifies
 the number of points that are remembered. It also defaults to 10.
 Any points older than these are erased and forgotten.
 The horizontal increment between samples is given by the
 <i>xUnit</i> parameter. Its default value is 1.0. The horizontal value
 of the first sample is given by the <i>xInit</i> parameter.
 Its default value is 0.0. The input is of type DoubleToken.
 </p><p>
 Note that this actor uses XOR drawing mode, which is fast, but
 will produce sometimes very annoying artifacts. In particular,
 when lines or points overlap, they will erase each other (if they
 are from the same data set) or produce some other color (if
 they are from distinct data sets).  To avoid these artifacts,
 use ArrayPlotter.</p>

 @author  Edward A. Lee
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Yellow (neuendor)
 @see ArrayPlotter
 */
public class SequenceScope extends SequencePlotter {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SequenceScope(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // set the parameters
        width = new Parameter(this, "width", new IntToken(10));
        width.setTypeEquals(BaseType.INT);
        persistence = new Parameter(this, "persistence", new IntToken(10));
        persistence.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The width of the X axis, in number of samples (an integer). */
    public Parameter width;

    /** The number of samples to be displayed at any one time (an integer). */
    public Parameter persistence;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Notify this that an attribute has changed.  If either parameter
     *  is changed, then this actor updates the configuration of the
     *  visible plot.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the expression of the
     *  attribute cannot be parsed or cannot be evaluated.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if ((attribute == width || attribute == xUnit || attribute == xInit)
                && plot != null) {
            // Ensure that protected variables are set.
            super.attributeChanged(attribute);

            int widthValue = ((IntToken) width.getToken()).intValue();
            plot.setXRange(_xInit, _xUnit * widthValue + _xInit);
        } else if (attribute == persistence && plot != null) {
            int persValue = ((IntToken) persistence.getToken()).intValue();

            // NOTE: We assume the superclass ensures this cast is safe.
            ((Plot) plot).setPointsPersistence(persValue);
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Initialize this actor.  This method configures the visible plot
     *  using the current parameter values.
     *  @exception IllegalActionException If the parent class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        int widthValue = ((IntToken) width.getToken()).intValue();
        plot.setXRange(_xInit, _xUnit * widthValue + _xInit);
        plot.setWrap(true);

        int persValue = ((IntToken) persistence.getToken()).intValue();

        // NOTE: We assume the superclass ensures this cast is safe.
        ((Plot) plot).setPointsPersistence(persValue);

        // Override the default so that there are not gaps in the lines.
        // NOTE: We assume the superclass ensures this cast is safe.
        if (((Plot) plot).getMarksStyle().equals("none")) {
            ((Plot) plot).setMarksStyle("pixels");
        }

        plot.repaint();
    }

    /** Call the base class postfire() method, then yield this
     *  thread so that the event thread gets a chance.  This is necessary,
     *  because otherwise the swing thread may be starved and accumulate a
     *  large number of points waiting to be plotted.
     *  @exception IllegalActionException If there is no director,
     *   or if the base class throws it.
     *  @return True if it is OK to continue.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        boolean result = super.postfire();
        Thread.yield();
        return result;
    }
}
