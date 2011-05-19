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

import java.awt.datatransfer.Clipboard;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.List;

import javax.swing.Action;
import javax.swing.event.ListDataListener;

/**
 * Application is an interface that captures the notion of a graphical
 * application. An application is associated with an ApplicationFrame,
 * and contains a set of Documents. All applications will need to
 * create an implementation of this interface.
 *
 * @author John Reekie
 * @version $Id$
 * @Pt.AcceptedRating Red
 */
public interface Application {
    /** Get an iterator over the names of the actions that are
     * contained by this application.
     */
    public Iterator actions();

    /** Add an action to the table of actions. Every action known by
     * the application should be added to this list so it can be
     * retrieved and invoked later. The action name must be unique in
     * this application.
     */
    public void addAction(Action action);

    /** Add a document to the application and notify document
     * listeners. Throw an exception if the document is already in the
     * list of documents.
     */
    public void addDocument(Document d);

    /** Add a document listener to this application. The document
     * listener is in fact a ListDataListener, which will be notified
     * with intervalAdded() and intervalRemoved() events when
     * documents are added or removed, and with a contentsChanged()
     * event when the current document is changed.
     */
    public void addDocumentListener(ListDataListener listener);

    /** Add a view to the application and notify view
     * listeners. Throw an exception if the view is already in the
     * list of views.
     */
    public void addView(View d);

    /** Add a view listener to this application. The view
     * listener is in fact a ListDataListener, which will be notified
     * with intervalAdded() and intervalRemoved() events when
     * views are added or removed, and with a contentsChanged()
     * event when the current view is changed.
     */
    public void addViewListener(ListDataListener listener);

    /** Add a property change listener to this application. Changes to
     * certain elements of the state of the application require that
     * registered property change listeners be notified of the change.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Try to close the given document, respecting the
     * storage policy.  Return whether or not the operation
     * was successful.
     */
    public boolean closeDocument(Document d);

    /**
     * Try to close the given view, respecting the
     * storage policy.  Return whether or not the operation
     * was successful.
     */
    public boolean closeView(View v);

    /** Create a view to display the given document.  The document
     * should already be added to the application. After calling this
     * method, most callers should set this view to be the current
     * view.
     */
    public View createView(Document d);

    /** Get an iterator over all documents
     */
    public List documentList();

    /** Get an iterator over all views
     */
    public List viewList();

    /** Get an action by name.
     */
    public Action getAction(String name);

    /** Get the current view. Generally, this will be the one that is
     * displayed in the window that is top-most in the display.
     * Return null if there is no current document.
     */
    public View getCurrentView();

    /** Return the clipboard used by this application.
     */
    public Clipboard getClipboard();

    /** Get the factory that this application uses to create document
     * objects.
     */
    public DocumentFactory getDocumentFactory();

    /** Get the context that this application draws itself in. Some
     * applications may have more one than one frame, in which case
     * they probably want to designate one as the "main frame" (heh
     * heh).
     */
    public AppContext getAppContext();

    /** Get the storage policy of this application.
     */
    public StoragePolicy getStoragePolicy();

    /** Get the title of this application
     */
    public String getTitle();

    /** Test whether the application frame is visible.
     * Return false if the application has no frame or
     * if the frame is not visible.
     */
    public boolean isVisible();

    /** This method should only be called by storage policies.  It
     * removes a document from the list of documents currently known
     * by this application, and remove all of the views associated
     * with this document.  Fire a list data event to registered
     * document listeners.  Throw an exception if the document is not
     * known.
     */
    public void removeDocument(Document d);

    /** This method should only be called by storage policies.  It
     * removes a view from the list of views currently known by this
     * application.  Fire a list data event to registered view
     * listeners.  If the removed view is the current view it is up to
     * the application to decide which view to display next. Throw an
     * exception if the view is not known.
     */
    public void removeView(View v);

    /** Remove a document list listener from this application.
     */
    public void removeDocumentListener(ListDataListener listener);

    /** Remove a view list listener from this application.
     */
    public void removeViewListener(ListDataListener listener);

    /** Remove a property change listener from this application.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener);

    /** Set the given view to be the current view. Fire a
     * contentsChanged() event to registered view listeners.
     * Throw an exception of the view is not known.
     */
    public void setCurrentView(View v);

    /** Set the visibility of the application's frame
     */
    public void setVisible(boolean visible);

    /** Report that an exception occurred to the user.
     */
    public void showError(String op, Exception e);
}
