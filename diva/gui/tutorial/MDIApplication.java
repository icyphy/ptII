/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.gui.tutorial;

import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JInternalFrame;

import diva.gui.Document;
import diva.gui.MDIContext;
import diva.gui.View;
import diva.gui.ViewAdapter;
import diva.gui.ViewEvent;

/**
 * An abstract superclass for applications that use an MDI (multiple
 * document interface) style of presentation. This class manages the
 * interaction between documents of the application, and the frame
 * that they are displayed in. For example, keeping documents and
 * views in sync, handling focus changes, and so on.
 *
 * The context must implement MDIContext for this to work properly.
 *
 * Note: this application only manages one view per document.
 * If you have multiple views per document, you will need to
 * implement the view-document mapping yourself.
 *
 * @author John Reekie (johnr@eecs.berkeley.edu)
 * @version $Revision$
 */
public abstract class MDIApplication extends AbstractApplication {

    // This class used to be in the parent directory, but only the
    // tutorial needed it, so we moved it. 

    /** A mapping from content panes to views
     */
    private HashMap _viewMap = new HashMap();

    /** Create an MDI application in the given MDI context
     */
    public MDIApplication(MDIContext context) {
        super(context);
        context.addViewListener(new MDIViewListener());
    }

    public void addView(View v) {
        super.addView(v);

        JComponent component = v.getComponent();

        // Add the component to the frame
        getMDIContext().addContentPane(v.getTitle(), component);
        // Yuk we need hash tables to map components to views ek
        _viewMap.put(component, v);
    }

    /** Given a document, create a new view which displays that
     * document. Subclasses must implement this method to
     * create, initialize, and then return a View object that
     * wraps a JComponent.
     */
    public abstract View createView (Document d);

    /** Get the MDI frame -- type-specific version of
     * getApplicationFrame().
     */
    public MDIContext getMDIContext() {
        return (MDIContext) getAppContext();
    }

    /** Get the Document displayed by the given component.
     */
    public View getView (JComponent c) {
        return (View) _viewMap.get(c);
    }

    /** Remove a view from the list of view currently known by this
     * application.  Fire a document list event to registered
     * listeners. Throw an exception if the document is not known.
     * This method assumes that setCurrentView will be called
     * subsequently on another view.
     */
    public void removeView (View v) {
        super.removeView(v);

        // Remove the display.
        JComponent pane = (JComponent) v.getComponent();
        _viewMap.remove(pane);

        //FIXME do this last, to avoid circular loop
        //      with the viewClosing callback
        getMDIContext().removeContentPane(pane);
    }

    /** Set the given document to be the current document, and raise
     * the internal window that corresponds to that component.
     */
    public void setCurrentView (View v) {
        super.setCurrentView(v);
        if(v != null) {
            getMDIContext().setCurrentContentPane(v.getComponent());
        }
    }

    private class MDIViewListener extends ViewAdapter {
        public void viewSelected(ViewEvent e) {
            JComponent jc = e.getView();
            View view = getView(jc);
            // FIXME: for some reason, closing
            //        a view also causes that view
            //        to be selected after it is
            //        closed?
            if(viewList().contains(view)) {
                // Prevent recursion
                if (getCurrentView() != view) {
                    setCurrentView(view);
                }
            }
        }
        public void viewClosing(ViewEvent e) {
            JComponent jc = e.getView();
            View view = getView(jc);
            // FIXME: avoid circular loop with the
            // removeDocument method (if the
            // file is closed from the menu,
            // rather than by clicking the X in
            // the internal pane
            if(viewList().contains(view)) {
                closeView(view);
                //workaround for combobox model bug
                setCurrentView(getCurrentView());
            }
        }
    }
}


