/* A simple graph view for Ptolemy models

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Red (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.basic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import ptolemy.actor.IOPort;
import ptolemy.actor.IORelation;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.PtolemyFrame;
import ptolemy.actor.gui.RunTableau;
import ptolemy.actor.gui.SizeAttribute;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.WindowPropertiesAttribute;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.undo.RedoChangeRequest;
import ptolemy.kernel.undo.UndoChangeRequest;
import ptolemy.kernel.undo.UndoStackAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.KernelRuntimeException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.LibraryAttribute;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.MoMLUndoEntry;
import ptolemy.util.CancelException;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;
import ptolemy.vergil.tree.EntityTreeModel;
import ptolemy.vergil.tree.PTree;
import ptolemy.vergil.tree.VisibleTreeModel;
import diva.canvas.CanvasUtilities;
import diva.canvas.Figure;
import diva.canvas.JCanvas;
import diva.canvas.Site;
import diva.canvas.connector.FixedNormalSite;
import diva.canvas.connector.Terminal;
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
import diva.gui.toolbox.FocusMouseListener;
import diva.gui.toolbox.JCanvasPanner;
import diva.util.java2d.ShapeUtilities;

//////////////////////////////////////////////////////////////////////////
//// BasicGraphFrame
/**
A simple graph view for ptolemy models.  This represents a level of the
hierarchy of a ptolemy model as a diva graph.  Cut, copy and paste operations
are supported using MoML.

@author  Steve Neuendorffer, Edward A. Lee
@version $Id$
@since Ptolemy II 2.0
*/
public abstract class BasicGraphFrame extends PtolemyFrame
    implements Printable, ClipboardOwner, ChangeListener {

    /** Construct a frame associated with the specified Ptolemy II model.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  This is typically done by calling show() on the controlling tableau.
     *  This constructor results in a graph frame that obtains its library
     *  either from the model (if it has one) or the default library defined
     *  in the configuration.
     *  @see Tableau#show()
     *  @param entity The model to put in this frame.
     *  @param tableau The tableau responsible for this frame.
     */
    public BasicGraphFrame(CompositeEntity entity, Tableau tableau) {
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
     *  @param entity The model to put in this frame.
     *  @param tableau The tableau responsible for this frame.
     *  @param defaultLibrary An attribute specifying the default library
     *   to use if the model does not have a library.
     */
    public BasicGraphFrame(
            CompositeEntity entity,
            Tableau tableau,
            LibraryAttribute defaultLibrary) {
        super(entity, tableau);

        entity.addChangeListener(this);

        getContentPane().setLayout(new BorderLayout());

        GraphPane pane = _createGraphPane();
        pane.getForegroundLayer().setPickHalo(2);

        _jgraph = new JGraph(pane);

        new EditorDropTarget(_jgraph);

        ActionListener deletionListener = new ActionListener() {
                /** Delete any nodes or edges from the graph that are currently
                 *  selected.  In addition, delete any edges that are connected to
                 *  any deleted nodes.
                 */
                public void actionPerformed(ActionEvent e) {
                    delete();
                }
            };

        _jgraph.registerKeyboardAction(deletionListener, "Delete",
                KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        _jgraph.setRequestFocusEnabled(true);
        _jgraph.addMouseListener(new FocusMouseListener());
        _jgraph.setAlignmentX(1);
        _jgraph.setAlignmentY(1);
        _jgraph.setBackground(BACKGROUND_COLOR);

        try {
            // The SizeAttribute property is used to specify the size
            // of the JGraph component. Unfortunately, with Swing's
            // mysterious and undocumented handling of component sizes,
            // there appears to be no way to control the size of the
            // JGraph from the size of the Frame, which is specified
            // by the WindowPropertiesAttribute.
            SizeAttribute size
                = (SizeAttribute)getModel().getAttribute(
                        "_vergilSize", SizeAttribute.class);
            if (size != null) {
                size.setSize(_jgraph);
            } else {
                // Set the default size.
                // Note that the location is of the frame, while the size
                // is of the scrollpane.
                _jgraph.setMinimumSize(new Dimension(200, 200));
                _jgraph.setPreferredSize(new Dimension(600, 400));
                _jgraph.setSize(600, 400);
            }
        } catch (Exception ex) {
            // Ignore problems here.  Errors simply result in a default
            // size and location.
        }

        // Create the panner.
        _graphPanner = new JCanvasPanner(_jgraph);
        _graphPanner.setPreferredSize(new Dimension(200, 150));
        _graphPanner.setMaximumSize(new Dimension(200, 150));
        _graphPanner.setSize(200, 150);
        // NOTE: Border causes all kinds of problems!
        // _graphPanner.setBorder(BorderFactory.createEtchedBorder());

        // Create the library of actors, or use the one in the entity,
        // if there is one.
        // FIXME: How do we make changes to the library persistent?
        boolean gotLibrary = false;
        try {
            LibraryAttribute libraryAttribute = (LibraryAttribute)
                entity.getAttribute("_library", LibraryAttribute.class);
            if (libraryAttribute != null) {
                // The model contains a library.
                _topLibrary = libraryAttribute.getLibrary();
                gotLibrary = true;
            }
        } catch (Exception ex) {
            try {
                MessageHandler.warning("Invalid library in the model.", ex);
            } catch (CancelException e) {}
        }
        if (!gotLibrary) {
            try {
                if (defaultLibrary != null) {
                    // A default library has been specified.
                    _topLibrary = defaultLibrary.getLibrary();
                    gotLibrary = true;
                }
            } catch (Exception ex) {
                try {
                    MessageHandler.warning(
                            "Invalid default library for the frame.", ex);
                } catch (CancelException e) {}
            }
        }
        if (!gotLibrary) {
            // Neither the model nor the argument have specified a library.
            // See if there is a default library in the configuration.
            Configuration configuration = getConfiguration();
            if (configuration != null) {
                _topLibrary = (CompositeEntity)
                    configuration.getEntity("actor library");
                if (_topLibrary == null) {
                    // Create an empty library by default.
                    Workspace workspace = entity.workspace();
                    _topLibrary = new CompositeEntity(workspace);
                    try {
                        _topLibrary.setName("topLibrary");
                        // Put a marker in so that this is
                        // recognized as a library.
                        new Attribute(_topLibrary, "_libraryMarker");
                    } catch (Exception ex) {
                        throw new InternalErrorException(
                                "Library configuration failed: " + ex);
                    }
                }
            }
        }

        _libraryModel = new VisibleTreeModel(_topLibrary);
        _library = new PTree(_libraryModel);
        _library.setRootVisible(false);
        _library.setBackground(BACKGROUND_COLOR);

        // If you want to expand the top-level libraries, uncomment this.
        /*
          Object[] path = new Object[2];
          path[0] = topLibrary;
          Iterator libraries = topLibrary.entityList().iterator();
          while (libraries.hasNext()) {
          path[1] = libraries.next();
          _library.expandPath(new TreePath(path));
          }
        */

        _libraryScrollPane = new JScrollPane(_library);
        _libraryScrollPane.setMinimumSize(new Dimension(200, 200));
        _libraryScrollPane.setPreferredSize(new Dimension(200, 200));

        // create the palette on the left.
        _palettePane = new JPanel();
        _palettePane.setBorder(null);
        _palettePane.setLayout(new BoxLayout(_palettePane, BoxLayout.Y_AXIS));

        _palettePane.add(_libraryScrollPane, BorderLayout.CENTER);
        _palettePane.add(_graphPanner, BorderLayout.SOUTH);

        _splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
        _splitPane.setLeftComponent(_palettePane);
        _splitPane.setRightComponent(_jgraph);
        getContentPane().add(_splitPane, BorderLayout.CENTER);

        // FIXME: hotkeys, shortcuts and move to a base class.
        _toolbar = new JToolBar();
        getContentPane().add(_toolbar, BorderLayout.NORTH);

        GUIUtilities.addToolBarButton(_toolbar, _zoomInAction);
        GUIUtilities.addToolBarButton(_toolbar, _zoomResetAction);
        GUIUtilities.addToolBarButton(_toolbar, _zoomFitAction);
        GUIUtilities.addToolBarButton(_toolbar, _zoomOutAction);

        _cutAction = new CutAction();
        _copyAction = new CopyAction();
        _pasteAction = new PasteAction();
        _createHierarchyAction = new CreateHierarchyAction();
        _layoutAction = new LayoutAction();
        _saveInLibraryAction = new SaveInLibraryAction();
        _importLibraryAction = new ImportLibraryAction();
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

    /** Get the currently selected objects from this document, if any,
     *  and place them on the clipboard in MoML format.
     */
    public void copy() {
        Clipboard clipboard =
            java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
        GraphPane graphPane = _jgraph.getGraphPane();
        GraphController controller =
            (GraphController)graphPane.getGraphController();
        SelectionModel model = controller.getSelectionModel();
        GraphModel graphModel = controller.getGraphModel();
        Object selection[] = model.getSelectionAsArray();
        // A set, because some objects may represent the same
        // ptolemy object.
        HashSet namedObjSet = new HashSet();
        HashSet nodeSet = new HashSet();
        // First get all the nodes.
        for (int i = 0; i < selection.length; i++) {
            if (selection[i] instanceof Figure) {
                Object userObject = ((Figure)selection[i]).getUserObject();
                if (graphModel.isNode(userObject)) {
                    nodeSet.add(userObject);
                    NamedObj actual =
                        (NamedObj)graphModel.getSemanticObject(userObject);
                    namedObjSet.add(actual);
                }
            }
        }
        for (int i = 0; i < selection.length; i++) {
            if (selection[i] instanceof Figure) {
                Object userObject = ((Figure)selection[i]).getUserObject();
                if (graphModel.isEdge(userObject)) {
                    // Check to see if the head and tail are both being
                    // copied.  Only if so, do we actually take the edge.
                    Object head = graphModel.getHead(userObject);
                    Object tail = graphModel.getTail(userObject);
                    boolean headOK = nodeSet.contains(head);
                    boolean tailOK = nodeSet.contains(tail);
                    Iterator objects = nodeSet.iterator();
                    while (!(headOK && tailOK) && objects.hasNext()) {
                        Object object = objects.next();
                        if (!headOK && GraphUtilities.isContainedNode(head,
                                object, graphModel)) {
                            headOK = true;
                        }
                        if (!tailOK && GraphUtilities.isContainedNode(tail,
                                object, graphModel)) {
                            tailOK = true;
                        }
                    }
                    if (headOK && tailOK) {
                        NamedObj actual =
                            (NamedObj)graphModel.getSemanticObject(userObject);
                        namedObjSet.add(actual);
                    }
                }
            }
        }
        StringWriter buffer = new StringWriter();
        try {
            Iterator elements = namedObjSet.iterator();
            while (elements.hasNext()) {
                NamedObj element = (NamedObj)elements.next();
                // first level to avoid obnoxiousness with
                // toplevel translations.
                element.exportMoML(buffer, 1);
            }
            CompositeEntity container = (CompositeEntity)graphModel.getRoot();
            buffer.write(container.exportLinks(1, namedObjSet));

            // The code below does not use a PtolemyTransferable,
            // to work around
            // a bug in the JDK that should be fixed as of jdk1.3.1.  The bug
            // is that cut and paste through the system clipboard to native
            // applications doesn't work unless you use string selection.
            clipboard.setContents(new StringSelection(buffer.toString()),
                    this);
        }
        catch (Exception ex) {
            MessageHandler.error("Copy failed", ex);
        }

    }

    /** Create a typed composite actor that contains the selected actors
     *  and connections. The created typed composite actor is transparent.
     *  The resulting topology is the same in the sense
     *  of deep connectivities.
     */
    public void createHierarchy() {
        GraphPane graphPane = _jgraph.getGraphPane();
        GraphController controller =
            (GraphController)graphPane.getGraphController();
        SelectionModel model = controller.getSelectionModel();
        GraphModel graphModel = controller.getGraphModel();
        Object selection[] = model.getSelectionAsArray();
        // A set, because some objects may represent the same
        // ptolemy object.
        HashSet namedObjSet = new HashSet();
        HashSet nodeSet = new HashSet();

        StringBuffer newPorts = new StringBuffer();
        StringBuffer extRelations = new StringBuffer();
        StringBuffer extConnections = new StringBuffer();
        StringBuffer intRelations = new StringBuffer();
        StringBuffer intConnections = new StringBuffer();

        // First get all the nodes.
        try {
            final CompositeEntity container =
                (CompositeEntity)graphModel.getRoot();
            final String name = container.uniqueName("typed composite actor");
            final TypedCompositeActor compositeActor = new TypedCompositeActor(
                    container, name);

            double[] location = new double[2];
            boolean gotLocation = false;
            for (int i = 0; i < selection.length; i++) {
                if (selection[i] instanceof Figure) {
                    if (!gotLocation) {
                        location[0] = ((Figure)selection[i]).getBounds().
                            getCenterX();
                        location[1] = ((Figure)selection[i]).getBounds().
                            getCenterY();
                        gotLocation = true;
                    }
                    Object userObject = ((Figure)selection[i]).getUserObject();
                    if (graphModel.isNode(userObject)) {
                        nodeSet.add(userObject);
                        NamedObj actual =
                            (NamedObj)graphModel.getSemanticObject(userObject);
                        namedObjSet.add(actual);
                    }
                }
            }

            for (int i = 0; i < selection.length; i++) {
                if (selection[i] instanceof Figure) {
                    Object userObject = ((Figure)selection[i]).getUserObject();
                    if (graphModel.isEdge(userObject)) {
                        // Check to see if the head and tail are both being
                        // selected.
                        Object head = graphModel.getHead(userObject);
                        //System.out.println("head:" +((NamedObj)head).getName());
                        Object tail = graphModel.getTail(userObject);
                        //System.out.println("tail:" +((NamedObj)tail).getName());

                        boolean headOK = nodeSet.contains(head);
                        boolean tailOK = nodeSet.contains(tail);
                        Iterator objects = nodeSet.iterator();
                        while (!(headOK && tailOK) && objects.hasNext()) {
                            Object object = objects.next();
                            if (!headOK && GraphUtilities.isContainedNode(head,
                                    object, graphModel)) {
                                headOK = true;
                            }
                            if (!tailOK && GraphUtilities.isContainedNode(tail,
                                    object, graphModel)) {
                                tailOK = true;
                            }
                        }
                        // For the edges at the boundary.
                        if ((!headOK && tailOK) || (headOK && !tailOK)) {
                            IOPort port = null;
                            IORelation relation = null;
                            boolean duplicateRelation = false;
                            if (head instanceof IOPort) {
                                port = (IOPort)head;
                                if (tail instanceof IOPort) {
                                    relation = (IORelation)graphModel.
                                        getSemanticObject(userObject);
                                    duplicateRelation = true;
                                } else {
                                    relation = (IORelation)graphModel.
                                        getSemanticObject(tail);
                                }
                            } else if (tail instanceof IOPort) {
                                port = (IOPort)tail;
                                relation = (IORelation)graphModel.
                                    getSemanticObject(head);
                            }
                            if (port != null) {
                                ComponentEntity entity = (ComponentEntity)
                                    ((IOPort)port).getContainer();
                                String portName = "port_" + i;
                                boolean isInput = ((IOPort)port).isInput();
                                boolean isOutput = ((IOPort)port).isOutput();
                                newPorts.append("<port name=\"" + portName +
                                        "\" class=\"ptolemy.actor.TypedIOPort"
                                        + "\">\n");
                                if (namedObjSet.contains(entity)) {
                                    // The port is inside the hierarchy.
                                    // The relation must be outside.
                                    // Create composite port.
                                    if (isInput) {
                                        newPorts.append(
                                                "<property name=\"input\"/>");
                                    }
                                    if (isOutput) {
                                        newPorts.append(
                                                "<property name=\"output\"/>");
                                    }
                                    newPorts.append("\n</port>\n");
                                    // Create internal relation and links.
                                    // Note we can only partially reuse
                                    // the relation name, one original relation
                                    // can be two internal relations.
                                    String relationName = relation.getName() + "_" + i;
                                    intRelations.append("<relation name=\"" +
                                            relationName + "\" class=\"" +
                                            "ptolemy.actor.TypedIORelation\"/>\n");
                                    intConnections.append("<link port=\"" +
                                            entity.getName() + "." + port.getName()
                                            + "\" relation=\"" +
                                            relationName + "\"/>\n");
                                    intConnections.append("<link port=\"" +
                                            portName + "\" relation=\"" +
                                            relationName + "\"/>\n");
                                    // Create external links.

                                    if (duplicateRelation) {
                                        extRelations.append("<relation name=\"" +
                                                relation.getName() + "\" class=\"" +
                                                "ptolemy.actor.TypedIORelation\"/>\n");
                                        IOPort otherPort = (IOPort)tail;
                                        ComponentEntity otherEntity =
                                            (ComponentEntity)otherPort.
                                            getContainer();
                                        if (otherEntity == container) {
                                            // This is a boundy port at a higher level.
                                            extConnections.append("<link port=\"" +
                                                    otherPort.getName() +
                                                    "\" relation=\"" +
                                                    relation.getName() + "\"/>\n");
                                        } else {
                                            extConnections.append("<link port=\"" +
                                                    otherEntity.getName() + "." +
                                                    otherPort.getName() +
                                                    "\" relation=\"" +
                                                    relation.getName() + "\"/>\n");
                                        }
                                    }

                                    extConnections.append("<link port=\"" +
                                            compositeActor.getName() + "."
                                            + portName + "\" relation=\"" +
                                            relation.getName() + "\"/>\n");
                                } else {
                                    // The port is outside the hierarchy.
                                    // The relation must be inside.
                                    if (isInput) {
                                        newPorts.append(
                                                "<property name=\"output\"/>");
                                    }
                                    if (isOutput) {
                                        newPorts.append(
                                                "<property name=\"input\"/>");
                                    }
                                    newPorts.append("\n</port>\n");

                                    String relationName = relation.getName() + "_" + i;
                                    extRelations.append("<relation name=\"" +
                                            relationName + "\" class=\"" +
                                            "ptolemy.actor.TypedIORelation\"/>\n");
                                    extConnections.append("<link port=\"" +
                                            entity.getName() + "." + port.getName()
                                            + "\" relation=\"" +
                                            relationName + "\"/>\n");
                                    extConnections.append("<link port=\"" +
                                            compositeActor.getName() + "."
                                            + portName + "\" relation=\"" +
                                            relationName + "\"/>\n");
                                    // Create external links.

                                    if (duplicateRelation) {
                                        intRelations.append("<relation name=\"" +
                                                relation.getName() + "\" class=\"" +
                                                "ptolemy.actor.TypedIORelation\"/>\n");
                                        IOPort otherPort = (IOPort)tail;
                                        ComponentEntity otherEntity =
                                            (ComponentEntity)otherPort.
                                            getContainer();
                                        intConnections.append("<link port=\"" +
                                                otherEntity.getName() + "." +
                                                otherPort.getName() +
                                                "\" relation=\"" +
                                                relation.getName() + "\"/>\n");
                                    }

                                    intConnections.append("<link port=\"" +
                                            portName + "\" relation=\"" +
                                            relation.getName() + "\"/>\n");
                                }
                            }
                        } else if (!headOK && !tailOK) {
                            // We only selected an edge. Build one input
                            // port, one output port for it, and build
                            // a direct connection.
                        }
                    }
                }
            }

            //System.out.println(" new port:" + newPorts);

            final Point2D point = new Point2D.Double();

            // Copy the selection.
            copy();
            _deleteWithoutUndo();

            // Create the MoML command.
            StringBuffer moml = new StringBuffer();
            // If the dropObj defers to something else, then we
            // have to check the parent of the object
            // for import attributes, and then we have to
            // generate import statements.  Note that everything
            // imported by the parent will be imported now by
            // the object into which this is dropped.
            moml.append("<group>\n");
            moml.append("<entity name=\"" + name + "\" class=\"ptolemy.actor"
                    + ".TypedCompositeActor\">\n");
            moml.append("\t<property name=\"_location\" class=\""
                    + "ptolemy.moml.Location\" value=\"" +
                    location[0] + ", " + location[1] + "\">\n");
            moml.append("\t</property>\n");
            moml.append(newPorts);

            // additional ports.
            Clipboard clipboard =
                java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable transferable = clipboard.getContents(this);
            try {
                moml.append((String)
                        transferable.getTransferData(DataFlavor.stringFlavor));
            } catch (Exception ex) {
                MessageHandler.error("Paste within Create Hierarchy failed",
                        ex);
            }

            // internal connections
            moml.append(intRelations);
            moml.append(intConnections);
            moml.append("</entity>\n");
            // external relations.
            moml.append(extRelations);
            moml.append(extConnections);
            // external connections.
            moml.append("</group>\n");
            //System.out.println(moml.toString());

            ChangeRequest request = null;

            request = new MoMLChangeRequest(
                    this, container, moml.toString()) {
                    protected void _execute() throws Exception {
                        super._execute();
                        NamedObj newObject = container.getEntity(name);
                        //_setLocation(compositeActor, point);
                    }
                };
            if (request != null) {
                container.requestChange(request);
            }
        } catch (Exception ex) {
            MessageHandler.error("Creating hierarchy failed", ex);
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

        GraphPane graphPane = _jgraph.getGraphPane();
        GraphController controller =
            (GraphController)graphPane.getGraphController();
        SelectionModel model = controller.getSelectionModel();

        AbstractBasicGraphModel graphModel =
            (AbstractBasicGraphModel)controller.getGraphModel();
        Object selection[] = model.getSelectionAsArray();

        Object userObjects[] = new Object[selection.length];
        // First remove the selection.
        for (int i = 0; i < selection.length; i++) {
            userObjects[i] = ((Figure)selection[i]).getUserObject();
            model.removeSelection(selection[i]);
        }

        // Holds a set of those elements whose deletion goes through MoML.
        // This is the large majority of deleted objects.
        // Currently the only exception are links from a port to nowhere.
        HashSet namedObjNodeSet = new HashSet();
        HashSet namedObjEdgeSet = new HashSet();
        // Holds those elements whose deletion does not go through MoML. This
        // is only links which are no connected to another port or a relation.
        HashSet edgeSet = new HashSet();

        // Generate the MoML to carry out the deletion
        StringBuffer moml = new StringBuffer();
        moml.append("<group>\n");

        // Delete edges then nodes, since deleting relations may
        // result in deleting links to that relation.
        for (int i = 0; i < selection.length; i++) {
            Object userObject = userObjects[i];
            if (graphModel.isEdge(userObject)) {
                NamedObj actual =
                    (NamedObj)graphModel.getSemanticObject(userObject);
                if (actual != null) {
                    moml.append(graphModel.getDeleteEdgeMoML(userObject));
                } else {
                    edgeSet.add(userObject);
                }
            }
        }
        for (int i = 0; i < selection.length; i++) {
            Object userObject = userObjects[i];
            if (graphModel.isNode(userObject)) {
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
            Iterator edges = edgeSet.iterator();
            while (edges.hasNext()) {
                Object nextEdge = edges.next();
                if (graphModel.isEdge(nextEdge)) {
                    graphModel.disconnectEdge(this, nextEdge);
                }
            }
        }
        finally {
            graphModel.setDispatchEnabled(true);
        }

        // Next process the deletion MoML. This should be the large majority
        // of most deletions.
        try {
            // Finally create and request the change
            NamedObj object = (NamedObj)graphModel.getRoot();
            NamedObj container = MoMLChangeRequest.getDeferredToParent(object);
            if (container == null) {
                container = (NamedObj)object.getContainer();
            }
            if (container == null) {
                container = object;
            }

            CompositeEntity toplevel = (CompositeEntity)container;
            MoMLChangeRequest change =
                new MoMLChangeRequest(this, toplevel, moml.toString());
            change.setUndoable(true);
            toplevel.requestChange(change);
        }
        catch (Exception ex) {
            MessageHandler.error("Delete failed, changeRequest was:" + moml,
                    ex);
        }
        graphModel.dispatchGraphEvent(
                new GraphEvent(
                        this,
                        GraphEvent.STRUCTURE_CHANGED,
                        graphModel.getRoot()));

    }

    /** Delete the currently selected objects from this document.
     */
    public void deleteOld() {
        // Note that we previously a delete was handled at the model level.
        // Now a delete is handled by generating MoML to carry out the delete
        // and handing that MoML to the parser

        GraphPane graphPane = _jgraph.getGraphPane();
        GraphController controller =
            (GraphController)graphPane.getGraphController();
        SelectionModel model = controller.getSelectionModel();
        // AbstractPtolemyGraphModel graphModel =
        //    (AbstractPtolemyGraphModel)controller.getGraphModel();
        AbstractBasicGraphModel graphModel =
            (AbstractBasicGraphModel)controller.getGraphModel();
        Object selection[] = model.getSelectionAsArray();

        Object userObjects[] = new Object[selection.length];
        // First remove the selection.
        for (int i = 0; i < selection.length; i++) {
            userObjects[i] = ((Figure)selection[i]).getUserObject();
            model.removeSelection(selection[i]);
        }

        // Holds a set of those elements whose deletion goes through MoML.
        // This is the large majority of deleted objects.
        // Currently the only exception are links from a port to nowhere.
        HashSet namedObjNodeSet = new HashSet();
        HashSet namedObjEdgeSet = new HashSet();
        // Holds those elements whose deletion does no go through MoML. This
        // is only links which are no connected to another port or a relation.
        HashSet edgeSet = new HashSet();
        // First make a set of all the semantic objects as they may
        // appear more than once
        for (int i = 0; i < selection.length; i++) {
            Object userObject = userObjects[i];
            if (graphModel.isEdge(userObject) ||
                    graphModel.isNode(userObject)) {
                NamedObj actual =
                    (NamedObj)graphModel.getSemanticObject(userObject);
                if (actual != null) {
                    if (graphModel.isEdge(userObject)) {
                        namedObjEdgeSet.add(actual);
                    } else {
                        namedObjNodeSet.add(actual);
                    }
                }
                else {
                    // Special case, do not handle through MoML but
                    // simply delete which means this deletion is not
                    // undoable
                    edgeSet.add(userObject);
                }
            }

        }
        // Merge the two hashsets so that any edges get deleted first.
        // This is need to avoid problems with relations not existing due to
        // the way some relations are hidden and ports are shown directly
        // connected

        // Formerly, we used an ArrayList here, but this caused problems
        // if we had a relation between two links and the user selected
        // the relation and a link by dragging a box.
        // JDK1.4 provides us with a LinkedHashSet that is ordered and
        // has unique elements, so we use that. -cxh 11/16/02
        //
        //ArrayList namedObjList = new ArrayList(namedObjEdgeSet);
        //namedObjList.addAll(namedObjNodeSet);

        LinkedHashSet namedObjList = new LinkedHashSet(namedObjEdgeSet);
        namedObjList.addAll(namedObjNodeSet);

        // Generate the MoML to carry out the deletion
        StringBuffer moml = new StringBuffer();
        moml.append("<group>\n");
        Iterator elements = namedObjList.iterator();
        while (elements.hasNext()) {
            NamedObj element = (NamedObj)elements.next();
            String deleteElemName = "";
            if (element instanceof Relation) {
                deleteElemName = "deleteRelation";
            }
            else if (element instanceof Entity) {
                deleteElemName = "deleteEntity";
            }
            else if (element instanceof Attribute) {
                deleteElemName = "deleteProperty";
            }
            else if (element instanceof Port) {
                deleteElemName = "deletePort";
            }
            else {
                // What else is there?
            }
            if (deleteElemName.length() > 0) {
                moml.append("<" + deleteElemName + " name=\"" +
                        element.getName() + "\" />\n");
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
            Iterator edges = edgeSet.iterator();
            while (edges.hasNext()) {
                Object nextEdge = edges.next();
                if (graphModel.isEdge(nextEdge)) {
                    graphModel.disconnectEdge(this, nextEdge);
                }
            }
        }
        finally {
            graphModel.setDispatchEnabled(true);
            /*graphModel.dispatchGraphEvent(new GraphEvent(
              this,
              GraphEvent.STRUCTURE_CHANGED,
              graphModel.getRoot()));*/
        }

        // Next process the deletion MoML. This should be the large majority
        // of most deletions.
        try {
            // Finally create and request the change
            CompositeEntity toplevel = (CompositeEntity)graphModel.getRoot();
            MoMLChangeRequest change =
                new MoMLChangeRequest(this, toplevel, moml.toString());
            change.setUndoable(true);
            toplevel.requestChange(change);
        }
        catch (Exception ex) {
            MessageHandler.error("Delete failed, changeRequest was:" + moml,
                    ex);
        }
    }

    /** Override the dispose method to unattach any listeners that may keep
     *  this model from getting garbage collected.
     */
    public void dispose() {
        // Remove the association with the library. This is necessary to allow
        // this frame, and the rest of the model to be properly garbage
        // collected
        _libraryModel.setRoot(null);
        super.dispose();
    }

    /** Return the center location of the visible part of the pane.
     *  @return The center of the visible part.
     */
    public Point2D getCenter() {
        Rectangle2D rect = getVisibleCanvasRectangle();
        return new Point2D.Double(rect.getCenterX(), rect.getCenterY());
    }

    /** Return the jgraph instance that this view uses to represent the
     *  ptolemy model.
     */
    public JGraph getJGraph() {
        return _jgraph;
    }

    /** Return the rectangle representing the visible part of the
     *  pane, transformed into canvas coordinates.  This is the range
     *  of locations that are visible, given the current pan and zoom.
     *  @return The rectangle representing the visible part.
     */
    public Rectangle2D getVisibleCanvasRectangle() {
        AffineTransform current =
            _jgraph.getCanvasPane().getTransformContext().getTransform();
        AffineTransform inverse;
        try {
            inverse = current.createInverse();
        }
        catch(NoninvertibleTransformException e) {
            throw new RuntimeException(e.toString());
        }
        Rectangle2D visibleRect = getVisibleRectangle();

        return ShapeUtilities.transformBounds(visibleRect,
                inverse);
    }

    /** Return the rectangle representing the visible part of the
     *  pane, in pixel coordinates on the screen.
     *  @return A rectangle whose upper left corner is at (0, 0) and whose
     *  size is the size of the canvas component.
     */
    public Rectangle2D getVisibleRectangle() {
        Dimension size = _jgraph.getSize();
        return new Rectangle2D.Double(0, 0,
                size.getWidth(), size.getHeight());
    }

    /** Layout the graph view.
     */
    public void layoutGraph() {
        GraphController controller =
            _jgraph.getGraphPane().getGraphController();
        LayoutTarget target = new PtolemyLayoutTarget(controller);
        //GraphModel model = controller.getGraphModel();
        AbstractBasicGraphModel model =
            (AbstractBasicGraphModel)controller.getGraphModel();
        PtolemyLayout layout = new PtolemyLayout(target);
        layout.setOrientation(LevelLayout.HORIZONTAL);
        layout.setRandomizedPlacement(false);

        // Before doing the layout, need to take a copy of all the current
        // node locations  which can be used to undo the effects of the move.
        try {
            CompositeEntity composite = model.getPtolemyModel();
            StringBuffer moml = new StringBuffer();
            moml.append("<group>\n");
            // NOTE: this gives at iteration over locations.
            Iterator nodes = model.nodes(composite);
            while (nodes.hasNext()) {
                Location location = (Location)nodes.next();
                // Get the containing element
                NamedObj element = (NamedObj)location.getContainer();
                // Give default values in case the previous locations value
                // has not yet been set
                String expression = location.getExpression();
                if (expression == null) {
                    expression = "0, 0";
                }
                // Create the MoML, wrapping the location attribute
                // in an element refering to the container
                String containingElementName = element.getMoMLInfo().elementName;
                moml.append("<" + containingElementName + " name=\"" +
                        element.getName() + "\" >\n");
                // NOTE: use the moml info element name here in case the
                // location is a vertex
                moml.append("<" + location.getMoMLInfo().elementName + " name=\"" +
                        location.getName() + "\" value=\"" + expression + "\" />\n");
                moml.append("</" + containingElementName + ">\n");
            }
            moml.append("</group>\n");

            // Push the undo entry onto the stack
            MoMLUndoEntry undoEntry = new MoMLUndoEntry(composite, moml.toString());
            UndoStackAttribute undoInfo = UndoStackAttribute.getUndoInfo(composite);
            undoInfo.push(undoEntry);
        } catch (Exception e) {
            // operation not undoable
        }

        // Perform the layout and repaint
        layout.layout(model.getRoot());
        _jgraph.repaint();
        _graphPanner.repaint();
    }

    /** Do nothing.
     */
    public void lostOwnership(Clipboard clipboard,
            Transferable transferable) {
    }

    /** Assuming the contents of the clipboard is MoML code, paste it into
     *  the current model by issuing a change request.
     */
    public void paste() {
        Clipboard clipboard =
            java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable transferable = clipboard.getContents(this);
        GraphPane graphPane = _jgraph.getGraphPane();
        GraphController controller =
            (GraphController)graphPane.getGraphController();
        GraphModel model = controller.getGraphModel();
        if (transferable == null) return;
        try {
            CompositeEntity toplevel = (CompositeEntity)model.getRoot();
            NamedObj container =
                MoMLChangeRequest.getDeferredToParent(toplevel);
            if (container == null) {
                container = toplevel;
            }
            StringBuffer moml = new StringBuffer();
            // The pasted version will have the names generated by the
            // uniqueName() method of the container, to ensure that they
            // do not collide with objects already in the container.
            if (container != toplevel) {
                moml.append("<entity name=\"" +
                        toplevel.getName(container) + "\">\n");
            }
            moml.append("<group name=\"auto\">\n");
            moml.append((String)
                    transferable.getTransferData(DataFlavor.stringFlavor));
            moml.append("</group>\n");
            if (container != toplevel) {
                moml.append("</entity>");
            }

            MoMLChangeRequest change =
                new MoMLChangeRequest(this, container, moml.toString());
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
    public int print(Graphics graphics, PageFormat format,
            int index) throws PrinterException {
        if (_jgraph != null) {
            Rectangle2D view = getVisibleRectangle();
            return _jgraph.print(graphics, format, index, view);
        } else {
            return NO_SUCH_PAGE;
        }
    }

    /**
     *  Redo the last undone change on the model
     */
    public void redo() {
        GraphPane graphPane = _jgraph.getGraphPane();
        GraphController controller =
            (GraphController)graphPane.getGraphController();
        GraphModel model = controller.getGraphModel();
        try {
            CompositeEntity toplevel = (CompositeEntity)model.getRoot();
            RedoChangeRequest change =
                new RedoChangeRequest(this, toplevel);
            toplevel.requestChange(change);
        }
        catch (Exception ex) {
            MessageHandler.error("Redo failed", ex);
        }
    }

    /** Save the given entity in the user library in the given
     *  configuration.
     *  @param entity The entity to save.
     *  @since Ptolemy 2.1
     */
    public static void saveComponentInLibrary(Configuration configuration,
            Entity entity) {
        try {
            CompositeEntity library = (CompositeEntity)
                configuration.getEntity("actor library."
                        + VERGIL_USER_LIBRARY_NAME);
            if (library == null) {
                MessageHandler.error(
                        "Save In Library failed: " +
                        "Could not find user library with name \"" +
                        VERGIL_USER_LIBRARY_NAME + "\".");
                return;
            }
            configuration.openModel(library);

            StringWriter buffer = new StringWriter();

            // Check if there is already something existing in the
            // user library with this name.
            if (library.getEntity(entity.getName()) != null) {
                MessageHandler.error(
                        "Save In Library failed: An object" +
                        " already exists in the user library with name " +
                        "\"" + entity.getName() + "\".");
                return;
            }
            entity.exportMoML(buffer, 1);

            ChangeRequest request =
                new MoMLChangeRequest(entity, library, buffer.toString());
            library.requestChange(request);
        }
        catch (IOException ex) {
            // Ignore.
        }
        catch (KernelException ex) {
            // Ignore.
        }
    }

    /** Set the center location of the visible part of the pane.
     *  This will cause the panner to center on the specified location
     *  with the current zoom factor.
     *  @param center The center of the visible part.
     */
    public void setCenter(Point2D center) {
        Rectangle2D visibleRect = getVisibleCanvasRectangle();
        AffineTransform newTransform =
            _jgraph.getCanvasPane().getTransformContext().getTransform();

        newTransform.translate(visibleRect.getCenterX() - center.getX(),
                visibleRect.getCenterY() - center.getY());

        _jgraph.getCanvasPane().setTransform(newTransform);
    }


    /**
     *  Undo the last undoable change on the model
     */
    public void undo() {
        GraphPane graphPane = _jgraph.getGraphPane();
        GraphController controller =
            (GraphController)graphPane.getGraphController();
        GraphModel model = controller.getGraphModel();
        try {
            CompositeEntity toplevel = (CompositeEntity)model.getRoot();
            UndoChangeRequest change =
                new UndoChangeRequest(this, toplevel);
            toplevel.requestChange(change);
        }
        catch (Exception ex) {
            MessageHandler.error("Undo failed", ex);
        }
    }

    /** Zoom in or out to magnify by the specified factor, from the current
     *  magnification.
     *  @param factor The magnification factor (relative to 1.0).
     */
    public void zoom(double factor) {
        JCanvas canvas = _jgraph.getGraphPane().getCanvas();
        AffineTransform current =
            canvas.getCanvasPane().getTransformContext().getTransform();
        // Save the center, so we remember what we were looking at.
        Point2D center = getCenter();
        current.scale(factor, factor);
        canvas.getCanvasPane().setTransform(current);
        // Reset the center.
        setCenter(center);
        _graphPanner.repaint();
    }

    /** Zoom to fit the current figures.
     */
    public void zoomFit() {
        GraphPane pane = _jgraph.getGraphPane();
        Rectangle2D bounds = pane.getForegroundLayer().getLayerBounds();
        if (bounds.isEmpty()) {
            // Empty diagram.
            return;
        }
        Rectangle2D viewSize = getVisibleRectangle();
        AffineTransform newTransform =
            CanvasUtilities.computeFitTransform(bounds, viewSize);
        JCanvas canvas = pane.getCanvas();
        canvas.getCanvasPane().setTransform(newTransform);
        _graphPanner.repaint();
    }

    /** Set zoom to the nominal.
     */
    public void zoomReset() {
        JCanvas canvas = _jgraph.getGraphPane().getCanvas();
        AffineTransform current =
            canvas.getCanvasPane().getTransformContext().getTransform();
        current.setToIdentity();
        canvas.getCanvasPane().setTransform(current);
        _graphPanner.repaint();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The name of the user library.  The default value is
     *  "user library".  The value of this variable is what appears
     *  in the Vergil left hand tree menu.
     */
    public static String VERGIL_USER_LIBRARY_NAME = "user library";

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the menus that are used by this frame.
     */
    protected void _addMenus() {
        super._addMenus();

        _editMenu = new JMenu("Edit");
        _editMenu.setMnemonic(KeyEvent.VK_E);
        _menubar.add(_editMenu);
        // Add the undo action, followed by a separator then the editing actions
        diva.gui.GUIUtilities.addHotKey(_jgraph, _undoAction);
        diva.gui.GUIUtilities.addMenuItem(_editMenu, _undoAction);
        diva.gui.GUIUtilities.addHotKey(_jgraph, _redoAction);
        diva.gui.GUIUtilities.addMenuItem(_editMenu, _redoAction);
        _editMenu.addSeparator();
        GUIUtilities.addHotKey(_jgraph, _cutAction);
        GUIUtilities.addMenuItem(_editMenu, _cutAction);
        GUIUtilities.addHotKey(_jgraph, _copyAction);
        GUIUtilities.addMenuItem(_editMenu, _copyAction);
        GUIUtilities.addHotKey(_jgraph, _pasteAction);
        GUIUtilities.addMenuItem(_editMenu, _pasteAction);

        // Hot key for configure (edit parameters).
        GUIUtilities.addHotKey(_jgraph, BasicGraphController._configureAction);

        // May be null if there are not multiple views in the configuration.
        if (_viewMenu == null) {
            _viewMenu = new JMenu("View");
            _viewMenu.setMnemonic(KeyEvent.VK_V);
            _menubar.add(_viewMenu);
        } else {
            _viewMenu.addSeparator();
        }
        GUIUtilities.addHotKey(_jgraph, _zoomInAction);
        GUIUtilities.addMenuItem(_viewMenu, _zoomInAction);
        GUIUtilities.addHotKey(_jgraph, _zoomResetAction);
        GUIUtilities.addMenuItem(_viewMenu, _zoomResetAction);
        GUIUtilities.addHotKey(_jgraph, _zoomFitAction);
        GUIUtilities.addMenuItem(_viewMenu, _zoomFitAction);
        GUIUtilities.addHotKey(_jgraph, _zoomOutAction);
        GUIUtilities.addMenuItem(_viewMenu, _zoomOutAction);

        _graphMenu = new JMenu("Graph");
        _graphMenu.setMnemonic(KeyEvent.VK_G);
        _menubar.add(_graphMenu);
        GUIUtilities.addHotKey(_jgraph, _layoutAction);
        GUIUtilities.addMenuItem(_graphMenu, _layoutAction);
        GUIUtilities.addHotKey(_jgraph, _saveInLibraryAction);
        GUIUtilities.addMenuItem(_graphMenu, _saveInLibraryAction);
        GUIUtilities.addHotKey(_jgraph, _importLibraryAction);
        GUIUtilities.addMenuItem(_graphMenu, _importLibraryAction);
        _graphMenu.addSeparator();
        diva.gui.GUIUtilities.addHotKey(_jgraph, _createHierarchyAction);
        diva.gui.GUIUtilities.addMenuItem(_graphMenu, _createHierarchyAction);
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
            GraphModel gm = _jgraph.getGraphPane().getGraphModel();
            if (gm instanceof AbstractBasicGraphModel) {
                ((AbstractBasicGraphModel)gm).removeListeners();
            }
        }
        return result;
    }

    /** Create a new graph pane.  Subclasses will override this to change
     *  the pane that is created.  Note that this method is called in
     *  constructor, so derived classes must be careful to not reference
     *  local variables that may not have yet been created.
     *  @return The pane that is created.
     */
    protected abstract GraphPane _createGraphPane();

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

    /** Query the user for a filename and save the model to that file.
     *  This overrides the base class so that if we are in
     *  an inside composite actor, then only that composite actor is
     *  saved.  In addition, since the superclass clones the model,
     *  we need to clear and reconstruct the model.
     *  @return True if the save succeeds.
     */
    protected boolean _saveAs() {
        try {
            _saveAsFlag = true;
            return super._saveAs();
        } finally {
            _saveAsFlag = false;
        }
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
            Component component = _jgraph.getParent();
            Component parent = component.getParent();
            while (parent != null && !(parent instanceof Frame)) {
                component = parent;
                parent = component.getParent();
            }
            // If there is no parent that is a Frame, do nothing.
            if (parent instanceof Frame) {
                WindowPropertiesAttribute properties
                    = (WindowPropertiesAttribute)getModel().getAttribute(
                            "_windowProperties", WindowPropertiesAttribute.class);
                if (properties == null) {
                    properties = new WindowPropertiesAttribute(
                            getModel(), "_windowProperties");
                }
                properties.recordProperties((Frame)parent);
            }
            // Have to also record the size of the JGraph because
            // setting the size of the frame is ignored if we don't
            // also set the size of the JGraph. Why? Who knows. Swing.
            SizeAttribute size = (
                    SizeAttribute)getModel().getAttribute(
                            "_vergilSize", SizeAttribute.class);
            if (size == null) {
                size = new SizeAttribute(getModel(), "_vergilSize");
            }
            size.recordSize(_jgraph);
        } catch (Exception ex) {
            // Ignore problems here.  Errors simply result in a default
            // size and location.
        }
        // NOTE: This used to override the base class so that saveAs
        // on a submodel would save only the submodel.  But this was
        // strange, to have behavior different from save, and also it
        // broke save for top-level modal models.  So now we just do
        // the same thing in saveAs as in save.
        /*
          if (_saveAsFlag && getModel().getContainer() != null) {
          java.io.FileWriter fout = new java.io.FileWriter(file);
          getModel().exportMoML(fout);
          fout.close();
          } else {
          super._writeFile(file);
          }
        */

        super._writeFile(file);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The library. */
    protected CompositeEntity _topLibrary;

    // FIXME: Comments are needed on all these.
    // FIXME: Need to be in alphabetical order.

    // NOTE: should be somewhere else?
    // Default background color is a light grey.
    protected static Color BACKGROUND_COLOR = new Color(0xe5e5e5);

    protected JGraph _jgraph;
    protected JCanvasPanner _graphPanner;
    protected JTree _library;
    protected EntityTreeModel _libraryModel;
    protected JScrollPane _libraryScrollPane;
    protected JPanel _palettePane;
    protected JSplitPane _splitPane;

    protected JToolBar _toolbar;
    protected JMenu _editMenu;
    protected Action _cutAction;
    protected Action _copyAction;
    protected Action _pasteAction;
    /** action for creating a level of hierarchy. */
    protected Action _createHierarchyAction;
    protected JMenu _graphMenu;
    protected Action _layoutAction;
    protected Action _saveInLibraryAction;
    protected Action _importLibraryAction;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Delete the currently selected objects from this document without
     *  undo
     */
    public void _deleteWithoutUndo() {
        //FIXME: This is the old delete() method, before undo was added
        // createHierarch() calls this method.
        GraphPane graphPane = _jgraph.getGraphPane();
        GraphController controller =
            (GraphController)graphPane.getGraphController();
        AbstractBasicGraphModel graphModel =
            (AbstractBasicGraphModel)controller.getGraphModel();
        // Note that we turn off event dispatching so that each individual
        // removal does not trigger graph redrawing.
        try {
            graphModel.setDispatchEnabled(false);
            SelectionModel model = controller.getSelectionModel();
            Object selection[] = model.getSelectionAsArray();
            Object userObjects[] = new Object[selection.length];
            // First remove the selection.
            for (int i = 0; i < selection.length; i++) {
                userObjects[i] = ((Figure)selection[i]).getUserObject();
                model.removeSelection(selection[i]);
            }

            // Remove all the edges first,
            // since if we remove the nodes first,
            // then removing the nodes might remove some of the edges.
            for (int i = 0; i < userObjects.length; i++) {
                Object userObject = userObjects[i];
                if (graphModel.isEdge(userObject)) {
                    graphModel.disconnectEdge(this, userObject);
                }
            }
            for (int i = 0; i < selection.length; i++) {
                Object userObject = userObjects[i];
                if (graphModel.isNode(userObject)) {
                    graphModel.removeNode(this, userObject);
                }
            }
        } finally {
            graphModel.setDispatchEnabled(true);
            graphModel.dispatchGraphEvent(new GraphEvent(
                    this,
                    GraphEvent.STRUCTURE_CHANGED,
                    graphModel.getRoot()));
        }
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Action to redo the last undone MoML change. */
    private Action _redoAction = new RedoAction();

    /** Flag indicating "save as" action rather than "save". */
    private boolean _saveAsFlag = false;

    /** Action to undo the last MoML change. */
    private Action _undoAction = new UndoAction();

    /** Action for zooming in. */
    private Action _zoomInAction = new ZoomInAction("Zoom In");

    /** Action for zoom reset. */
    private Action _zoomResetAction = new ZoomResetAction("Zoom Reset");

    /** Action for zoom fitting. */
    private Action _zoomFitAction = new ZoomFitAction("Zoom Fit");

    /** Action for zooming out. */
    private Action _zoomOutAction = new ZoomOutAction("Zoom Out");

    ///////////////////////////////////////////////////////////////////
    ////                     private inner classes                 ////

    ///////////////////////////////////////////////////////////////////
    //// CopyAction

    /** Action to copy the current selection. */
    private class CopyAction extends AbstractAction {

        /** Create a new action to copy the current selection. */
        public CopyAction() {
            super("Copy");
            putValue("tooltip",
                    "Copy the current selection onto the clipboard.");
            putValue(GUIUtilities.ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_C,
                            Event.CTRL_MASK));
            putValue(GUIUtilities.MNEMONIC_KEY,
                    new Integer(KeyEvent.VK_C));
        }

        /** Copy the current selection. */
        public void actionPerformed(ActionEvent e) {
            copy();
        }
    }

    /////////////////////////////////////////////////////////////////////
    //// CreateHierarchy

    /** Action to create a typed composite actor that contains the
     *  the selected actors.
     */
    private class CreateHierarchyAction extends AbstractAction {

        /**  Create a new action to introduce a level of hierarchy.
         */
        public CreateHierarchyAction() {
            super("CreateHierarchy");
            putValue("tooltip",
                    "Create a TypedCompositeActor that contains the"
                    + " selected actors.");
            //putValue(diva.gui.GUIUtilities.ACCELERATOR_KEY,
            //        KeyStroke.getKeyStroke(KeyEvent.VK_H,
            //                java.awt.Event.CTRL_MASK));
            //putValue(diva.gui.GUIUtilities.MNEMONIC_KEY,
            //        new Integer(KeyEvent.VK_H));
        }

        public void actionPerformed(ActionEvent e) {
            createHierarchy();
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// CutAction

    /** Action to copy and delete the current selection. */
    private class CutAction extends AbstractAction {

        /** Create a new action to copy and delete the current selection. */
        public CutAction() {
            super("Cut");
            putValue("tooltip",
                    "Cut the current selection onto the clipboard.");
            putValue(GUIUtilities.ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_X,
                            Event.CTRL_MASK));
            putValue(GUIUtilities.MNEMONIC_KEY,
                    new Integer(KeyEvent.VK_T));
        }

        /** Copy and delete the current selection. */
        public void actionPerformed(ActionEvent e) {
            cut();
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// EditIconAction

    // 'Edit Icon' pop up menu not shipped with PtII1.0.
    // See also ptolemy.vergil.basic/kernel/ActorGraphFrame.java
    //     public class EditIconAction extends FigureAction {
    //         public EditIconAction() {
    //             super("Edit Icon");
    //         }

    //         public void actionPerformed(ActionEvent e) {
    //             // Figure out what entity.
    //             super.actionPerformed(e);
    //             NamedObj object = getTarget();
    //             if (!(object instanceof Entity)) return;
    //             Entity entity = (Entity) object;
    //             XMLIcon icon = null;
    //             List iconList = entity.attributeList(XMLIcon.class);
    //             if (iconList.size() == 0) {
    //                 try {
    //                     icon = new XMLIcon(entity, entity.uniqueName("icon"));
    //                 } catch (Exception ex) {
    //                     throw new InternalErrorException(
    //                             "duplicated name, but there were no other icons.");
    //                 }
    //             } else if (iconList.size() == 1) {
    //                 icon = (XMLIcon)iconList.get(0);
    //             } else {
    //                 throw new InternalErrorException("entity " + entity +
    //                         " contains more than one icon");
    //             }
    //             // FIXME make a tableau.
    //             ApplicationContext appContext = new ApplicationContext();
    //             appContext.setTitle("Icon editor");
    //             new IconEditor(appContext, icon);
    //         }
    //     }

    ///////////////////////////////////////////////////////////////////
    //// ExecuteSystemAction

    /** An action to open a run control window. */
    private class ExecuteSystemAction extends AbstractAction {

        /** Construct an action to execute the model. */
        public ExecuteSystemAction() {
            super("Go");
            putValue("tooltip", "Execute The Model");
            putValue(GUIUtilities.ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_G,
                            Event.CTRL_MASK));
            putValue(GUIUtilities.MNEMONIC_KEY,
                    new Integer(KeyEvent.VK_G));
        }

        /** Open a run control window. */
        public void actionPerformed(ActionEvent e) {
            try {
                PtolemyEffigy effigy =
                    (PtolemyEffigy)getTableau().getContainer();
                new RunTableau(effigy, effigy.uniqueName("tableau"));
            } catch (Exception ex) {
                MessageHandler.error("Execution Failed", ex);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// ImportLibraryAction

    /** An action to import a library of components. */
    private class ImportLibraryAction extends AbstractAction {

        /** Create a new action to import a library of components. */
        public ImportLibraryAction() {
            super("Import Library");
            putValue("tooltip", "Import a library into the Palette");
            putValue(GUIUtilities.MNEMONIC_KEY,
                    new Integer(KeyEvent.VK_M));
        }

        /** Import a library by first opening a file chooser dialog and
         *  then importing the specified library.
         */
        public void actionPerformed(ActionEvent e) {
            // NOTE: this code is mostly copied from Top.
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Select a library");

            if (_getDirectory() != null) {
                chooser.setCurrentDirectory(_getDirectory());
            } else {
                // The default on Windows is to open at user.home, which is
                // typically an absurd directory inside the O/S installation.
                // So we use the current directory instead.
                // FIXME: This will throw a security exception in an applet?
                String cwd = StringUtilities.getProperty("user.dir");
                if (cwd != null) {
                    chooser.setCurrentDirectory(new File(cwd));
                }
            }
            int result = chooser.showOpenDialog(BasicGraphFrame.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    File file = chooser.getSelectedFile();
                    // FIXME it would be nice if MoMLChangeRequest had the
                    // ability to read from a URL
                    StringBuffer buffer = new StringBuffer();
                    FileReader reader = new FileReader(file);
                    char[] chars = new char[50];
                    while (reader.ready()) {
                        int count = reader.read(chars, 0, 50);
                        buffer.append(chars, 0, count);
                    }
                    PtolemyEffigy effigy =
                        (PtolemyEffigy)getTableau().getContainer();
                    Configuration configuration =
                        (Configuration)effigy.toplevel();
                    NamedObj library =
                        configuration.getEntity("actor library");
                    if (library == null) return;
                    ChangeRequest request =
                        new MoMLChangeRequest(this, library,
                                buffer.toString(),
                                file.toURL());
                    library.requestChange(request);
                    _setDirectory(chooser.getCurrentDirectory());
                } catch (Exception ex) {
                    MessageHandler.error("Library import failed.", ex);
                }
            }
        }
    };

    ///////////////////////////////////////////////////////////////////
    //// LayoutAction

    /** Action to automatically lay out the graph. */
    private class LayoutAction extends AbstractAction {

        /** Create a new action to automatically lay out the graph. */
        public LayoutAction() {
            super("Automatic Layout");
            putValue("tooltip", "Layout the Graph (Ctrl+T)");
            putValue(GUIUtilities.ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_T, Event.CTRL_MASK));
            putValue(GUIUtilities.MNEMONIC_KEY,
                    new Integer(KeyEvent.VK_L));
        }

        /** Lay out the graph. */
        public void actionPerformed(ActionEvent e) {
            try {
                layoutGraph();
            } catch (Exception ex) {
                MessageHandler.error("Layout failed", ex);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// PasteAction

    /** Paste the current contents of the clipboard into the current model. */
    private class PasteAction extends AbstractAction {

        /** Create a new action to paste the current contents of the
         *  clipboard into the current model.
         */
        public PasteAction() {
            super("Paste");
            putValue("tooltip",
                    "Paste the contents of the clipboard.");
            putValue(GUIUtilities.ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_V,
                            Event.CTRL_MASK));
            putValue(GUIUtilities.MNEMONIC_KEY,
                    new Integer(KeyEvent.VK_P));
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
    private class PtolemyLayout extends LevelLayout {

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
            HashMap map = new HashMap();

            // Copy all the nodes for the graph.
            for (Iterator i = model.nodes(origComposite); i.hasNext(); ) {
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
            Iterator i =
                GraphUtilities.partiallyContainedEdges(origComposite, model);
            while (i.hasNext()) {
                Object origEdge = i.next();
                Object origTail = model.getTail(origEdge);
                Object origHead = model.getHead(origEdge);
                if (origHead != null && origTail != null) {
                    Figure tailFigure =
                        (Figure)target.getVisualObject(origTail);
                    Figure headFigure =
                        (Figure)target.getVisualObject(origHead);
                    // Swap the head and the tail if it will improve the
                    // layout, since LevelLayout only uses directed edges.
                    if (tailFigure instanceof Terminal) {
                        Terminal terminal = (Terminal)tailFigure;
                        Site site = terminal.getConnectSite();
                        if (site instanceof FixedNormalSite) {
                            double normal = site.getNormal();
                            int direction =
                                CanvasUtilities.getDirection(normal);
                            if (direction == SwingUtilities.WEST) {
                                Object temp = origTail;
                                origTail = origHead;
                                origHead = temp;
                            }
                        }
                    } else if (headFigure instanceof Terminal) {
                        Terminal terminal = (Terminal)headFigure;
                        Site site = terminal.getConnectSite();
                        if (site instanceof FixedNormalSite) {
                            double normal = site.getNormal();
                            int direction =
                                CanvasUtilities.getDirection(normal);
                            if (direction == SwingUtilities.EAST) {
                                Object temp = origTail;
                                origTail = origHead;
                                origHead = temp;
                            }
                        }
                    }

                    origTail =
                        _getParentInGraph(model, origComposite, origTail);
                    origHead =
                        _getParentInGraph(model, origComposite, origHead);
                    Object copyTail = map.get(origTail);
                    Object copyHead = map.get(origHead);

                    if (copyHead != null && copyTail != null) {
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
        private Object _getParentInGraph(GraphModel model,
                Object graph, Object node) {
            while (node != null && !model.containsNode(graph, node)) {
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
    private class PtolemyLayoutTarget extends BasicLayoutTarget {

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
            GraphModel model = getController().getGraphModel();
            if (composite == getRootGraph()) {
                // Take into account the current zoom and pan.
                Rectangle2D bounds = getVisibleCanvasRectangle();

                double width = bounds.getWidth();
                double height = bounds.getHeight();

                double borderPercentage = (1-getLayoutPercentage())/2;
                double x = borderPercentage*width + bounds.getX();
                double y = borderPercentage*height + bounds.getY();
                double w = getLayoutPercentage()*width;
                double h = getLayoutPercentage()*height;
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
                double location[] = ((Locatable)node).getLocation();
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
                    ((Locatable)node).setLocation(location);
                } catch (IllegalActionException ex) {
                    throw new InternalErrorException(ex.getMessage());
                }
            }
        }
    }

    /////////////////////////////////////////////////////////////////////
    //// RedoAction

    /**
     *  Undo the last undoable MoML change on the current current model.
     */
    private class RedoAction extends AbstractAction {

        /**
         *  Create a new action to paste the current contents of the clipboard
         *  into the current model.
         */
        public RedoAction() {
            super("Redo");
            putValue("tooltip",
                    "Redo the last change undone.");
            putValue(diva.gui.GUIUtilities.ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_Y,
                            (java.awt.Event.CTRL_MASK)));
            // FIXME: Why is this R?
            putValue(diva.gui.GUIUtilities.MNEMONIC_KEY,
                    new Integer(KeyEvent.VK_R));
        }

        /**
         *  Paste the current contents of the clipboard into the current model.
         *
         * @param  e  Description of Parameter
         */
        public void actionPerformed(ActionEvent e) {
            redo();
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// SaveInLibraryAction

    // FIXME: The following needs quite a bit of work.
    // Changes to the library are not persistent.
    /** An action to save the current model in a library. */
    private class SaveInLibraryAction extends AbstractAction {

        /** Create a new action to save a model in a library. */
        public SaveInLibraryAction() {
            super("Save In Library");
            putValue("tooltip", "Save as a Component in Library");
            putValue(GUIUtilities.MNEMONIC_KEY,
                    new Integer(KeyEvent.VK_S));
        }

        /** Create a new instance of the current model in the
         *  actor library of the configuration.
         */
        public void actionPerformed(ActionEvent e) {
            PtolemyEffigy effigy =
                (PtolemyEffigy)getTableau().getContainer();
            NamedObj object = effigy.getModel();
            if (object == null) {
                return;
            }
            if (!(object instanceof Entity)) {
                throw new KernelRuntimeException("Could not save in "
                        + "library, '" + object + "' is not an Entity");
            }

            Entity entity = (Entity) object;
            Configuration configuration = (Configuration)effigy.toplevel();
            saveComponentInLibrary(configuration, entity);
        }
    }

    /////////////////////////////////////////////////////////////////////
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
            putValue("tooltip",
                    "Undo the last change.");
            putValue(diva.gui.GUIUtilities.ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_Z,
                            java.awt.Event.CTRL_MASK));
            // FIXME: Why is this U?
            putValue(diva.gui.GUIUtilities.MNEMONIC_KEY,
                    new Integer(KeyEvent.VK_U));
        }

        /**
         *  Paste the current contents of the clipboard into the current model.
         *
         * @param  e  Description of Parameter
         */
        public void actionPerformed(ActionEvent e) {
            undo();
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// ZoomInAction

    // An action to zoom in.
    public class ZoomInAction extends AbstractAction {
        public ZoomInAction(String description) {
            super(description);
            // Load the image by using the absolute path to the gif.
            // Using a relative location should work, but it does not.
            // Use the resource locator of the class.
            // For more information, see
            // jdk1.3/docs/guide/resources/resources.html
            URL img = getClass().getResource(
                    "/ptolemy/vergil/basic/img/zoomin.gif");
            if (img != null) {
                ImageIcon icon = new ImageIcon(img);
                putValue(GUIUtilities.LARGE_ICON, icon);
            }
            putValue("tooltip", description + " (Ctrl+Shift+=)");
            // NOTE: The following assumes that the + key is the same
            // as the = key.  Unfortunately, the VK_PLUS key event doesn't
            // work, so we have to do it this way.
            putValue(GUIUtilities.ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS,
                            Event.CTRL_MASK
                            | Event.SHIFT_MASK));
            putValue(GUIUtilities.MNEMONIC_KEY,
                    new Integer(KeyEvent.VK_Z));
        }

        public void actionPerformed(ActionEvent e) {
            zoom(1.25);
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// ZoomResetAction

    // An action to reset zoom.
    public class ZoomResetAction extends AbstractAction {
        public ZoomResetAction(String description) {
            super(description);
            // Load the image by using the absolute path to the gif.
            // Using a relative location should work, but it does not.
            // Use the resource locator of the class.
            // For more information, see
            // jdk1.3/docs/guide/resources/resources.html
            URL img = getClass().getResource(
                    "/ptolemy/vergil/basic/img/zoomreset.gif");
            if (img != null) {
                ImageIcon icon = new ImageIcon(img);
                putValue(GUIUtilities.LARGE_ICON, icon);
            }
            putValue("tooltip", description + " (Ctrl+=)");
            putValue(GUIUtilities.ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS,
                            Event.CTRL_MASK));
            putValue(GUIUtilities.MNEMONIC_KEY,
                    new Integer(KeyEvent.VK_M));
        }

        public void actionPerformed(ActionEvent e) {
            zoomReset();
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// ZoomFitAction

    // An action to zoom fit.
    public class ZoomFitAction extends AbstractAction {
        public ZoomFitAction(String description) {
            super(description);
            // Load the image by using the absolute path to the gif.
            // Using a relative location should work, but it does not.
            // Use the resource locator of the class.
            // For more information, see
            // jdk1.3/docs/guide/resources/resources.html
            URL img = getClass().getResource(
                    "/ptolemy/vergil/basic/img/zoomfit.gif");
            if (img != null) {
                ImageIcon icon = new ImageIcon(img);
                putValue(GUIUtilities.LARGE_ICON, icon);
            }
            putValue("tooltip", description + " (Ctrl+Shift+-)");
            putValue(GUIUtilities.ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_MINUS,
                            Event.CTRL_MASK
                            | Event.SHIFT_MASK));
            putValue(GUIUtilities.MNEMONIC_KEY,
                    new Integer(KeyEvent.VK_F));
        }

        public void actionPerformed(ActionEvent e) {
            zoomFit();
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// ZoomOutAction

    // An action to zoom out.
    public class ZoomOutAction extends AbstractAction {
        public ZoomOutAction(String description) {
            super(description);
            // Load the image by using the absolute path to the gif.
            // Using a relative location should work, but it does not.
            // Use the resource locator of the class.
            // For more information, see
            // jdk1.3/docs/guide/resources/resources.html
            URL img = getClass().getResource(
                    "/ptolemy/vergil/basic/img/zoomout.gif");
            if (img != null) {
                ImageIcon icon = new ImageIcon(img);
                putValue(GUIUtilities.LARGE_ICON, icon);
            }
            putValue("tooltip", description + " (Ctrl+-)");
            putValue(GUIUtilities.ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_MINUS,
                            Event.CTRL_MASK));
            putValue(GUIUtilities.MNEMONIC_KEY,
                    new Integer(KeyEvent.VK_U));
        }

        public void actionPerformed(ActionEvent e) {
            zoom(1.0/1.25);
        }
    }
}
