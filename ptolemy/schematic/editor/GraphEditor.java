/* The ptolemy schematic editor.

 Copyright (c) 1998-1999 The Regents of the University of California.
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

package ptolemy.schematic.editor;

import ptolemy.schematic.util.EditorIcon;
import ptolemy.schematic.util.IconLibrary;
import ptolemy.schematic.util.IconLibraryFactory;
import ptolemy.schematic.util.LibraryIcon;
import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.IntToken;
import ptolemy.gui.*;
import ptolemy.moml.MoMLParser;
import diva.graph.*;
import diva.graph.editor.*;
import diva.graph.layout.*;
import diva.graph.model.*;
import diva.graph.toolbox.*;
import diva.gui.*;
import diva.gui.toolbox.*;
import diva.resource.DefaultBundle;
import diva.resource.RelativeBundle;
import java.awt.Image;
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
//// GraphEditor
/**
 * A graph editor for non-hierarchical graphs. This is a complete
 * graph-editing application that includes automatic layout, load from
 * and save to XML, editable properties, and so on. It is intended as
 * an example application that you can use as a basis for building a
 * customized graph editor for your own application.
 *
 * @author Steve Neuendorffer, John Reekie 
 * @version $Id$
 */
public class GraphEditor extends MDIApplication {

    /** A mapping from documents to content panes
     */
    private HashMap _contentPanes = new HashMap();

    /** A mapping from content panes to documents
     */
    private HashMap _documents = new HashMap();

    /** The director selection combobox
     */
    private JComboBox _directorComboBox;

    /** The layout selection combobox
     */
    private JComboBox _layoutComboBox;

    /** The layout engine
     */
    private GlobalLayout _globalLayout;

    /** Construct a new graph editing application. The application
     * will not have any open graph documents, until they are opened
     * by getting the "Open" action an invoking its actionPerformed()
     * method.
     */
    public GraphEditor () {
        super();

        // Create local objects
	JTreePane treepane = new JTreePane("");	
        DesktopFrame frame = new DesktopFrame(this, treepane);
        setApplicationFrame(frame);

        // Create and initialize the storage policy
        DefaultStoragePolicy storage = new DefaultStoragePolicy();
        setStoragePolicy(storage);
	FileFilter ff = new FileFilter() {
	    public boolean accept (File file) {
		return GUIUtilities.getFileExtension(file).
                    toLowerCase().equals("xml");
	    }
	    public String getDescription () {
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

        setDocumentFactory(new GraphDocument.Factory());
        // _incrementalLayout = new LevelLayout();

        // Initialize the menubar, toolbar, and palettes
        initializeMenuBar(frame.getJMenuBar());
        initializeToolBar(frame.getJToolBar());
        initializePalette();     
	
        Icon icon = getResources().getImageIcon("GraphIconImage");
        Image iconImage = getResources().getImage("GraphIconImage");
	
        frame.setFrameIcon(icon);
        frame.setIconImage(iconImage);
		
        // Experimental -- doesn't work... open a file
        // getAction("open").actionPerformed(null);
    }

    /** Given a document, create a new view which displays that
     * document. This class creates a JGraph.
     */
    public JComponent createView (Document d) {
	GraphPane pane = new GraphPane(new EditorGraphController(), 
				       new EditorGraphImpl());
	JGraph jgraph = new JGraph(pane);
	new EditorDropTarget(jgraph);
        GraphController controller = 
	    jgraph.getGraphPane().getGraphController();
        
	CompositeEntity entity = (CompositeEntity) ((GraphDocument)d).getGraph();
	GraphImpl impl = controller.getGraphImpl();
	Graph graph = impl.createGraph(entity);
	
        // Set and draw the new graph
        controller.setGraph(graph);

        return jgraph;

    }

    /** Redisplay a document after it appears on the screen. This method
     * should be overridden by applications that need to perform some
     * form of update when the component appears on the screen.
     * This class executes a graph layout algorithm on the document
     */
    public void redisplay (Document d, JComponent c) {
        JGraph jgraph = (JGraph) c;
        redoLayout(jgraph, (String) _layoutComboBox.getSelectedItem());
    }
   
    /** Return the entity library associated with this GraphEditor
     */
    public CompositeEntity getEntityLibrary () {
	return _entityLibrary;
    }

    /** Return the icon library associated with this GraphEditor
     */
    public IconLibrary getIconLibrary () {
	return _iconLibrary;
    }

    /** Get the title of this application
     */
    public String getTitle() {
        return "PtolemyII";
    }

    /** Initialize the palette in the.
     */
    public void initializePalette () {
        DesktopFrame frame = ((DesktopFrame) getApplicationFrame());
        JTreePane pane = (JTreePane)frame.getPalettePane();

        parseLibraries();
	//System.out.println("Icons = " + _iconLibrary.description());

        CompositeEntity lib = getEntityLibrary();
 
	// We have "" because that is the name that was given in the
	// treepane constructor.
        createTreeNodes(pane, "", lib);	
	
        // FIXME This should be setDividerLocation(double), but this 
        // appears to be broken in jdk1.2.2.   
	pane.setDividerLocation(150);
	JSplitPane splitPane = frame.getSplitPane();
	splitPane.setDividerLocation(150);
    }
    
    public void createTreeNodes(JTreePane pane,
            String parent, CompositeEntity library) {
	SchematicPalette palette = new SchematicPalette();
	Enumeration enum = library.getEntities();
        int i = 0;
	pane.addEntry(parent, library.getFullName(), palette);
 
        while(enum.hasMoreElements()) {
            Entity entity = 
                (Entity) enum.nextElement();
            if(!(entity instanceof CompositeEntity)) {
                palette.addEntity(entity, 
                        60, 50 + (i++) * 50);     
            }

            if(entity instanceof CompositeEntity) {
		createTreeNodes(pane, library.getFullName(),
                        (CompositeEntity)entity);
            }
        }	
	//palette.triggerLayout();
    }

    /** Initialize the given menubar. Currently, all strings are
     * hard-wired, but maybe we should be getting them out of the
     * application resources.
     */
    public void initializeMenuBar (JMenuBar mb) {
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

        action = new AbstractAction ("Edit Director Parameters") {
            public void actionPerformed(ActionEvent e) {
                /*   // Create a dialog and attach the dialog values 
                // to the parameters of the object                    
                Document d = getCurrentDocument();
                if (d == null) {
                    return;
                } 
                Schematic schematic =
                    (Schematic) d.getCurrentSheet().getModel();
                SchematicDirector object = schematic.getDirector();

                System.out.println(object);
                JFrame frame = new JFrame("Parameters for " + object);
                JPanel pane = (JPanel) frame.getContentPane();
                
                Query query = new ParameterQuery(object);
                Enumeration parameters = object.deepParameters();
                pane.add(query);
                frame.setVisible(true);
                frame.pack();
                */
            }
        };
        addAction(action);
        addMenuItem(menuDevel, action, 'P', "Edit Director Parameters");

        action = new AbstractAction ("Execute System") {
            public void actionPerformed(ActionEvent e) {
                GraphDocument d = (GraphDocument)getCurrentDocument();
                if (d == null) {
                    return;
                } 
                try {
		    CompositeActor toplevel = 
		    (CompositeActor) d.getGraph();

                    // FIXME set the Director.  This is a hack, but it's the 
                    // Simplest hack.
		    ptolemy.domains.sdf.kernel.SDFDirector director = 
		    new ptolemy.domains.sdf.kernel.SDFDirector();
		    //		    _entityLibrary.getEntity(
		    //	(String)_directorComboBox.getSelectedItem());
		    toplevel.setDirector(director);
                    director.iterations.setExpression("25");

                    Manager manager = new Manager();
		    toplevel.setManager(manager);
		    // manager.addDebugListener(new StreamListener());
                    manager.startRun();
                    
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
    public void initializeToolBar (JToolBar tb) {
        Action action;
        RelativeBundle resources = getResources();

        // Conventional new/open/save buttons
        action = getAction("New");
        addToolBarButton(tb, action, null, resources.getImageIcon("NewImage"));

        action = getAction("Open");
        addToolBarButton(tb, action, null, resources.getImageIcon("OpenImage"));
        action = getAction("Save");
        addToolBarButton(tb, action, null, resources.getImageIcon("SaveImage"));
        //tb.addSeparator();

        // Layout combobox
        _layoutComboBox = new JComboBox();
        String dflt = "Random layout";
        _layoutComboBox.addItem(dflt);
        _layoutComboBox.addItem("Levelized layout");
        _layoutComboBox.setSelectedItem(dflt);
        _layoutComboBox.setMaximumSize(_layoutComboBox.getMinimumSize());
        _layoutComboBox.addItemListener(new ItemListener() {
            public void itemStateChanged (ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    GraphDocument d = (GraphDocument) getCurrentDocument();
                    JGraph jg = (JGraph) _contentPanes.get(d);
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
            public void itemStateChanged (ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    // FIXME do something.
                }
            }
        });
        tb.add(_directorComboBox);
    }

    /** Create and run a new graph application
     */
    public static void main (String argv[]) {
        GraphEditor ge = new GraphEditor();
        ge.setVisible(true);
    }

    /** Parse the xml libraries
     */
    public void parseLibraries () {
        URL iconlibURL = null;
        URL entitylibURL = null;
        // Get the path to the icon library. Read the PTII root from
        // the system properties
        try {
            URL urlbase = new URL("file:" + System.getProperty("PTII"));
            iconlibURL = new URL(urlbase, 
		"ptII/ptolemy/actor/lib/genericicons.ptml");
				 
            //            entitylibURL = new URL(urlbase, 
            //		"ptII/ptolemy/schematic/lib/rootEntityLibrary.ptml");
            entitylibURL = new URL(urlbase, 
		"ptII/ptolemy/actor/lib/genericentities.xml");
				   	    
            _iconLibrary = new IconLibrary();
	    _iconLibrary.setName("root");
	    IconLibraryFactory.parseIconLibrary(_iconLibrary, iconlibURL);
	    LibraryIcon.setIconLibrary(_iconLibrary);

            MoMLParser parser = new MoMLParser();
            _entityLibrary =
                (CompositeEntity) parser.parse(entitylibURL,
					       entitylibURL.openStream());

            //            _entityLibrary = 
            //   PTMLObjectFactory.parseEntityLibrary(entitylibURL, 
            //          _iconLibrary);
            System.out.println("Parsed:\n" + _entityLibrary);
        }
        catch (Exception e) {
            System.out.println(e);
        }
    } 

    /** Redo the layout of the given JGraph.
     */
    public void redoLayout (JGraph jgraph, String type) {
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

    IconLibrary _iconLibrary;
    CompositeEntity _entityLibrary;
}

