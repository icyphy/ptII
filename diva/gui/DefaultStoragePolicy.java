/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.gui;

import java.awt.Component;
import java.io.File;
import java.net.URL;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * A Default storage policy that is useful for most applications.  This
 * policy is somewhat intelligent about preventing the user from doing
 * something stupid (like closing the document without saving).
 *
 * @author John Reekie (johnr@eecs.berkeley.edu)
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Revision$
 */
public class DefaultStoragePolicy extends AbstractStoragePolicy {

    /** The file chooser that is used to open files.  This can be
     * accessed with the getOpenFileChooser() methods so that
     * applications can add/manipulate file filters.
     */
    private JFileChooser _openFileChooser = new JFileChooser();

    /** The file chooser that is used to save files.  This can be
     * accessed with the getSaveFileChooser() methods so that
     * applications can add/manipulate file filters.
     */
    private JFileChooser _saveFileChooser = new JFileChooser();

    /** Close the document.  Forward the request to the document.  Do
     * nothing if the document is null.  If the file is sucessfully saved and
     * closed, then return true, otherwise return false.
     */
    public boolean close (Document d) {
        if (d != null) {
            try {
                d.close();
                if(d.isDirty()){
                    // ask if the user wants to save
                    Component parent =
                        d.getApplication().getAppContext().makeComponent();
                    String message = "Do you want to save your changes to "
                        + d.getTitle();
                    String title = "Closing Document";
                    int val =
                        JOptionPane.showConfirmDialog(parent, message, title,
                                JOptionPane.YES_NO_CANCEL_OPTION,
                                JOptionPane.WARNING_MESSAGE);
                    if(val == JOptionPane.YES_OPTION){
                        if(save(d)) {
                            // If we successfully saved,
                            // then remove the document
                            d.getApplication().removeDocument(d);
                        } else {
                            return false;
                        }
                    } else if(val == JOptionPane.NO_OPTION){
                        d.getApplication().removeDocument(d);
                    } else {
                        // cancel and do nothing
                        return false;
                    }
                } else {
                    // otherwise the document hasn't been changed
                    // since the last save, so just remove it.
                    d.getApplication().removeDocument(d);
                }
            } catch (Exception e) {
                d.getApplication().showError("Close document failed.", e);
                return false;
            }
        }
        return true;
    }

    /** Get the open file chooser used by this storage policy. This allows
     * the application to set file filters on the chooser.
     */
    public JFileChooser getOpenFileChooser () {
        return _openFileChooser;
    }

    /** Get the open file chooser used by this storage policy.
     *
     * @deprecated Use getOpenFileChooser() or getSaveFileChooser()
     */
    public JFileChooser getFileChooser () {
        return _openFileChooser;
    }

    /** Get the save file chooser used by this storage policy. This allows
     * the application to set file filters on the chooser.
     */
    public JFileChooser getSaveFileChooser () {
        return _saveFileChooser;
    }

    /** Open a new document. Open a file chooser and open the selected
     * file using the application's document factory.  Return the new
     * document if one was created, otherwise null.
     */
    public Document open (Application app) {
        int result;
        Document doc;

        _openFileChooser.setCurrentDirectory(new File(getDirectory()));
        result = _openFileChooser.showOpenDialog(
                app.getAppContext().makeComponent());
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                File file = _openFileChooser.getSelectedFile();
                doc = this.open(file, app);
            } catch (Exception e) {
                app.showError("File open failed.", e);
                return null;
            }
            return doc;
        } else {
            return null;
        }
    }

    /** Open a file and create a new document.  Convert the file reference
     * to canonical form in the process.   Return the new
     * document if one was created, otherwise null.
     */
    public Document open (File file, Application app) {
        Document doc;
        try {
            File canonical = file.getCanonicalFile();
            setDirectory(canonical);
            doc = app.getDocumentFactory().createDocument(app, canonical);
            doc.open();
        } catch (Exception e) {
            app.showError("File open failed.", e);
            return null;
        }
        return doc;
    }

    /** Open a URL and create a new document. Return the new document
     * if one was created, otherwise null.
     */
    public Document open (URL url, Application app) {
        Document doc;
        try {
            doc = app.getDocumentFactory().createDocument(app,url);
            doc.open();
        } catch (Exception e) {
            app.showError("URL open failed.", e);
            return null;
        }
        return doc;
    }

    /** Save the document. Forward the request to the document.  Do
     * nothing if the document is null. Always return true, unless an
     * I/O exception occured.
     */
    public boolean save (Document d) {
        if (d != null) {
            if(d.getFile() == null) {
                return saveAs(d);
            }
            else {
                try {
                    d.save();
                }
                catch (Exception e) {
                    d.getApplication().showError("Save document failed.", e);
                    return false;
                }
                d.getApplication().getAppContext().showStatus(
                        "Saved " + d.getTitle());
                d.setDirty(false);
            }
        }
        return true;
    }

    /** Save the document to a user-specified location. Open a file
     * chooser and forward the request to the document. Don't change
     * the document's file object.  Do nothing if the document is
     * null. Return true if successul, otherwise false.
     */
    public boolean saveAs (Document d) {
        if (d != null) {
            int result;
            Application app = d.getApplication();
            AppContext context = app.getAppContext();

            // Open a chooser dialog
            _saveFileChooser.setCurrentDirectory(new File(getDirectory()));
            result = _saveFileChooser.showSaveDialog(context.makeComponent());
            if (result == JFileChooser.APPROVE_OPTION) {
                File chosenFile = _saveFileChooser.getSelectedFile();
                setDirectory(chosenFile);
                if (chosenFile.exists()) {
                    // Query on overwrite
                    int opt = JOptionPane.showConfirmDialog(
                            context.makeComponent(),
                            "File \"" + chosenFile.getName() +
                            "\" exists. Overwrite?", "Overwrite file?",
                            JOptionPane.YES_NO_OPTION);
                    if (opt != JOptionPane.YES_OPTION) {
                        context.showStatus("File not saved");
                        return false;
                    }
                }
                try {
                    d.saveAs(chosenFile);
                } catch (Exception e) {
                    app.showError("Save document failed.", e);
                    return false;
                }
                d.setFile(chosenFile);
                context.showStatus("Saved " + d.getTitle());
                d.setDirty(false);
                return true;
            } else {
                return false;
            }
        }
        return true;
    }
}


