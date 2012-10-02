/*
@Copyright (c) 2010-2011 The Regents of the University of California.
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

package ptdb.gui;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractListModel;

///////////////////////////////////////////////////////////////////
//// ArrayModelList

/**
 * Extends AbstractListModel and implements the function through ArrayList.
 *
 * @author Alek Wang
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating red (wenjiaow)
 * @Pt.AcceptedRating red (wenjiaow)
 *
 */
public class ArrayModelList extends AbstractListModel {

    /**
     * Construct the ArrayModelList through the passed ArrayList object.
     *
     * @param items The passed list instance.
     */
    public ArrayModelList(List<String> items) {

        // Sort the passed items initially.
        Collections.sort(items);

        _items = items;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Add a new item to this list.
     *
     * @param newItem The new item to be added into the list.
     * @return Return the index of the newly added item.
     */
    public int addItem(String newItem) {
        //        int index = _items.size();
        int index = _getInsertIndex(newItem);

        _items.add(index, newItem);

        fireIntervalAdded(this, index, index);

        return index;
    }

    /**
     * Get the item at a certain index.
     *
     * @param i The index of item to be gotten.
     * @return The item at the given index.
     */
    public String getElementAt(int i) {
        return _items.get(i);
    }

    /**
     * Get the size of the stored list.
     *
     * @return The size of the stored list.
     */
    public int getSize() {
        return _items.size();
    }

    /**
     * Remove the given item from the list.
     *
     * @param item The item to be removed from the list.
     */
    public void removeItem(String item) {
        for (Iterator<String> iterator = _items.iterator(); iterator.hasNext();) {
            String existingItem = iterator.next();
            if (existingItem.equals(item)) {
                int index = _items.indexOf(item);

                _items.remove(existingItem);

                fireIntervalRemoved(this, index, index);
                break;
            }
        }
    }

    /**
     * Update the item at a given index.
     *
     * @param item The new item to put into the list.
     * @param index The index of the item to be replaced.
     * @return Return the new index of the updated item.
     */
    public int updateItem(String item, int index) {

        // Remove the original item from the list.
        _items.remove(index);

        //        _items.set(index, item);
        //
        //        fireIntervalAdded(this, index, index);
        // Add the new item into the list.
        return addItem(item);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private int _getInsertIndex(String itemToInsert) {

        int index = 0;

        for (String itemString : _items) {

            if (itemString.compareTo(itemToInsert) >= 0) {
                break;
            }

            index++;
        }

        return index;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private List<String> _items;
}
