/* Top-level window for Ptolemy models with a menubar and status bar.

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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (eal@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import ptolemy.actor.CompositeActor;
import ptolemy.gui.CancelException;
import ptolemy.gui.MessageHandler;
import ptolemy.gui.Top;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

//////////////////////////////////////////////////////////////////////////
//// TableauFrame
/**
This is a top-level window associated with a tableau that has
a menubar and status bar. Derived classes should add components
to the content pane using a line like:
<pre>
    getContentPane().add(component, BorderLayout.CENTER);
</pre>

@author Edward A. Lee
@version $Id$
*/
public abstract class TableauFrame extends Top {

    /** Construct an empty top-level frame.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  It may also be desirable to call centerOnScreen().
     */
    public TableauFrame() {
        super();
    }

    /** Construct an empty top-level frame managed by the specified
     *  tableau. After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  It may also be desirable to call centerOnScreen().
     */
    public TableauFrame(Tableau tableau) {
        super();
        setTableau(tableau);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the configuration at the top level of the hierarchy.
     *  @return The configuration controlling this frame, or null
     *   if there isn't one.
     */
    public Configuration getConfiguration() {
        NamedObj tableau = getTableau();
        if (tableau != null) {
            NamedObj toplevel = tableau.toplevel();
            if (toplevel instanceof Configuration) {
                return (Configuration)toplevel;
            }
        }
        return null;
    }

    /** Get the model directory in the top level configuration.
     *  @return The model directory, or null if there isn't one.
     */
    public ModelDirectory getDirectory() {
        Configuration configuration = getConfiguration();
        if (configuration != null) {
            Entity directory = configuration.getEntity("directory");
            if (directory instanceof ModelDirectory) {
                return (ModelDirectory)directory;
            }
        }
        return null;
    }

    /** Get the effigy for the model associated with this window.
     *  @return The effigy for the model, or null if none exists.
     */
    public Effigy getEffigy() {
        if (_tableau != null) {
            return (Effigy)_tableau.getContainer();
        }
        return null;
    }

    /** Get the effigy for the specified Ptolemy model.
     *  This searches all instances of PtolemyEffigy deeply contained by
     *  the directory, and returns the first one it encounters
     *  that is an effigy for the specified model.
     *  @return The effigy for the model, or null if none exists.
     */
    public PtolemyEffigy getEffigy(NamedObj model) {
        CompositeEntity directory = getDirectory();
        return _findEffigyForModel(directory, model);
    }

    /** Get the tableau that created this frame.
     *  @return The tableau.
     */
    public Tableau getTableau() {
        return _tableau;
    }

    /** Return true if the data associated with this window has been
     *  modified since it was first read or last saved.  This returns
     *  the value set by calls to setModified(), or false if that method
     *  has not been called.
     *  @return True if the data has been modified.
     */
    public boolean isModified() {
        Effigy effigy = getEffigy();
        if (effigy != null) {
            return effigy.isModified();
        } else {
            return super.isModified();
        }
    }

    /** Record whether the data associated with this window has been
     *  modified since it was first read or last saved.  If you call
     *  this with a true argument, then subsequent attempts to close
     *  the window will trigger a dialog box to confirm the closing.
     *  This overrides the base class to delegate to the effigy.
     *  @param modified Indicator of whether the data has been modified.
     */
    public void setModified(boolean modified) {
        Effigy effigy = getEffigy();
        if (effigy != null) {
            effigy.setModified(modified);
        } else {
            super.setModified(modified);
        }
    }

    /** Set the tableau that represents this frame.
     *  @param key The key identifying the model.
     */
    public void setTableau(Tableau tableau) {
	_tableau = tableau;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add a View menu if a Tableau was given in the constructor.
     */
    protected void _addMenus() {
        super._addMenus();
        if (_tableau != null) {
	    // Check to see if we have an effigy factory.
	    Configuration configuration = (Configuration)_tableau.toplevel();
	    EffigyFactory effigyFactory = 
		(EffigyFactory)configuration.getEntity("effigyFactory");
	    if(effigyFactory != null) {
		// Enable the "New" item in the File menu.
		_fileMenuItems[1].setEnabled(true);
	    }

	    Effigy tableauContainer = (Effigy)_tableau.getContainer();
            if (tableauContainer != null) {
                _factoryContainer = tableauContainer.getTableauFactory();
                if (_factoryContainer != null) {
                    // If setTableau() has been called on the effigy,
                    // then there are multiple possible views of data
                    // represented in this top-level window.
                    // Thus, we create a View menu here.
                    JMenu viewMenu = new JMenu("View");
                    viewMenu.setMnemonic(KeyEvent.VK_V);
                    _menubar.add(viewMenu);
                    ViewMenuListener vml = new ViewMenuListener();
                    Iterator factories =
                            _factoryContainer.entityList(TableauFactory.class)
                            .iterator();
                    while (factories.hasNext()) {
                        TableauFactory factory
                                = (TableauFactory)factories.next();
                        String name = factory.getName();
                        JMenuItem item = new JMenuItem(name);
                        // The "action command" is available to the listener.
                        item.setActionCommand(name);
                        item.setMnemonic(name.charAt(0));
                        item.addActionListener(vml);
                        viewMenu.add(item);
                    }
                }
            }
        }
    }

    /** Close the window.  Derived classes should override this to
     *  release any resources or remove any listeners.  In this class,
     *  if the data associated with this window has been modified,
     *  and there are no other tableaux in the parent effigy or
     *  any effigy that contains it,
     *  then ask the user whether to save the data before closing.
     */
    protected void _close() {
        // NOTE: We use dispose() here rather than just hiding the
        // window.  This ensures that derived classes can react to
        // windowClosed events rather than overriding the
        // windowClosing behavior given here.
        if (_otherOpenTableaux(getEffigy(), 1)) {
            // There are other tableau, so just close.
            dispose();
        } else {
            super._close();
        }
    }

    /** Open a new Ptolemy II model.
     */
    protected void _new() {
        if (_tableau != null) {
	    // Check to see if we have an effigy factory.
	    Configuration configuration = (Configuration)_tableau.toplevel();
	    configuration.newModel();
	}
    }

    /** Read the specified URL.  This delegates to the ModelDirectory
     *  to ensure that the preferred tableau of the model is opened, and
     *  that a model is not opened more than once.
     *  @param url The URL to read.
     *  @exception Exception If the URL cannot be read.
     */
    protected void _read(URL url) throws Exception {
        if (_tableau == null) {
            throw new Exception("No associated Tableau!"
            + " Can't open a file.");
        }
        // NOTE: Used to use for the first argument the following, but
        // it seems to not work for relative file references:
        // new URL("file", null, _directory.getAbsolutePath()
        Configuration configuration = (Configuration)_tableau.toplevel();
        configuration.openModel(url, url, url.toExternalForm());
    }

    /** Save the model to the current file, determined by the
     *  and _file protected variable, or if that has not been set,
     *  by the <i>url</i> attribute of the associated effigy, or if
     *  that has not been set or is not a writable file, then invoke
     *  _saveAs(). This calls _writeFile() to perform the save.
     *  @return True if the save succeeds.
     */
    protected boolean _save() {
        // FIXME: If we are inside another model, this overwrites the
        // toplevel model with the lower level model.
        if (_file == null) {
            // Access the URL info from the model to attempt to set
            // the _file variable.
            Effigy effigy = getEffigy();
            if (effigy != null) {
                URL url = effigy.url.getURL();
                if (url != null) {
                    String protocol = url.getProtocol();
                    if (protocol.equals("file")) {
                        String filename = url.getFile();
                        _file = new File(filename);
                    }
                }
            }
        }
        return super._save();
    }

    /** Query the user for a filename, save the model to that file,
     *  and open a new window to view the model.
     *  This overrides the base class to update the entry in the
     *  ModelDirectory.
     *  @return True if the save succeeds.
     */
    protected boolean _saveAs() {
        JFileChooser fileDialog = new JFileChooser();
        fileDialog.setDialogTitle("Save as...");
        if (_directory != null) {
            fileDialog.setCurrentDirectory(_directory);
        } else {
            // The default on Windows is to open at user.home, which is
            // typically an absurd directory inside the O/S installation.
            // So we use the current directory instead.
            // FIXME: This will probably fail with a security exception in
            // applets.
            String cwd = System.getProperty("user.dir");
            if (cwd != null) {
                fileDialog.setCurrentDirectory(new File(cwd));
            }
        }
        int returnVal = fileDialog.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileDialog.getSelectedFile();

            _file = file;
	    _directory = fileDialog.getCurrentDirectory();
            boolean result = _save();

            if (result) {
                // Open a new window on the model.
                try {
                    URL newURL = file.toURL();
                    String newKey = newURL.toExternalForm();
                    getConfiguration().openModel(newURL, newURL, newKey);
                } catch (Exception ex) {
                    MessageHandler.error(
                            "Unable to open the file just saved.",ex);
                }
            }
            return result;
        }
        // Action was cancelled.
        return false;
    }

    /** Write the model to the specified file.  This method delegates
     *  to the top effigy containing the associated Tableau, if there
     *  is one, and otherwise throws an exception.
     *  @param file The file to write to.
     *  @exception IOException If the write fails.
     */
    protected void _writeFile(File file) throws IOException {
        Tableau tableau = getTableau();
        if (tableau != null) {
            Effigy effigy = (Effigy)tableau.getContainer();
            if (effigy != null) {
                effigy.writeFile(file);
                return;
            }
        }
        throw new IOException("Cannot find an effigy to delegate writing.");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Recursively search the specified composite for an instance of
    // PtolemyEffigy that matches the specified model.
    private PtolemyEffigy _findEffigyForModel(
            CompositeEntity composite, NamedObj model) {
        if (composite != null) {
            Iterator effigies = 
                   composite.entityList(PtolemyEffigy.class).iterator();
            while (effigies.hasNext()) {
                PtolemyEffigy effigy = (PtolemyEffigy)effigies.next();
                // First see whether this effigy matches.
                if (effigy.getModel() == model) {
                    return effigy;
                }
                // Then see whether any effigy inside this one matches.
                PtolemyEffigy inside = _findEffigyForModel(effigy, model);
                if (inside != null) {
                    return inside;
                }
            }
        }
        return null;
    }

    // Return true if the specified effigy has more than the specified
    // number of open tableau, or if any effigy that deeply contains it
    // has at least one open tableau.
    private boolean _otherOpenTableaux(Effigy effigy, int minimum) {
        if (effigy != null) {
            List tableaux = effigy.entityList(Tableau.class);
            if (tableaux.size() > minimum) {
                return true;
            } else {
                Nameable container = effigy.getContainer();
                if (container instanceof Effigy) {
                    return _otherOpenTableaux((Effigy)container, 0);
                }
            }
        }
        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The container of view factories, if one has been found.
    private TableauFactory _factoryContainer = null;

    // The tableau that created this frame.
    private Tableau _tableau = null;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Listener for view menu commands. */
    class ViewMenuListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (_factoryContainer != null) {
                JMenuItem target = (JMenuItem)e.getSource();
                String actionCommand = target.getActionCommand();
                TableauFactory factory = (TableauFactory)
                        _factoryContainer.getEntity(actionCommand);
                if (factory != null) {
                    Effigy tableauContainer = (Effigy)_tableau.getContainer();
                    try {
                        factory.createTableau(tableauContainer);
                    } catch (Exception ex) {
                        MessageHandler.error("Cannot create view", ex);
                    }
                }
            }
            // NOTE: The following should not be needed, but jdk1.3beta
            // appears to have a bug in swing where repainting doesn't
            // properly occur.
            repaint();
        }
    }
}
