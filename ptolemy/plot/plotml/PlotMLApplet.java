/* Plotter applet that is capable of reading PlotML files.

@Author: Edward A. Lee

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
@ProposedRating red (eal@eecs.berkeley.edu)
@AcceptedRating red (cxh@eecs.berkeley.edu)
*/
package ptolemy.plot.plotml;

import ptolemy.plot.PlotApplet;
import ptolemy.plot.Plot;

import com.microstar.xml.XmlException;

import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;

//////////////////////////////////////////////////////////////////////////
//// PlotMLApplet

/** An Applet that can plot data in PlotML format from a URL.
 *  The URL should be specified using the dataurl applet parameter.
 *
 *  @author Edward A. Lee
 *  @version $Id$
 *  @see ptolemy.plot.PlotBox
 *  @see ptolemy.plot.Plot
 */
public class PlotMLApplet extends PlotApplet {

    /** Return a string describing this applet.
     */
    public String getAppletInfo() {
        return "PlotMLApplet 2.0: A data plotter.\n" +
            "By: Edward A. Lee, eal@eecs.berkeley.edu and\n " +
            "Christopher Hylands, cxh@eecs.berkeley.edu\n" +
            "($Id$)";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Read the specified stream.  This method checks to see whether
     *  the data is PlotML data, and if so, creates a parser to read it.
     *  If not, it defers to the parent class to read it.
     *  @param in The input stream.
     *  @exception IOException If the stream cannot be read.
     */
    protected void _read(InputStream in) throws IOException {
        // Create a buffered input stream so that mark and reset
        // are supported.
        BufferedInputStream bin = new BufferedInputStream(in);
        // Peek at the file...
        bin.mark(9);
        // Read 8 bytes in case 16-bit encoding is being used.
        byte[] peek = new byte[8];
        bin.read(peek);
        bin.reset();
        if ((new String(peek)).startsWith("<?xm")) {
            // file is an XML file.
            PlotMLParser parser = _newParser();
            try {
                URL docBase = getDocumentBase();
                parser.parse(docBase, bin);
            } catch (Exception ex) {
                String msg;
                if (ex instanceof XmlException) {
                    XmlException xmlex = (XmlException)ex;
                    msg =
                        "PlotMLApplet: failed to parse PlotML data:\n"
                        + "line: " + xmlex.getLine()
                        + ", column: " + xmlex.getColumn()
                        + "\nIn entity: " + xmlex.getSystemId()
                        + "\n";
                } else {
                    msg = "PlotMLApplet: failed to parse PlotML data:\n";
                }
                System.err.println(msg + ex.toString());
                ex.printStackTrace();
            }
        } else {
            super._read(bin);
        }
    }

    /** Create a new parser object for the applet.  Derived classes can
     *  redefine this method to return a different type of parser.
     */
    protected PlotMLParser _newParser() {
        return new PlotMLParser((Plot)plot());
    }
}
