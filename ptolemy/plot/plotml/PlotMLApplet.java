/* Plotter applet that is capable of reading PlotML files.

@Author: Edward A. Lee

@Version: $Id$

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
@ProposedRating red (eal@eecs.berkeley.edu)
@AcceptedRating red (cxh@eecs.berkeley.edu)
*/
package ptolemy.plot.plotml;

import ptolemy.plot.PlotApplet;

// FIXME: Trim these.
import java.applet.Applet;
import java.io.IOException;
import java.io.InputStream;
import java.awt.*;
import java.net.*;              // Need URL

//////////////////////////////////////////////////////////////////////////
//// PlotMLApplet

/** An Applet that can plot data in PlotML format from a URL.
 *  The URL should be specified using the dataurl applet parameter.
 *
 *  @author Edward A. Lee
 *  @version $Id$
 *  @see PlotBox
 *  @see Plot
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
     *  <b>NOTE:</b> The current version of Netscape (4.51) does not
     *  support mark() and reset() on InputStream, so in order to make
     *  this applet work with Netscape, we do not peek at the file to
     *  see whether it is an XML file, but rather just assume that it is.
     *  @param in The input stream.
     *  @exception IOException If the stream cannot be read.
     */
    protected void _read(InputStream in) throws IOException {
        // Peek at the file...

        /* FIXME: See comment above... We cannot do this currently.
        in.mark(9);
        // Read 8 bytes in case 16-bit encoding is being used.
        byte[] peek = new byte[8];
        in.read(peek);
        in.reset();
        if ((new String(peek)).startsWith("<?xm")) {
        */
            // file is an XML file.
            PlotMLParser parser = new PlotMLParser(plot());
            try {
                URL docBase = getDocumentBase();
                parser.parse(docBase, in);
            } catch (Exception ex) {
                System.err.println(
                    "PlotMLApplet: failed to parse PlotML data:\n"
                    + ex.toString());
                ex.printStackTrace();
            }
        /* FIXME: See method comment above...
        } else {
            super._read(in);
        }
        */
    }
}
