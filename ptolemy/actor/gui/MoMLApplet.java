/* Basic applet that constructs a Ptolemy II model from a MoML file.

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
package ptolemy.actor.gui;

import java.net.URL;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.attributes.VersionAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.Documentation;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;
import ptolemy.moml.filter.RemoveGraphicalClasses;

///////////////////////////////////////////////////////////////////
//// MoMLApplet

/**
 This is an applet that constructs a Ptolemy II model from a MoML file.
 "MoML" stands for "Modeling Markup Language." It is an XML schema for
 constructing Ptolemy II models.
 <p>
 This class offers a number of alternatives that control the visual
 appearance of the applet. By default, the applet places on the screen
 a set of control buttons that can be used to start, stop, pause, and
 resume the model.  Below those buttons, it places the visual elements
 of any actors in the model that implement the Placeable interface,
 such as plotters or textual output.
 <p>
 The applet parameters are:
 <ul>
 <li>
 <i>background</i>: The background color, typically given as a hex
 number of the form "#<i>rrggbb</i>" where <i>rr</i> gives the red
 component, <i>gg</i> gives the green component, and <i>bb</i> gives
 the blue component.
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
 <p>
 To create a model in a different way, say without a <i>modelClass</i>
 applet parameter, you may extend this class and override the
 protected method _createModel().  If you wish to alter the way
 that the model is represented on the screen, you can extend this
 class an override the _createView() method.  The rendition in this class
 is an instance of ModelPane.

 @author  Edward A. Lee, Christopher Hylands
 @version $Id$
 @since Ptolemy II 0.4
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
@SuppressWarnings("serial")
public class MoMLApplet extends PtolemyApplet {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return applet information. If the top-level model element
     *  contains a <i>doc</i> element, then the contents of that element
     *  is included in the applet information.
     *  @return A string giving information about the applet.
     */
    @Override
    public String getAppletInfo() {
        // Include the release and build number to aid in user support.
        String version = "Ptolemy II " + VersionAttribute.CURRENT_VERSION;
        String build = "\n(Build: $Id$)";

        if (_toplevel != null) {
            String tip = Documentation.consolidate(_toplevel);

            if (tip != null) {
                return version + " model given in MoML:\n" + tip + build;
            } else {
                return version + " model given in MoML." + build;
            }
        }

        return "MoML applet for " + version
                + "\nPtolemy II comes from UC Berkeley, Department of EECS.\n"
                + "See http://ptolemy.eecs.berkeley.edu/ptolemyII" + build;
    }

    /** Describe the applet parameters.
     *  @return An array describing the applet parameters.
     */
    @Override
    public String[][] getParameterInfo() {
        String[][] newInfo = { { "modelURL", "", "URL for the MoML file" }, };
        return _concatStringArrays(super.getParameterInfo(), newInfo);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Read the model from the <i>modelURL</i> applet parameter
     *  and filter out any graphical classes that might require us
     *  to have diva.jar in the classpath.
     *  @param workspace The workspace in which to create the model.
     *  @return A model.
     *  @exception Exception If something goes wrong.
     */
    @Override
    protected NamedObj _createModel(Workspace workspace) throws Exception {
        // Filter out graphical classes.
        return _createModel(workspace, true);
    }

    /** Read the model from the <i>modelURL</i> applet parameter.
     *  @param workspace The workspace in which to create the model.
     *  @param filterGraphicalClasses  If true, then filter out graphical
     *  classes that might require diva.jar to be in the classpath
     *  @return A model.
     *  @exception Exception If something goes wrong.
     */
    protected NamedObj _createModel(Workspace workspace,
            boolean filterGraphicalClasses) throws Exception {
        // ptolemy.vergil.MoMLViewerApplet() calls this with
        // filterGraphicalClasses set to false.
        _modelURL = _readModelURLParameter();

        MoMLParser parser = new MoMLParser();

        // FIXME: if we call _createModel twice, then we will add
        // this filter twice.  We reset the filter list here,
        // though we will lose any other filters.
        MoMLParser.setMoMLFilters(null);

        MoMLParser.setMoMLFilters(BackwardCompatibility.allFilters());

        RemoveGraphicalClasses removeNonAppletClasses = new RemoveGraphicalClasses();
        // If filterGraphicalClasses is true, then we filter out
        // graphical classes so that we do not require diva.jar.  If
        // it is false, then we clear the filter.  In both cases we
        // add some classes that are always filtered.
        if (!filterGraphicalClasses) {
            RemoveGraphicalClasses.clear();
        }

        // Exclude the code generator
        removeNonAppletClasses.put(
                "ptolemy.codegen.kernel.StaticSchedulingCodeGenerator", null);
        removeNonAppletClasses
        .put("ptolemy.vergil.kernel.attributes.DocumentationAttribute",
                null);
        MoMLParser.addMoMLFilter(removeNonAppletClasses);

        URL docBase = getDocumentBase();
        URL xmlFile = new URL(docBase, _modelURL);
        _manager = null;

        NamedObj toplevel = parser.parse(docBase, xmlFile);
        _workspace = toplevel.workspace();

        if (_fragment != null && !_fragment.trim().equals("")) {
            // A fragment was specified, so we should look inside.
            ComponentEntity inside = null;

            if (toplevel instanceof CompositeEntity) {
                inside = ((CompositeEntity) toplevel).getEntity(_fragment);
            }

            if (inside == null) {
                throw new IllegalActionException(toplevel,
                        "No such contained entity: " + _fragment);
            }

            toplevel = inside;
        } else if (toplevel instanceof CompositeActor) {
            CompositeActor result = (CompositeActor) toplevel;
            _manager = result.getManager();

            if (_manager == null) {
                _manager = new Manager(_workspace, "manager");
                result.setManager(_manager);
            }

            _manager.addExecutionListener(this);
        }

        return toplevel;
    }

    /** Read the modelURL applet parameter.
     *  If the modelURL applet parameter does not exist, then
     *  read the model applet parameter.  As a side effect,
     *  the _fragment field is set with any text after a "#".
     *  @exception Exception Thrown if there is no modelURL or model
     *  applet parameter.
     *  @return the value of the modelURL or model parameter.
     */
    protected String _readModelURLParameter() throws Exception {
        _modelURL = getParameter("modelURL");

        if (_modelURL == null) {
            // For backward compatibility, try name "model".
            _modelURL = getParameter("model");

            if (_modelURL == null) {
                throw new Exception("Applet does not not specify a modelURL.");
            }
        }

        // NOTE: Regrettably, Java's URL class is too dumb
        // to handle a fragment part of a URL.  Thus, if
        // there is one, we have to remove it.  Note that
        // Java calls this a "fragment", a "ref", and
        // and "reference", all in different parts of the
        // docs.
        int sharp = _modelURL.indexOf("#");

        if (sharp > 0) {
            _fragment = _modelURL.substring(sharp + 1);
            _modelURL = _modelURL.substring(0, sharp);
        }

        return _modelURL;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected fields                  ////

    /** The fragment of the modelURL, if any.  This field is set after
     *  _readMoMLAppletParameter() is called.
     */
    protected String _fragment = "";

    /** The modelURL.  This field is set after
     * _readMoMLAppletParameter() is called.
     */
    protected String _modelURL = "";
}
