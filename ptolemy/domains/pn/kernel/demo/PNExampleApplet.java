/* Applet version of PNExample

@Author: Christopher Hylands, Mudit Goel

@Version: $Id$

@Copyright (c) 1997 The Regents of the University of California.
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
*/
package ptolemy.domains.pn.kernel.demo;

import ptolemy.kernel.*;
import ptolemy.domains.pn.kernel.*;
import java.applet.Applet;
import java.awt.*;

//////////////////////////////////////////////////////////////////////////
//// PNExampleApplet
/* Applet version of PNExample
 * @author Christopher Hylands, Mudit Goel
 * @version $Id$
 */
public class PNExampleApplet extends Applet {
    

    /** Return a string describing this applet.
     */
    public String getAppletInfo() {
        return "PNExampleApplet.\n" +
            "By: Christopher Hylands, cxh@eecs.berkeley.edu and\n" +
            "Mudit Goel, mudit@eecs.berkeley.edu\n" +
            "($Id$)";
    }

    /** Return information about parameters. */
    public String[][] getParameterInfo () {
        String pinfo[][] = {
            {"pnmode", "integer: 0,1,2", "PN mode"},
            {"pncount", "integer greater than 0", "When to stop"},
        };
        return pinfo;
    }

    /** For use as an application.  Use the following command to run:
     * java -classpath $PTOLEMY/tycho/java:/optolemy/jdk1.1.4/lib/classes.zip ptolemy.domains.pn.kernel.demo.PNExampleApplet 0 1
     */
    public static void main(String args[])
    {
        int pnmode = Integer.parseInt(args[0]);
        int pncount = Integer.parseInt(args[1]);
        PNExampleApplet myapplet = new PNExampleApplet() ;
        myapplet._run(pnmode, pncount);
	return;
    }

    /**
     * Initialize the applet.  Read the applet parameters.
     */
    public void init() {
        if (_debug > 8) System.out.println("PNExampleApplet: init");
        int width, height;
        setLayout(new BorderLayout());

        // Process the width and height applet parameters
        try {
            width = Integer.valueOf(getParameter("width")).intValue();
        } catch (NullPointerException e) {
            width = 400;
        }
        try {
            height = Integer.valueOf(getParameter("height")).intValue();
        } catch (NullPointerException e) {
            height = 400;
        }
        if (_debug > 8)
            System.out.println("PNExampleApplet: init: about to resize"+width);
        resize(width,height);

        try {
            _pnmode = Integer.valueOf(getParameter("pnmode")).intValue();
        } catch (NullPointerException e) {
        } catch (NumberFormatException e) {
        }

        try {
            _pncount = Integer.valueOf(getParameter("pncount")).intValue();
        } catch (NullPointerException e) {
        } catch (NumberFormatException e) {
        }

        super.init();
    }

    public void run () {
        if (_debug > 8) System.out.println("PNExampleApplet: run");
	repaint();
    }

    /** Start the run.
     */
    public void start () {
        if (_debug > 8) System.out.println("PNExampleApplet: start");
        _run(_pnmode, _pncount);
    }

    /** Stop the run.
     */
    public void stop () {
        if (_debug > 8) System.out.println("PNExampleApplet: stop");
    }


    //////////////////////////////////////////////////////////////////////////
    ////                         protected variables                      ////

    // If non-zero, print out debugging messages.
    protected int _debug = 10;

    //////////////////////////////////////////////////////////////////////////
    ////                         private methods                          ////

    /** Run the universe in pnmode for pncount. */
    private void _run(int pnmode, int pncount) {
        try {
            if (_debug > 9)
                System.out.println("PNExampleApplet: _run before new");
            PNUniverse myUniverse = new PNUniverse(pnmode, pncount);
            if (_debug > 9)
                System.out.println("PNExampleApplet: _run before execute");
            myUniverse.execute();
            System.out.println("Bye World\n");
        } catch (GraphException e) {
            System.out.println("PNExampleApplet: _run: GraphException: " +
                    e.getMessage());
        } //catch (NameDuplicationException e) {
        //  System.out.println("PNExampleApplet: _run: " +
        //                    "NameDuplicationException: " +
        //                    e.getMessage());
        //        }
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    private int _pnmode = 0;         // Mode of PN Buffer Algorithm.
    private int _pncount = 1;        // Number of times to run.
}
