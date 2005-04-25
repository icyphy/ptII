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

import diva.gui.toolbox.ListDataModel;

import java.util.Iterator;

import javax.swing.event.ListDataListener;


/**
 * A document that contains a linear sequence of Pages.
 * This class is useful for documents which their data into logical
 * pages. Generally this class is most useful for partitioned documents
 * where all the partitions are stored together.  For partitions that are
 * stored separately, it is probably easiest to just use separate documents.
 * Note that a page can contain any kind of data, and the
 * interpretation and graphical representation of a list of page is
 * up to the concrete document class and the corresponding
 * application.  Other than containing a sequence of pages, this
 * class is used the same as AbstractDocument and provides the same abstract
 * methods.
 *
 * @author John Reekie
 * @version $Id$
 */
public class MultipageModel {
    /** The support object for pages.
     */
    private ListDataModel _pages = new ListDataModel();

    /** The title of this model;
     */
    private String _title = "Untitled";

    /** Add a page to the document and notify page listeners.
     */
    public void addPage(Page p) {
        _pages.addElement(p);
    }

    /** Add a page to the document at the given index (between 0
     * and getPageCount()-1) and notify page listeners.
     */
    public void insertPage(Page p, int index) {
        _pages.insertElementAt(p, index);
    }

    /** Add a page listener to this document. The page listener is
     * in fact a ListDataListener, which will be notified with
     * intervalAdded() and intervalRemoved() events when pages are
     * added or removed, and with a contentsChanged() event when the
     * current page is changed.
     */
    public void addPageListener(ListDataListener listener) {
        _pages.addListDataListener(listener);
    }

    /** Get the current page. Return null if there is no current
     * page, which, provided the Document is implemented correctly,
     * will only happen if the document contains no pages.
     */
    public Page getCurrentPage() {
        return (Page) _pages.getSelectedItem();
    }

    /** Get the page at the given index.
     */
    public Page getPage(int index) {
        return (Page) _pages.getElementAt(index);
    }

    /** Get the page with the given title.
     */
    public Page getPage(String title) {
        for (Iterator i = pages(); i.hasNext();) {
            Page s = (Page) i.next();

            if (s.getTitle().equals(title)) {
                return s;
            }
        }

        return null;
    }

    /** Get the number of pages in this document.
     */
    public int getPageCount() {
        return _pages.getSize();
    }

    /** Return the title of this model
     */
    public String getTitle() {
        return _title;
    }

    /** Get the index of the given page. -1 if it is not known.
     */
    public int indexOf(Page p) {
        return _pages.getIndexOf(p);
    }

    /** Return an iterator over all pages
     */
    public Iterator pages() {
        return _pages.iterator();
    }

    /** Remove the given page from this document and notify listeners.
     * If the removed page is the current page, change the current
     * page to the one after it, unless it was the last one, in which
     * case changed it to the one before, unless this was the last page,
     * in which case there is no selected page.
     */
    public void removePage(Page p) {
        _pages.removeElement(p);
    }

    /** Remove a page listener from this document.
     */
    public void removePageListener(ListDataListener listener) {
        _pages.removeListDataListener(listener);
    }

    /** Set the current page. Notify all page listeners with a
     * contentsChanged() event. Throw an exception if the current
     * page is not in the document.
     */
    public void setCurrentPage(Page p) {
        _pages.setSelectedItem(p);
    }

    /** Set the title of this model to the given title.
     */
    public void setTitle(String title) {
        _title = title;
    }
}
