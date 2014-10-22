/*
 Copyright (c) 1998-2014 The Regents of the University of California
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
 PROVIDED HEREUNDER IS ON AN  BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY
 */
package diva.gui;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.net.URL;

import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEditSupport;

/**
 * An abstract implementation of the Document interface.
 * This class implements the common elements of the Document
 * abstraction, and takes care of listener notification for
 * properties.
 *
 * @author John Reekie
 * @version $Id$
 */
public abstract class AbstractDocument implements Document {
    /** The flag that says whether the content of the document has
     *  changed since the last save.
     */
    private boolean _dirty = false;

    /** The flag that says whether the document is editable.
     */
    private boolean _editable = true;

    /** The flag that says whether the document is writable.
     */
    private boolean _writable = true;

    /** The support object for property changes
     */
    private PropertyChangeSupport _propertyChange;

    /** The application that owns this document
     */
    private Application _application = null;

    /** The file that contains this document's data
     */
    private File _file = null;

    /** The URL that contains this document's data
     */
    private URL _url = null;

    /** The undo manager that maintains a list of edits.
     */
    private UndoManager _undoManager = null;

    private UndoableEditSupport _editSupport = null;

    /** Construct a document that is owned by the given application
     */
    public AbstractDocument(Application a) {
        _application = a;
        _undoManager = new UndoManager();
        _editSupport = new UndoableEditSupport();
    }

    /** Add a property change listener to this document. Changes to
     * certain elements of the state will cause all registered
     * property listeners to be notified.
     */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (_propertyChange == null) {
            _propertyChange = new PropertyChangeSupport(this);
        }

        _propertyChange.addPropertyChangeListener(listener);
    }

    /**
     * Return the undoable edit support.  You need this to post edits.
     */
    @Override
    public UndoableEditSupport getEditSupport() {
        return _editSupport;
    }

    /**
     * Return the undo manager of this document.
     */
    @Override
    public UndoManager getUndoManager() {
        return _undoManager;
    }

    /** Close the document. Do not attempt to save the document first
     * or do any other user-interface things like that. This method
     * must never "fail." Note that actions such as querying the user
     * to save a modified document and so on are the responsibility of the
     * application, not the Document implementation. Return true if
     * the operation was successful, otherwise false.  In this base class
     * do nothing.
     *
     * @exception Exception If the close operation fails.
     */
    @Override
    public void close() throws Exception {
    }

    /** Get the application that this document belongs to.
     */
    @Override
    public Application getApplication() {
        return _application;
    }

    /** Get the file object that this document is associated with.  If
     * the document has multiple file objects, then get the one that
     * can be considered the "primary" one. This field may be null.
     *
     * @see #getURL()
     */
    @Override
    public File getFile() {
        return _file;
    }

    /** Get the short title of this document. By default this is the
     * tail of the filename.
     */
    @Override
    public String getShortTitle() {
        if (getFile() != null) {
            return getFile().getName();
        } else if (getURL() != null) {
            return getURL().getFile();
        } else {
            return "Untitled";
        }
    }

    /** Get the title of this document. By default it is equal to the
     * file or URL path.
     */
    @Override
    public String getTitle() {
        if (getFile() != null) {
            return getFile().getAbsolutePath();
        } else if (getURL() != null) {
            return getURL().toString();
        } else {
            return "Untitled";
        }
    }

    /** Get the URL that this document is associated with.  If the
     * document has multiple URL objects, then get the one that can be
     * considered the "primary" one. This field may be null.
     *
     * @see #getFile()
     */
    @Override
    public URL getURL() {
        return _url;
    }

    /** Test the "dirty" flag.  If changes made to a document
     * haven't been saved, this flag is set to true.
     */
    @Override
    public boolean isDirty() {
        return _dirty;
    }

    /** Test the "editable" flag. In general, editors should only
     * allow a document's data to be changed if this flag is set. This
     * flag is true by default.
     */
    @Override
    public boolean isEditable() {
        return _editable;
    }

    /** Test the "writable" flag. In general, editors should only
     * allow a document's data to be written to storage if this flag
     * is set.  This flag is true by default.
     */
    @Override
    public boolean isWritable() {
        return _writable;
    }

    /** Open the document from its current file or URL. Throw an
     * exception if the operation failed.
     *
     * @exception Exception If the close operation fails.
     */
    @Override
    public abstract void open() throws Exception;

    /** Remove a property change listener from this document.
     */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        _propertyChange.removePropertyChangeListener(listener);
    }

    /** Save the document to its current file or URL.  Throw an
     * exception if the operation failed.
     *
     * @exception Exception If the save operation fails.
     */
    @Override
    public abstract void save() throws Exception;

    /** Save the document to the given file.  Return true if
     * successful, false if not. Do <i>not</i> change the file
     * attribute to the new File object as that is the responsibility
     * of the application, which it will do according to its storage
     * policy.
     *
     * @see #save()
     * @exception Exception If the save-as operation fails.
     */
    @Override
    public abstract void saveAs(File file) throws Exception;

    /** Save the document to the given file.  Throw an exception if
     * the operation failed. Return true if successful, false if
     * not. Do <i>not</i> change the file attribute to the new File
     * object as that is the responsibility of the application, which
     * it will do according to its storage policy.
     *
     * @see #save()
     * @exception Exception If the save-as operation fails.
     */
    @Override
    public abstract void saveAs(URL url) throws Exception;

    /** Set the file that this document saves itself to. This is a
     * low-level method and should only be used by storage policy
     * classes. Fire a property change listener to registered
     * listeners.
     */
    @Override
    public void setFile(File file) {
        File old = _file;
        _file = file;

        if (_propertyChange != null) {
            _propertyChange.firePropertyChange("file", old, file);
        }
    }

    /** Set the URL that this document saves itself to. This is a
     * low-level method and should only be used by storage policy
     * classes. Fire a property change listener to registered
     * listeners.
     */
    @Override
    public void setURL(URL url) {
        URL old = _url;
        _url = url;

        if (_propertyChange != null) {
            _propertyChange.firePropertyChange("url", old, url);
        }
    }

    /** Set the "editable" flag. Fire a property change event
     * to registered listeners.
     */
    @Override
    public void setEditable(boolean flag) {
        boolean old = _editable;
        _editable = flag;

        if (_propertyChange != null) {
            _propertyChange.firePropertyChange("editable", old, flag);
        }
    }

    /** Set the "dirty" flag.  Fire a property change event to
     * registered listeners.
     */
    @Override
    public void setDirty(boolean flag) {
        boolean old = _dirty;
        _dirty = flag;

        if (_propertyChange != null) {
            _propertyChange.firePropertyChange("dirty", old, flag);
        }
    }

    /** Set the "writable" flag. Fire a property change event
     * to registered listeners.
     */
    @Override
    public void setWritable(boolean flag) {
        boolean old = _writable;
        _writable = flag;

        if (_propertyChange != null) {
            _propertyChange.firePropertyChange("writable", old, flag);
        }
    }
}
