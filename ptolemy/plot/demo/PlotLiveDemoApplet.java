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
@ProposedRating red (eal@eecs.berkeley.edu)
@AcceptedRating red (cxh@eecs.berkeley.edu)
*/

package ptolemy.plot.demo;

import ptolemy.plot.*;

//////////////////////////////////////////////////////////////////////////
//// PlotLiveDemoApplet

/** An Applet that demonstrates the PlotLiveDemo class.
 *
 *  @author Edward A. Lee, Christopher Hylands
 *  @version $Id$
 */
public class PlotLiveDemoApplet extends PlotLiveApplet {

    /** Return a string describing this applet.
     *  @return A description of the applet.
     */
    public String getAppletInfo() {
        return "PlotLiveDemoApplet 1.2: Demo of PlotLive.\n" +
            "By: Edward A. Lee, eal@eecs.berkeley.edu\n" +
            "    Christopher Hylands, @eecs.berkeley.edu\n" +
            "($Id$)";
    }

    /** Create a new Plot object to operate on.
     */
    public PlotBox newPlot() {
        return new PlotLiveDemo();
    }
}
