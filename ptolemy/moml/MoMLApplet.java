/* Basic applet that constructs a Ptolemy II model from a MoML file.

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
@AcceptedRating Red (eal@eecs.berkeley.edu)

*/

package ptolemy.moml;

import java.net.URL;
import java.awt.Color;

import com.microstar.xml.XmlException;

import ptolemy.moml.*;
import ptolemy.gui.*;
import ptolemy.actor.gui.PtolemyApplet;

//////////////////////////////////////////////////////////////////////////
//// MoMLApplet
/**
Basic applet that constructs a Ptolemy II model from a MoML file.
The MoML file is given as a URL via the applet parameter <i>model</i>.

@author  Edward A. Lee
@version $Id$
@see ptolemy.gui.MoML
*/
public class MoMLApplet extends PtolemyApplet {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return applet information. If the top-level model element
     *  contains a <i>doc</i> element, then the contents of that element
     *  is included in the applet information.
     *  @return A string giving information about the applet.
     */
    public String getAppletInfo() {
        if (_toplevel != null) {
            DocAttribute doc = (DocAttribute)_toplevel.
                   getAttribute(DocAttribute.DOC_ATTRIBUTE_NAME);
            if (doc != null) {
                return "MoML applet for Ptolemy II:\n" + doc.toString();
            }
        }
        return "MoML applet for Ptolemy II.\n" +
        "Ptolemy II comes from UC Berkeley, Department of EECS.\n" +
        "See http://ptolemy.eecs.berkeley.edu/ptolemyII";
    }

    /** Describe the applet parameters.
     *  @return An array describing the applet parameters.
     */
    public String[][] getParameterInfo() {
        String newinfo[][] = {
            {"model", "", "URL for the MoML file"}
        };
        return _concatStringArrays(super.getParameterInfo(), newinfo);
    }

    /** Create a MoML parser and parse a file.
     */
    public void init() {

        // Process the background parameter.
        _background = Color.white;
        try {
            String colorSpecification = getParameter("background");
            if (colorSpecification != null) {
                _background = Color.decode(colorSpecification);
            }
        } catch (Exception ex) {
            report("Warning: background parameter failed: ", ex);
        }
        setBackground(_background);

        try {
            String modelURL = getParameter("model");
            if (modelURL == null) {
                throw new Exception(
                        "MoML applet does not not specify a model parameter!");
            }
            MoMLParser parser = new MoMLParser();
            URL docBase = getDocumentBase();
            URL xmlFile = new URL(docBase, modelURL);
            _toplevel = parser.parse(docBase, xmlFile.openStream());
            _manager = _toplevel.getManager();
            _manager.addExecutionListener(this);
            _workspace = _toplevel.workspace();
        } catch (Exception ex) {
            if (ex instanceof XmlException) {
                XmlException xmlEx = (XmlException)ex;
                // FIXME: Unfortunately, the line and column information
                // reported by the microstar parser are wrong, so the
                // following information is misleading at best.
                report("MoML exception on line " + xmlEx.getLine()
                + ", column " + xmlEx.getColumn()
                + ", of file:\n" + xmlEx.getSystemId(), ex);
            } else {
                report("MoML applet failed:\n", ex);
            }
            _setupOK = false;
        }
    }
}
