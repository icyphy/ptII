/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.gui;

/**
 * A simple concrete implementation of the Page interface.
 *
 * @author Heloise Hse  (hwawen@eecs.berkeley.edu)
 * @author John Reekie  (johnr@eecs.berkeley.edu)
 * @version $Revision$
 * @rating Red
 */
public class BasicPage implements Page {
    /**
     * The owning multipage.
     */
    private MultipageModel _multipage;

    /**
     * The title of the page.
     */
    private String _title;

    /**
     * The model.
     */
    private Object _model;

    /**
     * Create a basic page owned by the given multipage.
     */
    public BasicPage (MultipageModel multi) {
        _multipage = multi;
    }

    /**
     * Create a basic page owned by the given multipage, and
     * with the given title.
     */
    public BasicPage (MultipageModel multi, String title) {
        _multipage = multi;
        _title = title;
    }

    /**
     * Create a basic page owned by the given multipage, and
     * with the given title and model.
     */
    public BasicPage (MultipageModel multi, String title, Object model) {
        this(multi, title);
        _model = model;
    }

    /**
     * Return the multipage that owns this model.
     */
    public MultipageModel getMultipage() {
        return _multipage;
    }

    /**
     * Return the model of the page.
     */
    public Object getModel() {
        return _model;
    }

    /**
     * Return the title of the page.
     */
    public String getTitle() {
        return _title;
    }

    /**
     * Set the model of the page.
     */
    public void setModel(Object model) {
        _model = model;
    }

    /**
     * Set the title of the page.
     */
    public void setTitle(String title){
        _title = title;
    }
}


