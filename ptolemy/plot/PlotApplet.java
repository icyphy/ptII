/* Appletable Plotter

@Author: Edward A. Lee and Christopher Hylands

@Version: $Id$

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

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JApplet;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.MalformedURLException;

//////////////////////////////////////////////////////////////////////////
//// PlotApplet

/** An Applet that can plot data from a URL.
 *  The URL should be specified using the dataurl applet parameter.
 *  The formatting commands are included in the file with the
 *  the data.
 *  If no URL is given, then a sample plot is generated.
 *
 *  @author Edward A. Lee, Christopher Hylands
 *  @version $Id$
 *  @see PlotBox
 *  @see Plot
 */
public class PlotApplet extends JApplet {

    /** Return a string describing this applet.
     *  @return A string describing the applet.
     */
    public String getAppletInfo() {
        return "PlotApplet " + PlotBox.PTPLOT_RELEASE +
	    ": A data plotter.\n" +
            "By: Edward A. Lee, eal@eecs.berkeley.edu and\n " +
            "Christopher Hylands, cxh@eecs.berkeley.edu\n" +
            "($Id$)";
    }

    /** Return information about parameters.
     *  @return A array of arrays giving parameter names, the type,
     *   and the default value or description.
     */
    public String[][] getParameterInfo() {
        String pinfo[][] = {
            {"background", "hexcolor value", "background color"},
            {"foreground", "hexcolor value", "foreground color"},
            {"dataurl",   "url",     "the URL of the data to plot"},
            {"height", "integer", "100"},
            {"width", "integer", "100"},
        };
        return pinfo;
    }

    /** Initialize the applet.  Read the applet parameters.
     */
    public void init() {
        super.init();

        if (_plot == null) {
            _plot = newPlot();
        }
        getContentPane().add(plot(), BorderLayout.NORTH);

        // Process the width and height applet parameters
        int width, height;
        String widthspec = getParameter("width");
        if (widthspec != null) width = Integer.parseInt(widthspec);
        else width = 400;

        String heightspec = getParameter("height");
        if (heightspec != null) height = Integer.parseInt(heightspec);
        else height = 400;

        _setPlotSize(width, height);
        plot().setButtons(true);

        // Process the background parameter.
        Color background = Color.white;
        String colorspec = getParameter("background");
        if (colorspec != null) background = PlotBox.getColorByName(colorspec);
        setBackground(background);
        plot().setBackground(background);
        getContentPane().setBackground(background);

        // Process the foreground parameter.
        Color foreground = Color.black;
        colorspec = getParameter("foreground");
        if (colorspec != null) foreground = PlotBox.getColorByName(colorspec);
        setForeground(foreground);
        plot().setForeground(foreground);
        plot().setVisible(true);

        // Process the dataurl parameter.
        String dataurlspec = getParameter("dataurl");
        if (dataurlspec != null) {
            try {
                showStatus("Reading data");
                URL dataurl = new URL(getDocumentBase(), dataurlspec);
                InputStream in = dataurl.openStream();
                _read(in);
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
     *  @return A new instance of PlotBox.
     */
    public PlotBox newPlot() {
        return new Plot();
    }

    /** Return the plot object to operate on.
     *  @return The plot object associated with this applet.
     */
    public PlotBox plot() {
        return _plot;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Read the specified stream.  Derived classes may override this
     *  to support other file formats.
     *  @param in The input stream.
     *  @exception IOException If the stream cannot be read.
     */
    protected void _read(InputStream in) throws IOException {
        plot().read(in);
    }

    /** Given the size of the applet, set the size of the plot.
     *  Derived classes may override this to allow room for other
     *  widgets below the plot.
     *  @param appletWidth The width of the applet.
     *  @param appletHeight The height of the applet.
     */
    protected void _setPlotSize(int appletWidth, int appletHeight) {
        plot().setSize(appletWidth, appletHeight);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    // The Plot component we are running.
    private transient PlotBox _plot;
}
