/* A user interface application for component-based design.

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

package ptolemy.vergil;

// FIXME: Trim this import list.
import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.gui.MessageHandler;
import ptolemy.moml.MoMLParser;
import ptolemy.vergil.toolbox.*;
import ptolemy.vergil.tree.LibraryTreeModel;

import diva.graph.*;
import diva.gui.*;
import diva.gui.toolbox.*;
import diva.resource.DefaultBundle;
import diva.resource.RelativeBundle;

import java.awt.Event;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.awt.print.PrinterException;
import java.awt.print.PageFormat;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.DataOutputStream;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.event.*;

//////////////////////////////////////////////////////////////////////////
//// VergilApplication
/**
Vergil is an extensible high-level graphical interface for component-based
design tools.  It is primarily aimed at Ptolemy II, although it could
be used with other tools as well.  The features of Vergil include:
<ul>
<li> Support for multiple types of documents.
<li> A module system to allow for easy extension.
<li> Expandable tool bars and menu bars.
</ul>
<p>
This class is associated with a desktop frame.  This frame contains a palette
into which packages can place design libraries.  The frame also inherits
improved support for multiple documents, a toolbar, status bar and
progress bar from the Diva desktop frame.
<p>
Only a singleton instance of this class ever exists, which is created by 
the static main method.

@author Steve Neuendorffer
@contributor John Reekie
@version $Id$
*/
public class VergilApplication extends MDIApplication {
    /**
     * Construct a new Vergil application.  Create a new desktop frame and
     * initialize it's menu bar and toolbar.  Load all the modular packages
     * for this application and open a starting document using the default
     * document factory.
     */
    protected VergilApplication(DesktopContext context) {
        super(context);

	JPanel palettePane = new JPanel();
	palettePane.setBorder(null);
        palettePane.setLayout(new BoxLayout(palettePane, BoxLayout.Y_AXIS));
	    
	// Initialize the context.
	context.setPalettePane(palettePane);

	// Messages from the message handler get centered in the 
	// toplevel frame.
	MessageHandler.setContext(context.makeComponent());

	// Use the system clipboard, if possible.
	Clipboard clipboard;
	try {
	    clipboard = 
		java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
	} catch (SecurityException ex) {
	    clipboard = new Clipboard(getTitle());
	}
	setClipboard(clipboard);
	
	// Handle exceptions thrown by awt events in a nice way.
        // No, this is not such a nice way.  It's using undocumented
        // features of Java, and features that will fail for applets.
        // We need to be systematically handling errors using
        // the MessageHandler class.  EAL
	// Actually it is using the documented unsupported way of doing this,
	// which is really the only way to catch all exceptions that happen
	// in the swing thread, since we don't create it.  SAN
	// ApplicationExceptionHandler.setApplication(this);

        // Create and initialize the storage policy
        try {
	    DefaultStoragePolicy storage = new DefaultStoragePolicy();
	    setStoragePolicy(storage);
	    FileFilter filter = new FileFilter() {
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
	    JFileChooser chooser;
	    chooser = storage.getOpenFileChooser();
	    chooser.addChoosableFileFilter(filter);
	   	    
	    chooser = storage.getSaveFileChooser();
	    chooser.addChoosableFileFilter(filter);
	} catch (SecurityException ex) {
	    // FIXME: create a new "NoStoragePolicy"
	    System.out.println(ex.getMessage());
	}

        // Initialize the menubar, toolbar, and palettes
        _initializeActions(context.getJMenuBar(), context.getJToolBar());
	JPanel toolBarPane = context.getToolBarPane();
	toolBarPane.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));

	Icon icon = getVergilResources().getImageIcon("frameImage");
	Image iconImage = getVergilResources().getImage("frameImage");

        context.setFrameIcon(icon);
	context.setIconImage(iconImage);
	context.setTitle(getTitle());

        setCurrentView(null);

	// Generic services.
	classReloadingService = new ClassReloadingService(this);
	addService(classReloadingService);
	
	// Instantiate the modules.
	// FIXME read this out of resources somehow.	
	new ptolemy.vergil.ptolemy.PtolemyModule(this);
   	
	final JPanner panner = new JPanner();
	panner.setPreferredSize(new Dimension(200, 150));
	panner.setMaximumSize(new Dimension(200, 150));
	panner.setSize(200, 150);
	panner.setBorder(BorderFactory.createEtchedBorder());
	// FIXME the below view listener never gets notified when no
	// views are selected.
	context.addViewListener(new ViewAdapter() {
	    public void viewSelected(ViewEvent e) {
		JComponent view = e.getView();
		if(view instanceof JScrollPane) {
		    panner.setViewport(((JScrollPane)view).getViewport());
		} else {
		    panner.setViewport(null);
		}
	    }
	});
	/*
	addDocumentListener(new ListDataListener() {
	    public void contentsChanged(ListDataEvent e) {
		updatePanner();
	    }
	    public void intervalAdded(ListDataEvent e) {
		updatePanner();
	    }
	    public void intervalRemoved(ListDataEvent e) {
		updatePanner();
	    }
	    public void updatePanner() {
		JComponent view = getView(getCurrentDocument());
		if(view instanceof JScrollPane) {
		    panner.setViewport(((JScrollPane)view).getViewport());
		} else {
		    panner.setViewport(null);
		}
	    }
	    });*/
	palettePane.add(panner);
	
	context.setVisible(true);
	
	// Start with a new document.
	// This is kindof
	// bogus, but it is not easy to fire the action manually.
	Action action = getAction(DefaultActions.NEW);
	// FIXME this is really a horrible horrible hack.
	javax.swing.Timer timer = new javax.swing.Timer(500, action);
	timer.setRepeats(false);
	timer.start();
    }

    public ClassReloadingService classReloadingService;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    public void addDocument(Document d) {
	super.addDocument(d);
	// Update the title of the frame when the title of the document
	// changes.
	d.addPropertyChangeListener(_titleChanger);
    }
	
    /**
     * Add the factory that creates new documents.  Also create a new
     * action and add it to the
     * File->New menu that will create documents with the given factory.
     */
    public void addDocumentFactory(VergilDocumentFactory factory) {
	final VergilDocumentFactory f = factory;
	final VergilApplication app = this;
        _documentFactoryList.add(factory);
	Action action = new AbstractAction (f.getName()) {
            public void actionPerformed(ActionEvent e) {
                Document doc = f.createDocument(app);
                addDocument(doc);
                View v = app.createView(doc);
                app.addView(v);
                app.setCurrentView(v);
            }
        };
        // NOTE: The first character of the action name is used as a mnemonic,
        // so some care is needed in choosing the name to maintain
        // a distinction.
        // FIXME: Need tooltips (replace null argument below).
        GUIUtilities.addMenuItem(
               _fileNewMenu, action, (int)(f.getName().charAt(0)), null);
    }

    /**
     * Add the menu to the menu bar of this application.
     */
    public void addMenu(JMenu menu) {
	AppContext context = getAppContext();
	if(context == null) return;
	JMenuBar menuBar = context.getJMenuBar();
	menuBar.add(menu);
    }

    /**
     * Add the service to the list of services that are published in
     * this application.
     */
    public void addService(Service service) {
	_serviceList.add(service);
    }

    /** 
     * Given a Diva document, create a new view that displays that
     * document. If the document is an instance of VergilDocument
     * then defer to the document to create the view.
     * @exception RuntimeException If the document is not an instance of 
     * VergilDocument.
     * @see #VergilDocument
     */
    public View createView(Document document) {
	if(!(document instanceof VergilDocument)) {
	    throw new RuntimeException("Can only create views " +
				       "on VergilDocuments.");
	}
	return ((VergilDocument)document).createDefaultView();
    }
           
    /** 
     * Return the list of factories that create new documents.
     * @return An unmodifiable list of instances of DocumentFactory.
     */
    public List documentFactoryList() {
        return Collections.unmodifiableList(_documentFactoryList);
    }

    /**
     * Return the DesktopContext that this application is using.
     */
    public DesktopContext getDesktopContext() {
	return (DesktopContext) getAppContext();
    }

    /** 
     * Return the default document factory.  If there is no default factory, 
     * then return null.  
     * @return The first factory added with the addDocumentFactory method.
     */
    public DocumentFactory getDocumentFactory() { 
        if(_documentFactoryList.size() < 1) 
            return null; 
        else 
	    return (DocumentFactory)_documentFactoryList.get(0);
    }

    /** 
     * Return the instance of this class that makes up the
     * application.
     */
    public static VergilApplication getInstance() {
	return _instance;
    }

    /** Get the resources object that is specific to this application.
     */
    public RelativeBundle getVergilResources () {
        return _vergilResources;
    }    

    /** 
     * Get the title of this application.  This class returns
     * the string "Vergil", although subclasses may override this.
     */
    public String getTitle() {
        return "Vergil";
    }

    /** 
     * Create a new instance of VergilApplication and make it visible.  
     * The application object is responsible for creating the persistent user
     * interface which will remain after this method returns.
     */
    public static void main(String argv[]) {
	_instance = new VergilApplication(new DesktopContext(new ApplicationContext()));
    }

   /** 
    * Redisplay a document after it appears on the screen. This method is 
    * called when a document is set to be the current document.  It is 
    * intended to allow an application to perform some action at this point,
    * such as executing a graph layout algorithm.  In this class, do nothing.
    */
    public void redisplay(Document document, JComponent view) {       
    }

    /** 
     * Remove the given factory that creates new documents from
     * this application.  Remove its entry in the File->New menu.
     */
    public void removeDocumentFactory(VergilDocumentFactory factory) {
	int index = _documentFactoryList.indexOf(factory);
        _documentFactoryList.remove(factory);
	_fileNewMenu.remove(_fileNewMenu.getItem(index));
    }

    /**
     * Remove the given menu from the menu bar of this application.
     */
    public void removeMenu(JMenu menu) {
	AppContext context = getAppContext();
	if(context == null) return;
	JMenuBar menuBar = context.getJMenuBar();
	menuBar.remove(menu);
    }

    /**
     * Remove the service to the list of services that are published in
     * this application.
     */
    public void removeService(Service service) {
	_serviceList.remove(service);
    }

    /** 
     * Return the list of services that are published in this application.
     * @return An unmodifiable list of instances of Service.
     */
    public List serviceList () {
        return Collections.unmodifiableList(_serviceList);
    }

    /** 
     * Return the list of services that are published in this application that
     * are instances of the specified class.
     * @return An unmodifiable list of instances of the given Service.
     */
    public List serviceList (Class filter) {
	List result = new LinkedList();
	Iterator services = _serviceList.iterator();
	while (services.hasNext()) {
	    Service service = (Service) services.next();
	    if (filter.isInstance(service)) {
		result.add(service);
	    }
	}
	return Collections.unmodifiableList(result);
    }

    /** 
     * Set the given document to be the current document, and raise
     * the internal window that corresponds to that document and give it
     * the keyboard focus.
     * If given document is not null, 
     * then ensure that the "Save" and "Save As"
     * actions are enabled.  If the given document is null, then disable
     * those actions.
     * @param document The document to set as the current document, or
     * null to set that there is no current document.
     */
    public void setCurrentView(View view) {
        super.setCurrentView(view);

        if(view == null) {
            Action saveAction = getAction(DefaultActions.SAVE);
            saveAction.setEnabled(false);
            Action saveAsAction = getAction(DefaultActions.SAVE_AS);
            saveAsAction.setEnabled(false);
        } else {
	    JComponent component = view.getComponent();
	    if (!component.hasFocus()) {
		component.requestFocus();
	    }
            Action saveAction = getAction(DefaultActions.SAVE);
            saveAction.setEnabled(true);
            Action saveAsAction = getAction(DefaultActions.SAVE_AS);
            saveAsAction.setEnabled(true);
        }
    }

    /** 
     * Throw an Exception.  Vergil uses a factory list instead of a 
     * single factory.  
     * @deprecated Use addDocumentFactory to add a document factory.
     */
    public void setDocumentFactory(DocumentFactory factory) {
	throw new RuntimeException("setDocumentFactory is not allowed, use " + 
				   "addDocumentFactory instead.");
    }

    /** Show the error without the stack trace by default.
     *  @deprecated Use MessageHandler.error() instead.
     */
    public void showError(String op, Exception e) {
        MessageHandler.error(op, e);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** 
     * Initialize the given menubar. Create a new File menu with 
     * "New", "Open", "Import Library", "Close", "Close Library", "Save",
     * "Save As", and "Exit" items.
     * The "New" item is a submenu that contains one item for each
     * document factory contained in this application.  The other items
     * are actions that defer to the current document for their
     * functionality.
     */
    private void _initializeActions(JMenuBar menuBar, JToolBar toolBar) {
        Action action;
        JMenuItem item;
        RelativeBundle resources = getResources();

	// FIXME pull the strings out of resources instead of hardcoding.
	
	// Create the File menu
        JMenu menuFile = new JMenu("File");
        menuFile.setMnemonic('F');
        menuBar.add(menuFile);
	
	// File->New and associated toolbar button.
        _fileNewMenu = new JMenu("New");
        _fileNewMenu.updateUI();
        _fileNewMenu.setMnemonic('N');
	menuFile.add(_fileNewMenu);
	
	action = DefaultActions.newAction(this);
        addAction(action);
	GUIUtilities.addToolBarButton(toolBar, action, "Open a new model", 
			 resources.getImageIcon("NewImage"));

	// File->Open and associated toolbar button.
        action = DefaultActions.openAction(this);
        addAction(action);
	GUIUtilities.addHotKey(getAppContext().getRootPane(), action, 
 	    KeyStroke.getKeyStroke(KeyEvent.VK_O, Event.CTRL_MASK));	
        GUIUtilities.addMenuItem(menuFile, action, 'O', "Open a model");
        GUIUtilities.addToolBarButton(toolBar, action, null, 
				      resources.getImageIcon("OpenImage"));

        // File->Import Library.
        action = new AbstractAction("Import Library") {
            public void actionPerformed(ActionEvent e) {
                // NOTE: Regrettably, the StoragePolicy interface does
                // not give access to the directory and file chooser,
                // so we have to cast.  This will create problems if
                // we later change the storage policy without extending
                // DefaultStoragePolicy.
                DefaultStoragePolicy policy
                       = (DefaultStoragePolicy)getStoragePolicy();
                String directory = policy.getDirectory();
                JFileChooser chooser = policy.getOpenFileChooser();
                chooser.setCurrentDirectory(new File(directory));
                int result = chooser.showOpenDialog(
                        getAppContext().makeComponent());
                if (result == JFileChooser.APPROVE_OPTION) {
                    try {
                        File file = chooser.getSelectedFile();
                        MoMLParser parser = new MoMLParser();
                        CompositeEntity toplevel = (CompositeEntity)
                                parser.parse(
                                    chooser.getCurrentDirectory().toURL(),
                                    new FileReader(file));
                        LibraryTreeModel.addLibrary(toplevel);
                    } catch (Exception ex) {
                        MessageHandler.error("Library import failed.", ex);
                    }
                }
            }
        };
        GUIUtilities.addMenuItem(menuFile, action, 'L',
               "Import a library and add to the tree");

	// File->Close
        action = DefaultActions.closeAction(this);
        addAction(action);
        GUIUtilities.addMenuItem(menuFile, action, 'C',
                "Close the current model");

        menuFile.addSeparator();

	// File->Save
        action = DefaultActions.saveAction(this);
        addAction(action);
	GUIUtilities.addHotKey(getAppContext().getRootPane(), action, 
 	    KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK));	
        GUIUtilities.addMenuItem(menuFile, action, 'S', "Save the current model");
        GUIUtilities.addToolBarButton(toolBar, action, null, 
			 resources.getImageIcon("SaveImage"));
	
	// File->SaveAs
        action = DefaultActions.saveAsAction(this);
        addAction(action);
        GUIUtilities.addMenuItem(menuFile, action, 'A',
                "Save the current model to a different file");

	// File->Print
        action = DefaultActions.printAction(this);
        addAction(action);
	GUIUtilities.addHotKey(getAppContext().getRootPane(), action, 
 	    KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.CTRL_MASK));	
        GUIUtilities.addMenuItem(menuFile, action, 'P', "Print current document");

        menuFile.addSeparator();

	// File->Exit
        action = DefaultActions.exitAction(this);
        addAction(action);
        GUIUtilities.addMenuItem(menuFile, action, 'X', "Exit from the graph editor");
	// Set the exit action of the context.
	getAppContext().setExitAction(action);

        // Create the File menu
        menuFile = new JMenu("Edit");
        menuFile.setMnemonic('E');
        menuBar.add(menuFile);

	// FIXME implement cut.
	//action = DefaultActions.cutAction(this);
        //addAction(action);
        //GUIUtilities.addMenuItem(menuFile, action, 'u', "Cut");

	// Edit->Copy
        action = DefaultActions.copyAction(this);
        addAction(action);
	GUIUtilities.addHotKey(getAppContext().getRootPane(), action, 
 	    KeyStroke.getKeyStroke(KeyEvent.VK_C, Event.CTRL_MASK));	
        GUIUtilities.addMenuItem(menuFile, action, 'C', "Copy");

	// Edit->Paste
	action = DefaultActions.pasteAction(this);
        addAction(action);
	GUIUtilities.addHotKey(getAppContext().getRootPane(), action, 
 	    KeyStroke.getKeyStroke(KeyEvent.VK_V, Event.CTRL_MASK));	
        GUIUtilities.addMenuItem(menuFile, action, 'P', "Paste");
    }

    /** 
     * A property change listener for documents.  It is responsible for
     * keeping the title of the internal frame for a document the same
     * as the title of the document.
     */
    public class TitleChanger implements PropertyChangeListener {
        /** 
         * When the file or url properties of the document change, 
         * assume that the title of the document has changed and
         * reset the title of the appropriate internal frame.
         */
        public void propertyChange(PropertyChangeEvent e) {
            String name = e.getPropertyName();
            if (name.equals("file") || 
		    name.equals("url")) {
                // the title has changed.
		Document d = (Document)e.getSource();
		Iterator i = viewList(d).iterator();
		while(i.hasNext()) {
		    View v = (View)i.next();
		    JInternalFrame frame = 
		      getDesktopContext().getInternalFrame(v.getComponent());
		    frame.setTitle(d.getTitle());
		}
            }
        }
    }
    
    /** 
     * This service is capable of managing the dynamic loading and
     * reloading of classes.  This service is associated with a dynamic
     * class path that is specified in the resource "dynamicClassPath".
     * Any classes in that class path should be loaded only through the 
     * class loader that this service returns.  When the class loader is
     * reset, a new class loader is created that will load those classes
     * freshly when they are used through that class loader.  So, to allow
     * a class to be dynamically reloaded, simply place it within the
     * dynamic class path, at its appropriate place.   For instance, if
     * the dynamic class path property is set to "/dynamic/" and you 
     * wish to allow everything in actor/lib to be reloaded, then move
     * the ptII/ptolemy/actor/lib director to ptII/dynamic/ptolemy/actor/lib.
     * <p> Note that 
     * there are several limitations to this.  First of all, classes that
     * were loaded previously still exist within the JVM.  The old Class
     * objects and instances of the old classes are still present (until
     * they are garbage collected), but are distinct from the new versions.
     * This is true even if the class itself has not changed.  Existing
     * objects are NOT automatically replaced with instances of the new
     * class.   Users of this service are responsible for reinstantiating 
     * any classes that my be dynamically reloaded.
     */
    public class ClassReloadingService extends AbstractService {
	// FIXME notification when reloading happens?
	/** 
	 * Create a new service that maintains a class loader for
	 * reloading classes.
	 */
	public ClassReloadingService(VergilApplication application) {
	    _setApplication(application);
	    resetClassLoader();
	}
	
	/**
	 * Return the class loader.
	 */
	public ClassLoader getClassLoader() {
	    return _classLoader;
	}
	
	/**
	 * Reset the class loader.  When the class loader is asked to load a 
	 * class, the class will be loaded from the system again and instances
	 * of the new classes will have no correspondence to any previously 
	 * instantiated classes.
	 */
	public void resetClassLoader() {
	    try {
		String classpath = 
		    getVergilResources().getString("dynamicClassPath");
		String separator = ":"; //System.getProperty("path.separator");
		StringTokenizer paths = new StringTokenizer(classpath, separator);
		URL urls[] = new URL[paths.countTokens()];
		int count = 0;
		while(paths.hasMoreTokens()) {
		    String path = paths.nextToken();
		    urls[count] = getClass().getResource(path);
		    count++;
		}
		_classLoader =
		    new URLClassLoader(urls, getClass().getClassLoader());
	    } catch (Exception ex) {
		_classLoader = getClass().getClassLoader();
	    }
	}
	
	// The class loader that this service uses.
	private ClassLoader _classLoader; 
    }

    ///////////////////////////////////////////////////////////////////
    ////                         package variables                 ////

    /**
     * Set the vergil instance.  I really don't like to do this. 
     */
    static void setInstance(VergilApplication application) {
	_instance = application;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The director selection combobox.
    private JComboBox _directorComboBox;

    // The layout selection combobox.
    private JComboBox _layoutComboBox;

    // The list of factories that create models.
    private List _documentFactoryList = new LinkedList();

    // The File->New menu.  Each document factory will appear in this menu.
    private JMenu _fileNewMenu = null;

    // The instance of this application.
    private static VergilApplication _instance = null;

    // The list of factories that create models.
    private List _serviceList = new LinkedList();

    // The title changer used by this application.  This listener will
    // be attached to all documents.
    private TitleChanger _titleChanger = new TitleChanger();

    // The resources object.
    private RelativeBundle _vergilResources = 
	new RelativeBundle("ptolemy.vergil.Vergil", 
			   getClass(), 
			   null);
}
















