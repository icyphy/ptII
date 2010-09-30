/* A simple graph view for Ptolemy models

 Copyright (c) 1998-2010 The Regents of the University of California.
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
 2
 */
package ptolemy.vergil.basic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import ptolemy.actor.DesignPatternGetMoMLAction;
import ptolemy.actor.IOPort;
import ptolemy.actor.IORelation;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.EditParametersDialog;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.PtolemyFrame;
import ptolemy.actor.gui.PtolemyPreferences;
import ptolemy.actor.gui.SizeAttribute;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFactory;
import ptolemy.actor.gui.UserActorLibrary;
import ptolemy.actor.gui.WindowPropertiesAttribute;
import ptolemy.actor.gui.properties.ToolBar;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ExpertParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.gui.Query;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.undo.RedoChangeRequest;
import ptolemy.kernel.undo.UndoChangeRequest;
import ptolemy.kernel.undo.UndoStackAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.IconLoader;
import ptolemy.moml.LibraryAttribute;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.MoMLUndoEntry;
import ptolemy.moml.MoMLVariableChecker;
import ptolemy.util.CancelException;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;
import ptolemy.vergil.icon.DesignPatternIcon;
import ptolemy.vergil.kernel.AttributeNodeModel;
import ptolemy.vergil.toolbox.MenuItemFactory;
import ptolemy.vergil.toolbox.MoveAction;
import ptolemy.vergil.tree.EntityTreeModel;
import ptolemy.vergil.tree.PTree;
import ptolemy.vergil.tree.PTreeMenuCreator;
import ptolemy.vergil.tree.VisibleTreeModel;
import diva.canvas.CanvasUtilities;
import diva.canvas.Figure;
import diva.canvas.JCanvas;
import diva.canvas.Site;
import diva.canvas.connector.FixedNormalSite;
import diva.canvas.connector.Terminal;
import diva.canvas.event.LayerAdapter;
import diva.canvas.event.LayerEvent;
import diva.canvas.interactor.SelectionModel;
import diva.graph.GraphController;
import diva.graph.GraphEvent;
import diva.graph.GraphModel;
import diva.graph.GraphPane;
import diva.graph.GraphUtilities;
import diva.graph.JGraph;
import diva.graph.basic.BasicLayoutTarget;
import diva.graph.layout.LayoutTarget;
import diva.graph.layout.LevelLayout;
import diva.gui.GUIUtilities;
import diva.gui.toolbox.JCanvasPanner;
import diva.gui.toolbox.JContextMenu;
import diva.util.java2d.ShapeUtilities;

///////////////////////////////////////////////////////////////////
//// BasicGraphFrame

/**
 A simple graph view for ptolemy models.  This represents a level of
 the hierarchy of a ptolemy model as a diva graph.  Cut, copy and
 paste operations are supported using MoML.

 @author  Steve Neuendorffer, Edward A. Lee, Contributors: Chad Berkeley (Kepler), Ian Brown (HSBC), Bert Rodiers, Christian Motika
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (neuendor)
 @Pt.AcceptedRating Red (johnr)
 */
public abstract class BasicGraphFrame extends PtolemyFrame implements
        Printable, ClipboardOwner, ChangeListener, MouseWheelListener,
        MouseListener, MouseMotionListener {

    /** Construct a frame associated with the specified Ptolemy II model
     *  or object. After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  This is typically done by calling show() on the controlling tableau.
     *  This constructor results in a graph frame that obtains its library
     *  either from the model (if it has one) or the default library defined
     *  in the configuration.
     *  @see Tableau#show()
     *  @param entity The model or object to put in this frame.
     *  @param tableau The tableau responsible for this frame.
     */
    public BasicGraphFrame(NamedObj entity, Tableau tableau) {
        this(entity, tableau, null);
    }

    /** Construct a frame associated with the specified Ptolemy II model.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  This is typically done by calling show() on the controlling tableau.
     *  This constructor results in a graph frame that obtains its library
     *  either from the model (if it has one), or the <i>defaultLibrary</i>
     *  argument (if it is non-null), or the default library defined
     *  in the configuration.
     *  @see Tableau#show()
     *  @param entity The model or object to put in this frame.
     *  @param tableau The tableau responsible for this frame.
     *  @param defaultLibrary An attribute specifying the default library
     *   to use if the model does not have a library.   The <i>defaultLibrary</i>
     *  attribute is only read if the model does not have a
     *  {@link ptolemy.moml.LibraryAttribute} with the name
     *  "<code>_library</code>", or if the LibraryAttribute cannot be
     *  read.
     */
    public BasicGraphFrame(NamedObj entity, Tableau tableau,
            LibraryAttribute defaultLibrary) {
        super(entity, tableau);
        _defaultLibrary = defaultLibrary;
        _initBasicGraphFrame();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to the fact that a change has been successfully executed
     *  by marking the data associated with this window modified.  This
     *  will trigger a dialog when the window is closed, prompting the
     *  user to save the data.
     *  @param change The change that has been executed.
     */
    public void changeExecuted(ChangeRequest change) {
        boolean persistent = true;

        // If the change is null, do not mark the model modified,
        // but do update the graph panner.
        if (change != null) {
            persistent = change.isPersistent();

            // Note that we don't want to accidently reset to false here.
            if (persistent) {
                setModified(persistent);
            }
        }

        if (_graphPanner != null) {
            _graphPanner.repaint();
        }
    }

    /** React to the fact that a change has triggered an error by
     *  doing nothing (the effigy is also listening and will report
     *  the error).
     *  @param change The change that was attempted.
     *  @param exception The exception that resulted.
     */
    public void changeFailed(ChangeRequest change, Exception exception) {
        // Do not report if it has already been reported.
        if (change == null) {
            MessageHandler.error("Change failed", exception);
        } else if (!change.isErrorReported()) {
            change.setErrorReported(true);
            MessageHandler.error("Change failed", exception);
        }
    }

    /** Clear the selected objects in this frame.
     */
    public void clearSelection() {
        GraphController controller = _getGraphController();
        SelectionModel model = controller.getSelectionModel();
        model.clearSelection();
    }

    /** Get the currently selected objects from this document, if any,
     *  and place them on the clipboard in MoML format.
     */
    public void copy() {
        HashSet<NamedObj> namedObjSet = _getSelectionSet();
        StringWriter buffer = new StringWriter();

        try {
            NamedObj container = (NamedObj) _getGraphModel().getRoot();

            // NOTE: The order in the model must be respected.
            Iterator<NamedObj> elements = container.sortContainedObjects(
                    namedObjSet).iterator();

            while (elements.hasNext()) {
                NamedObj element = elements.next();

                // first level to avoid obnoxiousness with
                // toplevel translations.
                element.exportMoML(buffer, 0);
            }

            if (container instanceof CompositeEntity) {
                buffer.write(((CompositeEntity) container).exportLinks(1,
                        namedObjSet));
            }

            Clipboard clipboard = java.awt.Toolkit.getDefaultToolkit()
                    .getSystemClipboard();

            // The code below does not use a PtolemyTransferable,
            // to work around
            // a bug in the JDK that should be fixed as of jdk1.3.1.  The bug
            // is that cut and paste through the system clipboard to native
            // applications doesn't work unless you use string selection.
            String momlToBeCopied = buffer.toString();
            String variablesToBePrepended = "";
            try {
                MoMLVariableChecker variableChecker = new MoMLVariableChecker();
                variablesToBePrepended = variableChecker.checkCopy(
                        momlToBeCopied, container);
            } catch (IllegalActionException ex) {
                // Ignore, maybe the missing symbols will work out
                // in the pasted context.
            }
            clipboard.setContents(new StringSelection(variablesToBePrepended
                    + momlToBeCopied), this);

        } catch (IOException ex) {
            MessageHandler.error("Copy failed", ex);
        }
    }

    /** Create a typed composite actor that contains the selected actors
     *  and connections. The created typed composite actor is transparent.
     *  The resulting topology is the same in the sense
     *  of deep connectivities.
     */
    public void createHierarchy() {
        GraphController controller = _getGraphController();
        SelectionModel model = controller.getSelectionModel();
        AbstractBasicGraphModel graphModel = (AbstractBasicGraphModel) controller
                .getGraphModel();

        List<Object> origSelection = Arrays.asList(model.getSelectionAsArray());
        HashSet<Object> selection = new HashSet<Object>();
        selection.addAll(origSelection);

        // A set, because some objects may represent the same
        // ptolemy object.
        HashSet<NamedObj> namedObjSet = new HashSet<NamedObj>();
        HashSet<Object> nodeSet = new HashSet<Object>();

        StringBuffer newPorts = new StringBuffer();
        StringBuffer extRelations = new StringBuffer();
        StringBuffer extConnections = new StringBuffer();
        StringBuffer intRelations = new StringBuffer();
        StringBuffer intConnections = new StringBuffer();

        HashSet<Object> selectedEdges = new HashSet<Object>();

        // First get all the nodes.
        try {
            NamedObj container = (NamedObj) graphModel.getRoot();

            if (!(container instanceof CompositeEntity)) {
                // This is an internal error because a reasonable GUI should not
                // provide access to this functionality.
                throw new InternalErrorException(
                        "Cannot create hierarchy if the container is not a CompositeEntity.");
            }

            CompositeEntity compositeActor = (CompositeEntity) container;

            String compositeActorName = container.uniqueName("CompositeActor");

            double[] location = new double[2];
            boolean gotLocation = false;

            for (Object selectedItem : origSelection) {
                if (selectedItem instanceof Figure) {
                    Object userObject = ((Figure) selectedItem).getUserObject();

                    // We want to skip the selection of ports in composite actors since
                    //  these should remain in the already existing composite actor.
                    // When the wire has been selected the port will be correctly duplicated.
                    if (userObject instanceof Location) {
                        Location loc = (Location) userObject;
                        NamedObj locationContainer = loc.getContainer();
                        if (locationContainer != null
                                && (locationContainer instanceof IOPort)) {
                            NamedObj portContainer = locationContainer
                                    .getContainer();
                            if (portContainer != null
                                    && portContainer instanceof CompositeEntity) {
                                // Remove element from selection
                                model.removeSelection(selectedItem);
                                selection.remove(selectedItem);

                                // Don't process this node.
                                continue;
                            }
                        }

                    }

                    if (!gotLocation) {
                        location[0] = ((Figure) selectedItem).getBounds()
                                .getCenterX();
                        location[1] = ((Figure) selectedItem).getBounds()
                                .getCenterY();
                        gotLocation = true;
                    }

                    if (graphModel.isNode(userObject)) {
                        nodeSet.add(userObject);

                        NamedObj actual = (NamedObj) graphModel
                                .getSemanticObject(userObject);
                        namedObjSet.add(actual);

                        // We will now add all links from and to the ports of the actor
                        //      as selected links since we don't want to lose links and relations when creating
                        //      hierarchies

                        if (actual instanceof Entity) {
                            for (IOPort port : (List<IOPort>) ((Entity) actual)
                                    .portList()) {
                                Iterator<?> outEdges = graphModel
                                        .outEdges(port);
                                while (outEdges.hasNext()) {
                                    Object obj = outEdges.next();
                                    selectedEdges.add(obj);
                                    selection.add(controller.getFigure(obj));
                                }
                                Iterator<?> inEdges = graphModel.inEdges(port);
                                while (inEdges.hasNext()) {
                                    Object obj = inEdges.next();
                                    selectedEdges.add(obj);
                                    selection.add(controller.getFigure(obj));
                                }
                            }
                        }
                    } else if (graphModel.isEdge(userObject)) {
                        selectedEdges.add(userObject);
                    }
                }
            }

            int i = 0;
            for (Object userObject : selectedEdges) {
                assert graphModel.isEdge(userObject);
                // Check to see if the head and tail are both being
                // selected.
                Object head = graphModel.getHead(userObject);

                // System.out.println("head:" +((NamedObj)head).getName());
                Object tail = graphModel.getTail(userObject);

                // System.out.println("tail:" +((NamedObj)tail).getName());
                boolean headOK = nodeSet.contains(head);
                boolean tailOK = nodeSet.contains(tail);
                Iterator<Object> objects = nodeSet.iterator();

                while (!(headOK && tailOK) && objects.hasNext()) {
                    Object object = objects.next();

                    if (!headOK
                            && GraphUtilities.isContainedNode(head, object,
                                    graphModel)) {
                        headOK = true;
                    }

                    if (!tailOK
                            && GraphUtilities.isContainedNode(tail, object,
                                    graphModel)) {
                        tailOK = true;
                    }
                }

                // For the edges at the boundary.
                if ((!headOK && tailOK) || (headOK && !tailOK)) {

                    LinkElementProperties headProperties = LinkElementProperties
                            .extractLinkProperties(head);
                    LinkElementProperties tailProperties = LinkElementProperties
                            .extractLinkProperties(tail);

                    if (headProperties.port == null
                            && tailProperties.port != null) {
                        //Swap head and tail
                        LinkElementProperties temp = headProperties;
                        headProperties = tailProperties;
                        tailProperties = temp;
                    }

                    IORelation relation = null;

                    boolean duplicateRelation = true;
                    if (headProperties.type == ElementInLinkType.RELATION) {
                        relation = (IORelation) graphModel
                                .getSemanticObject(headProperties.element);
                        duplicateRelation = false;
                    } else if (tailProperties.type == ElementInLinkType.RELATION) {
                        relation = (IORelation) graphModel
                                .getSemanticObject(tailProperties.element);
                        duplicateRelation = false;
                    } else {
                        relation = (IORelation) graphModel
                                .getSemanticObject(userObject);
                        duplicateRelation = true;
                    }

                    if (headProperties.port != null) {
                        ComponentEntity entity = (ComponentEntity) headProperties.port
                                .getContainer();
                        String portName = "port_" + i;
                        boolean isInput = headProperties.port.isInput();
                        boolean isOutput = headProperties.port.isOutput();
                        newPorts.append("<port name=\"" + portName
                                + "\" class=\"ptolemy.actor.TypedIOPort"
                                + "\">\n");

                        if (headProperties.port.isMultiport()) {
                            newPorts.append("<property name=\"multiport\"/>\n");
                        }

                        if (namedObjSet.contains(entity)) {
                            // The port is inside the hierarchy.
                            // The relation must be outside.
                            // Create composite port.
                            if (isInput) {
                                newPorts.append("<property name=\"input\"/>");
                            }

                            if (isOutput) {
                                newPorts.append("<property name=\"output\"/>");
                            }

                            newPorts.append("\n</port>\n");

                            // Create internal relation and links.
                            // Note we can only partially reuse
                            // the relation name, one original relation
                            // can be two internal relations.
                            String relationName = relation.getName() + "_" + i;
                            intRelations.append("<relation name=\""
                                    + relationName + "\" class=\""
                                    + "ptolemy.actor.TypedIORelation\"/>\n");
                            intConnections.append("<link port=\""
                                    + entity.getName() + "."
                                    + headProperties.port.getName()
                                    + "\" relation=\"" + relationName
                                    + "\"/>\n");
                            intConnections.append("<link port=\"" + portName
                                    + "\" relation=\"" + relationName
                                    + "\"/>\n");

                            // Create external links.
                            if (duplicateRelation) {
                                extRelations
                                        .append("<relation name=\""
                                                + relation.getName()
                                                + "\" class=\""
                                                + "ptolemy.actor.TypedIORelation\"/>\n");

                                ComponentEntity otherEntity = (ComponentEntity) tailProperties.port
                                        .getContainer();

                                if (otherEntity == container) {
                                    // This is a boundary port at a higher level.
                                    extConnections.append("<link port=\""
                                            + tailProperties.port.getName()
                                            + "\" relation=\""
                                            + relation.getName() + "\"/>\n");
                                } else {
                                    extConnections.append("<link port=\""
                                            + otherEntity.getName() + "."
                                            + tailProperties.port.getName()
                                            + "\" relation=\""
                                            + relation.getName() + "\"/>\n");
                                }
                            }

                            extConnections.append("<link port=\""
                                    + compositeActorName + "." + portName
                                    + "\" relation=\"" + relation.getName()
                                    + "\"/>\n");
                        } else {
                            // The port is outside the hierarchy.
                            // The relation must be inside.
                            if ((isInput && headProperties.type == ElementInLinkType.PORT_IN_ACTOR)
                                    || (isOutput && headProperties.type == ElementInLinkType.STANDALONE_PORT)) {
                                newPorts.append("<property name=\"output\"/>");
                            }

                            if ((isOutput && headProperties.type == ElementInLinkType.PORT_IN_ACTOR)
                                    || (isInput && headProperties.type == ElementInLinkType.STANDALONE_PORT)) {
                                newPorts.append("<property name=\"input\"/>");
                            }

                            newPorts.append("\n</port>\n");

                            String relationName = relation.getName() + "_" + i;
                            extRelations.append("<relation name=\""
                                    + relationName + "\" class=\""
                                    + "ptolemy.actor.TypedIORelation\"/>\n");
                            String entityPrefix = "";
                            if (getModel() != entity) {
                                entityPrefix = entity.getName() + ".";
                            }
                            extConnections.append("<link port=\""
                                    + entityPrefix
                                    + headProperties.port.getName()
                                    + "\" relation=\"" + relationName
                                    + "\"/>\n");
                            extConnections.append("<link port=\""
                                    + compositeActorName + "." + portName
                                    + "\" relation=\"" + relationName
                                    + "\"/>\n");

                            // Create external links.
                            if (duplicateRelation) {
                                intRelations
                                        .append("<relation name=\""
                                                + relation.getName()
                                                + "\" class=\""
                                                + "ptolemy.actor.TypedIORelation\"/>\n");

                                ComponentEntity otherEntity = (ComponentEntity) tailProperties.port
                                        .getContainer();

                                String otherEntityPrefix = "";
                                if (getModel() != otherEntity) {
                                    otherEntityPrefix = otherEntity.getName()
                                            + ".";
                                }

                                intConnections.append("<link port=\""
                                        + otherEntityPrefix
                                        + tailProperties.port.getName()
                                        + "\" relation=\"" + relation.getName()
                                        + "\"/>\n");
                            }

                            intConnections.append("<link port=\"" + portName
                                    + "\" relation=\"" + relation.getName()
                                    + "\"/>\n");
                        }
                    }
                    //                        } else if (!headOK && !tailOK) {
                    //                            // We only selected an edge. Build one input
                    //                            // port, one output port for it, and build
                    //                            // a direct connection.
                }
                ++i;
            }

            // System.out.println(" new ports:" + newPorts);

            // Create the MoML command.
            StringBuffer moml = new StringBuffer();

            // If the dropObj defers to something else, then we
            // have to check the parent of the object
            // for import attributes, and then we have to
            // generate import statements.  Note that everything
            // imported by the parent will be imported now by
            // the object into which this is dropped.
            moml.append("<group>\n");

            // Copy the selection, then get it from the clipboard
            // and insert its MoML description in the new composite.
            // This must be done before the call to _deleteMoML(),
            // which clears the selection.
            String selectionMoML;
            copy();
            Clipboard clipboard = java.awt.Toolkit.getDefaultToolkit()
                    .getSystemClipboard();
            Transferable transferable = clipboard.getContents(this);
            try {
                selectionMoML = (String) transferable
                        .getTransferData(DataFlavor.stringFlavor);
            } catch (Exception ex) {
                throw new InternalErrorException(null, ex,
                        "Getting data from clipboard failed.");
            }

            // Generate the MoML to carry out the deletion.

            moml.append(_deleteMoML(graphModel,
                    selection.toArray(new Object[] {}), model));

            moml.append("<entity name=\"" + compositeActorName
                    + "\" class=\"ptolemy.actor.TypedCompositeActor\">\n");
            moml.append("\t<property name=\"_location\" class=\""
                    + "ptolemy.kernel.util.Location\" value=\"" + location[0]
                    + ", " + location[1] + "\">\n");
            moml.append("\t</property>\n");
            moml.append(newPorts);

            moml.append(selectionMoML);

            // Internal relations and connections.
            moml.append(intRelations);
            moml.append(intConnections);
            moml.append("</entity>\n");

            // External relations and connections.
            moml.append(extRelations);
            moml.append(extConnections);

            moml.append("</group>\n");

            // System.out.println(moml.toString());

            MoMLChangeRequest request = null;
            request = new MoMLChangeRequest(this, container, moml.toString());
            request.setUndoable(true);

            container.requestChange(request);
            NamedObj newObject = compositeActor.getEntity(compositeActorName);
            // Kepler wants a different icon.
            IconLoader _iconLoader = MoMLParser.getIconLoader();
            if (_iconLoader != null) {
                _iconLoader.loadIconForClass(
                        "ptolemy.actor.TypedCompositeActor", newObject);
            }
        } catch (Throwable throwable) {
            MessageHandler.error("Creating hierarchy failed", throwable);
        }
    }

    /** Remove the currently selected objects from this document, if any,
     *  and place them on the clipboard.
     */
    public void cut() {
        copy();
        delete();
    }

    /** Delete the currently selected objects from this document.
     */
    public void delete() {
        // Note that we previously a delete was handled at the model level.
        // Now a delete is handled by generating MoML to carry out the delete
        // and handing that MoML to the parser
        GraphController controller = _getGraphController();
        SelectionModel model = controller.getSelectionModel();
        AbstractBasicGraphModel graphModel = (AbstractBasicGraphModel) controller
                .getGraphModel();
        Object[] selection = model.getSelectionAsArray();

        // Generate the MoML to carry out the deletion
        StringBuffer moml = _deleteMoML(graphModel, selection, model);

        // Next process the deletion MoML. This should be the large majority
        // of most deletions.
        try {
            // Finally create and request the change
            NamedObj container = graphModel.getPtolemyModel();
            MoMLChangeRequest change = new MoMLChangeRequest(this, container,
                    moml.toString());
            change.setUndoable(true);
            container.requestChange(change);
        } catch (Exception ex) {
            MessageHandler
                    .error("Delete failed, changeRequest was:" + moml, ex);
        }

        graphModel.dispatchGraphEvent(new GraphEvent(this,
                GraphEvent.STRUCTURE_CHANGED, graphModel.getRoot()));
    }

    /** Dispose of this frame.
     *     Override this dispose() method to unattach any listeners that may keep
     *  this model from getting garbage collected.  This method calls
     *  {@link #disposeSuper()}.
     */
    public void dispose() {
        // Remove the association with the library. This is necessary to allow
        // this frame, and the rest of the model to be properly garbage
        // collected
        if (_libraryModel != null) {
            _libraryModel.setRoot(null);
        }
        _openGraphFrames.remove(this);
        disposeSuper();
    }

    /** Invoke the dispose() method of the superclass,
     *  {@link ptolemy.actor.gui.PtolemyFrame}.
     */
    public void disposeSuper() {
        // This method is used by Kepler for the tabbed pane interface.
        super.dispose();
    }

    /** Expand all the rows of the library.
     *  Expanding all the rows is useful for testing.
     */
    public void expandAllLibraryRows() {
        for (int i = 0; i < _library.getRowCount(); i++) {
            _library.expandRow(i);
        }
    }

    /** Export the current submodel as a design pattern using a method similar to
     *  Save As.
     */
    public void exportDesignPattern() {
        StringAttribute alternateGetMoml = null;
        DesignPatternIcon icon = null;
        try {
            NamedObj model = getModel();
            try {
                if (model.getAttribute("_alternateGetMomlAction") == null) {
                    alternateGetMoml = new StringAttribute(model,
                            "_alternateGetMomlAction");
                    alternateGetMoml
                            .setExpression(DesignPatternGetMoMLAction.class
                                    .getName());
                }

                if (model.getAttribute("_designPatternIcon") == null) {
                    icon = new DesignPatternIcon(model, "_designPatternIcon");
                }
            } catch (Exception e) {
                throw new InternalErrorException(null, e, "Fail to prepare "
                        + "for exporting a design pattern.");
            }

            _prepareExportDesignPattern();
            _saveAs();
        } finally {
            _finishExportDesignPattern();

            if (alternateGetMoml != null) {
                try {
                    alternateGetMoml.setContainer(null);
                } catch (KernelException e) {
                    // Ignore. This shouldn't happen.
                }
            }
            if (icon != null) {
                try {
                    icon.setContainer(null);
                } catch (KernelException e) {
                    // Ignore. This shouldn't happen.
                }
            }
        }
    }

    /** Return the center location of the visible part of the pane.
     *  @return The center of the visible part.
     *  @see #setCenter(Point2D)
     */
    public Point2D getCenter() {
        Rectangle2D rect = getVisibleCanvasRectangle();
        return new Point2D.Double(rect.getCenterX(), rect.getCenterY());
    }

    /** Return the JGraph instance that this view uses to represent the
     *  ptolemy model.
     *  @return the JGraph.
     *  @see #setJGraph(JGraph)
     */
    public JGraph getJGraph() {
        return _jgraph;
    }

    /** Return the JCanvasPanner instance.
     *  @return the JCanvasPanner
     */
    public JCanvasPanner getGraphPanner() {
        return _graphPanner;
    }

    /** Return a set of instances of NamedObj representing the objects
     *  that are currently selected.  This set has no particular order
     *  to it. If you need the selection objects in proper order, as
     *  defined by the container, then call sortContainedObjects()
     *  on the container to sort the result.
     *  @return The set of selected objects.
     */
    public HashSet<NamedObj> getSelectionSet() {
        return _getSelectionSet();
    }

    /** Return the rectangle representing the visible part of the
     *  pane, transformed into canvas coordinates.  This is the range
     *  of locations that are visible, given the current pan and zoom.
     *  @return The rectangle representing the visible part.
     */
    public Rectangle2D getVisibleCanvasRectangle() {
        AffineTransform current = getJGraph().getCanvasPane()
                .getTransformContext().getTransform();
        AffineTransform inverse;

        try {
            inverse = current.createInverse();
        } catch (NoninvertibleTransformException e) {
            throw new RuntimeException(e.toString());
        }

        Rectangle2D visibleRect = getVisibleRectangle();

        return ShapeUtilities.transformBounds(visibleRect, inverse);
    }

    /** Return the rectangle representing the visible part of the
     *  pane, in pixel coordinates on the screen.
     *  @return A rectangle whose upper left corner is at (0, 0) and whose
     *  size is the size of the canvas component.
     */
    public Rectangle2D getVisibleRectangle() {
        Dimension size = getJGraph().getSize();
        return new Rectangle2D.Double(0, 0, size.getWidth(), size.getHeight());
    }

    /** Import a design pattern into the current design.
     */
    public void importDesignPattern() {
        JFileChooser fileDialog = new JFileChooser();

        if (_fileFilter != null) {
            fileDialog.addChoosableFileFilter(_fileFilter);
        }

        fileDialog.setDialogTitle("Select a design pattern file.");
        if (_directory != null) {
            fileDialog.setCurrentDirectory(_directory);
        } else {
            String currentWorkingDirectory = StringUtilities
                    .getProperty("user.dir");
            if (currentWorkingDirectory != null) {
                fileDialog
                        .setCurrentDirectory(new File(currentWorkingDirectory));
            }
        }

        if (fileDialog.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            _directory = fileDialog.getCurrentDirectory();
            NamedObj model = null;
            try {
                File file = fileDialog.getSelectedFile().getCanonicalFile();
                URL url = file.toURI().toURL();
                MoMLParser parser = new MoMLParser();
                MoMLParser.purgeModelRecord(url);
                model = parser.parse(url, url);
                MoMLParser.purgeModelRecord(url);
            } catch (Exception e) {
                report(new IllegalActionException(null, e,
                        "Error reading input"));
            }
            if (model != null) {
                Attribute attribute = model
                        .getAttribute("_alternateGetMomlAction");
                String className = DesignPatternGetMoMLAction.class.getName();
                if (attribute == null
                        || !(attribute instanceof StringAttribute)
                        || !((StringAttribute) attribute).getExpression()
                                .equals(className)) {
                    report(new IllegalActionException(
                            "The model is not a design pattern."));
                } else {
                    String moml = new DesignPatternGetMoMLAction().getMoml(
                            model, model.getName());
                    NamedObj context = getModel();
                    MoMLChangeRequest request = new MoMLChangeRequest(this,
                            context, moml);
                    context.requestChange(request);
                }
            }
        }
    }

    /** Layout the graph view.
     *  If the configuration contains a parameter named
     *  _layoutGraphDialog, the that parameter is assumed
     *  to name a class that creates a modal dialog that displays
     *  controls to change the layout.  The class should have a constructor
     *  that takes a Frame argument. If the parameter cannot
     *  be read, then the default Ptolemy layout mechanism in
     *  {@link #layoutGraphWithPtolemyLayout()} is used.
     */
    public void layoutGraphDialog() {
        boolean success = false;
        try {
            Configuration configuration = getConfiguration();
            StringParameter layoutGraphDialogParameter = (StringParameter) configuration
                    .getAttribute("_layoutGraphDialog", Parameter.class);
            if (layoutGraphDialogParameter == null) {
                layoutGraphWithPtolemyLayout();
                success = true;
            } else {
                String layoutGraphDialogClassName = layoutGraphDialogParameter
                        .stringValue();
                try {
                    Class layoutGraphDialogClass = Class
                            .forName(layoutGraphDialogClassName);
                    List layoutFactories = getModel().attributeList(
                            layoutGraphDialogClass);
                    TableauFactory tableauFactory = null;
                    if (layoutFactories.size() > 0) {
                        tableauFactory = (TableauFactory) layoutFactories
                                .get(0);
                    } else {
                        Constructor layoutGraphDialogConstructor = layoutGraphDialogClass
                                .getDeclaredConstructor(NamedObj.class,
                                        String.class);
                        tableauFactory = (TableauFactory) layoutGraphDialogConstructor
                            .newInstance((PtolemyEffigy) getTableau().getContainer(),
                                    "layoutGraphFactory");
                    }
                    Tableau kielerTableau = tableauFactory.createTableau(
                    //getModel(), this);
                            (PtolemyEffigy) getTableau().getContainer());
                    
                    kielerTableau.show();
                    success = true;
                } catch (Throwable throwable) {
                    new Exception(
                            "Failed to invoke layout graph dialog class \""
                                    + layoutGraphDialogClassName
                                    + "\", which was read from the configuration.",
                            throwable).printStackTrace();
                    layoutGraphWithPtolemyLayout();
                }
            }
        } catch (Throwable throwable) {
            new Exception(
                    "Failed to read _layoutGraphDialogParameter from configuration",
                    throwable).printStackTrace();
            if (!success) {
                layoutGraphWithPtolemyLayout();
            }
        }
    }

    /** Layout the automatically using the KIELER dataflow layout algorithm
     *  with place and route.
     *  _layoutGraphAction, the that parameter is assumed
     *  to name a class that executes the automatic layout action.
     *  If the parameter cannot be read, then the default Ptolemy layout mechanism
     *  in {@link #layoutGraphWithPtolemyLayout()} is used.
     */
    public void layoutGraph() {
        boolean success = false;
        try {
            Configuration configuration = getConfiguration();
            StringParameter layoutGraphActionParameter = (StringParameter) configuration
                    .getAttribute("_layoutGraphAction", Parameter.class);
            if (layoutGraphActionParameter == null) {
                layoutGraphWithPtolemyLayout();
                success = true;
            } else {
                String layoutGraphActionClassName = layoutGraphActionParameter
                        .stringValue();
                try {
                    Class layoutGraphActionClass = Class
                            .forName(layoutGraphActionClassName);
                    Object layoutAction = null;
                    
                    Constructor layoutGraphActionConstructor = layoutGraphActionClass
                            .getDeclaredConstructor();         
                    
                  layoutAction = (Object) layoutGraphActionConstructor
                  .newInstance();
                  
                  if (layoutAction instanceof IGuiAction) {
                      ((IGuiAction)layoutAction).doAction(getModel());
                  }
                  
                    success = true;
                } catch (Throwable throwable) {
                    new Exception(
                            "Failed to invoke layout graph dialog class \""
                                    + layoutGraphActionClassName
                                    + "\", which was read from the configuration.",
                            throwable).printStackTrace();
                    layoutGraphWithPtolemyLayout();
                }
            }
        } catch (Throwable throwable) {
            new Exception(
                    "Failed to read _layoutGraphDialogParameter from configuration",
                    throwable).printStackTrace();
            if (!success) {
                layoutGraphWithPtolemyLayout();
            }
        }
    }

    /** Layout the graph view using the default PtolemyLayout. */
    public void layoutGraphWithPtolemyLayout() {
        GraphController controller = _getGraphController();
        AbstractBasicGraphModel model = _getGraphModel();
        LayoutTarget target = new PtolemyLayoutTarget(controller);
        PtolemyLayout layout = new PtolemyLayout(target);
        layout.setOrientation(LevelLayout.HORIZONTAL);
        layout.setRandomizedPlacement(false);

        // Before doing the layout, need to take a copy of all the current
        // node locations  which can be used to undo the effects of the move.
        try {
            NamedObj composite = model.getPtolemyModel();
            StringBuffer moml = new StringBuffer();
            moml.append("<group>\n");

            // NOTE: this gives at iteration over locations.
            Iterator<Location> nodes = model.nodes(composite);

            while (nodes.hasNext()) {
                Location location = nodes.next();

                // Get the containing element
                NamedObj element = location.getContainer();

                // Give default values in case the previous locations value
                // has not yet been set
                String expression = location.getExpression();

                if (expression == null) {
                    expression = "0, 0";
                }

                // Create the MoML, wrapping the location attribute
                // in an element referring to the container
                String containingElementName = element.getElementName();
                moml.append("<" + containingElementName + " name=\""
                        + element.getName() + "\" >\n");

                // NOTE: use the moml info element name here in case the
                // location is a vertex
                moml.append("<" + location.getElementName() + " name=\""
                        + location.getName() + "\" value=\"" + expression
                        + "\" />\n");
                moml.append("</" + containingElementName + ">\n");
            }

            moml.append("</group>\n");

            // Push the undo entry onto the stack
            MoMLUndoEntry undoEntry = new MoMLUndoEntry(composite,
                    moml.toString());
            UndoStackAttribute undoInfo = UndoStackAttribute
                    .getUndoInfo(composite);
            undoInfo.push(undoEntry);
        } catch (Throwable throwable) {
            // operation not undoable
        }

        // Perform the layout and repaint
        layout.layout(model.getRoot());
        getJGraph().repaint();
        if (_graphPanner != null) {
            _graphPanner.repaint();
        }
    }

    /** Do nothing.
     */
    public void lostOwnership(Clipboard clipboard, Transferable transferable) {
    }

    /** Open the container, if any, of the entity.
     *  If this entity has no container, then do nothing.
     */
    public void openContainer() {
        GraphModel model = _getGraphModel();
        NamedObj toplevel = (NamedObj) model.getRoot();
        if (toplevel != toplevel.toplevel()) {
            try {
                Configuration configuration = getConfiguration();
                // FIXME: do what with the return value?
                configuration.openInstance(toplevel.getContainer());
            } catch (Throwable throwable) {
                MessageHandler.error("Failed to open container", throwable);
            }
        }
    }

    /** Assuming the contents of the clipboard is MoML code, paste it into
     *  the current model by issuing a change request.
     */
    public void paste() {
        Clipboard clipboard = java.awt.Toolkit.getDefaultToolkit()
                .getSystemClipboard();
        Transferable transferable = clipboard.getContents(this);
        GraphModel model = _getGraphModel();

        if (transferable == null) {
            return;
        }

        try {
            NamedObj container = (NamedObj) model.getRoot();
            StringBuffer moml = new StringBuffer();

            // The pasted version will have the names generated by the
            // uniqueName() method of the container, to ensure that they
            // do not collide with objects already in the container.
            moml.append("<group name=\"auto\">\n");
            //moml.append("<group>\n");
            moml.append((String) transferable
                    .getTransferData(DataFlavor.stringFlavor));

            moml.append("</group>\n");

            MoMLChangeRequest change = new OffsetMoMLChangeRequest(this,
                    container, moml.toString());
            change.setUndoable(true);
            container.requestChange(change);
        } catch (Exception ex) {
            MessageHandler.error("Paste failed", ex);
        }
    }

    /** Print the visible portion of the graph to a printer,
     *  which is represented by the specified graphics object.
     *  @param graphics The context into which the page is drawn.
     *  @param format The size and orientation of the page being drawn.
     *  @param index The zero based index of the page to be drawn.
     *  @return PAGE_EXISTS if the page is rendered successfully, or
     *   NO_SUCH_PAGE if pageIndex specifies a non-existent page.
     *  @exception PrinterException If the print job is terminated.
     */
    public int print(Graphics graphics, PageFormat format, int index)
            throws PrinterException {
        if (getJGraph() != null) {
            Rectangle2D view = getVisibleRectangle();
            return getJGraph().print(graphics, format, index, view);
        } else {
            return NO_SUCH_PAGE;
        }
    }

    /** Redo the last undone change on the model.
     *  @see #undo()
     */
    public void redo() {
        GraphModel model = _getGraphModel();

        try {
            NamedObj toplevel = (NamedObj) model.getRoot();
            RedoChangeRequest change = new RedoChangeRequest(this, toplevel);
            toplevel.requestChange(change);
        } catch (Exception ex) {
            MessageHandler.error("Redo failed", ex);
        }
    }

    /** Open a file browser and save the given entity in the file specified
     *  by the user.
     *  @param entity The entity to save.
     *  @exception Exception If there is a problem saving the component.
     *  @since Ptolemy 4.0
     */
    public void saveComponentInFile(Entity entity) throws Exception {
        // NOTE: This mirrors similar code in Top and TableauFrame, but
        // I can't find any way to re-use that code, since the details
        // are slightly different at each step here.
        JFileChooser fileDialog = new JFileChooser();
        fileDialog.setDialogTitle("Save actor as...");

        if (_directory != null) {
            fileDialog.setCurrentDirectory(_directory);
        } else {
            // The default on Windows is to open at user.home, which is
            // typically not what we want.
            // So we use the current directory instead.
            // This will fail with a security exception in applets.
            String currentWorkingDirectory = StringUtilities
                    .getProperty("user.dir");

            if (currentWorkingDirectory != null) {
                fileDialog
                        .setCurrentDirectory(new File(currentWorkingDirectory));
            }
        }

        fileDialog.setSelectedFile(new File(fileDialog.getCurrentDirectory(),
                entity.getName() + ".xml"));

        // Show the dialog.
        int returnVal = fileDialog.showSaveDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileDialog.getSelectedFile();

            if (!_confirmFile(entity, file)) {
                return;
            }

            // Record the selected directory.
            _directory = fileDialog.getCurrentDirectory();

            java.io.FileWriter fileWriter = null;

            try {
                fileWriter = new java.io.FileWriter(file);

                // Make sure the entity name saved matches the file name.
                String name = entity.getName();
                String filename = file.getName();
                int period = filename.indexOf(".");

                if (period > 0) {
                    name = filename.substring(0, period);
                } else {
                    name = filename;
                }

                fileWriter.write("<?xml version=\"1.0\" standalone=\"no\"?>\n"
                        + "<!DOCTYPE " + entity.getElementName() + " PUBLIC "
                        + "\"-//UC Berkeley//DTD MoML 1//EN\"\n"
                        + "    \"http://ptolemy.eecs.berkeley.edu"
                        + "/xml/dtd/MoML_1.dtd\">\n");

                entity.exportMoML(fileWriter, 0, name);
            } finally {
                if (fileWriter != null) {
                    fileWriter.close();
                }
            }
        }
    }

    /** Save the given entity in the user library in the given
     *  configuration.
     *  @param configuration The configuration.
     *  @param entity The entity to save.
     *  @since Ptolemy 2.1
     *  @deprecated Use {@link ptolemy.actor.gui.UserActorLibrary#saveComponentInLibrary(Configuration, Entity)}
     */
    public static void saveComponentInLibrary(Configuration configuration,
            Entity entity) {
        try {
            ptolemy.actor.gui.UserActorLibrary.saveComponentInLibrary(
                    configuration, entity);
        } catch (Exception ex) {
            // We catch exceptions here because this method used to
            // not throw Exceptions, and we don't want to break compatibility.
            MessageHandler
                    .error("Failed to save \"" + entity.getName() + "\".");
        }
    }

    /** Set the center location of the visible part of the pane.
     *  This will cause the panner to center on the specified location
     *  with the current zoom factor.
     *  @param center The center of the visible part.
     *  @see #getCenter()
     */
    public void setCenter(Point2D center) {
        Rectangle2D visibleRect = getVisibleCanvasRectangle();
        AffineTransform newTransform = getJGraph().getCanvasPane()
                .getTransformContext().getTransform();

        newTransform.translate(visibleRect.getCenterX() - center.getX(),
                visibleRect.getCenterY() - center.getY());

        getJGraph().getCanvasPane().setTransform(newTransform);
    }

    /** Set the JGraph instance that this view uses to represent the
     *  ptolemy model.
     *  @param jgraph The JGraph.
     *  @see #getJGraph()
     */
    public void setJGraph(JGraph jgraph) {
        _jgraph = jgraph;
    }

    /** Undo the last undoable change on the model.
     *  @see #redo()
     */
    public void undo() {
        GraphModel model = _getGraphModel();

        try {
            NamedObj toplevel = (NamedObj) model.getRoot();
            UndoChangeRequest change = new UndoChangeRequest(this, toplevel);
            toplevel.requestChange(change);
        } catch (Exception ex) {
            MessageHandler.error("Undo failed", ex);
        }
    }

    /** Zoom in or out to magnify by the specified factor, from the current
     *  magnification.
     *  @param factor The magnification factor (relative to 1.0).
     */
    public void zoom(double factor) {
        JCanvas canvas = getJGraph().getGraphPane().getCanvas();
        AffineTransform current = canvas.getCanvasPane().getTransformContext()
                .getTransform();

        // Save the center, so we remember what we were looking at.
        Point2D center = getCenter();
        current.scale(factor, factor);
        canvas.getCanvasPane().setTransform(current);

        // Reset the center.
        setCenter(center);

        if (_graphPanner != null) {
            _graphPanner.repaint();
        }
    }

    /** Zoom to fit the current figures.
     */
    public void zoomFit() {
        GraphPane pane = getJGraph().getGraphPane();
        Rectangle2D bounds = pane.getForegroundLayer().getLayerBounds();

        if (bounds.isEmpty()) {
            // Empty diagram.
            return;
        }

        Rectangle2D viewSize = getVisibleRectangle();
        AffineTransform newTransform = CanvasUtilities.computeFitTransform(
                bounds, viewSize);
        JCanvas canvas = pane.getCanvas();
        canvas.getCanvasPane().setTransform(newTransform);

        if (_graphPanner != null) {
            _graphPanner.repaint();
        }
    }

    /** Set zoom to the nominal.
     */
    public void zoomReset() {
        JCanvas canvas = getJGraph().getGraphPane().getCanvas();
        AffineTransform current = canvas.getCanvasPane().getTransformContext()
                .getTransform();
        current.setToIdentity();
        canvas.getCanvasPane().setTransform(current);

        if (_graphPanner != null) {
            _graphPanner.repaint();
        }
    }

    /**
     * Called when the mouse is clicked.
     * This base class does nothing when the mouse is clicked.
     * However, events _are_ handled by the components within this component.
     * @param event The mouse event.
     */
    public void mouseClicked(MouseEvent event) {
        // Implementation of the MouseMotionListener interface.
    }

    /** Transform the graph by the amount the mouse is dragged
     *  while the middle mouse button is held down.
     * @param event The drag event.
     */
    public void mouseDragged(MouseEvent event) {
        // Implementation of the MouseMotionListener interface.
        // See https://chess.eecs.berkeley.edu/bugzilla/show_bug.cgi?id=73

        if (event.isAltDown()) {
            // Only interested in middle button. (defined as the alt modifier)
            int deltaX = event.getX() - _previousMouseX;
            int deltaY = event.getY() - _previousMouseY;

            AffineTransform newTransform = getJGraph().getCanvasPane()
                    .getTransformContext().getTransform();
            newTransform.translate(deltaX, deltaY);
            getJGraph().getCanvasPane().setTransform(newTransform);

            _previousMouseX = event.getX();
            _previousMouseY = event.getY();
            event.consume();
        }
    }

    /**
     * Called when the mouse enters this component.
     * This base class does nothing when the enters this component.
     * However, events _are_ handled by the components within this component.
     * @param event The mouse event.
     */
    public void mouseEntered(MouseEvent event) {
        // Implementation of the MouseMotionListener interface.
    }

    /**
      * Called when the mouse leaves this component.
      * This base class does nothing when the exits this component.
      * However, events _are_ handled by the components within this component.
      * @param event The mouse event.
      */
    public void mouseExited(MouseEvent event) {
        // Implementation of the MouseMotionListener interface.
    }

    /** Called when the mouse is moved.
     * This base class does nothing when the mouse is moved.
     * @param event Contains details of the movement event.
     * However, events _are_ handled by the components within this component.
     */
    public void mouseMoved(MouseEvent event) {
        // Implementation of the MouseMotionListener interface.
    }

    /** Store the location of the middle mouse event.
     * @param event The mouse event.
     */
    public void mousePressed(MouseEvent event) {
        if (event.isAltDown()) {
            // Only interested in middle button. (defined as the alt modifier)
            _previousMouseX = event.getX();
            _previousMouseY = event.getY();
            event.consume();
        }
    }

    /**
      * Called when the mouse is released.
      * This base class does nothing when the mouse is moved.
      * However, events _are_ handled by the components within this component.
      * @param event The mouse event.
      */
    public void mouseReleased(MouseEvent event) {
        // Implementation of the MouseMotionListener interface.
    }

    /** Scroll in when the mouse wheel is moved.
     * @param event The mouse wheel event.
     */
    public void mouseWheelMoved(MouseWheelEvent event) {
        // Scrolling the wheel away from you zooms in. This is arbitrary and
        // should be configurable by the user.
        //
        // TODO: It would be nice to centre the zoom on where the
        // mouse is. That would mirror what apps like google earth do.

        int notches = event.getWheelRotation();
        double zoomFactor = 1.25;
        if (notches > 0) {
            zoomFactor = 1.0 / zoomFactor;
        }
        zoom(zoomFactor);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Default background color is a light grey. */
    public static final Color BACKGROUND_COLOR = new Color(0xe5e5e5);

    /** The name of the user library.  The default value is
     *  "UserLibrary".  The value of this variable is what appears
     *  in the Vergil left hand tree menu.
     *  @deprecated Use {@link ptolemy.actor.gui.UserActorLibrary#USER_LIBRARY_NAME}
     */
    public static String VERGIL_USER_LIBRARY_NAME = UserActorLibrary.USER_LIBRARY_NAME;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Initialize this BasicGraphFrame.
     * Derived classes may call this method in their constructors.
     */
    protected void _initBasicGraphFrame() {

        getModel().addChangeListener(this);

        getContentPane().setLayout(new BorderLayout());

        _rightComponent = _createRightComponent(getModel());

        ActionListener deletionListener = new ActionListener() {
            /** Delete any nodes or edges from the graph that are
             *  currently selected.  In addition, delete any edges
             *  that are connected to any deleted nodes.
             */
            public void actionPerformed(ActionEvent e) {
                delete();
            }
        };

        _rightComponent.registerKeyboardAction(deletionListener, "Delete",
                KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        _rightComponent.registerKeyboardAction(deletionListener, "BackSpace",
                KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        _rightComponent.setRequestFocusEnabled(true);

        // We used to do this, but it would result in context menus
        // getting lost on the mac.
        // _jgraph.addMouseListener(new FocusMouseListener());
        _rightComponent.setAlignmentX(1);
        _rightComponent.setAlignmentY(1);
        // Background color is parameterizable by preferences.
        Configuration configuration = getConfiguration();
        _rightComponent.setBackground(BACKGROUND_COLOR);
        if (configuration != null) {
            try {
                PtolemyPreferences preferences = PtolemyPreferences
                        .getPtolemyPreferencesWithinConfiguration(configuration);
                if (preferences != null) {
                    _rightComponent.setBackground(preferences.backgroundColor
                            .asColor());
                }
            } catch (IllegalActionException e1) {
                // Ignore the exception and use the default color.
            }
        }

        try {
            // The SizeAttribute property is used to specify the size
            // of the JGraph component. Unfortunately, with Swing's
            // mysterious and undocumented handling of component sizes,
            // there appears to be no way to control the size of the
            // JGraph from the size of the Frame, which is specified
            // by the WindowPropertiesAttribute.
            SizeAttribute size = (SizeAttribute) getModel().getAttribute(
                    "_vergilSize", SizeAttribute.class);

            if (size != null) {
                size.setSize(_rightComponent);
            } else {
                // Set the default size.
                // Note that the location is of the frame, while the size
                // is of the scrollpane.
                _rightComponent.setMinimumSize(new Dimension(200, 200));
                _rightComponent.setPreferredSize(new Dimension(600, 400));
                _rightComponent.setSize(600, 400);
            }

            // Set the zoom factor.
            Parameter zoom = (Parameter) getModel().getAttribute(
                    "_vergilZoomFactor", Parameter.class);

            if (zoom != null) {
                zoom(((DoubleToken) zoom.getToken()).doubleValue());

                // Make sure the visibility is only expert.
                zoom.setVisibility(Settable.EXPERT);
            }

            // Set the pan position.
            Parameter pan = (Parameter) getModel().getAttribute(
                    "_vergilCenter", Parameter.class);

            if (pan != null) {
                ArrayToken panToken = (ArrayToken) pan.getToken();
                Point2D center = new Point2D.Double(
                        ((DoubleToken) panToken.getElement(0)).doubleValue(),
                        ((DoubleToken) panToken.getElement(1)).doubleValue());
                setCenter(center);

                // Make sure the visibility is only expert.
                pan.setVisibility(Settable.EXPERT);
            }
        } catch (Throwable throwable) {
            // Ignore problems here.  Errors simply result in a default
            // size and location.
        }

        _rightComponent.addMouseWheelListener(this);
        _rightComponent.addMouseMotionListener(this);
        _rightComponent.addMouseListener(this);

        // If we don't have a library, we might be trying to only show
        // models
        // FIXME: should we be checking for _library instead?
        if ((CompositeEntity) configuration.getEntity("actor library") != null) {
            // Create the panner.
            _graphPanner = new JCanvasPanner(getJGraph());
            _graphPanner.setPreferredSize(new Dimension(200, 150));
            _graphPanner.setMaximumSize(new Dimension(200, 150));
            _graphPanner.setSize(200, 150);
        }

        // NOTE: Border causes all kinds of problems!
        // _graphPanner.setBorder(BorderFactory.createEtchedBorder());
        // Create the library of actors, or use the one in the entity,
        // if there is one.
        // FIXME: How do we make changes to the library persistent?
        boolean gotLibrary = false;

        try {
            LibraryAttribute libraryAttribute = (LibraryAttribute) getModel()
                    .getAttribute("_library", LibraryAttribute.class);

            if (libraryAttribute != null) {
                // The model contains a library.
                try {
                    _topLibrary = libraryAttribute.getLibrary();
                    gotLibrary = true;
                } catch (SecurityException ex) {
                    System.out.println("Warning: failed to parse "
                            + "_library attribute (running in an applet "
                            + "or sandbox always causes this)");
                }
            }
        } catch (Exception ex) {
            try {
                MessageHandler.warning("Invalid library in the model.", ex);
            } catch (CancelException e) {
            }
        }

        if (!gotLibrary) {
            try {
                if (_defaultLibrary != null) {
                    // A default library has been specified.
                    _topLibrary = _defaultLibrary.getLibrary();
                    gotLibrary = true;
                }
            } catch (SecurityException ex) {
                // Ignore, we are in an applet or sandbox.
                // We already printed a message, why print it again?
            } catch (Exception ex) {
                try {
                    // FIXME: It seems wrong to call MessageHandler here,
                    // instead, we should throw an IllegalActionException?
                    MessageHandler.warning(
                            "Invalid default library for the frame.", ex);
                } catch (CancelException e) {
                }
            }
        }

        if (!gotLibrary) {
            // Neither the model nor the argument have specified a library.
            // See if there is a default library in the configuration.
            _topLibrary = _createDefaultLibrary(getModel().workspace());
        }

        // Only include the palettePane and panner if there is an actor library.
        // The ptinyViewer configuration uses this.
        if ((CompositeEntity) configuration.getEntity("actor library") != null) {
            _libraryModel = new VisibleTreeModel(_topLibrary);
            _library = new PTree(_libraryModel);
            _library.setRootVisible(false);
            _library.setBackground(BACKGROUND_COLOR);

            // If you want to expand the top-level libraries, uncomment this.
            // Object[] path = new Object[2];
            // path[0] = _topLibrary;
            // Iterator libraries = _topLibrary.entityList().iterator();
            // while (libraries.hasNext()) {
            //     path[1] = libraries.next();
            //     _library.expandPath(new javax.swing.tree.TreePath(path));
            // }

            _libraryContextMenuCreator = new PTreeMenuCreator();
            _libraryContextMenuCreator
                    .addMenuItemFactory(new OpenLibraryMenuItemFactory());
            _libraryContextMenuCreator
                    .addMenuItemFactory(new DocumentationMenuItemFactory());
            _library.addMouseListener(_libraryContextMenuCreator);

            _libraryScrollPane = new JScrollPane(_library);
            _libraryScrollPane.setMinimumSize(new Dimension(200, 200));
            _libraryScrollPane.setPreferredSize(new Dimension(200, 200));

            // create the palette on the left.
            _palettePane = new JPanel();
            _palettePane.setBorder(null);
            _palettePane
                    .setLayout(new BoxLayout(_palettePane, BoxLayout.Y_AXIS));

            _palettePane.add(_libraryScrollPane, BorderLayout.CENTER);
            if (_graphPanner != null) {
                _palettePane.add(_graphPanner, BorderLayout.SOUTH);
            }

            _splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
            _splitPane.setLeftComponent(_palettePane);
            _splitPane.setRightComponent(_rightComponent);
            getContentPane().add(_splitPane, BorderLayout.CENTER);
        } else {
            getContentPane().add(_rightComponent, BorderLayout.CENTER);
        }

        _toolbar = new JToolBar();
        _toolbar.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
        try {
            new ToolBar(getTableau(), "toolbar", _toolbar, BorderLayout.NORTH);
        } catch (Exception e) {
            throw new InternalErrorException("Unable to create tool bar.");
        }

        GUIUtilities.addToolBarButton(_toolbar, _saveAction);

        // Note that in Top we disable Print unless the class implements
        // the Printable or Pageable interfaces.  By definition, this class
        // implements the Printable interface
        GUIUtilities.addToolBarButton(_toolbar, _printAction);

        GUIUtilities.addToolBarButton(_toolbar, _zoomInAction);
        GUIUtilities.addToolBarButton(_toolbar, _zoomResetAction);
        GUIUtilities.addToolBarButton(_toolbar, _zoomFitAction);
        GUIUtilities.addToolBarButton(_toolbar, _zoomOutAction);

        GUIUtilities.addToolBarButton(_toolbar, _openContainerAction);
        if (getModel() == getModel().toplevel()) {
            // If we are at the top level, disable
            _openContainerAction.setEnabled(false);
        }

        _cutAction = new CutAction();
        _copyAction = new CopyAction();
        _pasteAction = new PasteAction();

        // FIXME: vergil.kernel.AttributeController also defines context
        // menu choices that do the same thing.
        _moveToFrontAction = new MoveToFrontAction();
        _moveToBackAction = new MoveToBackAction();

        _editPreferencesAction = new EditPreferencesAction();

        // Add a weak reference to this to keep track of all
        // the graph frames that have been created.
        _openGraphFrames.add(this);
    }

    /** Create the menus that are used by this frame.
     */
    protected void _addMenus() {
        super._addMenus();

        _editMenu = new JMenu("Edit");
        _editMenu.setMnemonic(KeyEvent.VK_E);
        _menubar.add(_editMenu);

        // Add the undo action, followed by a separator then the editing actions
        diva.gui.GUIUtilities.addHotKey(_getRightComponent(), _undoAction);
        diva.gui.GUIUtilities.addMenuItem(_editMenu, _undoAction);
        diva.gui.GUIUtilities.addHotKey(_getRightComponent(), _redoAction);
        diva.gui.GUIUtilities.addMenuItem(_editMenu, _redoAction);
        _editMenu.addSeparator();
        GUIUtilities.addHotKey(_getRightComponent(), _cutAction);
        GUIUtilities.addMenuItem(_editMenu, _cutAction);
        GUIUtilities.addHotKey(_getRightComponent(), _copyAction);
        GUIUtilities.addMenuItem(_editMenu, _copyAction);
        GUIUtilities.addHotKey(_getRightComponent(), _pasteAction);
        GUIUtilities.addMenuItem(_editMenu, _pasteAction);

        _editMenu.addSeparator();

        GUIUtilities.addHotKey(_getRightComponent(), _moveToBackAction);
        GUIUtilities.addMenuItem(_editMenu, _moveToBackAction);
        GUIUtilities.addHotKey(_getRightComponent(), _moveToFrontAction);
        GUIUtilities.addMenuItem(_editMenu, _moveToFrontAction);

        _editMenu.addSeparator();
        GUIUtilities.addMenuItem(_editMenu, _editPreferencesAction);

        // Hot key for configure (edit parameters).
        GUIUtilities.addHotKey(_getRightComponent(),
                BasicGraphController._configureAction);

        // May be null if there are not multiple views in the configuration.
        if (_viewMenu == null) {
            _viewMenu = new JMenu("View");
            _viewMenu.setMnemonic(KeyEvent.VK_V);
            _menubar.add(_viewMenu);
        } else {
            _viewMenu.addSeparator();
        }

        GUIUtilities.addHotKey(_getRightComponent(), _zoomInAction);
        GUIUtilities.addMenuItem(_viewMenu, _zoomInAction);
        GUIUtilities.addHotKey(_getRightComponent(), _zoomResetAction);
        GUIUtilities.addMenuItem(_viewMenu, _zoomResetAction);
        GUIUtilities.addHotKey(_getRightComponent(), _zoomFitAction);
        GUIUtilities.addMenuItem(_viewMenu, _zoomFitAction);
        GUIUtilities.addHotKey(_getRightComponent(), _zoomOutAction);
        GUIUtilities.addMenuItem(_viewMenu, _zoomOutAction);
    }

    /** Return true if any element of the specified list is implied.
     *  An element is implied if its getDerivedLevel() method returns
     *  anything smaller than Integer.MAX_VALUE.
     *  @param elements A list of instances of NamedObj.
     *  @return True if any element in the list is implied.
     *  @see NamedObj#getDerivedLevel()
     */
    protected boolean _checkForImplied(List<NamedObj> elements) {
        Iterator<NamedObj> elementIterator = elements.iterator();

        while (elementIterator.hasNext()) {
            NamedObj element = elementIterator.next();

            if (element.getDerivedLevel() < Integer.MAX_VALUE) {
                MessageHandler.error("Cannot change the position of "
                        + element.getFullName()
                        + " because the position is set by the class.");
                return true;
            }
        }

        return false;
    }

    /** Override the base class to remove the listeners we have
     *  created when the frame closes.  Specifically,
     *  remove our panner-updating listener from the entity.
     *  Also remove the listeners our graph model has created.
     *  @return True if the close completes, and false otherwise.
     */
    protected boolean _close() {
        boolean result = super._close();

        if (result) {
            getModel().removeChangeListener(this);

            AbstractBasicGraphModel graphModel = _getGraphModel();
            graphModel.removeListeners();
        }

        return result;
    }

    /** Create the default library to use if an entity has no
     *  LibraryAttribute.  Note that this is called in the
     *  constructor and therefore overrides in subclasses
     *  should not refer to any members that may not have been
     *  initialized. If no library is found in the configuration,
     *  then an empty one is created in the specified workspace.
     *  @param workspace The workspace in which to create
     *   the library, if one needs to be created.
     *  @return The new library, or null if there is no
     *   configuration.
     */
    protected CompositeEntity _createDefaultLibrary(Workspace workspace) {
        Configuration configuration = getConfiguration();

        if (configuration != null) {
            CompositeEntity result = (CompositeEntity) configuration
                    .getEntity("actor library");

            if (result == null) {
                // Create an empty library by default.
                result = new CompositeEntity(workspace);

                try {
                    result.setName("topLibrary");

                    // Put a marker in so that this is
                    // recognized as a library.
                    new Attribute(result, "_libraryMarker");
                } catch (Exception ex) {
                    throw new InternalErrorException(
                            "Library configuration failed: " + ex);
                }
            }

            return result;
        } else {
            return null;
        }
    }

    /** Create the items in the File menu. A null element in the array
     *  represents a separator in the menu.
     *  In this class, if the configuration contains a StringParmeter
     *  with the name "_exportPDFActionClassName", the then value of
     *  that parameter is assumed to name a class that extends AbstractAction
     *  and that export PDF.
     *  If there is no parameter by that name, then value returned
     *  by super._createFileMenuItems is returned.
     *  @return The items in the File menu, optionally include an "Export PDF"
     *  menu choice.
     */
    protected JMenuItem[] _createFileMenuItems() {
        JMenuItem[] fileMenuItems = super._createFileMenuItems();
        try {
            // FIXME: this does not work because the configuration is
            // not set when this method is called.
            Configuration configuration = getConfiguration();
            if (configuration == null) {
                //new Exception("BSF: configuration is null").printStackTrace();
            } else {
                StringParameter exportPDFActionClassNameParameter = (StringParameter) configuration
                        .getAttribute("_exportPDFActionClassName",
                                StringParameter.class);

                if (exportPDFActionClassNameParameter == null) {
                    // Parameter not set, so just return
                    return fileMenuItems;
                }

                if (_exportPDFAction == null) {
                    String exportPDFActionClassName = exportPDFActionClassNameParameter
                            .stringValue();
                    try {
                        Class exportPDFActionClass = Class
                                .forName(exportPDFActionClassName);
                        Constructor exportPDFActionConstructor = exportPDFActionClass
                                .getDeclaredConstructor(BasicGraphFrame.class);
                        _exportPDFAction = (AbstractAction) exportPDFActionConstructor
                                .newInstance(this);
                        System.out.println("BGF: _exportPDFAction: "
                                + _exportPDFAction);
                    } catch (Throwable throwable) {
                        new InternalErrorException(
                                null,
                                throwable,
                                "Failed to construct export PDF class \""
                                        + exportPDFActionClassName
                                        + "\", which was read from the configuration.");
                    }
                }
            }
        } catch (Exception ex) {
            throw new InternalErrorException(null, ex,
                    "Failed to read _exportPDFActionName from the Configuration");
        }

        // Uncomment the next block to have Export PDF *ALWAYS* enabled.
        // We don't want it always enabled because ptiny, the applets and
        // Web Start should not included this AGPL'd piece of software

        // if (_exportPDFAction == null) {
        //     //String exportPDFActionClassName = exportPDFActionClassNameParameter.stringValue();
        //     String exportPDFActionClassName = "ptolemy.vergil.basic.itextpdf.ExportPDFAction";
        //     try {
        //         Class exportPDFActionClass = Class
        //                 .forName(exportPDFActionClassName);
        //         Constructor exportPDFActionConstructor = exportPDFActionClass
        //                 .getDeclaredConstructor(BasicGraphFrame.class);
        //         _exportPDFAction = (AbstractAction) exportPDFActionConstructor
        //                 .newInstance(this);
        //     } catch (Throwable throwable) {
        //         new InternalErrorException(null, throwable,
        //                 "Failed to construct export PDF class \""
        //                         + exportPDFActionClassName
        //                         + "\", which was read from the configuration.");
        //     }
        // }
        // End of block to uncomment.

        if (_exportPDFAction != null) {
            // Insert the Export PDF item after the Print item in the menu.
            int i = 0;
            for (JMenuItem item : fileMenuItems) {
                i++;
                if (item.getActionCommand().equals("Print")) {
                    // Add a Export PDF item here.
                    JMenuItem exportItem = new JMenuItem(_exportPDFAction);
                    JMenuItem[] newItems = new JMenuItem[fileMenuItems.length + 1];
                    System.arraycopy(fileMenuItems, 0, newItems, 0, i);
                    newItems[i] = exportItem;
                    System.arraycopy(fileMenuItems, i, newItems, i + 1,
                            fileMenuItems.length - i);
                    return newItems;
                }
            }
        }
        return fileMenuItems;
    }

    /** Create a new graph pane.  Subclasses will override this to change
     *  the pane that is created.  Note that this method is called in
     *  constructor, so derived classes must be careful to not reference
     *  local variables that may not have yet been created.
     *  @param entity The object to be displayed in the pane.
     *  @return The pane that is created.
     */
    protected abstract GraphPane _createGraphPane(NamedObj entity);

    /** Create the component that goes to the right of the library.
     *  @param entity The entity to display in the component.
     *  @return The component that goes to the right of the library.
     */
    protected JComponent _createRightComponent(NamedObj entity) {
        GraphPane pane = _createGraphPane(entity);
        pane.getForegroundLayer().setPickHalo(2);
        pane.getForegroundEventLayer().setConsuming(false);
        pane.getForegroundEventLayer().setEnabled(true);
        pane.getForegroundEventLayer().addLayerListener(new LayerAdapter() {
            /** Invoked when the mouse is pressed on a layer
             * or figure.
             */
            public void mousePressed(LayerEvent event) {
                Component component = event.getComponent();

                if (!component.hasFocus()) {
                    component.requestFocus();
                }
            }
        });

        setJGraph(new JGraph(pane));
        _dropTarget = new EditorDropTarget(_jgraph);
        return _jgraph;
    }

    /** Create a SizeAttribute for the current model when it is being saved to
     *  a file. The size recorded in the SizeAttribute is the size of the
     *  current canvas.
     *  @return The SizeAttribute.
     *  @exception IllegalActionException If "_vergilSize" is found but is not
     *  an instance of SizeAttribute, or if a SizeAttribute is not accepted by
     *  the current model.
     *  @exception NameDuplicationException If the name "_vergilSize" is already
     *  used when trying to create the SizeAttribute.
     */
    protected SizeAttribute _createSizeAttribute()
            throws IllegalActionException, NameDuplicationException {
        // Have to also record the size of the JGraph because
        // setting the size of the frame is ignored if we don't
        // also set the size of the JGraph. Why? Who knows. Swing.
        SizeAttribute size = (SizeAttribute) getModel().getAttribute(
                "_vergilSize", SizeAttribute.class);

        if (size == null) {
            size = new SizeAttribute(getModel(), "_vergilSize");
        }

        size.recordSize(_getRightComponent());
        return size;
    }

    /** Export the model into the writer with the given name. If
     *  {@link ptolemy.vergil.basic#_exportSelectedObjectsOnly} is set
     *  to true, then only the selected named objects are exported;
     *  otherwise, the whole model is exported with its exportMoML()
     *  method.
     *
     *  @param writer The writer.
     *  @param model The model to export.
     *  @param name The name of the exported model.
     *  @exception IOException If an I/O error occurs.
     */
    protected void _exportDesignPattern(Writer writer, NamedObj model,
            String name) throws IOException {
        if (_query != null && _query.hasEntry("selected")
                && _query.getBooleanValue("selected")) {
            try {
                model.workspace().getReadAccess();
                String elementName = model.getElementName();
                writer.write("<?xml version=\"1.0\" standalone=\"no\"?>\n"
                        + "<!DOCTYPE " + elementName + " PUBLIC "
                        + "\"-//UC Berkeley//DTD MoML 1//EN\"\n"
                        + "    \"http://ptolemy.eecs.berkeley.edu"
                        + "/xml/dtd/MoML_1.dtd\">\n");

                writer.write("<" + elementName + " name=\"" + name
                        + "\" class=\"" + model.getClassName() + "\"");

                if (model.getSource() != null) {
                    writer.write(" source=\"" + model.getSource() + "\">\n");
                } else {
                    writer.write(">\n");
                }

                String[] attributeNames = { "_alternateGetMomlAction",
                        "_designPatternIcon", "_transformationBefore",
                        "_transformationAfter" };
                for (String attributeName : attributeNames) {
                    Attribute attribute = model.getAttribute(attributeName);
                    if (attribute != null) {
                        attribute.exportMoML(writer, 1);
                    }
                }

                HashSet<NamedObj> namedObjSet = _getSelectionSet();
                NamedObj container = (NamedObj) _getGraphModel().getRoot();
                Iterator<NamedObj> elements = container.sortContainedObjects(
                        namedObjSet).iterator();
                while (elements.hasNext()) {
                    elements.next().exportMoML(writer, 1);
                }

                if (model instanceof CompositeEntity) {
                    writer.write(((CompositeEntity) model).exportLinks(1,
                            namedObjSet));
                }

                writer.write("</" + elementName + ">\n");
            } finally {
                model.workspace().doneReading();
            }
        } else {
            if (model.getContainer() != null) {
                writer.write("<?xml version=\"1.0\" standalone=\"no\"?>\n"
                        + "<!DOCTYPE " + model.getElementName() + " PUBLIC "
                        + "\"-//UC Berkeley//DTD MoML 1//EN\"\n"
                        + "    \"http://ptolemy.eecs.berkeley.edu"
                        + "/xml/dtd/MoML_1.dtd\">\n");
            }
            model.exportMoML(writer, 0, name);
        }
    }

    /** Finish exporting a design pattern.
     */
    protected void _finishExportDesignPattern() {
    }

    /** Get the directory that was last accessed by this window.
     *  @see #_setDirectory
     *  @return The directory last accessed.
     */
    protected File _getDirectory() {
        // NOTE: This method is necessary because we wish to have
        // this accessed by inner classes, and there is a bug in
        // jdk1.2.2 where inner classes cannot access protected
        // static members.
        return _directory;
    }

    /** Return the graph controller associated with this frame.
     *  @return The graph controller associated with this frame.
     */
    protected GraphController _getGraphController() {
        GraphPane graphPane = getJGraph().getGraphPane();
        return graphPane.getGraphController();
    }

    /** Return the graph model associated with this frame.
     *  @return The graph model associated with this frame.
     */
    protected AbstractBasicGraphModel _getGraphModel() {
        GraphController controller = _getGraphController();
        return (AbstractBasicGraphModel) controller.getGraphModel();
    }

    /** Return the right component on which graph editing occurs.
     *  @return The JGraph on which graph editing occurs.
     */
    protected JComponent _getRightComponent() {
        return _rightComponent;
    }

    /** Return a set of instances of NamedObj representing the objects
     *  that are currently selected.  This set has no particular order
     *  to it. If you need the selection objects in proper order, as
     *  defined by the container, then call sortContainedObjects()
     *  on the container to sort the result.
     *  @return The set of selected objects.
     */
    protected HashSet<NamedObj> _getSelectionSet() {
        GraphController controller = _getGraphController();
        GraphModel graphModel = controller.getGraphModel();
        SelectionModel model = controller.getSelectionModel();
        Object[] selection = model.getSelectionAsArray();

        // A set, because some objects may represent the same
        // ptolemy object.
        HashSet<NamedObj> namedObjSet = new HashSet<NamedObj>();
        HashSet<Object> nodeSet = new HashSet<Object>();

        // First get all the nodes.
        for (int i = 0; i < selection.length; i++) {
            if (selection[i] instanceof Figure) {
                Object userObject = ((Figure) selection[i]).getUserObject();

                if (graphModel.isNode(userObject)) {
                    NamedObj actual = (NamedObj) graphModel
                            .getSemanticObject(userObject);
                    //System.out.println("BasicGraphFrame._getSelectionSet() actual: " + actual.getClass().getName());
                    //if ( !(actual instanceof PortParameter)) { 
                    nodeSet.add(userObject);
                    namedObjSet.add(actual);
                    //}
                }
            }
        }

        for (int i = 0; i < selection.length; i++) {
            if (selection[i] instanceof Figure) {
                Object userObject = ((Figure) selection[i]).getUserObject();

                if (graphModel.isEdge(userObject)) {
                    // Check to see if the head and tail are both being
                    // copied.  Only if so, do we actually take the edge.
                    Object head = graphModel.getHead(userObject);
                    Object tail = graphModel.getTail(userObject);
                    boolean headOK = nodeSet.contains(head);
                    boolean tailOK = nodeSet.contains(tail);
                    Iterator<Object> objects = nodeSet.iterator();

                    while (!(headOK && tailOK) && objects.hasNext()) {
                        Object object = objects.next();

                        if (!headOK
                                && GraphUtilities.isContainedNode(head, object,
                                        graphModel)) {
                            headOK = true;
                        }

                        if (!tailOK
                                && GraphUtilities.isContainedNode(tail, object,
                                        graphModel)) {
                            tailOK = true;
                        }
                    }

                    if (headOK && tailOK) {
                        // Add the relation.
                        NamedObj actual = (NamedObj) graphModel
                                .getSemanticObject(userObject);
                        namedObjSet.add(actual);
                    }
                }
            }
        }

        return namedObjSet;
    }

    /** Return true if this is a design pattern.
     *  @return true if the model corresponding to this object
     *  has a DesignPatternIcon attribute.
     */
    protected boolean _isDesignPattern() {
        NamedObj model = getModel();
        return !model.attributeList(DesignPatternIcon.class).isEmpty();
    }

    /** Prepare to export a design pattern.
     *  In this base class, do nothing.
     */
    protected void _prepareExportDesignPattern() {
    }

    /** Create and return a file dialog for the "Save As" command.
     *  This overrides the base class to add options to the dialog.
     *  @return A file dialog for save as.
     */
    protected JFileChooser _saveAsFileDialog() {
        JFileChooser fileDialog = super._saveAsFileDialog();

        if (_isDesignPattern()) {
            if (_getSelectionSet().isEmpty()) {
                fileDialog.setAccessory(null);
            } else {
                _query = new Query();
                _query.addCheckBox("selected", "Selected objects only", true);
                fileDialog.setAccessory(_query);
            }
        }

        return fileDialog;
    }

    /** Set the directory that was last accessed by this window.
     *  @see #_getDirectory
     *  @param directory The directory last accessed.
     */
    protected void _setDirectory(File directory) {
        // NOTE: This method is necessary because we wish to have
        // this accessed by inner classes, and there is a bug in
        // jdk1.2.2 where inner classes cannot access protected
        // static members.
        _directory = directory;
    }

    /** Enable or disable drop into.
     *  @param enable False to disable.
     */
    protected void _setDropIntoEnabled(boolean enable) {
        _dropTarget.setDropIntoEnabled(enable);
    }

    /** Write the model to the specified file.  This overrides the base
     *  class to record the current size and position of the window
     *  in the model.
     *  @param file The file to write to.
     *  @exception IOException If the write fails.
     */
    protected void _writeFile(File file) throws IOException {
        // First, record size and position.
        try {
            // Record the position of the top-level frame, assuming
            // there is one.
            Component component = _getRightComponent().getParent();
            Component parent = component.getParent();

            while ((parent != null) && !(parent instanceof Frame)) {
                component = parent;
                parent = component.getParent();
            }

            // If there is no parent that is a Frame, do nothing.
            // We know that: (parent == null) || (parent instanceof Frame)
            if (parent != null) {
                WindowPropertiesAttribute properties = (WindowPropertiesAttribute) getModel()
                        .getAttribute("_windowProperties",
                                WindowPropertiesAttribute.class);

                if (properties == null) {
                    properties = new WindowPropertiesAttribute(getModel(),
                            "_windowProperties");
                }

                properties.recordProperties((Frame) parent);
            }

            _createSizeAttribute();

            // Also record zoom and pan state.
            JCanvas canvas = getJGraph().getGraphPane().getCanvas();
            AffineTransform current = canvas.getCanvasPane()
                    .getTransformContext().getTransform();

            // We assume the scaling in the X and Y directions are the same.
            double scale = current.getScaleX();
            Parameter zoom = (Parameter) getModel().getAttribute(
                    "_vergilZoomFactor", Parameter.class);

            if (zoom == null) {
                // NOTE: This will not propagate.
                zoom = new ExpertParameter(getModel(), "_vergilZoomFactor");
            }

            zoom.setToken(new DoubleToken(scale));

            // Make sure the visibility is only expert.
            zoom.setVisibility(Settable.EXPERT);

            // Save the center, to record the pan state.
            Point2D center = getCenter();
            Parameter pan = (Parameter) getModel().getAttribute(
                    "_vergilCenter", Parameter.class);

            if (pan == null) {
                // NOTE: This will not propagate.
                pan = new ExpertParameter(getModel(), "_vergilCenter");
            }

            Token[] centerArray = new Token[2];
            centerArray[0] = new DoubleToken(center.getX());
            centerArray[1] = new DoubleToken(center.getY());
            pan.setToken(new ArrayToken(centerArray));

            // Make sure the visibility is only expert.
            pan.setVisibility(Settable.EXPERT);
        } catch (Throwable throwable) {
            // Ignore problems here.  Errors simply result in a default
            // size and location.
        }

        if (_isDesignPattern()) {
            FileWriter fileWriter = null;

            try {
                fileWriter = new FileWriter(file);
                String name = getModel().getName();
                String filename = file.getName();
                int period = filename.indexOf(".");
                if (period > 0) {
                    name = filename.substring(0, period);
                } else {
                    name = filename;
                }
                _exportDesignPattern(fileWriter, getModel(), name);
            } finally {
                if (fileWriter != null) {
                    fileWriter.close();
                }
            }
        } else {
            super._writeFile(file);
        }
    }

    /** Return the MoML to delete the specified selection objects.
     *  This has the side effect of unselecting the objects. It also
     *  deletes edges that are not fully connected (these deletions
     *  cannot be done through MoML, and cannot be undone).
     *  @param graphModel The graph model.
     *  @param selection The selection.
     *  @param model The selection model.
     *  @return The MoML to delete the selected objects.
     */
    protected StringBuffer _deleteMoML(AbstractBasicGraphModel graphModel,
            Object[] selection, SelectionModel model) {

        // First collect selected objects into the userObjects array
        // and deselect them.
        Object[] userObjects = new Object[selection.length];
        for (int i = 0; i < selection.length; i++) {
            userObjects[i] = ((Figure) selection[i]).getUserObject();
            model.removeSelection(selection[i]);
        }

        // Create a set to hold those elements whose deletion
        // does not go through MoML. This is only links that
        // are not connected to another port or a relation.
        HashSet<Object> edgeSet = new HashSet<Object>();

        StringBuffer moml = new StringBuffer("<group>\n");

        // Delete edges then nodes, since deleting relations may
        // result in deleting links to that relation.
        for (int i = 0; i < selection.length; i++) {
            Object userObject = userObjects[i];

            if (graphModel.isEdge(userObject)) {
                NamedObj actual = (NamedObj) graphModel
                        .getSemanticObject(userObject);

                // If there is no semantic object, then this edge is
                // not fully connected, so we can't go through MoML.
                if (actual == null) {
                    edgeSet.add(userObject);
                } else {
                    moml.append(graphModel.getDeleteEdgeMoML(userObject));
                }
            }
        }

        // First, delete all the non-attributes.
        // This helps avoid deleting properties such as top level parameters
        // upon which the entities depend.
        // FIXME: what if we have a parameter that is used by both the selection
        // and the other parts of the model?
        for (int i = 0; i < selection.length; i++) {
            Object userObject = userObjects[i];

            NamedObjNodeModel namedObjNodeModel = (NamedObjNodeModel) graphModel
                    .getNodeModel(userObject);
            if (graphModel.isNode(userObject)
                    && !(namedObjNodeModel instanceof AttributeNodeModel)) {
                NamedObj actual = (NamedObj) graphModel
                        .getSemanticObject(userObject);
                if (!(actual instanceof ParameterPort)) {
                    // We don't delete ParameterPorts here because if
                    // we drag a region around a ParmeterPort, then
                    // both the PortParameter and the ParameterPort
                    // are selected.  Deleting both results in an
                    // error.  If we just click (not drag) on a
                    // ParameterPort, then the PortParameter is only
                    // selected and deletion work ok.  See
                    // https://chess.eecs.berkeley.edu/bugzilla/show_bug.cgi?id=311
                    moml.append(graphModel.getDeleteNodeMoML(userObject));
                }
            }
        }

        // Now delete attributes.
        for (int i = 0; i < selection.length; i++) {
            Object userObject = userObjects[i];

            NamedObjNodeModel namedObjNodeModel = (NamedObjNodeModel) graphModel
                    .getNodeModel(userObject);
            if (graphModel.isNode(userObject)
                    && namedObjNodeModel instanceof AttributeNodeModel) {
                moml.append(graphModel.getDeleteNodeMoML(userObject));
            }
        }

        moml.append("</group>\n");

        // Have both MoML to perform deletion and set of objects whose
        // deletion does not go through MoML. This set of objects
        // should be very small and so far consists of only links that are not
        // connected to a relation
        try {
            // First manually delete any objects whose deletion does not go
            // through MoML and so are not undoable
            // Note that we turn off event dispatching so that each individual
            // removal does not trigger graph redrawing.
            graphModel.setDispatchEnabled(false);

            Iterator<Object> edges = edgeSet.iterator();

            while (edges.hasNext()) {
                Object nextEdge = edges.next();

                if (graphModel.isEdge(nextEdge)) {
                    graphModel.disconnectEdge(this, nextEdge);
                }
            }
        } finally {
            graphModel.setDispatchEnabled(true);
        }

        return moml;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The default Library. **/
    protected LibraryAttribute _defaultLibrary;

    /** The cut action. */
    protected Action _cutAction;

    /** The copy action. */
    protected Action _copyAction;

    /** The edit menu. */
    protected JMenu _editMenu;

    /** The action to edit preferences. */
    protected EditPreferencesAction _editPreferencesAction;

    /** The export to PDF action. */
    protected Action _exportPDFAction;

    /** The panner. */
    protected JCanvasPanner _graphPanner;

    /** The library display widget. */
    protected JTree _library;

    /** The library context menu creator. */
    protected PTreeMenuCreator _libraryContextMenuCreator;

    /** The library model. */
    protected EntityTreeModel _libraryModel;

    /** The library scroll pane. */
    protected JScrollPane _libraryScrollPane;

    /** Action to move to the back. */
    protected MoveToBackAction _moveToBackAction;

    /** Action to move to the front. */
    protected MoveToFrontAction _moveToFrontAction;

    /** The library display panel. */
    protected JPanel _palettePane;

    /** The paste action. */
    protected Action _pasteAction;

    /** The split pane for library and editor. */
    protected JSplitPane _splitPane;

    /** The toolbar. */
    protected JToolBar _toolbar;

    /** The library. */
    protected CompositeEntity _topLibrary;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The instance of EditorDropTarget associated with the JGraph. */
    protected EditorDropTarget _dropTarget;

    /** The instance of JGraph for this editor. */
    protected JGraph _jgraph;

    /** Action for opening the container, moving uplevel. */
    private Action _openContainerAction = new OpenContainerAction(
            "Open the container");

    /** List of references to graph frames that are open. */
    protected static LinkedList<BasicGraphFrame> _openGraphFrames = new LinkedList<BasicGraphFrame>();

    /** X coordinate of where we last processed a press or drag of the
     *  middle mouse button.
     */
    private int _previousMouseX = 0;

    /** Y coordinate of where we last processed a press or drag of the
     *  middle mouse button.
     */
    private int _previousMouseY = 0;

    /**  Action to print the model. */
    private Action _printAction = new PrintAction("Print");

    /** Action to redo the last undone MoML change. */
    private Action _redoAction = new RedoAction();

    /** The right component for this editor. */
    protected JComponent _rightComponent;

    /**  Action to save the model. */
    private Action _saveAction = new SaveAction("Save");

    /** Action to undo the last MoML change. */
    private Action _undoAction = new UndoAction();

    /** Action for zooming in. */
    protected Action _zoomInAction = new ZoomInAction("Zoom In");

    /** Action for zoom reset. */
    protected Action _zoomResetAction = new ZoomResetAction("Zoom Reset");

    /** Action for zoom fitting. */
    protected Action _zoomFitAction = new ZoomFitAction("Zoom Fit");

    /** Action for zooming out. */
    protected Action _zoomOutAction = new ZoomOutAction("Zoom Out");

    ///////////////////////////////////////////////////////////////////
    ////                     private inner classes                 ////

    ///////////////////////////////////////////////////////////////////
    //// CopyAction

    /** Action to copy the current selection. */
    protected class CopyAction extends AbstractAction {
        /** Create a new action to copy the current selection. */
        public CopyAction() {
            super("Copy");
            putValue("tooltip",
                    "Copy the current selection onto the clipboard.");
            putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_C, Toolkit.getDefaultToolkit()
                            .getMenuShortcutKeyMask()));
            putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_C));
        }

        /** Copy the current selection. */
        public void actionPerformed(ActionEvent e) {
            copy();
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// CutAction

    /** Action to copy and delete the current selection. */
    protected class CutAction extends AbstractAction {
        /** Create a new action to copy and delete the current selection. */
        public CutAction() {
            super("Cut");
            putValue("tooltip", "Cut the current selection onto the clipboard.");
            putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_X, Toolkit.getDefaultToolkit()
                            .getMenuShortcutKeyMask()));
            putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_T));
        }

        /** Copy and delete the current selection. */
        public void actionPerformed(ActionEvent e) {
            cut();
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// DocumentationMenuItemFactory

    /**
     *  Create a menu item that will show documentation
     */
    private class DocumentationMenuItemFactory implements MenuItemFactory {
        /**
         * Add an item to the given context menu that bring up the
         * documentation for the given object
         */
        public JMenuItem create(final JContextMenu menu, final NamedObj object) {
            Action action = new GetDocumentationAction() {
                public void actionPerformed(ActionEvent e) {
                    Configuration configuration = getConfiguration();
                    setConfiguration(configuration);
                    super.actionPerformed(e);
                }
            };

            action.putValue("tooltip", "Get Documentation.");
            action.putValue(diva.gui.GUIUtilities.MNEMONIC_KEY,
                    Integer.valueOf(KeyEvent.VK_D));
            return menu.add(action, (String) action.getValue(Action.NAME));
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// EditPreferencesAction

    /** Action to edit the preferences.
     */
    protected class EditPreferencesAction extends AbstractAction {
        public EditPreferencesAction() {
            super("Edit Preferences");
            putValue("tooltip", "Change the Vergil preferences");
            putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_E));
        }

        public void actionPerformed(ActionEvent e) {
            Configuration configuration = getConfiguration();
            PtolemyPreferences preferences = null;

            try {
                preferences = (PtolemyPreferences) configuration.getAttribute(
                        PtolemyPreferences.PREFERENCES_WITHIN_CONFIGURATION,
                        PtolemyPreferences.class);
            } catch (IllegalActionException ex) {
                MessageHandler.error("Preferences attribute found, "
                        + "but not of the right class.", ex);
            }

            if (preferences == null) {
                MessageHandler
                        .message("No preferences given in the configuration.");
            } else {
                // Open a modal dialog to edit the parameters.
                new EditParametersDialog(BasicGraphFrame.this, preferences,
                        "Edit Ptolemy Preferences");

                // Make the current global variables conform with the
                // new values.
                try {
                    preferences.setAsDefault();
                } catch (IllegalActionException ex) {
                    MessageHandler.error("Invalid expression.", ex);
                    actionPerformed(e);
                }

                // If any parameter has changed, all open vergil
                // windows need to be notified.
                Iterator<BasicGraphFrame> frames = _openGraphFrames.iterator();

                while (frames.hasNext()) {
                    BasicGraphFrame frame = frames.next();
                    GraphModel graphModel = frame._getGraphController()
                            .getGraphModel();
                    graphModel
                            .dispatchGraphEvent(new GraphEvent(this,
                                    GraphEvent.STRUCTURE_CHANGED, graphModel
                                            .getRoot()));

                    if (frame._graphPanner != null) {
                        frame._graphPanner.repaint();
                    }
                }

                // Make the changes persistent.
                try {
                    preferences.save();
                } catch (IOException ex) {
                    try {
                        MessageHandler.warning("Failed to save preferences.",
                                ex);
                    } catch (CancelException e1) {
                        // Ignore cancel.
                    }
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// ElementInLinkType
    /**
     * An enumerate to specifies what kind of element the element (head or tail) is in a link.
     */
    private enum ElementInLinkType {
        PORT_IN_ACTOR, STANDALONE_PORT, RELATION
    }

    ///////////////////////////////////////////////////////////////////
    //// ExecuteSystemAction

    /** An action to open a run control window. */
    //    private class ExecuteSystemAction extends AbstractAction {
    //        /** Construct an action to execute the model. */
    //        public ExecuteSystemAction() {
    //            super("Go");
    //            putValue("tooltip", "Execute The Model");
    //            putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
    //                    KeyEvent.VK_G, Toolkit.getDefaultToolkit()
    //                            .getMenuShortcutKeyMask()));
    //            putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_G));
    //        }
    //
    //        /** Open a run control window. */
    //        public void actionPerformed(ActionEvent e) {
    //            try {
    //                PtolemyEffigy effigy = (PtolemyEffigy) getTableau()
    //                        .getContainer();
    //                new RunTableau(effigy, effigy.uniqueName("tableau"));
    //            } catch (Exception ex) {
    //                MessageHandler.error("Execution Failed", ex);
    //            }
    //        }
    //    }

    ///////////////////////////////////////////////////////////////////
    //// LinkElementProperties
    /**
     * A class that keeps stores basic properties of element (head, tail) in a link
     */
    static private class LinkElementProperties {
        /**
         * Create a LinkElementProperties from the element (head or tail), a port if one is available and the ElementInLinkType
         */
        LinkElementProperties(Object element, IOPort port,
                ElementInLinkType type) {
            this.element = element;
            this.port = port;
            this.type = type;
        }

        /**
         * Extract the properties from an element (head or tail) in a link a return these as an ElementInLinkType
         */
        static LinkElementProperties extractLinkProperties(Object element) {
            IOPort elementPort = null;
            ElementInLinkType elementType = ElementInLinkType.PORT_IN_ACTOR;
            if (element instanceof IOPort) {
                //This is a port of an actor
                elementPort = (IOPort) element;
                elementType = ElementInLinkType.PORT_IN_ACTOR;
            } else if (element instanceof Location) {
                //Either a port (not one of an actor) or a relation
                NamedObj elementContainer = ((Location) element).getContainer();
                if (elementContainer instanceof IOPort) {
                    //This is a port
                    elementPort = (IOPort) elementContainer;
                    elementType = ElementInLinkType.STANDALONE_PORT;
                } else {
                    //This is a relation
                    assert elementContainer instanceof IORelation;
                    elementType = ElementInLinkType.RELATION;
                }
            }
            return new LinkElementProperties(element, elementPort, elementType);
        }

        public final Object element;
        public final IOPort port;
        public final ElementInLinkType type;
    }

    ///////////////////////////////////////////////////////////////////
    //// MoveToBackAction
    /** Action to move the current selection to the back (which corresponds
     *  to first in the ordered list).
     */
    protected class MoveToBackAction extends AbstractAction {
        public MoveToBackAction() {
            // Note that we also have "Send to Back" in
            // vergil/kernel/AttributeController.java
            super("Send to Back");
            putValue("tooltip", "Send to back of like objects");
            putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_B, Toolkit.getDefaultToolkit()
                            .getMenuShortcutKeyMask()));
            putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_B));
        }

        public void actionPerformed(ActionEvent e) {
            final NamedObj container = (NamedObj) _getGraphModel().getRoot();

            // Get the selection objects.
            // NOTE: The order in the model must be respected.
            HashSet<NamedObj> namedObjSet = _getSelectionSet();
            final List<NamedObj> elements = container
                    .sortContainedObjects(namedObjSet);

            // Return if any is a derived object.
            if (_checkForImplied(elements)) {
                return;
            }

            // Issue a change request, since this requires write access.
            ChangeRequest request = new ChangeRequest(container, "Send to back") {
                protected void _execute() throws IllegalActionException {
                    MoveAction.move(elements, MoveAction.TO_FIRST, container);
                }
            };

            container.requestChange(request);
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// MoveToFrontAction

    /** Action to move the current selection to the back (which corresponds
     *  to first in the ordered list).
     */
    protected class MoveToFrontAction extends AbstractAction {
        public MoveToFrontAction() {
            // Note that we also have "Bring to Front" in
            // vergil/kernel/AttributeController.java
            super("Bring to Front");
            putValue("tooltip", "Bring to front of like objects");
            putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_F, Toolkit.getDefaultToolkit()
                            .getMenuShortcutKeyMask()));
            putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_F));
        }

        public void actionPerformed(ActionEvent e) {
            final NamedObj container = (NamedObj) _getGraphModel().getRoot();

            // Get the selection objects.
            // NOTE: The order in the model must be respected.
            HashSet<NamedObj> namedObjSet = _getSelectionSet();
            final List<NamedObj> elements = container
                    .sortContainedObjects(namedObjSet);

            // Return if any is a derived object.
            if (_checkForImplied(elements)) {
                return;
            }

            // Issue a change request, since this requires write access.
            ChangeRequest request = new ChangeRequest(container,
                    "Bring to front") {
                protected void _execute() throws IllegalActionException {
                    MoveAction.move(elements, MoveAction.TO_LAST, container);
                }
            };

            container.requestChange(request);
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// PasteAction

    /** Paste the current contents of the clipboard into the current model. */
    protected class PasteAction extends AbstractAction {
        /** Create a new action to paste the current contents of the
         *  clipboard into the current model.
         */
        public PasteAction() {
            super("Paste");
            putValue("tooltip", "Paste the contents of the clipboard.");
            putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_V, Toolkit.getDefaultToolkit()
                            .getMenuShortcutKeyMask()));
            putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_P));
        }

        /** Paste the current contents of the clipboard into
         *  the current model.
         */
        public void actionPerformed(ActionEvent e) {
            paste();
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// PtolemyLayout

    /** A layout algorithm for laying out ptolemy graphs.  Since our edges
     *  are undirected, this layout algorithm turns them into directed edges
     *  aimed consistently. i.e. An edge should always be "out" of an
     *  internal output port and always be "in" of an internal input port.
     *  Conversely, an edge is "out" of an external input port, and "in" of
     *  an external output port.  The copying operation also flattens
     *  the graph, because the level layout algorithm doesn't understand
     *  how to layout hierarchical nodes.
     */
    private static class PtolemyLayout extends LevelLayout {
        // FIXME: input ports should be on left, and output ports on right.

        /** Construct a new levelizing layout with a vertical orientation. */
        public PtolemyLayout(LayoutTarget target) {
            super(target);
        }

        /** Copy the given graph and make the nodes/edges in the copied
         *  graph point to the nodes/edges in the original.
         */
        protected Object copyComposite(Object origComposite) {
            LayoutTarget target = getLayoutTarget();
            GraphModel model = target.getGraphModel();
            diva.graph.basic.BasicGraphModel local = getLocalGraphModel();
            Object copyComposite = local.createComposite(null);
            HashMap<Object, Object> map = new HashMap<Object, Object>();

            // Copy all the nodes for the graph.
            for (Iterator<?> i = model.nodes(origComposite); i.hasNext();) {
                Object origNode = i.next();

                if (target.isNodeVisible(origNode)) {
                    Rectangle2D r = target.getBounds(origNode);
                    LevelInfo inf = new LevelInfo();
                    inf.origNode = origNode;
                    inf.x = r.getX();
                    inf.y = r.getY();
                    inf.width = r.getWidth();
                    inf.height = r.getHeight();

                    Object copyNode = local.createNode(inf);
                    local.addNode(this, copyNode, copyComposite);
                    map.put(origNode, copyNode);
                }
            }

            // Add all the edges.
            Iterator<?> i = GraphUtilities.partiallyContainedEdges(
                    origComposite, model);

            while (i.hasNext()) {
                Object origEdge = i.next();
                Object origTail = model.getTail(origEdge);
                Object origHead = model.getHead(origEdge);

                if ((origHead != null) && (origTail != null)) {
                    Figure tailFigure = (Figure) target
                            .getVisualObject(origTail);
                    Figure headFigure = (Figure) target
                            .getVisualObject(origHead);

                    // Swap the head and the tail if it will improve the
                    // layout, since LevelLayout only uses directed edges.
                    if (tailFigure instanceof Terminal) {
                        Terminal terminal = (Terminal) tailFigure;
                        Site site = terminal.getConnectSite();

                        if (site instanceof FixedNormalSite) {
                            double normal = site.getNormal();
                            int direction = CanvasUtilities
                                    .getDirection(normal);

                            if (direction == SwingUtilities.WEST) {
                                Object temp = origTail;
                                origTail = origHead;
                                origHead = temp;
                            }
                        }
                    } else if (headFigure instanceof Terminal) {
                        Terminal terminal = (Terminal) headFigure;
                        Site site = terminal.getConnectSite();

                        if (site instanceof FixedNormalSite) {
                            double normal = site.getNormal();
                            int direction = CanvasUtilities
                                    .getDirection(normal);

                            if (direction == SwingUtilities.EAST) {
                                Object temp = origTail;
                                origTail = origHead;
                                origHead = temp;
                            }
                        }
                    }

                    origTail = _getParentInGraph(model, origComposite, origTail);
                    origHead = _getParentInGraph(model, origComposite, origHead);

                    Object copyTail = map.get(origTail);
                    Object copyHead = map.get(origHead);

                    if ((copyHead != null) && (copyTail != null)) {
                        Object copyEdge = local.createEdge(origEdge);
                        local.setEdgeTail(this, copyEdge, copyTail);
                        local.setEdgeHead(this, copyEdge, copyHead);
                    }
                }
            }

            return copyComposite;
        }

        // Unfortunately, the head and/or tail of the edge may not
        // be directly contained in the graph.  In this case, we need to
        // figure out which of their parents IS in the graph
        // and calculate the cost of that instead.
        private Object _getParentInGraph(GraphModel model, Object graph,
                Object node) {
            while ((node != null) && !model.containsNode(graph, node)) {
                Object parent = model.getParent(node);

                if (model.isNode(parent)) {
                    node = parent;
                } else {
                    node = null;
                }
            }

            return node;
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// PtolemyLayoutTarget

    /** A layout target that translates locatable nodes. */
    private/*static*/class PtolemyLayoutTarget extends BasicLayoutTarget {
        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.
        // However, we call getVisibleCanvasRectangle(), which cannot
        // be static.

        /** Construct a new layout target that operates
         *  in the given pane.
         */
        public PtolemyLayoutTarget(GraphController controller) {
            super(controller);
        }

        /** Return the viewport of the given graph as a rectangle
         *  in logical coordinates.
         */
        public Rectangle2D getViewport(Object composite) {
            //GraphModel model = getController().getGraphModel();

            if (composite == getRootGraph()) {
                // Take into account the current zoom and pan.
                Rectangle2D bounds = getVisibleCanvasRectangle();

                double width = bounds.getWidth();
                double height = bounds.getHeight();

                double borderPercentage = (1 - getLayoutPercentage()) / 2;
                double x = (borderPercentage * width) + bounds.getX();
                double y = (borderPercentage * height) + bounds.getY();
                double w = getLayoutPercentage() * width;
                double h = getLayoutPercentage() * height;
                return new Rectangle2D.Double(x, y, w, h);
            } else {
                return super.getViewport(composite);
            }
        }

        /** Translate the figure associated with the given node in the
         *  target's view by the given delta.
         */
        public void translate(Object node, double dx, double dy) {
            super.translate(node, dx, dy);

            if (node instanceof Locatable) {
                double[] location = ((Locatable) node).getLocation();

                if (location == null) {
                    location = new double[2];

                    Figure figure = getController().getFigure(node);
                    location[0] = figure.getBounds().getCenterX();
                    location[1] = figure.getBounds().getCenterY();
                } else {
                    location[0] += dx;
                    location[1] += dy;
                }

                try {
                    ((Locatable) node).setLocation(location);
                } catch (IllegalActionException ex) {
                    throw new InternalErrorException(ex.getMessage());
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// OpenContainerAction
    /** An action to open the container of this entity. */
    private class OpenContainerAction extends AbstractAction {
        /** Construct an open container action.  This action opens
         *  the container of this class.  If this entity is the toplevel
         *  then the icon is disabled.
         *  @param description A string that describes the action.  Spaces are
         *  permitted, each word is usually capitalized.
         */
        public OpenContainerAction(String description) {
            super(description);
            // Load the image by using the absolute path to the gif.
            // Using a relative location should work, but it does not.
            // Use the resource locator of the class.
            // For more information, see
            // jdk1.3/docs/guide/resources/resources.html
            GUIUtilities.addIcons(this, new String[][] {
                    { "/ptolemy/vergil/basic/img/up.gif",
                            GUIUtilities.LARGE_ICON },
                    { "/ptolemy/vergil/basic/img/up_o.gif",
                            GUIUtilities.ROLLOVER_ICON },
                    { "/ptolemy/vergil/basic/img/up_ov.gif",
                            GUIUtilities.ROLLOVER_SELECTED_ICON },
                    { "/ptolemy/vergil/basic/img/up_on.gif",
                            GUIUtilities.SELECTED_ICON } });

            putValue("tooltip", description);

        }

        /** Open the parent container, if any.
         *  @param event The action event, ignored by this method.
         */
        public void actionPerformed(ActionEvent event) {
            openContainer();
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// OpenLibraryMenuItemFactory

    /**
     *  Create a menu item that will open a library in editable form.
     */
    private class OpenLibraryMenuItemFactory implements MenuItemFactory {
        /**
         * Add an item to the given context menu that will open the
         * given object as an editable model.
         */
        public JMenuItem create(final JContextMenu menu, final NamedObj object) {
            Action action = new AbstractAction("Open for Editing") {
                public void actionPerformed(ActionEvent e) {
                    try {
                        getConfiguration().openModel(object);
                    } catch (KernelException ex) {
                        MessageHandler.error("Open failed.", ex);
                    }
                }
            };

            action.putValue("tooltip", "Open library for editing.");
            action.putValue(diva.gui.GUIUtilities.MNEMONIC_KEY,
                    Integer.valueOf(KeyEvent.VK_O));
            return menu.add(action, (String) action.getValue(Action.NAME));
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// PrintAction

    /**
     *  Print the current model.
     */
    private class PrintAction extends AbstractAction {
        /** Construct a print action.
         *  @param description A string that describes the action.  Spaces are
         *  permitted, each word is usually capitalized.
         */
        public PrintAction(String description) {
            super(description);
            putValue("tooltip", description);

            // Load the image by using the absolute path to the gif.
            // Using a relative location should work, but it does not.
            // Use the resource locator of the class.
            // For more information, see
            // jdk1.3/docs/guide/resources/resources.html
            GUIUtilities.addIcons(this, new String[][] {
                    { "/ptolemy/vergil/basic/img/print.gif",
                            GUIUtilities.LARGE_ICON },
                    { "/ptolemy/vergil/basic/img/print_o.gif",
                            GUIUtilities.ROLLOVER_ICON },
                    { "/ptolemy/vergil/basic/img/print_ov.gif",
                            GUIUtilities.ROLLOVER_SELECTED_ICON },
                    { "/ptolemy/vergil/basic/img/print_on.gif",
                            GUIUtilities.SELECTED_ICON } });
        }

        /** Print the current layout.
         *  @param event The action event, ignored by this method.
         */
        public void actionPerformed(ActionEvent event) {
            _print();
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// RedoAction

    /**
     *  Redo the last undone MoML change on the current current model.
     */
    private class RedoAction extends AbstractAction {
        /**
         *  Create a new action to paste the current contents of the clipboard
         *  into the current model.
         */
        public RedoAction() {
            super("Redo");
            putValue("tooltip", "Redo the last change undone.");
            putValue(diva.gui.GUIUtilities.ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit
                            .getDefaultToolkit().getMenuShortcutKeyMask()));
            putValue(diva.gui.GUIUtilities.MNEMONIC_KEY,
                    Integer.valueOf(KeyEvent.VK_R));
        }

        /**
         *  Redo the last undone MoML change on the current current model.
         *
         * @param e The event for the action.
         */
        public void actionPerformed(ActionEvent e) {
            redo();
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// SaveAction

    /**
     *  Save the current model.
     */
    private class SaveAction extends AbstractAction {
        /** Construct a save action.
         *  @param description A string that describes the action.  Spaces are
         *  permitted, each word is usually capitalized.
         */
        public SaveAction(String description) {
            super(description);
            putValue("tooltip", description);
            // Load the image by using the absolute path to the gif.
            // Using a relative location should work, but it does not.
            // Use the resource locator of the class.
            // For more information, see
            // jdk1.3/docs/guide/resources/resources.html
            GUIUtilities.addIcons(this, new String[][] {
                    { "/ptolemy/vergil/basic/img/save.gif",
                            GUIUtilities.LARGE_ICON },
                    { "/ptolemy/vergil/basic/img/save_o.gif",
                            GUIUtilities.ROLLOVER_ICON },
                    { "/ptolemy/vergil/basic/img/save_ov.gif",
                            GUIUtilities.ROLLOVER_SELECTED_ICON },
                    { "/ptolemy/vergil/basic/img/save_on.gif",
                            GUIUtilities.SELECTED_ICON } });
            putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_Z));
        }

        /** Save the current layout.
         *  @param e The action event, ignored by this method.
         */
        public void actionPerformed(ActionEvent e) {
            _save();
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// UndoAction

    /**
     *  Undo the last undoable MoML change on the current current model.
     */
    private class UndoAction extends AbstractAction {
        /**
         *  Create a new action to paste the current contents of the clipboard
         *  into the current model.
         */
        public UndoAction() {
            super("Undo");
            putValue("tooltip", "Undo the last change.");
            putValue(diva.gui.GUIUtilities.ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit
                            .getDefaultToolkit().getMenuShortcutKeyMask()));
            putValue(diva.gui.GUIUtilities.MNEMONIC_KEY,
                    Integer.valueOf(KeyEvent.VK_U));
        }

        /**
         *  Undo the last undoable MoML change on the current current model.
         *
         * @param e The event for the action.
         */
        public void actionPerformed(ActionEvent e) {
            undo();
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// ZoomInAction
    /** An action to zoom in. */
    private class ZoomInAction extends AbstractAction {
        /** Construct a zoom in action.
         *  @param description A string that describes the action.  Spaces are
         *  permitted, each word is usually capitalized.
         */
        public ZoomInAction(String description) {
            super(description);

            // Load the image by using the absolute path to the gif.
            // Using a relative location should work, but it does not.
            // Use the resource locator of the class.
            // For more information, see
            // jdk1.3/docs/guide/resources/resources.html
            GUIUtilities.addIcons(this, new String[][] {
                    { "/ptolemy/vergil/basic/img/zoomin.gif",
                            GUIUtilities.LARGE_ICON },
                    { "/ptolemy/vergil/basic/img/zoomin_o.gif",
                            GUIUtilities.ROLLOVER_ICON },
                    { "/ptolemy/vergil/basic/img/zoomin_ov.gif",
                            GUIUtilities.ROLLOVER_SELECTED_ICON },
                    { "/ptolemy/vergil/basic/img/zoomin_on.gif",
                            GUIUtilities.SELECTED_ICON } });

            putValue("tooltip", description + " (Ctrl+Shift+=)");

            // NOTE: The following assumes that the + key is the same
            // as the = key.  Unfortunately, the VK_PLUS key event doesn't
            // work, so we have to do it this way.
            putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_EQUALS, Toolkit.getDefaultToolkit()
                            .getMenuShortcutKeyMask() | Event.SHIFT_MASK));
            putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_Z));
        }

        /** Zoom in by a factor of 1.25.
         *  @param e The action event, ignored by this method.
         */
        public void actionPerformed(ActionEvent e) {
            zoom(1.25);
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// ZoomResetAction
    /** An action to reset zoom. */
    private class ZoomResetAction extends AbstractAction {
        /** Construct a zoom reset action.
         *  @param description A string that describes the action.  Spaces are
         *  permitted, each word is usually capitalized.
         */
        public ZoomResetAction(String description) {
            super(description);

            // Load the image by using the absolute path to the gif.
            // Using a relative location should work, but it does not.
            // Use the resource locator of the class.
            // For more information, see
            // jdk1.3/docs/guide/resources/resources.html
            GUIUtilities.addIcons(this, new String[][] {
                    { "/ptolemy/vergil/basic/img/zoomreset.gif",
                            GUIUtilities.LARGE_ICON },
                    { "/ptolemy/vergil/basic/img/zoomreset_o.gif",
                            GUIUtilities.ROLLOVER_ICON },
                    { "/ptolemy/vergil/basic/img/zoomreset_ov.gif",
                            GUIUtilities.ROLLOVER_SELECTED_ICON },
                    { "/ptolemy/vergil/basic/img/zoomreset_on.gif",
                            GUIUtilities.SELECTED_ICON } });

            // Control-m is usually carriage return.  In this case, we use
            // it to mean "return the zoom to the original state".
            putValue("tooltip", description + " (Ctrl+M)");
            putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_M, Toolkit.getDefaultToolkit()
                            .getMenuShortcutKeyMask()));
            putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_M));
        }

        /** Reset the zoom.
         *  @param e The action event, ignored by this method.
         */
        public void actionPerformed(ActionEvent e) {
            zoomReset();
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// ZoomFitAction
    /** An action to zoom fit.*/
    private class ZoomFitAction extends AbstractAction {
        /** Construct a zoom fit action.
         *  @param description A string that describes the action.  Spaces are
         *  permitted, each word is usually capitalized.
         */
        public ZoomFitAction(String description) {
            super(description);

            // Load the image by using the absolute path to the gif.
            // Using a relative location should work, but it does not.
            // Use the resource locator of the class.
            // For more information, see
            // jdk1.3/docs/guide/resources/resources.html
            GUIUtilities.addIcons(this, new String[][] {
                    { "/ptolemy/vergil/basic/img/zoomfit.gif",
                            GUIUtilities.LARGE_ICON },
                    { "/ptolemy/vergil/basic/img/zoomfit_o.gif",
                            GUIUtilities.ROLLOVER_ICON },
                    { "/ptolemy/vergil/basic/img/zoomfit_ov.gif",
                            GUIUtilities.ROLLOVER_SELECTED_ICON },
                    { "/ptolemy/vergil/basic/img/zoomfit_on.gif",
                            GUIUtilities.SELECTED_ICON } });

            putValue("tooltip", description + " (Ctrl+Shift+-)");
            putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_MINUS, Toolkit.getDefaultToolkit()
                            .getMenuShortcutKeyMask() | Event.SHIFT_MASK));
            putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_F));
        }

        /** Zoom so that the entire graph is visible.
         *  @param e The action event, ignored by this method.
         */
        public void actionPerformed(ActionEvent e) {
            zoomFit();
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// ZoomOutAction
    /** An action to zoom out. */
    private class ZoomOutAction extends AbstractAction {
        /** Construct a zoom fit action.
         *  @param description A string that describes the action.  Spaces are
         *  permitted, each word is usually capitalized.
         */
        public ZoomOutAction(String description) {
            super(description);

            // Load the image by using the absolute path to the gif.
            // Using a relative location should work, but it does not.
            // Use the resource locator of the class.
            // For more information, see
            // jdk1.3/docs/guide/resources/resources.html
            GUIUtilities.addIcons(this, new String[][] {
                    { "/ptolemy/vergil/basic/img/zoomout.gif",
                            GUIUtilities.LARGE_ICON },
                    { "/ptolemy/vergil/basic/img/zoomout_o.gif",
                            GUIUtilities.ROLLOVER_ICON },
                    { "/ptolemy/vergil/basic/img/zoomout_ov.gif",
                            GUIUtilities.ROLLOVER_SELECTED_ICON },
                    { "/ptolemy/vergil/basic/img/zoomout_on.gif",
                            GUIUtilities.SELECTED_ICON } });

            putValue("tooltip", description + " (Ctrl+-)");
            putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_MINUS, Toolkit.getDefaultToolkit()
                            .getMenuShortcutKeyMask()));
            putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_U));
        }

        /** Zoom out by a factor of 1/1.25.
         *  @param e The action event, ignored by this method.
         */
        public void actionPerformed(ActionEvent e) {
            zoom(1.0 / 1.25);
        }
    }

}
