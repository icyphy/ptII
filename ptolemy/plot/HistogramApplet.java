/* HistogramApplet

@Copyright (c) 1997-1999 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.plot;

import ptolemy.plot.*;
import java.awt.*;
import java.applet.Applet;
import java.net.*;
import java.io.FileNotFoundException;
import java.io.IOException;

//////////////////////////////////////////////////////////////////////////
//// HistogramApplet
/**
 * A Histogram.  Data can be given in ASCII format at a URL.
 * If none is given, then a sample histogram is generated.
 *
 * @author Edward A. Lee
 * @version $Id$
 */
public class HistogramApplet extends Applet {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return a string describing this applet.
     */
    public String getAppletInfo() {
        return "Histogram 1.0: Demo of PlotApplet.\n" +
            "By: Edward A. Lee, eal@eecs.berkeley.edu\n " +
            "($Id$)";
    }

    /** Return information about parameters.
     */
    public String[][] getParameterInfo () {
        String pinfo[][] = {
            {"background", "hexcolor value", "background color"},
            {"foreground", "hexcolor value", "foreground color"},
            {"dataurl",   "url",     "the URL of the data to plot"},
            {"height", "integer", "100"},
            {"width", "integer", "100"},
        };
        return pinfo;
    }

    /**
     * Initialize the applet.
     */
    public void init () {
        super.init();
        Histogram histogram = new Histogram();
        add(histogram);

        // Process the width and height applet parameters
        int width, height;

        String widthspec = getParameter("width");
        if (widthspec != null) width = Integer.parseInt(widthspec);
        else width = 400;

        String heightspec = getParameter("height");
        if (heightspec != null) height = Integer.parseInt(heightspec);
        else height = 400;

        histogram.setSize(width, height);
        histogram.setButtons(true);

        // Process the background parameter.
        Color background = Color.white;
        String colorspec = getParameter("background");
        if (colorspec != null) background = PlotBox.getColorByName(colorspec);
        setBackground(background);
        histogram.setBackground(background);

        // Process the foreground parameter.
        Color foreground = Color.black;
        colorspec = getParameter("foreground");
        if (colorspec != null) foreground = PlotBox.getColorByName(colorspec);
        setForeground(foreground);
        histogram.setForeground(foreground);
        histogram.setVisible(true);

        // Process the dataurl parameter.
        String dataurlspec = getParameter("dataurl");
        boolean urlgiven = false;
        if (dataurlspec != null) {
            try {
                showStatus("Reading data");
                URL dataurl = new URL(getDocumentBase(), dataurlspec);
                histogram.read(dataurl.openStream());
                urlgiven = true;
                showStatus("Done");
            } catch (MalformedURLException e) {
                System.err.println(e.toString());
            } catch (FileNotFoundException e) {
                System.err.println("PlotApplet: file not found: " +e);
            } catch (IOException e) {
                System.err.println("PlotApplet: error reading input file: " +e);
            }
        }
        if (!urlgiven) {
            histogram.samplePlot();
            histogram.setYRange(0,100);
        }
        histogram.fillPlot();
    }
}
