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

/**
 * A simple concrete implementation of the Page interface.
 *
 * @author Heloise Hse
 * @author John Reekie
 * @version $Id$
 * @Pt.AcceptedRating Red
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
    public BasicPage(MultipageModel multi) {
        _multipage = multi;
    }

    /**
     * Create a basic page owned by the given multipage, and
     * with the given title.
     */
    public BasicPage(MultipageModel multi, String title) {
        _multipage = multi;
        _title = title;
    }

    /**
     * Create a basic page owned by the given multipage, and
     * with the given title and model.
     */
    public BasicPage(MultipageModel multi, String title, Object model) {
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
    @Override
    public Object getModel() {
        return _model;
    }

    /**
     * Return the title of the page.
     */
    @Override
    public String getTitle() {
        return _title;
    }

    /**
     * Set the model of the page.
     */
    @Override
    public void setModel(Object model) {
        _model = model;
    }

    /**
     * Set the title of the page.
     */
    @Override
    public void setTitle(String title) {
        _title = title;
    }
}
