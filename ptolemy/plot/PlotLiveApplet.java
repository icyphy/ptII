/* A live signal plotter applet

@Author: Edward A. Lee and Christopher Hylands

@Copyright (c) 1997-2000 The Regents of the University of California.
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

                                                PT_COPYRIGHT_VERSION_2
                                                COPYRIGHTENDKEY
@ProposedRating Yellow (cxh@eecs.berkeley.edu)
@AcceptedRating Yellow (cxh@eecs.berkeley.edu)
*/
package ptolemy.plot;

//////////////////////////////////////////////////////////////////////////
//// PlotLiveApplet

/** An Applet for the PlotLive class.  Derived classes should redefine
 *  newPlot() to return a concrete instance of PlotLive.
 *
 *  @author Edward A. Lee, Christopher Hylands
 *  @version $Id$
 */
public class PlotLiveApplet extends PlotApplet {

    /** Return a string describing this applet.
     *  @return A description of the applet.
     */
    public String getAppletInfo() {
        return "PlotLiveApplet " + PlotBox.PTPLOT_RELEASE +
            " : Demo of PlotLive.\n" +
            "By: Edward A. Lee, eal@eecs.berkeley.edu\n" +
            "    Christopher Hylands, cxh@eecs.berkeley.edu\n" +
            "($Id$)";
    }

    /** Start the plot thread.
     */
    public void start() {
        ((PlotLive)plot()).start();
    }

    /** Stop the plot thread.
     */
    public void stop() {
        ((PlotLive)plot()).stop();
    }
}
