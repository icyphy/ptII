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

import ptolemy.schematic.util.IconLibrary;
import ptolemy.schematic.util.IconLibraryFactory;
import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.IntToken;
import ptolemy.gui.*;
import ptolemy.moml.*;
import diva.graph.*;
import diva.graph.editor.*;
import diva.graph.layout.*;
import diva.graph.model.*;
import diva.graph.toolbox.*;
import diva.gui.*;
import diva.gui.toolbox.*;
import java.awt.Image;
import java.awt.event.*;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.DataOutputStream;
import java.net.URL;
import java.util.*;
import javax.swing.*;
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
public class GraphEditor extends AbstractApplication {

    /** The frame we live in
     */
    private DesktopFrame _applicationFrame;

    /** The factory that creates graph documents
     */
    private DocumentFactory _documentFactory;

    /** Our storage policy
     */
    private DefaultStoragePolicy _storagePolicy;

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
        _applicationFrame = new DesktopFrame(this);
        _documentFactory = new GraphDocument.Factory();
        // _incrementalLayout = new LevelLayout();

        // Initialize the menubar, toolbar, and palettes
        initializeMenuBar(_applicationFrame.getJMenuBar());
        initializeToolBar(_applicationFrame.getJToolBar());
        initializePalette();     
	
        Icon icon = getApplicationResources().getImageIcon("GraphIcon");
        Image iconImage = getApplicationResources().getImage("GraphIcon");
        // Image iconImg = getApplicationResources().getImageIcon("GraphIcon");
	
        _applicationFrame.setFrameIcon(icon);
        _applicationFrame.setIconImage(iconImage);
	
        // Create and initialize the storage policy
	_storagePolicy = new DefaultStoragePolicy();
	JFileChooser fc = _storagePolicy.getFileChooser();
	FileFilter ff = new FileFilter() {
	    public boolean accept (File file) {
		return GUIUtilities.getFileExtension(file).
                    toLowerCase().equals("ptml");
	    }
	    public String getDescription () {
		return "PTML files";
	    }
	};
	fc.addChoosableFileFilter(ff);
	fc.setFileFilter(ff);
	
        // Experimental -- doesn't work... open a file
        // getAction("open").actionPerformed(null);
    }

    /** Get the frame that this application draws itself in.
     */
    public ApplicationFrame getApplicationFrame () {
        return _applicationFrame;
    }

    /** Display the given document. The document should already be
     * added to the application. After calling this method, most
     * callers should set this document to be the current document.
     */
    public void displayDocument (Document d) {
	GraphPane pane = new GraphPane(new EditorGraphController(), 
				       new EditorGraphImpl());
	JGraph jgraph = new JGraph(pane);
	new EditorDropTarget(jgraph);
        GraphController controller = 
	    jgraph.getGraphPane().getGraphController();
        
        Graph graph = (Graph) d.getSheet(0).getModel();

        // Set and draw the new graph
        controller.setGraph(graph);

        // Add the JGraph to the application frame
        _applicationFrame.addContentPane(d.getTitle(), jgraph);

        // Yuk we need hash tables to map jgraphs to documents ek
        _contentPanes.put(d, jgraph);
        _documents.put(jgraph, d);

        // If the pane gets the focus, make the document the current
        // document. This is a neat trick to avoid using hashes, actually...
        final Document fd = d;
        final JComponent jc = jgraph;
        _applicationFrame.addViewListener(new ViewAdapter() {
            public void viewSelected(ViewEvent e) {
                JComponent view = e.getView();
                // Check this is the right one
                if (view == jc) {
                    // FIXME: for some reason, closing
                    //        a view also causes that view
                    //        to be selected after it is
                    //        closed?
                    if(indexOf(fd) != -1) {
                        // Prevent recursion
                        if (getCurrentDocument() != fd) {
                            setCurrentDocument(fd);
                        }
                    }
                }
            }
            public void viewClosing(ViewEvent e) {
                JComponent view = e.getView();
                if (view == jc) {
                    // FIXME: avoid circular loop with the
                    // removeDocument method (if the
                    // file is closed from the menu,
                    // rather than by clicking the X in
                    // the internal pane
                    if(indexOf(fd) != -1) {
                        removeDocument(fd);
                        setCurrentDocument(getCurrentDocument());
                    }
                }
            }
        });

        // Perform the layout
        redoLayout(jgraph, (String) _layoutComboBox.getSelectedItem());
    }

    /** Get the graph document factory
     */
    public DocumentFactory getDocumentFactory () {
        return _documentFactory;
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

    /** Get the storage policy of this application.
     */
    public StoragePolicy getStoragePolicy () {
        return _storagePolicy;
    }

    /** Get the title of this application
     */
    public String getTitle() {
        return "PtolemyII";
    }

    /** Initialize the palette in the.
     */
    public void initializePalette () {
        JShadePane s =_applicationFrame.getShadePane();

        parseLibraries();
        //        JTabbedPane pane = createPaneFromEntityLibrary(_entityLibrary);
        // FIXME Get the right library.
        CompositeEntity lib = new CompositeEntity();
        try {
        
        lib.setName("root");
        ComponentEntity e;
        Port p;
	ptolemy.schematic.util.Icon i;
        e = new ComponentEntity(lib, "E1");
	p = e.newPort("P1");
	p = e.newPort("P2");
        i = new ptolemy.schematic.util.Icon(e);
        e = new ComponentEntity(lib, "E2");
        i = new ptolemy.schematic.util.Icon(e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }

	System.out.println("library = " + lib.description());
        JTabbedPane pane = createPaneFromComposite(lib);
	//        JTabbedPane pane = createPaneFromComposite(getEntityLibrary());
        s.addShade("Entity Library", null, pane, "The Default entity library");
    }
    
    public JTabbedPane createPaneFromComposite(CompositeEntity library) {
        Enumeration enum;
        JTabbedPane pane = new JTabbedPane();
        enum = library.getEntities();
        while(enum.hasMoreElements()) {
            Entity entity = 
                (Entity) enum.nextElement();
            if(entity instanceof CompositeEntity) {
                pane.addTab(entity.getName(), 
                        createPaneFromComposite((CompositeEntity)entity));
            }
        }

        enum = library.getEntities();
        if(enum.hasMoreElements()) {
            SchematicPalette palette = new SchematicPalette();
            int i = 0;
            while(enum.hasMoreElements()) {
                Entity entity = 
                    (Entity) enum.nextElement();
                if(!(entity instanceof CompositeEntity)) {
                    palette.addEntity(entity, 
                        60, 50 + (i++) * 50);     
                }       
            }
            if(i > 0)
                pane.addTab("entities", palette);
        }
        return pane;
    }
    
    /** Initialize the given menubar. Currently, all strings are
     * hard-wired, but maybe we should be getting them out of the
     * ApplicationResources.
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
                Document d = getCurrentDocument();
                if (d == null) {
                    return;
                } 
                try {
                    /*
                    Schematic schematic = 
                        (Schematic) d.getCurrentSheet().getModel();
                    // FIXME set the Director.  This is a hack, but it's the 
                    // Simplest hack.
                    //   SchematicDirector director = 
			//_entityLibrary.findDirector(
                    //       (String)_directorComboBox.getSelectedItem());
		    //schematic.setDirector(director);
                    
                    PtolemyModelFactory factory = new PtolemyModelFactory();
                    TypedCompositeActor system = 
                        factory.createPtolemyModel(schematic);
                    Manager manager = system.getManager();
		    // manager.addDebugListener(new StreamListener());
                    manager.startRun();
                    */
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
     * from the ApplicationResources object and added to the
     * actions. Note that the image icons are not added to the actions
     * -- if we did that, the icons would appear in the menus, which I
     * suppose is a neat trick but completely useless.
     */
    public void initializeToolBar (JToolBar tb) {
        Action action;
        ApplicationResources resources = getApplicationResources();

        // Conventional new/open/save buttons
        action = getAction("New");
        addToolBarButton(tb, action, null, resources.getImageIcon("New"));

        action = getAction("Open");
        addToolBarButton(tb, action, null, resources.getImageIcon("Open"));

        action = getAction("Save");
        addToolBarButton(tb, action, null, resources.getImageIcon("Save"));

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
        _directorComboBox.addItem("de.director");
        _directorComboBox.setSelectedItem(dflt);
        _directorComboBox.setMaximumSize(_directorComboBox.getMinimumSize());
        _directorComboBox.addItemListener(new ItemListener() {
            public void itemStateChanged (ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    setDirectorOfCurrentDocument((String)e.getItem());
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
		"ptII/ptolemy/schematic/lib/rootIconLibrary.ptml");
				 
            //            entitylibURL = new URL(urlbase, 
            //		"ptII/ptolemy/schematic/lib/rootEntityLibrary.ptml");
            entitylibURL = new URL(urlbase, 
		"ptII/ptolemy/actor/lib/genericentities.xml");
				   
            _iconLibrary = IconLibraryFactory.parseIconLibrary(iconlibURL);

                        MoMLParser parser = new MoMLParser();
            _entityLibrary =
                parser.parse(entitylibURL, entitylibURL.openStream());

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

    /** Set the given document to be the current document, and raise
     * the internal window that corresponds to that component.
     */
    public void removeDocument (Document d) {
        super.removeDocument(d);
        JComponent pane = (JComponent) _contentPanes.get(d);
        _contentPanes.remove(d);
        _documents.remove(pane);

        //FIXME do this last, to avoid circular loop
        //      with the viewClosing callback
        _applicationFrame.removeContentPane(pane);
    }
    
    /** Set the given document to be the current document, and raise
     * the internal window that corresponds to that component.
     */
    public void setCurrentDocument (Document d) {
        super.setCurrentDocument(d);
        if(d != null) {
            JComponent pane = (JComponent) _contentPanes.get(d);
            _applicationFrame.setCurrentContentPane(pane);
        }
        //        setDirectorOfCurrentDocument((String) _directorComboBox.getSelectedItem());
    }
    
    /** Set the director of the current document to the director in
     * the entity library with the given name
     */
    public void setDirectorOfCurrentDocument(String name) {
        GraphDocument d = (GraphDocument) getCurrentDocument();
        //        if(d == null) return;
        //Schematic schematic = 
        //   (Schematic) d.getCurrentSheet().getModel();
        //SchematicDirector director = 
        //    _entityLibrary.findDirector(name);
        //schematic.setDirector(director);
    }

    /** Show an error that occurred in this class.
     */
    private void showError(String op, Exception e) {
        // Show the stack trace in a scrollable text area.
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        JTextArea text = new JTextArea(sw.toString(), 8, 60);
        JScrollPane stext = new JScrollPane(text);

        // We want to stack the text area with another message
        Object[] message = new Object[2];
        message[0] = "Error in GraphEditor: " + op + " failed.\n"
            + "Please submit a bug report.";
        message[1] = stext;

        // Show the MODAL dialog
        JOptionPane.showMessageDialog(
                getApplicationFrame(),
                message,
                "We can't program for nuts",
                JOptionPane.WARNING_MESSAGE);
    }
    IconLibrary _iconLibrary;
    CompositeEntity _entityLibrary;
}

