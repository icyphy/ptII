/* A simple graph view for Ptolemy models

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

@ProposedRating Red (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.ptolemy;

import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.gui.MessageHandler;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.gui.DocumentationViewerTableau;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.MoMLApplication;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.PtolemyTop;
import ptolemy.actor.gui.RunTableau;
import ptolemy.actor.gui.style.EditableChoiceStyle;
import ptolemy.moml.Locatable;
import ptolemy.moml.Location;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.Vertex;
import ptolemy.vergil.VisualNotation;
import ptolemy.vergil.graph.EditorDropTarget;
import ptolemy.vergil.graph.EditorGraphController;
import ptolemy.vergil.graph.PtolemyGraphModel;
import ptolemy.vergil.icon.IconEditor;
import ptolemy.vergil.toolbox.EditParametersFactory;
import ptolemy.vergil.toolbox.EditParameterStylesFactory;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.toolbox.MenuActionFactory;
import ptolemy.vergil.toolbox.MenuItemFactory;
import ptolemy.vergil.toolbox.PtolemyListCellRenderer;
import ptolemy.vergil.toolbox.PtolemyMenuFactory;
import ptolemy.vergil.toolbox.XMLIcon;
import ptolemy.vergil.tree.LibraryTreeModel;

import diva.canvas.CanvasUtilities;
import diva.canvas.Site;
import diva.canvas.Figure;
import diva.canvas.connector.FixedNormalSite;
import diva.canvas.connector.Terminal;
import diva.canvas.interactor.SelectionModel;

import diva.gui.ApplicationContext;
import diva.gui.Document;
import diva.gui.toolbox.FocusMouseListener;
import diva.gui.toolbox.JContextMenu;
import diva.gui.toolbox.JPanner;

import diva.graph.JGraph;

import diva.graph.GraphController;
import diva.graph.GraphModel;
import diva.graph.GraphPane;
import diva.graph.GraphUtilities;
import diva.graph.MutableGraphModel;
import diva.graph.basic.BasicLayoutTarget;
import diva.graph.layout.LevelLayout;
import diva.graph.layout.LayoutTarget;
import diva.graph.toolbox.DeletionListener;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.BorderLayout;
import java.awt.geom.Point2D;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Rectangle2D;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.awt.print.PrinterException;
import java.awt.print.PageFormat;

import java.io.IOException;
import java.io.StringWriter;
import java.io.File;

import java.net.URL;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.KeyStroke;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingUtilities;

//////////////////////////////////////////////////////////////////////////
//// GraphFrame
/**
A simple graph view for ptolemy models.  This represents a level of the
hierarchy of a ptolemy model as a diva graph.  Cut, copy and paste operations
are supported using MoML and the graph itself is created using a visual
notation as a a factory

@author  Steve Neuendorffer
@version $Id$
*/
public class GraphFrame extends PtolemyTop
    implements Printable, ClipboardOwner {
   
    public GraphFrame(CompositeEntity entity) {
	_model = entity;

	getContentPane().setLayout(new BorderLayout());
	
	// create the graph editor, using the visual notation.
	// These two things control the view of a ptolemy model.
	EditorGraphController controller = new EditorGraphController();
	PtolemyGraphModel model = new PtolemyGraphModel(entity);
	
	GraphPane pane = new GraphPane(controller, model);
	// FIXME	VisualNotation notation = _getVisualNotation(entity);
	// FIXME        GraphPane pane = notation.createView();
	_jgraph = new JGraph(pane);
	//	GraphController _controller =
	//    _jgraph.getGraphPane().getGraphController();
	
	new EditorDropTarget(_jgraph);
	
	ActionListener deletionListener = new DeletionListener();
	_jgraph.registerKeyboardAction(deletionListener, "Delete",
				       KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
				       JComponent.WHEN_IN_FOCUSED_WINDOW);
	_jgraph.setRequestFocusEnabled(true);
	_jgraph.addMouseListener(new FocusMouseListener());
	_jgraph.setAlignmentX(1);
	_jgraph.setAlignmentY(1);
	_jgraph.setBackground(BACKGROUND_COLOR);
	
	// Ugh..  I hate setting the size like this.
	_jgraph.setPreferredSize(new Dimension(600, 450));
	_jgraph.setSize(600, 450);
	
	// wrap the graph editor in a scroll pane.
	_graphScrollPane = new JScrollPane(_jgraph);
	_graphScrollPane.setVerticalScrollBarPolicy(_graphScrollPane.VERTICAL_SCROLLBAR_NEVER);
	_graphScrollPane.setHorizontalScrollBarPolicy(_graphScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

	// create the panner.
	_graphPanner = new JPanner();
	_graphPanner.setPreferredSize(new Dimension(200, 150));
	_graphPanner.setMaximumSize(new Dimension(200, 150));
	_graphPanner.setSize(200, 150);
	_graphPanner.setBorder(BorderFactory.createEtchedBorder());
        _graphPanner.setViewport(_graphScrollPane.getViewport());

	// create the palette.
	// FIXME get the library from someplace reasonable.
	//	try {
	    // Get the url for the entity library.
	//    URL entityLibURL = 
	//	MoMLApplication.specToURL(((Settable)getTableau().getAttribute("library")).getExpression());
	    // Create the library browser.
	//    _library = LibraryTreeModel.createTree(entityLibURL);
	//} catch (IOException ex) {
	    _library = LibraryTreeModel.createTree(null);
	    //}
        _library.setBackground(BACKGROUND_COLOR);
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
	_splitPane.setRightComponent(_graphScrollPane);
	getContentPane().add(_splitPane, BorderLayout.CENTER);

	// Now add the actions.
	// FIXME: hotkeys, shortcuts and move to a base class.
	_toolbar = new JToolBar();
	getContentPane().add(_toolbar, BorderLayout.NORTH);

	_cutAction = new CutAction();
	diva.gui.GUIUtilities.addHotKey(_jgraph, _cutAction);
	diva.gui.GUIUtilities.addMenuItem(_editMenu, _cutAction);

	_copyAction = new CopyAction();
	diva.gui.GUIUtilities.addHotKey(_jgraph, _copyAction);
	diva.gui.GUIUtilities.addMenuItem(_editMenu, _copyAction);
	
	_pasteAction = new PasteAction();
	diva.gui.GUIUtilities.addHotKey(_jgraph, _pasteAction);
	diva.gui.GUIUtilities.addMenuItem(_editMenu, _pasteAction);
	
	_layoutAction = new LayoutAction();
	diva.gui.GUIUtilities.addHotKey(_jgraph, _layoutAction);
	diva.gui.GUIUtilities.addMenuItem(_editMenu, _layoutAction);
	
	// FIXME what about other notations?  publish a list of actions
	// like EditorKit?
	_newPortAction = controller.getNewPortAction();
	_editMenu.add(_newPortAction);
	diva.gui.GUIUtilities.addToolBarButton(_toolbar, _newPortAction);

	_newRelationAction = controller.getNewRelationAction();
	_editMenu.add(_newRelationAction);
	diva.gui.GUIUtilities.addToolBarButton(_toolbar, _newRelationAction);

	// FIXME make a service.
	_directorModel = new DefaultComboBoxModel();
	try {
	    // FIXME MoMLize
	    Director dir;
	    dir = new ptolemy.domains.sdf.kernel.SDFDirector();
	    dir.setName("SDF");
	    _directorModel.addElement(dir);
	    dir = new ptolemy.domains.dt.kernel.DTDirector();
	    dir.setName("DT");
	    _directorModel.addElement(dir);
	    dir = new ptolemy.domains.pn.kernel.PNDirector();
	    dir.setName("PN");
	    _directorModel.addElement(dir);
	    dir = new ptolemy.domains.de.kernel.DEDirector();
	    dir.setName("DE");
	    _directorModel.addElement(dir);
	    dir = new ptolemy.domains.csp.kernel.CSPDirector();
	    dir.setName("CSP");
	    _directorModel.addElement(dir);
	    dir = new ptolemy.domains.dde.kernel.DDEDirector();
	    dir.setName("DDE");
	    _directorModel.addElement(dir);
	    dir = new ptolemy.domains.fsm.kernel.FSMDirector();
	    dir.setName("FSM");
	    _directorModel.addElement(dir);

	    dir = new ptolemy.domains.ct.kernel.CTMixedSignalDirector();
	    dir.setName("CT");
	    Parameter solver;
	    solver = (Parameter)dir.getAttribute("ODESolver");
	    EditableChoiceStyle style;
	    style = new EditableChoiceStyle(solver, "style");
	    new Parameter(style, "choice0", new StringToken(
		"ptolemy.domains.ct.kernel.solver.ExplicitRK23Solver"));
	    new Parameter(style, "choice1", new StringToken(
                "ptolemy.domains.ct.kernel.solver.BackwardEulerSolver"));
	    new Parameter(style, "choice2", new StringToken(
	        "ptolemy.domains.ct.kernel.solver.ForwardEulerSolver"));

	    solver = (Parameter)dir.getAttribute("breakpointODESolver");
	    style = new EditableChoiceStyle(solver, "style");
	    new Parameter(style, "choice0", new StringToken(
                "ptolemy.domains.ct.kernel.solver.DerivativeResolver"));
	    new Parameter(style, "choice1", new StringToken(
		"ptolemy.domains.ct.kernel.solver.BackwardEulerSolver"));
	    new Parameter(style, "choice2", new StringToken(
		"ptolemy.domains.ct.kernel.solver.ImpulseBESolver"));
            _directorModel.addElement(dir);

            dir = new ptolemy.domains.ct.kernel.CTEmbeddedDirector();	    
	    dir.setName("CTEmbedded");
	    //Parameter solver;
	    solver = (Parameter)dir.getAttribute("ODESolver");
	    //EditableChoiceStyle style;
	    style = new EditableChoiceStyle(solver, "style");
	    new Parameter(style, "choice0", new StringToken(
		"ptolemy.domains.ct.kernel.solver.ExplicitRK23Solver"));
	    new Parameter(style, "choice1", new StringToken(
                "ptolemy.domains.ct.kernel.solver.BackwardEulerSolver"));
	    new Parameter(style, "choice2", new StringToken(
	        "ptolemy.domains.ct.kernel.solver.ForwardEulerSolver"));

	    solver = (Parameter)dir.getAttribute("breakpointODESolver");
	    style = new EditableChoiceStyle(solver, "style");
	    new Parameter(style, "choice0", new StringToken(
                "ptolemy.domains.ct.kernel.solver.DerivativeResolver"));
	    new Parameter(style, "choice1", new StringToken(
		"ptolemy.domains.ct.kernel.solver.BackwardEulerSolver"));
	    new Parameter(style, "choice2", new StringToken(
		"ptolemy.domains.ct.kernel.solver.ImpulseBESolver"));
	    _directorModel.addElement(dir);

	    dir = new ptolemy.domains.giotto.kernel.GiottoDirector();
	    dir.setName("Giotto");
	    _directorModel.addElement(dir);
            dir = new ptolemy.domains.rtp.kernel.RTPDirector();
	    dir.setName("RTP");
	    _directorModel.addElement(dir);
	}
	catch (Exception ex) {
	    MessageHandler.error("Director combobox creation failed", ex);
	}
	//FIXME find these names somehow.
	_directorComboBox = new JComboBox(_directorModel);
	_directorComboBox.setRenderer(new PtolemyListCellRenderer());
	_directorComboBox.setMaximumSize(_directorComboBox.getMinimumSize());
        _directorComboBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
		    // When a director is selected, update the 
		    // director of the model in the current document.
		    final Director director = (Director) e.getItem();
		    PtolemyEffigy effigy = 
			(PtolemyEffigy)getTableau().getContainer();
		    if(effigy == null) return;
		    CompositeEntity entity = 
			(CompositeEntity)effigy.getModel();
		    if(entity instanceof CompositeActor) {
			final CompositeActor actor = (CompositeActor) entity;
			final Director oldDirector = actor.getDirector();
                        if((oldDirector == null) || (director.getClass()
                                != oldDirector.getClass())) {
                            actor.requestChange(new ChangeRequest(
                                   this, "Set Director") {
                                protected void _execute() throws Exception {
                                    Director clone = (Director)
                                            director.clone(actor.workspace());
                                    actor.setDirector(clone);
                                }
                            });
                        }					      
		    }
                }
            }
        });
        _toolbar.add(_directorComboBox);

	_executeSystemAction = new ExecuteSystemAction();
	diva.gui.GUIUtilities.addHotKey(_jgraph, _executeSystemAction);
	diva.gui.GUIUtilities.addMenuItem(_executeMenu, _executeSystemAction);
	diva.gui.GUIUtilities.addToolBarButton(_toolbar, 
					       _executeSystemAction);
	
	_editIconAction = new EditIconAction();
	_lookInsideAction = new LookInsideAction();
	_getDocumentationAction = new GetDocumentationAction();
	controller.getEntityController().setMenuFactory(new EntityContextMenuFactory(controller));
 	controller.getEntityPortController().setMenuFactory(new PortContextMenuFactory(controller));
  	controller.getPortController().setMenuFactory(new PortContextMenuFactory(controller));
  	controller.getRelationController().setMenuFactory(new RelationContextMenuFactory(controller));
  	controller.getLinkController().setMenuFactory(new RelationContextMenuFactory(controller));
    }

    /** Remove the currently selected objects from this document, if any,
     * and place them on the given clipboard.  If the document does not
     * support such an operation, then do nothing.
     */
    public void cut () {
	// FIXME
	copy();
    }

    /** Return the jgraph instance that this view uses to represent the 
     *  ptolemy model.
     */
    public JGraph getJGraph() {
	return _jgraph;
    }

    /** Layout the graph view.
     */
    public void layoutGraph() {
	GraphController controller =
	    _jgraph.getGraphPane().getGraphController();
        LayoutTarget target = new PtolemyLayoutTarget(controller);
        GraphModel model = controller.getGraphModel();
        PtolemyLayout layout = new PtolemyLayout(target);
	layout.setOrientation(LevelLayout.HORIZONTAL);
	layout.setRandomizedPlacement(false);
        // Perform the layout and repaint
	layout.layout(model.getRoot());
        _jgraph.repaint();
    }

    /** Do nothing.
     */
    public void lostOwnership(Clipboard clipboard, 
			      Transferable transferable) {
    }

    /** Clone the objects currently on the clipboard, if any,
     * and place them in the given document.  If the document does not
     * support such an operation, then do nothing.  This method is responsible
     * for copying the data.
     */
    public void paste () {
	Clipboard clipboard = 
	    java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
	Transferable transferable = clipboard.getContents(this);
	GraphPane graphPane = _jgraph.getGraphPane();
	GraphController controller =
	    (GraphController)graphPane.getGraphController();
	MutableGraphModel model = controller.getGraphModel();
	Workspace workspace = ((NamedObj) model.getRoot()).workspace();
	if(transferable == null) 
	    return;
	try {
	    String string = (String)
		transferable.getTransferData(DataFlavor.stringFlavor);
	    CompositeEntity toplevel = (CompositeEntity)model.getRoot();
	    MoMLParser parser = new MoMLParser(workspace);
	    parser.setContext(toplevel);
	    toplevel.requestChange(
                new MoMLChangeRequest(this, parser, string));
	} catch (UnsupportedFlavorException ex) {
	    System.out.println("Transferable object didn't " + 
			       "support stringFlavor: " +
			       ex.getMessage());
	} catch (IOException ex) {
	    System.out.println("IOException when pasting: " + 
			       ex.getMessage());
	} catch (Exception ex) {
	    ex.printStackTrace();
	    throw new RuntimeException(ex.getMessage());
	} 
    }

    /** Print the document to a printer, represented by the specified graphics
     *  object.  This method assumes that a view exists of the this document
     *  in the application.
     *  @param graphics The context into which the page is drawn.
     *  @param format The size and orientation of the page being drawn.
     *  @param index The zero based index of the page to be drawn.
     *  @returns PAGE_EXISTS if the page is rendered successfully, or
     *   NO_SUCH_PAGE if pageIndex specifies a non-existent page.
     *  @exception PrinterException If the print job is terminated.
     */
    public int print(Graphics graphics, PageFormat format,
            int index) throws PrinterException {
	if(_jgraph != null) {
            return _jgraph.print(graphics, format, index);
        } else return NO_SUCH_PAGE;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the menus that are used by this frame.
     */
    protected void _addMenus() {
       	_editMenu = new JMenu("Edit");
        _editMenu.setMnemonic(KeyEvent.VK_E);
	_menubar.add(_editMenu);
	_executeMenu = new JMenu("Execute");
        _executeMenu.setMnemonic(KeyEvent.VK_X);
	_menubar.add(_executeMenu);
    }

    /** Get the currently selected objects from this document, if any,
     * and place them on the given clipboard. 
     */
    public void copy () {
	Clipboard clipboard = 
	    java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
	GraphPane graphPane = _jgraph.getGraphPane();
	GraphController controller =
	    (GraphController)graphPane.getGraphController();
	SelectionModel model = controller.getSelectionModel();
	GraphModel graphModel = controller.getGraphModel();
	Object selection[] = model.getSelectionAsArray();
	HashSet objectSet = new HashSet();
	for(int i = 0; i < selection.length; i++) {
	    if(selection[i] instanceof Figure) {
		Object userObject = ((Figure)selection[i]).getUserObject();
		NamedObj object = (NamedObj)userObject;
		NamedObj actual = 
		    (NamedObj)graphModel.getSemanticObject(object);
		if(objectSet.contains(actual)) continue;
		objectSet.add(actual);
	    }
	}
	
	StringWriter buffer = new StringWriter();	   
	try {
	    buffer.write("<group>\n");
	    Iterator elements = objectSet.iterator();
	    while(elements.hasNext()) {
		NamedObj element = (NamedObj) elements.next();
		// first level to avoid obnoxiousness with 
		// toplevel translations.
		element.exportMoML(buffer, 1);
	    }
	    CompositeEntity container = (CompositeEntity)graphModel.getRoot();
	    buffer.write(container.exportLinks(1, objectSet));
	    buffer.write("</group>\n");
	 
	    // The code below does not use a PtolemyTransferable, 
	    // to work around
	    // a bug in the JDK that should be fixed as of jdk1.3.1.  The bug
	    // is that cut and paste through the system clipboard to native
	    // applications doesn't work unless you use string selection. 
	    clipboard.setContents(new StringSelection(buffer.toString()), 
				  this);
	}
	catch (Exception ex) {
	    ex.printStackTrace();
	}

    }
 
    /** Write the model to the specified file.
     *  @param file The file to write to.
     *  @exception IOException If the write fails.
     */
    protected void _writeFile(File file) throws IOException {
        java.io.FileWriter fout = new java.io.FileWriter(file);
        _model.exportMoML(fout);
        fout.close();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     public inner classes                  ////
    
    ///////////////////////////////////////////////////////////////////
    ////                     private inner classes                 ////

    private class CopyAction extends AbstractAction {
	public CopyAction() {
	    super("Copy");
	    putValue("tooltip", 
		     "Copy the current selection onto the clipboard.");
	    putValue(diva.gui.GUIUtilities.ACCELERATOR_KEY, 
		     KeyStroke.getKeyStroke(KeyEvent.VK_C, 
					    java.awt.Event.CTRL_MASK));
	    putValue(diva.gui.GUIUtilities.MNEMONIC_KEY,
		     new Integer(KeyEvent.VK_C));
	}

	public void actionPerformed(ActionEvent e) {
	    copy();
	}
    }

    private class CutAction extends AbstractAction {
	public CutAction() {
	    super("Cut");
	    putValue("tooltip", 
		     "Cut the current selection onto the clipboard.");
	    putValue(diva.gui.GUIUtilities.ACCELERATOR_KEY, 
		     KeyStroke.getKeyStroke(KeyEvent.VK_X, 
					    java.awt.Event.CTRL_MASK));
	    putValue(diva.gui.GUIUtilities.MNEMONIC_KEY,
		     new Integer(KeyEvent.VK_T));
	}

	public void actionPerformed(ActionEvent e) {
	    cut();
	}
    }

    private class PasteAction extends AbstractAction {
	public PasteAction() {
	    super("Paste");
	    putValue("tooltip", 
		     "Paste the contents of the clipboard.");
	    putValue(diva.gui.GUIUtilities.ACCELERATOR_KEY, 
		     KeyStroke.getKeyStroke(KeyEvent.VK_V, 
					    java.awt.Event.CTRL_MASK));
	    putValue(diva.gui.GUIUtilities.MNEMONIC_KEY,
		     new Integer(KeyEvent.VK_P));
	}

	public void actionPerformed(ActionEvent e) {
	    paste();
	}
    }

    private class LayoutAction extends AbstractAction {
	public LayoutAction() {
	    super("Automatic Layout");
	}
	public void actionPerformed(ActionEvent e) {
	    try {
		layoutGraph();
	    } catch (Exception ex) {
		MessageHandler.error("Layout failed", ex);
	    }
	}      
    }

    public class EditIconAction extends FigureAction {
	public EditIconAction() {
	    super("Edit Icon");
	}

	public void actionPerformed(ActionEvent e) {
	    // Figure out what entity.
	    super.actionPerformed(e);		
	    NamedObj object = getTarget();
	    if(!(object instanceof Entity)) return;
	    Entity entity = (Entity) object;
	    XMLIcon icon = null;
	    List iconList = entity.attributeList(XMLIcon.class);
	    if(iconList.size() == 0) {
		try {
		    icon = new XMLIcon(entity, entity.uniqueName("icon"));
		} catch (Exception ex) {
		    throw new InternalErrorException("duplicated name, but "
						     + "there were no " +
						     "other icons.");
		}
	    } else if(iconList.size() == 1) {
		icon = (XMLIcon)iconList.get(0);
	    } else {
		throw new InternalErrorException("entity " + entity + 
				 "contains more than one icon");
	    }
	    // FIXME make a tableau.
	    ApplicationContext appContext = new ApplicationContext();
	    appContext.setTitle("Icon editor");
	    new IconEditor(appContext, icon);
	}
    }

    /**
     * The factory for creating context menus on entities.
     */
    // FIXME this has to move into the visual notation.
    public class EntityContextMenuFactory extends PtolemyMenuFactory {
	public EntityContextMenuFactory(GraphController controller) {
	    super(controller);
	    addMenuItemFactory(new EditParametersFactory());
	    addMenuItemFactory(new EditParameterStylesFactory());
	    addMenuItemFactory(new MenuActionFactory(_getDocumentationAction));
	    addMenuItemFactory(new MenuActionFactory(_lookInsideAction));
	    addMenuItemFactory(new MenuActionFactory(_editIconAction));
	}
    }

    /**
     * The factory for creating context menus on ports.
     */
    // FIXME this has to move into the visual notation.
    public class PortContextMenuFactory extends PtolemyMenuFactory {
	public PortContextMenuFactory(GraphController controller) {
	    super(controller);
	    addMenuItemFactory(new PortDescriptionFactory());
	    addMenuItemFactory(new EditParametersFactory());
	    addMenuItemFactory(new EditParameterStylesFactory());
	    addMenuItemFactory(new MenuActionFactory(_getDocumentationAction));
	}

	public class PortDescriptionFactory extends MenuItemFactory {
	    /**
	     * Add an item to the given context menu that will configure the
	     * parameters on the given target.
	     */
	    public JMenuItem create(JContextMenu menu, NamedObj target) {
		target = _getItemTargetFromMenuTarget(target);
		if(target instanceof IOPort) {
		    IOPort port = (IOPort)target;
		    String string = "";
		    int count = 0;
		    if(port.isInput()) {
			string += "Input";
			count++;
		    }
		    if(port.isOutput()) {
			if(count > 0) {
			    string += ", ";
			}
			string += "Output";
			count++;
		    }
		    if(port.isMultiport()) {
			if(count > 0) {
			    string += ", ";
			}
			string += "Multiport";
			count++;
		    }
		    if(count > 0) {
			return menu.add(new JMenuItem("   " + string));
		    }
		}
		return null;
	    }
	    
	    /**
	     * Get the name of the items that will be created. 
	     * This is provided so
	     * that factory can be overriden slightly with the name changed.
	     */
	    protected String _getName() {
		return null;
	    }     
	}
    }
    
    private class ExecuteSystemAction extends AbstractAction {
	public ExecuteSystemAction() {
	    super("Go");
	    putValue("tooltip", "Execute The Model");
	    putValue(Action.ACCELERATOR_KEY, 
		     KeyStroke.getKeyStroke(KeyEvent.VK_G, 
					    java.awt.Event.CTRL_MASK));
	    putValue(diva.gui.GUIUtilities.MNEMONIC_KEY,
		     new Integer(KeyEvent.VK_G));
	}

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
    
    public class GetDocumentationAction extends FigureAction {
	public GetDocumentationAction() {
	    super("Get Documentation");
	}
	public void actionPerformed(ActionEvent e) {	    
	    // Create a dialog for configuring the object.
	    // FIXME this should probably be one frame for each class.
	    super.actionPerformed(e);		
	    NamedObj target = getTarget();
	    String className = target.getClass().getName();     
	    try {
		Effigy effigy = (Effigy)getTableau().getContainer();
		DocumentationViewerTableau viewer = 
		    new DocumentationViewerTableau(effigy, 
					  effigy.uniqueName("tableau"));
		viewer.dottedClass.setExpression(className);
	    } catch (Exception ex) {
		MessageHandler.error("Could not view Documentation for " + 
				     className, ex);
	    }
	}
    };
        
    // An action to look inside a composite.
    private class LookInsideAction extends FigureAction {
	public LookInsideAction() {
	    super("Look Inside");
	}
	public void actionPerformed(ActionEvent e) {
	    // Figure out what entity.
	    super.actionPerformed(e);		
	    NamedObj object = getTarget();
	    if(!(object instanceof CompositeEntity)) return;
	    CompositeEntity entity = (CompositeEntity)object;
	    NamedObj deferredTo = entity.getDeferMoMLDefinitionTo();
	    if(deferredTo != null) {
		entity = (CompositeEntity)deferredTo;
	    }

	    // FIXME create a new ptolemy effigy and
	    // a new graphTableau for it.
	}
    }
   
    // A layout algorithm for laying out ptolemy graphs.  Since our edges
    // are undirected, this layout algorithm turns them into directed edges
    // aimed consistently. i.e. An edge should always be "out" of an
    // internal output port and always be "in" of an internal input port.
    // Conversely, an edge is "out" of an external input port, and "in" of
    // an external output port.  The copying operation also flattens
    // the graph, because the level layout algorithm doesn't understand
    // how to layout hierarchical nodes.
    private class PtolemyLayout extends LevelLayout {
	
	/**
	 * Construct a new levelizing layout with a vertical orientation.
	 */
	public PtolemyLayout(LayoutTarget target) {
	    super(target);
	}

	/**
	 * Copy the given graph and make the nodes/edges in the copied
	 * graph point to the nodes/edges in the original.
	 */ 
	protected Object copyComposite(Object origComposite) {
	    LayoutTarget target = getLayoutTarget();
	    GraphModel model = target.getGraphModel();
	    diva.graph.basic.BasicGraphModel local = getLocalGraphModel();
	    Object copyComposite = local.createComposite(null);
	    HashMap map = new HashMap();
	    
	    // Copy all the nodes for the graph.
	    for(Iterator i = model.nodes(origComposite); i.hasNext(); ) {
		Object origNode = i.next();
		if(target.isNodeVisible(origNode)) {
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
	    Iterator i = GraphUtilities.localEdges(origComposite, model); 
	    while(i.hasNext()) {
		Object origEdge = i.next();
		Object origTail = model.getTail(origEdge);
		Object origHead = model.getHead(origEdge);
		if(origHead != null && origTail != null) {
		    Figure tailFigure = 
			(Figure)target.getVisualObject(origTail);
		    Figure headFigure = 
			(Figure)target.getVisualObject(origHead);
		    // Swap the head and the tail if it will improve the 
		    // layout, since LevelLayout only uses directed edges.
		    if(tailFigure instanceof Terminal) {
			Terminal terminal = (Terminal)tailFigure;
			Site site = terminal.getConnectSite();
			if(site instanceof FixedNormalSite) {
			    double normal = site.getNormal();
			    int direction = 
				CanvasUtilities.getDirection(normal);
			    if(direction == SwingUtilities.WEST) {
				Object temp = origTail;
				origTail = origHead;
				origHead = temp;
			    }
			}
		    } else if(headFigure instanceof Terminal) {
			Terminal terminal = (Terminal)headFigure;
			Site site = terminal.getConnectSite();
			if(site instanceof FixedNormalSite) {
			    double normal = site.getNormal();
			    int direction = 
				CanvasUtilities.getDirection(normal);
			    if(direction == SwingUtilities.EAST) {
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

		    if(copyHead != null && copyTail != null) {
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
	    while(node != null && !model.containsNode(graph, node)) {
		Object parent = model.getParent(node);
		if(model.isNode(parent)) {
		    node = parent;
		} else {
		    node = null;
		}
	    }
	    return node;
	}
    }

    // A layout target that translates locatable nodes.
    private class PtolemyLayoutTarget extends BasicLayoutTarget {
	/**
	 * Construce a new layout target that operates
	 * in the given pane.
	 */
	public PtolemyLayoutTarget(GraphController controller) {
	    super(controller);
	}
    
	/**
	 * Translate the figure associated with the given node in the
	 * target's view by the given delta.
	 */
	public void translate(Object node, double dx, double dy) {
	    super.translate(node, dx, dy);
	    // FIXME: this is a bad way to handle locatables.
	    if(node instanceof Locatable) {
		double location[] = ((Locatable)node).getLocation();
		if(location == null) {
		    location = new double[2];
		    Figure figure = getController().getFigure(node);
		    location[0] = figure.getBounds().getCenterX();
		    location[1] = figure.getBounds().getCenterY();
		} else {
		    location[0] += dx;
		    location[1] += dy;
		}
		((Locatable)node).setLocation(location);
 	    }
	}
    }

    /**
     * The factory for creating context menus on relations.
     */
    // FIXME this has to move into the visual notation.
    public class RelationContextMenuFactory 
	extends PtolemyMenuFactory {
	public RelationContextMenuFactory(GraphController controller) {
	    super(controller);
	    addMenuItemFactory(new EditParametersFactory());
	    addMenuItemFactory(new EditParameterStylesFactory());
	    addMenuItemFactory(new MenuActionFactory(_getDocumentationAction));
	}
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Return a visual notation that can create a view on this document.
    // In this class, we search the toplevel entity in the model for a
    // Ptolemy notation attribute and return the first one found.
    //
    /*  private VisualNotation _getVisualNotation(CompositeEntity model) {
	List notationList = model.attributeList(VisualNotation.class);
	Iterator notations = notationList.iterator();
	VisualNotation notation = null;
        if(notations.hasNext()) {
	    notation = (VisualNotation) notations.next();
	} else {
	    notation = new PtolemyNotation();
	}
	return notation;
	}*/

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // FIXME: should be somewhere else?
    // Default background color is a light grey.
    private static Color BACKGROUND_COLOR = new Color(0xe5e5e5);

    // The model that this window controls, if any.
    private CompositeEntity _model;

    private JGraph _jgraph;
    private JScrollPane _graphScrollPane;
    private JPanner _graphPanner;
    private JTree _library;
    private JScrollPane _libraryScrollPane;
    private JPanel _palettePane;
    private JSplitPane _splitPane;
	
    private JMenu _editMenu;
    private JMenu _executeMenu;
    private JToolBar _toolbar;

    private Action _getDocumentationAction;
    private Action _editIconAction;
    private Action _lookInsideAction;
    private Action _newPortAction;
    private Action _newRelationAction;
    private Action _executeSystemAction;
    private Action _cutAction;
    private Action _copyAction;
    private Action _pasteAction;
    private Action _layoutAction;

    private JComboBox _directorComboBox;
    private DefaultComboBoxModel _directorModel;
}
