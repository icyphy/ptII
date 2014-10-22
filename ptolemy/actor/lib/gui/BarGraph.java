/* Plot bar graphs, given arrays of doubles as inputs.

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

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.plot.Plot;

///////////////////////////////////////////////////////////////////
//// BarGraph

/**
 <p>A bar graph plotter.  This plotter contains an instance of the Plot
 class from the Ptolemy plot package as a public member. Data at
 the input, which can consist of any number of channels, are plotted
 on this instance.  Each input channel is plotted as a separate data set.
 Each input token is an array of doubles.</p>
 <p>
 The <i>iterationsPerUpdate</i> parameter can be used to fine tune
 the display.  It can be quite expensive to generate the display, and
 by default, this actor generates it on every firing.  If
 <i>iterationsPerUpdate</i> is set to some integer greater than
 one, then it specifies how many iterations should be executed
 between updates. Thus, if <i>iterationsPerUpdate</i> = 2, then every
 second time this actor fires, it will update the display. That is,
 it will update its display on the first firing, the third, the
 fifth, etc. It will, however, consume its inputs on every firing.
 The plot is always updated in the wrapup() method.</p>

 @author  Edward A. Lee
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class BarGraph extends ArrayPlotter {
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

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-20\" y=\"-20\" " + "width=\"40\" height=\"40\" "
                + "style=\"fill:lightGrey\"/>\n" + "<rect x=\"-12\" y=\"-12\" "
                + "width=\"24\" height=\"24\" " + "style=\"fill:white\"/>\n"
                + "<rect x=\"2\" y=\"-18\" " + "width=\"4\" height=\"4\" "
                + "style=\"fill:grey\"/>\n" + "<rect x=\"8\" y=\"-18\" "
                + "width=\"4\" height=\"4\" " + "style=\"fill:grey\"/>\n"
                + "<rect x=\"14\" y=\"-18\" " + "width=\"4\" height=\"4\" "
                + "style=\"fill:grey\"/>\n" + "<rect x=\"-8\" y=\"2\" "
                + "width=\"4\" height=\"10\" " + "style=\"fill:red\"/>\n"
                + "<rect x=\"-2\" y=\"-8\" " + "width=\"4\" height=\"20\" "
                + "style=\"fill:red\"/>\n" + "<rect x=\"4\" y=\"-5\" "
                + "width=\"4\" height=\"17\" " + "style=\"fill:red\"/>\n"
                + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the plot has not already been created, create it.
     *  If configurations specified by a call to configure() have not yet
     *  been processed, process them.  This overrides the base class to
     *  also start counting iterations, so that the
     *  <i>iterationsPerUpdate</i> parameter works.
     *  @exception IllegalActionException If the parent class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        // NOTE: We assume the superclass ensures this cast is safe.
        ((Plot) plot).setBars(true);
        ((Plot) plot).setConnected(false);
    }
}
