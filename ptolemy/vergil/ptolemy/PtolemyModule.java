/* A modular Vergil package for Ptolemy models.

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

@ProposedRating Yellow (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.ptolemy;

import ptolemy.actor.*;
import ptolemy.actor.gui.*;
import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.IntToken;
import ptolemy.gui.*;
import ptolemy.moml.Vertex;
import ptolemy.moml.MoMLParser;
import ptolemy.vergil.*;
import ptolemy.vergil.graph.*;
import ptolemy.vergil.toolbox.*;

import diva.canvas.*;
import diva.canvas.connector.*;
import diva.graph.*;
import diva.graph.layout.*;
import diva.graph.model.*;
import diva.graph.toolbox.GraphParser;
import diva.graph.toolbox.GraphWriter;
import diva.gui.*;
import diva.gui.toolbox.*;
import diva.resource.RelativeBundle;
import java.util.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.net.URL;

import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import ptolemy.domains.sdf.kernel.*;
import ptolemy.domains.pn.kernel.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.domains.dde.kernel.*;
import ptolemy.domains.csp.kernel.*;
import ptolemy.domains.fsm.kernel.FSMDirector;

/**
 * A module that can be plugged into Vergil that adds support for
 * Ptolemy II.  This package adds a Ptolemy II menu to the menu bar, which
 * allows access to the model of the currently selected document, if that
 * document is a Ptolemy document.  It also adds a new tool bar that contains
 * a pulldown menu for selecting directors and a pulldown menu for executing a
 * graph layout algorithm.
 * <p>
 * This package contains a list of Directors which are placed in a
 * toolbar menu.
 * <p>
 * This package contains a list of Visual notations.  Each notation is
 * capable of creating a view on a Ptolemy Document.  In some cases,
 * certain notations may be preferable for some domains.  For instance,
 * Finite State Machine models are usually represented using a bubble
 * and arc diagram.  However, bubble and arc diagrams are not usually used
 * for dataflow diagrams.
 * <p>
 * Currently the only access to the model that is provided is execution of the
 * model.  When the model is executed, this package listens to the state of
 * the model's manager and updates the Vergil status bar.
 *
 * @author Steve Neuendorffer
 * @version $Id$
 */
public class PtolemyModule implements Module {
    /**
     * Create a new module that will extend the functionality of the given
     * Vergil application.  Create a new "Ptolemy II" menu and add it to
     * the application's menu bar.  Create and add an action for viewing a 
     * MoML description of the current document.  Create and add an action for
     * executing the model of the current document.  Both of these actions
     * are only enabled if the current document is an instance of
     * PtolemyDocument.  Also create a new toolbar and add it to the 
     * application.  Create and add a button to the toolbar which will 
     * trigger layout of the current document.
     */
    public PtolemyModule(VergilApplication application) {
	_application = application;
	Action action;
	// First add the the generic actions.

	action = new FigureAction("Look Inside") {
	    public void actionPerformed(ActionEvent e) {
		// Figure out what entity.
		super.actionPerformed(e);		
		NamedObj object = getTarget();
		if(!(object instanceof CompositeEntity)) return;
		CompositeEntity entity = (CompositeEntity)object;
		Application app = getApplication();
		PtolemyDocument doc = new PtolemyDocument(app);
		doc.setModel(entity);
		app.addDocument(doc);
		app.displayDocument(doc);
		app.setCurrentDocument(doc);}
	};
	_application.addAction(action);

        // Create the Devel menu
        JMenu menuDevel = new JMenu("Ptolemy II");
        menuDevel.setMnemonic('P');
        _application.addMenu(menuDevel);

        action = new AbstractAction ("Print document info") {
            public void actionPerformed(ActionEvent e) {
                Document d = _application.getCurrentDocument();
                if (d == null) {
                    System.out.println("Document is null");
                } else {
                    System.out.println(d.toString());
                }
            }
        };
	GUIUtilities.addMenuItem(menuDevel, action, 'I',
				 "Print current document info");

        action = new AbstractAction("Execute System") {
            public void actionPerformed(ActionEvent e) {
                PtolemyDocument d =
		(PtolemyDocument) _application.getCurrentDocument();
                if (d == null) {
                    return;
                }
                try {
		    CompositeActor toplevel =
                        (CompositeActor) d.getModel();

                    // FIXME there is alot of code in here that is similar
                    // to code in MoMLApplet and MoMLApplication.  I think
                    // this should all be in ModelPane.
                    
                    // Create a manager.
		    // Attaching these listeners is a nasty business...
		    // All Managers are not created equal, since some have
		    // listeners attached.
                    Manager manager = toplevel.getManager();
                    if(manager == null) {
                        manager =
                            new Manager(toplevel.workspace(), "Manager");
                        toplevel.setManager(manager);
			manager.addExecutionListener(
			    new VergilExecutionListener(_application));
                    }

                    if(_executionFrame != null) {
			_executionFrame.getContentPane().removeAll();
                    } else {
                        _executionFrame = new JFrame();
                    }

                    ModelPane modelPane = new ModelPane(toplevel);
                    _executionFrame.getContentPane().add(modelPane,
                            BorderLayout.NORTH);
                    // Create a panel to place placeable objects.
                    JPanel displayPanel = new JPanel();
                    displayPanel.setLayout(new BoxLayout(displayPanel,
                            BoxLayout.Y_AXIS));
                    modelPane.setDisplayPane(displayPanel);

                    // Put placeable objects in a reasonable place
                    for(Iterator i = toplevel.deepEntityList().iterator();
                        i.hasNext();) {
                        Object o = i.next();
                        if(o instanceof Placeable) {
                            ((Placeable) o).place(
                                    displayPanel);
                        }
                    }

                    if(_executionFrame != null) {
                        _executionFrame.setVisible(true);
                    }

		    final JFrame packframe = _executionFrame;
		    Action packer = new AbstractAction() {
			public void actionPerformed(ActionEvent event) {
			    packframe.getContentPane().doLayout();
			    packframe.repaint();
			    packframe.pack();
			}
		    };
		    javax.swing.Timer timer =
                        new javax.swing.Timer(200, packer);
		    timer.setRepeats(false);
		    timer.start();
                } catch (Exception ex) {
                    getApplication().showError("Execution Failed", ex);
                }

            }
        };
	GUIUtilities.addMenuItem(menuDevel, action, 'E', "Execute System");

	action = new AbstractAction("Automatic Layout") {
            public void actionPerformed(ActionEvent e) {
		PtolemyDocument d = (PtolemyDocument)
		    _application.getCurrentDocument();
		JGraph jg = (JGraph) _application.getView(d);
		_redoLayout(jg);
	    }
        };
        GUIUtilities.addMenuItem(menuDevel, action, 'L', 
				 "Automatically layout the model");
	
	JToolBar tb = new JToolBar();
	Container pane =
	    ((DesktopFrame)_application.getApplicationFrame()).getToolBarPane();
	pane.add(tb);
	
	String dflt = "";
	// Creating the renderers this way is rather nasty..
	NodeRenderer renderer = new PortController.PortRenderer();
	Figure figure = renderer.render(null);
	Icon icon = 
	    new FigureIcon(figure, 20, 20);
	GUIUtilities.addToolBarButton(tb, new newPortAction(), 
				      "New External Port", icon);

	renderer = new RelationController.RelationRenderer();
	figure = renderer.render(null);
	icon = new FigureIcon(figure, 20, 20);
	GUIUtilities.addToolBarButton(tb, new newRelationAction(), 
				      "New Relation", icon);

	//figure = EditorIcon._createDefaultBackgroundFigure();
	//icon = new FigureIcon(figure, 20, 20);
	//GUIUtilities.addToolBarButton(tb, new newCompositeAction(), 
	//			      "New Composite", icon);

	_directorModel = new DefaultComboBoxModel();
	try {
	    Director dir;
	    dir = new SDFDirector();
	    dir.setName("SDF");
	    _directorModel.addElement(dir);
	    dir = new PNDirector();
	    dir.setName("PN");
	    _directorModel.addElement(dir);
	    dir = new DEDirector();
	    dir.setName("DE");
	    _directorModel.addElement(dir);
	    dir = new CSPDirector();
	    dir.setName("CSP");
	    _directorModel.addElement(dir);
	    dir = new DDEDirector();
	    dir.setName("DDE");
	    _directorModel.addElement(dir);
	    dir = new FSMDirector();
	    dir.setName("FSM");
	    _directorModel.addElement(dir);
	}
	catch (Exception ex) {
	    _application.showError("Director combobox creation failed", ex);
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
		    Director director = (Director) e.getItem();
		    PtolemyDocument d = (PtolemyDocument)
			_application.getCurrentDocument();
		    if(d == null) return;
		    CompositeEntity entity =
			d.getModel();
		    if(entity instanceof Actor) {
			CompositeActor actor =
			    (CompositeActor) entity;
			try {
			    Director clone =
				(Director)director.clone(actor.workspace());
			    actor.setDirector(clone);
			} catch (Exception ex) {
                            getApplication().showError("Failed to set " + 
                                    "Director", ex);
			}
		    }
                }
            }
        });
        tb.add(_directorComboBox);

	ListDataListener ldl = new ListDataListener() {
	    public void contentsChanged(ListDataEvent event) {		
		// When the current document is changed, set the 
		// director menu to whatever director is currently associated
		// with the model in the document.
		PtolemyDocument d = 
		(PtolemyDocument)_application.getCurrentDocument();
		if(d == null) {
		    _directorModel.setSelectedItem(null);
		    return;
		}
                CompositeEntity entity = d.getModel();
                if(!(entity instanceof CompositeActor)) {
                    _directorModel.setSelectedItem(null);
                    return;
                }
		CompositeActor actor = (CompositeActor)entity;
		Director director = actor.getDirector();
		if(director == null) {
		    _directorModel.setSelectedItem(null);
		    return;
		}
		Director foundDirector = null;
		for(int i = 0; foundDirector == null && i < _directorModel.getSize(); i++) {
		    if(director.getClass().isInstance(_directorModel.getElementAt(i))) {
		    	foundDirector = 
		    	(Director)_directorModel.getElementAt(i);
		    }
		}
		_directorModel.setSelectedItem(foundDirector);
	    }
	    public void intervalAdded(ListDataEvent event) {
		contentsChanged(event);
	    }
	    public void intervalRemoved(ListDataEvent event) {
		contentsChanged(event);
	    }
	};
	application.addDocumentListener(ldl);
        application.addDocumentFactory(new PtolemyDocument.Factory());
        application.addDocumentFactory(new PtolemyDocument.FSMFactory());

	SwingUtilities.invokeLater(new PaletteInitializer());

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Add a visual notation to the list of visual notations.
     */
    public void addNotation(PtolemyNotation notation) {
	_notationModel.addElement(notation);
    }

    /**
     * Add a director to the list of possible directors.
     */
    public void addDirector(Director director) {
	_directorModel.addElement(director);
    }

    /**
     * Return a list of the directors in the director list.
     */
    public List directorList() {
	List list = new LinkedList();
	for(int i = 0; i < _directorModel.getSize(); i++) {
	    list.add(_directorModel.getElementAt(i));
	}
	return list;
    }

    /** 
     * Return the application that contains this module.
     */
    public Application getApplication() {
        return _application;
    }

    /** 
     * Return the entity library for this application.
     */
    public CompositeEntity getEntityLibrary() {
        return _entityLibrary;
    }

    /** 
     * Return the icon library associated with this Vergil.
     */
    public CompositeEntity getIconLibrary() {
	return _iconLibrary;
    }

    /** 
     * Return the resources for this module.
     */
    public RelativeBundle getModuleResources() {
        return _moduleResources;
    }

    /**
     * Return a list of the notations in the notation list.
     */
    public List notationList() {
	List list = new LinkedList();
	for(int i = 0; i < _notationModel.getSize(); i++) {
	    list.add(_notationModel.getElementAt(i));
	}
	return list;
    }

    /**
     * Remove a director from the list of directors.
     */
    public void removeDirector(Director director) {
	_directorModel.removeElement(director);
    }

    /**
     * Remove a notation from the list of notations.
     */
    public void removeNotation(PtolemyNotation notation) {
	_notationModel.removeElement(notation);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private inner classes                 ////

    // An action to create a new port.
    private class newPortAction extends AbstractAction {
	public newPortAction() {
	    super("New External Port");
	}

	public void actionPerformed(ActionEvent e) {
	    VergilApplication app = (VergilApplication)getApplication();
	    Document doc = app.getCurrentDocument();
	    // Only create ports for ptolemy documents.
	    if(!(doc instanceof PtolemyDocument)) return;
	    JGraph jgraph = (JGraph)app.getView(doc);
	    GraphPane pane = jgraph.getGraphPane();
	    Point2D point = pane.getSize();
	    double x = point.getX()/2;
	    double y = point.getY()/2;
	    EditorGraphController controller = 
		(EditorGraphController)pane.getGraphController();
	    Graph graph = controller.getGraph();
	    CompositeEntity toplevel =
	    (CompositeEntity)graph.getSemanticObject();
	    Port port = null;
	    if(toplevel == null)
		return;
	    else {
		try {
		    port = toplevel.newPort(toplevel.uniqueName("port"));
		}
		catch (Exception ex) {
		    VergilApplication.getInstance().showError(
			 "Create port failed:", ex);
		    return;
		}
	    }
	    Node node = controller.getPortController().createNode(port);
	    controller.getPortController().addNode(node, x, y);
	}
    }
    
    // An action to create a new relation.
    private class newRelationAction extends AbstractAction {
	public newRelationAction() {
	    super("New Relation");
	}

	public void actionPerformed(ActionEvent e) {
	    VergilApplication app = (VergilApplication)getApplication();
	    Document doc = app.getCurrentDocument();
	    // Only create ports for ptolemy documents.
	    if(!(doc instanceof PtolemyDocument)) return;
	    JGraph jgraph = (JGraph)app.getView(doc);
	    GraphPane pane = jgraph.getGraphPane();
	    Point2D point = pane.getSize();
	    double x = point.getX()/2;
	    double y = point.getY()/2;
	    EditorGraphController controller = 
		(EditorGraphController)pane.getGraphController();
	    Graph graph = controller.getGraph();
	    CompositeEntity toplevel =
	    (CompositeEntity)graph.getSemanticObject();
	    Relation relation = null;
	    Vertex vertex = null;
	    if(toplevel == null)
		return;
	    else {
		try {
		    relation = 
			toplevel.newRelation(toplevel.uniqueName("relation"));
		    vertex = new Vertex(relation,
                        relation.uniqueName("vertex"));
		}
		catch (Exception ex) {
		    VergilApplication.getInstance().showError(
			 "Create relation failed:", ex);
		    return;
		}
	    }
	    Node node = 
		controller.getRelationController().createNode(vertex);
	    controller.getRelationController().addNode(node, x, y);
	}
    }
    
    // A Runnable object that is responsible for initializing the 
    // design palette.  This is done in a separate
    // thread, because loading the libraries can take quite a while.
    private class PaletteInitializer implements Runnable {
	/** 
	 * Parse the icon and entity libraries and populate the 
	 * design palette.
	 */
	public void run() {
	    DesktopFrame frame = 
		((DesktopFrame) _application.getApplicationFrame());
	    JPanel pane = (JPanel)frame.getPalettePane();
	    
	    _parseLibraries();

	    //System.out.println("Icons = " + _iconLibrary.description());

	    CompositeEntity library = getEntityLibrary();

	    // We have "" because that is the name that was given in the
	    // treepane constructor.
	    //System.out.println("lib = " + lib.description());
		// createNodes(pane, "TreePane", library);
	    JTree designTree = new JDesignTree(library);
	    JScrollPane scrollPane = new JScrollPane(designTree);
	    scrollPane.setMinimumSize(new Dimension(200, 200));
	    scrollPane.setPreferredSize(new Dimension(200, 200));
	    pane.setLayout(new BorderLayout());
	    pane.add(scrollPane, BorderLayout.CENTER);
	    
	    JSplitPane splitPane = frame.getSplitPane();
	    splitPane.resetToPreferredSizes();
	    splitPane.validate();
	    splitPane.repaint();
	} 
    }

    // An execution listener that displays the status of the current
    // document.
    public static class VergilExecutionListener implements ExecutionListener {
	public VergilExecutionListener(Application a) {
            _application = a;
        }

        // Defer to the application to display the error to the user.
	public void executionError(Manager manager, Exception exception) {
	    _application.showError(manager.getName(), exception);
	}
	
	// Do nothing when execution finishes
	public void executionFinished(Manager manager) {
	}

	// Display the new manager state in the application's status bar.
	public void managerStateChanged(Manager manager) {
	    DesktopFrame frame = (DesktopFrame)
		_application.getApplicationFrame();
	    JStatusBar statusBar = frame.getStatusBar();
	    statusBar.setMessage(manager.getState().getDescription());
	}
        private Application _application;
    }

    // A class for properly doing the layout of the graphs we have
    private class PtolemyLayout extends LevelLayout {
	/**
	 * Construct a new levelizing layout with a horizontal orientation.
	 */
	public PtolemyLayout() {
	    super();
	    setOrientation(LevelLayout.HORIZONTAL);
	}
	
	/**
	 * Construct a new levelizing layout with a vertical orientation
	 * which uses the given graph implementation on which to perform
	 * its layout, create dummy nodes, etc.
	 */
	public PtolemyLayout(GraphImpl impl) {
	    super(impl);
	}
	
	/**
	 * Copy the given graph and make the nodes/edges in the copied
	 * graph point to the nodes/edges in the original.
	 */
	protected Graph copyGraph(Graph origGraph, LayoutTarget target) {
	    GraphImpl impl = getGraphImpl();
	    Graph copyGraph = impl.createGraph(null);
	    Hashtable map = new Hashtable();
	    
	    // Copy all the nodes for the graph.
	    for(Iterator i = origGraph.nodes(); i.hasNext(); ) {
		Node origNode = (Node)i.next();
		if(target.isNodeVisible(origNode)) {
		    Rectangle2D r = target.getBounds(origNode);
		    LevelInfo inf = new LevelInfo();
		    inf.origNode = origNode;
		    inf.x = r.getX();
		    inf.y = r.getY();
		    inf.width = r.getWidth();
		    inf.height = r.getHeight();
		    Node copyNode = impl.createNode(inf);
		    impl.addNode(copyNode, copyGraph);
		    map.put(origNode, copyNode);
		}
	    }
	    
	    // Add all the edges.
	    for(Iterator i = GraphUtilities.localEdges(origGraph); 
		i.hasNext(); ) {
		Edge origEdge = (Edge)i.next();
		Node origTail = origEdge.getTail();
		Node origHead = origEdge.getHead();
		if(origHead != null && origTail != null) {
		    Figure tailFigure = (Figure)origTail.getVisualObject();
		    Figure headFigure = (Figure)origHead.getVisualObject();
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
				Node temp = origTail;
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
				Node temp = origTail;
				origTail = origHead;
				origHead = temp;
			    }
			}
		    }

		    origTail = _getParentInGraph(origGraph, origTail);
		    origHead = _getParentInGraph(origGraph, origHead);
		    Node copyTail = (Node)map.get(origTail);
		    Node copyHead = (Node)map.get(origHead);
		    if(copyHead != null && copyTail != null) {
			Edge copyEdge = impl.createEdge(origEdge);
			impl.setEdgeTail(copyEdge, copyTail);
			impl.setEdgeHead(copyEdge, copyHead);
		    }
		}
	    }
	    return copyGraph;
	}

	// Unfortunately, the head and/or tail of the edge may not 
	// be directly contained in the graph.  In this case, we need to
	// figure out which of their parents IS in the graph 
	// and calculate the cost of that instead.
	private Node _getParentInGraph(Graph graph, Node node) {
	    while(node != null && !graph.contains(node)) {
		Graph parent = node.getParent();
		if(parent instanceof Node) {
		    node = (Node)parent;
		} else {
		    node = null;
		}
	    }
	    return node;
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Parse the entity and icon XML libraries.  Set the entity and icon
    // libraries for this application.
    private void _parseLibraries() {
        URL iconlibURL = null;
        URL entitylibURL = null;
        try {
            iconlibURL = 
		getModuleResources().getResource("rootIconLibrary");
            entitylibURL = 
		getModuleResources().getResource("rootEntityLibrary");

            MoMLParser parser;
            parser = new MoMLParser();
	    _iconLibrary =
                (CompositeEntity) parser.parse(iconlibURL,
                        iconlibURL.openStream());
            LibraryIcon.setIconLibrary(_iconLibrary);

            //FIXME: this is bogus  The parser should be reusable.
            parser = new MoMLParser();
            _entityLibrary =
                (CompositeEntity) parser.parse(entitylibURL,
                        entitylibURL.openStream());
        }
        catch (Exception e) {
           getApplication().showError("Failed to parse libraries", e);
        }
    }

    // Redo the layout of the given JGraph.
    private void _redoLayout(JGraph jgraph) {
	GraphController controller = 
	    jgraph.getGraphPane().getGraphController();
        LayoutTarget target = new BasicLayoutTarget(controller);
        Graph graph = controller.getGraph();
        PtolemyLayout layout = new PtolemyLayout();
	layout.setOrientation(LevelLayout.HORIZONTAL);
	layout.setRandomizedPlacement(false);
        // Perform the layout and repaint
        try {
            layout.layout(target, graph);
        } catch (Exception e) {
            getApplication().showError("Layout failed", e);
        }
        jgraph.repaint();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The application that this package is associated with.
    private VergilApplication _application;

    // The frame in which any placeable objects create their output.
    //  This will be null until a model with something placeable is
    //  executed.
    private JFrame _executionFrame = null;
    
    // The director selection combobox
    private JComboBox _directorComboBox;

    // The layout button
    private JButton _layoutButton;

    // The list of directors.
    private DefaultComboBoxModel _directorModel;

    // The list of notations.
    private DefaultComboBoxModel _notationModel;

    // The layout engine
    private GlobalLayout _globalLayout;

    // The Icon Library.
    private CompositeEntity _iconLibrary;

    // The Entity Library.
    private CompositeEntity _entityLibrary;

    // The resources for this module.
    private RelativeBundle _moduleResources =
	new RelativeBundle("ptolemy.vergil.ptolemy.Ptolemy", getClass(), null);
}
