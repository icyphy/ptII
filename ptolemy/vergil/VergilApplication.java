/* The ptolemy schematic editor.

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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil;

import ptolemy.actor.*;
import ptolemy.actor.gui.*;
import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.IntToken;
import ptolemy.gui.*;
import ptolemy.moml.MoMLParser;
import ptolemy.vergil.graph.*;
import ptolemy.vergil.toolbox.*;
import diva.canvas.interactor.SelectionModel;
import diva.canvas.Figure;
import diva.graph.*;
import diva.graph.editor.*;
import diva.graph.layout.*;
import diva.graph.model.*;
import diva.graph.toolbox.*;
import diva.gui.*;
import diva.gui.toolbox.*;
import diva.resource.DefaultBundle;
import diva.resource.RelativeBundle;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.event.*;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.DataOutputStream;
import java.net.URL;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.filechooser.FileFilter;

//////////////////////////////////////////////////////////////////////////
//// VergilApplication
/**
Vergil is the user interface for Ptolemy II.

@author Steve Neuendorffer
@contributor John Reekie
@version $Id$
*/
public class VergilApplication extends MDIApplication {
    /**
     * Construct a new graph editing application.
     */
    public VergilApplication() {
        super();

        // Create local objects
	JTreePane treepane = new JTreePane(".");
        DesktopFrame frame = new DesktopFrame(this, treepane);
        setApplicationFrame(frame);

        // Create and initialize the storage policy
        DefaultStoragePolicy storage = new DefaultStoragePolicy();
        setStoragePolicy(storage);
	FileFilter ff = new FileFilter() {
	    public boolean accept(File file) {
		if(file.isDirectory()) {
		    return true;
		}
		else {
		    return GUIUtilities.getFileExtension(file).
                        toLowerCase().equals("xml");
		}
	    }
	    public String getDescription() {
		return "XML files";
	    }
	};
        JFileChooser fc;
        fc = storage.getOpenFileChooser();
        fc.addChoosableFileFilter(ff);
        fc.setFileFilter(ff);

        fc = storage.getSaveFileChooser();
        fc.addChoosableFileFilter(ff);
        fc.setFileFilter(ff);

        setDocumentFactory(new PtolemyDocument.Factory());
        // _incrementalLayout = new LevelLayout();

        // Initialize the menubar, toolbar, and palettes
        initializeMenuBar(frame.getJMenuBar());
        initializeToolBar(frame.getJToolBar());
        initializePalette();

        Icon icon = getResources().getImageIcon("GraphIconImage");
        Image iconImage = getResources().getImage("GraphIconImage");

        frame.setFrameIcon(icon);
        frame.setIconImage(iconImage);

        setCurrentDocument(null);

        // Swing is stupid and adds components with the cross-platform UI and
        // not the system UI.
        SwingUtilities.updateComponentTreeUI(treepane);

	// Start with a new document.
	// This is kindof
	// bogus, but it is not easy to fire the action manually.
	Action action = getAction(DefaultActions.NEW);
	// FIXME this is really a horrible horrible hack.
	javax.swing.Timer timer = new javax.swing.Timer(100, action);
	timer.setRepeats(false);
	timer.start();
    }

    /** Given a document, create a new view which displays that
     * document. This class creates a JGraph.
     */
    public JComponent createView(Document d) {
	if(!(d instanceof VergilDocument)) {
	    throw new RuntimeException("Can only create views " +
				       "on Vergil documents.");
	}
	VisualNotation notation = ((VergilDocument)d).getVisualNotation();
	GraphPane pane = notation.createView(d);

	// CRAP.. This stuff is registered on the JGraph.  
	// Can we figure out how to register it on the graph pane?
	JGraph jgraph = new JGraph(pane);
	new EditorDropTarget(jgraph);
        GraphController controller =
	    jgraph.getGraphPane().getGraphController();

	ActionListener deletionListener = new DeletionListener();
        jgraph.registerKeyboardAction(deletionListener, "Delete",
                KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        jgraph.setRequestFocusEnabled(true);
        jgraph.addMouseListener(new MouseFocusMover());
        return jgraph;

	/*
        //FIXME
	GraphPane pane = new GraphPane(new EditorGraphController(),
                new VergilGraphImpl());
	JGraph jgraph = new JGraph(pane);
	new EditorDropTarget(jgraph);
        GraphController controller =
	    jgraph.getGraphPane().getGraphController();

	CompositeEntity entity =
	    (CompositeEntity) ((VergilDocument)d).getGraph();
	GraphImpl impl = controller.getGraphImpl();
	Graph graph = impl.createGraph(entity);

        // FIXME layout parts of graph that don't have a location.

        // Set and draw the new graph
        controller.setGraph(graph);

	*/
    }

    /** Delete any selected nodes and all attached edges in the current graph.
     */
    public class DeletionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JGraph jgraph = (JGraph) e.getSource();
            GraphPane graphPane = jgraph.getGraphPane();
            EditorGraphController controller =
                (EditorGraphController)graphPane.getGraphController();
            GraphImpl impl = controller.getGraphImpl();
            SelectionModel model = controller.getSelectionModel();
            Object selection[] = model.getSelectionAsArray();
            // Remove all the edges first, since if we remove the nodes first,
            // then removing the nodes might remove some of the edges.
            for(int i = 0; i < selection.length; i++) {
		if(selection[i] instanceof Figure) {
		    Object userObject =
                        ((Figure)selection[i]).getUserObject();
		    if(userObject instanceof Edge) {
                        model.removeSelection(selection[i]);
                        Edge edge = (Edge) userObject;
                        controller.removeEdge(edge);
		    }
                }
            }
	    for(int i = 0; i < selection.length; i++) {
		if(selection[i] instanceof Figure) {
		    Object userObject =
                        ((Figure)selection[i]).getUserObject();
                    if(userObject instanceof Node) {
                        model.removeSelection(selection[i]);
			Node node = (Node) userObject;
			controller.removeNode(node);
                    }
                }
            }
	}
    }

    /** Grab the keyboard focus when the component that this listener is
     *  attached to is clicked on.
     */
    public class MouseFocusMover extends MouseAdapter {
        public void mouseClicked(
                MouseEvent mouseEvent) {
	    Component component =
                mouseEvent.getComponent();

            if (!component.hasFocus()) {
                component.requestFocus();
            }
        }
    }

    /** Redisplay a document after it appears on the screen. This method
     * should be overridden by applications that need to perform some
     * form of update when the component appears on the screen.
     * This class executes a graph layout algorithm on the document
     */
    public void redisplay(Document d, JComponent c) {
        JGraph jgraph = (JGraph) c;
        //       redoLayout(jgraph, (String) _layoutComboBox.getSelectedItem());
    }

    /** Return the entity library associated with this Vergil
     */
    public CompositeEntity getEntityLibrary() {
	return _entityLibrary;
    }

    /** Return the icon library associated with this Vergil
     */
    public CompositeEntity getIconLibrary() {
	return _iconLibrary;
    }

    /** Get the title of this application
     */
    public String getTitle() {
        return "Vergil Editor";
    }

    /** Initialize the palette in the.
     */
    public void initializePalette() {
        DesktopFrame frame = ((DesktopFrame) getApplicationFrame());
        JTreePane pane = (JTreePane)frame.getPalettePane();

        parseLibraries();
	//System.out.println("Icons = " + _iconLibrary.description());

        CompositeEntity lib = getEntityLibrary();

	// We have "" because that is the name that was given in the
	// treepane constructor.
	//System.out.println("lib = " + lib.description());
        createTreeNodes(pane, lib.getFullName(), lib);

	JSplitPane splitPane = frame.getSplitPane();

	// There are differences in the way swing acts in JDK1.2 and 1.3
	// The way to get it to work with both is to set
	// the preferred size along with the minimum size.   JDK1.2 has a
	// bug where the preferred size may be inferred to be less than the
	// minimum size when the pane is first created.
	pane.setMinimumSize(new Dimension(150, 150));
	((JComponent)pane.getTopComponent()).
	    setMinimumSize(new Dimension(150, 150));
	((JComponent)pane.getTopComponent()).
	    setPreferredSize(new Dimension(150, 150));
	splitPane.validate();
    }

    public void createTreeNodes(JTreePane pane,
            String parent, CompositeEntity library) {
        Iterator entities = library.entityList().iterator();
        int i = 0;
        while(entities.hasNext()) {
            Entity entity = (Entity)entities.next();
            if(!(entity instanceof CompositeEntity)) {
		GraphPalette palette = (GraphPalette)
		    pane.getComponentAt(library.getFullName());
		ptolemy.moml.Icon icon =
		    (ptolemy.moml.Icon) entity.getAttribute("_icon");
                palette.addNode(icon,
                        60, 50 + (i++) * 50);
            }

            if(entity instanceof CompositeEntity) {
                GraphPalette palette = new GraphPalette();
		pane.addEntry(parent, entity.getFullName(), palette);
		createTreeNodes(pane, entity.getFullName(),
                        (CompositeEntity)entity);
		palette.setMinimumSize(new Dimension(200, 200));
            }
        }
    }

    public RelativeBundle getGUIResources() {
        return _guiResources;
    }

    /** Initialize the given menubar. Currently, all strings are
     * hard-wired, but maybe we should be getting them out of the
     * application resources.
     */
    public void initializeMenuBar(JMenuBar mb) {
        Action action;
        JMenuItem item;

        // Create the File menu
        JMenu menuFile = new JMenu("File");
        menuFile.setMnemonic('F');
        mb.add(menuFile);

        action = DefaultActions.newAction(this);
        addAction(action);
        addMenuItem(menuFile, action, 'N', "Create a new graph document");

        action = DefaultActions.openAction(this);
        addAction(action);
        addMenuItem(menuFile, action, 'O', "Open a graph document");

        action = DefaultActions.closeAction(this);
        addAction(action);
        addMenuItem(menuFile, action, 'C', "Close the current graph document");

        menuFile.addSeparator();

        action = DefaultActions.saveAction(this);
        addAction(action);
        addMenuItem(menuFile, action, 'S', "Save the current graph document");

        action = DefaultActions.saveAsAction(this);
        addAction(action);
        addMenuItem(menuFile, action, 'A',
                "Save the current graph document to a different file");

        menuFile.addSeparator();

        action = DefaultActions.exitAction(this);
        addAction(action);
        addMenuItem(menuFile, action, 'X', "Exit from the graph editor");

        // Create the Devel menu
        JMenu menuDevel = new JMenu("Devel");
        menuDevel.setMnemonic('D');
        mb.add(menuDevel);

        action = new AbstractAction ("Print document info") {
            public void actionPerformed(ActionEvent e) {
                Document d = getCurrentDocument();
                if (d == null) {
                    System.out.println("Graph document is null");
                } else {
                    System.out.println(d.toString());
                }
            }
        };
        addAction(action);
        addMenuItem(menuDevel, action, 'P', "Print current document info");

        action = new AbstractAction("Execute System") {
            public void actionPerformed(ActionEvent e) {
                PtolemyDocument d = (PtolemyDocument)getCurrentDocument();
                if (d == null) {
                    return;
                }
                try {
		    CompositeActor toplevel =
                        (CompositeActor) d.getGraph();

                    // FIXME there is alot of code in here that is similar
                    // to code in MoMLApplet and MoMLApplication.  I think
                    // this should all be in ModelPane.
                    // FIXME set the Director.  This is a hack, but it's the
                    // Simplest hack.
                    if(toplevel.getDirector() == null) {
                        ptolemy.domains.sdf.kernel.SDFDirector director =
                            new ptolemy.domains.sdf.kernel.SDFDirector(
                                    toplevel.workspace());
                        //		    _entityLibrary.getEntity(
                        //	(String)_directorComboBox.getSelectedItem());
                        toplevel.setDirector(director);
                        director.iterations.setExpression("25");
                    }

                    // Create a manager.
                    Manager manager = toplevel.getManager();
                    if(manager == null) {
                        manager =
                            new Manager(toplevel.workspace(), "Manager");
                        toplevel.setManager(manager);
                        // manager.addDebugListener(new StreamListener());
			manager.addExecutionListener(new VergilExecutionListener());
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

                    //                    manager.startRun();

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
                    ex.printStackTrace();
                    throw new GraphException(ex.getMessage());
                }

            }
        };
        addAction(action);
        addMenuItem(menuDevel, action, 'E', "Execute System");
    }

    /** Initialize the given toolbar. Image icons will be obtained
     * from the application resources and added to the
     * actions. Note that the image icons are not added to the actions
     * -- if we did that, the icons would appear in the menus, which I
     * suppose is a neat trick but completely useless.
     */
    public void initializeToolBar(JToolBar tb) {
        Action action;
        RelativeBundle resources = getResources();

        // Conventional new/open/save buttons
        action = getAction(DefaultActions.NEW);
        addToolBarButton(tb, action, null, resources.getImageIcon("NewImage"));

        action = getAction(DefaultActions.OPEN);
        addToolBarButton(tb, action, null, resources.getImageIcon("OpenImage"));
        action = getAction(DefaultActions.SAVE);
        addToolBarButton(tb, action, null, resources.getImageIcon("SaveImage"));
        //tb.addSeparator();

	String dflt = "";
        // Layout combobox
        _layoutComboBox = new JComboBox();
        dflt = "Random layout";
        _layoutComboBox.addItem(dflt);
        _layoutComboBox.addItem("Levelized layout");
        _layoutComboBox.setSelectedItem(dflt);
        _layoutComboBox.setMaximumSize(_layoutComboBox.getMinimumSize());
        _layoutComboBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    VergilDocument d = (VergilDocument) getCurrentDocument();
                    JGraph jg = (JGraph) getView(d);
                    redoLayout(jg, (String) e.getItem());
                }
            }
        });
        tb.add(_layoutComboBox);

        //tb.addSeparator();

	//FIXME find these names somehow.
	_directorComboBox = new JComboBox();
	dflt = "sdf.director";
        _directorComboBox.addItem(dflt);
        _directorComboBox.setSelectedItem(dflt);
        _directorComboBox.setMaximumSize(_directorComboBox.getMinimumSize());
        _directorComboBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    // FIXME do something.
                }
            }
        });
        tb.add(_directorComboBox);
    }

    /** Create and run a new graph application
     */
    public static void main(String argv[]) {
        VergilApplication ge = new VergilApplication();
        ge.setVisible(true);
    }

    /** Parse the xml libraries
     */
    public void parseLibraries() {
        URL iconlibURL = null;
        URL entitylibURL = null;
        try {
            iconlibURL = getGUIResources().getResource("rootIconLibrary");
            entitylibURL = getGUIResources().getResource("rootEntityLibrary");

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
            System.out.println(e);
        }
    }

    /** Redo the layout of the given JGraph.
     */
    public void redoLayout(JGraph jgraph, String type) {
        GraphController controller = jgraph.getGraphPane().getGraphController();
        LayoutTarget target = new BasicLayoutTarget(controller);
        Graph graph = controller.getGraph();
        GlobalLayout layout;

        if (type.equals("Random layout")) {
            layout = new RandomLayout();
        } else if(type.equals("Grid layout")) {
	    layout = new GridAnnealingLayout();
	} else {
            layout = new LevelLayout();
        }
        // Perform the layout and repaint
        try {
            layout.layout(target, graph);
        } catch (Exception e) {
            showError("layout", e);
        }
        jgraph.repaint();
    }

    /** Set the given document to be the current document, and raise
     * the internal window that corresponds to that component.
     * In this class, there are some things that we want to enable and
     * disable if there are no documents present.
     */
    public void setCurrentDocument(Document d) {
        super.setCurrentDocument(d);
        if(d == null) {
            Action saveAction = getAction(DefaultActions.SAVE);
            saveAction.setEnabled(false);
            Action saveAsAction = getAction(DefaultActions.SAVE_AS);
            saveAsAction.setEnabled(false);
        } else {
            Action saveAction = getAction(DefaultActions.SAVE);
            saveAction.setEnabled(true);
            Action saveAsAction = getAction(DefaultActions.SAVE_AS);
            saveAsAction.setEnabled(true);
        }

    }

    public class VergilExecutionListener implements ExecutionListener {
	public VergilExecutionListener() {
	}

	public void executionError(Manager manager, Exception exception) {
	    showError(manager.getName(), exception);
	}

	public void executionFinished(Manager manager) {

	}

	public void managerStateChanged(Manager manager) {
	    DesktopFrame frame = (DesktopFrame) getApplicationFrame();
	    JStatusBar statusBar = frame.getStatusBar();
	    statusBar.setMessage(manager.getState().getDescription());
	}
    }

    /** The frame in which any placeable objects create their output.
     *  This will be null until a model with something placeable is
     *  executed.
     */
    private JFrame _executionFrame = null;

    /** The director selection combobox
     */
    private JComboBox _directorComboBox;

    /** The layout selection combobox
     */
    private JComboBox _layoutComboBox;

    /** The layout engine
     */
    private GlobalLayout _globalLayout;

    /** The application specific resources
     */
    private RelativeBundle _guiResources =
    new RelativeBundle("ptolemy.vergil.Library", getClass(), null);

    /** The Icon Library
     */
    private CompositeEntity _iconLibrary;

    /** The Entity Library
     */
    private CompositeEntity _entityLibrary;
}
