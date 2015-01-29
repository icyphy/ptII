/* Applet that displays a vergil block diagram.

 Copyright (c) 1998-2014 The Regents of the University of California.
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

 */
package ptolemy.vergil;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.net.URL;
import java.util.Locale;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.ConfigurationApplication;
import ptolemy.actor.gui.MoMLApplet;
import ptolemy.actor.gui.SizeAttribute;
import ptolemy.domains.modal.kernel.FSMActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.vergil.actor.ActorGraphModel;
import ptolemy.vergil.actor.ActorViewerGraphController;
import ptolemy.vergil.modal.FSMGraphController;
import ptolemy.vergil.modal.FSMGraphModel;
import diva.graph.GraphModel;
import diva.graph.GraphPane;
import diva.graph.JGraph;

///////////////////////////////////////////////////////////////////
//// MoMLViewerApplet

/**
 This applet displays a graph view of a specified MoML file.
 <p>
 The applet parameters are:
 <ul>
 <li>
 <i>background</i>: The background color, typically given as a hex
 number of the form "#<i>rrggbb</i>" where <i>rr</i> gives the red
 component, <i>gg</i> gives the green component, and <i>bb</i> gives
 the blue component.
 <li>
 <i>configuration</i>:
 The relative pathname to a Ptolemy II xml configuration file.
 for example, if the configuration applet parameter is set to
 "ptolemy/configs/full/configuration.xml", then the user will
 have the full vergil interface, including look inside capability.
 (<b>Experimental</b>).
 <li>
 <i>controls</i>:
 This gives a comma-separated list
 of any subset of the words "buttons", "topParameters", and
 "directorParameters" (case insensitive), or the word "none".
 If this parameter is not given, then it is equivalent to
 giving "buttons", and only the control buttons mentioned above
 will be displayed.  If the parameter is given, and its value is "none",
 then no controls are placed on the screen.  If the word "topParameters"
 is included in the comma-separated list, then controls for the
 top-level parameters of the model are placed on the screen, below
 the buttons.  If the word "directorParameters" is included,
 then controls for the director parameters are also included.
 This parameter is ignored unless <i>includeRunPanel</i> is given as "true".
 <li>
 <i>includeRunPanel</i>: An indicator to include a run panel below the
 schematic.  The value must be "true" or no run panel will be displayed.
 <li>
 <i>modelURL</i>: The name of a URI (or URL) containing the
 MoML file that defines the model.
 <li>
 <i>orientation</i>: This can have value "horizontal", "vertical", or
 "controls_only" (case insensitive).  If it is "vertical", then the
 controls are placed above the visual elements of the Placeable actors.
 This is the default.  If it is "horizontal", then the controls
 are placed to the left of the visual elements.  If it is "controls_only"
 then no visual elements are placed.
 </ul>

 @author  Steve Neuendorffer and Edward A. Lee
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (eal)
 */
@SuppressWarnings("serial")
public class MoMLViewerApplet extends MoMLApplet {
    // FIXME: this is a total hack as a placeholder for a general
    // implementation going through configurations.
    // FIXME: Perhaps the context menu
    // should have a run-model option?
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Describe the applet parameters.
     *  @return An array describing the applet parameters.
     */
    @Override
    public String[][] getParameterInfo() {
        String[][] newinfo = {
                { "includeRunPanel", "", "Indicator to include run panel" },
                { "configuration", "", "Ptolemy II configuration" }, };
        return _concatStringArrays(super.getParameterInfo(), newinfo);
    }

    /** Override the base class to not start
     *  execution of the model. This method is called by the
     *  browser or applet viewer to inform this applet that it should
     *  start its execution. It is called after the init method
     *  and each time the applet is revisited in a Web page.
     */
    @Override
    public void start() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Read the model from the <i>modelURL</i> applet parameter
     *  and do not filter out any graphical classes.
     *  @param workspace The workspace in which to create the model.
     *  @return A model.
     *  @exception Exception If something goes wrong.
     */
    @Override
    protected NamedObj _createModel(Workspace workspace) throws Exception {
        // Do not filter out graphical classes.
        // FIXME: if we have a configuration, then we are parsing
        // the model twice!!!
        return _createModel(workspace, false);
    }

    /** Override the base class to create a schematic view instead of
     *  a ModelPane.  If the toplevel model created by _createModel()
     *  is not an instance of CompositeEntity, then do nothing.
     */
    @Override
    protected void _createView() {
        if (!(_toplevel instanceof CompositeEntity)) {
            return;
        }

        // Get the configuration, if any
        String configurationPath = getParameter("configuration");

        if (configurationPath != null) {
            try {
                URL specificationURL = ConfigurationApplication
                        .specToURL(configurationPath);
                _configuration = ConfigurationApplication
                        .readConfiguration(specificationURL);
                report("Opened '" + specificationURL + "': " + _configuration);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to open '"
                        + configurationPath + "':", ex);
            }
        }

        //        if (_configuration != null) {
        //            // We have a configuration
        //            try {
        //                //URL docBase = getDocumentBase();
        //                //URL inURL = new URL(docBase, _modelURL);
        //                //String key = inURL.toExternalForm();
        //                // Now defer to the model reader.
        //                //Tableau tableau = _configuration.openModel(inURL, inURL,
        //                //        key);
        //                //JFrame frame = new JFrame();
        //                //tableau.setFrame(frame);
        //                //getContentPane().add(tableau.getFrame().getContentPane(),
        //                //        BorderLayout.NORTH);
        //            } catch (Exception ex) {
        //                throw new RuntimeException("Failed to open '" + _modelURL
        //                        + "'.", ex);
        //            }
        //        }

        GraphPane pane = null;

        // FIXME: Temporary hack so we can view FSMs properly.
        // This should be replaced with a proper tableau mechanism.
        if (_toplevel instanceof FSMActor) {
            FSMGraphController controller = new FSMGraphController();
            FSMGraphModel graphModel = new FSMGraphModel((FSMActor) _toplevel);

            // FIXME: To get things like open documentation to work, have
            // to specify a configuration.  But currently, there isn't one.
            if (_configuration != null) {
                controller.setConfiguration(_configuration);
            }

            pane = new GraphPane(controller, graphModel);
        } else {
            // top level is not an FSM actor.
            ActorViewerGraphController controller = new ActorViewerGraphController();

            // FIXME: To get things like open documentation to work, have
            // to specify a configuration.  But currently, there isn't one.
            if (_configuration != null) {
                controller.setConfiguration(_configuration);
            }

            GraphModel model = new ActorGraphModel(_toplevel);
            pane = new GraphPane(controller, model);
        }

        JGraph modelViewer = new JGraph(pane);

        // Get dimensions from the model, if they are present.
        // Otherwise, use the same defaults used by vergil.
        boolean boundsSet = false;

        try {
            SizeAttribute vergilBounds = (SizeAttribute) _toplevel
                    .getAttribute("_vergilSize", SizeAttribute.class);
            if (vergilBounds != null) {
                boundsSet = vergilBounds.setSize(modelViewer);
            }
        } catch (Throwable throwable) {
            // Ignore and set to default.
        }

        if (!boundsSet) {
            // Set default size
            Dimension size = new Dimension(400, 300);
            modelViewer.setMinimumSize(size);
            modelViewer.setPreferredSize(size);
        }

        // Inherit the background color from the applet parameter.
        modelViewer.setBackground(getBackground());

        // Do not include a scroll pane, since generally we size the
        // applet to show the entire model.
        // JScrollPane scrollPane = new JScrollPane(modelViewer);
        // getContentPane().add(scrollPane, BorderLayout.NORTH);
        // scrollPane.setBackground(getBackground());
        getContentPane().add(modelViewer, BorderLayout.NORTH);

        // Call the superclass here to get a control panel
        // below the schematic.
        String panelFlag = getParameter("includeRunPanel");

        if (panelFlag != null
                && panelFlag.trim().toLowerCase(Locale.getDefault())
                .equals("true")) {
            // NOTE: We could create a separator between the schematic
            // and the control panel here.
            super._createView();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    /** The configuration that is read from the configuration applet param. */
    private Configuration _configuration;
}
