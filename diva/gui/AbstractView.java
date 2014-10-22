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

import java.awt.datatransfer.Clipboard;

import javax.swing.JComponent;

/**
 * An abstract implementation of the View interface that consists of
 * mostly empty methods to be filled in by concrete subclasses.
 *
 * @author Michael Shilman
 * @version $Id$
 * @Pt.AcceptedRating Red
 */
public abstract class AbstractView implements View {
    /** The document being viewed.
     */
    private Document _document;

    /**
     * Construct a view of the given document.
     */
    public AbstractView(Document doc) {
        _document = doc;
    }

    /** Close the view.
     * @exception Exception If the close operation fails.
     */
    @Override
    public void close() throws Exception {
    }

    /** Get the currently selected objects from this view, if any,
     * and place them on the given clipboard.  If the view does not
     * support such an operation, then do nothing.
     */
    @Override
    public void copy(Clipboard c) {
    }

    /** Remove the currently selected objects from this view, if any,
     * and place them on the given clipboard.  If the view does not
     * support such an operation, then do nothing.
     */
    @Override
    public void cut(Clipboard c) {
    }

    /** Return the component that implements the display of this view.
     * The returned object should be a unique object that corresponds
     * to this view.
     */
    @Override
    public abstract JComponent getComponent();

    /** Get the document that this view is viewing.
     */
    @Override
    public Document getDocument() {
        return _document;
    }

    /** Get the title of this document
     */
    @Override
    public abstract String getTitle();

    /** Get the short title of this document. The short title
     * is used in situations where the regular title is likely
     * to be too long, such as iconified windows, menus, and so on.
     */
    @Override
    public abstract String getShortTitle();

    /** Clone the objects currently on the clipboard, if any, and
     * place them in the given view.  If the document does not support
     * such an operation, then do nothing.  This method is responsible
     * for copying the data.
     */
    @Override
    public void paste(Clipboard c) {
    }
}
