/* Plot functions of time in oscilloscope style.

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

import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.plot.Plot;

///////////////////////////////////////////////////////////////////
//// TimedScope

/**
 <p>A signal plotter that plots in an oscilloscope style, meaning that the
 horizontal axis is wrapped and that there is finite persistence.  This
 plotter contains an instance of the Plot class from the Ptolemy plot
 package as a public member.  Data at the input, which can consist of
 any number of channels, are plotted on this instance.  Each channel is
 plotted as a separate data set.  The input is of type DoubleToken.

 </p><p>The horizontal axis represents time.  The <i>width</i> parameter is
 a double that gives the width of the plot. The horizontal axis will be
 labeled from 0.0 to <i>width</i>.  It defaults to 10.0.

 </p><p>If the <i>persistence</i> parameter is positive, then it specifies
 the amount of time into the past that points are shown.  It also
 defaults to 10.0, so any point older than 10.0 time units is erased and
 forgotten.</p>

 @author  Edward A. Lee
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Yellow (neuendor)
 */
public class TimedScope extends TimedPlotter {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public TimedScope(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // set the parameters
        width = new Parameter(this, "width", new DoubleToken(10.0));
        width.setTypeEquals(BaseType.DOUBLE);
        persistence = new Parameter(this, "persistence", new DoubleToken(10.0));
        persistence.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The width of the X axis (a double). */
    public Parameter width;

    /** The amount of data displayed at any one time (a double).
     *  This has units of the X axis.
     */
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
        if (attribute == width && plot != null) {
            double widthValue = ((DoubleToken) width.getToken()).doubleValue();
            plot.setXRange(0.0, widthValue);
        } else if (attribute == persistence && plot != null) {
            double persValue = ((DoubleToken) persistence.getToken())
                    .doubleValue();

            // NOTE: We assume the superclass ensures this cast is safe.
            ((Plot) plot).setXPersistence(persValue);
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Initialize this actor.  This method configures the plotter
     *  using the current parameter values.
     *  @exception IllegalActionException If the parent class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        double widthValue = ((DoubleToken) width.getToken()).doubleValue();
        plot.setXRange(0.0, widthValue);

        // NOTE: We assume the superclass ensures this cast is safe.
        ((Plot) plot).setWrap(true);

        double persValue = ((DoubleToken) persistence.getToken()).doubleValue();
        ((Plot) plot).setXPersistence(persValue);
        plot.repaint();

        // Override the default so that there are not gaps in the lines.
        if (((Plot) plot).getMarksStyle().equals("none")) {
            ((Plot) plot).setMarksStyle("pixels");
        }
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
