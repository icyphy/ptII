/*
 * $Id$
 *
 * Copyright (c) 1998 The Regents of the University of California.
 * All rights reserved.  See the file COPYRIGHT for details.
 */
package ptolemy.schematic.editor;

import ptolemy.schematic.util.*;
import diva.graph.*;
import diva.graph.editor.*;
import diva.graph.JGraph;
import diva.graph.GraphController;
import diva.graph.BasicLayoutTarget;

import diva.graph.layout.GlobalLayout;
import diva.graph.layout.LevelLayout;
import diva.graph.layout.RandomLayout;
import diva.graph.layout.LayoutTarget;
import diva.graph.model.Edge;
import diva.graph.model.Graph;
import diva.graph.model.GraphModel;
import diva.graph.model.GraphUtilities;
import diva.graph.model.Node;

import diva.graph.toolbox.GraphWriter;

import diva.gui.AbstractApplication;
import diva.gui.ApplicationFrame;
import diva.gui.ApplicationResources;
import diva.gui.DefaultStoragePolicy;
import diva.gui.DesktopFrame;
import diva.gui.DefaultActions;
import diva.gui.Document;
import diva.gui.DocumentFactory;
import diva.gui.GUIUtilities;
import diva.gui.StoragePolicy;
import diva.gui.ViewAdapter;
import diva.gui.ViewEvent;
import diva.gui.toolbox.JShadePane;
import diva.gui.toolbox.JPalette;

import java.awt.Image;

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.DataOutputStream;

import java.util.HashMap;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;

import javax.swing.filechooser.FileFilter;

/**
 * A graph editor for non-hierarchical graphs. This is a complete
 * graph-editing application that includes automatic layout, load from
 * and save to XML, editable properties, and so on. It is intended as
 * an example application that you can use as a basis for building a
 * customized graph editor for your own application.
 *
 * @author John Reekie (johnr@eecs.berkeley.edu)
 * @version $Revision$
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
              return GUIUtilities.getFileExtension(file).toLowerCase().equals(
                      "xml");
          }
          public String getDescription () {
              return "XML files";
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
				       new SchematicGraphImpl());
	JGraph jgraph = new JGraph(pane);
	new GraphDropTarget(jgraph);
        GraphController controller = jgraph.getGraphPane().getGraphController();
        GraphModel model = (GraphModel) d.getSheet(0).getModel();
        Graph graph = model.getGraph();

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
    
    /** Get the storage policy of this application.
     */
    public StoragePolicy getStoragePolicy () {
        return _storagePolicy;
    }

    /** Get the title of this application
     */
    public String getTitle() {
        return "Diva graph editor";
    }

    /** Initialize the palette in the.
     */
    public void initializePalette () {
        JShadePane s =_applicationFrame.getShadePane();
        
        ApplicationResources resources = getApplicationResources();
        Icon newIcon = resources.getImageIcon("New");
        Icon openIcon = resources.getImageIcon("Open");
        Icon saveIcon = resources.getImageIcon("Save");
        
        JPalette p1 = new JPalette();
        p1.removeAll();
        p1.addIcon(newIcon, "foo");
        p1.addIcon(openIcon, "bar");
        p1.addIcon(saveIcon, "baz");
        s.addShade("Test1", newIcon, p1, "new group -- cool icons!");
        
        JPalette p2 = new JPalette();
        SchematicPalette p3 = new SchematicPalette();
       	SchematicTerminal n = new SchematicTerminal("test1", null);

	p3.addNode(n, 30, 30);

        s.addShade("Test2", openIcon, p2,"open group -- disabled!");

        s.addShade("Test3", saveIcon, p3,"save group -- boring...");
        s.setEnabledAt(1, false);
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
    }

    /** Create and run a new graph application
     */
    public static void main (String argv[]) {
        GraphEditor ge = new GraphEditor();
        ge.setVisible(true);
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
}

