/* Handle model and layout file operations.

 Copyright (c) 2011 The Regents of the University of California.
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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.netbeans.api.visual.widget.Widget;

import ptolemy.actor.IOPort;
import ptolemy.data.IntMatrixToken;
import ptolemy.homer.gui.HomerMainFrame;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;

/** Handle model and layout file operations.
 * 
 *  @author Peter Foldes
 *  @version $Id$
 *  @since Ptolemy II 8.1
 *  @Pt.ProposedRating Red (pdf)
 *  @Pt.AcceptedRating Red (pdf)
 */
public class LayoutFileOperations {

    private LayoutFileOperations() {
    }

    public static void save(HomerMainFrame parent) {
        // TODO
    }

    public static void open(HomerMainFrame parent, URL modelURL, URL layoutURL)
            throws IllegalActionException, NameDuplicationException {
        // TODO
    }

    /** Open a MoML file, parse it, and the parsed model.
     * 
     *  @param url The url of the model.
     *  @return The parsed model.
     *  @exception IllegalActionException If the parsing failed.
     */
    public static CompositeEntity openModelFile(URL url)
            throws IllegalActionException {
        CompositeEntity topLevel = null;
        MoMLParser parser = new MoMLParser(new Workspace());
        MoMLParser.setMoMLFilters(BackwardCompatibility.allFilters());
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
//
//        CompositeActor model = null;
//        BufferedWriter out = null;
//        try {
//            // Get the original model
//            model = (CompositeActor) openModelFile(mainFrame.getModelURL());
//
//            // Add remote attributes to elements
//            for (NamedObj element : mainFrame.getRemoteObjectSet()) {
//                String strippedFullName = stripFullName(element.getFullName());
//
//                // Check if the element is a sink or a source
//                if (element instanceof ComponentEntity) {
//                    SinkOrSource sinkOrSource = isSinkOrSource((ComponentEntity) element);
//
//                    if (sinkOrSource == SinkOrSource.SOURCE
//                            || sinkOrSource == SinkOrSource.SINK_AND_SOURCE) {
//                        SingletonParameter parameter = new SingletonParameter(
//                                model.getEntity(strippedFullName),
//                                HomerConstants.REMOTE_NODE);
//                        parameter.setPersistent(true);
//                        parameter.setExpression(HomerConstants.REMOTE_SOURCE);
//                    } else if (sinkOrSource == SinkOrSource.SINK) {
//                        SingletonParameter parameter = new SingletonParameter(
//                                model.getEntity(strippedFullName),
//                                HomerConstants.REMOTE_NODE);
//                        parameter.setPersistent(true);
//                        parameter.setExpression(HomerConstants.REMOTE_SINK);
//                    }
//                } else if (element instanceof Attribute) {
//                    SingletonParameter parameter = new SingletonParameter(
//                            model.getAttribute(strippedFullName),
//                            HomerConstants.REMOTE_NODE);
//                    parameter.setPersistent(true);
//                    parameter.setExpression(HomerConstants.REMOTE_ATTRIBUTE);
//                }
//            }
//
//            // Add location and tab information to elements
//            Attribute tabs = new Attribute(model, HomerConstants.TABS_NODE);
//
//            HashMap<TabScenePanel, StringAttribute> tabTags = new HashMap<TabScenePanel, StringAttribute>();
//            for (NamedObj element : mainFrame.getWidgetMap().keySet()) {
//                Widget widget = (Widget) mainFrame.getWidgetMap().get(element);
//                String strippedFullName = stripFullName(element.getFullName());
//                // Add location
//                NamedObj elementInModel = null;
//
//                if (element instanceof Attribute) {
//                    elementInModel = model.getAttribute(strippedFullName);
//                } else if (element instanceof ComponentEntity) {
//                    elementInModel = model.getEntity(strippedFullName);
//                } else {
//                    // TODO throw exception
//                }
//
//                new HomerLocation(elementInModel, HomerConstants.POSITION_NODE)
//                        .setToken(getLocationToken(widget));
//
//                StringAttribute tabTag = tabTags.get(mainFrame
//                        .getWidgetTabMap().get(widget));
//                if (tabTag == null) {
//                    tabTag = new StringAttribute(tabs, tabs.uniqueName("tab_"));
//                    // FIXME set correct name
//                    tabTag.setExpression(tabTag.getName());
//                    tabTags.put(mainFrame.getWidgetTabMap().get(widget), tabTag);
//                }
//                // Add tab information
//                new StringAttribute(elementInModel, HomerConstants.TAB_NODE)
//                        .setExpression(tabTag.getName());
//                // Store tag identifier for later
//            }
//
//            // Create layout model
//            System.out.println(model.exportMoML());
//            new ProxyModelBuilder(ProxyModelType.CLIENT, model).build();
//            // Save in file
//            out = new BufferedWriter(new FileWriter(layoutFile));
//            model.exportMoML(out);
//        } catch (Exception e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } finally {
//            if (out != null) {
//                try {
//                    out.close();
//                } catch (IOException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//            }
//        }

    }

    private static CompositeEntity mergeModelWithLayout(CompositeEntity model,
            CompositeEntity layout, HashSet<Attribute> attributesToMerge)
            throws IllegalActionException, NameDuplicationException {
        HashSet<NamedObj> container = new HashSet<NamedObj>();

        // Traverse all elements in the layout.
        _getProxyElements(layout, container);

        for (NamedObj element : container) {
            Attribute proxyAttribute = element
                    .getAttribute(HomerConstants.REMOTE_NODE);
            if (element instanceof ComponentEntity) {
                proxyAttribute.setContainer(model
                        .getEntity(stripFullName(element.getFullName())));
            } else if (element instanceof Attribute) {
                proxyAttribute.setContainer(model
                        .getAttribute(stripFullName(element.getFullName())));
            }
        }

        return model;
    }

    private static CompositeEntity mergeModelWithLayout(URL modelURL,
            URL layoutURL, HashSet<Attribute> attributesToMerge)
            throws IllegalActionException, NameDuplicationException {
        CompositeEntity model = openModelFile(modelURL);
        CompositeEntity layout = openModelFile(layoutURL);
        return mergeModelWithLayout(model, layout, attributesToMerge);
    }

    private static CompositeEntity mergeModelWithLayout(String modelURL,
            String layoutURL, HashSet<Attribute> attributesToMerge)
            throws MalformedURLException, IllegalActionException,
            NameDuplicationException {
        return mergeModelWithLayout(new URL(modelURL), new URL(layoutURL),
                attributesToMerge);
    }

    /** Strips the first part of a compound element name, including the
     *  "." at the beginning.
     * 
     * @param fullName The compound name of an element.
     * @return The stripped name of the element, where the first part of
     * the compound name is removed, including the "." at the beginning.
     */
    private static String stripFullName(String fullName) {
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
        int[][] location = new int[][] { { widget.getBounds().x,
                widget.getBounds().y, widget.getBounds().width,
                widget.getBounds().height } };
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

    /** Get all the elements marked as proxies under the element and add them to
     *  the container. 
     * 
     *  @param element The element to search for proxy attribute and other elements
     *  that have proxy attributes.
     *  @param container
     *  @throws IllegalActionException
     *  @throws NameDuplicationException
     */
    private static void _getProxyElements(NamedObj element,
            HashSet<NamedObj> container) throws IllegalActionException,
            NameDuplicationException {

        // Found the attribute, find the element in the original model
        // and add the attribute to it.
        if (element.getAttribute(HomerConstants.REMOTE_NODE) != null) {
            // Found proxy attribute, add it to the container
            container.add(element);
        } else {
            // Element did not contain the proxy attribute, let's search the
            // other named objects within the element.
            for (Iterator iterator = element.containedObjectsIterator(); iterator
                    .hasNext();) {
                NamedObj namedObj = (NamedObj) iterator.next();
                _getProxyElements(namedObj, container);
            }
        }
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

}
