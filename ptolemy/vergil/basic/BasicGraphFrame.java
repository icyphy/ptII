/* A simple graph view for Ptolemy models

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
 2
 */
package ptolemy.vergil.basic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
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
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import ptolemy.actor.DesignPatternGetMoMLAction;
import ptolemy.actor.IOPort;
import ptolemy.actor.IORelation;
import ptolemy.actor.gui.BrowserEffigy;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.DialogTableau;
import ptolemy.actor.gui.EditParametersDialog;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyFrame;
import ptolemy.actor.gui.PtolemyPreferences;
import ptolemy.actor.gui.SizeAttribute;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.UserActorLibrary;
import ptolemy.actor.gui.WindowPropertiesAttribute;
import ptolemy.actor.gui.properties.ToolBar;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ExpertParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.gui.ComponentDialog;
import ptolemy.gui.ExtensionFilenameFilter;
import ptolemy.gui.ImageExportable;
import ptolemy.gui.JFileChooserBugFix;
import ptolemy.gui.MemoryCleaner;
import ptolemy.gui.PtFileChooser;
import ptolemy.gui.PtGUIUtilities;
import ptolemy.gui.Query;
import ptolemy.gui.Top;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.undo.RedoChangeRequest;
import ptolemy.kernel.undo.UndoChangeRequest;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.ErrorHandler;
import ptolemy.moml.IconLoader;
import ptolemy.moml.LibraryAttribute;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.MoMLVariableChecker;
import ptolemy.moml.SimpleErrorHandler;
import ptolemy.util.CancelException;
import ptolemy.util.MessageHandler;
import ptolemy.util.SimpleMessageHandler;
import ptolemy.vergil.icon.DesignPatternIcon;
import ptolemy.vergil.kernel.AttributeNodeModel;
import ptolemy.vergil.toolbox.MenuItemFactory;
import ptolemy.vergil.toolbox.MoveAction;
import ptolemy.vergil.tree.ClassAndEntityTreeModel;
import ptolemy.vergil.tree.EntityTreeModel;
import ptolemy.vergil.tree.PTree;
import ptolemy.vergil.tree.PTreeMenuCreator;
import ptolemy.vergil.tree.PtolemyTreeCellRenderer;
import ptolemy.vergil.tree.VisibleTreeModel;
import diva.canvas.CanvasComponent;
import diva.canvas.CanvasUtilities;
import diva.canvas.Figure;
import diva.canvas.FigureLayer;
import diva.canvas.JCanvas;
import diva.canvas.event.EventLayer;
import diva.canvas.event.LayerAdapter;
import diva.canvas.event.LayerEvent;
import diva.canvas.interactor.SelectionModel;
import diva.graph.GraphController;
import diva.graph.GraphEvent;
import diva.graph.GraphModel;
import diva.graph.GraphPane;
import diva.graph.GraphUtilities;
import diva.graph.JGraph;
import diva.gui.GUIUtilities;
import diva.gui.toolbox.JCanvasPanner;
import diva.gui.toolbox.JContextMenu;
import diva.util.Filter;
import diva.util.UserObjectContainer;
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
@SuppressWarnings("serial")
public abstract class BasicGraphFrame extends PtolemyFrame implements
        Printable, ClipboardOwner, ChangeListener, MouseWheelListener,
        MouseListener, MouseMotionListener, ImageExportable, HTMLExportable {

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
    @Override
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
    @Override
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
     *  of deep connections.
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
                                && locationContainer instanceof IOPort) {
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
                if (!headOK && tailOK || headOK && !tailOK) {

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
                            if (isInput
                                    && headProperties.type == ElementInLinkType.PORT_IN_ACTOR
                                    || isOutput
                                    && headProperties.type == ElementInLinkType.STANDALONE_PORT) {
                                newPorts.append("<property name=\"output\"/>");
                            }

                            if (isOutput
                                    && headProperties.type == ElementInLinkType.PORT_IN_ACTOR
                                    || isInput
                                    && headProperties.type == ElementInLinkType.STANDALONE_PORT) {
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
                    selection.toArray(new Object[selection.size()]), model));

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

        // Used by Kepler's Comad.
        selection = BasicGraphFrameExtension.filterDeletedObjects(graphModel,
                selection);

        // Generate the MoML to carry out the deletion
        StringBuffer moml = _deleteMoML(graphModel, selection, model);

        BasicGraphFrameExtension.filterDeleteMoml(graphModel, selection, moml);

        // Next process the deletion MoML. This should be the large majority
        // of most deletions.
        try {
            // Finally create and request the change
            NamedObj container = graphModel.getPtolemyModel();
            MoMLChangeRequest change = new MoMLChangeRequest(this, container,
                    moml.toString());
            change.setUndoable(true);
            container.requestChange(change);
            BasicGraphFrameExtension.alternateDelete(selection, graphModel,
                    container);
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
    @Override
    public void dispose() {
        if (_debugClosing) {
            System.out.println("BasicGraphFrame.dispose() : " + this.getName());
        }

        // Remove the association with the library. This is necessary to allow
        // this frame, and the rest of the model to be properly garbage
        // collected
        if (_libraryModel != null) {
            _libraryModel.setRoot(null);
        }
        _openGraphFrames.remove(this);

        if (_jgraph != null) {
            GraphPane pane = _jgraph.getGraphPane();
            EventLayer foregroundEventLayer = pane.getForegroundEventLayer();
            foregroundEventLayer.removeLayerListener(_mousePressedLayerAdapter);
        }

        //int removed =
        MemoryCleaner.removeActionListeners(_toolbar);
        //System.out.println("BasicGraphFrame toolbar action listeners removed: " + removed);

        NamedObj model = getModel();
        if (model != null) {
            // SaveAs of an ontology solver resulted in a NPE.
            getModel().removeChangeListener(this);
        }

        if (_rightComponent != null) {
            _rightComponent.removeMouseWheelListener(this);
            _rightComponent.removeMouseMotionListener(this);
            _rightComponent.removeMouseListener(this);
        }

        if (_libraryContextMenuCreator != null) {
            _libraryContextMenuCreator.clear();
        }

        _mousePressedLayerAdapter = null;

        // Top.dispose() sets all the AbstractAction to null.
        disposeSuper();
    }

    /** Invoke the dispose() method of the superclass,
     *  {@link ptolemy.actor.gui.PtolemyFrame}.
     */
    public void disposeSuper() {
        if (_debugClosing) {
            System.out.println("BasicGraphFrame.disposeSuper() : "
                    + this.getName());
        }

        // This method is used by Kepler for the tabbed pane interface.
        super.dispose();
    }

    /** Expand all the rows of the library.
     *  Expanding all the rows is useful for testing.
     */
    @Override
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

    /** Given a NamedObj, return the corresponding BasicGraphFrame.
     *  @param model The NamedObj for the model. See
     *  {@link ptolemy.actor.gui.ConfigurationApplication#openModel(String)}
     *  for a static method that returns the model
     *  @return The BasicGraphFrame that corresponds with the model or
     *  null if the model argument was null, the effigy for the model
     *  cannot be found or if the Effigy does not contain a Tableau.
     */
    public static BasicGraphFrame getBasicGraphFrame(NamedObj model) {
        if (model == null) {
            return null;
        }
        // See PtolemyLayoutAction for similar code.
        Effigy effigy = Configuration.findEffigy(model);
        return getBasicGraphFrame(effigy);
    }

    /** Given an Effigy, return the corresponding BasicGraphFrame, if any.
     *  @param effigy The Effigy. To determine the Effigy of a
     *  NamedObj, use {@link  ptolemy.actor.gui.Configuration#findEffigy(NamedObj)}.
     *  @return The BasicGraphFrame that corresponds with the Effigy
     *  or null if the Effigy does not contain a Tableau.
     */
    public static BasicGraphFrame getBasicGraphFrame(Effigy effigy) {
        if (effigy == null) {
            return null;
        }
        List entities = effigy.entityList(Tableau.class);
        if (entities == null) {
            return null;
        }

        BasicGraphFrame frame = null;
        Iterator tableaux = entities.iterator();
        while (tableaux.hasNext()) {
            Tableau tableau = (Tableau) tableaux.next();
            if (tableau.getFrame() instanceof BasicGraphFrame) {
                frame = (BasicGraphFrame) tableau.getFrame();
                break;
            }
        }
        return frame;
    }

    /** Return the center location of the visible part of the pane.
     *  @return The center of the visible part.
     *  @see #setCenter(Point2D)
     */
    public Point2D getCenter() {
        Rectangle2D rect = getVisibleCanvasRectangle();
        return new Point2D.Double(rect.getCenterX(), rect.getCenterY());
    }

    /** Return the size of the contents of this window.
     *  @return The size of the contents.
     */
    @Override
    public Dimension getContentSize() {
        return getJGraph().getSize();
    }

    /** Return the figure that is an icon of a NamedObj and is
     *  under the specified point, or null if there is none.
     *  The point argument may need to be transformed, see
     *  {@link ptolemy.vergil.basic.EditorDropTargetListener#_getFigureUnder(Point2D)}.
     *
     *  @param pane The pane in which to search
     *  @param point The point in the graph pane.
     *  @param filteredFigures figures that are filtered from the object search
     *  @return The object under the specified point, or null if there
     *   is none or it is not a NamedObj.
     */
    public static Figure getFigureUnder(GraphPane pane, Point2D point,
            final Object[] filteredFigures) {

        FigureLayer layer = pane.getForegroundLayer();

        // Find the figure under the point.
        // NOTE: Unfortunately, FigureLayer.getCurrentFigure() doesn't
        // work with a drop target (I guess it hasn't seen the mouse events),
        // so we have to use a lower level mechanism.
        double halo = layer.getPickHalo();
        double width = halo * 2;
        Rectangle2D region = new Rectangle2D.Double(point.getX() - halo,
                point.getY() - halo, width, width);
        // Filter away all figures given by the filteredFigures array
        CanvasComponent figureUnderMouse = layer.pick(region, new Filter() {
            @Override
            public boolean accept(Object o) {
                for (Object filter : filteredFigures) {
                    CanvasComponent figure = (CanvasComponent) o;
                    while (figure != null) {
                        if (figure.equals(filter)) {
                            return false;
                        }
                        figure = figure.getParent();
                    }
                }
                return true;
            }
        });

        // Find a user object belonging to the figure under the mouse
        // or to any figure containing it (it may be a composite figure).
        Object objectUnderMouse = null;

        while (figureUnderMouse instanceof UserObjectContainer
                && objectUnderMouse == null) {
            objectUnderMouse = ((UserObjectContainer) figureUnderMouse)
                    .getUserObject();

            if (objectUnderMouse instanceof NamedObj) {
                if (figureUnderMouse instanceof Figure) {
                    return (Figure) figureUnderMouse;
                }
            }

            figureUnderMouse = figureUnderMouse.getParent();
        }

        return null;
    }

    /** The frame (window) being exported to HTML.
     *  @return This frame.
     */
    public PtolemyFrame getFrame() {
        return this;
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

    /** Get the directory that was last accessed.
     *  @return The last directory
     *  @see #setLastDirectory(File)
     */
    public File getLastDirectory() {
        return _directory;
    }

    /** Set the directory that was last accessed by this window.
     *  @see #getLastDirectory()
     *  @param directory The directory last accessed.
     */
    public void setLastDirectory(File directory) {
        // NOTE: This method is necessary because we wish to have
        // this accessed by inner classes, and there is a bug in
        // jdk1.2.2 where inner classes cannot access protected
        // static members.
        setDirectory(directory);
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
        JFileChooserBugFix jFileChooserBugFix = new JFileChooserBugFix();
        Color background = null;
        PtFileChooser ptFileChooser = null;

        try {
            background = jFileChooserBugFix.saveBackground();
            ptFileChooser = new PtFileChooser(this,
                    "Select a design pattern file.", JFileChooser.OPEN_DIALOG);
            //if (_fileFilter != null) {
            //    ptFileChooser.addChoosableFileFilter(_fileFilter);
            //}

            ptFileChooser.setCurrentDirectory(_directory);

            int returnVal = ptFileChooser.showDialog(this, "Import");

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                Top.setDirectory(ptFileChooser.getCurrentDirectory());
                NamedObj model = null;
                File file = null;
                try {
                    file = ptFileChooser.getSelectedFile().getCanonicalFile();
                    URL url = file.toURI().toURL();
                    MoMLParser parser = new MoMLParser();
                    MoMLParser.purgeModelRecord(url);
                    model = parser.parse(url, url);
                    MoMLParser.purgeModelRecord(url);
                } catch (Exception e) {
                    report(new IllegalActionException(getModel(), e,
                            "Error reading input file \"" + file + "\"."));
                }
                if (model != null) {
                    Attribute attribute = model
                            .getAttribute("_alternateGetMomlAction");
                    String className = DesignPatternGetMoMLAction.class
                            .getName();
                    if (attribute == null
                            || !(attribute instanceof StringAttribute)
                            || !((StringAttribute) attribute).getExpression()
                                    .equals(className)) {
                        report(new IllegalActionException("The model \"" + file
                                + "\" is not a design pattern."));
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
        } finally {
            jFileChooserBugFix.restoreBackground(background);
        }
    }

    /** Do nothing.
     */
    @Override
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

    /** Opens the nearest composite actor above the target in the hierarchy
     *  and possibly change the zoom and centering to show the target.
     *  This method is useful for displaying search results and actors that
     *  cause errors.
     *  @param target The target.
     *  @param owner The frame that, per the user, is generating the dialog.
     */
    public static void openComposite(final Frame owner, final NamedObj target) {
        // This method is static so that it
        NamedObj container = target.getContainer();
        while (container != null && !(container instanceof CompositeEntity)) {
            container = container.getContainer();
        }
        if (container == null) {
            // Hmm.  Could not find container?
            container = target;
        }
        try {
            if (owner != null) {
                report(owner, "Opening " + container.getFullName());
            }
            Effigy effigy = Configuration.findEffigy(target.toplevel());
            if (effigy == null) {
                throw new IllegalActionException(target, "Failed to find an "
                        + "effigy for the toplevel "
                        + target.toplevel().getFullName());
            }
            Configuration configuration = (Configuration) effigy.toplevel();
            Tableau tableau = configuration.openInstance(container);

            // Try to zoom and center on the target.

            // Get the _location attribute.  If it is null, then maybe
            // this is a parameter in an actor so go up the hierarchy
            // until we get to the container we found above or we find
            // a non-null location attribute.
            Location locationAttribute = (Location) target.getAttribute(
                    "_location", Location.class);
            if (locationAttribute == null) {
                NamedObj targetContainer = target.getContainer();
                while (targetContainer != null
                        && (locationAttribute = (Location) targetContainer
                                .getAttribute("_location", Location.class)) == null) {
                    // FindBugs: Load of known null value.  locationAttribute is always null here.
                    // The break is unnecessary as if locationAttribute is non-null, then
                    // the body of the while loop is not executed.
                    //if (locationAttribute != null
                    if (targetContainer.equals(container)) {
                        break;
                    }
                    targetContainer = targetContainer.getContainer();
                }
            }
            if (locationAttribute != null) {
                Frame frame = tableau.getFrame();
                if (frame instanceof BasicGraphFrame) {
                    BasicGraphFrame basicGraphFrame = (BasicGraphFrame) frame;

                    double[] locationArray = locationAttribute.getLocation();
                    Point2D locationPoint2D = new Point2D.Double(
                            locationArray[0], locationArray[1]);

                    GraphPane pane = basicGraphFrame.getJGraph().getGraphPane();

                    // The value returned by Rectangle2D.outcode()
                    int outcode = 0;
                    Figure figure = BasicGraphFrame.getFigureUnder(pane,
                            locationPoint2D, new Object[] {});
                    if (figure == null) {
                        // If we can't find the figure, then force zoom and center.
                        // I'm not sure if this can ever happen, but it might help
                        outcode = 666;
                    } else {
                        Rectangle2D figureBounds = figure.getBounds();
                        Rectangle2D canvasBounds = basicGraphFrame
                                .getVisibleCanvasRectangle();
                        outcode = canvasBounds.outcode(figureBounds.getX(),
                                figureBounds.getY());
                        //basicGraphFrame.zoomFit(pane, figureBounds);
                        //basicGraphFrame.zoom(0.6);
                    }

                    // Get the scale, assume that the scaling in the X
                    // and Y directions are the same.
                    AffineTransform current = pane.getCanvas().getCanvasPane()
                            .getTransformContext().getTransform();
                    double scale = current.getScaleX();
                    if (scale < 0.8 || scale > 2.0 || outcode != 0) {
                        // Only reset the zoom if the would be difficult to see
                        // the component or if the component is not visible.
                        basicGraphFrame.zoomReset();
                        basicGraphFrame.setCenter(locationPoint2D);
                    }
                }
            }
            if (owner != null) {
                report(owner, "Opened " + container.getFullName());
            }
        } catch (Throwable throwable) {
            MessageHandler.error("Failed to open container", throwable);
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

            // Needed by Kepler's Comad.
            BasicGraphFrameExtension.alternatePasteMomlModification(container,
                    moml);

            moml.append("</group>\n");

            MoMLChangeRequest change = new OffsetMoMLChangeRequest(this,
                    container, moml.toString());
            change.setUndoable(true);
            container.requestChange(change);

            // Added by Lei Dou to update the signature for Kepler/Comad
            BasicGraphFrameExtension.alternatePaste(container, moml);
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
    @Override
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

    //     /** Open a file browser and save the given entity in the file specified
    //      *  by the user.
    //      *  @param entity The entity to save.
    //      *  @exception Exception If there is a problem saving the component.
    //      *  @since Ptolemy 4.0
    //      */
    //     public void saveComponentInFile(Entity entity) throws Exception {
    //         // FIXME: This method is probably no
    //         // NOTE: This mirrors similar code in Top and TableauFrame, but
    //         // I can't find any way to re-use that code, since the details
    //         // are slightly different at each step here.

    //         JFileChooserBugFix jFileChooserBugFix = new JFileChooserBugFix();
    //         Color background = null;
    //         PtFileChooser ptFileChooser = null;

    //         try {
    //             background = jFileChooserBugFix.saveBackground();
    //             ptFileChooser = new PtFileChooser(this,
    //                     "Save Component as...",
    //                     JFileChooser.SAVE_DIALOG);
    //             ptFileChooser.setCurrentDirectory(_directory);
    //             // Hmm, is getCurrentDirectory necessary here?
    //             ptFileChooser.setSelectedFile(new File(ptFileChooser.getCurrentDirectory(),
    //                             entity.getName() + ".xml"));

    //             int returnVal = ptFileChooser.showDialog(this,
    //                     "Save");
    //             if (returnVal == JFileChooser.APPROVE_OPTION) {
    //                 // We set _directory below.
    //                 File file = ptFileChooser.getSelectedFile();

    //                 if (!_confirmFile(entity, file)) {
    //                     return;
    //                 }

    //                 // Record the selected directory.
    //                 _directory = ptFileChooser.getCurrentDirectory();

    //                 java.io.FileWriter fileWriter = null;

    //                 try {
    //                     fileWriter = new java.io.FileWriter(file);

    //                     // Make sure the entity name saved matches the file name.
    //                     String name = entity.getName();
    //                     String filename = file.getName();
    //                     int period = filename.indexOf(".");

    //                     if (period > 0) {
    //                         name = filename.substring(0, period);
    //                     } else {
    //                         name = filename;
    //                     }

    //                     fileWriter.write("<?xml version=\"1.0\" standalone=\"no\"?>\n"
    //                             + "<!DOCTYPE " + entity.getElementName() + " PUBLIC "
    //                             + "\"-//UC Berkeley//DTD MoML 1//EN\"\n"
    //                             + "    \"http://ptolemy.eecs.berkeley.edu"
    //                             + "/xml/dtd/MoML_1.dtd\">\n");

    //                     entity.exportMoML(fileWriter, 0, name);
    //                 } finally {
    //                     if (fileWriter != null) {
    //                         fileWriter.close();
    //                     }
    //                 }
    //             }
    //         } finally {
    //             jFileChooserBugFix.restoreBackground(background);
    //         }
    //     }

    /** Report a message to either the status bar or message handler.
     *  @param owner The frame that, per the user, is generating the
     *  dialog.
     *  @param message The message.
     */
    public static void report(Frame owner, String message) {
        if (owner instanceof Top) {
            ((Top) owner).report(message);
        } else {
            MessageHandler.message(message);
        }
    }

    /** Save the given entity in the user library in the given
     *  configuration.
     *  @param configuration The configuration.
     *  @param entity The entity to save.
     *  @since Ptolemy 2.1
     *  @deprecated Use {@link ptolemy.actor.gui.UserActorLibrary#saveComponentInLibrary(Configuration, Entity)}
     */
    @Deprecated
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

    /** Update the size, zoom and position of the window.
     *  This method is typically called when closing the window
     *  or writing the moml file out.
     *  @exception IllegalActionException If there is a problem
     *  getting a parameter.
     *  @exception NameDuplicationException If there is a problem
     *  creating a parameter.
     */
    public void updateWindowAttributes() throws IllegalActionException,
            NameDuplicationException {
        // First, record size and position.

        // See "composite window size & position not always saved"
        // http://bugzilla.ecoinformatics.org/show_bug.cgi?id=5637

        // Record the position of the top-level frame, assuming
        // there is one.
        Component component = _getRightComponent().getParent();
        Component parent = component.getParent();

        while (parent != null && !(parent instanceof Frame)) {
            component = parent;
            parent = component.getParent();
        }

        // Oddly, sometimes getModel returns null?  $PTII/bin/ptinvoke
        // ptolemy.vergil.basic.export.ExportModel -force htm -run
        // -openComposites -whiteBackground
        // ptolemy/actor/gt/demo/MapReduce/MapReduce.xml
        // $PTII/ptolemy/actor/gt/demo/MapReduce/MapReduce
        NamedObj model = getModel();
        if (model != null) {

            // If there is no parent that is a Frame, do nothing.
            // We know that: (parent == null) || (parent instanceof Frame)
            if (parent != null) {
                WindowPropertiesAttribute properties = (WindowPropertiesAttribute) model
                        .getAttribute("_windowProperties",
                                WindowPropertiesAttribute.class);

                if (properties == null) {
                    properties = new WindowPropertiesAttribute(model,
                            "_windowProperties");
                }

                // This method uses MoMLChangeRequest
                properties.recordProperties((Frame) parent);
            }

            _createSizeAttribute();

            // Also record zoom and pan state.
            JCanvas canvas = getJGraph().getGraphPane().getCanvas();
            AffineTransform current = canvas.getCanvasPane()
                    .getTransformContext().getTransform();

            // We assume the scaling in the X and Y directions are the same.
            double scale = current.getScaleX();
            Parameter zoom = (Parameter) model.getAttribute(
                    "_vergilZoomFactor", Parameter.class);

            boolean updateValue = false;
            if (zoom == null || zoom.getToken() == null) {
                // NOTE: This will not propagate.
                zoom = new ExpertParameter(model, "_vergilZoomFactor");
                zoom.setToken("1.0");
                updateValue = true;
            } else {
                double oldZoom = ((DoubleToken) zoom.getToken()).doubleValue();
                if (oldZoom != scale) {
                    updateValue = true;
                }
            }

            if (updateValue) {
                // Don't call setToken(), instead use a MoMLChangeRequest so that
                // the model is marked modified so that any changes are preserved.
                //zoom.setToken(new DoubleToken(scale));
                String moml = "<property name=\"_vergilZoomFactor\" "
                        + " value=\"" + scale + "\"/>";
                MoMLChangeRequest request = new MoMLChangeRequest(this, model,
                        moml);
                request.setUndoable(true);
                model.requestChange(request);

                // Make sure the visibility is only expert.
                zoom.setVisibility(Settable.EXPERT);
            }

            // Save the center, to record the pan state.
            Point2D center = getCenter();
            Parameter pan = (Parameter) model.getAttribute("_vergilCenter",
                    Parameter.class);

            updateValue = false;
            if (pan == null || pan.getToken() == null) {
                // NOTE: This will not propagate.
                pan = new ExpertParameter(model, "_vergilCenter");
                pan.setToken("{" + center.getX() + ", " + center.getY() + "}");
                updateValue = true;
            } else {
                Token[] oldCenter = ((ArrayToken) pan.getToken()).arrayValue();
                double oldCenterX = ((DoubleToken) oldCenter[0]).doubleValue();
                double oldCenterY = ((DoubleToken) oldCenter[1]).doubleValue();
                if (center.getX() != oldCenterX || center.getY() != oldCenterY) {
                    updateValue = true;
                }
            }

            if (updateValue) {
                //Token[] centerArray = new Token[2];
                //centerArray[0] = new DoubleToken(center.getX());
                //centerArray[1] = new DoubleToken(center.getY());
                //pan.setToken(new ArrayToken(centerArray));

                String moml = "<property name=\"_vergilCenter\" "
                        + " value=\"{" + center.getX() + ", " + center.getY()
                        + "}\"/>";
                MoMLChangeRequest request = new MoMLChangeRequest(this, model,
                        moml);
                request.setUndoable(true);
                getModel().requestChange(request);

                // Make sure the visibility is only expert.
                pan.setVisibility(Settable.EXPERT);
            }
        } // model == null
    }

    /** Write an HTML page based on the current view of the model
     *  to the specified destination directory. The file will be
     *  named "index.html," and supporting files, including at
     *  least a gif image showing the contents currently visible in
     *  the graph frame, will be created. If there are any plot windows
     *  open or any composite actors open, then gif and/or HTML will
     *  be generated for those as well and linked to the gif image
     *  created for this frame.
     *  <p>
     *  The generated page has a header with the name of the model,
     *  a reference to a GIF image file with name equal to the name
     *  of the model with a ".gif" extension appended, and a script
     *  that reacts when the mouse is moved over an actor by
     *  displaying a table with the parameter values of the actor.
     *  The gif image is assumed to have been generated with the
     *  current view using the {@link #writeImage(OutputStream, String)}
     *  method.
     *  @param parameters The parameters that control the export.
     *  @param writer The writer to use the write the HTML. If this is null,
     *   then create an index.html file in the
     *   directory given by the directoryToExportTo field of the parameters.
     *  @exception IOException If unable to write associated files, or if the
     *   current configuration does not support it.
     *  @exception PrinterException If unable to write associated files.
     *  @exception IllegalActionException If something goes wrong accessing the model.
     */
    @Override
    public void writeHTML(ExportParameters parameters, Writer writer)
            throws PrinterException, IOException, IllegalActionException {
        if (_exportHTMLAction != null) {
            ((HTMLExportable) _exportHTMLAction).writeHTML(parameters, writer);
        } else {
            throw new IOException("Export to Web not supported.");
        }
    }

    /** Write an image to the specified output stream in the specified format.
     *  Supported formats include at least "gif" and "png", standard image file formats.
     *  The image is a rendition of the current view of the model.
     *  <p>{@link ptolemy.vergil.basic.export.ExportModel} is a standalone class
     *  that exports an image of a model.
     *  @param stream The output stream to write to.
     *  @param format The image format to generate.
     *  @see #writeHTML(ExportParameters, Writer)
     *  @exception IOException If writing to the stream fails.
     *  @exception PrinterException  If the specified format is not supported.
     */
    @Override
    public void writeImage(OutputStream stream, String format)
            throws PrinterException, IOException {
        writeImage(stream, format, null);
    }

    /** Write an image to the specified output stream in the specified format with
     *  the specified background color.
     *  Supported formats include at least "gif" and "png", standard image file formats.
     *  The image is a rendition of the current view of the model.
     *  <p>{@link ptolemy.vergil.basic.export.ExportModel} is a standalone class
     *  that exports an image of a model.
     *  @param stream The output stream to write to.
     *  @param format The image format to generate.
     *  @param background The background color, or null to use the current color.
     *  @see #writeHTML(ExportParameters, Writer)
     *  @exception IOException If writing to the stream fails.
     *  @exception PrinterException  If the specified format is not supported.
     */
    public void writeImage(OutputStream stream, String format, Color background)
            throws PrinterException, IOException {
        JCanvas canvas = getJGraph().getGraphPane().getCanvas();
        Color previousBackground = canvas.getBackground();
        try {
            if (background != null) {
                canvas.setBackground(background);
            }
            getJGraph().exportImage(stream, format);
        } finally {
            if (background != null) {
                canvas.setBackground(previousBackground);
            }
        }
    }

    /** Zoom in or out to magnify by the specified factor, from the current
     *  magnification.
     *  @param factor The magnification factor (relative to 1.0).
     */
    public void zoom(double factor) {
        try {
            _zoomFlag = true;
            JCanvas canvas = getJGraph().getGraphPane().getCanvas();
            AffineTransform current = canvas.getCanvasPane()
                    .getTransformContext().getTransform();

            // Save the center, so we remember what we were looking at.
            Point2D center = getCenter();
            current.scale(factor, factor);
            canvas.getCanvasPane().setTransform(current);

            // Reset the center.
            setCenter(center);

            if (_graphPanner != null) {
                _graphPanner.repaint();
            }
        } finally {
            _zoomFlag = false;
        }
    }

    /** Zoom to fit the current figures.
     */
    public void zoomFit() {
        GraphPane pane = getJGraph().getGraphPane();
        Rectangle2D bounds = pane.getForegroundLayer().getLayerBounds();
        zoomFit(pane, bounds);
    }

    /** Zoom to fit the bounds.
     *  @param pane The pane.
     *  @param bounds The bound to zoom to.
     */
    public void zoomFit(GraphPane pane, Rectangle2D bounds) {
        if (bounds.isEmpty()) {
            // Empty diagram.
            return;
        }

        Rectangle2D viewSize = getVisibleRectangle();
        Rectangle2D paddedViewSize = new Rectangle2D.Double(viewSize.getX()
                + _ZOOM_FIT_PADDING, viewSize.getY() + _ZOOM_FIT_PADDING,
                viewSize.getWidth() - 2 * _ZOOM_FIT_PADDING,
                viewSize.getHeight() - 2 * _ZOOM_FIT_PADDING);
        AffineTransform newTransform = CanvasUtilities.computeFitTransform(
                bounds, paddedViewSize);
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
    @Override
    public void mouseClicked(MouseEvent event) {
        // Implementation of the MouseMotionListener interface.
    }

    /** Transform the graph by the amount the mouse is dragged
     *  while the middle mouse button is held down.
     * @param event The drag event.
     */
    @Override
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
    @Override
    public void mouseEntered(MouseEvent event) {
        // Implementation of the MouseMotionListener interface.
    }

    /**
     * Called when the mouse leaves this component.
     * This base class does nothing when the exits this component.
     * However, events _are_ handled by the components within this component.
     * @param event The mouse event.
     */
    @Override
    public void mouseExited(MouseEvent event) {
        // Implementation of the MouseMotionListener interface.
    }

    /** Called when the mouse is moved.
     * This base class does nothing when the mouse is moved.
     * @param event Contains details of the movement event.
     * However, events _are_ handled by the components within this component.
     */
    @Override
    public void mouseMoved(MouseEvent event) {
        // Implementation of the MouseMotionListener interface.
    }

    /** Store the location of the middle mouse event.
     * @param event The mouse event.
     */
    @Override
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
    @Override
    public void mouseReleased(MouseEvent event) {
        // Implementation of the MouseMotionListener interface.
    }

    /** Scroll in when the mouse wheel is moved.
     * @param event The mouse wheel event.
     */
    @Override
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
    @Deprecated
    public static String VERGIL_USER_LIBRARY_NAME = UserActorLibrary.USER_LIBRARY_NAME;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add a layout menu.
     *  @param graphMenu The menu to which to add the layout menu.
     */
    protected void _addLayoutMenu(JMenu graphMenu) {
        // The layout action is created by BasicGraphFrame.
        if (_layoutAction != null) {
            // If we are running with -ptinyViewer, then the layout facility
            // might not be present.
            GUIUtilities.addHotKey(_getRightComponent(), _layoutAction);
            GUIUtilities.addMenuItem(graphMenu, _layoutAction);
            if (_layoutConfigDialogAction != null) {
                GUIUtilities.addMenuItem(graphMenu, _layoutConfigDialogAction);
            }
            graphMenu.addSeparator();
        }
    }

    /** Create the menus that are used by this frame.
     */
    @Override
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

        _graphMenu = new JMenu("Graph");
        _graphMenu.setMnemonic(KeyEvent.VK_G);
        _menubar.add(_graphMenu);
        GUIUtilities.addHotKey(_getRightComponent(), _findAction);
        GUIUtilities.addMenuItem(_graphMenu, _findAction);
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
    @Override
    protected boolean _close() {
        if (_debugClosing) {
            System.out.println("BasicGraphFrame._close() : " + this.getName());
        }

        // See "composite window size & position not always saved"
        // http://bugzilla.ecoinformatics.org/show_bug.cgi?id=5637

        // Don't update the _windowProperties attribute during _close()
        // For example, if the model is a large model and there is an
        // error and the user clicks on "Go To Actor", then the model
        // may be zoomed.  When the user closes the model, they will
        // be prompted to save.  Even worse, it appears that the size
        // and location of windows can be slightly different between
        // different platforms.

        //         try {
        //             _updateWindowAttributes();
        //         } catch (KernelException ex) {
        //             // Ignore problems here.  Errors simply result in a default
        //             // size and location.
        //             System.out.println("While closing, failed to update size, position or zoom factor: " + ex);
        //         }

        boolean result = super._close();

        if (result) {
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

    /** Create the items in the File menu's Export section
     *  This method adds a menu items to export images of the plot
     *  in GIF, PNG, and possibly PDF.
     *  @return The items in the File menu.
     */
    @Override
    protected JMenuItem[] _createFileMenuItems() {
        JMenuItem[] fileMenuItems = super._createFileMenuItems();

        JMenu importMenu = (JMenu) fileMenuItems[_IMPORT_MENU_INDEX];
        importMenu.setEnabled(true);

        JMenu exportMenu = (JMenu) fileMenuItems[_EXPORT_MENU_INDEX];
        exportMenu.setEnabled(true);

        // Get the "export PDF" action classname from the configuration.
        // This may or many not be included because it depends on GPL'd code,
        // and hence cannot be included included in any pure BSD distribution.
        // NOTE: Cannot use getConfiguration() because the configuration is
        // not set when this method is called. Hence, we assume that there
        // is only one configuration, or that if there are multiple configurations
        // in this execution, that the first one will determine whether PDF
        // export is provided.
        Configuration configuration = (Configuration) Configuration
                .configurations().get(0);
        // NOTE: Configuration should not be null, but just in case:
        if (configuration != null) {

            // Here, we get the _importActionClassNames from the configuration.
            // _importActionClassNames is an array of Strings where each element
            // names a class to that is an import action.
            // See also _classesToRemove in Configuration.java
            try {
                Parameter importActionClassNames = (Parameter) configuration
                        .getAttribute("_importActionClassNames",
                                Parameter.class);
                if (importActionClassNames != null) {
                    ArrayToken importActionClassNamesToken = (ArrayToken) importActionClassNames
                            .getToken();
                    for (int i = 0; i < importActionClassNamesToken.length(); i++) {
                        String importActionClassName = ((StringToken) importActionClassNamesToken
                                .getElement(i)).stringValue();
                        try {
                            // Get the class, instantiate it and add it to the menu.
                            Class importActionClass = Class
                                    .forName(importActionClassName);
                            Constructor constructor = importActionClass
                                    .getDeclaredConstructor(new Class[] { Top.class });
                            AbstractAction importAction = (AbstractAction) constructor
                                    .newInstance(new Object[] { this });
                            JMenuItem importItem = new JMenuItem(importAction);
                            importMenu.add(importItem);
                        } catch (Throwable throwable) {
                            // We do not want to abort at this point because the worst
                            // case is that we will have no Import FMU in the menu.
                            // That is better than preventing the user from opening a model.
                            System.err
                                    .println("Warning: Tried to create the an import menu item, but failed: "
                                            + throwable);
                        }
                    }
                }
            } catch (Throwable throwable) {
                if (!_printedImportActionClassNamesMessage) {
                    _printedImportActionClassNamesMessage = true;
                    System.err
                        .println("Problem reading the _importActionClassNames parameter from "
                                + "the configuration: " + throwable);
                }
            }

            // PDF Action.
            try {
                _exportPDFAction = (AbstractAction) configuration
                        .getStringParameterAsClass("_exportPDFActionClassName",
                                new Class[] { Top.class },
                                new Object[] { this });
            } catch (Throwable throwable) {
                // We do not want to abort at this point because the worst
                // case is that we will have no Export PDF in the menu.
                // That is better than preventing the user from opening a model.
                //System.err
                //    .printlns("Warning: Tried to create the Export PDF menu item, but failed: "
                //            + throwable);
            }

            // Deal with the HTML Action next.
            try {
                _exportHTMLAction = (AbstractAction) configuration
                        .getStringParameterAsClass(
                                "_exportHTMLActionClassName",
                                new Class[] { BasicGraphFrame.class },
                                new Object[] { this });
            } catch (Throwable throwable) {
                // We do not want to abort at this point because the worst
                // case is that we will have no Export to Web in the menu.
                // That is better than preventing the user from opening a model.

                // We don't include the GPL'd iText PDF in the
                // release, so don't print a message if it is missing.

                //System.err
                //        .println("Warning: Tried to create the Export to Web menu item, but failed: "
                //                + throwable);
            }
        }

        // Uncomment the next block to have Export PDF *ALWAYS* enabled.
        // We don't want it always enabled because ptiny, the applets and
        // Web Start should not included this AGPL'd piece of software

        // NOTE: Comment out the entire block with lines that begin with //
        // so that the test in adm notices that the block is commented out.

        //                 if (_exportPDFAction == null) {
        //                     //String exportPDFActionClassName = exportPDFActionClassNameParameter.stringValue();
        //                     String exportPDFActionClassName = "ptolemy.vergil.basic.export.itextpdf.ExportPDFAction";
        //                     try {
        //                         Class exportPDFActionClass = Class
        //                                 .forName(exportPDFActionClassName);
        //                         Constructor exportPDFActionConstructor = exportPDFActionClass
        //                                 .getDeclaredConstructor(Top.class);
        //                         _exportPDFAction = (AbstractAction) exportPDFActionConstructor
        //                                 .newInstance(this);
        //                     } catch (Throwable throwable) {
        //                         new InternalErrorException(null, throwable,
        //                                 "Failed to construct export PDF class \""
        //                                         + exportPDFActionClassName
        //                                         + "\", which was read from the configuration.");
        //                     }
        //                 }

        // End of block to uncomment.

        if (_exportPDFAction != null) {
            // Insert the Export PDF item.
            JMenuItem exportItem = new JMenuItem(_exportPDFAction);
            exportMenu.add(exportItem);
        }

        // Next do the export GIF action.
        if (_exportGIFAction == null) {
            _exportGIFAction = new ExportImageAction("GIF");
        }
        JMenuItem exportItem = new JMenuItem(_exportGIFAction);
        exportMenu.add(exportItem);

        // Next do the export PNG action.
        if (_exportPNGAction == null) {
            _exportPNGAction = new ExportImageAction("PNG");
        }
        exportItem = new JMenuItem(_exportPNGAction);
        exportMenu.add(exportItem);

        // Next do the export HTML action.
        if (_exportHTMLAction != null) {
            // Insert the Export to Web item.
            exportItem = new JMenuItem(_exportHTMLAction);
            exportMenu.add(exportItem);
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

        FigureLayer fl = pane.getForegroundLayer();
        fl.setPickHalo(2);

        EventLayer fel = pane.getForegroundEventLayer();
        fel.setConsuming(false);
        fel.setEnabled(true);

        _mousePressedLayerAdapter = new MousePressedLayerAdapter();
        fel.addLayerListener(_mousePressedLayerAdapter);

        JGraph graph = new JGraph(pane);
        setJGraph(graph);
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
        NamedObj model = getModel();
        if (model != null) {
            SizeAttribute size = (SizeAttribute) model.getAttribute(
                    "_vergilSize", SizeAttribute.class);

            if (size == null) {
                size = new SizeAttribute(getModel(), "_vergilSize");
            }

            size.recordSize(_getRightComponent());
            return size;
        }
        return null;
    }

    /** Export the model into the writer with the given name. If
     *  the _query has a selected entry and it is true,
     *  then only the selected named objects are exported;
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
     *  @deprecated Use {@link #getLastDirectory()} instead
     */
    @Deprecated
    protected File _getDirectory() {
        return _getCurrentDirectory();
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
        for (Object element : selection) {
            if (element instanceof Figure) {
                Object userObject = ((Figure) element).getUserObject();

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

        for (Object element : selection) {
            if (element instanceof Figure) {
                Object userObject = ((Figure) element).getUserObject();

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

    /**
     * Initialize this BasicGraphFrame.
     * Derived classes may call this method in their constructors.
     * Derived classes should call the various _initBasicGraphFrame*() methods
     * so as to avoid code duplication
     */
    protected void _initBasicGraphFrame() {

        // WARNING: If you change this method, then Kepler will probably break.
        // kepler/gui/src/org/kepler/gui/KeplerGraphFrame.java extends BasicGraphFrame
        // and has an _initBasicGraphFrame() method.

        // To build Kepler under Eclipse, see
        // https://kepler-project.org/developers/reference/kepler-and-eclipse

        // This method calls a series of other protected methods whose
        // names start with _initBasicGraphFrame. These methods contain common
        // functionality between this class and KeplerGraphFrame so
        // that there is a chance that we avoid a ton of code
        // duplication.

        // Code that is different between this class and KeplerGraphFrame
        // appears below.

        // Eventually, perhaps the common functionality can be put into one
        // method.

        _initBasicGraphFrameInitialization();

        ActionListener deletionListener = new DeletionListener();

        _rightComponent.registerKeyboardAction(deletionListener, "Delete",
                KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        _rightComponent.registerKeyboardAction(deletionListener, "BackSpace",
                KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        _initBasicGraphFrameRightComponent();

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

        _initBasicGraphFrameRightComponentMouseListeners();

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
                _rightComponent.setPreferredSize(new Dimension(700, 500));
                _rightComponent.setSize(600, 450);
            }

            _initBasicGraphFrameSetZoomAndPan();
        } catch (Throwable throwable) {
            // Ignore problems here.  Errors simply result in a default
            // size and location.
        }

        // If we don't have a library, we might be trying to only show
        // models
        // FIXME: should we be checking for _library instead?
        if (configuration != null
                && (CompositeEntity) configuration.getEntity("actor library") != null) {
            // Create the panner.
            _graphPanner = new JCanvasPanner(getJGraph());
            _graphPanner.setPreferredSize(new Dimension(200, 150));
            // _graphPanner.setMaximumSize(new Dimension(200, 450));
            _graphPanner.setSize(200, 150);
            // NOTE: Border causes all kinds of problems!
            _graphPanner.setBorder(BorderFactory.createEtchedBorder());
        }

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
                    if (_topLibrary != null) {
                        gotLibrary = true;
                    }
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
        if (configuration != null
                && (CompositeEntity) configuration.getEntity("actor library") != null) {
            _libraryModel = new VisibleTreeModel(_topLibrary);
            // Second arguments prevents parameter values from showing in the library.
            _library = new PTree(_libraryModel, false);
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
            // See _treeViewScrollPane below.
            _libraryScrollPane.setMinimumSize(new Dimension(200, 200));
            _libraryScrollPane.setPreferredSize(new Dimension(200, 300));

            // create the palette on the left.
            _palettePane = new JPanel();
            _palettePane.setBorder(null);
            _palettePane.setLayout(new GridBagLayout());

            // create a query for search.
            JPanel findPanel = new JPanel(new GridBagLayout());

            // Put in the label.
            GridBagConstraints labelConstraints = new GridBagConstraints();
            labelConstraints.gridx = 0;
            labelConstraints.gridy = 0;
            JLabel label = new JLabel("Find:");
            findPanel.add(label, labelConstraints);

            // Put in the entry box.
            _findInLibraryEntryBox = new JTextField(12);
            _findInLibraryEntryBox.addActionListener(new FindInLibraryAction());
            GridBagConstraints entryBoxConstraints = new GridBagConstraints();
            entryBoxConstraints.gridx = 1;
            entryBoxConstraints.gridy = 0;
            entryBoxConstraints.fill = GridBagConstraints.HORIZONTAL;
            entryBoxConstraints.weightx = 1.0;
            findPanel.add(_findInLibraryEntryBox, entryBoxConstraints);

            // Put in the find panel.
            GridBagConstraints findPanelConstraints = new GridBagConstraints();
            findPanelConstraints.gridx = 0;
            findPanelConstraints.gridy = 0;
            findPanelConstraints.fill = GridBagConstraints.HORIZONTAL;
            _palettePane.add(findPanel, findPanelConstraints);

            // The Hierarchy Tree browser for CompositeEntities.
            NamedObj model = getModel();
            if (!(model instanceof CompositeEntity)) {
                // EditIconFrame will have a EditorIcon as a model, not a CompositeEntity.
                _treeViewScrollPane = null;
            } else {
                _treeViewModel = new ClassAndEntityTreeModel(
                        (CompositeEntity) getModel().toplevel());

                // Second arguments prevents parameter values from showing in the library,
                // I'm not sure if that is relevant for the hierarchy tree browser.
                _treeView = new PTree(_treeViewModel, false);
                // Replaced by mouse listener.
                // _treeView.addTreeSelectionListener(new HierarchyTreeSelectionListener());
                _treeView.addMouseListener(new HierarchyTreeMouseAdapter());
                _treeView.setBackground(BACKGROUND_COLOR);
                _treeView.setCellRenderer(new HierarchyTreeCellRenderer());

                _treeViewScrollPane = new JScrollPane(_treeView);
                // See _libraryScrollPane above.
                _treeViewScrollPane.setMinimumSize(new Dimension(200, 200));
                _treeViewScrollPane.setPreferredSize(new Dimension(200, 300));

                // Make the Ptolemy model visible in the tree.
                TreePath modelTreePath = null;
                {
                    // Traverse the Ptolemy model hierarchy, create a list, reverse it,
                    // create an array and then a TreePath.
                    List<NamedObj> compositeList = new LinkedList<NamedObj>();
                    NamedObj composite = getModel();
                    while (composite != null) {
                        compositeList.add(composite);
                        composite = composite.getContainer();
                    }
                    java.util.Collections.reverse(compositeList);
                    Object[] composites = compositeList.toArray();
                    modelTreePath = new TreePath(composites);
                }
                _treeView.expandPath(modelTreePath);
                _treeView.makeVisible(modelTreePath);
                _treeView.scrollPathToVisible(modelTreePath);
            }

            // Put in the tabbed pane that contains the hierarchy browser and the library
            JTabbedPane libraryTreeTabbedPane = new JTabbedPane();
            libraryTreeTabbedPane.add("Library", _libraryScrollPane);
            if (_treeViewScrollPane != null) {
                libraryTreeTabbedPane.add("Tree", _treeViewScrollPane);
            }

            GridBagConstraints tabbedPaneConstraints = new GridBagConstraints();
            tabbedPaneConstraints.gridx = 0;
            tabbedPaneConstraints.gridy = 1;
            tabbedPaneConstraints.fill = GridBagConstraints.BOTH;
            tabbedPaneConstraints.weightx = 1.0;
            tabbedPaneConstraints.weighty = 0.7;
            _palettePane.add(libraryTreeTabbedPane, tabbedPaneConstraints);

            // Add the graph panner.
            if (_graphPanner != null) {
                GridBagConstraints pannerConstraints = new GridBagConstraints();
                pannerConstraints.gridx = 0;
                pannerConstraints.gridy = 2;
                pannerConstraints.weighty = 0.3;
                pannerConstraints.fill = GridBagConstraints.BOTH;
                _palettePane.add(_graphPanner, pannerConstraints);
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
            throw new InternalErrorException(getTableau(), e,
                    "Unable to create tool bar.");
        }

        GUIUtilities.addToolBarButton(_toolbar, _saveAction);

        // Note that in Top we disable Print unless the class implements
        // the Printable or Pageable interfaces.  By definition, this class
        // implements the Printable interface
        GUIUtilities.addToolBarButton(_toolbar, _printAction);

        _initBasicGraphFrameToolBarZoomButtons();

        GUIUtilities.addToolBarButton(_toolbar, _openContainerAction);
        if (getModel() == getModel().toplevel()
                || getModel().getClass().getName()
                        .equals("ptolemy.domains.modal.modal.ModalController")) {
            // If we are at the top level, disable.  If we are in a
            // ModalModel, disable.  See "Up button does not work in
            // modal models"
            // https://chess.eecs.berkeley.edu/bugzilla/show_bug.cgi?id=323
            _openContainerAction.setEnabled(false);
        }

        _initBasicGraphFrameActions();

        // Add a weak reference to this to keep track of all
        // the graph frames that have been created.
        _openGraphFrames.add(this);
    }

    /** Add the cut, copy, paste, move to front, mode to back
     *  actions.  Also add the EditPreferencesAction and initialize
     *  the layout gui.
     *  Derived classes usually call this at the end of
     *  _initBasicGraphFrame().
     */
    protected void _initBasicGraphFrameActions() {
        _cutAction = new CutAction();
        _copyAction = new CopyAction();
        _pasteAction = new PasteAction();
        _findAction = new FindAction();

        // FIXME: vergil.kernel.AttributeController also defines context
        // menu choices that do the same thing.
        _moveToFrontAction = new MoveToFrontAction();
        _moveToBackAction = new MoveToBackAction();

        _editPreferencesAction = new EditPreferencesAction();

        _initLayoutGuiAction();
    }

    /** Set up the right component. */
    protected void _initBasicGraphFrameRightComponent() {
        _rightComponent.setRequestFocusEnabled(true);
        _rightComponent.setAlignmentX(1);
        _rightComponent.setAlignmentY(1);
    }

    /** Add listeners to the right component. */
    protected void _initBasicGraphFrameRightComponentMouseListeners() {
        _rightComponent.addMouseWheelListener(this);
        _rightComponent.addMouseMotionListener(this);
        _rightComponent.addMouseListener(this);
    }

    /** Common initialization for a BasicGraphFrame.
     *  Derived classes should call this method early in
     *  _initBasicGraphFrame().
     */
    protected void _initBasicGraphFrameInitialization() {
        getModel().addChangeListener(this);
        getContentPane().setLayout(new BorderLayout());
        _rightComponent = _createRightComponent(getModel());
    }

    /** Add tool bar buttons.
     *  Derived classes should set _toolbar before calling
     *  this method.
     */
    protected void _initBasicGraphFrameToolBarZoomButtons() {
        GUIUtilities.addToolBarButton(_toolbar, _zoomInAction);
        GUIUtilities.addToolBarButton(_toolbar, _zoomResetAction);
        GUIUtilities.addToolBarButton(_toolbar, _zoomFitAction);
        GUIUtilities.addToolBarButton(_toolbar, _zoomOutAction);
    }

    /** Set the zoom factor and the pan.
     *  @exception IllegalActionException If the zoom or pan parameters
     *  cannot be read.
     */
    protected void _initBasicGraphFrameSetZoomAndPan()
            throws IllegalActionException {

        // Set the zoom factor.
        Parameter zoom = (Parameter) getModel().getAttribute(
                "_vergilZoomFactor", Parameter.class);
        if (zoom != null) {
            zoom(((DoubleToken) zoom.getToken()).doubleValue());
            // Make sure the visibility is only expert.
            zoom.setVisibility(Settable.EXPERT);
        }

        // Set the pan position.
        Parameter pan = (Parameter) getModel().getAttribute("_vergilCenter",
                Parameter.class);

        if (pan != null) {
            ArrayToken panToken = (ArrayToken) pan.getToken();
            Point2D center = new Point2D.Double(
                    ((DoubleToken) panToken.getElement(0)).doubleValue(),
                    ((DoubleToken) panToken.getElement(1)).doubleValue());
            setCenter(center);

            // Make sure the visibility is only expert.
            pan.setVisibility(Settable.EXPERT);
        }

        // If we have neither zooming or panning info...
        if (zoom == null && pan == null) {
            // ...set the top left corner of the view to the top left corner of the model.
            // Note: This code only works for a zoom factor of 1.0, which is no problem at
            // this stage since that's the default and no zooming info was found in the model.
            GraphPane pane = getJGraph().getGraphPane();
            Rectangle2D bounds = pane.getForegroundLayer().getLayerBounds();
            Rectangle2D visible = getVisibleRectangle();

            double centerX = visible.getCenterX()
                    - (visible.getX() - bounds.getX());
            double centerY = visible.getCenterY()
                    - (visible.getY() - bounds.getY());

            // Set the new center point, but add a little free space between model and border
            setCenter(new Point2D.Double(centerX - 10.0, centerY - 10.0));
        }
    }

    /** Initialize the layout gui. */
    protected void _initLayoutGuiAction() {
        // Try to create an advanced layout action.
        final IGuiAction layoutGuiAction = _createLayoutAction();
        if (layoutGuiAction != null) {
            _layoutAction = new AbstractAction("Automatic Layout") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    layoutGuiAction.doAction(getModel());
                }
            };
            // The advanced layout action is available, so create the configuration
            // dialog for displaying layout parameters.
            _layoutConfigDialogAction = new LayoutConfigDialogAction();
        } else {
            // The advanced layout action is not available, so use the simple
            // Ptolemy layout algorithm.
            _layoutAction = new AbstractAction("Automatic Layout") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    new PtolemyLayoutAction().doAction(getModel());
                }
            };
        }
        _layoutAction.putValue("tooltip", "Layout the graph (Ctrl+T)");
        _layoutAction.putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke
                .getKeyStroke(KeyEvent.VK_T, Toolkit.getDefaultToolkit()
                        .getMenuShortcutKeyMask()));
        _layoutAction.putValue(GUIUtilities.MNEMONIC_KEY,
                Integer.valueOf(KeyEvent.VK_L));
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
     *  This overrides the base class so that if this is a design pattern
     *  and items are selected, then the user is asked if they
     *  want to save only the selected objects.
     *  <p>If {@link ptolemy.gui.PtGUIUtilities#useFileDialog()} returns true
     *  then {@link ptolemy.gui.Top#_saveAs()} uses this method.  Otherwise,
     *  {@link #_saveAsJFileChooserComponent()} is used.</p>
     *  @return A file dialog for save as.
     */
    @Override
    protected FileDialog _saveAsFileDialogComponent() {
        FileDialog fileDialog = super._saveAsFileDialogComponent();
        if (_isDesignPattern()) {
            if (!_getSelectionSet().isEmpty()) {
                // FIXME: It is not clear to me when this code would be called.
                // File -> New -> Ptera Model, then opening DesignPatterns,
                // dragging in a ListenToInput, selecting it and doing Save As
                // does not do it.

                _query = new Query();
                _query.addCheckBox("selected", "Selected objects only", true);
                // The problem here is that with FileDialog, we can't add the
                // query as an accessory like we can with JFileChooser.  So, we
                // pop up a check box dialog before bringing up the FileDialog.
                new ComponentDialog(this, "Save submodel only?", _query);
            }
        }

        return fileDialog;
    }

    /** Create and return a file dialog for the "Save As" command.
     *  This overrides the base class so that if this is a design pattern
     *  and items are selected, then the user is asked if they
     *  want to save only the selected objects.
     *  <p>If {@link ptolemy.gui.PtGUIUtilities#useFileDialog()} returns false
     *  then {@link ptolemy.gui.Top#_saveAs()} uses this method.  Otherwise,
     *  {@link #_saveAsFileDialogComponent()} is used.</p>

     *  @return A file dialog for save as.
     */
    @Override
    protected JFileChooser _saveAsJFileChooserComponent() {
        JFileChooser fileChooser = super._saveAsJFileChooserComponent();

        if (_isDesignPattern()) {
            if (_getSelectionSet().isEmpty()) {
                fileChooser.setAccessory(null);
            } else {
                _query = new Query();
                _query.addCheckBox("selected", "Selected objects only", true);
                fileChooser.setAccessory(_query);
            }
        }

        return fileChooser;
    }

    /** Set the directory that was last accessed by this window.
     *  @see #getLastDirectory
     *  @param directory The directory last accessed.
     *  @deprecated Use {@link #setDirectory(File)} instead
     */
    @Deprecated
    protected void _setDirectory(File directory) {
        setDirectory(directory);
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
    @Override
    protected void _writeFile(File file) throws IOException {
        try {
            updateWindowAttributes();
        } catch (KernelException ex) {
            // Ignore problems here.  Errors simply result in a
            // default size and location.
            System.out
                    .println("While writing, failed to save size, position or zoom factor: "
                            + ex);
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

    /** The copy action. */
    protected Action _copyAction;

    /** The cut action. */
    protected Action _cutAction;

    /** The default Library. **/
    protected LibraryAttribute _defaultLibrary;

    /** The instance of EditorDropTarget associated with the JGraph. */
    protected EditorDropTarget _dropTarget;

    /** The edit menu. */
    protected JMenu _editMenu;

    /** The action to edit preferences. */
    protected EditPreferencesAction _editPreferencesAction;

    /** The export to GIF action. */
    protected Action _exportGIFAction;

    /** The export HTML action. */
    protected Action _exportHTMLAction;

    /** The export to PDF action. */
    protected Action _exportPDFAction;

    /** The export to PNG action. */
    protected Action _exportPNGAction;

    /** The find action. */
    protected Action _findAction;

    /** The graph menu. */
    protected JMenu _graphMenu;

    /** The panner. Note that this variable
     *  can be null if the configuration does not have an entity named
     *  "actor library".  For example, see $PTII/bin/vergil -ptinyViewer.
     */
    protected JCanvasPanner _graphPanner;

    /** The instance of JGraph for this editor. */
    protected JGraph _jgraph;

    /** The action for automatically laying out the graph.
     *  This can be either an advanced layout or the simple Ptolemy layout,
     *  depending on whether the better one is available.
     */
    protected Action _layoutAction;

    /** The action for opening the layout configuration dialog.
     *  This reference can be {@code null}, since the dialog is only supported
     *  if advanced layout is available. In this case the action should not
     *  be shown in menus.
     */
    protected Action _layoutConfigDialogAction;

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

    /** List of references to graph frames that are open. */
    protected static LinkedList<BasicGraphFrame> _openGraphFrames = new LinkedList<BasicGraphFrame>();

    /** The library display panel. */
    protected JPanel _palettePane;

    /** The paste action. */
    protected Action _pasteAction;

    /** The right component for this editor. */
    protected JComponent _rightComponent;

    /** The split pane for library and editor. Note that this variable
     *  can be null if the configuration does not have an entity named
     *  "actor library".  For example, see $PTII/bin/vergil -ptinyViewer.
     */
    protected JSplitPane _splitPane;

    /** The toolbar. */
    protected JToolBar _toolbar;

    /** The library. */
    protected CompositeEntity _topLibrary;

    /** The tree view of the model, used for browsing large models. */
    protected PTree _treeView;

    /** The tree view scroll pane. */
    protected JScrollPane _treeViewScrollPane;

    /** The tree view  model. */
    protected ClassAndEntityTreeModel _treeViewModel;

    /** Action for zoom fitting. */
    protected Action _zoomFitAction = new ZoomFitAction("Zoom Fit");

    /** Action for zooming in. */
    protected Action _zoomInAction = new ZoomInAction("Zoom In");

    /** Action for zooming out. */
    protected Action _zoomOutAction = new ZoomOutAction("Zoom Out");

    /** Action for zoom reset. */
    protected Action _zoomResetAction = new ZoomResetAction("Zoom Reset");

    /** True if we are inside zoom().  Used by derived classes with scrollbars.
     */
    protected boolean _zoomFlag = false;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Create an action for advanced automatic layout, if possible.
     *
     * @return a layout action, or null if it cannot be created
     */
    private IGuiAction _createLayoutAction() {
        try {
            StringParameter layoutGraphActionParameter = (StringParameter) getConfiguration()
                    .getAttribute("_layoutGraphAction", StringParameter.class);
            if (layoutGraphActionParameter != null) {
                // Try to find the class given in the configuration.
                Class layoutGraphActionClass = Class
                        .forName(layoutGraphActionParameter.stringValue());

                // Try to create an instance using the default constructor.
                Object object = layoutGraphActionClass.getDeclaredConstructor()
                        .newInstance();

                if (object instanceof IGuiAction) {
                    // If the action is a filter and the model is set, ask the action
                    // whether is supports the model.
                    if (object instanceof Filter && getModel() != null) {
                        if (!((Filter) object).accept(getModel())) {
                            return null;
                        }
                    }

                    return (IGuiAction) object;
                }
            }
        } catch (Throwable throwable) {
            // Fail silently!
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The entry box for find in library. */
    JTextField _findInLibraryEntryBox;

    /** A layer adapter to handle the mousePressed event. */
    private MousePressedLayerAdapter _mousePressedLayerAdapter;

    /** Action for opening the container, moving uplevel. */
    private Action _openContainerAction = new OpenContainerAction(
            "Open the container");

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

    /** True if the message about problems reading
     *  _importActionClassNames has been printed.
     */
    private static boolean _printedImportActionClassNamesMessage = false;

    /** Action to redo the last undone MoML change. */
    private Action _redoAction = new RedoAction();

    /**  Action to save the model. */
    private Action _saveAction = new SaveAction("Save");

    /** Action to undo the last MoML change. */
    private Action _undoAction = new UndoAction();

    private static double _ZOOM_FIT_PADDING = 5.0;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** A Layer Adapter to handle the mousePressed layer event. */
    protected static class MousePressedLayerAdapter extends LayerAdapter {
        // FindBugs indicates that this should be a static class.

        /** Invoked when the mouse is pressed on a layer
         * or figure.
         */
        @Override
        public void mousePressed(LayerEvent event) {
            Component component = event.getComponent();

            if (!component.hasFocus()) {
                component.requestFocus();
            }
        }
    }

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
        @Override
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
        @Override
        public void actionPerformed(ActionEvent e) {
            cut();
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// DeletionListener

    /** An ActionListener for handling deletion events. */
    private class DeletionListener implements ActionListener {
        /** Delete any nodes or edges from the graph that are
         *  currently selected.  In addition, delete any edges
         *  that are connected to any deleted nodes.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            delete();
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
        @Override
        public JMenuItem create(final JContextMenu menu, final NamedObj object) {
            Action action = new GetDocumentationAction() {
                @Override
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

        @Override
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
    //// ExportImageAction

    /** Export an image of the model. */
    public class ExportImageAction extends AbstractAction {
        /** Create a new action to export an image.
         *  @param formatName The image format to be exported,
         *  currently, "GIF" and "PNG" are supported.
         */
        public ExportImageAction(String formatName) {
            super("Export " + formatName);
            _formatName = formatName.toLowerCase(Locale.getDefault());
            putValue("tooltip", "Export " + formatName + " image to a file.");
            // putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_G));
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                   ////

        /** Export an image.
         *
         *  <p> Under Mac OS X, use java.awt.FileDialog.
         *  Under other OS's, use javax.swing.JFileChooser. Under Mac OS
         *  X, see {@link ptolemy.gui.PtGUIUtilities#useFileDialog()} for
         *  how to select between the two.</p>
         *
         *  @param e The event that triggered this action.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooserBugFix jFileChooserBugFix = new JFileChooserBugFix();
            Color background = null;
            PtFileChooser ptFileChooser = null;
            try {
                background = jFileChooserBugFix.saveBackground();

                ptFileChooser = new PtFileChooser(BasicGraphFrame.this,
                        "Specify a " + _formatName + " file to be written.",
                        JFileChooser.SAVE_DIALOG);

                ptFileChooser.setSelectedFile(new File(getModel().getName()
                        + "." + _formatName));
                LinkedList extensions = new LinkedList();
                extensions.add(_formatName);
                ptFileChooser
                        .addChoosableFileFilter(new ExtensionFilenameFilter(
                                extensions));
                ptFileChooser.setCurrentDirectory(_directory);

                int returnVal = ptFileChooser.showDialog(
                        BasicGraphFrame.this,
                        "Export "
                                + _formatName.toUpperCase(Locale.getDefault()));

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    _directory = ptFileChooser.getCurrentDirectory();
                    File imageFile = ptFileChooser.getSelectedFile()
                            .getCanonicalFile();

                    if (imageFile.getName().indexOf(".") == -1) {
                        // If the user has not given the file an extension, add it
                        imageFile = new File(imageFile.getAbsolutePath() + "."
                                + _formatName);
                    }
                    // The Mac OS X FileDialog will ask if we want to save before this point.
                    if (imageFile.exists() && !PtGUIUtilities.useFileDialog()) {
                        if (!MessageHandler.yesNoQuestion("Overwrite \""
                                + imageFile.getName() + "\"?")) {
                            return;
                        }
                    }
                    OutputStream out = null;
                    try {
                        out = new FileOutputStream(imageFile);
                        getJGraph().exportImage(out, _formatName);
                    } finally {
                        if (out != null) {
                            out.close();
                        }
                    }

                    // Open the image file.
                    if (MessageHandler
                            .yesNoQuestion("Open \""
                                    + imageFile.getCanonicalPath()
                                    + "\" in a browser?")) {
                        Configuration configuration = getConfiguration();
                        try {
                            URL imageURL = new URL(imageFile.toURI().toURL()
                                    .toString()
                                    + "#in_browser");
                            configuration.openModel(imageURL, imageURL,
                                    imageURL.toExternalForm(),
                                    BrowserEffigy.staticFactory);
                        } catch (Throwable throwable) {
                            MessageHandler.error("Failed to open \""
                                    + imageFile.getName() + "\".", throwable);
                        }
                    }
                }
            } catch (Exception ex) {
                MessageHandler.error(
                        "Export to "
                                + _formatName.toUpperCase(Locale.getDefault())
                                + " failed", ex);
            } finally {
                jFileChooserBugFix.restoreBackground(background);
            }
        }

        private String _formatName;
    }

    ///////////////////////////////////////////////////////////////////
    //// ExportMapAction

    /** Accept only folders in a file browser. */
    static public class FolderFileFilter extends FileFilter {
        /** Accept only folders.
         *  @param fileOrDirectory The file or directory to be checked.
         *  @return true if the file is a directory.
         */
        @Override
        public boolean accept(File fileOrDirectory) {
            if (fileOrDirectory.isDirectory()) {
                return true;
            }
            return false;
        }

        /**  The description of this filter. */
        @Override
        public String getDescription() {
            return "Choose a Folder";
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// FindAction

    /** Action to search for text in a model. */
    protected class FindAction extends AbstractAction {
        /** Create a new action to search for text. */
        public FindAction() {
            super("Find");
            putValue("tooltip", "Find occurrences of specified text.");
            putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_F, Toolkit.getDefaultToolkit()
                            .getMenuShortcutKeyMask()));
            putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_F));
        }

        /** Open a dialog to find the specified text. */
        @Override
        public void actionPerformed(ActionEvent e) {
            DialogTableau dialogTableau = DialogTableau.createDialog(
                    BasicGraphFrame.this, getConfiguration(), getEffigy(),
                    SearchResultsDialog.class, (Entity) getModel());

            if (dialogTableau != null) {
                dialogTableau.show();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// FindInLibraryAction

    /** An ActionListener for handling deletion events. */
    private class FindInLibraryAction implements ActionListener {
        /** Delete any nodes or edges from the graph that are
         *  currently selected.  In addition, delete any edges
         *  that are connected to any deleted nodes.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            _library.clearSelection();
            String text = _findInLibraryEntryBox.getText().trim()
                    .toLowerCase(Locale.getDefault());
            if (text.equals("")) {
                // Nothing to search for. Ignore.
                _previousText = null;
                return;
            }
            NamedObj root = (NamedObj) _libraryModel.getRoot();
            if (!text.equals(_previousText)) {
                // Restart the search from the beginning.
                if (_stack == null) {
                    _stack = new Stack<NamedObj>();
                    _indexes = new Stack<Integer>();
                } else {
                    _stack.clear();
                    _indexes.clear();
                }
                _stack.push(root);
                _indexes.push(Integer.valueOf(1));
            }
            _previousText = text;
            try {
                // Indicate that something is happening with the cursor.
                // setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                _findInLibraryEntryBox.setCursor(Cursor
                        .getPredefinedCursor(Cursor.WAIT_CURSOR));
                _library.setCursor(Cursor
                        .getPredefinedCursor(Cursor.WAIT_CURSOR));

                // Put a message in the progress bar at the bottom of the window.
                // FIXME: Sadly, this doesn't work.
                report("Opening Libraries ...");

                // Traverse the model until we get a match.
                // _stack will be empty only when no further match is found.
                boolean foundOne = false;
                int index = 0;
                while (!_stack.isEmpty()) {
                    NamedObj parent = _stack.peek();
                    if (_findInSublibrary(text, parent, index)) {
                        // Found one. Stop the search.
                        foundOne = true;
                        break;
                    } else {
                        // Pop up one level and continue the search.
                        _stack.pop();
                        index = _indexes.pop();
                    }
                }
                if (!foundOne) {
                    // Reached the end of the library.
                    try {
                        if (MessageHandler
                                .yesNoCancelQuestion("Reached the end of the library. Start search again from the top?")) {
                            // Restart the search.
                            _stack.clear();
                            _indexes.clear();
                            _stack.push(root);
                            _indexes.push(Integer.valueOf(0));

                            actionPerformed(e);
                        }
                    } catch (CancelException e1) {
                        // Canceled by user.
                        return;
                    }
                }
            } finally {
                // Restore the cursor.
                // setCursor(Cursor.getDefaultCursor());
                _findInLibraryEntryBox.setCursor(Cursor.getDefaultCursor());
                _library.setCursor(Cursor.getDefaultCursor());

                // Clear the message in the progress bar at the bottom of the window.
                report("");
            }
        }

        /** Search the specified library for a name or class name that
         *  contains the specified text.
         *  @param text The text to search for.
         *  @param library The library to search.
         *  @param index The index at which to start the search.
         *  @return True if a match is found.
         */
        private boolean _findInSublibrary(String text, NamedObj library,
                int index) {
            int count = _libraryModel.getChildCount(library);
            for (int i = index; i < count; i++) {
                NamedObj candidate = (NamedObj) _libraryModel.getChild(library,
                        i);
                String name = candidate.getName();
                if (name.toLowerCase(Locale.getDefault()).contains(text)) {
                    // Found a match to the name.
                    _stack.push(candidate);
                    Object[] path = _stack.toArray();
                    TreePath pathToHit = new TreePath(path);
                    _library.makeVisible(pathToHit);
                    _library.scrollPathToVisible(pathToHit);
                    _library.addSelectionPath(pathToHit);
                    // Start the next search at the next item.
                    _indexes.push(Integer.valueOf(i + 1));
                    return true;
                }
                // Not a match. See whether any of its children are a match.
                int childCount = 0;
                ErrorHandler momlErrorHandler = MoMLParser.getErrorHandler();
                MoMLParser.setErrorHandler(new SimpleErrorHandler());
                MessageHandler messageHandler = MessageHandler
                        .getMessageHandler();
                MessageHandler.setMessageHandler(new SimpleMessageHandler());
                try {
                    childCount = _libraryModel.getChildCount(candidate);
                } catch (Throwable throwable) {
                    report("Skipping opening " + candidate.getName() + ": "
                            + throwable);
                } finally {
                    MoMLParser.setErrorHandler(momlErrorHandler);
                    MessageHandler.setMessageHandler(messageHandler);
                }
                if (!_libraryModel.isLeaf(candidate) && childCount > 0) {
                    _stack.push(candidate);
                    _indexes.push(Integer.valueOf(i + 1));
                    if (_findInSublibrary(text, candidate, 0)) {
                        return true;
                    } else {
                        _stack.pop();
                        _indexes.pop();
                    }
                }
            }
            return false;
        }

        private String _previousText;
        private Stack<NamedObj> _stack;
        private Stack<Integer> _indexes;
    }

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
    //// HierarchyTreeCellRenderer

    /** Render a cell in the model hierarchy tree.  The model being
     *  displayed is highlighted.
     */
    class HierarchyTreeCellRenderer extends PtolemyTreeCellRenderer {

        /** Create a new rendition for the given object.
         *  If the object is the same as the currently displayed Ptolemy
         *  model, then make it bold.
         */
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean sel, boolean expanded, boolean leaf, int row,
                boolean hasFocus) {

            DefaultTreeCellRenderer component = (DefaultTreeCellRenderer) super
                    .getTreeCellRendererComponent(tree, value, selected,
                            expanded, leaf, row, hasFocus);
            NamedObj model = getModel();
            if (model != null && component != null && model.equals(value)) {
                component.setText("<html><b>" + component.getText()
                        + "</b></html>");
            }
            return this;
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// HierarchyTreeSelectionListener

    /** The user selected a node in the Hierarchy tree browser */
    // Replaced by mouse listener
    /*
    private class HierarchyTreeSelectionListener implements
            TreeSelectionListener {
        // The value of the selection in the model hierarchy tree
        // browser changed.
        @Override
        public void valueChanged(TreeSelectionEvent event) {
            // Returns the last path element of the selection.
            // This method is useful only when the selection model allows a single selection.
            Object lastSelectedPathComponent = _treeView
                    .getLastSelectedPathComponent();
            if (lastSelectedPathComponent instanceof NamedObj) {
                try {
                    getConfiguration().openInstance(
                            (NamedObj) lastSelectedPathComponent);
                } catch (Throwable throwable) {
                    MessageHandler.error("Could not open "
                            + lastSelectedPathComponent, throwable);
                }
            }
        }
    }
        */

    ///////////////////////////////////////////////////////////////////
    //// HierarchyTreeMouseAdapter

    /** Listen for clicks of the mouse on the tree.
     */
    private class HierarchyTreeMouseAdapter extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
                if(e.getClickCount() == 2) {
                        // Returns the last path element of the selection.
                        // This method is useful only when the selection model allows a single selection.
                        Object lastSelectedPathComponent = _treeView
                                        .getLastSelectedPathComponent();
                        if (lastSelectedPathComponent instanceof NamedObj) {
                                try {
                                        getConfiguration().openInstance(
                                                        (NamedObj) lastSelectedPathComponent);
                                } catch (Throwable throwable) {
                                        MessageHandler.error("Could not open "
                                                        + lastSelectedPathComponent, throwable);
                                }
                        }
                }
        }
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

        @Override
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
                @Override
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

        @Override
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
                @Override
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
        @Override
        public void actionPerformed(ActionEvent e) {
            paste();
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
        @Override
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
        @Override
        public JMenuItem create(final JContextMenu menu, final NamedObj object) {
            Action action = new AbstractAction("Open for Editing") {
                @Override
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
        @Override
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
        @Override
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
        @Override
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
        @Override
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
        @Override
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
        @Override
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
        @Override
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
        @Override
        public void actionPerformed(ActionEvent e) {
            zoom(1.0 / 1.25);
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// LayoutConfigDialogAction

    /** Action to display a dialog for setting layout options. */
    private class LayoutConfigDialogAction extends AbstractAction {
        /** Create a new action to show the layout configuration dialog. */
        public LayoutConfigDialogAction() {
            super("Configure Layout...");
            putValue("tooltip",
                    "Set parameters for controlling the layout algorithm");
        }

        /** Show the layout configuration dialog. */
        @Override
        public void actionPerformed(ActionEvent e) {
            NamedObj model = getModel();
            Attribute attribute = model.getAttribute("_layoutConfiguration");
            if (attribute == null) {
                String momlChange = "<property name=\"_layoutConfiguration\" class=\"ptolemy.vergil.basic.layout.LayoutConfiguration\"/>";
                model.requestChange(new MoMLChangeRequest(this, model,
                        momlChange, false));
                attribute = model.getAttribute("_layoutConfiguration");
                if (attribute == null) {
                    MessageHandler
                            .error("Could not create the layout configuration attribute.");
                    return;
                }
            }
            new EditParametersDialog(BasicGraphFrame.this, attribute,
                    "Configure Layout Parameters");
        }
    }

}
