/* Utility class to handle model and layout file operations.

 Copyright (c) 2011-2014 The Regents of the University of California.
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

package ptolemy.homer.kernel;

import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntMatrixToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.homer.gui.HomerMainFrame;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;
import ptserver.util.ProxyModelBuilder;
import ptserver.util.ProxyModelBuilder.ProxyModelType;
import ptserver.util.ServerUtility;

///////////////////////////////////////////////////////////////////
//// LayoutFileOperations

/** Utility class to handle model and layout file operations.
 *
 *  @author Peter Foldes
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (pdf)
 *  @Pt.AcceptedRating Red (pdf)
 */
public final class LayoutFileOperations {

    ///////////////////////////////////////////////////////////////////
    ////                constructor                                ////

    /** Hide constructor so that class is only used as a utility class.
     */
    private LayoutFileOperations() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Open a model and an existing layout on the given frame. The information from both
     *  will be merged into one model that is returned.
     *
     *  @param mainFrame The frame containing the contents infrastructure and the visual
     *  representations.
     *  @param modelURL The url to the original Ptolemy II model file to use.
     *  @param layoutURL The url to the layout file associated with the Ptolemy II model.
     *  @exception IllegalActionException If the model cannot be merged with the layout.
     *  @exception CloneNotSupportedException If cloning of the Ptolemy II model is not
     *  supported.
     *  @exception NameDuplicationException If there is a name duplication when merging the
     *  model and the layout.
     *  @return A model that has the merged information from both the model and the layout.
     */
    public static CompositeEntity open(HomerMainFrame mainFrame, URL modelURL,
            URL layoutURL) throws IllegalActionException,
            NameDuplicationException, CloneNotSupportedException {
        HashSet<Class<? extends Attribute>> classesToMerge = new HashSet<Class<? extends Attribute>>();
        classesToMerge.add(HomerLocation.class);

        HashSet<String> namedObjectsToMerge = new HashSet<String>();
        namedObjectsToMerge.add(HomerConstants.SCREEN_SIZE);
        namedObjectsToMerge.add(HomerConstants.ENABLED_NODE);
        namedObjectsToMerge.add(HomerConstants.REQUIRED_NODE);
        namedObjectsToMerge.add(HomerConstants.TAB_NODE);
        namedObjectsToMerge.add(HomerConstants.TABS_NODE);
        namedObjectsToMerge.add(HomerConstants.ORIENTATION_NODE);
        namedObjectsToMerge.add(ServerUtility.REMOTE_OBJECT_TAG);

        CompositeEntity mergedModel = ServerUtility.mergeModelWithLayout(
                modelURL, layoutURL, classesToMerge, namedObjectsToMerge);
        return mergedModel;
    }

    /** Given a frame containing a model, this method will parse the model and populate
     *  the contents defined in the frame.
     *
     *  @param mainFrame The frame containing the model to be parsed and the underlying contents
     *  infrastructure.
     *  @exception IllegalActionException If the parsing fails.
     *  @exception NameDuplicationException If during the parsing a name duplication is found.
     */
    public static void parseModel(HomerMainFrame mainFrame)
            throws IllegalActionException, NameDuplicationException {
        LayoutParser parser = new LayoutParser(mainFrame.getTopLevelActor());
        HashSet<NamedObj> proxyElements = parser.getProxyElements();
        HashSet<NamedObj> visualElements = parser.getPositionableElements();
        ArrayList<TabDefinition> tabs = parser.getTabDefinitions();

        // Add tabs to the mainframe
        for (TabDefinition tab : tabs) {
            mainFrame.addTab(tab.getTag(), tab.getName());
        }

        // Add visual elements.
        for (NamedObj object : visualElements) {
            Attribute tab = object.getAttribute(HomerConstants.TAB_NODE);
            if (tab == null || !(tab instanceof Settable)) {
                // FIXME Maybe there elements should be added to a default tab.
                throw new IllegalActionException(object,
                        "Visual object with no tab defined.");
            }
            String tag = ((Settable) tab).getExpression();
            Scene scene = mainFrame.getTabContent(tag);
            mainFrame.addVisualNamedObject(tag, new HomerWidgetElement(object,
                    scene));
        }

        // Add non-visual elements
        for (NamedObj object : proxyElements) {
            if (!visualElements.contains(object)) {
                mainFrame.addNonVisualNamedObject(object);
            }
        }
    }

    /** Open a MoML file, parse it, and return the parsed model.
     *
     *  @param url The url of the model.
     *  @return The parsed model.
     *  @exception IllegalActionException If the parsing failed.
     */
    public static CompositeEntity openModelFile(URL url)
            throws IllegalActionException {
        MoMLParser.setMoMLFilters(BackwardCompatibility.allFilters());
        CompositeEntity topLevel = null;
        MoMLParser parser = new MoMLParser(new Workspace());
        MoMLParser.purgeAllModelRecords();
        parser.resetAll();
        try {
            topLevel = (CompositeEntity) parser.parse(null, url);
        } catch (Exception e) {
            throw new IllegalActionException(null, e, "Unable to parse url: "
                    + url);
        }

        return topLevel;
    }

    /** Save a layout to a MoML file. The layout file should contain all the
     *  information needed to create visual representations of elements and
     *  to communicate remotely.
     *
     *  @param mainFrame The object containing all the information about the
     *  elements to create the layout file, such as position information.
     *  @param layoutFile The file the layout is saved to.
     */
    public static void saveAs(HomerMainFrame mainFrame, File layoutFile) {

        CompositeActor model = null;
        BufferedWriter out = null;
        try {
            // Get the original model
            model = (CompositeActor) openModelFile(mainFrame.getModelURL());

            // Get access on both workplace
            mainFrame.getTopLevelActor().workspace().getReadAccess();
            model.workspace().getWriteAccess();

            // Add remote attributes to elements
            for (NamedObj element : mainFrame.getRemoteObjectSet()) {
                // Add the proxy attributes to all objects in the stored set.
                if (!HomerMainFrame.isLabelWidget(element)) {
                    _markAsProxy(model, element);
                }
            }

            // Create layout model
            new ProxyModelBuilder(ProxyModelType.CLIENT, model).build();

            // Add the screen orientation.
            StringAttribute orientationNode = (StringAttribute) model
                    .getAttribute(HomerConstants.ORIENTATION_NODE);
            if (orientationNode != null) {
                model.removeAttribute(orientationNode);
            }

            orientationNode = new StringAttribute(model,
                    HomerConstants.ORIENTATION_NODE);
            orientationNode.setPersistent(true);
            orientationNode.setVisibility(Settable.NONE);
            orientationNode
            .setExpression(mainFrame.getOrientation().toString());

            // Add screen dimensions to top level actor.
            ArrayToken token = new ArrayToken(new IntToken[] {
                    new IntToken(mainFrame.getScreenSize().width),
                    new IntToken(mainFrame.getScreenSize().height) });

            Parameter screenSizeNode = (Parameter) model
                    .getAttribute(HomerConstants.SCREEN_SIZE);
            if (screenSizeNode != null) {
                model.removeAttribute(screenSizeNode);
            }

            screenSizeNode = new Parameter(model, HomerConstants.SCREEN_SIZE);
            screenSizeNode.setPersistent(true);
            screenSizeNode.setVisibility(Settable.NONE);
            screenSizeNode.setToken(token);

            // Clone the tabs
            Attribute tabs = (Attribute) mainFrame.getTopLevelActor()
                    .getAttribute(HomerConstants.TABS_NODE)
                    .clone(model.workspace());
            tabs.setPersistent(true);
            tabs.setContainer(model);

            // Add location and tab information to elements and labels.
            for (TabDefinition tab : mainFrame.getAllTabs()) {

                // Add location and tab information for each element in the tab.
                for (PositionableElement element : tab.getElements()) {
                    HomerWidgetElement homerElement = (HomerWidgetElement) element;
                    String strippedFullName = _stripFullName(homerElement
                            .getElement().getFullName());
                    NamedObj elementInModel = null;
                    NamedObj elementOnScreen = element.getElement();

                    if (homerElement.getElement() instanceof Attribute) {
                        elementInModel = model.getAttribute(strippedFullName);
                    } else if (homerElement.getElement() instanceof ComponentEntity) {
                        elementInModel = model.getEntity(strippedFullName);
                    } else {
                        // This should never happen.
                        throw new IllegalStateException(
                                "Unrecognized element type");
                    }

                    // Add enabled.
                    Parameter enabledNode = (Parameter) elementInModel
                            .getAttribute(HomerConstants.ENABLED_NODE);
                    if (enabledNode != null) {
                        elementInModel.removeAttribute(enabledNode);
                    }

                    enabledNode = new Parameter(elementInModel,
                            HomerConstants.ENABLED_NODE);
                    enabledNode.setPersistent(true);
                    enabledNode.setVisibility(Settable.NONE);

                    Parameter enabled = (Parameter) elementOnScreen
                            .getAttribute(HomerConstants.ENABLED_NODE);
                    if (enabled != null && enabled.getToken() != null) {
                        enabledNode.setToken(enabled.getToken());
                    } else {
                        enabledNode.setToken(new BooleanToken(true));
                    }

                    // Add required
                    Parameter requiredNode = (Parameter) elementInModel
                            .getAttribute(HomerConstants.REQUIRED_NODE);
                    if (requiredNode != null) {
                        elementInModel.removeAttribute(requiredNode);
                    }

                    requiredNode = new Parameter(elementInModel,
                            HomerConstants.REQUIRED_NODE);
                    requiredNode.setPersistent(true);
                    requiredNode.setVisibility(Settable.NONE);

                    Parameter required = (Parameter) elementOnScreen
                            .getAttribute(HomerConstants.REQUIRED_NODE);
                    if (required != null && required.getToken() != null) {
                        requiredNode.setToken(required.getToken());
                    } else {
                        requiredNode.setToken(new BooleanToken(false));
                    }

                    // Add location
                    Attribute positionNode = elementInModel
                            .getAttribute(HomerConstants.POSITION_NODE);
                    if (positionNode != null) {
                        elementInModel.removeAttribute(positionNode);
                    }
                    new HomerLocation(elementInModel,
                            HomerConstants.POSITION_NODE)
                    .setToken(getLocationToken(homerElement.getWidget()));

                    // Add tab information
                    Attribute tabNode = elementInModel
                            .getAttribute(HomerConstants.TAB_NODE);
                    if (tabNode != null) {
                        elementInModel.removeAttribute(tabNode);
                    }
                    new StringAttribute(elementInModel, HomerConstants.TAB_NODE)
                    .setExpression(tab.getTag());
                }
            }

            // Save in file
            out = new BufferedWriter(new FileWriter(layoutFile));
            model.exportMoML(out);
        } catch (Throwable throwable) {
            // TODO Auto-generated catch block
            throwable.printStackTrace();
        } finally {
            // Release models
            mainFrame.getTopLevelActor().workspace().doneReading();
            if (model != null) {
                model.workspace().doneWriting();
            }

            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    /** Strips the first part of a compound element name, including the
     *  "." at the beginning.
     *
     * @param fullName The compound name of an element.
     * @return The stripped name of the element, where the first part of
     * the compound name is removed, including the "." at the beginning.
     */
    private static String _stripFullName(String fullName) {
        if (fullName.indexOf(".") == -1 || fullName.length() < 2) {
            return fullName;
        }
        return fullName.substring(fullName.substring(1).indexOf(".") + 2);
    }

    /** Get the location from a widget in the form of an IntMatrixToken.
     *
     *  @param widget The widget from where to extract the location from.
     *  @return The IntMatrixToken representing the location of the widget.
     */
    public static IntMatrixToken getLocationToken(Widget widget) {
        Point preferredLocation = widget.getPreferredLocation();
        Rectangle bounds = widget.getPreferredBounds();
        Insets insets = widget.getBorder().getInsets();
        int[][] location = new int[][] { {
            bounds.x + preferredLocation.x + insets.left,
            bounds.y + preferredLocation.y + insets.top,
            bounds.width - insets.right, bounds.height - insets.bottom } };

        IntMatrixToken locationToken = null;
        try {
            locationToken = new IntMatrixToken(location);
        } catch (IllegalActionException e) {
            // This is reached only if the location matrix is null.
            e.printStackTrace();
        }

        return locationToken;
    }

    /** Check if the entity is a sink or source. It checks based on the ports of
     *  connected relations.
     *
     *  @param entity The Ptolemy entity to check.
     *  @return SinkOrSource enumeration indicating whether the entity is a sink, a
     *  source, both, or none.
     */
    public static SinkOrSource isSinkOrSource(ComponentEntity entity) {
        boolean isSink = true;
        boolean isSource = true;

        for (Object portObject : entity.portList()) {
            if (!(portObject instanceof IOPort)) {
                continue;
            }

            IOPort port = (IOPort) portObject;
            for (Object relationObject : port.linkedRelationList()) {
                Relation relation = (Relation) relationObject;
                List<Port> linkedPortList = relation.linkedPortList(port);

                for (Port connectingPort : linkedPortList) {
                    if (connectingPort instanceof IOPort) {
                        if (port.isOutput()) {
                            isSink = false;
                        }
                        if (port.isInput()) {
                            isSource = false;
                        }
                    }
                }
            }
        }

        if (isSink && isSource) {
            return SinkOrSource.SINK_AND_SOURCE;
        } else if (isSource) {
            return SinkOrSource.SOURCE;
        } else if (isSink) {
            return SinkOrSource.SINK;
        }

        return SinkOrSource.NONE;
    }

    /** Categorization of an entity.
     */
    public static enum SinkOrSource {
        /** Categorize entity as a sink.
         */
        SINK,
        /** Categorize entity as a source.
         */
        SOURCE,
        /** Categorize entity as both a sink and a source.
         */
        SINK_AND_SOURCE,
        /** Categorize entity as neither a sink or source.
         */
        NONE
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Mark the given element as a proxy element in the given model.
     *
     *  @param model The model where the similar element to the given element is
     *  marked as proxy.
     *  @param element The element defining which element in the model will be marked
     *  as proxy.
     *  @exception IllegalActionException If the model does not contain the element,
     *  or if it cannot be marked as a proxy element.
     */
    private static void _markAsProxy(CompositeActor model, NamedObj element)
            throws IllegalActionException {
        String strippedFullName = _stripFullName(element.getFullName());

        // Add a new proxy attribute
        if (element instanceof ComponentEntity) {
            // Remove the proxy attribute if it's present.
            ComponentEntity entityInModel = model.getEntity(strippedFullName);
            if (entityInModel == null) {
                throw new IllegalActionException(element,
                        "Entity not found in the model.");
            }
            Attribute proxy = entityInModel
                    .getAttribute(ServerUtility.REMOTE_OBJECT_TAG);
            if (proxy != null) {
                element.removeAttribute(proxy);
            }

            SinkOrSource sinkOrSource = isSinkOrSource((ComponentEntity) element);

            try {
                model.workspace().getWriteAccess();

                if (sinkOrSource == SinkOrSource.SOURCE
                        || sinkOrSource == SinkOrSource.SINK_AND_SOURCE) {
                    SingletonParameter parameter = new SingletonParameter(
                            entityInModel, ServerUtility.REMOTE_OBJECT_TAG);
                    parameter.setVisibility(Settable.NONE);
                    parameter.setPersistent(true);
                    parameter
                    .setExpression(ServerUtility.PROXY_SOURCE_ATTRIBUTE);
                } else if (sinkOrSource == SinkOrSource.SINK) {
                    SingletonParameter parameter = new SingletonParameter(
                            entityInModel, ServerUtility.REMOTE_OBJECT_TAG);
                    parameter.setVisibility(Settable.NONE);
                    parameter.setPersistent(true);
                    parameter.setExpression(ServerUtility.PROXY_SINK_ATTRIBUTE);
                }
            } catch (IllegalActionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NameDuplicationException e) {
                // Since the attribute was removed, this case should not happen
            } finally {
                model.workspace().doneWriting();
            }
        } else if (element instanceof Attribute) {
            try {
                model.workspace().getWriteAccess();
                Attribute attributeInModel = model
                        .getAttribute(strippedFullName);
                if (attributeInModel == null) {
                    throw new IllegalActionException(element,
                            "Attribute not found in the model.");
                }
                Attribute proxy = attributeInModel
                        .getAttribute(ServerUtility.REMOTE_OBJECT_TAG);
                if (proxy != null) {
                    element.removeAttribute(proxy);
                }

                SingletonParameter parameter = new SingletonParameter(
                        model.getAttribute(strippedFullName),
                        ServerUtility.REMOTE_OBJECT_TAG);
                parameter.setVisibility(Settable.NONE);
                parameter.setPersistent(true);
                parameter.setExpression(ServerUtility.REMOTE_ATTRIBUTE);
            } catch (IllegalActionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NameDuplicationException e) {
                // Since the attribute was removed, this case should not happen
            } finally {
                model.workspace().doneWriting();
            }
        }
    }

}
