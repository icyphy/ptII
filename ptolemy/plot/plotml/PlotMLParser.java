/* A parser for PlotML (Plot Markup Language) supporting Plot commands.

 Copyright (c) 1998-1999 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

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

package ptolemy.plot.plotml;

// Ptolemy imports.
import ptolemy.plot.*;

// Java imports.
// FIXME: Trim this.
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Hashtable;
import java.util.Stack;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.InstantiationException;
import java.lang.IllegalAccessException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.net.URL;

// XML imports.
import com.microstar.xml.*;


//////////////////////////////////////////////////////////////////////////
//// PlotMLParser
/**
This class constructs a plot from specifications
in PlotML (Plot Markup Language), which is an XML language.
This class supports extends the base class to
support the subset that applies to the Plot class.
It ignores unrecognized elements in the DTD.
The class contains an instance of the Microstar &AElig;lfred XML
parser and implements callback methods to interpret the parsed XML.
The way to use this class is to contruct it with a reference to
a Plot object and then call its parse() method.

@author Edward A. Lee
@version $Id$
*/
public class PlotMLParser extends PlotBoxMLParser {

    /** Contruct an parser to parse commands for the specified plot object.
     *  @param plot The plot object to which to apply the commands.
     */
    public PlotMLParser(Plot plot) {
        super(plot);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** End an element. For most elements this method
     *  calls the appropriate PlotBox method.
     *  &AElig;lfred will call this method at the end of each element
     *  (including EMPTY elements).
     *  @param elementName The element type name.
     */
    public void endElement(String elementName) throws Exception {
        super.endElement(elementName);
        // FIXME -- is this method needed?
    }

    /** Start a document.  This method is called just before the parser
     *  attempts to read the first entity (the root of the document).
     *  It is guaranteed that this will be the first method called.
     */
    public void startDocument() {
        super.startDocument();
        _currentDataset = -1;
    }

    /** Start an element.
     *  This is called at the beginning of each XML
     *  element.  By the time it is called, all of the attributes
     *  for the element will already have been reported using the
     *  attribute() method.  Unrecognized elements are ignored.
     *  @param elementName The element type name.
     *  @exception XmlException If the element produces an error
     *   in constructing the model.
     */
    public void startElement(String elementName) throws XmlException {
        try {
            // NOTE: The elements are alphabetical below...
            if (elementName.equals("dataset")) {
                String name = (String)_attributes.get("name");
                _currentDataset++;
                if (name != null) {
                    ((Plot)_plot).addLegend(_currentDataset, name);
                }

            } else if (elementName.equals("m")) {
                _addPoint(false, elementName);

            } else if (elementName.equals("move")) {
                _addPoint(false, elementName);

            } else if (elementName.equals("p")) {
                _addPoint(true, elementName);

            } else if (elementName.equals("point")) {
                _addPoint(true, elementName);

            } else {
                super.startElement(elementName);
            }
        } catch (Exception ex) {
            if (ex instanceof XmlException) {
                throw (XmlException)ex;
            } else {
                String msg = "XML element \"" + elementName
                        + "\" triggers exception:\n  " + ex.toString();
                throw new XmlException(msg,
                        _currentExternalEntity(),
                        _parser.getLineNumber(),
                        _parser.getColumnNumber());
            }
        }
        // NOTE: if super is called, this gets done twice.
        // Any way to avoid it?
        _attributes.clear();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected members                 ////

    /** The current dataset number in a "dataset" element. */
    protected int _currentDataset = -1;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Add a point based on the current _attributes.
    // If the first argument is true, connect it to the previous point.
    // The second argument is the element name, used for error reporting.
    private void _addPoint(boolean connected, String element) throws Exception {
        String xSpec = (String)_attributes.get("x");
        _checkForNull(xSpec, "No x value for element \"" + element + "\"");
        // NOTE: Do not use parseDouble() to maintain Java 1.1 compatibility.
        double x = (Double.valueOf(xSpec)).doubleValue();

        String ySpec = (String)_attributes.get("y");
        _checkForNull(ySpec, "No y value for element \"" + element + "\"");
        // NOTE: Do not use parseDouble() to maintain Java 1.1 compatibility.
        double y = (Double.valueOf(ySpec)).doubleValue();

        String lowSpec = (String)_attributes.get("lowErrorBar");
        String highSpec = (String)_attributes.get("highErrorBar");
        if (lowSpec == null && highSpec == null) {
            ((Plot)_plot).addPoint(_currentDataset, x, y, connected);
        } else {
            double low, high;
            if (lowSpec != null) {
                low = (Double.valueOf(lowSpec)).doubleValue();
            } else {
                low = x;
            }
            if (highSpec != null) {
                high = (Double.valueOf(highSpec)).doubleValue();
            } else {
                high = x;
            }
            ((Plot)_plot).addPointWithErrorBars(_currentDataset, x, y,
                    low, high, connected);
        }
    }
}
