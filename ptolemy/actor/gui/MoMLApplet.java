/* Basic applet that constructs a Ptolemy II model from a MoML file.

 Copyright (c) 1998-2000 The Regents of the University of California.
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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (eal@eecs.berkeley.edu)

*/

package ptolemy.actor.gui;

// Java imports.
import java.net.URL;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Enumeration;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

// XML imports
import com.microstar.xml.XmlException;

// Ptolemy imports
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Configurable;
import ptolemy.actor.IOPort;
import ptolemy.actor.Manager;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.gui.*;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.Documentation;
import ptolemy.moml.MoMLParser;
//////////////////////////////////////////////////////////////////////////
//// MoMLApplet
/**
This is an applet that constructs a Ptolemy II model from a MoML file.
"MoML" stands for "Modeling Markup Language." It is an XML language for
constructing Ptolemy II models.
The applet parameters are:
<ul>
<li><i>background</i>: The background color, typically given as a hex
number of the form "#<i>rrggbb</i>" where <i>rr</i> gives the red
component, <i>gg</i> gives the green component, and <i>bb</i> gives
the blue component.
<li><i>model</i>: The name of a URI (or URL) containing the
MoML file that defines the model.
<li><i>runControls</i>: The number of run controls to put on the screen.
The value must be an integer.
If the value is greater than zero, then a "Go" button
created.  If the value is greater than one, then a "Stop" button
is also created.
</ul>
Any entity that is created in parsing the MoML file that implements
the Placeable interface is placed in the applet.  Thus, entities
with visual displays automatically have their visual displays
appearing in the applet.
<p>
If the top-level object in the MoML file is an instance of
TypedCompositeActor, then the _toplevel protected member is set
to refer to it, and an instance of Manager is created for it.
Otherwise, the _toplevel member will be null.

@author  Edward A. Lee
@version $Id$
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
            String tip = Documentation.consolidate(_toplevel);
            if (tip != null) {
                return "Ptolemy II model given in MoML:\n" + tip;
            } else {
                return "Ptolemy II model given in MoML.";
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
            {"model", "", "URL for the MoML file"},
        };
        return _concatStringArrays(super.getParameterInfo(), newinfo);
    }

    /** Create a MoML parser and parse a file.
     */
    public void init() {

        // Do not call super.init() because it creates a toplevel
        // manager.  Since we don't call it, we have to process the
        // background parameter.

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
        getContentPane().setBackground(_background);
        setBackground(_background);
        try {
            String modelURL = getParameter("model");
            if (modelURL == null) {
                throw new Exception(
                        "MoML applet does not not specify a model parameter!");
            }
            // Create a panel to place placeable objects.
            JPanel displayPanel = new JPanel();
            displayPanel.setLayout(new BoxLayout(displayPanel,
                    BoxLayout.Y_AXIS));
            displayPanel.setBackground(_background);

            // Specify that all Placeable entities be placed in the applet.
            MoMLParser parser = new MoMLParser(null, displayPanel);
            URL docBase = getDocumentBase();
            URL xmlFile = new URL(docBase, modelURL);
            _toplevel = null;
            _manager = null;
            NamedObj toplevel = parser.parse(docBase, xmlFile);
            _workspace = toplevel.workspace();
            if (toplevel instanceof TypedCompositeActor) {
                _toplevel = (TypedCompositeActor)toplevel;
                _manager = new Manager(_workspace, "manager");
                _toplevel.setManager(_manager);
                _manager.addExecutionListener(this);
                ModelPane pane = new ModelPane(_toplevel);
                pane.setDisplayPane(displayPanel);
                getContentPane().add(pane);
                pane.setDefaultButton();
                pane.setBackground(_background);
            }
        } catch (Exception ex) {
            if (ex instanceof XmlException) {
                XmlException xmlEx = (XmlException)ex;
                // FIXME: The file reported below is wrong... Why?
                report("MoML exception on line " + xmlEx.getLine()
                        + ", column " + xmlEx.getColumn() + ", in entity:\n"
                        + xmlEx.getSystemId(), ex);
            } else {
                report("MoML applet failed:\n", ex);
            }
            _setupOK = false;
        }
    }
}
