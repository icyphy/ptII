/*
 Copyright (c) 1998-2005 The Regents of the University of California
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
import java.io.File;
import java.net.URL;

import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEditSupport;

/**
 * Document is an interface that captures the notion of a document in
 * a graphical application. Typically, documents are associated with
 * files, although this is not necessarily so.  Each application will
 * typically create one or more implementations of this interface for
 * the types of documents it can work with.
 *
 * @author John Reekie
 * @version $Id$
 */
public interface Document {
    /** Add a property change listener to this document. Changes to
     * certain elements of the state of the Document require that
     * registered property change listeners be notified of the change.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener);

    /** Close the document. Do not attempt to save the document first
     * or do any other user-interface things like that. This method
     * can thrown an exception if there is a failure, but it should
     * only do so if there is no way it can recover. Note that actions
     * such as querying the user to save a modified document and so on
     * are the responsibility of the application, not the Document
     * implementation.
     *
     * @exception Exception If the close operation fails.
     */
    public void close() throws Exception;

    /** Get the application that this document belongs to.
     */
    public Application getApplication();

    /**
     * Return the undoable edit support.  You need this to post edits.
     */
    public UndoableEditSupport getEditSupport();

    /**
     * Return the undo manager of this document.
     */
    public UndoManager getUndoManager();

    /** Get the file object that this document is associated with.  If
     * the document has multiple file objects, then get the one that
     * can be considered the "primary" one. This field may be null.
     *
     * @see #getURL()
     */
    public File getFile();

    /** Get the title of this document
     */
    public String getTitle();

    /** Get the short title of this document. The short title
     * is used in situations where the regular title is likely
     * to be too long, such as iconified windows, menus, and so on.
     */
    public String getShortTitle();

    /** Get the URL that this document is associated with.  If the
     * document has multiple URL objects, then get the one that can be
     * considered the "primary" one. This field may be null.
     *
     * @see #getFile()
     */
    public URL getURL();

    /** Test the "dirty" flag.  If changes made to a document
     * haven't been saved, this flag is set to true.
     */
    public boolean isDirty();

    /** Test the "editable" flag. In general, editors should only allow
     * a document's data to be changed if this flag is set.
     */
    public boolean isEditable();

    /** Test the "writable" flag. In general, editors should only allow
     * a document's data to be written to storage if this flag is set.
     */
    public boolean isWritable();

    /** Open the document from its current file or URL. Throw an
     * exception if the operation failed.
     *
     * @exception Exception If the close operation fails.
     */
    public void open() throws Exception;

    /** Remove a property change listener from this document.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener);

    /** Save the document to its current file or URL.  Throw an
     * exception if the operation failed. Reasons for failure might
     * include the fact that the file is not writable, or that the
     * document has a URL but we haven't implemented HTTP-DAV support
     * yet...
     *
     * @exception Exception If the save operation fails.
     */
    public void save() throws Exception;

    /** Save the document to the given file.  Throw an exception if
     * the operation failed. Return true if successful, false if
     * not. Do <i>not</i> change the file attribute to the new File
     * object as that is the responsibility of the application, which
     * it will do according to its storage policy.
     *
     * @see #save()
     * @exception Exception If the save-as operation fails.
     */
    public void saveAs(File file) throws Exception;

    /** Save the document to the given URL.  Throw an exception if the
     * operation failed.  Do <i>not</i> change the URL attribute to
     * the new URL object as that is the responsibility of the
     * application, which it will do according to its storage policy.
     *
     * @see #save()
     * @exception Exception If the save-as operation fails.
     */
    public void saveAs(URL url) throws Exception;

    /** Set the "editable" flag. Fire a property change event
     * to registered listeners.
     */
    public void setEditable(boolean flag);

    /** Set the file that this document saves itself to. This is a
     * low-level method and should only be used by storage policy
     * classes. Fire a property change listener to registered listeners.
     */
    public void setFile(File file);

    /** Set the "dirty" flag.  Fire a property change event to
     * registered listeners.
     */
    public void setDirty(boolean flag);

    /** Set the URL that this document saves itself to. This is a
     * low-level method and should only be used by storage policy
     * classes. Fire a property change listener to registered listeners.
     */
    public void setURL(URL url);

    /** Set the "writable" flag. Fire a property change event
     * to registered listeners.
     */
    public void setWritable(boolean flag);
}
