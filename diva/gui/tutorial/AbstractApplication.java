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

import java.awt.datatransfer.Clipboard;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;
import javax.swing.event.ListDataListener;

import diva.gui.AppContext;
import diva.gui.Application;
import diva.gui.GUIUtilities;
import diva.gui.DocumentFactory;
import diva.gui.Document;
import diva.gui.StoragePolicy;
import diva.gui.View;
import diva.gui.toolbox.ListDataModel;
import diva.resource.DefaultBundle;
import diva.resource.RelativeBundle;

/**
 * An abstract implementation of the Application interface.  This
 * class implements the common elements of the Application
 * abstraction, and takes care of listener notification for documents
 * and properties. Concrete subclasses will generally override a
 * number of these methods to add their own behavior.
 *
 * Most importantly, this abstract class does not provide any support for
 * creating documents, or managing the views of those documents.  For an
 * example of one way of managing the view associated with a document, see
 * MDIApplication.
 *
 * @author John Reekie (johnr@eecs.berkeley.edu)
 * @version $Revision$
 * @rating Red
 */
public abstract class AbstractApplication implements Application {

    // This class used to be in the parent directory, but only the
    // tutorial needed it, so we moved it. 

    /** The resources object
     */
    private RelativeBundle _resources = new DefaultBundle();

    /** The support object for property changes
     */
    private PropertyChangeSupport _propertyChangeSupport;

    /** The frame that this application displays in
     */
    private AppContext _appContext = null;

    /** The list of documents
     */
    private ListDataModel _documents = new ListDataModel();

    /** The list of views
     */
    private ListDataModel _views = new ListDataModel();

    /** Remembered actions
     */
    private HashMap _actions = new HashMap();

    /** Map documents to their views.
     */
    private HashMap _documentMap = new HashMap();

    /** The factory that creates graph documents
     */
    private DocumentFactory _documentFactory;

    /** Our storage policy
     */
    private StoragePolicy _storagePolicy;

    /** The clipboard
     */
    private Clipboard _clipboard;

    /** Create an abstract application that resides
     * in the given context (e.g. a frame or an applet).
     */
    public AbstractApplication(AppContext context) {
        setAppContext(context);
    }

    /** Get an iterator over the names of the actions that are
     * contained by this application.
     */
    public Iterator actions () {
        return _actions.keySet().iterator();
    }

    /** Add an action to the table of actions. Every action known by
     * the application should be added here so it can be retrieved and
     * invoked later.
     */
    public void addAction (Action action) {
        _actions.put(action.getValue(Action.NAME), action);
    }

    /** Add an action to a menu and return the menu item created.  If
     * the tool tip is null, use the "tooltip" property already in the
     * action, otherwise add the property to the action. (The mnemonic
     * isn't added.)  The new menu item is added to the action as the
     * "menuItem" property.  The menu item's text is set using the
     * action's name and is enabled by default.
     * @deprecated Use method in GUIUtilities instead.
     */
    public JMenuItem addMenuItem (JMenu menu, Action action,
            int mnemonic, String tooltip) {
        return GUIUtilities.addMenuItem(menu, action, mnemonic, tooltip);
    }

    /** Add an action to a menu and return the menu item created.  If
     * the tool tip is null, use the "tooltip" property already in the
     * action, otherwise add the property to the action. (The mnemonic
     * isn't added.)  The new menu item is added to the action as the
     * "menuItem" property.  The menu item's text is set to be "label",
     * and is disabled or enabled according to "isEnabled."
     * @deprecated Use method in GUIUtilities instead.
     */
    public JMenuItem addMenuItem (JMenu menu, String label, Action action,
            int mnemonic, String tooltip, boolean isEnabled) {
        return GUIUtilities.addMenuItem(menu, label, action,
                mnemonic, tooltip, isEnabled);
    }

    /** Add an action to the toolbar.  If the tool tip is null, use
     * the "tooltip" property already in the action, otherwise add the
     * property to the action. The new button is added to the action
     * as the "toolButton" property.  The button is enabled by
     * default.
     * @deprecated Use method in GUIUtilities instead.
     */
    public JButton addToolBarButton (JToolBar toolbar, Action action,
            String tooltip, Icon icon){
        return GUIUtilities.addToolBarButton(toolbar, action, tooltip, icon);
    }

    /** Add an action to the toolbar.  If the tool tip is null, use
     * the "tooltip" property already in the action, otherwise add the
     * property to the action. The new button is added to the action
     * as the "toolButton" property.
     * @deprecated Use method in GUIUtilities instead.
     */
    public JButton addToolBarButton (JToolBar toolbar, Action action,
            String tooltip, Icon icon, boolean isEnabled) {
        return GUIUtilities.addToolBarButton(toolbar, action, tooltip,
                icon, isEnabled);
    }

    /** Add a property change listener to this application. Changes to
     * certain elements of the state will cause all registered
     * property listeners to be notified.
     */
    public void addPropertyChangeListener (PropertyChangeListener listener) {
        if (_propertyChangeSupport == null) {
            _propertyChangeSupport = new PropertyChangeSupport(this);
        }
        _propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /** Add a document to the list of documents currently known by
     * this application. Fire a document list event to registered
     * listeners. Throw an exception if the document is already
     * in the list of documents.
     */
    public void addDocument (Document d) {
        if (_documents.contains(d)) {
            throw new IllegalArgumentException(
                    "Document " + d
                    + " is already known by application " + this);
        }
        _documents.addElement(d);
    }

    /** Add a view to the list of views currently known by
     * this application. Fire a view list event to registered
     * listeners. Throw an exception if the view is already
     * in the list of views.
     */
    public void addView (View v) {
        if (_views.contains(v)) {
            throw new IllegalArgumentException(
                    "View " + v
                    + " is already known by application " + this);
        }
        List l = (List)_documentMap.get(v.getDocument());
        if(l == null) {
            l = new LinkedList();
            _documentMap.put(v.getDocument(), l);
        }
        l.add(v);
        _views.addElement(v);
    }

    /** Add a document listener to this application.  The document
     * listener is in fact a ListDataListener, which will be notified
     * with intervalAdded() and intervalRemoved() events when
     * documents are added or removed, and with a contentsChanged()
     * event when the current document is changed.
     */
    public void addDocumentListener (ListDataListener listener) {
        _documents.addListDataListener(listener);
    }

    /** Add a view listener to this application.  The view
     * listener is in fact a ListDataListener, which will be notified
     * with intervalAdded() and intervalRemoved() events when
     * views are added or removed, and with a contentsChanged()
     * event when the current view is changed.
     */
    public void addViewListener (ListDataListener listener) {
        _views.addListDataListener(listener);
    }

    /**
     * Try to close the given document using the
     * storage policy.
     */
    public boolean closeDocument(Document d) {
        return getStoragePolicy().close(d);
    }

    /**
     * Try to close the given view using the storage policy.
     */
    public boolean closeView(View v) {
        List views = (List)_documentMap.get(v.getDocument());
        if(views.size() > 0 || closeDocument(v.getDocument())) {
            removeView(v);
            return true;
        }
        return false;
    }

    /** Create a view to display the given document.  The document
     * should already be added to the application. After calling this
     * method, most callers should set this view to be the current
     * view.
     */
    public abstract View createView (Document d);

    /** Get list of all document objects known by this
     * application.
     */
    public List documentList () {
        return _documents.getList();
    }

    /** Get an action by name.
     */
    public Action getAction (String name) {
        return (Action)_actions.get(name);
    }

    /** Get the frame that this application draws itself in.
     */
    public AppContext getAppContext () {
        return _appContext;
    }

    /** Get the clipboard object for this application.
     */
    public Clipboard getClipboard () {
        return _clipboard;
    }

    /** Get the current view. Generally, this will be the one that
     * is displayed in the window that is top-most in the display.
     */
    public View getCurrentView () {
        return (View) _views.getSelectedItem();
    }

    /** Get the factory that creates new documents
     */
    public DocumentFactory getDocumentFactory () {
        return _documentFactory;
    }

    /** Get the resources object.
     */
    public RelativeBundle getResources () {
        return _resources;
    }

    /** Get the storage policy of this application.
     */
    public StoragePolicy getStoragePolicy () {
        return _storagePolicy;
    }

    /** Get the title of this application
     */
    public abstract String getTitle();

    /** Test whether the application frame is visible.
     * Return false if the application has no frame or
     * if the frame is not visible.
     */
    public boolean isVisible () {
        if (getAppContext() == null) {
            return false;
        } else {
            return getAppContext().isVisible();
        }
    }

    /** Remove a document from the list of documents currently known
     * by this application, and remove all of the views associated with
     * this document.  Fire a list data event to registered
     * document listeners.  Throw an exception if the document is
     * not known.
     */
    public void removeDocument (Document d) {
        if (!_documents.contains(d)) {
            throw new IllegalArgumentException(
                    "Document " + d
                    + " is not known by application " + this);
        }
        _documents.removeElement(d);
        List views = (List)_documentMap.get(d);
        for(Iterator i = views.iterator(); i.hasNext(); ) {
            View v = (View)i.next();
            i.remove();
            removeView(v);
        }
        _documentMap.remove(d);
    }

    /** Remove a view from the list of views currently known by this
     * application.  Fire a list data event to registered view
     * listeners.  If the removed view is the current view it is up to
     * the application to decide which view to display next. Throw an
     * exception if the view is not known.
     */
    public void removeView (View v) {
        if (!_views.contains(v)) {
            throw new IllegalArgumentException(
                    "View " + v
                    + " is not known by application " + this);
        }
        _views.removeElement(v);
        List views = (List)_documentMap.get(v.getDocument());
        if(views != null) {
            views.remove(v);
        }
    }

    /** Remove a document list listener from this application.
     */
    public void removeDocumentListener (ListDataListener listener) {
        _documents.removeListDataListener(listener);
    }

    /** Remove a view list listener from this application.
     */
    public void removeViewListener (ListDataListener listener) {
        _views.removeListDataListener(listener);
    }

    /** Remove a property change listener from this application.
     */
    public void removePropertyChangeListener (PropertyChangeListener listener) {
        _propertyChangeSupport.removePropertyChangeListener(listener);
    }

    /** Set the clipboard that is used by this application.  This is
     * generally used by subclass constructors.  To use the system
     * clipboard use something like
     * <code>setClipboard(Toolkit.getDefaultToolkit().getSystemClipboard())</code>
     */
    public void setClipboard(Clipboard clipboard) {
        _clipboard = clipboard;
    }

    /** Set the given view to be the current view. Fire a
     * contentsChanged() event to registered view listeners.
     * Throw an exception if the view is not known.
     */
    public void setCurrentView (View v) {
        if (v != null && !_views.contains(v)) {
            throw new IllegalArgumentException(
                    "View " + v
                    + " is not known by application " + this);
        }
        _views.setSelectedItem(v);
    }

    /** Set the factory that creates new documents for use by subclasses
     * constructors only.
     */
    protected void setDocumentFactory(DocumentFactory df) {
        _documentFactory = df;
    }

    /** Set the storage policy of this application, for use by subclass
     * constructors only
     */
    public void setStoragePolicy(StoragePolicy sp) {
        _storagePolicy = sp;
    }

    /** Set the visibility of the application's frame
     */
    public void setVisible (boolean visible) {
        if (getAppContext() != null) {
            getAppContext().setVisible(visible);
        }
    }


    /** Show an error in a dialog box with stack trace.
     */
    public void showError(String op, Exception e) {
        GUIUtilities.showStackTrace(getAppContext().makeComponent(), e,
                "Please submit a bug report.\n\n" + op);
    }

    /** Set the app context that this application draws itself in. For use
     * by subclass constructors only.
     */
    protected void setAppContext(AppContext ac) {
        _appContext = ac;
    }

    /** Get a list of all view objects known by this
     * application.
     */
    public List viewList () {
        return _views.getList();
    }

    /** Get a list of all view objects known by this
     * application.
     */
    public List viewList (Document d) {
        List list = (List)_documentMap.get(d);
        if(list == null)
            list = new LinkedList();
        return list;
    }
}


