/*
 * $Id$
 *
@Copyright (c) 1998-2004 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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


 */
package diva.gui.tutorial;

import java.awt.FlowLayout;
import java.awt.Image;
import java.io.File;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileFilter;

import diva.gui.AppContext;
import diva.gui.ApplicationContext;
import diva.gui.DefaultActions;
import diva.gui.DefaultStoragePolicy;
import diva.gui.DesktopContext;
import diva.gui.Document;
import diva.gui.GUIUtilities;
import diva.gui.View;
import diva.resource.RelativeBundle;

/**
 * A simple MDI text editor.  This application uses the MDIApplication base
 * class to provide a separate internal frame associated with each document.
 * This class is similar in many ways to the ApplicationTutorial.  The
 * code in the initializeApp(), initializeMenuBar(), and initializeToolBar()
 * methods is the same in both.
 * <p>
 * Note that there appears to be a bug in jdk1.2 with the keyboard handling
 * in JEditorPane.  Maximizing one of the internal frames loses the event
 * hooks that the JEditorPane uses for keyboard input.  This is fixed in
 * jdk1.3.
 *
 * @author Steve Neuendorffer (neuendor@eecs.berkeley.edu)
 * @version $Revision$
 */
public class MDIApplicationTutorial extends MDIApplication {

    /** The listener for changing titles.  This listener will be attached
     *  to each document to reset the titles on the appropriate internal
     *  frame.
     */
    //FIXME    private PropertyChangeListener _titleChanger = new TitleChanger();

    /**
     * Construct a new instance of the Tutorial, running in a new
     * application context.
     */
    public static void main(String argv[]) {
        AppContext context = new ApplicationContext();
        // This is an MDI application that uses a desktop context.
        new MDIApplicationTutorial(new DesktopContext(context));
    }

    public MDIApplicationTutorial(DesktopContext context) {
        super(context);

        initializeApp();

        // Initialize the context.  DesktopContext already has a toolbar
        // and a menu bar.
        initializeMenuBar(context.getJMenuBar());
        JPanel toolBarPane = context.getToolBarPane();
        toolBarPane.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));
        initializeToolBar(context.getJToolBar());

        // Set the icon in the upper left corner of the context.
        Image iconImage = getResources().getImage("GraphIconImage");
        context.setIconImage(iconImage);
        // Set the icon for the internal frames of the desktop context.
        Icon icon = getResources().getImageIcon("GraphIconImage");
        context.setFrameIcon(icon);

        // Set the size of the context.
        context.makeComponent().setSize(800, 600);
        // and make it visible.
        context.setVisible(true);
    }

    /** Create a view on the given document.
     */
    public View createView (Document d) {
        return new TextView((TextDocument)d);
    }

    /** Get the title of this application
     */
    public String getTitle() {
        return "MDI Application Tutorial";
    }

    /** Initialize the application.
     */
    public void initializeApp() {
        // Create and initialize the storage policy
        try {
            DefaultStoragePolicy storage = new DefaultStoragePolicy();
            setStoragePolicy(storage);
            FileFilter ff = new FileFilter() {
                    public boolean accept (File file) {
                        return GUIUtilities.getFileExtension(file).
                            toLowerCase().equals("txt");
                    }
                    public String getDescription () {
                        return "Text files";
                    }
                };
            JFileChooser fc;
            fc = storage.getOpenFileChooser();
            fc.addChoosableFileFilter(ff);
            fc.setFileFilter(ff);

            fc = storage.getSaveFileChooser();
            fc.addChoosableFileFilter(ff);
            fc.setFileFilter(ff);
        } catch (SecurityException ex) {
            // FIXME: create a new "NoStoragePolicy"
        }

        setDocumentFactory(new TextDocument.Factory());
    }

    /** Initialize the menu bar
     */
    public void initializeMenuBar(JMenuBar menuBar) {
        Action action;
        JMenuItem item;

        // Create the File menu
        JMenu menuFile = new JMenu("File");
        menuFile.setMnemonic('F');
        menuBar.add(menuFile);

        action = DefaultActions.newAction(this);
        addAction(action);
        GUIUtilities.addMenuItem(menuFile, action, 'N',
                "Create a new graph document");

        action = DefaultActions.openAction(this);
        addAction(action);
        GUIUtilities.addMenuItem(menuFile, action, 'O',
                "Open a graph document");

        action = DefaultActions.closeAction(this);
        addAction(action);
        GUIUtilities.addMenuItem(menuFile, action, 'C',
                "Close the current graph document");

        menuFile.addSeparator();

        action = DefaultActions.saveAction(this);
        addAction(action);
        GUIUtilities.addMenuItem(menuFile, action, 'S',
                "Save the current graph document");

        action = DefaultActions.saveAsAction(this);
        addAction(action);
        GUIUtilities.addMenuItem(menuFile, action, 'A',
                "Save the current graph document to a different file");

        menuFile.addSeparator();

        action = DefaultActions.exitAction(this);
        addAction(action);
        GUIUtilities.addMenuItem(menuFile, action, 'X',
                "Exit from the graph editor");
        // Hook the exit action into the frame's close button, if we are
        // running in an ApplicationContext.
        getAppContext().setExitAction(action);
    }

    /** Initialize the given toolbar. Image icons will be obtained
     * from the ApplicationResources object and added to the
     * actions. Note that the image icons are not added to the actions
     * -- if we did that, the icons would appear in the menus, which I
     * suppose is a neat trick but completely useless.
     */
    public void initializeToolBar (JToolBar tb) {
        Action action;
        RelativeBundle resources = getResources();

        // Conventional new/open/save buttons
        action = getAction("New");
        GUIUtilities.addToolBarButton(tb, action, null,
                resources.getImageIcon("NewImage"));

        action = getAction("Open");
        GUIUtilities.addToolBarButton(tb, action, null,
                resources.getImageIcon("OpenImage"));

        action = getAction("Save");
        GUIUtilities.addToolBarButton(tb, action, null,
                resources.getImageIcon("SaveImage"));
    }

    /**
     * FIXME
     *
     * A property change listener for documents.  It is responsible for
     * keeping the title of the internal frame for a document the same
     * as the title of the document.
     *
     public class TitleChanger implements PropertyChangeListener {
     /**
      * When the file or url properties of the document change,
      * assume that the title of the document has changed and
      * reset the title of the appropriate internal frame.
      *
      public void propertyChange(PropertyChangeEvent e) {
      String name = e.getPropertyName();
      if (name.equals("file") ||
      name.equals("url") ||
      name.equals("title")) {
      // the title has changed.
      Document doc = (Document)e.getSource();
      DesktopContext context = (DesktopContext)getAppContext();
      JInternalFrame frame = context.getInternalFrame(getView(doc));
      frame.setTitle(doc.getTitle());
      }
      }
      }*/
}




