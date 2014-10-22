/* Plot X-Y data with finite persistence.

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
//// XYScope

/**
 <p>An X-Y plotter that plots with finite persistence.
 This plotter contains an instance of the Plot class
 from the Ptolemy plot package as a public member.
 Data at <i>inputX</i> and <i>inputY</i> are plotted on this instance.
 Both <i>inputX</i> and <i>inputY</i> are multiports that
 take a DoubleToken.
 When plotted, the first channel of <i>inputX</i> and the first channel
 of <i>inputY</i> are together considered the first signal,
 then the second channel of <i>inputX</i> and the second channel
 of <i>inputY</i> are considered the second signal, and so on.
 This requires that <i>inputX</i> and
 <i>inputY</i> have the same width.
 </p><p>
 This actor
 assumes that there is at least one token available on each channel
 when it fires. The horizontal axis is given by the value of the
 input from <i>inputX</i> and vertical axis is given by <i>inputY</i>.
 </p><p>
 If the <i>persistence</i> parameter is positive, then it specifies
 the number of points that are shown.
 It defaults to 100, so any point older than 100 samples is
 erased and forgotten.</p>

 @author  Edward A. Lee
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Yellow (neuendor)
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
    public XYScope(CompositeEntity container, String name)
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
        if (attribute == persistence && plot != null) {
            int persValue = ((IntToken) persistence.getToken()).intValue();

            // NOTE: We assume the superclass ensures this cast is safe.
            ((Plot) plot).setPointsPersistence(persValue);
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Configure the plotter using the current parameter values.
     *  @exception IllegalActionException If the parent class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        int persValue = ((IntToken) persistence.getToken()).intValue();

        // NOTE: We assume the superclass ensures this cast is safe.
        ((Plot) plot).setPointsPersistence(persValue);
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
     *  or if the base class throws it.
     *  @return True if it is OK to continue.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        boolean result = super.postfire();
        plot.repaint();
        Thread.yield();
        return result;
    }
}
