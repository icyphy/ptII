/* Appletable Plotter

@Author: Edward A. Lee and Christopher Hylands

@Version: $Id$

@Copyright (c) 1997-1998 The Regents of the University of California.
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
@ProposedRating Red
@AcceptedRating Red
*/
package ptolemy.plot;

import java.applet.Applet;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.awt.*;
import java.net.*;              // Need URL

//////////////////////////////////////////////////////////////////////////
//// PlotApplet

/** An Applet that can plot data from a URL.
 *  The URL should be specified using the dataurl applet parameter.
 *  Normally, the formatting commands are included in the file with the
 *  the data, but it is also possible to include them in the
 *  pxgraphargs applet parameter.  That parameter contains
 *  command-line style arguments compatible with the older pxgraph program.
 *  See the documentation for the Pxgraph class for the format of
 *  these arguments.  See the documentation for the PlotBox and Plot
 *  classes for the file format for the URL.
 *
 *  @author Edward A. Lee, Christopher Hylands
 *  @version $Id$
 *  @see PlotBox
 *  @see Plot
 *  @see Pxgraph
 */
public class PlotApplet extends Applet {

    /** Return a string describing this applet.
     */
    public String getAppletInfo() {
        return "PlotApplet 2.0: A data plotter.\n" +
            "By: Edward A. Lee, eal@eecs.berkeley.edu and\n " +
            "Christopher Hylands, cxh@eecs.berkeley.edu\n" +
            "($Id$)";
    }

    /** Return information about parameters.
     */
    public String[][] getParameterInfo () {
        String pinfo[][] = {
            {"background", "hexcolor value", "background color"},
            {"foreground", "hexcolor value", "foreground color"},
            {"dataurl",   "url",     "the URL of the data to plot"},
            {"pxgraphargs",   "args",
             "pxgraph style command line arguments"}
        };
        return pinfo;
    }

    /** Initialize the applet.  Read the applet parameters.
     */
    public void init() {
        super.init();
        setLayout(new BorderLayout());

        if (_myPlot == null) {
            _myPlot = newPlot();
        }
        add("Center", plot());

        // Process the width and height applet parameters
        int width, height;
        String widthspec = getParameter("width");
        if (widthspec != null) width = Integer.parseInt(widthspec);
        else width = 400;

        String heightspec = getParameter("height");
        if (heightspec != null) height = Integer.parseInt(heightspec);
        else height = 400;

        plot().setSize(width, height);
        plot().setButtons(true);

        // Process the background parameter.
        Color background = Color.white;
        String colorspec = getParameter("background");
        if (colorspec != null) background = PlotBox.getColorByName(colorspec);
        setBackground(background);
        plot().setBackground(background);

        // Process the foreground parameter.
        Color foreground = Color.black;
        colorspec = getParameter("foreground");
        if (colorspec != null) foreground = PlotBox.getColorByName(colorspec);
        setForeground(foreground);
        plot().setForeground(foreground);
        plot().setVisible(true);

        // Process the pxgraphargs parameter.
        String pxgraphargs = null;
        pxgraphargs = getParameter("pxgraphargs");
        if (pxgraphargs != null) {
            try {
                // Since there may be filenames specified here, we use the
                // set the document base.  Note that we prefer that files and
                // URLs be specified using the dataurl parameter.
                showStatus("Reading arguments");
                plot()._documentBase = getDocumentBase();
                plot().parsePxgraphargs(pxgraphargs);
                showStatus("Done");
            } catch (CmdLineArgException e) {
                System.err.println("PlotApplet: failed to parse `"+pxgraphargs+
                        "': " +e);
            } catch (FileNotFoundException e) {
                System.err.println("PlotApplet: file not found: " +e);
            } catch (IOException e) {
                System.err.println("PlotApplet: error reading input file: " +e);
            }
        }

        // Process the dataurl parameter.
        String dataurlspec = getParameter("dataurl");
        if (dataurlspec != null) {
            try {
                showStatus("Reading data");
                URL dataurl = new URL(getDocumentBase(), dataurlspec);
                plot().read(dataurl.openStream());
                showStatus("Done");
            } catch (MalformedURLException e) {
                System.err.println(e.toString());
            } catch (FileNotFoundException e) {
                System.err.println("PlotApplet: file not found: " +e);
            } catch (IOException e) {
                System.err.println("PlotApplet: error reading input file: " +e);
            }
        }
    }

    /** Create a new Plot object for the applet.  Derived classes can
     *  redefine this method to return a different type of plot object.
     */
    public Plot newPlot() {
        return new Plot();
    }

    /** Return the Plot object to operate on.
     */
    public Plot plot() {
        return _myPlot;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    // The Plot component we are running.
    private transient Plot _myPlot;
}
