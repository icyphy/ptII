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

package ptolemy.vergil.ptolemy.kernel;

import ptolemy.vergil.ptolemy.GraphFrame;
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
//// KernelGraphFrame
/**
A simple graph view for ptolemy models.  This represents a level of the
hierarchy of a ptolemy model as a diva graph.  Cut, copy and paste operations
are supported using MoML and the graph itself is created using a visual
notation as a a factory

@author  Steve Neuendorffer
@version $Id$
*/
public class KernelGraphFrame extends GraphFrame {
   
    public KernelGraphFrame(CompositeEntity entity) {
	super(entity);
    }
	    
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the menus that are used by this frame.
     */
    protected void _addMenus() {
	super._addMenus();
       	_executeMenu = new JMenu("Execute");
        _executeMenu.setMnemonic(KeyEvent.VK_X);
	_menubar.add(_executeMenu);
	diva.gui.GUIUtilities.addMenuItem(_editMenu, _newPortAction);
       	diva.gui.GUIUtilities.addToolBarButton(_toolbar, _newPortAction);

	diva.gui.GUIUtilities.addMenuItem(_editMenu, _newRelationAction);
	diva.gui.GUIUtilities.addToolBarButton(_toolbar, _newRelationAction);

        _toolbar.add(_directorComboBox);

	diva.gui.GUIUtilities.addHotKey(_jgraph, _executeSystemAction);
	diva.gui.GUIUtilities.addMenuItem(_executeMenu, _executeSystemAction);
	diva.gui.GUIUtilities.addToolBarButton(_toolbar, 
					       _executeSystemAction);
	    }
    /** Create a new graph pane.
     */
    protected GraphPane _createGraphPane() {
	// create the graph editor
	// These two things control the view of a ptolemy model.
	_controller = new EditorGraphController();
	PtolemyGraphModel graphModel = new PtolemyGraphModel(_model);
	
	GraphPane pane = new GraphPane(_controller, graphModel);
	_newPortAction = _controller.getNewPortAction();
	_newRelationAction = _controller.getNewRelationAction();
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
	_executeSystemAction = new ExecuteSystemAction();
	_editIconAction = new EditIconAction();
	_lookInsideAction = new LookInsideAction();
	_getDocumentationAction = new GetDocumentationAction();
	_controller.getEntityController().setMenuFactory(new EntityContextMenuFactory(_controller));
 	_controller.getEntityPortController().setMenuFactory(new PortContextMenuFactory(_controller));
  	_controller.getPortController().setMenuFactory(new PortContextMenuFactory(_controller));
  	_controller.getRelationController().setMenuFactory(new RelationContextMenuFactory(_controller));
  	_controller.getLinkController().setMenuFactory(new RelationContextMenuFactory(_controller));
	return pane;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private inner classes                 ////

    private class EditIconAction extends FigureAction {
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
    private class EntityContextMenuFactory extends PtolemyMenuFactory {
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
    
    private class GetDocumentationAction extends FigureAction {
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
    private class RelationContextMenuFactory 
	extends PtolemyMenuFactory {
	public RelationContextMenuFactory(GraphController controller) {
	    super(controller);
	    addMenuItemFactory(new EditParametersFactory());
	    addMenuItemFactory(new EditParameterStylesFactory());
	    addMenuItemFactory(new MenuActionFactory(_getDocumentationAction));
	}
    }
  
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private EditorGraphController _controller;
    private Action _getDocumentationAction;
    private Action _editIconAction;
    private Action _lookInsideAction;
    private Action _newPortAction;
    private Action _newRelationAction;
    private JMenu _executeMenu;
    private Action _executeSystemAction;

    private JComboBox _directorComboBox;
    private DefaultComboBoxModel _directorModel;
}
